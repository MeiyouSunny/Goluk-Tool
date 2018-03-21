package com.mobnote.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.thirdshare.IThirdShareFn;
import com.mobnote.golukmain.thirdshare.bean.SharePlatform;
import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONObject;

/**
 * Created by lily on 16-6-1.
 */
public class ZhugeUtils {

    /**
     * 5分钟
     **/
    private static final int MINS_5 = 300;
    /**
     * 10分钟
     **/
    private static final int MINS_10 = 600;
    /**
     * 30分钟
     **/
    private static final int MINS_30 = 1800;
    /**
     * 60分钟
     **/
    private static final int MINS_60 = 3600;
    /**
     * 120分钟
     **/
    private static final int MINS_120 = 7200;

    /**
     * 用户统计
     *
     * @param context
     * @param uid           用户ID
     * @param name          用户昵称
     * @param desc          是否默认个性签名
     * @param shareVideoNum 上传视频数
     * @param followedNum   关注好友数
     * @param fansNum       粉丝数
     */
    public static void userInfoAnalyze(Context context, String uid, String name, String desc,
                                       String shareVideoNum, String followedNum, String fansNum) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_user_id), uid);
            json.put(context.getString(R.string.str_zhuge_user_nickname), name);
            json.put(context.getString(R.string.str_zhuge_user_login_style), getLoginStyle(context));
            json.put(context.getString(R.string.str_zhuge_user_desc), getYesOrNo(false, context, desc));
            json.put(context.getString(R.string.str_zhuge_user_type), getIpcModle(context));
            json.put(context.getString(R.string.str_zhuge_user_sharevideo_number), shareVideoNum);
            json.put(context.getString(R.string.str_zhuge_user_followed_number), followedNum);
            json.put(context.getString(R.string.str_zhuge_user_fans_number), fansNum);

            ZhugeSDK.getInstance().identify(context, uid, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 登录方式
     *
     * @param context
     * @return 微信用户 / 极路客用户
     */
    private static String getLoginStyle(Context context) {
        String str = GolukFileUtils.loadString(GolukFileUtils.LOGIN_PLATFORM, "");
        if ("".equals(str)) {
            return context.getString(R.string.str_zhuge_user_login_style_goluk);
        } else if ("weixin".equals(str)) {
            return context.getString(R.string.str_zhuge_user_login_style_goluk);
        }
        return str;
    }

    /**
     * 用户类型
     *
     * @param context
     * @return G1/G2/T1/T1S/T2/其它/无设备
     */
    private static String getIpcModle(Context context) {
        String ipcModle = SharedPrefUtil.getIpcModel();
        if ("".equals(ipcModle)) {
            return context.getString(R.string.str_zhuge_user_type_no_ipc);
        }
        if (context.getString(R.string.str_zhuge_user_type_g1).equals(ipcModle)) {
            return ipcModle;
        }
        if (context.getString(R.string.str_zhuge_user_type_g2).equals(ipcModle)) {
            return ipcModle;
        }
        if (context.getString(R.string.str_zhuge_user_type_t1).equals(ipcModle)) {
            return ipcModle;
        }
        if (context.getString(R.string.str_zhuge_user_type_t1s).equals(ipcModle) || "T1s".equals(ipcModle)) {
            return ipcModle;
        }
        if (context.getString(R.string.str_zhuge_user_type_t2).equals(ipcModle)) {
            return ipcModle;
        }

        if (context.getString(R.string.str_zhuge_user_type_t3).equals(ipcModle) || "T3".equals(ipcModle)) {
            return ipcModle;
        }

        return context.getString(R.string.str_zhuge_share_video_network_other);
    }

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
            json.put(context.getString(R.string.str_zhuge_play_video_desc), getYesOrNo(true, context, desc));
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
     * @param context
     * @param source
     */
    public static void eventCallAlbum(Context context, String source) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_call_album_source), source);
            json.put(context.getString(R.string.str_zhuge_call_album_connect_ipc_state), getIsBind(context));

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
     * 安卓热点链接不成功页面访问
     * @param context
     * @param type
     * @param msg
     */
    public static void eventHotspotCreatFailed(Context context, String type, String msg) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_wifi_connect_fail_ipc_type), getIpcModle(context));
            json.put(context.getString(R.string.str_zhuge_after_effect_export_fail_os_version), android.os.Build.VERSION.RELEASE);
            json.put(context.getString(R.string.str_zhuge_ipc_hotspot_connect_type), type);
            json.put(context.getString(R.string.str_zhuge_after_effect_export_fail_manufacturer), android.os.Build.MANUFACTURER);
            json.put(context.getString(R.string.str_zhuge_wifi_connect_fail_reason), msg);
            json.put(context.getString(R.string.str_zhuge_after_effect_export_fail_device), android.os.Build.MODEL);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_wifi_connect_fail_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出编辑视频失败
     *
     * @param context
     * @param factory    视频时长
     * @param deviceType 音乐类型
     * @param osVersion  导出分辨率
     */
    public static void eventVideoExportFail(Context context, String factory, String deviceType, String osVersion) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_after_effect_export_fail_manufacturer), factory);
            json.put(context.getString(R.string.str_zhuge_after_effect_export_fail_device), deviceType);
            json.put(context.getString(R.string.str_zhuge_after_effect_export_fail_os_version), osVersion);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_after_effect_export_fail_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频拆分
     *
     * @param context
     */
    public static void eventChunkSplit(Context context) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_chunk_cut_video_type),
                    context.getString(R.string.str_ae_split));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_chunk_cut_video_tag), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频删除
     *
     * @param context
     */
    public static void eventChunkRemove(Context context) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_chunk_cut_video_type),
                    context.getString(R.string.str_ae_delete));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_chunk_cut_video_tag), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频添加
     *
     * @param context
     */
    public static void eventAddChunk(Context context, boolean ret) {
        try {
            JSONObject json = new JSONObject();
            if (ret) {
                json.put(context.getString(R.string.str_zhuge_add_chunk_status),
                        context.getString(R.string.str_zhuge_add_chunk_success));
            } else {
                json.put(context.getString(R.string.str_zhuge_add_chunk_status),
                        context.getString(R.string.str_zhuge_add_chunk_fail));
            }

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_add_chunk_tag), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSharePlatform(Context context, int channel) {
        switch (channel) {
            case SharePlatform.SHARE_PLATFORM_QQ:
                return context.getString(R.string.str_zhuge_share_video_channel_qq);
            case SharePlatform.SHARE_PLATFORM_QQ_ZONE:
                return context.getString(R.string.str_zhuge_share_video_channel_qq_space);
            case SharePlatform.SHARE_PLATFORM_WEXIN:
                return context.getString(R.string.str_zhuge_share_video_channel_weixin);
            case SharePlatform.SHARE_PLATFORM_WEXIN_CIRCLE:
                return context.getString(R.string.str_zhuge_share_video_channel_weixin_friends);
            case SharePlatform.SHARE_PLATFORM_WEIBO_SINA:
                return context.getString(R.string.str_zhuge_share_video_channel_sina);
            case SharePlatform.SHARE_PLATFORM_FACEBOOK:
                return context.getString(R.string.str_zhuge_share_video_channel_facebook);
            case SharePlatform.SHARE_PLATFORM_LINE:
                return context.getString(R.string.str_zhuge_share_video_channel_line);
            case SharePlatform.SHARE_PLATFORM_WHATSAPP:
                return context.getString(R.string.str_zhuge_share_video_channel_whatsapp);
            case SharePlatform.SHARE_PLATFORM_TWITTER:
                return context.getString(R.string.str_zhuge_share_video_channel_twitter);
            case SharePlatform.SHARE_PLATFORM_INSTAGRAM:
                return context.getString(R.string.str_zhuge_share_video_channel_instagram);
            default:
                return context.getString(R.string.str_zhuge_have_not);
        }
    }

    /**
     * 上传分享视频
     *
     * @param context
     * @param videoType    视频类型
     * @param videoQuality 分享视频质量
     * @param videoLength  视频时长范围
     * @param desc         是否有人工描述
     * @param channel      分享渠道
     * @param action       参加活动
     */
    public static void eventShareVideo(Context context, String videoType, String videoQuality,
                                       int videoLength, String desc, int channel, String action, String state) {
        float duration = ((float) videoLength) / 1000;
        String durationStr = "unsupported length";
        if (duration >= 10f && duration < 14f) {
            durationStr = "10~13S";
        } else if (duration >= 14f && duration <= 30f) {
            durationStr = "14~30S";
        } else if (duration > 30f && duration <= 60f) {
            durationStr = "30~60S";
        } else if (duration > 60f && duration <= 90f) {
            durationStr = "60~90S";
        }
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_share_video_type), getVideoType(context, videoType));
            json.put(context.getString(R.string.str_zhuge_share_video_quality), videoQuality);
            json.put(context.getString(R.string.str_zhuge_share_video_network), getNetworkType(context));
            if (GolukApplication.getInstance().isIpcLoginSuccess) {
                json.put(context.getString(R.string.str_zhuge_share_video_connect_ipc), context.getString(R.string.str_zhuge_yes));
            } else {
                json.put(context.getString(R.string.str_zhuge_share_video_connect_ipc), context.getString(R.string.str_zhuge_no));
            }
            json.put(context.getString(R.string.str_zhuge_share_video_length), durationStr);
            if (TextUtils.isEmpty(desc)) {
                json.put(context.getString(R.string.str_zhuge_share_video_desc), context.getString(R.string.str_zhuge_have_not));
            } else {
                json.put(context.getString(R.string.str_zhuge_share_video_desc), context.getString(R.string.str_zhuge_have));
            }
            json.put(context.getString(R.string.str_zhuge_share_video_channel), getSharePlatform(context, channel));
            json.put(context.getString(R.string.str_zhuge_share_video_action), getAction(context, action));
            json.put(context.getString(R.string.str_zhuge_share_video_state), state);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_upload_sharevideo_event), json);

            // XLog
            XLog.tag("ShareVideo").i(json);
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
     *
     * @param context
     * @param time    　时长
     * @param state   　发起状态
     * @param voice   　直播声音
     */
    public static void eventOpenLive(Context context, int time, String state, boolean voice) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_live_time), getOpenLiveTime(context, time));
            json.put(context.getString(R.string.str_zhuge_live_state), state);
            json.put(context.getString(R.string.str_zhuge_live_voice), getLiveVoice(context, voice));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_start_live_event), json);

            // XLog
            XLog.i(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭直播
     *
     * @param context
     * @param closeType 关闭类型
     * @param time      已直播时间
     */
    public static void eventCloseLive(Context context, String closeType, int time) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_close_live_type), closeType);
            json.put(context.getString(R.string.str_zhuge_close_live_time), getCloseLiveTime(context, time));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_close_live_event), json);

            // XLog
            XLog.i(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关注用户
     *
     * @param context
     * @param from    　关注来源
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
     * 精选页面
     * @param context
     */
    public static void eventWonderfulPage(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_wonderful_event));
    }

    /**
     * 精选页面-轮播图点击
     * @param context
     * @param page
     */
    public static void eventSlideView(Context context, int page) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_slide_view_order), context.getString(R.string.str_zhuge_slide_view_page, page));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_slide_view_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 精选页面-标签点击
     * @param context
     * @param lableName
     */
    public static void eventBannerText(Context context, String lableName) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_wonderful_lable_name), lableName);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_wonderful_lable_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 精选页面-下拉刷新
     * @param context
     */
    public static void eventWonderfulPull(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_wonderful_pulltorefresh_event));
    }

    /**
     * 精选页面-上拉加载
     * @param context
     * @param number
     */
    public static void eventWonderfulPush(Context context, int number) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_pushtorefresh_depth), context.getString(R.string.str_zhuge_pushtorefresh_numbers, number));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_wonderful_pushtorefresh_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 最新页面
     * @param context
     */
    public static void eventNewestPage(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_newest_event));
    }

    /**
     * 最新页面-上拉加载
     * @param context
     * @param number
     */
    public static void eventNewestlPush(Context context, int number) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_pushtorefresh_depth), context.getString(R.string.str_zhuge_pushtorefresh_numbers, number));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_newest_pushtorefresh_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 最新页面-分类页面
     * @param context
     */
    public static void eventNewestSort(Context context, String name) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_newest_sort_name), name);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_newest_sort_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IPC页面-精彩抓拍
     * @param context
     */
    public static void eventIpcSnap(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_snap_event));
    }

    /**
     * IPC页面-设置
     * @param context
     */
    public static void eventIpcSettings(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_settings_event));
    }

    /**
     * IPC-记录仪点击
     * @param context
     */
    public static void eventIpcCarrecorder(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_carrecorder_event));
    }

    /**
     * IPC-待连接页面
     * @param context
     * @param from
     */
    public static void eventWaitConnect(Context context, boolean from) {
        try {
            JSONObject json = new JSONObject();
            String str = "";
            if (from) {
                str = context.getString(R.string.str_zhuge_ipc_album);
            } else {
                str = context.getString(R.string.str_zhuge_ipc_carrecorder);
            }
            json.put(context.getString(R.string.str_zhuge_call_album_source), str);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_wait_connect_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IPC-连接中页面
     * @param context
     * @param from
     */
    public static void eventConnecting(Context context, boolean from) {
        try {
            JSONObject json = new JSONObject();
            String str = "";
            if (from) {
                str = context.getString(R.string.str_zhuge_ipc_album);
            } else {
                str = context.getString(R.string.str_zhuge_ipc_carrecorder);
            }
            json.put(context.getString(R.string.str_zhuge_call_album_source), str);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_connecting_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IPC-连接失败页面
     * @param context
     * @param from
     */
    public static void eventConnectFail(Context context, boolean from) {
        try {
            JSONObject json = new JSONObject();
            String str = "";
            if (from) {
                str = context.getString(R.string.str_zhuge_ipc_album);
            } else {
                str = context.getString(R.string.str_zhuge_ipc_carrecorder);
            }
            json.put(context.getString(R.string.str_zhuge_call_album_source), str);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_connect_fail_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IPC-连接失败页面-仅Wi-fi连接
     * @param context
     * @param from
     */
    public static void eventConnectFailWifi(Context context, boolean from) {
        try {
            JSONObject json = new JSONObject();
            String str = "";
            if (from) {
                str = context.getString(R.string.str_zhuge_ipc_album);
            } else {
                str = context.getString(R.string.str_zhuge_ipc_carrecorder);
            }
            json.put(context.getString(R.string.str_zhuge_call_album_source), str);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_connect_fail_wifi_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * IPC-连接成功
     * @param context
     */
    public static void eventConnectSuccess(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_connect_success_event));
    }

    /**
     * 相册页面-连接记录仪
     * @param context
     */
    public static void eventAlbumClickToConnectIPC(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ablum_to_connect_ipc_event));
    }

    /**
     * 相册页面-批量删除视频
     * @param context
     * @param type 页面类型  本地/精彩/紧急/循环
     */
    public static void eventAlbumBatchDelete(Context context, int type) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_album_detail_style), getAlbumType(context, type));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_album_batch_delete_video_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 相册页面-批量下载到本地
     * @param context
     * @param type 视频类型  精彩/紧急/循环
     */
    public static void eventAlbumBatchDownload(Context context, int type) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_share_video_type), getAlbumType(context, type));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_album_batch_download_video_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 相册详情页面-删除视频
     * @param context
     */
    public static void eventAlbumDeleteVideo(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_album_delete_single_video_event));
    }

    /**
     * 相册详情页面-下载到本地
     * @param context
     */
    public static void eventAlbumDownloadVideo(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_album_download_single_video_event));
    }

    /**
     * 视频自动同步
     * @param context
     * @param isStop  是否中止同步
     * @param number  同步视频数量
     */
    public static void eventAutoSynchronizeVideo(Context context, String isStop, int number) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_synchronize_video_whether_stop), isStop);
            json.put(context.getString(R.string.str_zhuge_synchronize_video_number), number);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_synchronize_video_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频制作页面-删除
     * @param context
     */
    public static void eventEditVideoDelete(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_edit_delete_video_event));
    }

    /**
     * 视频制作页面-裁剪
     * @param context
     */
    public static void eventEditVideoCut(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_edit_cut_video_event));
    }

    /**
     * 视频制作页面-添加音乐
     * @param context
     */
    public static void eventEditVideoMusic(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_edit_video_add_music_event));
    }

    /**
     * 视频详细页面
     * @param context
     * @param from  访问来源
     */
    public static void eventVideoDetail(Context context, String from) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_call_album_source), from);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_video_detail_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频评论
     * @param context
     */
    public static void eventCommentVideo(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_comment_video_event));
    }

    /**
     * 视频点赞
     * @param context
     * @param from  所在页面
     */
    public static void eventPraiseVideo(Context context, String from) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_praise_video_page), from);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_praise_video_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频分享
     * @param context
     * @param channel 分享渠道
     * @param from 所在页面
     */
    public static void eventShareVideo(Context context, String channel, String from) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_share_video_channel), getShareChannel(context, channel));
            json.put(context.getString(R.string.str_zhuge_praise_video_page), from);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_share_video_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 直播页面
     * @param context
     * @param from 访问来源
     */
    public static void eventLive(Context context, String from) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_call_album_source), from);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_live_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 直播分享
     * @param context
     * @param channel 分享渠道
     */
    public static void eventLiveShare(Context context, String channel) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_share_video_channel), getShareChannel(context, channel));

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_live_share_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册页面
     * @param context
     */
    public static void eventRegist(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_regist_event));
    }

    /**
     * 短信验证页面
     * @param context
     * @param from  来源
     */
    public static void eventSmsCode(Context context, boolean from) {
        try {
            JSONObject json = new JSONObject();
            String str = "";
            if (from) {
                str = context.getString(R.string.str_zhuge_sms_code_from_regist);
            } else {
                str = context.getString(R.string.str_zhuge_sms_code_from_forget_pwd);
            }
            json.put(context.getString(R.string.str_zhuge_from), str);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_sms_code_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册成功
     * @param context
     */
    public static void eventRegistSuccess(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_regist_success_event));
    }

    /**
     * 登录页面
     * @param context
     */
    public static void eventLogin(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_login_event));
    }

    /**
     * 微信登录
     * @param context
     */
    public static void eventWixinLogin(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_wxin_login_event));
    }

    /**
     * 登录成功
     * @param context
     * @param type 属性
     */
    public static void eventLoginSuccess(Context context, String type) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_login_type), type);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_login_success_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 忘记密码
     * @param context
     */
    public static void eventForgetPwd(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_sms_code_from_forget_pwd));
    }

    /**
     * 个人主页
     * @param context
     */
    public static void eventUserCenter(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_followed_from_usercenter));
    }

    /**
     * 搜索页面
     * @param context
     * @param keyWords  搜索关键词
     * @param result  搜索结果
     */
    public static void eventSearch(Context context, String keyWords, String result) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_search_key_words), keyWords);
            json.put(context.getString(R.string.str_zhuge_search_result), result);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_search_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息中心页面
     * @param context
     */
    public static void eventMsgCenter(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_msg_center_event));
    }

    /**
     * 消息中心-赞页面
     * @param context
     */
    public static void eventMsgCenterPraise(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_msg_center_praise_event));
    }

    /**
     * 消息中心-回复/评论页面
     * @param context
     */
    public static void eventMsgCenterComment(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_msg_center_comment_event));
    }

    /**
     * 摄像头管理页面
     * @param context
     */
    public static void eventIpcManage(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_manage_event));
    }

    /**
     * 固件升级页面
     * @param context
     */
    public static void eventIpcUpdate(Context context) {
        ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_update_event));
    }

    /**
     * 固件升级弹窗
     * @param context
     * @param operate  升级操作
     */
    public static void eventIpcUpdateDialog(Context context, String operate) {
        try {
            JSONObject json = new JSONObject();
            json.put(context.getString(R.string.str_zhuge_ipc_update_dialog_operate), operate);

            ZhugeSDK.getInstance().track(context, context.getString(R.string.str_zhuge_ipc_update_dialog_event), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是或否
     *
     * @param context
     * @param tag
     * @param desc
     * @return
     */
    public static String getYesOrNo(boolean tag, Context context, String desc) {
        String str = "";
        if (null == desc || "".equals(desc)) {
            str = context.getString(R.string.str_zhuge_no);
        } else {
            if (tag && (desc.equals(context.getString(R.string.str_zhuge_play_video_default_desc1))
                    || desc.equals(context.getString(R.string.str_zhuge_play_video_default_desc2)))) {
                str = context.getString(R.string.str_zhuge_no);
            } else {
                str = context.getString(R.string.str_zhuge_yes);
            }
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
        String str = context.getString(R.string.str_zhuge_video_type_ssp);
        if ("1".equals(type)) {
            return context.getString(R.string.str_zhuge_video_type_bgt);
        } else if ("3".equals(type)) {
            return context.getString(R.string.str_zhuge_video_type_mlfj);
        } else if ("4".equals(type)) {
            return context.getString(R.string.str_zhuge_video_type_ssp);
        } else if ("5".equals(type)) {
            return context.getString(R.string.str_zhuge_video_type_sgbl);
        } else {
            str = context.getString(R.string.str_zhuge_video_type_ssp);
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
     *
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
     *
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
     *
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

    /**
     * IPC连接状态
     * @param context
     * @return  已连接/未连接
     */
    private static String getIsBind(Context context) {
        if (GolukApplication.getInstance().isBindSucess()) {
            return context.getString(R.string.str_zhuge_call_album_connect_ipc_state_true);
        }
        return context.getString(R.string.str_zhuge_call_album_connect_ipc_state_false);
    }

    /**
     * 视频类型    本地/精彩/紧急/循环
     * @param context
     * @param type
     * @return
     */
    private static String getAlbumType(Context context, int type) {
        String str = "";
        if (type == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
            str = context.getString(R.string.str_zhuge_video_player_wonderful);
        } else if (type == PhotoAlbumConfig.PHOTO_BUM_IPC_URG) {
            str = context.getString(R.string.str_zhuge_video_player_urgent);
        } else if (type == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
            str = context.getString(R.string.str_zhuge_video_player_recycle);
        } else {
            str = context.getString(R.string.str_zhuge_video_player_local);
        }
        return str;
    }

    /**
     * 获取分享渠道
     * @param context
     * @param channel
     * @return
     */
    private static String getShareChannel(Context context, String channel){
        String str = "";
        if (IThirdShareFn.TYPE_WEIXIN.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_weixin);
        } else if (IThirdShareFn.TYPE_WEIBO_XINLANG.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_sina);
        } else if (IThirdShareFn.TYPE_QQ.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_qq);
        } else if (IThirdShareFn.TYPE_WEIXIN_CIRCLE.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_weixin_friends);
        } else if (IThirdShareFn.TYPE_QQ_ZONE.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_qq_space);
        } else if (IThirdShareFn.TYPE_FACEBOOK.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_facebook);
        } else if (IThirdShareFn.TYPE_TWITTER.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_twitter);
        } else if (IThirdShareFn.TYPE_INSTAGRAM.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_instagram);
        } else if (IThirdShareFn.TYPE_WHATSAPP.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_whatsapp);
        } else if (IThirdShareFn.TYPE_LINE.equals(channel)) {
            str = context.getString(R.string.str_zhuge_share_video_channel_line);
        } else {
            str = context.getString(R.string.str_zhuge_share_video_channel_copy);
        }
        return str;
    }

}
