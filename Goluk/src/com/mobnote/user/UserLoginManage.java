package com.mobnote.user;

import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.MD5Utils;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.userlogin.UserData;
import com.mobnote.golukmain.userlogin.UserResult;
import com.mobnote.golukmain.userlogin.UserloginBeanRequest;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.sina.weibo.sdk.utils.MD5;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.ViewDebug.IntToString;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 登录管理类
 * 
 * @author mobnote
 * 
 */
public class UserLoginManage implements IRequestResultListener {

	private GolukApplication mApp = null;
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	private UserLoginInterface mLoginInterface = null;

	private UserloginBeanRequest userloginBean = null;

	/** 用户信息 **/
	private String phone = null;
	/** 输入密码错误限制 */
	public int countErrorPassword = 1;

	public UserLoginManage(GolukApplication mApp) {
		super();
		userloginBean = new UserloginBeanRequest(IPageNotifyFn.PageType_Login,this);
		this.mApp = mApp;
	}

	public void setUserLoginInterface(UserLoginInterface mInterface) {
		this.mLoginInterface = mInterface;
	}

	public void loginStatusChange(int mStatus) {
		mApp.loginStatus = mStatus;
		if (mLoginInterface != null) {
			mLoginInterface.loginCallbackStatus();
		}
	}

	/**
	 * 登陆 当帐号和密码输入框都有内容时,激活为可点击状态
	 */
	public void login(String phone, String pwd) {
		userloginBean.get(phone, MD5.hexdigest(pwd));
	}

	public boolean login(ThirdLoginInfo info) {
		boolean b = false;
		String condi = JSON.toJSONString(info);
		b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_OauthLogin, condi);
		return b;
	}

	/**
	 * 登录回调
	 * 
	 * @param obj
	 */
	public void loginCallBack(int success, Object outTime, Object obj) {
		GolukDebugUtils.e("", "登录回调---loginCallBack---" + success + "---" + obj);
		// --------------------------登录中的状态 0-----------------------------
		int codeOut = (Integer) outTime;
		if (1 == success) {
			try {
				String data = (String) obj;
				GolukDebugUtils.i("lily", "-----UserLoginManage-----" + data);
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				JSONObject jsonData = json.optJSONObject("data");
				String uid = jsonData.optString("uid");
				switch (code) {
				case 200:
					// 登录成功后，存储用户的登录信息
					mSharedPreferences = mApp.getContext().getSharedPreferences("firstLogin",Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putBoolean("FirstLogin", false);
					// 提交
					mEditor.commit();

					mSharedPreferences = mApp.getContext().getSharedPreferences("setup", Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putString("uid", uid);
					mEditor.commit();
					// 登录成功跳转
					if (mApp.registStatus != 2) {
						GolukUtils.showToast(mApp.getContext(),mApp.getResources().getString(R.string.user_login_success));
					}
					loginStatusChange(1);// 登录成功
					mApp.isUserLoginSucess = true;
					mApp.loginoutStatus = false;
					break;
				case 500:
				case 400:
					UserUtils.showDialog(mApp.getContext(), mApp.getResources().getString(R.string.user_background_error));
					loginStatusChange(2);
					break;
				case 405:
					loginStatusChange(3);// 手机号未注册
					break;
				case 402:
					GolukUtils.showToast(mApp.getContext(), mApp.getResources().getString(R.string.user_password_error));
					loginStatusChange(2);
					countErrorPassword++;
					break;
				case 403:
					loginStatusChange(5);

					break;
				default:
					break;
				}
			} catch (Exception ex) {
				loginStatusChange(2);
				ex.printStackTrace();
			}
		} else {
			// 网络超时当重试按照3、6、9、10s的重试机制，当网络链接超时时
			GolukDebugUtils.i("outtime", "-----网络链接超时超时超时-------xxxx---"+ codeOut);
			switch (codeOut) {
			case 1:
				loginStatusChange(4);
				break;
			case 2:
				loginStatusChange(4);
				break;
			case 3:
				loginStatusChange(4);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 同步获取用户信息
	 */
	@SuppressWarnings("unused")
	public void initData() {
		GolukDebugUtils.i("lily", "------initData()-----UserLoginManage-----");
		String info = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		if (null == info) {
			return;
		}
		try {
			JSONObject json = new JSONObject(info);

			GolukDebugUtils.i("info", "====json()====" + json);
			String head = json.getString("head");
			String name = json.getString("nickname");
			String id = json.getString("key");
			String sex = json.getString("sex");
			String sign = json.getString("desc");
			phone = json.optString("phone");
			String uid = json.getString("uid");
			// 退出登录后，将信息存储
			mSharedPreferences = mApp.getContext().getSharedPreferences("setup", Context.MODE_PRIVATE);
			mEditor = mSharedPreferences.edit();
			GolukDebugUtils.i("lily", "------UserLoginManage----" + phone);
			mEditor.putString("setupPhone", UserUtils.formatSavePhone(phone));
			GolukDebugUtils.i("logintest", phone + "======1111====");
			mEditor.putBoolean("noPwd", false);
			mEditor.putString("uid", uid);
			mEditor.commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if (requestType == IPageNotifyFn.PageType_Login) {
			GolukDebugUtils.e("", "登录回调---loginCallBack------" + result);
			try {
				GolukDebugUtils.i("lily", "-----UserLoginManage-----" + result);
				UserResult userresult = (UserResult) result;
				if ("true".equals(userresult.state)) {

					int code = Integer.parseInt(userresult.code);
					UserData userdata = userresult.data;
					String uid = userdata.uid;
					switch (code) {
					case 200:
						// 登录成功后，存储用户的登录信息
						mSharedPreferences = mApp.getContext().getSharedPreferences("firstLogin",Context.MODE_PRIVATE);
						mEditor = mSharedPreferences.edit();
						mEditor.putBoolean("FirstLogin", false);
						// 提交
						mEditor.commit();

						mSharedPreferences = mApp.getContext().getSharedPreferences("setup",Context.MODE_PRIVATE);
						mEditor = mSharedPreferences.edit();
						mEditor.putString("uid", uid);
						mEditor.commit();
						GolukDebugUtils.i("zenghao", "-----UserLoginManage-----" + com.alibaba.fastjson.JSONObject.toJSONString(userdata));
						SharedPrefUtil.saveUserInfo(com.alibaba.fastjson.JSONObject.toJSONString(userdata));
						SharedPrefUtil.saveUserToken(userdata.token);
						// 登录成功跳转
						if (mApp.registStatus != 2) {
							GolukUtils.showToast(mApp.getContext(),mApp.getResources().getString(R.string.user_login_success));
						}
						loginStatusChange(1);// 登录成功
						mApp.isUserLoginSucess = true;
						mApp.loginoutStatus = false;
						break;
					case 500:
					case 400:
						UserUtils.showDialog(mApp.getContext(),mApp.getResources().getString(R.string.user_background_error));
						loginStatusChange(2);
						break;
					case 405:
						loginStatusChange(3);// 手机号未注册
						break;
					case 402:
						GolukUtils.showToast(mApp.getContext(),mApp.getResources().getString(R.string.user_password_error));
						loginStatusChange(2);
						countErrorPassword++;
						break;
					case 403:
						loginStatusChange(5);

						break;
					default:
						break;
					}
				}
			} catch (Exception ex) {
				loginStatusChange(2);
				ex.printStackTrace();
			}
		}
	}

}
