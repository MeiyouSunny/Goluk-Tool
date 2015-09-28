/**
 * 
 */
package cn.com.mobnote.golukmobile.helper;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.loopj.android.http.*;

/**
 * @描述 异步HttpClient辅助类
 * @作者 卜长清，buchangqing@goluk.com
 * @日期 2015-09-11
 * @版本 1.0
 */
public class AsyncHttpClientHelper {
	private static AsyncHttpClient httpClient = null;
	private String protocol = "http";																// 使用的协议，默认HTTP
	private String server = "svr.goluk.cn";														// 服务器
	private String uri = "cdcComment/comment.htm";									// 服务

	public AsyncHttpClientHelper() {
		initHttpClient();
	}
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getUrl() {
		return String.format("%s://%s/%s", protocol, server, uri);
	}

	
	public static synchronized AsyncHttpClient getHttpClient() {
		 if(httpClient == null) { 
			 httpClient = new AsyncHttpClient(); 
		 }
		 return httpClient;
	 }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// RESTful API
	/**
	 * HTTP/GET 请求
	 * @param params 请求参数
	 * @param responseHandler 
	 */
	public void get(RequestParams params, AsyncHttpResponseHandler responseHandler) {
		 String url = getUrl();
		httpClient.get(url, params, responseHandler);
	}
	
	/**
	 * HTTP/POST 请求
	 * @param params 请求参数
	 * @param responseHandler
	 */
	 public void post(RequestParams params, AsyncHttpResponseHandler responseHandler) {
		 String url = getUrl();
		 httpClient.post(url, params, responseHandler);
	  }
	 
	/**
	 * HTTP/PUT 请求
	 * @param params 请求参数
	 * @param responseHandler
	 */
	 public void put(RequestParams params, AsyncHttpResponseHandler responseHandler) {
		 String url = getUrl();
		 httpClient.put(url, params, responseHandler);
	  }
	 
	 /**
	 * HTTP/PUT 请求
	 * @param params 请求参数
	 * @param responseHandler
	 */
	 public void delete(RequestParams params, AsyncHttpResponseHandler responseHandler) {
		 String url = getUrl();
		 httpClient.delete(url, params, responseHandler);
	  }
	 
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Private methods
	 
	 // TODO: 性能优化
	 private synchronized void initHttpClient() {
		 if(httpClient == null) {
			 httpClient = new AsyncHttpClient(); 
		 }
	 }
}
