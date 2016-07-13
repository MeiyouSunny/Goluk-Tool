package com.mobnote.golukmain.multicast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.mobnote.wifibind.WifiRsBean;

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
                    GolukDebugUtils.bt("HOTSPOT_CONNECT_LOG_TAG", "socket receive data Time Out");
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimer(final int seconds) {
        if (seconds <= 0) {
            return;
        }
        cancelTimer();
        mHandler.sendEmptyMessageDelayed(MSG_H_TIMEOUT, seconds);
    }

    private void cancelTimer() {
        if (mHandler.hasMessages(MSG_H_TIMEOUT)) {
            mHandler.removeMessages(MSG_H_TIMEOUT);
        }
    }

    public void findServerIpAddress(int type, final String ssid, final String ip, int seconds) {
        if (null != mUdpSocket) {
            return;
        }
        mIsCancel = false;
        mType = type;
        GolukDebugUtils.e("", "TestUDP--------findServerIpAddress-----1");
        startTimer(seconds);
        try {
            mUdpSocket = new DatagramSocket(GRAM_PORT);
            GolukDebugUtils.e("", "TestUDP--------findServerIpAddress-----2");
        } catch (SocketException e) {
            if (!this.mIsCancel) {
                mHandler.sendEmptyMessage(MSG_H_ACCEPT_ERROR);
            }

            GolukDebugUtils.e("", "TestUDP--------findServerIpAddress-----Exception");
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
                        GolukDebugUtils.bt("HOTSPOT_CONNECT_LOG_TAG", "waiting socket");
                        mUdpSocket.receive(mPacket);
                        GolukDebugUtils.bt("HOTSPOT_CONNECT_LOG_TAG", "socket receive data");
                        int length = mPacket.getLength();
                        byte[] data = mPacket.getData();
                        String s = new String(data, 0, length - 1, "GBK");

                        GolukDebugUtils.e("", "++TestUDP--------findServerIpAddress----accept Sucess!!!!!!!!:	" + s);

                        if (s.startsWith(PRE_CONNECT_SIGN)) {
                            GolukDebugUtils.bt("HOTSPOT_CONNECT_LOG_TAG", "socket receive data from ipc");
                            String address2 = mPacket.getAddress().toString();
                            receiveSucess(ssid, address2);
                            break;
                        } else {
                            GolukDebugUtils.e("", "+++TestUDP--------findServerIpAddress-----77777 recvbuf1=" + s);
                        }
                    } catch (Exception e) {
                        GolukDebugUtils.e("",
                                "++TestUDP--------findServerIpAddress-------8888888888-ip=  Accept Data Exception ");
                        e.printStackTrace();
                        GolukDebugUtils.bt("HOTSPOT_CONNECT_LOG_TAG", "socket receive data exception");
                        if (!mIsCancel) {
                            mHandler.sendEmptyMessage(MSG_H_ACCEPT_ERROR);
                            isCanScan = false;
                        }

                        return;
                    }
                    GolukDebugUtils.e("",
                            "++TestUDP--------findServerIpAddress-------99999999999--ip=" + mPacket.getAddress());
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

}
