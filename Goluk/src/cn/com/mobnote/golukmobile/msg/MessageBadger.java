package cn.com.mobnote.golukmobile.msg;

import java.lang.reflect.Field;

import cn.com.mobnote.golukmobile.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MessageBadger {
	private final static String sLauncherClass = "cn.com.mobnote.golukmobile.GuideActivity";
	private final static String TAG = "MessageBadger";

	public static void sendBadgeNumber(int number, Context context) {
		if (number <= 0) {
			number = 0;
		}

		// Treat samsumg as default
		if (Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
//			sendToXiaoMi(number, context);
		} else if(Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
			sendToSamsumg(number, context);
		} else if(Build.MANUFACTURER.toLowerCase().contains("sony")) {
			sendToSony(number, context);
		} else if(Build.MANUFACTURER.toLowerCase().equals("meizu")) {
			sendToSamsumg(number, context);
		} else if(Build.MANUFACTURER.toLowerCase().equals("huawei")) {
//			setHuaweiBadge(number, context);
		} else {
			Log.d(TAG, "unsupported manufacturer");
		}
	}

	private static void sendToXiaoMi(int number, Context context) {
		NotificationManager nm = (NotificationManager)context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		Notification notification = null;
		boolean isMiUIV6 = true;
		try {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context);
			builder.setAutoCancel(true);
			builder.setSmallIcon(R.drawable.home_red_point_little);
			builder.setDefaults(Notification.DEFAULT_LIGHTS);
			notification = builder.build();
			Class miuiNotificationClass = Class
					.forName("android.app.MiuiNotification");
			Object miuiNotification = miuiNotificationClass.newInstance();
			Field field = miuiNotification.getClass().getDeclaredField(
					"messageCount");
			field.setAccessible(true);
			field.set(miuiNotification, Integer.valueOf(number));
			field = notification.getClass().getField("extraNotification");
			field.setAccessible(true);
			field.set(notification, miuiNotification);
		} catch (Exception e) {
			e.printStackTrace();
			// miui 6之前的版本
			isMiUIV6 = false;
			Intent localIntent = new Intent(
					"android.intent.action.APPLICATION_MESSAGE_UPDATE");
			localIntent.putExtra(
					"android.intent.extra.update_application_component_name",
					context.getPackageName() + "/" + sLauncherClass);
			localIntent.putExtra(
					"android.intent.extra.update_application_message_text",
					number);
			context.sendBroadcast(localIntent);
		} finally {
			if (notification != null && isMiUIV6) {
				// miui6以上版本需要使用通知发送
				nm.notify(101010, notification);
			}
		}
	}

	private static void sendToSony(int number, Context context) {
		boolean isShow = true;
		if (0 == number) {
			isShow = false;
		}

		Intent localIntent = new Intent();
		localIntent.putExtra(
			"com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE",
			isShow);
		localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
		localIntent.putExtra(
				"com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME",
				sLauncherClass);
		localIntent.putExtra(
				"com.sonyericsson.home.intent.extra.badge.MESSAGE", number);
		localIntent.putExtra(
				"com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME",
				context.getPackageName());
		context.sendBroadcast(localIntent);
	}

	private static void sendToSamsumg(int number, Context context) {
		Intent localIntent = new Intent(
				"android.intent.action.BADGE_COUNT_UPDATE");
		localIntent.putExtra("badge_count", number);
		localIntent.putExtra("badge_count_package_name",
				context.getPackageName());
		localIntent
				.putExtra("badge_count_class_name", sLauncherClass);
		context.sendBroadcast(localIntent);
	}

	private static void setHuaweiBadge(int number, Context context) {
		String launcherClassName = sLauncherClass;
		if (launcherClassName == null) {
			return;
		}
		Bundle localBundle = new Bundle();
		localBundle.putString("package", context.getPackageName());
		localBundle.putString("class", launcherClassName);
		localBundle.putInt("badgenumber", number);
		context.getContentResolver().call(
			Uri.parse("content://com.huawei.android.launcher.settings/badge/"),
			"change_badge", null, localBundle);
	}
}
