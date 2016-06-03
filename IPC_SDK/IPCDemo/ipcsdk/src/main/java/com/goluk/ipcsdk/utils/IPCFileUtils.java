package com.goluk.ipcsdk.utils;

/**
 * Created by crack on 2016/5/26.
 */
public class IPCFileUtils {

    /** T1设备视频预览地址 */
    private static final String T1_URL_PRE = "rtsp://";
    private static final String T1_URL_END = "/stream1";
    /**
     * Get file size
     * @param fileName
     * @return size of remote file
     * */
    public static int getFileSize(String fileName) {
        return 0;
    }

    /**
     * Get remote file url
     * @param fileName
     * @return file url
     */
    public static String getRemoteFileUrl(String fileName){
        return null;
    }

    /**
     * get rtmp video preview url
     * @return the rtmp url
     */
    public static String getRtmpPreviewUrl(){
        return T1_URL_PRE + "192.168.62.1" + T1_URL_END;
    }
}