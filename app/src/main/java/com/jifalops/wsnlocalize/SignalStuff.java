package com.jifalops.wsnlocalize;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.wsnlocalize.data.Limits;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.Trainer;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.data.WindowScaler;
import com.jifalops.wsnlocalize.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.file.RssiReaderWriter;
import com.jifalops.wsnlocalize.file.WindowReaderWriter;
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
class SignalStuff {
    public interface SignalCallbacks {
        void onTrainingStarting(SignalStuff s, int samples);
        void onTrainingComplete(SignalStuff s, double[] weights, double error, int samples);
        void onWindowReady(SignalStuff s, WindowRecord record);
        void onSentSuccess(SignalStuff s, boolean wasRssi, int count);
        void onSentFailure(SignalStuff s, boolean wasRssi, int count, int respCode, String resp, String result);
        void onSentFailure(SignalStuff s, boolean wasRssi, int count, String volleyError);
    }

    private final String signalType;
    private final Trainer trainer;
    private final RssiReaderWriter rssiRW;
    private final WindowReaderWriter windowRW;
    private final NumberReaderWriter sampleRW;
    private final NumberReaderWriter weightRW;
    private final List<RssiRecord> rssi = new ArrayList<>();
    private final List<WindowRecord> windows = new ArrayList<>();
    private final SignalCallbacks callbacks;
    private double[][] toTrain, weightHistory;
    boolean enabled; // used by activity

    SignalStuff(String signalType, File dir, Limits rssiLimits, Limits windowLimits,
                SignalCallbacks callbacks) {
        this.signalType = signalType;
        trainer = new Trainer(rssiLimits, windowLimits, myCallbacks);
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

    public String getSignalType() {
        return signalType;
    }

    public int getRssiCount() {
        return rssi.size();
    }
    public int getWindowCount() {
        return windows.size();
    }

    public void truncate() {
        rssiRW.truncate();
        windowRW.truncate();
        sampleRW.truncate();
        weightRW.truncate();
    }

    public void close() {
        rssiRW.close();
        windowRW.close();
        sampleRW.close();
        weightRW.close();
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
                                callbacks.onSentSuccess(SignalStuff.this, true, toSend);
                            } else {
                                rssi.addAll(sending);
                                callbacks.onSentFailure(SignalStuff.this, true, toSend,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            rssi.addAll(sending);
                            callbacks.onSentFailure(SignalStuff.this, true, toSend,
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
                                callbacks.onSentSuccess(SignalStuff.this, false, toSend);
                            } else {
                                windows.addAll(sending);
                                callbacks.onSentFailure(SignalStuff.this, false, toSend,
                                        response.responseCode, response.responseMessage,
                                        response.queryResult);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    windows.addAll(sending);
                    callbacks.onSentFailure(SignalStuff.this, false, toSend, volleyError.toString());
                }
            }));
        }
    }


    private static abstract class MyCallbacks extends Trainer.TrainingCallbacks implements
            RssiReaderWriter.RssiCallbacks, WindowReaderWriter.WindowCallbacks,
            NumberReaderWriter.NumberCallbacks {}

    private final MyCallbacks myCallbacks = new MyCallbacks() {
        @Override
        public void onRssiRecordsRead(RssiReaderWriter rw, List<RssiRecord> records) {
            rssi.clear();
            rssi.addAll(records);
        }

        @Override
        public void onRssiRecordsWritten(RssiReaderWriter rw, int recordsWritten) {

        }

        @Override
        public void onWindowRecordsRead(WindowReaderWriter rw, List<WindowRecord> records) {
            windows.clear();
            windows.addAll(records);
        }

        @Override
        public void onWindowRecordsWritten(WindowReaderWriter rw, int recordsWritten) {

        }

        @Override
        public void onNumbersRead(NumberReaderWriter rw, double[][] numbers) {
            if (rw == sampleRW) {
                toTrain = numbers;
            } else if (rw == weightRW) {
                weightHistory = numbers;
            }
        }

        @Override
        public void onNumbersWritten(NumberReaderWriter rw, int recordsWritten) {

        }

        @Override
        public void onWindowRecordReady(WindowRecord record, List<RssiRecord> from) {
            windows.add(record);
            List<WindowRecord> list = new ArrayList<>();
            list.add(record);
            windowRW.writeRecords(list, true);
            rssiRW.writeRecords(from, true);

            if (weightHistory != null && weightHistory.length > 0) {
                double[][] sample = new double[][] {record.toTrainingArray()};
                double[][] scaled = WindowScaler.scale(sample);
                double[] outputs = trainer.nnet.calcOutputs(
                        weightHistory[weightHistory.length-1], scaled[0]);
                double estimate = WindowScaler.unscale(outputs)[0];
                record.estimated = (float) estimate;
            }
            callbacks.onWindowReady(SignalStuff.this, record);
        }

        @Override
        public double[][] onTimeToTrain(List<WindowRecord> records, double[][] samples) {
            samples = WindowScaler.scale(samples);
            sampleRW.writeNumbers(samples, true);
            if (toTrain == null) {
                toTrain = samples;
            }
            else {
                toTrain = Arrays.concat(toTrain, samples);
            }
            toTrain = WindowScaler.randomize(toTrain);
            callbacks.onTrainingStarting(SignalStuff.this, toTrain.length);
            return toTrain;
        }

        @Override
        public void onTrainingComplete(double[] weights, double error, int samples) {
            double[][] tmp = new double[][] { weights };
            weightRW.writeNumbers(tmp, true);
            if (weightHistory == null) {
                weightHistory = tmp;
            } else {
                Arrays.concat(weightHistory, tmp);
            }
            callbacks.onTrainingComplete(SignalStuff.this, weights, error, samples);
        }
    };
}
