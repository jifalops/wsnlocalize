package com.jifalops.wsnlocalize.request;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * Based off of CustomRequest from http://stackoverflow.com/questions/19837820/volley-jsonobjectrequest-post-request-not-working
 * Child classes should override {@link #getParams()} to pass data to the server.
 */
public abstract class AbsRequest extends Request<JSONObject> {
    protected static final String REQUEST_TYPE = "requestType";
    protected static final String REQUEST_SIGNAL = "signalType";
    public static final String URL = "http://localization.jifalops.com/logging_service.php";

    public static class MyResponse {
        public final String responseMessage, queryResult;
        public final int responseCode;
        private MyResponse(JSONObject response) {
            int code = 0;
            String msg = "", result = "";
            try {
                code = response.getInt("responseCode");
                msg = response.getString("responseMessage");
                result = response.getString("queryResult");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            responseCode = code;
            responseMessage = msg;
            queryResult = result;
        }
    }

    private Listener<MyResponse> listener;

    public AbsRequest(Listener<MyResponse> responseListener, ErrorListener errorListener) {
        super(Method.POST, URL, errorListener);
        this.listener = responseListener;
        // Do not retry. If a request fails, they instead can be manually retried.
        // (Data was being submitted multiple times.)
        setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        listener.onResponse(new MyResponse(response));
    }
}