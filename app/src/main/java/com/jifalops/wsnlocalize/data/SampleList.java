package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SampleList extends ArrayList<Sample> {

    public SampleList() {}

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
                for (Rssi r : macList) {
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

    private boolean limitsReached(RssiList rssi, Sample.Window window) {
        int len = rssi.size();
        // stats not possible on a single rssi.
        return len >= 2 && window.reached(len, rssi.get(len - 1).time - rssi.get(0).time);
    }

    public void sortByDistance() {
        Collections.sort(this, new Comparator<Sample>() {
            @Override
            public int compare(Sample lhs, Sample rhs) {
                if (lhs.distance < rhs.distance) return -1;
                if (lhs.distance > rhs.distance) return 1;
                return 0;
            }
        });
    }

    public Map<Double, SampleList> splitByDistance() {
        Map<Double, SampleList> map = new HashMap<>();
        SampleList list;
        for (Sample s : this) {
            list = map.get(s.distance);
            if (list == null) {
                list = new SampleList();
                map.put(s.distance, list);
            }
            list.add(s);
        }
        return map;
    }
}
