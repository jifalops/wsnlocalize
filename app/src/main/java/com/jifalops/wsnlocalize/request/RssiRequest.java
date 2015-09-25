package com.jifalops.wsnlocalize.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.jifalops.wsnlocalize.data.Rssi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RssiRequest extends AbsRequest {

    private final List<Rssi> records;
    private final String signalType;
    public RssiRequest(String signalType, List<Rssi> records, Response.Listener<MyResponse>listener,
                       Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.signalType = signalType;
        this.records = records;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put(AbsRequest.REQUEST_TYPE, "rssi");
        params.put(AbsRequest.REQUEST_SIGNAL, signalType);
        for (int i = 0, size = records.size(); i < size; i++) {
            params.put(i+"", records.get(i).toString());
        }
        return params;
    }
}
