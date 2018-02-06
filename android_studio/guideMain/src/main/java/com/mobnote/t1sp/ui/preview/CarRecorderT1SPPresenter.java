package com.mobnote.t1sp.ui.preview;

import likly.mvp.Presenter;

public interface CarRecorderT1SPPresenter extends Presenter<CarRecorderT1SPModel, CarRecorderT1SPView> {

    /**
     * 获取视频设置信息
     */
    void getVideoSettingInfo(boolean onlySettingInfo);

    /**
     * 抓拍精彩视频
     */
    void captureVideo();

    /**
     * 获取最新2个视频(精彩视频和紧急视频综合)
     */
    void getLatestTwoVideos();

    /**
     * 获取当前设备模式
     */
    void getDeviceMode();

    /**
     * 退出回放模式
     */
    void exitPlaybackMode();

    /**
     * 退出设置模式
     */
    void exitSetMode();

    /**
     * 打开循环录像
     */
    void openLoopMode();

    /**
     * 自动同步系统时间
     */
    void autoSyncSystemTime();

}
