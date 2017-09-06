package com.rd.veuisdk.net;

import java.io.File;
import java.util.ArrayList;

import android.text.TextUtils;

import com.rd.http.NameValuePair;
import com.rd.net.RdHttpClient;
import com.rd.veuisdk.database.SpecialData;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.CommonStyleUtils.STYPE;

public class SpecialUtils {

	public static final int DEFAULT_ID = "aiwoma".hashCode();

	public StyleInfo getStyleInfo(int key) {
		try {
			StyleInfo info = null;

			if (sArray.size() != 0)
				info = getIndex2(sArray, key);
			if (null == info) {
				if (downloaded.size() != 0) {
					info = getIndex2(downloaded, key);
				}
			}
			if (null != info) {
				return info;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		StyleInfo info = new StyleInfo();
		info.pid = DEFAULT_ID;
		info.st = STYPE.special;
		return CommonStyleUtils.getDefualt(info);
	}

	private StyleInfo getIndex2(ArrayList<StyleInfo> list, int styleId) {
		StyleInfo temp = null, result = null;
		int len = list.size();
		for (int i = 0; i < len; i++) {
			temp = list.get(i);
			if (temp.pid == styleId) {
				result = temp;
				break;
			}

		}

		return result;
	}

	public ArrayList<StyleInfo> getStyleInfos() {
		return sArray;
	}

	public void clearArray() {
		sArray.clear();
	}

	public void putStyleInfo(StyleInfo info) {

		sArray.add(info);

	}

	private static SpecialUtils instance;

	public static SpecialUtils getInstance() {
		if (null == instance) {
			instance = new SpecialUtils();
		}
		return instance;
	}

	/**
	 * Activity onDestory() 释放内存
	 */
	public void recycle() {
		downloaded.clear();
		sArray.clear();
	}

	private SpecialUtils() {

	}

	public String getSpecialJson() {
		try {
			return RdHttpClient.post(URLConstants.STYLEURL, new NameValuePair(
					"os", Integer.toString(2)));
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;
	}

	private static ArrayList<StyleInfo> sArray = new ArrayList<StyleInfo>(),
			downloaded = new ArrayList<StyleInfo>();

	public ArrayList<StyleInfo> getStyleDownloaded() {
		getDownloadData();
		return downloaded;

	}

	private void getDownloadData() {
		downloaded.clear();
		ArrayList<StyleInfo> dblist = SpecialData.getInstance().getAll();
		for (int i = 0; i < dblist.size(); i++) {
			StyleInfo tempInfo = dblist.get(i);
			if (!TextUtils.isEmpty(tempInfo.mlocalpath)) {
				File f = new File(tempInfo.mlocalpath);
				CommonStyleUtils.checkStyle(f, tempInfo);

			}
			tempInfo.st = STYPE.special;
			downloaded.add(tempInfo);
		}
		dblist.clear();

	}

}
