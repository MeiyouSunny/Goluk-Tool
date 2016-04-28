package com.mobnote.golukmain;

import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBindPhoneNum;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.msg.MessageBadger;
import com.mobnote.golukmain.userlogin.CancelResult;
import com.mobnote.golukmain.userlogin.UserCancelBeanRequest;
import com.mobnote.golukmain.xdpush.GolukNotification;
import com.mobnote.manager.MessageManager;
import com.mobnote.user.DataCleanManage;
import com.mobnote.user.IpcUpdateManage;
import com.mobnote.user.UserInterface;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.Const;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 
 * @ 功能描述:Goluk个人设置
 * 
 * @author 陈宣宇
 * 
 */

public class UserSetupActivity extends CarRecordBaseActivity implements OnClickListener, UserInterface,IRequestResultListener,
		ILiveDialogManagerFn {
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;

	/** 退出按钮 **/
	private Button btnLoginout;
	/** 缓存大小显示 **/
	private TextView mTextCacheSize = null;
	/** 用户信息 **/
	private String phone = null;
	/** 登录的状态 **/
	private SharedPreferences mPreferences = null;
	private boolean isFirstLogin = false;
	private Editor mEditor = null;
	/** 正在登录对话框 */
	private Builder mBuilder = null;
	private AlertDialog dialog = null;
	/** 清除缓存 **/
	private RelativeLayout mClearCache = null;

	/** 绑定手机 **/
	private RelativeLayout mBindPhone = null;
	private TextView mBindTitle = null;

	private String vIpc = "";

	/** 连接ipc后自动同步开关 **/
//	private ImageButton mBtnSwitch = null;
	private View mBtnSwitch = null;
	public static final String MANUAL_SWITCH = "manualswitch";
	
	private UserCancelBeanRequest userCancelBeanRequest;
	
	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_personal_setup);

		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();

		vIpc = SharedPrefUtil.getIPCVersion();
		// 页面初始化
		init();
		EventBus.getDefault().register(this);
		
		userCancelBeanRequest = new UserCancelBeanRequest(IPageNotifyFn.PageType_SignOut, this);
//		int b = SettingUtils.getInstance().getInt(MANUAL_SWITCH, 5);
//		if (b) {
////			mBtnSwitch.setBackgroundResource(R.drawable.set_open_btn);
////			mBtnSwitch.setImageResource(R.drawable.set_open_btn);
//		} else {
////			mBtnSwitch.setBackgroundResource(R.drawable.set_close_btn);
////			mBtnSwitch.setImageResource(R.drawable.set_close_btn);
//		}
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(mContext, "UserSetup");
		mApp.mUser.setUserInterface(this);
		judgeLogin();
		// 缓存
		try {
			String cacheSize = DataCleanManage.getTotalCacheSize(mContext);
			mTextCacheSize.setText(cacheSize);
			GolukDebugUtils.i("lily", "------cacheSize-------" + cacheSize);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onEventMainThread(EventBindPhoneNum event) {
		if (null == event) {
			return;
		}

		if (1 == event.getCode()) {
			mBindTitle.setText(R.string.str_already_bind);
			mBindPhone.setEnabled(false);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		LiveDialogManager.getManagerInstance().setDialogManageFn(null);
		mApp.mUser.setUserInterface(null);
	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init() {

		/** 清除缓存 */
		mClearCache = (RelativeLayout) findViewById(R.id.remove_cache_item);
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		// 退出按钮
		btnLoginout = (Button) findViewById(R.id.loginout_btn);
		// 清除缓存大小显示
		mTextCacheSize = (TextView) findViewById(R.id.user_personal_setup_cache_size);
		
		mBindPhone = (RelativeLayout) findViewById(R.id.RelativeLayout_binding_phone);
		mBindTitle = (TextView) findViewById(R.id.textview_binding_phone_des);
		// 自动同步开关
//		mBtnSwitch = (ImageButton) findViewById(R.id.set_ipc_btn);
		mBtnSwitch = findViewById(R.id.set_ipc_item);
		// 消息通知添加监听
		findViewById(R.id.notify_comm_item).setOnClickListener(this);

		// 注册监听
		btnLoginout.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		/** 清除缓存 **/
		mClearCache.setOnClickListener(this);
		/** 自动同步开关 **/
		mBtnSwitch.setOnClickListener(this);
		/** 绑定手机号 **/
		mBindPhone.setOnClickListener(this);
	}

	/**
	 * 判断按钮是否为登录或者未登录
	 */
	public void judgeLogin() {
		// 没有登录过的状态
		mPreferences = getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);
		if (!isFirstLogin) {// 登录过
			if (mApp.loginStatus == 1 || mApp.registStatus == 2 || mApp.autoLoginStatus == 2
					|| mApp.isUserLoginSucess == true) {// 上次登录成功
				btnLoginout.setText(this.getResources().getString(R.string.logout));
				mBindPhone.setVisibility(View.VISIBLE);
				if (TextUtils.isEmpty(mApp.mCurrentPhoneNum)) {
					mBindTitle.setText(R.string.str_not_bind);
					mBindPhone.setEnabled(true);
				} else {
					mBindTitle.setText(R.string.str_already_bind);
					mBindPhone.setEnabled(false);
				}
			} else {
				btnLoginout.setText(this.getResources().getString(R.string.login_text));
				mBindPhone.setVisibility(View.GONE);
			}
		} else {
			if (mApp.registStatus == 2) {
				btnLoginout.setText(this.getResources().getString(R.string.logout));
				mBindPhone.setVisibility(View.VISIBLE);
				if (TextUtils.isEmpty(mApp.mCurrentPhoneNum)) {
					mBindTitle.setText(R.string.str_not_bind);
					mBindPhone.setEnabled(true);
				} else {
					mBindTitle.setText(R.string.str_already_bind);
					mBindPhone.setEnabled(false);
				}
			} else {
				btnLoginout.setText(this.getResources().getString(R.string.login_text));
				mBindPhone.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.back_btn) {
			mApp.mUser.setUserInterface(null);
			// 返回
			this.finish();
		} else if (id == R.id.loginout_btn) {
			if (btnLoginout.getText().toString().equals(this.getResources().getString(R.string.login_text))) {
				if (mApp.autoLoginStatus == 1) {
					mBuilder = new AlertDialog.Builder(mContext);
					dialog = mBuilder.
							setMessage(this.getResources().getString(R.string.user_personal_autoloading_progress)).
							setCancelable(true).
							setOnKeyListener(new OnKeyListener() {
								@Override
								public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
									if (keyCode == KeyEvent.KEYCODE_BACK) {
										return true;
									}
									return false;
								}
							}).create();
					dialog.show();
					return;
				}
				if(GolukApplication.getInstance().isInteral()  == false){
					initIntent(InternationUserLoginActivity.class);
				}else{
					initIntent(UserLoginActivity.class);
				}
				
			} else if (btnLoginout.getText().toString().equals(this.getResources().getString(R.string.logout))) {
				new AlertDialog.Builder(mContext).
					setTitle(this.getResources().getString(R.string.wifi_link_prompt)).
					setMessage(this.getResources().getString(R.string.str_confirm_loginout)).
					setPositiveButton(this.getResources().getString(R.string.str_button_ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								getLoginout();
							}
						}).setNegativeButton(this.getResources().getString(R.string.user_cancle), null).create().show();
			}
		} else if (id == R.id.remove_cache_item) {
			mApp.mUser.setUserInterface(null);
			GolukDebugUtils.i("lily", "----clearcach-----" + Const.getAppContext().getCacheDir().getPath());
			if (mTextCacheSize.getText().toString().equals("0M")) {
				UserUtils.showDialog(mContext, this.getResources().getString(R.string.str_no_cache));
			} else {
				new AlertDialog.Builder(mContext).
					setTitle(this.getResources().getString(R.string.wifi_link_prompt)).
					setMessage(this.getResources().getString(R.string.str_confirm_clear_cache)).
					setNegativeButton(this.getResources().getString(R.string.user_cancle), null).
					setPositiveButton(this.getResources().getString(R.string.str_button_ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								DataCleanManage.deleteFile(Const.getAppContext().getCacheDir());
								mTextCacheSize.setText("0.00B");
							}
						}).create().show();
			}
		} else if (id == R.id.set_ipc_item) {
			//			if (SettingUtils.getInstance().getBoolean(AUTO_SWITCH, true)) {
////				mBtnSwitch.setBackgroundResource(R.drawable.set_close_btn);
////				mBtnSwitch.setImageResource(R.drawable.set_close_btn);
//				SettingUtils.getInstance().putBoolean(AUTO_SWITCH, false);
//			} else {
////				mBtnSwitch.setBackgroundResource(R.drawable.set_open_btn);
////				mBtnSwitch.setImageResource(R.drawable.set_open_btn);
//				SettingUtils.getInstance().putBoolean(AUTO_SWITCH, true);
//			}
			// Start switch choosen activity
			Intent intent = new Intent(this, VideoSyncSettingActivity.class);
			startActivityForResult(intent, GolukConfig.REQUEST_CODE_VIDEO_SYNC_SETTING);
		} else if (id == R.id.notify_comm_item) {
			startMsgSettingActivity();
		} else if (id == R.id.RelativeLayout_binding_phone) {
			Intent itRegist = new Intent(this, UserRegistActivity.class);
			itRegist.putExtra("fromRegist", "fromBindPhone");
			startActivity(itRegist);
		} else {
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK &&
				requestCode == GolukConfig.REQUEST_CODE_VIDEO_SYNC_SETTING) {
			// save new sync value
			if(null != data) {
				int syncValue = data.getIntExtra(GolukConfig.STRING_VIDEO_SYNC_SETTING_VALUE, -1);
				SettingUtils.getInstance().putInt(MANUAL_SWITCH, syncValue);
			}
		}
	}

	/**
	 * 跳转到消息通知设置界面
	 * 
	 * @author jyf
	 */
	private void startMsgSettingActivity() {
		if (!mApp.isUserLoginSucess) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_please_login));
			return;
		}
		Intent intent = new Intent(this, PushSettingActivity.class);
		startActivity(intent);
	}

	/**
	 * 退出
	 */
	public void getLoginout() {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
		} else {
			userCancelBeanRequest.get(mApp.getMyInfo().uid);
//			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,IPageNotifyFn.PageType_SignOut, "");
//			if (!b) {
//				GolukUtils.showToast(this, this.getResources().getString(R.string.str_loginout_fail));
//				return;
//			}
			LiveDialogManager.getManagerInstance().showCommProgressDialog(this, LiveDialogManager.DIALOG_TYPE_LOGOUT,
					"", this.getResources().getString(R.string.str_loginouting), true);
		}
	}

	private void logoutSucess() {
		// 注销成功
		mApp.isUserLoginSucess = false;
		mApp.loginoutStatus = true;// 注销成功
		mApp.registStatus = 3;// 注册失败
		mApp.autoLoginStatus = 3;
		mApp.loginStatus = 3;

		mPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		mEditor.putBoolean("FirstLogin", true);// 注销完成后，设置为没有登录过的一个状态
		// 提交修改
		mEditor.commit();
		GolukFileUtils.remove(GolukFileUtils.THIRD_USER_INFO);
		mBindPhone.setVisibility(View.GONE);
		GolukUtils.showToast(mContext, this.getResources().getString(R.string.str_loginout_success));
		btnLoginout.setText(this.getResources().getString(R.string.login_text));
		MessageManager.getMessageManager().setMessageEveryCount(0, 0, 0,0);
		GolukNotification.getInstance().clearAllNotification(this);
	}


	/**
	 * 同步获取用户信息
	 */
	public void initData() {
		UserInfo info = mApp.getMyInfo();
		try {
			if(info != null && info.phone!= null  && !"".equals(info.phone)){
				GolukDebugUtils.i("lily", "====json()====" + JSON.toJSONString(info));
				// 注销后，将信息存储
				mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
				mEditor = mPreferences.edit();
				mEditor.putString("setupPhone", info.phone);
				mEditor.commit();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 没有登录过、登录失败、正在登录需要登录
	 */
	@SuppressWarnings("rawtypes")
	public void initIntent(Class intentClass) {
		Intent it = new Intent(UserSetupActivity.this, intentClass);
		it.putExtra("isInfo", "setup");

		mPreferences = getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		mEditor.putString("toRepwd", "set");
		mEditor.commit();

		startActivity(it);
	}

	/**
	 * 注销后，点击返回键，返回到无用户信息的页面
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 取消正在自动登录的对话框
	 */
	public void dismissAutoDialog() {
		if (null != dialog) {
			dialog.dismiss();
			dialog = null;
		}
	}

	@Override
	public void statusChange() {
		if (mApp.autoLoginStatus != 1) {
			dismissAutoDialog();
			if (mApp.autoLoginStatus == 2) {
				btnLoginout.setText(this.getResources().getString(R.string.logout));
			}
		}
	}

	/**
	 * App升级与IPC升级回调方法
	 * 
	 * @param function
	 *            App升级/IPC升级 2/ 3
	 * @param data
	 *            交互数据
	 * @author jyf
	 * @date 2015年6月24日
	 */
	public void updateCallBack(int function, Object data) {
		switch (function) {
		case IpcUpdateManage.FUNCTION_SETTING_APP:
			break;
		case IpcUpdateManage.FUNCTION_SETTING_IPC:
			break;
		default:
			break;
		}
	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (dialogType == LiveDialogManager.DIALOG_TYPE_LOGOUT) {
			if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {
//				// 用户取消注销
//				mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_SignOut,
//						JsonUtil.getCancelJson());
			}
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if(requestType == IPageNotifyFn.PageType_SignOut){
			LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
			CancelResult cancelResult = (CancelResult) result;
			if(cancelResult!=null && cancelResult.success){
				if("0".equals(cancelResult.data.result)){
					SharedPrefUtil.saveUserInfo("");
					SharedPrefUtil.saveUserPwd("");
					SharedPrefUtil.saveUserToken("");
					GolukApplication.getInstance().mCurrentUId = "";
					logoutSucess();
				}else{
					GolukUtils.showToast(this, this.getResources().getString(R.string.str_loginout_fail));
				}
			}else{
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_loginout_fail));
			}
			
		}
	}

}
