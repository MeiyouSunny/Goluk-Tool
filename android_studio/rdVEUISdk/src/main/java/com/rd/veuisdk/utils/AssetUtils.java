package com.rd.veuisdk.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.content.res.AssetManager;

public class AssetUtils {

	/**
	 * 获取Asset文件的文本内容
	 * 
	 * @param am
	 * @param strAssetFile
	 * @return
	 */
	public static String getAssetText(AssetManager am, String strAssetFile) {
		try {
			InputStreamReader inputReader = new InputStreamReader(
					am.open(strAssetFile));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			StringBuffer sb = new StringBuffer();
			while ((line = bufReader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}
}
