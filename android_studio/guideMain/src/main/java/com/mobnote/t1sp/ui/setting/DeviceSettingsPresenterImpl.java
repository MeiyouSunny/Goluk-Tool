package com.mobnote.t1sp.ui.setting;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.bean.SettingValue;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.callback.SettingInfosCallback;

import java.util.ArrayList;
import java.util.Map;

import likly.mvp.BasePresenter;

public class DeviceSettingsPresenterImpl extends BasePresenter<DeviceSettingsModel, DeviceSettingsView> implements DeviceSettingsPresenter {

    @Override
    public void enterOrExitSettingMode(boolean isEnter) {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterOrExitSettingModeParam(isEnter), new CommonCallback() {
            @Override
            protected void onSuccess() {
                // 获取参数
                getAllInfo();
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                System.out.print("");
            }
        });
    }

    @Override
    public void getAllInfo() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getSettingInfoParam(), new SettingInfosCallback() {
            @Override
            public void onGetSettingInfos(SettingInfo settingInfo) {
                getView().onGetSettingInfos(settingInfo);
            }

            @Override
            public void onStart() {
                getView().showLoadingDialog();
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
            }

            @Override
            public void onFinish() {
                getView().hideLoadingDialog();
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
        sendSetRequest(ParamsBuilder.setAutoRotateParam(onOff));
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
        sendSetRequest(ParamsBuilder.setRecordSoundParam(onOff));
    }

    @Override
    public void setSelectionSettingValue(int type, String value) {
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
        }

        ApiUtil.apiServiceAit().sendRequest(params, new CommonCallback() {
            @Override
            public void onStart() {
                getView().showLoadingDialog();
            }

            @Override
            protected void onSuccess() {
                getAllInfo();
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
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
                getView().onResetFactory(true);
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                getView().onResetFactory(false);
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
                System.out.print("");
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                Log.e("Setting", errorCode + errorMessage);
            }

            @Override
            public void onFinish() {
                getView().hideLoadingDialog();
            }
        });
    }

    @Override
    public ArrayList<SettingValue> generateSettingValues(Context context, int labelsId, int valuesId, String currentValue) {
        ArrayList<SettingValue> settingValues = new ArrayList<>();
        final Resources resources = context.getResources();
        final String[] labels = resources.getStringArray(labelsId);
        final String[] values = resources.getStringArray(valuesId);

        SettingValue settingValue;
        final int size = labels.length;
        for (int i = 0; i < size; i++) {
            settingValue = new SettingValue(values[i], labels[i]);
            if (!TextUtils.isEmpty(currentValue))
                settingValue.isSelected = (values[i].toLowerCase().contains(currentValue.toLowerCase()));
            settingValues.add(settingValue);
        }

        if (TextUtils.isEmpty(currentValue))
            settingValues.get(0).isSelected = true;

        return settingValues;
    }

    @Override
    public String getSettingLabelByValue(Context context, int labelsId, int valuesId, String value) {
        final Resources resources = context.getResources();
        final String[] labels = resources.getStringArray(labelsId);
        final String[] values = resources.getStringArray(valuesId);

        final int size = labels.length;
        for (int i = 0; i < size; i++) {
            if (values[i].contains(value))
                return labels[i];
        }

        return labels[0];
    }

}
