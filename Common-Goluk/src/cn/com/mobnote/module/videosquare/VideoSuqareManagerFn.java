package cn.com.mobnote.module.videosquare;

import cn.com.mobnote.logic.IGolukCommFn;

public interface VideoSuqareManagerFn extends IGolukCommFn{
	public static final int RESULE_SUCESS = 1;
	/** 获取广场列表 */
	public static final int SquareCmd_Req_SquareList = 0;
	/** 获取热门列表 */
	public static final int SquareCmd_Req_HotList = 256;
	/** 点击次数上报 */
	public static final int SquareCmd_Req_ClickUp = 512;
	/** 点赞 */
	public static final int SquareCmd_Req_Praise = 768;
	/** 举报 */
	public static final int SquareCmd_Req_ReportUp = 1024;
	/** 分享请求 */
	public static final int SquareCmd_Req_ShareVideo = 1536;
	/** 获取分享地址 */
	public static final int SquareCmd_Req_GetShareUrl = 1537;
	
	/** 获取本地广场视频列表缓存 */
    public static final int SquareCmd_Get_SquareCache = 0;
    /** 获取本地热门视频列表缓存 */
    public static final int SquareCmd_Get_HotCache = 256;
	
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2);

}
