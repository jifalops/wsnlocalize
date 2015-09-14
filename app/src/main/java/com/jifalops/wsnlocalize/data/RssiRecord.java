package com.jifalops.wsnlocalize.data;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class RssiRecord {
    public final String mac;
    public final int rssi, freq;
    public final double distance;
    public final long time;

    public RssiRecord(String mac, int rssi, int freq, long time, double distance) {
        this.mac = mac;
        this.rssi = rssi;
        this.freq = freq;
        this.distance = distance;
        this.time = time;
    }

    public RssiRecord(String[] csv) {
        mac = csv[0];
        rssi = Integer.valueOf(csv[1]);
        freq = Integer.valueOf(csv[2]);
        distance = Double.valueOf(csv[3]);
        time = Long.valueOf(csv[4]);
    }

    @Override
    public String toString() {
        return mac +","+ rssi +","+ freq +","+ distance +","+ time;
    }
}
