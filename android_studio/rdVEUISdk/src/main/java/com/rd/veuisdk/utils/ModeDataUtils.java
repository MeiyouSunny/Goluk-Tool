package com.rd.veuisdk.utils;

import android.content.Context;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.rd.http.NameValuePair;
import com.rd.net.RdHttpClient;
import com.rd.vecore.RdVECore;
import com.rd.veuisdk.model.MVWebInfo;
import com.rd.veuisdk.model.WebFilterInfo;
import com.rd.veuisdk.model.bean.AppData;
import com.rd.veuisdk.model.bean.DataBean;
import com.rd.veuisdk.utils.cache.CacheManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取模板数据
 * 2018/06/20
 */
public class ModeDataUtils {


    //音效
    public static final String TYPE_YUN_AUDIO_EFFECT = "audio";
    //云音乐--190625
    public static final String TYPE_YUN_CLOUD_MUSIC = "cloud_music";
    //ae模板
    public static final String TYPE_VIDEO_AE = "videoae";
    //特效
    public static final String TYPE_SPECIAL_EFFECT = "specialeffects";
    //贴纸
    public static final String TYPE_STICKERS = "stickers";

    //转场
    public static final String TYPE_TRANSITION = "transition";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_YUN_AUDIO_EFFECT, TYPE_YUN_CLOUD_MUSIC, TYPE_VIDEO_AE, TYPE_SPECIAL_EFFECT, TYPE_STICKERS, TYPE_TRANSITION})
    public @interface ResourceType {

    }


    private static String appkey = "";

    public static void init(String key) {
        appkey = key;
    }

    public static final String TYPE_MUSIC = "bk_music";
    public static final String TYPE_SUB_TITLE = "sub_title";
    public static final String TYPE_MV = "mv";
    public static final String TYPE_FILTER = "filter";
    public static final String TYPE_EFFECTS = "effects";//贴纸
    public static final String TYPE_FONT = "font_family";

    public static final String TYPE_CLOUD_MUSIC = "cloud_music_type";
    public static final String TYPE_MVAE = "mvae";


    //rd资源列表 （支持分类id请求）
    public static final String RD_APP_DATA = "http://d.56show.com/filemanage2/public/filemanage/file/appData";

    //rd单项分类列表
    public static final String RD_TYPE_URL = "http://d.56show.com/filemanage2/public/filemanage/file/typeData";

    /**
     * @param url  自定义的网络接口
     * @param type bk_music（背景音乐） 或 sub_title（字幕） 或 start_end（片头片尾） 或 water_mark（水印）或 filter（滤镜） effects（特效），font_family（字体）, cloud_music_type(云音乐)
     * @return
     */
    public static String getModeData(String url, @ResourceType String type) {
        String requestId = CacheManager.getCacheManager().getKey("url:" + url + "?type:" + type + "appkey:" + appkey + "ver:" + RdVECore.getVersionCode());
        String result = CacheManager.getCacheManager().getCache(requestId);
        if (!TextUtils.isEmpty(result)) {
            return result;
        }
        try {
            result = RdHttpClient.post(url, new NameValuePair("type", type), new NameValuePair("appkey", appkey), new NameValuePair("os", "android"),
                    new NameValuePair("ver", RdVECore.getVersionCode() + ""));
            CacheManager.getCacheManager().putCache(requestId, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取分类下面的数据
     *
     * @param type     类型  字幕|特效 ....
     * @param category 根据分类id获取数据
     */
    public static String getModeData(String url, @ResourceType String type, String category) {
        String requestId = CacheManager.getCacheManager().getKey("url:" + url + "?type:" + type + "appkey:" + appkey + "category:" + category + "ver:" + RdVECore.getVersionCode());
        String result = CacheManager.getCacheManager().getCache(requestId);
        if (!TextUtils.isEmpty(result)) {
            return result;
        }
        try {
            result = RdHttpClient.post(url, new NameValuePair("type", type), new NameValuePair("appkey", appkey), new NameValuePair("os", "android"),
                    new NameValuePair("ver", RdVECore.getVersionCode() + ""), new NameValuePair("category", category));
            CacheManager.getCacheManager().putCache(requestId, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 获取资源分类 （190704）
     *
     * @param type 特效、ae模板
     * @return
     */
    public static String getTypeData(String url, @ResourceType String type) {
        return getModeData(url, type);
    }


    /**
     * 云音乐、音效 (均支持分类、分页)
     *
     * @param type     类别
     * @param category 获取到的分类id
     * @param page_num 分页数
     * @return
     */
    public static String getModeData(String url, @ResourceType String type, String category, int page_num) {
        String requestId = CacheManager.getCacheManager().getKey("url:" + url + "?type:" + type + "appkey:" + appkey + "category:" + category + "page_num:" + page_num + "ver:" + RdVECore.getVersionCode());
        String result = CacheManager.getCacheManager().getCache(requestId);
        if (!TextUtils.isEmpty(result)) {
            return result;
        }
        try {
            result = RdHttpClient.post(url, new NameValuePair("type", type), new NameValuePair("appkey", appkey), new NameValuePair("os", "android"),
                    new NameValuePair("category", category), new NameValuePair("page_num", String.valueOf(page_num)));
            CacheManager.getCacheManager().putCache(requestId, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 获取网络数据
     *
     * @param context
     * @param url
     * @param type
     * @return
     */
    public static String getData(Context context, String url, String type) {
        return getModeData(url, type);
    }


    /**
     * 网络数据
     */
    public static List init(Context context, String url, String type) {
        String content = getModeData(url, type);
        List list = null;
        if (!TextUtils.isEmpty(content)) {
            AppData appData = JSON.parseObject(content, AppData.class);
            if (null != appData && appData.getData() != null) {
                int len = appData.getData().size();
                DataBean dataBean;
                list = new ArrayList();
                for (int i = 0; i < len; i++) {
                    dataBean = appData.getData().get(i);
                    String name = dataBean.getName();
                    String file = dataBean.getFile();
                    String cover = dataBean.getCover();
                    long updatetime = dataBean.getUpdatetime();
                    if (type.equals(TYPE_MV)) {
                        list.add(new MVWebInfo(file, cover, name, updatetime));
                    } else if (type.equals(TYPE_FILTER)) {
                        list.add(new WebFilterInfo(file, cover, name, "", updatetime));
                    }
                }
            }
        }

        return list;
    }

    /**
     * 特效
     */
    public static AppData getEffectAppData(String url, @ResourceType String type, String category) {
        String content = getModeData(url, type, category);
        if (!TextUtils.isEmpty(content)) {
            return JSON.parseObject(content, AppData.class);
        }
        return null;
    }


}
