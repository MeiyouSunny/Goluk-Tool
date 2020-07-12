//package com.mobnote.golukmain.xdpush;
//
//import com.mobnote.application.GolukApplication;
//import com.mobnote.golukmobile.GuideActivity;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import cn.com.tiros.debug.GolukDebugUtils;
//
///**
// * 主要接受点击状态栏里的通知时的数据
// *
// * @author jyf
// */
//public class GolukClickNotificationReceiver extends BroadcastReceiver {
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		GolukDebugUtils.e("", "XDPushReceiver--------------------------onReceiver");
//		if (null == intent) {
//			return;
//		}
//		final String action = intent.getAction();
//		if (action.equals(GolukNotification.NOTIFICATION_BROADCAST)) {
//			if (GolukApplication.getInstance() != null) {
//				if (GolukApplication.getInstance().isExit()) {
//					startApp(context, intent);
//				} else {
//					pushMessage(context, intent);
//				}
//			} else {
//				startApp(context, intent);
//			}
//		}
//
//
//	}
//
//	/**
//	 * 程序未启动，需要启动APP
//	 *
//	 * @param context
//	 * @author jyf
//	 */
//	private void startApp(Context context, Intent intent) {
//		Intent startIntent = new Intent(context, GuideActivity.class);
//		startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		if (null != intent) {
//			String from = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_FROM);
//			String json = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_JSON);
//			startIntent.putExtra(GolukNotification.NOTIFICATION_KEY_FROM, from);
//			startIntent.putExtra(GolukNotification.NOTIFICATION_KEY_JSON, json);
//		}
//		context.startActivity(startIntent);
//	}
//
//	private void pushMessage(Context context, Intent intent) {
//		GolukNotification.getInstance().startMain(intent);
//	}
//
//}
