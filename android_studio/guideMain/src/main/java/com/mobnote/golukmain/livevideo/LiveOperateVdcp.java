package com.mobnote.golukmain.livevideo;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.livevideo.bean.StartLiveBean;
import com.mobnote.util.GolukFastJsonUtil;
import com.umeng.socialize.sina.helper.Base64;

public class LiveOperateVdcp implements ILiveOperateFn {

    private final int TIMER_OUT = 20 * 1000;
    private boolean isSuccess = false;
    private ILiveFnAdapter mListener = null;
    private Timer mTimer = null;
    /**
     * 直播是否成功过
     */
    public boolean isSuccessed = false;

    Handler mHanlder = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (100 == msg.what) {
                GolukDebugUtils.e("", "newlive-----LiveOperateVdcp-----timeout:  ");
                isSuccess = false;
                sendResult(ILiveFnAdapter.STATE_FAILED);
            }
            super.handleMessage(msg);
        }
    };

    public LiveOperateVdcp(ILiveFnAdapter listener) {
        GolukApplication.getInstance().mLiveOperater = this;
        mListener = listener;
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                mHanlder.sendEmptyMessage(100);
            }

        }, TIMER_OUT);

    }

    private void cancelTimer() {
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public boolean startLive(StartLiveBean bean) {
        if (bean == null || TextUtils.isEmpty(bean.url)) {
            return false;
        }
        GolukDebugUtils.e("", "newlive-----LiveOperateVdcp-----startLive  " + bean.url);
        bean.url = String.valueOf(Base64.encode(bean.url.getBytes()));
        String jsonData = GolukFastJsonUtil.setParseObj(bean);
        GolukDebugUtils.e("", "newlive-----LiveOperateVdcp-----startLive  jsonData" + jsonData);
        boolean is = GolukApplication.getInstance().mIPCControlManager.startLive(jsonData);
        if (!is) {
            isSuccess = false;
            sendResult(ILiveFnAdapter.STATE_FAILED);
        }
        GolukDebugUtils.e("", "newlive-----LiveOperateVdcp-----startLive isSuccess: " + is);
        return isSuccess;
    }

    @Override
    public void stopLive() {
        GolukApplication.getInstance().mIPCControlManager.stopLive();
    }

    public void sendResult(int state) {
        if (null != mListener) {
            mListener.Live_CallBack(state);
        }
    }

    private void CallBack_commPush(int param1, Object param2) {
        try {
            VdcpLiveBean bean = GolukFastJsonUtil.getParseObj((String) param2, VdcpLiveBean.class);
            // GolukDebugUtils.e("",
            // "newlive-----LiveOperateVdcp-----CallBack_Ipc content:  " +
            // bean.content + "  top:"
            // + bean.topic);
            final String content = bean.content.trim();
            if ("begin".equals(content)) {

            } else if ("connect".equals(content)) {

            } else if ("sending".equals(content)) {
                isSuccessed = true;
                if (!isSuccess) {
                    isSuccess = true;
                    sendResult(ILiveFnAdapter.STATE_SUCCESS);
                } else {
                    // 发送中...
                }
                this.cancelTimer();
                this.startTimer();
            } else if ("disconnect".equals(content)) {
                isSuccess = false;
                sendResult(ILiveFnAdapter.STATE_FAILED);
            } else if ("retrying".equals(content)) {

            } else if ("timeout".equals(content)) {
                sendResult(ILiveFnAdapter.STATE_TIME_END);
            } else if ("stop".equals(content)) {

            }
        } catch (Exception e) {
            GolukDebugUtils.e("", "newlive-----LiveOperateVdcp-----CallBack_Ipc Exception:  ");
        }
    }

    private void CallBack_LiveStart(int param1, Object param2) {
        GolukDebugUtils.e("", "newlive-----GolukApplication----CallBack_LiveStart----liveStart:  param1: " + param1);
        if (IPCManagerFn.RESULE_SUCESS != param1) {
            // 请求失败
            isSuccess = false;
            sendResult(ILiveFnAdapter.STATE_FAILED);
            return;
        }
    }

    public void CallBack_Ipc(int msg, int param1, Object param2) {
        switch (msg) {
            case IPCManagerFn.IPC_VDCP_Msg_LiveStart:
                CallBack_LiveStart(param1, param2);
                break;
            case IPCManagerFn.IPC_VDCP_Msg_PushEvent_Comm:
                CallBack_commPush(param1, param2);
                break;
        }
    }

    @Override
    public void exit() {
        cancelTimer();
        GolukApplication.getInstance().mLiveOperater = null;
        GolukApplication.getInstance().mIPCControlManager.stopLive();
    }

    @Override
    public void onStart() {

    }

    @Override
    public boolean liveState() {
        return isSuccessed;
    }

    @Override
    public int getZhugeErrorCode() {
        return 0;
    }

}
