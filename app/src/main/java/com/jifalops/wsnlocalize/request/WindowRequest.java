package com.jifalops.wsnlocalize.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.jifalops.wsnlocalize.data.RssiRecord;
import com.jifalops.wsnlocalize.data.WindowRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class WindowRequest extends AbsRequest {

    private final List<WindowRecord> records;
    private final double[] estimates;
    private final String signalType;
    public WindowRequest(String signalType, List<WindowRecord> records, double[] estimates,
                         Response.Listener<MyResponse> listener,
                         Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.signalType = signalType;
        this.records = records;
        this.estimates = estimates;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put(AbsRequest.REQUEST_TYPE, "window");
        params.put(AbsRequest.REQUEST_SIGNAL, signalType);
        for (int i = 0, size = records.size(); i < size; i++) {
            params.put(i+"", records.get(i).toString() +","+ estimates[i]);
        }
        return params;
    }
}
