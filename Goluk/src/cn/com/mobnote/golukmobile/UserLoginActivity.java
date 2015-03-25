package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.util.console;
import cn.com.mobonote.golukmobile.comm.GolukMobile;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * 登陆模块
 * 
 * 1、手机号码、密码的输入 2、手机号码快速注册 3、忘记密码（重置密码） 4、第三方登陆
 * 
 * @author mobnote
 */
public class UserLoginActivity extends Activity implements OnClickListener {

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
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		mApplication.setContext(mContext, "UserLogin");
				
		initView();
		// 设置title
		mTextViewTitle.setText("登陆");
		
	}
	private boolean mDelAllNum = false;
	public void initView() {
		// 登陆title
		mBackButton = (Button) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		// 手机号和密码、登陆按钮
		mEditTextPhoneNumber = (EditText) findViewById(R.id.user_login_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_login_pwd);
		mBtnLogin = (Button) findViewById(R.id.user_login_btn);
		// 快速注册
		mTextViewRegist = (TextView) findViewById(R.id.user_login_phoneRegist);
		mTextViewForgetPwd = (TextView) findViewById(R.id.user_login_forgetpwd);
		// 第三方登陆
		mImageViewWeichat = (ImageView) findViewById(R.id.user_login_weichat);
		mImageViewSina = (ImageView) findViewById(R.id.user_login_sina);
		mImageViewQQ = (ImageView) findViewById(R.id.user_login_qq);
		// loading组件
		mLoading = (RelativeLayout) findViewById(R.id.loading);
		
		/**
		 * 监听绑定
		 */
		// title返回按钮
		mBackButton.setOnClickListener(this);
		//手机号、密码文本框
		mEditTextPhoneNumber.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String pwd = mEditTextPwd.getText().toString();
//				mDelAllNum = false;
				if("".equals(arg0.toString())){
					if("".equals(pwd)){
						//显示普通按钮
						mBtnLogin.setBackgroundResource(R.drawable.icon_more);
					}
				}
				else{
					if(!"".equals(pwd)){
						//显示高亮登录按钮
						mBtnLogin.setBackgroundResource(R.drawable.icon_login);
					}
				}
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
			@Override
			public void afterTextChanged(Editable arg0) {}
		});
		mEditTextPwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhoneNumber.getText().toString();
				if("".equals(arg0.toString())){
					if("".equals(phone)){
						//显示普通按钮
						mBtnLogin.setBackgroundResource(R.drawable.icon_more);
					}
				}
				else{
					if(!"".equals(phone)){
						//显示高亮登录按钮
						mBtnLogin.setBackgroundResource(R.drawable.icon_login);
					}
				}
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
			@Override
			public void afterTextChanged(Editable arg0) {}
		});
		/*mEditTextPhoneNumber.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				console.log("focus---" + arg1);
				if(!arg1){
					mDelAllNum = true;
				}
			}
		});*/
		//登陆按钮
		mBtnLogin.setOnClickListener(this);
		// 快速注册
		mTextViewRegist.setOnClickListener(this);
		mTextViewForgetPwd.setOnClickListener(this);
		// 第三方登陆
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
//			itForget.putExtra("user_login_forget", phone);
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
						String condi = "{\"PNumber\":\"" + phone + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
						boolean b = mApplication.mGoluk.GoLuk_CommonGetPage(GolukMobile.PageType_Login,condi);
						if(b){
							console.log("回调成功");
						}
					}else{
						new AlertDialog.Builder(this)
						.setTitle("错误信息提示")
						.setMessage("密码格式输入不正确,请输入 6-16 位数字、字母,字母区分大小写!")
						.setPositiveButton("确定", null)
						.create().show();
					}
				}else{
//					mBtnLogin.setBackgroundResource(R.drawable.icon_login);
//					mEditTextPwd.setError("密码不能为空");
				}
			}else{
				new AlertDialog.Builder(this)
				.setTitle("错误信息提示")
				.setMessage("手机号格式错误,请重新输入！")
				.setPositiveButton("确定", null)
				.create().show();
			}
		}else{
			
		}
	}
	
	/**
	 * 登录回调
	 * @param obj
	 */
	public void loginCallBack(int success,Object obj){
		console.log("登录回调---loginCallBack---" + success + "---" + obj);
		if(1 == success){
			try{
				String data = (String)obj;
				JSONObject json = new JSONObject(data);
				int code = Integer.valueOf(json.getString("code"));
				String msg = json.getString("msg");
				if(code == 200){
					//登录成功跳转
					Intent login = new Intent(UserLoginActivity.this,MainActivity.class);
					startActivity(login);
//					mLoginDialog.hide();
				}else{
					new AlertDialog.Builder(this)
					.setTitle("错误提示")
					.setMessage("此手机号还未被注册")
					.setNegativeButton("取消", null)
					.setPositiveButton("注册", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							Intent it = new Intent(UserLoginActivity.this,UserRegistActivity.class);
							startActivity(it);
						}
					}).create().show();;
//					console.toast("登陆失败"+msg+"-------"+code, mContext);
					
				}
			}
			catch(Exception ex){}
		}
		else{
			console.toast("登录失败", mContext);
		}
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

}
