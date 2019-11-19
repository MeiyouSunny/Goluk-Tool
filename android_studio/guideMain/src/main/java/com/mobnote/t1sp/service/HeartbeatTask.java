package com.mobnote.t1sp.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.util.Const;

import java.util.Map;

import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 心跳任务
 * 小白在进入特定的模式,需要发送心跳位置当前开模式,否则会自动退出当前模式
 */
public class HeartbeatTask {

    // 间隔时间 12s
    private static final int DELAY_TIME = 10 * 1000;
    // 消息类型
    private static final int MSG_TYPE_HEARTBEAT = 0;

    /* 设置模式 */
    public static final int MODE_TYPE_SETTING = 1;
    /* 回放模式 */
    public static final int MODE_TYPE_PLAYBACK = 2;

    private HandlerThread mHeartbeatThread;
    private Handler mHandler;
    private int mModeType;
    private Map<String, String> mParams;

    public HeartbeatTask(int modeType) {
        this.mModeType = modeType;
        init();
    }

    private void init() {
        mHeartbeatThread = new HandlerThread("Hearbeat Thread");
        mHeartbeatThread.start();

        mHandler = new Handler(mHeartbeatThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                sendHeartbeat();
            }
        };

        if (mModeType == MODE_TYPE_SETTING) {
            mParams = ParamsBuilder.sendHeartbeatSettingModeParam();
        } else if (mModeType == MODE_TYPE_PLAYBACK) {
            mParams = ParamsBuilder.sendHeartbeatPlaybackModeParam();
        }
    }

    public void start() {
        mHandler.sendEmptyMessageDelayed(MSG_TYPE_HEARTBEAT, DELAY_TIME);
    }

    public void stop() {
        mHandler.removeMessages(MSG_TYPE_HEARTBEAT);
        mHeartbeatThread.quit();
    }

    /**
     * 发送设置心跳
     */
    private void sendHeartbeat() {
        GolukDebugUtils.e(Const.LOG_TAG, "Send hearbeat");
        ApiUtil.apiServiceAit().sendRequest(mParams, new CommonCallback() {
            @Override
            protected void onSuccess() {
                GolukDebugUtils.e(Const.LOG_TAG, "Receive hearbeat success");
                mHandler.sendEmptyMessageDelayed(MSG_TYPE_HEARTBEAT, DELAY_TIME);
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                GolukDebugUtils.e(Const.LOG_TAG, "Receive hearbeat failed");
                mHandler.sendEmptyMessageDelayed(MSG_TYPE_HEARTBEAT, 5000);
            }
        });
    }

}
