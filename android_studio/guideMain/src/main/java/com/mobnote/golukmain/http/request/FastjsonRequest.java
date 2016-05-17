package com.mobnote.golukmain.http.request;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import cn.com.tiros.debug.GolukDebugUtils;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.AuthFailureError;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.alibaba.fastjson.JSON;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.util.GolukUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;
 
/**
 * Volley adapter for JSON requests that will be parsed into Java objects by Fastjason.
 */
public class FastjsonRequest<T> extends Request<T> {
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Map<String, String> params;
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
        super(type, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.params = params;
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
    	if(null != listener){
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
            JSONObject result = JSON.parseObject(json);
            int code = result.getIntValue("code");
            if(code == 10001 || code == 10002){//token过期
//                GolukUtils.showToast(GolukApplication.getInstance().getContext(),GolukApplication.getInstance().getContext().getResources().getString(R.string.str_token_timeout_volley));
//              Intent intent = new Intent(GolukApplication.getInstance().getContext(), UserLoginActivity.class);
//              GolukApplication.getInstance().getContext().startActivity(intent);
            }
			return Response.success(JSON.parseObject(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (Exception e) {
			return Response.error(new ParseError(e));
		}
	}
}