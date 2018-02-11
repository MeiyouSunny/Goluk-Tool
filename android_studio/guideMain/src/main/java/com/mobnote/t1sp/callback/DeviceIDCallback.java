package com.mobnote.t1sp.callback;

import android.text.TextUtils;

import com.mobnote.t1sp.bean.SettingInfo;

/**
 * 设备ID Callback
 */
public abstract class DeviceIDCallback extends DataCallback {
    // 设备信息
    private static final String KEY_DEVICE_INFO = "Camera.Menu.DeviceID=";

    @Override
    protected void parseData(String[] datas) {
        if (datas == null || datas.length < 3)
            return;

        SettingInfo settingInfo = new SettingInfo();
        for (String line : datas) {
            if (line.contains(KEY_DEVICE_INFO)) {
                parseVersionInfo(settingInfo, line.substring(KEY_DEVICE_INFO.length(), line.length()));
            }
        }

        onGetSettingInfos(settingInfo);
    }

    /**
     * 解析设备型号/ID/版本
     *
     * @param data eg:  EC3DFD3B8000:GOLUK:GOLUK-T1S-01:00112671:0.4
     */
    private void parseVersionInfo(SettingInfo settingInfo, String data) {
        if (settingInfo == null || TextUtils.isEmpty(data))
            return;
        String[] infos = data.split(":");
        // 型号固定为T1SP
        settingInfo.deviceModel = "T1SP";
        settingInfo.deviceId = infos[infos.length - 2];
        settingInfo.deviceVersion = infos[infos.length - 1];

    }

    /**
     * 获取设置参数
     *
     * @param settingInfo 设置参数
     */
    public abstract void onGetSettingInfos(SettingInfo settingInfo);

}
