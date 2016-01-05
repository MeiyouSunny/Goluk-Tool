package cn.com.mobnote.golukmobile.carrecorder.settings;

import android.content.Context;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;

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
			bitrate = context.getResources().getStringArray(R.array.list_quality_bitrate1);
		} else if (IPCControlManager.G2_SIGN.equals(ipcName)) {
			bitrate = context.getResources().getStringArray(R.array.list_quality_bitrate2);
		} else {
			bitrate = context.getResources().getStringArray(R.array.list_quality_bitrate_t1);
		}
		return bitrate;
	}
}
