package cn.com.tiros.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.os.AsyncTask;
import android.provider.Settings;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import cn.com.tiros.debug.GolukDebugUtils;

public class Http implements IHttpFn {

	private int mHttpHandler; // http c端句柄
	private HttpGet mHttpGet;
	private HttpPost mHttpPost;
	private HttpPut mHttpPut = null;
	private HttpDelete mHttpDelete = null;
	public HttpClient mHttpClient;
	public HttpAsyncTask httpasync;
	private HttpParams mParams;
	private Map<String, String> mHeaders;
	private int httpConnectTimeOutTime = 30 * 1000;// 网络链接默认超时时间
	private int httpSoTimeOutTime = 20 * 1000;// 默认链接超时时间

	public int netstate = 1;// 当前网络状态，默认为等待状态
	// 暂定3G网络上传的速率为10/s
	private final int mSpeed = 10;

	public void sys_httpcreate(int http) {
		mHttpHandler = http;
		mHeaders = new HashMap<String, String>();
		mParams = new BasicHttpParams();
		mHttpClient = new DefaultHttpClient(mParams);
		HttpClientParams.setRedirecting(mParams, false);
	}

	public int getConnTimeOutTime() {
		return httpConnectTimeOutTime;
	}

	public int getSoTimeOutTime() {
		return httpSoTimeOutTime;
	}

	public void sys_httpdestroy() {
		sys_httpcancel();
		mHeaders.clear();
		mHeaders = null;
		if (mHttpClient != null) {
			mHttpClient.getConnectionManager().shutdown();
			mHttpClient = null;
		}
		mHttpHandler = 0;
		mParams = null;
		httpasync = null;
		mHttpPost = null;
		mHttpGet = null;
	}

	/**
	 * 
	 * @param[in] dwMSecs1 - http发送请求到收到服务器响应的等待连接超时时间
	 * @param[in] dwMSecs2 - http接收数据片段间隔超时时间
	 */
	public void sys_httpsettimeout(int dwMSecs1, int dwMSecs2) {
		httpConnectTimeOutTime = dwMSecs1;
		httpSoTimeOutTime = dwMSecs2;
	}

	public boolean sys_httpaddheader(String name, String value) {
		mHeaders.put(name, value);
		return true;
	}

	public void sys_httpremoveheader(String name) {
		mHeaders.remove(name);
	}

	// shizy 20120409 修改http，每次请求完成，Error，Cancel都要清空head信息。
	public void clearHeaders() {
		if (mHeaders != null) {
			mHeaders.clear();
		}
	}

	public int getTrasferTime(long datalength) {

		int time = ((int) (datalength / 1024 / mSpeed)) * 1000;
		return time;
	}

	public boolean sys_httppost(String contentType, String url, byte[] data) {
		isNeedWapConnect();

		// post数据量比较大，需要根据数据大小设定超时时间
		// 暂定3G网络上传的速率为10/s
		int speed = 10;
		int time = (data.length / 1024 / speed) * 1000;
		// 如果计算的时间大于上层设置时长，那么修改超时时间
		if (time > httpConnectTimeOutTime) {
			HttpConnectionParams.setConnectionTimeout(mParams, time);
			HttpConnectionParams.setSoTimeout(mParams, time + httpSoTimeOutTime);
		} else {
			HttpConnectionParams.setConnectionTimeout(mParams, httpConnectTimeOutTime);
			HttpConnectionParams.setSoTimeout(mParams, httpSoTimeOutTime);
		}

		URI uri = URI.create(url);

		if (mHttpClient == null) {
			mHttpClient = new DefaultHttpClient(mParams);
		}

		if (mHttpPost == null) {
			mHttpPost = new HttpPost(uri);
		} else {
			mHttpPost.setURI(uri);
			Header[] headers = mHttpPost.getAllHeaders();
			if (headers != null) {
				for (int i = 0; i < headers.length; i++) {
					mHttpPost.removeHeader(headers[i]);
				}
			}
		}
		addPostData(mHttpPost, contentType, uri, data);

		boolean bContainsHead = isContainHead(uri.getHost());

		try {
			httpasync = new HttpAsyncTask(this, mHttpHandler, mHttpClient, mHttpPost, bContainsHead);
//			httpasync.execute();
			httpasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean sys_httpput(String url, byte[] data) {
		HttpConnectionParams.setConnectionTimeout(mParams, httpConnectTimeOutTime);
		HttpConnectionParams.setSoTimeout(mParams, httpSoTimeOutTime);
		URI uri = URI.create(url);
		if (null == mHttpClient) {
			mHttpClient = new DefaultHttpClient(mParams);
		}
		if (null == mHttpPut) {
			mHttpPut = new HttpPut(uri);
		} else {
			mHttpPut.setURI(uri);
			removeAllHeader(mHttpPut);
		}
		addHeader(mHttpPut, null);
		boolean bContainsHead = isContainHead(uri.getHost());
		try {
			httpasync = new HttpAsyncTask(this, mHttpHandler, mHttpClient, mHttpPut, bContainsHead);
//			httpasync.execute();
			httpasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void removeAllHeader(HttpRequestBase requestBase) {
		Header[] headers = requestBase.getAllHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.length; i++) {
				requestBase.removeHeader(headers[i]);
			}
		}
	}

	private void addHeader(HttpRequestBase requestBase, String contentType) {
		if (null != contentType && !"".equals(contentType)) {
			requestBase.setHeader("Content-Type", contentType);
		}

		if (null == mHeaders) {
			return;
		}
		Set<Entry<String, String>> set = mHeaders.entrySet();
		Iterator<Entry<String, String>> iterator = set.iterator();
		while (iterator.hasNext()) {
			@SuppressWarnings("rawtypes")
			Entry entry = (Entry) iterator.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			requestBase.setHeader(key, value);
			entry = null;
		}
		set = null;
		iterator = null;
		clearHeaders();
	}

	private void setTimeOut(int time) {
		// 如果计算的时间大于上层设置时长，那么修改超时时间
		if (time > httpConnectTimeOutTime) {
			HttpConnectionParams.setConnectionTimeout(mParams, time);
			HttpConnectionParams.setSoTimeout(mParams, time + httpSoTimeOutTime);
		} else {
			HttpConnectionParams.setConnectionTimeout(mParams, httpConnectTimeOutTime);
			HttpConnectionParams.setSoTimeout(mParams, httpSoTimeOutTime);
		}
	}

	private boolean isContainHead(String host) {
		if (host.contains(PRIVATE_URL_SIGN1) || host.contains(PRIVATE_URL_SIGN2) || host.contains(PRIVATE_URL_SIGN3)) {
			return true;
		}

		return false;
	}

	public boolean sys_httpget(String url) {
		isNeedWapConnect();
		HttpConnectionParams.setConnectionTimeout(mParams, httpConnectTimeOutTime);
		HttpConnectionParams.setSoTimeout(mParams, httpSoTimeOutTime);
		URI uri = URI.create(url);

		if (mHttpClient == null) {
			mHttpClient = new DefaultHttpClient(mParams);
		}

		if (mHttpGet == null) {
			mHttpGet = new HttpGet(createGetUri(uri));
		} else {
			mHttpGet.setURI(createGetUri(uri));
		}

		if (mHeaders != null) {

			Set<Entry<String, String>> set = mHeaders.entrySet();

			Iterator<Entry<String, String>> iterator = set.iterator();

			while (iterator.hasNext()) {
				@SuppressWarnings("rawtypes")
				Entry entry = (Entry) iterator.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				mHttpGet.setHeader(key, value);
				entry = null;
			}
			set = null;
			iterator = null;
			clearHeaders();
		}

		boolean bContainsHead = isContainHead(uri.getHost());

		try {
			httpasync = new HttpAsyncTask(this, mHttpHandler, mHttpClient, mHttpGet, bContainsHead);
//			httpasync.execute();
			httpasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean sys_httpdelete(String url, byte[] data) {
		isNeedWapConnect();
		HttpConnectionParams.setConnectionTimeout(mParams, httpConnectTimeOutTime);
		HttpConnectionParams.setSoTimeout(mParams, httpSoTimeOutTime);
		URI uri = URI.create(url);
		if (mHttpClient == null) {
			mHttpClient = new DefaultHttpClient(mParams);
		}
		if (mHttpDelete == null) {
			mHttpDelete = new HttpDelete(createGetUri(uri));
		} else {
			mHttpDelete.setURI(createGetUri(uri));
		}
		addHeader(mHttpDelete, null);
		boolean bContainsHead = isContainHead(uri.getHost());
		try {
			httpasync = new HttpAsyncTask(this, mHttpHandler, mHttpClient, mHttpDelete, bContainsHead);
//			httpasync.execute();
			httpasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	HttpAsyncTaskPostFile mPostFileAsyncTask = null;

	/**
	 * 传输文件
	 *
	 * @param uploadurl
	 *            上传文件的服务器地址
	 * @param filePath
	 *            上传的文件路径
	 * @return
	 * @author jiayf
	 * @date Feb 9, 2015
	 */
	public boolean sys_httppostfile(String uploadurl, String filePath) {
		GolukDebugUtils.e("", "SystemApi-----java-----Http-----sys_httppostfile:  url: " + uploadurl + "   filePath:"
				+ filePath);
		isNeedWapConnect();
		URI uri = URI.create(uploadurl);
		boolean bContainsHead = isContainHead(uri.getHost());
		// 转换真实的文件路径
		filePath = FileUtils.libToJavaPath(filePath);

		try {
			mPostFileAsyncTask = new HttpAsyncTaskPostFile(this, mHttpHandler, uploadurl, filePath, bContainsHead,
					mHeaders);
//			mPostFileAsyncTask.execute();
			mPostFileAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void sys_httpcancel() {
		httpConnectTimeOutTime = 30 * 1000;
		httpSoTimeOutTime = 20 * 1000;

		if (httpasync != null) {
			httpasync.cancel(true);
			httpasync.mIsCancel = true;
			httpasync = null;
		}

		if (null != mPostFileAsyncTask) {
			mPostFileAsyncTask.cancel(true);
			mPostFileAsyncTask.mIsCancel = true;
			mPostFileAsyncTask = null;
		}

		if (mHttpPost != null) {
			if (!mHttpPost.isAborted()) {
				mHttpPost.abort();
			}
			mHttpPost = null;
		}
		if (mHttpGet != null) {
			if (!mHttpGet.isAborted()) {
				mHttpGet.abort();
			}
			mHttpGet = null;
		}
		if (mHttpPut != null) {
            if (!mHttpPut.isAborted()) {
                mHttpPut.abort();
            }
            mHttpPut = null;
        }
		if (mHttpDelete != null) {
            if (!mHttpDelete.isAborted()) {
                mHttpDelete.abort();
            }
            mHttpDelete = null;
        }
		if (netstate == NETSTATE_RUNNING) {
			if (mHttpClient != null) {
				mHttpClient.getConnectionManager().shutdown();
				mHttpClient = null;
			}
		}
		clearHeaders();
		netstate = NETSTATE_WAIT;
	}

	public void sys_httpgetuserdata() {

	}

	public static String[][] formetString(String query) {
		Vector<String> name = new Vector<String>();
		Vector<String> value = new Vector<String>();
		Vector<String> pairs = new Vector<String>();
		query = "&" + query + "&";
		int size = 0;
		for (int i = 0; i < query.length(); i++) {
			if (query.charAt(i) == '&') {
				pairs.addElement(query.substring(size, i));
				size = i;
			}
		}
		for (int i = 1; i < pairs.size(); i++) {
			String str = (String) pairs.elementAt(i);
			int index = str.indexOf("=");
			name.addElement(str.substring(1, index));
			value.addElement(str.substring(index + 1, str.length()));
		}
		String[][] result = new String[pairs.size() - 1][2];
		for (int i = 0; i < result.length; i++) {
			result[i][0] = (String) name.elementAt(i);
			result[i][1] = (String) (value.elementAt(i)).toString();
		}
		name.clear();
		value.clear();
		pairs.clear();
		name = null;
		value = null;
		pairs = null;
		return result;
	}

	public List<NameValuePair> getPostParams(URI uri) {
		List<NameValuePair> params = null;
		String query = null;
		try {
			query = uri.getQuery();
			if (query != null) {
				String[][] changeparam = formetString(query);
				params = new ArrayList<NameValuePair>(changeparam.length);
				for (int i = 0; i < changeparam.length; i++) {
					params.add(new BasicNameValuePair(changeparam[i][0], changeparam[i][1]));
				}
			}
		} catch (Exception e) {
		}
		return params;
	}

	public void addPostData(HttpPost post, String contentType, URI uri, byte[] data) {
		if (contentType != null) {
			post.setHeader("Content-Type", contentType);
		}
		if (mHeaders != null) {
			Set<Entry<String, String>> set = mHeaders.entrySet();
			Iterator<Entry<String, String>> iterator = set.iterator();
			while (iterator.hasNext()) {
				@SuppressWarnings("rawtypes")
				Entry entry = (Entry) iterator.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				post.setHeader(key, value);
				entry = null;
			}
			set = null;
			iterator = null;
			clearHeaders();
		}

		if (getPostParams(uri) != null) {
			try {
				post.setEntity(new UrlEncodedFormEntity(getPostParams(uri)));
			} catch (UnsupportedEncodingException e1) {
			}
		}
		post.setEntity(new ByteArrayEntity(data));

		ByteArrayEntity bae = new ByteArrayEntity(data);
	}

	public URI createGetUri(URI uri) {
		String scheme = null, host = null, path = null, query = null, fragment = null;
		int port = -1;
		scheme = uri.getScheme();
		host = uri.getHost();
		port = uri.getPort();
		path = uri.getPath();
		try {
			query = uri.getQuery();
		} catch (Exception e) {
			query = null;
		}
		try {
			fragment = uri.getFragment();
		} catch (Exception e) {
			fragment = null;
		}
		try {
			uri = URIUtils.createURI(scheme, host, port, path, query, fragment);
		} catch (URISyntaxException e1) {
		}
		return uri;
	}

	public void isNeedWapConnect() {
		String defaultHost = Proxy.getDefaultHost();
		ConnectivityManager cm = (ConnectivityManager) Const.getAppContext().getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (defaultHost != null && netinfo != null && netinfo.getType() != ConnectivityManager.TYPE_WIFI) {
			HttpHost proxy = new HttpHost(defaultHost, Proxy.getDefaultPort());
			mParams.setParameter(AllClientPNames.DEFAULT_PROXY, proxy);
		} else {
			mParams.removeParameter(AllClientPNames.DEFAULT_PROXY);
		}
	}

	public boolean isNetWorkEnable() {
		ConnectivityManager cm = (ConnectivityManager) Const.getAppContext().getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo != null) {
			if (netinfo.isConnected()) {
				return true;
			}
			return false;
		}
		return false;
	}

	public boolean isAirPlaneMode() {
		return (Settings.System.getInt(Const.getAppContext().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0) ? false
				: true;
	}

	public static native void sys_httpEvent(int http, int statecode, int param1, int param2);

	public static native void sys_httpEvent(int http, int statecode, int param1, byte[] param2);

	public static native void sys_httpEvent(int http, int statecode, int param1, String param2);
}
