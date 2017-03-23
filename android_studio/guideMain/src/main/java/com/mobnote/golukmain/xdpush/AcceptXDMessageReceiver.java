package com.mobnote.golukmain.xdpush;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.msg.MessageBadger;
import com.mobnote.manager.MessageManager;
import com.mobnote.util.JsonUtil;
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
		//Log.i("", "jyf----XG-----AcceptXdMessage  title:" + title + "  msg:" + customContent);
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
//		if (GolukApplication.getInstance() != null && !GolukApplication.getInstance().isExit()) {
			// 程序内显示框
//			GolukNotification.getInstance().showAppInnerPush(context, bean);
//		} else {
			// 程序外通知
//		EventBus.getDefault().post(new EventPushMsg(EventConfig.PUSH_MSG_GET, bean));
	//	MessageManager.getMessageManager().setCommentCount(commentCount);
		GolukApplication mApp = GolukApplication.getInstance();
		if(null == mApp) {
			return;
		}

		int type = 0;
		if(null != bean.params) {
			JSONArray array = null;
			try {
				array = new JSONArray(bean.params);
				int size = array.length();
				if(size > 0) {
					JSONObject obj = array.getJSONObject(0);
					type = JsonUtil.getJsonIntValue(obj, "t", 0);

					// 101 = comment
					// 102 = like/praise
					// 103  follow
					// 200~300 = system
					// 300~400 = official notification
                    // 209 live video to followers
                    if(0 == type) {
                        //do nothing
					} else if(101 == type) {
						if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
							int num = MessageManager.getMessageManager().getCommentCount();
							MessageManager.getMessageManager().setCommentCount(num + 1);
							if(mApp.isExit()) {
								MessageBadger.sendBadgeNumber(MessageManager.
										getMessageManager().getMessageTotalCount(), context);
							}
						}
					} else if(102 == type) {
						if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
							int num = MessageManager.getMessageManager().getPraiseCount();
							MessageManager.getMessageManager().setPraiseCount(num + 1);
							if(mApp.isExit()) {
								MessageBadger.sendBadgeNumber(MessageManager.
										getMessageManager().getMessageTotalCount(), context);
							}
						}
					}else if(103 == type){
						if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
							int num = MessageManager.getMessageManager().getFollowCount();
							MessageManager.getMessageManager().setFollowCount(num + 1);
							if(mApp.isExit()) {
								MessageBadger.sendBadgeNumber(MessageManager.
										getMessageManager().getMessageTotalCount(), context);
							}
						}
					}else if(type >= 200 && type < 300) {
						if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
							int num = MessageManager.getMessageManager().getSystemMessageCount();
							MessageManager.getMessageManager().setSystemMessageCount(num + 1);
							if(mApp.isExit()) {
								MessageBadger.sendBadgeNumber(MessageManager.
										getMessageManager().getMessageTotalCount(), context);
							}
						}
					} else if(type >= 300 && type < 400) {
						// for miui to sync number on launcher
//						int num = MessageManager.getMessageManager().getSystemMessageCount();
//						MessageManager.getMessageManager().setSystemMessageCount(num);
					} else {
						// do nothing
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (GolukApplication.getInstance() != null && !GolukApplication.getInstance().isExit()) {
			// 程序内通知不显示
		} else {
			GolukNotification.getInstance().showNotify(context, bean, json);
		}
	}

}
