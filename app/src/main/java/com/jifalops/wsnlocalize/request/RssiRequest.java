package com.jifalops.wsnlocalize.request;

import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RssiRequest extends AbsRequest {
    public static class RssiFilter {
        public interface FilterCallback {
            void onRecordReady(RssiRecord record);
        }
        private final FilterCallback callback;
        private final List<RssiRecord> records = new ArrayList<>();
        private final int maxTimeMillis, maxCount;
        private long startTimeNanos;
        public RssiFilter(int maxTimeMillis, int maxCount, FilterCallback callback) {
            this.maxTimeMillis = maxTimeMillis;
            this.maxCount = maxCount;
            this.callback = callback;
        }
        public void add(RssiRecord record) {
            records.add(record);
            checkLimits();
        }
        private void checkLimits() {
            if (startTimeNanos == 0) startTimeNanos = System.nanoTime();
            long time = (System.nanoTime() - startTimeNanos) / 1_000_000;
            if (time >= maxTimeMillis || records.size() >= maxCount) {
                callback.onRecordReady(findBestRecord());
                records.clear();
                startTimeNanos = 0;
            }
        }
        private RssiRecord findBestRecord() {
            RssiRecord best = records.get(0);
            for (RssiRecord r : records) {
                if (r.rssi > best.rssi) {
                    best = r;
                }
            }
            return best;
        }
    }

    public static class RssiRecord {
        public final String localMac, remoteMac, remoteDesc, rssiMethod, time;
        public final int rssi, freq;
        public final float distance;
        public RssiRecord(String localMac, String remoteMac, String remoteDesc, String rssiMethod,
                          int rssi, int freq, float distance, String time) {
            this.localMac = localMac;
            this.remoteMac = remoteMac;
            this.remoteDesc = remoteDesc;
            this.rssiMethod = rssiMethod;
            this.rssi = rssi;
            this.freq = freq;
            this.distance = distance;
            this.time = time;
        }

        public RssiRecord(String jsonObject) throws JSONException {
            JSONObject json = new JSONObject(jsonObject);
            localMac = json.getString("localMac");
            remoteMac = json.getString("remoteMac");
            remoteDesc = json.getString("remoteDesc");
            rssiMethod = json.getString("method");
            rssi = json.getInt("rssi");
            freq = json.getInt("freq");
            distance = (float) json.getDouble("distance");
            time = json.getString("time");
        }

        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            try {
                json.put("localMac", localMac);
                json.put("remoteMac", remoteMac);
                json.put("remoteDesc", remoteDesc);
                json.put("method", rssiMethod);
                json.put("rssi", rssi);
                json.put("freq", freq);
                json.put("distance", distance);
                json.put("time", time);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
    }

    private final List<RssiRecord> records;

    public RssiRequest(List<RssiRecord> records, Response.Listener<MyResponse>listener,
                       Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.records = records;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put(AbsRequest.REQUEST_TYPE, "rssi");
        for (int i = 0, size = records.size(); i < size; i++) {
            params.put(i+"", records.get(i).toString());
        }
        return params;
    }
}
