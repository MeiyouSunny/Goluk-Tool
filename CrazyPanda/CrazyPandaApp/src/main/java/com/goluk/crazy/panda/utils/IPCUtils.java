package com.goluk.crazy.panda.utils;


import com.goluk.crazy.panda.ipc.base.IPCConstant;

public class IPCUtils {
    public static int adaptIPCNameByWifiName(String wifiName) {
        if (wifiName.startsWith(IPCConstant.IPC_TYPE_CRAZY_PANDA_V1)) {
            return IPCConstant.IPC_MODE_T1;
        }
        return IPCConstant.IPC_MODE_UNKNOWN;
    }
}
