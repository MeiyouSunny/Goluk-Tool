package cn.com.mobnote.module.videosquare;


public class VideoSquareManagerAdapter {
	/** 回调接口实例 */
	private static VideoSuqareManagerFn fn = null;

	/**
	 * 设置接口回调
	 * @param _fn 回调接口
	 * @author xuhw
	 * @date 2015年4月13日
	 */
	public static void setVideoSuqareListener(VideoSuqareManagerFn _fn) {
		fn = _fn;
	}

	public static void VideoSquare_CallBack(int event, int msg, int param1, Object param2) {
//		LogUtil.e("jyf", "jyf-----IPCManage_CallBack--------------IPCManagerAdapter-11---event:" + event + "	param1:"
//				+ param1 + " msg:");
		if (null == fn) {
			return;
		}
		String data = null;
		if (param2 instanceof String) {
			data = (String)param2;
		}
//		LogUtil.e("jyf", "jyf-----IPCManage_CallBack--------------IPCManagerAdapter-22---event:" + event + " msg:" + data);
		fn.VideoSuqare_CallBack(event, msg, param1, param2);
	}
}