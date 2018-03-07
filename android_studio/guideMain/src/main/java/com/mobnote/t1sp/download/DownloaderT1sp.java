package com.mobnote.t1sp.download;

import java.util.List;

/**
 * T1SP 下载管理
 */
public interface DownloaderT1sp {

    void addDownloadTasks(List<Task> tasks, IDownloadSuccess listener);

    void cancelAllDownloadTask(boolean showCancelMsg);

    boolean isDownloading();

    List<Task> getDownloadList();

    void destory();

    public interface IDownloadSuccess {
        void onVideoDownloadSuccess(String videoName, boolean sucess);
    }

}
