package com.mobnote.t2s.files;

public interface IpcQuery {

    /* 循环影像 */
    int TYPE_NORMAL = 1;
    /* 紧急录像 */
    int TYPE_URGENT = 2;
    /* 精彩视频 */
    int TYPE_CAPTURE = 4;
    /* 一秒一拍 */
    int TYPE_TIMESLAPSE = 8;

    /**
     * 查询循环视频列表
     *
     * @return
     */
    boolean queryNormalVideoList();

    /**
     * 查询紧急视频列表
     *
     * @return
     */
    boolean queryUrgentVideoList();

    /**
     * 查询抓拍视频列表
     *
     * @return
     */
    boolean queryCaptureVideoList();

    /**
     * 查询缩时视频列表
     *
     * @return
     */
    boolean queryTimeslapseVideoList();

    /**
     * 释放
     */
    void onDestory();

}
