package com.mobnote.golukmain.multicast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.mobnote.util.GolukUtils;
import com.mobnote.wifibind.WifiRsBean;
import com.tencent.bugly.crashreport.CrashReport;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import cn.com.tiros.debug.GolukDebugUtils;

public class NetUtil {

    private static final int GRAM_PORT = 35293;
    public static final int SUCESS = 1;
    public static final int ERROR = 2;
    public static final int TIMEOUT = 3;
    /**
     * 接受数据成功
     */
    private static final int MSG_H_ACCEPT_SUCESS = 4;
    private static final int MSG_H_ACCEPT_ERROR = 6;
    /**
     * 超时
     */
    private static final int MSG_H_TIMEOUT = 7;

    private static final String PRE_CONNECT_SIGN = "Goluk,good";

    private DatagramPacket mPacket = null;
    private byte[] recvbuf = new byte[256];
    private boolean isCanScan = false;
    private DatagramSocket mUdpSocket = null;
    private int mType = 0;
    private static NetUtil mInstance = new NetUtil();
    private IMultiCastFn mFn = null;

    public static NetUtil getInstance() {
        return mInstance;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_H_ACCEPT_SUCESS:
                    cancelTimer();
                    isCanScan = false;
                    WifiRsBean bean = (WifiRsBean) msg.obj;
                    sendData(SUCESS, bean);
                    // 连接成功后，关闭连接
                    cancel();
                    break;
                case MSG_H_ACCEPT_ERROR:
                    cancelTimer();
                    sendData(ERROR, null);
                    GolukDebugUtils.e("", "Error!!!!!!!");
                    cancel();
                    break;
                case MSG_H_TIMEOUT:
                    cancelTimer();
                    cancel();
                    sendData(TIMEOUT, null);
                    GolukDebugUtils.e("", "Error!!!!!!!");
                    GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.4  " + mType + " socket receive data Time Out");
                    try {
                        throw new RuntimeException(mType + " socket receive data time out : 90 s");
                    } catch (Exception e) {
                        CrashReport.postCatchedException(e);
                    }
                    break;
            }
            super.handleMessage(msg);
        }

        ;
    };

    public void setMultiCastListener(IMultiCastFn fn) {
        mFn = fn;
    }

    // 移除指定监听
    public void unRemoveMultiCastListener(IMultiCastFn fn) {
        if (mFn == fn)
            mFn = null;
    }

    private void sendData(int sucess, Object obj) {
        GolukDebugUtils.e("", "MultiCastUtil-----sendData------ip2222222:   " + sucess);
        if (null != mFn) {
            mFn.MultiCaskCallBack(mType, sucess, obj);
        }
    }

    private boolean mIsCancel = false;

    public void cancel() {
        try {
            mIsCancel = true;
            isCanScan = false;
            if (null != mUdpSocket) {
                mUdpSocket.close();
                mUdpSocket = null;
                GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.0.3 " + mType + " close socket");
            }
        } catch (Exception e) {
            GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.0.3 " + mType + " close socket Exception:" + GolukUtils.getExceptionStackString(e));
            e.printStackTrace();
            CrashReport.postCatchedException(e);
        }
    }

    private void startTimer(final int seconds) {
        if (seconds <= 0) {
            return;
        }
        cancelTimer();
        GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.0.2 " + mType + "  timer started:" + String.valueOf(seconds) + " MS");
        mHandler.sendEmptyMessageDelayed(MSG_H_TIMEOUT, seconds);
    }

    private void cancelTimer() {
        if (mHandler.hasMessages(MSG_H_TIMEOUT)) {
            mHandler.removeMessages(MSG_H_TIMEOUT);
            GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.0.2 " + mType + "  timer stop");
        }
    }

    public void findServerIpAddress(int type, final String ssid, final String ip, int seconds) {
        GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4  " + type + " waiting ipc connect hotspot");
        if (null != mUdpSocket) {
            GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.0.1  " + type + "socket exists");
            return;
        }
        mIsCancel = false;
        mType = type;
        GolukDebugUtils.e("", "TestUDP--------findServerIpAddress-----1");
        startTimer(seconds);
        try {
            mUdpSocket = new DatagramSocket(GRAM_PORT);
            GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.1  " + type + " create socket");
            GolukDebugUtils.e("", "TestUDP--------findServerIpAddress-----2");
        } catch (SocketException e) {
            GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.1  " + type + " create socket failed " + GolukUtils.getExceptionStackString(e));
            if (!this.mIsCancel) {
                mHandler.sendEmptyMessage(MSG_H_ACCEPT_ERROR);
            }
            GolukDebugUtils.e("", "TestUDP--------findServerIpAddress-----Exception");
            CrashReport.postCatchedException(e);
            return;
        }

        isCanScan = true;

        GolukDebugUtils.bt("HOTSPOT_CONNECT_LOG_TAG", "waiting ipc connect phone hotspot thread create");
        new Thread(new Runnable() {
            @Override
            public void run() {
                GolukDebugUtils.e("", "+TestUDP--------findServerIpAddress-----4-");
                while (isCanScan) {
                    try {
                        mPacket = new DatagramPacket(recvbuf, 256);
                        GolukDebugUtils.e("", "++TestUDP--------findServerIpAddress-----accept socket Data");
                        GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.2  " + mType + " waiting socket");
                        mUdpSocket.receive(mPacket);
                        GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.3  " + mType + " socket receive data");
                        int length = mPacket.getLength();
                        byte[] data = mPacket.getData();
                        String s = new String(data, 0, length - 1, "GBK");
                        GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.3.1  " + mType + " socket recvbuf " + s);
                        GolukDebugUtils.e("", "++TestUDP--------findServerIpAddress----accept Sucess!!!!!!!!:	" + s);
                        if (s.startsWith(PRE_CONNECT_SIGN)) {
                            GolukDebugUtils.bt("HOTSPOT_CONNECT_LOG_TAG", "socket receive data from ipc");
                            String address2 = mPacket.getAddress().toString();
                            receiveSucess(ssid, address2);
                            GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.4  " + mType + "connected ssid: " + ssid + " ip: " + address2);
                            break;
                        } else {
                            GolukDebugUtils.e("", "+++TestUDP--------findServerIpAddress-----77777 recvbuf1=" + s);
                        }
                    } catch (Exception e) {
                        GolukDebugUtils.e("",
                                "++TestUDP--------findServerIpAddress-------8888888888-ip=  Accept Data Exception ");
                        e.printStackTrace();
                        GolukDebugUtils.bt(GolukDebugUtils.HOTSPOT_CONNECT_LOG_TAG, "4.4  " + mType + " socket receive data exception:" + GolukUtils.getExceptionStackString(e));
                        if (!mIsCancel) {
                            mHandler.sendEmptyMessage(MSG_H_ACCEPT_ERROR);
                            isCanScan = false;
                        }
                        CrashReport.postCatchedException(e);
                        return;
                    }
                    GolukDebugUtils.e("", "++TestUDP--------findServerIpAddress-------99999999999--ip=" + mPacket.getAddress());
                }
            }
        }).start();

    }

    private void receiveSucess(String ssid, String ip) {
        GolukDebugUtils.e("", "+++TestUDP--------receiveSucess-----1111111 recvbuf1=: " + ip);
        if (ip.contains("/")) {
            ip = ip.replace("/", "");
        }
        WifiRsBean bean = new WifiRsBean();
        bean.setIpc_ip(ip);
        bean.setIpc_ssid(ssid);
        Message msg = new Message();
        msg.what = MSG_H_ACCEPT_SUCESS;
        msg.obj = bean;
        mHandler.sendMessage(msg);
    }


    //判断移动数据是否打开
    public static boolean isMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return networkInfo.isAvailable();
        }
        return false;
    }


}
