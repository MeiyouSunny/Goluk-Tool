package com.mobnote.t1sp.bean;

import android.text.TextUtils;

/**
 * 设备当前状态模式
 */

public class DeviceMode {

    private static final String MODE_PREVIEW = "Videomode";
    private static final String MODE_PLAYBACK = "Idlemode";
    private static final String STATE_RECORDING = "Recording";
    private static final String STATE_STANDBY = "Standby";

    public String mode;
    public String recordState;

    /**
     * 是否处于回放模式
     */
    public boolean isInPlaybackMode() {
        return !TextUtils.isEmpty(mode) && TextUtils.equals(mode, MODE_PLAYBACK);
    }

}
