package com.mobnote.golukmain.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.mobnote.util.GolukUtils;

public  class UrlHostManager {

	public static final String USER_CENTER_PATH = "/cdcRegister/modifyUserInfo.htm";
	public static final String UPLOAD_IMAG_PATH = "/fileService/HeadUploadServlet";
	public static final String ONLINE_HOST = "http://s.goluk.cn";
	public static final String QCLOUD_HOST = "http://q.goluk.cn";
	public static final String TEST_HOST = "http://server.goluk.cn";
	public static final String DEV_HOST = "http://svr.goluk.cn";
	public static final String ONLINE_WEBHOST = "http://surl.goluk.cn";
	public static final String TEST_WEBHOST = "http://surl3.goluk.cn";
	public static final String DEV_WEBHOST = "http://surl2.goluk.cn";
	public static final String QCLOUD_WEBHOST = "http://qsurl.goluk.cn";
	private String mHost = "";
	private static String mWebPageHost = "";

	private final String SERVER_FLAG_NAME = "serverflag";

	UrlHostManager() {

		String flag = GolukUtils.getAssestFileContent(SERVER_FLAG_NAME).trim();
		if (flag.equalsIgnoreCase("nvd")) {
			mHost = ONLINE_HOST;
			mWebPageHost = ONLINE_WEBHOST;
		} else if (flag.equalsIgnoreCase("test")) {
			mHost = TEST_HOST;
			mWebPageHost = TEST_WEBHOST;
		} else if (flag.equalsIgnoreCase("txy")) {
			mHost = QCLOUD_HOST;
			mWebPageHost = QCLOUD_WEBHOST;
		} else {
			mHost = DEV_HOST;
			mWebPageHost = DEV_WEBHOST;
		}
	}
	
	public static String getEncodedUrlParams(Map<String, String> params)
			throws AuthFailureError {

		StringBuilder encodedParams = new StringBuilder();
		String paramsEncoding = "UTF-8";
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (null == entry.getValue()) {
					continue;
				}
				encodedParams.append(URLEncoder.encode(entry.getKey(),
						paramsEncoding));
				encodedParams.append('=');
				encodedParams.append(URLEncoder.encode(entry.getValue(),
						paramsEncoding));
				encodedParams.append('&');
			}
			return encodedParams.toString();
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Encoding not supported: "
					+ paramsEncoding, uee);
		}
	}

    public String getHost() {
    	return mHost;
    }
    
    public static String getWebPageHost() {
    	return mWebPageHost;
    }
}
