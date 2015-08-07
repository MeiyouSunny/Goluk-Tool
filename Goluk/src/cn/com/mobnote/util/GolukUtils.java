package cn.com.mobnote.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class GolukUtils {
	/** Goluk綁定视频连接地址 */
	public static final String URL_BIND_HELP = "http://surl.goluk.cn/faq/video.html";
	/** Goluk绑定连接出现问题URL */
	public static final String URL_BIND_CONN_PROBLEM = "http://surl.goluk.cn/faq/link.html";

	public static void getMobileInfo(Activity activity) {
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels; // 屏幕宽度（像素）
		int height = metric.heightPixels; // 屏幕高度（像素）
		float density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
	}

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
	 * 
	 * @param context
	 * @param text
	 *            需要显示的文本信息
	 * @author xuhw
	 * @date 2015年5月29日
	 */
	public static void showToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 显示短提示
	 * 
	 * @param context
	 *            上下文
	 * @param text
	 *            需要显示的文本信息
	 * @param duration
	 *            信息显示持续时间
	 * @author xuhw
	 * @date 2015年5月29日
	 */
	public static void showToast(Context context, String text, int duration) {
		Toast.makeText(context, text, duration).show();
	}

	/**
	 * 写文件
	 * 
	 * @param filename
	 *            文件绝对路径
	 * @param msg
	 *            写入文件的信息
	 * @param append
	 *            ture:追加方式写入文件 flase:覆盖的方式写入文件
	 * @author xuhw
	 * @date 2015年5月29日
	 */
	public static void writeFile(String filename, String msg, boolean append) {
		try {
			FileOutputStream fos = new FileOutputStream(filename, append);
			fos.write(msg.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void freeBitmap(Bitmap bitmap) {
		if (null != bitmap && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	public static void freeBitmap(Drawable drawable) {
		if (null == drawable) {
			return;
		}
		BitmapDrawable bd = (BitmapDrawable) drawable;
		Bitmap bitmap = bd.getBitmap();
		if (null != bitmap && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	/**
	 * 检查sd卡剩余容量是否可用
	 * 
	 * @param filesize
	 *            文件大小 MB
	 * @return
	 * @author xuhw
	 * @date 2015年6月10日
	 */
	public static boolean checkSDStorageCapacity(double filesize) {
		float availableSize = 0;
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		availableSize = (float) (blockSize * availableBlocks / 1024) / 1024;
		if ((availableSize - 10) >= filesize) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 打开手机系统WIFI列表
	 * 
	 * @param context
	 * @author jyf
	 * @date 2015年7月3日
	 */
	public static void showSystemWifiList(Context context) {
		Intent intent = new Intent();
		intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
		context.startActivity(intent);
	}

	public static int getSystemSDK() {
		try {
			return Integer.parseInt(android.os.Build.VERSION.SDK);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * 获取系统版本号
	 */
	public static String getSystem_version() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * 获取手机型号
	 */
	public static String getPhone_models() {
		return android.os.Build.MODEL;
	}

	@SuppressLint("NewApi")
	public static Bitmap createVideoThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			bitmap = retriever.getFrameAtTime();
		} catch (IllegalArgumentException ex) {
		} catch (RuntimeException ex) {
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
			}
		}
		return bitmap;
	}

	public static String getCurrentTime() {
		StringBuffer buffer = new StringBuffer();
		Time t = new Time();
		t.setToNow();
		buffer.append(t.hour);
		buffer.append(":");
		buffer.append(t.minute);
		return new String(buffer);
	}

	@SuppressLint("SimpleDateFormat")
	public static String formatTime(String date) {
		String time = "";
		if (null != date) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

			try {
				Date strtodate = formatter.parse(date);
				if (null != strtodate) {
					// formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					if (null != formatter) {
						time = formatter.format(strtodate);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return time;
	}

	/**
	 * 弹出软键盘
	 * 
	 * @param edit
	 * @author jyf
	 * @date 2015年8月7日
	 */
	public static void showSoft(final EditText edit) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) edit.getContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(edit, 0);
			}
		}, 500);
	}
	
	public static String getCurrentFormatTime() {
		String time =DateFormat.format("yyyy-MM-dd hh:mm:ss", Calendar.getInstance().getTime()).toString();
		return time;
	}

}
