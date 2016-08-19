package com.goluk.crazy.panda.ipc.base;


public interface IIPCPushServiceCallBack {
    void onExecuted(int command, String param1, String param2);
}
