package cn.com.mobnote.user;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

public class UserRegistManager {

	private static final String TAG = "lily";
	private GolukApplication mApp = null;
	/** 获取验证码json串 **/
	private String isIdentify = "";

	public UserRegistManager(GolukApplication mApp) {
		super();
		this.mApp = mApp;
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

	}

	/**
	 * 注册/重置密码 获取验证码回调
	 * 
	 * @param success
	 * @param obj
	 */
	public void getIdentifyCallback(int success, Object obj) {
		GolukDebugUtils.i(TAG, "-----------getIdentifyCallback-------success---" + success + "-----obj---" + obj);
		if (1 == success) {

		} else {

		}
	}

}
