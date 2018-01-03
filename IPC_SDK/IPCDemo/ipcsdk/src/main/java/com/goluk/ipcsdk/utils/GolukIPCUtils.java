package com.goluk.ipcsdk.utils;

import android.text.TextUtils;

import com.goluk.ipcsdk.main.GolukIPCSdk;

import cn.com.tiros.api.IpcWifiManager;

/**
 * Created by leege100 on 16/6/7.
 */
public class GolukIPCUtils {
    /**
     * get rtmp video preview url
     *
     * @return the rtmp url
     */
    public static String getRtmpPreviewUrl() {
        if (!GolukIPCSdk.getInstance().isSdkValid()) {
            return null;
        }
        if (IpcWifiManager.isT1T2())

            return "rtsp://192.168.62.1/stream1";
        if (IpcWifiManager.isT3())
            return "rtsp://192.168.62.1/sub";

        return "";

    }

    /**
     * get remote video url
     *
     * @param fileName
     * @return
     */
    public static String getRemoteVideoUrl(String fileName) {
        if (!GolukIPCSdk.getInstance().isSdkValid()) {
            return null;
        }
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        if (IpcWifiManager.isT3()) {
            String type = "";
            if (videoFileIsNormalType(fileName)) {
                type = "normal";
            } else if (videoFileIsUrgentType(fileName)) {
                type = "urgent";
            }
            return "http://" + "192.168.62.1" + ":5080/rec/" + type + "/" + fileName;
        }

        if (IpcWifiManager.isT1T2()) {
            String[] names = fileName.split("_");
            if (names != null && names.length > 3) {
                if (names[0].equals("NRM")) {
                    fileName = names[0] + "_" + names[1];
                } else {
                    fileName = names[0] + "_" + names[2];
                }
                return "http://" + "192.168.62.1" + "/api/video?id=" + fileName;
            }
        }

        return null;
    }

    /**
     * get remote image url
     *
     * @param fileName
     * @return
     */
    public static String getRemoteImageUrl(String fileName) {
        if (!GolukIPCSdk.getInstance().isSdkValid()) {
            return null;
        }
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String[] names = fileName.split("_");
        if (names != null && names.length > 3) {
            if (names[0].equals("NRM")) {
                fileName = names[0] + "_" + names[1];
            } else {
                fileName = names[0] + "_" + names[2];
            }
            return "http://" + "192.168.62.1" + "/api/thumb?id=" + fileName;
        }
        return null;
    }

    private static boolean videoFileIsNormalType(String videoName) {
        return !TextUtils.isEmpty(videoName) && videoName.contains("NRM");
    }

    private static boolean videoFileIsUrgentType(String videoName) {
        return !TextUtils.isEmpty(videoName) && videoName.contains("URG");
    }

}
