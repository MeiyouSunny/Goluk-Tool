package com.mobnote.golukmain.internation.login;

import org.json.JSONObject;

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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.tiros.debug.GolukDebugUtils;
import cn.smssdk.SMSSDK;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventLoginSuccess;
import com.mobnote.eventbus.EventRegister;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;

import org.json.JSONObject;

import cn.com.tiros.debug.GolukDebugUtils;
import cn.smssdk.SMSSDK;
import de.greenrobot.event.EventBus;

/**
 * 注册
 * <p>
 * 1、注册手机号密码 2、获取验证码 3、登陆
 *
 * @author mobnote
 */
public class InternationUserRegistActivity extends BaseActivity implements OnClickListener, OnTouchListener {

    /**
     * 选择国家界面
     */
    public static final int REG_REQUESTCODE_SELECTCTROY = 100;
    /**
     * 免责申明条款
     **/
    public static final String PRIVACY_POLICY_WEB_URL = "http://www.goluk.cn/legal.html";
    /**
     * 手机号、密码、注册按钮
     **/
    private EditText mEditTextPhone;
    private EditText mEmailEt;
    private TextView mPhoneTab;
    private TextView mEmailTab;
    private View mPhoneTabIndicator;
    private View mEmailTabIndicator;
    private LinearLayout mPhoneLL;

    private boolean mIsPhoneSelected;
    private boolean mIsPhoneEmpty;
    private boolean mIsEmailEmpty;

    private Button mBtnRegist;
    private Context mContext = null;
    private GolukApplication mApplication = null;
    /**
     * 注册
     **/
    private CustomLoadingDialog mCustomProgressDialog = null;
    /**
     * 获取验证码
     **/
    private CustomLoadingDialog mCustomProgressDialogIdentify = null;
    /**
     * 记录注册成功的状态
     **/
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;
    /**
     * 注册成功跳转页面的判断标志
     */
    private String registOk = null;
    private TextView zoneTv = null;

    private boolean isAcceptMsgcode = true;

    private ImageView mCloseBtn;

    private TextView mLoginBtn;

    private TextView mRegistPrivacy;
    private TextView mRegistPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.internation_user_regist);
        initData();
        initView();
        UserUtils.addActivity(InternationUserRegistActivity.this);
        EventBus.getDefault().register(this);
    }

    private void initData() {
        mContext = this;
        mIsPhoneEmpty = true;
        mIsEmailEmpty = true;
        mIsPhoneSelected = true;
        mIsPhoneSelected = getIntent().getBooleanExtra("isPhoneSelected", false);
        mApplication = (GolukApplication) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAcceptMsgcode = true;
        mApplication.setContext(mContext, "UserRegist");
        getInfo();
    }

    public void initView() {
        // 手机号、密码、注册按钮
        mEditTextPhone = (EditText) findViewById(R.id.user_regist_phonenumber);
        mEmailEt = (EditText) findViewById(R.id.et_email);
        mPhoneTab = (TextView) findViewById(R.id.tab_phone);
        mEmailTab = (TextView) findViewById(R.id.tab_email);
        mPhoneTabIndicator = findViewById(R.id.tab_phone_indicator);
        mEmailTabIndicator = findViewById(R.id.tab_email_indicator);
        mPhoneLL = (LinearLayout) findViewById(R.id.ll_phone);

        mBtnRegist = (Button) findViewById(R.id.user_regist_btn);
        zoneTv = (TextView) findViewById(R.id.user_regist_zone);
        mCloseBtn = (ImageView) findViewById(R.id.close_btn);
        mLoginBtn = (TextView) findViewById(R.id.login_user_btn);
        mRegistPrivacy = (TextView) findViewById(R.id.regist_privacy);
        mRegistPolicy = (TextView) findViewById(R.id.regist_policy);

        if (mBaseApp.mLocationCityCode != null) {
            zoneTv.setText(mBaseApp.mLocationCityCode.area + "+" + mBaseApp.mLocationCityCode.code);
        } else {
            zoneTv.setText(GolukUtils.getDefaultZone());
        }

        if (mIsPhoneSelected) {
            phoneTabSelected();
        } else {
            emailTabSelected();
        }
        // 监听绑定
        mBtnRegist.setOnClickListener(this);
        zoneTv.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);
        mCloseBtn.setOnClickListener(this);
        mRegistPrivacy.setOnClickListener(this);
        mRegistPolicy.setOnClickListener(this);
        mPhoneTab.setOnClickListener(this);
        mEmailTab.setOnClickListener(this);

        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_regist_loading));
        }
        if (null == mCustomProgressDialogIdentify) {
            mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_identify_loading));
        }

        mEditTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String phone = mEditTextPhone.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    mIsPhoneEmpty = false;
                } else {
                    mIsPhoneEmpty = true;
                }
                resetRegisterBtnState();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        mEmailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String email = mEmailEt.getText().toString();
                if (!TextUtils.isEmpty(email)) {
                    mIsEmailEmpty = false;
                } else {
                    mIsEmailEmpty = true;
                }
                resetRegisterBtnState();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    /**
     * 手机号码获取
     */
    private void getInfo() {
        Intent itLoginPhone = getIntent();
        String email = itLoginPhone.getStringExtra("intentLoginEmail");
        String phone = itLoginPhone.getStringExtra("intentLogin");
        if (mIsPhoneSelected && !TextUtils.isEmpty(phone)) {
            mEditTextPhone.setText(phone);
            mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
        }
        if (!mIsPhoneSelected && !TextUtils.isEmpty(email)) {
            mEmailEt.setText(email);
        }
        Intent itRepassword = getIntent();
        if (null != itRepassword.getStringExtra("intentRepassword")) {
            String repwdNum = itRepassword.getStringExtra("intentRepassword");
            mEditTextPhone.setText(repwdNum);
            mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
        }

        /**
         * 判断是从哪个入口进行的注册
         */
        Intent itRegist = getIntent();
        if (null != itRegist.getStringExtra("fromRegist")) {
            registOk = itRegist.getStringExtra("fromRegist");
        }
        GolukDebugUtils.i("final", "--------UserRegistActivty-------registOk----" + registOk);

        getPhone();
    }

    private void resetRegisterBtnState() {
        if (mIsPhoneSelected && !mIsPhoneEmpty) {
            mBtnRegist.setTextColor(Color.parseColor("#FFFFFF"));
            mBtnRegist.setEnabled(true);
        } else if (!mIsPhoneSelected && !mIsEmailEmpty) {
            mBtnRegist.setTextColor(Color.parseColor("#FFFFFF"));
            mBtnRegist.setEnabled(true);
        } else {
            mBtnRegist.setTextColor(Color.parseColor("#7fffffff"));
            mBtnRegist.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) { // 数据发送成功
            if (REG_REQUESTCODE_SELECTCTROY == requestCode) {
                CountryBean bean = (CountryBean) data.getSerializableExtra(InternationUserLoginActivity.COUNTRY_BEAN);
                zoneTv.setText(bean.area + " +" + bean.code);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_btn) {
            finish();
        } else if (view.getId() == R.id.user_regist_zone) {
            Intent intent = new Intent(this, UserSelectCountryActivity.class);
            startActivityForResult(intent, REG_REQUESTCODE_SELECTCTROY);
        } else if (view.getId() == R.id.user_regist_btn) {
            register();
        } else if (view.getId() == R.id.login_user_btn || view.getId() == R.id.close_btn) {
            this.finish();
        } else if (view.getId() == R.id.regist_policy || view.getId() == R.id.regist_privacy) {
            Intent privacy = new Intent(this, UserOpenUrlActivity.class);
            privacy.putExtra("url", PRIVACY_POLICY_WEB_URL);
            mContext.startActivity(privacy);
        } else if(view.getId() == R.id.tab_email) {
            if (!mIsPhoneSelected) {
                return;
            }
            emailTabSelected();

        } else if(view.getId() == R.id.tab_phone) {
            if (mIsPhoneSelected) {
                return;
            }
            phoneTabSelected();
        }
    }

    private void phoneTabSelected() {
        mIsPhoneSelected = true;
        mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_grey));
        mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_white));
        mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_grey));
        mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_white));
        mPhoneLL.setVisibility(View.VISIBLE);
        mEmailEt.setVisibility(View.GONE);
        resetRegisterBtnState();
    }

    private void emailTabSelected() {
        mIsPhoneSelected = false;
        mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_white));
        mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_grey));
        mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_white));
        mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_grey));
        mPhoneLL.setVisibility(View.GONE);
        mEmailEt.setVisibility(View.VISIBLE);
        resetRegisterBtnState();
    }

    private void registerByPhone() {
        String zone = zoneTv.getText().toString();
        String phone = mEditTextPhone.getText().toString();
        if (TextUtils.isEmpty(zone)) {
            return;
        }

        if (!"".equals(phone)) {
            mBtnRegist.setEnabled(true);
            if (!UserUtils.isNetDeviceAvailable(mContext)) {
                GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
            } else {
                if (!mApplication.mTimerManage.flag) {
                    GolukUtils.showToast(this, this.getResources().getString(R.string.user_timer_count_hint));
                } else {
                    mApplication.mTimerManage.timerCancel();
                    // 获取验证码
                    int zoneCode = zone.indexOf("+");
                    String code = zone.substring(zoneCode + 1, zone.length());
                    GolukMobUtils.sendSms(code, phone);
                    UserUtils.hideSoftMethod(this);
                    mCustomProgressDialogIdentify.show();
                    mBtnRegist.setEnabled(false);
                    mEditTextPhone.setEnabled(false);
                }

            }
        } else {
            UserUtils.hideSoftMethod(this);
            UserUtils.showDialog(mContext, this.getResources().getString(R.string.user_login_phone_show_error));
        }
    }

    private void registerByEmail() {
        String emailAddress = mEmailEt.getText().toString();
        if (TextUtils.isEmpty(emailAddress) || !UserUtils.emailValidation(emailAddress)) {
            showToast(R.string.email_invalid);
            return;
        }
        if (!UserUtils.isNetDeviceAvailable(mContext)) {
            GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, InternationUserPwdActivity.class);
        intent.putExtra("email",emailAddress);
        intent.putExtra("from",registOk);
        startActivity(intent);
    }

    /**
     * 注册
     */
    public void register() {

        if(mIsPhoneSelected) {
            registerByPhone();
        } else {
            registerByEmail();
        }

    }

    public void onEventMainThread(EventLoginSuccess event) {
        finish();
    }

    public void onEventMainThread(EventRegister event) {
        if (null == event) {
            return;
        }
        if (!isAcceptMsgcode) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.EVENT_REGISTER_CODE:
                if (SMSSDK.RESULT_COMPLETE == event.getmResult()) {
                    if (SMSSDK.EVENT_GET_VERIFICATION_CODE == event.getmEvent()) {
                        // 获取验证码成功
                        isAcceptMsgcode = false;
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

    // 获取验证码成功
    private void callBack_getCode_Success() {
        closeProgressDialogIdentify();
        GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_success));

        String phone = mEditTextPhone.getText().toString();
        String zone = zoneTv.getText().toString();

        Intent getIdentify = new Intent(InternationUserRegistActivity.this, InternationUserIdentifyActivity.class);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_DIFFERENT, true);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_PHONE, phone);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_INTER_REGIST, registOk);

        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_REGISTER_CODE, zone);
        GolukDebugUtils.i("final", "------UserRegistActivity-------identifyCallbackInterface-------registOk------"
                + registOk);
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
        if (view.getId() == R.id.user_regist_btn) {
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
        }
    }

    @Override
    protected void onDestroy() {
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
