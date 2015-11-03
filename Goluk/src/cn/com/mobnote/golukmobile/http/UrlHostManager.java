package cn.com.mobnote.golukmobile.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;

public  class UrlHostManager {

	public static final String USER_CENTER_PATH = "/cdcRegister/modifyUserInfo.htm";
	public static final String UPLOAD_IMAG_PATH = "/fileService/HeadUploadServlet";
	public static final String ONLINE_HOST = "http://s.goluk.cn";
	public static final String TEST_HOST = "http://server.goluk.cn";
	private String mHost = "";
	private Context mContext;
	private final String SERVER_FLAG_NAME = "serverflag";
	UrlHostManager(Context context) {
		mContext = context;
		String flag = getFromAsset(SERVER_FLAG_NAME);
		if (flag.equalsIgnoreCase("nvd")) {
			mHost = ONLINE_HOST;
		} else if (flag.equalsIgnoreCase("test")) {
			mHost = TEST_HOST;
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

    private String getFromAsset(String fileName) {
    	String Result="";
    	try { 
            InputStreamReader inputReader = new InputStreamReader(mContext.getAssets().open(fileName) ); 
           BufferedReader bufReader = new BufferedReader(inputReader);
           String line="";
           while((line = bufReader.readLine()) != null)
               Result += line;
           return Result;
       } catch (Exception e) { 
           e.printStackTrace(); 
       }
    	return Result;
    }

    public String getHost() {
    	return mHost;
    }
}
