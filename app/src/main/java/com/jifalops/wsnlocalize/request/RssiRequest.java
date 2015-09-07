package com.jifalops.wsnlocalize.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.jifalops.wsnlocalize.data.RssiRecordOld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RssiRequest extends AbsRequest {

    private final List<RssiRecordOld> records;

    public RssiRequest(List<RssiRecordOld> records, Response.Listener<MyResponse>listener,
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
