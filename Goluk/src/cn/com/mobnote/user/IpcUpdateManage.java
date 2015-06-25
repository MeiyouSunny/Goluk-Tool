package cn.com.mobnote.user;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.view.KeyEvent;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.UpdateActivity;
import cn.com.mobnote.golukmobile.UserSetupActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * app升级+ipc升级
 * 
 * @author mobnote
 *
 */
public class IpcUpdateManage implements IPCManagerFn {

	private static final String TAG = "lily";
	private GolukApplication mApp = null;
	private static final String BIN_PATH = "fs1:/update/ipc_upgrade_2015-04-30-15-58.bin";

	/** 启动APP **/
	public static final int FUNCTION_AUTO = 0;
	/** 连接ipc **/
	public static final int FUNCTION_CONNECTIPC = 1;
	/** 设置中点击版本检测 **/
	public static final int FUNCTION_SETTING_APP = 2;
	/** 设置中点击ipc升级 **/
	public static final int FUNCTION_SETTING_IPC = 3;

	/** version文件的路径 */
	public static final String VERSION_PATH = "fs6:/version";

	/** 设置中点击版本检测 / 固件升级中loading **/
	private CustomLoadingDialog mCustomLoadingDialog = null;

	/** 下载类型 */
	public static final int TYPE_DOWNLOAD = 0;
	/** 安装 */
	public static final int TYPE_INSTALL = 1;

	private int mFunction = -1;
	/** 下载提示框 */
	private AlertDialog mDownloadDialog = null;

	public IpcUpdateManage(GolukApplication mApp) {
		super();
		this.mApp = mApp;
	}

	/**
	 * 升级 将appVersion和ipcVersion信息请求给服务器
	 * 
	 * @param function
	 * @param vipc
	 */
	public boolean requestInfo(int function, String vipc) {
		if (!UserUtils.isNetDeviceAvailable(mApp.getContext())) {
			GolukDebugUtils.i(TAG, "网络连接异常，请检查网络后重试");
			return false;
		} else {
			// {“AppVersionFilePath”:”fs6:/version”, “IpcVersion”:”1.2.3.4”}
			if (isHasUpdateDialogShow()) {
				// 上次检查结果的Dialog还存在，不进行下次操作
				return false;
			}
			String ipcString = JsonUtil.putIPC(VERSION_PATH, vipc);
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_CheckUpgrade, ipcString);
			GolukDebugUtils.i(TAG, "=====" + b + "===ipcUpdateManage======");
			if (b) {
				mFunction = function;

				if (mFunction == FUNCTION_SETTING_APP || mFunction == FUNCTION_SETTING_IPC) {
					if (null == mCustomLoadingDialog) {
						mCustomLoadingDialog = new CustomLoadingDialog(this.mApp.getContext(), "检测中，请稍候……");
					}
					mCustomLoadingDialog.show();
				}
			}
			return b;
		}
	}

	private void showLoadingDialog() {
		// TODO 弹出Loading对话框
	}

	private void dimissLoadingDialog() {
		// TODO 隐藏Loading对话框
	}

	/**
	 * 取消升级Http请求
	 * 
	 * @author jyf
	 * @date 2015年6月24日
	 */
	public void cancelHttpRequest() {
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_CheckUpgrade,
				JsonUtil.getCancelJson());
	}

	// ipc连接成功后调用
	public int connectIpc() {
		return mFunction;
	}

	/**
	 * 通过版本号，获取本地文件，如果本地文件存在，則不下载，直接提示安装，如果不存在，則要提示下载
	 * 
	 * @param ipcVersion
	 *            IPC版本号
	 * @return null　表示文件不存在
	 * @author jyf
	 * @date 2015年6月24日
	 */
	public String getLocalFile(String ipcVersion) {
		//
		return null;
	}

	/**
	 * 升级回调
	 * 
	 * @param success
	 * @param outTime
	 * @param obj
	 */
	public void requestInfoCallback(int success, Object outTime, Object obj) {
		GolukDebugUtils.i(TAG, "=======requestInfoCallback=======");
		// 取消loading显示
		closeProgressDialog();
		int codeOut = (Integer) outTime;
		dimissLoadingDialog();
		if (1 == success) {
			try {
				String dataObj = (String) obj;
				JSONObject json = new JSONObject(dataObj);
				GolukDebugUtils.i(TAG, "------upgradeGoluk---" + json);
				String data = json.getString("data");
				JSONObject jsonData = new JSONObject(data);

				String goluk = jsonData.getString("goluk");
				JSONArray ipc = jsonData.getJSONArray("ipc");
				// 保存ipc匹配信息
				mApp.mSharedPreUtil.saveIPCMatchInfo(ipc.toString());

				GolukDebugUtils.i(TAG, "===ipc----goluk=------" + goluk);

				if (FUNCTION_AUTO == mFunction) {

					if (goluk.equals("{}")) {
						GolukDebugUtils.i("lily", "------goluk为空，不用进行升级------");
						// APP不需要升级，判断ipc是否需要升级
						IPCInfo ipcInfo = ipcUpdateUtils(ipc);
					} else {
						appUpgradeUtils(goluk);
					}

				} else if (FUNCTION_CONNECTIPC == mFunction) {
					// ipc连接后不匹配
					if (goluk.equals("{}")) {
						GolukDebugUtils.i("lily", "------goluk为空，不用进行升级------");
						// APP不需要升级，判断ipc是否需要升级
						IPCInfo ipcInfo = ipcUpdateUtils(ipc);
					} else {
						appUpgradeUtils(goluk);
					}
				} else if (FUNCTION_SETTING_APP == mFunction) {
					final Context tempContext = mApp.getContext();
					if (tempContext != null && tempContext instanceof UserSetupActivity) {
						((UserSetupActivity) tempContext).updateCallBack(FUNCTION_SETTING_APP, goluk);
					}

					if (goluk.equals("{}")) {
						// 设置页版本检测需要提示
						GolukUtils.showToast(mApp.getContext(), "当前已是最新版本");
					} else {
						appUpgradeUtils(goluk);
					}
				} else if (FUNCTION_SETTING_IPC == mFunction) {
					final Context tempContext = mApp.getContext();
					if (tempContext != null && tempContext instanceof UserSetupActivity) {
						((UserSetupActivity) tempContext).updateCallBack(FUNCTION_SETTING_IPC, json);
					}
					IPCInfo ipcInfo = ipcUpdateUtils(ipc);
					if (ipcInfo == null) {
						// ipc不需要升级 ----> 提示 ------> 结束
						GolukUtils.showToast(mApp.getContext(), "极路客固件当前已是最新版本");
					} else {
						/**
						 * ipc需要升级，前提是判断app是否需要升级 app不需要升级，直接下载并升级ipc
						 * app需要升级，设个年级app
						 */
						if (goluk.equals("{}")) {
							// APP不需要升级
							GolukDebugUtils.i("lily", "------goluk为空，不用进行升级------");
							// 提示下载并升级ipc

							final String localBinPath = this.getLocalFile(ipcInfo.version);
							if (null == localBinPath) {
								// TODO 提示用户下载文件Dialog
								ipcUpgrade(TYPE_DOWNLOAD, ipcInfo);
							} else {
								// TODO 弹框提示用户安装本地的文件 (Dialog)
								ipcUpgrade(TYPE_INSTALL, ipcInfo);
							}

						} else {
							appUpgradeUtils(goluk);
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			GolukDebugUtils.i(TAG, "-----网络链接超时---------" + codeOut);
			switch (codeOut) {
			case 1:
			case 2:
			case 3:
			default:
				GolukDebugUtils.i(TAG, "网络连接超时，请检查后重试");
				break;
			}
		}
		mFunction = -1;
	}

	/**
	 * 下载ipc文件
	 */
	public boolean download(String url, String path) {
		// {“url”:”http://www.baidu.com”,
		// “savePath”:”fs1:/update/ipc_upgrade_2015-04-30-15-58.bin”}
		String str = JsonUtil.ipcDownLoad(url, path);
		return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_CommDownloadFile, str);
	}

	/**
	 * 下载ipc文件回调 void* pvUser, int type ,int state , unsigned long param1,
	 * unsigned long param2 其中的state==2时，param1为下载进度数值（0~100）
	 * 
	 * @param state
	 * @param param1
	 * @param param2
	 */
	public void downloadCallback(int state, Object param1, Object param2) {
		GolukDebugUtils.i(TAG, "------------downloadCallback-----------");
		if (mApp.getContext() != null && mApp.getContext() instanceof UpdateActivity) {
			((UpdateActivity) mApp.getContext()).downloadCallback(state, param1, param2);
		}
	}

	/**
	 * 判断下载提示框是否正在显示
	 * 
	 * @return true/false 显示／未显示
	 * @author jyf
	 * @date 2015年6月25日
	 */
	private boolean isHasUpdateDialogShow() {
		if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
			return true;
		}
		return false;
	}

	private void dimissDownLoadDialog() {
		if (null != mDownloadDialog) {
			mDownloadDialog.dismiss();
			mDownloadDialog = null;
		}
	}

	/**
	 * type: 0/1 下载／安装 ipc升级
	 * 
	 * ipc下载文件
	 * 
	 * @param ipcInfo
	 * 
	 */
	public void ipcUpgrade(final int type, final IPCInfo ipcInfo) {
		GolukDebugUtils.i(TAG, "------------isConnect-----------" + mApp.isIpcLoginSuccess);
		final String msg = TYPE_DOWNLOAD == type ? "是否下载固件升级文件？" : "是否安装固件升级文件？";
		mDownloadDialog = new AlertDialog.Builder(mApp.getContext()).setMessage(msg)
				.setPositiveButton("", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// 跳转升级界面
						dimissDownLoadDialog();
						Intent intent = new Intent(mApp.getContext(), UpdateActivity.class);
						intent.putExtra("update_sign", type);
						intent.putExtra("update_data", ipcInfo);
						mApp.getContext().startActivity(intent);
					}
				}).setNegativeButton("取消", null).setCancelable(false).create();

		mDownloadDialog.show();
	}

	/**
	 * ipc升级解析
	 * 
	 * @param ipc
	 * @return
	 */
	public IPCInfo ipcUpdateUtils(JSONArray ipc) {
		/**
		 * APP不用升级的话，判断ipc是否需要升级 isnew 0不需要升级 1需要升级
		 */
		IPCInfo ipcInfo = null;
		IPCInfo[] upgradeArray = JsonUtil.upgradeJson(ipc);
		final int length = upgradeArray.length;
		for (int i = 0; i < length; i++) {
			String ipc_filesize = upgradeArray[i].filesize;
			String ipc_content = upgradeArray[i].appcontent;
			// 保存ipc文件大小
			mApp.mSharedPreUtil.saveIpcFileSize(ipc_filesize);
			// 保存ipc更新信息
			mApp.mSharedPreUtil.saveIpcContent(ipc_content);

			String ipc_isnew = upgradeArray[i].isnew;
			if ("0".equals(ipc_isnew)) {
				GolukDebugUtils.i(TAG, "ipc当前已是最新版本，不需要升级");
			} else if ("1".equals(ipc_isnew)) {
				GolukDebugUtils.i(TAG, "ipc需要升级");
				ipcInfo = upgradeArray[i];
			}
		}

		if (null != ipcInfo) {
			// IPC需要升级
			final String localBinPath = this.getLocalFile(ipcInfo.version);
			if (null == localBinPath) {
				// TODO 提示用户下载文件Dialog
				ipcUpgrade(TYPE_DOWNLOAD, ipcInfo);
			} else {
				// TODO 弹框提示用户安装本地的文件 (Dialog)
				ipcUpgrade(TYPE_INSTALL, ipcInfo);
			}

		}
		return ipcInfo;
	}

	/**
	 * app升级解析
	 * 
	 * @param goluk
	 */
	public void appUpgradeUtils(String goluk) {
		try {
			JSONObject jsonGoluk = new JSONObject(goluk);
			APPInfo appInfo = JsonUtil.appUpgradeJson(jsonGoluk);
			String appcontent = appInfo.appcontent;
			String url = appInfo.url;
			String isUpdate = appInfo.isupdate;
			/**
			 * APP升级 0非强制升级 1强制升级 非强制升级不退出程序，强制升级退出程序
			 */
			if (isUpdate.equals("1")) {
				mApp.mUpgrade.showUpgradeGoluk(mApp.getContext(), appcontent, url);
			} else if (isUpdate.equals("0")) {
				mApp.mUpgrade.showUpgradeGoluk2(mApp.getContext(), appcontent, url);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 不匹配升级提示
	 * 
	 * @param mContext
	 * @param message
	 * @param vIpc
	 */
	public void showUnMatchDialog(final Context mContext, String message, final String vIpc) {
		Builder mBuilder = new AlertDialog.Builder(mContext);
		AlertDialog dialog = mBuilder.setTitle("发现新版本").setMessage(message).setPositiveButton("稍后再说", null)
				.setNegativeButton("马上升级", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO 弹Loading框,提示正在检测
						requestInfo(FUNCTION_CONNECTIPC, vIpc);
					}
				}).setCancelable(false).setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							return true;
						}
						return false;
					}
				}).create();
		dialog.show();
	}

	/**
	 * ipc安装升级
	 */
	public void ipcInstall() {
		new AlertDialog.Builder(mApp.getContext()).setTitle("固件升级提示").setMessage("")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// 判断是否有升级文件
						boolean isHasFile = UserUtils.fileIsExists();
						if (isHasFile) {
							if (GolukApplication.getInstance().getIpcIsLogin()) {
								boolean u = GolukApplication.getInstance().getIPCControlManager().ipcUpgrade(BIN_PATH);
								if (u) {
									// 正在准备文件，请稍候……
									UpdateActivity.mUpdateHandler.sendEmptyMessage(UpdateActivity.UPDATE_PREPARE_FILE);
								}
							} else {
								// ipc未连接
								UpdateActivity.mUpdateHandler.sendEmptyMessage(UpdateActivity.UPDATE_IPC_UNUNITED);
							}
						} else {
							// 提示没有升级文件
							UpdateActivity.mUpdateHandler.sendEmptyMessage(UpdateActivity.UPDATE_FILE_NOT_EXISTS);
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * ipc安装升级回调
	 * 
	 * @param success
	 * @param param1
	 * @param param2
	 */
	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.i(TAG, "=========ipcInstallCallback===========");
		if (mApp.getContext() != null && mApp.getContext() instanceof UpdateActivity) {
			((UpdateActivity) mApp.getContext()).IPCManage_CallBack(event, msg, param1, param2);
		}
	}

	/**
	 * 关闭加载中对话框
	 */
	private void closeProgressDialog() {
		if (null != mCustomLoadingDialog) {
			mCustomLoadingDialog.close();
			mCustomLoadingDialog = null;
		}
	}

}
