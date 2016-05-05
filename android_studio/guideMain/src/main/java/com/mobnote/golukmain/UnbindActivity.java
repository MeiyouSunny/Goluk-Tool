package com.mobnote.golukmain;

import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.wifibind.WifiUnbindSelectListActivity;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.user.IpcUpdateManage;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

public class UnbindActivity extends BaseActivity implements OnClickListener, IPCManagerFn {

	/** title **/
	private ImageButton mBackBtn = null;
	private TextView mTextTitle = null;
	private RelativeLayout mPwdLayout = null;
	private RelativeLayout mUpdateLayout = null;
	private TextView mTextPasswordName = null;
	private TextView mTextCameraName = null;
	private Button mUnbindBtn = null;

	private GolukApplication mApplication = null;
	private Context mContext = null;
	private boolean isGetIPCSucess = false;

	private String mGolukSSID = "";
	private String mGolukPWD = "";
	private String mApSSID = "";
	private String mApPWD = "";
	/** 固件版本号 **/
	private TextView mTextVersion = null;
	/** 获取版本号 **/
	private String vIpc = "";
	private RelativeLayout mIPCViewLayout;
	private TextView mIPCModelText, mIPCNumberText, mIPCVersionText;
	private ImageView mIPCimage;
	public static final String TAG = "Unbind";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = this;
		// 获得GolukApplication对象
		mApplication = (GolukApplication) getApplication();

		setContentView(R.layout.unbind_layout);
		initView();
		if (mApplication.getIPCControlManager() != null) {
			mApplication.getIPCControlManager().addIPCManagerListener(TAG, this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApplication.setContext(mContext, TAG);
		// 固件版本号
		vIpc = SharedPrefUtil.getIPCVersion();
		String ipcModel = GolukApplication.getInstance().mIPCControlManager.mProduceName;
		String ipcNumber = SharedPrefUtil.getIPCNumber();
		mUnbindBtn.setText(this.getResources().getString(R.string.str_ipc_change_others));
		// 获取当前使用的信息
		WifiBindHistoryBean bean = WifiBindDataCenter.getInstance().getCurrentUseIpc();
		if (mApplication.isBindSucess() && null != bean) {
			mIPCViewLayout.setVisibility(View.VISIBLE);
			mPwdLayout.setEnabled(true);
			// String ipcName = this.ipcName();
			mTextCameraName.setText(bean.ipc_ssid);
			mTextVersion.setText(vIpc);
			GolukApplication.getInstance().getIPCControlManager();
			if (IPCControlManager.T1_SIGN.equals(ipcModel) || IPCControlManager.T1s_SIGN.equals(ipcModel)) {
				mIPCimage.setImageResource(R.drawable.connect_t1_icon_1);
			} else {
				mIPCimage.setImageResource(R.drawable.ipc);
			}
			mIPCModelText.setText(this.getResources().getString(R.string.app_name) + ipcModel);
			mIPCNumberText.setText(this.getResources().getString(R.string.str_ipc_number_text) + ipcNumber);
			mIPCVersionText.setText(this.getResources().getString(R.string.str_ipc_version_text) + vIpc);
		} else {
			mUnbindBtn.setText(this.getResources().getString(R.string.str_ipc_change_bind_news));
			mIPCViewLayout.setVisibility(View.GONE);
			mPwdLayout.setEnabled(false);
			mTextVersion.setText("");
			mTextCameraName.setText("");
			mTextPasswordName.setText("");
		}
		// 密码
		if (null != mApplication && mApplication.isBindSucess() && mApplication.isIpcLoginSuccess) {
			mApplication.getIPCControlManager().getIpcWifiConfig();
		}
	}

	// 初始化
	public void initView() {
		// title
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mTextTitle = (TextView) findViewById(R.id.user_title_text);
		// body
		mTextCameraName = (TextView) findViewById(R.id.unbind_camera_name);
		mUnbindBtn = (Button) findViewById(R.id.unbind_layout_btn);
		mPwdLayout = (RelativeLayout) findViewById(R.id.unbind_layout_password);
		mTextPasswordName = (TextView) findViewById(R.id.unbind_password_name);
		mTextVersion = (TextView) findViewById(R.id.unbind_update_name);
		mUpdateLayout = (RelativeLayout) findViewById(R.id.unbind_layout_update);
		mIPCViewLayout = (RelativeLayout) findViewById(R.id.rl_unbind_view);
		mIPCModelText = (TextView) findViewById(R.id.goluk_name);
		mIPCNumberText = (TextView) findViewById(R.id.goluk_mobile);
		mIPCVersionText = (TextView) findViewById(R.id.goluk_version);
		mIPCimage = (ImageView) findViewById(R.id.goluk_icon);

		mTextTitle.setText(this.getResources().getString(R.string.my_camera_title_text));

		/**
		 * 监听
		 */
		mBackBtn.setOnClickListener(this);
		mUnbindBtn.setOnClickListener(this);
		mPwdLayout.setOnClickListener(this);
		mUpdateLayout.setOnClickListener(this);

	}

	@Override
	protected void onDestroy() {
		if (mApplication.getIPCControlManager() != null) {
			mApplication.getIPCControlManager().removeIPCManagerListener(TAG);
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.back_btn) {
			this.finish();
		} else if (id == R.id.unbind_layout_btn) {
			Intent itWifiLink = new Intent(UnbindActivity.this, WifiUnbindSelectListActivity.class);
			startActivity(itWifiLink);
		} else if (id == R.id.unbind_layout_password) {
			if (!isGetIPCSucess) {
				return;
			}
			String password = mTextPasswordName.getText().toString();
			Intent it = new Intent(UnbindActivity.this, UserSetupChangeWifiActivity.class);
			it.putExtra("wifiPwd", password);
			it.putExtra("golukssid", mGolukSSID);
			it.putExtra("golukpwd", mGolukPWD);
			it.putExtra("apssid", mApSSID);
			it.putExtra("appwd", mApPWD);
			startActivityForResult(it, 10);
		} else if (id == R.id.unbind_layout_update) {
			// 防止重复点击
			if (!mApplication.mIpcUpdateManage.isCanClick()) {
				return;
			}
			if (mApplication.mLoadStatus) {// 下载中
				new AlertDialog.Builder(mApplication.getContext())
						.setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
						.setMessage(this.getResources().getString(R.string.str_new_updatefile_loading))
						.setPositiveButton(getResources().getString(R.string.user_repwd_ok),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										if ((Integer) (mApplication.mIpcUpdateManage.mParam1) == 100) {
											// 查询ipc升级文件是否存在
											String ipcFile = mApplication.mIpcUpdateManage.isHasIPCFile(vIpc);
											if (!"".equals(ipcFile) && null != ipcFile) {
												Intent itent = new Intent(UnbindActivity.this, UpdateActivity.class);
												itent.putExtra(UpdateActivity.UPDATE_SIGN, 1);
												startActivity(itent);
											} else {
												boolean b = mApplication.mIpcUpdateManage.requestInfo(
														IpcUpdateManage.FUNCTION_SETTING_IPC, vIpc);
											}
										} else {
											Intent it = new Intent(UnbindActivity.this, UpdateActivity.class);
											it.putExtra(UpdateActivity.UPDATE_PROGRESS,
													(Integer) (mApplication.mIpcUpdateManage.mParam1));
											startActivity(it);
										}
									}
								}).show();
			} else {
				if ((Integer) (mApplication.mIpcUpdateManage.mParam1) == -1) {// 下载失败/程序刚进来
					boolean b = mApplication.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_IPC, vIpc);
				} else {// 下载成功
					String ipcFile = mApplication.mIpcUpdateManage.isHasIPCFile(vIpc);
					if ("".equals(ipcFile) || null == ipcFile) {
						boolean b = mApplication.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_IPC,
								vIpc);
					} else {
						Intent itUpdate = new Intent(UnbindActivity.this, UpdateActivity.class);
						itUpdate.putExtra(UpdateActivity.UPDATE_SIGN, 1);
						startActivity(itUpdate);
					}
				}
			}
		} else {
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		GolukDebugUtils.e("", "-----UnbindActivity------onActivityResult request:" + requestCode + "  resultCode:"
				+ resultCode);
		if (10 == requestCode) {
			if (10 == resultCode) {
				this.finish();
			}
		}
	}

	// public String ipcName() {
	// SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind",
	// MODE_PRIVATE);
	// return preferences.getString("ipc_bind_name", "");
	// }

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (ENetTransEvent_IPC_VDCP_CommandResp != event) {
			return;
		}
		GolukDebugUtils.e("", "jyf-----UnbindActivity-----IPCManage_CallBack event:" + event + " msg:" + msg
				+ " param1:" + param1 + " param2:" + param2);
		switch (msg) {
		case IPC_VDCP_Msg_GetWifiCfg:
			if (0 == param1) {
				// 获取成功
				callBack_getPwdSuccess((String) param2);
			} else {
				// 获取失败
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_getwificfg_fail));
			}
			break;
		}
	}

	/**
	 * 获取IPC密码成功
	 * 
	 * @param jsonPwd
	 * @author jyf
	 */
	private void callBack_getPwdSuccess(String jsonPwd) {
		try {
			JSONObject obj = new JSONObject(jsonPwd);
			mGolukSSID = obj.getString("GolukSSID");
			mGolukPWD = obj.getString("GolukPWD");
			// 摄像头信息
			mApSSID = obj.getString("AP_SSID");
			mApPWD = obj.getString("AP_PWD");
			// SharedPrefUtil.saveIpcPwd(mApPWD);
			if (mApplication.isBindSucess()) {
				mTextPasswordName.setText(mApPWD);
			}
			isGetIPCSucess = true;
		} catch (Exception e) {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_getwificfg_fail));
		}
	}

}
