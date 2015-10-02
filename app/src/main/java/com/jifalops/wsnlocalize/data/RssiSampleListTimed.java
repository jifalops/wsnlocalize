package com.jifalops.wsnlocalize.data;

import com.jifalops.wsnlocalize.toolbox.neuralnet.SampleList;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RssiSampleListTimed extends SampleList<RssiSampleTimed> {

    public RssiSampleListTimed() {}

    public RssiSampleListTimed(int capacity) {
        super(capacity);
    }

    public RssiSampleListTimed(List<double[]> samples) {
        for (double[] a : samples) {
            add(new RssiSampleTimed(a));
        }
    }

    public RssiSampleListTimed(RssiList rssi, SampleWindow window) {
        Map<Double, RssiList> distances = rssi.splitByDistance();
        Map<String, RssiList> macs;
        RssiList sample = new RssiList();
        // For each distance...
        for (RssiList distList : distances.values()) {
            macs = distList.splitByMac();
            // For each MAC...
            for (RssiList macList : macs.values()) {
                macList.sortByTime();
                // Here we have a chronological list of rssi that came
                // from a single device at a single distance.
                for (Rssi r : macList) {
                    sample.add(r);
                    if (limitsReached(sample, window)) {
                        add(new RssiSampleTimed(sample));
                        sample.clear();
                    }
                }
                if (sample.size() >= window.minCount) {
                    add(new RssiSampleTimed(sample));
                }
                sample.clear();
            }
        }
    }

    private boolean limitsReached(RssiList rssi, SampleWindow window) {
        int len = rssi.size();
        // stats not possible on a single rssi.
        return len >= 2 && window.reached(len, rssi.get(len - 1).time - rssi.get(0).time);
    }

    public void sortByDistance() {
        Collections.sort(this, new Comparator<RssiSample>() {
            @Override
            public int compare(RssiSample lhs, RssiSample rhs) {
                if (lhs.distance < rhs.distance) return -1;
                if (lhs.distance > rhs.distance) return 1;
                return 0;
            }
        });
    }

    public Map<Double, RssiSampleListTimed> splitByDistance() {
        Map<Double, RssiSampleListTimed> map = new HashMap<>();
        RssiSampleListTimed list;
        for (RssiSampleTimed s : this) {
            list = map.get(s.distance);
            if (list == null) {
                list = new RssiSampleListTimed();
                map.put(s.distance, list);
            }
            list.add(s);
        }
        return map;
    }
}
