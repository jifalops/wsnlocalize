package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;
import java.util.List;

/**
 *  Collects a list of {@link WindowRecord} until the count AND time limits are reached.
 *  It then calls {@link Callback#onTimeToTrain(List, double[][])} and clears the internal list.
 */
public class TrainingTrigger {
    public interface Callback {
        /**
         * @param samples format of the WindowRecords to be used for training in a neural network.
         */
        void onTimeToTrain(List<WindowRecord> records, double[][] samples);
    }
    public final int minCount;
    public final long minElapsedMillis;
    private final List<WindowRecord> records = new ArrayList<>();
    private final Callback callback;
    private long startTime = 0;
    public TrainingTrigger(int minCount, int minElapsedMillis, Callback callback) {
        this.minCount = minCount;
        this.minElapsedMillis = minElapsedMillis;
        this.callback = callback;
    }

    public void add(WindowRecord record) {
        records.add(record);
        if (startTime == 0) startTime = System.nanoTime();
        long time = (System.nanoTime() - startTime) / 1_000_000;
        if (records.size() >= minCount && time >= minElapsedMillis) {
            double[][] samples = new double[records.size()][13];
            for (int i = 0, len = samples.length; i < len; ++i) {
                WindowRecord r = records.get(i);
                samples[i] = new double[] {
                    r.rss.min, r.rss.max, r.rss.range,
                    r.rss.mean, r.rss.median, r.rss.stdDev,
                    r.elapsed.min, r.elapsed.max, r.elapsed.range,
                    r.elapsed.mean, r.elapsed.median, r.elapsed.stdDev,
                    r.distance
                };
            }
            callback.onTimeToTrain(records, WindowScaler.scaleAndRandomize(samples));
            records.clear();
            startTime = 0;
        }
    }
}
