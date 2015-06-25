package cn.com.mobnote.golukmobile;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.user.DataCleanManage;
import cn.com.mobnote.user.IPCInfo;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
public class UpdateActivity extends BaseActivity implements OnClickListener,IPCManagerFn,OnTouchListener {

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
	/**GolukApplication**/
	private GolukApplication mApp = null;

	/** 0 下载 1安装 **/
	public final static String UPDATE_SIGN = "update_sign";
	/** 数据展示 **/
	public final static String UPDATE_DATA = "update_data";

	/** 0下载 / 1安装的标志 **/
	private int mSign = 0;
	/** 数据 **/
	private IPCInfo mIpcInfo = null;
	
	/**ipc安装升级中更新UI显示**/
	private String stage = "";
	private String percent = "";
	public static Handler mUpdateHandler = null;
	private Timer mTimer = null;
	
	/**文件不存在**/
	public static final int UPDATE_FILE_NOT_EXISTS = 10;
	/**准备文件**/
	public static final int UPDATE_PREPARE_FILE = 11;
	/**传输文件**/
	public static final int UPDATE_TRANSFER_FILE = 12;
	/**文件传输成功**/
	public static final int UPDATE_TRANSFER_OK = 13;
	/**正在升级**/
	public static final int UPDATE_UPGRADEING = 14;
	/**升级成功**/
	public static final int UPDATE_UPGRADE_OK = 15;
	/**升级失败**/
	public static final int UPDATE_UPGRADE_FAIL = 16;
	/**校验不通过**/
	public static final int UPDATE_UPGRADE_CHECK = 17;
	/**ipc未连接**/
	public static final int UPDATE_IPC_UNUNITED = 18;
	/**ipc连接断开**/
	public static final int UPDATE_IPC_DISCONNECT = 19;
	
	/**下载状态**/
	private int downloadStatus = 0;
	
	/**下载失败**/
	private static final int DOWNLOAD_STATUS_FAIL = 0;
	/**下载成功**/
	private static final int DOWNLOAD_STATUS_SUCCESS = 1;
	/**下载中**/
	private static final int DOWNLOAD_STATUS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.upgrade_layout);

		mApp = (GolukApplication)getApplication();
		initView();

		Intent it = getIntent();
		mSign = it.getIntExtra(UPDATE_SIGN, 0);
		mIpcInfo = (IPCInfo) it.getSerializableExtra(UPDATE_DATA);

		mTextIpcVersion.setText(mIpcInfo.version);
		String size = DataCleanManage.getFormatSize(Double.parseDouble(mIpcInfo.filesize));
		mTextIpcSize.setText(size);
		mTextUpdateContent.setText(mIpcInfo.appcontent);

		if (mSign == 0) {
			boolean b = mApp.mIpcUpdateManage.download(mIpcInfo.url, mIpcInfo.path);
			if(b){
				mTextDowload.setText("下载中");
				mBtnDownload.setText("下载中…0%");
				downloadStatus = DOWNLOAD_STATUS;
				mBtnDownload.setBackgroundResource(R.drawable.icon_more);
				mBtnDownload.setEnabled(false);
			}else{
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
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(null != GolukApplication.getInstance().getIPCControlManager()){
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
			// 下载 / 升级
			if (mSign == 0) {
				if(DOWNLOAD_STATUS_FAIL == downloadStatus){
					mTextDowload.setText("下载中");
					mBtnDownload.setText("下载中…0%");
					mApp.mIpcUpdateManage.download(mIpcInfo.url, mIpcInfo.path);
				}
			} else if (mSign == 1) {
				mTextDowload.setText("已下载");
				mBtnDownload.setText("安装此极路客固件程序");
				mBtnDownload.setBackgroundResource(R.drawable.icon_login);
				mBtnDownload.setEnabled(true);
				mApp.mIpcUpdateManage.ipcInstall();
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
		GolukDebugUtils.i("lily", "------------downloadCallback-----------");
		downloadStatus = state;
		if (state == DOWNLOAD_STATUS) {
			// 下载中
			int progress = (Integer) param1;
			mBtnDownload.setBackgroundResource(R.drawable.icon_more);
			mBtnDownload.setEnabled(false);
			mBtnDownload.setText("正在下载…" + progress + "%");
		} else if (state == DOWNLOAD_STATUS_SUCCESS) {
			// 下载成功
			mBtnDownload.setText("安装此极路客固件程序");
			mBtnDownload.setBackgroundResource(R.drawable.icon_login);
			mBtnDownload.setEnabled(true);
			mSign = 1;
		} else if (state == DOWNLOAD_STATUS_FAIL) {
			// 下载失败
			mBtnDownload.setBackgroundResource(R.drawable.icon_login);
			mBtnDownload.setEnabled(true);
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
		GolukDebugUtils.e("lily", "YYYYYY====IPC_VDCP_Msg_IPCUpgrade====msg="+msg+"===param1="+param1+"==param2="+param2+"--------event-----"+event);
		if(event == ENetTransEvent_IPC_UpGrade_Resp){
			if(IPC_VDCP_Msg_IPCUpgrade == msg){
				GolukDebugUtils.e("lily", "---------连接ipc-------");
				if(param1 == RESULE_SUCESS){
					String str = (String)param2;
					GolukDebugUtils.i("lily", "--str----"+str);
					if(TextUtils.isEmpty(str)){
						return ;
					}
					try{
						JSONObject json = new JSONObject(str);
						stage = json.getString("stage");
						percent = json.getString("percent");
						GolukDebugUtils.i("lily", "---------stage-----"+stage+"-------percent----"+percent);
						if(stage.equals("1")){
							//正在传输文件，请稍候……
							mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_FILE);
						}
						if(stage.equals("1") && percent.equals("100")){
							//传输文件成功
							mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_OK);
						}
						if(stage.equals("2")){
							//开始升级，可能需要几分钟，请不要给摄像头断电。
							mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADEING);
							if(!percent.equals("100")){
								timerTask();
							}
							timerCancel();
						}
						if(stage.equals("2") && percent.equals("100")){
							//升级成功
							mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_OK);
						}
						if(stage.equals("3")){
							if(percent.equals("-1")){
								mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_CHECK);
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					//升级失败
					mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_FAIL);
				}
			}
		}
	}
	
	/**
	 * 固件升级过程中超时
	 * 1000x60=6000
	 */
	public void timerTask(){
		timerCancel();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				//ipc断开
				mUpdateHandler.sendEmptyMessage(UPDATE_IPC_DISCONNECT);
			}
		}, 6000);
	}
	
	public void timerCancel(){
		if(mTimer !=null){
			mTimer.cancel();
			mTimer = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(null != GolukApplication.getInstance().getIPCControlManager()){
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
	
}
