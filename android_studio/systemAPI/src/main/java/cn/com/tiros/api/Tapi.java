package cn.com.tiros.api;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;
import java.util.Vector;

public class Tapi {

	/** 默认的mobileid */
	public static final String DEFAULT_MOBILEID = "123456789123456";
	/** 保存deviceId的本地路径 */
	private static final String LIB_PATH_MOBILEID = "fs0:/mobileid.txt";
	/** 保存deviceId的本地路径 */
	private static String mobileFilePath = null;
	/** 获取当前用户的使用mobile类型 */
	private int mType;
	private static TelephonyManager mTelephonyManager;
	private static WifiManager wifiManager;
	/** 保存基站信息 */
	private Vector<BaseStationInfo> basestationinfo = null;

	public void sys_tapicreate() {
		// mTelephonyManager = (TelephonyManager)
		// Const.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		initTeleManager();
		wifiManager = (WifiManager) Const.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		mType = mTelephonyManager.getPhoneType();
		if (null == mobileFilePath) {
			mobileFilePath = FileUtils.libToJavaPath(LIB_PATH_MOBILEID);
		}
	}

	private static void initTeleManager() {
		if (null == mTelephonyManager) {
			mTelephonyManager = (TelephonyManager) Const.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		}
	}

	public void sys_tapidestroy() {
		mTelephonyManager = null;
		basestationinfo = null;
	}

//	public int sys_tapigetbscount() {
//
//		if (basestationinfo != null) {
//			basestationinfo.clear();
//			basestationinfo = null;
//		}
//
//		initTeleManager();
//
//		if (mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT
//				|| mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_UNKNOWN) {
//			return 0;
//		}
//
//		basestationinfo = new Vector<BaseStationInfo>();
//
//		if (mType == TelephonyManager.PHONE_TYPE_CDMA) { // C网定位
//			android.telephony.cdma.CdmaCellLocation cdmacell = (android.telephony.cdma.CdmaCellLocation) mTelephonyManager
//					.getCellLocation();
//			if (cdmacell != null) {
//				BaseStationInfo stationInfo = new BaseStationInfo();
//				stationInfo.mLac = cdmacell.getSystemId();
//				stationInfo.mCellId = cdmacell.getBaseStationId();
//				int baselon = cdmacell.getBaseStationLongitude();
//				int baselat = cdmacell.getBaseStationLatitude();
//				if (baselon == Integer.MAX_VALUE || baselat == Integer.MAX_VALUE) {
//					return 0;
//				}
//
//				// 单位：度 * 1000000
//				stationInfo.mLon = baselon / 14400D * 1000000;
//				stationInfo.mLat = baselat / 14400D * 1000000;
//
//				// 判断获取到的经纬度是否在国界内
//				if (stationInfo.mLon < 71000000D || stationInfo.mLon > 136000000D || stationInfo.mLat < 16000000D
//						|| stationInfo.mLat > 57000000D) {
//					return 0;
//				}
//
//				String MCC_MNC = mTelephonyManager.getSubscriberId();
//
//				if (MCC_MNC != null && MCC_MNC.length() > 0) {
//					stationInfo.mMcc = Integer.parseInt(MCC_MNC.substring(0, 3));
//					stationInfo.mMnc = Integer.parseInt(MCC_MNC.substring(3, 5));
//				}
//				stationInfo.mSignalstrength = 30;
//				basestationinfo.add(stationInfo);
//
//				stationInfo = null;
//			}
//			return basestationinfo.size();
//
//		} else { // G网定位
//			// 主基站
//			GsmCellLocation gsmcell = (GsmCellLocation) mTelephonyManager.getCellLocation();
//			if (gsmcell != null) {
//				BaseStationInfo stationInfo = new BaseStationInfo();
//				stationInfo.mLac = gsmcell.getLac();
//				stationInfo.mCellId = gsmcell.getCid();
//				stationInfo.mSignalstrength = 33;
//				String MCC_MNC = mTelephonyManager.getSubscriberId();
//				if (MCC_MNC != null && MCC_MNC.length() > 0) {
//					stationInfo.mMcc = Integer.parseInt(MCC_MNC.substring(0, 3));
//					stationInfo.mMnc = Integer.parseInt(MCC_MNC.substring(3, 5));
//				}
//				basestationinfo.add(stationInfo);
//			}
//			// 相邻基站
//			List<NeighboringCellInfo> cells = mTelephonyManager.getNeighboringCellInfo();
//			if (cells != null && cells.size() != 0) {
//				int size = cells.size();
//				for (int i = 0; i < cells.size(); i++) {
//					BaseStationInfo stationInfo = new BaseStationInfo();
//					stationInfo.mLac = cells.get(i).getLac();
//					stationInfo.mCellId = cells.get(i).getCid();
//					stationInfo.mSignalstrength = (short) cells.get(i).getRssi();
//					String MCC_MNC = mTelephonyManager.getSubscriberId();
//
//					if (MCC_MNC != null && MCC_MNC.length() > 0) {
//						stationInfo.mMcc = Integer.parseInt(MCC_MNC.substring(0, 3));
//						stationInfo.mMnc = Integer.parseInt(MCC_MNC.substring(3, 5));
//					}
//					if (stationInfo.mCellId > 0) {
//						basestationinfo.add(stationInfo);
//					} else {
//						size = size - 1;
//					}
//
//					stationInfo = null;
//				}
//			}
//			return basestationinfo.size();
//		}
//	}

	public BaseStationInfo sys_tapigetbsbyindex(int aIndex) {
		if (basestationinfo == null || aIndex < 0 || aIndex >= basestationinfo.size()) {
			return null;
		}
		return basestationinfo.elementAt(aIndex);
	}

	public WIFIInfo sys_tapigetconnwifiinfo() {
		if (wifiManager == null) {
			return null;
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null) {
			return null;
		}
		if (wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
			WIFIInfo info = new WIFIInfo();
			info.mName = wifiInfo.getSSID();
			info.mMac = wifiInfo.getBSSID().replaceAll(":", "");
			info.mIp = "" + wifiInfo.getIpAddress();
			info.mSignalstrength = (short) wifiInfo.getRssi();
			return info;
		}
		return null;
	}

	public String sys_tapigetmobileid() {
		return getMobileId();
	}

	public static String getDeviceId() {
		initTeleManager();
		return getMobileId();
	}

	/**
	 * 获取mobileId
	 *
	 * @return
	 * @author jiayf
	 * @date Jan 27, 2015
	 */
	public static String getMobileId() {
		return Build.BRAND + "-" + Build.ID;

	}

	/**
	 * 获取本地文件保存的device ID
	 *
	 * @return device ID
	 * @author jiayf
	 * @date Mar 23, 2015
	 */
	private static String getLocalFileDeviceId() {
		String mobileId = null;
		FileInputStream fis = null;
		try {
			if (null == mobileFilePath) {
				mobileFilePath = FileUtils.libToJavaPath(LIB_PATH_MOBILEID);
			}
			File file = new File(mobileFilePath);
			if (!file.exists()) {
				return mobileId;
			}
			fis = new FileInputStream(file);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			mobileId = new String(b);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fis) {
					fis.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return mobileId;
	}

	/**
	 * 保存Device ID到本地文件
	 *
	 * @param mobileId
	 * @author jiayf
	 * @date Mar 23, 2015
	 */
	private static void saveLocalFileDeviceId(String mobileId) {
		FileOutputStream out = null;
		try {
			if (null == mobileFilePath) {
				mobileFilePath = FileUtils.libToJavaPath(LIB_PATH_MOBILEID);
			}
			File file = new File(mobileFilePath);
			if (file.exists()) {
				file.delete();
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			out.write(mobileId.getBytes());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != out) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取wifi Mac地址
	 *
	 * @return
	 * @author jiayf
	 * @date Mar 23, 2015
	 */
	private static String getWifiMac() {
		if (null == wifiManager) {
			return null;
		}
		WifiInfo info = wifiManager.getConnectionInfo();
		if (null == info) {
			return null;
		}

		return info.getMacAddress();
	}

	/**
	 * 产生Android id
	 *
	 * @return
	 * @author jiayf
	 * @date Jan 27, 2015
	 */
	private static String getAndroidId() {
		return android.provider.Settings.Secure.getString(Const.getAppContext().getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
	}

	/**
	 * 产生一个UUID来充当Device ID
	 *
	 * @return
	 * @author jiayf
	 * @date Mar 23, 2015
	 */
	private synchronized static String getDeviceidFromUUID() {
		String sID = null;
		try {
			File installation = new File(Const.getAppContext().getFilesDir(), "INSTALLATION");
			if (!installation.exists()) {
				writeInstallationFile(installation);
			}
			sID = readInstallationFile(installation);
		} catch (Exception e) {
			e.printStackTrace();
			return sID;
		}
		return sID;
	}

	private static String readInstallationFile(File installation) throws IOException {
		RandomAccessFile f = new RandomAccessFile(installation, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

	private static void writeInstallationFile(File installation) throws IOException {
		FileOutputStream out = new FileOutputStream(installation);
		String id = UUID.randomUUID().toString();
		out.write(id.getBytes());
		out.close();
	}

	/**
	 * @brief 获取设备卡的IMSI
	 * @return - 实际获取的IMSI
	 */
	public String sys_tapigetimsi() {
		return mTelephonyManager.getSubscriberId();
	}

	/**
	 * @brief 获取当前网络的联网类别
	 * @return - 实时获取当前网络的联网类别：0:普通网络(默认及获取不到具体类型) 1:wifi 2:gsm 3:cdma 4:tdcdma
	 *         5:cdma2000 6:wcdma。。。
	 */
//	public int sys_tapigetnettype() {
//
//		ConnectivityManager manager = (ConnectivityManager) Const.getAppContext().getSystemService(
//				Context.CONNECTIVITY_SERVICE);
//
//		if (manager == null) {
//			return -1;
//		}
//
//		NetworkInfo netWorkInfo = manager.getActiveNetworkInfo();
//
//		if (netWorkInfo == null || !netWorkInfo.isConnected()) {
//			return -1;
//		}
//
//		int type = netWorkInfo.getType();
//		if (type == ConnectivityManager.TYPE_WIFI) {
//			return 1;
//		} else if (type == ConnectivityManager.TYPE_MOBILE) {
//			int phoneType = mTelephonyManager.getNetworkType();
//			if (phoneType == TelephonyManager.NETWORK_TYPE_CDMA) {
//				return 3;
//			} else if (phoneType == TelephonyManager.NETWORK_TYPE_EDGE) {
//				return 2;
//			}
//		}
//		return 0;
//	}

	public String sys_tapigetosversion() {
		return android.os.Build.VERSION.RELEASE;
	}

	public String sys_tapigetdevicemodel() {
		return android.os.Build.MODEL;
	}

	public String sys_tapigetmanufacturername() {
		String manufacturer = android.os.Build.MANUFACTURER;
		String product = android.os.Build.PRODUCT;
		// String device = android.os.Build.DEVICE;
		if (null != manufacturer) {
			return manufacturer;
		}
		if (null != product) {
			return product;
		}
		return "";
	}

	public String tapigetlbskey() {
		return null;
	}
}
