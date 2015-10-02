package com.jifalops.wsnlocalize.data.helper;

import android.util.Log;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.Rssi;
import com.jifalops.wsnlocalize.data.RssiList;
import com.jifalops.wsnlocalize.file.RssiReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class RssiHelper {
    static final String TAG = RssiHelper.class.getSimpleName();

    private static RssiHelper instance;
    public static RssiHelper getInstance() {
        if (instance == null) instance = new RssiHelper();
        return instance;
    }

    final RssiList
            bt = new RssiList(),
            btle = new RssiList(),
            wifi = new RssiList(),
            wifi5g = new RssiList();
    final RssiReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
    boolean allLoaded, btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;

    private RssiHelper() {
        btRW = new RssiReaderWriter(App.Files.getRssiFile(App.SIGNAL_BT));
        btleRW = new RssiReaderWriter(App.Files.getRssiFile(App.SIGNAL_BTLE));
        wifiRW = new RssiReaderWriter(App.Files.getRssiFile(App.SIGNAL_WIFI));
        wifi5gRW = new RssiReaderWriter(App.Files.getRssiFile(App.SIGNAL_WIFI5G));

        if (!btRW.readRssi(new AbsTextReaderWriter.TypedReadListener<Rssi>() {
            @Override
            public void onReadSucceeded(List<Rssi> list, int typingExceptions) {
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
        })) btLoaded = true;

        if (!btleRW.readRssi(new AbsTextReaderWriter.TypedReadListener<Rssi>() {
            @Override
            public void onReadSucceeded(List<Rssi> list, int typingExceptions) {
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
        })) btleLoaded = true;

        if(!wifiRW.readRssi(new AbsTextReaderWriter.TypedReadListener<Rssi>() {
            @Override
            public void onReadSucceeded(List<Rssi> list, int typingExceptions) {
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
        })) wifiLoaded = true;

        if(!wifi5gRW.readRssi(new AbsTextReaderWriter.TypedReadListener<Rssi>() {
            @Override
            public void onReadSucceeded(List<Rssi> list, int typingExceptions) {
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
        })) wifi5gLoaded = true;

        checkIfAllLoaded();
    }

    private void checkIfAllLoaded() {
        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
            allLoaded = true;
        }
    }

    public boolean isLoaded() { return allLoaded; }

    public RssiList getBt() { return bt; }
    public RssiList getBtle() { return btle; }
    public RssiList getWifi() { return wifi; }
    public RssiList getWifi5g() { return wifi5g; }
    public RssiList getAll() {
        RssiList list = new RssiList();
        list.addAll(bt);
        list.addAll(btle);
        list.addAll(wifi);
        list.addAll(wifi5g);
        return list;
    }

    public void addBt(List<Rssi> rssi) {
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
    public void addBtle(List<Rssi> rssi) {
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
    public void addWifi(List<Rssi> rssi) {
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
    public void addWifi5g(List<Rssi> rssi) {
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
