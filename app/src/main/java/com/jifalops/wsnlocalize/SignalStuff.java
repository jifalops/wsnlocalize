package com.jifalops.wsnlocalize;

import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.Trainer;
import com.jifalops.wsnlocalize.data.WindowRecord;
import com.jifalops.wsnlocalize.data.WindowScaler;
import com.jifalops.wsnlocalize.file.NumberReaderWriter;
import com.jifalops.wsnlocalize.file.RssiReaderWriter;
import com.jifalops.wsnlocalize.file.WindowReaderWriter;
import com.jifalops.wsnlocalize.request.AbsRequest;
import com.jifalops.wsnlocalize.request.RssiRequest;
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
        void onEstimateReady(SignalStuff s, double estimate, double error, WindowRecord record);
    }


    private final Trainer trainer;
    private final RssiReaderWriter rssiRW;
    private final WindowReaderWriter windowRW;
    private final NumberReaderWriter sampleRW;
    private final NumberReaderWriter weightRW;
    private final List<RssiRecord> rssi = new ArrayList<>();
    private final List<WindowRecord> windows = new ArrayList<>();
    private final SignalCallbacks callbacks;
    private double[][] toTrain, weightHistory;
    boolean enabled;

    SignalStuff(String signalType, File dir,
                int minRssiCountForWindow, int minRssiTimeMillisForWindow,
                int minWindowCountForTraining, int minWindowTimeMillisForTraining,
                SignalCallbacks callbacks) {
        trainer = new Trainer(minRssiCountForWindow, minRssiTimeMillisForWindow,
                minWindowCountForTraining, minWindowTimeMillisForTraining, myCallbacks);
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
        rssi.add(record);
        trainer.add(record);
    }

    public void close() {
        rssiRW.close();
        windowRW.close();
        sampleRW.close();
        weightRW.close();
    }

    private void send() {
//        final List<RssiRecordOld> records = new ArrayList<>(rssiRecords);
//        rssiRecords.clear();
//        toSendCountView.setText("0");
//        App.getInstance().sendRequest(new RssiRequest(records,
//                new Response.Listener<AbsRequest.MyResponse>() {
//                    @Override
//                    public void onResponse(AbsRequest.MyResponse response) {
//                        if (response.responseCode == 200) {
//                            Toast.makeText(RssiActivity.this,
//                                    "Sent " + records.size() + " records successfully",
//                                    Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(RssiActivity.this,
//                                    response.responseCode + ": " + response.responseMessage +
//                                            " Result: " + response.queryResult,
//                                    Toast.LENGTH_LONG).show();
//                            rssiRecords.addAll(records);
//                            toSendCountView.setText(rssiRecords.size() + "");
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                Toast.makeText(RssiActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
//                rssiRecords.addAll(records);
//                toSendCountView.setText(rssiRecords.size()+"");
//            }
//        }));
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
                double[][] sample = new double[][] {record.toArray()};
                double[][] scaled = WindowScaler.scale(sample);
                double[] outputs = trainer.nnet.calcOutputs(
                        weightHistory[weightHistory.length-1], scaled[0]);
                double estimate = WindowScaler.unscale(outputs)[0];
                double error = (estimate - record.distance) / record.distance;
                callbacks.onEstimateReady(SignalStuff.this, estimate, error, record);
            }
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
