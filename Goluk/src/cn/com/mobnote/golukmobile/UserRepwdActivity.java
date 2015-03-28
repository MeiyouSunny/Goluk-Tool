package cn.com.mobnote.golukmobile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R.id;
import cn.com.mobnote.user.CountDownButtonHelper;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.user.CountDownButtonHelper.OnFinishListener;
import cn.com.mobnote.util.console;
import cn.com.mobonote.golukmobile.comm.GolukMobile;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class UserRepwdActivity extends Activity implements OnClickListener{

	//title
	private Button mBtnBack;
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
	private RelativeLayout mLoading = null ;
	//验证码获取显示进度条
	private RelativeLayout mIdentifyLoading = null;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_repwd);
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserRepwd");
		
		initView();
		//title
		mTextViewTitle.setText("重设密码");
		
	}
	public void initView(){
		mBtnBack = (Button) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		mEditTextPhone = (EditText) findViewById(R.id.user_repwd_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_repwd_pwd);
		mEditTextIdentify = (EditText) findViewById(R.id.user_repwd_identify);
		mBtnIdentity = (Button) findViewById(R.id.user_repwd_identify_btn);
		mBtnOK = (Button) findViewById(R.id.user_repwd_ok_btn);
		mLoading = (RelativeLayout) findViewById(R.id.loading_layout);
		mIdentifyLoading = (RelativeLayout) findViewById(R.id.loading_identify);
		
		/**
		 * 绑定监听
		 */
		mBtnBack.setOnClickListener(this);
		mBtnIdentity.setOnClickListener(this);
		mBtnOK.setOnClickListener(this);
		
		/**
		 * 手机号、密码、验证码文本框改变监听
		 */
		mEditTextPhone.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				String pwd = mEditTextPwd.getText().toString();
				String identify = mEditTextIdentify.getText().toString();
				if ("".equals(arg0.toString())) {
					if ("".equals(pwd)) {
						// 显示普通按钮
						mBtnOK.setBackgroundResource(R.drawable.icon_more);
						mBtnIdentity.setBackgroundResource(R.drawable.icon_more);
					}
				} else {
					if (!"".equals(pwd)) {
						if (!"".equals(identify)) {
							// 显示高亮重置密码按钮
							mBtnOK.setBackgroundResource(R.drawable.icon_login);
						} else {
							// 显示高亮重置密码按钮
							mBtnOK.setBackgroundResource(R.drawable.icon_more);
						}
						mBtnIdentity.setBackgroundResource(R.drawable.icon_login);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		mEditTextPwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				String phone = mEditTextPhone.getText().toString();
				String identify = mEditTextIdentify.getText().toString();
				if ("".equals(arg0.toString())) {
					if ("".equals(phone) || phone.length() != 11) {
						// 显示普通按钮
						mBtnOK.setBackgroundResource(R.drawable.icon_more);
						mBtnIdentity.setBackgroundResource(R.drawable.icon_more);
					}
				} else {
					if (!"".equals(phone) && phone.length() == 11) {
						if (!"".equals(identify)) {
							// 显示高亮重置密码按钮
							mBtnOK.setBackgroundResource(R.drawable.icon_login);
						} else {
							// 显示高亮重置密码按钮
							mBtnOK.setBackgroundResource(R.drawable.icon_more);
						}
						mBtnIdentity.setBackgroundResource(R.drawable.icon_login);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		mEditTextIdentify.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				String phone = mEditTextPhone.getText().toString();
				String pwd = mEditTextPwd.getText().toString();
				if ("".equals(arg0.toString())) {
					if ("".equals(phone)) {
						mBtnOK.setBackgroundResource(R.drawable.icon_more);
					}
				} else {
					if (!"".equals(phone)) {
						if (!"".equals(pwd)) {
							mBtnOK.setBackgroundResource(R.drawable.icon_login);
						} else {
							mBtnOK.setBackgroundResource(R.drawable.icon_more);
						}
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
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
			getRepwdIdentify();
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
		String password= mEditTextPwd.getText().toString();
		/**
		 * 自动获取验证码
		 */
		smsHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				//获取本机手机号
				TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String localPhoneNumber = telManager.getLine1Number();
				console.log(localPhoneNumber);
//				if(localPhoneNumber.equals(mEditTextPhone.getText().toString())){
					mEditTextIdentify.setText(smsCode);
//				}else{
//					mEditTextIdentify.setText("");
//				}
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
					// 服务端发送短息的手机号。。+86开头？
//					String from = sms.getOriginatingAddress();
//					String from = "10690148001667";
//					console.log(from);
				}
			}
		};
//		registerReceiver(smsReceiver, smsFilter);
		
		/**
		 * 对手机号、密码进行判断
		 */
		if(!"".equals(phone)){
			if(phone.startsWith("1") && phone.length() == 11){
				if(!"".equals(password)){
					if(password.length()>=6 && password.length()<=16){
						String isIdentify = "{\"PNumber\":\"" + phone  + "\",\"type\":\"2\"}";
						console.log(isIdentify);
						boolean b = mApplication.mGoluk.GoLuk_CommonGetPage(GolukMobile.PageType_GetVCode, isIdentify);
						if(b){
							UserUtils.hideSoftMethod(this);
							mIdentifyLoading.setVisibility(View.VISIBLE);
							console.log(b+"");
							registerReceiver(smsReceiver, smsFilter);
							flag = true;
							//点击获取验证码手机号、密码不可被点击
							mEditTextPhone.setFocusable(false);
							mEditTextPwd.setFocusable(false);
						}
						
					}else{
						UserUtils.showDialog(this, "密码格式输入不正确,请输入 6-16 位数字、字母或常用符号，字母区分大小写");
					}
				}
			}else{
				UserUtils.showDialog(this, "手机号格式输入错误，请重新输入");
			}
		}else{
			mEditTextPhone.setFocusable(true);
			mEditTextPwd.setFocusable(true);
		}
	}
	/**
	 * 获取验证码回调
	 */
	public void isRepwdCallBack(int success,Object obj){
		console.log("验证码获取回调---isRepwdCallBack---" + success + "---" + obj);
		
		if(1 == success){
			try{
				String data = (String)obj;
				console.log(data);
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				String msg = json.getString("msg");
				
				/*unregisterReceiver(smsReceiver);
				flag = false;*/
				mIdentifyLoading.setVisibility(View.GONE);
				switch (code) {
				case 200:
					//验证码获取成功
					console.toast("下发验证码成功", mContext);
					/**
					 * 点击获取验证码的时候进行倒计时
					 */
					mCountDownHelper = new CountDownButtonHelper(mBtnIdentity, 60, 1);
					mCountDownHelper.setOnFinishListener(new OnFinishListener() {
						
						@Override
						public void finish() {
							// TODO Auto-generated method stub
							mBtnIdentity.setText("重新发送");
							mEditTextPhone.setFocusable(true);
							mEditTextPwd.setFocusable(true);
						}
					});
					mCountDownHelper.start();
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
			        .setMessage("此手机号还未被注册")
					.setNegativeButton("取消", null)
					.setPositiveButton("马上注册", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							Intent intentRepwd = new Intent(UserRepwdActivity.this,UserRegistActivity.class);
							intentRepwd.putExtra("intentRepassword", mEditTextPhone.getText().toString());
							startActivity(intentRepwd);
						}
					}).create().show();
					break;
				case 440:
					UserUtils.showDialog(this, "输入手机号异常");
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
		if(!"".equals(identify)){
			//{PNumber：“13054875692”，Password：“XXX”，VCode：“1234”}
			String isRepwd = "{\"PNumber\":\"" + phone + "\",\"Password\":\""+password+"\",\"VCode\":\""+identify+ "\",\"tag\":\"android\"}";
			console.log(isRepwd);
			boolean b = mApplication.mGoluk.GoLuk_CommonGetPage(GolukMobile.PageType_ModifyPwd, isRepwd);
			console.log(b+"");
			if(b){
				//隐藏软件盘
			   UserUtils.hideSoftMethod(this);
				mLoading.setVisibility(View.VISIBLE);
			}
		}else{
//			UserUtils.showDialog(this, "请先获取验证码");
			mBtnOK.setBackgroundResource(R.drawable.icon_more);
//			mBtnOK.setFocusable(false);
		}
	}
	/**
	 * 重置密码回调
	 */
	public void repwdCallBack(int success,Object obj){
		console.log("---重置密码回调-----"+success+"----"+obj);
		if(1 == success){
			try{
				String data = (String) obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				console.log(code+"");
				String msg = json.getString("msg");
				
				mLoading.setVisibility(View.GONE);
				switch (code) {
				case 200:
					//注册成功
					console.toast("重置密码成功", mContext);
					Intent it = new Intent(UserRepwdActivity.this,UserLoginActivity.class);
					startActivity(it);
					break;
				case 500:
					UserUtils.showDialog(this, "服务端程序异常");
					break;
				case 405:
					UserUtils.showDialog(this, "此手机号还未被注册");
					break;
				case 406:
					UserUtils.showDialog(this, "请输入正确的验证码");
					break;
				case 407:
					UserUtils.showDialog(this, "输入验证码超时");
					break;

				default:
					break;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			console.log("重置密码失败");
		}
	}
	
	/**
	 * 销毁广播
	 */
	/*@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("bug", smsReceiver+"--------");
		unregisterReceiver(smsReceiver);
	}*/
	private boolean flag = false;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		console.log("=============repwd");
		if(flag){
			unregisterReceiver(smsReceiver);			
		}
		flag = false;
	}
	
}
