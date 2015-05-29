package cn.com.mobnote.util;

import java.io.FileOutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.widget.Toast;

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
	 * 
	 * @param url
	 * @param mContext
	 */
	public static void openUrl(String url, Context mContext) {
		// 版本升级---打开浏览器
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse(url);
		intent.setData(content_url);
		mContext.startActivity(intent);
	}

	// 获取版本号
	public static String getVersion(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static int getVersionCode(Context context)// 获取版本号(内部识别号)
	{
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 显示短提示
	 * @param context
	 * @param text 需要显示的文本信息
	 * @author xuhw
	 * @date 2015年5月29日
	 */
	public void showToast(Context context, String text){
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 显示短提示
	 * @param context 上下文
	 * @param text 需要显示的文本信息
	 * @param duration 信息显示持续时间
	 * @author xuhw
	 * @date 2015年5月29日
	 */
	public void showToast(Context context, String text, int duration){
		Toast.makeText(context, text, duration).show();
	}
	
	/**
	 * 写文件
	 * @param filename 文件绝对路径
	 * @param msg 写入文件的信息
	 * @param append ture:追加方式写入文件 flase:覆盖的方式写入文件
	 * @author xuhw
	 * @date 2015年5月29日
	 */
	public static void writeFile(String filename, String msg, boolean append){
        try{
        	FileOutputStream fos = new FileOutputStream(filename, append);
        	fos.write(msg.getBytes());
        	fos.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

}
