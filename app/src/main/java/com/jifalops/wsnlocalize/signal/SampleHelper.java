package com.jifalops.wsnlocalize.signal;

import android.util.Log;

import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.toolbox.file.NumberReaderWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SampleHelper {
    static final String TAG = SampleHelper.class.getSimpleName();

    public interface SamplesCallback {
        void onSamplesLoaded();
    }

    final List<double[]>
            bt = new ArrayList<>(),
            btle = new ArrayList<>(),
            wifi = new ArrayList<>(),
            wifi5g = new ArrayList<>();
    final NumberReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
    boolean btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;
    SamplesCallback callback;

    public SampleHelper(SamplesCallback onLoadFinished) {
        callback = onLoadFinished;

        btRW = new NumberReaderWriter(App.getFile(App.SIGNAL_BT, App.DATA_SAMPLES));
        btleRW = new NumberReaderWriter(App.getFile(App.SIGNAL_BTLE, App.DATA_SAMPLES));
        wifiRW = new NumberReaderWriter(App.getFile(App.SIGNAL_WIFI, App.DATA_SAMPLES));
        wifi5gRW = new NumberReaderWriter(App.getFile(App.SIGNAL_WIFI5G, App.DATA_SAMPLES));

        if(!btRW.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
            @Override
            public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                bt.addAll(list);
                btLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " BT samples are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read BT samples.", e);
            }
        })) btLoaded = true;

        if (!btleRW.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
            @Override
            public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                btle.addAll(list);
                btleLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " BTLE samples are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read BTLE samples.", e);
            }
        })) btleLoaded = true;

        if(!wifiRW.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
            @Override
            public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                wifi.addAll(list);
                wifiLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " WiFi samples are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read WiFi samples.", e);
            }
        })) wifiLoaded = true;

        if (!wifi5gRW.readNumbers(new AbsTextReaderWriter.TypedReadListener<double[]>() {
            @Override
            public void onReadSucceeded(List<double[]> list, int typingExceptions) {
                wifi5g.addAll(list);
                wifi5gLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " WiFi5G samples are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read WiFi5G samples.", e);
            }
        })) wifi5gLoaded = true;
        checkIfAllLoaded();
    }

    private void checkIfAllLoaded() {
        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
            callback.onSamplesLoaded();
        }
    }

    public List<double[]> getBt() { return bt; }
    public List<double[]> getBtle() { return btle; }
    public List<double[]> getWifi() { return wifi; }
    public List<double[]> getWifi5g() { return wifi5g; }
    public List<double[]> getAll() {
        List<double[]> list = new ArrayList<>();
        list.addAll(bt);
        list.addAll(btle);
        list.addAll(wifi);
        list.addAll(wifi5g);
        return list;
    }

    public void addBt(double[] e) {
        btRW.writeNumbers(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
            @Override
            public void onWriteSucceed(int linesWritten) {

            }

            @Override
            public void onWriteFailed(IOException e) {
                Log.e(TAG, "Failed writing BT estimator.", e);
            }
        });
        bt.add(e);
    }
    public void addBtle(double[] e) {
        btleRW.writeNumbers(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
            @Override
            public void onWriteSucceed(int linesWritten) {

            }

            @Override
            public void onWriteFailed(IOException e) {
                Log.e(TAG, "Failed writing BTLE estimator.", e);
            }
        });
        btle.add(e);
    }
    public void addWifi(double[] e) {
        wifiRW.writeNumbers(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
            @Override
            public void onWriteSucceed(int linesWritten) {

            }

            @Override
            public void onWriteFailed(IOException e) {
                Log.e(TAG, "Failed writing WiFi estimator.", e);
            }
        });
        wifi.add(e);
    }
    public void addWifi5g(double[] e) {
        wifi5gRW.writeNumbers(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
            @Override
            public void onWriteSucceed(int linesWritten) {

            }

            @Override
            public void onWriteFailed(IOException e) {
                Log.e(TAG, "Failed writing WiFi5G estimator.", e);
            }
        });
        wifi5g.add(e);
    }
}
