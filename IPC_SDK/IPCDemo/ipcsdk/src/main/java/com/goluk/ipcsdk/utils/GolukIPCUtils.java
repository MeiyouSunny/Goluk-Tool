package com.goluk.ipcsdk.utils;

import android.text.TextUtils;

import com.goluk.ipcsdk.main.GolukIPCSdk;

/**
 * Created by leege100 on 16/6/7.
 */
public class GolukIPCUtils {
    /**
     * get rtmp video preview url
     * @return the rtmp url
     */
    public static String getRtmpPreviewUrl(){
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return null;
        }
        return "rtsp://" + "192.168.62.1" + "/stream1";

    }

    /**
     * get remote video url
     * @param fileName
     * @return
     */
    public static String getRemoteVideoUrl(String fileName){
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return null;
        }
        if(TextUtils.isEmpty(fileName)){
            return null;
        }
        String[] names = fileName.split("_");
        if (names != null && names.length > 3) {
            if (names[0].equals("NRM")) {
                fileName = names[0] + "_" + names[1];
            } else {
                fileName = names[0] + "_" + names[2];
            }
            return "http://" + "192.168.62.1" + "/api/video?id=" + fileName;
        }
        return null;
    }

    /**
     * get remote image url
     * @param fileName
     * @return
     */
    public static String getRemoteImageUrl(String fileName){
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return null;
        }
        if(TextUtils.isEmpty(fileName)){
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
}
