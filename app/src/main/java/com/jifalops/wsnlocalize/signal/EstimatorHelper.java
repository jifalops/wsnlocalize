//package com.jifalops.wsnlocalize.signal;
//
//import android.util.Log;
//
//import com.jifalops.wsnlocalize.App;
//import com.jifalops.wsnlocalize.data.Estimator;
//import com.jifalops.wsnlocalize.file.EstimatorReaderWriter;
//import com.jifalops.wsnlocalize.toolbox.file.AbsTextReaderWriter;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
///**
// * @deprecated
// */
//public class EstimatorHelper {
//    static final String TAG = EstimatorHelper.class.getSimpleName();
//
//    public interface EstimatorsCallback {
//        void onEstimatorsLoaded();
//    }
//
//    final List<Estimator>
//            bt = new ArrayList<>(),
//            btle = new ArrayList<>(),
//            wifi = new ArrayList<>(),
//            wifi5g = new ArrayList<>();
//    final EstimatorReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
//    boolean btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;
//    EstimatorsCallback callback;
//    final boolean maxSamplesOnly;
//
//    public EstimatorHelper(boolean best, final boolean maxSamplesOnly, EstimatorsCallback onLoadFinished) {
//        callback = onLoadFinished;
//        this.maxSamplesOnly = maxSamplesOnly;
//
//        String dataType = best ? App.DATA_BEST_ESTIMATOR : App.DATA_ESTIMATORS;
//        btRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_BT, dataType));
//        btleRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_BTLE, dataType));
//        wifiRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_WIFI, dataType));
//        wifi5gRW = new EstimatorReaderWriter(App.getFile(App.SIGNAL_WIFI5G, dataType));
//
//        if (!btRW.readEstimators(new AbsTextReaderWriter.TypedReadListener<Estimator>() {
//            @Override
//            public void onReadSucceeded(List<Estimator> list, int typingExceptions) {
//                add(list, bt);
//                btLoaded = true;
//                if (typingExceptions > 0) {
//                    Log.e(TAG, typingExceptions + " BT estimators are corrupted.");
//                }
//                checkIfAllLoaded();
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                Log.e(TAG, "Failed to read BT estimators.", e);
//            }
//        })) btLoaded = true;
//
//        if (!btleRW.readEstimators(new AbsTextReaderWriter.TypedReadListener<Estimator>() {
//            @Override
//            public void onReadSucceeded(List<Estimator> list, int typingExceptions) {
//                add(list, btle);
//                btleLoaded = true;
//                if (typingExceptions > 0) {
//                    Log.e(TAG, typingExceptions + " BTLE estimators are corrupted.");
//                }
//                checkIfAllLoaded();
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                Log.e(TAG, "Failed to read BTLE estimators.", e);
//            }
//        })) btleLoaded = true;
//
//        if (!wifiRW.readEstimators(new AbsTextReaderWriter.TypedReadListener<Estimator>() {
//            @Override
//            public void onReadSucceeded(List<Estimator> list, int typingExceptions) {
//                add(list, wifi);
//                wifiLoaded = true;
//                if (typingExceptions > 0) {
//                    Log.e(TAG, typingExceptions + " WiFi estimators are corrupted.");
//                }
//                checkIfAllLoaded();
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                Log.e(TAG, "Failed to read WiFi estimators.", e);
//            }
//        })) wifiLoaded = true;
//
//        if (!wifi5gRW.readEstimators(new AbsTextReaderWriter.TypedReadListener<Estimator>() {
//            @Override
//            public void onReadSucceeded(List<Estimator> list, int typingExceptions) {
//                add(list, wifi5g);
//                wifi5gLoaded = true;
//                if (typingExceptions > 0) {
//                    Log.e(TAG, typingExceptions + " WiFi5G estimators are corrupted.");
//                }
//                checkIfAllLoaded();
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                Log.e(TAG, "Failed to read WiFi5G estimators.", e);
//            }
//        })) wifi5gLoaded = true;
//        checkIfAllLoaded();
//    }
//
//    private void add(List<Estimator> from, List<Estimator> to) {
//        if (maxSamplesOnly) {
//            int max = 0;
//            for (Estimator de : from) {
//                if (de.results.numSamples > max) max = de.results.numSamples;
//            }
//            for (Estimator de : from) {
//                if (de.results.numSamples == max) to.add(de);
//            }
//        } else {
//            to.addAll(from);
//        }
//    }
//
//    private void checkIfAllLoaded() {
//        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
//            callback.onEstimatorsLoaded();
//        }
//    }
//
//    public List<Estimator> getBt() { return bt; }
//    public List<Estimator> getBtle() { return btle; }
//    public List<Estimator> getWifi() { return wifi; }
//    public List<Estimator> getWifi5g() { return wifi5g; }
//    public List<Estimator> getAll() {
//        List<Estimator> list = new ArrayList<>();
//        list.addAll(bt);
//        list.addAll(btle);
//        list.addAll(wifi);
//        list.addAll(wifi5g);
//        return list;
//    }
//
//    public void addBt(Estimator e) {
//        btRW.writeEstimators(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
//            @Override
//            public void onWriteSucceed(int linesWritten) {
//
//            }
//
//            @Override
//            public void onWriteFailed(IOException e) {
//                Log.e(TAG, "Failed writing BT estimator.", e);
//            }
//        });
//        bt.add(e);
//    }
//    public void addBtle(Estimator e) {
//        btleRW.writeEstimators(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
//            @Override
//            public void onWriteSucceed(int linesWritten) {
//
//            }
//
//            @Override
//            public void onWriteFailed(IOException e) {
//                Log.e(TAG, "Failed writing BTLE estimator.", e);
//            }
//        });
//        btle.add(e);
//    }
//    public void addWifi(Estimator e) {
//        wifiRW.writeEstimators(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
//            @Override
//            public void onWriteSucceed(int linesWritten) {
//
//            }
//
//            @Override
//            public void onWriteFailed(IOException e) {
//                Log.e(TAG, "Failed writing WiFi estimator.", e);
//            }
//        });
//        wifi.add(e);
//    }
//    public void addWifi5g(Estimator e) {
//        wifi5gRW.writeEstimators(Collections.singletonList(e), true, new AbsTextReaderWriter.WriteListener() {
//            @Override
//            public void onWriteSucceed(int linesWritten) {
//
//            }
//
//            @Override
//            public void onWriteFailed(IOException e) {
//                Log.e(TAG, "Failed writing WiFi5G estimator.", e);
//            }
//        });
//        wifi5g.add(e);
//    }
//}
