package cn.com.mobnote.golukmobile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.CountDownButtonHelper;
import cn.com.mobnote.user.UserIdentifyInterface;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
/**
 * 注册 
 * 
 * 1、注册手机号密码 
 * 2、获取验证码
 *  3、登陆
 * 
 * @author mobnote
 *
 */
public class UserRegistActivity extends BaseActivity implements OnClickListener, UserIdentifyInterface, OnTouchListener {

	/**注册title**/
	private ImageButton mBackButton;
	private TextView mTextViewTitle;
	/**手机号、密码、注册按钮**/
	private EditText mEditTextPhone, mEditTextPwd;
	private Button mBtnRegist;
	
	private Context mContext = null;
	private GolukApplication mApplication = null;
	
	/**倒计时帮助类**/
	private CountDownButtonHelper mCountDownhelper;
	/**自动获取验证码**/
	private BroadcastReceiver smsReceiver;
	private IntentFilter smsFilter;
	private Handler mSendMesageHandler;
	private String strBody;
	/**注册**/
	private CustomLoadingDialog mCustomProgressDialog=null;
	/**获取验证码**/
	private CustomLoadingDialog mCustomProgressDialogIdentify = null;
	/**判断获取验证码按钮是否被点击过**/
	private boolean identifyClick = false;
	/**记录注册成功的状态**/
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	/**注册成功跳转页面的判断标志*/
	private String registOk = null;
	/**获取验证码的次数**/
	private String freq = "";
	/**6次获取验证码**/
	private static final int IDENTIFY_COUNT = 6;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_regist);
		
		mContext = this;
		SysApplication.getInstance().addActivity(this);
		mApplication = (GolukApplication) getApplication();
		
		initView();
		// title
		mTextViewTitle.setText("注册");
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		mApplication.setContext(mContext, "UserRegist");
		
		if(null == mCustomProgressDialog){
			mCustomProgressDialog = new CustomLoadingDialog(mContext,"注册中，请稍候……");
		}
		if(null == mCustomProgressDialogIdentify){
			mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, "验证码获取中……");
		}
		
		getInfo();
	}
	
	public void initView() {
		// title
		mBackButton = (ImageButton) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		// 手机号、密码、注册按钮
		mEditTextPhone = (EditText) findViewById(R.id.user_regist_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_regist_pwd);
		mBtnRegist = (Button) findViewById(R.id.user_regist_btn);

		/**
		 * 监听绑定
		 */
		mBackButton.setOnClickListener(this);
		mBtnRegist.setOnClickListener(this);
		mBtnRegist.setOnTouchListener(this);

	}
	/**
	 * 手机号码获取
	 */
	public void getInfo() {
		Intent itLoginPhone = getIntent();
		if (null != itLoginPhone.getStringExtra("intentLogin")) {
			String number = itLoginPhone.getStringExtra("intentLogin").toString();
			GolukDebugUtils.i("user", number);
			mEditTextPhone.setText(number);
		}
		Intent itRepassword = getIntent();
		if (null != itRepassword.getStringExtra("intentRepassword")) {
			String repwdNum = itRepassword.getStringExtra("intentRepassword").toString();
			mEditTextPhone.setText(repwdNum);
		}

		/**
		 * 判断是从哪个入口进行的注册
		 */
		Intent itRegist = getIntent();
		if (null != itRegist.getStringExtra("fromRegist")) {
			registOk = itRegist.getStringExtra("fromRegist").toString();
		}

		getPhone();

		// 手机号、密码、验证码文本框改变监听
		mEditTextPhone.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String phone = mEditTextPhone.getText().toString().replace("-", "");
				String pwd = mEditTextPwd.getText().toString();
				if (arg1) {
					// 注册按钮
					if (!"".equals(phone) && !"".equals(pwd)) {
						mBtnRegist.setBackgroundResource(R.drawable.icon_login);
						mBtnRegist.setEnabled(true);
					} else {
						mBtnRegist.setBackgroundResource(R.drawable.icon_more);
						mBtnRegist.setEnabled(false);
					}
				}
			}
		});

		mEditTextPhone.addTextChangedListener(new TextWatcher() {
			private boolean isDelete = false;

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhone.getText().toString().replace("-", "");
				String pwd = mEditTextPwd.getText().toString();
				// 注册按钮
				if (!"".equals(phone) && !"".equals(pwd)) {
					mBtnRegist.setBackgroundResource(R.drawable.icon_login);
					mBtnRegist.setEnabled(true);
				} else {
					mBtnRegist.setBackgroundResource(R.drawable.icon_more);
					mBtnRegist.setEnabled(false);
				}

				// 格式化显示手机号
				mEditTextPhone.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(View arg0, int keyCode, KeyEvent arg2) {
						if (keyCode == KeyEvent.KEYCODE_DEL) {
							isDelete = true;
						}
						return false;
					}
				});
				UserUtils.formatPhone(arg0, mEditTextPhone);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

		mEditTextPwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhone.getText().toString().replace("-", "");
				String pwd = mEditTextPwd.getText().toString();
				// 注册按钮
				if (!"".equals(phone) && !"".equals(pwd)) {
					mBtnRegist.setBackgroundResource(R.drawable.icon_login);
					mBtnRegist.setEnabled(true);
				} else {
					mBtnRegist.setBackgroundResource(R.drawable.icon_more);
					mBtnRegist.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		// 返回
		case R.id.back_btn:
			finish();
			break;
		// 注册按钮
		case R.id.user_regist_btn:
			//点按钮后,弹出登录中的提示,样式使用系统 loading 样式,文字描述:注册中
			//注册成功:弹出系统短提示:注册成功,以登录状态进入 Goluk 首页
			mApplication.registOrRepwd = true;
			regist();
			break;
		}
	}
	/**
	 * 获取验证码
	 */
	/*@SuppressLint("HandlerLeak")
	public void getIdentify(){
		String phone = mEditTextPhone.getText().toString();
		
		*//**
		 * 自动获取验证码
		 *//*
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mEditTextIdentify.setText(strBody);
			}
		};
		smsFilter = new IntentFilter();
		smsFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		smsFilter.setPriority(Integer.MAX_VALUE);
		smsReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Object[] objs = (Object[]) intent.getExtras().get("pdus");
				for (Object obj : objs) {
					byte[] pdu = (byte[]) obj;
					SmsMessage sms = SmsMessage.createFromPdu(pdu);
					// 短信的内容
					String message = sms.getMessageBody();					
					String regEx="[^0-9]";   
					Pattern p = Pattern.compile(regEx);   
					Matcher m = p.matcher(message);   
					strBody = m.replaceAll("").trim();
					handler.sendEmptyMessage(1);
					
				}
			}
		};
		*//**
		 * 对获取验证码进行判断
		 *//*
		if(UserUtils.isMobileNO(phone)){
			String isIdentify = "{\"PNumber\":\"" + phone + "\",\"type\":\"1\"}";
			GolukDebugUtils.e("",isIdentify);
			boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_GetVCode, isIdentify);
			if(b){
				identifyClick = true;
				UserUtils.hideSoftMethod(this);
				mCustomProgressDialogIdentify.show();
				registerReceiver(smsReceiver, smsFilter);
				click = 1;
				GolukDebugUtils.e("",b + "");
				mBtnRegist.setEnabled(false);
				mEditTextPhone.setEnabled(false);
				mEditTextPwd.setEnabled(false);
				mEditTextIdentify.setEnabled(false);
				mBackButton.setEnabled(false);
				mTextViewLogin.setEnabled(false);
			} else {

			}
		}else{
			if(!phone.equals("")){
				UserUtils.showDialog(mContext, "手机号输入格式错误，请重新输入");
			}else{
				mBtnIdentify.setEnabled(false);
			}
		}
		
	}*/
	
	/**
	 * 验证码回调
	 */
	/*public void identifyCallback(int success,Object obj){
		GolukDebugUtils.e("","验证码获取回调---identifyCallBack---" + success + "---" + obj);
		closeProgressDialogIdentify();
		//点击验证码按钮手机号、密码不可被修改
		mEditTextPhone.setEnabled(true);
		mEditTextIdentify.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		mBackButton.setEnabled(true);
		mBtnRegist.setEnabled(true);
		mTextViewLogin.setEnabled(true);		
		if(1 == success){
			try{
				String data = (String)obj;
				GolukDebugUtils.e("",data);
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				freq = json.getString("freq");
				GolukDebugUtils.i("lily", "------freq------"+freq);
				switch (code) {
				case 200:
					GolukUtils.showToast(mContext, "验证码已经发送，请查收短信");
					
					int freqInt = 0;
					try {
						freqInt = Integer.parseInt(freq);
					} catch (Exception e) {
						GolukUtils.showToast(mContext, "请重新获取验证码");
						return;
					}
					int count = IDENTIFY_COUNT - freqInt;
					GolukDebugUtils.i("lily", freqInt+"====freqInt===="+count);
					if (count > 0) {
						if (count < IDENTIFY_COUNT - 1) {
							UserUtils
									.showDialog(mContext, this.getResources().getString(R.string.count_identify_first) + count + this.getResources().getString(R.string.count_identify_second));
						}
					} else {
						UserUtils.showDialog(mContext, this.getResources().getString(R.string.count_identify_six));
					}
					//验证码获取成功
					*//**
					 * 点击获取验证码的时候进行倒计时
					 *//*
					mEditTextPhone.setEnabled(false);
					mCountDownhelper = new CountDownButtonHelper(mBtnIdentify, 60, 1);
					mCountDownhelper.setOnFinishListener(new OnFinishListener() {
						@Override
						public void finish() {
							mBtnIdentify.setText("重新获取");
							//倒计时结束后手机号、密码可以更改
							mEditTextPhone.setEnabled(true);
						}
					});
					mCountDownhelper.start();
					
					break;
				case 201:
					UserUtils.showDialog(this, "该手机号1小时内下发5次以上验证码");
					break;

				case 500:
					UserUtils.showDialog(this, "服务端程序异常");
					break;

				case 405:
					new AlertDialog.Builder(this)
					.setMessage("此手机号已经被注册")
					.setNegativeButton("取消", null)
					.setPositiveButton("立即登录", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
//							getPhone();
							if(mApplication.loginoutStatus = true){
								String phone = mEditTextPhone.getText().toString();
								mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
								mEditor = mSharedPreferences.edit();
								mEditor.putString("setupPhone", phone);
								mEditor.putBoolean("noPwd", true);
								mEditor.commit();
							}
							finish();
						}
					}).create().show();
					break;

				case 440:
					UserUtils.showDialog(this, "输入手机号异常");
					break;
				case 480:
					UserUtils.showDialog(this, "验证码发送失败，请重新发送");
					GolukDebugUtils.i("lily", "-------identifyCallback-----480----");
					break;
				case 470:
					UserUtils.showDialog(mContext, "获取验证码失败,此手机号已经达到获取验证码上限");
					break;
				default:
					break;
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		else{
			GolukUtils.showToast(mContext, "验证码获取失败");
			mBtnIdentify.setText("重新获取");
		}
	}*/
	
	/**
	 * 注册
	 */
	public void regist(){
		String phone = mEditTextPhone.getText().toString().replace("-", "");
		String password = mEditTextPwd.getText().toString();
		
		if (!"".equals(phone) && UserUtils.isMobileNO(phone)) {
			if (!"".equals(password)) {
				mBtnRegist.setEnabled(true);
				if (password.length() >= 6 && password.length() <= 16) {
					if (!UserUtils.isNetDeviceAvailable(mContext)) {
						GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
					} else {
						mApplication.mIdentifyManage.setUserIdentifyInterface(this);
						boolean b = mApplication.mIdentifyManage.getIdentify(true,phone);
						if(b){
							click = 1;
							UserUtils.hideSoftMethod(this);
							mCustomProgressDialogIdentify.show();
							mBtnRegist.setEnabled(false);
							mEditTextPhone.setEnabled(false);
							mEditTextPwd.setEnabled(false);
							mBackButton.setEnabled(false);
						}else{
							closeProgressDialogIdentify();
							mBtnRegist.setEnabled(true);
							mEditTextPhone.setEnabled(true);
							mEditTextPwd.setEnabled(true);
							mBackButton.setEnabled(true);
							GolukUtils.showToast(mContext, "验证码获取失败");
						}
						
						// {PNumber：“13054875692”，Password：“xxx”，VCode：“1234”}
						/*String isRegist = "{\"PNumber\":\"" + phone + "\",\"Password\":\"" + password
								+ "\",\"VCode\":\"" + identify + "\",\"tag\":\"android\"}";
						GolukDebugUtils.e("", isRegist);
						GolukDebugUtils.i("lily", "------UserRegistActivity---不点击获取验证码---111------" + freq);
						int freqInt = 0;
						if (identifyClick) {
							try {
								freqInt = Integer.parseInt(freq);
							} catch (Exception e) {
								GolukUtils.showToast(mContext, "请重新获取验证码");
								return;
							}

							GolukDebugUtils.i("lily", "------UserRegistActivity---不点击获取验证码---------" + freq);
							if (freqInt > IDENTIFY_COUNT) {
								UserUtils.showDialog(mContext,
										this.getResources().getString(R.string.count_identify_limit) + IDENTIFY_COUNT
												+ "次)");
							} else {
								if (identify.length() < 6) {
									UserUtils.showDialog(mContext, "验证码格式输入不正确");
								} else {
									boolean b = mApplication.mGoluk.GolukLogicCommRequest(
											GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Register,
											isRegist);
									if (b) {
										mApplication.registStatus = 1;// 注册中……
										// 隐藏软件盘
										UserUtils.hideSoftMethod(this);
										mCustomProgressDialog.show();
										mEditTextPhone.setEnabled(false);
										mEditTextIdentify.setEnabled(false);
										mEditTextPwd.setEnabled(false);
										mBtnIdentify.setEnabled(false);
										mTextViewLogin.setEnabled(false);
										mBackButton.setEnabled(false);
										mBtnRegist.setEnabled(false);
									}
								}
							}
						} else {
							GolukUtils.showToast(mContext, "请先获取验证码");
						}*/
					}
				} else {
					UserUtils.showDialog(UserRegistActivity.this, this.getResources().getString(R.string.user_login_password_show_error));
					mBtnRegist.setEnabled(true);
				}
			}
		} else {
			UserUtils.showDialog(mContext, this.getResources().getString(R.string.user_login_phone_show_error));
		}

	}
	
	/**
	 * 注册回调
	 */
	public void registCallback(int success,Object outTime,Object obj){
		int codeOut = (Integer) outTime;
		GolukDebugUtils.e("","注册回调---registCallback---"+success+"---"+obj);
		closeProgressDialog();
		mEditTextPhone.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		mBackButton.setEnabled(true);
		mBtnRegist.setEnabled(true);
		if(1 == success){
			try{
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				GolukDebugUtils.e("",code+"");
				
				switch (code) {
				case 200:
					//注册成功
					GolukUtils.showToast(mContext, "注册成功");
					mApplication.registStatus = 2;//注册成功的状态
					//登录成功跳转
					mApplication.loginStatus=1;//登录成功
					mApplication.isUserLoginSucess = true;
					
					//注册成功后再次调用登录的接口
					registLogin();
					break;
				case 500:
					UserUtils.showDialog(this, "服务端程序异常");
					break;
				case 405:
					UserUtils.showDialog(this, "用户已注册");
					break;
				case 406:
					if(identifyClick){
						UserUtils.showDialog(this, "请输入正确的验证码");
					}else{
						GolukUtils.showToast(mContext, "请先获取验证码");
					}
					break;
				case 407:
					String phone = mEditTextPhone.getText().toString().replace("-", "");
					if(UserUtils.isMobileNO(phone)){
						if(identifyClick){
							UserUtils.showDialog(this, "输入验证码超时");
						}else{
							GolukUtils.showToast(mContext, "请先获取验证码");
						}
					}else{
						UserUtils.showDialog(this, "手机格式输入错误,请重新输入");
					}
					break;
				case 480:
					if(identifyClick){
						UserUtils.showDialog(this, "验证码获取失败");
					}else{
						GolukUtils.showToast(mContext, "请先获取验证码");
					}
					break;

				default:
					break;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			// 网络超时当重试按照3、6、9、10s的重试机制，当网络链接超时时
			GolukDebugUtils.i("outtime", "-----网络链接超时超时超时" + codeOut);
			GolukUtils.showToast(mContext, "网络连接超时");
			switch (codeOut) {
			case 1:
				mApplication.registStatus = 3;
				break;
			case 2:
				mApplication.registStatus = 3;
				break;
			case 3:// 超时
				mApplication.registStatus = 3;
				break;
			default:
				break;
			}
		}
	}
	/**
	 * 销毁广播
	 */
	private int click = 0;
	@Override
	protected void onPause() {
		super.onPause();
		/*if(click == 1&&smsReceiver.isInitialStickyBroadcast()){
			unregisterReceiver(smsReceiver);
		}*/
	}
	/**
	 * 注册完成后自动调一次登录的接口，以存储用户信息
	 */
	public void registLogin(){
		GolukDebugUtils.e("","---------registLogin()----------");
		String phone = mEditTextPhone.getText().toString().replace("-", "");
		String pwd = mEditTextPwd.getText().toString();
		String condi = "{\"PNumber\":\"" + phone + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
		boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Login, condi);
		if(b){
			GolukDebugUtils.i("lily", "=======UserRegistActivity====="+b);
			//登录成功跳转
			mApplication.loginStatus=0;//登录中
		}
	}
	/**
	 * 登录的回调
	 */
	public void registLoginCallBack(int success,Object obj){
		GolukDebugUtils.e("","---------------registLoginCallBack()-------------------");
		mApplication.loginStatus=0;//登录中
		if(1 == success){
			try{
				String data = (String)obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				switch (code) {
				case 200:
					//登录成功后，存储用户的登录信息
					mSharedPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putBoolean("FirstLogin", false);
					//提交修改
					mEditor.commit();
					//登录成功跳转
					mApplication.loginStatus=1;//登录成功
					mApplication.isUserLoginSucess = true;
					mApplication.registStatus = 2;//注册成功的状态
					
					Intent it = null;
					if(registOk.equals("fromStart")){
						GolukDebugUtils.i("lily", "========用户未注册2222======");
						it = new Intent(UserRegistActivity.this,MainActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(it);
					}else if(registOk.equals("fromIndexMore")){
						it = new Intent(UserRegistActivity.this,IndexMoreActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(it);
					}else if(registOk.equals("fromSetup")){
						it = new Intent(UserRegistActivity.this,UserSetupActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(it);
					}
					finish();
					break;
					
					default :
					break;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			//回调执行失败
		}
	}
	@Override
	public void identifyCallbackInterface() {
		switch (mApplication.identifyStatus) {
		//验证码获取中
		case 0:
			UserUtils.hideSoftMethod(this);
			mCustomProgressDialogIdentify.show();
			mBtnRegist.setEnabled(false);
			mEditTextPhone.setEnabled(false);
			mEditTextPwd.setEnabled(false);
			mBackButton.setEnabled(false);
			break;
			//获取验证码成功
		case 1:
			closeProgressDialogIdentify();
			GolukUtils.showToast(this, "验证码发送成功");
			
			String phone = mEditTextPhone.getText().toString();
			Intent getIdentify = new Intent(UserRegistActivity.this,UserIdentifyActivity.class);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_DIFFERENT, true);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_PHONE, phone);
			startActivity(getIdentify);
			break;
			//获取验证码失败
		case 2:
			closeProgressDialogIdentify();
			GolukUtils.showToast(mContext, "验证码获取失败");
			break;
			//code=201
		case 3:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, "该手机号1小时内下发6次以上验证码");
			break;
			//code=500
		case 4:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, "服务端程序异常");
			break;
			//code=405
		case 5:
			closeProgressDialogIdentify();
			new AlertDialog.Builder(this)
			.setTitle("提示")
			.setMessage("此手机号已经被注册")
			.setNegativeButton("取消", null)
			.setPositiveButton("立即登录", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					if(mApplication.loginoutStatus = true){
						String phone = mEditTextPhone.getText().toString().replace("-", "");
						mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
						mEditor = mSharedPreferences.edit();
						mEditor.putString("setupPhone", phone);
						mEditor.putBoolean("noPwd", true);
						mEditor.commit();
					}
					finish();
				}
			}).create().show();
			break;
			//code=440
		case 6:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, "输入手机号异常");
			break;
			//code=480
		case 7:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, "验证码发送失败，请重新发送");
			break;
			//code=470
		case 8:
			closeProgressDialogIdentify();
			UserUtils.showDialog(mContext, "获取验证码失败,此手机号已经达到获取验证码上限");
			break;
		default:
			break;
		}
	}
	/**
	 * 获取手机号
	 */
	public void getPhone(){
		if(mApplication.loginoutStatus = true){
			String phone = mEditTextPhone.getText().toString();
			mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
			mEditor = mSharedPreferences.edit();
			mEditor.putString("setupPhone", phone);
			mEditor.putBoolean("noPwd", false);
			mEditor.commit();
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_regist_btn:
			String phoneNumber = mEditTextPhone.getText().toString().replace("-", "");
			String pwd = mEditTextPwd.getText().toString();
			if(!"".equals(phoneNumber) && !"".equals(pwd)){
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					mBtnRegist.setBackgroundResource(R.drawable.icon_login_click);
					break;
				case MotionEvent.ACTION_UP:
					mBtnRegist.setBackgroundResource(R.drawable.icon_login);
					break;
				default:
					break;
				}				
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	/**
	 * 关闭注册中的对话框
	 */
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
		}
	}
	/**
	 * 关闭注册中的对话框
	 */
	private void closeProgressDialogIdentify(){
		if(null != mCustomProgressDialogIdentify){
			mCustomProgressDialogIdentify.close();
			mBtnRegist.setEnabled(true);
			mEditTextPhone.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mBackButton.setEnabled(true);
		}
	}
	
}
