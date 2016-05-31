package com.goluk.ipcsdk.utils;

/**
 * Created by hanzheng on 2016/5/30.
 */
public class IPCConnectState {
    private boolean state = false;
    private static IPCConnectState connectState;
    public IPCConnectState (){}

    public static IPCConnectState getConnectState(){
        if(connectState == null){
            connectState = new IPCConnectState();
        }
        return connectState;
    }

    public void setState(boolean result){
        state = result;
    }

    public boolean getState(){
        return state;
    }
}
