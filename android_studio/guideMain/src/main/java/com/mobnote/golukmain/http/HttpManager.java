package com.mobnote.golukmain.http;

import java.io.File;
import java.util.Map;

import org.apache.http.client.HttpClient;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;


import com.mobnote.application.GolukApplication;

import android.content.Context;
import android.text.TextUtils;

public class HttpManager {
	  /** Default on-disk cache directory. */
    private static final String DEFAULT_CACHE_DIR = "volley";

    private Context mContext;


    private GolukRequestQueue mRequestQueue;
    private UrlHostManager mUrlHostManager;
    private static HttpManager mInstance;

    private HttpManager() {
		mContext = GolukApplication.getInstance();
        mRequestQueue = newRequestQueue();
        mUrlHostManager = new UrlHostManager();
    }

    public String getWebDirectHost() {
    	return mUrlHostManager.getHost();
    }
    
    public String getWebH5Host(){
    	return mUrlHostManager.getWebPageHost();
    }

	public static synchronized HttpManager getInstance() {
		if (mInstance == null) {
			mInstance = new HttpManager();
		}
		return mInstance;
	}

	/**
	 * Adds the specified request to the global queue .
	 * 
	 * @param req
	 */
	public <T> void add(Request<T> req) {

		mRequestQueue.add(req);
	}

	/**
	 * cancel the specified requests using tag .
	 * 
	 * @param tag
	 */
    public void cancelAll(final Object tag) {
        mRequestQueue.cancelAll(tag);
    }

	/**
	 * cancel all request in the RequestQueue.
	 * 
	 * @param tag
	 */
    public void cancelAll() {
        mRequestQueue.cancelAll(new RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
  
    /**
     * Creates a default instance of the worker pool and calls {@link GolukRequestQueue#start()} on it.
     *
     * @return A started {@link GolukRequestQueue} instance.
     */
    private GolukRequestQueue newRequestQueue() {
        File cacheDir = new File(mContext.getCacheDir(), DEFAULT_CACHE_DIR);

        Cache cache = new DiskBasedCache(cacheDir);

        HurlStack stack = new HurlStack();
        Network network = new BasicNetwork(stack);

        GolukRequestQueue queue = new GolukRequestQueue(cache, network);
        queue.start();

        return queue;
    }
    
    public String getUrl(int type, String path, Map<String, String> param) {
    	String url = mUrlHostManager.getHost();
    	if (!TextUtils.isEmpty(path)) {
    		url += path;
    	}

    	if(type == Method.GET && param != null && (param.size() != 0)){
            String encodedParams = null;
			try {
				encodedParams = UrlHostManager.getEncodedUrlParams(param);
			} catch (AuthFailureError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String extra = "";
            if (encodedParams != null && encodedParams.length() > 0) {
                if (!url.endsWith("?")) {
                	extra += "?";
                }
                extra += encodedParams;
            }
            url = url + extra;
		}

    	return url;
    }
}
