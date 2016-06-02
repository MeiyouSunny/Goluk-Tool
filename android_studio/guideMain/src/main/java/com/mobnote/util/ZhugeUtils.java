package com.mobnote.util;

import android.content.Context;

import com.mobnote.golukmain.R;

import org.json.JSONObject;

/**
 * Created by lily on 16-6-1.
 */
public class ZhugeUtils {

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

}
