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
import cn.com.mobnote.list.WiFiListAdapter;
import cn.com.mobnote.list.WiFiListManage;
import cn.com.mobnote.list.WiFiListManage.WiFiListData;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiConnectManagerSupport.WifiCipherType;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:wifi列表
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiLinkListActivity extends BaseActivity implements OnClickListener, WifiConnCallBack {

	public static String willConnName2 = null;
	public static String willConnMac2 = null;

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

	private final int STATE_NONE = 0;
	private final int STATE_SCANING = 1;
	private final int STATE_HZ_CONNING = 2;
	private final int STATE_IP_CONNING = 3;

	private int mState = STATE_NONE;

	private boolean mIsCanAcceptIPC = false;
	private TextView mHelpTv = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_list);
		mContext = this;

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(mWifiManager, this);

		SysApplication.getInstance().addActivity(this);
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, "WiFiLinkList");
		// 页面初始化
		init();
		mLoading.setVisibility(View.VISIBLE);
		mBaseHandler.sendEmptyMessageDelayed(100, 1000);
	}

	@Override
	protected void hMessage(Message msg) {
		if (100 == msg.what) {
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
		mState = STATE_SCANING;
		if (!isFrist) {
			mLoading.setVisibility(View.VISIBLE);
		}

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
		mState = STATE_HZ_CONNING;
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
		mState = STATE_HZ_CONNING;
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
		WiFiInfo.AP_SSID = mLinkWiFiName;
		WiFiInfo.AP_PWD = pwd;
		WiFiInfo.AP_MAC = mac;
	}

	/**
	 * 通知logic连接ipc
	 */
	public void sendLogicLinkIpc() {
		// 先获取ipc是否已连接
		mState = STATE_IP_CONNING;
		boolean isLogin = mApp.getIpcIsLogin();
		GolukDebugUtils.e("", "ipc连接状态---WiFiLinkListActivity---b---" + isLogin);
		if (!isLogin) {
			mLoading.setVisibility(View.VISIBLE);
			// 连接ipc热点wifi---调用ipc接口
			GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1");
			mIsCanAcceptIPC = true;
			boolean b = mApp.mIPCControlManager.setIPCWifiState(true, "192.168.62.1");
			GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
		} else {
			mState = STATE_NONE;
			// ipc已连接
			ipcLinkedCallBack();
		}
	}

	public void ipcLinkFailedCallBack() {
		mApp.mIPCControlManager.setIPCWifiState(false, "");
		if (!mIsCanAcceptIPC) {
			return;
		}
		mIsCanAcceptIPC = false;
		mLoading.setVisibility(View.GONE);
		this.nextNotCan();
		mWiFiListManage.setNoSelect();
		mWiFiListAdapter.notifyDataSetChanged();
	}

	/**
	 * ipc连接成功回调
	 */
	public void ipcLinkedCallBack() {
		if (!mIsCanAcceptIPC) {
			return;
		}
		mIsCanAcceptIPC = false;

		mState = STATE_NONE;
		GolukDebugUtils.e("", "ipc连接成功回调---ipcLinkedCallBack---1");
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
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			// 返回
			finish();
			break;
		case R.id.refresh_help_btn:
			// 获取wifi列表
			getWiFiList(false, true);
			break;
		case R.id.next_btn:
			// 已连接ipc热点,可以跳转到修改密码页面
			if (mHasLinked) {
				toNextView();
			} else {
				// 灰色按钮不能点击
				GolukUtils.showToast(mContext, "请先连接IPC-WIFI");
			}
			break;
		case R.id.wifi_link_list_help:
			GolukUtils.showToast(this, "连接帮助");
			break;
		}
	}

	private void setDefaultInfo() {
		// 保存默认的信息
		WiFiInfo.AP_PWD = IPC_PWD_DEFAULT;
		String wifiName = WiFiInfo.AP_SSID;
		String name = wifiName.replace("Goluk", "GOLUK");
		WiFiInfo.GolukSSID = name;
		WiFiInfo.GolukPWD = MOBILE_HOT_PWD_DEFAULT;
	}

	private void toNextView() {
		setDefaultInfo();

		// 跳转到修改热点密码页面
		Intent modifyPwd = new Intent(WiFiLinkListActivity.this, WiFiLinkCompleteActivity2.class);
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
			mState = STATE_IP_CONNING;
		} else {
			this.nextNotCan();
			mState = STATE_NONE;
			GolukUtils.showToast(mContext, message);
		}
	}

	private void callBack_ScanWifiList(int state, int process, String message, Object arrays) {
		GolukDebugUtils.e("", "wifi链接接口回调---type---callBack_ScanWifiList---state---" + state + "---process---"
				+ process + "---message---" + message + "---arrays---" + arrays);
		mState = STATE_NONE;
		if (state < 0) {
			mState = STATE_NONE;
			return;
		}
		GolukDebugUtils.e("", "wifi链接接口回调---type---callBack_ScanWifiList---state---222222");
		// 获取wifi列表
		WifiRsBean[] beans = (WifiRsBean[]) arrays;
		if (beans == null) {
			mState = STATE_NONE;
			return;
		}
		GolukDebugUtils.e("", "wifi链接接口回调---type---callBack_ScanWifiList---state---33333333");
		mWiFiListManage.analyzeWiFiData(beans);
		mWiFiListAdapter.notifyDataSetChanged();
		this.nextNotCan();
		GolukDebugUtils.e("", "wifi链接接口回调---type---callBack_ScanWifiList---state---44444");

	}

	@Override
	public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
		GolukDebugUtils.e("", "wifi链接接口回调---type---" + type + "---state---" + state + "---process---" + process
				+ "---message---" + message + "---arrays---" + arrays);
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
}
