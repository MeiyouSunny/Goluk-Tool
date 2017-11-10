package com.mobnote.t1sp.util;

import android.os.Environment;

import com.mobnote.golukmain.photoalbum.FileInfoManagerUtils;
import com.mobnote.util.SortByDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File工具类: T1SP
 */
public class FileUtil {
    /* SD卡路径 */
    public static final String EXTERNAL_SD_PATH = Environment.getExternalStorageDirectory().getPath();
    /* 精彩视频路径 */
    public static final String WONDERFUL_VIDEO_PATH = EXTERNAL_SD_PATH + "/goluk/video/wonderful/";
    /* 紧急视频路径 */
    public static final String URGENT_VIDEO_PATH = EXTERNAL_SD_PATH + "/goluk/video/urgent/";

    /* 精彩视频文件前缀 */
    public static final String WONDERFUL_VIDEO_PREFIX = "SHARE";
    /* 紧急视频文件前缀 */
    public static final String URGENT_VIDEO_PREFIX = "EMER";
    /* 循环视频文件前缀 */
    public static final String LOOP_VIDEO_PREFIX = "FILE";

    /* 精彩视频 */
    public static final int VIDEO_TYPE_WONDERFUL = 1;
    /* 紧急视频 */
    public static final int VIDEO_TYPE_URGENT = 2;

    /**
     * 根据类型获取本地最新2个视频
     *
     * @param type 视频类型
     */
    public static List<String> getNewVideoByType(int type) {
        String path = "";
        if (type == VIDEO_TYPE_WONDERFUL) {
            path = WONDERFUL_VIDEO_PATH;
        } else if (type == VIDEO_TYPE_URGENT) {
            path = URGENT_VIDEO_PATH;
        }

        List<String> list = FileInfoManagerUtils.getFileNames(path, "(.+?(mp|MP)4)");
        Collections.sort(list, new SortByDate());
        List<String> result = new ArrayList<String>();
        if (list.size() > 0)
            result.add(path + list.get(0));
        if (list.size() > 1)
            result.add(path + list.get(1));

        return result;
    }

    /**
     * 获取最新2个视频(精彩视频和紧急视频综合)
     */
    public static List<String> getLatestTwoVideosWithWonfulAndUrgent() {
        List<String> wonderfuls = getNewVideoByType(VIDEO_TYPE_WONDERFUL);
        List<String> urgents = getNewVideoByType(VIDEO_TYPE_URGENT);
        List<String> videos = new ArrayList<String>();
        List<String> result = new ArrayList<String>();

        if (wonderfuls != null) {
            videos.addAll(wonderfuls);
        }
        if (urgents != null) {
            videos.addAll(urgents);
        }

        Collections.sort(videos, new SortByDate());
        if (videos.size() > 0)
            result.add(videos.get(0));
        if (videos.size() > 1)
            result.add(videos.get(1));

        return result;
    }

    /**
     * T1SP 获取网络完整URL
     */
    public static String getVideoUrlByPath(String videoPath) {
        return Const.HTTP_SCHEMA + Const.IP + videoPath;
    }

    /**
     * 根据视频名获取精彩视频绝对路径
     */
    public static String getWonderfulVideoPathByName(String videoName) {
        return WONDERFUL_VIDEO_PATH + videoName;
    }

}
