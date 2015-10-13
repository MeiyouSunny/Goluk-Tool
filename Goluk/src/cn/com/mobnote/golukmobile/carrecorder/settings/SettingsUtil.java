package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.content.Context;
import cn.com.mobnote.golukmobile.R;

public class SettingsUtil {

	/**
	 * 视频分辨率
	 * 
	 * @param context
	 * @param ipcName
	 * @return
	 */
	public static String[] returnResolution(Context context, String ipcName) {
		String[] resolution = null;
		if ("G1".equals(ipcName)) {
			resolution = context.getResources().getStringArray(R.array.list_quality_resolution1);
		} else {
			resolution = context.getResources().getStringArray(R.array.list_quality_resolution2);
		}
		return resolution;
	}

	/**
	 * 
	 * 视频质量码率
	 * 
	 * @param context
	 * @param ipcName
	 * @return
	 */
	public static String[] returnBitrate(Context context, String ipcName) {
		String[] bitrate = null;
		if ("G1".equals(ipcName)) {
			bitrate = context.getResources().getStringArray(R.array.list_quality_bitrate1);
		} else {
			bitrate = context.getResources().getStringArray(R.array.list_quality_bitrate2);
		}
		return bitrate;
	}
}
