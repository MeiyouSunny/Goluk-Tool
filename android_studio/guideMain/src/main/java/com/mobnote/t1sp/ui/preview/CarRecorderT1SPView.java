package com.mobnote.t1sp.ui.preview;

import likly.mvp.View;

public interface CarRecorderT1SPView extends View<CarRecorderT1SPPresenter> {

    /* 抓拍视频为前8后8 */
    int CAPTURE_TIME_16_COUNT_TIME = 10;

    void onOpenLoopModeSuccess();

    void onOpenLoopModeFailed();

    void onOpenLoopModeErrorNoSdCard();

    void onCaptureStart();

    void showLoading();

    void hideLoading();

}
