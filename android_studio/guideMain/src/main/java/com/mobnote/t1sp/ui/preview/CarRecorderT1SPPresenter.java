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
     * 自动同步系统时间
     */
    void autoSyncSystemTime();

    /**
     * 旋转预览视频
     */
    void rotateVideo();

}
