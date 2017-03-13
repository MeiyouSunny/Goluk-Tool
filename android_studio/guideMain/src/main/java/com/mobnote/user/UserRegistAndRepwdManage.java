package com.mobnote.user;

import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.bean.CheckVcodeBean;
import com.mobnote.golukmain.internation.login.InternationCheckVcodeRequest;
import com.mobnote.golukmain.userinfohome.bean.UserRecomBean;
import com.mobnote.user.bindphone.BindPhoneRequest;
import com.mobnote.user.bindphone.bean.BindPhoneDataBean;
import com.mobnote.user.bindphone.bean.BindPhoneRetBean;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

public class UserRegistAndRepwdManage implements IRequestResultListener {

	private static final String TAG = "lily";
	private GolukApplication mApp = null;
	private UserRegistAndRepwdInterface mInterface = null;

    private static final int BIND_PHONE_SUCCESS = 0;
    private static final int BIND_PHONE_PARAM_ERROR = 1;
    private static final int BIND_PHONE_UNKNOWN_EXCEPTION = 2;
    private static final int BIND_PHONE_VCODE_MISMATCH = 3;
    private static final int BIND_PHONE_VCODE_TIMEOUT = 4;

    /** 用于二次验证 **/
    public String mStep2Code = "";

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
			UserRegistRequest urr = new UserRegistRequest(IPageNotifyFn.PageType_Register,this);
			urr.get(phone,password,vCode,"");
			return true;
//			return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
//					IPageNotifyFn.PageType_Register, jsonStr);
		} else {
			UserRepwdRequest urr = new UserRepwdRequest(IPageNotifyFn.PageType_ModifyPwd,this);
			urr.get(phone,password,vCode,"");
			return true;
//			return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
//					IPageNotifyFn.PageType_ModifyPwd, jsonStr);
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
	public boolean registAndRepwd(boolean b, String phone, String password, String vCode, String zone) {
		String jsonStr = JsonUtil.registAndRepwdJson(phone, password, vCode, zone);
		// TODO 判断获取验证码的次数，判断输入的验证码格式
		if(null == mApp || null == mApp.mGoluk) {
			return false;
		}
		
		GolukDebugUtils.e("","registAndRepwd: " + jsonStr);

		if (b) {
//			UserRegistRequest urr = new UserRegistRequest(IPageNotifyFn.PageType_Register,this);
//			urr.get(phone,password,vCode,zone);
//			return true;
			//检查验证码
			InternationCheckVcodeRequest checkVcode = new InternationCheckVcodeRequest(IPageNotifyFn.PageType_InternationalCheckvcode, this);
			return checkVcode.get(phone, vCode, zone);
		} else {
			UserRepwdRequest urr = new UserRepwdRequest(IPageNotifyFn.PageType_ModifyPwd,this);
			urr.get(phone,password,vCode,zone);
			return true;
		}
	}

	/**
	 * 绑定手机
	 * 
	 * @param phone
	 * @param vCode
	 * @return
	 */
	public boolean bindPhoneNum(String phone, String vCode) {
		String jsonStr = "{\"phone\":\"" + phone + "\",\"vcode\":\"" + vCode + "\"}";
		// TODO 判断获取验证码的次数，判断输入的验证码格式
		if(null == mApp || null == mApp.mGoluk) {
			return false;
		}

		return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_BindInfo, jsonStr);
	}

	public void bindPhoneNumCallback(int success, Object outTime, Object obj) {
		int codeOut = (Integer) outTime;
		if (1 == success) {
			try {
				String result = (String) obj;
				JSONObject json = new JSONObject(result);
				JSONObject data = json.optJSONObject("data");

				if (data != null) {
					String status = data.optString("result");
					if (TextUtils.isDigitsOnly(status)) {
						int code = Integer.valueOf(status);
						switch (code) {
						case 0:
							registAndRepwdStatusChange(2);
							break;
						case 1:
						case 2:
							registAndRepwdStatusChange(3);
							break;
						case 3:
							registAndRepwdStatusChange(6);
							break;
						case 4:
							registAndRepwdStatusChange(7);
							break;
						default:
                            registAndRepwdStatusChange(9);
							break;

						}
					}
				} else {
					registAndRepwdStatusChange(3);
				}
			} catch (Exception e) {
				registAndRepwdStatusChange(3);
				e.printStackTrace();
			}
		} else {
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

    public void sendBindRequest(String uid, String phone, String vcode) {
        BindPhoneRequest request =
                new BindPhoneRequest(IPageNotifyFn.PageType_BindInfo, this);
        if(null != mApp && mApp.isUserLoginSucess) {
            if(!TextUtils.isEmpty(mApp.mCurrentUId)) {
                request.get("100", uid, phone, vcode);
            }
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_BindInfo) {
            BindPhoneRetBean retBean = (BindPhoneRetBean) result;
			if (retBean == null) {
				return;
			}
            if (null == retBean) {
                registAndRepwdStatusChange(9);
                return;
            }

            if (!retBean.success) {
                if(null == retBean.data) {
                    registAndRepwdStatusChange(9);
                    return;
                }
            }

            BindPhoneDataBean dataBean = retBean.data;
            if (TextUtils.isDigitsOnly(dataBean.result)) {
                int code = Integer.valueOf(dataBean.result);
                switch (code) {
                    case BIND_PHONE_SUCCESS:
                        registAndRepwdStatusChange(2);
                        break;
                    case BIND_PHONE_PARAM_ERROR:
                    case BIND_PHONE_UNKNOWN_EXCEPTION:
                        registAndRepwdStatusChange(3);
                        break;
                    case BIND_PHONE_VCODE_MISMATCH:
                        registAndRepwdStatusChange(6);
                        break;
                    case BIND_PHONE_VCODE_TIMEOUT:
                        registAndRepwdStatusChange(7);
                        break;
                    case GolukConfig.SERVER_TOKEN_EXPIRED:
                    case GolukConfig.SERVER_TOKEN_DEVICE_INVALID:
                    case GolukConfig.SERVER_TOKEN_INVALID:
                        if(mInterface instanceof Activity) {
                            Activity activity = (Activity)mInterface;
                            GolukUtils.startLoginActivity(activity);
                            activity.finish();
                        }
                        break;
                    default:
                        registAndRepwdStatusChange(9);
                        break;
                }
            } else {
                registAndRepwdStatusChange(9);
            }
        }else if(requestType == IPageNotifyFn.PageType_ModifyPwd ){
			UserRepwdBean urr = (UserRepwdBean) result;
			if (urr == null) {
				return;
			}
			int code = Integer.parseInt(urr.code);
				try {
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
		}else if(requestType == IPageNotifyFn.PageType_Register ){
			UserRegistBean urr = (UserRegistBean) result;
			if (urr == null) {
				return;
			}
				int code = Integer.parseInt(urr.code);
					try {
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
		} else if (requestType == IPageNotifyFn.PageType_InternationalCheckvcode) {
			CheckVcodeBean bean = (CheckVcodeBean) result;
			if (null == bean) {
				registAndRepwdStatusChange(9);
				return;
			}
			int code = bean.code;
			if (code == 0) {
				if(null != bean.data) {
					mStep2Code = bean.data.step2code;
				}
				registAndRepwdStatusChange(2);
			} else if (code == 20010) {//错误
				registAndRepwdStatusChange(6);
			} else if (code == 21001) {//超时
				registAndRepwdStatusChange(7);
			} else if (code == 12010) {//超出限制
				registAndRepwdStatusChange(10);
			} else {
				registAndRepwdStatusChange(9);
			}
		}
    }
}
