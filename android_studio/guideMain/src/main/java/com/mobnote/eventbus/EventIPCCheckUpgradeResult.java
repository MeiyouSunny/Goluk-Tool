package com.mobnote.eventbus;

/**
 * Created by pavkoo on 2016/7/18.
 */
public class EventIPCCheckUpgradeResult {
    /**
     * 有新固件包
     */
    public static final int EVENT_RESULT_TYPE_NEW = 1;
    /**
     * 有新固件包，但是不以后更新
     */
    public static final int EVENT_RESULT_TYPE_NEW_DELAY = 2;
    public int ResultType;

    public EventIPCCheckUpgradeResult(int type) {
        ResultType = type;
    }
}
