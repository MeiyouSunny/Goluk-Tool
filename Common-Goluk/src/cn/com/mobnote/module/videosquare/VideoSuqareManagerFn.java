package cn.com.mobnote.module.videosquare;

import cn.com.mobnote.logic.IGolukCommFn;

public interface VideoSuqareManagerFn extends IGolukCommFn{
	public static final int RESULE_SUCESS = 1;
	/** 获取广场列表 */
	public static final int SquareCmd_Req_SquareList = 0;
	/** 获取热门列表 */
	public static final int SquareCmd_Req_HotList = 256;

	
	
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2);

}
