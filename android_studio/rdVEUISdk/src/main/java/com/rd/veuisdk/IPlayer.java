package com.rd.veuisdk;

/**
 * 播放器的基本功能
 */
public interface IPlayer {
    /**
     * 是否播放中...
     *
     * @return
     */
    boolean isPlaying();

    /**
     * 开始播放
     */
    void start();

    /**
     * 暂停预览
     */
    void pause();

    /**
     * 跳转到指定时间点
     *
     * @param msec 单位：毫秒
     */
    void seekTo(int msec);


    /**
     * @return 获取播放持续时间(ms)
     */
    int getDuration();

    /**
     * @return 获取当前播放器的时间点 单位：毫秒
     */
    int getCurrentPosition();
}
