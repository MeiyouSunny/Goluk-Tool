package com.mobnote.t1sp.util;

import java.text.DecimalFormat;

/**
 * Util
 */
public class StringUtil {

    /**
     * 格式化文件大小 M -> GB
     *
     * @param fileSize M
     * @return
     */
    public static String formatFileSize(long fileSize) {
        DecimalFormat formater = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileSize < 1024) {
            fileSizeString = formater.format((double) fileSize) + "M";
        } else if (fileSize < 1024 * 1024) {
            fileSizeString = formater.format((double) fileSize / 1024) + "GB";
        }

        return fileSizeString;
    }

    /**
     * 容量大小转字符串
     *
     * @param size 容量大小
     * @return
     * @author xuhw
     * @date 2015年4月11日
     */
    public static String getSize(double size) {
        String result = "";
        double totalsize = 0;

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        if (size >= 1024) {
            totalsize = size / 1024;
            result = df.format(totalsize) + "GB";
        } else {
            totalsize = size;
            result = df.format(totalsize) + "MB";
        }

        return result;
    }

}
