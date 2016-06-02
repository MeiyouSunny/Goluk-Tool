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
    public void callback_setAudeoRecord(boolean success);

    /**
     *
     * @param enable
     */
    public void callback_getAudeoRecord(boolean enable);

    /**
     *
     * @param success
     */
    public void callback_setTime(boolean success);

    /**
     *
     * @param timestamp
     */
    public void callback_getTime(long timestamp);
}
