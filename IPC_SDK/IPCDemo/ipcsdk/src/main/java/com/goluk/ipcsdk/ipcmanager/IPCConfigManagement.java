package com.goluk.ipcsdk.ipcmanager;

/**
 * Created by leege100 on 16/5/26.
 */
public class IPCConfigManagement {

    /**
     * enable/disable audio record
     * @see com.goluk.ipcsdk.listener.IPCConfigListener
     * @param isEnable
     * @return
     */
    public boolean enableAudioRecord(boolean isEnable){
        return false;
    }

    /**
     * update goluk carrecorder time
     * @see com.goluk.ipcsdk.listener.IPCConfigListener
     * @param timeStamp timestamp in seconds
     * @return
     */
    public boolean setTime(long timeStamp){
        return false;
    }
}
