package com.jifalops.wsnlocalize.signal;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.jifalops.toolbox.util.ResettingList;
import com.jifalops.toolbox.util.SimpleLog;
import com.jifalops.toolbox.wifi.WifiScanner;
import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;

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

    public void send() { //TODO
    }

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
    SampleHelper sampleHelper;
    RssiHelper rssiHelper;
    boolean collectEnabled, shouldUseBt, shouldUseBtle, shouldUseWifi, shouldUseWifi5g;

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
                for (SamplerListener l : listeners) l.onRssiLoadedFromDisk();
            }
        });

        sampleHelper = new SampleHelper(new SampleHelper.SamplesCallback() {
            @Override
            public void onSamplesLoaded() {
                addEvent(LOG_IMPORTANT, "Samples Loaded from disk.");
                for (SamplerListener l : listeners) l.onSamplesLoadedFromDisk();
            }
        });

        bt = new ResettingList<>(App.btWindowTrigger, new ResettingList.LimitsCallback<RssiRecord>() {
            @Override
            public void onLimitsReached(List<RssiRecord> list, long time) {
                rssiHelper.addBt(list);
                sampleHelper.addBt(new WindowRecord(list).toSample());
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "BT sample created from %d RSSI in %.1fs.", list.size(), time/1000f));
                for (SamplerListener l : listeners) l.onBtSampleAdded();
            }
        });
        btle = new ResettingList<>(App.btleWindowTrigger, new ResettingList.LimitsCallback<RssiRecord>() {
            @Override
            public void onLimitsReached(List<RssiRecord> list, long time) {
                rssiHelper.addBtle(list);
                sampleHelper.addBtle(new WindowRecord(list).toSample());
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "BTLE sample created from %d RSSI in %.1fs.", list.size(), time / 1000f));
                for (SamplerListener l : listeners) l.onBtleSampleAdded();
            }
        });
        wifi = new ResettingList<>(App.wifiWindowTrigger, new ResettingList.LimitsCallback<RssiRecord>() {
            @Override
            public void onLimitsReached(List<RssiRecord> list, long time) {
                rssiHelper.addWifi(list);
                sampleHelper.addWifi(new WindowRecord(list).toSample());
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "WIFI sample created from %d RSSI in %.1fs.", list.size(), time / 1000f));
                for (SamplerListener l : listeners) l.onWifiSampleAdded();
            }
        });
        wifi5g = new ResettingList<>(App.wifiWindowTrigger, new ResettingList.LimitsCallback<RssiRecord>() {
            @Override
            public void onLimitsReached(List<RssiRecord> list, long time) {
                rssiHelper.addWifi5g(list);
                sampleHelper.addWifi5g(new WindowRecord(list).toSample());
                addEvent(LOG_IMPORTANT, String.format(Locale.US,
                        "WIFI5G sample created from %d RSSI in %.1fs.", list.size(), time / 1000f));
                for (SamplerListener l : listeners) l.onWifi5gSampleAdded();
            }
        });
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
        //TODO
//        bt.clearPendingSendLists();
//        btle.clearPendingSendLists();
//        wifi.clearPendingSendLists();
//        wifi5g.clearPendingSendLists();
    }

    public void clearTrainingSamples() {
        //TODO not available for now (would be bad accident)
//        bt.clearTrainingSamples();
//        btle.clearTrainingSamples();
//        wifi.clearTrainingSamples();
//        wifi5g.clearTrainingSamples();
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
                if (signal.equals(App.SIGNAL_BT)) {
                    bt.add(record);
                } else if (signal.equals(App.SIGNAL_BTLE)) {
                    btle.add(record);
                } else if (signal.equals(App.SIGNAL_WIFI)) {
                    wifi.add(record);
                } else if (signal.equals(App.SIGNAL_WIFI5G)) {
                    wifi5g.add(record);
                }
                for (SamplerListener l : listeners) l.onRecordAdded(signal, device, record);
            } else {
                addEvent(LOG_ALL, "Ignoring " + rssi + " dBm (" + freq + " MHz) for device " +
                        device.id + " (" + signal + ").");
            }
        }
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
            String signal;
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
        void onRssiLoadedFromDisk();
        void onRecordAdded(String signal, Device device, RssiRecord r);
        void onSamplesLoadedFromDisk();
        void onBtSampleAdded();
        void onBtleSampleAdded();
        void onWifiSampleAdded();
        void onWifi5gSampleAdded();
    }
    private final List<SamplerListener> listeners = new ArrayList<>(1);
    public boolean registerListener(SamplerListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(SamplerListener l) {
        return listeners.remove(l);
    }
}
