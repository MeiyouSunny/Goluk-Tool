package cn.com.tiros.api;

import android.util.Log;

import cn.com.tiros.debug.GolukDebugUtils;

public class Debug {

    public static void sys_assert(boolean exp) {
        assert (exp);
    }

    public static void sys_dbgprintf(String msg) {

        if (GolukDebugUtils.BUGLY_ENABLE) {
            GolukDebugUtils.bt("sys_dbgprintf", msg);
        } else {
            Log.e("sys_dbgprintf", msg);
        }
    }
}
