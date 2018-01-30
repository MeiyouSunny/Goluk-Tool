package com.mobnote.t1sp.callback;

import com.mobnote.t1sp.bean.DeviceMode;

/**
 * 当前模式
 */

public abstract class DeviceModeCallback extends DataCallback {

    private static final String KEY_MODE = "Camera.Preview.MJPEG.status.mode=";
    private static final String KEY_RECORD_STATE = "Camera.Preview.MJPEG.status.record=";

    @Override
    protected void onServerError(int errorCode, String errorMessage) {

    }

    @Override
    protected void parseData(String[] datas) {
        DeviceMode deviceMode = new DeviceMode();
        for (String line : datas) {
            if (line.contains(KEY_MODE)) {
                deviceMode.mode = line.substring(KEY_MODE.length(), line.length());
            } else if (line.contains(KEY_RECORD_STATE)) {
                deviceMode.recordState = line.substring(KEY_RECORD_STATE.length(), line.length());
            }
        }

        onGetDeviceModeInfo(deviceMode);
    }

    public abstract void onGetDeviceModeInfo(DeviceMode deviceMode);

}
