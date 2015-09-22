package com.jifalops.wsnlocalize.signal;

import android.util.Log;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.file.RssiReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiHelper {
    static final String TAG = RssiHelper.class.getSimpleName();

    public interface RssiCallback {
        void onRssiLoaded();
    }

    final List<RssiRecord>
            bt = new ArrayList<>(),
            btle = new ArrayList<>(),
            wifi = new ArrayList<>(),
            wifi5g = new ArrayList<>();
    final RssiReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
    boolean btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;
    RssiCallback callback;

    public RssiHelper(RssiCallback onLoadFinished) {
        callback = onLoadFinished;

        btRW = new RssiReaderWriter(App.getFile(App.SIGNAL_BT, App.DATA_RSSI));
        btleRW = new RssiReaderWriter(App.getFile(App.SIGNAL_BTLE, App.DATA_RSSI));
        wifiRW = new RssiReaderWriter(App.getFile(App.SIGNAL_WIFI, App.DATA_RSSI));
        wifi5gRW = new RssiReaderWriter(App.getFile(App.SIGNAL_WIFI5G, App.DATA_RSSI));

        btRW.readRssi(new AbsTextReaderWriter.TypedReadListener<RssiRecord>() {
            @Override
            public void onReadSucceeded(List<RssiRecord> list, int typingExceptions) {
                bt.addAll(list);
                btLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " BT rssi are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read BT rssi.", e);
            }
        });

        btleRW.readRssi(new AbsTextReaderWriter.TypedReadListener<RssiRecord>() {
            @Override
            public void onReadSucceeded(List<RssiRecord> list, int typingExceptions) {
                btle.addAll(list);
                btleLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " BTLE rssi are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read BTLE rssi.", e);
            }
        });

        wifiRW.readRssi(new AbsTextReaderWriter.TypedReadListener<RssiRecord>() {
            @Override
            public void onReadSucceeded(List<RssiRecord> list, int typingExceptions) {
                wifi.addAll(list);
                wifiLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " WiFi rssi are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read WiFi rssi.", e);
            }
        });

        wifi5gRW.readRssi(new AbsTextReaderWriter.TypedReadListener<RssiRecord>() {
            @Override
            public void onReadSucceeded(List<RssiRecord> list, int typingExceptions) {
                wifi5g.addAll(list);
                wifi5gLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " WiFi5G rssi are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read WiFi5G rssi.", e);
            }
        });
    }

    void checkIfAllLoaded() {
        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
            callback.onRssiLoaded();
        }
    }

    public List<RssiRecord> getBt() { return bt; }
    public List<RssiRecord> getBtle() { return btle; }
    public List<RssiRecord> getWifi() { return wifi; }
    public List<RssiRecord> getWifi5g() { return wifi5g; }
    public List<RssiRecord> getAll() {
        List<RssiRecord> list = new ArrayList<>();
        list.addAll(bt);
        list.addAll(btle);
        list.addAll(wifi);
        list.addAll(wifi5g);
        return list;
    }

    public void addBt(List<RssiRecord> rssi) {
        btRW.writeRecords(rssi, true, new AbsTextReaderWriter.WriteListener() {
            @Override
            public void onWriteSucceed(int linesWritten) {

            }

            @Override
            public void onWriteFailed(IOException e) {
                Log.e(TAG, "Failed writing BT estimator.", e);
            }
        });
        bt.addAll(rssi);
    }
    public void addBtle(List<RssiRecord> rssi) {
        btleRW.writeRecords(rssi, true, new AbsTextReaderWriter.WriteListener() {
            @Override
            public void onWriteSucceed(int linesWritten) {

            }

            @Override
            public void onWriteFailed(IOException e) {
                Log.e(TAG, "Failed writing BTLE estimator.", e);
            }
        });
        btle.addAll(rssi);
    }
    public void addWifi(List<RssiRecord> rssi) {
        wifiRW.writeRecords(rssi, true, new AbsTextReaderWriter.WriteListener() {
            @Override
            public void onWriteSucceed(int linesWritten) {

            }

            @Override
            public void onWriteFailed(IOException e) {
                Log.e(TAG, "Failed writing WiFi estimator.", e);
            }
        });
        wifi.addAll(rssi);
    }
    public void addWifi5g(List<RssiRecord> rssi) {
        wifi5gRW.writeRecords(rssi, true, new AbsTextReaderWriter.WriteListener() {
            @Override
            public void onWriteSucceed(int linesWritten) {

            }

            @Override
            public void onWriteFailed(IOException e) {
                Log.e(TAG, "Failed writing WiFi5G estimator.", e);
            }
        });
        wifi5g.addAll(rssi);
    }
}
