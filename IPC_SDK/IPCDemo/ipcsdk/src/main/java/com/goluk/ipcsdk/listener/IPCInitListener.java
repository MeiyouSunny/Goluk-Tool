package com.goluk.ipcsdk.listener;

/**
 * Created by leege100 on 16/6/5.
 */
public interface IPCInitListener {
    /**
     * the callback of initSDK
     *
     * @param isSuccess
     * @param msg
     */
    public void initCallback(boolean isSuccess, String msg);
}
