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

}
