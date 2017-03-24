package com.mobnote.golukmain;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBindPhoneNum;
import com.mobnote.eventbus.EventLoginSuccess;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.user.UserIdentifyInterface;
import com.mobnote.user.UserProtocolClickableSpan;
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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
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
 * 注册
 * 
 * 1、注册手机号密码 2、获取验证码 3、登陆
 * 
 * @author mobnote
 *
 */
public class UserRegistActivity extends BaseActivity implements OnClickListener, UserIdentifyInterface, OnTouchListener {

	/** 注册title **/
	private ImageButton mBackButton;
	private TextView mTextViewTitle;
	/** 手机号、密码、注册按钮 **/
	private EditText mEditTextPhone, mEditTextPwd;
	private Button mBtnRegist;

	private Context mContext = null;
	private GolukApplication mApplication = null;

	/** 注册 **/
	private CustomLoadingDialog mCustomProgressDialog = null;
	/** 获取验证码 **/
	private CustomLoadingDialog mCustomProgressDialogIdentify = null;
	/** 记录注册成功的状态 **/
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	/** 注册成功跳转页面的判断标志 */
	private String registOk = null;

	private TextView mHintText = null;

	public void onEventMainThread(EventLoginSuccess event) {
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_regist);

		mContext = this;

		mApplication = (GolukApplication) getApplication();
		EventBus.getDefault().register(this);
		initView();
		getInfo();
		// title
		if ("fromBindPhone".equals(registOk)) {
			mTextViewTitle.setText(R.string.str_binding_phone);
			mEditTextPwd.setVisibility(View.GONE);
			findViewById(R.id.imageview_split).setVisibility(View.GONE);
		} else {
			mTextViewTitle.setText(R.string.user_regist);
		}

		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_regist_loading));
		}
		if (null == mCustomProgressDialogIdentify) {
			mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_identify_loading));
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		mApplication.setContext(mContext, "UserRegist");

		ZhugeUtils.eventRegist(this);
	}

	public void initView() {
		// title
		mBackButton = (ImageButton) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		// 手机号、密码、注册按钮
		mEditTextPhone = (EditText) findViewById(R.id.user_regist_phonenumber);
		mEditTextPwd = (EditText) findViewById(R.id.user_regist_pwd);
		mBtnRegist = (Button) findViewById(R.id.user_regist_btn);
		mHintText = (TextView) findViewById(R.id.user_regist_hint);

		/**
		 * 监听绑定
		 */
		mBackButton.setOnClickListener(this);
		mBtnRegist.setOnClickListener(this);
		mBtnRegist.setOnTouchListener(this);

	}

	public void onEventMainThread(EventBindPhoneNum event) {
		if (null == event) {
			return;
		}

		if (1 == event.getCode()) {
			finish();
		}
	}

	private void showHintText(TextView hintText) {
		String allText = this.getString(R.string.str_user_regist_below_hint);
		SpannableString spannableString = new SpannableString(allText);
		ClickableSpan clickableSpan = new UserProtocolClickableSpan(this);
		spannableString.setSpan(clickableSpan, 8, 12, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		ClickableSpan clickableSpan2 = new UserProtocolClickableSpan(this);
		spannableString.setSpan(clickableSpan2, 13, allText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		hintText.setText(spannableString);
		hintText.setMovementMethod(LinkMovementMethod.getInstance());
	}

	/**
	 * 手机号码获取
	 */
	public void getInfo() {

		showHintText(mHintText);

		Intent itLoginPhone = getIntent();
		if (null != itLoginPhone.getStringExtra("intentLogin")) {
			String number = itLoginPhone.getStringExtra("intentLogin").toString();
			GolukDebugUtils.i("user", number);
			mEditTextPhone.setText(UserUtils.formatSavePhone(number));
			mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
		}
		Intent itRepassword = getIntent();
		if (null != itRepassword.getStringExtra("intentRepassword")) {
			String repwdNum = itRepassword.getStringExtra("intentRepassword").toString();
			mEditTextPhone.setText(repwdNum);
			mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
		}

		/**
		 * 判断是从哪个入口进行的注册
		 */
		Intent itRegist = getIntent();
		if (null != itRegist.getStringExtra("fromRegist")) {
			registOk = itRegist.getStringExtra("fromRegist").toString();
		}
		GolukDebugUtils.i("final", "--------UserRegistActivty-------registOk----" + registOk);

		getPhone();

		// 手机号、密码、验证码文本框改变监听
		mEditTextPhone.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				String phone = mEditTextPhone.getText().toString().replace("-", "");
				String pwd = mEditTextPwd.getText().toString();
				if (arg1) {
					// 注册按钮
					if (!"".equals(phone) && !"".equals(pwd) && phone.length() == 11 && pwd.length() >= 6
							&& UserUtils.isMobileNO(phone)) {
						mBtnRegist.setBackgroundResource(R.drawable.icon_login);
						mBtnRegist.setEnabled(true);
					} else {
						mBtnRegist.setBackgroundResource(R.drawable.icon_more);
						mBtnRegist.setEnabled(false);
					}
				}
			}
		});

		mEditTextPhone.addTextChangedListener(new TextWatcher() {
			private boolean isDelete = false;

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhone.getText().toString().replace("-", "");
				String pwd = mEditTextPwd.getText().toString();
				// 注册按钮
				if ("fromBindPhone".equals(registOk)) {
					if (!"".equals(phone) && phone.length() == 11 && UserUtils.isMobileNO(phone)) {
						mBtnRegist.setBackgroundResource(R.drawable.icon_login);
						mBtnRegist.setEnabled(true);
					} else {
						mBtnRegist.setBackgroundResource(R.drawable.icon_more);
						mBtnRegist.setEnabled(false);
					}
				} else {
					if (!"".equals(phone) && !"".equals(pwd) && phone.length() == 11 && pwd.length() >= 6
							&& UserUtils.isMobileNO(phone)) {
						mBtnRegist.setBackgroundResource(R.drawable.icon_login);
						mBtnRegist.setEnabled(true);
					} else {
						mBtnRegist.setBackgroundResource(R.drawable.icon_more);
						mBtnRegist.setEnabled(false);
					}
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
				String phone = mEditTextPhone.getText().toString().replace("-", "");
				String pwd = mEditTextPwd.getText().toString();
				// 注册按钮
				if (!"fromBindPhone".equals(registOk)) {
					if (!"".equals(phone) && !"".equals(pwd) && phone.length() == 11 && pwd.length() >= 6
							&& UserUtils.isMobileNO(phone)) {
						mBtnRegist.setBackgroundResource(R.drawable.icon_login);
						mBtnRegist.setEnabled(true);
					} else {
						mBtnRegist.setBackgroundResource(R.drawable.icon_more);
						mBtnRegist.setEnabled(false);
					}
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
		} else if (id == R.id.user_regist_btn) {
			// 点按钮后,弹出登录中的提示,样式使用系统 loading 样式,文字描述:注册中
			// 注册成功:弹出系统短提示:注册成功,以登录状态进入 Goluk 首页
			regist();
		}
	}

	/**
	 * 注册
	 */
	public void regist() {
		String phone = mEditTextPhone.getText().toString().replace("-", "");
		String password = mEditTextPwd.getText().toString();

		if (!"".equals(phone) && UserUtils.isMobileNO(phone)) {
			if (("fromBindPhone".equals(registOk)) || (!"".equals(password))) {
				mBtnRegist.setEnabled(true);
				if (("fromBindPhone".equals(registOk)) || (password.length() >= 6 && password.length() <= 16)) {
					if (!UserUtils.isNetDeviceAvailable(mContext)) {
						GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
					} else {
						if (!mApplication.mTimerManage.flag) {
							GolukUtils.showToast(this, this.getResources().getString(R.string.user_timer_count_hint));
						} else {
							mApplication.mTimerManage.timerCancel();
							mApplication.mIdentifyManage.setUserIdentifyInterface(this);
							boolean b = mApplication.mIdentifyManage.getIdentify(true, phone);
							if (b) {
								UserUtils.hideSoftMethod(this);
								mCustomProgressDialogIdentify.show();
								mBtnRegist.setEnabled(false);
								mEditTextPhone.setEnabled(false);
								mEditTextPwd.setEnabled(false);
								mBackButton.setEnabled(false);
							} else {
								closeProgressDialogIdentify();
								GolukUtils.showToast(mContext,
										this.getResources().getString(R.string.user_getidentify_fail));
							}
						}

					}
				} else {
					UserUtils.hideSoftMethod(this);
					UserUtils.showDialog(UserRegistActivity.this,
							this.getResources().getString(R.string.user_login_password_show_error));
					mBtnRegist.setEnabled(true);
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
			mBtnRegist.setEnabled(false);
			mEditTextPhone.setEnabled(false);
			mEditTextPwd.setEnabled(false);
			mBackButton.setEnabled(false);
			break;
		// 获取验证码成功
		case 1:
			closeProgressDialogIdentify();
			GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_success));

			String phone = mEditTextPhone.getText().toString();
			String password = mEditTextPwd.getText().toString();
			Intent getIdentify = new Intent(UserRegistActivity.this, UserIdentifyActivity.class);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_DIFFERENT, true);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_PHONE, phone);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_PASSWORD, password);
			getIdentify.putExtra(UserIdentifyActivity.IDENTIFY_INTER_REGIST, registOk);
			GolukDebugUtils.i("final", "------UserRegistActivity-------identifyCallbackInterface-------registOk------"
					+ registOk);
			startActivity(getIdentify);
			break;
		// 获取验证码失败
		case 2:
			closeProgressDialogIdentify();
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_getidentify_fail));
			break;
		// code=201
		case 3:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_getidentify_limit));
			break;
		// code=500
		case 4:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_background_error));
			break;
		// code=405
		case 5:
			closeProgressDialogIdentify();
			new AlertDialog.Builder(this)
					.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
					.setMessage(this.getResources().getString(R.string.user_already_regist))
					.setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
					.setPositiveButton(this.getResources().getString(R.string.user_immediately_ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									if (mApplication.loginoutStatus = true) {
										String phone = mEditTextPhone.getText().toString();
										mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
										mEditor = mSharedPreferences.edit();
										mEditor.putString("setupPhone", phone);
										mEditor.putBoolean("noPwd", true);
										mEditor.commit();
									}
									finish();
								}
							}).create().show();
			break;
		// code=440
		case 6:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_phone_input_error));
			break;
		// code=480
		case 7:
			closeProgressDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_send_identify_fail));
			break;
		// code=470
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

	/**
	 * 获取手机号
	 */
	public void getPhone() {
		if (mApplication.loginoutStatus = true) {
			String phone = mEditTextPhone.getText().toString();
			mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
			mEditor = mSharedPreferences.edit();
			mEditor.putString("setupPhone", phone);
			mEditor.putBoolean("noPwd", false);
			mEditor.commit();
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		int id = view.getId();
		if (id == R.id.user_regist_btn) {
			String phoneNumber = mEditTextPhone.getText().toString().replace("-", "");
			String pwd = mEditTextPwd.getText().toString();
			if (!"".equals(phoneNumber) && !"".equals(pwd)) {
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
		} else {
		}
		return false;
	}

	/**
	 * 关闭注册中获取验证码的对话框
	 */
	private void closeProgressDialogIdentify() {
		if (null != mCustomProgressDialogIdentify) {
			mCustomProgressDialogIdentify.close();
			mBtnRegist.setEnabled(true);
			mEditTextPhone.setEnabled(true);
			mEditTextPwd.setEnabled(true);
			mBackButton.setEnabled(true);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		closeProgressDialogIdentify();
		if (mCustomProgressDialog != null) {
			mCustomProgressDialog.close();
		}
		mCustomProgressDialogIdentify = null;
		mCustomProgressDialog = null;
	}
}
