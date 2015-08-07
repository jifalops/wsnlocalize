package com.jifalops.wsnlocalize.request;

import android.os.Build;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class RssiRequest extends AbsRequest {
    private String localWifiMac, localBtMac, remoteWifiMac, remoteBtMac, method;
    private double rssi, actualDistance;

    public RssiRequest(String localWifiMac, String localBtMac, String remoteWifiMac, String remoteBtMac,
                       String method, double rssi, double actualDistance,
                       Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.localWifiMac = localWifiMac;
        this.localBtMac = localBtMac;
        this.remoteWifiMac = remoteWifiMac;
        this.remoteBtMac = remoteBtMac;
        this.method = method;
        this.rssi = rssi;
        this.actualDistance = actualDistance;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put(AbsRequest.REQUEST_TYPE, "rssi");
        params.put("localWifiMac", localWifiMac);
        params.put("localBtMac", localBtMac);
        params.put("remoteWifiMac", remoteWifiMac);
        params.put("remoteBtMac", remoteBtMac);
        params.put("method", method);
        params.put("rssi", rssi +"");
        params.put("actualDistance", actualDistance +"");
        params.put("api", Build.VERSION.SDK_INT +"");
        return params;
    }
}
