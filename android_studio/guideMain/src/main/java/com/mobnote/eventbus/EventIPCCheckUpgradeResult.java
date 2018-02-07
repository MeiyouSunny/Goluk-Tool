package com.mobnote.eventbus;

import com.mobnote.user.IPCInfo;

/**
 * Created by pavkoo on 2016/7/18.
 */
public class EventIPCCheckUpgradeResult {
    /**
     * 有新固件包
     */
    public static final int EVENT_RESULT_TYPE_NEW = 1;
    /**
     * 有新固件包，未下载但是不以后更新
     */
    public static final int EVENT_RESULT_TYPE_NEW_DELAY = 2;
    /**
     * 有新固件包，已下载但是不以后更新
     */
    public static final int EVENT_RESULT_TYPE_NEW_OFFLINE_INSTALL_DELAY = 3;
    public static final int EVENT_RESULT_TYPE_NEW_INSTALL_DELAY = 4;
    public int ResultType;
    public IPCInfo ipcInfo;

    public EventIPCCheckUpgradeResult() {
    }

    public EventIPCCheckUpgradeResult(int type) {
        ResultType = type;
    }

    public EventIPCCheckUpgradeResult(int type, IPCInfo ipcInfo) {
        this(type);
        this.ipcInfo = ipcInfo;
    }
}
