package com.mobnote.t1sp.ui.preview;

import likly.mvp.Presenter;

public interface CarRecorderT1SPPresenter extends Presenter<CarRecorderT1SPModel, CarRecorderT1SPView> {

    /**
     * 获取视频设置信息
     */
    void getVideoSettingInfo();

    /**
     * 设置录制声音开关
     *
     * @param onOff 开/关
     */
    void setRecordSound(boolean onOff);

    /**
     * 抓拍精彩视频
     */
    void captureVideo();

}
