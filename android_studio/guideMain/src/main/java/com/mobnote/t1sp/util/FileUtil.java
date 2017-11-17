package com.mobnote.t1sp.util;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import com.mobnote.golukmain.photoalbum.FileInfoManagerUtils;
import com.mobnote.util.SortByDate;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File工具类: T1SP
 */
public class FileUtil {
    /* SD卡路径 */
    public static final String EXTERNAL_SD_PATH = Environment.getExternalStorageDirectory().getPath();
    /* Goluk目录 */
    public static final String GOLUK_DIR = EXTERNAL_SD_PATH + "/goluk/";
    /* 精彩视频路径 */
    public static final String WONDERFUL_VIDEO_PATH = EXTERNAL_SD_PATH + "/goluk/video/wonderful/";
    /* 紧急视频路径 */
    public static final String URGENT_VIDEO_PATH = EXTERNAL_SD_PATH + "/goluk/video/urgent/";
    /* 循环视频路径 */
    public static final String LOOP_VIDEO_PATH = EXTERNAL_SD_PATH + "/goluk/video/loop/";
    /* 视频缩略图缓存路径 */
    public static final String THUMB_CACHE_DIR = EXTERNAL_SD_PATH + "/goluk/goluk_carrecorder/image/";

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

    /**
     * 从文件路径获取文件名
     *
     * @param path 文件路径
     */
    public static String getFileNameFromPath(String path) {
        if (TextUtils.isEmpty(path) || !path.contains("/"))
            return "";

        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String getThumbCacheByVideoName(String videoName) {
        String fileName = videoName.replace("MP4", "jpg");
        fileName = FileUtil.THUMB_CACHE_DIR + fileName;
        return fileName;
    }

    /**
     * 拷贝文件
     *
     * @param srcFileName 源文件
     * @param newFileName 目标文件
     */
    public static void copyFile(String srcFileName, String newFileName) {
        if (TextUtils.isEmpty(srcFileName) || TextUtils.isEmpty(newFileName))
            return;
        copyFile(new File(srcFileName), new File(newFileName));
    }

    /**
     * 拷贝文件
     *
     * @param srcFile 源文件
     * @param newFile 目标文件
     */
    public static void copyFile(File srcFile, File newFile) {
        if (newFile.exists() && newFile.length() == srcFile.length())
            return;
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            if (srcFile.exists()) { // 文件存在时
                inStream = new FileInputStream(srcFile); // 读入原文件
                fs = new FileOutputStream(newFile);
                byte[] buffer = new byte[1024 * 4];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inStream) {
                    inStream.close();
                }
                if (null != fs) {
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存Bitmap到本地文件
     *
     * @param bitmap   Bitmap
     * @param savePath 保存路径
     * @param percent  压缩百分比
     */
    public static void saveBitmap(Bitmap bitmap, File savePath, int percent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, percent, baos);
        byte[] byteArray = baos.toByteArray();
        try {
            FileOutputStream fos = new FileOutputStream(savePath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(byteArray);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 把fs1:/路径转换位真实路径
     */
    public static String convertFs1ToRealPath(String path) {
        if (TextUtils.isEmpty(path))
            return "";
        if (path.contains("fs1:/")) {
            path = path.substring("fs1:/".length());
            path = GOLUK_DIR + path;
        }

        return path;
    }

}
