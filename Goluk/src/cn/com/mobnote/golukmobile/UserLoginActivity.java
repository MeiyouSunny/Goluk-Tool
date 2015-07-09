package cn.com.mobnote.golukmobile;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
import cn.com.mobnote.user.UserLoginInterface;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 
 * 登陆模块
 * 
 * 1、手机号码、密码的输入 2、手机号码快速注册 3、忘记密码（重置密码） 4、第三方登陆
 * 
 * @author mobnote
 */
public class UserLoginActivity extends BaseActivity implements OnClickListener,UserLoginInterface ,OnTouchListener{
	
	private static final String TAG = "lily";
	/**判断是否能点击提交按钮**/
	private boolean isOnClick=false;
	/**登陆title**/
	private ImageButton mBackButton;
	private TextView mTextViewTitle;
	/**手机号和密码**/
	private EditText mEditTextPhoneNumber, mEditTextPwd;
	private Button mBtnLogin ;
	/**快速注册**/
	private TextView mTextViewRegist, mTextViewForgetPwd;
	/**application**/
	private GolukApplication mApplication = null;
	/**context**/
	private Context mContext = null;
	private String phone = null;
	private String pwd = null;
	/**将用户的手机号和密码保存到本地**/
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	
	/**判断登录**/
	private String justLogin = "";
	private CustomLoadingDialog mCustomProgressDialog=null;
	
	private boolean flag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_login);
		
		mContext = this;
		//获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();
		
		SysApplication.getInstance().addActivity(this);
		
		initView();
		
		mApplication.mLoginManage.initData();
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		mApplication.setContext(mContext, "UserLogin");
		
		if(null == mCustomProgressDialog){
			mCustomProgressDialog = new CustomLoadingDialog(mContext,"登录中，请稍候……");
		}
		getInfo();
		
		// 设置title
  		mTextViewTitle.setText("登录");
		
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if (!flag) {
			mSharedPreferences = getSharedPreferences("setup", Context.MODE_PRIVATE);
			GolukDebugUtils.i(TAG, mSharedPreferences.getString("setupPhone", "")+"=======保存phone1111");
			if(null != mEditTextPhoneNumber.getText().toString() && mEditTextPhoneNumber.getText().toString().replace("-", "").length() == 11){
				String phone = mEditTextPhoneNumber.getText().toString();
				mEditor = mSharedPreferences.edit();
				mEditor.putString("setupPhone", phone);
				mEditor.putBoolean("noPwd", false);
				//提交
				mEditor.commit();
				GolukDebugUtils.i(TAG, mSharedPreferences.getString("setupPhone", "")+"=======保存phone2222"+phone);
			}
		}
	}
	
	public void initView() {
		// 登录title
		mBackButton = (ImageButton) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		// 手机号和密码、登录按钮
		mEditTextPhoneNumber = (EditText) findViewById(R.id.user_login_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_login_pwd);
		mBtnLogin = (Button) findViewById(R.id.user_login_layout_btn);
		// 快速注册
		mTextViewRegist = (TextView) findViewById(R.id.user_login_phoneRegist);
		mTextViewForgetPwd = (TextView) findViewById(R.id.user_login_forgetpwd);
		
		// title返回按钮
		mBackButton.setOnClickListener(this);
		//登录按钮
		mBtnLogin.setOnClickListener(this);
		mBtnLogin.setOnTouchListener(this);
		// 快速注册
		mTextViewRegist.setOnClickListener(this);
		mTextViewForgetPwd.setOnClickListener(this);
		
	}
	
	public void getInfo(){
		Intent intentStart = getIntent();
		//登录页面返回
		if(null != intentStart.getStringExtra("isInfo")){
			justLogin = intentStart.getStringExtra("isInfo").toString();
		}
		
		/**
		 * 填写手机号
		 */
		mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		if(!"".equals(mSharedPreferences.getString("setupPhone", ""))){
			String phone = mSharedPreferences.getString("setupPhone", "");
			GolukDebugUtils.i(TAG, "----UserLoginActivity---获取手机号-----"+phone);
			mEditTextPhoneNumber.setText(phone);
			mEditTextPhoneNumber.setSelection(phone.length());
		}
		
		boolean b = mSharedPreferences.getBoolean("noPwd", false);
		if(b){
			mEditTextPwd.setText("");
		}
		GolukDebugUtils.i(TAG, mEditTextPhoneNumber.getText().toString());
		
		/**
		 * 监听绑定
		 */

		//手机号、密码文本框
		mEditTextPhoneNumber.addTextChangedListener(new TextWatcher() {
			private boolean isDelete = false;
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String Phonenum=mEditTextPhoneNumber.getText().toString().replace("-", "");
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
				//格式化显示手机号
				mEditTextPhoneNumber.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(View arg0, int keyCode, KeyEvent arg2) {
						if (keyCode == KeyEvent.KEYCODE_DEL) {
		                    isDelete = true;
		              }
						return false;
					}
				});
				UserUtils.formatPhone(arg0, mEditTextPhoneNumber);
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
				String number = mEditTextPhoneNumber.getText().toString().replace("-", "");
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
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		// 返回
		case R.id.back_btn:
			mApplication.mLoginManage.setUserLoginInterface(null);
			finish();
			break;
		// 登陆按钮
		case R.id.user_login_layout_btn:
			loginManage();
			break;
		// 手机快速注册
		case R.id.user_login_phoneRegist:
			mApplication.mLoginManage.setUserLoginInterface(null);
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
			mApplication.mLoginManage.setUserLoginInterface(null);
			Intent itForget = new Intent(UserLoginActivity.this,UserRepwdActivity.class);
			startActivity(itForget);
			break;
		}
	}
	
	/**
	 * 登录管理类
	 * 
	 */
	public void loginManage(){
		phone = mEditTextPhoneNumber.getText().toString().replace("-", "");
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
						UserUtils.showDialog(mApplication.getContext(), this.getResources().getString(R.string.user_login_password_show_error));
					}
				}
			}else{
					UserUtils.showDialog(mApplication.getContext(), this.getResources().getString(R.string.user_login_phone_show_error));
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
				mApplication.mLoginManage.setUserLoginInterface(null);
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
						mApplication.mLoginManage.setUserLoginInterface(null);
						Intent it = new Intent(UserLoginActivity.this,UserRegistActivity.class);
						it.putExtra("intentLogin", mEditTextPhoneNumber.getText().toString().replace("-", ""));
						it.putExtra("fromRegist", "fromStart");
						
						if(justLogin.equals("main") || justLogin.equals("back")){//从起始页注册
							it.putExtra("fromRegist", "fromStart");
						}else if(justLogin.equals("indexmore")){//从更多页个人中心注册
							it.putExtra("fromRegist", "fromIndexMore");
						}else if(justLogin.equals("setup")){//从设置页注册
							it.putExtra("fromRegist", "fromSetup");
						}
						
						startActivity(it);
					}
				}).create().show();
			}else{
				UserUtils.showDialog(this, this.getResources().getString(R.string.user_login_phone_show_error));
			}
			break;
		case 4:
			GolukUtils.showToast(this, "网络连接超时");
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
			.setMessage(this.getResources().getString(R.string.user_login_password_limit_top_hint))
			.setPositiveButton(this.getResources().getString(R.string.user_repwd_ok), new DialogInterface.OnClickListener() {
					
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					mApplication.mLoginManage.setUserLoginInterface(null);
					Intent it = new Intent(UserLoginActivity.this,UserRepwdActivity.class);
					it.putExtra(TAG, mEditTextPhoneNumber.getText().toString().replace("-", ""));
					startActivity(it);
				}
			})
			.create().show();
			break;
			//密码错误
		case 6:
			closeProgressDialog();
			mEditTextPwd.setText("");
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
		case R.id.user_login_layout_btn:
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
	 */
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			mBackButton.setEnabled(true);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		boolean isCurrentRunningForeground=isRunningForeground();
		flag = isCurrentRunningForeground;
	}
	
	public boolean isRunningForeground(){
		String packageName=getPackageName(this);
		String topActivityClassName=getTopActivityName(this);
		GolukDebugUtils.i(TAG, "packageName="+packageName+",topActivityClassName="+topActivityClassName);
		if (packageName!=null&&topActivityClassName!=null&&topActivityClassName.startsWith(packageName)) {
			GolukDebugUtils.i(TAG, "---> isRunningForeGround");
			return true;
		} else {
			GolukDebugUtils.i(TAG, "---> isRunningBackGround");
			return false;
		}
	}
	
	
	public  String getTopActivityName(Context context){
		String topActivityClassName=null;
		 ActivityManager activityManager =
		(ActivityManager)(context.getSystemService(android.content.Context.ACTIVITY_SERVICE )) ;
		 //android.app.ActivityManager.getRunningTasks(int maxNum) 
		 //即最多取得的运行中的任务信息(RunningTaskInfo)数量
	     List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1) ;
	     if(runningTaskInfos != null){
	    	 ComponentName f=runningTaskInfos.get(0).topActivity;
	    	 topActivityClassName=f.getClassName();
	    	
	     }
	     //按下Home键盘后 topActivityClassName
	     return topActivityClassName;
	}
	
	public String getPackageName(Context context){
		 String packageName = context.getPackageName();  
		 return packageName;
	}

}
