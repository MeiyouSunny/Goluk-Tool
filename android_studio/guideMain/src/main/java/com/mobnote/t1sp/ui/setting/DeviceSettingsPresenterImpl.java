package com.mobnote.t1sp.ui.setting;

import android.os.Handler;
import android.os.Looper;

import com.mobnote.golukmain.R;
import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.callback.SettingInfosCallback;

import java.util.Map;

import likly.dollar.$;
import likly.mvp.BasePresenter;

public class DeviceSettingsPresenterImpl extends BasePresenter<DeviceSettingsModel, DeviceSettingsView> implements DeviceSettingsPresenter {

    @Override
    public void enterOrExitSettingMode(boolean isEnter) {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterOrExitSettingModeParam(isEnter), new CommonCallback() {
            @Override
            protected void onSuccess() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 获取参数
                        getAllInfo();
                    }
                }, 300);
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
            }
        });
    }

    @Override
    public void getAllInfo() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getSettingInfoParam(), new SettingInfosCallback() {
            @Override
            public void onGetSettingInfos(SettingInfo settingInfo) {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
            }

        });
    }

    @Override
    public void getSDCardInfo() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getSettingInfoParam(), new SettingInfosCallback() {
            @Override
            public void onGetSettingInfos(SettingInfo settingInfo) {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
            }
        });
    }

    @Override
    public void setPowerSound(boolean onOff) {
        sendSetRequest(ParamsBuilder.setPowerSoundParam(onOff));
    }

    @Override
    public void setCaptureSound(boolean onOff) {
        sendSetRequest(ParamsBuilder.setCaptureSoundParam(onOff));
    }

    @Override
    public void setEmgVideoSound(boolean onOff) {
        sendSetRequest(ParamsBuilder.setEmgVideoSoundParam(onOff));
    }

    @Override
    public void setSleepMode(boolean onOff) {
        sendSetRequest(ParamsBuilder.setSleepModeParam(onOff));
    }

    @Override
    public void setPKMode(boolean onOff) {
        sendSetRequest(ParamsBuilder.setPKModeParam(onOff));
    }

    @Override
    public void setAutoRotate(boolean onOff) {
    }

    @Override
    public void setRecStamp(boolean onOff) {
        sendSetRequest(ParamsBuilder.setRecStampParam(onOff));
    }

    @Override
    public void setParkGuard(boolean onOff) {
        sendSetRequest(ParamsBuilder.setPKModeParam(onOff));
    }

    @Override
    public void setMTD(boolean onOff) {
        sendSetRequest(ParamsBuilder.setMTDParam(!onOff ? ParamsBuilder.VALUE_OFF : ParamsBuilder.VALUE_HIGH));
    }

    @Override
    public void setSoundRecord(boolean onOff) {

    }

    @Override
    public void setSelectionSettingValue(final int type, final String value) {
        Map<String, String> params = null;
        switch (type) {
            case DeviceSettingsView.TYPE_VIDEO_RES:
                params = ParamsBuilder.setVideoClarityParam(value);
                break;
            case DeviceSettingsView.TYPE_SNAP_TIME:
                params = ParamsBuilder.setCaptureTimeParam(value);
                break;
            case DeviceSettingsView.TYPE_GSENSOR:
                params = ParamsBuilder.setGSensorParam(value);
                break;
            case DeviceSettingsView.TYPE_PARKING_GUARD:
                params = ParamsBuilder.setParkingGuardParam(value);
                break;
            case DeviceSettingsView.TYPE_MTD:
                params = ParamsBuilder.setMTDParam(value);
                break;
            case DeviceSettingsView.TYPE_POWER_OFF_DELAY:
                params = ParamsBuilder.setPowerOffDelayParam(value);
                break;
            case DeviceSettingsView.TYPE_LANGUAGE:
                params = ParamsBuilder.setLanguageParam(value);
                break;
        }

        ApiUtil.apiServiceAit().sendRequest(params, new CommonCallback() {
            @Override
            public void onStart() {
                getView().showLoadingDialog();
            }

            @Override
            protected void onSuccess() {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                $.toast().text(R.string.str_carrecoder_setting_failed).show();
            }

            @Override
            public void onFinish() {
                getView().hideLoadingDialog();
            }
        });
    }

    @Override
    public void resetFactory() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.resetFactoryParam(), new CommonCallback() {
            @Override
            protected void onSuccess() {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
            }
        });
    }

    /**
     * 发送设置请求
     *
     * @param params 参数
     */
    private void sendSetRequest(Map<String, String> params) {
        if (params == null || params.size() == 0)
            return;

        ApiUtil.apiServiceAit().sendRequest(params, new CommonCallback() {
            @Override
            public void onStart() {
                getView().showLoadingDialog();
            }

            @Override
            protected void onSuccess() {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                $.toast().text(R.string.str_carrecoder_setting_failed).show();
            }

            @Override
            public void onFinish() {
                getView().hideLoadingDialog();
            }
        });
    }

}
