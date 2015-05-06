package cn.com.mobnote.golukmobile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.CountDownButtonHelper;
import cn.com.mobnote.user.UserIdentifyInterface;
import cn.com.mobnote.user.UserRegistInterface;
import cn.com.mobnote.user.CountDownButtonHelper.OnFinishListener;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.console;
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
public class UserRegistActivity extends Activity implements OnClickListener,UserRegistInterface,UserIdentifyInterface,OnTouchListener {

	// 注册title
	private Button mBackButton;
	private TextView mTextViewTitle;
	// 手机号、密码、注册按钮
	private EditText mEditTextPhone, mEditTextPwd;
	private Button mBtnRegist;
	// 验证码
	private Button mBtnIdentify;
	private EditText mEditTextIdentify;
	// 登陆
	private TextView mTextViewLogin;
	
	private Context mContext = null;
	private GolukApplication mApplication = null;
	
	//倒计时帮助类
	private CountDownButtonHelper mCountDownhelper;
	//自动获取验证码
	private BroadcastReceiver smsReceiver;
	private IntentFilter smsFilter;
	private Handler handler;
	private String strBody;
	//注册进度条
	private RelativeLayout mLoading = null;
	private CustomLoadingDialog mCustomProgressDialog=null;//注册
	//注册获取验证码显示进度条
	private RelativeLayout mIdentifyLoading = null;
	private CustomLoadingDialog mCustomProgressDialogIdentify = null;//获取验证码
	//判断获取验证码按钮是否被点击过
	private boolean identifyClick = false;
	/**记录注册成功的状态**/
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	/**注册成功跳转页面的判断标志*/
	private String registOk = null;
	/**获取验证码的次数**/
	private String freq = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_regist);
	}
	@Override
	protected void onResume() {
		super.onResume();
		mContext = this;
		SysApplication.getInstance().addActivity(this);
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserRegist");
		
		if(null == mCustomProgressDialog){
			mCustomProgressDialog = new CustomLoadingDialog(mContext,"注册中，请稍候……");
		}
		if(null == mCustomProgressDialogIdentify){
			mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, "验证码获取中……");
		}
		initView();
		// title
		mTextViewTitle.setText("注册");
	}
	@SuppressLint("ResourceAsColor")
	public void initView() {
		// title
		mBackButton = (Button) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		// 手机号、密码、注册按钮
		mEditTextPhone = (EditText) findViewById(R.id.user_regist_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_regist_pwd);
		mBtnRegist = (Button) findViewById(R.id.user_regist_btn);
		// 验证码
		mBtnIdentify = (Button) findViewById(R.id.user_regist_identify_btn);
		mEditTextIdentify = (EditText) findViewById(R.id.user_regist_identify);
		// 登陆
		mTextViewLogin = (TextView) findViewById(R.id.user_regist_login);
		//注册按钮进度条
		mLoading = (RelativeLayout) findViewById(R.id.loading_layout);
		//获取验证码进度条
		mIdentifyLoading = (RelativeLayout) findViewById(R.id.loading_identify);

		Intent itLoginPhone = getIntent();
		if(null != itLoginPhone.getStringExtra("intentLogin")){
			String number = itLoginPhone.getStringExtra("intentLogin").toString();
			Log.i("user", number);
			mEditTextPhone.setText(number);
			mBtnIdentify.setBackgroundResource(R.drawable.icon_login);
		}
		Intent itRepassword = getIntent(); 
		if(null != itRepassword.getStringExtra("intentRepassword")){
			String repwdNum = itRepassword.getStringExtra("intentRepassword").toString();
			mEditTextPhone.setText(repwdNum);
			mBtnIdentify.setBackgroundResource(R.drawable.icon_login);
		}
		
		/**
		 *	判断是从哪个入口进行的注册 
		 */
		Intent itRegist = getIntent();
		if(null != itRegist.getStringExtra("fromRegist")){
			registOk = itRegist.getStringExtra("fromRegist").toString();
		}
		
		/**
		 * 注册  --->  退出 --->  再次进入  ----->  登录页面获得注册传来的phone
		 */
		/*if(mApplication.loginoutStatus = true){
			String phone = mEditTextPhone.getText().toString();
			mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
			mEditor = mSharedPreferences.edit();
			mEditor.putString("setupPhone", phone);
			mEditor.commit();
		}*/
		getPhone();
		
		/**
		 * 监听绑定
		 */
		// 返回
		mBackButton.setOnClickListener(this);
		// 注册按钮
		mBtnRegist.setOnClickListener(this);
		mBtnRegist.setOnTouchListener(this);
		//手机号、密码、验证码文本框改变监听
		mEditTextPhone.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String phone = mEditTextPhone.getText().toString();
				if(!arg1){
					if(!phone.equals("")){
						if(!UserUtils.isMobileNO(phone)){
							UserUtils.showDialog(UserRegistActivity.this, "手机格式输入错误,请重新输入");
						}
					}else{
						UserUtils.showDialog(UserRegistActivity.this, "手机号不能为空");
					}
				}
			}
		});
		
		// 密码输入后，离开立即判断
		mEditTextPwd.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String password = mEditTextPwd.getText().toString();
				if (!arg1) {
					if (!password.equals("")) {
						if (password.length() < 6 || password.length() > 16) {
							UserUtils.showDialog(UserRegistActivity.this,"密码格式输入不正确,请输入 6-16 位数字、字母，字母区分大小写");
						}
					}else{
						UserUtils.showDialog(UserRegistActivity.this, "密码不能为空");
					}
				}
			}
		});
		
		mEditTextPhone.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhone.getText().toString();
				String pwd = mEditTextPwd.getText().toString();
				String identify = mEditTextIdentify.getText().toString();
				if(!"".equals(phone)){
					if(phone.length() == 11 && phone.startsWith("1") && UserUtils.isMobileNO(phone)){
						mBtnIdentify.setBackgroundResource(R.drawable.icon_login);
						mBtnIdentify.setEnabled(true);
					}else{
						//手机号非法，获取验证码按钮不可点击
						mBtnIdentify.setBackgroundResource(R.drawable.icon_more);
						mBtnIdentify.setEnabled(false);
					}
				}else{
					//手机号为空
					mBtnIdentify.setBackgroundResource(R.drawable.icon_more);
					mBtnIdentify.setEnabled(false);
				}
				//注册按钮
				if(!"".equals(phone) && !"".equals(pwd) && !"".equals(identify)){
					mBtnRegist.setBackgroundResource(R.drawable.icon_login);
					mBtnRegist.setEnabled(true);
				}else{
					mBtnRegist.setBackgroundResource(R.drawable.icon_more);
					mBtnRegist.setEnabled(false);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		mEditTextIdentify.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String password = mEditTextPwd.getText().toString();
				String identify = mEditTextIdentify.getText().toString();
				String phone = mEditTextPhone.getText().toString();
				if(!"".equals(password) && !"".equals(identify)&&!phone.equals("")){
					mBtnRegist.setBackgroundResource(R.drawable.icon_login);
					mBtnRegist.setEnabled(true);
				}else{
					mBtnRegist.setBackgroundResource(R.drawable.icon_more);
					mBtnRegist.setEnabled(false);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) { }
			
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		mEditTextPwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhone.getText().toString();
				String pwd = mEditTextPwd.getText().toString();
				String identify = mEditTextIdentify.getText().toString();
				//注册按钮
				if(!"".equals(phone) && !"".equals(pwd) && !"".equals(identify)){
					mBtnRegist.setBackgroundResource(R.drawable.icon_login);
					mBtnRegist.setEnabled(true);
				}else{
					mBtnRegist.setBackgroundResource(R.drawable.icon_more);
					mBtnRegist.setEnabled(false);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		mBtnIdentify.setOnClickListener(this);
		mBtnIdentify.setOnTouchListener(this);
		// 登录
		mTextViewLogin.setOnClickListener(this);
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
			regist();
			break;
		// 获取验证码按钮
		case R.id.user_regist_identify_btn:
			if(!UserUtils.isNetDeviceAvailable(mContext)){
				console.toast("当前网络不可用，请检查网络后重试", mContext);
			}else{	
				getIdentify();
			}
			break;
		// 登陆
		case R.id.user_regist_login:
			finish();
			break;
		}
	}
	/**
	 * 获取验证码
	 */
	@SuppressLint("HandlerLeak")
	public void getIdentify(){
		String phone = mEditTextPhone.getText().toString();
		
		/**
		 * 自动获取验证码
		 */
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
		/**
		 * 对获取验证码进行判断
		 */
		if(!"".equals(phone) && UserUtils.isMobileNO(phone)){
			String isIdentify = "{\"PNumber\":\"" + phone + "\",\"type\":\"1\"}";
			console.log(isIdentify);
			boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_GetVCode, isIdentify);
			if(b){
				identifyClick = true;
				UserUtils.hideSoftMethod(this);
//				mIdentifyLoading.setVisibility(View.VISIBLE);
				mCustomProgressDialogIdentify.show();
				registerReceiver(smsReceiver, smsFilter);
				click = 1;
				console.log(b + "");
				mBtnRegist.setEnabled(false);
				mEditTextPhone.setEnabled(false);
				mEditTextPwd.setEnabled(false);
				mEditTextIdentify.setEnabled(false);
				mBackButton.setEnabled(false);
				mTextViewLogin.setEnabled(false);
			} else {

			}
		}else{
			mBtnIdentify.setEnabled(false);
		}
		
	}
	
	/**
	 * 验证码回调
	 */
	public void identifyCallback(int success,Object obj){
		console.log("验证码获取回调---identifyCallBack---" + success + "---" + obj);
//		mIdentifyLoading.setVisibility(View.GONE);
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
				console.log(data);
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				freq = json.getString("freq");
				Log.i("lily", "------freq------"+freq);
				switch (code) {
				case 200:
					console.toast("验证码已经发送，请查收短信", mContext);
					
					if(freq.equals("2")){//第二次获取验证码
						new AlertDialog.Builder(mContext)
						.setMessage("此手机号还有 1 次获取验证码的机会,请确保手机号码正确和手机号所在的设备有信号")
						.setPositiveButton("确定", null)
						.create().show();
					}else if(freq.equals("3")){//第三次获取验证码
						new AlertDialog.Builder(mContext)
						.setMessage("此手机号之后已经不能再获取验证码,请确保手机号码正确和手机号所在的设备有信号")
						.setPositiveButton("确定", null)
						.create().show();
					}
					//验证码获取成功
					/**
					 * 点击获取验证码的时候进行倒计时
					 */
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
							getPhone();
							finish();
						}
					}).create().show();
					break;

				case 440:
					UserUtils.showDialog(this, "输入手机号异常");
					break;
				case 480:
					UserUtils.showDialog(this, "验证码获取失败");
					break;
				case 470:
					UserUtils.showDialog(mContext, "获取验证码失败,此手机号已经达到获取验证码上限");
					break;
				default:
					break;
				}
				/*unregisterReceiver(smsReceiver);
				click = 2;*/
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		else{
			console.toast("验证码获取失败", mContext);
//			mBtnIdentify.setBackgroundResource(R.drawable.icon_login);
			mBtnIdentify.setText("重新获取");
		}
	}
	
	/**
	 * 注册
	 */
	public void regist(){
		String phone = mEditTextPhone.getText().toString();
		String password = mEditTextPwd.getText().toString();
		String identify = mEditTextIdentify.getText().toString();
		
		if(!"".equals(phone) && UserUtils.isMobileNO(phone)){
			if(!"".equals(password) && !"".equals(identify)){
				mBtnRegist.setEnabled(true);
				if(password.length()>=6 && password.length()<=16){
					if(!UserUtils.isNetDeviceAvailable(mContext)){
						console.toast("当前网络不可用，请检查网络后重试", mContext);
					}else{
					//{PNumber：“13054875692”，Password：“xxx”，VCode：“1234”}
					String isRegist = "{\"PNumber\":\"" + phone + "\",\"Password\":\""+password+"\",\"VCode\":\""+identify+ "\",\"tag\":\"android\"}";
					console.log(isRegist);
					Log.i("lily", "------UserRegistActivity---不点击获取验证码---111------"+freq);
					if(identifyClick){
						int freqInt = Integer.parseInt(freq);
						Log.i("lily", "------UserRegistActivity---不点击获取验证码---------"+freq);
						if(freqInt>3){
							UserUtils.showDialog(mContext, "获取验证码失败,此手机号已经达到获取验证码上限(每天 3 次)");
						}else{
							boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_Register, isRegist);
							if(b){
								mApplication.registStatus = 1;//注册中……
								//隐藏软件盘
								UserUtils.hideSoftMethod(this);
//								mLoading.setVisibility(View.VISIBLE);
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
					}else{
						console.toast("请先获取验证码", mContext);
					}
				}
				}else{
					UserUtils.showDialog(UserRegistActivity.this,"密码格式输入不正确,请输入 6-16 位数字、字母，字母区分大小写");
					mBtnRegist.setEnabled(true);
			}
		}
		}else{
			mBtnRegist.setEnabled(false);
//			UserUtils.showDialog(mContext, "手机格式输入错误，请重新输入");
		}
		
}
	
	/**
	 * 注册回调
	 */
	public void registCallback(int success,Object outTime,Object obj){
		int codeOut = (Integer) outTime;
		console.log("注册回调---registCallback---"+success+"---"+obj);
//		mLoading.setVisibility(View.GONE);
		closeProgressDialog();
		mEditTextPhone.setEnabled(true);
		mEditTextIdentify.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		mBtnIdentify.setEnabled(true);
		mTextViewLogin.setEnabled(true);
		mBackButton.setEnabled(true);
		mBtnRegist.setEnabled(true);
		if(1 == success){
			try{
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				console.log(code+"");
				
				switch (code) {
				case 200:
					//注册成功
					console.toast("注册成功", mContext);
					mApplication.registStatus = 2;//注册成功的状态
					//注册成功后再次调用登录的接口
					registLogin();
					Intent it = null;
					if(registOk.equals("fromStart")){
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
						console.toast("请先获取验证码", mContext);
					}
					break;
				case 407:
					String phone = mEditTextPhone.getText().toString();
					if(UserUtils.isMobileNO(phone)){
						if(identifyClick){
							UserUtils.showDialog(this, "输入验证码超时");
						}else{
							console.toast("请先获取验证码", mContext);
						}
					}else{
						UserUtils.showDialog(this, "手机格式输入错误,请重新输入");
					}
					break;
				case 480:
					if(identifyClick){
						UserUtils.showDialog(this, "验证码获取失败");
					}else{
						console.toast("请先获取验证码", mContext);
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
			android.util.Log.i("outtime", "-----网络链接超时超时超时" + codeOut);
//			console.toast("当前网络状况不佳，请检查网络", mContext);
			console.toast("网络连接超时", mContext);
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
		if(click == 1&&smsReceiver.isInitialStickyBroadcast()){
			unregisterReceiver(smsReceiver);
		}
	}
	/**
	 * 注册完成后自动调一次登录的接口，以存储用户信息
	 */
	public void registLogin(){
		console.log("---------registLogin()----------");
		String phone = mEditTextPhone.getText().toString();
		String pwd = mEditTextPwd.getText().toString();
		String condi = "{\"PNumber\":\"" + phone + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
		boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Login, condi);
		if(b){
			Log.i("yyy", "=======UserRegistActivity====="+b);
			//---------------------------登录成功的状态  1-------------------------
			//登录成功跳转
			mApplication.loginStatus=1;//登录成功
			mApplication.isUserLoginSucess = true;
		}else{
			
		}
	}
	/**
	 * 登录的回调
	 */
	public void registLoginCallBack(int success,Object obj){
		console.log("---------------registLoginCallBack()-------------------");
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
					//---------------------------登录成功的状态  1----------------------------
					//登录成功跳转
					mApplication.loginStatus=1;//登录成功
					mApplication.isUserLoginSucess = true;
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
	public void registStatusChange() {
		
	}
	@Override
	public void identifyCallbackInterface() {
		
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
			mEditor.commit();
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_regist_btn:
			String phoneNumber = mEditTextPhone.getText().toString();
			String pwd = mEditTextPwd.getText().toString();
			String identify = mEditTextIdentify.getText().toString();
			if(!"".equals(phoneNumber) && !"".equals(pwd) && !"".equals(identify)){
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
		case R.id.user_regist_identify_btn:
			String phone = mEditTextPhone.getText().toString();
			if(!"".equals(phone) && UserUtils.isMobileNO(phone)){
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					mBtnIdentify.setBackgroundResource(R.drawable.icon_login_click);
					break;
				case MotionEvent.ACTION_UP:
					mBtnIdentify.setBackgroundResource(R.drawable.icon_login);
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
		}
	}
	
}
