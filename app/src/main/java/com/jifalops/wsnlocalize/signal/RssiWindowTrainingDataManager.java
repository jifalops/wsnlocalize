package com.jifalops.wsnlocalize.signal;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.ResettingList;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.Trainer;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.file.RssiReaderWriter;
import com.jifalops.wsnlocalize.file.TextReaderWriter;
import com.jifalops.wsnlocalize.file.WindowReaderWriter;
import com.jifalops.wsnlocalize.neuralnet.Scaler;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.RssiRequest;
import com.jifalops.wsnlocalize.request.WindowRequest;
import com.jifalops.wsnlocalize.util.Arrays;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiWindowTrainingDataManager {
    public interface Callbacks {
        void onRssiLoadedFromDisk(String signal, List<RssiRecord> records);
        void onWindowsLoadedFromDisk(String signal, List<WindowRecord> records);
        void onTrainingStarting(String signal, int samples);
        void onGenerationFinished(String signal, int gen, double best, double mean, double stdDev);
        void onTrainingComplete(String signal, double[] weights, double error, int samples, int generations);
        void onWindowReady(String signal, WindowRecord record);
        void onSentSuccess(String signal, boolean wasRssi, int count);
        void onSentFailure(String signal, boolean wasRssi, int count, int respCode, String resp, String result);
        void onSentFailure(String signal, boolean wasRssi, int count, String volleyError);
    }

    final String signalType;
    final Trainer trainer;
    final RssiReaderWriter rssiRW;
    final WindowReaderWriter windowRW;
    final NumberReaderWriter sampleRW;
    final NumberReaderWriter weightRW;
    final List<RssiRecord> rssi = new ArrayList<>();
    final List<WindowRecord> windows = new ArrayList<>();
    final Callbacks callbacks;
    private double[][] toTrain, weightHistory;
    Scaler scaler;

    RssiWindowTrainingDataManager(String signalType, File dir, ResettingList.Limits rssiWindowLimits,
                                  ResettingList.Limits windowTrainingLimits, Callbacks callbacks) {
        this.signalType = signalType;
        MyCallbacks myCallbacks = new MyCallbacks();
        trainer = new Trainer(rssiWindowLimits, windowTrainingLimits, myCallbacks);
        rssiRW = new RssiReaderWriter(new File(dir, signalType+"-rssi.csv"), myCallbacks);
        windowRW = new WindowReaderWriter(new File(dir, signalType+"-windows.csv"), myCallbacks);
        sampleRW = new NumberReaderWriter(new File(dir, signalType+"-samples.csv"), myCallbacks);
        weightRW = new NumberReaderWriter(new File(dir, signalType+"-weights.csv"), myCallbacks);
        this.callbacks = callbacks;
        rssiRW.readRecords();
        windowRW.readRecords();
        sampleRW.readNumbers();
        weightRW.readNumbers();
    }

    public void add(RssiRecord record) {
        if (record.distance > 0) {
            rssi.add(record);
            trainer.add(record);
        }
    }

    public int getRssiCount() {
        return rssi.size();
    }
    public int getWindowCount() {
        return windows.size();
    }

    public void resetCurrentWindow() {
        trainer.resetCurrentWindow();
    }

    public void clearPendingSendLists() {
        trainer.resetCurrentWindow();
        rssi.clear();
        windows.clear();
        rssiRW.truncate();
        windowRW.truncate();
    }

    public void clearTrainingSamples() {
        trainer.resetAllWindows();
        toTrain = null;
        weightHistory = null;
        sampleRW.truncate();
        weightRW.truncate();
    }

    public void close() {
        rssiRW.close();
        windowRW.close();
        sampleRW.close();
        weightRW.close();
        trainer.close();
    }

    public void send() {
        if (rssi.size() > 0) {
            final List<RssiRecord> sending = new ArrayList<>(rssi);
            final int toSend = sending.size();
            rssi.clear();
            App.getInstance().sendRequest(new RssiRequest(signalType, rssi,
                    new Response.Listener<AbsRequest.MyResponse>() {
                        @Override
                        public void onResponse(AbsRequest.MyResponse response) {
                            if (response.responseCode == 200) {
                                rssiRW.writeRecords(rssi, false);
                                callbacks.onSentSuccess(signalType, true, toSend);
                            } else {
                                rssi.addAll(sending);
                                callbacks.onSentFailure(signalType, true, toSend,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            rssi.addAll(sending);
                            callbacks.onSentFailure(signalType, true, toSend,
                                    volleyError.toString());
                        }
                    }));
        }
        if (windows.size() > 0) {
            final List<WindowRecord> sending = new ArrayList<>(windows);
            final int toSend = sending.size();
            windows.clear();
            App.getInstance().sendRequest(new WindowRequest(signalType, windows,
                    new Response.Listener<AbsRequest.MyResponse>() {
                        @Override
                        public void onResponse(AbsRequest.MyResponse response) {
                            if (response.responseCode == 200) {
                                windowRW.writeRecords(windows, false);
                                callbacks.onSentSuccess(signalType, false, toSend);
                            } else {
                                windows.addAll(sending);
                                callbacks.onSentFailure(signalType, false, toSend,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    windows.addAll(sending);
                    callbacks.onSentFailure(signalType, false, toSend, volleyError.toString());
                }
            }));
        }
    }


    private class MyCallbacks implements Trainer.TrainerCallbacks,
            RssiReaderWriter.RssiCallbacks, WindowReaderWriter.WindowCallbacks,
            NumberReaderWriter.NumberCallbacks {
        @Override
        public void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records) {
            rssi.clear();
            rssi.addAll(records);
            callbacks.onRssiLoadedFromDisk(signalType, records);
        }

        @Override
        public void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten) {
//            callbacks.onDataFileWrite(rw);
        }

        @Override
        public void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records) {
            windows.clear();
            windows.addAll(records);
            callbacks.onWindowsLoadedFromDisk(signalType, records);
        }

        @Override
        public void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten) {
//            callbacks.onDataFileWrite(rw);
        }

        @Override
        public void onNumbersRead(NumberReaderWriter rw, double[][] numbers) {
            if (rw == sampleRW) {
                toTrain = numbers;
            } else if (rw == weightRW) {
                weightHistory = numbers;
            }
//            callbacks.onDataFileRead(rw);
        }

        @Override
        public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {
//            callbacks.onDataFileWrite(rw);
        }

        @Override
        public void onWindowRecordReady(WindowRecord record, List<RssiRecord> from) {
            windows.add(record);
            List<WindowRecord> list = new ArrayList<>();
            list.add(record);
            windowRW.writeRecords(list, true);
            rssiRW.writeRecords(from, true);

            if (weightHistory != null && weightHistory.length > 0 && scaler != null) {
                double[][] sample = new double[][] {record.toTrainingArray()};
                double[][] scaled = scaler.scale(sample);
                double[] outputs = trainer.calcOutputs(
                        weightHistory[weightHistory.length-1], scaled[0]);
                double estimate = scaler.unscale(outputs)[0];
                record.estimated = estimate;
            }
            callbacks.onWindowReady(signalType, record);
        }

        @Override
        public double[][] onTimeToTrain(List<WindowRecord> records, double[][] samples) {
            sampleRW.writeNumbers(samples, true);
            if (toTrain == null) {
                toTrain = samples;
            }
            else {
                toTrain = Arrays.concat(toTrain, samples);
            }
            callbacks.onTrainingStarting(signalType, toTrain.length);
            return toTrain;
        }

        @Override
        public void onGenerationFinished(int gen, double best, double mean, double stdDev) {
            callbacks.onGenerationFinished(signalType, gen, best, mean, stdDev);
        }

        @Override
        public void onTrainingComplete(double[] weights, double error, int samples, int generations, Scaler scaler) {
            double[][] tmp = new double[][] { weights };
            RssiWindowTrainingDataManager.this.scaler = scaler;
            weightRW.writeNumbers(tmp, true);
            if (weightHistory == null) {
                weightHistory = tmp;
            } else {
                Arrays.concat(weightHistory, tmp);
            }
            callbacks.onTrainingComplete(signalType, weights, error, samples, generations);
        }
    }

}
