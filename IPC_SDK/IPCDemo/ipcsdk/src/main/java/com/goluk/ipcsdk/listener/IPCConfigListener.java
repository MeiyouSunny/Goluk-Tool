package com.goluk.ipcsdk.listener;

/**
 * IPC config callback
 * Created by leege100 on 16/5/26.
 */
public interface IPCConfigListener {

    /**
     *
     * @param success
     */
    public void enableAudeoRecordCallback(boolean success);

    /**
     *
     * @param success
     */
    public void setTimeCallback(boolean success);
}
