package cn.com.mobnote.golukmobile;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.user.CountDownButtonHelper;
import cn.com.mobnote.user.CountDownButtonHelper.OnFinishListener;
import cn.com.mobnote.user.UserIdentifyInterface;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 获取验证码
 * 
 * @author mobnote
 *
 */
public class UserIdentifyActivity extends BaseActivity implements OnClickListener, OnTouchListener,
		UserIdentifyInterface {

	public static final String IDENTIFY_DIFFERENT = "identify_different";
	public static final String IDENTIFY_PHONE = "identify_phone";
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
	/** 获取验证码 **/
	private CustomLoadingDialog mCustomDialogIdentify = null;
	private SharedPreferences mSharedPreferences = null;
	private Editor mEditor = null;
	/** 倒计时帮助类 **/
	private CountDownButtonHelper mCountDownhelper = null;
	
	private boolean justDifferent = false;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.user_identify_layout);

		mContext = this;
		mApp = (GolukApplication) getApplication();

		initView();
		getInfo();
		
		countTime();

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

		mAnimation = (AnimationDrawable) mImageAnim.getBackground();

		if (null != mAnimation) {
			mAnimation.start();
		}

		if (null == mCustomDialogIdentify) {
			mCustomDialogIdentify = new CustomLoadingDialog(mContext, "验证码获取中……");
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
			mTextTitle.setText("正在发送短信到+86" + title_phone);
		}

		if (null != it.getStringExtra(IDENTIFY_DIFFERENT)) {
			justDifferent = it.getBooleanExtra(IDENTIFY_DIFFERENT, false);
//			String identify_different = it.getStringExtra(IDENTIFY_DIFFERENT).toString();
			/*if ("user_regist".equals(identify_different)) {
				// TODO 注册逻辑
			} else if ("user_repwd".equals(identify_different)) {
				// TODO 重置密码逻辑
			}*/

		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.user_identify_layout_getidentify_btn:
			getIdentify(justDifferent,title_phone);
			break;
		case R.id.user_identify_btn:

			break;
		default:
			break;
		}
	}

	/**
	 * 重新获取验证码
	 * 
	 * @param phone
	 */
	public void getIdentify(boolean flag,String phone) {
		mApp.mIdentifyManage.setUserIdentifyInterface(this);
		boolean b = mApp.mIdentifyManage.getIdentify(justDifferent,phone);
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

	/**
	 * 获取验证码
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
			GolukUtils.showToast(this, "验证码发送成功");
			//倒计时
			countTime();
			
			break;
		// 获取验证码失败
		case 2:
			closeDialogIdentify();
			GolukUtils.showToast(mContext, "验证码获取失败");
			break;
		// code=201
		case 3:
			closeDialogIdentify();
			UserUtils.showDialog(this, "该手机号1小时内下发6次以上验证码");
			break;
		// code=500
		case 4:
			closeDialogIdentify();
			UserUtils.showDialog(this, "服务端程序异常");
			break;
		// code=405
		case 5:
			closeDialogIdentify();
			new AlertDialog.Builder(this).setTitle("提示").setMessage("此手机号已经被注册").setNegativeButton("取消", null)
					.setPositiveButton("立即登录", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (mApp.loginoutStatus = true) {
								mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
								mEditor = mSharedPreferences.edit();
								mEditor.putString("setupPhone", title_phone.replace("-", ""));
								mEditor.putBoolean("noPwd", true);
								mEditor.commit();
							}
							finish();
						}
					}).create().show();
			break;
		// code=440
		case 6:
			closeDialogIdentify();
			UserUtils.showDialog(this, "输入手机号异常");
			break;
		// code=480
		case 7:
			closeDialogIdentify();
			UserUtils.showDialog(this, "验证码发送失败，请重新发送");
			break;
		// code=470
		case 8:
			closeDialogIdentify();
			UserUtils.showDialog(mContext, "获取验证码失败,此手机号已经达到获取验证码上限");
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
					mBtnNext.setBackgroundResource(R.drawable.icon_login);
					mBtnNext.setEnabled(true);
				}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_DEL) {
			if (mEditTextSix.isFocused()) {
				mEditTextFive.setFocusableInTouchMode(true);
				if (mEditTextSix.getText().toString().length() == 1) {
					mEditTextSix.getText().clear();
					mEditTextSix.requestFocus();
					return true;
				} else {
					mEditTextSix.clearFocus();
					mEditTextFive.getText().clear();
					mEditTextSix.setFocusable(false);
					mEditTextFive.requestFocus();
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
			this.finish();
		}
		return false;

	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.user_identify_btn:
			String identify_one = mEditTextOne.getText().toString();
			String identify_two = mEditTextTwo.getText().toString();
			String identify_three = mEditTextThree.getText().toString();
			String identify_four = mEditTextFour.getText().toString();
			String identify_five = mEditTextFive.getText().toString();
			String identify_six = mEditTextSix.getText().toString();
			if (!"".equals(identify_one) && !"".equals(identify_two) && !"".equals(identify_three)
					&& !"".equals(identify_four) && !"".equals(identify_five) && !"".equals(identify_six)) {
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
			break;
		}
		return false;
	}

	private void closeDialogIdentify() {
		if (null != mCustomDialogIdentify) {
			mCustomDialogIdentify.close();
			mBtnBack.setEnabled(true);
			mEditTextOne.setEnabled(true);
			mBtnCount.setEnabled(true);
			mBtnNext.setEnabled(true);
		}
	}
	
	public void countTime(){
		mCountDownhelper = new CountDownButtonHelper(mBtnCount, "重新获取", 60, 1);
		mCountDownhelper.setOnFinishListener(new OnFinishListener() {

			@Override
			public void finish() {
				mBtnCount.setText("重新获取");
				// 倒计时结束后手机号、密码可以更改
				mBtnCount.setEnabled(true);
			}
		});
		mCountDownhelper.start();
	}

}
