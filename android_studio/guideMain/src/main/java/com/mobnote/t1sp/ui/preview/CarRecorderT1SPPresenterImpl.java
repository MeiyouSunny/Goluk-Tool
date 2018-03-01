package com.mobnote.t1sp.ui.preview;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.settings.TimeSettingActivity;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.bean.DeviceMode;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.callback.DeviceModeCallback;
import com.mobnote.t1sp.callback.SettingInfosCallback;
import com.mobnote.t1sp.util.Const;
import com.mobnote.t1sp.util.FileUtil;
import com.rd.veuisdk.utils.DateTimeUtils;

import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;
import likly.dollar.$;
import likly.mvp.BasePresenter;

public class CarRecorderT1SPPresenterImpl extends BasePresenter<CarRecorderT1SPModel, CarRecorderT1SPView> implements CarRecorderT1SPPresenter {

    @Override
    public void getVideoSettingInfo(final boolean onlySettingInfo) {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getSettingInfoParam(), new SettingInfosCallback() {
            @Override
            public void onGetSettingInfos(SettingInfo settingInfo) {
                getView().onGetVideoSettingInfo(settingInfo, onlySettingInfo);
                getLatestTwoVideos();
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                getView().onOpenLoopModeFailed();
            }
        });
    }

    @Override
    public void captureVideo() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.captureParam(false), new CommonCallback() {
            @Override
            protected void onSuccess() {
                getView().onCaptureStart();
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                if (errorCode == 716) {
                    $.toast().text("抓拍是吧,没有SD卡").show();
                    return;
                }
                $.toast().text(R.string.capture_failed).show();
            }
        });
    }

    @Override
    public void getLatestTwoVideos() {
        List<String> videos = FileUtil.getLatestTwoVideosWithWonfulAndUrgent();
        getView().onGetLatestTwoVideos(videos);
    }

    @Override
    public void getDeviceMode() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getCameraModeParam(), new DeviceModeCallback() {
            @Override
            public void onGetDeviceModeInfo(final DeviceMode deviceMode) {
                getView().onGetDeviceModeInfo(deviceMode);
            }

            @Override
            public void onError(Throwable throwable) {
                getView().onExitOtherModeFailed();
            }
        });
    }

    @Override
    public void exitPlaybackMode() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterPlaybackModeParam(false),
                new CommonCallback() {

                    @Override
                    protected void onSuccess() {
                        getView().onExitOtherModeSuccess();
                    }

                    @Override
                    protected void onServerError(int errorCode, String errorMessage) {
                        getView().onExitOtherModeFailed();
                    }
                });

//        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterVideoModeParam(), new CommonCallback() {
//            @Override
//            protected void onSuccess() {
//                $.toast().text(R.string.recovery_to_record).show();
//            }
//
//            @Override
//            protected void onServerError(int errorCode, String errorMessage) {
//                $.toast().text(errorMessage).show();
//            }
//        });

    }

    @Override
    public void exitSetMode() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterOrExitSettingModeParam(false),
                new CommonCallback() {

                    @Override
                    protected void onSuccess() {
                        getView().onExitOtherModeSuccess();
                    }

                    @Override
                    protected void onServerError(int errorCode, String errorMessage) {
                        getView().onExitOtherModeFailed();
                    }
                });
    }

    @Override
    public void openLoopMode() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.openLoopRecordParam(),
                new CommonCallback() {

                    @Override
                    protected void onSuccess() {
                        getView().onOpenLoopModeSuccess();
                    }

                    @Override
                    protected void onServerError(int errorCode, String errorMessage) {
                        getView().onOpenLoopModeFailed();
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
            protected void onSuccess() {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
            }
        });
    }

}
