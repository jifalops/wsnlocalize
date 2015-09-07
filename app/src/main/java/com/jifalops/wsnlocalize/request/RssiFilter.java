package com.jifalops.wsnlocalize.request;

import com.jifalops.wsnlocalize.data.RssiRecordOld;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RssiFilter {
    public interface FilterCallback {
        void onRecordReady(RssiRecordOld record, int recordsFiltered, long elapsedMillis);
    }

    private static class DeviceRssiList extends ArrayList<RssiRecordOld> {
        long startTimeNanos = 0;

        @Override
        public boolean add(RssiRecordOld r) {
            if (startTimeNanos == 0) startTimeNanos = System.nanoTime();
            return super.add(r);
        }

        @Override
        public void clear() {
            super.clear();
            startTimeNanos = 0;
        }

        RssiRecordOld findBestRecord() {
            RssiRecordOld best = get(0);
            for (RssiRecordOld r : this) {
                if (r.rssi > best.rssi) {
                    best = r;
                }
            }
            return best;
        }

        boolean isDeviceForRecord(RssiRecordOld r) {
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

    private DeviceRssiList addAndGetDevice(RssiRecordOld record) {
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

    public void add(RssiRecordOld record) {
        DeviceRssiList d = addAndGetDevice(record);
        checkLimits(d);
    }

    private void checkLimits(DeviceRssiList d) {
        long time = (System.nanoTime() - d.startTimeNanos) / 1_000_000;
        if (time >= minTimeMillis && d.size() >= minCount) {
            callback.onRecordReady(d.findBestRecord(), d.size() - 1, time);
            d.clear();
        }
    }

}
