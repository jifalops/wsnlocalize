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
public class DeviceRequest extends AbsRequest {
    public static class Device {
        public final String name, desc;
        public Device(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
        public Device(String jsonObject) throws JSONException {
            JSONObject json = new JSONObject(jsonObject);
            name = json.getString("name");
            desc = json.getString("desc");
        }
        @Override
        public String toString() {
            JSONObject json = new JSONObject();
            try {
                json.put("name", name);
                json.put("desc", desc);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }
    }

    private final List<Device> devices;

    public DeviceRequest(List<Device> devices,
                         Response.Listener<MyResponse> listener, Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.devices = devices;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put(AbsRequest.REQUEST_TYPE, "device");
        for (int i = 0, size = devices.size(); i < size; i++) {
            params.put(i+"", devices.get(i).toString());
        }
        return params;
    }
}
