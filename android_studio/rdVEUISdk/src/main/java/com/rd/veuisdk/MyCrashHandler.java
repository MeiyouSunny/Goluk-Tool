package com.rd.veuisdk;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.utils.FileLog;

/**
 * 全局异常处理器
 * 
 * @author abreal
 * 
 */
public class MyCrashHandler implements UncaughtExceptionHandler {
	private static MyCrashHandler myCrashHandler;
	private Context context;
	private String os_version;

	/** 系统默认的UncaughtException处理类 */
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	private MyCrashHandler() {

	}

	public static synchronized MyCrashHandler getInstance() {
		if (myCrashHandler != null) {
			return myCrashHandler;
		} else {
			myCrashHandler = new MyCrashHandler();
			return myCrashHandler;
		}
	}

	public void init(Context context) {
		this.context = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void uncaughtException(Thread t, Throwable th) {
		try {
			String errorinfo = getErrorInfo(th);
			String headstring = "";
			int nIndex = -1;
			if ((nIndex = errorinfo.indexOf("Caused by:")) >= 0) {
				String ssString = errorinfo.substring(nIndex);
				String[] ss = ssString.split("\n\t");
				headstring = ss[0] + "\n\t" + ss[1] + "\n\t" + ss[2] + "\n\t";
			}
			os_version = getOsVersion(context) + "\nversion:"
					+ CoreUtils.getVersionName(context);
			String stacktrace = "Crached:\r\n" + "os_version:" + os_version
					+ "\r\n";
			stacktrace += "deviceid:" + Build.MANUFACTURER + Build.PRODUCT
					+ "\r\n";
			stacktrace += headstring + errorinfo;

			Log.w("CrashHandler", stacktrace);
			FileLog.writeLog(stacktrace.toString());
			// SdkEntry.onExitApp(context, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (null != mDefaultHandler) {
			mDefaultHandler.uncaughtException(t, th);
		}
	}

	private String getErrorInfo(Throwable t) {
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		t.printStackTrace(pw);
		pw.close();
		String error = writer.toString();
		return error;
	}

	/**
	 * get OS number
	 * 
	 * @param context
	 * @return
	 */
	public static String getOsVersion(Context context) {
		String osVersion = android.os.Build.VERSION.RELEASE;
		return osVersion;
	}

}