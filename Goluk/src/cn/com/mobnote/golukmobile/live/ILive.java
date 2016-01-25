package cn.com.mobnote.golukmobile.live;

import cn.com.mobnote.golukmobile.R;

public interface ILive {

//	public static final String LIVE_START_PROGRESS_MSG = "正在创建直播，请稍候...";

//	public static final String LIVE_RETRY_UPLOAD_MSG = "直播断开，正在为您重新连接...";

//	public static final String LIVE_NET_ERROR = "网络异常，直播结束";

	public static final String LIVE_DIALOG_TITLE = "";

//	public static final String LIVE_RETRY_LIVE = "正在恢复直播，请稍候...";

//	public static final String LIVE_TIME_END = "时光匆匆，直播结束，再见";

//	public static final String LIVE_CREATE = "正在创建直播，请稍候...";

//	public static final String LIVE_EXIT_PROMPT = "您当前正在直播中，是否退出直播？";

//	public static final String LIVE_EXIT_PROMPT2 = "是否退出观看直播？";

//	public static final String LIVE_UPLOAD_FIRST_ERROR = "很抱歉，直播创建不成功，再试一次吧。";

	public static final int LOCATION_TYPE_UNKNOW = -1;
	public static final int LOCATION_TYPE_POINT = 0;
	public static final int LOCATION_TYPE_HEAD = 1;

	public static final String TAG = "LiveActivity";

	/** 8s视频 */
	public static final int MOUNTS = 114;

	public final int DURATION_TIMEOUT = 90 * 1000;

	/** 是否是直播 */
	public static final String KEY_IS_LIVE = "isLive";
	/** 要加入的群组ID */
	public static final String KEY_GROUPID = "groupID";
	/** 播放与直播地址 */
	public static final String KEY_PLAY_URL = "key_play_url";
	public static final String KEY_JOIN_GROUP = "key_join_group";
	public static final String KEY_USERINFO = "key_userinfo";
	public static final String KEY_LIVE_DATA = "key_livedata";
	public static final String KEY_LIVE_CONTINUE = "key_live_continue";
	public static final String KEY_LIVE_SETTING_DATA = "key_live_setting_data";

	public static final int[] shootImg = { R.drawable.live_btn_6s_record, R.drawable.live_btn_5s_record,
			R.drawable.live_btn_4s_record, R.drawable.live_btn_3s_record, R.drawable.live_btn_2s_record,
			R.drawable.live_btn_1s_record };

	public static final int[] mHeadImg = { 0, R.drawable.editor_boy_one, R.drawable.editor_boy_two,
			R.drawable.editor_boy_three, R.drawable.editor_girl_one, R.drawable.editor_girl_two,
			R.drawable.editor_girl_three, R.drawable.head_unknown };

	public static final int[] mBigHeadImg = { R.drawable.editor_head_feault7, R.drawable.editor_head_boy1,
			R.drawable.editor_head_boy2, R.drawable.editor_head_boy3, R.drawable.editor_head_girl4,
			R.drawable.editor_head_girl5, R.drawable.editor_head_girl6, R.drawable.editor_head_feault7 };

	/** 开始说话 */
	public final int MSG_SPEAKING_START_SPEAK = 1;
	/** 其它人说话结束 */
	public final int MSG_SPEAKING_OTHER_END = 3;
	/** 说话超时 */
	public final int MSG_SPEEKING_TIMEOUT = 4;
	public final int MSG_SPEEKING_BUSY = 5;

	/** 开启超时记录定时器 */
	public static final int MSG_H_SPEECH_OUT_TIME = 1;
	/** 对讲倒计时定时器 */
	public static final int MSG_H_SPEECH_COUNT_DOWN = 2;
	/** 定时查询录制视频文件是否存在 */
	public static final int MSG_H_QUERYFILEEXIT = 3;
	/** 视频上传失败 */
	public static final int MSG_H_UPLOAD_TIMEOUT = 4;
	/** 重新上传视频 */
	public static final int MSG_H_RETRY_UPLOAD = 5;
	/** 重新加载预览界面 */
	public static final int MSG_H_RETRY_SHOW_VIEW = 6;
	/** 重新请求看别人详情 */
	public static final int MSG_H_RETRY_REQUEST_DETAIL = 7;
	/** 播放器错误里，UI需要更新 */
	public static final int MSG_H_PLAY_LOADING = 8;
	/** 回到我的位置 */
	public static final int MSG_H_TO_MYLOCATION = 9;
	/** 查询地图上的大头針数据 */
	public static final int MSG_H_TO_GETMAP_PERSONS = 10;
	/** 文件查询时间 */
	public static final int QUERYFILETIME = 500;
}
