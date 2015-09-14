package com.jifalops.wsnlocalize.signal;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.data.ResettingList;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.TextReaderWriter;
import com.jifalops.wsnlocalize.util.SimpleLog;
import com.jifalops.wsnlocalize.wifi.WifiScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class SignalController {
    public static final int LOG_IMPORTANT = 3;
    public static final int LOG_INFORMATIVE = 2;
    public static final int LOG_ALL = 1;
    private static final int LOG_DEFAULT = LOG_INFORMATIVE;

    public static final String SIGNAL_BT = "bt";
    public static final String SIGNAL_BTLE = "btle";
    public static final String SIGNAL_WIFI = "wifi";

    final ResettingList.Limits
        btWindowTrigger = new ResettingList.Limits(3, 10_000, 5, 120_000),
        btTrainTrigger = new ResettingList.Limits(0,0,0,0),

        btleWindowTrigger = new ResettingList.Limits(15, 5_000, 20, 30_000),
        btleTrainTrigger = new ResettingList.Limits(0,0,0,0),

        wifiWindowTrigger = new ResettingList.Limits(5, 5_000, 10, 30_000),
        wifiTrainTrigger = new ResettingList.Limits(0,0,0,0);


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

    RssiWindowTrainingDataManager bt, btle, wifi, wifi5g;
    boolean collectEnabled, shouldUseBt, shouldUseBtle, shouldUseWifi;

    final List<Device> devices = new ArrayList<>();     // All devices
//    final List<Integer> deviceIds = new ArrayList<>();  // devices at known distance
    double distance;

    final SimpleLog log = new SimpleLog();
    int logLevel = LOG_DEFAULT;

    BtBeacon btBeacon;
    BtLeBeacon btleBeacon;
    WifiScanner wifiScanner;

    static SignalController instance;
    public static SignalController getInstance(Context ctx) {
        if (instance == null) instance = new SignalController(ctx.getApplicationContext());
        return instance;
    }
    private SignalController(Context ctx) {
        btBeacon = BtBeacon.getInstance(ctx);
        btleBeacon = BtLeBeacon.getInstance(ctx);
        wifiScanner = WifiScanner.getInstance(ctx);

        File dir = ctx.getExternalFilesDir(null);

        bt = new RssiWindowTrainingDataManager(SIGNAL_BT, dir,
                btWindowTrigger, btTrainTrigger, callbacks);
        btle = new RssiWindowTrainingDataManager(SIGNAL_BTLE, dir,
                btleWindowTrigger, btleTrainTrigger, callbacks);
        wifi = new RssiWindowTrainingDataManager(SIGNAL_WIFI, dir,
                wifiWindowTrigger, wifiTrainTrigger, callbacks);
        wifi5g = new RssiWindowTrainingDataManager(SIGNAL_WIFI + "5g", dir,
                wifiWindowTrigger, wifiTrainTrigger, callbacks);
    }

    public SimpleLog getLog() {
        return log;
    }

    public void setLogLevel(int level) {
        logLevel = level;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setDistance(double d) {
        distance = d;
    }

    public double getDistance() {
        return distance;
    }

    public void send() {
        bt.send();
        btle.send();
        wifi.send();
        wifi5g.send();
    }

    public void close() {
        bt.close();
        btle.close();
        wifi.close();
        wifi5g.close();
    }

    public void clearPendingSendLists() {
        bt.clearPendingSendLists();
        btle.clearPendingSendLists();
        wifi.clearPendingSendLists();
        wifi5g.clearPendingSendLists();
    }

    public void clearTrainingSamples() {
        bt.clearTrainingSamples();
        btle.clearTrainingSamples();
        wifi.clearTrainingSamples();
        wifi5g.clearTrainingSamples();
    }

    public List<Device> getDevices() {
        return devices;
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
            log.add(LOG_INFORMATIVE, "Found new device, " + device.id);
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
        bt.resetCurrentWindow();
    }
    private void enableBtle() {
        btleBeacon.registerListener(btLeBeaconListener);
        btleBeacon.startBeaconing();
    }
    private void disableBtle() {
        btleBeacon.stopBeaconing();
        btleBeacon.unregisterListener(btLeBeaconListener);
        btle.resetCurrentWindow();
    }
    private void enableWifi() {
        wifiScanner.registerListener(wifiScanListener);
        wifiScanner.startScanning(100);
    }
    private void disableWifi() {
        wifiScanner.stopScanning();
        wifiScanner.unregisterListener(wifiScanListener);
        wifi.resetCurrentWindow();
        wifi5g.resetCurrentWindow();
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
        if (use && collectEnabled) enableWifi();
        else if (!use && collectEnabled) disableWifi();
    }

    public void setCollectEnabled(boolean enabled) {
        if (collectEnabled == enabled) return;
        collectEnabled = enabled;
        if (enabled) {
            if (shouldUseBt) enableBt();
            if (shouldUseBtle) enableBtle();
            if (shouldUseWifi) enableWifi();
        } else {
            if (shouldUseBt) disableBt();
            if (shouldUseBtle) disableBtle();
            if (shouldUseWifi) disableWifi();
        }
    }

    public boolean getShouldUseBt() { return shouldUseBt; }
    public boolean getShouldUseBtle() { return shouldUseBtle; }
    public boolean getShouldUseWifi() { return shouldUseWifi; }
    public boolean getCollectEnabled() { return collectEnabled; }

    void addRecord(Device device, String signal, int rssi, int freq) {
        if (collectEnabled) {
            if (device.isDistanceKnown && rssi < 0 && distance > 0) {
                log.add(LOG_INFORMATIVE, "Device " + device.id + ": " +
                        rssi + " dBm (" + freq + " MHz) at " +
                        distance + "m (" + signal + ").");
//                String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(new Date());
                RssiRecord record = new RssiRecord(device.mac, rssi, freq,
                        System.currentTimeMillis(), distance);
                if (signal.equals(SIGNAL_BT)) {
                    bt.add(record);
                } else if (signal.equals(SIGNAL_BTLE)) {
                    btle.add(record);
                } else if (signal.equals(SIGNAL_WIFI)) {
                    if (freq < 4000) {
                        wifi.add(record);
                    } else {
                        wifi5g.add(record);
                    }
                }
            } else {
                log.add(LOG_ALL, "Ignoring " + rssi + " dBm (" + freq + " MHz) for device " +
                        device.id + " (" + signal + ").");
            }
        }
    }


    final BtBeacon.BtBeaconListener btBeaconListener = new BtBeacon.BtBeaconListener() {
        @Override
        public void onDeviceFound(BluetoothDevice device, short rssi) {
            addRecord(getDevice(device.getAddress(), device.getName() + " (BT)"),
                    SIGNAL_BT, rssi, 2400);
        }

        @Override
        public void onThisDeviceDiscoverableChanged(boolean discoverable) {
            log.add(LOG_INFORMATIVE, "BT Discoverability changed to " + discoverable);
        }

        @Override
        public void onDiscoveryStarting() {
            log.add(LOG_ALL, "Scanning for BT devices...");
        }
    };

    final BtLeBeacon.BtLeBeaconListener btLeBeaconListener = new BtLeBeacon.BtLeBeaconListener() {
        @Override
        public void onAdvertiseNotSupported() {
            log.add(LOG_IMPORTANT, "BTLE advertisement not supported on this device.");
        }

        @Override
        public void onAdvertiseStartSuccess(AdvertiseSettings settingsInEffect) {
            log.add(LOG_IMPORTANT, "BTLE advertising started at " +
                    settingsInEffect.getTxPowerLevel() + " dBm.");
        }

        @Override
        public void onAdvertiseStartFailure(int errorCode, String errorMsg) {
            log.add(LOG_IMPORTANT, "BTLE advertisements failed to start (" + errorCode + "): " + errorMsg) ;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            log.add(LOG_ALL, "Received " + results.size() + " batch scan results (BTLE).");
            for (ScanResult sr : results) {
                handleScanResult(sr);
            }
        }

        @Override
        public void onScanFailed(int errorCode, String errorMsg) {
            log.add(LOG_IMPORTANT, "BT-LE scan failed (" + errorCode + "): " + errorMsg);
        }

        void handleScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                addRecord(getDevice(device.getAddress(), device.getName() + " (BTLE)"),
                        SIGNAL_BTLE, result.getRssi(), 2400);
            } else {
                log.add(LOG_INFORMATIVE, "BTLE received " + result.getRssi() + " dBm from null device.");
            }
        }
    };

    final WifiScanner.ScanListener wifiScanListener = new WifiScanner.ScanListener() {
        @Override
        public void onScanResults(List<android.net.wifi.ScanResult> scanResults) {
            log.add(LOG_ALL, "WiFi found " + scanResults.size() + " results.");
            for (android.net.wifi.ScanResult r : scanResults) {
                addRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                        SIGNAL_WIFI, r.level, r.frequency);
            }
        }
    };

    final RssiWindowTrainingDataManager.Callbacks callbacks = new RssiWindowTrainingDataManager.Callbacks() {
        @Override
        public void onDataFileRead(TextReaderWriter rw) {
//            if (rw == bt.rssiRW) btRssiCountView.setText(bt.getRssiCount() + "");
//            else if (rw == bt.windowRW) btWindowCountView.setText(bt.getWindowCount() + "");
//            else if (rw == btle.rssiRW) btleRssiCountView.setText(btle.getRssiCount()+"");
//            else if (rw == btle.windowRW) btleWindowCountView.setText(btle.getWindowCount()+"");
//            else if (rw == wifi.rssiRW) wifiRssiCountView.setText(wifi.getRssiCount()+"");
//            else if (rw == wifi.windowRW) wifiWindowCountView.setText(wifi.getWindowCount()+"");
        }

        @Override
        public void onDataFileWrite(TextReaderWriter rw) {

        }

        @Override
        public void onTrainingStarting(RssiWindowTrainingDataManager s, int samples) {
            log.add(LOG_INFORMATIVE, "Training " + s.getSignalType() + " with " + samples + " samples.");
        }

        @Override
        public void onTrainingComplete(RssiWindowTrainingDataManager s, double[] weights, double error, int samples) {
            log.add(LOG_IMPORTANT, "Trained " + s.getSignalType() + " with " +
                    samples + " samples, error = " + String.format(Locale.US, "%.3f", error));
        }

        @Override
        public void onWindowReady(RssiWindowTrainingDataManager s, WindowRecord record) {
            String msg = s.getSignalType() + " window: " + record.rss.count + " in " +
                    formatMillis(record.elapsed.millis);
            if (record.estimated != 0) {
                double error = (record.estimated - record.distance) / record.distance;
                msg += String.format(Locale.US, ", est: %.1fm %.1f%%",
                        record.estimated, error * 100);
            }
            log.add(LOG_IMPORTANT, msg);
//            if (s == bt) btWindowCountView.setText(bt.getWindowCount()+"");
//            else if (s == btle) btleWindowCountView.setText(btle.getWindowCount()+"");
//            else if (s == wifi) wifiWindowCountView.setText(wifi.getWindowCount()+"");
        }

        @Override
        public void onSentSuccess(RssiWindowTrainingDataManager s, boolean wasRssi, int count) {
            String type = wasRssi ? " rssi " : " window ";
            log.add(LOG_IMPORTANT, s.getSignalType() + " sent " + count + type + "records successfully.");
        }

        @Override
        public void onSentFailure(RssiWindowTrainingDataManager s, boolean wasRssi, int count,
                                  int respCode, String resp, String result) {
            String type = wasRssi ? " rssi " : " window ";
            log.add(LOG_IMPORTANT, s.getSignalType() + " failed to send " + count + type + "records. " +
                    respCode + ": " + resp + ". Result: " + result);
        }

        @Override
        public void onSentFailure(RssiWindowTrainingDataManager s, boolean wasRssi, int count, String volleyError) {
            String type = wasRssi ? " rssi " : " window ";
            log.add(LOG_IMPORTANT, s.getSignalType() + " failed to send " + count + type + "records. " +
                    volleyError);
        }
    };

    String formatMillis(long millis) {
        return String.format(Locale.US, "%.1fs", ((double)millis)/1000);
    }


    public interface SignalListener {

    }
    private final List<SignalListener> listeners = new ArrayList<>(1);
    public boolean registerListener(SignalListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(SignalListener l) {
        return listeners.remove(l);
    }
}
