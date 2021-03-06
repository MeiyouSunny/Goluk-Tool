package com.mobnote.t1sp.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.TextView;

public class ViewUtil {

    public static void goActivity(Context context, Class<? extends Activity> activity) {
        goActivity(context, activity, null, null);
    }

    public static void goActivity(Context context, Class<? extends Activity> activity, String extraKey, Parcelable data) {
        Intent intent = new Intent(context, activity);
        if (!TextUtils.isEmpty(extraKey))
            intent.putExtra(extraKey, data);
        context.startActivity(intent);
    }

    public static void goActivityAndClearTop(Context context, Class<? extends Activity> activity) {
        Intent intent = new Intent(context, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startService(Context context, Class<? extends Service> service) {
        Intent intent = new Intent(context, service);
        context.startService(intent);
    }

    public static String getTextViewValue(TextView textView) {
        return textView.getText().toString();
    }

}
