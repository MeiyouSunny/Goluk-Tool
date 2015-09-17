package cn.com.mobnote.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.GuideActivity;
import cn.com.mobnote.golukmobile.xdpush.GolukNotification;
import cn.com.tiros.debug.GolukDebugUtils;

public class GolukPushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		GolukDebugUtils.e("", "XDPushReceiver--------------------------onReceiver");

		if (GolukApplication.getInstance() != null) {
			if (GolukApplication.getInstance().isExit()) {
				startApp(context, intent);
			} else {
				pushMessage(context, intent);
			}
		} else {
			startApp(context, intent);
		}
	}

	/**
	 * 程序未启动，需要启动APP
	 * 
	 * @param context
	 * @author jyf
	 */
	private void startApp(Context context, Intent intent) {

		Intent startIntent = new Intent(context, GuideActivity.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		if (null != intent) {
			String from = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_FROM);
			String action = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_ACTION);
			startIntent.putExtra(GolukNotification.NOTIFICATION_KEY_FROM, from);
			startIntent.putExtra(GolukNotification.NOTIFICATION_KEY_ACTION, action);
		}

		context.startActivity(startIntent);
	}

	private void pushMessage(Context context, Intent intent) {
		GolukNotification.getInstance().startMain(intent);
	}

}
