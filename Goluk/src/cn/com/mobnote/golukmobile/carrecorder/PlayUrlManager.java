package cn.com.mobnote.golukmobile.carrecorder;

import cn.com.mobnote.application.GolukApplication;

public class PlayUrlManager {

	private static final String COMM_URL_PRE = "rtsp://admin:123456@";
	private static final String COMM_URL_END = "/sub";

	private static final String T1_URL_PRE = "rtsp://";
	private static final String T1_URL_END = "/stream1";

	public static String getRtspUrl_G1G2() {
		return COMM_URL_PRE + GolukApplication.mIpcIp + COMM_URL_END;
	}

	public static String getRtspUrl_T1() {
		return T1_URL_PRE + GolukApplication.mIpcIp + T1_URL_END;
	}

}
