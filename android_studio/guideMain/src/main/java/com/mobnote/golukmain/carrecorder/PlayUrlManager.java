package com.mobnote.golukmain.carrecorder;

import com.mobnote.application.GolukApplication;

public class PlayUrlManager {

	/** 视频上传地址, 用于直播 */
	public static final String UPLOAD_VOIDE_PRE = "rtmp://goluk.8686c.com/live/";
	/** 自己看 */
	public static final String DEFAULT_RTSP_URL = "rtsp://192.168.43.234/sub";
	/** 其它人看 */
	public static final String DEFAULT_LIVE_URL = "rtmp://211.103.234.234/live/test100";

	/** G1 及 G2 视频设备地址 */
	private static final String COMM_URL_PRE = "rtsp://";
	private static final String COMM_URL_END = "/sub";

	/** T1设备视频预览地址 */
	private static final String T1_URL_PRE = "rtsp://";
	private static final String T1_URL_END = "/stream1";

	private static String getRtspUrl_G1G2() {
		return COMM_URL_PRE + GolukApplication.mIpcIp + COMM_URL_END;
	}

	private static String getRtspUrl_T1() {
		return T1_URL_PRE + GolukApplication.mIpcIp + T1_URL_END;
	}

	public static String getRtspUrl() {
		if (null == GolukApplication.getInstance().mIPCControlManager) {
			return "";
		}
		String currentProduceName = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		if (IPCControlManager.G1_SIGN.equals(currentProduceName)
				|| IPCControlManager.G2_SIGN.equals(currentProduceName)
				|| IPCControlManager.T1s_SIGN.equals(currentProduceName)
				|| IPCControlManager.T3_SIGN.equals(currentProduceName)) {
			return getRtspUrl_G1G2();
		} else if (IPCControlManager.T1_SIGN.equals(currentProduceName)
				|| IPCControlManager.T2_SIGN.equals(currentProduceName)) {
			return getRtspUrl_T1();
		} else {
			return "";
		}
	}

}
