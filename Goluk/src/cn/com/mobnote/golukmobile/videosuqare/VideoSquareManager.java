package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;

/**
 * 1.编辑器必须显示空白处
 * 
 * 2.所有代码必须使用TAB键缩进
 * 
 * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
 * 
 * 4.注释必须在行首写.(枚举除外)
 * 
 * 5.函数使用块注释,代码逻辑使用行注释
 * 
 * 6.文件头部必须写功能说明
 * 
 * 7.所有代码文件头部必须包含规则说明
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
	private HashMap<String, VideoSuqareManagerFn> mVideoSquareManagerListener = null;

	public VideoSquareManager(GolukApplication application) {
		mApplication = application;
		mVideoSquareManagerListener = new HashMap<String, VideoSuqareManagerFn>();
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
	public boolean getSquareList(String channel, String type, List<String> attribute, String operation, String timestamp) {
		String json = JsonCreateUtils.getSquareListRequestJson(channel, type, attribute, operation, timestamp);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_SquareList,
				json);
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
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_SquareList,
				json);
	}

	/**
	 * 获取热门列表
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param operation
	 *            操作：0.首次进入30条 1.下拉30条 2.上拉30条
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean getHotList(String channel, String operation) {
		String json = JsonCreateUtils.getHotListRequestJson(channel, operation);
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_HotList, json);
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
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_ClickUp, json);
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
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_Praise, json);
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
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_ReportUp, json);
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
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_GetShareUrl,
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
		return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, SquareCmd_Req_ShareVideo,
				json);
	}

	/**
	 * 同步获取视频广场列表数据
	 * 
	 * @param attribute
	 *            属性标签： 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料 6.堵车预警 7.惊险十分
	 *            8.疯狂超车 9.感人瞬间 10.传递正能量
	 * @return
	 * @author xuhw
	 * @date 2015年4月27日
	 */
	public String getSquareList(String attribute) {
		JSONObject json = new JSONObject();
		try {
			json.put("attribute", attribute);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square, SquareCmd_Get_SquareCache,
				json.toString());
	}

	/**
	 * 同步获取热门列表数据
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年4月24日
	 */
	public String getHotList() {
		return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square, SquareCmd_Get_HotCache, "");
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

		Iterator<String> iter = mVideoSquareManagerListener.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			if (null != key) {
				VideoSuqareManagerFn fn = mVideoSquareManagerListener.get(key);
				if (null != fn) {
					fn.VideoSuqare_CallBack(event, msg, param1, param2);
				}
			}
		}

	}

}
