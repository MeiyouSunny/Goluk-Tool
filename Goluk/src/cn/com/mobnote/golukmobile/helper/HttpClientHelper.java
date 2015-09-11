package cn.com.mobnote.golukmobile.helper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * @描述 HttpClient辅助类
 * @作者 卜长清，buchangqing@goluk.com
 * @日期 2015-09-09
 * @版本 1.0
 */
public class HttpClientHelper {
	private static HttpClient httpClient = null;
	private String protocol = "http";																// 使用的协议，默认HTTP
	private String server = "svr.goluk.cn";														// 服务器
	private String service = "cdcComment/comment.htm";							// 服务
	
	public HttpClientHelper() {
		initHttpClient();
	}
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
	
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
	
	 public static synchronized HttpClient getHttpClient() {
		 if(httpClient == null) {
			 final HttpParams httpParams = new BasicHttpParams();  
			 httpClient = new DefaultHttpClient(httpParams); 
		 }
		 return httpClient;
	 }
	 
	 // TODO: 性能优化
	 private synchronized void initHttpClient() {
		 if(httpClient == null) {
			 final HttpParams httpParams = new BasicHttpParams();  
			 httpClient = new DefaultHttpClient(httpParams); 
		 }
	 }

	/**
	 * HTTP/GET 请求
	 * @param params 参数表
	 * @return 响应内容
	 */
	public String get(LinkedList<BasicNameValuePair> params) {
		String param = URLEncodedUtils.format(params, "UTF-8");
		String url = String.format("%s://%s/%s?%s", protocol, server, service, param);
		HttpGet getMethod = new HttpGet(url);
		 int code = -1;
		 String content = null;
		
		try {  
			HttpResponse response = httpClient.execute(getMethod); 
		    code = response.getStatusLine().getStatusCode();
		    content = EntityUtils.toString(response.getEntity(), "utf-8");
		} catch (ClientProtocolException e) {  
			Log.e("goluk", "Http GET request error: " + e.getMessage());
		    e.printStackTrace();
		} catch (IOException e) {
			Log.e("goluk", "Http GET request error: " + e.getMessage());
		    e.printStackTrace();  
		} 
		
		return content;
	}

	/**
	 * HTTP/POST 请求
	 * @param params 参数表
	 * @return 
	 */
	public String post(LinkedList<BasicNameValuePair> params) {
		String url = String.format("%s://%s/%s", protocol, server, service);
		HttpPost postMethod = new HttpPost(url);
		 int code = -1;
		 String content = null;
		
		try {  
			postMethod.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
		    HttpResponse response = httpClient.execute(postMethod); 
		    code = response.getStatusLine().getStatusCode();
		    content = EntityUtils.toString(response.getEntity(), "utf-8");
		} catch (UnsupportedEncodingException e) { 
			Log.e("goluk", "Params cannot be encoded: " + e.getMessage());
		    e.printStackTrace();  
		} catch (ClientProtocolException e) {  
			Log.e("goluk", "Http POST request error: " + e.getMessage());
		    e.printStackTrace();  
		} catch (IOException e) {  
			Log.e("goluk", "Http POST request error: " + e.getMessage());
		    e.printStackTrace();  
		} 
		
		return content;
	}
}
