package cn.com.mobnote.golukmobile.xdpush;

import android.content.Context;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

/**
 * XGPushBaseReceiver类提供透传消息的接收和操作结果的反馈
 * */
public class AcceptXDMessageReceiver extends XGPushBaseReceiver {

	public static final String LogTag = "AcceptXDMessageReceiver";

	// 通知展示
	@Override
	public void onNotifactionShowedResult(Context context, XGPushShowedResult notifiShowedRlt) {

	}

	@Override
	public void onUnregisterResult(Context context, int errorCode) {
		// 反注册成功或失败
	}

	@Override
	public void onSetTagResult(Context context, int errorCode, String tagName) {
		// 设置Tag成功
	}

	@Override
	public void onDeleteTagResult(Context context, int errorCode, String tagName) {
		// 删除Tag成功或失败
	}

	// 通知点击回调 actionType=1为该消息被清除，actionType=0为该消息被点击
	@Override
	public void onNotifactionClickedResult(Context context, XGPushClickedResult message) {
		// 当通知弹出时，点击会执行此方法 (腾讯SDK自己弹出的通知)
	}

	@Override
	public void onRegisterResult(Context context, int errorCode, XGPushRegisterResult message) {
		// 注册状态
	}

	// 消息透传 收到消息
	@Override
	public void onTextMessage(Context context, XGPushTextMessage message) {
		String customContent = message.getContent();
		String title = message.getTitle();
		GolukDebugUtils.e("", "jyf----XG-----AcceptXdMessage  title:" + title + "  msg:" + customContent);
		dealTextMsg(context, title, customContent);
	}

	/**
	 * 处理推送下来的自定义消息
	 * 
	 * @param msg
	 * @author jyf
	 */
	private void dealTextMsg(Context context, String title, String json) {
		if (null == json || json.equals("")) {
			return;
		}
		XingGeMsgBean bean = JsonUtil.parseXingGePushMsg(json);
		if (null == bean) {
			return;
		}
		if (GolukApplication.getInstance() != null && !GolukApplication.getInstance().isExit()) {
			// 程序内显示框
			GolukNotification.getInstance().showAppInnerPush(context, bean);
		} else {
			// 程序外通知
			GolukNotification.getInstance().showNotify(context, bean, json);
		}
	}

}
