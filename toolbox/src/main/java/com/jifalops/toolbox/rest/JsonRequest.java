//package com.jifalops.toolbox.rest;
//
//import com.android.volley.DefaultRetryPolicy;
//import com.android.volley.NetworkResponse;
//import com.android.volley.ParseError;
//import com.android.volley.Request;
//import com.android.volley.Response;
//import com.android.volley.Response.ErrorListener;
//import com.android.volley.Response.Listener;
//import com.android.volley.toolbox.HttpHeaderParser;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.UnsupportedEncodingException;
//
///**
// * Based off of CustomRequest from http://stackoverflow.com/questions/19837820/volley-jsonobjectrequest-post-request-not-working
// * Child classes should override {@link #getParams()} to pass data to the server.
// */
//public abstract class JsonRequest extends Request<JSONObject> {
//    private final Listener<JSONObject> responseListener;
//    public JsonRequest(int method, String url, Listener<JSONObject> responseListener, ErrorListener errorListener) {
//        super(Method.POST, url, errorListener);
//        this.responseListener = responseListener;
//    }
//
//    public void setMaxRetries(int retries) {
//        setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
//                retries,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//    }
//
//    @Override
//    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//        try {
//            String jsonString = new String(response.data,
//                    HttpHeaderParser.parseCharset(response.headers));
//            return Response.success(new JSONObject(jsonString),
//                    HttpHeaderParser.parseCacheHeaders(response));
//        } catch (UnsupportedEncodingException e) {
//            return Response.error(new ParseError(e));
//        } catch (JSONException je) {
//            return Response.error(new ParseError(je));
//        }
//    }
//
//    @Override
//    protected void deliverResponse(JSONObject response) {
//        responseListener.onResponse(response);
//    }
//}