package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import android.annotation.SuppressLint;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.entity.WiFiInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.reportlog.ReportLogManager;
import cn.com.mobnote.list.WiFiListAdapter;
import cn.com.mobnote.list.WiFiListManage;
import cn.com.mobnote.list.WiFiListManage.WiFiListData;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiConnectManagerSupport.WifiCipherType;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * Wifi扫描列表
 *
 */
public class WiFiLinkListActivity extends BaseActivity implements OnClickListener, WifiConnCallBack,
		ILiveDialogManagerFn {

	public static String willConnName2 = null;
	public static String willConnMac2 = null;
	/** 扫描WIFI列表消息 */
	private static final int MSG_H_SCAN_WIFI = 100;

	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** loading */
	private RelativeLayout mLoading = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;
	/** 说明文字 */
	private TextView mDescTitleText = null;
	private TextView mDescTitleText2 = null;
	/** IPC信号动画 */
	private ImageView mIpcSignalImage = null;
	private AnimationDrawable mIpcSignalAnim = null;
	/** 刷新按钮 */
	private ImageButton mRefreshHelpBtn = null;
	/** 下一步按钮 */
	private Button mNextBtn = null;
	/** wifi列表 */
	private ListView mWiFiList = null;
	/** wifi列表manage */
	private WiFiListManage mWiFiListManage = null;
	private WifiConnectManager mWac = null;
	/** wifi列表适配器 */
	public WiFiListAdapter mWiFiListAdapter = null;
	public ArrayList<WiFiListData> mWiFiListData = null;
	/** 当前是否已连接ipc wifi */
	private boolean mHasLinked = false;
	/** 连接wifi名称 */
	public String mLinkWiFiName = null;
	private WifiManager mWifiManager = null;

	private boolean mIsCanAcceptIPC = false;
	private TextView mHelpTv = null;
	/** 是否第一次进入本界面，主要在onResume中使用 */
	private boolean mIsFirst = true;
	/** 区分是自动连接　还是用户手动点击连接 */
	private boolean isAutoConn = false;
	/** 连接失败次数 */
	private final int SHOW_SETTING_COUNT = 3;
	/** 绑定失败次数，超过3次，提示用户去系统WIFI列表綁定 */
	private int mFailedCount = 0;

	private static final String TAG = "WiFiLinkListActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_list);
		mContext = this;

		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, "WiFiLinkList");
		// 清除数据
		ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).clear();
		// 写日志，表示绑定失败
		ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setType("2");

		GFileUtils.writeLiveLog("WifiLinkListActivity-----------------onCreate--------1111");
		collectLog("onCreate", "---1");

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(mWifiManager, this);

		SysApplication.getInstance().addActivity(this);

		// 页面初始化
		init();

		dealAutoConn();
		// mLoading.setVisibility(View.VISIBLE);
		// mBaseHandler.sendEmptyMessageDelayed(MSG_H_SCAN_WIFI, 1000);
	}

	private void dealAutoConn() {
		GFileUtils.writeLiveLog("WiFiLinkListActivity-------dealAutoConn  -------111");

		collectLog("dealAutoConn", "-----1");
		if (null == mWac) {
			return;
		}
		GFileUtils.writeLiveLog("WiFiLinkListActivity-------dealAutoConn  -------22222");
		collectLog("dealAutoConn", "-----2");

		WifiRsBean bean = mWac.getConnResult();
		collectLog("dealAutoConn", "-----3");
		if (null == bean) {
			GFileUtils.writeLiveLog("WiFiLinkListActivity-------dealAutoConn   : NULL");
			collectLog("dealAutoConn", "-----4 NULL");
			GolukDebugUtils.e("", "WiFiLinkListActivity 通知logic连接ipc---dealAutoConn--------NULL---没有连接上WIFI");
			// 未连接Goluk,直接扫描列表
			mLoading.setVisibility(View.VISIBLE);
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_SCAN_WIFI, 1000);
		} else {
			GFileUtils.writeLiveLog("WiFiLinkListActivity-------dealAutoConn  -------NOT  NULL");
			collectLog("dealAutoConn", "-----5 NOT  NULL");
			WifiRsBean[] beanArray = new WifiRsBean[1];
			beanArray[0] = bean;
			willConnName2 = bean.getIpc_ssid();
			willConnMac2 = bean.getIpc_bssid();
			mWiFiListManage.analyzeWiFiData(beanArray);
			mWiFiListAdapter.notifyDataSetChanged();
			saveConnectWifiMsg(willConnName2, "", willConnMac2);

			collectLog("dealAutoConn", "willConnName2:" + willConnName2 + "  willConnMac2:" + willConnMac2);

			GFileUtils.writeLiveLog("WiFiLinkListActivity-------dealAutoConn   : willConnName2:" + willConnName2
					+ "  willConnMac2:" + willConnMac2);

			GolukDebugUtils.e("", "WiFiLinkListActivity 通知logic连接ipc---dealAutoConn--------连接上了：" + willConnName2);
			if (mApp.isIpcLoginSuccess) {
				GFileUtils.writeLiveLog("WiFiLinkListActivity-------dealAutoConn  -------444444444");
				collectLog("dealAutoConn", "-----6");
				// 直接显示在列表中
				this.nextCan();
				if (willConnName2 != null && null != willConnMac2) {
					mWiFiListAdapter.refreshConnectState(willConnName2, willConnMac2);
				}
			} else {
				// 去连接IPC
				GFileUtils.writeLiveLog("WiFiLinkListActivity-------dealAutoConn   : sendLogicLinkIpc-------:");
				collectLog("dealAutoConn", "-----7--------sendLogicLinkIpc");
				isAutoConn = true;
				sendLogicLinkIpc();
				mLoading.setVisibility(View.VISIBLE);
			}
		}
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
		if (MSG_H_SCAN_WIFI == msg.what) {
			getWiFiList(true, true);
		}
	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init() {
		// 获取页面元素
		mLoading = (RelativeLayout) findViewById(R.id.loading_layout);
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mIpcSignalImage = (ImageView) findViewById(R.id.imageView2);
		mIpcSignalAnim = (AnimationDrawable) mIpcSignalImage.getBackground();

		mDescTitleText = (TextView) findViewById(R.id.wifilist_textView1);
		mDescTitleText2 = (TextView) findViewById(R.id.wifilist_textView2);
		mRefreshHelpBtn = (ImageButton) findViewById(R.id.refresh_help_btn);
		mNextBtn = (Button) findViewById(R.id.next_btn);

		mHelpTv = (TextView) findViewById(R.id.wifi_link_list_help);
		mHelpTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
		mHelpTv.setOnClickListener(this);

		mWiFiList = (ListView) findViewById(R.id.wifi_list_listview);

		mWiFiListManage = new WiFiListManage(mContext);
		mWiFiListData = mWiFiListManage.getWiFiList();
		mWiFiListAdapter = new WiFiListAdapter(mContext, mWiFiListData);
		mWiFiList.setAdapter(mWiFiListAdapter);

		// 注册事件
		mBackBtn.setOnClickListener(this);
		mRefreshHelpBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);

		// 启动动画
		mIpcSignalAnim.start();
		// 修改title说明文字颜色
		final String connStr1 = "确认<font color=\"#0587ff\"> WiFi指示灯 </font>闪烁";
		mDescTitleText.setText(Html.fromHtml(connStr1));
		mDescTitleText.getPaint().setFakeBoldText(true);
		final String connStr2 = "连接名称为<font color=\"#0587ff\"> Goluk xxxxx </font>的WiFi";
		mDescTitleText2.setText(Html.fromHtml(connStr2));
		mDescTitleText2.getPaint().setFakeBoldText(true);
	}

	/**
	 * 获取wifi列表
	 */
	private void getWiFiList(boolean isFrist, boolean b) {
		if (!isFrist) {
			mLoading.setVisibility(View.VISIBLE);
		}

		GFileUtils.writeLiveLog("WiFiLinkListActivity-------getWiFiList-------isFrist:" + isFrist + "  b:" + b);

		collectLog("getWiFiList", "isFrist:" + isFrist + "  b:" + b);

		GolukDebugUtils.e("", "获取wifi列表---getWiFiList---");
		// 获取文件列表tcay_ap_ipc
		mWac.scanWifiList("", b);
	}

	/**
	 * 连接指定wifi
	 * 
	 * @param wifiName
	 * @param pwd
	 */
	public void connectWiFi(String wifiName, String mac, String pwd) {
		GFileUtils.writeLiveLog("WiFiLinkListActivity-------connectWiFi-------has Password wifiName:" + wifiName
				+ "  mac:" + mac + "  pwd:" + pwd);

		collectLog("connectWiFi", "has Password wifiName:" + wifiName + "  mac:" + mac + "  pwd:" + pwd);

		isAutoConn = false;
		willConnName2 = wifiName;
		willConnMac2 = mac;
		mLoading.setVisibility(View.VISIBLE);
		saveConnectWifiMsg(wifiName, pwd, mac);
		// 连接wifi
		mWac.connectWifi(wifiName, pwd, WifiCipherType.WIFICIPHER_WPA);
		GolukDebugUtils.e("", "开始连接选定wifi---connectWiFi---" + wifiName + "---" + pwd);
	}

	/**
	 * 连接指定wifi
	 * 
	 * @param wifiName
	 */
	public void connectWiFi(String wifiName, String mac) {
		GFileUtils.writeLiveLog("WiFiLinkListActivity-------connectWiFi-------no Password wifiName:" + wifiName
				+ " mac:" + mac);

		collectLog("connectWiFi--no pass", "no Password wifiName:" + wifiName + " mac:" + mac);

		isAutoConn = false;
		willConnName2 = wifiName;
		willConnMac2 = mac;
		mLoading.setVisibility(View.VISIBLE);
		saveConnectWifiMsg(wifiName, "", mac);
		// 连接wifi
		mWac.connectWifi(wifiName, "", WifiCipherType.WIFICIPHER_NOPASS);
		GolukDebugUtils.e("", "开始连接选定wifi---connectWiFi---" + wifiName + "---pwd---空");
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
	public void sendLogicLinkIpc() {
		GFileUtils.writeLiveLog("WiFiLinkListActivity-------sendLogicLinkIpc-------");

		collectLog("sendLogicLinkIpc", "--------1");
		// 先获取ipc是否已连接
		boolean isLogin = mApp.getIpcIsLogin();
		GolukDebugUtils.e("", "WiFiLinkListActivity ipc连接状态---WiFiLinkListActivity---b---" + isLogin);
		collectLog("sendLogicLinkIpc", "--------2---isLogin---: " + isLogin);
		if (!isLogin) {
			mLoading.setVisibility(View.VISIBLE);
			// 连接ipc热点wifi---调用ipc接口
			GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1");
			collectLog("sendLogicLinkIpc", "--------3------: ");
			mIsCanAcceptIPC = true;
			boolean b = mApp.mIPCControlManager.setIPCWifiState(true, "192.168.62.1");
			GolukDebugUtils.e("", "WiFiLinkListActivity 通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
			collectLog("sendLogicLinkIpc", "--------4---b---: " + b);
		} else {
			// ipc已连接
			mIsCanAcceptIPC = true;
			ipcLinkedCallBack();
		}
	}

	public void ipcLinkFailedCallBack() {
		collectLog("ipcLinkFailedCallBack", "--------1");
		GolukDebugUtils.e("", "WiFiLinkListActivity  通知logic连接ipc---dealAutoConn--------ipcLinkFailedCallBack：");
		mApp.mIPCControlManager.setIPCWifiState(false, "");
		if (!mIsCanAcceptIPC) {
			return;
		}

		collectLog("ipcLinkFailedCallBack", "--------2");

		mIsCanAcceptIPC = false;
		mLoading.setVisibility(View.GONE);
		this.nextNotCan();
		mWiFiListManage.setNoSelect();
		mWiFiListAdapter.notifyDataSetChanged();
		if (isAutoConn) {
			mLoading.setVisibility(View.VISIBLE);
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_SCAN_WIFI, 1000);
		}

		collectLog("ipcLinkFailedCallBack", "--------3");

		isAutoConn = false;
	}

	/**
	 * ipc连接成功回调
	 */
	public void ipcLinkedCallBack() {
		if (!mIsCanAcceptIPC) {
			return;
		}
		mIsCanAcceptIPC = false;
		isAutoConn = false;
		GolukDebugUtils.e("", "WiFiLinkListActivity   ipc连接成功回调---ipcLinkedCallBack---1");

		collectLog("ipcLinkedCallBack", "ipc Conn----sucess!!!: ");
		mLoading.setVisibility(View.GONE);
		// 标识已连接ipc热点,可以点击下一步
		this.nextCan();
		if (willConnName2 != null && null != willConnMac2) {
			mWiFiListAdapter.refreshConnectState(willConnName2, willConnMac2);
		}
	}

	@Override
	protected void onResume() {
		mApp.setContext(this, "WiFiLinkList");
		super.onResume();
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		if (mIsFirst) {
			mIsFirst = false;
		} else {
			if (!mHasLinked) {
				GFileUtils.writeLiveLog("WiFiLinkListActivity-------onResume----------auto Conn");

				collectLog("onResume", "----auto Conn");
				this.dealAutoConn();
			}
		}

	}

	private void back() {
		reportLog();
		finish();
		LiveDialogManager.getManagerInstance().dismissTwoButtonDialog();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			GFileUtils.writeLiveLog("---WiFiLinkListActivity----------------click Back");

			collectLog("onClick", "click Back: ");
			// 返回
			back();
			break;
		case R.id.refresh_help_btn:
			// 获取wifi列表
			getWiFiList(false, true);
			break;
		case R.id.next_btn:
			// 已连接ipc热点,可以跳转到修改密码页面
			if (mHasLinked) {
				toNextView();
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
		mHasLinked = true;
		mNextBtn.setBackgroundResource(R.drawable.connect_mianbtn);
	}

	private void nextNotCan() {
		mNextBtn.setBackgroundResource(R.drawable.connect_mianbtn_ash);
		mHasLinked = false;
	}

	private void callBack_IPCConnect(int state, int process, String message, Object arrays) {
		if (state >= 0) {
			// 连接成功
			// 通知ipc连接成功
			sendLogicLinkIpc();
		} else {
			this.nextNotCan();
			GolukUtils.showToast(mContext, message);
			showSettingDialog();
		}
	}

	private void showSettingDialog() {
		mFailedCount++;
		if (mFailedCount > SHOW_SETTING_COUNT) {
			LiveDialogManager.getManagerInstance().showTwoBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_WIFIBIND_SHOWSETTING, "提示", "请去系统WIFI连接设置");
		}
	}

	private void callBack_ScanWifiList(int state, int process, String message, Object arrays) {
		GolukDebugUtils.e("", "--type---callBack_ScanWifiList---state---" + state + "---process---" + process
				+ "---message---" + message + "---arrays---" + arrays);

		collectLog("callBack_ScanWifiList", "--type---callBack_ScanWifiList---state---" + state + "---process---"
				+ process + "---message---" + message);

		if (state < 0) {
			return;
		}
		GolukDebugUtils.e("", "wifi链接接口回调---type---callBack_ScanWifiList---state---222222");

		// collectLog("callBack_ScanWifiList",
		// "---type---callBack_ScanWifiList---state---222222");
		// 获取wifi列表
		WifiRsBean[] beans = (WifiRsBean[]) arrays;
		if (beans == null) {
			return;
		}
		GolukDebugUtils.e("", "wifi链接接口回调---type---callBack_ScanWifiList---state---33333333");
		collectLog("callBack_ScanWifiList", "---type---callBack_ScanWifiList---state---33333");
		mWiFiListManage.analyzeWiFiData(beans);
		mWiFiListAdapter.notifyDataSetChanged();
		this.nextNotCan();
		GolukDebugUtils.e("", "wifi链接接口回调---type---callBack_ScanWifiList---state---44444");
		collectLog("callBack_ScanWifiList", "---type---callBack_ScanWifiList---state---444444");

	}

	private void free() {
		if (null != mWiFiListManage) {
			mWiFiListManage.clear();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			GolukDebugUtils.e("", "按下系统返回键---WiFiLinkListActivity---1");
			GFileUtils.writeLiveLog("---WiFiLinkListActivity-------onKeyDown---------Back---111");
			collectLog("onKeyDown", "--WiFiLinkListActivity-------onKeyDown---------Back---111");
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukDebugUtils.e("", "jyf-----WifiBind-----List-----onDestroy----");
		GFileUtils.writeLiveLog("WiFiLinkListActivity-------onDestroy---------------------1111");
		collectLog("onDestroy", "onDestroy--1");
		free();
	}

	@Override
	public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
		final String log = "wifi---type---" + type + "---state---" + state + "---process---" + process
				+ "---message---" + message;

		GolukDebugUtils.e("", log);

		GFileUtils.writeLiveLog(log);

		collectLog("wifiCallBack", log);

		mLoading.setVisibility(View.GONE);
		switch (type) {
		case 1:
			// 扫描WIFI列表 返回结果
			callBack_ScanWifiList(state, process, message, arrays);
			break;
		case 2:
			// 手机连接IPC热点结果
			callBack_IPCConnect(state, process, message, arrays);
			break;
		default:
			GolukUtils.showToast(mContext, message);
			break;
		}
	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (LiveDialogManager.DIALOG_TYPE_WIFIBIND_SHOWSETTING == dialogType) {
			switch (function) {
			case LiveDialogManager.FUNCTION_DIALOG_OK:
				// 跳转系统WIFI列表
				GFileUtils.writeLiveLog("WiFiLinkListActivity-------Jump----System WifiLIst-------");
				collectLog("dialogManagerCallBack", "-Jump----System WifiLIst");
				Intent intent = new Intent();
				intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
				startActivity(intent);
				break;
			default:
				break;
			}
		}

	}
}
