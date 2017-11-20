package com.mobnote.t1sp.ui.preview;

import likly.mvp.Presenter;

public interface CarRecorderT1SPPresenter extends Presenter<CarRecorderT1SPModel, CarRecorderT1SPView> {

    /**
     * 获取视频设置信息
     */
    void getVideoSettingInfo();

    /**
     * 抓拍精彩视频
     */
    void captureVideo();

    /**
     * 获取最新2个视频(精彩视频和紧急视频综合)
     */
    void getLatestTwoVideos();

}
