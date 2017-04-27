package com.mobnote.golukmain.livevideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.golukmain.carrecorder.PreferencesReader;
import com.mobnote.golukmain.carrecorder.RecorderMsgReceiverBase;
import com.mobnote.golukmain.livevideo.bean.StartLiveBean;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.ResultConstants;

public class LiveOperateCarrecord implements ILiveOperateFn {

    private Context mContext = null;
    private String liveVid = null;
    /**
     * 直播开启标志
     */
    private boolean isStartLive = false;
    private boolean isSucessBind = false;
    private ILiveFnAdapter mListener = null;
    private boolean isStart = false;
    private int mZhugeErrorCode = 0;

    public LiveOperateCarrecord(Context context, ILiveFnAdapter listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public boolean startLive(final StartLiveBean bean) {
        GolukDebugUtils.e("", "newlive-----LiveOperateCarrecord-----startLive :11 ");
        if (isStartLive) {
            return true;
        }
        GolukDebugUtils.e("", "newlive-----LiveOperateCarrecord-----startLive :22 ");
        if (CarRecorderManager.isRTSPLiving()) {
            // 正在直播，不可以开始
            // liveUploadVideoFailed();
            sendResult(ILiveFnAdapter.STATE_FAILED);
            GolukDebugUtils.e("", "newlive-----LiveOperateCarrecord-----startLive :failed");
            return false;
        }
        try {
            SharedPreferences sp = mContext.getSharedPreferences("CarRecorderPreferaces", Context.MODE_PRIVATE);
            sp.edit().putString("url_live", bean.url).apply();
            sp.edit().commit();
            CarRecorderManager.updateLiveConfiguration(new PreferencesReader(mContext, false).getConfig());
            CarRecorderManager.setLiveMute(bean.isVoice);
            CarRecorderManager.startRTSPLive();
            isStartLive = true;
        } catch (RecorderStateException e) {
            e.printStackTrace();
            // liveUploadVideoFailed();
            isStart = false;
            sendResult(ILiveFnAdapter.STATE_FAILED);
        }
        return false;
    }

    @Override
    public void stopLive() {
        try {
            if (isStart) {
                isStartLive = false;
                CarRecorderManager.stopRTSPLive();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResult(int state) {
        if (null != mListener) {
            mListener.Live_CallBack(state);
        }
    }

    @Override
    public void onStart() {
        try {
            if (!isStart) {
                isStart = true;
                CarRecorderManager.onStartRTSP(mContext);
            }
        } catch (RecorderStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        if (!isSucessBind) {
            try {
                mContext.registerReceiver(managerReceiver, new IntentFilter(CarRecorderManager.ACTION_RECORDER_MESSAGE));
                isSucessBind = true;
            }catch (Exception ex){
            }
        }
    }

    /**
     * 响应视频Manager消息
     */
    private BroadcastReceiver managerReceiver = new RecorderMsgReceiverBase() {
        @Override
        public void onManagerBind(Context context, int nResult, String strResultInfo) {
        }

        public void onLiveRecordBegin(Context context, int nResult, String strResultInfo) {
            if (nResult >= ResultConstants.SUCCESS) {
                isStart = true;
                sendResult(ILiveFnAdapter.STATE_SUCCESS);
            } else {
                //TODO 异常退出时记录错误码
                mZhugeErrorCode = nResult;
                // 视频录制上传失败
                // liveUploadVideoFailed();
                isStart = false;
                sendResult(ILiveFnAdapter.STATE_FAILED);
            }
        }

        @Override
        public void onLiveRecordFailed(Context context, int nResult, String strResultInfo) {
            isStart = false;
            sendResult(ILiveFnAdapter.STATE_FAILED);
            // liveUploadVideoFailed();
            //TODO 异常退出时记录错误码
            mZhugeErrorCode = nResult;
        }
    };

    @Override
    public void exit() {
        if (isSucessBind) {
            mContext.unregisterReceiver(managerReceiver);
            isSucessBind = false;
        }
    }

    @Override
    public boolean liveState() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getZhugeErrorCode() {
        //直播异常退出时错误码
        return mZhugeErrorCode;
    }

}
