package com.goluk.crazy.panda.ipc.base;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by pavkoo on 2016/8/18.
 */

public class IPCConstant {
    public static final String IPC_TYPE_CRAZY_PANDA_V1 = "\"Goluk_T1";
    //T1
    public static final int IPC_MODE_T1 = 1;
    //未知
    public static final int IPC_MODE_UNKNOWN = -1;

    static final int IPC_STATE_DISCONNECT = 0;
    static final int IPC_STATE_CONNECTING = 1;
    static final int IPC_STATE_CONNECTED = 2;
    static final String CONNECT_IPC_IP = "192.168.62.1";
    static final int CONNECT_IPC_IP_PORT = 80;
    static final int IPC_SOCKET_BUFFER_LENGTH = 8192;

    static final int TIMEOUT = 90 * 1000;
    static final int HEART_BEAT_TIMER = 20000;
    static final int RECONNECT_IPC_TIMER = 3000;

    @IntDef({IPC_STATE_DISCONNECT, IPC_STATE_CONNECTING, IPC_STATE_CONNECTED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface IPC_STATE {
    }


    /*
    *----------------------------------------------------------
    * IPC 功能模块定义区
    * ---------------------------------------------------------
     */
    public static int IPC_COMMAND_START_HEART_BEAT = 1;
}
