package cn.com.mobnote.user;

import android.annotation.SuppressLint;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 注册管理类
 * @author mobnote
 *
 */
public class UserRegistManage {

	private GolukApplication mApp = null;
	private UserRegistInterface mRegistInterface = null;
	private UserIdentifyInterface mIdentifyInterface = null;
	
	public UserRegistManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
//		mApp.initLogic();
	}
	
	public void setIdentifyInterface(UserIdentifyInterface identifyInterface){
		this.mIdentifyInterface = identifyInterface;
	}
	public void setRegistInterface(UserRegistInterface mInterface){
		this.mRegistInterface = mInterface;
	}
	//验证码状态的判断
	public void identifyStatusChange(int mIdentifyStatus){
		mApp.identifyStatus = mIdentifyStatus;
		if(mIdentifyInterface != null){
			mIdentifyInterface.identifyCallbackInterface();
		}
	}
	//注册状态的判断
	public void registStatusChange(int mStatus)
	{
		mApp.registStatus = mStatus;
		if(mRegistInterface != null)
		{
			mRegistInterface.registStatusChange();
		}
	}

	/**
	 * 获取验证码
	 */
	@SuppressLint("HandlerLeak")
	public boolean getIdentify(String phone,String password){
		//获取验证码
		boolean bIndentify = false;
		String isIdentify = "{\"PNumber\":\"" + phone + "\",\"type\":\"1\"}";
		GolukDebugUtils.e("",isIdentify);
		bIndentify = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_GetVCode, isIdentify);
		return bIndentify;
	}
	
}
