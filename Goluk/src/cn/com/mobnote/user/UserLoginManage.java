package cn.com.mobnote.user;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.console;

/**
 * 登录管理类
 * @author mobnote
 *
 */
public class UserLoginManage {
	
	private GolukApplication mApp = null;
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	private UserLoginInterface mLoginInterface = null;

	public UserLoginManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
		mApp.initLogic();
	}

	public void setUserLoginInterface(UserLoginInterface mInterface){
		this.mLoginInterface = mInterface;
	}
	
	public void loginStatusChange(int mStatus)
	{
		mApp.loginStatus = mStatus;
		if(mLoginInterface != null)
		{
			mLoginInterface.loginCallbackStatus();
		}
	}
	
	/**
	 * 登陆
	 * 当帐号和密码输入框都有内容时,激活为可点击状态
	 */
	public boolean login(String phone,String pwd){
		boolean b = false;
		// 网络判断
		if (!UserUtils.isNetDeviceAvailable(mApp.getContext())) {
			console.toast("当前网络状态不佳，请检查网络后重试", mApp.getContext());
			loginStatusChange(2);// 登录失败
		} else {
			String condi = "{\"PNumber\":\"" + phone + "\",\"Password\":\""+ pwd + "\",\"tag\":\"android\"}";
			b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_Login, condi);
		}
		return b;
	}
	
	/**
	 * 登录回调
	 * @param obj
	 */
	public void loginCallBack(int success,Object outTime,Object obj){
		console.log("登录回调---loginCallBack---" + success + "---" + obj);
		//--------------------------登录中的状态  0-----------------------------
		int codeOut = (Integer) outTime;
		if(1 == success){
			try{
				String data = (String)obj;
				Log.i("test", data);
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				switch (code) {
				case 200:
					//登录成功后，存储用户的登录信息
					mSharedPreferences = mApp.getContext().getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putBoolean("FirstLogin", false);
					//提交
					mEditor.commit();
					//---------------------------登录成功的状态  1------------------------------
					//登录成功跳转
					SysApplication.getInstance().exit();//杀死前边所有的Activity
					console.toast("登录成功", mApp.getContext());
					loginStatusChange(1);//登录成功
					mApp.isUserLoginSucess = true;
					break;
				case 500:
					UserUtils.showDialog(mApp.getContext(), "服务端程序异常");
					loginStatusChange(2);
					break;
				case 405:
					loginStatusChange(3);//手机号未注册
					break;
				case 402:
					console.toast("密码错误,请重试", mApp.getContext());
					loginStatusChange(2);
					break;
				default:
					break;
				}
			}
			catch(Exception ex){
				loginStatusChange(2);
				ex.printStackTrace();
			}
		}
		else{
			//网络超时当重试按照3、6、9、10s的重试机制，当网络链接超时时
			android.util.Log.i("outtime", "-----网络链接超时超时超时"+codeOut);
			switch (codeOut) {
			case 700:
				loginStatusChange(4);
				break;
			case 600:
				//网络未链接
				loginStatusChange(4);
			case 601:
				//http封装错误
				loginStatusChange(4);
				break;
			default:
				break;
			}
		}
	}
	
}
