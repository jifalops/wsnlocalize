package com.jifalops.wsnlocalize.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class RssiList extends ArrayList<RssiRecord> {

    public void sortByMac() {
        Collections.sort(this, new Comparator<RssiRecord>() {
            @Override
            public int compare(RssiRecord lhs, RssiRecord rhs) {
                return lhs.mac.compareToIgnoreCase(rhs.mac);
            }
        });
    }

    public void sortByRssi() {
        Collections.sort(this, new Comparator<RssiRecord>() {
            @Override
            public int compare(RssiRecord lhs, RssiRecord rhs) {
                return lhs.rssi - rhs.rssi;
            }
        });
    }

    public void sortByFreq() {
        Collections.sort(this, new Comparator<RssiRecord>() {
            @Override
            public int compare(RssiRecord lhs, RssiRecord rhs) {
                return lhs.freq - rhs.freq;
            }
        });
    }

    public void sortByTime() {
        Collections.sort(this, new Comparator<RssiRecord>() {
            @Override
            public int compare(RssiRecord lhs, RssiRecord rhs) {
                if (lhs.time < rhs.time) return -1;
                if (lhs.time > rhs.time) return 1;
                return 0;
            }
        });
    }

    public void sortByDistance() {
        Collections.sort(this, new Comparator<RssiRecord>() {
            @Override
            public int compare(RssiRecord lhs, RssiRecord rhs) {
                if (lhs.distance < rhs.distance) return -1;
                if (lhs.distance > rhs.distance) return 1;
                return 0;
            }
        });
    }

    public Map<String, RssiList> splitByMac() {
        Map<String, RssiList> map = new HashMap<>();
        RssiList list;
        String mac;
        for (RssiRecord r : this) {
            mac = r.mac.toUpperCase(Locale.US);
            list = map.get(mac);
            if (list == null) {
                list = new RssiList();
                map.put(mac, list);
            }
            list.add(r);
        }
        return map;
    }

    public Map<Integer, RssiList> splitByRssi() {
        Map<Integer, RssiList> map = new HashMap<>();
        RssiList list;
        for (RssiRecord r : this) {
            list = map.get(r.rssi);
            if (list == null) {
                list = new RssiList();
                map.put(r.rssi, list);
            }
            list.add(r);
        }
        return map;
    }

    public Map<Integer, RssiList> splitByFreq() {
        Map<Integer, RssiList> map = new HashMap<>();
        RssiList list;
        for (RssiRecord r : this) {
            list = map.get(r.freq);
            if (list == null) {
                list = new RssiList();
                map.put(r.freq, list);
            }
            list.add(r);
        }
        return map;
    }

    public Map<Double, RssiList> splitByDistance() {
        Map<Double, RssiList> map = new HashMap<>();
        RssiList list;
        for (RssiRecord r : this) {
            list = map.get(r.distance);
            if (list == null) {
                list = new RssiList();
                map.put(r.distance, list);
            }
            list.add(r);
        }
        return map;
    }
}
