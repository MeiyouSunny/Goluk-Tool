package com.mobnote.golukmain.live;

import com.mobnote.golukmain.R;

public interface ILive {

    public static final String LIVE_DIALOG_TITLE = "";

    public static final int LOCATION_TYPE_UNKNOW = -1;
    public static final int LOCATION_TYPE_POINT = 0;
    public static final int LOCATION_TYPE_HEAD = 1;

    public static final String TAG = "LiveActivity";

    /**
     * 8s视频
     */
    public static final int MOUNTS = 114;

    public final int DURATION_TIMEOUT = 90 * 1000;

    /**
     * 是否是直播
     */
    public static final String KEY_IS_LIVE = "isLive";
    /**
     * 要加入的群组ID
     */
    public static final String KEY_GROUPID = "groupID";
    /**
     * 播放与直播地址
     */
    public static final String KEY_PLAY_URL = "key_play_url";
    public static final String KEY_VID = "key_vid";
    public static final String KEY_JOIN_GROUP = "key_join_group";
    public static final String KEY_USERINFO = "key_userinfo";
    public static final String KEY_LIVE_DATA = "key_livedata";
    public static final String KEY_LIVE_CONTINUE = "key_live_continue";
    public static final String KEY_LIVE_SETTING_DATA = "key_live_setting_data";

//    public static final int[] shootImg = {R.drawable.live_btn_6s_record, R.drawable.live_btn_5s_record,
//            R.drawable.live_btn_4s_record, R.drawable.live_btn_3s_record, R.drawable.live_btn_2s_record,
//            R.drawable.live_btn_1s_record};

    public static final int[] mHeadImg = {0, R.drawable.editor_boy_one, R.drawable.editor_boy_two,
            R.drawable.editor_boy_three, R.drawable.editor_girl_one, R.drawable.editor_girl_two,
            R.drawable.editor_girl_three, R.drawable.head_unknown};

    public static final int[] mBigHeadImg = {R.drawable.editor_head_feault7, R.drawable.editor_head_boy1,
            R.drawable.editor_head_boy2, R.drawable.editor_head_boy3, R.drawable.editor_head_girl4,
            R.drawable.editor_head_girl5, R.drawable.editor_head_girl6, R.drawable.editor_head_feault7};

    /**
     * 开始说话
     */
    public final int MSG_SPEAKING_START_SPEAK = 1;
    /**
     * 其它人说话结束
     */
    public final int MSG_SPEAKING_OTHER_END = 3;
    /**
     * 说话超时
     */
    public final int MSG_SPEEKING_TIMEOUT = 4;
    public final int MSG_SPEEKING_BUSY = 5;

    /**
     * 开启超时记录定时器
     */
    public static final int MSG_H_SPEECH_OUT_TIME = 1;
    /**
     * 对讲倒计时定时器
     */
    public static final int MSG_H_SPEECH_COUNT_DOWN = 2;
    /**
     * 定时查询录制视频文件是否存在
     */
    public static final int MSG_H_QUERYFILEEXIT = 3;
    /**
     * 视频上传失败
     */
    public static final int MSG_H_UPLOAD_TIMEOUT = 4;
    /**
     * 重新上传视频
     */
    public static final int MSG_H_RETRY_UPLOAD = 5;
    /**
     * 重新加载预览界面
     */
    public static final int MSG_H_RETRY_SHOW_VIEW = 6;
    /**
     * 重新请求看别人详情
     */
    public static final int MSG_H_RETRY_REQUEST_DETAIL = 7;
    /**
     * 播放器错误里，UI需要更新
     */
    public static final int MSG_H_PLAY_LOADING = 8;
    /**
     * 回到我的位置
     */
    public static final int MSG_H_TO_MYLOCATION = 9;
    /**
     * 查询地图上的大头針数据
     */
    public static final int MSG_H_TO_GETMAP_PERSONS = 10;
    /**
     * 重新发起一个新的直播
     */
    public static final int MSG_H_START_NEW_LIVE = 100;
    /**
     * 开始请求服务器
     */
    public static final int MSG_H_REQUEST_SERVER = 101;
    /**
     * 文件查询时间
     */
    public static final int QUERYFILETIME = 500;
    /**
     * 显示直播信息layout
     */
    public static final int MSG_H_SHOW_LIVE_INFO = 600;
    /**
     * 隐藏直播信息layout
     */
    public static final int MSG_H_HIDE_LIVE_INFO = 601;
}
