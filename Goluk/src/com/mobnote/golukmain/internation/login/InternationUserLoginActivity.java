package com.mobnote.golukmain.internation.login;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.user.UserLoginInterface;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 
 * 登陆模块
 * 
 * 1、手机号码、密码的输入 2、手机号码快速注册 3、忘记密码（重置密码） 4、第三方登陆
 * 
 * @author mobnote
 */
public class InternationUserLoginActivity extends BaseActivity implements OnClickListener, UserLoginInterface, OnTouchListener {

	private static final String TAG = "lily";
	/** 判断是否能点击提交按钮 **/
	private boolean isOnClick = false;
	/** 登陆title **/
	// private ImageButton mBackButton;
	// private TextView mTextViewTitle;
	/** 手机号和密码 **/
	private EditText mEditTextPhoneNumber, mEditTextPwd;
	private Button mBtnLogin;
	/** 快速注册 **/
	private TextView mTextViewRegist, mTextViewForgetPwd;
	/** application **/
	private GolukApplication mApplication = null;
	/** context **/
	private Context mContext = null;
	private String phone = null;
	private String pwd = null;
	/** 将用户的手机号和密码保存到本地 **/
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;

	/** 判断登录 **/
	private String justLogin = "";
	private CustomLoadingDialog mCustomProgressDialog = null;

	private boolean flag = false;

	private UMShareAPI mShareAPI = null;
	private TextView mSelectCountryText = null;
	public static final int REQUEST_SELECT_COUNTRY_CODE = 1000;
	public static final String COURTRY_BEAN = "countrybean";

	public TextView mLoginInfacebookTxt;
	public ImageView mLoginInfacebookImg;
	private ImageView mCloseBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.internation_user_login);

		mShareAPI = UMShareAPI.get(mContext);

		mContext = this;
		// 获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();

		initView();
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_loginning));
		}

		// 设置title
		// mTextViewTitle.setText(this.getResources().getString(R.string.user_login_title_text));

		if (null != mApplication && null != mApplication.mLoginManage) {
			mApplication.mLoginManage.initData();
		}

		UserUtils.addActivity(InternationUserLoginActivity.this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// GolukDebugUtils.d("facebooklogin2", "facebooklogin data = " +
		// data.toString());
		mShareAPI.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_SELECT_COUNTRY_CODE:
			if (RESULT_OK != resultCode) {
				return;
			}
			if (null != data) {
				CountryBean bean = (CountryBean) data.getSerializableExtra(COURTRY_BEAN);
				mSelectCountryText.setText(bean.area + " +" + bean.code);
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mApplication.setContext(mContext, "UserLogin");

		getInfo();
	}

	/** auth callback interface **/
	private UMAuthListener umAuthListener = new UMAuthListener() {
		@Override
		public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
			GolukDebugUtils.e("", "youmeng----goluk----SharePlatformUtil----umAuthListener----onComplete");
			GolukDebugUtils.e("facebooklogin1", "facebooklogin data = " + data.toString() + "action = " + action);
			if (action == 0) {
				mShareAPI.getPlatformInfo(InternationUserLoginActivity.this, SHARE_MEDIA.FACEBOOK, umAuthListener);
			} else if (action == 2) {
				JSONObject json = new JSONObject();
				try {
					json.put("platform", "facebook");
					json.put("userinfo", URLEncoder.encode(new JSONObject(data).toString(), "utf-8"));
					json.put("devices", "");
					mApplication.mLoginManage.setUserLoginInterface(InternationUserLoginActivity.this);
					boolean flog = mBaseApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
							IPageNotifyFn.PageType_OauthLogin, json.toString());
					GolukDebugUtils.e("facebooklogin1", "facebooklogin request = " + json.toString() + " flog = " + flog);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				Toast.makeText(mContext.getApplicationContext(), "Authorize succeed", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		public void onError(SHARE_MEDIA platform, int action, Throwable t) {
			Toast.makeText(mContext.getApplicationContext(), "Authorize fail", Toast.LENGTH_SHORT).show();
			GolukDebugUtils.e("", "youmeng----goluk----SharePlatformUtil----umAuthListener----onError");
		}

		@Override
		public void onCancel(SHARE_MEDIA platform, int action) {
			Toast.makeText(mContext.getApplicationContext(), "Authorize cancel", Toast.LENGTH_SHORT).show();
			GolukDebugUtils.e("", "youmeng----goluk----SharePlatformUtil----umAuthListener----onCancel");
		}
	};

	@Override
	protected void onRestart() {
		super.onRestart();
		if (!flag) {
			mSharedPreferences = getSharedPreferences("setup", Context.MODE_PRIVATE);
			GolukDebugUtils.i(TAG, mSharedPreferences.getString("setupPhone", "") + "=======保存phone1111");
			if (null != mEditTextPhoneNumber.getText().toString()
					&& mEditTextPhoneNumber.getText().toString().length() == 11) {
				String phone = mEditTextPhoneNumber.getText().toString();
				mEditor = mSharedPreferences.edit();
				mEditor.putString("setupPhone", phone);
				mEditor.putBoolean("noPwd", false);
				// 提交
				mEditor.commit();
				GolukDebugUtils.i(TAG, mSharedPreferences.getString("setupPhone", "") + "=======保存phone2222" + phone);
			}
		}
	}

	public void initView() {
		// 登录title
		// mBackButton = (ImageButton) findViewById(R.id.back_btn);
		// mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		// 手机号和密码、登录按钮
		mEditTextPhoneNumber = (EditText) findViewById(R.id.user_login_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_login_pwd);
		mBtnLogin = (Button) findViewById(R.id.user_login_layout_btn);
		// 快速注册
		mTextViewRegist = (TextView) findViewById(R.id.insert_user_btn);
		mTextViewForgetPwd = (TextView) findViewById(R.id.user_login_forgetpwd);
		// select country
		mSelectCountryText = (TextView) findViewById(R.id.tv_user_login_select_country);
		mLoginInfacebookTxt = (TextView) findViewById(R.id.login_facebook_btn_txt);
		mLoginInfacebookImg = (ImageView) findViewById(R.id.login_facebook_btn_img);
		mCloseBtn = (ImageView) findViewById(R.id.close_btn);
		mCloseBtn.setOnClickListener(this);
		mSelectCountryText.setOnClickListener(this);
		if(mBaseApp.mLocationCityCode!=null){
			mSelectCountryText.setText(mBaseApp.mLocationCityCode.area + "+" + mBaseApp.mLocationCityCode.code);
		}else{
			mSelectCountryText.setText(GolukUtils.getDefaultZone());
		}

		// title返回按钮
		// mBackButton.setOnClickListener(this);
		// 登录按钮
		mBtnLogin.setOnClickListener(this);
		//mBtnLogin.setOnTouchListener(this);
		mLoginInfacebookTxt.setOnClickListener(this);
		mLoginInfacebookImg.setOnClickListener(this);
		// 快速注册
		mTextViewRegist.setOnClickListener(this);
		mTextViewForgetPwd.setOnClickListener(this);
	}

	public void getInfo() {
		Intent intentStart = getIntent();
		// 登录页面返回
		if (null != intentStart.getStringExtra("isInfo")) {
			justLogin = intentStart.getStringExtra("isInfo").toString();
		}

		/**
		 * 填写手机号
		 */
		mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		if (!"".equals(mSharedPreferences.getString("setupPhone", ""))) {
			String phone = mSharedPreferences.getString("setupPhone", "");
			GolukDebugUtils.i(TAG, "----UserLoginActivity--------phone:" + phone);
			mEditTextPhoneNumber.setText(phone);
			mEditTextPhoneNumber.setSelection(mEditTextPhoneNumber.getText().toString().length());
		}

		boolean b = mSharedPreferences.getBoolean("noPwd", false);
		if (b) {
			mEditTextPwd.setText("");
		}
		GolukDebugUtils.i(TAG, mEditTextPhoneNumber.getText().toString() + "------------------");

		/**
		 * 监听绑定
		 */

		// 手机号、密码文本框
		mEditTextPhoneNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String Phonenum = mEditTextPhoneNumber.getText().toString();
				String psw = mEditTextPwd.getText().toString();
				if (Phonenum.equals("")) {
					isOnClick = false;
				}
				
				if (!Phonenum.equals("") && UserUtils.isNumber(Phonenum) && !"".equals(psw.trim())) {
					mBtnLogin.setTextColor(Color.parseColor("#FFFFFF"));
					mBtnLogin.setEnabled(true);
				} else {
					mBtnLogin.setTextColor(Color.parseColor("#7fffffff"));
					mBtnLogin.setEnabled(false);
				}
//				UserUtils.formatPhone(arg0, mEditTextPhoneNumber);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
		
		mEditTextPhoneNumber.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				mEditTextPhoneNumber.setTextColor(getResources().getColor(R.color.login_next_btn_success));
			}
		});
		
		mEditTextPwd.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				mEditTextPwd.setTextColor(getResources().getColor(R.color.login_next_btn_success));				
			}
		});
		
		// 密码监听
		mEditTextPwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String number = mEditTextPhoneNumber.getText().toString();
				String psw = mEditTextPwd.getText().toString();
				if (isOnClick) {
					if (!"".equals(psw.trim()) && !"".equals(number)) {
						mBtnLogin.setTextColor(Color.parseColor("#FFFFFF"));
						mBtnLogin.setEnabled(true);
					} else {
						mBtnLogin.setTextColor(Color.parseColor("#7fffffff"));
						mBtnLogin.setEnabled(false);
					}
				}
				if (!psw.equals("") && !"".equals(number) && UserUtils.isNumber(number)) {
					mBtnLogin.setTextColor(Color.parseColor("#FFFFFF"));
					mBtnLogin.setEnabled(true);
					mEditTextPwd.setTextColor(getResources().getColor(R.color.login_next_btn_fial));
				} else {
					mBtnLogin.setTextColor(Color.parseColor("#7fffffff"));
					mBtnLogin.setEnabled(false);
					mEditTextPwd.setTextColor(getResources().getColor(R.color.login_next_btn_fial));
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
	public void onClick(View view) {
		
		if(view.getId() == R.id.back_btn){
			mApplication.mLoginManage.setUserLoginInterface(null);
			UserUtils.hideSoftMethod(this);
			setResult(Activity.RESULT_CANCELED);
			this.finish();
		}else if(view.getId() == R.id.user_login_layout_btn){
			loginManage();
		}else if(view.getId() == R.id.insert_user_btn){
			mApplication.mLoginManage.setUserLoginInterface(null);
			UserUtils.hideSoftMethod(this);
			Intent itRegist = new Intent(InternationUserLoginActivity.this, InternationUserRegistActivity.class);
			GolukDebugUtils.i("final", "-----------UserLoginActivity-----------" + justLogin);
			if (justLogin.equals("main") || justLogin.equals("back")) {// 从起始页注册
				itRegist.putExtra("fromRegist", "fromStart");
			} else if (justLogin.equals("indexmore")) {// 从更多页个人中心注册
				itRegist.putExtra("fromRegist", "fromIndexMore");
			} else if (justLogin.equals("setup")) {// 从设置页注册
				itRegist.putExtra("fromRegist", "fromSetup");
			} else if (justLogin.equals("profit")) {
				itRegist.putExtra("fromRegist", "fromProfit");
			}
			startActivity(itRegist);
		}else if(view.getId() == R.id.user_login_forgetpwd){
			mApplication.mLoginManage.setUserLoginInterface(null);
			UserUtils.hideSoftMethod(this);
			Intent itForget = new Intent(InternationUserLoginActivity.this, InternationUserRepwdActivity.class);
			if (justLogin.equals("main") || justLogin.equals("back")) {// 从起始页注册
				itForget.putExtra("fromRegist", "fromStart");
			} else if (justLogin.equals("indexmore")) {// 从更多页个人中心注册
				itForget.putExtra("fromRegist", "fromIndexMore");
			} else if (justLogin.equals("setup")) {// 从设置页注册
				itForget.putExtra("fromRegist", "fromSetup");
			} else if (justLogin.equals("profit")) {
				itForget.putExtra("fromRegist", "fromProfit");
			}
			startActivity(itForget);
		}else if(view.getId() == R.id.tv_user_login_select_country){
			mApplication.mLoginManage.setUserLoginInterface(null);
			UserUtils.hideSoftMethod(this);
			Intent itSelectCountry = new Intent(this, UserSelectCountryActivity.class);
			startActivityForResult(itSelectCountry, REQUEST_SELECT_COUNTRY_CODE);
		}else if(view.getId() == R.id.login_facebook_btn_img || view.getId() == R.id.login_facebook_btn_txt){
			if(GolukUtils.isNetworkConnected(this)){
				if(mShareAPI.isInstall(this, SHARE_MEDIA.FACEBOOK)){
					mShareAPI.doOauthVerify(this, SHARE_MEDIA.FACEBOOK, umAuthListener);
				}else{
					GolukUtils.showToast(this, getResources().getString(R.string.str_facebook_no_install));
				}
			}else{
				GolukUtils.showToast(this,getResources().getString(R.string.str_timeout));
			}
		}else if(view.getId() == R.id.close_btn){
			this.finish();
		}
	}

	/**
	 * 登录管理类
	 * 
	 */
	public void loginManage() {
		phone = mEditTextPhoneNumber.getText().toString();
		pwd = mEditTextPwd.getText().toString();
		if (!"".equals(phone)) {
//			if (UserUtils.isMobileNO(phone)) {
				if (!"".equals(pwd)) {
					if (pwd.length() >= 6 && pwd.length() <= 16) {
						if (!UserUtils.isNetDeviceAvailable(this)) {
							UserUtils.hideSoftMethod(this);
							GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
						} else {
							mApplication.mLoginManage.setUserLoginInterface(this);
							mApplication.mLoginManage.login(phone, pwd,"");
//							if (b) {
								mApplication.loginStatus = 0;
								UserUtils.hideSoftMethod(this);
								mCustomProgressDialog.show();
								mEditTextPhoneNumber.setEnabled(false);
								mEditTextPwd.setEnabled(false);
								mTextViewRegist.setEnabled(false);
								mTextViewForgetPwd.setEnabled(false);
								mBtnLogin.setEnabled(false);
								// mBackButton.setEnabled(false);
//							} else {
//								closeProgressDialog();
//								mApplication.loginStatus = 2;
//							}
						}

					} else {
						UserUtils.hideSoftMethod(this);
						UserUtils.showDialog(mApplication.getContext(),
								this.getResources().getString(R.string.user_login_password_show_error));
					}
				}
//			} else {
//				UserUtils.hideSoftMethod(this);
//				UserUtils.showDialog(mApplication.getContext(),
//						this.getResources().getString(R.string.user_login_phone_show_error));
//			}
		}
	}

	/**
	 * 登录管理类回调返回的状态 0登录中 1登录成功 2登录失败 3用户未注册 4登录超时
	 */
	@Override
	public void loginCallbackStatus() {
		GolukDebugUtils.e("","facebooklogin loginCallbackStatus = " + mApplication.loginStatus);
		switch (mApplication.loginStatus) {
		case 0:
			break;
		case 1:
			// 登录成功后关闭个人中心启动模块页面

			mApplication.isUserLoginSucess = true;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			// mBackButton.setEnabled(true);
			mApplication.mUser.timerCancel();
			mApplication.autoLoginStatus = 2;
			Intent it = new Intent();
			if ("profit".equals(justLogin)) {
				it.setClass(InternationUserLoginActivity.this, MyProfitActivity.class);
				startActivity(it);
			} else {
				setResult(Activity.RESULT_OK, it);
			}
			EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_REQUEST));
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
			// mBackButton.setEnabled(true);
			break;
		case 3:
			mApplication.isUserLoginSucess = false;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			// mBackButton.setEnabled(true);
//			if (UserUtils.isMobileNO(phone)) {
				new AlertDialog.Builder(this)
						.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
						.setMessage(this.getResources().getString(R.string.user_no_regist))
						.setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
						.setPositiveButton(this.getResources().getString(R.string.user_regist),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										mApplication.mLoginManage.setUserLoginInterface(null);
										Intent it = new Intent(InternationUserLoginActivity.this, InternationUserRegistActivity.class);
										it.putExtra("intentLogin", mEditTextPhoneNumber.getText().toString());
										if (justLogin.equals("main") || justLogin.equals("back")) {// 从起始页注册
											it.putExtra("fromRegist", "fromStart");
										} else if (justLogin.equals("indexmore")) {// 从更多页个人中心注册
											it.putExtra("fromRegist", "fromIndexMore");
										} else if (justLogin.equals("setup")) {// 从设置页注册
											it.putExtra("fromRegist", "fromSetup");
										} else if (justLogin.equals("profit")) {// 从我的收益注册
											it.putExtra("fromRegist", "fromProfit");
										}

										startActivity(it);
									}
								}).create().show();
//			} else {
//				UserUtils.showDialog(this, this.getResources().getString(R.string.user_login_phone_show_error));
//			}
			break;
		case 4:
			GolukUtils.showToast(this, this.getResources().getString(R.string.user_netword_outtime));
			mApplication.isUserLoginSucess = false;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			// mBackButton.setEnabled(true);
			break;
		case 5:
			mApplication.isUserLoginSucess = false;
			closeProgressDialog();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			// mBackButton.setEnabled(true);
			new AlertDialog.Builder(mContext)
					.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
					.setMessage(this.getResources().getString(R.string.user_login_password_limit_top_hint))
					.setPositiveButton(this.getResources().getString(R.string.user_repwd_ok),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									mApplication.mLoginManage.setUserLoginInterface(null);
									Intent it = new Intent(InternationUserLoginActivity.this, InternationUserRepwdActivity.class);
									it.putExtra("errorPwdOver", mEditTextPhoneNumber.getText().toString());
									if (justLogin.equals("main") || justLogin.equals("back")) {// 从起始页注册
										it.putExtra("fromRegist", "fromStart");
									} else if (justLogin.equals("indexmore")) {// 从更多页个人中心注册
										it.putExtra("fromRegist", "fromIndexMore");
									} else if (justLogin.equals("setup")) {// 从设置页注册
										it.putExtra("fromRegist", "fromSetup");
									} else if (justLogin.equals("profit")) {// 从我的收益注册
										it.putExtra("fromRegist", "fromProfit");
									}
									startActivity(it);
								}
							}).create().show();
			break;
		// 密码错误
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
		/*case R.id.user_login_layout_btn:
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
			break;*/

		default:
			break;
		}
		return false;
	}

	/**
	 * 关闭加载中对话框
	 */
	private void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
			mEditTextPhoneNumber.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mTextViewRegist.setEnabled(true);
			mTextViewForgetPwd.setEnabled(true);
			mBtnLogin.setEnabled(true);
			// mBackButton.setEnabled(true);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		boolean isCurrentRunningForeground = isRunningForeground();
		flag = isCurrentRunningForeground;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mCustomProgressDialog != null) {
			if (mCustomProgressDialog.isShowing()) {
				mCustomProgressDialog.close();
				mCustomProgressDialog = null;
			}
		}
		super.onDestroy();
	}

	public boolean isRunningForeground() {
		String packageName = getPackageName(this);
		String topActivityClassName = getTopActivityName(this);
		GolukDebugUtils.i(TAG, "packageName=" + packageName + ",topActivityClassName=" + topActivityClassName);
		if (packageName != null && topActivityClassName != null && topActivityClassName.startsWith(packageName)) {
			GolukDebugUtils.i(TAG, "---> isRunningForeGround");
			return true;
		} else {
			GolukDebugUtils.i(TAG, "---> isRunningBackGround");
			return false;
		}
	}

	public String getTopActivityName(Context context) {
		String topActivityClassName = null;
		ActivityManager activityManager = (ActivityManager) (context
				.getSystemService(android.content.Context.ACTIVITY_SERVICE));
		// android.app.ActivityManager.getRunningTasks(int maxNum)
		// 即最多取得的运行中的任务信息(RunningTaskInfo)数量
		List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
		if (runningTaskInfos != null) {
			ComponentName f = runningTaskInfos.get(0).topActivity;
			topActivityClassName = f.getClassName();

		}
		// 按下Home键盘后 topActivityClassName
		return topActivityClassName;
	}

	public String getPackageName(Context context) {
		String packageName = context.getPackageName();
		return packageName;
	}

//	public void pageNotifyCallBack(int success, Object param1, Object param2) {
//		GolukDebugUtils.e("","facebooklogin success = " + success + "data = " + param2.toString());
//
//		JSONObject rootObj;
//		try {
//			rootObj = new JSONObject((String) param2);
//
//			// 登录成功后，存储用户的登录信息
//			mSharedPreferences = mApplication.getContext().getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
//			mEditor = mSharedPreferences.edit();
//			mEditor.putBoolean("FirstLogin", false);
//			// 提交
//			mEditor.commit();
//
//			mSharedPreferences = mApplication.getContext().getSharedPreferences("setup", Context.MODE_PRIVATE);
//			mEditor = mSharedPreferences.edit();
//			mEditor.putString("uid", rootObj.getJSONObject("data").getString("uid").toString());
//			mEditor.commit();
//			GolukApplication.getInstance().mCurrentUId = rootObj.getJSONObject("data").getString("uid").toString();
//			mApplication.isUserLoginSucess = true;
//			mApplication.loginoutStatus = false;
//
//			mEditTextPhoneNumber.setEnabled(true);
//			mEditTextPwd.setEnabled(true);
//			mTextViewRegist.setEnabled(true);
//			mTextViewForgetPwd.setEnabled(true);
//			mBtnLogin.setEnabled(true);
//			mApplication.mUser.timerCancel();
//			mApplication.autoLoginStatus = 2;
//			Intent it = new Intent();
//			if ("profit".equals(justLogin)) {
//				it.setClass(InternationUserLoginActivity.this, MyProfitActivity.class);
//				startActivity(it);
//			} else {
//				setResult(Activity.RESULT_OK, it);
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		this.finish();
//	}

}
