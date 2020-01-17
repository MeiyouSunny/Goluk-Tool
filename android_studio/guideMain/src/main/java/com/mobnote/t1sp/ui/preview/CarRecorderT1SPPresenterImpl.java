package com.mobnote.t1sp.ui.preview;

import android.content.Context;
import android.util.Log;

import com.mobnote.application.GlobalWindow;
import com.mobnote.eventbus.EventDownloadVideoFinish;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.settings.TimeSettingActivity;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.callback.SettingInfosCallback;
import com.mobnote.t1sp.download2.IpcDownloadListener;
import com.mobnote.t1sp.download2.IpcDownloader;
import com.mobnote.t1sp.download2.IpcDownloaderImpl;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.Const;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.t1sp.util.GolukUtils;
import com.mobnote.t1sp.util.LocalWonderfulVideoQueryTask;
import com.mobnote.t1sp.util.ThumbAsyncTask;
import com.mobnote.t2s.files.IpcFileQueryF4;
import com.mobnote.t2s.files.IpcFileQueryListener;
import com.mobnote.t2s.files.IpcQuery;
import com.rd.veuisdk.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import goluk.com.t1s.api.callback.CallbackCmd;
import goluk.com.t1s.api.callback.CallbackSDCardStatus;
import likly.dollar.$;
import likly.mvp.BasePresenter;

public class CarRecorderT1SPPresenterImpl extends BasePresenter<CarRecorderT1SPModel, CarRecorderT1SPView> implements CarRecorderT1SPPresenter, IpcFileQueryListener {

    IpcQuery mIpcQuery;
    Context mContext;

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

    @Override
    public void queryRecent3WonderfulVideo(Context context) {
        if (mIpcQuery == null)
            mIpcQuery = new IpcFileQueryF4(this, context);
        mContext = context;
        mIpcQuery.queryCaptureVideoList();
    }

    @Override
    public void refreshWonderfulVideos() {
        new LocalWonderfulVideoQueryTask(new LocalWonderfulVideoQueryTask.LocalWonderfulQueryListener() {
            @Override
            public void onWonderfulVideoQueryed(List<VideoInfo> videoInfos) {
                if (!CollectionUtils.isEmpty(videoInfos)) {
                    getView().onRefreshWonderfulVideos(videoInfos);
                }
            }
        }).execute();
    }

    @Override
    public void onNormalVideoListQueryed(ArrayList<VideoInfo> fileList) {
    }

    @Override
    public void onUrgentVideoListQueryed(ArrayList<VideoInfo> fileList) {
    }

    @Override
    public void onCaptureVideoListQueryed(ArrayList<VideoInfo> fileList) {
        // 抓拍视频连接查询结果
        if (CollectionUtils.isEmpty(fileList))
            return;
        // 取最近3个
        final List<VideoInfo> videoInfos = new ArrayList<>();
        if (fileList.size() >= 1 && !FileUtil.isLocalExist(fileList.get(0).filename))
            videoInfos.add(fileList.get(0));
        if (fileList.size() >= 2 && !FileUtil.isLocalExist(fileList.get(1).filename))
            videoInfos.add(fileList.get(1));
        if (fileList.size() >= 3 && !FileUtil.isLocalExist(fileList.get(2).filename))
            videoInfos.add(fileList.get(2));
        if (fileList.size() >= 4 && !FileUtil.isLocalExist(fileList.get(3).filename))
            videoInfos.add(fileList.get(3));
        if (fileList.size() >= 5 && !FileUtil.isLocalExist(fileList.get(4).filename))
            videoInfos.add(fileList.get(4));

        // 开始下载
        final IpcDownloader ipcDownloader = IpcDownloaderImpl.getInstance();
        ipcDownloader.addDownloadFileList(videoInfos);
        ipcDownloader.setListener(new IpcDownloadListener() {
            @Override
            public void onDownloadCountUpdate(int currentDownload, int total) {
                // 更新下载: 当前下载第几个/总个数
                Log.e("IpcDownloader", currentDownload + "/" + total);
                final String showTxt = mContext.getString(R.string.str_video_transfer_ongoing)
                        + currentDownload + mContext.getString(R.string.str_slash) + total;
                if (!GlobalWindow.getInstance().isShow()) {
                    GlobalWindow.getInstance().createVideoUploadWindow(showTxt);
                } else {
                    GlobalWindow.getInstance().updateText(currentDownload, total);
                }
            }

            @Override
            public void onProgressUpdate(String fileName, int progress) {
                // 当前文件下载进度
                Log.e("IpcDownloader", fileName + ": " + progress + "%");
                GlobalWindow.getInstance().refreshPercent(progress);
            }

            @Override
            public void onSingleFileDownloadResult(String fileName, boolean isSuccess, String msg) {
                // 当前文件下载最后状态
                Log.e("IpcDownloader", fileName + " Result:" + isSuccess);

                // 解析本地视频封面
                if (isSuccess) {
                    new ThumbAsyncTask(mContext, null).execute(GolukUtils.getVideoSavePath(fileName));
                }

                refreshWonderfulVideos();
            }

            @Override
            public void onDownloadedComplete(int countSuccess, int countfailed, int countTotal) {
                // 所有文件下载完成
                Log.e("IpcDownloader", "onAllDownloaded");
                if (mContext != null) {
                    $.toast().text(R.string.download_complete).show();
                    GlobalWindow.getInstance().topWindowSucess(mContext.getString(R.string.str_video_transfer_success));
                }
                // 发送本地更新视频Event
                EventBus.getDefault().post(new EventDownloadVideoFinish());
                GlobalWindow.getInstance().dimissGlobalWindow();
            }

            @Override
            public void onSDNoEnoughError(int countSuccess, int countfailed, int countTotal) {

            }

        });
        // 开始下载
        ipcDownloader.start();
    }

    @Override
    public void onTimeslapseVideoListQueryed(ArrayList<VideoInfo> fileList) {
    }

    @Override
    public void onGetVideoListIsEmpty() {
    }

    @Override
    public void onQueryVideoListFailed() {
    }

}
