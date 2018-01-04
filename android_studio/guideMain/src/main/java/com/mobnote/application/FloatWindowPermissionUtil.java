package com.mobnote.application;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import com.mobnote.golukmain.R;

import java.lang.reflect.Method;

/**
 * FloatWindow permission judge
 */
public class FloatWindowPermissionUtil {

    public static boolean judgePermission(Context context) {
        boolean allowDrawOverlays = true;

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> c = Class.forName("android.provider.Settings");
                Method canDrawOverlays = c.getDeclaredMethod("canDrawOverlays", Context.class);

                if (canDrawOverlays != null) {
                    allowDrawOverlays = (boolean) canDrawOverlays.invoke(null, context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!allowDrawOverlays) {
                Toast.makeText(context, context.getString(R.string.str_system_window_not_allowed), Toast.LENGTH_LONG).show();
            }
        }

        return allowDrawOverlays;
    }

}
