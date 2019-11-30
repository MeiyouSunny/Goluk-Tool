package com.mobnote.t1sp.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackCmd;

public class IpcFileDelete {

    private IpcFileListener mIpcFileListener;

    private CallbackCmd mCallback;

    private List<String> mPaths;
    private String mCurrentPath;

    public IpcFileDelete(IpcFileListener listener) {
        mIpcFileListener = listener;

        mCallback = new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                deleteNext();
            }

            @Override
            public void onFail(int i, int i1) {
                deleteNext();
            }
        };
    }

    public void deleteRemoteFile(String path) {
        List<String> videoInfos = new ArrayList<>(1);
        videoInfos.add(path);
        deleteRemoteFiles(videoInfos);
    }

    public void deleteRemoteFiles(List<String> paths) {
        mPaths = new ArrayList(Arrays.asList(new String[paths.size()]));
        Collections.copy(mPaths, paths);
        deleteNext();
    }

    private void deleteNext() {
        if (mPaths == null || mPaths.isEmpty()) {
            mIpcFileListener.onRemoteFileDeleted(true);
            mPaths = null;
            mCurrentPath = null;
        }

        mCurrentPath = mPaths.remove(0);
        ApiUtil.deleteRemoteFile(mCurrentPath, mCallback);
    }

}
