package com.mobnote.receiver;

import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventWifiState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class NetworkStateReceiver extends BroadcastReceiver {
	// private static int lastType = -1;
	private final static String TAG = "NetworkStateReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		GolukDebugUtils.e(TAG, "wifi---网络广播........");
		String action = intent.getAction();
		Message msg = new Message();
		msg.what = 3;
		if (!isNetworkAvailable(context)) {
//			GolukDebugUtils.e("", "wifi---网络广播....网络不可用...." + action);
			Log.d(TAG, "wifi---网络广播....网络不可用...." + action);
			msg.obj = false;
//			if (null != MainActivity.mMainHandler) {
//				MainActivity.mMainHandler.sendMessage(msg);
//			}
			EventBus.getDefault().post(new EventWifiState(EventConfig.WIFI_STATE, false));
		} else {
			GolukDebugUtils.e("", "wifi---网络广播....网络可用...." + action);
			Log.d(TAG, "wifi---网络广播....网络不可用...." + action);
			msg.obj = true;
//			if (null != MainActivity.mMainHandler) {
//				MainActivity.mMainHandler.sendMessage(msg);
//			}
			EventBus.getDefault().post(new EventWifiState(EventConfig.WIFI_STATE, true));
		}
	}

	/**
	 * 网络是否可用
	 *
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = mgr.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

}
