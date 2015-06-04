package cn.com.mobnote.golukmobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.entity.WiFiInfo;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.wifibind.ViewFrame;
import cn.com.mobnote.golukmobile.wifibind.WifiLinkSetIpcLayout;
import cn.com.mobnote.golukmobile.wifibind.WifiLinkSucessLayout;
import cn.com.mobnote.golukmobile.wifibind.WifiLinkWaitConnLayout;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.debug.GolukDebugUtils;

public class WiFiLinkCompleteActivity2 extends BaseActivity implements OnClickListener, WifiConnCallBack {

	private static final String TAG = "WiFiLinkBindAll";
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private ImageButton mBackBtn = null;

	private WifiConnectManager mWac = null;

	private boolean isExit = false;
	/** 中间的根布局 */
	private FrameLayout mMiddleLayout = null;

	private WifiLinkSetIpcLayout layout1 = null;
	private WifiLinkWaitConnLayout layout2 = null;
	private WifiLinkSucessLayout layout3 = null;
	/** 当前正在显示的布局 */
	private ViewFrame mCurrentLayout = null;

	private Button mCompleteBtn = null;
	private ImageView mProgressImg = null;

	private int connectCount = 0;

	/** ipc连接mac地址 */
	private String mIpcMac = "";
	private String mWiFiIp = "";

	/** 开始使用状态 */
	private boolean mIsComplete = false;

	private final int STATE_SET_IPC_INFO = 0;
	private final int STATE_WAIT_CONN = 1;
	private final int STATE_SUCESS = 2;

	private int mState = STATE_SET_IPC_INFO;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_complete2);
		mContext = this;
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(wm, this);
		initChildView();
		mMiddleLayout = (FrameLayout) findViewById(R.id.wifi_link_complete_frmelayout);
		mCompleteBtn = (Button) findViewById(R.id.complete_btn);
		mCompleteBtn.setOnClickListener(this);
		mProgressImg = (ImageView) findViewById(R.id.wifilink_progress);
		init();
		toSetIPCInfoView();
		SysApplication.getInstance().addActivity(this);
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, TAG);

		setIpcLinkInfo();
	}

	private void initChildView() {
		layout1 = new WifiLinkSetIpcLayout(this);
		layout2 = new WifiLinkWaitConnLayout(this);
		layout3 = new WifiLinkSucessLayout(this);
	}

	@Override
	protected void hMessage(Message msg) {
		if (msg.what == 100) {
			createPhoneHot();
		}
	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init() {
		// 获取页面元素
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		// 注册事件
		mBackBtn.setOnClickListener(this);
	}

	/**
	 * 设置ipc信息，包括修改IPC的密码，IPC连接手机的热点信息
	 */
	private void setIpcLinkInfo() {
		connectCount++;
		// 连接ipc热点wifi---调用ipc接口
		GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---1");
		final String json = getSetIPCJson();
		GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---2---josn---" + json);
		boolean b = mApp.mIPCControlManager.setIpcLinkPhoneHot(json);
		if (!b) {
			GolukUtils.showToast(mContext, "调用设置IPC连接热点失败");
		}
		GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---3---b---" + b);
	}

	private String getSetIPCJson() {
		// 写死ip,网关
		final String ip = "192.168.1.103";
		final String way = "192.168.1.103";
		// 连接ipc热点wifi---调用ipc接口
		GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---1");
		String json = "";
		if (null != WiFiInfo.AP_PWD && !"".equals(WiFiInfo.AP_PWD)) {
			json = "{\"AP_SSID\":\"" + WiFiInfo.AP_SSID + "\",\"AP_PWD\":\"" + WiFiInfo.AP_PWD + "\",\"GolukSSID\":\""
					+ WiFiInfo.GolukSSID + "\",\"GolukPWD\":\"" + WiFiInfo.GolukPWD + "\",\"GolukIP\":\"" + ip
					+ "\",\"GolukGateway\":\"" + way + "\" }";
		} else {
			json = "{\"GolukSSID\":\"" + WiFiInfo.GolukSSID + "\",\"GolukPWD\":\"" + WiFiInfo.GolukPWD
					+ "\",\"GolukIP\":\"" + ip + "\",\"GolukGateway\":\"" + way + "\" }";
		}
		return json;
	}

	/**
	 * 设置IPC信息成功回调
	 */
	public void setIpcLinkWiFiCallBack(int state) {
		if (0 == state) {
			// 隐藏loading
			toWaitConnView();
			// 开始创建手机热点
			mBaseHandler.sendEmptyMessageDelayed(100, 3 * 1000);
			GolukDebugUtils.e("",
					"WJUN_____IPC_VDCP_TransManager_OnParserData设置热点信息成功回调-----Java-----setIpcLinkWiFiCallBack");
		} else {
			GolukDebugUtils.e("", "WJUN_____IPC_VDCP_TransManager_OnParserData-----失败----------");
			if (connectCount > 3) {
				GolukUtils.showToast(this, "绑定失败");
			} else {
				GolukUtils.showToast(this, "绑定失败, 重新连接 ");
				setIpcLinkInfo();
			}
		}
	}

	/**
	 * 创建手机热点
	 */
	private void createPhoneHot() {
		String wifiName = WiFiInfo.GolukSSID;
		String pwd = WiFiInfo.GolukPWD;
		String ipcssid = WiFiInfo.AP_SSID;
		String ipcmac = WiFiInfo.AP_MAC;
		// 创建热点之前先断开ipc连接
		mApp.mIPCControlManager.setIPCWifiState(false, null);
		// 改变Application-IPC退出登录
		mApp.setIpcLoginOut();
		// 调用韩峥接口创建手机热点
		GolukDebugUtils
				.e("", "创建手机热点---startWifiAp---1---" + wifiName + "---" + pwd + "---" + ipcssid + "---" + ipcmac);
		mWac.createWifiAP(wifiName, pwd, ipcssid, ipcmac);
	}

	/**
	 * wifi热点创建成功
	 */
	private void hotWiFiCreateSuccess() {

	}

	/**
	 * 通知logic连接ipc
	 */
	private void sendLogicLinkIpc(String ip, String ipcmac) {
		// 连接ipc热点wifi---调用ipc接口
		GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1---ip---" + ip);
		GolukApplication.mIpcIp = ip;
		mIpcMac = ipcmac;
		mWiFiIp = ip;
		boolean b = mApp.mIPCControlManager.setIPCWifiState(true, ip);
		GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
	}

	/**
	 * 保存wifi绑定标识
	 */
	private void saveBindMark() {
		// 绑定完成,保存标识
		SharedPreferences preferences = mContext.getSharedPreferences("ipc_wifi_bind", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean("isbind", true);
		// 提交修改
		editor.commit();
	}

	// 保存绑定的wifi名称
	private void saveBind(String name) {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
		preferences.edit().putString("ipc_bind_name", name).commit();
	}

	/**
	 * ipc连接成功回调
	 */
	public void ipcLinkWiFiCallBack() {
		this.toSucessView();

		// mCreateHotText.setVisibility(View.GONE);
		// mLinkedLayout.setVisibility(View.VISIBLE);
		// mLinkedDesc.setText(Html.fromHtml("你的Goluk已<font color=\"#0587ff\">成功连接</font>到手机"));
		// mCompleteBtn.setBackgroundResource(R.drawable.connect_mianbtn);
		mIsComplete = true;

		// 保存连接数据
		WifiRsBean beans = new WifiRsBean();
		beans.setIpc_mac(mIpcMac);
		beans.setIpc_ssid(WiFiInfo.AP_SSID);
		beans.setPh_ssid(WiFiInfo.GolukSSID);
		beans.setPh_pass(WiFiInfo.GolukPWD);
		beans.setIpc_ip(mWiFiIp);
		mWac.saveConfiguration(beans);
		saveBind(WiFiInfo.AP_SSID);
		// 保存绑定标识
		saveBindMark();
	}

	/**
	 * 退出页面设置
	 */
	private void backSetup() {
		if (isExit) {
			return;
		}
		isExit = true;
		if (this.STATE_SET_IPC_INFO == mState) {
			// 不支持返回
		} else if (this.STATE_WAIT_CONN == mState) {
			this.finish();
			// 没连接,关闭热点
			if (null != mWac) {
				mWac.closeWifiAP();
			}
		} else if (this.STATE_SUCESS == mState) {
			// 不支持返回
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			GolukDebugUtils.e("", "按下系统返回键---WiFiLinkCompleteActivity---1");
			// 返回关闭全部页面
			backSetup();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukDebugUtils.e("", "通知logic停止连接ipc---WiFiLinkCompleteActivity---onDestroy---1");
	}

	@Override
	protected void onResume() {
		mApp.setContext(this, TAG);
		super.onResume();
	}

	private void bindSucess() {
		saveBindMark();
		// 关闭wifi绑定全部页面
		SysApplication.getInstance().exit();

		// 跳转到ipc预览页面
		Intent i = new Intent(mContext, CarRecorderActivity.class);
		startActivity(i);
	}

	boolean isSucess = false;

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.back_btn:
			backSetup();
			break;
		case R.id.complete_btn:
			if (this.STATE_SUCESS == mState) {
				// 綁定成功后，可以进入行车记录仪
				// 关闭wifi绑定全部页面
				SysApplication.getInstance().exit();
				// 跳转到ipc预览页面
				Intent i = new Intent(mContext, CarRecorderActivity.class);
				startActivity(i);
			}
			break;
		}
	}

	private void toSetIPCInfoView() {
		mBackBtn.setVisibility(View.GONE);
		this.mState = STATE_SET_IPC_INFO;
		mMiddleLayout.removeAllViews();
		freeLayout();
		mMiddleLayout.addView(layout1.getRootLayout());
		mCurrentLayout = layout1;
		layout1.start();
	}

	private void toWaitConnView() {
		mBackBtn.setVisibility(View.VISIBLE);
		this.mState = STATE_WAIT_CONN;
		mMiddleLayout.removeAllViews();
		freeLayout();
		mMiddleLayout.addView(layout2.getRootLayout());
		mCurrentLayout = layout2;
		layout2.start();
	}

	private void freeLayout() {
		if (null != mCurrentLayout) {
			mCurrentLayout.free();
		}
	}

	private void toSucessView() {
		mBackBtn.setVisibility(View.GONE);
		this.mState = STATE_SUCESS;
		mMiddleLayout.removeAllViews();
		freeLayout();
		mMiddleLayout.addView(layout3.getRootLayout());
		mCurrentLayout = layout3;
		layout3.start();
		mCompleteBtn.setBackgroundResource(R.drawable.connect_mianbtn);
		mProgressImg.setBackgroundResource(R.drawable.setp_4);
	}

	@Override
	public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
		GolukDebugUtils.e("", "wifi链接接口回调---type---" + type + "---state---" + state + "---process---" + process
				+ "---message---" + message + "---arrays---" + arrays);
		switch (type) {
		case 3:
			if (state == 0) {
				switch (process) {
				case 0:
					// 创建热点成功
					hotWiFiCreateSuccess();
					break;
				case 1:
					// ipc成功连接上热点
					try {
						WifiRsBean[] bean = (WifiRsBean[]) arrays;
						if (null != bean) {
							GolukDebugUtils.e("", "IPC连接上WIFI热点回调---length---" + bean.length);
							if (bean.length > 0) {
								sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
							}
						}
					} catch (Exception e) {
						GolukUtils.showToast(mContext, "IPC连接热点返回信息不是数组");
					}
					break;
				default:
					GolukUtils.showToast(mContext, message);
					break;
				}
			} else {
				GolukUtils.showToast(mContext, message);
			}
			break;
		}
	}

}
