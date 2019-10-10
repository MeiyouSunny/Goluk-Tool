package com.mobnote.t1sp.ui.setting;

import com.mobnote.t1sp.base.ui.LoadingView;

import likly.mvp.View;

public interface DeviceSettingsView extends View<DeviceSettingsPresenter>, LoadingView {

    int TYPE_VIDEO_RES = 1;
    int TYPE_SNAP_TIME = 2;
    int TYPE_GSENSOR = 3;
    int TYPE_PARKING_GUARD = 4;
    int TYPE_MTD = 5;
    int TYPE_POWER_OFF_DELAY = 6;
    int TYPE_LANGUAGE = 7;
    int TYPE_CAPTURE_QULITY = 8;
    int TYPE_VOLUME_LEVEL = 9;

}
