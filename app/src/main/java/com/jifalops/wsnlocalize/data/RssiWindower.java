package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;
import java.util.List;

/**
 *  Collects a list of {@link RssiRecord} until the count OR time limits are reached.
 *  It then calls {@link Callback#onWindowRecordReady(WindowRecord, List)} and clears the internal list.
 */
public class RssiWindower {
    public interface Callback {
        void onWindowRecordReady(WindowRecord record, List<RssiRecord> from);
    }
    public final int minCount;
    public final int minElapsedMillis;
    private final List<RssiRecord> records = new ArrayList<>();
    private final Callback callback;
    public RssiWindower(int minCount, int minElapsedMillis, Callback callback) {
        this.minCount = minCount;
        this.minElapsedMillis = minElapsedMillis;
        this.callback = callback;
    }

    public void add(RssiRecord record) {
        records.add(record);
        long elapsed = records.get(records.size()-1).time - records.get(0).time;
        if (records.size() >= minCount || elapsed >= minElapsedMillis) {
            callback.onWindowRecordReady(new WindowRecord(records), records);
            records.clear();
        }
    }
}
