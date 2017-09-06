package com.rd.veuisdk.utils;

import android.content.res.Resources;
import android.media.MediaPlayer;

import com.rd.http.NameValuePair;
import com.rd.lib.utils.CoreUtils;
import com.rd.net.RdHttpClient;
import com.rd.veuisdk.model.MusicItem;
import com.rd.veuisdk.net.URLConstants;

import java.io.File;
import java.io.IOException;

public class MusicUtils {
	private MusicUtils() {

	}

	/**
	 * 获取音乐下载次数
	 * 
	 * @param mids
	 */
	public static String musicdowncount(String mids) {

		return RdHttpClient.post(URLConstants.MUSICDOWNCOUNT,
				new NameValuePair("product", String.valueOf(1)),
				new NameValuePair("idstr", mids));

	}

	public static MusicItem addAssetMusic(Resources res, String strItemName,
			final String strCaption, String strAssetName) {
		String stAssName = strAssetName;
		if (strAssetName.contains("/")) {
			stAssName = strAssetName.substring(
					strAssetName.lastIndexOf("/") + 1, strAssetName.length());
		}

		String path = PathUtils.getAssetFileNameForSdcard(strItemName,
				stAssName);

		try {
			File f = new File(path);
			if (f.exists()) {
				long lAssetFileLength = CoreUtils.getAssetResourceLen(
						res.getAssets(), strAssetName);
				if (lAssetFileLength != f.length()) {
					CoreUtils.assetRes2File(res.getAssets(), strAssetName,
							f.getAbsolutePath());
				}
			} else {
				CoreUtils.assetRes2File(res.getAssets(), strAssetName,
						f.getAbsolutePath());
			}
			if (f.exists()) {
				// Log.e("exit",
				// strCaption + "---" + strAssetName + "--"
				// + f.getAbsolutePath());
				MusicItem musicItem = new MusicItem();
				musicItem.setAssetsName(strAssetName);
				musicItem
						.setDuration(getMusicItemDuration(f.getAbsolutePath()));
				if (musicItem.getDuration() < 0) {
					musicItem.setDuration(0);
				}
				musicItem.setPath(f.getAbsolutePath());
				musicItem.setTitle(strCaption);
				return musicItem;
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static int getMusicItemDuration(String path) {
		MediaPlayer mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(path);
			mPlayer.prepare();
			return mPlayer.getDuration();
		} catch (Exception e) {
			return 0;
		} finally {
			mPlayer.release();
			mPlayer = null;
		}
	}


}
