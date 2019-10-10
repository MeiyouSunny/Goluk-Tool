package com.mobnote.t1sp.util;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

public class GolukUtils {

    /**
     * 文件大小的转换
     *
     * @param fileSize 文件大小
     * @return
     * @author xuhw
     * @date 2015年3月25日
     */
    public static String getSizeShow(double fileSize) {

        String result = "";
        double totalsize = 0;

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        if (fileSize >= 1024) {
            totalsize = fileSize / 1024;
            result = df.format(totalsize) + "GB";
        } else {
            totalsize = fileSize;
            result = df.format(totalsize) + "MB";
        }

        return result;
    }

    /**
     * 时间转换成字符串
     *
     * @param time 秒
     * @return
     * @author xuhw
     * @date 2015年3月25日
     */
    public static String minutesTimeToString(int time) {
        String result = null;

//		if (time / 60 < 1) {
//			if (time < 10) {
//				result = "00:0" + time;
//			} else {
//				result = "00:" + time;
//			}
//
//		} else {
//			int minue = time / 60;
//			int nSecond = time % 60;
//			String minueStr = minue > 10 ? "" + minue : "0" + minue;
//			String secondStr = nSecond > 10 ? "" + nSecond : "0" + nSecond;
//			result = minueStr + ":" + secondStr;
//		}

        result = time + "s";

        return result;
    }

    /**
     * 格式化时间格式
     *
     * @param time 时间
     * @return
     * @author xuhw
     * @date 2015年3月25日
     */
    public static String getTimeStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result = sdf.format(new Long(time));
        return result;
    }

    public static long parseStringToMilli(String timeString) {
        if (TextUtils.isDigitsOnly(timeString))
            return -1;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date time = sdf.parse(timeString);
            return time.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static String getDateStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String result = sdf.format(new Long(time));
        return result;
    }

    /**
     * 创建文件夹
     *
     * @param dirname 文件夹路径
     * @author xuhw
     * @date 2015年2月12日
     */
    public static boolean makedir(String dirname) {
        File dir = new File(dirname);
        if (!dir.exists()) {
            return dir.mkdirs();
        }

        return false;
    }

    public static int parseVideoFileType(String filename) {
        int type = -1;

        if (filename.contains("WND")) {
            type = IPCManagerFn.TYPE_SHORTCUT;
        } else if (filename.contains("URG")) {
            type = IPCManagerFn.TYPE_URGENT;
        } else if (filename.contains("NRM")) {
            type = IPCManagerFn.TYPE_CIRCULATE;
        }

        return type;
    }

    /**
     * 列表是否为空
     *
     * @param list 列表
     * @return 是否为空
     */
    public static boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }

    /**
     * 获取视频封面保存路径
     *
     * @param fileName 视频名称
     * @return
     */
    public static String getThumbSavePath(String fileName) {
        String videoPath = Environment.getExternalStorageDirectory() + File.separator
                + "goluk" + File.separator
                + "thumb" + File.separator
                + fileName;
        videoPath = videoPath.replace("mp4", "thumb");
        videoPath = videoPath.replace("MP4", "thumb");
        return videoPath;
    }

    /**
     * 获取SD卡剩余容量(字节)
     *
     * @return
     */
    public static long getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long bolckSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();
        long sizeAvailable = bolckSize * availableBlocks;
        return sizeAvailable;
    }

    /**
     * 判断是否SD卡足够
     *
     * @param sizeNeed 需要的大小(字节)
     * @return
     */
    public static boolean isSDEnough(long sizeNeed) {
        long sizeAvailable = getSDAvailableSize();
        // 预留的10M大小
        long sizeReserved = 1024 * 1024 * 10;

        return (sizeAvailable - sizeNeed) > sizeReserved;
    }

    public static boolean isSwitchOn(int state) {
        return state == 1;
    }

    public static int parseVolumeLevel(int levelF4) {
        if (levelF4 == 1)
            return 2;
        if (levelF4 == 2)
            return 1;
        if (levelF4 == 3)
            return 0;

        return 1;
    }

    /**
     * 获取视频保存路径
     *
     * @param fileName 视频名称
     * @return
     */
    public static String getVideoSavePath(String fileName) {
        String dir = "";
        if (fileName.contains("WND")) {
            dir = Const.DIR_WONDERFUL;
        } else if (fileName.contains("URG")) {
            dir = Const.DIR_URGENT;
        } else if (fileName.contains("NRM_TL")) {
            dir = Const.DIR_TIMESLAPSE;
        } else if (fileName.contains("NRM")) {
            dir = Const.DIR_LOOP;
        }

        String videoPath = Environment.getExternalStorageDirectory() + File.separator
                + "goluk" + File.separator
                + "video" + File.separator
                + dir + File.separator
                + fileName;
        return videoPath;
    }

}
