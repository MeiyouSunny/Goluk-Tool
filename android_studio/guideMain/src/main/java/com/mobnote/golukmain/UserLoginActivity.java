package com.mobnote.golukmain;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventLoginSuccess;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.golukmain.thirdlogin.ThirdPlatformLoginUtil;
import com.mobnote.golukmain.thirdlogin.ThirdUserInfoGet;
import com.mobnote.user.ThirdLoginInfo;
import com.mobnote.user.UserLoginInterface;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.TextView;

import cn.com.mobnote.eventbus.EventShortLocationFinish;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 登陆模块
 * <p>
 * 1、手机号码、密码的输入 2、手机号码快速注册 3、忘记密码（重置密码） 4、第三方登陆
 *
 * @author mobnote
 */
public class UserLoginActivity extends BaseActivity implements OnClickListener, UserLoginInterface, OnTouchListener,
        ThirdUserInfoGet {

    private static final String TAG = "lily";
    /**
     * 判断是否能点击提交按钮
     **/
    private boolean isOnClick = false;
    /**
     * 登陆title
     **/
    private ImageButton mBackButton;
    private TextView mTextViewTitle;
    /**
     * 手机号和密码
     **/
    private EditText mEditTextPhoneNumber, mEditTextPwd;
    private Button mBtnLogin;
    /**
     * 快速注册
     **/
    private TextView mTextViewRegist, mTextViewForgetPwd;
    /**
     * application
     **/
    private GolukApplication mApplication = null;
    /**
     * context
     **/
    private Context mContext = null;
    private String phone = null;
    private String pwd = null;
    /**
     * 将用户的手机号和密码保存到本地
     **/
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;

    /**
     * 判断登录
     **/
    private String justLogin = "";
    private CustomLoadingDialog mCustomProgressDialog = null;

    private boolean flag = false;

    /**
     * 微信登陆
     **/
    Button mImageViewWeiXinLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
        setContentView(R.layout.user_login);
        mContext = this;
        // 获得GolukApplication对象
        mApplication = (GolukApplication) getApplication();
        initView();
        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_loginning));
        }
        // 设置title
        mTextViewTitle.setText(this.getResources().getString(R.string.user_login_title_text));

        if (null != mApplication && null != mApplication.mLoginManage) {
            mApplication.mLoginManage.initData();
        }

        mShareAPI = UMShareAPI.get(mContext);

	}

	public void onEventMainThread(EventLoginSuccess event) {
		finish();
	}

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mImageViewWeiXinLogin) {
            mImageViewWeiXinLogin.setEnabled(true);
        }
        mApplication.setContext(mContext, "UserLogin");

        getInfo();

        ZhugeUtils.eventLogin(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mImageViewWeiXinLogin) {
            mImageViewWeiXinLogin.setEnabled(false);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!flag) {
            mSharedPreferences = getSharedPreferences("setup", Context.MODE_PRIVATE);
            GolukDebugUtils.i(TAG, mSharedPreferences.getString("setupPhone", "") + "=======保存phone1111");
            if (null != mEditTextPhoneNumber.getText().toString()
                    && mEditTextPhoneNumber.getText().toString().replace("-", "").length() == 11) {
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
        // 登录按钮
        mBtnLogin.setOnClickListener(this);
        mBtnLogin.setOnTouchListener(this);
        // 快速注册
        mTextViewRegist.setOnClickListener(this);
        mTextViewForgetPwd.setOnClickListener(this);
        // 微信登陆
        mImageViewWeiXinLogin = (Button) findViewById(R.id.btn_weixin_login);
        mImageViewWeiXinLogin.setOnClickListener(this);

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
            private boolean isDelete = false;

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String Phonenum = mEditTextPhoneNumber.getText().toString().replace("-", "");
                String psw = mEditTextPwd.getText().toString();
                if (Phonenum.equals("")) {
                    isOnClick = false;
                }
                if (!Phonenum.equals("") && !psw.equals("") && psw.length() >= 6 && Phonenum.length() == 11
                        && UserUtils.isMobileNO(Phonenum)) {
                    mBtnLogin.setBackgroundResource(R.drawable.icon_login);
                    mBtnLogin.setEnabled(true);
                } else {
                    mBtnLogin.setBackgroundResource(R.drawable.icon_more);
                    mBtnLogin.setEnabled(false);
                }
                // 格式化显示手机号
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
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
        // 密码监听
        mEditTextPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String number = mEditTextPhoneNumber.getText().toString().replace("-", "");
                String psw = mEditTextPwd.getText().toString();
                if (isOnClick) {
                    if (!psw.equals("") && psw.length() >= 6) {
                        mBtnLogin.setBackgroundResource(R.drawable.icon_login);
                        mBtnLogin.setEnabled(true);
                    } else {
                        mBtnLogin.setBackgroundResource(R.drawable.icon_more);
                        mBtnLogin.setEnabled(false);
                    }
                }
                if (!number.equals("") && !psw.equals("") && psw.length() >= 6 && number.length() == 11
                        && UserUtils.isMobileNO(number)) {
                    mBtnLogin.setBackgroundResource(R.drawable.icon_login);
                    mBtnLogin.setEnabled(true);
                } else {
                    mBtnLogin.setBackgroundResource(R.drawable.icon_more);
                    mBtnLogin.setEnabled(false);
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
        if (GolukUtils.isFastDoubleClick()) {
            return;
        }
        int id = arg0.getId();
        if (id == R.id.back_btn) {
            mApplication.mLoginManage.setUserLoginInterface(null);
            UserUtils.hideSoftMethod(this);
            setResult(Activity.RESULT_CANCELED);
            this.finish();
        } else if (id == R.id.user_login_layout_btn) {
            loginManage();
        } else if (id == R.id.user_login_phoneRegist) {
            mApplication.mLoginManage.setUserLoginInterface(null);
            UserUtils.hideSoftMethod(this);
            Intent itRegist = new Intent(UserLoginActivity.this, UserRegistActivity.class);
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
        } else if (id == R.id.user_login_forgetpwd) {
            mApplication.mLoginManage.setUserLoginInterface(null);
            UserUtils.hideSoftMethod(this);
            Intent itForget = new Intent(UserLoginActivity.this, UserRepwdActivity.class);
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
        } else if (id == R.id.btn_weixin_login) {
            if (GolukUtils.isNetworkConnected(this)) {
                if (!GolukUtils.isAppInstalled(this, "com.tencent.mm")) {
                    GolukUtils.showToast(this, getString(R.string.str_no_weixin));
                    return;
                }
                ZhugeUtils.eventWixinLogin(this);
                String infoStr = GolukFileUtils.loadString(GolukFileUtils.THIRD_USER_INFO, "");
                if (TextUtils.isEmpty(infoStr)) {
                    ThirdPlatformLoginUtil thirdPlatformLogin = new ThirdPlatformLoginUtil(this);
                    thirdPlatformLogin.setListener(this);
                    thirdPlatformLogin.login(SHARE_MEDIA.WEIXIN);
                } else {
                    mApplication.mLoginManage.setUserLoginInterface(this);

                    HashMap<String, String> info = new HashMap<String, String>();
                    info.put("platform", "weixin");
                    info.put("userinfo", infoStr);
                    info.put("devices", GolukFileUtils.loadString(GolukFileUtils.KEY_BIND_HISTORY_LIST, ""));
                    mApplication.mLoginManage.loginBy3rdPlatform(info);
                    mApplication.loginStatus = 0;
                    showProgressDialog();
                }
            } else {
                GolukUtils.showToast(this, getResources().getString(R.string.str_check_network));
            }

        }
    }

    /**
     * 登录管理类
     */
    private void loginManage() {
        phone = mEditTextPhoneNumber.getText().toString().replace("-", "");
        pwd = mEditTextPwd.getText().toString();
        if (!"".equals(phone)) {
            if (UserUtils.isMobileNO(phone)) {
                if (!"".equals(pwd)) {
                    if (pwd.length() >= 6 && pwd.length() <= 16) {
                        if (!UserUtils.isNetDeviceAvailable(this)) {
                            UserUtils.hideSoftMethod(this);
                            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
                        } else {
                            mApplication.mLoginManage.setUserLoginInterface(this);
                            mApplication.mLoginManage.loginByPhone(phone, pwd, "");
                            mApplication.loginStatus = 0;
                            showProgressDialog();
//							if (b) {

							/*} else {
								closeProgressDialog();
								mApplication.loginStatus = 2;
							}*/
                        }

                    } else {
                        UserUtils.hideSoftMethod(this);
                        UserUtils.showDialog(mApplication.getContext(),
                                this.getResources().getString(R.string.user_login_password_show_error));
                    }
                }
            } else {
                UserUtils.hideSoftMethod(this);
                UserUtils.showDialog(mApplication.getContext(),
                        this.getResources().getString(R.string.user_login_phone_show_error));
            }
        }
    }

    /**
     * 登录管理类回调返回的状态 0登录中 1登录成功 2登录失败 3用户未注册 4登录超时
     */
    @Override
    public void loginCallbackStatus() {
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
                mBackButton.setEnabled(true);
                mApplication.mUser.timerCancel();
                mApplication.autoLoginStatus = 2;
                mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
                String uid = mSharedPreferences.getString("uid", "");
                if ("profit".equals(justLogin)) {
                    Intent itProfit = new Intent(UserLoginActivity.this, MyProfitActivity.class);
                    // itProfit.putExtra("uid", uid);
                    // itProfit.putExtra("phone", phone);
                    startActivity(itProfit);
                }
                EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_REQUEST));
                setResult(Activity.RESULT_OK);
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
                if (UserUtils.isMobileNO(phone)) {
                    new AlertDialog.Builder(this)
                            .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                            .setMessage(this.getResources().getString(R.string.user_no_regist))
                            .setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
                            .setPositiveButton(this.getResources().getString(R.string.user_regist),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            mApplication.mLoginManage.setUserLoginInterface(null);
                                            Intent it = new Intent(UserLoginActivity.this, UserRegistActivity.class);
                                            it.putExtra("intentLogin",
                                                    mEditTextPhoneNumber.getText().toString().replace("-", ""));
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
                } else {
                    UserUtils.showDialog(this, this.getResources().getString(R.string.user_login_phone_show_error));
                }
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
                        .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                        .setMessage(this.getResources().getString(R.string.user_login_password_limit_top_hint))
                        .setPositiveButton(this.getResources().getString(R.string.user_repwd_ok),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mApplication.mLoginManage.setUserLoginInterface(null);
                                        Intent it = new Intent(UserLoginActivity.this, UserRepwdActivity.class);
                                        it.putExtra("errorPwdOver",
                                                mEditTextPhoneNumber.getText().toString().replace("-", ""));
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
        int id = view.getId();
        if (id == R.id.user_login_layout_btn) {
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
        } else {
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
            mBackButton.setEnabled(true);
			mImageViewWeiXinLogin.setEnabled(true);
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
		EventBus.getDefault().unregister(this);
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
                .getSystemService(Context.ACTIVITY_SERVICE));
        // android.app.ActivityManager.getRunningTasks(int maxNum)
        // 即最多取得的运行中的任务信息(RunningTaskInfo)数量
        List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
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

    private UMShareAPI mShareAPI = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mShareAPI.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void getUserInfo(boolean success, String usrInfo, String platform) {
        GolukDebugUtils.e("", "three login------UserLogingActivity--getUserInfo ---1");
        if (success) {
            mApplication.mLoginManage.setUserLoginInterface(this);
            HashMap<String, String> info = new HashMap<String, String>();
            info.put("platform", platform);
            info.put("userinfo", usrInfo);
            info.put("devices", GolukFileUtils.loadString(GolukFileUtils.KEY_BIND_HISTORY_LIST, ""));
            mApplication.mLoginManage.loginBy3rdPlatform(info);
            mApplication.loginStatus = 0;
            showProgressDialog();
        }
    }

    private void showProgressDialog() {
        UserUtils.hideSoftMethod(this);
        if (!this.isFinishing()) {
            mCustomProgressDialog.show();
        }

        mEditTextPhoneNumber.setEnabled(false);
        mEditTextPwd.setEnabled(false);
        mTextViewRegist.setEnabled(false);
        mTextViewForgetPwd.setEnabled(false);
        mBtnLogin.setEnabled(false);
        mBackButton.setEnabled(false);
        mImageViewWeiXinLogin.setEnabled(false);
    }
}
