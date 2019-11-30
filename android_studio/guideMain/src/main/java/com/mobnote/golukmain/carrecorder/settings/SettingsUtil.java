package com.mobnote.golukmain.carrecorder.settings;

import android.content.Context;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;

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
		if (GolukApplication.getInstance().mIPCControlManager.isG1Relative()) {
			resolution = context.getResources().getStringArray(R.array.list_quality_resolution1);
		} else if (IPCControlManager.G2_SIGN.equals(ipcName)) {
			resolution = context.getResources().getStringArray(R.array.list_quality_resolution2);
		} else {
			resolution = context.getResources().getStringArray(R.array.list_quality_resolution_t1);
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
		if (GolukApplication.getInstance().mIPCControlManager.isG1Relative()) {
			bitrate = context.getResources().getStringArray(R.array.list_quality_bitrate_g2_t3);
		} else if (IPCControlManager.G2_SIGN.equals(ipcName)
				|| IPCControlManager.T3_SIGN.equals(ipcName)
				|| IPCControlManager.T3U_SIGN.equals(ipcName)) {
			bitrate = context.getResources().getStringArray(R.array.list_quality_bitrate_g2_t3);
		} else {
			bitrate = context.getResources().getStringArray(R.array.list_quality_bitrate_t1_t2);
		}
		return bitrate;
	}
}
