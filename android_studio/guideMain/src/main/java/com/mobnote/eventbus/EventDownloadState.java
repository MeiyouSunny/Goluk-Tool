package com.mobnote.eventbus;

import com.mobnote.user.IpcUpdateManage;

/**
 * 固件下载状态Event
 */
public class EventDownloadState {

    public int state;

    public EventDownloadState(int state) {
        this.state = state;
    }

    public boolean isDownloading() {
        return state == IpcUpdateManage.DOWNLOAD_STATUS;
    }

    public boolean isDownloadSuccess() {
        return state == IpcUpdateManage.DOWNLOAD_STATUS_SUCCESS;
    }

    public boolean isDownloadFailed() {
        return state == IpcUpdateManage.DOWNLOAD_STATUS_FAIL;
    }

}
