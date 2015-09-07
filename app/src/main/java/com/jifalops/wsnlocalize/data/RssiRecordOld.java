package com.jifalops.wsnlocalize.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class RssiRecordOld {
    public final String localMac, remoteMac, remoteDesc, rssiMethod, time;
    public final int rssi, freq;
    public final float distance;

    public RssiRecordOld(String localMac, String remoteMac, String remoteDesc, String rssiMethod,
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

    public RssiRecordOld(String jsonObject) throws JSONException {
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
