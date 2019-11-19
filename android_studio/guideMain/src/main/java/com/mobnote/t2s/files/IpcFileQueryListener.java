package com.mobnote.t2s.files;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;

import java.util.ArrayList;

/**
 * Created by HuangJW on 2019/3/6 11:10.
 * Mail: 499655607@qq.com
 * Powered by Goluk
 */
public interface IpcFileQueryListener {

    /**
     * 查询返回的一般视频文件列表
     *
     * @param fileList ArrayList<VideoInfo>
     */
    void onNormalVideoListQueryed(ArrayList<VideoInfo> fileList);

    /**
     * 查询返回的紧急视频文件列表
     *
     * @param fileList ArrayList<VideoInfo>
     */
    void onUrgentVideoListQueryed(ArrayList<VideoInfo> fileList);

    /**
     * 查询返回的循环视频文件列表
     *
     * @param fileList ArrayList<VideoInfo>
     */
    void onCaptureVideoListQueryed(ArrayList<VideoInfo> fileList);

    /**
     * 查询返回的缩时视频文件列表
     *
     * @param fileList ArrayList<VideoInfo>
     */
    void onTimeslapseVideoListQueryed(ArrayList<VideoInfo> fileList);

    /**
     * 获取视频列表数据为空
     */
    void onGetVideoListIsEmpty();

    /**
     * 查询视频失败
     */
    void onQueryVideoListFailed();

}
