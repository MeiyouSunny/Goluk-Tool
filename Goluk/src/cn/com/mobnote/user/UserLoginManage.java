package cn.com.mobnote.user;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 登录管理类
 * 
 * @author mobnote
 *
 */
public class UserLoginManage {

	private GolukApplication mApp = null;
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	private UserLoginInterface mLoginInterface = null;

	/** 用户信息 **/
	private String phone = null;
	/** 输入密码错误限制 */
	public int countErrorPassword = 1;

	public UserLoginManage(GolukApplication mApp) {
		super();
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
	public boolean login(String phone, String pwd) {
		boolean b = false;
		String condi = "{\"PNumber\":\"" + phone + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
		b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Login, condi);
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
				switch (code) {
				case 200:
					// 登录成功后，存储用户的登录信息
					mSharedPreferences = mApp.getContext().getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putBoolean("FirstLogin", false);
					// 提交
					mEditor.commit();
					// ---------------------------登录成功的状态
					// 1------------------------------
					// 登录成功跳转
					if (mApp.registStatus != 2) {
						GolukUtils.showToast(mApp.getContext(),
								mApp.getResources().getString(R.string.user_login_success));
					}
					loginStatusChange(1);// 登录成功
					mApp.isUserLoginSucess = true;
					mApp.loginoutStatus = false;
					break;
				case 500:
					UserUtils.showDialog(mApp.getContext(),
							mApp.getResources().getString(R.string.user_background_error));
					loginStatusChange(2);
					break;
				case 405:
					loginStatusChange(3);// 手机号未注册
					break;
				case 402:
					GolukUtils
							.showToast(mApp.getContext(), mApp.getResources().getString(R.string.user_password_error));
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
			GolukDebugUtils.i("outtime", "-----网络链接超时超时超时-------xxxx---" + codeOut);
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
			phone = json.getString("phone");
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

}
