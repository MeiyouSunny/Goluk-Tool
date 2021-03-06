package com.mobnote.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.fan.FanListActivity;
import com.mobnote.golukmain.following.FollowingListActivity;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.startshare.VideoEditActivity;
import com.mobnote.golukmain.usercenter.NewUserCenterActivity;
import com.mobnote.golukmain.usercenter.UCUserInfo;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import cn.com.tiros.debug.GolukDebugUtils;

public class GolukUtils {

	/** 1 ??????????????? ???0 ??????????????? ?????????????????????????????? */
	public static final String GOLUK_APP_VERSION = "1";

	/** Goluk????????????????????????URL */
	public static final String URL_BIND_CONN_PROBLEM = "http://surl.goluk.cn/faq/link.html";

	public static float mDensity = 1.0f;

	public static final String T1S_WIFINAME_SIGN = "Goluk_T1S";
	public static final String T1_WIFINAME_SIGN = "Goluk_T1";
	public static final String G1G2_WIFINAME_SIGN = "Goluk";

	// ???????????????
	private static int keyBoardHeight = 250;
	private static boolean isSettingBoardHeight = false;

	public static int getKeyBoardHeight() {
		return keyBoardHeight;
	}

	public static void setKeyBoardHeight(int height) {
		if (height <= 0) {
			return;
		}
		keyBoardHeight = height;
		isSettingBoardHeight = true;
	}

	public static boolean isSettingBoardHeight() {
		return isSettingBoardHeight;
	}

	public static void getMobileInfo(Activity activity) {
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels; // ????????????????????????
		int height = metric.heightPixels; // ????????????????????????
		mDensity = metric.density; // ???????????????0.75 / 1.0 / 1.5???
		int densityDpi = metric.densityDpi; // ????????????DPI???120 / 160 / 240???

		keyBoardHeight = (int) (keyBoardHeight * mDensity);

		GolukDebugUtils.e("", " mobile info:" + mDensity);
	}

	public static String getDefaultZone() {
		String current = getLanguageAndCountry();
		if (current.equals("zh_CN")) {
			return "CN +86";
		} else {
			return "US +1";
		}
	}

	/**
	 * ???????????? ???????????????
	 * 
	 * @param second
	 * @return
	 * @author jiayf
	 * @date Apr 13, 2015
	 */
	public static String secondToString(final int second) {
		String timeStr = "";
		if (second >= 60) {
			int hour = second / 3600; // ???
			int restMinS = second - hour * 3600;
			int min = restMinS / 60; // ???
			int sec = restMinS % 60; // ???

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
	 * ??????????????????????????????url
	 * 
	 * @param url
	 * @param mContext
	 */
	public static void openUrl(String url, Context mContext) {
		// ????????????---???????????????
		try {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse(url);
			intent.setData(content_url);
			mContext.startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			Toast.makeText(mContext,
					mContext.getString(R.string.str_no_browser_found),
					Toast.LENGTH_SHORT).show();
			anfe.printStackTrace();
		}
	}

	// ???????????????
	public static String getVersion(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static int getVersionCode(Context context)// ???????????????(???????????????)
	{
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * ???????????????
	 * 
	 * @param context
	 * @param text
	 *            ???????????????????????????
	 * @author xuhw
	 * @date 2015???5???29???
	 */
	public static void showToast(Context context, String text) {
		if (mToast == null) {
			mToast = Toast.makeText(GolukApplication.getInstance(), text,
					Toast.LENGTH_SHORT);
		} else {
			mToast.setText(text);
		}

		mToast.show();
	}

	/**
	 * ???????????????
	 * 
	 * @param context
	 *            ?????????
	 * @param text
	 *            ???????????????????????????
	 * @param duration
	 *            ????????????????????????
	 * @author xuhw
	 * @date 2015???5???29???
	 */
	private static Toast mToast = null;

	public static void showToast(Context context, String text, int duration) {
		if (mToast == null) {
			mToast = Toast.makeText(GolukApplication.getInstance(), text,
					duration);
		} else {
			mToast.setText(text);
			mToast.setDuration(duration);
		}

		mToast.show();
	}

	/**
	 * ?????????
	 * 
	 * @param filename
	 *            ??????????????????
	 * @param msg
	 *            ?????????????????????
	 * @param append
	 *            ture:???????????????????????? flase:???????????????????????????
	 * @author xuhw
	 * @date 2015???5???29???
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
	 * ??????sd???????????????????????????
	 * 
	 * @param filesize
	 *            ???????????? MB
	 * @return
	 * @author xuhw
	 * @date 2015???6???10???
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
	 * ??????????????????WIFI??????
	 * 
	 * @param context
	 * @author jyf
	 * @date 2015???7???3???
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
	 * ?????????????????????
	 */
	public static String getSystem_version() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * ??????????????????
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
	 * ???????????????
	 * 
	 * @param edit
	 * @author jyf
	 * @date 2015???8???7???
	 */
	public static void showSoft(final EditText edit) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) edit
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(edit, 0);
			}
		}, 500);
	}

	public static final void showSoftNotThread(final View view) {
		InputMethodManager inputManager = (InputMethodManager) view
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(view, 0);
	}

	// ??????????????????
	@SuppressLint("NewApi")
	public static void hideSoft(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static String getCurrentFormatTime(Context context) {
		String time = DateFormat.format(
				context.getString(R.string.str_date_formatter),
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
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS", Locale.CHINESE);

			try {
				Date strtodate = formatter.parse(date);
				if (null != strtodate) {
					formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
							Locale.CHINESE);
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
	 * ????????????????????????????????????()
	 * 
	 * @param time
	 *            ??????2010-11-20 11:10:10
	 * @return
	 * @author jyf
	 * @date 2015???8???7???
	 */
	public static String getCommentShowFormatTime(Context context, String time) {
		try {
			String result = formatTimeYMDHMS(time);
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm", Locale.CHINESE);
			Date oldDate = formatter.parse(result);
			// ????????? 2010-11-20 11:10
			String ymdhm = formatter.format(oldDate);

			result = ymdhm;

			// ??????????????????
			Calendar c1 = Calendar.getInstance();
			c1.setTime(oldDate);
			int oldYear = c1.get(Calendar.YEAR);
			int oldMonth = c1.get(Calendar.MONTH) + 1;
			int oldDay = c1.get(Calendar.DAY_OF_MONTH);

			// ????????????
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(new Date());
			int currentYear = currentCalendar.get(Calendar.YEAR);
			int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;
			int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

			if (currentYear == oldYear && oldMonth == currentMonth
					&& oldDay == currentDay) {
				// ??????
				SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm",
						Locale.CHINESE);
				String todayFormatStr = hhmmFormat.format(oldDate);
				result = context.getString(R.string.str_today) + " "
						+ todayFormatStr;
			} else if (currentYear == oldYear && oldMonth == currentMonth
					&& oldDay + 1 == currentDay) {
				// ??????
				SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm",
						Locale.CHINESE);
				String todayFormatStr = hhmmFormat.format(oldDate);
				result = context.getString(R.string.str_yestoday) + " "
						+ todayFormatStr;
			} else if (currentYear == oldYear) {
				// ??????
				SimpleDateFormat hhmmFormat = new SimpleDateFormat(
						"MM-dd HH:mm", Locale.CHINESE);
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

	public static String getFormatedNumber(String fmtnumber) {
		String number;
		try {
			int wg = Integer.parseInt(fmtnumber);

			if (wg >= 10000) {
				DecimalFormat df = new DecimalFormat("0.0");
				number = df.format((float) wg / 1000) + "K";
			} else {
				number = "" + fmtnumber;
			}
		} catch (Exception e) {
			return fmtnumber;
		}

		return number;
	}

	public static String getFormatNumber(int fmtnumber) {
		String number;

		if (fmtnumber >= 10000) {
			DecimalFormat df = new DecimalFormat("0.0");
			number = df.format((float) fmtnumber / 1000) + "K";
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
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyyMMddHHmmssSSS");
			Date strtodate = formatter.parse(date);
			long historytime = strtodate.getTime();

			Date curDate = new Date(curTime);
			int curYear = curDate.getYear();
			int history = strtodate.getYear();

			long diff = Math.abs(historytime - curTime);// ?????????
			if (curYear == history) {
				if (diff <= WEEK && diff > DAY) {
					return time = context.getString(R.string.str_day_refresh,
							(diff / DAY));// ????????????
				} else if (diff <= DAY && diff > HOUR) {
					return time = context.getString(R.string.str_hours_refresh,
							(diff / HOUR));// ???????????????
				} else if (diff <= HOUR) {
					int min = (int) (diff / MINTUE);
					if (min < 1) {
						min = 1;
					}
					return time = context.getString(
							R.string.str_minute_refresh, min);// ???????????????
				} else {
					SimpleDateFormat jn = new SimpleDateFormat(
							context.getString(R.string.str_month_day_refresh));
					return jn.format(strtodate);// ????????????????????????
				}
			} else {
				SimpleDateFormat jn = new SimpleDateFormat(
						context.getString(R.string.str_year_month_day_refresh));
				return jn.format(strtodate);// ???????????????????????????
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
	 * ???drawable???????????????????????????Uri??????
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
				return jn.format(strtodate);// ????????????????????????
			} else {
				SimpleDateFormat jn = new SimpleDateFormat("-yyyy.MM.dd-");
				return jn.format(strtodate);// ???????????????????????????
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

	public static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
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
	 * ????????????wifi??????
	 * 
	 * @param context
	 * @author jyf
	 */
	public static void startSystemWifiList(Context context) {
		if (null == context) {
			return;
		}
		try {
			Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
			context.startActivity(intent);
		} catch (Exception e) {

		}

	}

	public static boolean isTestServer() {
		String serverSign = GolukUtils.getAssestFileContent("serverflag");
		GolukDebugUtils.e("aaa", "serverSign: " + serverSign);
		if (null != serverSign
				&& (serverSign.trim().equals("test") || serverSign.trim()
						.equals("dev"))) {
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
	 * ??????wifi????????? ssid ????????????????????? (G?????????T??????)
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
	 * ????????????????????????
	 * 
	 * @return
	 */
	public static String getLanguage() {
		return Locale.getDefault().getLanguage();
	}

	/**
	 * ????????????????????????
	 * 
	 * @return
	 */
	private static String getCountry() {
		return Locale.getDefault().getCountry();
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * ???????????????????????? ???????????????
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
	 * ?????????????????????
	 * 
	 * @return
	 */
	public static String getLanguageAndCountry() {

		final String realZone = getLanguage() + "_" + getCountry();

		String[] allZone = GolukApplication.getInstance()
				.getApplicationContext().getResources()
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

	public static void startUserCenterActivity(Context context, String uid,
			String nickname, String avatar, String customAvatar, String sex,
			String introduction) {

		if (!isNetworkConnected(context)) {
			Toast.makeText(context, context.getString(R.string.network_error),
					Toast.LENGTH_SHORT).show();
			return;
		}

		UCUserInfo user = new UCUserInfo();
		user.uid = uid;
		user.nickname = nickname;
		user.headportrait = avatar;
		user.introduce = introduction;
		user.sex = sex;
		user.customavatar = customAvatar;
		user.praisemenumber = "0";
		user.sharevideonumber = "0";
		Intent i = new Intent(context, NewUserCenterActivity.class);
		i.putExtra("userinfo", user);
		i.putExtra("type", 0);
		context.startActivity(i);
	}

	public static void startVideoDetailActivity(Context context, String videoId) {
		Intent intent = null;
		intent = new Intent(context, VideoDetailActivity.class);
		intent.putExtra(VideoDetailActivity.VIDEO_ID, videoId);
		intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
		context.startActivity(intent);
	}

	public static void startFollowingListActivity(Context context, String uId) {

		if (!isNetworkConnected(context)) {
			Toast.makeText(context, context.getString(R.string.network_error),
					Toast.LENGTH_SHORT).show();
			return;
		}

		Intent intent = null;
		intent = new Intent(context, FollowingListActivity.class);
		intent.putExtra("linkuid", uId);
		context.startActivity(intent);
	}

	public static void startFanListActivity(Context context, String uId) {
		if (!isNetworkConnected(context)) {
			Toast.makeText(context, context.getString(R.string.network_error),
					Toast.LENGTH_SHORT).show();
			return;
		}

		Intent intent = null;
		intent = new Intent(context, FanListActivity.class);
		intent.putExtra("linkuid", uId);
		context.startActivity(intent);
	}

	/**
	 * ?????????????????????,???????????????????????????????????????????????????????????????????????????
	 * @param context
	 */
	public static void startLoginActivity(Context context){
		Intent intent = new Intent();
		if(GolukApplication.getInstance().isInteral()){
			intent.setClass(context, UserLoginActivity.class);
		}else{
			intent.setClass(context, InternationUserLoginActivity.class);
		}
		context.startActivity(intent);
	}

	public static void startVideoEditActivity(Context context, int type,
			String path) {
		Intent intent = new Intent(context, VideoEditActivity.class);

		int tempType = 2;
		if (type == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
			tempType = 3;
		}

		intent.putExtra("type", tempType);
		intent.putExtra("cn.com.mobnote.video.path", path);
		context.startActivity(intent);
	}

	public static void changePraiseStatus(List<VideoSquareInfo> dataList,
			boolean status, String videoId) {
		if (TextUtils.isEmpty(videoId) || null == dataList
				|| dataList.size() == 0) {
			return;
		}

		for (int i = 0; i < dataList.size(); i++) {
			VideoSquareInfo vs = dataList.get(i);
			if (videoId.equals(vs.mVideoEntity.videoid)) {
				int number = Integer.parseInt(vs.mVideoEntity.praisenumber);
				if (status) {
					number++;
				} else {
					number--;
				}

				vs.mVideoEntity.praisenumber = "" + number;
				vs.mVideoEntity.ispraise = status ? "1" : "0";
				// mNewestAdapter.notifyDataSetChanged();
				break;
			}
		}
	}

	public static boolean isAppInstalled(Context context, String appPackage) {
		final PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals(appPackage)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * ??????assets?????????
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String getDataFromAssets(Context context, String fileName) {
		InputStreamReader inputReader = null;
		try {
			inputReader = new InputStreamReader(context.getAssets().open(
					fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String result = "";
			while ((line = bufReader.readLine()) != null) {
				result += line;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputReader != null) {
				try {
					inputReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		return null;
	}

	/**
	 * ????????????????????????
	 * 
	 * @param size
	 *            ????????????
	 * @return
	 * @author xuhw
	 * @date 2015???4???11???
	 */
	public static String getSize(double size) {
		String result = "";
		double totalsize = 0;

		java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
		if (size >= 1024) {
			totalsize = size / 1024;
			result = df.format(totalsize) + "GB";
		} else {
			totalsize = size;
			result = df.format(totalsize) + "MB";
		}

		return result;
	}

	public static void setTabHostVisibility(boolean visible, Activity activity) {
		if(!isActivityAlive(activity)) {
			return;
		}
		if(activity instanceof MainActivity) {
			MainActivity main = (MainActivity)activity;
			main.setTabHostVisibility(visible);
		}
	}
}
