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
    public final String signal;

    public RssiRecord(String signal, int rssi, int freq, long time, float distance) {
        this.signal = signal;
        this.rssi = rssi;
        this.freq = freq;
        this.distance = distance;
        this.time = time;
    }

    public RssiRecord(String jsonObject) throws JSONException {
        JSONObject json = new JSONObject(jsonObject);
        signal = json.getString("signal");
        rssi = json.getInt("rssi");
        freq = json.getInt("freq");
        distance = (float) json.getDouble("distance");
        time = json.getLong("time");
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("signal", signal);
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
