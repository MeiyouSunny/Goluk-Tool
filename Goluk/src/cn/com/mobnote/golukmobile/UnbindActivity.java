package cn.com.mobnote.golukmobile;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.wifibind.WiFiLinkIndexActivity;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.user.IpcUpdateManage;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.SharedPrefUtil;
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
			mApplication.getIPCControlManager().addIPCManagerListener("Unbind", this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApplication.setContext(mContext, "Unbind");
		// 固件版本号
		vIpc = SharedPrefUtil.getIPCVersion();
		/**
		 * 判断是否是否绑定 true 绑定 显示解绑UI false 未绑定 显示未绑定UI
		 */
		boolean b = this.isBindSucess();
		GolukDebugUtils.e("bind", "====isBindSuccess====" + b);
		if (b) {
			mUnbindBtn.setText("解除设备绑定连接");
			mPwdLayout.setEnabled(true);
			String ipcName = this.ipcName();
			GolukDebugUtils.i("lily", "-------ipcName-----" + ipcName);
			mTextCameraName.setText(ipcName);
			mTextVersion.setText(vIpc);
		} else {
			mUnbindBtn.setText("绑定");
			mPwdLayout.setEnabled(false);
			mTextVersion.setText("");
		}
		// 密码
		if (null != mApplication) {
			if (isBindSucess()) {
				boolean isSucess = mApplication.getIPCControlManager().getIpcWifiConfig();
				if (isSucess) {
					// LiveDialogManager.getManagerInstance().showCustomDialog(this,
					// "正在获取信息");
				}
			}
			if (isBindSucess()) {
				String ipcPwd = SharedPrefUtil.getIpcPwd();
				mTextPasswordName.setText(ipcPwd);
			}
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

		mTextTitle.setText("摄像头管理");

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
		// TODO Auto-generated method stub
		if (mApplication.getIPCControlManager() != null) {
			mApplication.getIPCControlManager().removeIPCManagerListener("Unbind");
		}
		super.onDestroy();
	}
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.back_btn:
			this.finish();
			break;
		case R.id.unbind_layout_btn:
			if (mUnbindBtn.getText().toString().equals("解除设备绑定连接")) {
				new AlertDialog.Builder(this).setTitle("提示")
						.setMessage(this.getResources().getString(R.string.unbind_hint_message))
						.setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								toUnbind();
								mPwdLayout.setEnabled(false);
								mApplication.mIPCControlManager.setIPCWifiState(false, "");
								mApplication.setIpcLoginOut();
								GolukUtils.showToast(mContext, "解除绑定成功");
								//解除绑定后，所有信息置空
								SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
								preferences.edit().putString("ipc_bind_name", "").commit();
								SharedPrefUtil.saveIpcPwd("");
								SharedPrefUtil.saveIPCVersion("");
								vIpc = "";
								mTextCameraName.setText("");
								mTextPasswordName.setText("");
								mTextVersion.setText("");
								SharedPrefUtil.saveIpcModel("");
								mApplication.mIPCControlManager.mProduceName = "";
								isGetIPCSucess = false;
								mUnbindBtn.setText("绑定");
							}
						}).create().show();
			} else if (mUnbindBtn.getText().toString().equals("绑定")) {
				GolukDebugUtils.i("lily", "-------绑定设备------");
				Intent itWifiLink = new Intent(UnbindActivity.this, WiFiLinkIndexActivity.class);
				startActivity(itWifiLink);
			}
			break;
		case R.id.unbind_layout_password:
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
			break;
		case R.id.unbind_layout_update:
			if (mApplication.mLoadStatus) {// 下载中
				new AlertDialog.Builder(mApplication.getContext()).setTitle("提示").setMessage("新极路客固件升级文件正在下载……")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if ((Integer) (mApplication.mIpcUpdateManage.mParam1) == 100) {
									//查询ipc升级文件是否存在
									String ipcFile = mApplication.mIpcUpdateManage.isHasIPCFile(vIpc);
									if(!"".equals(ipcFile) && null != ipcFile){
										Intent itent = new Intent(UnbindActivity.this, UpdateActivity.class);
										itent.putExtra(UpdateActivity.UPDATE_SIGN, 1);
										startActivity(itent);
									}else{
										boolean b = mApplication.mIpcUpdateManage.requestInfo(
												IpcUpdateManage.FUNCTION_SETTING_IPC, vIpc);
									}
//									String localFile = mApplication.mIpcUpdateManage.getLocalFile(vIpc);
//									if (null == localFile || "".equals(localFile)) {
//										boolean b = mApplication.mIpcUpdateManage.requestInfo(
//												IpcUpdateManage.FUNCTION_SETTING_IPC, vIpc);
//									} else {
//										Intent itent = new Intent(UnbindActivity.this, UpdateActivity.class);
//										itent.putExtra(UpdateActivity.UPDATE_SIGN, 1);
//										startActivity(itent);
//									}
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
					if("".equals(ipcFile) || null == ipcFile){
						boolean b = mApplication.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_IPC,
								vIpc);
					}else{
						Intent itUpdate = new Intent(UnbindActivity.this, UpdateActivity.class);
						itUpdate.putExtra(UpdateActivity.UPDATE_SIGN, 1);
						startActivity(itUpdate);
					}
//					String localFile = mApplication.mIpcUpdateManage.getLocalFile(vIpc);
//					if (null == localFile || "".equals(localFile)) {
//						boolean b = mApplication.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_IPC,
//								vIpc);
//					} else {
//						Intent itUpdate = new Intent(UnbindActivity.this, UpdateActivity.class);
//						itUpdate.putExtra(UpdateActivity.UPDATE_SIGN, 1);
//						startActivity(itUpdate);
//					}
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		GolukDebugUtils.e("", "jyf-----UnbindActivity------onActivityResult request:" + requestCode + "  resultCode:"
				+ resultCode);
		if (10 == requestCode) {
			if (10 == resultCode) {
				this.finish();
			}
		}
	}

	public String ipcName() {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
		return preferences.getString("ipc_bind_name", "");

	}

	// 是否綁定过 Goluk true为绑定
	public boolean isBindSucess() {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
		return preferences.getBoolean("isbind", false);
	}

	// 解绑
	public void toUnbind() {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
		Editor mEditor = preferences.edit();
		mEditor.putBoolean("isbind", false);
		mEditor.commit();
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "jyf-----UnbindActivity-----IPCManage_CallBack event:" + event + " msg:" + msg
				+ " param1:" + param1 + " param2:" + param2);
		if (ENetTransEvent_IPC_VDCP_CommandResp == event) {
			switch (msg) {
			case IPC_VDCP_Msg_GetWifiCfg:
				// event:1 msg:1033 param1:0 param2:{"GolukPWD": "123456789",
				// "AP_SSID": "Golukd6e16e", "AP_PWD": "123456789", "GolukSSID":
				// "GOLUKd6e16e"}
				// LiveDialogManager.getManagerInstance().dissmissCustomDialog();
				if (0 == param1) {
					// 获取成功
					try {
						JSONObject obj = new JSONObject((String) param2);
						mGolukSSID = obj.getString("GolukSSID");
						mGolukPWD = obj.getString("GolukPWD");
						// 摄像头信息
						mApSSID = obj.getString("AP_SSID");
						mApPWD = obj.getString("AP_PWD");
						SharedPrefUtil.saveIpcPwd(mApPWD);
						GolukDebugUtils.i("lily", "--------mApPWD------" + mApPWD);
						if (isBindSucess()) {
							mTextPasswordName.setText(mApPWD);
						}
						isGetIPCSucess = true;
					} catch (Exception e) {
						GolukUtils.showToast(this, "获取失败");
					}
				} else {
					// 获取失败
					GolukUtils.showToast(this, "获取失败");
				}

				break;
			}
		}
	}

}
