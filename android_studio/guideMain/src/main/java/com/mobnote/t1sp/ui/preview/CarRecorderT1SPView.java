package com.mobnote.t1sp.ui.preview;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;

import java.util.List;

import likly.mvp.View;

public interface CarRecorderT1SPView extends View<CarRecorderT1SPPresenter> {

    /* 抓拍视频为前8后8 */
    int CAPTURE_TIME_16_COUNT_TIME = 10;

    void onOpenLoopModeSuccess();

    void onOpenLoopModeFailed();

    void onOpenLoopModeErrorNoSdCard();

    void onCaptureStart();

    void onNoSDCarcChecked();

    void showLoading();

    void hideLoading();

    void onRefreshWonderfulVideos(List<VideoInfo> videoInfos);

}
