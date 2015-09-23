package cn.com.mobnote.golukmobile.xdpush;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.GuideActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserOpenUrlActivity;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.util.JsonUtil;

public class GolukNotification {
	/** 推送标识，主要是主界面接受，用于区分是否是推送数据 */
	public static final String NOTIFICATION_KEY_FROM = "from";
	/** 用户点击状态栏时发出的广播 */
	public static final String NOTIFICATION_BROADCAST = "cn.com.goluk.broadcast.clicknotification";
	/** 用户点击状态栏，带的数据传输给主页 */
	public static final String NOTIFICATION_KEY_JSON = "json";

	/** 这个属性决定 ，用户点击通知以后，是启动Activity 还是发广播 , true为发广播 ，false为启动Activity */
	private static final boolean isBroadCast = true;

	private static GolukNotification mInstance = new GolukNotification();
	/** 主要封装对信鸽的操作 */
	private XGInit xgInit = null;
	/** 是否可以弹框(在一分钟只弹一次框) */
	private boolean isCanShowDialog = true;
	/** 显示程序内推送对话框 */
	private AlertDialog mPushShowDialog = null;
	/** timer用于计时一分钟 */
	private Timer mTimer = null;
	/** Timer计时时长 */
	private final int TIMER_OUT = 60 * 1000;

	public void createXG(Activity activity) {
		xgInit = new XGInit(activity);
		xgInit.init();
	}

	public XGInit getXg() {
		return xgInit;
	}

	public static synchronized GolukNotification getInstance() {
		return mInstance;
	}

	/**
	 * 显示程序外通知
	 * 
	 * @param msgBean
	 *            推送解析后的数据
	 * @param json
	 *            推送原始数据
	 * @author jyf
	 */
	public void showNotify(Context context, XingGeMsgBean msgBean, String json) {
		NotificationManager mNotiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotiManager.notify(msgBean.notifyId, createNotification(context, json, msgBean.title, msgBean.msg));
	}

	/**
	 * 创建一个通知显示体
	 * 
	 * @author jyf
	 */
	private Notification createNotification(Context startActivity, String json, String title, String content) {
		Notification noti = new Notification();
		setNoticationParam(noti);
		PendingIntent contentIntent = null;
		if (isBroadCast) {
			Intent intent = getBroadCastIntent(json);
			// 注意最后一个参数必须 写 PendingIntent.FLAG_UPDATE_CURRENT,
			// 否则下个Activity无法接受到消息
			contentIntent = PendingIntent.getBroadcast(startActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			// 注意最后一个参数必须 写 PendingIntent.FLAG_UPDATE_CURRENT,
			// 否则下个Activity无法接受到消息
			Intent intent = getWillStartIntent(startActivity);
			contentIntent = PendingIntent.getActivity(startActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		noti.setLatestEventInfo(startActivity, title, content, contentIntent);
		return noti;
	}

	/**
	 * 设置显示状态栏通知时，配置的基本参数，比如声音等
	 * 
	 * @author jyf
	 */
	private void setNoticationParam(Notification noti) {
		if (null == noti) {
			return;
		}
		final int icon = R.drawable.icon;
		// 立即通知
		final long when = System.currentTimeMillis();
		// 通知显示图标
		noti.icon = icon;
		// 通知什么时候显示，使用当前时间 ，就是立即显示
		noti.when = when;
		if (!isNight()) {
			// 通知显示默认声音
			noti.defaults |= Notification.DEFAULT_SOUND;
		}
		// 通知可以被 状态栏的清除按钮给清除掉, 用户点击也会清除掉
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		// 让声音无限循环，直到用户响应
		// noti.flags = Notification.FLAG_INSISTENT;
	}

	/**
	 * 如果是夜间，则推送来了，不响声音
	 * 
	 * @return true/false 夜/白
	 * @author jyf
	 */
	private boolean isNight() {
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour >= 8 && hour < 22) {
			// 白天
			return false;
		}
		return true;
	}

	/**
	 * 配置推送点击时发送的广播
	 * 
	 * @return
	 * @author jyf
	 */
	private Intent getBroadCastIntent(String json) {
		Intent intent = new Intent(NOTIFICATION_BROADCAST);
		intent.putExtra(NOTIFICATION_KEY_FROM, "notication");
		intent.putExtra(NOTIFICATION_KEY_JSON, json);
		return intent;
	}

	/**
	 * 配置，点击下拉通知时，启动的Activity
	 * 
	 * @param startActivity
	 * @return
	 * @author jyf
	 */
	private Intent getWillStartIntent(Context startActivity) {
		Intent intent = new Intent(startActivity, GuideActivity.class);
		// 设置启动模式,
		// 保证在程序启动的时候，把程序调入前台，并执行当前界面的onNewIntent()方法
		// 程序未启动，则把程序启动起来
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		// 添加以下action，也可以使被启动的Activity接受到消息，不知道为什么
		// intent.setAction(String.valueOf(System.currentTimeMillis()));
		intent.putExtra(NOTIFICATION_KEY_FROM, "notication");

		return intent;
	}

	/**
	 * 程序退出时调用
	 * 
	 * @author jyf
	 */
	public void destroy() {
		if (null != mTimer) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	/**
	 * 启动1分钟不弹框的计时
	 * 
	 * @author jyf
	 */
	private void startTimer() {
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				isCanShowDialog = true;
			}
		}, TIMER_OUT);
	}

	/**
	 * 销毁推送对话框
	 * 
	 * @author jyf
	 */
	public void dimissPushDialog() {
		if (null != mPushShowDialog) {
			mPushShowDialog.dismiss();
			mPushShowDialog = null;
		}
	}

	/**
	 * 判断接收到的推送消息是否可以显示
	 * 
	 * @return true/false 可以显示/不显示
	 * @author jyf
	 */
	private boolean isCanShowPushDialog() {
		if (!isCanShowDialog) {
			return false;
		}
		Context context = GolukApplication.getInstance().getContext();
		if (context instanceof LiveActivity || context instanceof CarRecorderActivity) {
			return false;
		}

		return true;
	}

	/**
	 * 程序内推送,显示对话框
	 * 
	 * @author jyf
	 */
	public void showAppInnerPush(final Context cnt, final XingGeMsgBean msgBean) {
		if (null == msgBean || !isCanShowPushDialog()) {
			return;
		}
		isCanShowDialog = false;
		startTimer();
		dimissPushDialog();
		Context context = GolukApplication.getInstance().getContext();
		mPushShowDialog = new AlertDialog.Builder(context).create();
		mPushShowDialog.setTitle(msgBean.title);
		mPushShowDialog.setMessage(msgBean.msg);
		mPushShowDialog.setCancelable(true);
		mPushShowDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if (null != mPushShowDialog) {
					mPushShowDialog.dismiss();
				}

			}
		});

		mPushShowDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				dealAppinnerClick(cnt, msgBean);
				if (null != mPushShowDialog) {
					mPushShowDialog.dismiss();
				}
			}
		});
		mPushShowDialog.show();
	}

	/**
	 * 处理程序内推送点击，程序内Dialog弹出后，点击“确定”的处理
	 * 
	 * @param msgBean
	 *            消息体
	 * @author jyf
	 */
	public void dealAppinnerClick(Context cnt, XingGeMsgBean msgBean) {
		if (null == msgBean) {
			return;
		}
		if ("0".equals(msgBean.target)) {
			// 不处理
		} else if ("1".equals(msgBean.target)) {
			// 启动程序
		} else if ("2".equals(msgBean.target)) {
			// 启动程序功能界面
			if ("1".equals(msgBean.tarkey)) {
				// 启动视频详情界面
				String[] vidArray = JsonUtil.parseVideoDetailId(msgBean.params);
				if (null != vidArray && vidArray.length > 0) {
					startDetail(vidArray[0]);
				}
			}
		} else if ("3".equals(msgBean.target)) {
			// 打开Web页
			if (null != msgBean.weburl && !"".equals(msgBean.weburl)) {
				Intent intent = new Intent(GolukApplication.getInstance().getContext(), UserOpenUrlActivity.class);
				intent.putExtra("url", msgBean.weburl);
				GolukApplication.getInstance().getContext().startActivity(intent);
			}
		}
	}

	/**
	 * 启动视频详情界面
	 * 
	 * @param vid
	 *            视频Id
	 * @author jyf
	 */
	public void startDetail(String vid) {
		Context context = GolukApplication.getInstance().getContext();
		Intent intent = new Intent(context, VideoDetailActivity.class);
		intent.putExtra(VideoDetailActivity.VIDEO_ID, vid);
		context.startActivity(intent);
	}

	/**
	 * 清除某个通知
	 * 
	 * @param activity
	 * @param id
	 * @author jyf
	 */
	public void clearNotication(Activity activity, int id) {
		NotificationManager mNotiManager = (NotificationManager) activity
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotiManager.cancel(id);
	}

	/**
	 * 用户点击状态栏上的消息处理,跳转到主界面
	 * 
	 * @param intent
	 *            包含用户点击的数据
	 * @author jyf
	 */
	public void startMain(Intent intent) {
		Context currentContext = GolukApplication.getInstance().getContext();
		if (null != currentContext) {
			Intent startIn = new Intent(currentContext, MainActivity.class);
			if (null != intent) {
				String from = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_FROM);
				String json = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_JSON);
				startIn.putExtra(GolukNotification.NOTIFICATION_KEY_FROM, from);
				startIn.putExtra(GolukNotification.NOTIFICATION_KEY_JSON, json);
			}
			currentContext.startActivity(startIn);
		}
	}

}