package com.jifalops.wsnlocalize.data;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class RssiRecord {
    public static final String SIGNAL_BT = "bt";
    public static final String SIGNAL_BTLE = "btle";
    public static final String SIGNAL_WIFI = "wifi";

    public final int rssi, freq;
    public final double distance;
    public final long time;

    public RssiRecord(int rssi, int freq, long time, double distance) {
        this.rssi = rssi;
        this.freq = freq;
        this.distance = distance;
        this.time = time;
    }

    public RssiRecord(String[] csv) {
        rssi = Integer.valueOf(csv[0]);
        freq = Integer.valueOf(csv[1]);
        distance = Double.valueOf(csv[2]);
        time = Long.valueOf(csv[3]);
    }

    @Override
    public String toString() {
        return rssi +","+ freq +","+ distance +","+ time;
    }
}
