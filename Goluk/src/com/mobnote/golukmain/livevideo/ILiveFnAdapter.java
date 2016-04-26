package com.mobnote.golukmain.livevideo;

public interface ILiveFnAdapter {

	public static final int STATE_START_PUSH = 0;
	public static final int STATE_CONN_SERVER = 1;
	public static final int STATE_SENDING = 2;
	public static final int STATE_FAILED = 3;
	public static final int STATE_RETRY = 4;
	/** 直播时间结束 */
	public static final int STATE_TIME_END = 5;
	/** 推流结束 */
	public static final int STATE_PUSH_END = 6;

	/** 直播成功开始 */
	public static final int STATE_SUCCESS = 7;

	/** 直播操作回调 */
	public void Live_CallBack(int state);

}
