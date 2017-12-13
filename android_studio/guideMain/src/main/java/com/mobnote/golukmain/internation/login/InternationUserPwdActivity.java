package com.mobnote.golukmain.internation.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventLoginSuccess;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserSetupActivity;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.bean.RegistBean;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.golukmain.userlogin.UserResult;
import com.mobnote.golukmain.userlogin.UserloginBeanRequest;
import com.mobnote.user.UserLoginInterface;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.umeng.socialize.sina.helper.MD5;

import org.json.JSONObject;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 国际版注册时设置密码页面
 */
public class InternationUserPwdActivity extends Activity implements View.OnClickListener, UserLoginInterface,
        IRequestResultListener {

    private ImageButton mImageBtnBack = null;
    private EditText mPwdEditText = null;
    private Button mNextBtn = null;
    private GolukApplication mApp = null;
    /**
     * 从上个页面传来的phone
     **/
    private String mPhone = "";
    private String mEmail = "";
    /**
     * 从上个页面传来的vcode
     **/
    private String mVcode = "";
    /**
     * 从上个页面传来的zone
     **/
    private String mZone = "";
    private String mFrom = "";
    /**
     * 2次验证码
     **/
    private String mStep2code = "";
    private CustomLoadingDialog mLoadingDialog = null;
    private SharedPreferences mSharedPreferences = null;
    private SharedPreferences.Editor mEditor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_internation_user_pwd);

        mApp = GolukApplication.getInstance();

        getDataInfo();

        initView();

    }

    private void getDataInfo() {
        Intent it = getIntent();
        mPhone = it.getStringExtra("phone");
        mEmail = it.getStringExtra("email");
        mVcode = it.getStringExtra("vcode");
        mZone = it.getStringExtra("zone");
        mFrom = it.getStringExtra("from");
        mStep2code = it.getStringExtra("step2code");
    }

    private void initView() {
        mImageBtnBack = (ImageButton) findViewById(R.id.ib_internation_pwd_back);
        mPwdEditText = (EditText) findViewById(R.id.et_internation_pwd);
        mNextBtn = (Button) findViewById(R.id.btn_interantion_pwd_next);
        mNextBtn.setEnabled(false);

        mImageBtnBack.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);

        if (null == mLoadingDialog) {
            mLoadingDialog = new CustomLoadingDialog(this, this.getResources().getString(
                    R.string.str_regist_loading));
        }
        if (!TextUtils.isEmpty(mEmail)) {
            mNextBtn.setText(getString(R.string.user_regist));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPwdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = mPwdEditText.getText().toString();
                if (!"".equals(password.trim()) && password.length() >= 6) {
                    mNextBtn.setTextColor(Color.parseColor("#FFFFFF"));
                    mNextBtn.setEnabled(true);
                } else {
                    mNextBtn.setTextColor(Color.parseColor("#7fffffff"));
                    mNextBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ib_internation_pwd_back) {
            finish();
        } else if (id == R.id.btn_interantion_pwd_next) {
            clickToRegist();
        }
    }

    private void clickToRegist() {
        final String pwd = mPwdEditText.getText().toString();
        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
        } else {
            if (null != pwd && pwd.length() >= 6 && pwd.length() <= 16) {
                if (!TextUtils.isEmpty(mPhone)) {
                    registerByPhone(pwd);
                } else {
                    registerByEmail(pwd);
                }
            } else {
                UserUtils.showDialog(this,
                        this.getResources().getString(R.string.user_login_password_show_error));
            }
        }
    }

    private void registerByPhone(String pwd) {
        InternationalPhoneRegisterRequest request = new InternationalPhoneRegisterRequest(IPageNotifyFn.PageType_InternationalRegister, this);
        boolean b = request.get(mPhone, MD5.hexdigest(pwd), mVcode, mZone, mStep2code);
        if (b) {
            mLoadingDialog.show();
            mNextBtn.setEnabled(false);
        } else {
            closeLoadingDialog();
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_fail));
        }
    }

    private void registerByEmail (String pwd){
        InternationalEmailRegisterRequest registerRequest = new InternationalEmailRegisterRequest(IPageNotifyFn.REGISTER_BY_EMAIL, this);
        boolean b = registerRequest.get(mEmail, MD5.hexdigest(pwd));
        if (b) {
            mLoadingDialog.show();
            mNextBtn.setEnabled(false);
        } else {
            closeLoadingDialog();
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_fail));
        }
    }

    private void closeLoadingDialog() {
        if (null != mLoadingDialog) {
            mLoadingDialog.close();
            mNextBtn.setEnabled(true);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_InternationalRegister) {
            closeLoadingDialog();
            RegistBean bean = (RegistBean) result;
            if (null == bean) {
                UserUtils.showDialog(this, this.getResources().getString(R.string.user_getidentify_fail));
                return;
            }
            int code = bean.code;
            if (code == 0) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_success));
                final String pwd = mPwdEditText.getText().toString();
                mApp.mLoginManage.setUserLoginInterface(this);

                UserloginBeanRequest userloginBean = new UserloginBeanRequest(IPageNotifyFn.PageType_Login, this);
                userloginBean.loginByPhone(mPhone, MD5.hexdigest(pwd), "");

                mApp.loginStatus = 0;// 登录中
            } else if (code == 20100) {
                UserUtils.showDialog(this, this.getResources().getString(R.string.user_already_regist));
            } else if (code == 22001) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_fail));
            } else {
                GolukUtils.showToast(this, bean.msg);
            }
        } else if (requestType == IPageNotifyFn.REGISTER_BY_EMAIL) {
            closeLoadingDialog();
            RegistBean bean = (RegistBean) result;
            if (null == bean) {
                UserUtils.showDialog(this, this.getResources().getString(R.string.user_getidentify_fail));
                return;
            }
            if (bean.code != 0) {
                GolukUtils.showToast(this, bean.msg);
                return;
            }
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_success));
            final String pwd = mPwdEditText.getText().toString();
            mApp.mLoginManage.setUserLoginInterface(this);

            UserloginBeanRequest userloginBean = new UserloginBeanRequest(IPageNotifyFn.PageType_Login, this);
            userloginBean.loginByEmail(mEmail, MD5.hexdigest(pwd), "");
            mApp.loginStatus = 0;// 登录中
        } else if (requestType == IPageNotifyFn.PageType_Login) {
            try {
                GolukDebugUtils.i("lily", "-----UserLoginManage-----" + result);
                UserResult userresult = (UserResult) result;
                int code = Integer.parseInt(userresult.code);
                switch (code) {
                    case 200:
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

                        if (!"".equals(userresult.data.phone)) {
                            json.put("phone", userresult.data.phone);
                        }
                        if (!"".equals(mPwdEditText.getText().toString())) {
                            json.put("pwd", mPwdEditText.getText().toString());
                        }
                        json.put("uid", userresult.data.uid);
                        SharedPrefUtil.saveUserPwd(json.toString());

                        GolukApplication.getInstance().parseLoginData(userresult.data);
                        EventBus.getDefault().post(new EventLoginSuccess());
                        EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_REQUEST));
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

    @Override
    public void loginCallbackStatus() {
        mPwdEditText.setEnabled(true);
        mNextBtn.setEnabled(true);
        switch (mApp.loginStatus) {
            case 0:
                break;
            case 1:
                // 登录成功后关闭个人中心启动模块页面
                mApp.isUserLoginSucess = true;
                mApp.mUser.timerCancel();
                mApp.autoLoginStatus = 2;
                Intent it = new Intent();
                if ("fromStart".equals(mFrom)) {
                    it = new Intent(InternationUserPwdActivity.this, MainActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(it);
                } else if ("fromIndexMore".equals(mFrom)) {
                    it = new Intent(InternationUserPwdActivity.this, MainActivity.class);
                    it.putExtra("showMe", "showMe");
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(it);
                } else if ("fromSetup".equals(mFrom)) {
                    it = new Intent(InternationUserPwdActivity.this, UserSetupActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(it);
                } else if ("fromProfit".equals(mFrom)) {
                    it = new Intent(InternationUserPwdActivity.this, MyProfitActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(it);
                    UserUtils.exit();
                }
                EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_REQUEST));
                this.finish();
                break;
            case 2:
                mApp.isUserLoginSucess = false;
                break;
            case 3:
                mApp.isUserLoginSucess = false;
                new AlertDialog.Builder(this)
                        .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                        .setMessage(this.getResources().getString(R.string.user_no_regist))
                        .setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
                        .setPositiveButton(this.getResources().getString(R.string.user_regist),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mApp.mLoginManage.setUserLoginInterface(null);
                                        Intent it = new Intent(InternationUserPwdActivity.this, InternationUserRegistActivity.class);
                                        it.putExtra("intentLogin", mPhone);
                                        if (mFrom.equals("main") || mFrom.equals("back")) {// 从起始页注册
                                            it.putExtra("fromRegist", "fromStart");
                                        } else if (mFrom.equals("indexmore")) {// 从更多页个人中心注册
                                            it.putExtra("fromRegist", "fromIndexMore");
                                        } else if (mFrom.equals("setup")) {// 从设置页注册
                                            it.putExtra("fromRegist", "fromSetup");
                                        } else if (mFrom.equals("profit")) {// 从我的收益注册
                                            it.putExtra("fromRegist", "fromProfit");
                                        }

                                        startActivity(it);
                                    }
                                }).create().show();
                break;
            case 4:
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_netword_outtime));
                mApp.isUserLoginSucess = false;
                break;
            case 5:
                mApp.isUserLoginSucess = false;
                new AlertDialog.Builder(this)
                        .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                        .setMessage(this.getResources().getString(R.string.user_login_password_limit_top_hint))
                        .setPositiveButton(this.getResources().getString(R.string.user_repwd_ok),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mApp.mLoginManage.setUserLoginInterface(null);
                                        Intent it = new Intent(InternationUserPwdActivity.this, InternationalResetPwdActivity.class);
                                        it.putExtra("errorPwdOver", mPhone);
                                        if (mFrom.equals("main") || mFrom.equals("back")) {// 从起始页注册
                                            it.putExtra("fromRegist", "fromStart");
                                        } else if (mFrom.equals("indexmore")) {// 从更多页个人中心注册
                                            it.putExtra("fromRegist", "fromIndexMore");
                                        } else if (mFrom.equals("setup")) {// 从设置页注册
                                            it.putExtra("fromRegist", "fromSetup");
                                        } else if (mFrom.equals("profit")) {// 从我的收益注册
                                            it.putExtra("fromRegist", "fromProfit");
                                        }
                                        startActivity(it);
                                    }
                                }).create().show();
                break;
            // 密码错误
            case 6:
                mPwdEditText.setText("");
                break;
            default:

                break;
        }
    }
}