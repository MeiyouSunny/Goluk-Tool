package com.mobnote.t1sp.download2;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;

import java.util.List;

/**
 * Created by HuangJW on 2019/3/5 10:24.
 * Mail: 499655607@qq.com
 * Powered by Goluk
 */
public interface IpcDownloader {

    /**
     * 添加要下载的视频名称
     *
     * @param videoInfo 视频名称
     */
    void addDownloadFile(VideoInfo videoInfo);

    /**
     * 添加要下载的视频名称列表
     *
     * @param videoInfos 视频名称列表
     */
    void addDownloadFileList(List<VideoInfo> videoInfos);

    /**
     * 设置监听
     *
     * @param listener IpcDownloadListener
     */
    void setListener(IpcDownloadListener listener);

    /**
     * 开始下载
     */
    void start();

    /**
     * 取消下载任务
     */
    void cancel();

}