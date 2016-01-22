package cn.com.mobnote.util;

import java.util.Comparator;

import android.text.TextUtils;
import cn.com.mobnote.golukmobile.fileinfo.GolukVideoInfoDbManager;
import cn.com.mobnote.golukmobile.fileinfo.VideoFileInfoBean;

public class SortByDate implements Comparator<String> {
	GolukVideoInfoDbManager mGolukVideoInfoDbManager = GolukVideoInfoDbManager.getInstance();
	@Override
	public int compare(String s1, String s2) {
		// TODO Auto-generated method stub
		String date1 = "";
		String date2 = "";
		if (mGolukVideoInfoDbManager != null) {
			VideoFileInfoBean videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(s1);
			if (videoFileInfoBean != null) {
				date1 = videoFileInfoBean.timestamp;
			}
			videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(s2);
			if (videoFileInfoBean != null) {
				date2 = videoFileInfoBean.timestamp;
			}
		}
		
		if (TextUtils.isEmpty(date1)) {
			date1 = getDateFromName(s1);
		}
		if (TextUtils.isEmpty(date2)) {
			date2 = getDateFromName(s2);
		}
		return (date2.compareTo(date1));
	}

	private String getDateFromName(String s) {
		String[] videos = s.split("_");
		String date = "";
		if (videos.length == 3) {
			date = videos[1];
			date = "20" + date;
		} else if (videos.length == 7) {
			date = videos[2];
		} else if (videos.length == 8) {
			date = videos[1];
		}
		return  date;
	}
}
