package cn.com.mobnote.golukmobile.wifibind;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventFinishWifiActivity;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog.ForbidBack;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.reportlog.ReportLogManager;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * Wifi扫描列表
 *
 */
public class WiFiLinkListActivity extends BaseActivity implements OnClickListener, WifiConnCallBack, ForbidBack {

	private static final String TAG = "WiFiLinkListActivity";
	private static final String CONNECT_IPC_IP = "192.168.62.1";

	/** 未连接或连接失败 */
	private static final int STATE_FAILED = 0;
	/** 连接中 */
	private static final int STATE_CONNING = 1;
	/** 连接成功 */
	private static final int STATE_SUCCESS = 2;

	public static String sWillConnName2 = null;
	public static String sWillConnMac2 = null;
	/** application */
	private GolukApplication mApp = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 说明文字 */
	private TextView mDescTitleText = null;
	private TextView mDescTitleText2 = null;
	private TextView mDescTitleText4 = null;
	/** IPC信号动画 */
	private ImageView mIpcSignalImage = null;
	private AnimationDrawable mIpcSignalAnim = null;
	/** 下一步按钮 */
	private Button mNextBtn = null;
	private TextView mHelpTv = null;

	private WifiManager mWifiManager = null;
	private WifiConnectManager mWac = null;
	/** 连接wifi名称 */
	public String mLinkWiFiName = null;

	private boolean mIsCanAcceptIPC = false;

	/** 用于表示当前的状态 0/1/2 未连接/连接中/已连接 */
	private int mCurrentState = STATE_FAILED;
	/** 连接中对话框 */
	private CustomLoadingDialog mConnectingDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_list);
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "WiFiLinkList");
		// 清除数据
		ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).clear();
		// 写日志，表示绑定失败
		ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setType("2");
		collectLog("onCreate", "---1");

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(mWifiManager, this);
		// 页面初始化
		initView();
		EventBus.getDefault().register(this);
	}

	/**
	 * 页面初始化
	 */
	private void initView() {
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mIpcSignalImage = (ImageView) findViewById(R.id.imageView2);
		mIpcSignalAnim = (AnimationDrawable) mIpcSignalImage.getBackground();
		mDescTitleText = (TextView) findViewById(R.id.wifilist_textView1);
		mDescTitleText2 = (TextView) findViewById(R.id.wifilist_textView3);
		mDescTitleText4 = (TextView) findViewById(R.id.wifilist_textView4);
		mNextBtn = (Button) findViewById(R.id.next_btn);
		mHelpTv = (TextView) findViewById(R.id.wifi_link_list_help);
		mHelpTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线

		// 注册事件
		mBackBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		mHelpTv.setOnClickListener(this);

		// 启动动画
		mIpcSignalAnim.start();
		// 修改title说明文字颜色
		final String connStr1 = getResources().getString(R.string.wifi_link_ok) + "<font color=\"#0587ff\"> "
				+ getResources().getString(R.string.wifi_link_wifi_light) + " </font>"
				+ getResources().getString(R.string.wifi_link_twinkle);
		mDescTitleText.setText(Html.fromHtml(connStr1));
		// mDescTitleText.getPaint().setFakeBoldText(true);
		final String connStr2 = getResources().getString(R.string.wifi_link_wifi_name)
				+ "<font color=\"#0587ff\"> Goluk xxxxxx </font>"
				+ getResources().getString(R.string.wifi_link_wifi_name2);
		mDescTitleText2.setText(Html.fromHtml(connStr2));
		// mDescTitleText2.getPaint().setFakeBoldText(true);

		this.nextCan();
		setStateSwitch();
	}

	private void setStateSwitch() {
		switch (mCurrentState) {
		case STATE_FAILED:
			mNextBtn.setText(getResources().getString(R.string.wifi_link_go_system));
			mDescTitleText4.setVisibility(View.INVISIBLE);
			break;
		case STATE_CONNING:
			break;
		case STATE_SUCCESS:
			mNextBtn.setText(getResources().getString(R.string.wifi_link_next));
			setSucessWifiName(sWillConnName2);
			break;
		default:
			break;
		}
	}

	/**
	 * 连接成功后，设置成功的wifi名称
	 * 
	 * @param wifiname
	 * @author jyf
	 */
	private void setSucessWifiName(String wifiname) {
		mDescTitleText4.setVisibility(View.VISIBLE);
		final String connStr2 = getResources().getString(R.string.wifi_link_success_conn) + " <font color=\"#0587ff\">"
				+ wifiname + "</font>";
		mDescTitleText4.setText(Html.fromHtml(connStr2));
		// mDescTitleText4.getPaint().setFakeBoldText(true);
	}

	private boolean isGetWifiBean() {
		if (null == mWac) {
			connFailed();
			return false;
		}
		WifiRsBean bean = mWac.getConnResult();
		collectLog("isGetWifiBean", "-----1");
		if (null == bean) {
			GolukDebugUtils.e("", "bindbind-------------isGetWifiBean---failed2  :");
			collectLog("isGetWifiBean", "-----2");
			connFailed();
			return false;
		}
		collectLog("dealAutoConn", "-----5 NOT  NULL");
		sWillConnName2 = bean.getIpc_ssid();
		sWillConnMac2 = bean.getIpc_bssid();
		if (sWillConnName2 == null || null == sWillConnMac2 || sWillConnName2.length() <= 0
				|| sWillConnMac2.length() <= 0) {
			GolukDebugUtils.e("", "bindbind-------------isGetWifiBean---failed3  :");
			collectLog("isGetWifiBean", "-----3");
			// 连接失败
			connFailed();
			return false;
		}
		collectLog("isGetWifiBean", "willConnName2:" + sWillConnName2 + "  willConnMac2:" + sWillConnMac2);
		saveConnectWifiMsg(sWillConnName2, "", sWillConnMac2);
		return true;
	}

	private void dealAutoConn() {
		collectLog("dealAutoConn", "-----1");
		// 获取当前连接的wifi
		if (!isGetWifiBean()) {
			return;
		}
		collectLog("dealAutoConn", "-----2");
		GolukDebugUtils.e("", "WiFiLinkListActivity 通知logic连接ipc---dealAutoConn--------连接上了：" + sWillConnName2);
		// 去连接IPC
		// isAutoConn = true;
		sendLogicLinkIpc();
	}

	private void collectLog(String method, String msg) {
		ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
				.addLogData(JsonUtil.getReportData(TAG, method, msg));
	}

	private void reportLog() {
		final String jsonData = ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
				.getReportData();
		mApp.uploadMsg(jsonData, false);
		ReportLogManager.getInstance().removeKey(IMessageReportFn.KEY_WIFI_BIND);
	}

	@Override
	protected void hMessage(Message msg) {
	}

	private void saveConnectWifiMsg(String wifiName, String pwd, String mac) {
		mLinkWiFiName = wifiName;
		WiFiInfo.IPC_SSID = mLinkWiFiName;
		WiFiInfo.IPC_PWD = pwd;
		WiFiInfo.IPC_MAC = mac;
	}

	/**
	 * 通知logic连接ipc
	 */
	private void sendLogicLinkIpc() {
		collectLog("sendLogicLinkIpc", "--------1");
		// 连接ipc热点wifi---调用ipc接口
		GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1");
		mIsCanAcceptIPC = true;
		boolean b = mApp.mIPCControlManager.setIPCWifiState(true, CONNECT_IPC_IP);
		GolukDebugUtils.e("", "bindbind-------------sendLogicLinkIpc  :" + b);
		collectLog("sendLogicLinkIpc", "--------3------: " + b);
		if (b) {
			this.showLoadingDialog();
			mCurrentState = STATE_CONNING;
			this.setStateSwitch();
		} else {
			connFailed();
		}
		GolukDebugUtils.e("", "WiFiLinkListActivity 通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
	}

	private void connFailed() {
		collectLog("connFailed", "--------1------: ");
		this.dimissLoadingDialog();
		mApp.mIPCControlManager.setIPCWifiState(false, "");
		mApp.isIpcConnSuccess = false;
		mCurrentState = STATE_FAILED;
		setStateSwitch();
	}

	private void showLoadingDialog() {
		if (null != mConnectingDialog) {
			return;
		}
		mConnectingDialog = new CustomLoadingDialog(this, getResources().getString(R.string.wifi_link_38_text));
		mConnectingDialog.setListener(this);
		mConnectingDialog.show();
	}

	private void dimissLoadingDialog() {
		if (null != mConnectingDialog) {
			mConnectingDialog.close();
			mConnectingDialog = null;
		}
	}

	public void ipcFailedCallBack() {
		collectLog("ipcLinkFailedCallBack", "--------1");
		GolukDebugUtils.e("", "WiFiLinkListActivity  通知logic连接ipc---dealAutoConn--------ipcLinkFailedCallBack：");
		mApp.mIPCControlManager.setIPCWifiState(false, "");
		// if (!mIsCanAcceptIPC) {
		// return;
		// }

		GolukDebugUtils.e("", "bindbind-------------ipcFailedCallBack  :");

		collectLog("ipcLinkFailedCallBack", "--------2");
		mIsCanAcceptIPC = false;
		this.dimissLoadingDialog();
		GolukUtils.showToast(this, getResources().getString(R.string.wifi_link_conn_failed));
		mCurrentState = STATE_FAILED;
		this.setStateSwitch();
		this.nextNotCan();
		collectLog("ipcLinkFailedCallBack", "--------3");
	}

	/**
	 * ipc连接成功回调
	 */
	public void ipcSucessCallBack() {
		if (!mIsCanAcceptIPC) {
			return;
		}
		collectLog("ipcLinkedCallBack", "ipc Conn----sucess!!!: ");
		GolukDebugUtils.e("", "bindbind-------------ipcSucessCallBack  :");
		mIsCanAcceptIPC = false;
		// isAutoConn = false;
		GolukDebugUtils.e("", "WiFiLinkListActivity   ipc连接成功回调---ipcLinkedCallBack---1");
		this.dimissLoadingDialog();
		// 标识已连接ipc热点,可以点击下一步
		this.nextCan();
		mCurrentState = STATE_SUCCESS;
		this.setStateSwitch();
	}

	@Override
	protected void onResume() {
		mApp.setContext(this, "WiFiLinkList");
		super.onResume();
		collectLog("onResume", "----auto Conn--1:" + mApp.isIpcConnSuccess);
		if (mApp.isIpcConnSuccess) {
			GolukDebugUtils.e("", "bindbind-------------onResume:" + mApp.isIpcLoginSuccess);
			if (isGetWifiBean()) {
				collectLog("onResume", "----auto Conn--2");
				// 连接成功，直接改变状态
				this.nextCan();
				mCurrentState = STATE_SUCCESS;
				this.setStateSwitch();
			}
		} else {
			GolukDebugUtils.e("", "bindbind-------------onResume 22 :" + mCurrentState);
			collectLog("onResume", "----auto Conn--3: " + mCurrentState);
			if (STATE_FAILED == mCurrentState || mCurrentState == STATE_SUCCESS) {
				if (mCurrentState == STATE_SUCCESS) {
					mApp.mIPCControlManager.setIPCWifiState(false, "");
				}
				dealAutoConn();
			}
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			collectLog("onClick", "click Back: ");
			// 返回
			exit();
			break;
		case R.id.next_btn:
			// 已连接ipc热点,可以跳转到修改密码页面
			if (STATE_SUCCESS == mCurrentState) {
				toNextView();
			} else if (STATE_FAILED == mCurrentState) {
				collectLog("dialogManagerCallBack", "-Jump----System WifiLIst");
				GolukUtils.startSystemWifiList(this);
			} else {

			}
			break;
		case R.id.wifi_link_list_help:
			GolukUtils.openUrl(GolukUtils.URL_BIND_CONN_PROBLEM, this);
			break;
		}
	}

	private void setDefaultInfo() {
		// 保存默认的信息
		WiFiInfo.IPC_PWD = IPC_PWD_DEFAULT;
		String wifiName = WiFiInfo.IPC_SSID;
		String name = wifiName.replace("Goluk", "GOLUK");
		WiFiInfo.MOBILE_SSID = name;
		WiFiInfo.MOBILE_PWD = MOBILE_HOT_PWD_DEFAULT;
	}

	private void toNextView() {
		setDefaultInfo();
		// 跳转到修改热点密码页面
		Intent modifyPwd = new Intent(WiFiLinkListActivity.this, WiFiLinkCompleteActivity.class);
		modifyPwd.putExtra("cn.com.mobnote.golukmobile.wifiname", mLinkWiFiName);
		startActivity(modifyPwd);
	}

	private void nextCan() {
		// mIsConnSucess = true;
		mNextBtn.setBackgroundResource(R.drawable.connect_mianbtn);
	}

	private void nextNotCan() {
		// mNextBtn.setBackgroundResource(R.drawable.connect_mianbtn_ash);
		// mIsConnSucess = false;
	}

	private void exit() {
		reportLog();
		finish();
		LiveDialogManager.getManagerInstance().dismissTwoButtonDialog();
		this.dimissLoadingDialog();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			GolukDebugUtils.e("", "按下系统返回键---WiFiLinkListActivity---1");
			collectLog("onKeyDown", "--WiFiLinkListActivity-------onKeyDown---------Back---111");
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onEventMainThread(EventFinishWifiActivity event) {
		finish();
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
		GolukDebugUtils.e("", "jyf-----WifiBind-----List-----onDestroy----");
		collectLog("onDestroy", "onDestroy--1");
		if (null != mWac) {
			mWac.unbind();
			mWac = null;
		}
		LiveDialogManager.getManagerInstance().dismissTwoButtonDialog();
		this.dimissLoadingDialog();
	}

	@Override
	public void wifiCallBack(int type, int state, int process, String message, Object arrays) {

	}

	@Override
	public void forbidBackKey(int backKey) {
		if (1 == backKey) {
			connFailed();
		}
	}

}
