package cn.com.mobnote.user;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.console;

/**
 * 自动登录
 * @author mobnote
 *
 */
@SuppressLint("HandlerLeak")
public class User {
	
	/**记录登录状态**/
	public SharedPreferences mPreferencesAuto ;
	public boolean isFirstLogin;
	/**
	 * 设置网络5分钟自动重试机制的定时器
	 */
	private Handler mHandler = null;
	private Timer mTimer = null;
	private Context mContext = null;
	
	private GolukApplication mApp = null;
	private UserInterface mUserInterface = null;
	
	public User(GolukApplication mApp) {
		this.mApp = mApp;
		mContext = mApp.getApplicationContext();
		
		mApp.initLogic();
		
		//初始化Handler
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == 1) {//  1是处理5分钟超时的处理
					timerCancel();
					initAutoLogin();
				}
				super.handleMessage(msg);
			}
		};
	}
	public void setUserInterface(UserInterface mInterfaces){
		this.mUserInterface = mInterfaces;
	}
	
	/**
	 * 不是第一次登录的话，调用自动登录
	 * 当用户使用到需要『登录在线』条件下登录权限的功能时
	 * ——判断用户是否在自动登录，若是，则客户端使用系统 loading 提示：正在为您登录，请稍后…
	 */
	public void initAutoLogin(){
		Log.i("setauto", "-----initAtuoLogin ---------");
		//网络判断
		if(!UserUtils.isNetDeviceAvailable(mContext)){
//			console.toast("网络链接异常，检查网络后重新自动登录", mContext);
			StatusChange(3);//自动登录失败
		}else{
			//判断是否已经登录了
			if(isFirstLogin){
				//是第一次登录
				return;
			}else{//不是第一次登录
				//{tag:”android/ios/pad/pc”}
				String autoLogin = "{\"tag\":\"android\"}";
				Log.i("setauto", "------自动登录-------");
				boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_AutoLogin, autoLogin);
				if(b){
					StatusChange(1);//自动登录中
					Log.i("setauto", "------自动登录-------"+b+"------自动登录的状态值-----"+mApp.autoLoginStatus);
				}else{
					StatusChange(3);//自动登录失败
				}
			}
		}
	}
	
	public void StatusChange(int aStatus)
	{
		mApp.autoLoginStatus = aStatus;
		if(mUserInterface != null)
		{
			mUserInterface.statusChange();
		}
	}
	
	/**
	 * 自动登录回调
	 * 
	 */
	public void initAutoLoginCallback(int success,Object outTime,Object obj){
		console.log("---------------自动登录回调---------------");
		int codeOut = (Integer) outTime;
		if(1 == success){
			try{
				String data = (String)obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				console.log(data);
				Log.i("lily", "----User-----"+data);
				switch (code) {
				case 200:
					//自动登录成功无提示
					console.toast("自动登录成功", mContext);
					StatusChange(2);//自动登录成功
					Log.i("setauto", "----ok---"+mApp.autoLoginStatus);
					mApp.loginoutStatus = false;
					mApp.isUserLoginSucess = true;
					break;
				//自动登录的一切异常都不进行提示
				case 500:
					timerTask();
					StatusChange(3);//自动登录失败
					//服务端异常
					break;
				case 405:
					//用户未注册
					break;
				case 402:
					//登录密码错误
					StatusChange(5);
					Log.i("setauto", "-------402 -----"+mApp.autoLoginStatus);
//					console.toast("密码错误，请重试", mContext);
					break;
				default:
					break;
				}
			}catch(Exception e){
				StatusChange(3);//自动登录失败
				e.printStackTrace();
			}
		}else{
			//网络超时当重试按照3、6、9、10s的重试机制，当网络链接超时时，5分钟后继续自动登录重试
			android.util.Log.i("setauto", "-----自动登录网络链接超时-----"+codeOut);
			switch (codeOut) {
			case 1://没有网络
				timerTask();
				StatusChange(4);//自动登录失败
				break;
			case 2://服务端错误
				timerTask();
				StatusChange(4);//自动登录失败
				break;
			case 3://网络链接超时
				timerTask();
				StatusChange(4);//自动登录超时
				break;
			default:
				break;
			}
			
		}
}
	
	/**
	 * 设置网络5分钟自动重试机制的定时器
	 * 1000x60x5=300000
	 */
	public void timerTask(){
		Log.i("setauto", "------timerTask()-----");
		timerCancel();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(1);
			}
		}, 300000);
	}
	
	public void timerCancel(){
		if(mTimer !=null){
			mTimer.cancel();
			mTimer = null;
		}
	}
	
}
