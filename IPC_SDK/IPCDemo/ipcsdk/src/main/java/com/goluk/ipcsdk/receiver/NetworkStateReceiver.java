package com.goluk.ipcsdk.receiver;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.goluk.ipcsdk.utils.IPCConnectState;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

public class NetworkStateReceiver extends BroadcastReceiver {
	// private static int lastType = -1;
	private final static String TAG = "NetworkStateReceiver";
	private Handler mHandler = new Handler();

	@Override
	public void onReceive(Context context, Intent intent) {
//		GolukDebugUtils.e(TAG, "wifi---网络广播........");
		String action = intent.getAction();
		Message msg = new Message();
		msg.what = IPCManagerFn.WIFI_CONNECT_SUUCESS;
		if (!isWifiConnected(context)) {
//			GolukDebugUtils.e("", "wifi---网络广播....网络不可用...." + action);
			Log.d(TAG, "wifi---网络广播....网络不可用...." + action);
			msg.obj = false;

			IPCConnectState.getConnectState().setState(false);
//			if (null != MainActivity.mMainHandler) {
//				MainActivity.mMainHandler.sendMessage(msg);
//			}
//			EventBus.getDefault().post(new EventWifiState(EventConfig.WIFI_STATE, false));
		} else {
//			GolukDebugUtils.e("", "wifi---网络广播....网络可用...." + action);
			Log.d(TAG, "wifi---网络广播....网络不可用...." + action);
			msg.obj = true;
			IPCConnectState.getConnectState().setState(true);
//			if (null != MainActivity.mMainHandler) {
//				MainActivity.mMainHandler.sendMessage(msg);
//			}
//			EventBus.getDefault().post(new EventWifiState(EventConfig.WIFI_STATE, true));
		}

		mHandler.sendMessage(msg);
	}

	/**
	 * 网络是否可用
	 *
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
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


	public static boolean isWifiConnected(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(wifiNetworkInfo.isConnected())
		{
			return true ;
		}

		return false ;
	}

}
