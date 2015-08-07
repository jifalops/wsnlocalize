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
public class DeviceRequest extends AbsRequest {
    private String wifiMac, btMac;

    public DeviceRequest(String wifiMac, String btMac,
             Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.wifiMac = wifiMac;
        this.btMac = btMac;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put(AbsRequest.REQUEST_TYPE, "device");
        params.put("wifiMac", wifiMac);
        params.put("btMac", btMac);
        params.put("model", Build.MODEL);
        return params;
    }
}
