package com.mobnote.golukmain.internation.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import cn.smssdk.SMSSDK;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventRegister;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserSetupActivity;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.golukmain.userlogin.UserResult;
import com.mobnote.golukmain.userlogin.UserloginBeanRequest;
import com.mobnote.user.UserIdentifyInterface;
import com.mobnote.user.UserRegistAndRepwdInterface;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.sina.weibo.sdk.utils.MD5;

import de.greenrobot.event.EventBus;

/**
 * 获取验证码
 * 
 * @author mobnote
 *
 */
public class InternationUserIdentifyActivity extends BaseActivity implements OnClickListener, UserRegistAndRepwdInterface,IRequestResultListener {

	public static final String IDENTIFY_DIFFERENT = "identify_different";
	public static final String IDENTIFY_PHONE = "identify_phone";
	public static final String IDENTIFY_PASSWORD = "identify_password";
	public static final String IDENTIFY_INTER_REGIST = "identify_inter_regist";
	public static final String IDENTIFY_REGISTER_CODE = "REGISTER_CODE";
	private static final String TAG = "lily";
	/** Application & Context **/
	private GolukApplication mApp = null;
	private Context mContext = null;
	private Button mBtnNext = null;
	private TextView mRetryGetCode = null;
	/** 跳转注册页标识 **/
	private String intentRegistInter = "";
	/** 获取验证码 **/
	private CustomLoadingDialog mCustomDialogIdentify = null;
	/** 注册 **/
	private CustomLoadingDialog mCustomDialogRegist = null;
	/** 重置密码 **/
	private CustomLoadingDialog mCustomDialogRepwd = null;
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	/** true/false 注册/重置密码标识 **/
	private boolean justDifferent = false;
	/** 自动获取验证码 **/
	private BroadcastReceiver smsReceiver;
	private IntentFilter smsFilter;
	private String strBody = "";
	/** 销毁广播标识 **/
	private int click = 0;
	boolean b = true;
	private EditText mPwdEditText = null;
	private EditText mCodeEditText = null;

	private String mZone = null;
	/** 发送验证码手机号 **/
	private String mUserPhone = "";

	/** 密码 **/
	private String intentPassword = "";
	/** title **/
	private ImageButton mBtnBack;
	
	private TextView mCodeText, mTitleText;

	public UserloginBeanRequest userloginBean = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		getInfo();
		if (justDifferent) {
			setContentView(R.layout.internation_user_identify_layout);
		} else {
			getWindow().setContentView(R.layout.user_reset_pwd_layout);
			mTitleText = (TextView) findViewById(R.id.user_title_text);
			mTitleText.setText(getString(R.string.str_reset_pwd_title));
		}
		mContext = this;
		mApp = (GolukApplication) getApplication();
		initView();

		EventBus.getDefault().register(this);
		// countTime();
		// 自动获取验证码
		// getSmsMessage();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(mContext, "UserIdentify");
		mPwdEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				changeBtnColor();
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				
			}
		});
		mCodeEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				changeBtnColor();
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				
			}
		});
	}

	/**
	 * 初始化view
	 */
	public void initView() {
		mBtnNext = (Button) findViewById(R.id.user_identify_btn);
		mRetryGetCode = (TextView) findViewById(R.id.user_identify_retry);
		mPwdEditText = (EditText) findViewById(R.id.user_login_pwd);
		mCodeEditText = (EditText) findViewById(R.id.user_identity_code);
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mCodeText = (TextView) findViewById(R.id.tv_code);
		mBtnNext.setOnClickListener(this);
		mRetryGetCode.setOnClickListener(this);
		mBtnBack.setOnClickListener(this);

		// 获取验证码
		if (null == mCustomDialogIdentify) {
			mCustomDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_identify_loading));
		}
		// 注册
		if (null == mCustomDialogRegist) {
			mCustomDialogRegist = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_regist_loading));
		}
		// 重置密码
		if (null == mCustomDialogRepwd) {
			mCustomDialogRepwd = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_repwd_loading));
		}
		
		if (null != mZone) {
			int zoneCode = mZone.indexOf("+");
			String code = mZone.substring(zoneCode, mZone.length());
			mCodeText.setText(code + " " + mUserPhone);
		}
		
	}

	/**
	 * 获取信息
	 */
	public void getInfo() {
		Intent it = getIntent();
		if (null == it) {
			return;
		}

		if (null != it.getStringExtra(IDENTIFY_PHONE)) {
			mUserPhone = it.getStringExtra(IDENTIFY_PHONE).toString();
		}

		mZone = it.getStringExtra(InternationUserIdentifyActivity.IDENTIFY_REGISTER_CODE);
		justDifferent = it.getBooleanExtra(IDENTIFY_DIFFERENT, false);
		GolukDebugUtils.i(TAG, "-------justDifferent-------" + justDifferent);

		if (null != it.getStringExtra(IDENTIFY_PASSWORD)) {
			intentPassword = it.getStringExtra(IDENTIFY_PASSWORD).toString();
		}

		if (null != it.getStringExtra(IDENTIFY_INTER_REGIST)) {
			intentRegistInter = it.getStringExtra(IDENTIFY_INTER_REGIST).toString();
		}
	}

	@Override
	public void onClick(View view) {
		if(R.id.back_btn == view.getId()){
			finish();
		}else if(R.id.user_identify_retry == view.getId()){
			// 重新获取验证码
			getUserIdentify();
		}else if(R.id.user_identify_btn == view.getId()){
			final String pwd = mPwdEditText.getText().toString();
			final String code = mCodeEditText.getText().toString();
			if (null != pwd && pwd.length() > 0) {
				intentPassword = pwd;
				toRegistAndRepwd(justDifferent, mUserPhone, MD5.hexdigest(pwd), code);
			}else{
				GolukUtils.showToast(this,this.getResources().getString(R.string.user_no_getidentify));
			}
		}

	}

	/**
	 * 重新获取验证码
	 */
	private void getUserIdentify() {
		if (!UserUtils.isNetDeviceAvailable(this)) {
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
			return;
		}
		int zoneCode = mZone.indexOf("+");
		String code = mZone.substring(zoneCode+1, mZone.length());
		GolukMobUtils.sendSms(code, mUserPhone);
		UserUtils.hideSoftMethod(this);
		mCustomDialogIdentify.show();
	}

	/**
	 * 获取验证码成功
	 * 
	 * @author jyf
	 */
	private void getCodeSuccess() {
		GolukDebugUtils.e("", "mob---msg:  EventHandler: UserIdentifyActivity-----onEventMainThread :  getCodeSuccess");
		closeDialogIdentify();
		GolukUtils.showToast(this, getResources().getString(R.string.user_getidentify_success));
	}

	/**
	 * 获取验证码失败
	 * 
	 * @author jyf
	 */
	private void getCodeFailed(Object data) {
		closeDialogIdentify();
		try {
			Throwable throwable = (Throwable) data;
			JSONObject obj = new JSONObject(throwable.getMessage());
			final String des = obj.optString("detail");
			int status = obj.optInt("status");
			if (!TextUtils.isEmpty(des)) {
				GolukUtils.showToast(this, des);
			} else {
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_getidentify_fail));
			}
		} catch (Exception e) {
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_getidentify_fail));
		}

	}

	public void onEventMainThread(EventRegister event) {
		if (null == event) {
			return;
		}

		GolukDebugUtils.e("",
				"mob---msg:  EventHandler: UserIdentifyActivity-----onEventMainThread :  " + event.getmResult()
						+ " event: " + event.getmEvent());

		switch (event.getOpCode()) {
		case EventConfig.EVENT_REGISTER_CODE:
			if (SMSSDK.RESULT_COMPLETE == event.getmResult()) {
				if (SMSSDK.EVENT_GET_VERIFICATION_CODE == event.getmEvent()) {
					// 获取验证码成功
					getCodeSuccess();
				}
			} else {
				if (SMSSDK.EVENT_GET_VERIFICATION_CODE == event.getmEvent()) {
					// 获取验证码失败
					getCodeFailed(event.getmData());
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return false;
	}

	/**
	 * 关闭获取验证码的loading
	 */
	private void closeDialogIdentify() {
		if (null != mCustomDialogIdentify) {
			mCustomDialogIdentify.close();
			// mBtnBack.setEnabled(true);
			// mEditTextOne.setEnabled(true);
			// mBtnCount.setEnabled(true);
			mBtnNext.setEnabled(true);
		}
	}

	/**
	 * 关闭注册loading
	 */
	private void closeDialogRegist() {
		if (null != mCustomDialogRegist) {
			mCustomDialogRegist.close();
			// mBtnBack.setEnabled(true);
			// mEditTextOne.setEnabled(true);
			// mBtnCount.setEnabled(true);
			mBtnNext.setEnabled(true);
		}
	}

	/**
	 * 关闭重置密码loading
	 */
	private void closeDialogRepwd() {
		if (null != mCustomDialogRepwd) {
			mCustomDialogRepwd.close();
			// mBtnBack.setEnabled(true);
			// mEditTextOne.setEnabled(true);
			// mBtnCount.setEnabled(true);
			mBtnNext.setEnabled(true);
		}
	}

	/**
	 * 判断关闭哪个对话框 // 从设置页注册 it.putExtra("fromRegist", "fromSetup");
	 * 
	 * @param b
	 */
	public void justCloseDialog(boolean b) {
		if (b) {
			closeDialogRegist();
		} else {
			closeDialogRepwd();
		}
	}

	/**
	 * 注册/重置密码
	 * 
	 * @param flag
	 * @param phone
	 * @param password
	 * @param vCode
	 */
	@SuppressWarnings("static-access")
	public void toRegistAndRepwd(boolean flag, String phone, String password, String vCode) {
		if (!UserUtils.isNetDeviceAvailable(this)) {
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
		} else {
			// TODO 需要判断获取验证码次数
			if ("".equals(vCode) || null == vCode) {
				GolukUtils.showToast(mApp.getContext(), this.getResources().getString(R.string.user_no_getidentify));
			} else {
				if (vCode.length() < 4) {
					GolukUtils.showToast(mApp.getContext(), this.getResources()
							.getString(R.string.user_identify_format));
				} else {
					GolukDebugUtils.i(TAG, "---------useridentifymanage_count------"
							+ mApp.mIdentifyManage.useridentifymanage_count);
					if (mApp.mIdentifyManage.useridentifymanage_count > mApp.mIdentifyManage.IDENTIFY_COUNT) {
						UserUtils.showDialog(mContext,
								this.getResources().getString(R.string.count_identify_count_six_limit));
					} else {
						mApp.mRegistAndRepwdManage.setUserRegistAndRepwd(this);
						String zone = mZone.substring(mZone.indexOf("+") + 1);
						boolean b = mApp.mRegistAndRepwdManage.registAndRepwd(flag, phone, password, vCode, zone);
						if (b) {
							if (flag) {
								mCustomDialogRegist.show();
							} else {
								mCustomDialogRepwd.show();
							}
							// mBtnBack.setEnabled(false);
							// mEditTextOne.setEnabled(false);
							// mBtnCount.setEnabled(false);
							mBtnNext.setEnabled(false);
						} else {
							justCloseDialog(flag);
							if (flag) {
								GolukUtils
										.showToast(mContext, this.getResources().getString(R.string.user_regist_fail));
							} else {
								GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_repwd_fail));
							}
						}
					}

				}
			}
		}

	}

	/**
	 * 注册/重置密码接口回调
	 */
	@Override
	public void registAndRepwdInterface() {
		switch (mApp.registStatus) {
		// 注册/重置密码 中
		case 1:
			justCloseDialog(justDifferent);
			// mBtnBack.setEnabled(false);
			// mEditTextOne.setEnabled(false);
			// mBtnCount.setEnabled(false);
			mBtnNext.setEnabled(false);
			break;
		// 注册/重置密码成功
		case 2:
			justCloseDialog(justDifferent);
			if (justDifferent) {
				GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_success));
			} else {
				GolukUtils.showToast(this, this.getResources().getString(R.string.user_repwd_success));
			}

			registLogin();
			break;
		// 注册/重置失败
		case 3:
			justCloseDialog(justDifferent);
			if (justDifferent) {
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_regist_fail));
			} else {
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_repwd_fail));
			}
			break;
		// code = 500
		case 4:
			justCloseDialog(justDifferent);
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_background_error));
			break;
		// code = 405
		case 5:
			justCloseDialog(justDifferent);
			if (justDifferent) {
				UserUtils.showDialog(this, this.getResources().getString(R.string.user_already_regist));
			} else {
				new AlertDialog.Builder(this)
						.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
						.setMessage(this.getResources().getString(R.string.user_no_regist))
						.setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
						.setPositiveButton(this.getResources().getString(R.string.user_immediately_regist),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										Intent intentRepwd = new Intent(InternationUserIdentifyActivity.this,
												InternationUserRegistActivity.class);
										intentRepwd.putExtra("intentRepassword", mUserPhone);
										startActivity(intentRepwd);
										finish();
									}
								}).create().show();
			}
			break;
		// code = 406
		case 6:
			justCloseDialog(justDifferent);
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_identify_right_hint));
			break;
		// code = 407
		case 7:
			justCloseDialog(justDifferent);
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_identify_outtime));
			break;
		// code = 480
		case 8:
			justCloseDialog(justDifferent);
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_getidentify_fail));
			break;
		// 超时
		case 9:
			justCloseDialog(justDifferent);
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_netword_outtime));
			break;
		default:
			break;
		}
	}

	/**
	 * 注册完成后自动调一次登录的接口，以存储用户信息
	 */
	public void registLogin() {
		userloginBean = new UserloginBeanRequest(IPageNotifyFn.PageType_Login, this);
		userloginBean.get(mUserPhone.replace("-", ""),  MD5.hexdigest(intentPassword),"");
		mApp.loginStatus = 0;// 登录中
//
//		final String pwd = mPwdEditText.getText().toString();
//		GolukDebugUtils.i("", "---------registLogin()----------");
//		String condi = "{\"PNumber\":\"" + mUserPhone + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
//		boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Login,
//				condi);
//		GolukDebugUtils.i("final", "--------UserIdentifyActivity--------registLogin()-------b-------" + b);
//		if (b) {
//			// 登录成功跳转
//			mApp.loginStatus = 0;// 登录中
//		}
	}

	/**
	 * 登录的回调
	 */
	public void registLoginCallBack(int success, Object obj) {
		GolukDebugUtils.e("", "---------------registLoginCallBack()-------------------");
		mApp.loginStatus = 0;// 登录中
		if (1 == success) {
			try {
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				JSONObject jsonData = json.optJSONObject("data");
				String uid = jsonData.optString("uid");
				switch (code) {
				case 200:
					// 登录成功后，存储用户的登录信息
					mSharedPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putBoolean("FirstLogin", false);
					mEditor.commit();
					mSharedPreferences = mApp.getContext().getSharedPreferences("setup", Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putString("uid", uid);
					mEditor.commit();
					// 登录成功跳转
					mApp.loginStatus = 1;// 登录成功
					mApp.isUserLoginSucess = true;
					mApp.registStatus = 2;// 注册成功的状态
					mApp.mUser.timerCancel();
					mApp.autoLoginStatus = 2;

					Intent it = null;
					mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
					if ("fromStart".equals(intentRegistInter)) {
						it = new Intent(InternationUserIdentifyActivity.this, MainActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(it);
					} else if ("fromIndexMore".equals(intentRegistInter)) {
						it = new Intent(InternationUserIdentifyActivity.this, MainActivity.class);
						it.putExtra("showMe", "showMe");
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(it);
					} else if ("fromSetup".equals(intentRegistInter)) {
						it = new Intent(InternationUserIdentifyActivity.this, UserSetupActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(it);
					} else if ("fromProfit".equals(intentRegistInter)) {
						it = new Intent(InternationUserIdentifyActivity.this, MyProfitActivity.class);
						// it.putExtra("uid", uid);
						// it.putExtra("phone", title_phone.replaceAll("-",
						// ""));
						it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(it);
						UserUtils.exit();
					}
					finish();
					break;

				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// 回调执行失败
		}
	}

	/**
	 * 保存手机号
	 */
	public void putPhone() {
		mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		GolukDebugUtils.i(TAG, "phone==" + mUserPhone);
		mEditor.putString("setupPhone", mUserPhone);
		mEditor.putBoolean("noPwd", true);
		mEditor.commit();
	}

	/**
	 * 获取短信
	 */
	public void getSmsMessage() {
		// 自动获取验证码请求
		smsFilter = new IntentFilter();
		smsFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		smsFilter.setPriority(Integer.MAX_VALUE);
		smsReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent == null || intent.getExtras() == null)
					return;
				Object[] objs = (Object[]) intent.getExtras().get("pdus");
				if (objs == null || objs.length <= 0)
					return;
				for (Object obj : objs) {
					byte[] pdu = (byte[]) obj;
					SmsMessage sms = SmsMessage.createFromPdu(pdu);
					if (null != sms) {
						// 短信的内容
						String message = sms.getMessageBody();
						String regEx = "[^0-9]";
						Pattern p = Pattern.compile(regEx);
						if (null != p && null != message) {
							Matcher m = p.matcher(message);
							if (null != m) {
								strBody = m.replaceAll(" ").trim();
							}
						}
					}
				}
				if (strBody == null || strBody.length() < 6)
					return;
				String one = strBody.substring(0, 1);
				String two = strBody.substring(1, 2);
				String three = strBody.substring(2, 3);
				String four = strBody.substring(3, 4);
				String five = strBody.substring(4, 5);
				String six = strBody.substring(strBody.length() - 1);
				GolukDebugUtils.i("kkk", "----one----" + one + "----two---" + two + "----three----" + three
						+ "---four----" + four + "-----five---" + five + "----six-----" + six);
				/*
				 * mEditTextOne.setText(one); mEditTextTwo.setText(two);
				 * mEditTextThree.setText(three); mEditTextFour.setText(four);
				 * mEditTextFive.setText(five); mEditTextSix.setText(six);
				 */
			}
		};
		// 注册读取短信内容
		registerReceiver(smsReceiver, smsFilter);
		click = 1;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		if (click == 1) {
			unregisterReceiver(smsReceiver);
		}
	}
	
	private void changeBtnColor() {
		String password = mPwdEditText.getText().toString();
		String code = mCodeEditText.getText().toString();
		if (!"".equals(password.trim()) && password.length()>5 && password.length()<16 && !"".equals(code.trim())) {
			if (justDifferent) {
				mBtnNext.setTextColor(Color.parseColor("#FFFFFF"));
				mBtnNext.setEnabled(true);
			} else {
				mBtnNext.setTextColor(Color.parseColor("#000000"));
				mBtnNext.setEnabled(true);
			}
		} else {
			if (justDifferent) {
				mBtnNext.setTextColor(Color.parseColor("#7fffffff"));
				mBtnNext.setEnabled(false);
			} else {
				mBtnNext.setTextColor(Color.parseColor("#33000000"));
				mBtnNext.setEnabled(false);
			}
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if(requestType == IPageNotifyFn.PageType_Login){
			try {
				GolukDebugUtils.i("lily", "-----UserLoginManage-----" + result);
				UserResult userresult = (UserResult) result;
				int code = Integer.parseInt(userresult.code);
				switch (code) {
					case 200:
						// 登录成功后，存储用户的登录信息
						mSharedPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
						mEditor = mSharedPreferences.edit();
						mEditor.putBoolean("FirstLogin", false);
						mEditor.commit();
						mSharedPreferences = mApp.getContext().getSharedPreferences("setup", Context.MODE_PRIVATE);
						mEditor = mSharedPreferences.edit();
						mEditor.putString("uid", userresult.data.uid);
						mEditor.commit();
						// 登录成功跳转
						mApp.loginStatus = 1;// 登录成功
						mApp.isUserLoginSucess = true;
						mApp.registStatus = 2;// 注册成功的状态
						mApp.mUser.timerCancel();
						mApp.autoLoginStatus = 2;

						Intent it = null;
						mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);

						SharedPrefUtil.saveUserInfo(com.alibaba.fastjson.JSONObject.toJSONString(userresult.data));
						SharedPrefUtil.saveUserToken(userresult.data.token);
						JSONObject json = new JSONObject();

						if(!"".equals(userresult.data.phone)){
							json.put("phone",userresult.data.phone);
						}
						if(!"".equals(intentPassword)){
							json.put("pwd", intentPassword);
						}
						json.put("uid", userresult.data.uid);
						SharedPrefUtil.saveUserPwd(json.toString());

						GolukApplication.getInstance().parseLoginData(userresult.data);
						if ("fromStart".equals(intentRegistInter)) {
							it = new Intent(InternationUserIdentifyActivity.this, MainActivity.class);
							it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							startActivity(it);
						} else if ("fromIndexMore".equals(intentRegistInter)) {
							it = new Intent(InternationUserIdentifyActivity.this, MainActivity.class);
							it.putExtra("showMe", "showMe");
							it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							startActivity(it);
						} else if ("fromSetup".equals(intentRegistInter)) {
							it = new Intent(InternationUserIdentifyActivity.this, UserSetupActivity.class);
							it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							startActivity(it);
						} else if("fromProfit".equals(intentRegistInter)) {
							it = new Intent(InternationUserIdentifyActivity.this,MyProfitActivity.class);
//						it.putExtra("uid", uid);
//						it.putExtra("phone", title_phone.replaceAll("-", ""));
							it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(it);
							UserUtils.exit();
						}
						finish();
						break;

					default:
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
