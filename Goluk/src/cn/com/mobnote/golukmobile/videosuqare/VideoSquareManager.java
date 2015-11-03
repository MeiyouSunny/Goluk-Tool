package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 
 * 视频广场接口管理类
 * 
 * 2015年4月17日
 * 
 * @author xuhw
 */
public class VideoSquareManager implements VideoSuqareManagerFn {
	/** Application实例,用于调用JNI的对象 */
	private GolukApplication mApplication = null;
	/** IPC回调监听列表 */
	private ConcurrentHashMap<String, VideoSuqareManagerFn> mVideoSquareManagerListener = null;

	public VideoSquareManager(GolukApplication application) {
		mApplication = application;
		mVideoSquareManagerListener = new ConcurrentHashMap<String, VideoSuqareManagerFn>();
		mApplication.mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_Square, this);
	}

	/**
	 * 获取广场列表数据
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param type
	 *            视频类型：0.全部 1.直播 2.点播
	 * @param attribute
	 *            属性标签：[“1”,”2”,”3”] 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料
	 *            6.堵车预警 7.惊险十分 8.疯狂超车 9.感人瞬间 10.传递正能量
	 * @param operation
	 *            操作：0.首次进入30条 1.下拉30条 2.上拉30条
	 * @param timestamp
	 *            时间戳：首次进入为空
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean getSquareList(String channel, String type, String attribute, String operation, String timestamp) {
		List<String> arr = new ArrayList<String>();
		arr.add(attribute);
		String json = JsonCreateUtils.getSquareListRequestJson(channel, type, arr, operation, timestamp);

		GolukDebugUtils.e("", "jyf----CategoryListView------------------getSquareList  json: " + json);

		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VSquare_Req_List_Video_Catlog, json);
	}

	/**
	 * 获取精选列表数据
	 * 
	 * @param jxid
	 *            　精选id首次进入为0
	 * @param pagesize
	 *            　默认四组
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public long getJXListData(String jxid, String pagesize) {
		String json = JsonCreateUtils.getJXListJson(jxid, pagesize);
		return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square, VSquare_Req_List_HandPick,
				json);
	}

	/**
	 * 获取专题列表数据
	 * 
	 * @param ztid
	 *            　专题id
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public boolean getZTListData(String ztid) {
		String json = JsonCreateUtils.getZTJson(ztid);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VSquare_Req_List_Topic_Content, json);
	}

	/**
	 * 获取聚合内容数据
	 * 
	 * @param ztid
	 *            　专题id
	 * @param operation
	 *            　0.首次进入　1.下拉　2.上拉
	 * @param timestamp
	 *            　时间戳：首次进入为空
	 * @param pagesize
	 *            　默认20个视频
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public boolean getJHListData(String ztid, String operation, String timestamp, String pagesize) {
		String json = JsonCreateUtils.getJHJson(ztid, operation, timestamp, pagesize);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_List_Tag_Content,
				json);
	}

	/**
	 * 获取视频详情数据
	 * 
	 * @param ztid
	 *            　专题id
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public boolean getVideoDetailData(String ztid) {
		String json = JsonCreateUtils.getZTJson(ztid);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_Get_VideoDetail,
				json);
	}

	/**
	 * 获取视频详情数据
	 * 
	 * @param ztid
	 *            　专题id
	 * @return
	 */
	public boolean getVideoDetailListData(String videoid) {
		String json = JsonCreateUtils.getVideoDetailJson(videoid);
		GolukDebugUtils.e("", "================getVideoDetailListData==" + json);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VSquare_Req_Get_VideoDetail_ComentList, json);
	}

	/**
	 * 获取视频详情数据
	 * 
	 * @param ztid
	 *            　专题id
	 * @return
	 */
	public boolean getUserInfo(String otheruid) {
		String json = JsonCreateUtils.getUserInfoJson(otheruid);
		GolukDebugUtils.e("", "=======getUserInfo==" + json);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VSquare_Req_MainPage_UserInfor, json);
	}

	/**
	 * 获取视频分类
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public long getZXListData() {
		return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square, VSquare_Req_List_Catlog, "");
	}

	/**
	 * 按类别获取视频列表（可用于更新）
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param type
	 *            视频类型：0.全部 1.直播 2.点播
	 * @param attribute
	 *            属性标签：[“1”,”2”,”3”] 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料
	 *            6.堵车预警 7.惊险十分 8.疯狂超车 9.感人瞬间 10.传递正能量
	 * @param operation
	 *            操作：0.首次进入30条 1.下拉30条 2.上拉30条
	 * @param timestamp
	 *            时间戳：首次进入为空
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public long getTypeVideoList(String channel, String type, List<String> attribute, String operation,
			String timestamp) {
		String json = JsonCreateUtils.getSquareListRequestJson(channel, type, attribute, operation, timestamp);
		return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square,
				VSquare_Req_List_Video_Catlog, json);
	}

	/**
	 * 获取评论列表（可用于更新）
	 * 
	 * @param topicid
	 *            视频、专题或直播的id
	 * @param topictype
	 *            1:单视频；2:专题；3:直播；4:其它
	 * @param operation
	 *            0:首次进入；1:下拉；2:上拉 首次进入：获取最新数据； - 下拉：获取更新数据，即比timestamp以后的数据； -
	 *            上拉：获取历史数据，即比timestamp以前的数据。
	 * @param timestamp
	 *            首次进入为空。格式：2015-08-01 08:00:00
	 * @param pagesize
	 *            默认20个评论
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public boolean getCommentListData(String topicid, String topictype, String operation, String timestamp,
			String pagesize) {
		String json = JsonCreateUtils.getCommentJson(topicid, topictype, operation, timestamp, pagesize);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_List_Comment,
				json);
	}

	/**
	 * 添加评论
	 * 
	 * @param topicid
	 *            视频、专题、直播等的id(必须)
	 * @param topictype
	 *            1:单视频；2:专题；3:直播；4:其它(必须)
	 * @param text
	 *            　评论内容(必须)
	 * @param replyid
	 *            　回复人id（可选）
	 * @param replyname
	 *            　回复人呢称（可选）
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public boolean addComment(String topicid, String topictype, String text, String replyid, String replyname) {
		String json = JsonCreateUtils.addCommentJson(topicid, topictype, text, replyid, replyname);
		GolukDebugUtils.e("", "VideoSuqare_CallBack=@@@@====json=" + json);
		return mApplication.mGoluk
				.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_Add_Comment, json);
	}

	/**
	 * 删除评论
	 * 
	 * @param id
	 *            评论id
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public boolean deleteComment(String id) {
		String json = JsonCreateUtils.delCommentJson(id);
		return mApplication.mGoluk
				.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_Del_Comment, json);
	}

	/**
	 * 点击次数上报
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param mDataList
	 *            视频列表数据
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean clickNumberUpload(String channel, List<VideoSquareInfo> mDataList) {
		String json = JsonCreateUtils.getClickVideoUploadRequestJson(channel, mDataList);
		GolukDebugUtils.e("", "VideoSuqare_CallBack=@@@@===json=" + json);
		return mApplication.mGoluk
				.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_ClickUp, json);
	}

	/**
	 * 点赞
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @param type
	 *            点赞类型：0.取消点赞 1.点赞
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean clickPraise(String channel, String videoid, String type) {
		String json = JsonCreateUtils.getClickPraiseRequestJson(channel, videoid, type);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_Praise, json);
	}

	/**
	 * 举报
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @param reporttype
	 *            举报类型：1.色情低俗 2.谣言惑众 3.政治敏感 4.其他原因
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean report(String channel, String videoid, String reporttype) {
		String json = JsonCreateUtils.getReportRequestJson(channel, videoid, reporttype);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_ReportUp,
				json);
	}

	/**
	 * 推荐视频
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @param reason
	 *            推荐理由
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public boolean recomVideo(String channel, String videoid, String reason) {
		String json = JsonCreateUtils.getRecomJson(channel, videoid, reason);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_RecomVideo,
				json);
	}

	/**
	 * 分享请求
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean shareVideoUp(String channel, String videoid) {
		String json = JsonCreateUtils.getShareVideoUpRequestJson(channel, videoid);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_ShareVideo,
				json);
	}

	/**
	 * 获取分享地址
	 * 
	 * @param videoid
	 *            视频id
	 * @param type
	 *            视频类型：1.直播 2.点播
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public long getShareUrlEx(String videoid, String type) {
		String json = JsonCreateUtils.getShareUrlRequestJson(videoid, type);
		return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square,
				VSquare_Req_VOP_GetShareURL_Video, json);
	}
	
	/**
	 * 获取分享地址
	 * 
	 * @param videoid
	 *            视频id
	 * @param type
	 *            视频类型：1.直播 2.点播
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean getShareUrl(String videoid, String type) {
		String json = JsonCreateUtils.getShareUrlRequestJson(videoid, type);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VSquare_Req_VOP_GetShareURL_Video, json);
	}

	/**
	 * 获取分享地址(专题和聚合)
	 * 
	 * @param ztype
	 *            专题类型 1:专题 2：tag
	 * @param ztid
	 *            专题id
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public boolean getTagShareUrl(String ztype, String ztid) {
		String json = JsonCreateUtils.getTagJson(ztype, ztid);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VSquare_Req_VOP_GetShareURL_Topic_Tag, json);
	}

	/**
	 * 获取精选本地缓存
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public String getJXList() {
		return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square,
				VSquare_Req_List_HandPick_LocalCache, "");
	}

	/**
	 * 获取最新视频分类缓存（不包含直播信息）
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年8月6日
	 */
	public String getZXList() {
		return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square,
				VSquare_Req_List_Catlog_LocalCache, "");
	}

	// 获取分类列表本地缓存(比如　曝光台，事故　，随手拍)
	public String getCategoryLocalCacheData(String json) {
		return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square,
				VSquare_Req_List_Video_Catlog_LocalCache, json);
	}

	/**
	 * 同步获取视频列表本地缓存
	 * 
	 * @param attribute
	 *            属性标签： 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料 6.堵车预警 7.惊险十分
	 *            8.疯狂超车 9.感人瞬间 10.传递正能量
	 * @return
	 * @author xuhw
	 * @date 2015年4月27日
	 */
	public String getTypeVideoList(String attribute) {
		JSONObject json = new JSONObject();
		try {
			json.put("attribute", attribute);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square,
				VSquare_Req_List_Video_Catlog_LocalCache, json.toString());
	}

	/**
	 * 获取个人或者他人的分享地址
	 * 
	 * @param uid
	 * @return
	 */
	public Boolean getUserCenterShareUrl(String uid) {
		JSONObject json = new JSONObject();
		try {
			json.put("otheruid", uid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_MainPage_Share,
				json.toString());
	}

	/**
	 * 
	 * @param ztype
	 * @param ztid
	 * @return
	 */
	public Long getUserCenter(String otheruid) {
		String json = JsonCreateUtils.getUserCenterJson(otheruid);
		return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square, VSquare_Req_MainPage_Infor, json);
	}

	/**
	 * 
	 * @param ztype
	 * @param ztid
	 * @return
	 */
	public String getUserCenter() {
		return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square, VSquare_Req_MainPage_Infor, null);
	}

	public long getUserCenterShareVideo(String otheruid, String operation, String timestamp) {
		String json = JsonCreateUtils.getUserCenterShareVideoJson(otheruid, operation, timestamp);
		return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square, VSquare_Req_MainPage_List_ShareVideo,
				json);
	}

	/**
	 * 添加视频广场监听
	 * 
	 * @param from
	 * @param fn
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public void addVideoSquareManagerListener(String from, VideoSuqareManagerFn fn) {
		this.mVideoSquareManagerListener.put(from, fn);
	}

	public boolean checkVideoSquareManagerListener(String from) {
		return this.mVideoSquareManagerListener.containsKey(from);
	}

	/**
	 * 删除视频广场监听
	 * 
	 * @param from
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public void removeVideoSquareManagerListener(String from) {
		this.mVideoSquareManagerListener.remove(from);
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "jyf----VideoSquareManager----event:" + event + "  msg:" + msg + "  param1: " + param1
				+ "  param2:" + param2);
		Iterator<String> iter = mVideoSquareManagerListener.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			if (null != key) {
				GolukDebugUtils.e("", "jyf----VideoSquareManager----key:" + key);
				VideoSuqareManagerFn fn = mVideoSquareManagerListener.get(key);
				if (null != fn) {
					fn.VideoSuqare_CallBack(event, msg, param1, param2);
				}
			}
		}

	}

}
