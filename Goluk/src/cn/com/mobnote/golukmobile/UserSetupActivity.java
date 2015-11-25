package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.DataCleanManage;
import cn.com.mobnote.user.IpcUpdateManage;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.SharedPrefUtil;
import cn.com.tiros.api.Const;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 
 * @ 功能描述:Goluk个人设置
 * 
 * @author 陈宣宇
 * 
 */

public class UserSetupActivity extends CarRecordBaseActivity implements OnClickListener, UserInterface,
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
//	public static Handler mHandler = null;

	private String vIpc = "";

	/** 连接ipc后自动同步开关 **/
	private ImageButton mBtnSwitch = null;
	public static final String AUTO_SWITCH = "autoswitch";

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
		boolean b = SettingUtils.getInstance().getBoolean(AUTO_SWITCH, true);
		if (b) {
//			mBtnSwitch.setBackgroundResource(R.drawable.set_open_btn);
			mBtnSwitch.setImageResource(R.drawable.set_open_btn);
		} else {
//			mBtnSwitch.setBackgroundResource(R.drawable.set_close_btn);
			mBtnSwitch.setImageResource(R.drawable.set_close_btn);
		}
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(mContext, "UserSetup");
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
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

//		mHandler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				super.handleMessage(msg);
//				if (msg.what == 0) {
//					GolukDebugUtils.i("lily", "已清除过缓存");
//				}
//			}
//		};

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
		// 自动同步开关
		mBtnSwitch = (ImageButton) findViewById(R.id.set_ipc_btn);
		// 消息通知添加监听
		findViewById(R.id.notify_comm_item).setOnClickListener(this);

		// 注册监听
		btnLoginout.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		/** 清除缓存 **/
		mClearCache.setOnClickListener(this);
		/** 自动同步开关 **/
		mBtnSwitch.setOnClickListener(this);
	}

	/**
	 * 判断按钮是否为登录或者未登录
	 */
	public void judgeLogin() {
		// 没有登录过的状态
		mPreferences = getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);
		GolukDebugUtils.i("lily", "----------UserSetupActivity11111-------" + mApp.registStatus);
		if (!isFirstLogin) {// 登录过
			GolukDebugUtils.i("lily", "----------UserSetupActivity-------" + mApp.registStatus);
			if (mApp.loginStatus == 1 || mApp.registStatus == 2 || mApp.autoLoginStatus == 2
					|| mApp.isUserLoginSucess == true) {// 上次登录成功
				btnLoginout.setText("注销");
			} else {
				btnLoginout.setText("登录");
			}
		} else {
			if (mApp.registStatus == 2) {
				btnLoginout.setText("注销");
			} else {
				btnLoginout.setText("登录");
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			mApp.mUser.setUserInterface(null);
			// 返回
			this.finish();
			break;
		// 退出按钮
		case R.id.loginout_btn:
			if (btnLoginout.getText().toString().equals("登录")) {
				if (mApp.autoLoginStatus == 1) {
					mBuilder = new AlertDialog.Builder(mContext);
					dialog = mBuilder.setMessage("正在为您登录，请稍候…").setCancelable(true)
							.setOnKeyListener(new OnKeyListener() {
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
				initIntent(UserLoginActivity.class);
			} else if (btnLoginout.getText().toString().equals("注销")) {
				new AlertDialog.Builder(mContext).setTitle("提示").setMessage("是否确认退出？")
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								getLoginout();
							}
						}).setNegativeButton("取消", null).create().show();
			}
			break;
		// 清除缓存
		case R.id.remove_cache_item:
			mApp.mUser.setUserInterface(null);
			GolukDebugUtils.i("lily", "----清除缓存-----" + Const.getAppContext().getCacheDir().getPath());
			if (mTextCacheSize.getText().toString().equals("0M")) {
				UserUtils.showDialog(mContext, "没有缓存数据");
			} else {
				new AlertDialog.Builder(mContext).setTitle("提示").setMessage("确定清除缓存？").setNegativeButton("取消", null)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								DataCleanManage.deleteFile(Const.getAppContext().getCacheDir());
								mTextCacheSize.setText("0.00B");
							}
						}).create().show();
			}
			break;
		// 自动同步开关
		case R.id.set_ipc_btn:
			if (SettingUtils.getInstance().getBoolean(AUTO_SWITCH, true)) {
//				mBtnSwitch.setBackgroundResource(R.drawable.set_close_btn);
				mBtnSwitch.setImageResource(R.drawable.set_close_btn);
				SettingUtils.getInstance().putBoolean(AUTO_SWITCH, false);
			} else {
//				mBtnSwitch.setBackgroundResource(R.drawable.set_open_btn);
				mBtnSwitch.setImageResource(R.drawable.set_open_btn);
				SettingUtils.getInstance().putBoolean(AUTO_SWITCH, true);
			}
			break;
		case R.id.notify_comm_item:
			startMsgSettingActivity();
			break;
		}
	}

	/**
	 * 跳转到消息通知设置界面
	 * 
	 * @author jyf
	 */
	private void startMsgSettingActivity() {
		if (!mApp.isUserLoginSucess) {
			GolukUtils.showToast(this, "请先登录");
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
			GolukUtils.showToast(mContext, "当前网络不可用，请检查网络后重试");
		} else {
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_SignOut, "");
			if (!b) {
				GolukUtils.showToast(this, "注销失败");
				return;
			}
			LiveDialogManager.getManagerInstance().showCommProgressDialog(this, LiveDialogManager.DIALOG_TYPE_LOGOUT,
					"", "正在注销...", true);
		}
	}

	private void logoutSucess() {
		// 注销成功
		mApp.isUserLoginSucess = false;
		mApp.loginoutStatus = true;// 注销成功
		mApp.registStatus = 3;// 注册失败

		mPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		mEditor.putBoolean("FirstLogin", true);// 注销完成后，设置为没有登录过的一个状态
		// 提交修改
		mEditor.commit();

		GolukUtils.showToast(mContext, "注销成功");
		btnLoginout.setText("登录");
	}

	/**
	 * 注销的回调
	 */
	public void getLogintoutCallback(int success, Object obj) {
		GolukDebugUtils.e("", "-----------------注销回调--------------------");
		LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
		if (1 != success) {
			GolukUtils.showToast(this, "注销失败");
			return;
		}
		logoutSucess();
	}

	/**
	 * 同步获取用户信息
	 */
	public void initData() {
		String info = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		try {
			JSONObject json = new JSONObject(info);

			GolukDebugUtils.i("lily", "====json()====" + json);
			phone = json.getString("phone");
			// 注销后，将信息存储
			mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
			mEditor = mPreferences.edit();
			mEditor.putString("setupPhone", phone);
			mEditor.commit();

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
				btnLoginout.setText("注销");
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
				// 用户取消注销
				mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_SignOut,
						JsonUtil.getCancelJson());
			}
		}
	}

}
