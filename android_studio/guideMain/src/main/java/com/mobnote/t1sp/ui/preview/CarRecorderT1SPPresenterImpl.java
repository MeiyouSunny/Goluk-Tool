package com.mobnote.t1sp.ui.preview;

import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.bean.FileInfo;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.callback.FileListCallback;
import com.mobnote.t1sp.callback.SettingInfosCallback;

import java.util.List;

import likly.dollar.$;
import likly.mvp.BasePresenter;

public class CarRecorderT1SPPresenterImpl extends BasePresenter<CarRecorderT1SPModel, CarRecorderT1SPView> implements CarRecorderT1SPPresenter {

    @Override
    public void getVideoSettingInfo() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getSettingInfoParam(), new SettingInfosCallback() {
            @Override
            public void onGetSettingInfos(SettingInfo settingInfo) {
                getView().onGetVideoSettingInfo(settingInfo);
                //getLatestCaptureVideos();
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
            }
        });
    }

    @Override
    public void setRecordSound(final boolean onOff) {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.setRecordSoundParam(onOff), new CommonCallback() {
            @Override
            protected void onSuccess() {
                getView().onSetRecordSoundSuccess(onOff);
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                $.toast().text(errorMessage).show();
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
                $.toast().text(errorMessage).show();
            }
        });
    }

    @Override
    public void getLatestCaptureVideos() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getFileListParam(ParamsBuilder.FILE_DIR_TYPE_SHARE,
                ParamsBuilder.FILE_TYPE_ALL, "0", "2"), new FileListCallback() {
            @Override
            public void onGetFileList(List<FileInfo> files) {
                if (files == null || files.isEmpty())
                    return;
                getView().onGetLatestCaptureVideos(files);
            }

            @Override
            protected void onSuccess() {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                $.toast().text(errorMessage).show();
            }
        });
    }

}
