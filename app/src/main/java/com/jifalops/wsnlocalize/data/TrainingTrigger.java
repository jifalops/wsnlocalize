package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;
import java.util.List;

/**
 *  Collects a list of {@link WindowRecord} until the count OR time limits are reached.
 *  It then calls {@link Callback#onTimeToTrain(List, double[][])} and clears the internal list.
 */
public class TrainingTrigger {
    public interface Callback {
        /**
         * @param samples format of the WindowRecords to be used for training in a neural network.
         */
        double[][] onTimeToTrain(List<WindowRecord> records, double[][] samples);
    }

    private final Limits limits;
    private final List<WindowRecord> records = new ArrayList<>();
    private final Callback callback;
    private long startTime = 0;
    public TrainingTrigger(Limits limits, Callback callback) {
        this.limits = limits;
        this.callback = callback;
    }

    public void add(WindowRecord record) {
        records.add(record);
        if (startTime == 0) startTime = System.nanoTime();
        if (limits.reached(records.size(), (System.nanoTime() - startTime) / 1_000_000)) {
            double[][] samples = makeSamples(records);
            callback.onTimeToTrain(records, samples);
            records.clear();
            startTime = 0;
        }
    }

    public static double[][] makeSamples(List<WindowRecord> records) {
        int len = records.size();
        double[][] samples = new double[len][WindowRecord.TRAINING_ARRAY_SIZE];
        for (int i = 0; i < len; ++i) {
            samples[i] = records.get(i).toTrainingArray();
        }
        return samples;
    }
}
