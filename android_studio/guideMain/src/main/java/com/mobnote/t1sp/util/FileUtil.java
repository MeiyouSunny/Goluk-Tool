package com.mobnote.t1sp.util;

import android.os.Environment;

import com.mobnote.golukmain.photoalbum.FileInfoManagerUtils;
import com.mobnote.util.SortByDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File工具类
 */
public class FileUtil {
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
            path = Environment.getExternalStorageDirectory().getPath() + "/goluk/video/wonderful/";
        } else if (type == VIDEO_TYPE_URGENT) {
            path = Environment.getExternalStorageDirectory().getPath() + "/goluk/video/urgent/";
        }

        List<String> list = FileInfoManagerUtils.getFileNames(path, "(.+?mp4)");
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

}
