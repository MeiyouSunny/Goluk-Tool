package com.mobnote.t1sp.listener;

/**
 * 抓拍(照片或视频)信息监听
 */
public interface OnCaptureListener {
    /**
     * 抓拍照片回调
     *
     * @param path 照片路径
     */
    void onCapturePic(String path);

    /**
     * 抓拍视频回调
     *
     * @param path 视频路径
     */
    void onCaptureVideo(String path);

}