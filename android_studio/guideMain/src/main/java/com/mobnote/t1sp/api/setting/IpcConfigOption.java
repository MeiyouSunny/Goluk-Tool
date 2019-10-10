package com.mobnote.t1sp.api.setting;

/**
 * IPC设置操作接口
 */
public interface IpcConfigOption {

    void getAllSettingConfig();

    boolean getIpcTime();

    boolean setIpcTime(long time);

    boolean getParkSleepMode();

    boolean setParkSleepMode(boolean enable);

    boolean setDriveFatigue(boolean enable);

    boolean getParkSecurityMode();

    boolean setParkSecurityMode(boolean enable);

    boolean getRecordStatus();

    boolean setRecordStatus(boolean enable);

    boolean getSoundRecordStatus();

    boolean setSoundRecordStatus(boolean enable);

    boolean getWatermarkStatus();

    boolean setWatermarkStatus(boolean enable);

    boolean getSoundPowerAndCapture();

    boolean setSoundPowerStatus(boolean enable);

    boolean setSoundCaptureStatus(boolean enable);

    boolean getSoundUrgent();

    boolean setSoundUrgentStatus(boolean enable);

    boolean getVolumeValue();

    boolean setVolumeValue(int value);

    boolean getCaptureVideoQulity();

    boolean setCaptureVideoQulity(int index);

    boolean getCaptureVideoType();

    boolean setCaptureVideoType(int value);

    boolean getCollisionSensity();

    boolean setCollisionSensity(int value);

    boolean getVideoEncodeConfig();

    boolean setVideoEncodeConfig(int index);

    boolean getSDCapacity();

    boolean formatSD();

    boolean resetFactory();

    boolean getTimelapseConfig();

    boolean setTimelapseConfig(boolean enable);

//    // 获取主录分辨率设置项
//    String[] getMainRecordSizeOptions();
//
//    // 获取抓拍分辨率设置项
//    String[] getCaptureRecordSizeOptions();

}
