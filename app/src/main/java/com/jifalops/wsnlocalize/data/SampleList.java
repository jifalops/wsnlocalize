package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SampleList extends ArrayList<Sample> {

    public SampleList(RssiList rssi, Sample.Window window) {
        Map<Double, RssiList> distances = rssi.splitByDistance();
        Map<String, RssiList> macs;
        RssiList sample = new RssiList();
        for (RssiList distList : distances.values()) {
            macs = distList.splitByMac();
            for (RssiList macList : macs.values()) {
                macList.sortByTime();
                // Here we have a chronological list of rssi that came
                // from a single device at a single distance.
                for (RssiRecord r : macList) {
                    sample.add(r);
                    if (limitsReached(sample, window)) {
                        add(new Sample(sample));
                        sample.clear();
                    }
                }
                if (sample.size() >= window.minCount) {
                    add(new Sample(sample));
                }
                sample.clear();
            }
        }
    }

    private boolean limitsReached(List<RssiRecord> rssi, Sample.Window window) {
        int len = rssi.size();
        if (len < 2) return false; // stats not possible on a single rssi.
        return window.reached(len, rssi.get(len - 1).time - rssi.get(0).time);
    }
}
