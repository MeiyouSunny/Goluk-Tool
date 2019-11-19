package com.mobnote.golukmain.http.request;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cn.com.tiros.debug.GolukDebugUtils;


import cn.com.tiros.debug.GolukDebugUtils;

/**
 * Volley adapter for JSON requests that will be parsed into Java objects by Fastjason.
 */
public class FastjsonRequest<T> extends Request<T> {
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Map<String, String> params;
    private String mRequestBody;
    private final Listener<T> listener;
 
    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     * @param headers Map of request headers
     */
    public FastjsonRequest(String url, Class<T> clazz, Map<String, String> headers,
                           Listener<T> listener, ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.params = null;
        this.listener = listener;
    }
    
    /**
     * Make a request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     * @param headers Map of request headers
     */
    public FastjsonRequest(int type, String url, Class<T> clazz, Map<String, String> headers,
                           Map<String, String> params,
                           Listener<T> listener, ErrorListener errorListener) {
        this(type, url, clazz, headers, params, "", listener, errorListener);
//        super(type, url, errorListener);
//        this.clazz = clazz;
//        this.headers = headers;
//        this.params = params;
//        this.listener = listener;
    }

    public FastjsonRequest(int type, String url, Class<T> clazz, Map<String, String> headers,
                           Map<String, String> params, String requestBody,
                           Listener<T> listener, ErrorListener errorListener) {
        super(type, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.params = params;
        this.mRequestBody = requestBody;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params != null ? params : super.getParams();
    }

    @Override
    protected void deliverResponse(T response) {
        if (null != listener) {
            listener.onResponse(response);
        }
    }

    public final Class<T> getClazz() {
        return clazz;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            GolukDebugUtils.e("", "-------------parseNetworkResponse--------json: " + json);
            return Response.success(JSON.parseObject(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    /** @deprecated */
    public byte[] getPostBody() throws AuthFailureError {
        return this.getBody();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (!TextUtils.isEmpty(mRequestBody)) {
            try {
                return mRequestBody.getBytes("utf-8");
            } catch (UnsupportedEncodingException var2) {
                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", new Object[]{this.mRequestBody, "utf-8"});
                return null;
            }
        }

        return super.getBody();
    }

}