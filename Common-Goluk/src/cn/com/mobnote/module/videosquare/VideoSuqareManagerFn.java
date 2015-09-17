package cn.com.mobnote.module.videosquare;

import cn.com.mobnote.logic.IGolukCommFn;

public interface VideoSuqareManagerFn extends IGolukCommFn {
	public static final int RESULE_SUCESS = 1;
	/** 获取热门列表 */
	public static final int SquareCmd_Req_HotList = 256;
	/** 获取分享地址 */
	public static final int SquareCmd_Req_GetShareUrl = 1537;

	/** 获取本地广场视频列表缓存 */
	public static final int SquareCmd_Get_SquareCache = 0;
	/** 获取本地热门视频列表缓存 */
	public static final int SquareCmd_Get_HotCache = 256;

	/** 获取精选列表 */
	public static final int VSquare_Req_List_HandPick = 0;
	/** 获取专题内容列表 */
	public static final int VSquare_Req_List_Topic_Content = 1;
	/** 获取聚合内容 */
	public static final int VSquare_Req_List_Tag_Content = 2;
	/** 获取视频详情 */
	public static final int VSquare_Req_Get_VideoDetail = 3;

	/** 获取视频分类 */
	public static final int VSquare_Req_List_Catlog = 256;
	/** 按类别获取视频列表（可用于更新） */
	public static final int VSquare_Req_List_Video_Catlog = 257;

	/** 获取评论列表（可用于更新） */
	public static final int VSquare_Req_List_Comment = 512;
	/** 添加评论 */
	public static final int VSquare_Req_Add_Comment = 513;
	/** 删除评论 */
	public static final int VSquare_Req_Del_Comment = 514;

	/** 点击次数上报 */
	public static final int VSquare_Req_VOP_ClickUp = 768;
	/** 点赞 */
	public static final int VSquare_Req_VOP_Praise = 769;
	/** 举报 */
	public static final int VSquare_Req_VOP_ReportUp = 770;
	/** 推荐视频 */
	public static final int VSquare_Req_VOP_RecomVideo = 771;
	/** 视频分享 */
	public static final int VSquare_Req_VOP_ShareVideo = 772;
	/** 获取分享地址(视频) */
	public static final int VSquare_Req_VOP_GetShareURL_Video = 773;
	/** 获取分享地址(专题和聚合) */
	public static final int VSquare_Req_VOP_GetShareURL_Topic_Tag = 774;
	/**获取个人中心数据集合*/
	public static final int VSquare_Req_MainPage_Infor = 1028;

	/**
	 * 获取本地数据
	 * */

	/** 获取精选本地缓存 */
	public static final int VSquare_Req_List_HandPick_LocalCache = 0;

	/** 获取最新视频分类缓存（不包含直播信息） */
	public static final int VSquare_Req_List_Catlog_LocalCache = 256;
	/** 获取视频列表本地缓存 */
	public static final int VSquare_Req_List_Video_Catlog_LocalCache = 257;

	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2);

}
