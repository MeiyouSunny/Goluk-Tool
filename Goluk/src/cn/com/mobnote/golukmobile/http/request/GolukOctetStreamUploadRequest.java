package cn.com.mobnote.golukmobile.http.request;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cn.com.mobnote.golukmobile.http.HttpManager;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public abstract class GolukOctetStreamUploadRequest<T> {
    private Class<T> mClazz;
    private Map<String, String> mHeaders = new HashMap<String, String>();
    private IRequestResultListener mListener;
    private Object mTag;
    private byte[] mBody;
    private int mRequestType;// requestType for call back
	public GolukOctetStreamUploadRequest(int requestType, Class<T> clazz, IRequestResultListener listener) {
		mClazz = clazz;
		mListener = listener;
		mRequestType = requestType;
		addDefaultHeader();
	}

	protected abstract String getPath();
	
	/**
	 * 继承实现，添加不变的Header,Header中要变化的参数在addHeader中添加
	 */
	protected void addDefaultHeader() {
//		String verName = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
//				IPageNotifyFn.PageType_GetVersion, "fs6:/version");
//		mHeaders.put("commappversion", verName);
//		mHeaders.put("commdevmodel", android.os.Build.MODEL);
//		mHeaders.put("commlat", "" + LngLat.lat);
//		mHeaders.put("commlon", "" + LngLat.lng);
//		mHeaders.put("commmid", "" + Tapi.getMobileId());
//		mHeaders.put("commostag", "Android");
//		mHeaders.put("commsysversion", android.os.Build.VERSION.RELEASE);
//		String uid = GolukApplication.getInstance().mCurrentUId;
//		if (TextUtils.isEmpty(uid)) {
//			mHeaders.put("commuid", "");
//		} else {
//			mHeaders.put("commuid", uid);
//		}
	}
	
	public void setTag(Object tag) {
		mTag = tag;
	}
	/**
	 * add variable header
	 * 
	 * @param optionParam
	 */
	public void addHeader(HashMap header) {
		if (header != null) {
			mHeaders.putAll(header);
		}
	}
	
	public void setBody(byte[] body) {
		mBody = body;
	}
	
	public void post() {
		String url = HttpManager.getInstance().getUrl(Method.POST, getPath(), null);
		OctetStreamUploadRequest<T> request = new OctetStreamUploadRequest<T>(url, mClazz, mHeaders, new Response.Listener<T>(){
			
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
			
		}) {
			@Override
			public byte[] getBody() throws AuthFailureError {
				// TODO Auto-generated method stub
				return mBody != null ? mBody:super.getBody();
			}
			@Override
			public String getBodyContentType() {
				// TODO Auto-generated method stub
				return "application/octet-stream";
			}
		};
		if (mTag == null) {
			mTag = mListener;
		}
		request.setTag(mTag);
		request.setShouldCache(false);
		HttpManager.getInstance().add(request);
	}
}
