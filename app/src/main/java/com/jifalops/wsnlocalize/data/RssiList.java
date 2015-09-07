package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;

/**
 *
 */
public class RssiList extends ArrayList<RssiRecord> {
    public RssiList getBtRecords() {
        return getRecords(RssiRecord.SIGNAL_BT);
    }
    public RssiList getBtLeRecords() {
        return getRecords(RssiRecord.SIGNAL_BTLE);
    }
    public RssiList getWiFiRecords() {
        return getRecords(RssiRecord.SIGNAL_WIFI);
    }

    public RssiList getRecords(String signal) {
        RssiList list = new RssiList();
        for (RssiRecord r : this) {
            if (r.signal.equals(signal)) {
                list.add(r);
            }
        }
        return list;
    }
}
