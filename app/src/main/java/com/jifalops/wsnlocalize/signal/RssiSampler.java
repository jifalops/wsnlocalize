package com.jifalops.wsnlocalize.signal;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.RssiReaderWriter;
import com.jifalops.wsnlocalize.file.WindowReaderWriter;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.RssiRequest;
import com.jifalops.wsnlocalize.request.WindowRequest;
import com.jifalops.wsnlocalize.toolbox.util.ResettingList;
import com.jifalops.wsnlocalize.toolbox.util.SimpleLog;
import com.jifalops.wsnlocalize.toolbox.wifi.WifiScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class RssiSampler {
    public static final int LOG_IMPORTANT = 3;
    public static final int LOG_INFORMATIVE = 2;
    public static final int LOG_ALL = 1;

    public static class Device {
        public final int id;
        public final String mac, desc;
        public boolean isDistanceKnown;
        public Device(int id, String mac, String desc) {
            this.id = id;
            this.mac = mac;
            this.desc = desc;
        }
        @Override
        public String toString() {
            return id + ": " + desc + " " + mac;
        }
    }

    ResettingList<RssiRecord> bt, btle, wifi, wifi5g;
    RssiHelper rssiHelper;
    WindowHelper windowHelper;
    SampleHelper sampleHelper;
    boolean rssiLoaded, windowsLoaded, samplesLoaded,
            collectEnabled, shouldUseBt, shouldUseBtle, shouldUseWifi, shouldUseWifi5g;

    final List<Device> devices = new ArrayList<>();
    double distance;

    final SimpleLog log = new SimpleLog();

    BtBeacon btBeacon;
    BtLeBeacon btleBeacon;
    WifiScanner wifiScanner;

    private static RssiSampler instance;
    public static RssiSampler getInstance(Context ctx) {
        if (instance == null) instance = new RssiSampler(ctx.getApplicationContext());
        return instance;
    }
    private RssiSampler(Context ctx) {
        btBeacon = BtBeacon.getInstance(ctx);
        btleBeacon = BtLeBeacon.getInstance(ctx);
        wifiScanner = new WifiScanner(ctx);

        rssiHelper = new RssiHelper(new RssiHelper.RssiCallback() {
            @Override
            public void onRssiLoaded() {
                addEvent(LOG_IMPORTANT, "RSSI Loaded from disk.");
                rssiLoaded = true;
                checkIfLoadComplete();
            }
        });

        windowHelper = new WindowHelper(new WindowHelper.WindowCallback() {
            @Override
            public void onWindowsLoaded() {
                addEvent(LOG_IMPORTANT, "Windows Loaded from disk.");
                windowsLoaded = true;
                checkIfLoadComplete();
            }
        });

        sampleHelper = new SampleHelper(new SampleHelper.SamplesCallback() {
            @Override
            public void onSamplesLoaded() {
                addEvent(LOG_IMPORTANT, "Samples Loaded from disk.");
                samplesLoaded = true;
                checkIfLoadComplete();
            }
        });

        bt = new ResettingList<>(App.btWindowTrigger, new ResettingList.LimitsCallback<RssiRecord>() {
            @Override
            public void onLimitsReached(List<RssiRecord> list, long time) {
                rssiHelper.addBt(list);
                WindowRecord w = new WindowRecord(list);
                windowHelper.addBt(w);
                double[] sample = w.toSample();
                sampleHelper.addBt(sample);
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "BT sample created from %d RSSI in %.1fs.", list.size(), time/1000f));
                for (SamplerListener l : listeners) l.onSampleAdded(App.SIGNAL_BT, sample);
            }
        });
        btle = new ResettingList<>(App.btleWindowTrigger, new ResettingList.LimitsCallback<RssiRecord>() {
            @Override
            public void onLimitsReached(List<RssiRecord> list, long time) {
                rssiHelper.addBtle(list);
                WindowRecord w = new WindowRecord(list);
                windowHelper.addBtle(w);
                double[] sample = w.toSample();
                sampleHelper.addBtle(sample);
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "BTLE sample created from %d RSSI in %.1fs.", list.size(), time / 1000f));
                for (SamplerListener l : listeners) l.onSampleAdded(App.SIGNAL_BTLE, sample);
            }
        });
        wifi = new ResettingList<>(App.wifiWindowTrigger, new ResettingList.LimitsCallback<RssiRecord>() {
            @Override
            public void onLimitsReached(List<RssiRecord> list, long time) {
                rssiHelper.addWifi(list);
                WindowRecord w = new WindowRecord(list);
                windowHelper.addWifi(w);
                double[] sample = w.toSample();
                sampleHelper.addWifi(sample);
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "WIFI sample created from %d RSSI in %.1fs.", list.size(), time / 1000f));
                for (SamplerListener l : listeners) l.onSampleAdded(App.SIGNAL_WIFI, sample);
            }
        });
        wifi5g = new ResettingList<>(App.wifiWindowTrigger, new ResettingList.LimitsCallback<RssiRecord>() {
            @Override
            public void onLimitsReached(List<RssiRecord> list, long time) {
                rssiHelper.addWifi5g(list);
                WindowRecord w = new WindowRecord(list);
                windowHelper.addWifi5g(w);
                double[] sample = w.toSample();
                sampleHelper.addWifi5g(sample);
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "WIFI5G sample created from %d RSSI in %.1fs.", list.size(), time / 1000f));
                for (SamplerListener l : listeners) l.onSampleAdded(App.SIGNAL_WIFI5G, sample);
            }
        });
    }

    void checkIfLoadComplete() {
        if (rssiLoaded && windowsLoaded && samplesLoaded) {
            for (SamplerListener l : listeners)
                l.onDataLoadedFromDisk(rssiHelper, windowHelper, sampleHelper);
        }
    }

    public SimpleLog getLog() {
        return log;
    }

    public void setDistance(double d) {
        distance = d;
    }

    public double getDistance() {
        return distance;
    }

    public void close() {
        instance = null;
    }

    public void clearPendingSendLists() {
        rssiHelper.btRW.truncate();
        rssiHelper.btleRW.truncate();
        rssiHelper.wifiRW.truncate();
        rssiHelper.wifi5gRW.truncate();
        windowHelper.btRW.truncate();
        windowHelper.btleRW.truncate();
        windowHelper.wifiRW.truncate();
        windowHelper.wifi5gRW.truncate();
    }

    public void clearSamples() {
        sampleHelper.btRW.truncate();
        sampleHelper.btleRW.truncate();
        sampleHelper.wifiRW.truncate();
        sampleHelper.wifi5gRW.truncate();
    }

    public void send() {
        send(App.SIGNAL_BT, rssiHelper.getBt(), windowHelper.getBt(),
                rssiHelper.btRW, windowHelper.btRW);
        send(App.SIGNAL_BTLE, rssiHelper.getBtle(), windowHelper.getBtle(),
                rssiHelper.btleRW, windowHelper.btleRW);
        send(App.SIGNAL_WIFI, rssiHelper.getWifi(), windowHelper.getWifi(),
                rssiHelper.wifiRW, windowHelper.wifiRW);
        send(App.SIGNAL_WIFI5G, rssiHelper.getWifi5g(), windowHelper.getWifi5g(),
                rssiHelper.wifi5gRW, windowHelper.wifi5gRW);
        addEvent(LOG_IMPORTANT, "RSSI and Windows sent.");
    }

    private void send(final String signal, final List<RssiRecord> rssi, final List<WindowRecord> windows,
            final RssiReaderWriter rssiRW, final WindowReaderWriter windowRW) {

        final int rssiSize = rssi.size(), windowSize = windows.size();

        if (rssiSize > 0) {
            final List<RssiRecord> toSend = new ArrayList<>(rssi);
            rssi.clear();
            App.sendRequest(new RssiRequest(signal, toSend,
                    new Response.Listener<AbsRequest.MyResponse>() {
                        @Override
                        public void onResponse(AbsRequest.MyResponse response) {
                            if (response.responseCode == 200) {
                                rssiRW.truncate();
                                for (SamplerListener l : listeners)
                                    l.onSentSuccess(signal, App.DATA_RSSI, rssiSize);
                            } else {
                                rssi.addAll(toSend);
                                for (SamplerListener l : listeners)
                                    l.onSentFailure(signal, App.DATA_RSSI, rssiSize,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    rssi.addAll(toSend);
                    for (SamplerListener l : listeners)
                        l.onSentFailure(signal, App.DATA_RSSI, rssiSize,
                            volleyError.toString());
                }
            }));
        }
        if (windowSize > 0) {
            final List<WindowRecord> toSend = new ArrayList<>(windows);
            windows.clear();
            App.sendRequest(new WindowRequest(signal, toSend,
                    new Response.Listener<AbsRequest.MyResponse>() {
                        @Override
                        public void onResponse(AbsRequest.MyResponse response) {
                            if (response.responseCode == 200) {
                                windowRW.truncate();
                                for (SamplerListener l : listeners)
                                    l.onSentSuccess(signal, App.DATA_WINDOW, windowSize);
                            } else {
                                windows.addAll(toSend);
                                for (SamplerListener l : listeners)
                                    l.onSentFailure(signal, App.DATA_WINDOW, windowSize,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    windows.addAll(toSend);
                    for (SamplerListener l : listeners)
                        l.onSentFailure(signal, App.DATA_WINDOW, windowSize, volleyError.toString());
                }
            }));
        }
    }

    public List<Device> getDevices() {
        return devices;
    }

    public Device getDevice(int index) {
        return devices.get(index);
    }

    public void resetKnownDistances() {
        for (Device d : devices) {
            d.isDistanceKnown = false;
        }
    }

    private Device getDevice(String mac, String desc) {
        Device device = null;
        for (Device d : devices) {
            if (d.mac.equals(mac)) {
                device = d;
                break;
            }
        }
        if (device == null) {
            device = new Device(devices.size()+1, mac, desc);
            devices.add(device);
            addEvent(LOG_INFORMATIVE, "Found new device, " + device.id);
            for (SamplerListener l : listeners) l.onDeviceFound(device);
        }
        return device;
    }

    private void enableBt() {
        btBeacon.registerListener(btBeaconListener);
        btBeacon.startBeaconing();
    }
    private void disableBt() {
        btBeacon.stopBeaconing();
        btBeacon.unregisterListener(btBeaconListener);
        bt.reset();
    }
    private void enableBtle() {
        btleBeacon.registerListener(btLeBeaconListener);
        btleBeacon.startBeaconing();
    }
    private void disableBtle() {
        btleBeacon.stopBeaconing();
        btleBeacon.unregisterListener(btLeBeaconListener);
        btle.reset();
    }
    private void enableWifi() {
        wifiScanner.registerListener(wifiScanListener);
        wifiScanner.startScanning(100);
    }
    private void disableWifi() {
        wifiScanner.stopScanning();
        wifiScanner.unregisterListener(wifiScanListener);
        wifi.reset();
        wifi5g.reset();
    }

    public void setShouldUseBt(boolean use) {
        if (shouldUseBt == use) return;
        shouldUseBt = use;
        if (use && collectEnabled) enableBt();
        else if (!use && collectEnabled) disableBt();
    }
    public void setShouldUseBtle(boolean use) {
        if (shouldUseBtle == use) return;
        shouldUseBtle = use;
        if (use && collectEnabled) enableBtle();
        else if (!use && collectEnabled) disableBtle();
    }
    public void setShouldUseWifi(boolean use) {
        if (shouldUseWifi == use) return;
        shouldUseWifi = use;
        if (use && collectEnabled && !shouldUseWifi5g) enableWifi();
        else if (!use && collectEnabled && !shouldUseWifi5g) disableWifi();
    }

    public void setShouldUseWifi5g(boolean use) {
        if (shouldUseWifi5g == use) return;
        shouldUseWifi5g = use;
        if (use && collectEnabled && !shouldUseWifi) enableWifi();
        else if (!use && collectEnabled && !shouldUseWifi) disableWifi();
    }

    public void setCollectEnabled(boolean enabled) {
        if (collectEnabled == enabled) return;
        collectEnabled = enabled;
        if (enabled) {
            if (shouldUseBt) enableBt();
            if (shouldUseBtle) enableBtle();
            if (shouldUseWifi || shouldUseWifi5g) enableWifi();
        } else {
            if (shouldUseBt) disableBt();
            if (shouldUseBtle) disableBtle();
            if (shouldUseWifi || shouldUseWifi5g) disableWifi();
        }
    }

    public boolean getShouldUseBt() { return shouldUseBt; }
    public boolean getShouldUseBtle() { return shouldUseBtle; }
    public boolean getShouldUseWifi() { return shouldUseWifi; }
    public boolean getShouldUseWifi5g() { return shouldUseWifi5g; }
    public boolean getCollectEnabled() { return collectEnabled; }

    void addEvent(int level, String msg) {
        log.add(level, msg);
        for (SamplerListener l : listeners) l.onMessageLogged(level, msg);
    }

    void addRecord(Device device, String signal, int rssi, int freq) {
        if (collectEnabled) {
            if (device.isDistanceKnown && rssi < 0 && distance > 0) {
                addEvent(LOG_INFORMATIVE, "Device " + device.id + ": " +
                        rssi + " dBm (" + freq + " MHz) at " +
                        distance + "m (" + signal + ").");
//                String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(new Date());
                RssiRecord record = new RssiRecord(device.mac, rssi, freq,
                        System.currentTimeMillis(), distance);
                switch (signal) {
                    case App.SIGNAL_BT:
                        bt.add(record);
                        break;
                    case App.SIGNAL_BTLE:
                        btle.add(record);
                        break;
                    case App.SIGNAL_WIFI:
                        wifi.add(record);
                        break;
                    case App.SIGNAL_WIFI5G:
                        wifi5g.add(record);
                        break;
                }
                for (SamplerListener l : listeners) l.onRecordAdded(signal, device, record);
            } else {
                addEvent(LOG_ALL, "Ignoring " + rssi + " dBm (" + freq + " MHz) for device " +
                        device.id + " (" + signal + ").");
            }
        }
    }

    public int getCount(String signal, String data) {
        switch (signal) {
            case App.SIGNAL_BT:
                switch (data) {
                    case App.DATA_RSSI:     return rssiHelper.getBt().size();
                    case App.DATA_WINDOW:   return windowHelper.getBt().size();
                    case App.DATA_SAMPLES:  return sampleHelper.getBt().size();
                }
                break;
            case App.SIGNAL_BTLE:
                switch (data) {
                    case App.DATA_RSSI:     return rssiHelper.getBtle().size();
                    case App.DATA_WINDOW:   return windowHelper.getBtle().size();
                    case App.DATA_SAMPLES:  return sampleHelper.getBtle().size();
                }
                break;
            case App.SIGNAL_WIFI:
                switch (data) {
                    case App.DATA_RSSI:     return rssiHelper.getWifi().size();
                    case App.DATA_WINDOW:   return windowHelper.getWifi().size();
                    case App.DATA_SAMPLES:  return sampleHelper.getWifi().size();
                }
                break;
            case App.SIGNAL_WIFI5G:
                switch (data) {
                    case App.DATA_RSSI:     return rssiHelper.getWifi5g().size();
                    case App.DATA_WINDOW:   return windowHelper.getWifi5g().size();
                    case App.DATA_SAMPLES:  return sampleHelper.getWifi5g().size();
                }
                break;
        }
        return 0;
    }
    
    
    final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addRecord(getDevice(device.getAddress(), device.getName() + " (BT)"),
                    App.SIGNAL_BT, rssi, 2400);
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {
            addEvent(LOG_INFORMATIVE, "BT Discoverability changed to " + discoverable);
        }

        @Override
        public void onDiscoveryStarting() {
            addEvent(LOG_ALL, "Scanning for BT devices...");
        }
    };

    final BtLeBeacon.BtLeBeaconListener btLeBeaconListener = new BtLeBeacon.BtLeBeaconListener() {
        @Override
        public void onAdvertiseNotSupported() {
            addEvent(LOG_IMPORTANT, "BTLE advertisement not supported on this device.");
        }

        @Override
        public void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect) {
            addEvent(LOG_IMPORTANT, "BTLE advertising started at " +
                    settingsInEffect.getTxPowerLevel() + " dBm.");
        }

        @Override
        public void onAdvertiseStartFailure(int errorCode, String errorMsg) {
            addEvent(LOG_IMPORTANT, "BTLE advertisements failed to start (" + errorCode + "): " + errorMsg) ;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            addEvent(LOG_ALL, "Received " + results.size() + " batch scan results (BTLE).");
            for (ScanResult sr : results) {
                handleScanResult(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode, String errorMsg) {
            addEvent(LOG_IMPORTANT, "BT-LE scan failed (" + errorCode + "): " + errorMsg);
        }

        void handleScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                addRecord(getDevice(device.getAddress(), device.getName() + " (BTLE)"),
                        App.SIGNAL_BTLE, result.getRssi(), 2400);
            } else {
                addEvent(LOG_INFORMATIVE, "BTLE received " + result.getRssi() + " dBm from null device.");
            }
        }
    };

    final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            addEvent(LOG_ALL, "WiFi found " + scanResults.size() + " results.");
            for (android.net.wifi.ScanResult r : scanResults) {
                if (r.frequency < 4000 && shouldUseWifi) {
                    addRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                            App.SIGNAL_WIFI, r.level, r.frequency);
                } else if (r.frequency > 4000 && shouldUseWifi5g) {
                    addRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                            App.SIGNAL_WIFI5G, r.level, r.frequency);
                }

            }
        }
    };

    public interface SamplerListener {
        void onMessageLogged(int level, String msg);
        void onDeviceFound(Device device);
        void onDataLoadedFromDisk(RssiHelper rssiHelper, WindowHelper windowHelper, SampleHelper sampleHelper);
        void onRecordAdded(String signal, Device device, RssiRecord r);
        void onSampleAdded(String signal, double[] sample);
        void onSentSuccess(String signal, String dataType, int count);
        void onSentFailure(String signal, String dataType, int count, int respCode, String resp, String result);
        void onSentFailure(String signal, String dataType, int count, String volleyError);
    }
    private final List<SamplerListener> listeners = new ArrayList<>(1);
    public boolean registerListener(SamplerListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(SamplerListener l) {
        return listeners.remove(l);
    }
}
