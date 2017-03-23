package cn.com.tiros.debug;

import android.util.Log;

import com.tencent.bugly.crashreport.BuglyLog;

import cn.com.tiros.api.BuildConfig;

public class GolukDebugUtils {
    private static final boolean DEBUG = true;
    /**
     * 是否启用Bugly来上传日志
     */
    public static boolean BUGLY_ENABLE = false;

    /**
     * 连接IPC需要经历的几个步骤：
     * 1.用户选择IPC的wifi
     * 2.连接IPC：通过调用通用层
     * {@link cn.com.mobnote.module.ipcmanager.IPCManagerFn#IPC_CommCmd_SetMode}
     * 和
     * {@link cn.com.mobnote.module.ipcmanager.IPCManagerFn#IPC_CommCmd_WifiChanged}
     * 执行连接IPC动作
     * 3.创建热点
     * 发送热点密码与名称信息到IPC
     * 4.ipc连接手机热点
     */
    public static final String CHOOSE_WIFI_LOG_TAG = "ChooseWifi";
    public static final String WIFI_CONNECT_LOG_TAG = "WifiConnection";
    public static final String CREATE_HOTSOPT_LOG_TAG = "CreateHotspot";
    public static final String HOTSPOT_CONNECT_LOG_TAG = "HotspotConnection";

    private static long TIMESTAMP = 0;
    private static String CURRENT_TAG = "";


    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG)
            Log.e(tag, msg);
    }

    /**
     * Bugly track 跟踪代码执行时间，用来优化代码。统计两段逻辑之间的代码时间间隔
     *
     * @param tag 针对不同分析主体，使用不同的TAG ，如果所有的东西都使用同一个TAG的话，就表示要统计整个TAG的使用情况
     * @param msg 具体信息
     */
    public static void bt(String tag, String msg) {
        if (BUGLY_ENABLE) {
            long duration;
            if (!tag.equals(CURRENT_TAG)) {
                TIMESTAMP = System.currentTimeMillis();
                BuglyLog.i(tag, "Start Count Time : " + String.valueOf(TIMESTAMP) + " MS\n" + msg);
            } else {
                duration = System.currentTimeMillis() - TIMESTAMP;
                TIMESTAMP = System.currentTimeMillis();
                BuglyLog.i(tag, "DURATION : " + String.valueOf(duration) + " MS\n" + msg);
            }
            CURRENT_TAG = tag;
        }
    }

    /**
     * Bugly error 纪录需要上传到bugly的error日志
     *
     * @param tag
     * @param msg
     */
    public static void be(String tag, String msg) {
        if (BUGLY_ENABLE)
            BuglyLog.e(tag, msg);
    }

}
