package com.goluk.ipcsdk.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.goluk.ipcsdk.main.GolukIPCSdk;

/**
 * Network state listener
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isWifiConnected = isWifiConnected(context);
        if (isWifiConnected) {
            GolukIPCSdk.getInstance().changeIpcMode();
        }
    }

    private static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connectivityManager)
            return false;

        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (null == activeNetInfo || !activeNetInfo.isAvailable())
            return false;

        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null == wifiNetInfo)
            return false;

        NetworkInfo.State state = wifiNetInfo.getState();
        return state == NetworkInfo.State.CONNECTED;
    }

}
