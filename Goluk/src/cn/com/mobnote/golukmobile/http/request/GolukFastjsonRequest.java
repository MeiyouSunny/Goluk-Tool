package cn.com.mobnote.golukmobile.http.request;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.http.HttpManager;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.LngLat;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.Tapi;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;


public abstract class GolukFastjsonRequest<T> {
    private  Class<T> mClazz;
    private  Map<String, String> mHeaders = new HashMap<String, String>();
    private  Map<String, String> mParams = new HashMap<String, String>();
    private  IRequestResultListener mListener;
    private Object mTag;
    private boolean bCache = false;
    private int mRequestType;// requestType for call back
    private GolukRetryPolicy mDefaultRetryPolicy;
	public GolukFastjsonRequest(int requestType, Class<T> clazz, IRequestResultListener listener) {
		mClazz = clazz;
		mListener = listener;
		mRequestType = requestType;
		mDefaultRetryPolicy = new GolukRetryPolicy();
		addDefaultHeader();
		addDefaultParam();
	}
	
	protected abstract String getPath();
	protected abstract String getMethod();
	/**
	 * 继承实现，添加不变的参数，可变的参数在addParam中添加
	 */
	protected void addDefaultParam() {
		String Method = getMethod();
		if (!TextUtils.isEmpty(Method))
		mParams.put("method", getMethod());
	}
	
	/**
	 * 继承实现，添加不变的Header,Header中要变化的参数在addHeader中添加
	 */
	protected void addDefaultHeader() {
//		String verName = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
//				IPageNotifyFn.PageType_GetVersion, "fs6:/version");
//		mHeaders.put("commappversion", verName);
		mHeaders.put("commdevmodel", android.os.Build.MODEL);
		mHeaders.put("commlat", "" + LngLat.lat);
		mHeaders.put("commlon", "" + LngLat.lng);
		mHeaders.put("commmid", "" + Tapi.getMobileId());
		mHeaders.put("commostag", "android");
		mHeaders.put("commsysversion", android.os.Build.VERSION.RELEASE);
		String uid = GolukApplication.getInstance().mCurrentUId;
		if (TextUtils.isEmpty(uid)) {
			mHeaders.put("commuid", "");
		} else {
			mHeaders.put("commuid", uid);
		}
	}

	protected Map<String, String> getHeader() {
		return mHeaders;
	}

	protected Map<String, String> getParam() {
		return mParams;
	}

	public void setTag(Object tag) {
		mTag = tag;
	}

	public void setCache(boolean b) {
		bCache = b;
	}

    protected void setCurrentTimeout(int timeout) {
    	mDefaultRetryPolicy.setCurrentTimeout(timeout);
    }
    
    protected void setCurrentRetryCount(int retryCount) {
    	mDefaultRetryPolicy.setCurrentRetryCount(retryCount);
    }

    protected void get() {
		addRequest(Method.GET);
	}
	
    protected void post() {
		addRequest(Method.POST);
	}
	
	private void addRequest(int type) {
		String url = HttpManager.getInstance().getUrl(type, getPath(), mParams);
		if(type == Method.GET){
            mParams = null;
		}

		FastjsonRequest<T> request = new FastjsonRequest<T>(type, url, mClazz, mHeaders, mParams, new Response.Listener<T>(){

			@Override
			public void onResponse(T response) {
				// TODO Auto-generated method stub
				if (mListener != null) {
					mListener.onLoadComplete(mRequestType, response);
				}
			}
			
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				if (mListener != null) {
					mListener.onLoadComplete(mRequestType, null);
				}
			}
			
		});
		if (mTag == null) {
			mTag = mListener;
		}
		request.setTag(mTag);
		request.setShouldCache(bCache);
		request.setRetryPolicy(mDefaultRetryPolicy);
		HttpManager.getInstance().add(request);
	}
}
