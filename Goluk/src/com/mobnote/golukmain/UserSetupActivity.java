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
 * @ ????????????:Goluk????????????
 * 
 * @author ?????????
 * 
 */

public class UserSetupActivity extends CarRecordBaseActivity implements OnClickListener, UserInterface,IRequestResultListener,
		ILiveDialogManagerFn {
	/** application */
	private GolukApplication mApp = null;
	/** ????????? */
	private Context mContext = null;
	/** ???????????? */
	private ImageButton mBackBtn = null;

	/** ???????????? **/
	private Button btnLoginout;
	/** ?????????????????? **/
	private TextView mTextCacheSize = null;
	/** ???????????? **/
	private String phone = null;
	/** ??????????????? **/
	private SharedPreferences mPreferences = null;
	private boolean isFirstLogin = false;
	private Editor mEditor = null;
	/** ????????????????????? */
	private Builder mBuilder = null;
	private AlertDialog dialog = null;
	/** ???????????? **/
	private RelativeLayout mClearCache = null;

	/** ???????????? **/
	private RelativeLayout mBindPhone = null;
	private TextView mBindTitle = null;

	private String vIpc = "";

	/** ??????ipc????????????????????? **/
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
		// ??????GolukApplication??????
		mApp = (GolukApplication) getApplication();

		vIpc = SharedPrefUtil.getIPCVersion();
		// ???????????????
		init();
		EventBus.getDefault().register(this);
		
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
		// ??????
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
	 * ???????????????
	 */
	@SuppressLint("HandlerLeak")
	private void init() {

		/** ???????????? */
		mClearCache = (RelativeLayout) findViewById(R.id.remove_cache_item);
		// ??????????????????
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		// ????????????
		btnLoginout = (Button) findViewById(R.id.loginout_btn);
		// ????????????????????????
		mTextCacheSize = (TextView) findViewById(R.id.user_personal_setup_cache_size);
		
		mBindPhone = (RelativeLayout) findViewById(R.id.RelativeLayout_binding_phone);
		mBindTitle = (TextView) findViewById(R.id.textview_binding_phone_des);
		// ??????????????????
//		mBtnSwitch = (ImageButton) findViewById(R.id.set_ipc_btn);
		mBtnSwitch = findViewById(R.id.set_ipc_item);
		// ????????????????????????
		findViewById(R.id.notify_comm_item).setOnClickListener(this);

		// ????????????
		btnLoginout.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		/** ???????????? **/
		mClearCache.setOnClickListener(this);
		/** ?????????????????? **/
		mBtnSwitch.setOnClickListener(this);
		/** ??????????????? **/
		mBindPhone.setOnClickListener(this);
	}

	/**
	 * ??????????????????????????????????????????
	 */
	public void judgeLogin() {
		// ????????????????????????
		mPreferences = getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);
		if (!isFirstLogin) {// ?????????
			if (mApp.loginStatus == 1 || mApp.registStatus == 2 || mApp.autoLoginStatus == 2
					|| mApp.isUserLoginSucess == true) {// ??????????????????
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
			// ??????
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
	 * ?????????????????????????????????
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
	 * ??????
	 */
	public void getLoginout() {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
		} else {
			userCancelBeanRequest = new UserCancelBeanRequest(IPageNotifyFn.PageType_SignOut, this);
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
		// ????????????
		mApp.isUserLoginSucess = false;
		mApp.loginoutStatus = true;// ????????????
		mApp.registStatus = 3;// ????????????
		mApp.autoLoginStatus = 3;
		mApp.loginStatus = 3;

		mPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		mEditor.putBoolean("FirstLogin", true);// ?????????????????????????????????????????????????????????
		// ????????????
		mEditor.commit();
		GolukFileUtils.remove(GolukFileUtils.THIRD_USER_INFO);
		mBindPhone.setVisibility(View.GONE);
		GolukUtils.showToast(mContext, this.getResources().getString(R.string.str_loginout_success));
		btnLoginout.setText(this.getResources().getString(R.string.login_text));
		MessageManager.getMessageManager().setMessageEveryCount(0, 0, 0,0);
		GolukNotification.getInstance().clearAllNotification(this);
	}


	/**
	 * ????????????????????????
	 */
	public void initData() {
		UserInfo info = mApp.getMyInfo();
		try {
			if(info != null && info.phone!= null  && !"".equals(info.phone)){
				GolukDebugUtils.i("lily", "====json()====" + JSON.toJSONString(info));
				// ???????????????????????????
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
	 * ?????????????????????????????????????????????????????????
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
	 * ???????????????????????????????????????????????????????????????
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * ????????????????????????????????????
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
	 * App?????????IPC??????????????????
	 * 
	 * @param function
	 *            App??????/IPC?????? 2/ 3
	 * @param data
	 *            ????????????
	 * @author jyf
	 * @date 2015???6???24???
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
//				// ??????????????????
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
					GolukApplication.getInstance().setLoginRespInfo("");
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
