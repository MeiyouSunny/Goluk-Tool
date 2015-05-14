package cn.com.mobnote.golukmobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
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
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.user.UserLoginInterface;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.console;

/**
 * 
 * 登陆模块
 * 
 * 1、手机号码、密码的输入 2、手机号码快速注册 3、忘记密码（重置密码） 4、第三方登陆
 * 
 * @author mobnote
 */
public class UserLoginActivity extends BaseActivity implements OnClickListener,UserLoginInterface ,OnTouchListener{
	//判断是否能点击提交按钮
	private boolean isOnClick=false;
	// 登陆title
	private Button mBackButton;
	private TextView mTextViewTitle;
	// 手机号和密码
	private EditText mEditTextPhoneNumber, mEditTextPwd;
	private Button mBtnLogin;
	// 快速注册
	private TextView mTextViewRegist, mTextViewForgetPwd;
	// 第三方登陆
//	private ImageView mImageViewWeichat, mImageViewSina, mImageViewQQ;
	//application
	private GolukApplication mApplication = null;
	//context
	private Context mContext = null;
	private String phone = null;
	private String pwd = null;
	//将用户的手机号和密码保存到本地
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	
	//判断登录
	private String justLogin = "";
	private CustomLoadingDialog mCustomProgressDialog=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_login);
		
		SysApplication.getInstance().addActivity(this);
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserLogin");
		
		mApplication.mLoginManage.initData();
		
		if(null == mCustomProgressDialog){
			mCustomProgressDialog = new CustomLoadingDialog(mContext,"登录中，请稍候……");
		}
		
		initView();
		// 设置title
		mTextViewTitle.setText("登录");
		
	}
//	private boolean mDelAllNum = false;
	public void initView() {
		// 登录title
		mBackButton = (Button) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		// 手机号和密码、登录按钮
		mEditTextPhoneNumber = (EditText) findViewById(R.id.user_login_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_login_pwd);
		mBtnLogin = (Button) findViewById(R.id.user_login_btn);
		// 快速注册
		mTextViewRegist = (TextView) findViewById(R.id.user_login_phoneRegist);
		mTextViewForgetPwd = (TextView) findViewById(R.id.user_login_forgetpwd);
		// 第三方登录
//		mImageViewWeichat = (ImageView) findViewById(R.id.user_login_weichat);
//		mImageViewSina = (ImageView) findViewById(R.id.user_login_sina);
//		mImageViewQQ = (ImageView) findViewById(R.id.user_login_qq);
		
		Intent itentGetRegist = getIntent();
		if(null !=  itentGetRegist.getStringExtra("intentRegist")){
			String phoneNumber = itentGetRegist.getStringExtra("intentRegist").toString();
			Log.i("lily", "----------intentRegist--------phoneNumber =   "+phoneNumber);
			mEditTextPhoneNumber.setText(phoneNumber);
		}
		
		Intent intentStart = getIntent();
		//登录页面返回
		if(null != intentStart.getStringExtra("isInfo")){
			justLogin = intentStart.getStringExtra("isInfo").toString();
		}
		
		/**
		 *如果填写手机号的EditText中有手机号，就保存 
		 */
		if(null != mEditTextPhoneNumber.getText().toString() && mEditTextPhoneNumber.length() == 11){
			String phone = mEditTextPhoneNumber.getText().toString();
			mSharedPreferences = getSharedPreferences("setup", Context.MODE_PRIVATE);
			mEditor = mSharedPreferences.edit();
			mEditor.putString("setupPhone", phone);
			//提交
			mEditor.commit();
		}
		
		/**
		 * 填写手机号
		 */
		mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		if(null != mSharedPreferences.getString("setupPhone", "") || !"".equals(mSharedPreferences.getString("setupPhone", ""))){
			String phone = mSharedPreferences.getString("setupPhone", "");
			Log.i("lily", "----UserLoginActivity---获取手机号-----"+phone);
			mEditTextPhoneNumber.setText(phone);
			mEditTextPhoneNumber.setSelection(phone.length());
			mEditTextPwd.setText("");
		}
		
		/**
		 * 监听绑定
		 */
		// title返回按钮
		mBackButton.setOnClickListener(this);
		mEditTextPhoneNumber.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String Phonenum=mEditTextPhoneNumber.getText().toString();
				String psw=mEditTextPwd.getText().toString();
				if(arg1){
					
				}else{
					if(!Phonenum.equals("")){
						if(Phonenum.length()==11){
							if(UserUtils.isMobileNO(Phonenum)){
								isOnClick=true;
							}else{
								isOnClick=false;
								UserUtils.showDialog(UserLoginActivity.this, "手机格式输入错误,请重新输入");
							}
						}else{
							isOnClick=false;
							UserUtils.showDialog(UserLoginActivity.this, "手机格式输入错误,请重新输入");
						}
					}else{
						isOnClick=false;
						UserUtils.showDialog(UserLoginActivity.this, "手机号不能为空");
					}
				if(isOnClick&&!psw.equals("")){
					mBtnLogin.setBackgroundResource(R.drawable.icon_login);
					mBtnLogin.setEnabled(true);
				}else{
					mBtnLogin.setBackgroundResource(R.drawable.icon_more);
					mBtnLogin.setEnabled(false);
				}
				}
			}
		});

		//手机号、密码文本框
		mEditTextPhoneNumber.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String Phonenum=mEditTextPhoneNumber.getText().toString();
				String psw=mEditTextPwd.getText().toString();
				if(Phonenum.equals("")){
					isOnClick=false;
				}
				if(!Phonenum.equals("")&&!psw.equals("")){
					mBtnLogin.setBackgroundResource(R.drawable.icon_login);
					mBtnLogin.setEnabled(true);
				}else{
					mBtnLogin.setBackgroundResource(R.drawable.icon_more);
					mBtnLogin.setEnabled(false);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		} );
		//密码监听
		mEditTextPwd.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String pwd=mEditTextPwd.getText().toString();
				if(arg1){
					
				}else{
					if(pwd.equals("") || pwd.length()<6 || pwd.length()>16){
						UserUtils.showDialog(UserLoginActivity.this, "密码格式输入不正确,请输入 6-16 位数字、字母,字母区分大小写");
					}
				}
			}
		});
		mEditTextPwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String number = mEditTextPhoneNumber.getText().toString();
				String psw=mEditTextPwd.getText().toString();
				if(isOnClick){
					if(!psw.equals("")){
						mBtnLogin.setBackgroundResource(R.drawable.icon_login);
						mBtnLogin.setEnabled(true);
					}else{
						mBtnLogin.setBackgroundResource(R.drawable.icon_more);
						mBtnLogin.setEnabled(false);
					}
				}
				if(!number.equals("")&&!psw.equals("")){
					mBtnLogin.setBackgroundResource(R.drawable.icon_login);
					mBtnLogin.setEnabled(true);
				}else{
					mBtnLogin.setBackgroundResource(R.drawable.icon_more);
					mBtnLogin.setEnabled(false);
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
		//登录按钮
		mBtnLogin.setOnClickListener(this);
		mBtnLogin.setOnTouchListener(this);
		// 快速注册
		mTextViewRegist.setOnClickListener(this);
		mTextViewForgetPwd.setOnClickListener(this);
//		// 第三方登录
//		mImageViewWeichat.setOnClickListener(this);
//		mImageViewSina.setOnClickListener(this);
//		mImageViewQQ.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		// 返回
		case R.id.back_btn:
			finish();
			break;
		// 登陆按钮
		case R.id.user_login_btn:
			loginManage();
			break;
		// 手机快速注册
		case R.id.user_login_phoneRegist:
			Intent itRegist = new Intent(UserLoginActivity.this,UserRegistActivity.class);
			if(justLogin.equals("main") || justLogin.equals("back")){//从起始页注册
				itRegist.putExtra("fromRegist", "fromStart");
			}else if(justLogin.equals("indexmore")){//从更多页个人中心注册
				itRegist.putExtra("fromRegist", "fromIndexMore");
			}else if(justLogin.equals("setup")){//从设置页注册
				itRegist.putExtra("fromRegist", "fromSetup");
			}
			startActivity(itRegist);
			break;
		// 忘记密码
		case R.id.user_login_forgetpwd:
			Intent itForget = new Intent(UserLoginActivity.this,UserRepwdActivity.class);
			startActivity(itForget);
			break;
//		// 第三方——微信
//		case R.id.user_login_weichat:
//
//			break;
//		// 第三方——新浪
//		case R.id.user_login_sina:
//			
//			break;
//		// 第三方——QQ
//		case R.id.user_login_qq:
//			
//			break;
		}
	}
	
	/**
	 * 登录管理类
	 * 
	 */
	public void loginManage(){
		phone = mEditTextPhoneNumber.getText().toString();
		pwd = mEditTextPwd.getText().toString();
		if(!"".equals(phone) ){
			if(UserUtils.isMobileNO(phone)){
				if(!"".equals(pwd)){
					if(pwd.length()>=6 && pwd.length()<=16){
						mApplication.mLoginManage.setUserLoginInterface(this);
						boolean b = mApplication.mLoginManage.login(phone, pwd);
						if(b){
							mApplication.loginStatus = 0;
							UserUtils.hideSoftMethod(this);
							mCustomProgressDialog.show();
							mEditTextPhoneNumber.setEnabled(false);
							mEditTextPwd.setEnabled(false);
							mTextViewRegist.setEnabled(false);
							mTextViewForgetPwd.setEnabled(false);
							mBtnLogin.setEnabled(false);
							mBackButton.setEnabled(false);
						}else{
							closeProgressDialog();
							mApplication.loginStatus = 2;
						}
					}else{
						UserUtils.showDialog(mApplication.getContext(), "密码格式输入不正确,请输入 6-16 位数字、字母,字母区分大小写");
					}
				}
			}else{
					UserUtils.showDialog(mApplication.getContext(), "手机号格式错误,请重新输入");
			}
		}
	}

	/**
	 * 登录管理类回调返回的状态
	 * 0登录中  1登录成功  2登录失败  3用户未注册  4登录超时
	 */
	@Override
	public void loginCallbackStatus() {
		switch (mApplication.loginStatus) {
		case 0:
			break;
		case 1:
			//登录成功后关闭个人中心启动模块页面
			if(null != UserStartActivity.mHandler){
				UserStartActivity.mHandler.sendEmptyMessage(UserStartActivity.EXIT);
			}
			
			mApplication.isUserLoginSucess = true;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			mBackButton.setEnabled(true);
			if(justLogin.equals("main")){
				Intent login = new Intent(UserLoginActivity.this,MainActivity.class);
				startActivity(login);
			}
			this.finish();
			break;
		case 2:
			mApplication.isUserLoginSucess = false;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			mBackButton.setEnabled(true);
			break;
		case 3:
			mApplication.isUserLoginSucess = false;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			mBackButton.setEnabled(true);
			if(UserUtils.isMobileNO(phone)){
				new AlertDialog.Builder(this)
				.setMessage("此手机号码还没有被注册")
				.setNegativeButton("取消", null)
				.setPositiveButton("注册", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Intent it = new Intent(UserLoginActivity.this,UserRegistActivity.class);
						it.putExtra("intentLogin", mEditTextPhoneNumber.getText().toString());
						it.putExtra("fromRegist", "fromStart");
						startActivity(it);
					}
				}).create().show();
			}else{
				UserUtils.showDialog(this, "手机号格式错误,请重新输入");
			}
			break;
		case 4:
			console.toast("网络连接超时", this);
			mApplication.isUserLoginSucess = false;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			mBackButton.setEnabled(true);
			break;
		case 5:
			mApplication.isUserLoginSucess = false;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			mBackButton.setEnabled(true);
			new AlertDialog.Builder(mContext)
			.setMessage("登录密码出错已经达到 5 次上限,账户被锁定 2 小时,请重置密码后登录")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Intent it = new Intent(UserLoginActivity.this,UserRepwdActivity.class);
					it.putExtra("errorPwdOver", mEditTextPhoneNumber.getText().toString());
					startActivity(it);
				}
			})
			.create().show();
			break;
		default:
			break;
		}
	}
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_login_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mBtnLogin.setBackgroundResource(R.drawable.icon_login_click);
				break;
			case MotionEvent.ACTION_UP:
				mBtnLogin.setBackgroundResource(R.drawable.icon_login);
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}
		return false;
	}
	
	/**
	 * 关闭加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
		}
	}
}
