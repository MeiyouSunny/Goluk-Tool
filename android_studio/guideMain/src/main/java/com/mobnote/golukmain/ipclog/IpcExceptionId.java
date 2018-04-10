package com.mobnote.golukmain.ipclog;

/**
 * 记录Ipc最后请求的Exception ID
 */
public class IpcExceptionId {

    public String ipcId;
    public int lastExceptionId;

    public IpcExceptionId() {
    }

    public IpcExceptionId(String ipcId, int lastExceptionId) {
        this.ipcId = ipcId;
        this.lastExceptionId = lastExceptionId;
    }

}
