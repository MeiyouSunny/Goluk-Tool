package com.mobnote.golukmain.internation.login;

import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventRegister;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;

import cn.com.tiros.debug.GolukDebugUtils;
import cn.smssdk.SMSSDK;
import de.greenrobot.event.EventBus;

/**
 * 重置密码
 * 
 * 1、输入手机号、密码 2、验证码的获取和判断 3、短信验证
 * 
 * @author mobnote
 *
 */
public class InternationUserRepwdActivity extends BaseActivity implements OnClickListener, OnTouchListener {

	/** 找回密码 */
	public static final int FIND_REQUESTCODE_SELECTCTROY = 20;
	/** title **/
	private ImageButton mBtnBack;
	private TextView mTextViewTitle;
	/** 手机号、密码、验证码 **/
	private EditText mEditTextPhone;
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
	private TextView zoneTv = null;

	private boolean isAcceptMsgcode = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.internation_user_repwd);
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
		mTextViewTitle.setText(this.getResources().getString(R.string.user_login_forgetpwd));
		UserUtils.addActivity(InternationUserRepwdActivity.this);

		EventBus.getDefault().register(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isAcceptMsgcode = true;
		mApplication.setContext(mContext, "UserRepwd");
		getInfo();
	}

	public void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
		mEditTextPhone = (EditText) findViewById(R.id.user_repwd_phonenumber);
		mBtnOK = (Button) findViewById(R.id.user_repwd_ok_btn);
		zoneTv = (TextView) findViewById(R.id.repwd_zone);
		if(mBaseApp.mLocationCityCode!=null){
			zoneTv.setText(mBaseApp.mLocationCityCode.area + "+" + mBaseApp.mLocationCityCode.code);
		}else{
			zoneTv.setText(GolukUtils.getDefaultZone());
		}
		// 绑定监听
		mBtnBack.setOnClickListener(this);
		mBtnOK.setOnClickListener(this);
		//mBtnOK.setOnTouchListener(this);
		zoneTv.setOnClickListener(this);
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
			String phone = it.getStringExtra("errorPwdOver");
			mEditTextPhone.setText(phone);
			mBtnOK.setTextColor(Color.parseColor("#000000"));
			mBtnOK.setEnabled(true);
			mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
		}

		/**
		 * 判断是从哪个入口进行的注册
		 */
		Intent itRepwd = getIntent();
		if (null != itRepwd.getStringExtra("fromRegist")) {
			repwdOk = itRepwd.getStringExtra("fromRegist");
		}
		GolukDebugUtils.i("final", "--------UserRegistActivty-------registOk----" + repwdOk);

		putPhones();

		mEditTextPhone.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String phone = mEditTextPhone.getText().toString();
				// 重置按钮
				if (!"".equals(phone) && UserUtils.isNumber(phone)) {
					mBtnOK.setTextColor(Color.parseColor("#000000"));
					mBtnOK.setEnabled(true);
				} else {
					mBtnOK.setTextColor(Color.parseColor("#33000000"));
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
	public void onClick(View view) {
		if(view.getId() == R.id.back_btn){
			finish();
		}else if(view.getId() == R.id.repwd_zone){
			Intent intent = new Intent(this, UserSelectCountryActivity.class);
			startActivityForResult(intent, FIND_REQUESTCODE_SELECTCTROY);
		}else if(view.getId() == R.id.user_repwd_ok_btn){
			repwd();
		}
	}

	public void onEventMainThread(EventRegister event) {
		if (null == event) {
			return;
		}
		if (!isAcceptMsgcode) {
			return;
		}

		GolukDebugUtils.i("final", "------UserRepwdActivity--------------onEventMainThread------" + event.getmEvent());

		switch (event.getOpCode()) {
		case EventConfig.EVENT_REGISTER_CODE:
			if (SMSSDK.RESULT_COMPLETE == event.getmResult()) {
				if (SMSSDK.EVENT_GET_VERIFICATION_CODE == event.getmEvent()) {
					// 获取验证码成功
					callBack_getCode_Success();
				}
			} else {
				if (SMSSDK.EVENT_GET_VERIFICATION_CODE == event.getmEvent()) {
					// 获取验证码失败
					callBack_getCode_Failed(event.getmData());
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 重置密码
	 */
	public void repwd() {
		String phone = mEditTextPhone.getText().toString();
		String zone = zoneTv.getText().toString();
		if (TextUtils.isEmpty(zone)) {
			return;
		}
		if (!"".equals(phone)) {
			mBtnOK.setFocusable(true);
			if (!UserUtils.isNetDeviceAvailable(this)) {
				UserUtils.hideSoftMethod(this);
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
			} else {
				if (!mApplication.mTimerManage.flag) {
					GolukUtils.showToast(this, this.getResources().getString(R.string.user_timer_count_hint));
				} else {
					mApplication.mTimerManage.timerCancel();
					int zoneCode = zone.indexOf("+");
					String code = zone.substring(zoneCode+1, zone.length());
					GolukMobUtils.sendSms(code, phone);
					UserUtils.hideSoftMethod(this);
					mCustomProgressDialogIdentify.show();
					mBtnOK.setEnabled(false);
					mEditTextPhone.setEnabled(false);
					mBtnBack.setEnabled(false);

				}
			}
		}
	}

	private void callBack_getCode_Success() {
		isAcceptMsgcode = false;
		closeProgressDialogIdentify();
		GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_success));
		String phone = mEditTextPhone.getText().toString();
		String zone = zoneTv.getText().toString();

		Intent getIdentify = new Intent(InternationUserRepwdActivity.this, InternationUserIdentifyActivity.class);
		getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_DIFFERENT, false);
		getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_PHONE, phone);
		getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_INTER_REGIST, repwdOk);

		getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_REGISTER_CODE, zone);
		GolukDebugUtils.i("final", "------UserRepwdActivity--------------registOk------" + repwdOk);
		startActivity(getIdentify);
	}

	private void callBack_getCode_Failed(Object data) {
		closeProgressDialogIdentify();
		try {
			Throwable throwable = (Throwable) data;
			JSONObject obj = new JSONObject(throwable.getMessage());
			final String des = obj.optString("detail");
			int status = obj.optInt("status");
			if (!TextUtils.isEmpty(des)) {
				GolukUtils.showToast(this, des);
			} else {
				GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_getidentify_fail));
			}
		} catch (Exception e) {
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_getidentify_fail));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (RESULT_OK == resultCode) { // 数据发送成功
			if (FIND_REQUESTCODE_SELECTCTROY == requestCode) {
				CountryBean bean = (CountryBean) data.getSerializableExtra(InternationUserLoginActivity.COUNTRY_BEAN);
				zoneTv.setText(bean.area + " +" + bean.code);
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		if(view.getId() == R.id.user_repwd_ok_btn){
			String phoneNumber = mEditTextPhone.getText().toString();
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
			mBtnBack.setEnabled(true);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
