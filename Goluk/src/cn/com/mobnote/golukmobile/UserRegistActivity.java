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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.CountDownButtonHelper;
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
public class UserRegistActivity extends Activity implements OnClickListener {

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
	//注册获取验证码显示进度条
	private RelativeLayout mIdentifyLoading = null;
	//判断获取验证码按钮是否被点击过
	private boolean identifyClick = false;

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
		 * 监听绑定
		 */
		// 返回
		mBackButton.setOnClickListener(this);
		// 注册按钮
		mBtnRegist.setOnClickListener(this);
		//手机号、密码、验证码文本框改变监听
		mEditTextPhone.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String phone = mEditTextPhone.getText().toString();
				if(!arg1){
					if(!phone.equals("")){
						if(!UserUtils.isMobileNO(phone)){
//								console.toast("手机号格式不对", mContext);
//							mEditTextPhone.setError("手机号格式不正确");
							UserUtils.showDialog(UserRegistActivity.this, "手机格式输入错误,请重新输入");
						}
					}
				}
			}
		});
		
		mEditTextPhone.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhone.getText().toString();
				if(!"".equals(phone)){
					if(phone.length() == 11 && phone.startsWith("1")){ 
						mBtnIdentify.setBackgroundResource(R.drawable.icon_login);
						mBtnIdentify.setEnabled(true);
						if(!UserUtils.isMobileNO(phone)){
							UserUtils.showDialog(UserRegistActivity.this, "手机格式输入错误,请重新输入");
						}
						
					}else{
						mBtnIdentify.setBackgroundResource(R.drawable.icon_more);
						mBtnIdentify.setEnabled(false);
					}
				}else{
					//手机号为空
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
				String password = mEditTextPwd.getText().toString();
				String identify = mEditTextIdentify.getText().toString();
				
				if(!"".equals(password) && !"".equals(identify)){
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
		// 登录
		mTextViewLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
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
			getIdentify();
			break;
		// 登陆
		case R.id.user_regist_login:
			Intent itLogin = new Intent(UserRegistActivity.this,UserLoginActivity.class);
			startActivity(itLogin);
			break;
		}
	}
	/**
	 * 获取验证码
	 */
	@SuppressLint("HandlerLeak")
	public void getIdentify(){
		String phone = mEditTextPhone.getText().toString();
		String password= mEditTextPwd.getText().toString();
		
		/**
		 * 自动获取验证码
		 */
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
//				TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//				String localPhoneNumber = telManager.getLine1Number();
//				if(localPhoneNumber.equals(mEditTextPhone.getText().toString())){
					mEditTextIdentify.setText(strBody);
				/*}else{
					mEditTextIdentify.setText("");
				}*/
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
					
					// 服务端发送短息的手机号。。+86开头？
//					String from = sms.getOriginatingAddress();
//					String from = "10690148001667";//goluk发送验证码的服务端号码
				}
			}
		};
//		registerReceiver(smsReceiver, smsFilter);
		
		/**
		 * 对获取验证码进行判断
		 */
		if(UserUtils.isMobileNO(phone)){
			mEditTextPhone.setEnabled(false);
			mEditTextIdentify.setEnabled(false);
			mEditTextPwd.setEnabled(false);
			String isIdentify = "{\"PNumber\":\"" + phone + "\",\"type\":\"1\"}";
			console.log(isIdentify);
			boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_GetVCode, isIdentify);
			identifyClick = true;
			UserUtils.hideSoftMethod(this);
			mIdentifyLoading.setVisibility(View.VISIBLE);
			registerReceiver(smsReceiver, smsFilter);
			click = 1;
			console.log(b + "");
			mBtnRegist.setEnabled(true);
		}else{
			mBtnIdentify.setEnabled(false);
//			UserUtils.showDialog(this, "手机格式输入错误,请重新输入");
		}
		
	}
	
	/**
	 * 验证码回调
	 */
	public void identifyCallback(int success,Object obj){
		console.log("验证码获取回调---identifyCallBack---" + success + "---" + obj);
		//点击验证码按钮手机号、密码不可被修改
		mEditTextPhone.setEnabled(true);
		mEditTextIdentify.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		handler1.removeCallbacks(runnable);
		if(1 == success){
			try{
				String data = (String)obj;
				console.log(data);
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
//				String msg = json.getString("msg");
				
				/*unregisterReceiver(smsReceiver);
				click = 2;*/
				mIdentifyLoading.setVisibility(View.GONE);
				switch (code) {
				case 200:
					console.toast("验证码已经发送，请查收短信", mContext);
					//验证码获取成功
					/**
					 * 点击获取验证码的时候进行倒计时
					 */
					mEditTextPhone.setEnabled(false);
					mCountDownhelper = new CountDownButtonHelper(mBtnIdentify, 60, 1);
					mCountDownhelper.setOnFinishListener(new OnFinishListener() {
						@Override
						public void finish() {
							// TODO Auto-generated method stub
							mBtnIdentify.setText("再次发送");
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
					.setTitle("Goluk温馨提示：")
					.setMessage("此手机号已经被注册啦!")
					.setNegativeButton("取消", null)
					.setPositiveButton("立即登录", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							Intent itRegist = new Intent(UserRegistActivity.this,UserLoginActivity.class);
							itRegist.putExtra("intentRegist", mEditTextPhone.getText().toString());
							startActivity(itRegist);
						}
					}).create().show();
					break;

				case 440:
					UserUtils.showDialog(this, "输入手机号异常");
					break;
				case 480:
					UserUtils.showDialog(this, "验证码获取失败");
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
		if(!"".equals(password) && !"".equals(identify)){
			mBtnRegist.setFocusable(true);
			if(password.length()>=6 && password.length()<=16){
				if(!UserUtils.isNetDeviceAvailable(mContext)){
					console.toast("当前网络状态不佳，请检查网络后重试", mContext);
				}else{
					//初始化定时器
				initTimer();
				handler1.postDelayed(runnable, 3000);//三秒执行一次runnable.
				//{PNumber：“13054875692”，Password：“XXX”，VCode：“1234”}
				String isRegist = "{\"PNumber\":\"" + phone + "\",\"Password\":\""+password+"\",\"VCode\":\""+identify+ "\",\"tag\":\"android\"}";
				console.log(isRegist);
				boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_Register, isRegist);
				
				console.log(b+"");
				if(b){
					//隐藏软件盘
					UserUtils.hideSoftMethod(this);
					mLoading.setVisibility(View.VISIBLE);
					mEditTextPhone.setEnabled(false);
					mEditTextIdentify.setEnabled(false);
					mEditTextPwd.setEnabled(false);
				}
		}
			}else{
			mBtnRegist.setFocusable(false);
//			UserUtils.showDialog(this, "密码格式输入不正确，请输入 6-16 位数字、字母，字母区分大小写");
		}
		}
	}
	
	/**
	 * 注册回调
	 */
	public void registCallback(int success,Object obj){
		handler1.removeCallbacks(runnable);
		mEditTextPhone.setEnabled(true);
		mEditTextIdentify.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		console.log("注册回调---registCallback---"+success+"---"+obj);
		if(1 == success){
			try{
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				console.log(code+"");
//				String msg = json.getString("msg");
				
				mLoading.setVisibility(View.GONE);
				switch (code) {
				case 200:
					//注册成功
					SysApplication.getInstance().exit();//杀死之前的所有activity，实现一键退出
					console.toast("注册成功", mContext);
					Intent it = new Intent(UserRegistActivity.this,MainActivity.class);
					startActivity(it);
					break;
				case 500:
					UserUtils.showDialog(this, "服务端程序异常");
					break;
				case 405:
					UserUtils.showDialog(this, "用户已注册");
					break;
				case 406:
//					UserUtils.showDialog(this, "请输入正确的验证码");
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
//					UserUtils.showDialog(this, "验证码获取失败");
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
			console.log("注册失败");
		}
	}
	/**
	 * 销毁广播
	 */
	private int click = 0;
	final Handler handler1=new Handler();
	private Runnable runnable;
	private void initTimer(){
		runnable=new Runnable(){
		@Override
		public void run() {
			console.toast("当前网络状态不佳，请检查网络后重试", mContext);
			mLoading.setVisibility(View.GONE);
			}
		};
	}
	@Override
	protected void onPause() {
		super.onPause();
		if(click == 1&&smsReceiver.isInitialStickyBroadcast()){
			unregisterReceiver(smsReceiver);
		}
	}
//	@Override
	/*protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i("bug", "==========regist");
		if(click == 1){
			unregisterReceiver(smsReceiver);
		}
	}*/
}
