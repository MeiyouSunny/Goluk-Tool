package com.goluk.crazy.panda.ipc.base;

import android.os.Handler;
import android.os.Message;

import com.goluk.crazy.panda.utils.IPCUtils;

import java.lang.ref.WeakReference;

import static com.goluk.crazy.panda.ipc.base.IPCConstant.HEART_BEAT_TIMER;

/**
 * Created by pavkoo on 2016/8/22.
 */
public class IPCManager {
    private static final int MESSAGE_HEART_BEAT = 1;
    private static final int MESSAGE_HEART_BEAT_TIME_OUT = -1;
    private boolean ipcBinded;
    private String mIPCName;

    private IPCManagerHandler handler;

    private static IPCManager ourInstance = new IPCManager();

    public static IPCManager getInstance() {
        return ourInstance;
    }

    private IPCManager() {
        handler = new IPCManagerHandler(this);
    }

    public boolean bind(String ssID) {
        ipcBinded = false;
        mIPCName = "";
        int mode = IPCUtils.adaptIPCNameByWifiName(ssID);
        if (mode == IPCConstant.IPC_MODE_T1) {
            mIPCName = ssID;
            ipcBinded = true;
            startHeartBeat();
        }
        return ipcBinded;
    }

    public boolean isIpcBinded() {
        return ipcBinded;
    }

    private void startHeartBeat() {

    }

//    public void executeCommand(int comm, String params,) {
//
//    }

    private void handlerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_HEART_BEAT:
                handler.removeMessages(MESSAGE_HEART_BEAT_TIME_OUT);
                handler.sendEmptyMessageDelayed(MESSAGE_HEART_BEAT_TIME_OUT, HEART_BEAT_TIMER);
                break;
        }
    }

    private static class IPCManagerHandler extends Handler {
        private final WeakReference<IPCManager> nManager;

        IPCManagerHandler(IPCManager manager) {
            nManager = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            IPCManager tempManager = nManager.get();
            if (tempManager == null) {
                return;
            }
            tempManager.handlerMessage(msg);
        }
    }

}
