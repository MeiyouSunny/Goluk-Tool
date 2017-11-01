package com.mobnote.golukmain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBindPhoneNum;
import com.mobnote.eventbus.EventLoginSuccess;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.golukmain.userlogin.UserResult;
import com.mobnote.golukmain.userlogin.UserloginBeanRequest;
import com.mobnote.user.CountDownButtonHelper;
import com.mobnote.user.UserIdentifyInterface;
import com.mobnote.user.UserRegistAndRepwdInterface;
import com.mobnote.user.UserUtils;
import com.mobnote.user.CountDownButtonHelper.OnFinishListener;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;
import com.sina.weibo.sdk.utils.MD5;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 获取验证码
 * 
 * @author mobnote
 *
 */
public class UserIdentifyActivity extends BaseActivity implements OnClickListener, OnTouchListener,
		UserIdentifyInterface, UserRegistAndRepwdInterface,IRequestResultListener {

	public static final String IDENTIFY_DIFFERENT = "identify_different";
	public static final String IDENTIFY_PHONE = "identify_phone";
	public static final String IDENTIFY_PASSWORD = "identify_password";
	public static final String IDENTIFY_INTER_REGIST = "identify_inter_regist";
	private static final String TAG = "lily";
	/** Application & Context **/
	private GolukApplication mApp = null;
	private Context mContext = null;
	/** title **/
	private ImageButton mBtnBack = null;
	private TextView mTextTitle = null;
	/** body **/
	private ImageView mImageAnim = null;
	private EditText mEditTextOne, mEditTextTwo, mEditTextThree, mEditTextFour, mEditTextFive, mEditTextSix;
	private Button mBtnCount = null;
	private Button mBtnNext = null;
	/** 动画 **/
	private AnimationDrawable mAnimation = null;
	/** 发送验证码手机号 **/
	private String title_phone = "";
	/** 密码 **/
	private String intentPassword = "";
	/** 跳转注册页标识 **/
	private String intentRegistInter = "";
	/** 获取验证码 **/
	private CustomLoadingDialog mCustomDialogIdentify = null;
	/** 注册 **/
	private CustomLoadingDialog mCustomDialogRegist = null;
	/** 重置密码 **/
	private CustomLoadingDialog mCustomDialogRepwd = null;
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	/** 倒计时帮助类 **/
	private CountDownButtonHelper mCountDownhelper = null;
	/** 注册/重置密码标识 **/
	private boolean justDifferent = false;
	/** 自动获取验证码 **/
	private BroadcastReceiver smsReceiver;
	private IntentFilter smsFilter;
	private String strBody = "";
	/** 销毁广播标识 **/
	private int click = 0;
	boolean b = true;
	
	private UserloginBeanRequest userloginBean = null;

	public void onEventMainThread(EventLoginSuccess event) {
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		EventBus.getDefault().register(this);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		setContentView(R.layout.user_identify_layout);

		mContext = this;
		mApp = (GolukApplication) getApplication();
		userloginBean = new UserloginBeanRequest(IPageNotifyFn.PageType_Login, this);
		initView();
		getInfo();

		countTime();

		// 自动获取验证码
		getSmsMessage();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(mContext, "UserIdentify");
	}

	/**
	 * 初始化view
	 */
	public void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		mImageAnim = (ImageView) findViewById(R.id.user_identify_anim_image);
		mEditTextOne = (EditText) findViewById(R.id.user_identify_item_one);
		mEditTextTwo = (EditText) findViewById(R.id.user_identify_item_two);
		mEditTextThree = (EditText) findViewById(R.id.user_identify_item_three);
		mEditTextFour = (EditText) findViewById(R.id.user_identify_item_four);
		mEditTextFive = (EditText) findViewById(R.id.user_identify_item_five);
		mEditTextSix = (EditText) findViewById(R.id.user_identify_item_six);
		mBtnCount = (Button) findViewById(R.id.user_identify_layout_getidentify_btn);
		mBtnNext = (Button) findViewById(R.id.user_identify_btn);

		// 手机动画
		mAnimation = (AnimationDrawable) mImageAnim.getBackground();
		if (null != mAnimation) {
			mAnimation.start();
		}
		// 获取验证码
		if (null == mCustomDialogIdentify) {
			mCustomDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_identify_loading));
		}
		// 注册
		if (null == mCustomDialogRegist) {
			mCustomDialogRegist = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_regist_loading));
		}
		// 重置密码
		if (null == mCustomDialogRepwd) {
			mCustomDialogRepwd = new CustomLoadingDialog(mContext, this.getResources().getString(
					R.string.str_repwd_loading));
		}
		// 6个框都为空
		int one = mEditTextOne.getText().toString().replace(" ", "").length();
		int Two = mEditTextTwo.getText().toString().replace(" ", "").length();
		int Three = mEditTextThree.getText().toString().replace(" ", "").length();
		int Four = mEditTextFour.getText().toString().replace(" ", "").length();
		int Five = mEditTextFive.getText().toString().replace(" ", "").length();
		int Six = mEditTextSix.getText().toString().replace(" ", "").length();
		if (one == 0 && Two == 0 && Three == 0 && Four == 0 && Five == 0 && Six == 0) {
			GolukDebugUtils.i(TAG, "--------6个都是空空空空---------");
			mEditTextOne.setFocusable(true);
			mEditTextTwo.setFocusable(false);
			mEditTextThree.setFocusable(false);
			mEditTextFour.setFocusable(false);
			mEditTextFive.setFocusable(false);
			mEditTextSix.setFocusable(false);
		}
		// 监听
		mBtnBack.setOnClickListener(this);
		mBtnCount.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);

		mEditTextOne.addTextChangedListener(mTextWatcher);
		mEditTextTwo.addTextChangedListener(mTextWatcher);
		mEditTextThree.addTextChangedListener(mTextWatcher);
		mEditTextFour.addTextChangedListener(mTextWatcher);
		mEditTextFive.addTextChangedListener(mTextWatcher);
		mEditTextSix.addTextChangedListener(mTextWatcher);
	}

	/**
	 * 获取信息
	 */
	public void getInfo() {
		Intent it = getIntent();
		if (null != it.getStringExtra(IDENTIFY_PHONE)) {
			title_phone = it.getStringExtra(IDENTIFY_PHONE).toString();
			mTextTitle.setText(this.getResources().getString(R.string.str_send_message) + title_phone);
		}

		justDifferent = it.getBooleanExtra(IDENTIFY_DIFFERENT, false);
		ZhugeUtils.eventSmsCode(this, justDifferent);
		GolukDebugUtils.i(TAG, "-------justDifferent-------" + justDifferent);

		if (null != it.getStringExtra(IDENTIFY_PASSWORD)) {
			intentPassword = it.getStringExtra(IDENTIFY_PASSWORD).toString();
		}

		if (null != it.getStringExtra(IDENTIFY_INTER_REGIST)) {
			intentRegistInter = it.getStringExtra(IDENTIFY_INTER_REGIST).toString();
		}

	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.back_btn) {
			mCountDownhelper.timer.cancel();
			finish();
		} else if (id == R.id.user_identify_layout_getidentify_btn) {
			GolukDebugUtils.i(TAG, "-----saveDifferent----" + justDifferent);
			if (null != title_phone) {
				getUserIdentify(justDifferent, title_phone.replace("-", ""));
			}
		} else if (id == R.id.user_identify_btn) {
			String vCode = mEditTextOne.getText().toString() + mEditTextTwo.getText().toString()
					+ mEditTextThree.getText().toString() + mEditTextFour.getText().toString()
					+ mEditTextFive.getText().toString() + mEditTextSix.getText().toString();
			GolukDebugUtils.i(TAG, "-----UserIdentifyActivity------vCode-------" + vCode);
			toRegistAndRepwd(justDifferent, title_phone.replace("-", ""), MD5.hexdigest(intentPassword), vCode);
		} else {
		}
	}

	/**
	 * 重新获取验证码
	 * 
	 * @param phone
	 */
	public void getUserIdentify(boolean flag, String phone) {
		if (!UserUtils.isNetDeviceAvailable(this)) {
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
		} else {
			mApp.mIdentifyManage.setUserIdentifyInterface(this);
			boolean b = mApp.mIdentifyManage.getIdentify(flag, phone);
			if (b) {
				mCustomDialogIdentify.show();
				mBtnBack.setEnabled(false);
				mEditTextOne.setEnabled(false);
				mBtnCount.setEnabled(false);
				mBtnNext.setEnabled(false);
			} else {
				closeDialogIdentify();
			}
		}

	}

	/**
	 * 获取验证码接口回调
	 */
	@Override
	public void identifyCallbackInterface() {
		switch (mApp.identifyStatus) {
		// 验证码获取中
		case 0:
			mCustomDialogIdentify.show();
			mBtnBack.setEnabled(false);
			mEditTextOne.setEnabled(false);
			mBtnCount.setEnabled(false);
			mBtnNext.setEnabled(false);
			break;
		// 获取验证码成功
		case 1:
			closeDialogIdentify();
			GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_success));
			// 倒计时
			countTime();

			break;
		// 获取验证码失败
		case 2:
			closeDialogIdentify();
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_getidentify_fail));
			break;
		// code=201
		case 3:
			closeDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_getidentify_limit));
			break;
		// code=500
		case 4:
			closeDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_background_error));
			break;
		// code=405
		case 5:
			closeDialogIdentify();
			if (justDifferent) {
				new AlertDialog.Builder(this)
						.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
						.setMessage(this.getResources().getString(R.string.user_already_regist))
						.setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
						.setPositiveButton(this.getResources().getString(R.string.user_immediately_ok),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										if (mApp.loginoutStatus = true) {
											mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
											mEditor = mSharedPreferences.edit();
											mEditor.putString("setupPhone", title_phone);
											mEditor.putBoolean("noPwd", true);
											mEditor.commit();
										}
										mCountDownhelper.timer.cancel();
										finish();
									}
								}).create().show();
			} else {
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
										Intent intentRepwd = new Intent(UserIdentifyActivity.this,
												UserRegistActivity.class);
										intentRepwd.putExtra("intentRepassword", title_phone);
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
										mCountDownhelper.timer.cancel();
										finish();
									}
								}).create().show();
			}
			break;
		// code=440
		case 6:
			closeDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_phone_input_error));
			break;
		// code=480
		case 7:
			closeDialogIdentify();
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_send_identify_fail));
			break;
		// code=470
		case 8:
			closeDialogIdentify();
			UserUtils.showDialog(mContext, this.getResources().getString(R.string.count_background_identify_count));
			break;
		case 10:
			closeDialogIdentify();
			break;
		default:
			break;
		}
	}

	/**
	 * 验证码输入框改变事件
	 */
	TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			if (arg0.length() == 1) {
				if (mEditTextOne.isFocusable()) {
					mEditTextTwo.setFocusable(true);
					mEditTextTwo.setFocusableInTouchMode(true);
				} else if (mEditTextTwo.isFocusable()) {
					mEditTextThree.setFocusable(true);
					mEditTextThree.setFocusableInTouchMode(true);
				} else if (mEditTextThree.isFocusable()) {
					mEditTextFour.setFocusable(true);
					mEditTextFour.setFocusableInTouchMode(true);
				} else if (mEditTextFour.isFocusable()) {
					mEditTextFive.setFocusable(true);
					mEditTextFive.setFocusableInTouchMode(true);
				} else if (mEditTextFive.isFocusable()) {
					mEditTextSix.setFocusable(true);
					mEditTextSix.setFocusableInTouchMode(true);
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void afterTextChanged(Editable mEditable) {

			if (mEditable.toString().length() == 1) {
				if (mEditTextOne.isFocused()) {
					mEditTextOne.setFocusable(false);
					mEditTextTwo.requestFocus();
				} else if (mEditTextTwo.isFocused()) {
					mEditTextTwo.setFocusable(false);
					mEditTextThree.requestFocus();
				} else if (mEditTextThree.isFocused()) {
					mEditTextThree.setFocusable(false);
					mEditTextFour.requestFocus();
				} else if (mEditTextFour.isFocused()) {
					mEditTextFour.setFocusable(false);
					mEditTextFive.requestFocus();
				} else if (mEditTextFive.isFocused()) {
					mEditTextFive.setFocusable(false);
					mEditTextSix.requestFocus();
				} else if (mEditTextSix.isFocused()) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mEditTextSix.getWindowToken(), 0);
				}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_DEL) {
			if (mEditTextSix.isFocused()) {

				if (!mEditTextSix.getText().toString().equals("")) {
					mEditTextSix.getText().clear();
					mEditTextSix.requestFocus();
					b = false;
				} else if (!b) {
					mEditTextSix.clearFocus();
					mEditTextSix.setFocusable(false);
					mEditTextFive.setFocusableInTouchMode(true);
					mEditTextFive.getText().clear();
					mEditTextFive.requestFocus();
					b = true;
				} else {
					mEditTextSix.getText().clear();
					mEditTextSix.requestFocus();
					b = false;
				}
			} else if (mEditTextFive.isFocused()) {
				mEditTextFive.clearFocus();
				mEditTextFive.setFocusable(false);
				mEditTextFour.setFocusableInTouchMode(true);
				mEditTextFour.getText().clear();
				mEditTextFour.requestFocus();
			} else if (mEditTextFour.isFocused()) {
				mEditTextFour.clearFocus();
				mEditTextFour.setFocusable(false);
				mEditTextThree.setFocusableInTouchMode(true);
				mEditTextThree.getText().clear();
				mEditTextThree.requestFocus();
			} else if (mEditTextThree.isFocused()) {
				mEditTextThree.clearFocus();
				mEditTextThree.setFocusable(false);
				mEditTextTwo.setFocusableInTouchMode(true);
				mEditTextTwo.getText().clear();
				mEditTextTwo.requestFocus();
			} else if (mEditTextTwo.isFocused()) {
				mEditTextTwo.clearFocus();
				mEditTextTwo.setFocusable(false);
				mEditTextOne.setFocusableInTouchMode(true);
				mEditTextOne.getText().clear();
				mEditTextOne.requestFocus();
			}
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mCountDownhelper.timer.cancel();
			this.finish();
		}
		return false;

	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		int id = view.getId();
		if (id == R.id.user_identify_btn) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mBtnNext.setBackgroundResource(R.drawable.icon_login_click);
				break;
			case MotionEvent.ACTION_UP:
				mBtnNext.setBackgroundResource(R.drawable.icon_login);
				break;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * 关闭获取验证码的loading
	 */
	private void closeDialogIdentify() {
		if (null != mCustomDialogIdentify) {
			mCustomDialogIdentify.close();
			mBtnBack.setEnabled(true);
			mEditTextOne.setEnabled(true);
			mBtnCount.setEnabled(true);
			mBtnNext.setEnabled(true);
		}
	}

	/**
	 * 关闭注册loading
	 */
	private void closeDialogRegist() {
		if (null != mCustomDialogRegist) {
			mCustomDialogRegist.close();
			mBtnBack.setEnabled(true);
			mEditTextOne.setEnabled(true);
			mBtnCount.setEnabled(true);
			mBtnNext.setEnabled(true);
		}
	}

	/**
	 * 关闭重置密码loading
	 */
	private void closeDialogRepwd() {
		if (null != mCustomDialogRepwd) {
			mCustomDialogRepwd.close();
			mBtnBack.setEnabled(true);
			mEditTextOne.setEnabled(true);
			mBtnCount.setEnabled(true);
			mBtnNext.setEnabled(true);
		}
	}

	/**
	 * 判断关闭哪个对话框 // 从设置页注册 it.putExtra("fromRegist", "fromSetup");
	 * 
	 * @param b
	 */
	public void justCloseDialog(boolean b) {
		if (b) {
			closeDialogRegist();
		} else {
			closeDialogRepwd();
		}
	}

	/**
	 * 倒计时
	 */
	public void countTime() {
		mCountDownhelper = new CountDownButtonHelper(this, mBtnCount, this.getResources().getString(
				R.string.user_identify_btn_afresh), 60, 1);
		mCountDownhelper.setOnFinishListener(new OnFinishListener() {

			@Override
			public void finish() {
				mBtnCount.setText(mContext.getResources().getString(R.string.user_identify_btn_afresh));
				// 倒计时结束后手机号、密码可以更改
				mBtnCount.setEnabled(true);
			}
		});
		mCountDownhelper.start();
		mApp.mTimerManage.timerCount();
	}

	/**
	 * 注册/重置密码
	 * 
	 * @param flag
	 * @param phone
	 * @param password
	 * @param vCode
	 */
	@SuppressWarnings("static-access")
	public void toRegistAndRepwd(boolean flag, String phone, String password, String vCode) {
		if (!UserUtils.isNetDeviceAvailable(this)) {
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
		} else {
			// TODO 需要判断获取验证码次数
			if ("".equals(vCode) || null == vCode) {
				GolukUtils.showToast(mApp.getContext(), this.getResources().getString(R.string.user_no_getidentify));
			} else {
				if (vCode.length() < 6) {
					GolukUtils.showToast(mApp.getContext(), this.getResources()
							.getString(R.string.user_identify_format));
				} else {
					GolukDebugUtils.i(TAG, "---------useridentifymanage_count------"
							+ mApp.mIdentifyManage.useridentifymanage_count);
					if (mApp.mIdentifyManage.useridentifymanage_count > mApp.mIdentifyManage.IDENTIFY_COUNT) {
						UserUtils.showDialog(mContext,
								this.getResources().getString(R.string.count_identify_count_six_limit));
					} else {
						mApp.mRegistAndRepwdManage.setUserRegistAndRepwd(this);
						boolean b = false;
						if ("fromBindPhone".equals(intentRegistInter)) {
//							b = mApp.mRegistAndRepwdManage.bindPhoneNum(phone, vCode);
                            b = true;
                            mApp.mRegistAndRepwdManage.sendBindRequest(mApp.mCurrentUId, phone, vCode);
						} else {
							b = mApp.mRegistAndRepwdManage.registAndRepwd(flag, phone, password, vCode);
						}
						if (b) {
							if (flag) {
								mCustomDialogRegist.show();
							} else {
								mCustomDialogRepwd.show();
							}
							mBtnBack.setEnabled(false);
							mEditTextOne.setEnabled(false);
							mBtnCount.setEnabled(false);
							mBtnNext.setEnabled(false);
						} else {
							justCloseDialog(flag);
							if (flag) {
								GolukUtils
										.showToast(mContext, this.getResources().getString(R.string.user_regist_fail));
							} else {
								GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_repwd_fail));
							}
						}
					}

				}
			}
		}

	}

	/**
	 * 注册/重置密码接口回调
	 */
	@Override
	public void registAndRepwdInterface() {
		switch (mApp.registStatus) {
		// 注册/重置密码 中
		case 1:
			justCloseDialog(justDifferent);
			mBtnBack.setEnabled(false);
			mEditTextOne.setEnabled(false);
			mBtnCount.setEnabled(false);
			mBtnNext.setEnabled(false);
			break;
		// 注册/重置密码成功
		case 2:
			justCloseDialog(justDifferent);
			if ("fromBindPhone".equals(intentRegistInter)) {
				EventBus.getDefault().post(new EventBindPhoneNum(1));
				if (null != title_phone) {
					mApp.mCurrentPhoneNum = title_phone.replace("-", "");
				};
				finish();
			} else {
				if (justDifferent) {
					ZhugeUtils.eventRegistSuccess(this);
					GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_success));
				} else {
					GolukUtils.showToast(this, this.getResources().getString(R.string.user_repwd_success));
				}

				registLogin();
			}
			break;
		// 注册/重置失败
		case 3:
			justCloseDialog(justDifferent);
			if ("fromBindPhone".equals(intentRegistInter)) {
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.str_bind_fail));
			} else {
				if (justDifferent) {
					GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_regist_fail));
				} else {
					GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_repwd_fail));
				}
			}
			break;
		// code = 500
		case 4:
			justCloseDialog(justDifferent);
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_background_error));
			break;
		// code = 405
		case 5:
			justCloseDialog(justDifferent);
			if (justDifferent) {
				UserUtils.showDialog(this, this.getResources().getString(R.string.user_already_regist));
			} else {
				new AlertDialog.Builder(this)
						.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
						.setMessage(this.getResources().getString(R.string.user_no_regist))
						.setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
						.setPositiveButton(this.getResources().getString(R.string.user_immediately_regist),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										Intent intentRepwd = new Intent(UserIdentifyActivity.this,
												UserRegistActivity.class);
										intentRepwd.putExtra("intentRepassword", title_phone);
										startActivity(intentRepwd);
										finish();
									}
								}).create().show();
			}
			break;
		// code = 406
		case 6:
			justCloseDialog(justDifferent);
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_identify_right_hint));
			break;
		// code = 407
		case 7:
			justCloseDialog(justDifferent);
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_identify_outtime));
			break;
		// code = 480
		case 8:
			justCloseDialog(justDifferent);
			UserUtils.showDialog(this, this.getResources().getString(R.string.user_getidentify_fail));
			break;
		// 超时
		case 9:
			justCloseDialog(justDifferent);
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_netword_outtime));
			break;
		default:
			break;
		}
	}

	/**
	 * 注册完成后自动调一次登录的接口，以存储用户信息
	 */
	public void registLogin() {
		
		userloginBean.loginByPhone(title_phone.replace("-", ""),  MD5.hexdigest(intentPassword),"");
		mApp.loginStatus = 0;// 登录中
//		
//		GolukDebugUtils.i("", "---------registLogin()----------");
//		String condi = "{\"PNumber\":\"" + title_phone.replace("-", "") + "\",\"Password\":\"" + intentPassword
//				+ "\",\"tag\":\"android\"}";
//		boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Login,
//				condi);
//		GolukDebugUtils.i("final", "--------UserIdentifyActivity--------registLogin()-------b-------" + b);
//		if (b) {
			// 登录成功跳转
//			mApp.loginStatus = 0;// 登录中
//		}
	}

//	/**
//	 * 登录的回调
//	 */
//	public void registLoginCallBack(int success, Object obj) {
//		GolukDebugUtils.e("", "---------------registLoginCallBack()-------------------");
//		mApp.loginStatus = 0;// 登录中
//		if (1 == success) {
//			
//		} else {
//			// 回调执行失败
//		}
//	}

	/**
	 * 保存手机号
	 */
	public void putPhone() {
		mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		GolukDebugUtils.i(TAG, "phone==" + title_phone);
		mEditor.putString("setupPhone", title_phone);
		mEditor.putBoolean("noPwd", true);
		mEditor.commit();
	}

	/**
	 * 获取短信
	 */
	public void getSmsMessage() {
		// 自动获取验证码请求
		smsFilter = new IntentFilter();
		smsFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		smsFilter.setPriority(Integer.MAX_VALUE);
		smsReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent == null || intent.getExtras() == null)
					return;
				Object[] objs = (Object[]) intent.getExtras().get("pdus");
				if (objs == null || objs.length <= 0)
					return;
				for (Object obj : objs) {
					byte[] pdu = (byte[]) obj;
					SmsMessage sms = SmsMessage.createFromPdu(pdu);
					if (null != sms) {
						// 短信的内容
						String message = sms.getMessageBody();
						String regEx = "[^0-9]";
						Pattern p = Pattern.compile(regEx);
						if (null != p && null != message) {
							Matcher m = p.matcher(message);
							if (null != m) {
								strBody = m.replaceAll(" ").trim();
							}
						}
					}
				}
				if (strBody == null || strBody.length() < 6)
					return;
				String one = strBody.substring(0, 1);
				String two = strBody.substring(1, 2);
				String three = strBody.substring(2, 3);
				String four = strBody.substring(3, 4);
				String five = strBody.substring(4, 5);
				String six = strBody.substring(strBody.length() - 1);
				GolukDebugUtils.i("kkk", "----one----" + one + "----two---" + two + "----three----" + three
						+ "---four----" + four + "-----five---" + five + "----six-----" + six);
				mEditTextOne.setText(one);
				mEditTextTwo.setText(two);
				mEditTextThree.setText(three);
				mEditTextFour.setText(four);
				mEditTextFive.setText(five);
				mEditTextSix.setText(six);
			}
		};
		// 注册读取短信内容
		registerReceiver(smsReceiver, smsFilter);
		click = 1;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		if (click == 1) {
			unregisterReceiver(smsReceiver);
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if(requestType == IPageNotifyFn.PageType_Login){
			try {
				GolukDebugUtils.i("lily", "-----UserLoginManage-----" + result);
				UserResult userresult = (UserResult) result;
				int code = Integer.parseInt(userresult.code);
				switch (code) {
				case 200:
					String type = "";
					if (justDifferent) {
						type = this.getString(R.string.str_zhuge_regist_success_event);
					} else {
						type = this.getString(R.string.str_zhuge_change_pwd_success);
					}
					ZhugeUtils.eventLoginSuccess(mApp.getContext(), type);
					// 登录成功后，存储用户的登录信息
					mSharedPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putBoolean("FirstLogin", false);
					mEditor.commit();
					mSharedPreferences = mApp.getContext().getSharedPreferences("setup", Context.MODE_PRIVATE);
					mEditor = mSharedPreferences.edit();
					mEditor.putString("uid", userresult.data.uid);
					mEditor.commit();
					// 登录成功跳转
					mApp.loginStatus = 1;// 登录成功
					mApp.isUserLoginSucess = true;
					mApp.registStatus = 2;// 注册成功的状态
					mApp.mUser.timerCancel();
					mApp.autoLoginStatus = 2;

					Intent it = null;
					mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
					
					SharedPrefUtil.saveUserInfo(com.alibaba.fastjson.JSONObject.toJSONString(userresult.data));
					SharedPrefUtil.saveUserToken(userresult.data.token);
					JSONObject json = new JSONObject();
					
					if(!"".equals(userresult.data.phone)){
						json.put("phone",userresult.data.phone);
					}
					if(!"".equals(intentPassword)){
						json.put("pwd", intentPassword);
					}
					json.put("uid", userresult.data.uid);
					SharedPrefUtil.saveUserPwd(json.toString());
				
					GolukApplication.getInstance().parseLoginData(userresult.data);
					EventBus.getDefault().post(new EventLoginSuccess());
					finish();
					break;

				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
