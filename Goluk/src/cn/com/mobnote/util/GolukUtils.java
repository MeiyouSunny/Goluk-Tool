package cn.com.mobnote.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class GolukUtils {

	/**
	 * 秒转换为 时：分：秒
	 * 
	 * @param second
	 * @return
	 * @author jiayf
	 * @date Apr 13, 2015
	 */
	public static String secondToString(final int second) {
		String timeStr = "";
		if (second >= 60) {
			int hour = second / 3600; // 时
			int restMinS = second - hour * 3600;
			int min = restMinS / 60; // 分
			int sec = restMinS % 60; // 秒

			String hourStr = "";
			String minStr = "";
			String secStr = "";

			if (hour > 0) {
				if (hour < 10) {
					hourStr = "0" + hour + ":";
				} else {
					hourStr = "" + hour + ":";
				}

			}

			if (min >= 10) {
				minStr = min + ":";
			} else {
				minStr = "0" + min + ":";
			}
			if (sec >= 10) {
				secStr = sec + "";
			} else {
				secStr = "0" + sec;
			}

			timeStr = hourStr + minStr + secStr;

		} else {
			if (second >= 10) {
				timeStr = "00:" + second;
			} else {
				timeStr = "00:0" + second;
			}
		}

		return timeStr;
	}
	
	/**
	 * 默认浏览器打开指定的url
	 * @param url
	 * @param mContext
	 */
	public static void openUrl(String url,Context mContext){
		//版本升级---打开浏览器
		Intent intent = new Intent();       
	    intent.setAction("android.intent.action.VIEW");   
	    Uri content_url = Uri.parse(url);  
	    intent.setData(content_url); 
	     mContext.startActivity(intent);
	}

}
