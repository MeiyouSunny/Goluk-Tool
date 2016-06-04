package com.mobnote.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONObject;

/**
 * Created by lily on 16-6-1.
 */
public class ZhugeUtils {

    /** 5分钟 **/
    private static final int MINS_5 = 300;
    /** 10分钟 **/
    private static final int MINS_10 = 600;
    /** 30分钟 **/
    private static final int MINS_30 = 1800;
    /** 60分钟 **/
    private static final int MINS_60 = 3600;
    /** 120分钟 **/
    private static final int MINS_120 = 7200;

    /**
     * 播放视频
     *
     * @param context
     * @param videoId
     * @param desc
     * @param videoType
     * @param playPage
     * @return
     */
    public static JSONObject eventPlayVideo(Context context, String videoId, String desc, String action, String videoType, int playPage) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_play_video_id), videoId);
            json.put(context.getString(R.string.str_zhuge_play_video_desc), getYesOrNo(context, desc));
            json.put(context.getString(R.string.str_zhuge_play_video_action), getAction(context, action));
            json.put(context.getString(R.string.str_zhuge_play_video_type), getVideoType(context, videoType));
            json.put(context.getString(R.string.str_zhuge_play_video_page), getPlayPage(context, playPage));
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 相册页面访问
     *
     * @param context
     * @param source
     * @return
     */
    public static void eventCallAlbum(Context context, String source) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_call_album_source), source);
            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_call_album_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 即刻分享页面访问
     *
     * @param context
     * @param source
     */
    public static void eventShare(Context context, String source) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_call_album_source), source);
            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_share_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频后处理页面访问
     *
     * @param context
     */
    public static void eventVideoEdit(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_video_edit_event));
    }

    /**
     * IPC页面访问
     *
     * @param context
     */
    public static void eventIpc(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_event));
    }

    /**
     * 导出后处理视频
     *
     * @param context
     * @param videoLength 视频时长
     * @param musicType   音乐类型
     * @param resolution  导出分辨率
     */
    public static void eventVideoExport(Context context, String videoLength, String musicType, String resolution) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_after_effect_video_duration), videoLength);
            json.put(context.getString(R.string.str_zhuge_after_effect_music_type), musicType);
            json.put(context.getString(R.string.str_zhuge_after_effect_resolution), resolution);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_after_effect_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传分享视频
     *
     * @param context
     * @param videoType    视频类型
     * @param videoQuality 分享视频质量
     * @param connectIPC   分享时是否链接记录仪
     * @param videoLength  视频时长范围
     * @param desc         是否有人工描述
     * @param channel      分享渠道
     * @param action       参加活动
     */
    public static void eventShareVideo(Context context, String videoType, String videoQuality, String connectIPC,
                                       String videoLength, String desc, String channel, String action) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_share_video_type), getVideoType(context, videoType));
            json.put(context.getString(R.string.str_zhuge_share_video_quality), videoQuality);
            json.put(context.getString(R.string.str_zhuge_share_video_network), getNetworkType(context));
            json.put(context.getString(R.string.str_zhuge_share_video_connect_ipc), connectIPC);
            json.put(context.getString(R.string.str_zhuge_share_video_length), videoLength);
            json.put(context.getString(R.string.str_zhuge_share_video_desc), getYesOrNo(context, desc));
            json.put(context.getString(R.string.str_zhuge_share_video_channel), channel);
            json.put(context.getString(R.string.str_zhuge_share_video_action), getAction(context, action));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_share_video_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 相册竖屏播放页面访问
     *
     * @param context
     * @param tag     相册标签
     * @param type    类型
     */
    public static void eventAlbumPlayer(Context context, String tag, String type) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_album_player_tag), tag);
            json.put(context.getString(R.string.str_zhuge_album_player_type), type);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_album_player_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发起直播
     * @param context
     * @param time　时长
     * @param state　发起状态
     * @param voice　直播声音
     */
    public static void eventOpenLive(Context context, int time, String state, boolean voice) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_live_time), getOpenLiveTime(context, time));
            json.put(context.getString(R.string.str_zhuge_live_state), state);
            json.put(context.getString(R.string.str_zhuge_live_voice), getLiveVoice(context, voice));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_live_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭直播
     * @param context
     * @param closeType 关闭类型
     * @param time 已直播时间
     */
    public static void eventCloseLive(Context context, String closeType, int time) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_close_live_type), closeType);
            json.put(context.getString(R.string.str_zhuge_close_live_time), getCloseLiveTime(context, time));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_close_live_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关注用户
     * @param context
     * @param from　关注来源
     */
    public static void eventFollowed(Context context, String from) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_followed_from), from);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_followed_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是或否
     *
     * @param context
     * @param desc
     * @return
     */
    public static String getYesOrNo(Context context, String desc) {
        String str = "";
        if (null == desc || "".equals(desc)) {
            str = context.getString(R.string.str_zhuge_no);
        } else {
            str = context.getString(R.string.str_zhuge_yes);
        }
        return str;
    }

    /**
     * 活动名称/未参加
     *
     * @param context
     * @param actionName
     * @return
     */
    private static String getAction(Context context, String actionName) {
        String str = "";
        if (null == actionName || "".equals(actionName)) {
            str = context.getString(R.string.str_zhge_play_video_no_participate);
        } else {
            str = actionName;
        }
        return str;
    }

    /**
     * 视频类型
     *
     * @param context
     * @param type
     * @return
     */
    private static String getVideoType(Context context, String type) {
        String str = "";
        if ("1".equals(type)) {
            return context.getString(R.string.str_zhuge_video_type_bgt);
        } else if ("3".equals(type)) {
            return context.getString(R.string.str_zhuge_video_type_mlfj);
        } else if ("4".equals(type)) {
            return context.getString(R.string.str_zhuge_video_type_ssp);
        } else if ("5".equals(type)) {
            return context.getString(R.string.str_zhuge_video_type_sgbl);
        } else {

        }
        return str;
    }

    /**
     * 视频播放页面
     *
     * @param context
     * @param source  1最新页　　２四大分类页　３我的关注内容列表　４活动聚合页　５视频详情页
     * @return
     */
    private static String getPlayPage(Context context, int source) {
        String str = "";
        switch (source) {
            case 1:
                str = context.getString(R.string.str_zhuge_play_video_page_newest);
                return str;
            case 2:
                str = context.getString(R.string.str_zhuge_play_video_page_category);
                return str;
            case 3:
                str = context.getString(R.string.str_zhuge_play_video_page_followed);
                return str;
            case 4:
                str = context.getString(R.string.str_zhuge_play_video_page_cluster);
                return str;
            case 5:
                str = context.getString(R.string.str_zhuge_play_video_page_videodetail);
                return str;
            default:
                return "";
        }
    }

    /**
     * 当前网络环境
     *
     * @param context
     * @return
     */
    private static String getNetworkType(Context context) {
        //获取系统网络服务
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) GolukApplication.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //没网
        if (null == connectivityManager) {
            return context.getString(R.string.str_zhuge_share_video_network_other);
        }
        //获取当前网络类型
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (null == activeNetInfo || !activeNetInfo.isAvailable()) {
            return context.getString(R.string.str_zhuge_share_video_network_other);
        }
        //判断是不是WIFI连接
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null == wifiNetInfo) {
            return context.getString(R.string.str_zhuge_share_video_network_other);
        }
        NetworkInfo.State state = wifiNetInfo.getState();
        if (null == state) {
            return context.getString(R.string.str_zhuge_share_video_network_other);
        }
        if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
            return context.getString(R.string.str_zhuge_share_video_network_wifi);
        }
        //判断手机网络类型
        NetworkInfo mobileNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null == mobileNetInfo) {
            return context.getString(R.string.str_zhuge_share_video_network_other);
        }
        NetworkInfo.State mobileState = mobileNetInfo.getState();
        String subName = mobileNetInfo.getSubtypeName();
        if (null == mobileNetInfo) {
            return context.getString(R.string.str_zhuge_share_video_network_other);
        }
        if (mobileState == NetworkInfo.State.CONNECTED || mobileState == NetworkInfo.State.CONNECTING) {
            switch (activeNetInfo.getSubtype()) {
                //如果是2g类型
                case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return context.getString(R.string.str_zhuge_share_video_network_other);
                //如果是3g类型
                case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return context.getString(R.string.str_zhuge_share_video_network_3g);
                //如果是4g类型
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return context.getString(R.string.str_zhuge_share_video_network_4g);
                default:
                    if (subName.equalsIgnoreCase("TD-SCDMA") || subName.equalsIgnoreCase("WCDMA") || subName.equalsIgnoreCase("CDMA2000")) {
                        return context.getString(R.string.str_zhuge_share_video_network_3g);
                    } else {
                        return context.getString(R.string.str_zhuge_share_video_network_other);
                    }
            }
        }
        return context.getString(R.string.str_zhuge_share_video_network_other);
    }

    /**
     * 开启直播时长
     * @param context
     * @param time
     * @return
     */
    private static String getOpenLiveTime(Context context, int time) {
        if (time < MINS_60 && time >= 0) {
            return context.getString(R.string.str_zhuge_live_time1);
        }
        if (time >= MINS_60 && time < MINS_120) {
            return context.getString(R.string.str_zhuge_live_time2);
        }
        if (time > MINS_120) {
            return context.getString(R.string.str_zhuge_live_time3);
        }
        return "";
    }

    /**
     * 关闭直播时已直播时间
     * @param context
     * @param time
     * @return
     */
    private static String getCloseLiveTime(Context context, int time) {
        if (time >= 0 && time <= MINS_5) {
            return context.getString(R.string.str_zhuge_close_live_time1);
        }
        if (time > MINS_5 && time <= MINS_10) {
            return context.getString(R.string.str_zhuge_close_live_time2);
        }
        if (time > MINS_10 && time <= MINS_30) {
            return context.getString(R.string.str_zhuge_close_live_time3);
        }
        if (time > MINS_30 && time <= MINS_60) {
            return context.getString(R.string.str_zhuge_close_live_time4);
        }
        if (time > MINS_60 && time <= MINS_120) {
            return context.getString(R.string.str_zhuge_close_live_time5);
        }
        if (time > MINS_120) {
            return context.getString(R.string.str_zhuge_close_live_time6);
        }
        return "";
    }

    /**
     * 获取直播声音
     * @param context
     * @param voice
     * @return
     */
    private static String getLiveVoice(Context context, boolean voice) {
        if (voice) {
            return context.getString(R.string.str_zhuge_live_voice_open);
        }
        return context.getString(R.string.str_zhuge_live_voice_close);
    }


}
