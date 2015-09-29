package cn.com.mobnote.module.videosquare;

public class VideoSquareManagerAdapter {
	/** 回调接口实例 */
	private static VideoSuqareManagerFn fn = null;

	/**
	 * 设置接口回调
	 * 
	 * @param _fn
	 *            回调接口
	 * @author xuhw
	 * @date 2015年4月13日
	 */
	public static void setVideoSuqareListener(VideoSuqareManagerFn _fn) {
		fn = _fn;
	}

	public static void VideoSquare_CallBack(int event, int msg, long param1, Object param2) {
		if (null == fn) {
			return;
		}
		fn.VideoSuqare_CallBack(event, msg, (int) param1, param2);
	}
}