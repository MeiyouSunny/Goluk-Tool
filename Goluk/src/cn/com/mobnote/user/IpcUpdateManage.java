package cn.com.mobnote.user;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.view.KeyEvent;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.UpdateActivity;
import cn.com.mobnote.golukmobile.UserSetupActivity;
import cn.com.mobnote.golukmobile.UserStartActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.api.FileUtils;
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

	/** BIN文件下载目录 */
	private static final String BIN_PATH_PRE = "fs1:/update";

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
	/** 保存当前正在下载的BIN文件版本信息 */
	public IPCInfo mDownLoadIpcInfo = null;
	private Builder mBuilder;
	private AlertDialog mAppUpdateDialog;

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
			GolukUtils.showToast(mApp.getContext(), "当前网络连接异常，请检查网络后重试");
			return false;
		} else {
			if (isHasUpdateDialogShow()) {
				// 上次检查结果的Dialog还存在，不进行下次操作
				return false;
			}
			this.cancelHttpRequest();
			String ipcString = JsonUtil.putIPC(VERSION_PATH, vipc);
			boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_CheckUpgrade, ipcString);
			GolukDebugUtils.i(TAG, "=====" + b + "===ipcUpdateManage======");
			if (b) {
				mFunction = function;
				if (mFunction == FUNCTION_SETTING_APP || mFunction == FUNCTION_SETTING_IPC) {
					showLoadingDialog();
				}
			} else {
				mFunction = -1;
			}
			return b;
		}
	}

	public void showLoadingDialog() {
		dimissLoadingDialog();
		if (null == mCustomLoadingDialog) {
			mCustomLoadingDialog = new CustomLoadingDialog(this.mApp.getContext(), "检测中，请稍候……");
		}
		mCustomLoadingDialog.show();
	}

	public void dimissLoadingDialog() {
		if (null != mCustomLoadingDialog) {
			mCustomLoadingDialog.close();
			mCustomLoadingDialog = null;
		}
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
		String fileInfo = mApp.mSharedPreUtil.getIpcLocalFileInfo();
		if (null == fileInfo || "".equals(fileInfo)) {
			return null;
		}
		IPCInfo ipcInfo = JsonUtil.getSingleIPCInfo(fileInfo);
		if (null == ipcInfo) {
			return null;
		}
		if (ipcVersion.equals(ipcInfo.version)) {
			try {
				final String filePath = BIN_PATH_PRE + "/" + ipcInfo.version + ".bin";
				final String abFilePath = FileUtils.libToJavaPath(filePath);
				File file = new File(abFilePath);
				if (file.exists()) {
					return filePath;
				}
			} catch (Exception e) {

			}
		}
		return null;
	}

	/**
	 * 获取下载的文件路径 (下载前获取一次，升级安装时获取一次)
	 * 
	 * @param filename
	 * @return
	 * @author jyf
	 * @date 2015年6月25日
	 */
	public String getBinFilePath(String filename) {
		return BIN_PATH_PRE + "/" + filename + ".bin";
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
		int codeOut = (Integer) outTime;
		dimissLoadingDialog();
		if (1 == success) {
			try {
				String dataObj = (String) obj;
				JSONObject json = new JSONObject(dataObj);
				GolukDebugUtils.i(TAG, "------upgradeGoluk---" + json);
				String data = json.getString("data");
				JSONObject jsonData = new JSONObject(data);

				final String goluk = jsonData.getString("goluk");
				JSONArray ipc = jsonData.getJSONArray("ipc");
				// 保存ipc匹配信息
				mApp.mSharedPreUtil.saveIPCMatchInfo(ipc.toString());

				GolukDebugUtils.i(TAG, "===ipc----goluk=------" + goluk);

				if (FUNCTION_AUTO == mFunction) {

					if (goluk.equals("{}")) {
						// APP不需要升级，判断ipc是否需要升级
						IPCInfo ipcInfo = ipcUpdateUtils(ipc);
						ipcUpgradeNext(ipcInfo);
					} else {
						appUpgradeUtils(goluk);
					}

				} else if (FUNCTION_CONNECTIPC == mFunction) {
					// ipc连接后不匹配
					if (goluk.equals("{}")) {
						// APP不需要升级，判断ipc是否需要升级
						IPCInfo ipcInfo = ipcUpdateUtils(ipc);
						ipcUpgradeNext(ipcInfo);
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
					SharedPreferences preferences = mApp.getContext().getSharedPreferences("ipc_wifi_bind",
							mApp.getContext().MODE_PRIVATE);
					boolean isbind = preferences.getBoolean("isbind", false);

					IPCInfo ipcInfo = ipcUpdateUtils(ipc);
					if (ipcInfo == null) {
						// ipc不需要升级
						if (!mApp.isIpcLoginSuccess && !isbind) {
							GolukUtils.showToast(mApp.getContext(), "您好像没有连接摄像头哦");
						} else {
							String version_new = jsonData.getString("version");
							GolukUtils.showToast(mApp.getContext(), "极路客固件版本号" + version_new + "，当前已是最新版本");
						}
					} else {
						/**
						 * ipc需要升级，前提是判断app是否需要升级 app不需要升级，直接下载并升级ipc
						 * app需要升级，设个年级app
						 */
						if (goluk.equals("{}")) {
							// APP不需要升级
							// 提示下载并升级ipc
							final String localBinPath = this.getLocalFile(ipcInfo.version);
							if (null == localBinPath) {
								// 提示用户下载文件Dialog
								ipcUpgrade(TYPE_DOWNLOAD, ipcInfo, ipcInfo.appcontent);
							} else {
								// 弹框提示用户安装本地的文件 (Dialog)
								ipcUpgrade(TYPE_INSTALL, ipcInfo, ipcInfo.appcontent);
							}
						} else {
							String clcik_version = "";
							String matchInfo = mApp.mSharedPreUtil.getIPCMatchInfo();
							JSONArray jsonArray = new JSONArray(matchInfo);

							IPCInfo[] upgradeArray = JsonUtil.upgradeJson(jsonArray);
							int length = 0;
							if (null == upgradeArray) {
								length = 0;
							} else {
								length = upgradeArray.length;
							}
							for (int i = 0; i < length; i++) {
								clcik_version = upgradeArray[i].version;
							}
							new AlertDialog.Builder(mApp.getContext()).setTitle("升级提示")
									.setMessage("发现新极路客固件版本" + clcik_version + "，为了正常升级新固件，请先下载最新的APP后再试。")
									.setPositiveButton("确定", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											appUpgradeUtils(goluk);
										}
									}).show();
						}
					}

				}

			} catch (Exception e) {
				if (FUNCTION_AUTO != mFunction) {
					// GolukUtils.showToast(mApp.getContext(), "没有ipc匹配列表");
					GolukUtils.showToast(mApp.getContext(), "极路客固件版本号" + mApp.mSharedPreUtil.getIPCVersion()
							+ "，当前已是最新版本");
				}
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
	public boolean isHasUpdateDialogShow() {
		if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
			return true;
		}
		if (null != mAppUpdateDialog && mAppUpdateDialog.isShowing()) {
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

	private void dimissAppDialog() {
		if (null != mAppUpdateDialog) {
			mAppUpdateDialog.dismiss();
			mAppUpdateDialog = null;
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
	public void ipcUpgrade(final int type, final IPCInfo ipcInfo, String message) {
		GolukDebugUtils.i(TAG, "------------isConnect-----------" + mApp.isIpcLoginSuccess);
		final String msg = TYPE_DOWNLOAD == type ? "发现新极路客固件版本，是否下载升级？" : message;
		mDownloadDialog = new AlertDialog.Builder(mApp.getContext()).setTitle("固件升级提示").setMessage(msg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// 跳转升级界面
						dimissDownLoadDialog();
						Intent intent = new Intent(mApp.getContext(), UpdateActivity.class);
						intent.putExtra(UpdateActivity.UPDATE_SIGN, type);
						intent.putExtra(UpdateActivity.UPDATE_DATA, ipcInfo);
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

		return ipcInfo;
	}

	/**
	 * 检测ipc需要升级后，进行升级
	 * 
	 * @param ipcInfo
	 */
	public void ipcUpgradeNext(IPCInfo ipcInfo) {
		if (null != ipcInfo) {
			// IPC需要升级
			final String localBinPath = this.getLocalFile(ipcInfo.version);
			if (null == localBinPath) {
				// TODO 提示用户下载文件Dialog
				ipcUpgrade(TYPE_DOWNLOAD, ipcInfo, ipcInfo.appcontent);
			} else {
				// TODO 弹框提示用户安装本地的文件 (Dialog)
				ipcUpgrade(TYPE_INSTALL, ipcInfo, ipcInfo.appcontent);
			}

		}
	}

	/**
	 * 下载BIN文件成功
	 * 
	 * @author jyf
	 * @date 2015年6月25日
	 */
	public void downIpcSucess() {
		final String path = FileUtils.libToJavaPath(BIN_PATH_PRE);
		// 遍历文件夹，删除其它安装包
		try {
			if (null == mDownLoadIpcInfo) {
				return;
			}
			File fileDir = new File(path);
			if (!fileDir.isDirectory()) {
				return;
			}
			File[] allFile = fileDir.listFiles();
			if (null != allFile && 0 < allFile.length) {
				for (int i = 0; i < allFile.length; i++) {
					if (!allFile[i].getName().contains(mDownLoadIpcInfo.version)) {
						// 需要把文件删除
						allFile[i].delete();
					}
				}
			}
			if (null == mDownLoadIpcInfo) {
				return;
			}
			// 更新当前信息
			final String downInfo = JsonUtil.getSingleIPCInfoJson(mDownLoadIpcInfo);
			if (null != downInfo) {
				mApp.mSharedPreUtil.setIpcLocalFileInfo(downInfo);
			}
			// 当前文件

			mDownLoadIpcInfo = null;

		} catch (Exception e) {
			e.printStackTrace();
		}

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
				showUpgradeGoluk(mApp.getContext(), appcontent, url);
			} else if (isUpdate.equals("0")) {
				showUpgradeGoluk2(mApp.getContext(), appcontent, url);
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
		AlertDialog dialog = mBuilder.setTitle("升级提示").setMessage(message).setPositiveButton("稍后再说", null)
				.setNegativeButton("马上升级", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO 弹Loading框,提示正在检测
						showLoadingDialog();
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
	public boolean ipcInstall(String filePath) {
		// 判断网络是否连接
		if (!UserUtils.isNetDeviceAvailable(mApp.getContext())) {
			GolukUtils.showToast(mApp.getContext(), "当前网络连接异常，请检查网络后重试");
			return false;
		} else {
			// 判断是否绑定
			@SuppressWarnings("static-access")
			SharedPreferences preferences = mApp.getContext().getSharedPreferences("ipc_wifi_bind",
					mApp.getContext().MODE_PRIVATE);
			boolean isbind = preferences.getBoolean("isbind", false);
			if (isbind) {
				return update(filePath);
			} else {
				// 判断摄像头是否连接
				if (GolukApplication.getInstance().getIpcIsLogin()) {
					return update(filePath);
				} else {
					UpdateActivity.mUpdateHandler.sendEmptyMessage(UpdateActivity.UPDATE_IPC_UNUNITED);
					return false;
				}
			}

		}
	}

	/**
	 * 升级流程
	 * 
	 * @param filePath
	 * @return
	 */
	public boolean update(String filePath) {
		boolean u = false;
		// 当前的版本号
		String current_version = mApp.mSharedPreUtil.getIPCVersion();
		// 获取升级ipc版本号
		try {
			String matchInfo = mApp.mSharedPreUtil.getIPCMatchInfo();
			JSONArray jsonArray = new JSONArray(matchInfo);
			IPCInfo[] upgradeArray = JsonUtil.upgradeJson(jsonArray);
			int length = 0;
			if (null == upgradeArray) {
				length = 0;
			} else {
				length = upgradeArray.length;
			}
			for (int i = 0; i < length; i++) {
				String update_version = upgradeArray[i].version;
				if (current_version.equals(update_version)) {
					GolukUtils.showToast(mApp.getContext(), "极路客固件版本号" + current_version + "，当前已是最新版本");
				} else {
					// 判断是否有升级文件
					boolean isHasFile = UserUtils.fileIsExists(filePath);
					if (isHasFile) {
						u = GolukApplication.getInstance().getIPCControlManager().ipcUpgrade(filePath);
						if (u) {
							// 正在准备文件，请稍候……
							UpdateActivity.mUpdateHandler.sendEmptyMessage(UpdateActivity.UPDATE_PREPARE_FILE);
						}
						return u;
					} else {
						// 提示没有升级文件
						UpdateActivity.mUpdateHandler.sendEmptyMessage(UpdateActivity.UPDATE_FILE_NOT_EXISTS);
						return false;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return u;
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
	 * 强制升级提示
	 * 
	 * @param mContext
	 * @param message1
	 * @param message2
	 */
	public void showUpgradeGoluk(final Context mContext, String message, final String url) {
		mBuilder = new AlertDialog.Builder(mContext);
		mAppUpdateDialog = mBuilder.setTitle("APP升级提示").setMessage(message)
				.setPositiveButton("马上下载", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dimissAppDialog();
						// 浏览器打开url
						GolukUtils.openUrl(url, mContext);

						if (GolukApplication.mMainActivity != null) {
							GolukApplication.mMainActivity.finish();
							GolukApplication.mMainActivity = null;
						}
						SysApplication.getInstance().exit();

						mApp.mIPCControlManager.setIPCWifiState(false, "");
						mApp.mGoluk.GolukLogicDestroy();
						if (null != UserStartActivity.mHandler) {
							UserStartActivity.mHandler.sendEmptyMessage(UserStartActivity.EXIT);
						}
						int PID = android.os.Process.myPid();
						android.os.Process.killProcess(PID);
						System.exit(0);

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
		mAppUpdateDialog.show();
	}

	/**
	 * 非强制升级提示
	 * 
	 * @param mContext
	 * @param message1
	 * @param message2
	 */
	public void showUpgradeGoluk2(final Context mContext, String message, final String url) {
		mBuilder = new AlertDialog.Builder(mContext);
		mAppUpdateDialog = mBuilder.setTitle("APP升级提示").setMessage(message)
				.setPositiveButton("稍后再说", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dimissAppDialog();
					}
				}).setNegativeButton("马上下载", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dimissAppDialog();
						// 浏览器打开url
						GolukUtils.openUrl(url, mContext);
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
		mAppUpdateDialog.show();
	}

	/**
	 * 升级停止
	 */
	public boolean stopIpcUpgrade() {
		GolukDebugUtils.i("lily", "---------stopIpcUpgrade()------" + IPC_VDCPCmd_StopIPCUpgrade);
		return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_StopIPCUpgrade, "");
	}

}
