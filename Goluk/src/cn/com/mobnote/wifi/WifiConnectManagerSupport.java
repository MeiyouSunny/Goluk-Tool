package cn.com.mobnote.wifi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import cn.com.mobnote.util.ThreeDES;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Environment;
import android.text.TextUtils;

public class WifiConnectManagerSupport {

	private WifiManager wifiManager = null;
	private static final String REGULAREXPRESSION = ";"; // 文件读取分隔符

	private static final String FILEPATH = Environment
			.getExternalStorageDirectory().getPath() + "/wificonfig/"; // 配置文件存储路径

	private static final int BUF_SIZE = 1024;

	public WifiConnectManagerSupport(WifiManager _wifiManager) {
		this.wifiManager = _wifiManager;
	}

	/**
	 * 写文件
	 * 
	 * @param fileName
	 * @param ssid
	 * @param passWord
	 * @return
	 */
	boolean writePassFile(String fileName, String ssid, String passWord)
			throws Exception {
		String tempPath = FILEPATH + fileName;

		File dir = new File(FILEPATH);
		// 先檢查該目錄是否存在
		if (!dir.exists()) {
			// 若不存在則建立它
			dir.mkdir();
		}
		byte[] srcByte = (ssid + REGULAREXPRESSION + passWord).getBytes();
		byte[] writeBytes = ThreeDES.encryptMode(srcByte);

		FileOutputStream out = null;

		try {
			out = new FileOutputStream(new File(tempPath));
			out.write(writeBytes);
			return true;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * 从文件中读取
	 * 
	 * @param fileName
	 * @return
	 */
	public String[] readPassFile(String fileName) throws Exception {
		String tempPath = FILEPATH + fileName;

		File file = null;
		byte[] types = null;
		BufferedInputStream in = null;
		ByteArrayOutputStream bos = null;
		try {
			file = new File(tempPath);
			//如果文件不存在返回null
			if (!file.exists()) {
				return null;
			}
			bos = new ByteArrayOutputStream((int) file.length());

			in = new BufferedInputStream(new FileInputStream(file));

			byte[] buffer = new byte[BUF_SIZE];
			int len = 0;
			while (-1 != (len = in.read(buffer, 0, BUF_SIZE))) {
				bos.write(buffer, 0, len);
			}
			types = bos.toByteArray();
			byte[] rs = ThreeDES.decryptMode(types);
			if (rs != null) {
				String ssid_pass = new String(rs);
				return ssid_pass.split(REGULAREXPRESSION);
			}
			return null;

		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (bos != null) {
				try {
					bos.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			file = null;
		}
	}

	// 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

	// 查看以前是否也配置过这个网络
	WifiConfiguration isExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager
				.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	WifiConfiguration createWifiInfo(String SSID, String Password,
			WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		// nopass
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		// wep
		if (Type == WifiCipherType.WIFICIPHER_WEP) {
			if (!TextUtils.isEmpty(Password)) {
				if (isHexWepKey(Password)) {
					config.wepKeys[0] = Password;
				}
				else {
					config.wepKeys[0] = "\"" + Password + "\"";
				}
			}
			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		// wpa
		if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			// 此处需要修改否则不能自动重联
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	// 打开wifi功能
	boolean openWifi() {
		boolean bRet = true;
		if (!wifiManager.isWifiEnabled()) {
			bRet = wifiManager.setWifiEnabled(true);
		}
		return bRet;
	}

	// 关闭wifi功能
	boolean closeWifi() {
		boolean bRet = true;
		if (wifiManager.isWifiEnabled()) {
			bRet = wifiManager.setWifiEnabled(false);
		}
		return bRet;
	}

	private static boolean isHexWepKey(String wepKey) {
		final int len = wepKey.length();

		// WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
		if (len != 10 && len != 26 && len != 58) {
			return false;
		}

		return isHex(wepKey);
	}

	private static boolean isHex(String key) {
		for (int i = key.length() - 1; i >= 0; i--) {
			final char c = key.charAt(i);
			if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
					&& c <= 'f')) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 得到扫描结果
	 */
	public WifiRsBean[] getScanResult(String title) {
		WifiRsBean bean = null;
		WifiRsBean[] rs = null;
		Collection<WifiRsBean> listRs = new ArrayList<WifiRsBean>();
		String regEx = "^" + title;
		WifiInfo info = wifiManager.getConnectionInfo();
		String conSSid = info.getSSID();
		// 开始扫描网络

		List<ScanResult> scanResult = wifiManager.getScanResults();

		if (scanResult != null) {
			for (ScanResult tempResult : scanResult) {

				boolean result = Pattern.compile(regEx)
						.matcher(tempResult.SSID).find();
				if (result) {
					bean = new WifiRsBean();
					bean.setWifiName(tempResult.SSID);
					//bean.setPwdStatus(tempResult.)
					bean.setWifiSignal(WifiManager.calculateSignalLevel(
							tempResult.level, 4));
					if (conSSid != null && !"".equals(conSSid)) {
						if (("\"" + bean.getWifiName() + "\"").equals(conSSid)) {
							bean.setConn(true);
						}
					}
					listRs.add(bean);
				}
			}

		}
		if (listRs.size() > 0) {
			rs = (WifiRsBean[]) listRs.toArray(new WifiRsBean[0]);
			listRs = null;
		}

		return rs;
	}

	/**
	 * 判断是否在列表中
	 * 
	 * @param ssid
	 * @param beans
	 * @return
	 */
	public boolean inWifiGroup(String ssid, WifiRsBean[] beans) {
		if (beans == null) {
			return false;
		}
		else {
			for (WifiRsBean temp : beans) {
				if (temp.getWifiName().equals(ssid))
					;
				return true;
			}
		}
		return false;
	}
}
