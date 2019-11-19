package com.mobnote.t1sp.ui.preview;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.settings.TimeSettingActivity;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.callback.SettingInfosCallback;
import com.mobnote.t1sp.util.Const;
import com.rd.veuisdk.utils.DateTimeUtils;

import cn.com.tiros.debug.GolukDebugUtils;
import goluk.com.t1s.api.callback.CallbackCmd;
import goluk.com.t1s.api.callback.CallbackSDCardStatus;
import likly.dollar.$;
import likly.mvp.BasePresenter;

public class CarRecorderT1SPPresenterImpl extends BasePresenter<CarRecorderT1SPModel, CarRecorderT1SPView> implements CarRecorderT1SPPresenter {

    @Override
    public void getVideoSettingInfo(final boolean onlySettingInfo) {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getSettingInfoParam(), new SettingInfosCallback() {
            @Override
            public void onGetSettingInfos(SettingInfo settingInfo) {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                //getView().onOpenLoopModeFailed();
            }
        });
    }

    @Override
    public void captureVideo() {
        goluk.com.t1s.api.ApiUtil.checkSDCardStatus(new CallbackSDCardStatus() {

            @Override
            public void onSuccess(int status) {
                if (status == 1) {
                    capture();
                } else {
                    getView().onNoSDCarcChecked();
                }
            }
        });
    }

    private void capture() {
        goluk.com.t1s.api.ApiUtil.captureVideo(new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                getView().onCaptureStart();
            }

            @Override
            public void onFail(int i, int i1) {

            }
        });
    }

    @Override
    public void autoSyncSystemTime() {
        boolean autoSyncn = SettingUtils.getInstance().getBoolean("systemtime", true);
        if (!autoSyncn)
            return;
        final String nowTime = DateTimeUtils.getNowTimeStringSplitWith$();
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.setTimeParam(nowTime), new CommonCallback() {
            @Override
            protected void onSuccess() {
                GolukDebugUtils.e(Const.LOG_TAG, "Sync system time success");
                // 保存时间
                $.config().putLong(TimeSettingActivity.TAG_LAST_SYNC_TIME, System.currentTimeMillis());
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                GolukDebugUtils.e(Const.LOG_TAG, "Sync system time failed");
            }
        });
    }

    @Override
    public void rotateVideo() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.rotateVideoParam(), new CommonCallback() {
            @Override
            public void onStart() {
                getView().showLoading();
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
                getView().hideLoading();
            }
        });
    }

}
