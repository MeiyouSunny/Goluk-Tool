package com.mobnote.t1sp.api.setting;

import com.mobnote.t1sp.util.GolukUtils;

import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.bean.SettingInfo;
import goluk.com.t1s.api.callback.CallbackCmd;
import goluk.com.t1s.api.callback.CallbackSDCapacity;
import goluk.com.t1s.api.callback.CallbackSetting;

public class IpcConfigOptionF4 implements IpcConfigOption {

    private IPCConfigListener mIpcConfigListener;

    private SettingInfo mSettingInfo;

    public IpcConfigOptionF4(IPCConfigListener listener) {
        mIpcConfigListener = listener;
    }

    @Override
    public void getAllSettingConfig() {
        ApiUtil.getSettingInfo(new CallbackSetting() {
            @Override
            public void onGetSettingInfo(SettingInfo settingInfo) {
                mSettingInfo = settingInfo;
                callbackValues();
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        // SD卡容量
        getSDCapacity();
    }

    private void callbackValues() {
        if (mIpcConfigListener == null || mSettingInfo == null)
            return;

        mIpcConfigListener.onWatermarkStatusGet(GolukUtils.isSwitchOn(mSettingInfo.watermark));
        mIpcConfigListener.onParkSecurityModeGet(GolukUtils.isSwitchOn(mSettingInfo.parkSecury));
        mIpcConfigListener.onParkSleepModeGet(GolukUtils.isSwitchOn(mSettingInfo.parkSleep));
        mIpcConfigListener.onSoundPowerStatusGet(GolukUtils.isSwitchOn(mSettingInfo.soundPowerOn));
        mIpcConfigListener.onDriveFatigueGet(GolukUtils.isSwitchOn(mSettingInfo.fatigueDriving));
        mIpcConfigListener.onSoundCaptureStatusGet(GolukUtils.isSwitchOn(mSettingInfo.soundCapture));
        mIpcConfigListener.onSoundRecordStatusGet(GolukUtils.isSwitchOn(mSettingInfo.audioRecord));
        mIpcConfigListener.onCollisionSensityGet(mSettingInfo.GSensor);
        mIpcConfigListener.onCaptureVideoQulityGet(mSettingInfo.captureSize);
        mIpcConfigListener.onVideoEncodeConfigGet(mSettingInfo.recordSize);
        mIpcConfigListener.onSoundUrgentStatusGet(GolukUtils.isSwitchOn(mSettingInfo.soundUrgent));
        mIpcConfigListener.onVolumeValueGet(GolukUtils.parseVolumeLevel(mSettingInfo.volumeLevel));
        mIpcConfigListener.onLanguageGet(mSettingInfo.language);
        mIpcConfigListener.onAutoRotateGet(GolukUtils.isSwitchOn(mSettingInfo.autoRotate));

    }

    @Override
    public boolean getIpcTime() {
        return false;
    }

    @Override
    public boolean setIpcTime(long time) {
        return false;
    }

    @Override
    public boolean getParkSleepMode() {
        return false;
    }

    @Override
    public boolean setParkSleepMode(boolean enable) {
        ApiUtil.setParkSleep(enable, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        return true;
    }

    @Override
    public boolean setDriveFatigue(boolean enable) {
        ApiUtil.setFatigueDriving(enable, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        return true;
    }

    @Override
    public boolean getParkSecurityMode() {
        return false;
    }

    @Override
    public boolean setParkSecurityMode(boolean enable) {
        ApiUtil.setParkSecury(enable, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        return true;
    }

    @Override
    public boolean getRecordStatus() {
        return false;
    }

    @Override
    public boolean setRecordStatus(boolean enable) {
        return false;
    }

    @Override
    public boolean getSoundRecordStatus() {
        return false;
    }

    @Override
    public boolean setSoundRecordStatus(final boolean enable) {
        ApiUtil.setSoundRecord(enable, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                startRecord();
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        return true;
    }

    @Override
    public boolean getWatermarkStatus() {
        return false;
    }

    @Override
    public boolean setWatermarkStatus(final boolean enable) {
        ApiUtil.startRecordWithoutVoice(false, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                ApiUtil.setWatermark(enable, new CallbackCmd() {
                    @Override
                    public void onSuccess(int i) {
                        startRecord();
                    }

                    @Override
                    public void onFail(int i, int i1) {
                    }
                });
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        return true;
    }

    private void startRecord() {
        ApiUtil.startRecordWithoutVoice(true, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
    }

    @Override
    public boolean getSoundPowerAndCapture() {
        return false;
    }

    @Override
    public boolean setSoundPowerStatus(boolean enable) {
        ApiUtil.setSoundPower(enable, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        return true;
    }

    @Override
    public boolean setSoundCaptureStatus(boolean enable) {
        ApiUtil.setSoundCapture(enable, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        return true;
    }

    @Override
    public boolean getSoundUrgent() {
        return false;
    }

    @Override
    public boolean setSoundUrgentStatus(boolean enable) {
        ApiUtil.setSoundUrgen(enable, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onSoundUrgentStatusSet(true);
            }

            @Override
            public void onFail(int i, int i1) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onSoundUrgentStatusSet(false);
            }
        });
        return true;
    }

    @Override
    public boolean getVolumeValue() {
        return false;
    }

    @Override
    public boolean setVolumeValue(int value) {
        if (value == 0) {
            value = 3;
        } else if (value == 1) {
            value = 2;
        } else if (value == 2) {
            value = 1;
        }

        ApiUtil.setVolumeLevel(value, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onVolumeValueSet(true);
            }

            @Override
            public void onFail(int i, int i1) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onVolumeValueSet(false);
            }
        });
        return true;
    }

    @Override
    public boolean getCaptureVideoQulity() {
        return false;
    }

    @Override
    public boolean setCaptureVideoQulity(final int index) {
        ApiUtil.startRecordWithoutVoice(false, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                ApiUtil.setCaptureSize(index, new CallbackCmd() {
                    @Override
                    public void onSuccess(int i) {
                        startRecord();
                    }

                    @Override
                    public void onFail(int i, int i1) {
                    }
                });
            }

            @Override
            public void onFail(int i, int i1) {

            }
        });
        return true;
    }

    @Override
    public boolean getCaptureVideoType() {
        return false;
    }

    @Override
    public boolean setCaptureVideoType(int value) {
        return false;
    }

    @Override
    public boolean getCollisionSensity() {
        return false;
    }

    @Override
    public boolean setCollisionSensity(int value) {
        ApiUtil.setGSensor(value, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
        return true;
    }

    @Override
    public boolean getVideoEncodeConfig() {
        return false;
    }

    @Override
    public boolean setVideoEncodeConfig(final int index) {
        ApiUtil.startRecordWithoutVoice(false, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                ApiUtil.setRecordSize(index, new CallbackCmd() {
                    @Override
                    public void onSuccess(int i) {
                        startRecord();
                    }

                    @Override
                    public void onFail(int i, int i1) {
                    }
                });
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });

        return true;
    }

    @Override
    public boolean getSDCapacity() {
        // SD卡容量
        ApiUtil.getSDCapacity(new CallbackSDCapacity() {
            @Override
            public void onSuccess(long totalSize, long freeSize) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onSDCapacityGet(totalSize / 1024 / 1024, freeSize / 1024 / 1024);
            }
        });
        return true;
    }

    @Override
    public boolean formatSD() {
        ApiUtil.startRecordWithoutVoice(false, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                ApiUtil.formatSDCard(new CallbackCmd() {
                    @Override
                    public void onSuccess(int i) {
                        if (mIpcConfigListener != null)
                            mIpcConfigListener.onFormatSDCardResult(true);

                        startRecord();
                    }

                    @Override
                    public void onFail(int i, int i1) {
                        if (mIpcConfigListener != null)
                            mIpcConfigListener.onFormatSDCardResult(false);
                    }
                });
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });

        return true;
    }

    @Override
    public boolean resetFactory() {
        ApiUtil.resetFactory(new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onResetFactoryResult(true);
            }

            @Override
            public void onFail(int i, int i1) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onResetFactoryResult(false);
            }
        });
        return true;
    }

    @Override
    public boolean getTimelapseConfig() {
        return false;
    }

    @Override
    public boolean setTimelapseConfig(boolean enable) {
        return false;
    }

    @Override
    public boolean setLanguage(int type) {
        ApiUtil.setLanguage(type, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onLanguageSet(true);
            }

            @Override
            public void onFail(int i, int i1) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onLanguageSet(false);
            }
        });
        return true;
    }

    @Override
    public boolean setAutoRotate(boolean enable) {
        ApiUtil.setAutoRotate(enable, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onAutoRotateSet(true);
            }

            @Override
            public void onFail(int i, int i1) {
                if (mIpcConfigListener != null)
                    mIpcConfigListener.onAutoRotateSet(false);
            }
        });
        return true;
    }

//    @Override
//    public String[] getMainRecordSizeOptions() {
//        return ConfigUtils.MAIN_RECORD_SIZE_OPTIONS_F4;
//    }
//
//    @Override
//    public String[] getCaptureRecordSizeOptions() {
//        return ConfigUtils.CAPTURE_RECORD_SIZE_OPTIONS_F4;
//    }

}
