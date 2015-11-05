package cn.com.mobnote.golukmobile.http.request;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.AuthFailureError;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;

public class OctetStreamUploadRequest<T> extends Request<T> {

    private final Map<String, String> mParams;
	private Listener<T> mListener;
    private final Class<T> mClazz;
    private final Map<String, String> mHeaders;
    /**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param clazz Relevant class object, for Fastjason's reflection
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public OctetStreamUploadRequest(String url, Class<T> clazz, Map<String, String> headers, Listener<T> listener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mClazz = clazz;
        mHeaders = headers;
        mParams = null;
    }

    public OctetStreamUploadRequest(String url, Class<T> clazz, Map<String, String> headers, Map<String, String> params, Listener<T> listener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mClazz = clazz;
        mHeaders = headers;
        mParams = params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders != null ? mHeaders : super.getHeaders();
    }
  
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
    	return mParams != null ? mParams : super.getParams();
    }

    @Override
    public String getBodyContentType() {
        return "application/octet-stream";
    }

	@Override
	protected void deliverResponse(T response) {
		// TODO Auto-generated method stub
    	if(null != mListener){
    		mListener.onResponse(response);
    	}
	}

	
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
    	try {
            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
            		JSON.parseObject(json, mClazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}
