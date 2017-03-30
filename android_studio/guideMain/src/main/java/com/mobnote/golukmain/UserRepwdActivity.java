package com.mobnote.golukmain;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventLoginSuccess;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.user.UserIdentifyInterface;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 重置密码
 * 
 * 1、输入手机号、密码 2、验证码的获取和判断 3、短信验证
 * 
 * @author mobnote
 *
 */
public class UserRepwdActivity extends BaseActivity implements OnClickListener, OnTouchListener, UserIdentifyInterface {

	/** title **/
	private ImageButton mBtnBack;
	private TextView mTextViewTitle;
	/** 手机号、密码、验证码 **/
	private EditText mEditTextPhone, mEditTextPwd;
	private Button mBtnOK;

	private Context mContext = null;
	private GolukApplication mApplication = null;
	/** 重置密码显示进度条 **/
	private CustomLoadingDialog mCustomProgressDialog = null;
	/** 验证码获取显示进度条 **/
	private CustomLoadingDialog mCustomProgressDialogIdentify = null;

	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	/** 重置密码跳转标志 **/
	private String repwdOk = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_repwd);

		mContext = this;

		mApplication = (GolukApplication) getApplication();

		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_repwd_loading));
		}
		if (null == mCustomProgressDialogIdentify) {
			mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_identify_loading));
		}
		initView();
		// title
		mTextViewTitle.setText(this.getResources().getString(R.string.user_login_forgetpwd));
		
		UserUtils.addActivity(UserRepwdActivity.this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mApplication.setContext(mContext, "UserRepwd");
		getInfo();
		ZhugeUtils.eventForgetPwd(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(EventLoginSuccess event) {
		finish();
	}

	public void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		mEditTextPhone = (EditText) findViewById(R.id.user_repwd_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_repwd_pwd);
		mBtnOK = (Button) findViewById(R.id.user_repwd_ok_btn);

		/**
		 * 绑定监听
		 */
		mBtnBack.setOnClickListener(this);
		mBtnOK.setOnClickListener(this);
		mBtnOK.setOnTouchListener(this);
	}

	/**
	 * 获取信息
	 */
	public void getInfo() {
		/**
		 * 登录页密码输入错误超过五次，跳转到重置密码也，并且填入手机号
		 */
		Intent it = getIntent();
		if (null != it.getStringExtra("errorPwdOver")) {
			String phone = it.getStringExtra("errorPwdOver").toString();
			mEditTextPhone.setText(UserUtils.formatSavePhone(phone));
			mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
		}

		/**
		 * 判断是从哪个入口进行的注册
		 */
		Intent itRepwd = getIntent();
		if (null != itRepwd.getStringExtra("fromRegist")) {
			repwdOk = itRepwd.getStringExtra("fromRegist").toString();
		}
		GolukDebugUtils.i("final", "--------UserRegistActivty-------registOk----" + repwdOk);

		putPhones();

		mEditTextPhone.addTextChangedListener(new TextWatcher() {
			private boolean isDelete = false;

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhone.getText().toString().replace("-", "");
				String pwd = mEditTextPwd.getText().toString();
				// 重置按钮
				if (!"".equals(phone) && !"".equals(pwd) && phone.length() == 11 && pwd.length() >= 6
						&& UserUtils.isMobileNO(phone)) {
					mBtnOK.setBackgroundResource(R.drawable.icon_login);
					mBtnOK.setEnabled(true);
				} else {
					mBtnOK.setBackgroundResource(R.drawable.icon_more);
					mBtnOK.setEnabled(false);
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
				String password = mEditTextPwd.getText().toString();
				String phone = mEditTextPhone.getText().toString().replace("-", "");
				if (!"".equals(password) && !phone.equals("") && phone.length() == 11 && password.length() >= 6
						&& UserUtils.isMobileNO(phone)) {
					mBtnOK.setBackgroundResource(R.drawable.icon_login);
					mBtnOK.setEnabled(true);
				} else {
					mBtnOK.setBackgroundResource(R.drawable.icon_more);
					mBtnOK.setEnabled(false);
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
		int id = arg0.getId();
		if (id == R.id.back_btn) {
			finish();
		} else if (id == R.id.user_repwd_ok_btn) {
			// 点按钮后,弹出重置密码中的提示,样式使用系统 loading 样式,文字描述:正在重置
			// 重置密码成功,弹出系统短提示:重置密码成功。同时跳转至登录页面。
			repwd();
		}
	}

	/**
	 * 重置密码
	 */
	public void repwd() {
		String phone = mEditTextPhone.getText().toString().replace("-", "");
		String password = mEditTextPwd.getText().toString();

		if (!"".equals(phone) && UserUtils.isMobileNO(phone)) {
			if (!"".equals(password)) {
				mBtnOK.setFocusable(true);
				if (password.length() >= 6 && password.length() <= 16) {
					if (!UserUtils.isNetDeviceAvailable(this)) {
						UserUtils.hideSoftMethod(this);
						GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
					} else {
						if (!mApplication.mTimerManage.flag) {
							GolukUtils.showToast(this, this.getResources().getString(R.string.user_timer_count_hint));
						} else {
							mApplication.mTimerManage.timerCancel();
							mApplication.mIdentifyManage.setUserIdentifyInterface(this);
							boolean b = mApplication.mIdentifyManage.getIdentify(false, phone);
							if (b) {
								UserUtils.hideSoftMethod(this);
								mCustomProgressDialogIdentify.show();
								mBtnOK.setEnabled(false);
								mEditTextPhone.setEnabled(false);
								mEditTextPwd.setEnabled(false);
								mBtnBack.setEnabled(false);
							} else {
								closeProgressDialogIdentify();
								GolukUtils.showToast(mContext,
										this.getResources().getString(R.string.user_getidentify_fail));
							}
						}

					}
				} else {
					UserUtils.hideSoftMethod(this);
					mBtnOK.setFocusable(true);
					UserUtils.showDialog(UserRepwdActivity.this,
							this.getResources().getString(R.string.user_login_password_show_error));
				}
			}
		} else {
			UserUtils.hideSoftMethod(this);
			UserUtils.showDialog(mContext, this.getResources().getString(R.string.user_login_phone_show_error));
		}

	}

	@Override
	public void identifyCallbackInterface() {
		switch (mApplication.identifyStatus) {
		// 验证码获取中
		case 0:
			UserUtils.hideSoftMethod(this);
			mCustomProgressDialogIdentify.show();
			mBtnOK.setEnabled(false);
			mEditTextPhone.setEnabled(false);
			mEditTextPwd.setEnabled(false);
			mBtnBack.setEnabled(false);
			break;
		// 验证码获取成功
		case 1:
			closeProgressDialogIdentify();
			GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_success));

			String phone = mEditTextPhone.getText().toString();
			String password = mEditTextPwd.getText().toString();
			Intent getIdentify = new Intent(UserRepwdActivity.this, UserIdentifyActivity.class);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_DIFFERENT, false);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_PHONE, phone);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_PASSWORD, password);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_INTER_REGIST, repwdOk);
			GolukDebugUtils.i("final", "------UserRepwdActivity--------------registOk------" + repwdOk);
			startActivity(getIdentify);
			break;
		// 验证码获取失败
		case 2:
			closeProgressDialogIdentify();
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_getidentify_fail));
			break;
		// code = 201
		case 3:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_getidentify_limit));
			break;
		// code = 500
		case 4:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_background_error));
			break;
		// code = 405
		case 5:
			closeProgressDialogIdentify();
			mSharedPreferences = getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
			final String just = mSharedPreferences.getString("toRepwd", "");
			new AlertDialog.Builder(this)
					.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
					.setMessage(this.getResources().getString(R.string.user_no_regist))
					.setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
					.setPositiveButton(this.getResources().getString(R.string.user_immediately_regist),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									String phone = mEditTextPhone.getText().toString();
									Intent intentRepwd = new Intent(UserRepwdActivity.this, UserRegistActivity.class);
									intentRepwd.putExtra("intentRepassword", phone);
									if (just.equals("start") || just.equals("mainActivity")) {
										intentRepwd.putExtra("fromRegist", "fromStart");
									} else if (just.equals("more")) {
										intentRepwd.putExtra("fromRegist", "fromIndexMore");
									} else if (just.equals("set")) {
										intentRepwd.putExtra("fromRegist", "fromSetup");
									} else if(just.equals("toProfit")) {
										intentRepwd.putExtra("fromRegist", "fromProfit");
									}
									startActivity(intentRepwd);
									finish();
								}
							}).create().show();
			break;
		// code = 440
		case 6:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_phone_input_error));
			break;
		// code = 480
		case 7:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_send_identify_fail));
			break;
		// code = 470
		case 8:
			closeProgressDialogIdentify();
			UserUtils.showDialog(mContext, this.getResources().getString(R.string.count_background_identify_count));
			break;
		// 超时
		case 9:
			closeProgressDialogIdentify();
			GolukUtils.showToast(this, this.getResources().getString(R.string.user_netword_outtime));
			break;
		case 10:
			closeProgressDialogIdentify();
			break;
		default:
			break;
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		int id = view.getId();
		if (id == R.id.user_repwd_ok_btn) {
			String phoneNumber = mEditTextPhone.getText().toString().replace("-", "");
			String pwd = mEditTextPwd.getText().toString();
			if (!"".equals(phoneNumber) && !"".equals(pwd)) {
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
		}
		return false;
	}

	public void putPhones() {
		String phone = mEditTextPhone.getText().toString();
		mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		GolukDebugUtils.i("lily", "phone==" + phone);
		mEditor.putString("setupPhone", phone);
		mEditor.putBoolean("noPwd", false);
		mEditor.commit();
	}

	public void putPhone() {
		String phone = mEditTextPhone.getText().toString();
		mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		GolukDebugUtils.i("lily", "phone==" + phone);
		mEditor.putString("setupPhone", phone);
		mEditor.putBoolean("noPwd", true);
		mEditor.commit();
	}

	/**
	 * 关闭重置中获取验证码的对话框
	 */
	private void closeProgressDialogIdentify() {
		if (null != mCustomProgressDialogIdentify) {
			mCustomProgressDialogIdentify.close();
			mBtnOK.setEnabled(true);
			mEditTextPhone.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mBtnBack.setEnabled(true);
		}
	}

}
