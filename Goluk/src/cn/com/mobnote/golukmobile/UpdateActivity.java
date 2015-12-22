package cn.com.mobnote.golukmobile;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventIPCUpdate;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.user.DataCleanManage;
import cn.com.mobnote.user.IPCInfo;
import cn.com.mobnote.user.IpcUpdateManage;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.SharedPrefUtil;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 升级下载安装
 * 
 * @author mobnote
 *
 */
public class UpdateActivity extends BaseActivity implements OnClickListener, IPCManagerFn, OnTouchListener {

	/** 返回按钮 **/
	private ImageButton mBtnBack = null;
	/** 下载 / 安装按钮 **/
	private Button mBtnDownload = null;
	/** 极路客固件版本号 **/
	private TextView mTextIpcVersion = null;
	/** 极路客固件大小 **/
	private TextView mTextIpcSize = null;
	/** 更新说明 **/
	private TextView mTextUpdateContent = null;
	/** 未下载 / 下载中 / 已下载 **/
	private TextView mTextDowload = null;
	/** 升级内容显示 **/
	private ScrollView mScrollView = null;
	/** 升级成功最新版本提示图片 **/
	private ImageView mUpdateNewImage = null;
	/** 升级成功最新版本提示文字 **/
	private TextView mUpdateNewText = null;
	/** 升级过程中提示不要断电图片 **/
	private ImageView mNoBreakImage = null;
	/** 升级过程中提示不要断点文字 **/
	private TextView mNoBreakText = null;
	/** GolukApplication **/
	private GolukApplication mApp = null;

	/** 0 下载 1安装 **/
	public final static String UPDATE_SIGN = "update_sign";
	/** 数据展示 **/
	public final static String UPDATE_DATA = "update_data";
	/** 升级文件下载中 **/
	public final static String UPDATE_PROGRESS = "update_progress";
	/** 是否为最新版本 **/
	public final static String UPDATE_IS_NEW = "update_is_new";

	/** 0下载 / 1安装的标志 **/
	private int mSign = 0;
	/** 数据 **/
	private IPCInfo mIpcInfo = null;

	/** ipc安装升级中更新UI显示 **/
	private String stage = "";
	private String percent = "";
	private Handler mUpdateHandler = null;
	private Timer mTimer = null;

	/** 文件不存在 **/
	// public static final int UPDATE_FILE_NOT_EXISTS = 10;
	/** 准备文件 **/
	// public static final int UPDATE_PREPARE_FILE = 11;
	/** 传输文件 **/
	public static final int UPDATE_TRANSFER_FILE = 12;
	/** 文件传输成功 **/
	public static final int UPDATE_TRANSFER_OK = 13;
	/** 正在升级 **/
	public static final int UPDATE_UPGRADEING = 14;
	/** 升级成功 **/
	public static final int UPDATE_UPGRADE_OK = 15;
	/** 升级失败 **/
	public static final int UPDATE_UPGRADE_FAIL = 16;
	/** 校验不通过 **/
	public static final int UPDATE_UPGRADE_CHECK = 17;
	/** ipc未连接 **/
	// public static final int UPDATE_IPC_UNUNITED = 18;
	/** ipc连接断开 **/
	public static final int UPDATE_IPC_DISCONNECT = 19;
	/** 升级1阶段摄像头断开连接 **/
	public static final int UPDATE_IPC_FIRST_DISCONNECT = 20;
	/** 升级2阶段摄像头断开连接 **/
	public static final int UPDATE_IPC_SECOND_DISCONNECT = 21;
	/** 下载状态 **/
	private int downloadStatus = 0;

	/** 传输文件 */
	private AlertDialog mSendDialog = null;
	/** 传输文件成功 **/
	private AlertDialog mSendOk = null;
	/** 正在升级中 */
	private AlertDialog mUpdateDialog = null;
	/** 升级成功 **/
	private AlertDialog mUpdateDialogSuccess = null;
	/** 升级失败 **/
	private AlertDialog mUpdateDialogFail = null;
	/** 升级准备中 **/
	private AlertDialog mPrepareDialog = null;
	/** 升级1阶段摄像头断开 **/
	private AlertDialog mFirstDialog = null;
	/** 升级2阶段摄像头断开 **/
	private AlertDialog mSecondDialog = null;

	private String ipc_version = "";
	private String ipc_size = "";
	private String ipc_content = "";
	private String ipc_url = "";
	private String ipc_path = "";
	/** true为已退出当前activity **/
	private boolean isExit = false;
	/** 回调中返回ipc是否断开标识／升级成功后ipc断开连接标识 **/
	private boolean mIsDisConnect = false;
	private boolean mIsSendFileOk = false;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.upgrade_layout);

		mApp = (GolukApplication) getApplication();
		initView();
		isExit = false;

		Intent it = getIntent();
		boolean isNew = it.getBooleanExtra(UPDATE_IS_NEW, false);
		if (isNew) {
			isNewVersion();
			return;
		}
		mSign = it.getIntExtra(UPDATE_SIGN, 0);
		mIpcInfo = (IPCInfo) it.getSerializableExtra(UPDATE_DATA);

		GolukDebugUtils.e("aaa", "=====mApp.mIpcInfo=====" + mIpcInfo);

		if (null != mIpcInfo) {
			SharedPrefUtil.saveIPCDownVersion(mIpcInfo.version);
			SharedPrefUtil.saveIpcFileSize(mIpcInfo.filesize);
			SharedPrefUtil.saveIpcContent(mIpcInfo.appcontent);
			SharedPrefUtil.saveIPCURL(mIpcInfo.url);
			SharedPrefUtil.saveIPCPath(mIpcInfo.path);
		}

		ipc_version = SharedPrefUtil.getIPCDownVersion();
		ipc_size = SharedPrefUtil.getIPCFileSize();
		ipc_content = SharedPrefUtil.getIPCContent();
		ipc_url = SharedPrefUtil.getIPCURL();
		ipc_path = SharedPrefUtil.getIPCPath();

		mTextIpcVersion.setText(ipc_version);
		String size = DataCleanManage.getFormatSize(Double.parseDouble(ipc_size));
		mTextIpcSize.setText(size);
		mTextUpdateContent.setText(ipc_content);

		Intent itClick = getIntent();
		int progressSetup = itClick.getIntExtra(UPDATE_PROGRESS, 0);

		GolukDebugUtils.i("", "----UpdateActivity----mSign-----" + mSign);
		GolukDebugUtils.i("", "----UpdateActivity----mApp.mLoadStatus-----" + mApp.mLoadStatus);
		GolukDebugUtils.i("", "----UpdateActivity----progressSetup-----" + progressSetup);
		if (mSign == 0) {
			if (mApp.mLoadStatus) {
				mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
				if (!UserUtils.isNetDeviceAvailable(this)) {
					GolukUtils.showToast(mApp.getContext(), "很抱歉，新极路客固件下载失败，请检查网络后重试");
					mTextDowload.setText("未下载");
					mBtnDownload.setText("下载新极路客固件程序");
					downloadStatus = IpcUpdateManage.DOWNLOAD_STATUS_FAIL;
					mBtnDownload.setBackgroundResource(R.drawable.icon_login);
					mBtnDownload.setEnabled(true);
				} else {
					mTextDowload.setText("下载中");
					mBtnDownload.setText("下载中…" + progressSetup + "%");
					downloadStatus = IpcUpdateManage.DOWNLOAD_STATUS;
					mBtnDownload.setBackgroundResource(R.drawable.icon_more);
					mBtnDownload.setEnabled(false);
				}
			} else {
				boolean b = mApp.mIpcUpdateManage.download(ipc_url, ipc_version);
				GolukDebugUtils.i("", "----UpdateActivity----download-----b：" + b);
				if (b) {
					mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
					mTextDowload.setText("下载中");
					mBtnDownload.setText("下载中…0%");
					downloadStatus = IpcUpdateManage.DOWNLOAD_STATUS;
					mBtnDownload.setBackgroundResource(R.drawable.icon_more);
					mBtnDownload.setEnabled(false);
				} else {
					mTextDowload.setText("未下载");
					mBtnDownload.setText("下载新极路客固件程序");
					downloadStatus = IpcUpdateManage.DOWNLOAD_STATUS_FAIL;
					mBtnDownload.setBackgroundResource(R.drawable.icon_login);
					mBtnDownload.setEnabled(true);
				}
			}
		} else if (mSign == 1) {
			mTextDowload.setText("已下载");
			mBtnDownload.setText("安装此极路客固件程序");
			mBtnDownload.setBackgroundResource(R.drawable.icon_login);
			mBtnDownload.setEnabled(true);
		}
		EventBus.getDefault().register(this);

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_TRANSFER_FILE:
					GolukDebugUtils.i("lily", "-------正在传输文件------");
					UserUtils.dismissUpdateDialog(mPrepareDialog);
					mIsDisConnect = true;
					mPrepareDialog = null;
					if (isExit) {
						return;
					}
					mIsDisConnect = true;
					if (mSendDialog == null) {
						mSendDialog = UserUtils.showDialogUpdate(UpdateActivity.this, "开始升级，过程可能需要几分钟，" + "\n"
								+ "请不要关闭摄像头电源……" + "\n" + "升级1阶段：" + percent + "%");
					} else {
						mSendDialog.setMessage("开始升级，过程可能需要几分钟，" + "\n" + "请不要关闭摄像头电源……" + "\n" + "升级1阶段：" + percent
								+ "%");
					}
					break;
				case UPDATE_TRANSFER_OK:
					UserUtils.dismissUpdateDialog(mSendDialog);
					mSendDialog = null;
					if (isExit) {
						return;
					}
					mSendOk = UserUtils.showDialogUpdate(UpdateActivity.this, "文件传输成功，正在为您准备升级");
					mIsSendFileOk = true;
					break;
				case UPDATE_UPGRADEING:
					UserUtils.dismissUpdateDialog(mSendOk);
					mSendOk = null;
					if (isExit) {
						return;
					}
					mIsDisConnect = true;
					if (mUpdateDialog == null) {
						mUpdateDialog = UserUtils.showDialogUpdate(UpdateActivity.this, "开始升级，过程可能需要几分钟，" + "\n"
								+ "请不要关闭摄像头电源……" + "\n" + "升级2阶段：" + percent + "%");
					} else {
						mUpdateDialog.setMessage("开始升级，过程可能需要几分钟，" + "\n" + "请不要关闭摄像头电源……" + "\n" + "升级2阶段：" + percent
								+ "%");
					}
					break;
				case UPDATE_UPGRADE_OK:
					mApp.mIpcUpdateManage.stopIpcUpgrade();
					UserUtils.dismissUpdateDialog(mUpdateDialog);
					mUpdateDialog = null;
					if (isExit) {
						return;
					}
					UserUtils
							.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, "恭喜您，极路客固件升级成功，正在重新启动，请稍候……");
					isNewVersion();
					break;
				case UPDATE_UPGRADE_FAIL:
					mApp.mIpcUpdateManage.stopIpcUpgrade();
					timerCancel();
					UserUtils.dismissUpdateDialog(mPrepareDialog);
					UserUtils.dismissUpdateDialog(mSendDialog);
					UserUtils.dismissUpdateDialog(mUpdateDialog);
					mPrepareDialog = null;
					mSendDialog = null;
					mUpdateDialog = null;
					mFirstDialog = null;
					mSecondDialog = null;
					if (isExit) {
						return;
					}
					mIsDisConnect = true;
					UserUtils.showUpdateSuccess(mUpdateDialogFail, UpdateActivity.this, "很抱歉，升级失败。请您重试。");
					mNoBreakImage.setVisibility(View.GONE);
					mNoBreakText.setVisibility(View.GONE);
					break;
				case UPDATE_UPGRADE_CHECK:
					if (isExit) {
						return;
					}
					UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, "校验不通过");
					mNoBreakImage.setVisibility(View.GONE);
					mNoBreakText.setVisibility(View.GONE);
					break;
				case UPDATE_IPC_DISCONNECT:
					timerCancel();
					UserUtils.dismissUpdateDialog(mPrepareDialog);
					UserUtils.dismissUpdateDialog(mSendDialog);
					mPrepareDialog = null;
					mUpdateDialog = null;
					if (isExit) {
						return;
					}
					mIsDisConnect = true;
					UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, "摄像头断开连接，请检查后重试");
					mNoBreakImage.setVisibility(View.GONE);
					mNoBreakText.setVisibility(View.GONE);
					break;
				case UPDATE_IPC_FIRST_DISCONNECT:
					timerCancel();
					UserUtils.dismissUpdateDialog(mPrepareDialog);
					UserUtils.dismissUpdateDialog(mSendDialog);
					mPrepareDialog = null;
					mSendDialog = null;
					mApp.mIpcUpdateManage.stopIpcUpgrade();
					mIsDisConnect = true;
					showUpdateFirstDisconnect("很抱歉，升级失败，请先不要关闭摄像头电源，等待摄像头重新启动后再试。");
					mNoBreakImage.setVisibility(View.GONE);
					mNoBreakText.setVisibility(View.GONE);
					break;
				case UPDATE_IPC_SECOND_DISCONNECT:
					timerCancel();
					UserUtils.dismissUpdateDialog(mUpdateDialog);
					mUpdateDialog = null;
					mApp.mIpcUpdateManage.stopIpcUpgrade();
					mIsDisConnect = true;
					showUpdateSecondDisconnect("很抱歉，摄像头连接异常中断，但它可能仍在升级中。请先不要关闭摄像头电源，等待摄像头升级成功。");
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	public void onEventMainThread(EventIPCUpdate event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.UPDATE_FILE_NOT_EXISTS:
			if (isExit) {
				return;
			}
			mIsDisConnect = true;
			UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this,
					getString(R.string.str_update_file_not_exist));
			mNoBreakImage.setVisibility(View.GONE);
			mNoBreakText.setVisibility(View.GONE);
			break;
		case EventConfig.UPDATE_PREPARE_FILE:
			if (isExit) {
				return;
			}
			mIsDisConnect = false;
			mPrepareDialog = UserUtils.showDialogUpdate(UpdateActivity.this,
					getString(R.string.str_update_prepare_file));
			mNoBreakImage.setVisibility(View.VISIBLE);
			mNoBreakText.setVisibility(View.VISIBLE);
			break;
		case EventConfig.UPDATE_IPC_UNUNITED:
			if (isExit) {
				return;
			}
			mIsDisConnect = true;
			UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this,
					getString(R.string.str_update_ipc_ununited));
			mNoBreakImage.setVisibility(View.GONE);
			mNoBreakText.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mApp.setContext(this, "Update");

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("carupgrade", this);
		}
	}

	// 初始化view
	public void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.back_btn);
		mBtnDownload = (Button) findViewById(R.id.update_btn);
		mTextIpcVersion = (TextView) findViewById(R.id.upgrade_ipc_name);
		mTextIpcSize = (TextView) findViewById(R.id.upgrade_ipc_size_text);
		mTextUpdateContent = (TextView) findViewById(R.id.update_info_content);
		mTextDowload = (TextView) findViewById(R.id.upgrade_ipc_size_download);
		mScrollView = (ScrollView) findViewById(R.id.sv_upgrade_body);
		mUpdateNewImage = (ImageView) findViewById(R.id.iv_upgrade_new);
		mUpdateNewText = (TextView) findViewById(R.id.tv_upgrade_new_text);
		mNoBreakImage = (ImageView) findViewById(R.id.iv_upgrade_nobreak_image);
		mNoBreakText = (TextView) findViewById(R.id.tv_upgrade_nobreak_text);

		// 监听
		mBtnBack.setOnClickListener(this);
		mBtnDownload.setOnClickListener(this);
		mBtnDownload.setOnTouchListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			exit();
			break;
		case R.id.update_btn:
			// 下载 / 升级
			if (mSign == 0) {
				if (IpcUpdateManage.DOWNLOAD_STATUS_FAIL == downloadStatus) {
					mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
					mTextDowload.setText("下载中");
					boolean b = mApp.mIpcUpdateManage.download(ipc_url, ipc_version);
					GolukDebugUtils.i("qqq", "----path------" + IpcUpdateManage.BIN_PATH_PRE + "/" + ipc_version
							+ ".bin");
					if (b) {
						mApp.mIpcUpdateManage.showLoadingDialog();
					} else {
						mApp.mIpcUpdateManage.dimissLoadingDialog();
					}
				}
			} else if (mSign == 1) {
				// TODO 判断摄像头是否连接 判断是否是最新版本
				if (!mApp.getIpcIsLogin()) {
					if (isExit) {
						return;
					}
					UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, this.getResources()
							.getString(R.string.update_no_connect_ipc_hint));
				} else {
					String version = SharedPrefUtil.getIPCVersion();
					GolukDebugUtils.i("lily", "-------version-----" + version + "------ipc_version-----" + ipc_version);
					if (version.equals(ipc_version)) {
						// GolukUtils.showToast(mApp.getContext(), "极路客固件版本号" +
						// version + "，当前已是最新版本");
						isNewVersion();
					} else {
						String file = mApp.mIpcUpdateManage.isHasIPCFile(ipc_version);
						boolean b = mApp.mIpcUpdateManage.ipcInstall(file);
					}
				}
			}
			break;
		default:
			break;
		}
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
		GolukDebugUtils.i("lily", "---UpdateActivity---------downloadCallback-----------state：" + state + "----param1："
				+ param1);
		mApp.mIpcUpdateManage.dimissLoadingDialog();
		downloadStatus = state;
		if (state == IpcUpdateManage.DOWNLOAD_STATUS) {
			// 下载中
			mApp.mLoadStatus = true;
			int progress = (Integer) param1;
			GolukDebugUtils.i("lily", "======下载文件progress=====" + progress);
			mBtnDownload.setBackgroundResource(R.drawable.icon_more);
			mBtnDownload.setEnabled(false);
			mBtnDownload.setText("正在下载…" + progress + "%");
			// 保存进度
			mApp.mLoadProgress = progress;
		} else if (state == IpcUpdateManage.DOWNLOAD_STATUS_SUCCESS) {
			// 下载成功
			mApp.mLoadStatus = false;
			mTextDowload.setText("已下载");
			mBtnDownload.setText("安装此极路客固件程序");
			mBtnDownload.setBackgroundResource(R.drawable.icon_login);
			mBtnDownload.setEnabled(true);
			mSign = 1;
			try {
				JSONObject json = new JSONObject((String) param2);
				String filePath = json.getString("filepath");
				GolukDebugUtils.i("lily", "---UpdateActivity---------downloadCallback-----------filePath：" + filePath);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// 下载成功删除文件
			// mApp.mIpcUpdateManage.downIpcSucess();
		} else if (state == IpcUpdateManage.DOWNLOAD_STATUS_FAIL) {
			// 下载失败
			mApp.mLoadStatus = false;
			GolukUtils.showToast(mApp.getContext(), "很抱歉，新极路客固件下载失败，请检查网络后重试");
			mTextDowload.setText("未下载");
			mBtnDownload.setText("下载新极路客固件程序");
			mBtnDownload.setBackgroundResource(R.drawable.icon_login);
			mBtnDownload.setEnabled(true);
			mSign = 0;
		}
	}

	/**
	 * ipc安装升级回调
	 * 
	 * @param event
	 * @param msg
	 * @param param1
	 * @param param2
	 */
	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("lily", "lily====IPC_VDCP_Msg_IPCUpgrade====msg=" + msg + "===param1=" + param1 + "==param2="
				+ param2 + "--------event-----" + event);
		if (isExit) {
			return;
		}
		if (mApp.mLoadStatus) {
			return;
		}
		if (event == ENetTransEvent_IPC_UpGrade_Resp) {
			if (IPC_VDCP_Msg_IPCUpgrade == msg) {
				GolukDebugUtils.e("lily", "---------连接ipc-------");
				if (param1 == RESULE_SUCESS) {
					mIsDisConnect = false;
					String str = (String) param2;
					GolukDebugUtils.i("lily", "--str----" + str);
					if (TextUtils.isEmpty(str)) {
						return;
					}
					try {
						JSONObject json = new JSONObject(str);
						stage = json.getString("stage");
						percent = json.getString("percent");
						GolukDebugUtils.i("lily", "---------stage-----" + stage + "-------percent----" + percent);
						if (stage.equals("1")) {
							// 正在传输文件，请稍候……
							mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_FILE);
							if (percent.equals("100")) {
								timerCancel();
								// 传输文件成功
								mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_OK);
								timerTaskOne();
							} else {
								timerTaskOne();
							}
						}
						if (stage.equals("2")) {
							// 开始升级，可能需要几分钟，请不要给摄像头断电。
							mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADEING);
							GolukDebugUtils.i("lily", "------------percent-------111111");
							if (!percent.equals("95") && !percent.equals("100")) {
								GolukDebugUtils.i("lily", "------------percent-------2222");
								timerTaskTwo();
							} else {
								GolukDebugUtils.i("lily", "------------percent-------33333");
								timerCancel();
								mApp.updateSuccess = true;
								// 升级成功
								mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_OK);
								mApp.mIpcUpdateManage.mParam1 = -1;
							}
						}
						if (stage.equals("3")) {
							if (percent.equals("-1")) {
								mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_CHECK);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (!(null != mFirstDialog && mFirstDialog.isShowing())
							|| !(null != mSecondDialog && mSecondDialog.isShowing())) {
						mApp.updateSuccess = false;
						// 升级失败
						GolukDebugUtils.e("uuuu", "---------param1!=0--------");
						mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_FAIL);
					}
				}
			}
		} else if (event == ENetTransEvent_IPC_VDCP_ConnectState) {
			if (mIsSendFileOk) {
				return;
			}
			if (mIsDisConnect) {
				return;
			}
			if (ConnectionStateMsg_DisConnected == msg) {
				UserUtils.dismissUpdateDialog(mPrepareDialog);
				UserUtils.dismissUpdateDialog(mUpdateDialogSuccess);
				UserUtils.dismissUpdateDialog(mSendDialog);
				UserUtils.dismissUpdateDialog(mSendOk);
				UserUtils.dismissUpdateDialog(mUpdateDialog);
				UserUtils.dismissUpdateDialog(mUpdateDialogFail);
				UserUtils.dismissUpdateDialog(mFirstDialog);
				UserUtils.dismissUpdateDialog(mSendDialog);
				mPrepareDialog = null;
				mUpdateDialogSuccess = null;
				mSendDialog = null;
				mSendOk = null;
				mUpdateDialog = null;
				mUpdateDialogFail = null;
				mFirstDialog = null;
				mSendDialog = null;
				UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this,
						this.getResources().getString(R.string.update_no_connect_ipc_hint));
				mNoBreakImage.setVisibility(View.VISIBLE);
				mNoBreakText.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 升级一阶段 超时时间１分钟 固件升级过程中超时 1000x60=60000
	 */
	private void timerTaskOne() {
		timerCancel();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// ipc断开
				if (stage.equals("1")) {
					if (null == mUpdateDialogFail || !mUpdateDialogFail.isShowing()) {
						mUpdateHandler.sendEmptyMessage(UPDATE_IPC_FIRST_DISCONNECT);
					}
				}
			}
		}, 60000);
	}

	/**
	 * 升级一阶段 超时时间３分钟 固件升级过程中超时 1000x60x3=180000
	 */
	private void timerTaskTwo() {
		timerCancel();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// ipc断开
				if (stage.equals("2") && !percent.equals("100")) {
					if (null == mUpdateDialogFail || !mUpdateDialogFail.isShowing()) {
						mUpdateHandler.sendEmptyMessage(UPDATE_IPC_SECOND_DISCONNECT);
					}
				}
			}
		}, 180000);
	}

	public void timerCancel() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		timerCancel();
		if (mApp.mIpcUpdateManage != null) {
			mApp.mIpcUpdateManage.dimissLoadingDialog();
		}

		if (null != mPrepareDialog) {
			UserUtils.dismissUpdateDialog(mPrepareDialog);
			mPrepareDialog = null;
		}

		if (null != mSendDialog) {
			UserUtils.dismissUpdateDialog(mSendDialog);
			mSendDialog = null;
		}

		if (null != mUpdateDialog) {
			UserUtils.dismissUpdateDialog(mUpdateDialog);
			mUpdateDialog = null;
		}
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("carupgrade");
		}
		EventBus.getDefault().unregister(this);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (view.getId()) {
		case R.id.update_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mBtnDownload.setBackgroundResource(R.drawable.icon_login_click);
				break;
			case MotionEvent.ACTION_UP:
				mBtnDownload.setBackgroundResource(R.drawable.icon_login);
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}
		return false;
	}

	/**
	 * 升级1阶段
	 */
	public void showUpdateFirstDisconnect(String message) {
		if (isExit) {
			return;
		}
		if (null == mFirstDialog) {
			mFirstDialog = new AlertDialog.Builder(UpdateActivity.this).setTitle("提示").setMessage(message)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							exit();
						}
					}).show();
		}
	}

	/**
	 * 升级2阶段
	 */
	public void showUpdateSecondDisconnect(String message) {
		if (isExit) {
			return;
		}
		if (null == mSecondDialog) {
			mSecondDialog = new AlertDialog.Builder(UpdateActivity.this).setTitle("提示").setMessage(message)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							exit();
						}
					}).show();
		}
	}

	/**
	 * 当前ipc是最新版本UI显示
	 */
	public void isNewVersion() {
		mIsDisConnect = true;
		mBtnDownload.setVisibility(View.GONE);
		mScrollView.setVisibility(View.GONE);
		mUpdateNewImage.setVisibility(View.VISIBLE);
		mUpdateNewText.setVisibility(View.VISIBLE);
	}

	public void exit() {
		isExit = true;
		mIsSendFileOk = false;
		mIsDisConnect = false;
		if (null != mUpdateHandler) {
			mUpdateHandler.removeCallbacksAndMessages(null);
		}
		finish();
		timerCancel();
		if (null != mUpdateDialogSuccess) {
			UserUtils.dismissUpdateDialog(mUpdateDialogSuccess);
			mUpdateDialogSuccess = null;
		}
		if (null != mPrepareDialog) {
			UserUtils.dismissUpdateDialog(mPrepareDialog);
			mPrepareDialog = null;
		}
		if (null != mSendDialog) {
			UserUtils.dismissUpdateDialog(mSendDialog);
			mSendDialog = null;
		}
		if (null != mSendOk) {
			UserUtils.dismissUpdateDialog(mSendOk);
			mSendOk = null;
		}
		if (null != mUpdateDialog) {
			UserUtils.dismissUpdateDialog(mUpdateDialog);
			mUpdateDialog = null;
		}
		if (null != mUpdateDialogFail) {
			UserUtils.dismissUpdateDialog(mUpdateDialogFail);
			mUpdateDialogFail = null;
		}
		if (null != mFirstDialog) {
			UserUtils.dismissUpdateDialog(mFirstDialog);
			mFirstDialog = null;
		}
		if (null != mSecondDialog) {
			UserUtils.dismissUpdateDialog(mSendDialog);
			mSendDialog = null;
		}

	}
}
