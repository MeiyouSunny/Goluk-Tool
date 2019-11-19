package com.mobnote.t1sp.file;

import java.util.ArrayList;
import java.util.List;

import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackCmd;

public class IpcFileDelete {

    private IpcFileListener mIpcFileListener;

    private int mNeedDeleteFilesCount, mDeletedFilesCount;
    private CallbackCmd mCallback;

    public IpcFileDelete(IpcFileListener listener) {
        mIpcFileListener = listener;

        mCallback = new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                mDeletedFilesCount++;
                if (mDeletedFilesCount >= mNeedDeleteFilesCount && mIpcFileListener != null) {
                    mDeletedFilesCount = 0;
                    mNeedDeleteFilesCount = 0;
                    mIpcFileListener.onRemoteFileDeleted(true);
                }
            }

            @Override
            public void onFail(int i, int i1) {
                System.out.println("");
            }
        };
    }

    public void deleteRemoteFile(String path) {
        List<String> videoInfos = new ArrayList<>(1);
        videoInfos.add(path);
        deleteRemoteFiles(videoInfos);
    }

    public void deleteRemoteFiles(List<String> paths) {
        mNeedDeleteFilesCount = paths.size();
        for (String path : paths) {
            ApiUtil.deleteRemoteFile(path, mCallback);
        }
    }

}
