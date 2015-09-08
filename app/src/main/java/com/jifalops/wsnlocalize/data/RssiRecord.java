package com.jifalops.wsnlocalize.data;

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
    public final float distance;
    public final long time;

    public RssiRecord(int rssi, int freq, long time, float distance) {
        this.rssi = rssi;
        this.freq = freq;
        this.distance = distance;
        this.time = time;
    }

    public RssiRecord(String jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject);
        rssi = json.getInt("rssi");
        freq = json.getInt("freq");
        distance = (float) json.getDouble("distance");
        time = json.getLong("time");
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
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
