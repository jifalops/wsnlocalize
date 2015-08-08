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
    private final String localWifiMac, localBtMac, remoteWifiMac, remoteBtMac, remoteDesc, method;
    private final double rssi, actualDistance;

    public RssiRequest(String localWifiMac, String localBtMac, String remoteWifiMac, String remoteBtMac,
                       String remoteDesc, String method, double rssi, double actualDistance,
                       Response.Listener<MyResponse> listener, Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.localWifiMac = localWifiMac;
        this.localBtMac = localBtMac;
        this.remoteWifiMac = remoteWifiMac;
        this.remoteBtMac = remoteBtMac;
        this.remoteDesc = remoteDesc;
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
        params.put("remoteDesc", remoteDesc);
        params.put("method", method);
        params.put("rssi", rssi +"");
        params.put("actual", actualDistance +"");
        params.put("api", Build.VERSION.SDK_INT +"");
        return params;
    }
}
