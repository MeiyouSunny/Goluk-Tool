package com.goluk.crazy.panda.ipc.base;

/**
 * Created by pavkoo on 2016/8/18.
 */
public class IPCManager {
    private static IPCManager ourInstance = new IPCManager();

    public static IPCManager getInstance() {
        return ourInstance;
    }

    private IPCManager() {
    }
}
