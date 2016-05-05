package com.mobnote.golukmain.carrecorder.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONObject;

import com.mobnote.wifibind.ThreeDES;
import com.mobnote.wifibind.WifiRsBean;

import cn.com.tiros.api.Const;

public class ReadWifiConfig {
	
	private static final String FILEPATH = Const.getAppContext().getFilesDir().getPath() + "/wificonfig/";

	/**
	 * 从文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static WifiRsBean readConfig() {
		String configString;
		try {
			configString = readPassFile("wifi.config");
			if (configString == null) {
				return null;
			}
			JSONObject config = new JSONObject(configString);
			WifiRsBean rs = new WifiRsBean();
			rs.setIpc_ssid(config.getString("ipc_ssid"));
			rs.setIpc_ip(config.getString("ipc_ip"));
			rs.setIpc_mac(config.getString("ipc_mac"));
			rs.setPh_ssid(config.getString("ph_ssid"));
			rs.setPh_pass(config.getString("ph_pass"));
			rs.setIpc_pass(config.getString("ipc_pass"));
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	/**
	 * 从文件中读取
	 * 
	 * @param fileName
	 * @return
	 */
	private static String readPassFile(String fileName) throws Exception {

		String tempPath = FILEPATH + fileName;
		File file = null;
		byte[] types = null;
		BufferedInputStream in = null;
		ByteArrayOutputStream bos = null;
		try {

			file = new File(tempPath);
			if (!file.exists()) {
				return null;
			}

			bos = new ByteArrayOutputStream((int) file.length());

			in = new BufferedInputStream(new FileInputStream(file));

			byte[] buffer = new byte[1024];
			int len = 0;
			while (-1 != (len = in.read(buffer, 0, 1024))) {
				bos.write(buffer, 0, len);
			}
			types = bos.toByteArray();
			byte[] rs = ThreeDES.decryptMode(types);
			if (rs != null) {
				String ssid_pass = new String(rs);
				return ssid_pass;
			}
			return null;

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			file = null;
		}
	}

}
