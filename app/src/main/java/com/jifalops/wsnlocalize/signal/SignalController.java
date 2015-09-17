package com.jifalops.wsnlocalize.signal;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.jifalops.wsnlocalize.Settings;
import com.jifalops.wsnlocalize.bluetooth.BtBeacon;
import com.jifalops.wsnlocalize.bluetooth.BtLeBeacon;
import com.jifalops.wsnlocalize.data.Estimator;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
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

    final List<Device> devices = new ArrayList<>();
    double distance;

    final SimpleLog log = new SimpleLog();

    BtBeacon btBeacon;
    BtLeBeacon btleBeacon;
    WifiScanner wifiScanner;

    private static SignalController instance;
    public static SignalController getInstance(Context ctx) {
        if (instance == null) instance = new SignalController(ctx.getApplicationContext());
        return instance;
    }
    private SignalController(Context ctx) {
        btBeacon = BtBeacon.getInstance(ctx);
        btleBeacon = BtLeBeacon.getInstance(ctx);
        wifiScanner = WifiScanner.getInstance(ctx);

        File dir = Settings.getDataDir(ctx);

        bt = new RssiWindowTrainingDataManager(Settings.SIGNAL_BT, dir,
                Settings.btWindowTrigger, Settings.btTrainTrigger, callbacks);
        btle = new RssiWindowTrainingDataManager(Settings.SIGNAL_BTLE, dir,
                Settings.btleWindowTrigger, Settings.btleTrainTrigger, callbacks);
        wifi = new RssiWindowTrainingDataManager(Settings.SIGNAL_WIFI, dir,
                Settings.wifiWindowTrigger, Settings.wifiTrainTrigger, callbacks);
        wifi5g = new RssiWindowTrainingDataManager(Settings.SIGNAL_WIFI5G, dir,
                Settings.wifiWindowTrigger, Settings.wifiTrainTrigger, callbacks);
    }

    public RssiWindowTrainingDataManager getBt() { return bt; }
    public RssiWindowTrainingDataManager getBtle() { return btle; }
    public RssiWindowTrainingDataManager getWifi() { return wifi; }
    public RssiWindowTrainingDataManager getWifi5g() { return wifi5g; }

    public SimpleLog getLog() {
        return log;
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
        instance = null;
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
            for (SignalListener l : listeners) l.onDeviceFound(device);
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

    void addEvent(int level, String msg) {
        log.add(level, msg);
        for (SignalListener l : listeners) l.onMessageLogged(level, msg);
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
                if (signal.equals(Settings.SIGNAL_BT)) {
                    bt.add(record);
                } else if (signal.equals(Settings.SIGNAL_BTLE)) {
                    btle.add(record);
                } else if (signal.equals(Settings.SIGNAL_WIFI)) {
                    wifi.add(record);
                } else if (signal.equals(Settings.SIGNAL_WIFI5G)) {
                    wifi5g.add(record);
                }
                for (SignalListener l : listeners) l.onRecordAdded(signal, device, record);
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
                    Settings.SIGNAL_BT, rssi, 2400);
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
                        Settings.SIGNAL_BTLE, result.getRssi(), 2400);
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
                signal = r.frequency < 4000 ? Settings.SIGNAL_WIFI : Settings.SIGNAL_WIFI5G;
                addRecord(getDevice(r.BSSID, r.SSID + " (WiFi " + r.frequency + "MHz)"),
                        signal, r.level, r.frequency);
            }
        }
    };

    final RssiWindowTrainingDataManager.Callbacks callbacks = new RssiWindowTrainingDataManager.Callbacks() {
        @Override
        public void onRssiLoadedFromDisk(String signal, List<RssiRecord> records) {
            for (SignalListener l : listeners) l.onRssiLoadedFromDisk(signal, records);
        }

        @Override
        public void onWindowsLoadedFromDisk(String signal, List<WindowRecord> records) {
            for (SignalListener l : listeners) l.onWindowsLoadedFromDisk(signal, records);
        }

        @Override
        public void onEstimatorsLoadedFromDisk(String signal, List<Estimator> estimators) {
            for (SignalListener l : listeners) l.onEstimatorsLoadedFromDisk(signal, estimators);
        }

        @Override
        public void onTrainingStarting(String signal, int samples) {
            addEvent(LOG_IMPORTANT, "Training " + signal + " with " + samples + " samples.");
            for (SignalListener l : listeners) l.onTrainingStarting(signal, samples);
        }

        @Override
        public void onGenerationFinished(String signal, int gen, double best, double mean, double stdDev) {
            addEvent(LOG_ALL, String.format(Locale.US,
                    "%s gen:%d best:%.4f mean:%.4f std:%.4f", signal, gen, best, mean, stdDev));
            for (SignalListener l : listeners) l.onGenerationFinished(signal, gen, best, mean, stdDev);
        }

        @Override
        public void onTrainingComplete(String signal, Estimator estimator, double error, int samples, int generations) {
            addEvent(LOG_IMPORTANT, "Trained " + signal + " with " +
                    samples + " samples, error = " + String.format(Locale.US, "%1.4f", error)
                    + " gen: " + generations);
            for (SignalListener l : listeners) l.onTrainingComplete(signal, estimator, error, samples, generations);
        }

        @Override
        public void onWindowReady(String signal, WindowRecord record) {
            String msg = signal + " window: " + record.rss.count + " in " +
                    formatMillis(record.elapsed.millis);
            if (record.estimated != 0) {
                double error = (record.estimated - record.distance) / record.distance;
                msg += String.format(Locale.US, ", est: %.1fm %.1f%%",
                        record.estimated, error * 100);
            }
            addEvent(LOG_IMPORTANT, msg);
            for (SignalListener l : listeners) l.onWindowReady(signal, record);
        }

        @Override
        public void onSentSuccess(String signal, String dataType, int count) {
            addEvent(LOG_IMPORTANT, signal + " sent " + count + " " + dataType + " records successfully.");
            for (SignalListener l : listeners) l.onSentSuccess(signal, dataType, count);
        }

        @Override
        public void onSentFailure(String signal, String dataType, int count,
                                  int respCode, String resp, String result) {
            addEvent(LOG_IMPORTANT, signal + " failed to send " + count + " " + dataType + " records. " +
                    respCode + ": " + resp + ". Result: " + result);
            for (SignalListener l : listeners) l.onSentFailure(signal, dataType, count, respCode, resp, result);
        }

        @Override
        public void onSentFailure(String signal, String dataType, int count, String volleyError) {
            addEvent(LOG_IMPORTANT, signal + " failed to send " + count + " " + dataType + " records. " +
                    volleyError);
            for (SignalListener l : listeners) l.onSentFailure(signal, dataType, count, volleyError);
        }
    };

    String formatMillis(long millis) {
        return String.format(Locale.US, "%.1fs", ((double)millis)/1000);
    }

    public interface SignalListener {
        void onMessageLogged(int level, String msg);
        void onDeviceFound(Device device);
        void onRssiLoadedFromDisk(String signal, List<RssiRecord> records);
        void onWindowsLoadedFromDisk(String signal, List<WindowRecord> records);
        void onEstimatorsLoadedFromDisk(String signal, List<Estimator> estimators);
        void onRecordAdded(String signal, Device device, RssiRecord r);
        void onTrainingStarting(String signal, int samples);
        void onGenerationFinished(String signal, int gen, double best, double mean, double stdDev);
        void onTrainingComplete(String signal, Estimator estimator, double error, int samples, int generations);
        void onWindowReady(String signal, WindowRecord record);
        void onSentSuccess(String signal, String dataType, int count);
        void onSentFailure(String signal, String dataType, int count, int respCode, String resp, String result);
        void onSentFailure(String signal, String dataType, int count, String volleyError);
    }
    private final List<SignalListener> listeners = new ArrayList<>(1);
    public boolean registerListener(SignalListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }
    public boolean unregisterListener(SignalListener l) {
        return listeners.remove(l);
    }
}
