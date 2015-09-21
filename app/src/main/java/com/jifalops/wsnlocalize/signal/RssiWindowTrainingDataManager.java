package com.jifalops.wsnlocalize.signal;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.toolbox.Arrays;
import com.jifalops.toolbox.ResettingList;
import com.jifalops.toolbox.file.NumberReaderWriter;
import com.jifalops.toolbox.neuralnet.TrainingResults;
import com.jifalops.wsnlocalize.App;
import com.jifalops.wsnlocalize.data.Estimator;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.file.EstimatorReaderWriter;
import com.jifalops.wsnlocalize.file.RssiReaderWriter;
import com.jifalops.wsnlocalize.file.WindowReaderWriter;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.EstimatorRequest;
import com.jifalops.wsnlocalize.request.RssiRequest;
import com.jifalops.wsnlocalize.request.WindowRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiWindowTrainingDataManager {
    public interface Callbacks {
        void onRssiLoadedFromDisk(String signal, List<RssiRecord> rssiList);
        void onWindowsLoadedFromDisk(String signal, List<WindowRecord> windows);
        void onEstimatorsLoadedFromDisk(String signal, List<Estimator> estimators);
        void onTrainingStarting(String signal, int samples);
        void onGenerationFinished(String signal, int gen, double best, double mean, double stdDev);
        void onTrainingComplete(String signal, Estimator estimator);
        void onWindowReady(String signal, WindowRecord record);
        void onSentSuccess(String signal, String dataType, int count);
        void onSentFailure(String signal, String dataType, int count, int respCode, String resp, String result);
        void onSentFailure(String signal, String dataType, int count, String volleyError);
    }

    final String signalType;
    final Trainer trainer;

    final RssiReaderWriter rssiRW;
    final WindowReaderWriter windowRW;
    final NumberReaderWriter sampleRW;
    final EstimatorReaderWriter estimatorRW;

    final List<RssiRecord> rssi = new ArrayList<>();
    final List<WindowRecord> windows = new ArrayList<>();
    final List<Estimator> estimators = new ArrayList<>();

    Estimator estimator;
    final double maxEstimate;

    final Callbacks callbacks;
    private double[][] toTrain;


    RssiWindowTrainingDataManager(String signalType, File dir, ResettingList.Trigger rssiWindowTrigger,
                                  ResettingList.Trigger windowTrainingTrigger, Callbacks callbacks) {
        this.signalType = signalType;
        MyCallbacks myCallbacks = new MyCallbacks();
        trainer = new Trainer(rssiWindowTrigger, windowTrainingTrigger, myCallbacks);
        rssiRW = new RssiReaderWriter(new File(dir,
                App.getFileName(signalType, App.DATA_RSSI)), myCallbacks);
        windowRW = new WindowReaderWriter(new File(dir,
                App.getFileName(signalType, App.DATA_WINDOW)), myCallbacks);
        sampleRW = new NumberReaderWriter(new File(dir,
                App.getFileName(signalType, App.DATA_SAMPLES)), myCallbacks);
        estimatorRW = new EstimatorReaderWriter(new File(dir,
                App.getFileName(signalType, App.DATA_ESTIMATOR)), myCallbacks);
        this.callbacks = callbacks;
        rssiRW.readRecords();
        windowRW.readRecords();
        sampleRW.readNumbers();
        estimatorRW.readLines();
        maxEstimate = (signalType == App.SIGNAL_BT || signalType == App.SIGNAL_BTLE)
                ? Estimator.BT_MAX
                : Estimator.WIFI_MAX;
    }

    public void add(RssiRecord record) {
        if (record.distance > 0) {
            rssi.add(record);
            trainer.add(record);
        }
    }

    public double[][] getSamples() {
        return toTrain;
    }

    public int getRssiCount() {
        return rssi.size();
    }
    public int getWindowCount() {
        return windows.size();
    }

    public int getEstimatorCount() {
        return estimators.size();
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
        sampleRW.truncate();
    }

    public void clearEstimators() {
        estimators.clear();
        estimator = null;
        estimatorRW.truncate();
    }

    public void close() {
//        rssiRW.close();
//        windowRW.close();
//        sampleRW.close();
//        estimatorRW.close();
        trainer.close();
    }

    public void send() {
        if (rssi.size() > 0) {
            final List<RssiRecord> sending = new ArrayList<>(rssi);
            final int toSend = sending.size();
            rssi.clear();
            App.sendRequest(new RssiRequest(signalType, rssi,
                    new Response.Listener<AbsRequest.MyResponse>() {
                        @Override
                        public void onResponse(AbsRequest.MyResponse response) {
                            if (response.responseCode == 200) {
                                rssiRW.writeRecords(rssi, false);
                                callbacks.onSentSuccess(signalType, App.DATA_RSSI, toSend);
                            } else {
                                rssi.addAll(sending);
                                callbacks.onSentFailure(signalType, App.DATA_RSSI, toSend,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    rssi.addAll(sending);
                    callbacks.onSentFailure(signalType, App.DATA_RSSI, toSend,
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
                                callbacks.onSentSuccess(signalType, App.DATA_WINDOW, toSend);
                            } else {
                                windows.addAll(sending);
                                callbacks.onSentFailure(signalType, App.DATA_WINDOW, toSend,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    windows.addAll(sending);
                    callbacks.onSentFailure(signalType, App.DATA_WINDOW, toSend, volleyError.toString());
                }
            }));
        }
        if (estimators.size() > 0) {
            final List<Estimator> sending = new ArrayList<>(estimators);
            final int toSend = sending.size();
            estimators.clear();
            App.getInstance().sendRequest(new EstimatorRequest(signalType, estimators,
                    new Response.Listener<AbsRequest.MyResponse>() {
                        @Override
                        public void onResponse(AbsRequest.MyResponse response) {
                            if (response.responseCode == 200) {
                                estimatorRW.writeRecords(estimators, false);
                                callbacks.onSentSuccess(signalType, App.DATA_ESTIMATOR, toSend);
                            } else {
                                estimators.addAll(sending);
                                callbacks.onSentFailure(signalType, App.DATA_ESTIMATOR, toSend,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    estimators.addAll(sending);
                    callbacks.onSentFailure(signalType, App.DATA_ESTIMATOR, toSend, volleyError.toString());
                }
            }));
        }
    }


    private class MyCallbacks implements Trainer.TrainerCallbacks,
            RssiReaderWriter.RssiCallbacks, WindowReaderWriter.WindowCallbacks,
            NumberReaderWriter.NumberCallbacks, EstimatorReaderWriter.EstimatorCallbacks {
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
            toTrain = numbers;
        }

        @Override
        public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {
//            callbacks.onDataFileWrite(rw);
        }

        @Override
        public void onWindowRecordReady(WindowRecord record, List<RssiRecord> from) {
            rssiRW.writeRecords(from, true);

            if (estimator != null) {
                record.estimated = estimator.estimate(record.toSample());
            }

            windows.add(record);
            List<WindowRecord> list = new ArrayList<>();
            list.add(record);
            windowRW.writeRecords(list, true);

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
        public void onTrainingComplete(TrainingResults results) {
            estimator = new Estimator(results, maxEstimate);
            estimators.add(estimator);

            List<Estimator> tmp = new ArrayList<>();
            tmp.add(estimator);
            estimatorRW.writeRecords(tmp, true);

            callbacks.onTrainingComplete(signalType, estimator);
        }

        @Override
        public void onEstimatorRecordsRead(EstimatorReaderWriter rw, List<Estimator> records) {
            estimators.clear();
            estimators.addAll(records);
            if (records.size() > 0) {
                estimator = estimators.get(estimators.size() - 1);
            }
            callbacks.onEstimatorsLoadedFromDisk(signalType, records);
        }

        @Override
        public void onEstimatorRecordsWritten(EstimatorReaderWriter rw, int recordsWritten) {

        }
    }

}
