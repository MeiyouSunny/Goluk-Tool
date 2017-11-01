package com.mobnote.golukmain.internation.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventLoginSuccess;
import com.mobnote.eventbus.EventRegister;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.bean.EmailVcodeRetBean;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;

import org.json.JSONObject;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import cn.smssdk.SMSSDK;
import de.greenrobot.event.EventBus;

/**
 * 重置密码
 * <p>
 * 1、输入手机号、密码 2、验证码的获取和判断 3、短信验证
 *
 * @author mobnote
 */
public class InternationalResetPwdActivity extends BaseActivity implements OnClickListener, IRequestResultListener {

    /**
     * 找回密码
     */
    public static final int FIND_REQUESTCODE_SELECTCTROY = 20;
    /**
     * title
     **/
    private ImageButton mBtnBack;
    private TextView mTextViewTitle;
    /**
     * 手机号、密码、验证码
     **/
    private EditText mEditTextPhone;
    private Button mBtnOK;

    private Context mContext = null;
    private GolukApplication mApplication = null;
    /**
     * 重置密码显示进度条
     **/
    private CustomLoadingDialog mCustomProgressDialog = null;
    /**
     * 验证码获取显示进度条
     **/
    private CustomLoadingDialog mCustomProgressDialogIdentify = null;

    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;
    /**
     * 重置密码跳转标志
     **/
    private String repwdOk = null;
    private TextView zoneTv = null;

    private EditText mEmailEt;
    private TextView mPhoneTab;
    private TextView mEmailTab;
    private View mPhoneTabIndicator;
    private View mEmailTabIndicator;
    private LinearLayout mPhoneLL;

    private boolean mIsPhoneSelected;
    private boolean mIsPhoneEmpty;
    private boolean mIsEmailEmpty;

    private boolean isAcceptMsgcode = true;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.internation_user_repwd);

        initData();
        initView();

        mTextViewTitle.setText(this.getResources().getString(R.string.user_login_forgetpwd));
        UserUtils.addActivity(InternationalResetPwdActivity.this);
        EventBus.getDefault().register(this);
    }

    private void initData() {
        mContext = this;
        mApplication = (GolukApplication) getApplication();
        mIsPhoneEmpty = true;
        mIsEmailEmpty = true;
        mIsPhoneSelected = getIntent().getBooleanExtra("isPhoneSelected", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAcceptMsgcode = true;
        mApplication.setContext(mContext, "UserRepwd");
        getInfo();
    }

    public void initView() {

        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_repwd_loading));
        }
        if (null == mCustomProgressDialogIdentify) {
            mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_identify_loading));
        }

        mBtnBack = (ImageButton) findViewById(R.id.back_btn);
        mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
        mEditTextPhone = (EditText) findViewById(R.id.user_repwd_phonenumber);
        mBtnOK = (Button) findViewById(R.id.user_repwd_ok_btn);
        zoneTv = (TextView) findViewById(R.id.repwd_zone);
        mEmailEt = (EditText) findViewById(R.id.et_email);
        mPhoneTab = (TextView) findViewById(R.id.tab_phone);
        mEmailTab = (TextView) findViewById(R.id.tab_email);
        mPhoneTabIndicator = findViewById(R.id.tab_phone_indicator);
        mEmailTabIndicator = findViewById(R.id.tab_email_indicator);
        mPhoneLL = (LinearLayout) findViewById(R.id.ll_phone);
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

        // 绑定监听
        mBtnBack.setOnClickListener(this);
        mBtnOK.setOnClickListener(this);
        zoneTv.setOnClickListener(this);
        mPhoneTab.setOnClickListener(this);
        mEmailTab.setOnClickListener(this);

        mEditTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String phone = mEditTextPhone.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    mIsPhoneEmpty = false;
                } else {
                    mIsPhoneEmpty = true;
                }
                resetBtnState();
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
                mEmail = mEmailEt.getText().toString();
                if (!TextUtils.isEmpty(mEmail)) {
                    mIsEmailEmpty = false;
                } else {
                    mIsEmailEmpty = true;
                }
                resetBtnState();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    private void resetBtnState() {
        if (mIsPhoneSelected && !mIsPhoneEmpty) {
            mBtnOK.setTextColor(Color.parseColor("#000000"));
            mBtnOK.setEnabled(true);
        } else if (!mIsPhoneSelected && !mIsEmailEmpty) {
            mBtnOK.setTextColor(Color.parseColor("#000000"));
            mBtnOK.setEnabled(true);
        } else {
            mBtnOK.setTextColor(Color.parseColor("#60000000"));
            mBtnOK.setEnabled(false);
        }
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
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_btn) {
            finish();
        } else if (view.getId() == R.id.repwd_zone) {
            Intent intent = new Intent(this, UserSelectCountryActivity.class);
            startActivityForResult(intent, FIND_REQUESTCODE_SELECTCTROY);
        } else if (view.getId() == R.id.user_repwd_ok_btn) {
            repwd();
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
        mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_lighter));
        mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_darker));
        mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_lighter));
        mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_darker));
        mPhoneLL.setVisibility(View.VISIBLE);
        mEmailEt.setVisibility(View.GONE);
        resetBtnState();
    }

    private void emailTabSelected() {
        mIsPhoneSelected = false;
        mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_darker));
        mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_lighter));
        mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_darker));
        mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_lighter));
        mPhoneLL.setVisibility(View.GONE);
        mEmailEt.setVisibility(View.VISIBLE);
        resetBtnState();
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

        if (!UserUtils.isNetDeviceAvailable(this)) {
            UserUtils.hideSoftMethod(this);
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
            return;
        }

        if(mIsPhoneSelected) {
            requestPhoneVcode();
        } else {
            requestEmailVcode();
        }
    }

    private void requestEmailVcode() {
        mEmail = mEmailEt.getText().toString();
        if (TextUtils.isEmpty(mEmail) || !UserUtils.emailValidation(mEmail)) {
            showToast(R.string.email_invalid);
            return;
        }
        UserUtils.hideSoftMethod(this);
        mCustomProgressDialogIdentify.show();
        mBtnOK.setEnabled(false);
        mEditTextPhone.setEnabled(false);
        mBtnBack.setEnabled(false);

        new EmailVcodeRequest(IPageNotifyFn.SEND_EMAIL_VCODE, this).send(mEmail,"2");
    }

    private void requestPhoneVcode() {
        String phone = mEditTextPhone.getText().toString();
        String zone = zoneTv.getText().toString();
        if (TextUtils.isEmpty(zone)) {
            return;
        }
        if (!"".equals(phone)) {
            mBtnOK.setFocusable(true);
            if (!mApplication.mTimerManage.flag) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_timer_count_hint));
            } else {
                mApplication.mTimerManage.timerCancel();
                int zoneCode = zone.indexOf("+");
                String code = zone.substring(zoneCode + 1, zone.length());
                GolukMobUtils.sendSms(code, phone);
                UserUtils.hideSoftMethod(this);
                mCustomProgressDialogIdentify.show();
                mBtnOK.setEnabled(false);
                mEditTextPhone.setEnabled(false);
                mBtnBack.setEnabled(false);

            }
        }
    }

    private void callBack_getCode_Success() {
        isAcceptMsgcode = false;
        closeProgressDialogIdentify();
        GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_success));
        String phone = mEditTextPhone.getText().toString();
        String zone = zoneTv.getText().toString();

        Intent getIdentify = new Intent(InternationalResetPwdActivity.this, InternationUserIdentifyActivity.class);
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

    public void putPhones() {
        String phone = mEditTextPhone.getText().toString();
        mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        GolukDebugUtils.i("lily", "phone==" + phone);
        mEditor.putString("setupPhone", phone);
        mEditor.putBoolean("noPwd", false);
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

    @Override
    public void onLoadComplete(int requestType, Object result) {
        closeProgressDialogIdentify();
        if (IPageNotifyFn.SEND_EMAIL_VCODE == requestType) {
            EmailVcodeRetBean retBean = (EmailVcodeRetBean)result;
            if (retBean == null) {
                return;
            }
            if(retBean.code != 0) {
                Toast.makeText(this, retBean.msg, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(InternationalResetPwdActivity.this, InternationUserIdentifyActivity.class);
            intent.putExtra(InternationUserIdentifyActivity.KEY_EMAIL_ADDRESS,mEmail);
            intent.putExtra(InternationUserIdentifyActivity.IDENTIFY_INTER_REGIST, repwdOk);
            startActivity(intent);
        }
    }
}
