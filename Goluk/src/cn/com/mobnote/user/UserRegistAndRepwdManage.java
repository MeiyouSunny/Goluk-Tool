package cn.com.mobnote.user;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

public class UserRegistAndRepwdManage {

	private static final String TAG = "lily";
	private GolukApplication mApp = null;
	private UserRegistAndRepwdInterface mInterface = null;

	public UserRegistAndRepwdManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
	}

	public void setUserRegistAndRepwd(UserRegistAndRepwdInterface mInterface) {
		this.mInterface = mInterface;
	}

	public void registAndRepwdStatusChange(int status) {
		mApp.registStatus = status;
		if (null != mInterface) {
			mInterface.registAndRepwdInterface();
		}
	}

	/**
	 * 注册/重置密码请求
	 * 
	 * @param phone
	 * @param password
	 * @param vCode
	 * @return
	 */
	public boolean registAndRepwd(boolean b, String phone, String password, String vCode) {
		String jsonStr = JsonUtil.registAndRepwdJson(phone, password, vCode);
		// TODO 判断获取验证码的次数，判断输入的验证码格式
		if(null == mApp || null == mApp.mGoluk) {
			return false;
		}

		if (b) {
			return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_Register, jsonStr);
		} else {
			return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_ModifyPwd, jsonStr);
		}
	}

	/**
	 * 注册/重置密码请求回调
	 * 
	 * @param success
	 * @param outTime
	 * @param obj
	 */
	public void registAndRepwdCallback(int success, Object outTime, Object obj) {
		GolukDebugUtils.i(TAG, "-----------registAndRepwdCallback--------success-----" + success
				+ "-------outTime-----" + outTime + "----obj----" + obj);
		int codeOut = (Integer) outTime;
		if (1 == success) {
			try {
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = json.getInt("code");
				GolukDebugUtils.i(TAG, "------code-----" + code);
				switch (code) {
				case 200:
					registAndRepwdStatusChange(2);
					break;
				case 500:
					registAndRepwdStatusChange(4);
					break;
				case 405:
					registAndRepwdStatusChange(5);
					break;
				case 406:
					registAndRepwdStatusChange(6);
					break;
				case 407:
					registAndRepwdStatusChange(7);
					break;
				case 480:
					registAndRepwdStatusChange(8);
					break;
				default:
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
				registAndRepwdStatusChange(9);
				break;
			}
		}

	}

}
