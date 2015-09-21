package com.jifalops.wsnlocalize.signal;

import android.util.Log;

import com.jifalops.toolbox.file.AbsTextReaderWriter;
import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.DistanceEstimator;
import com.jifalops.wsnlocalize.file.EstimatorReaderWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class EstimatorHelper {
    static final String TAG = EstimatorHelper.class.getSimpleName();

    public interface EstimatorsCallback {
        void onEstimatorsLoaded();
    }

    final List<DistanceEstimator>
            bt = new ArrayList<>(),
            btle = new ArrayList<>(),
            wifi = new ArrayList<>(),
            wifi5g = new ArrayList<>();
    final EstimatorReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
    boolean btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;
    EstimatorsCallback callback;

    public EstimatorHelper(EstimatorsCallback onLoadFinished) {
        callback = onLoadFinished;

        btRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_BT, App.DATA_ESTIMATOR));
        btleRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_BTLE, App.DATA_ESTIMATOR));
        wifiRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_WIFI, App.DATA_ESTIMATOR));
        wifi5gRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_WIFI5G, App.DATA_ESTIMATOR));

        btRW.readEstimators(new AbsTextReaderWriter.TypedReadListener<DistanceEstimator>() {
            @Override
            public void onReadSucceeded(List<DistanceEstimator> list, int typingExceptions) {
                bt.addAll(list);
                btLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " BT estimators are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read BT estimators.", e);
            }
        });

        btleRW.readEstimators(new AbsTextReaderWriter.TypedReadListener<DistanceEstimator>() {
            @Override
            public void onReadSucceeded(List<DistanceEstimator> list, int typingExceptions) {
                btle.addAll(list);
                btleLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " BTLE estimators are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read BTLE estimators.", e);
            }
        });

        wifiRW.readEstimators(new AbsTextReaderWriter.TypedReadListener<DistanceEstimator>() {
            @Override
            public void onReadSucceeded(List<DistanceEstimator> list, int typingExceptions) {
                wifi.addAll(list);
                wifiLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " WiFi estimators are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read WiFi estimators.", e);
            }
        });

        wifi5gRW.readEstimators(new AbsTextReaderWriter.TypedReadListener<DistanceEstimator>() {
            @Override
            public void onReadSucceeded(List<DistanceEstimator> list, int typingExceptions) {
                wifi5g.addAll(list);
                wifi5gLoaded = true;
                if (typingExceptions > 0) {
                    Log.e(TAG, typingExceptions + " WiFi5G estimators are corrupted.");
                }
                checkIfAllLoaded();
            }

            @Override
            public void onReadFailed(IOException e) {
                Log.e(TAG, "Failed to read WiFi5G estimators.", e);
            }
        });
    }

    void checkIfAllLoaded() {
        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
            callback.onEstimatorsLoaded();
        }
    }

    public List<DistanceEstimator> getBt() { return bt; }
    public List<DistanceEstimator> getBtle() { return btle; }
    public List<DistanceEstimator> getWifi() { return wifi; }
    public List<DistanceEstimator> getWifi5g() { return wifi5g; }
    public List<DistanceEstimator> getAll() {
        List<DistanceEstimator> list = new ArrayList<>();
        list.addAll(bt);
        list.addAll(btle);
        list.addAll(wifi);
        list.addAll(wifi5g);
        return list;
    }

    public void addBt(DistanceEstimator e) {
        btRW.writeEstimators(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
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
    public void addBtle(DistanceEstimator e) {
        btleRW.writeEstimators(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
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
    public void addWifi(DistanceEstimator e) {
        wifiRW.writeEstimators(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
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
    public void addWifi5g(DistanceEstimator e) {
        wifi5gRW.writeEstimators(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
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
