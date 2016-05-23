package com.mobnote.golukmain.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.mobnote.util.GolukUtils;

public  class UrlHostManager {

	public static final String USER_CENTER_PATH = "/cdcRegister/modifyUserInfo.htm";
	public static final String UPLOAD_IMAG_PATH = "/fileService/HeadUploadServlet";
	// 国内版域名
	public static final String ONLINE_HOST = "https://s.goluk.cn";
	// 腾讯云域名
	public static final String QCLOUD_HOST = "https://q.goluk.cn";
	// 测试服务器
	public static final String TEST_HOST = "https://server.goluk.cn";
	// 开发服务器域名
	public static final String DEV_HOST = "https://svr.goluk.cn";
	// 国际版域名
	public static final String INTERNATIIONAL_HOST = "https://iserver.goluk.cn";

	public static final String ONLINE_WEBHOST = "https://surl.goluk.cn";
	public static final String TEST_WEBHOST = "https://surl3.goluk.cn";
	public static final String DEV_WEBHOST = "https://surl2.goluk.cn";
	public static final String QCLOUD_WEBHOST = "https://qsurl.goluk.cn";
	public static final String INTERNATION_WEBHOST = "https://isurl.goluk.cn";
	private String mHost = "";
	private static String mWebPageHost = "";

	private final String SERVER_FLAG_NAME = "serverflag";

	private static final String SIGN_INTERNATION = "invd";

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
		} else if (flag.equalsIgnoreCase(SIGN_INTERNATION)) {
			mHost = INTERNATIIONAL_HOST;
			mWebPageHost = INTERNATION_WEBHOST;
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
