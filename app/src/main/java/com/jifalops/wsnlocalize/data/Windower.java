package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;
import java.util.List;

/**
 *  Collects a list of {@link RssiRecord} until the count AND time limits are reached.
 *  It then calls {@link Callback#onWindowRecordReady(WindowRecord)} and clears the internal list.
 */
public class Windower {
    public interface Callback {
        void onWindowRecordReady(WindowRecord record);
    }
    public final int minCount;
    public final int minElapsedMillis;
    private final List<RssiRecord> records = new ArrayList<>();
    private final Callback callback;
    public Windower(int minCount, int minElapsedMillis, Callback callback) {
        this.minCount = minCount;
        this.minElapsedMillis = minElapsedMillis;
        this.callback = callback;
    }

    public void add(RssiRecord record) {
        records.add(record);
        if (records.size() >= minCount &&
                (records.get(records.size()-1).time - records.get(0).time) >= minElapsedMillis) {
            callback.onWindowRecordReady(new WindowRecord(records));
            records.clear();
        }
    }
}
