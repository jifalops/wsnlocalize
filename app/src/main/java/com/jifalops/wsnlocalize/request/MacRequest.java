package com.jifalops.wsnlocalize.request;

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
public class MacRequest extends AbsRequest {
    public static class Mac {
        public final String mac, desc;
        public Mac(String mac, String desc) {
            this.mac = mac;
            this.desc = desc;
        }
        public Mac(String jsonObject) throws JSONException {
            JSONObject json = new JSONObject(jsonObject);
            mac = json.getString("mac");
            desc = json.getString("desc");
        }
        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            try {
                json.put("mac", mac);
                json.put("desc", desc);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
    }

    private final List<Mac> macs;

    public MacRequest(List<Mac> macs,
                      Response.Listener<MyResponse> listener, Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.macs = macs;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put(AbsRequest.REQUEST_TYPE, "mac");
        for (int i = 0, size = macs.size(); i < size; i++) {
            params.put(i+"", macs.get(i).toString());
        }
        return params;
    }
}
