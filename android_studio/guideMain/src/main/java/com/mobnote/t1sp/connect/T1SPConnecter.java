package com.mobnote.t1sp.connect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventWifiState;
import com.mobnote.golukmain.multicast.NetUtil;
import com.mobnote.t1sp.service.T1SPUdpService;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.Const;
import com.mobnote.t1sp.util.TimeSync;
import com.mobnote.wifibind.WifiConnectManager;

import java.util.ArrayList;
import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import goluk.com.t1s.api.callback.CallbackCmd;

/**
 * T1SP 连接管理
 */
public class T1SPConnecter {

    private static T1SPConnecter mInstance;

    private Context mContext;
    // 是否已经连接上
    private boolean mIsConnected;
    // 是否正在连接中
    private boolean mIsConnecing;
    // 回调
    private List<T1SPConntectListener> mListeners;
    /* 为了解决T1SP和其他设备实时录像页面不是同一个页面造成两个录像页面的问题 */
    public Activity mRecordActivity;
    // 结束录像页面时是否需要断开WIFI连接
    private boolean mNeedDisconnectWIFI = true;

    private WifiConnectManager mWifiConnectManager;

    private T1SPConnecter() {
    }

    public void init(Context context) {
        mContext = context;
        mIsConnected = false;
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiConnectManager = new WifiConnectManager(mContext, wifiManager);

        mListeners = new ArrayList<>();
        EventBus.getDefault().register(this);
    }

    public synchronized static T1SPConnecter instance() {
        if (mInstance == null)
            mInstance = new T1SPConnecter();
        return mInstance;
    }

    /**
     * WIFI状态变化广播
     */
    public void onEventMainThread(EventWifiState event) {
        if (EventConfig.WIFI_STATE == event.getOpCode()) {
            if (event.getMsg()) {
                if (NetUtil.isWIFIConnected(mContext) && mWifiConnectManager.isConnectedT1sWifi() && hasListeners()) {
                    // WIFI连接成功,发送连接请求
                    connectToDevice();
                } else {
                    // WIFI未连接
                    mIsConnected = false;
                    mIsConnecing = false;

                    setConnected(false);
                    GolukApplication.getInstance().setIpcLoginState(false);
                    stateCallback(-1);
                    stopUdpService();
                }
            }
//            else {
//                if (!NetUtil.isWifiConnected(mContext)) {
//                    setConnected(false);
//                    GolukApplication.getInstance().setIpcLoginState(false);
//                    stateCallback(-1);
//                    stopUdpService();
//                }
//            }
        }
    }

    /**
     * 连接设备
     */
    public void connectToDevice() {
        if (mIsConnecing)
            return;

        mIsConnecing = true;
        goluk.com.t1s.api.ApiUtil.sendConnectTest(new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                stateCallback(1);

                // T1SP连接成功
                setConnected(true);
                GolukApplication.getInstance().setIpcLoginState(true);
                // 同步时间
                TimeSync timeSync = new TimeSync();
                timeSync.syncTime();

                // 保存设备ID和版本
                //SharedPrefUtil.saveIPCNumber(settingInfo.deviceId);
                //SharedPrefUtil.saveIPCVersion(settingInfo.deviceVersion);
                //SharedPrefUtil.saveIpcModel(IPCControlManager.T2S_SIGN);
            }

            @Override
            public void onFail(int i, int i1) {
                stateCallback(2);
                setConnected(false);
            }
        });
    }

    /**
     * 状态回调
     *
     * @param state -1:断开连接; 0:开始连接; 1:连接成功; 2:连接失败
     */
    private void stateCallback(int state) {
        if (CollectionUtils.isEmpty(mListeners))
            return;
        for (T1SPConntectListener listener : mListeners) {
            if (state == -1) {
                GolukDebugUtils.e(Const.LOG_TAG, "Disconnect T1SP device...");
                listener.onT1SPDisconnected();
            } else if (state == 0) {
                GolukDebugUtils.e(Const.LOG_TAG, "Start connect to T1SP device...");
                listener.onT1SPConnectStart();
            } else if (state == 1) {
                GolukDebugUtils.e(Const.LOG_TAG, "Connected to T1SP device success...");
                listener.onT1SPConnectResult(true);
            } else if (state == 2) {
                GolukDebugUtils.e(Const.LOG_TAG, "Connected to T1SP device failed...");
                listener.onT1SPConnectResult(false);
            }
        }

    }

    public void addListener(T1SPConntectListener listener) {
        if (mListeners != null && listener != null)
            mListeners.add(listener);
    }

    public void removeListener(T1SPConntectListener listener) {
        if (mListeners != null && listener != null)
            mListeners.remove(listener);
    }

    private boolean hasListeners() {
        return mListeners != null && !mListeners.isEmpty();
    }

    public void finishRecordActivity(Class<? extends Activity> activity) {
        if (mRecordActivity == null)
            return;
        if (mRecordActivity.getClass() == activity) {
            mNeedDisconnectWIFI = false;
            mRecordActivity.finish();
        }
    }

    /**
     * 是否已经连上设备
     */
    public boolean isConnected() {
        return mIsConnected;
    }

    public void setConnected(boolean mIsConnected) {
        this.mIsConnected = mIsConnected;
        mIsConnecing = false;
    }

    public void needDisconnectWIFI(boolean needDisconnectWIFI) {
        mNeedDisconnectWIFI = needDisconnectWIFI;
    }

    public boolean needDisconnectWIFI() {
        return mNeedDisconnectWIFI;
    }

    /**
     * 停止Udp监听
     */
    private void stopUdpService() {
        if (mContext == null)
            return;
        Intent serviceIntent = new Intent(mContext, T1SPUdpService.class);
        mContext.stopService(serviceIntent);
    }

}
