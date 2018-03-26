package com.mobnote.golukmain.photoalbum;

import android.text.TextUtils;

/**
 * 相册配置类，所有相册相关的固定配置请写在这里
 *
 * @author leege100
 */
public class PhotoAlbumConfig {

    /***相册本地所有类型视频*/
    public static final int PHOTO_BUM_LOCAL = 0;
    /***相册远程精彩视频*/
    public static final int PHOTO_BUM_IPC_WND = 1;
    /***相册远程紧急视频*/
    public static final int PHOTO_BUM_IPC_URG = 2;
    /***相册远程循环视频*/
    public static final int PHOTO_BUM_IPC_LOOP = 3;
    /***相册远程缩时视频*/
    public static final int PHOTO_BUM_IPC_TIMESLAPSE = 4;

    /**
     * 自动查询
     **/
    public static final String VIDEO_LIST_TAG_SEARACH = "0";
    /**
     * 相册查询
     **/
    public static final String VIDEO_LIST_TAG_PHOTO = "1";

    /**
     * 本地文件存储目录
     */
    public static final String LOCAL_LOOP_VIDEO_PATH = "fs1:/video/loop/";
    public static final String LOCAL_WND_VIDEO_PATH = "fs1:/video/wonderful/";
    public static final String LOCAL_URG_VIDEO_PATH = "fs1:/video/urgent/";

    /* 视频文件名前缀 */
    public static final String PREFIX_LOOP = "NRM";
    public static final String PREFIX_WND = "WND";
    public static final String PREFIX_URG = "URG";
    public static final String PREFIX_TIMESLAPSE = "NRM_TL";

    /**
     * 根据视频名称获取视频类型
     */
    public static int getVideoTypeByName(String videoName) {
        if (TextUtils.isEmpty(videoName))
            return 0;
        if (videoName.startsWith(PREFIX_WND))
            return PHOTO_BUM_IPC_WND;
        if (videoName.startsWith(PREFIX_URG))
            return PHOTO_BUM_IPC_URG;
        if (videoName.startsWith(PREFIX_TIMESLAPSE))
            return PHOTO_BUM_IPC_TIMESLAPSE;
        if (videoName.startsWith(PREFIX_LOOP))
            return PHOTO_BUM_IPC_LOOP;

        return 0;
    }

}
