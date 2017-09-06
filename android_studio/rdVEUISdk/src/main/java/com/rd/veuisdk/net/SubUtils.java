package com.rd.veuisdk.net;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;

import com.rd.http.NameValuePair;
import com.rd.net.RdHttpClient;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.utils.CommonStyleUtils;

public class SubUtils {

	public static final int DEFAULT_ID = "text_sample".hashCode();

	public StyleInfo getStyleInfo(int key) {
		try {
			if (downloaded.size() != 0) {
				return getIndex2(downloaded, key);
			}
			if (sArray.size() != 0) {
				return getIndex2(sArray, key);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		StyleInfo info = new StyleInfo();
		info.pid = DEFAULT_ID;
		return CommonStyleUtils.getDefualt(info);

	}

	public String getSubJson() {
		try {
			return RdHttpClient.post(URLConstants.GETZIMU, new NameValuePair(
					"os", Integer.toString(2)));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private StyleInfo getIndex2(ArrayList<StyleInfo> list, int styleId) {
		StyleInfo temp = null, result = list.get(0);
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

	public void putStyleInfo(StyleInfo info) {
		sArray.add(info);

	}

	private static SubUtils instance;

	public static SubUtils getInstance() {
		if (null == instance) {
			instance = new SubUtils();
		}
		return instance;
	}

	private SubUtils() {

	}

	public String getStyleList() {
		return RdHttpClient.post(URLConstants.STYLEURL);

	}

	private static ArrayList<StyleInfo> sArray = new ArrayList<StyleInfo>(),
			downloaded = new ArrayList<StyleInfo>();

	public void clearArray() {
		sArray.clear();
	}

	public ArrayList<StyleInfo> getDownLoadedList(Context context) {

		getStyleDownloaded(context);

		return downloaded;
	}

	public int getFileIndex(File[] fs, String targetName) {
		int index = 0;
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].getName().equals(targetName)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public void getStyleDownloaded(Context context) {
		downloaded.clear();

		ArrayList<StyleInfo> dblist = SubData.getInstance().getAll();
		for (int i = 0; i < dblist.size(); i++) {
			StyleInfo tempInfo = dblist.get(i);
			if (!TextUtils.isEmpty(tempInfo.mlocalpath)) {
				File f = new File(tempInfo.mlocalpath);
				CommonStyleUtils.checkStyle(f, tempInfo);
			}
			downloaded.add(tempInfo);
		}
		dblist.clear();

	}

	/**
	 * Activity onDestory() 释放内存
	 */
	public void recycle() {
		downloaded.clear();
		sArray.clear();
	}

}
