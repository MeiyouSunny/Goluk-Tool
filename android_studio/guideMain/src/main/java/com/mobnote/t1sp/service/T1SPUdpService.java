package com.mobnote.t1sp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.mobnote.t1sp.listener.OnCaptureListener;
import com.mobnote.t1sp.listener.OnSettingsListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import static com.mobnote.t1sp.service.T1SPUdpService.UDPThread.parseMediaPath;

/**
 * 小白UDP监听服务
 * 监听请求异步UDP方式回调结果
 * <p>
 * <p>
 * 要做两个功能
 * 1.链接成功之后,开始发心跳包
 * 有三种模式 a1:预览模式, a2:设置模式, a3: playback 模式
 * <p>
 * 当wifii链接上的时候,且
 * 只有在a2,a3模式下,才会发心跳包
 * 当wifi断开的时候,不再发心跳包
 * 2.只要链接成功之后,就会一直打开 udp监听
 * 除非 wifi断开,那么 udp 断开
 * <p>
 * 3.进入预览界面启动service,当退出app的时候,service 退出
 * 预览--->service----> 启动定时器----->判断状态---->心跳包------>
 * <p>
 * 预览--->service----> 启动udp监听----->其他界面,启动
 */
public class T1SPUdpService extends Service {

    public static boolean alive = true;
    private UDPThread mUdp;
    private Timer mTimer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alive = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onCreate() {
        if (mUdp != null) {
            setAlive(false);
            // mUdpThread.setUdpListener(null);
            mUdp.interrupt();
            mUdp = null;
            mUdp = new UDPThread();
            setAlive(true);
            mUdp.start();
        } else {
            mUdp = new UDPThread();
            setAlive(true);
            mUdp.start();
        }

        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimer != null) {
            mTimer.schedule(new RefreshTask(), 0, 9000);
        }

    }

    class RefreshTask extends TimerTask {
        @Override
        public void run() {
            if (!alive) {
                mUdp = null;
                mUdp = new UDPThread();
                setAlive(true);
                mUdp.start();
            }

        }

    }

    public static class UDPThread extends Thread {
        // Action tag
        public final static String ACTION_TAG = "action=";
        // 判断返回状态
        public final static String ACTION_STUTAS = "status=";

        // Udp协议监听
        public final static String ACTION_CAPTURE_PIC = "capture_pic";
        public final static String ACTION_CAPTURE_VIDEO = "capture_video";
        public final static String ACTION_LOCK_VIDEO = "lock_video";
        public final static String ACTION_SD_FORMAT = "sd_format";
        public final static String ACTION_VENDOR_INFO = "vendor_info";
        public final static String ACTION_UPDATE_FW = "update_fw";

        // 监听返回状态
        public final static String STATIC_SUCCESS = "0";
        public final static String SD_FORM_OK = "FORMAT_SD_DONE";
        public final static String STATIC_FAIL = "1";
        // 返回路径
        public final static String MEDIA_PATH = "path=";

        // UDP端口
        private final int port = 49142;
        private DatagramSocket socket = null;
        private byte[] buffer = new byte[4096];
        private DatagramPacket packet;
        private final String listenIP = "0.0.0.0";

        private int ticket;
        private int utime;

        public UDPThread() {
            sendBroad();
        }

        public void sendBroad() {
            if (socket != null) return;
            try {
                packet = new DatagramPacket(buffer, buffer.length);
                socket = new DatagramSocket(port, InetAddress.getByName(listenIP));
                socket.setBroadcast(true);
                //socket.setSoTimeout(5000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void finalize() throws Throwable {
            alive = false;
            if (socket != null) {
                socket.close();
            }
            socket = null;
        }

        @Override
        public void run() {
            super.run();
            try {
                while (alive) {
                    try {
                        if (socket == null) {
                            packet = new DatagramPacket(buffer, buffer.length);
                            socket = new DatagramSocket(port, InetAddress.getByName(listenIP));
                        }
                        packet.setLength(buffer.length);
                        socket.receive(packet);
                        String data = new String(packet.getData(), 0, packet.getLength());
                        if (verifyDataValid(data)) {
                            Message msg = mUIHandler.obtainMessage(0, data);
                            msg.sendToTarget();
                        }
                    } catch (java.io.InterruptedIOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException ex) {
                sendBroad();

            } finally {
                if (socket != null)
                    socket.close();
                socket = null;
            }

        }

        /**
         * 验证数据长度是否有效
         */
        private boolean verifyDataValid(String data) {
            String info[];
            int sum, i;
            char[] achar;

            try {
                info = data.split("CHKSUM=");
                sum = Integer.parseInt(info[1]);
                achar = info[0].toCharArray();
                for (i = 0; i < achar.length; i++)
                    sum -= achar[i];
            } catch (Exception e) {
                return false;
            }
            return sum == 0;
        }

        /**
         * 解析回调结果,并回调给对应Listener:
         * 文件路径 或者 操作(格式化SD卡、固件升级)结果
         */
        static void parseMediaPath(String data) {
            String info[];
            int sum, i;
            char[] achar;
            info = data.split(ACTION_TAG);
            if (info.length > 1 && info[1].contains(ACTION_CAPTURE_PIC)) {
                String path = data.split(MEDIA_PATH)[1].split("\n")[0];
                if (path.contains(".JPG") && path.contains("IMG") && path.contains("SD"))
                    if (mListener != null)
                        mListener.onCapturePic(path);
                return;
            } else if (info.length > 1 && info[1].contains(ACTION_CAPTURE_VIDEO)) {
                String path = data.split(MEDIA_PATH)[1].split("\n")[0];
                if (path.contains(".MP4") && path.contains("SHARE") && path.contains("SD"))
                    if (mListener != null)
                        mListener.onCaptureVideo(path);
                return;
            } else if (info.length > 1 && info[1].contains(ACTION_LOCK_VIDEO)) {
                String path = data.split(MEDIA_PATH)[1].split("\n")[0];
                if (path.contains(".MP4") && path.contains("SD")) {
                    String status = data.split(ACTION_STUTAS)[1].split("\n")[0];
                    if (mListener != null)
                        mListener.onLockVideo(path, status.contains(STATIC_SUCCESS));
                }
                return;
            } else if (info.length > 1 && info[1].contains(ACTION_SD_FORMAT)) {
                String path = data.split(ACTION_STUTAS)[1].split("\n")[0];
                if (mSetListener != null)
                    mSetListener.onSdFormat(path.contains(STATIC_SUCCESS));
                return;
            } else if (info.length > 1 && info[1].contains(ACTION_UPDATE_FW)) {
                String path = data.split(ACTION_STUTAS)[1].split("\n")[0];
                if (mSetListener != null)
                    mSetListener.onUpdateFw(path.contains(STATIC_SUCCESS));
                return;
            }

        }

    }

    static Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            parseMediaPath((String) msg.obj);
        }
    };

    public static void setAlive(boolean isAlive) {
        alive = isAlive;
    }

    public static OnCaptureListener mListener;
    public static OnSettingsListener mSetListener;

    public static void setCaptureListener(OnCaptureListener mL) {
        mListener = mL;
    }

    public static void setSetListener(OnSettingsListener mL) {
        mSetListener = mL;
    }

}