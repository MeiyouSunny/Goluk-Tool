package cn.com.mobnote.user;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class UserIdentifyManage {

	private static final String TAG = "lily";
	private GolukApplication mApp = null;
	private UserIdentifyInterface mIdentifyInterface = null;
	/** 获取验证码json串 **/
	private String isIdentify = "";

	public UserIdentifyManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
	}

	public void setUserIdentifyInterface(UserIdentifyInterface mInterface) {
		this.mIdentifyInterface = mInterface;
	}

	public void identifyStatusChange(int status) {
		mApp.identifyStatus = status;
		if (null != mIdentifyInterface) {
			mIdentifyInterface.identifyCallbackInterface();
		}
	}

	/**
	 * 注册/重置密码 获取验证码
	 * 
	 * @param phoneNumber
	 */
	public void getIdentify(String phoneNumber) {
		if (mApp.registOrRepwd) {
			isIdentify = "{\"PNumber\":\"" + phoneNumber + "\",\"type\":\"1\"}";
		} else {
			isIdentify = "{\"PNumber\":\"" + phoneNumber + "\",\"type\":\"2\"}";
		}
		boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVCode, isIdentify);
		if (b) {
			identifyStatusChange(0);
		} else {
			identifyStatusChange(2);
		}
	}

	/**
	 * 注册/重置密码 获取验证码回调
	 * 
	 * @param success
	 * @param obj
	 */
	public void getIdentifyCallback(int success, Object outTime, Object obj) {
		GolukDebugUtils.i(TAG, "-----------getIdentifyCallback-------success---" + success + "-----obj---" + obj);
		int codeOut = (Integer) outTime;
		if (1 == success) {
			try {
				String data = (String) obj;
				GolukDebugUtils.i(TAG, "----------getIdentifyCallback-------data--------" + data);
				JSONObject json = new JSONObject(data);
				int code = json.getInt("code");
				int freq = json.getInt("freq");
				switch (code) {
				case 200:
					identifyStatusChange(1);
					break;
				case 201:
					identifyStatusChange(3);
					break;
				case 500:
					identifyStatusChange(4);
					break;
				case 405:
					identifyStatusChange(5);
					break;
				case 440:
					identifyStatusChange(6);
					break;
				case 480:
					identifyStatusChange(7);
					break;
				case 470:
					identifyStatusChange(8);
					break;
				default:
					identifyStatusChange(2);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// 网络超时当重试按照3、6、9、10s的重试机制，当网络链接超时时
			GolukDebugUtils.i("outtime", "-----网络链接超时超时超时-------xxxx---" + codeOut);
			switch (codeOut) {
			case 1:
			case 2:
			case 3:
			default:
				GolukUtils.showToast(mApp.getContext(), "网络连接超时");
				break;
			}
		}
	}
}
