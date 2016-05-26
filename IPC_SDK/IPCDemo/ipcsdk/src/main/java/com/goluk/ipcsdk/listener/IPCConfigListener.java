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
    public void callback_enableAudeoRecord(boolean success);

    /**
     *
     * @param success
     */
    public void callback_setTime(boolean success);
}
