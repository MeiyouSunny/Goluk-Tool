package cn.com.mobnote.golukmobile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.user.CountDownButtonHelper;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.user.CountDownButtonHelper.OnFinishListener;
import cn.com.mobnote.util.console;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 重置密码
 * 
 * 1、输入手机号、密码
 * 2、验证码的获取和判断
 * 3、短信验证
 * 
 * @author mobnote
 *
 */
public class UserRepwdActivity extends BaseActivity implements OnClickListener,OnTouchListener{

	//title
	private ImageButton mBtnBack;
	private TextView mTextViewTitle;
	//手机号、密码、验证码
	private EditText mEditTextPhone,mEditTextPwd,mEditTextIdentify;
	private Button mBtnIdentity,mBtnOK;
	//倒计时的帮助类
	private CountDownButtonHelper mCountDownHelper;
	//自动获取验证码
	private BroadcastReceiver smsReceiver = null;
	private IntentFilter smsFilter;
	private Handler smsHandler;
	private String smsCode;

	private Context mContext = null;
	private GolukApplication mApplication = null;
	//重置密码显示进度条
	private CustomLoadingDialog mCustomProgressDialog=null;//重置密码
	//验证码获取显示进度条
	private CustomLoadingDialog mCustomProgressDialogIdentify = null;//获取验证码
	//判断获取验证码按钮是否已经被点击
	private boolean identifyClick = false;
	/**重置密码获取验证码后台返回的次数**/
	private String freq = "";
	
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null; 
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_repwd);
		
		mContext = this;
		SysApplication.getInstance().addActivity(this);
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserRepwd");
		
		if(null == mCustomProgressDialog){
			mCustomProgressDialog = new CustomLoadingDialog(mContext,"重置中，请稍候……");
		}
		if(null == mCustomProgressDialogIdentify){
			mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, "验证码获取中……");
		}
		initView();
		//title
		mTextViewTitle.setText("重设密码");
		
	}
	public void initView(){
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		mEditTextPhone = (EditText) findViewById(R.id.user_repwd_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_repwd_pwd);
		mEditTextIdentify = (EditText) findViewById(R.id.user_repwd_identify);
		mBtnIdentity = (Button) findViewById(R.id.user_repwd_identify_btn);
		mBtnOK = (Button) findViewById(R.id.user_repwd_ok_btn);
		
		/**
		 * 绑定监听
		 */
		mBtnBack.setOnClickListener(this);
		mBtnIdentity.setOnClickListener(this);
		mBtnOK.setOnClickListener(this);
		mBtnIdentity.setOnTouchListener(this);
		mBtnOK.setOnTouchListener(this);
		
		/**
		 * 登录页密码输入错误超过五次，跳转到重置密码也，并且填入手机号
		 */
		Intent it = getIntent();
		if(null != it.getStringExtra("errorPwdOver")){
			String phone = it.getStringExtra("errorPwdOver").toString();
			mEditTextPhone.setText(phone);
			mBtnIdentity.setBackgroundResource(R.drawable.icon_login);
		}
		
		//手机号输入后，离开立即判断
		mEditTextPhone.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String phone = mEditTextPhone.getText().toString();
				if(!arg1){
					if(!phone.equals("")){
						if(!UserUtils.isMobileNO(phone)){
							UserUtils.showDialog(UserRepwdActivity.this, "手机格式输入错误,请重新输入");
						}
					}else{
						UserUtils.showDialog(UserRepwdActivity.this, "手机号不能为空");
					}
				}
			}
		});
		//密码输入后，离开立即判断
		mEditTextPwd.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String password = mEditTextPwd.getText().toString();
				if(!arg1){
					if(!password.equals("")){
						if(password.length()<6 || password.length()>16){
							UserUtils.showDialog(UserRepwdActivity.this, "密码格式输入不正确,请输入 6-16 位数字、字母，字母区分大小写");
						}
					}else{
						UserUtils.showDialog(UserRepwdActivity.this, "密码不能为空");
					}
				}
			}
		});
		// 验证码输入后，离开立即判断
		mEditTextIdentify.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String identify = mEditTextIdentify.getText().toString();
				if (!arg1) {
					if (!"".equals(identify)) {
						if (identify.length() < 6) {
							UserUtils.showDialog(mContext, "验证码格式输入不正确");
						}
					} else {
						UserUtils.showDialog(mContext, "验证码不能为空");
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
						mBtnIdentity.setBackgroundResource(R.drawable.icon_login);
						mBtnIdentity.setEnabled(true);
					}else{
						//手机号不合法
						mBtnIdentity.setBackgroundResource(R.drawable.icon_more);
						mBtnIdentity.setEnabled(false);
					}
				}else{
					//手机号为空
					mBtnIdentity.setBackgroundResource(R.drawable.icon_more);
					mBtnIdentity.setEnabled(false);
				}
				//重置按钮
				if(!"".equals(phone) && !"".equals(pwd) && !"".equals(identify)){
					mBtnOK.setBackgroundResource(R.drawable.icon_login);
					mBtnOK.setEnabled(true);
				}else{
					mBtnOK.setBackgroundResource(R.drawable.icon_more);
					mBtnOK.setEnabled(false);
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
					mBtnOK.setBackgroundResource(R.drawable.icon_login);
					mBtnOK.setEnabled(true);
				}else{
					mBtnOK.setBackgroundResource(R.drawable.icon_more);
					mBtnOK.setEnabled(false);
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
				String mEditText = mEditTextPhone.getText().toString();
				if(!"".equals(password) && !"".equals(identify)&&!mEditText.equals("")){
					mBtnOK.setBackgroundResource(R.drawable.icon_login);
					mBtnOK.setEnabled(true);
				}else{
					mBtnOK.setBackgroundResource(R.drawable.icon_more);
					mBtnOK.setEnabled(false);
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
	}
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		//返回
		case R.id.back_btn:
			finish();
			break;
		//获取验证码按钮
		case R.id.user_repwd_identify_btn:
			//点击状态:点击后弹出系统短提示:发送中,请稍后;发送后弹出系统短提示:验证码已经发送,请查收短信。
			if(!UserUtils.isNetDeviceAvailable(mContext)){
				console.toast("当前网络不可用，请检查网络后重试", mContext);
			}else{
				getRepwdIdentify();
			}
			break;
		//重设按钮
		case R.id.user_repwd_ok_btn:
			//点按钮后,弹出重置密码中的提示,样式使用系统 loading 样式,文字描述:正在重置
			//重置密码成功,弹出系统短提示:重置密码成功。同时跳转至登录页面。
			repwd();
			break;
		}
	}
	
	/**
	 * 获取重置密码的验证码
	 */
	@SuppressLint("HandlerLeak")
	public void getRepwdIdentify(){

		String phone = mEditTextPhone.getText().toString();
		/**
		 * 自动获取验证码
		 */
		smsHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mEditTextIdentify.setText(smsCode);
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
					smsCode = m.replaceAll("").trim();
					smsHandler.sendEmptyMessage(1);
				}
			}
		};
//		registerReceiver(smsReceiver, smsFilter);
		
		/**
		 * 对手机号、密码进行判断
		 */
		/**
		 * 对获取验证码进行判断
		 */
		if(UserUtils.isMobileNO(phone)){
			String isIdentify = "{\"PNumber\":\"" + phone + "\",\"type\":\"2\"}";
			GolukDebugUtils.e("",isIdentify);
			boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_GetVCode, isIdentify);
			if (b) {
				identifyClick = true;
				UserUtils.hideSoftMethod(this);
				mCustomProgressDialogIdentify.show();
				registerReceiver(smsReceiver, smsFilter);
				click = 1;
				GolukDebugUtils.e("",b + "");
				mBtnOK.setEnabled(false);
				mEditTextPhone.setEnabled(false);
				mEditTextPwd.setEnabled(false);
				mEditTextIdentify.setEnabled(false);
				mBtnBack.setEnabled(false);
			} else {

			}
		} else {
			mBtnIdentity.setEnabled(false);
		}
	}
	/**
	 * 获取验证码回调
	 */
	public void isRepwdCallBack(int success,Object obj){
		GolukDebugUtils.e("","验证码获取回调---isRepwdCallBack---" + success + "---" + obj);
		closeProgressDialogIdentify();
		mEditTextPhone.setEnabled(true);
		mEditTextIdentify.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		mBtnOK.setEnabled(true);
		mBtnBack.setEnabled(true);
//		handler1.removeCallbacks(runnable);
//		console.toast("发送中，请稍后", mContext);
		if(1 == success){
			try{
				String data = (String)obj;
				GolukDebugUtils.e("",data);
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				freq = json.getString("freq");
				/*unregisterReceiver(smsReceiver);
				flag = false;*/
				
				switch (code) {
				case 200:
					console.toast("验证码已经发送，请查收短信", mContext);
					//验证码获取成功
					/**
					 * 点击获取验证码的时候进行倒计时
					 */
					mEditTextPhone.setEnabled(false);
					mCountDownHelper = new CountDownButtonHelper(mBtnIdentity, 60, 1);
					mCountDownHelper.setOnFinishListener(new OnFinishListener() {
						
						@Override
						public void finish() {
							mBtnIdentity.setText("重新获取");
							mEditTextPhone.setEnabled(true);
							mEditTextPhone.setFocusable(true);
							mEditTextPwd.setFocusable(true);
						}
					});
					mCountDownHelper.start();
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
					break;
				case 201:
					UserUtils.showDialog(this, "该手机号1小时内下发5次以上验证码");
					break;
				case 500:
					UserUtils.showDialog(this, "服务端程序异常");
					break;
				case 405:
					String phone =  mEditTextPhone.getText().toString();
					
					mSharedPreferences = getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
					final String just = mSharedPreferences.getString("toRepwd", "");
					GolukDebugUtils.i("lily", "======just====SharedPreferences===="+just);
					
					if(UserUtils.isMobileNO(phone)){
						new AlertDialog.Builder(this)
				        .setMessage("此手机号还未被注册")
						.setNegativeButton("取消", null)
						.setPositiveButton("马上注册", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent intentRepwd = new Intent(UserRepwdActivity.this,UserRegistActivity.class);
								intentRepwd.putExtra("intentRepassword", mEditTextPhone.getText().toString());
								
								if(just.equals("start") || just.equals("mainActivity")){
									intentRepwd.putExtra("fromRegist", "fromStart");
								}else if(just.equals("more")){
									intentRepwd.putExtra("fromRegist", "fromIndexMore");
								}else if(just.equals("set")){
									intentRepwd.putExtra("fromRegist", "fromSetup");
								}
								
								startActivity(intentRepwd);
								finish();
							}
						}).create().show();
					}else{
						UserUtils.showDialog(this, "手机格式输入错误,请重新输入");
					}
					break;
				case 440:
					UserUtils.showDialog(this, "输入手机号异常");
					break;
				case 480:
					UserUtils.showDialog(this, "验证码获取失败");
					break;
				case 470:
					UserUtils.showDialog(mContext, "获取验证码已达上限");
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
			console.toast("验证码获取失败", mContext);
		}
	}
	/**
	 * 重置密码
	 */
	public void repwd(){
		String phone = mEditTextPhone.getText().toString();
		String password = mEditTextPwd.getText().toString();
		String identify = mEditTextIdentify.getText().toString();
		if(!"".equals(phone) && UserUtils.isMobileNO(phone)){
			if (!"".equals(password) && !"".equals(identify)) {
				mBtnOK.setFocusable(true);
				if (password.length() >= 6 && password.length() <= 16) {
					if (!UserUtils.isNetDeviceAvailable(this)) {
						console.toast("当前网络不可用，请检查网络后重试", mContext);
					} else {
						// {PNumber：“13054875692”，Password：“xxx”，VCode：“1234”}
						String isRegist = "{\"PNumber\":\"" + phone+ "\",\"Password\":\"" + password+ "\",\"VCode\":\"" + identify+ "\",\"tag\":\"android\"}";
						GolukDebugUtils.e("",isRegist);
						if(identifyClick){
							int freqInt = Integer.valueOf(freq);
							GolukDebugUtils.i("lily", "---------重置密码获取验证码的次数----"+freqInt);
							if(freqInt>3){
								UserUtils.showDialog(mContext, "获取验证码失败,此手机号已经达到获取验证码上限(每天 3 次)");
							}else{
								if(identify.length()<6){
									UserUtils.showDialog(mContext, "验证码格式输入不正确");
								}else{
									boolean b = mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_ModifyPwd, isRegist);
									GolukDebugUtils.e("",b + "");
									if (b) {
										// 隐藏软件盘
										UserUtils.hideSoftMethod(this);
										mCustomProgressDialog.show();
										mEditTextPhone.setEnabled(false);
										mEditTextIdentify.setEnabled(false);
										mEditTextPwd.setEnabled(false);
										mBtnIdentity.setEnabled(false);
										mBtnBack.setEnabled(false);
										mBtnOK.setEnabled(false);
									}
								}
							}
						}else{
							console.toast("请先获取验证码", mContext);
						}
					}
				} else {
					mBtnOK.setFocusable(true);
					UserUtils.showDialog(UserRepwdActivity.this,"密码格式输入不正确,请输入 6-16 位数字、字母，字母区分大小写");
				}
			}
		}else{
			UserUtils.showDialog(mContext, "手机格式输入错误");
		}
		
	}
	/**
	 * 重置密码回调
	 */
	public void repwdCallBack(int success,Object outTime,Object obj){
		GolukDebugUtils.e("","---重置密码回调-----"+success+"----"+obj);
		closeProgressDialog();
		mEditTextPhone.setEnabled(true);
		mEditTextIdentify.setEnabled(true);
		mEditTextPwd.setEnabled(true);
		mBtnIdentity.setEnabled(true);
		mBtnBack.setEnabled(true);
		mBtnOK.setEnabled(true);
		int codeOut = (Integer) outTime;
		if(1 == success){
			try{
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				GolukDebugUtils.e("",code+"");
				
				switch (code) {
				case 200:
					//重置密码成功
					console.toast("重置密码成功", mContext);
					/*Intent it = new Intent(UserRepwdActivity.this,UserLoginActivity.class);
					it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					it.putExtra("repwdOk", mEditTextPhone.getText().toString());
					GolukDebugUtils.i("lily", "--------密码错误，重置密码成功------"+mEditTextPhone.getText().toString());
					startActivity(it);
					this.finish();*/
					putPhone();
					this.finish();
					break;
				case 500:
					UserUtils.showDialog(this, "服务端程序异常");
					break;
				case 405:
					String phone = mEditTextPhone.getText().toString();
					
					if(UserUtils.isMobileNO(phone) && phone.startsWith("1")){
						new AlertDialog.Builder(this)
				        .setMessage("此手机号还未被注册")
						.setNegativeButton("取消", null)
						.setPositiveButton("马上注册", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent intentRepwd = new Intent(UserRepwdActivity.this,UserRegistActivity.class);
								intentRepwd.putExtra("intentRepassword", mEditTextPhone.getText().toString());
								startActivity(intentRepwd);
								finish();
							}
						}).create().show();
					}else{
						UserUtils.showDialog(this, "手机格式输入错误,请重新输入");
					}
					
					break;
				case 406:
					if(identifyClick){
						UserUtils.showDialog(this, "请输入正确的验证码");
					}else{
						console.toast("请先获取验证码", mContext);
					}
					break;
				case 407:
					String phones = mEditTextPhone.getText().toString();
					if(UserUtils.isMobileNO(phones)){
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
			//网络超时当重试按照3、6、9、10s的重试机制，当网络链接超时时
			GolukDebugUtils.i("outtime", "-----网络链接超时超时超时-------xxxx---"+codeOut);
			console.toast("网络连接超时", mContext);
			switch (codeOut) {
			case 1:
				
				break;
			case 2:
				
				break;
			case 3:
				
				break;
			default:
				break;
			}
		}
	}
	
	private int click = 0;
	/*final Handler handler1=new Handler();
	private Runnable runnable;
	private void initTimer(){
		runnable=new Runnable(){
		@Override
		public void run() {
			console.toast("网络链接超时", mContext);
			mLoading.setVisibility(View.GONE);
			}
		};
	}*/
	
	/**
	 * 销毁广播
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(click == 1&&smsReceiver.isInitialStickyBroadcast()){
			unregisterReceiver(smsReceiver);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_repwd_ok_btn:
			String phoneNumber = mEditTextPhone.getText().toString();
			String pwd = mEditTextPwd.getText().toString();
			String identify = mEditTextIdentify.getText().toString();
			if(!"".equals(phoneNumber) && !"".equals(pwd) && !"".equals(identify)){
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					mBtnOK.setBackgroundResource(R.drawable.icon_login_click);
					break;
				case MotionEvent.ACTION_UP:
					mBtnOK.setBackgroundResource(R.drawable.icon_login);
					break;

				default:
					break;
				}
			}
			break;
		case R.id.user_repwd_identify_btn:
			String phone = mEditTextPhone.getText().toString();
			if(!"".equals(phone) && UserUtils.isMobileNO(phone)){
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					mBtnIdentity.setBackgroundResource(R.drawable.icon_login_click);
					break;
				case MotionEvent.ACTION_UP:
					mBtnIdentity.setBackgroundResource(R.drawable.icon_login);
					break;

				default:
					break;
				}
				break;
			}
		default:
			break;
		}
		return false;
	}
	
	public void putPhone(){
		String phone = mEditTextPhone.getText().toString();
		mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		GolukDebugUtils.i("lily", "phone=="+phone);
		mEditor.putString("setupPhone", phone);
		mEditor.putBoolean("noPwd", true);
		mEditor.commit();
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
