package com.mobnote.t1sp.download;

import org.succlz123.okdownload.OkDownloadEnqueueListener;
import org.succlz123.okdownload.OkDownloadError;

/**
 * 下载监听
 */
public class DownloadListener implements OkDownloadEnqueueListener {

    private String downloadPath;

    public DownloadListener(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    @Override
    public void onStart(int id) {

    }

    @Override
    public void onProgress(int progress, long cacheSize, long totalSize) {

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(OkDownloadError error) {

    }
}
