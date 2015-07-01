package cn.com.mobnote.golukmobile;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.user.DataCleanManage;
import cn.com.mobnote.user.IPCInfo;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
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
	/** GolukApplication **/
	private GolukApplication mApp = null;

	/** 0 下载 1安装 **/
	public final static String UPDATE_SIGN = "update_sign";
	/** 数据展示 **/
	public final static String UPDATE_DATA = "update_data";
	/**升级文件下载中**/
	public final static String UPDATE_PROGRESS = "update_progress";

	/** 0下载 / 1安装的标志 **/
	private int mSign = 0;
	/** 数据 **/
	private IPCInfo mIpcInfo = null;

	/** ipc安装升级中更新UI显示 **/
	private String stage = "";
	private String percent = "";
	public static Handler mUpdateHandler = null;
	private Timer mTimer = null;

	/** 文件不存在 **/
	public static final int UPDATE_FILE_NOT_EXISTS = 10;
	/** 准备文件 **/
	public static final int UPDATE_PREPARE_FILE = 11;
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
	public static final int UPDATE_IPC_UNUNITED = 18;
	/** ipc连接断开 **/
	public static final int UPDATE_IPC_DISCONNECT = 19;
	/** 升级1阶段摄像头断开连接 **/
	public static final int UPDATE_IPC_FIRST_DISCONNECT = 20;
	/** 升级2阶段摄像头断开连接 **/
	public static final int UPDATE_IPC_SECOND_DISCONNECT = 21;
	/** 下载状态 **/
	private int downloadStatus = 0;

	/** 下载失败 **/
	private static final int DOWNLOAD_STATUS_FAIL = 0;
	/** 下载成功 **/
	private static final int DOWNLOAD_STATUS_SUCCESS = 1;
	/** 下载中 **/
	private static final int DOWNLOAD_STATUS = 2;

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
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.upgrade_layout);

		mApp = (GolukApplication) getApplication();
		initView();

		Intent it = getIntent();
		mSign = it.getIntExtra(UPDATE_SIGN, 0);
		mIpcInfo = (IPCInfo) it.getSerializableExtra(UPDATE_DATA);
		
		GolukDebugUtils.e("aaa", "=====mApp.mIpcInfo====="+mIpcInfo);
		
		if(null != mIpcInfo){
			mApp.mSharedPreUtil.saveIPCDownVersion(mIpcInfo.version);
			mApp.mSharedPreUtil.saveIpcFileSize(mIpcInfo.filesize);
			mApp.mSharedPreUtil.saveIpcContent(mIpcInfo.appcontent);
			mApp.mSharedPreUtil.saveIPCURL(mIpcInfo.url);
			mApp.mSharedPreUtil.saveIPCPath(mIpcInfo.path);
		}
		
		ipc_version = mApp.mSharedPreUtil.getIPCDownVersion();
		ipc_size = mApp.mSharedPreUtil.getIPCFileSize();
		ipc_content = mApp.mSharedPreUtil.getIPCContent();
		ipc_url = mApp.mSharedPreUtil.getIPCURL();
		ipc_path = mApp.mSharedPreUtil.getIPCPath();
		
		mTextIpcVersion.setText(ipc_version);
		String size = DataCleanManage.getFormatSize(Double.parseDouble(ipc_size));
		mTextIpcSize.setText(size);
		mTextUpdateContent.setText(ipc_content);
		
		Intent itClick = getIntent();
		int progressSetup = itClick.getIntExtra(UPDATE_PROGRESS, 0);

		if (mSign == 0) {
			if(mApp.mLoadStatus){
				mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
				mTextDowload.setText("下载中");
				mBtnDownload.setText("下载中…"+progressSetup+"%");
				downloadStatus = DOWNLOAD_STATUS;
				mBtnDownload.setBackgroundResource(R.drawable.icon_more);
				mBtnDownload.setEnabled(false);
				return ;
			}
			boolean b = mApp.mIpcUpdateManage.download(ipc_url,
					mApp.mIpcUpdateManage.getBinFilePath(ipc_version));
			if (b) {
				mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
				mTextDowload.setText("下载中");
				mBtnDownload.setText("下载中…0%");
				downloadStatus = DOWNLOAD_STATUS;
				mBtnDownload.setBackgroundResource(R.drawable.icon_more);
				mBtnDownload.setEnabled(false);
			} else {
				mTextDowload.setText("未下载");
				mBtnDownload.setText("下载新极路客固件程序");
				downloadStatus = DOWNLOAD_STATUS_FAIL;
				mBtnDownload.setBackgroundResource(R.drawable.icon_login);
				mBtnDownload.setEnabled(true);
			}
		} else if (mSign == 1) {
			mTextDowload.setText("已下载");
			mBtnDownload.setText("安装此极路客固件程序");
			mBtnDownload.setBackgroundResource(R.drawable.icon_login);
			mBtnDownload.setEnabled(true);
		}

		mUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_FILE_NOT_EXISTS:
					UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, "没有找到升级文件。");
					break;
				case UPDATE_PREPARE_FILE:
					mPrepareDialog = UserUtils.showDialogUpdate(UpdateActivity.this, "正在为您准备升级，请稍候……");
					break;
				case UPDATE_TRANSFER_FILE:
					GolukDebugUtils.i("lily", "-------正在传输文件------");
					UserUtils.dismissUpdateDialog(mPrepareDialog);
					mPrepareDialog = null;
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
					mSendOk = UserUtils.showDialogUpdate(UpdateActivity.this, "文件传输成功，正在为您准备升级");
					break;
				case UPDATE_UPGRADEING:
					UserUtils.dismissUpdateDialog(mSendOk);
					mSendOk = null;
					if (mUpdateDialog == null) {
						mUpdateDialog = UserUtils.showDialogUpdate(UpdateActivity.this, "开始升级，过程可能需要几分钟，" + "\n"
								+ "请不要关闭摄像头电源……" + "\n" + "升级2阶段：" + percent + "%");
					} else {
						mUpdateDialog.setMessage("开始升级，过程可能需要几分钟，" + "\n" + "请不要关闭摄像头电源……" + "\n" + "升级2阶段：" + percent
								+ "%");
					}
					break;
				case UPDATE_UPGRADE_OK:
					UserUtils.dismissUpdateDialog(mUpdateDialog);
					mUpdateDialog = null;
					UserUtils
							.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, "恭喜您，极路客固件升级成功，正在重新启动，请稍候……");
					mBtnDownload.setText("已安装");
					mBtnDownload.setBackgroundResource(R.drawable.icon_more);
					mBtnDownload.setEnabled(false);
					break;
				case UPDATE_UPGRADE_FAIL:
					UserUtils.dismissUpdateDialog(mPrepareDialog);
					UserUtils.dismissUpdateDialog(mSendDialog);
					UserUtils.dismissUpdateDialog(mUpdateDialog);
					mPrepareDialog = null;
					mSendDialog = null;
					mUpdateDialog = null;
					mFirstDialog = null;
					mSecondDialog = null;
					UserUtils.showUpdateSuccess(mUpdateDialogFail, UpdateActivity.this, "很抱歉，升级失败。请您重试。");
					break;
				case UPDATE_UPGRADE_CHECK:
					UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, "校验不通过");
					break;
				case UPDATE_IPC_UNUNITED:
					UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, "您好像没有连接摄像头哦。");
					break;
				case UPDATE_IPC_DISCONNECT:
					timerCancel();
					UserUtils.dismissUpdateDialog(mPrepareDialog);
					UserUtils.dismissUpdateDialog(mSendDialog);
					mPrepareDialog = null;
					mUpdateDialog = null;
					UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, "摄像头断开连接，请检查后重试");
					break;
				case UPDATE_IPC_FIRST_DISCONNECT:
					mApp.mIpcUpdateManage.stopIpcUpgrade();
					timerCancel();
					UserUtils.dismissUpdateDialog(mPrepareDialog);
					UserUtils.dismissUpdateDialog(mSendDialog);
					mPrepareDialog = null;
					mSendDialog = null;
					showUpdateFirstDisconnect("很抱歉，升级失败，请先不要关闭摄像头电源，等待摄像头重新启动后再试。");
					timerFive();
					break;
				case UPDATE_IPC_SECOND_DISCONNECT:
					mApp.mIpcUpdateManage.stopIpcUpgrade();
					timerCancel();
					UserUtils.dismissUpdateDialog(mUpdateDialog);
					mUpdateDialog = null;
					showUpdateSecondDisconnect("很抱歉，摄像头连接异常中断，但它可能仍在升级中。请先不要关闭摄像头电源，等待摄像头升级成功。");
					timerFive();
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};

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

		// 监听
		mBtnBack.setOnClickListener(this);
		mBtnDownload.setOnClickListener(this);
		mBtnDownload.setOnTouchListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.update_btn:
			mApp.mIpcUpdateManage.showLoadingDialog();
			// 下载 / 升级
			if (mSign == 0) {
				if (DOWNLOAD_STATUS_FAIL == downloadStatus) {
					mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
					mTextDowload.setText("下载中");
					boolean b = mApp.mIpcUpdateManage.download(ipc_url, ipc_path);
					if(b){
						
					}else{
						mApp.mIpcUpdateManage.dimissLoadingDialog();
					}
				}
			} else if (mSign == 1) {
				boolean b = mApp.mIpcUpdateManage.ipcInstall(mApp.mIpcUpdateManage.getBinFilePath(ipc_version));
				if(b){
					
				}else{
					mApp.mIpcUpdateManage.dimissLoadingDialog();
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
		GolukDebugUtils.i("lily", "------------downloadCallback-----------" + state);
		downloadStatus = state;
		mApp.mIpcUpdateManage.dimissLoadingDialog();
		if (state == DOWNLOAD_STATUS) {
			// 下载中
			mApp.mLoadStatus = true;
			int progress = (Integer) param1;
			GolukDebugUtils.i("lily", "======下载文件progress=====" + progress);
			mBtnDownload.setBackgroundResource(R.drawable.icon_more);
			mBtnDownload.setEnabled(false);
			mBtnDownload.setText("正在下载…" + progress + "%");
			//保存进度
			mApp.mLoadProgress = progress;
		} else if (state == DOWNLOAD_STATUS_SUCCESS) {
			// 下载成功
			mApp.mLoadStatus = false;
			mTextDowload.setText("已下载");
			mBtnDownload.setText("安装此极路客固件程序");
			mBtnDownload.setBackgroundResource(R.drawable.icon_login);
			mBtnDownload.setEnabled(true);
			mSign = 1;
			// 下载成功删除文件
			mApp.mIpcUpdateManage.downIpcSucess();
		} else if (state == DOWNLOAD_STATUS_FAIL) {
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
		GolukDebugUtils.e("lily", "YYYYYY====IPC_VDCP_Msg_IPCUpgrade====msg=" + msg + "===param1=" + param1
				+ "==param2=" + param2 + "--------event-----" + event);
		mApp.mIpcUpdateManage.dimissLoadingDialog();
		if (event == ENetTransEvent_IPC_UpGrade_Resp) {
			if (IPC_VDCP_Msg_IPCUpgrade == msg) {
				GolukDebugUtils.e("lily", "---------连接ipc-------");
				if (param1 == RESULE_SUCESS) {
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
								timerTask();
							} else {
								timerTask();
							}
						}
						if (stage.equals("2")) {
							// 开始升级，可能需要几分钟，请不要给摄像头断电。
							mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADEING);
							if (!percent.equals("95")) {
								timerTask();
							} else {
								timerCancel();
								// 升级成功
								mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_OK);
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
					if (!(null != mFirstDialog && mFirstDialog.isShowing())|| !(null != mSecondDialog && mSecondDialog.isShowing())) {
						// 升级失败
						mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_FAIL);
					}
				}
			}
		}
	}

	/**
	 * 固件升级过程中超时 1000x60=6000
	 */
	public void timerTask() {
		timerCancel();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// ipc断开
				if (stage.equals("1")) {
					mUpdateHandler.sendEmptyMessage(UPDATE_IPC_FIRST_DISCONNECT);
				} else if (stage.equals("2")) {
					mUpdateHandler.sendEmptyMessage(UPDATE_IPC_SECOND_DISCONNECT);
				}
			}
		}, 6000);
	}

	/**
	 * ipc断开连接后，5秒自动关闭当前页面
	 */
	public void timerFive() {
		timerCancel();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				finish();
			}
		}, 5000);
	}

	public void timerCancel() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("carupgrade");
		}
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
		if (null == mFirstDialog) {
			mFirstDialog = new AlertDialog.Builder(UpdateActivity.this).setMessage(message)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					})
					.show();
		}
	}

	/**
	 * 升级2阶段
	 */
	public void showUpdateSecondDisconnect(String message) {
		if (null == mSecondDialog) {
			mSecondDialog = new AlertDialog.Builder(UpdateActivity.this).setMessage(message)
					.setPositiveButton("确定", null).show();
		}
	}
}
