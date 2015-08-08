package com.jifalops.wsnlocalize.request;

import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RssiRequest extends AbsRequest {
    public static class RssiRecord {
        public final String localWifiMac,  localBtMac,  remoteWifiMac,  remoteBtMac, remoteName, rssiMethod;
        public final float rssi, actualDistance;
        public RssiRecord(String localWifiMac, String localBtMac, String remoteWifiMac,
                          String remoteBtMac, String remoteName, String rssiMethod,
                          float rssi, float actualDistance) {
            this.localWifiMac = localWifiMac;
            this.localBtMac = localBtMac;
            this.remoteWifiMac = remoteWifiMac;
            this.remoteBtMac = remoteBtMac;
            this.remoteName = remoteName;
            this.rssiMethod = rssiMethod;
            this.rssi = rssi;
            this.actualDistance = actualDistance;
        }

        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            try {
                json.put("localWifiMac", localWifiMac);
                json.put("localBtMac", localBtMac);
                json.put("remoteWifiMac", remoteWifiMac);
                json.put("remoteBtMac", remoteBtMac);
                json.put("remoteName", remoteName);
                json.put("method", rssiMethod);
                json.put("rssi", rssi);
                json.put("actual", actualDistance);
            } catch (JSONException e) {
                Log.e("RssiRequest", "JSON Exception", e);
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