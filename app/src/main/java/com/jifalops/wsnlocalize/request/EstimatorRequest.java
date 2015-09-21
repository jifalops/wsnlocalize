package com.jifalops.wsnlocalize.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.jifalops.wsnlocalize.data.DistanceEstimator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @deprecated
 */
public class EstimatorRequest extends AbsRequest {
    private final List<DistanceEstimator> estimators;
    private final String signalType;
    public EstimatorRequest(String signalType, List<DistanceEstimator> estimators,
                            Response.Listener<MyResponse> listener,
                            Response.ErrorListener errorListener) {
        super(listener, errorListener);
        this.signalType = signalType;
        this.estimators = estimators;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put(AbsRequest.REQUEST_TYPE, "estimator");
        params.put(AbsRequest.REQUEST_SIGNAL, signalType);
        for (int i = 0, size = estimators.size(); i < size; i++) {
            params.put(i+"", estimators.get(i).toString());
        }
        return params;
    }
}
