package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.console;
import cn.com.mobonote.golukmobile.comm.GolukMobile;

/**
 * 
 * 登陆模块
 * 
 * 1、手机号码、密码的输入 2、手机号码快速注册 3、忘记密码（重置密码） 4、第三方登陆
 * 
 * @author mobnote
 */
public class UserLoginActivity extends Activity implements OnClickListener {
	//判断是否能点击提交俺绣
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
	private ImageView mImageViewWeichat, mImageViewSina, mImageViewQQ;
	// loading组件
	private RelativeLayout mLoading;
	//application
	private GolukApplication mApplication = null;
	//context
	private Context mContext = null;
	private String phone;
	private String pwd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
		initView();
		// 设置title
		mTextViewTitle.setText("登录");
	}
	private boolean mDelAllNum = false;
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
		mImageViewWeichat = (ImageView) findViewById(R.id.user_login_weichat);
		mImageViewSina = (ImageView) findViewById(R.id.user_login_sina);
		mImageViewQQ = (ImageView) findViewById(R.id.user_login_qq);
		// loading组件
		mLoading = (RelativeLayout) findViewById(R.id.loading_layout);
		
		Intent itentGetRegist = getIntent();
		if(null !=  itentGetRegist.getStringExtra("intentRegist")){
			String phoneNumber = itentGetRegist.getStringExtra("intentRegist").toString();
			mEditTextPhoneNumber.setText(phoneNumber);
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
					if(!Phonenum.equals("")&&Phonenum.length()==11){
						if(UserUtils.isMobileNO(Phonenum)){
							isOnClick=true;
						}else{
							isOnClick=false;
//							console.toast("手机号格式不好", mContext);
							UserUtils.showDialog(UserLoginActivity.this, "手机格式输入错误,请重新输入");
						}
				}else{
					isOnClick=false;
//					mEditTextPhoneNumber.setError("手机号格式不正确");
					UserUtils.showDialog(UserLoginActivity.this, "手机格式输入错误,请重新输入");
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
		mEditTextPwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
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
		// 快速注册
		mTextViewRegist.setOnClickListener(this);
		mTextViewForgetPwd.setOnClickListener(this);
		// 第三方登录
		mImageViewWeichat.setOnClickListener(this);
		mImageViewSina.setOnClickListener(this);
		mImageViewQQ.setOnClickListener(this);

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		// 返回
		case R.id.back_btn:
			finish();
			break;
		// 登陆按钮
		case R.id.user_login_btn:
			//参数PNumber：“13054875692”，Password：“1234”,tag:”android/ios/pad/pc”}
			login();
			
			break;
		// 手机快速注册
		case R.id.user_login_phoneRegist:
			Intent itRegist = new Intent(UserLoginActivity.this,UserRegistActivity.class);
			startActivity(itRegist);
			break;
		// 忘记密码
		case R.id.user_login_forgetpwd:
			Intent itForget = new Intent(UserLoginActivity.this,UserRepwdActivity.class);
			startActivity(itForget);
			break;
		// 第三方——微信
		case R.id.user_login_weichat:

			break;
		// 第三方——新浪
		case R.id.user_login_sina:
			
			break;
		// 第三方——QQ
		case R.id.user_login_qq:
			
			break;
		}
	}
	/**
	 * 登陆
	 * 当帐号和密码输入框都有内容时,激活为可点击状态
	 */
	private void login(){
		phone = mEditTextPhoneNumber.getText().toString();
		pwd = mEditTextPwd.getText().toString();
		if(!"".equals(phone) ){
			if(phone.startsWith("1") && phone.length() == 11){
				if(!"".equals(pwd)){
					if(pwd.length()>=6 && pwd.length()<=16){
						//网络判断
						if(!UserUtils.isNetDeviceAvailable(mContext)){
							console.toast("当前网络状态不佳，请检查网络后重试", mContext);
						}else{
							//初始化定时器
						initTimer();
						handler.postDelayed(runnable, 3000);//san 秒执行一次runnable.
						String condi = "{\"PNumber\":\"" + phone + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
						boolean b = mApplication.mGoluk.GoLuk_CommonGetPage(GolukMobile.PageType_Login,condi);
						if(b){
							//隐藏软件盘
						    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						    imm.hideSoftInputFromWindow(UserLoginActivity.this.getCurrentFocus().getWindowToken(), 0);
							mLoading.setVisibility(View.VISIBLE);
							console.log("回调成功");
							//文本框不可被修改
							mEditTextPhoneNumber.setEnabled(false);
							mEditTextPwd.setEnabled(false);
						}
						}
					}else{
						UserUtils.showDialog(this, "密码格式输入不正确,请输入 6-16 位数字、字母,字母区分大小写");
					}
				}else{
//					mBtnLogin.setBackgroundResource(R.drawable.icon_login);
				}
			}else{
				UserUtils.showDialog(this, "手机号格式错误,请重新输入");
			}
		}else{
			mEditTextPhoneNumber.setFocusable(true);
			mEditTextPwd.setFocusable(true);
		}
	}
	
	/**
	 * 登录回调
	 * @param obj
	 */
	public void loginCallBack(int success,Object obj){
		console.log("登录回调---loginCallBack---" + success + "---" + obj);
		if(1 == success){
			handler.removeCallbacks(runnable);
			try{
				String data = (String)obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				String msg = json.getString("msg");
				mLoading.setVisibility(View.GONE);
				switch (code) {
				case 200:
					//登录成功跳转
					SysApplication.getInstance().exit();//杀死前边所有的Activity
					console.toast("登录成功！", mContext);
					Intent login = new Intent(UserLoginActivity.this,MainActivity.class);
					startActivity(login);
					break;
				case 500:
					UserUtils.showDialog(this, "服务端程序异常");
					break;
				case 405:
					String phone = mEditTextPhoneNumber.getText().toString();
					if(UserUtils.isMobileNO(phone)){
						new AlertDialog.Builder(this)
						.setTitle("Goluk温馨提示：")
						.setMessage("此手机号码还没有被注册")
						.setNegativeButton("取消", null)
						.setPositiveButton("注册", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent it = new Intent(UserLoginActivity.this,UserRegistActivity.class);
								it.putExtra("intentLogin", mEditTextPhoneNumber.getText().toString());
								startActivity(it);
							}
						}).create().show();
					}else{
						UserUtils.showDialog(this, "手机号格式错误,请重新输入");
					}
					break;
				case 402:
					console.toast("密码错误,请重试", mContext);
					break;
				default:
					break;
				}
				mEditTextPhoneNumber.setFocusable(true);
				mEditTextPwd.setFocusable(true);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		else{
			console.toast("登录失败", mContext);
		}
		mEditTextPhoneNumber.setEnabled(true);
		mEditTextPwd.setEnabled(true);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
			/*if(keyCode == event.KEYCODE_DEL){
				if(mDelAllNum){
					mEditTextPhoneNumber.setText("");
				}
		}*/
		return super.onKeyDown(keyCode, event);
	}
	final Handler handler=new Handler();
	private Runnable runnable;
	private void initTimer(){
		runnable=new Runnable(){
		@Override
		public void run() {
			console.toast("当前网络不佳", mContext);
			}
		};
	}
}
