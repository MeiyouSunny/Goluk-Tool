package com.mobnote.t1sp.api.setting;

public interface IPCConfigListener {

    void onDeviceTimeSet(boolean success);

    void onDeviceTimeGet(long timestamp);

    void onParkSleepModeSet(boolean success);

    void onDriveFatigueSet(boolean success);

    void onParkSleepModeGet(boolean enable);

    void onDriveFatigueGet(boolean enable);

    void onParkSecurityModeSet(boolean success);

    void onParkSecurityModeGet(boolean enable);

    void onRecordStatusGet(boolean enable);

    void onRecordStatusSet(boolean success);

    void onSoundRecordStatusGet(boolean enable);

    void onSoundRecordStatusSet(boolean success);

    void onWatermarkStatusGet(boolean enable);

    void onWatermarkStatusSet(boolean success);

    void onSoundPowerStatusGet(boolean enable);

    void onSoundPowerAndCaptureStatusSet(boolean success);

    void onSoundCaptureStatusGet(boolean enable);

    void onSoundUrgentStatusGet(boolean enable);

    void onSoundUrgentStatusSet(boolean success);

    void onVolumeValueGet(int value);

    void onVolumeValueSet(boolean success);

    void onCaptureVideoQulityGet(int index);

    void onCaptureVideoQulitySet(boolean success);

    void onCaptureVideoTypeGet(int value);

    void onCaptureVideoTypeSet(boolean success);

    void onCollisionSensityGet(int value);

    void onCollisionSensitySet(boolean success);

    void onVideoEncodeConfigGet(int index);

    void onVideoEncodeConfigSet(boolean success);

    void onSDCapacityGet(double total, double free);

    void onFormatSDCardResult(boolean success);

    void onResetFactoryResult(boolean success);

    void onTimeslapseConfigGet(boolean enable);

    void onTimeslapseConfigSet(boolean success);

}
