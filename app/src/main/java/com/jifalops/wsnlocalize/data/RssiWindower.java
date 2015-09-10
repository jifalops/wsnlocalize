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
    public final Limits limits;
    private final List<RssiRecord> records = new ArrayList<>();
    private final Callback callback;
    public RssiWindower(Limits limits, Callback callback) {
        this.limits = limits;
        this.callback = callback;
    }

    public void add(RssiRecord record) {
        records.add(record);
        if (limits.reached(records.size(), record.time - records.get(0).time)) {
            callback.onWindowRecordReady(new WindowRecord(records), records);
            records.clear();
        }
    }
}
