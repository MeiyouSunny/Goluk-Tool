package com.mobnote.videoedit.utils;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class DeviceUtil {
	public static int dp2px(Context context, int dp) {
		return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
	}

	public static int px2dp(Context context, int px) {
		return (int) (px / context.getResources().getDisplayMetrics().density + 0.5f);
	}

	/**
	 * 获取屏幕宽单位是像素
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidthSize(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	/**
	 * 获取屏幕的尺�?
	 * 
	 * @param context
	 * @return
	 */
	public static int[] getScreenSize(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int[] size = { dm.widthPixels, dm.heightPixels };
		return size;
	}

	public static String getVersionName(Context context) {
		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(
					packageName, 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
		}
		return "";
	}

	public static int getVersionCode(Context context) {
		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(
					packageName, 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
		}
		return 0;
	}

	/**
	 * 获取顶部通知栏的高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}

	// 根据手机的五项参数，获取手机的id，用于日志提�?
	public static String getDeviceID(String deviceId, Activity a) {
		if (TextUtils.isEmpty(deviceId)) {

			Context appContext = a;
			// 1. The IMEI: 仅仅只对Android手机有效:
			TelephonyManager TelephonyMgr = (TelephonyManager) appContext
					.getSystemService(appContext.TELEPHONY_SERVICE);
			String szImei = TelephonyMgr.getDeviceId();

			// 2. Pseudo-Unique ID, 这个在任何Android手机中都有效
			String m_szDevIDShort = "35"
					+ // we make this look like a valid IMEI
					Build.BOARD.length() % 10 + Build.BRAND.length() % 10
					+ Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
					+ Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
					+ Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
					+ Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
					+ Build.TAGS.length() % 10 + Build.TYPE.length() % 10
					+ Build.USER.length() % 10;
			// 3. The Android ID , 通常被认为不可信，因为它有时为null�?
			String m_szAndroidID = Secure.getString(
					appContext.getContentResolver(), Secure.ANDROID_ID);
			// 4. The WLAN MAC Address string, 是另�?��唯一ID�?
			WifiManager wm = (WifiManager) appContext.getApplicationContext()
					.getSystemService(appContext.WIFI_SERVICE);
			String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
			// 5. The BT MAC Address string, 只在有蓝牙的设备上运行�?
			BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth
														// adapter
			m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			String m_szBTMAC = m_BluetoothAdapter.getAddress();
			String m_szLongID = szImei + m_szDevIDShort + m_szAndroidID
					+ m_szWLANMAC + m_szBTMAC;
			// compute md5
			MessageDigest m = null;
			try {
				m = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
			// get md5 bytes
			byte p_md5Data[] = m.digest();
			// create a hex string
			String m_szUniqueID = new String();
			for (int i = 0; i < p_md5Data.length; i++) {
				int b = (0xFF & p_md5Data[i]);
				// if it is a single digit, make sure it have 0 in front (proper
				// padding)
				if (b <= 0xF)
					m_szUniqueID += "0";
				// add number to string
				m_szUniqueID += Integer.toHexString(b);
			} // hex string to uppercase
			m_szUniqueID = m_szUniqueID.toUpperCase();
			deviceId = m_szUniqueID;
		}
		return deviceId;
	}
}
