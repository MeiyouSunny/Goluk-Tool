package com.mobnote.t1sp.callback;

import android.text.TextUtils;

import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.util.StringUtil;

/**
 * 统一接口获取设置参数Callback
 */
public abstract class SettingInfosCallback extends DataCallback {
    // 循环视频质量
    private static final String KEY_VIDEO_RES = "Camera.Menu.VideoRes=";
    // 声音控制
    private static final String KEY_SOUND_RECORD = "Camera.Menu.SoundRecord=";
    // 紧急碰撞感应
    private static final String KEY_GSENSOR = "Camera.Menu.GSensor=";
    // 停车安防模式
    private static final String KEY_PARKING_GUARD = "Camera.Menu.ParkingGuard=";
    // 移动侦测
    private static final String KEY_MTD = "Camera.Menu.MTD=";
    // 关机时间
    private static final String KEY_POWER_OFF_DELAY = "Camera.Menu.PowerOffDelay=";
    // 开机声音
    private static final String KEY_PWR_SOUND = "Camera.Menu.PWRSound=";
    // 拍照提示音
    private static final String KEY_SNAP_SOUND = "Camera.Menu.SnapSound=";
    // 自动旋转
    private static final String KEY_AUTO_ROTATE = "Camera.Menu.Autorotate=";
    // 视频水印
    private static final String KEY_REC_STAMP = "Camera.Menu.RecStamp=";
    // 精彩视频时间
    private static final String KEY_SNAP_TIME = "Camera.Menu.SnapTime=";
    // 存储卡容量
    private static final String KEY_SD_INFO = "Camera.Menu.SDInfo=";
    // 设备信息
    private static final String KEY_DEVICE_INFO = "Camera.Menu.DeviceID=";
    // 安防模式
    private static final String KEY_PK_MODE = "Camera.Menu.PKMode=";
    // 休眠模式
    private static final String KEY_SLEEP_MODE = "Camera.Menu.SleepMode=";
    // 紧急视频声音
    private static final String KEY_EMG_VIDEO_SOUND = "Camera.Menu.LockSound=";
    // 语言
    private static final String KEY_LANGUAGE = "Camera.Menu.Language=";

    @Override
    protected void parseData(String[] datas) {
        if (datas == null || datas.length < 3)
            return;

        SettingInfo settingInfo = new SettingInfo();
        String value = "";
        for (String line : datas) {
            if (line.contains(KEY_VIDEO_RES)) {
                settingInfo.videoRes = line.substring(KEY_VIDEO_RES.length(), line.length());
            } else if (line.contains(KEY_SOUND_RECORD)) {
                settingInfo.soundRecord = parseOnOffValue(line.substring(KEY_SOUND_RECORD.length(), line.length()));
            } else if (line.contains(KEY_GSENSOR)) {
                settingInfo.GSensor = line.substring(KEY_GSENSOR.length(), line.length());
            } else if (line.contains(KEY_PARKING_GUARD)) {
                settingInfo.parkingGuard = !TextUtils.equals(ParamsBuilder.VALUE_OFF, line.substring(KEY_PARKING_GUARD.length(), line.length()));
            } else if (line.contains(KEY_MTD)) {
                settingInfo.MTD = !TextUtils.equals(ParamsBuilder.VALUE_OFF, line.substring(KEY_MTD.length(), line.length()));
            } else if (line.contains(KEY_POWER_OFF_DELAY)) {
                settingInfo.powerOffDelay = line.substring(KEY_POWER_OFF_DELAY.length(), line.length());
            } else if (line.contains(KEY_PWR_SOUND)) {
                settingInfo.powerSound = parseOnOffValue(line.substring(KEY_PWR_SOUND.length(), line.length()));
            } else if (line.contains(KEY_PK_MODE)) {
                settingInfo.pkMode = parseOnOffValue(line.substring(KEY_PK_MODE.length(), line.length()));
            } else if (line.contains(KEY_SLEEP_MODE)) {
                settingInfo.sleepMode = parseOnOffValue(line.substring(KEY_SLEEP_MODE.length(), line.length()));
            } else if (line.contains(KEY_EMG_VIDEO_SOUND)) {
                settingInfo.emgVideoSound = parseOnOffValue(line.substring(KEY_EMG_VIDEO_SOUND.length(), line.length()));
            } else if (line.contains(KEY_SNAP_SOUND)) {
                settingInfo.snapSound = parseOnOffValue(line.substring(KEY_SNAP_SOUND.length(), line.length()));
            } else if (line.contains(KEY_AUTO_ROTATE)) {
                settingInfo.autoRotate = parseOnOffValue(line.substring(KEY_AUTO_ROTATE.length(), line.length()));
            } else if (line.contains(KEY_REC_STAMP)) {
                settingInfo.recStamp = parseOnOffValue(line.substring(KEY_REC_STAMP.length(), line.length()));
            } else if (line.contains(KEY_SNAP_TIME)) {
                settingInfo.captureTime = line.substring(KEY_SNAP_TIME.length(), line.length());
            } else if (line.contains(KEY_SD_INFO)) {
                settingInfo.SDCardInfo = parseSDCardInfo(line.substring(KEY_SD_INFO.length(), line.length()));
            } else if (line.contains(KEY_DEVICE_INFO)) {
                parseVersionInfo(settingInfo, line.substring(KEY_DEVICE_INFO.length(), line.length()));
            } else if (line.contains(KEY_LANGUAGE)) {
                settingInfo.language = line.substring(KEY_LANGUAGE.length(), line.length());
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

    private boolean parseOnOffValue(String value) {
        return !TextUtils.isEmpty(value) && "ON".equals(value);
    }

    /**
     * 解析SDCard容量信息
     */
    private String parseSDCardInfo(String storageInfo) {
        if (storageInfo.contains("MB")) {
            storageInfo = storageInfo.substring(0, storageInfo.indexOf("MB"));
            if (storageInfo.contains("\\")) {
                String[] sizeData = storageInfo.split("\\\\");
                if (sizeData != null && sizeData.length >= 2) {
                    String formatResult = StringUtil.formatFileSize(Long.parseLong(sizeData[1]) - Long.parseLong(sizeData[0]))
                            + "/"
                            + StringUtil.formatFileSize(Long.parseLong(sizeData[1]));

                    return formatResult;
                }
            }
        }

        return "";
    }

    /**
     * 获取设置参数
     *
     * @param settingInfo 设置参数
     */
    public abstract void onGetSettingInfos(SettingInfo settingInfo);

}
