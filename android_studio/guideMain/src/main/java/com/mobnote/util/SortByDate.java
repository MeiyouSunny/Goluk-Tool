package com.mobnote.util;

import android.text.TextUtils;

import com.mobnote.t1sp.util.FileUtil;

import java.util.Comparator;

public class SortByDate implements Comparator<String> {
//	GolukVideoInfoDbManager mGolukVideoInfoDbManager = GolukVideoInfoDbManager.getInstance();
	@Override
	public int compare(String s1, String s2) {
		// TODO Auto-generated method stub
		String date1 = "";
		String date2 = "";
//		if (mGolukVideoInfoDbManager != null) {
//			VideoFileInfoBean videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(s1);
//			if (videoFileInfoBean != null) {
//				date1 = videoFileInfoBean.timestamp;
//			}
//			videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(s2);
//			if (videoFileInfoBean != null) {
//				date2 = videoFileInfoBean.timestamp;
//			}
//		}

		if (TextUtils.isEmpty(date1)) {
			date1 = getDateFromName(s1);
		}
		if (TextUtils.isEmpty(date2)) {
			date2 = getDateFromName(s2);
		}
		return (date2.compareTo(date1));
	}

	private String getDateFromName(String fileName) {
		String date = "";
		if (fileName.contains("_")) {
			// 传统视频文件,如 WND3_171101112822_0030.mp4
			String[] videos = fileName.split("_");
			if (videos.length == 3) {
				date = videos[1];
				date = "20" + date;
			} else if (videos.length == 7) {
				date = videos[2];
			} else if (videos.length == 8) {
				date = videos[1];
			}
		} else if (fileName.contains("-")) {
			// T1SP视频文件, 如 SHARE171109-173846F.MP4
			int startIndex = 0;
			if (fileName.contains(FileUtil.WONDERFUL_VIDEO_PREFIX)) {
				startIndex = FileUtil.WONDERFUL_VIDEO_PREFIX.length();
			} else if (fileName.contains(FileUtil.URGENT_VIDEO_PREFIX)) {
				startIndex = FileUtil.URGENT_VIDEO_PREFIX.length();
			} else if (fileName.contains(FileUtil.LOOP_VIDEO_PREFIX)) {
				startIndex = FileUtil.LOOP_VIDEO_PREFIX.length();
			}
			date = fileName.substring(startIndex, fileName.indexOf("F."));
			date = "20" + date;
			date = date.replace("-", "");
		}

		return date;
	}

}
