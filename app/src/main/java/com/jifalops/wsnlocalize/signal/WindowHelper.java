//package com.jifalops.wsnlocalize.signal;
//
//import android.util.Log;
//
//import com.jifalops.wsnlocalize.App;
//import com.jifalops.wsnlocalize.file.WindowReaderWriter;
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
//public class WindowHelper {
//    static final String TAG = WindowHelper.class.getSimpleName();
//
//    public interface WindowCallback {
//        void onWindowsLoaded();
//    }
//
//    final List<WindowRecord>
//            bt = new ArrayList<>(),
//            btle = new ArrayList<>(),
//            wifi = new ArrayList<>(),
//            wifi5g = new ArrayList<>();
//    final WindowReaderWriter btRW, btleRW, wifiRW, wifi5gRW;
//    boolean btLoaded, btleLoaded, wifiLoaded, wifi5gLoaded;
//    WindowCallback callback;
//
//    public WindowHelper(WindowCallback onLoadFinished) {
//        callback = onLoadFinished;
//
//        btRW = new WindowReaderWriter(App.getFile(App.SIGNAL_BT, App.DATA_WINDOW));
//        btleRW = new WindowReaderWriter(App.getFile(App.SIGNAL_BTLE, App.DATA_WINDOW));
//        wifiRW = new WindowReaderWriter(App.getFile(App.SIGNAL_WIFI, App.DATA_WINDOW));
//        wifi5gRW = new WindowReaderWriter(App.getFile(App.SIGNAL_WIFI5G, App.DATA_WINDOW));
//
//        if(!btRW.readRecords(new AbsTextReaderWriter.TypedReadListener<WindowRecord>() {
//            @Override
//            public void onReadSucceeded(List<WindowRecord> list, int typingExceptions) {
//                bt.addAll(list);
//                btLoaded = true;
//                if (typingExceptions > 0) {
//                    Log.e(TAG, typingExceptions + " BT windows are corrupted.");
//                }
//                checkIfAllLoaded();
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                Log.e(TAG, "Failed to read BT windows.", e);
//            }
//        })) btLoaded = true;
//
//        if (!btleRW.readRecords(new AbsTextReaderWriter.TypedReadListener<WindowRecord>() {
//            @Override
//            public void onReadSucceeded(List<WindowRecord> list, int typingExceptions) {
//                btle.addAll(list);
//                btleLoaded = true;
//                if (typingExceptions > 0) {
//                    Log.e(TAG, typingExceptions + " BTLE windows are corrupted.");
//                }
//                checkIfAllLoaded();
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                Log.e(TAG, "Failed to read BTLE windows.", e);
//            }
//        })) btleLoaded = true;
//
//        if (!wifiRW.readRecords(new AbsTextReaderWriter.TypedReadListener<WindowRecord>() {
//            @Override
//            public void onReadSucceeded(List<WindowRecord> list, int typingExceptions) {
//                wifi.addAll(list);
//                wifiLoaded = true;
//                if (typingExceptions > 0) {
//                    Log.e(TAG, typingExceptions + " WiFi windows are corrupted.");
//                }
//                checkIfAllLoaded();
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                Log.e(TAG, "Failed to read WiFi windows.", e);
//            }
//        })) wifiLoaded = true;
//
//        if (!wifi5gRW.readRecords(new AbsTextReaderWriter.TypedReadListener<WindowRecord>() {
//            @Override
//            public void onReadSucceeded(List<WindowRecord> list, int typingExceptions) {
//                wifi5g.addAll(list);
//                wifi5gLoaded = true;
//                if (typingExceptions > 0) {
//                    Log.e(TAG, typingExceptions + " WiFi5G windows are corrupted.");
//                }
//                checkIfAllLoaded();
//            }
//
//            @Override
//            public void onReadFailed(IOException e) {
//                Log.e(TAG, "Failed to read WiFi5G windows.", e);
//            }
//        })) wifi5gLoaded = true;
//        checkIfAllLoaded();
//    }
//
//    void checkIfAllLoaded() {
//        if (btLoaded && btleLoaded && wifiLoaded && wifi5gLoaded) {
//            callback.onWindowsLoaded();
//        }
//    }
//
//    public List<WindowRecord> getBt() { return bt; }
//    public List<WindowRecord> getBtle() { return btle; }
//    public List<WindowRecord> getWifi() { return wifi; }
//    public List<WindowRecord> getWifi5g() { return wifi5g; }
//    public List<WindowRecord> getAll() {
//        List<WindowRecord> list = new ArrayList<>();
//        list.addAll(bt);
//        list.addAll(btle);
//        list.addAll(wifi);
//        list.addAll(wifi5g);
//        return list;
//    }
//
//    public void addBt(WindowRecord window) {
//        btRW.writeRecords(Collections.singletonList(window), true, new AbsTextReaderWriter.WriteListener() {
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
//        bt.add(window);
//    }
//    public void addBtle(WindowRecord window) {
//        btleRW.writeRecords(Collections.singletonList(window), true, new AbsTextReaderWriter.WriteListener() {
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
//        btle.add(window);
//    }
//    public void addWifi(WindowRecord window) {
//        wifiRW.writeRecords(Collections.singletonList(window), true, new AbsTextReaderWriter.WriteListener() {
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
//        wifi.add(window);
//    }
//    public void addWifi5g(WindowRecord window) {
//        wifi5gRW.writeRecords(Collections.singletonList(window), true, new AbsTextReaderWriter.WriteListener() {
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
//        wifi5g.add(window);
//    }
//}
