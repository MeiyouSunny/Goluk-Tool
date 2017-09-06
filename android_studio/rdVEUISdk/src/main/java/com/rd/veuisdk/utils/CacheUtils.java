package com.rd.veuisdk.utils;

import java.io.File;

import android.content.Context;

import com.rd.cache.ImageCache;

/**
 * @author abreal
 * 
 */
public class CacheUtils {
    /**
     * 视频缩略图缓冲目录
     */
    public static final String LOCAL_VIDEO_THUMBNAIL_CACHE_DIR = "local_video_thumbnails";
    public static final String STYLE_ANIM_CACHE_DIR = "style_cache_dir";
    /**
     * 相册缩略图缓冲目录
     */
    public static final String GALLERY_THUMBNAIL_CACHE_DIR = "gallery_thumbnails";
    public static final String GALLERY_THUMBNAIL_CACHE_DIR_NEW = "gallery_thumbnails_new";
    /**
     * 视频截图缓冲目录
     */
    public static final String VIDEO_SNAPSHOT_CACHE_DIR = "video_snapshot";
    /**
     * 在线视频缩略图缓冲目录
     */
    public static final String HTTP_VIDEO_THUMBNAIL_CACHE_DIR = "http_video_thumbnail";
    /**
     * 在线头像缓冲目录
     */
    public static final String HTTP_HEAD_THUMBNAIL_CACHE_DIR = "http_head_thumbnail";
    /**
     * 默认缩略图缓冲显示宽度
     */
    public static final int THUMBNAIL_DEFAULT_WIDTH = 160;
    /**
     * 默认缩略图缓冲显示高度
     */
    public static final int THUMBNAIL_DEFAULT_HEIGHT = 120;
    /**
     * 缩略图缓冲设定显示宽度
     */
    public static final int VIDEO_THUMBNAIL_WIDTH = 512;
    /**
     * 缩略图缓冲设定显示高度
     */
    public static final int VIDEO_THUMBNAIL_HEIGHT = 288;

    /**
     * 大缩略图缓冲设定显示宽度
     */
    public static final int VIDEO_BIG_THUMBNAIL_WIDTH = 960;
    /**
     * 大缩略图缓冲设定显示高度
     */
    public static final int VIDEO_BIG_THUMBNAIL_HEIGHT = 540;

    /**
     * 头像缩略图缓冲设定显示宽度
     */
    public static final int HEAD_THUMBNAIL_WIDTH = 56;
    /**
     * 头像缩略图缓冲设定显示高度
     */
    public static final int HEAD_THUMBNAIL_HEIGHT = 56;

    /**
     * 个人中心 用户头像大小
     */
    public static final int HEAD_USER_WIDTH = 150;
    public static final int HEAD_USER_HEIGHT = 150;
    public static final String HEAD_USER_HEIGHT_CACHE_DIR = "http_user_thumbnail";

    /**
     * 粉丝，关注，添加好友会员头像
     */
    public static final int HEAD_IDOL_WIDTH = 108;
    public static final int HEAD_IDOL_HEIGHT = 108;
    public static final String HTTP_IDOl_CACHE_DIR = "http_idol_thumbnail";
    /**
     * 音乐艺术家
     */
    public static final int HEAD_ARTIST_WIDTH = 200;
    public static final String HTTP_ARTIST_CACHE_DIR = "http_artist_thumbnail";

    /**
     * 清除我的视频缓存
     */
    public static void cleanLocalVideo(Context context) {
	File fLocalVideoCatchDir = ImageCache.getDiskCacheDir(context,
		LOCAL_VIDEO_THUMBNAIL_CACHE_DIR);
	if (fLocalVideoCatchDir.exists() && fLocalVideoCatchDir.isDirectory()) {
	    File[] arrFiles = fLocalVideoCatchDir.listFiles();
	    if (arrFiles != null && arrFiles.length > 0) {
		for (File fEachCacheFile : arrFiles) {
		    fEachCacheFile.delete();
		}
	    }
	}
    }

}
