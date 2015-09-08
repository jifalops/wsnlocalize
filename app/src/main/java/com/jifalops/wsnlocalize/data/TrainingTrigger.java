package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;
import java.util.List;

/**
 *  Collects a list of {@link WindowRecord} until the count AND time limits are reached.
 *  It then calls {@link Callback#onTimeToTrain(List)} and clears the internal list.
 */
public class TrainingTrigger {
    public interface Callback {
        void onTimeToTrain(List<WindowRecord> records);
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
            callback.onTimeToTrain(records);
            records.clear();
            startTime = 0;
        }
    }
}
