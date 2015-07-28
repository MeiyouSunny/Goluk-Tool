package cn.com.mobnote.golukmobile.carrecorder.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

//	public static String toMD5String(String str) {       
//		MessageDigest messageDigest = null;       
//		try {       
//			messageDigest = MessageDigest.getInstance("MD5");       
//	        messageDigest.reset();       
//	         
//	        messageDigest.update(str.getBytes("UTF-8"));       
//		} 
//		catch (Exception e) {
//			return null;
//		}   
//	         
//		byte[] byteArray = messageDigest.digest();       
//	         
//		StringBuffer md5StrBuff = new StringBuffer();       
//	            
//		for (int i = 0; i < byteArray.length; i++) {                   
//			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1){       
//				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
//			}else {      
//				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
//			}
//		}       
//	     
//		return md5StrBuff.toString().toUpperCase();      
//	}
	
	public static String hashKeyForDisk(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
		    cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
		    if (hex.length() == 1) {
		    	sb.append('0');
		    }
		    sb.append(hex);
		}
		return sb.toString();
	}

	public static boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(3000);
			urlConnection.setReadTimeout(5000);
			urlConnection.setRequestMethod("GET");
			in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
			out = new BufferedOutputStream(outputStream, 8 * 1024);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}

