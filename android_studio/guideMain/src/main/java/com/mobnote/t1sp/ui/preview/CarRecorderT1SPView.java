package com.mobnote.t1sp.ui.preview;

import com.mobnote.t1sp.bean.SettingInfo;

import java.util.List;

import likly.mvp.View;

public interface CarRecorderT1SPView extends View<CarRecorderT1SPPresenter> {

    /* 精彩视频抓拍30S */
    int CAPTURE_TIME_30 = 30;
    /* 精彩视频抓拍12S */
    int CAPTURE_TIME_12 = 12;

    void onGetVideoSettingInfo(SettingInfo settingInfo);

    void onSetRecordSoundSuccess(boolean onOff);

    void onCaptureStart();

    void onGetLatestTwoVideos(List<String> videos);

}
