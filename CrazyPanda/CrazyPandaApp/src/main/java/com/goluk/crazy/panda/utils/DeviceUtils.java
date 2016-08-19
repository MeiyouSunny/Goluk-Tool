package com.goluk.crazy.panda.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by DELL-PC on 2016/8/19.
 */
public class DeviceUtils {

    /* Device width, return as px */
    public static int getDeviceWith(Activity activity) {
        if(null == activity) {
            return 0;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

}
