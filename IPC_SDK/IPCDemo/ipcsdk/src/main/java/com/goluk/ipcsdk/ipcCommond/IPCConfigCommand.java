package com.goluk.ipcsdk.ipcCommond;

import android.content.Context;

import com.goluk.ipcsdk.bean.BaseIPCCommand;
import com.goluk.ipcsdk.listener.IPCConfigListener;

/**
 * Created by leege100 on 16/5/26.
 */
public class IPCConfigCommand extends BaseIPCCommand{

    /**
     *
     * @param listener
     */
    public IPCConfigCommand(IPCConfigListener listener,Context cxt){
        super(cxt);

    }

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

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {

    }
}
