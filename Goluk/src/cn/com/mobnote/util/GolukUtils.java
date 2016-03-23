package cn.com.mobnote.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.tiros.debug.GolukDebugUtils;

public class GolukUtils {
	/** Goluk绑定连接出现问题URL */
	public static final String URL_BIND_CONN_PROBLEM = "http://surl.goluk.cn/faq/link.html";

	public static float mDensity = 1.0f;

	public static final String T1S_WIFINAME_SIGN = "Goluk_T1S";
	public static final String T1_WIFINAME_SIGN = "Goluk_T1";
	public static final String G1G2_WIFINAME_SIGN = "Goluk";

	public static void getMobileInfo(Activity activity) {
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels; // 屏幕宽度（像素）
		int height = metric.heightPixels; // 屏幕高度（像素）
		mDensity = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）

		GolukDebugUtils.e("", " mobile info:" + mDensity);
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
		try {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse(url);
			intent.setData(content_url);
			mContext.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			Toast.makeText(mContext, mContext.getString(R.string.str_no_browser_found), Toast.LENGTH_SHORT).show();
			anfe.printStackTrace();
		}
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
		if (mToast == null) {
			mToast = Toast.makeText(GolukApplication.getInstance(), text, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(text);
		}

		mToast.show();
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
	private static Toast mToast = null;

	public static void showToast(Context context, String text, int duration) {
		if (mToast == null) {
			mToast = Toast.makeText(GolukApplication.getInstance(), text, duration);
		} else {
			mToast.setText(text);
			mToast.setDuration(duration);
		}

		mToast.show();
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
		int minute = t.minute;
		String aa = "" + minute;
		if (minute < 10) {
			aa = "0" + minute;
		}
		buffer.append(aa);
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

	public static String getCurrentFormatTime(Context context) {
		String time = DateFormat.format(context.getString(R.string.str_date_formatter),
				Calendar.getInstance().getTime()).toString();
		return time;
	}

	public static String getCurrentCommentTime() {
		Calendar calar = Calendar.getInstance();
		int year = calar.get(Calendar.YEAR);
		int month = calar.get(Calendar.MONTH) + 1;
		int day = calar.get(Calendar.DAY_OF_MONTH);
		int h = calar.get(Calendar.HOUR_OF_DAY);
		int m = calar.get(Calendar.MINUTE);
		int s = calar.get(Calendar.SECOND);
		int hm = calar.get(Calendar.MILLISECOND);

		StringBuffer sb = new StringBuffer();
		sb.append(year);
		if (month >= 10) {
			sb.append(month);
		} else {
			sb.append("0" + month);
		}

		if (day >= 10) {
			sb.append(day);
		} else {
			sb.append("0" + day);
		}
		if (h >= 10) {
			sb.append(h);
		} else {
			sb.append("0" + h);
		}

		if (m >= 10) {
			sb.append(m);
		} else {
			sb.append("0" + m);
		}
		if (s >= 10) {
			sb.append(s);
		} else {
			sb.append("0" + s);
		}

		if (hm >= 100) {
			sb.append(hm);
		} else if (hm >= 10 && hm < 100) {
			sb.append("0" + hm);
		} else {
			sb.append("00" + hm);
		}

		return sb.toString();
	}

	public static String formatTimeYMDHMS(String date) {
		String time = "";
		if (null != date) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINESE);

			try {
				Date strtodate = formatter.parse(date);
				if (null != strtodate) {
					formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
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
	 * 获取评论列表显示时间规则()
	 * 
	 * @param time
	 *            类似2010-11-20 11:10:10
	 * @return
	 * @author jyf
	 * @date 2015年8月7日
	 */
	public static String getCommentShowFormatTime(Context context, String time) {
		try {
			String result = formatTimeYMDHMS(time);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE);
			Date oldDate = formatter.parse(result);
			// 转换成 2010-11-20 11:10
			String ymdhm = formatter.format(oldDate);

			result = ymdhm;

			// 视频相关时间
			Calendar c1 = Calendar.getInstance();
			c1.setTime(oldDate);
			int oldYear = c1.get(Calendar.YEAR);
			int oldMonth = c1.get(Calendar.MONTH) + 1;
			int oldDay = c1.get(Calendar.DAY_OF_MONTH);

			// 当前时间
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(new Date());
			int currentYear = currentCalendar.get(Calendar.YEAR);
			int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;
			int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

			if (currentYear == oldYear && oldMonth == currentMonth && oldDay == currentDay) {
				// 今天
				SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);
				String todayFormatStr = hhmmFormat.format(oldDate);
				result = context.getString(R.string.str_today) + " " + todayFormatStr;
			} else if (currentYear == oldYear && oldMonth == currentMonth && oldDay + 1 == currentDay) {
				// 昨天
				SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);
				String todayFormatStr = hhmmFormat.format(oldDate);
				result = context.getString(R.string.str_yestoday)+" " + todayFormatStr;
			} else if (currentYear == oldYear) {
				// 本年
				SimpleDateFormat hhmmFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINESE);
				String todayFormatStr = hhmmFormat.format(oldDate);
				result = todayFormatStr;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return time;
	}

	public static String getFormatNumber(String fmtnumber) {
		String number;
		try {
			int wg = Integer.parseInt(fmtnumber);

			if (wg < 100000) {
				DecimalFormat df = new DecimalFormat("#,###");
				number = df.format(wg);
			} else {
				number = "100,000+";
			}
		} catch (Exception e) {
			return fmtnumber;
		}

		return number;
	}
	
	public static String getFormatNumber(int fmtnumber) {
		String number;

		if (fmtnumber > 10000) {
			DecimalFormat df = new DecimalFormat("0.0");
			number = df.format((float)fmtnumber/1000) + "K";
		} else {
			number = "" + fmtnumber;
		}
		return number;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getNewCategoryShowTime(Context context, String date) {
		final long MINTUE = 60 * 1000;
		final long HOUR = 60 * MINTUE;
		final long DAY = 24 * HOUR;
		final long WEEK = 7 * DAY;

		String time = null;
		try {
			long curTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			Date strtodate = formatter.parse(date);
			long historytime = strtodate.getTime();

			Date curDate = new Date(curTime);
			int curYear = curDate.getYear();
			int history = strtodate.getYear();

			long diff = Math.abs(historytime - curTime);// 时间差
			if (curYear == history) {
				if (diff <= WEEK && diff > DAY) {
					return time = diff / DAY + context.getString(R.string.str_day_refresh);// 天前更新
				} else if (diff <= DAY && diff > HOUR) {
					return time = diff / HOUR + context.getString(R.string.str_hours_refresh);// 小时前更新
				} else if (diff <= HOUR) {
					int min = (int) (diff / MINTUE);
					if (min < 1) {
						min = 1;
					}
					return time = min + context.getString(R.string.str_minute_refresh);// 分钟前更新
				} else {
					SimpleDateFormat jn = new SimpleDateFormat(context.getString(R.string.str_month_day_refresh));
					return jn.format(strtodate);// 今年内：月日更新
				}
			} else {
				SimpleDateFormat jn = new SimpleDateFormat(context.getString(R.string.str_year_month_day_refresh));
				return jn.format(strtodate);// 非今年：年月日更新
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return time;
	}

	public static boolean isCanClick = true;
	private static Timer mTimer = null;

	public static void startTimer(int time) {
		isCanClick = false;
		cancelTimer();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				isCanClick = true;
			}
		}, time);
	}

	public static void cancelTimer() {
		if (null != mTimer) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	/**
	 * 把drawable中的资源图片转换成Uri格式
	 * 
	 * @param resId
	 * @return
	 * @author jyf
	 */
	public static Integer getResourceUri(int resId) {
		return Integer.valueOf(resId);
	}

	@SuppressLint("SimpleDateFormat")
	public static String getTime(String date) {
		String time = null;
		try {
			long curTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date strtodate = formatter.parse(date);

			Date curDate = new Date(curTime);
			int curYear = curDate.getYear();
			int history = strtodate.getYear();

			if (curYear == history) {
				SimpleDateFormat jn = new SimpleDateFormat("-MM.dd-");
				return jn.format(strtodate);// 今年内：月日更新
			} else {
				SimpleDateFormat jn = new SimpleDateFormat("-yyyy.MM.dd-");
				return jn.format(strtodate);// 非今年：年月日更新
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return time;
	}

	public static String getAssestFileContent(String fileName) {
		if (null == fileName || "".equals(fileName)) {
			return "";
		}
		String result = "";
		InputStream is = null;
		try {
			is = GolukApplication.getInstance().getAssets().open(fileName);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			result = new String(buffer, "GB2312");
		} catch (Exception e) {

		} finally {
			if (null != is) {
				try {
					is.close();
					is = null;
				} catch (IOException e) {
				}

			}
		}

		return result;

	}

	public static String compute32(byte[] content) {
		StringBuffer buf = new StringBuffer("");
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			try {
				md.update(content);
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte b[] = md.digest();
			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return buf.toString();
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static boolean isActivityAlive(Activity activity) {
		if (activity == null) {
			return false;
		}

		if (Build.VERSION.SDK_INT > 16) {
			if (activity.isDestroyed() || activity.isFinishing()) {
				return false;
			}
		} else {
			if (activity.isFinishing()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 打开系统wifi列表
	 * 
	 * @param context
	 * @author jyf
	 */
	public static void startSystemWifiList(Context context) {
		if (null == context) {
			return;
		}
		Intent intent = new Intent();
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
		context.startActivity(intent);
	}

	public static boolean isTestServer() {
		String serverSign = GolukUtils.getAssestFileContent("serverflag");
		GolukDebugUtils.e("aaa", "serverSign: " + serverSign);
		if (null != serverSign && (serverSign.trim().equals("test") || serverSign.trim().equals("dev"))) {
			return true;
		} else {
			return false;
		}
	}

	private static long lastClickTime = 0;
	public static final int MIN_CLICK_DELAY_TIME = 500;

	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		if (time - lastClickTime < MIN_CLICK_DELAY_TIME) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	/**
	 * 通过wifi的原始 ssid ，获取设备类型 (G系列，T系列)
	 * 
	 * @param mWillConnName
	 * @return
	 * @author jyf
	 */
	public static String getIpcTypeFromName(String mWillConnName) {
		if (null == mWillConnName) {
			return "";
		}
		String ipcType = "";
		if (mWillConnName.startsWith(T1S_WIFINAME_SIGN)) {
			ipcType = IPCControlManager.MODEL_G;
		} else if (mWillConnName.startsWith(T1_WIFINAME_SIGN)) {
			ipcType = IPCControlManager.MODEL_T;
		} else if (mWillConnName.startsWith(G1G2_WIFINAME_SIGN)) {
			ipcType = IPCControlManager.MODEL_G;
		} else {

		}
		GolukDebugUtils.e("", "WifiBindList----getIpcType: " + ipcType);
		return ipcType;
	}
	
	/**
	 * 获取国家语言编码
	 * 
	 * @return
	 */
	public static String getLanguage() {
		return Locale.getDefault().getLanguage();
	}

	/**
	 * 获取国家地区编码
	 * 
	 * @return
	 */
	private static String getCountry() {
		return Locale.getDefault().getCountry();
	}
	
	/**
	 * 判断时国际版还是国内版 
	 * 
	 * 国内０　　国际１ 默认为国际
	 * 
	 * @return
	 */
	public static String getCommversion() {
		String commversion = "1";
		if (!"zh".equals(getLanguage())) {
			commversion = "1";
		} else {
			commversion = "0";
		}
		return commversion;
	}
	
	/**
	 * 获取语言与国家
	 * 
	 * @return
	 */
	public static String getLanguageAndCountry() {

		final String realZone = getLanguage() + "_" + getCountry();

		String[] allZone = GolukApplication.getInstance().getApplicationContext().getResources()
				.getStringArray(R.array.zone_array);
		if (null == allZone || allZone.length <= 0) {
			return realZone;
		}
		final int length = allZone.length;
		for (int i = 0; i < length; i++) {
			if (realZone.equals(allZone[i])) {
				return allZone[i];
			}
		}
		return realZone;
	}
	
}
