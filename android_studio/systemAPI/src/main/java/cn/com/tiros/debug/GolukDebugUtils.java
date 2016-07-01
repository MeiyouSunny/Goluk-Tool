package cn.com.tiros.debug;

import android.util.Log;

import cn.com.tiros.api.BuildConfig;

public class GolukDebugUtils {
    private static final boolean DEBUG =BuildConfig.DEBUG;
    public static final String WIFICONNECT_LOG_TAG = "WifiConnection";
    public static final String HOTSPOT_CONNECT_LOG_TAG = "HotspotConnection";

    private static long TIMESTAMP = 0;

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
     * track 跟踪代码执行时间，用来优化代码。统计两段逻辑之间的代码时间间隔
     *
     * @param tag 针对不同分析主体，使用不同的TAG ，如果所有的东西都使用同一个TAG的话，就表示要统计整个TAG的使用情况
     * @param msg 具体信息
     */
    public static void t(String tag, String msg) {
        if (DEBUG) {
            long duration = 0;
            if (TIMESTAMP == 0) {
                TIMESTAMP = System.currentTimeMillis();
                Log.i(tag, "Start Count Time : " + String.valueOf(TIMESTAMP/ 1000.0) + " S\n" + msg);
            } else {
                duration = System.currentTimeMillis() - TIMESTAMP;
                TIMESTAMP = System.currentTimeMillis();
                Log.i(tag, "DURATION : " + String.valueOf(duration/ 1000.0) + "S\n" + msg);
            }
        }
    }

}
