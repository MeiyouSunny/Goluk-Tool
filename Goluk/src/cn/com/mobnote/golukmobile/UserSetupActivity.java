package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
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
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.DataCleanManage;
import cn.com.mobnote.user.IpcUpdateManage;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.api.Const;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 1.类命名首字母大写 2.公共函数驼峰式命名 3.属性函数驼峰式命名 4.变量/参数驼峰式命名 5.操作符之间必须加空格 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处 8.所有代码必须使用TAB键缩进 9.函数使用块注释,代码逻辑使用行注释 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致 </pre>
 * 
 * @ 功能描述:Goluk个人设置
 * 
 * @author 陈宣宇
 * 
 */

public class UserSetupActivity extends CarRecordBaseActivity implements OnClickListener, UserInterface {
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
	/** 版本号显示 **/
	private TextView mTextVersionCode = null;
	/** 更新版本号信息 **/
	public static Handler mHandlerVersion = null;
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
	public static Handler mHandler = null;
	/** 解除绑定 **/
	private RelativeLayout mUnbindItem = null;
	/** 版本检测 **/
	private RelativeLayout mAppUpdate = null;
	/** 固件升级 */
	private RelativeLayout mUpdateItem = null;

	/** APP版本号显示 **/
	private TextView mTextAppVersion = null;
	/** IPC固件版本号显示 **/
	private TextView mTextIPCVersion = null;

	private String vIpc = "";

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_personal_setup);

		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();

		/** 清除缓存 */
		mClearCache = (RelativeLayout) findViewById(R.id.remove_cache_item);
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		// 退出按钮
		btnLoginout = (Button) findViewById(R.id.loginout_btn);
		// 清除缓存大小显示
		mTextCacheSize = (TextView) findViewById(R.id.user_personal_setup_cache_size);
		// 解除绑定
		mUnbindItem = (RelativeLayout) findViewById(R.id.unbind_item);
		// 版本号
		mTextVersionCode = (TextView) findViewById(R.id.user_setup_versioncode);
		// 版本检测
		mAppUpdate = (RelativeLayout) findViewById(R.id.app_update_item);
		// APP版本号
		mTextAppVersion = (TextView) findViewById(R.id.app_update_text_version);
		// IPC版本号
		mTextIPCVersion = (TextView) findViewById(R.id.ipc_update_text_version);

	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onResume() {
		super.onResume();

		mApp.setContext(mContext, "UserSetup");

		mApp.initSharedPreUtil(this);
		vIpc = mApp.mSharedPreUtil.getIPCVersion();

		mApp.mUser.setUserInterface(this);

		// 页面初始化
		init();

		// 调用同步接口，在设置页显示版本号
		String verName = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVersion, "fs6:/version");
		GolukDebugUtils.i("upgrade", "=======+version+=====" + verName);
		mTextVersionCode.setText(verName);
		mTextAppVersion.setText(verName);
		String vIpc = mApp.mSharedPreUtil.getIPCVersion();
		GolukDebugUtils.i("lily", vIpc + "===UserSetupActivity----vipc------" + verName);
		mTextIPCVersion.setText(vIpc);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0) {
					GolukDebugUtils.i("lily", "已清除过缓存");
				}
			}
		};

	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init() {

		try {
			String cacheSize = DataCleanManage.getTotalCacheSize(mContext);
			mTextCacheSize.setText(cacheSize);
			GolukDebugUtils.i("lily", "------cacheSize-------" + cacheSize);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 没有登录过的状态
		mPreferences = getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferences.getBoolean("FirstLogin", true);
		GolukDebugUtils.i("lily", "----------UserSetupActivity11111-------" + mApp.registStatus);
		if (!isFirstLogin) {// 登录过
			GolukDebugUtils.i("lily", "----------UserSetupActivity-------" + mApp.registStatus);
			if (mApp.loginStatus == 1 || mApp.registStatus == 2 || mApp.autoLoginStatus == 2
					|| mApp.isUserLoginSucess == true) {// 上次登录成功
				btnLoginout.setText("退出登录");
			} else {
				btnLoginout.setText("登录");
			}
		} else {
			if (mApp.registStatus == 2) {
				btnLoginout.setText("退出登录");
			} else {
				btnLoginout.setText("登录");
			}
		}
		btnLoginout.setOnClickListener(this);

		// 注册事件
		mBackBtn.setOnClickListener(this);
		/** 清除缓存 **/
		mClearCache.setOnClickListener(this);
		/** 解除绑定 **/
		mUnbindItem.setOnClickListener(this);
		/** 版本检测 **/
		mAppUpdate.setOnClickListener(this);
		/** 固件升级 */
		mUpdateItem = (RelativeLayout) findViewById(R.id.update_item);
		mUpdateItem.setOnClickListener(this);
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
		case R.id.setup_item:
			// 跳转到设置页面
			GolukDebugUtils.e("", "onclick---setup--item");
			break;
		// 退出按钮
		case R.id.loginout_btn:
			if (btnLoginout.getText().toString().equals("登录")) {
				if (mApp.autoLoginStatus == 1) {
					mBuilder = new AlertDialog.Builder(mContext);
					dialog = mBuilder.setMessage("正在为您登录，请稍候……").setCancelable(false)
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
			} else if (btnLoginout.getText().toString().equals("退出登录")) {
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
		// 解除绑定
		case R.id.unbind_item:
			mApp.mUser.setUserInterface(null);
			Intent itUnbind = new Intent(UserSetupActivity.this, UnbindActivity.class);
			startActivity(itUnbind);
			break;
		// 版本检测
		case R.id.app_update_item:
			mApp.mUser.setUserInterface(null);
			// 点击设置页中版本检测无最新版本提示标识
			GolukDebugUtils.i("lily", vIpc + "========UserSetupActivity==点击版本检测===中ipcVersion=====");
			boolean appB = mApp.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_APP, vIpc);
			break;
		// 固件升级
		case R.id.update_item:
			GolukDebugUtils.i("lily", vIpc + "========UserSetupActivity===点击固件升级==中ipcVersion=====");
			if (mApp.mLoadStatus && mApp.mLoadProgress != 100) {
				new AlertDialog.Builder(mApp.getContext()).setTitle("提示").setMessage("新极路客固件升级文件正在下载……")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent it = new Intent(UserSetupActivity.this, UpdateActivity.class);
								it.putExtra(UpdateActivity.UPDATE_PROGRESS, mApp.mLoadProgress);
								startActivity(it);
							}
						}).show();
			} else {
				boolean b = mApp.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_IPC, vIpc);
			}
			break;
		}
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
			GolukDebugUtils.e("", b + "");
			if (b) {
				// 注销成功
				mApp.isUserLoginSucess = false;
				mApp.loginoutStatus = true;// 注销成功
				mApp.registStatus = 3;// 注册失败

				mPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
				mEditor = mPreferences.edit();
				mEditor.putBoolean("FirstLogin", true);// 注销完成后，设置为没有登录过的一个状态
				// 提交修改
				mEditor.commit();

				GolukUtils.showToast(mContext, "退出登录成功");
				btnLoginout.setText("登录");

			} else {
				// 注销失败
				mApp.loginoutStatus = false;
				mApp.isUserLoginSucess = true;
			}
		}

	}

	/**
	 * 退出登录的回调
	 */
	public void getLogintoutCallback(int success, Object obj) {
		GolukDebugUtils.e("", "-----------------退出登录回调--------------------");
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
			// 退出登录后，将信息存储
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
	 * 退出登录后，点击返回键，返回到无用户信息的页面
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
				btnLoginout.setText("退出登录");
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

}
