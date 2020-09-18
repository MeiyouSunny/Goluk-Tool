package com.mobnote.t2s.files;

import android.content.Context;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.t2s.utils.GolukIPCUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.bean.FileInfo;
import goluk.com.t1s.api.callback.CallbackFiles;

public class IpcFileQueryF4 implements IpcQuery {

    private IpcFileQueryListener mListener;

    public IpcFileQueryF4(IpcFileQueryListener listener, Context context) {
        mListener = listener;
    }

    @Override
    public boolean queryNormalVideoList() {
        return queryRemoteFiles(TYPE_NORMAL);
    }

    @Override
    public boolean queryUrgentVideoList() {
        return queryRemoteFiles(TYPE_URGENT);
    }

    @Override
    public boolean queryCaptureVideoList() {
        return queryRemoteFiles(TYPE_CAPTURE);
    }

    @Override
    public boolean queryTimeslapseVideoList() {
        return queryRemoteFiles(TYPE_TIMESLAPSE);
    }

    @Override
    public void onDestory() {
    }

    private boolean queryRemoteFiles(final int type) {
        ApiUtil.queryFileList(new CallbackFiles() {
            @Override
            public void onSuccess(List<FileInfo> list) {
                if (list == null)
                    return;
                parseVideos(list, type);
            }

            @Override
            public void onFail() {
                if (mListener != null)
                    mListener.onQueryVideoListFailed();
            }
        });
        return true;
    }

    private void parseVideos(List<FileInfo> list, int type) {
        if (list == null || list.isEmpty()) {
            if (mListener != null)
                mListener.onGetVideoListIsEmpty();
            return;
        }
        // 根据类型筛选
        list = GolukIPCUtils.filterFilesByType(list, type);
        // 转换
        ArrayList<VideoInfo> videos = new ArrayList<>();
        for (FileInfo fileInfo : list) {
            videos.add(GolukIPCUtils.parseF4FileInfo(fileInfo, type));
        }
        // 排序
        Collections.sort(videos,
                new Comparator<VideoInfo>() {
                    @Override
                    public int compare(VideoInfo lhs, VideoInfo rhs) {
                        return rhs.time > lhs.time ? 1 : -1;
                    }
                });

        if (mListener != null) {
            if (type == TYPE_NORMAL) {
                mListener.onNormalVideoListQueryed(videos);
            } else if (type == TYPE_CAPTURE) {
                mListener.onCaptureVideoListQueryed(videos);
            } else if (type == TYPE_URGENT) {
                mListener.onUrgentVideoListQueryed(videos);
            } else if (type == TYPE_TIMESLAPSE) {
                mListener.onTimeslapseVideoListQueryed(videos);
            }
        }
    }

}
