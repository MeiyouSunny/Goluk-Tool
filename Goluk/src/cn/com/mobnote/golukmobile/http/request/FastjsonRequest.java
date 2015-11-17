package cn.com.mobnote.golukmobile.http.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.AuthFailureError;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.alibaba.fastjson.JSON;

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
            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));
            System.out.println("ddjson : " + json);
            return Response.success(
            		JSON.parseObject(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}