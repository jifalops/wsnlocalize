package com.jifalops.wsnlocalize.request;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiFilter {
    public interface FilterCallback {
        void onRecordReady(RssiRequest.RssiRecord record, int recordsFiltered);
    }

    private static class DeviceRssiList extends ArrayList<RssiRequest.RssiRecord> {
        long startTimeNanos = 0;

        @Override
        public boolean add(RssiRequest.RssiRecord r) {
            if (startTimeNanos == 0) startTimeNanos = System.nanoTime();
            return super.add(r);
        }

        @Override
        public void clear() {
            super.clear();
            startTimeNanos = 0;
        }

        RssiRequest.RssiRecord findBestRecord() {
            RssiRequest.RssiRecord best = get(0);
            for (RssiRequest.RssiRecord r : this) {
                if (r.rssi > best.rssi) {
                    best = r;
                }
            }
            return best;
        }

        boolean isDeviceForRecord(RssiRequest.RssiRecord r) {
            return size() > 0 && get(0).remoteMac.equals(r.remoteMac);
        }
    }

    private final FilterCallback callback;
    private final List<DeviceRssiList> deviceRecords = new ArrayList<>();
    private final int minTimeMillis, minCount;

    public RssiFilter(int minTimeMillis, int minCount, FilterCallback callback) {
        this.minTimeMillis = minTimeMillis;
        this.minCount = minCount;
        this.callback = callback;
    }

    private DeviceRssiList addAndGetDevice(RssiRequest.RssiRecord record) {
        DeviceRssiList device = null;
        for (DeviceRssiList d : deviceRecords) {
            if (d.isDeviceForRecord(record)) {
                device = d;
                break;
            }
        }
        if (device == null) {
            device = new DeviceRssiList();
            deviceRecords.add(device);
        }
        device.add(record);
        return device;
    }

    public void add(RssiRequest.RssiRecord record) {
        DeviceRssiList d = addAndGetDevice(record);
        checkLimits(d);
    }

    private void checkLimits(DeviceRssiList d) {
        long time = (System.nanoTime() - d.startTimeNanos) / 1_000_000;
        if (time >= minTimeMillis && d.size() >= minCount) {
            callback.onRecordReady(d.findBestRecord(), d.size() - 1);
            d.clear();
        }
    }

}
