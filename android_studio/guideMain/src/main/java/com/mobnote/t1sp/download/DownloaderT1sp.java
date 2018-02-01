package com.mobnote.t1sp.download;

import java.util.List;

/**
 * T1SP 下载管理
 */
public interface DownloaderT1sp {

    void addDownloadTasks(List<Task> tasks);

    void cancelAllDownloadTask(boolean showCancelMsg);

    boolean isDownloading();

    void destory();

}
