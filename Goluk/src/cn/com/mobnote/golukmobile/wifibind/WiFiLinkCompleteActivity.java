package cn.com.mobnote.golukmobile.wifibind;

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
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.reportlog.ReportLog;
import cn.com.mobnote.golukmobile.reportlog.ReportLogManager;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.debug.GolukDebugUtils;

public class WiFiLinkCompleteActivity extends BaseActivity implements OnClickListener, WifiConnCallBack,
		ILiveDialogManagerFn {

	private static final String TAG = "WiFiLinkBindAll";
	private static final String TAG_LOG = "WiFiLinkCompleteActivity";
	/** 创建手机热点消息 */
	private static final int MSG_H_CREATE_HOT = 100;
	/** 计时时间到，跳转到“等待”界面 */
	private static final int MSG_H_TO_WAITING_VIEW = 101;

	/** 设置IPC配置消息超时时间 */
	private static final int TIMEOUT_SETIPC = 6 * 1000;
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

	private final int STATE_SET_IPC_INFO = 0;
	private final int STATE_WAIT_CONN = 1;
	private final int STATE_SUCESS = 2;

	private int mState = STATE_SET_IPC_INFO;

	WifiManager mWifiManager = null;

	private int mStep = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_link_complete2);
		GFileUtils.writeLiveLog("WiFiLinkCompleteActivity------------onCreate-------:");
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(mContext, TAG);

		collectLog("onCreate", "-----1");
		mContext = this;
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(mWifiManager, this);
		initChildView();
		mMiddleLayout = (FrameLayout) findViewById(R.id.wifi_link_complete_frmelayout);
		mCompleteBtn = (Button) findViewById(R.id.complete_btn);
		mCompleteBtn.setOnClickListener(this);
		mProgressImg = (ImageView) findViewById(R.id.wifilink_progress);
		init();
		toSetIPCInfoView();
		SysApplication.getInstance().addActivity(this);

		setIpcLinkInfo();
		// 6秒后，没有配置成功，直接跳转“等待连接”界面
		mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_WAITING_VIEW, TIMEOUT_SETIPC);
	}

	private void initChildView() {
		layout1 = new WifiLinkSetIpcLayout(this);
		layout2 = new WifiLinkWaitConnLayout(this);
		layout3 = new WifiLinkSucessLayout(this);
	}

	private void collectLog(String method, String msg) {
		ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
				.addLogData(JsonUtil.getReportData(TAG_LOG, method, msg));

	}

	@Override
	protected void hMessage(Message msg) {
		if (MSG_H_CREATE_HOT == msg.what) {
			createPhoneHot();
		} else if (MSG_H_TO_WAITING_VIEW == msg.what) {
			if (STATE_SET_IPC_INFO == mState) {
				// 直接跳转下个界面
				toWaitConnView();
				// 开始创建手机热点
				mBaseHandler.sendEmptyMessageDelayed(MSG_H_CREATE_HOT, 3 * 1000);
			}
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
		collectLog("setIpcLinkInfo", "--setIpcLinkPhoneHot---1");
		final String json = getSetIPCJson();
		GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---2---josn---" + json);
		collectLog("setIpcLinkInfo", "--setIpcLinkPhoneHot---2---josn---" + json);
		boolean b = mApp.mIPCControlManager.setIpcLinkPhoneHot(json);
		if (!b) {
			// GolukUtils.showToast(mContext, "调用设置IPC连接热点失败");
		}
		GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---3---b---" + b);
		collectLog("setIpcLinkInfo", "--setIpcLinkPhoneHot---3---b---" + b);
	}

	private String getSetIPCJson() {

		collectLog("getSetIPCJson", "----1");

		// 连接ipc热点wifi---调用ipc接口
		GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---1");
		String json = "";
		if (null != WiFiInfo.IPC_PWD && !"".equals(WiFiInfo.IPC_PWD)) {
			json = "{\"AP_SSID\":\"" + WiFiInfo.IPC_SSID + "\",\"AP_PWD\":\"" + WiFiInfo.IPC_PWD
					+ "\",\"GolukSSID\":\"" + WiFiInfo.MOBILE_SSID + "\",\"GolukPWD\":\"" + WiFiInfo.MOBILE_PWD
					+ "\",\"GolukIP\":\"" + DEFAULT_IP + "\",\"GolukGateway\":\"" + DEFAULT_WAY + "\" }";
		} else {
			json = "{\"GolukSSID\":\"" + WiFiInfo.MOBILE_SSID + "\",\"GolukPWD\":\"" + WiFiInfo.MOBILE_PWD
					+ "\",\"GolukIP\":\"" + DEFAULT_IP + "\",\"GolukGateway\":\"" + DEFAULT_WAY + "\" }";
		}

		collectLog("getSetIPCJson", "---json: " + json);

		return json;
	}

	/**
	 * 设置IPC信息成功回调
	 */
	public void setIpcLinkWiFiCallBack(int state) {
		if (STATE_SET_IPC_INFO != mState) {
			return;
		}
		collectLog("setIpcLinkWiFiCallBack", "---1 :   " + state);
		if (0 == state) {
			mBaseHandler.removeMessages(MSG_H_TO_WAITING_VIEW);
			// 隐藏loading
			toWaitConnView();
			// 开始创建手机热点
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_CREATE_HOT, 3 * 1000);
			GolukDebugUtils.e("",
					"WJUN_____IPC_VDCP_TransManager_OnParserData设置热点信息成功回调-----Java-----setIpcLinkWiFiCallBack");

			collectLog("setIpcLinkWiFiCallBack", "--------: 2");

		} else {
			GolukDebugUtils.e("", "WJUN_____IPC_VDCP_TransManager_OnParserData-----失败----------");

			collectLog("setIpcLinkWiFiCallBack", "---: 3 failed:  " + connectCount);

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
		String wifiName = WiFiInfo.MOBILE_SSID;
		String pwd = WiFiInfo.MOBILE_PWD;
		String ipcssid = WiFiInfo.IPC_SSID;
		String ipcmac = WiFiInfo.IPC_MAC;
		// 创建热点之前先断开ipc连接
		mApp.mIPCControlManager.setIPCWifiState(false, "");
		// 改变Application-IPC退出登录
		mApp.setIpcLoginOut();
		// 调用韩峥接口创建手机热点
		GolukDebugUtils
				.e("", "创建手机热点---startWifiAp---1---" + wifiName + "---" + pwd + "---" + ipcssid + "---" + ipcmac);

		collectLog("createPhoneHot", wifiName + "---" + pwd + "---" + ipcssid + "---" + ipcmac);

		mWac.createWifiAP(wifiName, pwd, ipcssid, ipcmac);
	}

	/**
	 * 通知logic连接ipc
	 */
	private void sendLogicLinkIpc(String ip, String ipcmac) {
		// 连接ipc热点wifi---调用ipc接口
		GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1---ip---" + ip);
		collectLog("sendLogicLinkIpc", "11--ip: " + ip + "   ipcmac:" + ipcmac);
		GolukApplication.mIpcIp = ip;
		mIpcMac = ipcmac;
		mWiFiIp = ip;
		boolean b = mApp.mIPCControlManager.setIPCWifiState(true, ip);
		GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);

		collectLog("sendLogicLinkIpc", "2---b:  " + b);
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
		GFileUtils
				.writeLiveLog("WifiLinkCompleteActivity-----------ipcLinkWiFiCallBack    Bind Sucess ! Bind Sucess ! Bind Sucess! ");

		collectLog("ipcLinkWiFiCallBack", "*****   Bind Sucess ! *****");

		// 设置绑定成功
		ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setType(ReportLog.TYPE_SUCESS);
		reportLog();

		this.toSucessView();
		// 保存连接数据
		WifiRsBean beans = new WifiRsBean();
		beans.setIpc_mac(mIpcMac);
		beans.setIpc_ssid(WiFiInfo.IPC_SSID);
		beans.setIpc_ip(mWiFiIp);
		beans.setIpc_pass(WiFiInfo.IPC_PWD);

		beans.setPh_ssid(WiFiInfo.MOBILE_SSID);
		beans.setPh_pass(WiFiInfo.MOBILE_PWD);

		mWac.saveConfiguration(beans);
		saveBind(WiFiInfo.IPC_SSID);
		// 保存绑定标识
		saveBindMark();
	}

	private void reportLog() {
		final String jsonData = ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
				.getReportData();
		mApp.uploadMsg(jsonData, false);
		ReportLogManager.getInstance().removeKey(IMessageReportFn.KEY_WIFI_BIND);
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
			collectLog("onKeyDown", " 11111");
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

		GolukDebugUtils.e("", "jyf-----WifiBind-----WifiCompelete-----onDestroy----");

		collectLog("onDestroy", "1");
		if (null != layout1) {
			layout1.free();
		}

		if (null != layout2) {
			layout2.free();
		}

		if (null != layout3) {
			layout3.free();
		}

	}

	@Override
	protected void onResume() {
		mApp.setContext(this, TAG);
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
		super.onResume();
	}

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
				if (null != mWac) {
					mWac.unbind();
				}
				mWac = null;
				GolukApplication.getInstance().stopDownloadList();//停止视频同步
				Intent it = new Intent(WiFiLinkCompleteActivity.this, CarRecorderActivity.class);
				it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(it);
				GFileUtils.writeLiveLog("WifiLinkCompleteActivity---------- Jump CarRecorderActivity----- ");

				// collectLog("onClick", " Jump CarRecorderActivity----- ");
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

	private void wifiCallBack_ipcConnHotSucess(String message, Object arrays) {
		WifiRsBean[] bean = (WifiRsBean[]) arrays;
		if (null == bean) {
			return;
		}
		GolukDebugUtils.e("", "自动wifi链接IPC连接上WIFI热点回调---length---" + bean.length);
		collectLog("wifiCallBack_ipcConnHotSucess", "自动wifi链接IPC连接上WIFI热点回调---length---" + bean.length);
		if (bean.length > 0) {
			GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1---ip---");
			collectLog("wifiCallBack_ipcConnHotSucess", "1");
			sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
		} else {
			collectLog("wifiCallBack_ipcConnHotSucess", "2 ");
		}
	}

	private void wifiCallBack_sameHot() {

	}

	private void wifiCallBack_3(int state, int process, String message, Object arrays) {
		if (state == 0) {
			switch (process) {
			case 0:
				// 创建热点成功
				break;
			case 1:
				// ipc成功连接上热点
				try {
					WifiRsBean[] bean = (WifiRsBean[]) arrays;
					if (null != bean) {
						GolukDebugUtils.e("", "IPC连接上WIFI热点回调---length---" + bean.length);
						collectLog("wifiCallBack_3", "IPC conn hot---length:" + bean.length);
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
			connFailed();
		}
	}

	private void wifiCallBack_5(int state, int process, String message, Object arrays) {
		if (state == 0) {
			switch (process) {
			case 0:
				// 创建热点成功
				break;
			case 1:
				// ipc成功连接上热点
				wifiCallBack_ipcConnHotSucess(message, arrays);
				break;
			case 2:
				// 用户已经创建与配置文件相同的热点，
				wifiCallBack_sameHot();
				break;
			case 3:
				// 用户已经连接到其它wifi，按连接失败处理
				connFailed();
				break;
			default:
				break;
			}
		} else {
			// 未连接
			connFailed();
		}
	}

	private void connFailed() {
		GFileUtils.writeLiveLog("WifiLinkCompleteActivity-----------connFailed : " + mStep);
		collectLog("connFailed", "WifiLinkCompleteActivity-----------connFailed : " + mStep);
		if (0 == mStep) {
			GFileUtils.writeLiveLog("WifiLinkCompleteActivity-----------connFailed show Dialog 请先将极路客断电5~10秒");
			collectLog("connFailed", "connFailed show Dialog  please 5~10s");
			// 弹框提示用户重启GoLUK
			LiveDialogManager.getManagerInstance()
					.showSingleBtnDialog(this, LiveDialogManager.DIALOG_TYPE_WIFIBIND_RESTART_IPC, "提示",
							"请先将极路客断电5~10秒，然后上电重新启动，点确认按钮，等待极路客连接到手机");
			mStep++;
		} else {
			GFileUtils.writeLiveLog("WifiLinkCompleteActivity-----------connFailed show Dialog 极路客绑定失败");
			collectLog("connFailed", "connFailed show Dialog Conn Failed");
			// 提示用户绑定失败，重新退出程序绑定
			LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_WIFIBIND_FAILED, "提示", "极路客绑定失败，请您重试");
		}
	}

	@Override
	public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
		final String log = " type---" + type + "---state---" + state + "---process---" + process + "---message---"
				+ message;

		collectLog("wifiCallBack", log);

		GolukDebugUtils.e("", log);

		GFileUtils.writeLiveLog(log);

		switch (type) {
		case 3:
			wifiCallBack_3(state, process, message, arrays);
			break;
		case 5:
			wifiCallBack_5(state, process, message, arrays);
			break;
		default:
			break;
		}
	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (LiveDialogManager.DIALOG_TYPE_WIFIBIND_RESTART_IPC == dialogType) {
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				GFileUtils
						.writeLiveLog("WifiLinkCompleteActivity-----------dialogManagerCallBack DIALOG_TYPE_WIFIBIND_RESTART_IPC");

				collectLog("dialogManagerCallBack", "DIALOG_TYPE_WIFIBIND_RESTART_IPC---clickOK");

				mWac = new WifiConnectManager(mWifiManager, this);
				mWac.autoWifiManage(WiFiInfo.IPC_SSID, WiFiInfo.IPC_PWD, WiFiInfo.MOBILE_SSID, WiFiInfo.MOBILE_PWD);
				mStep++;
			}
		} else if (LiveDialogManager.DIALOG_TYPE_WIFIBIND_FAILED == dialogType) {
			GFileUtils
					.writeLiveLog("WifiLinkCompleteActivity-----------dialogManagerCallBack DIALOG_TYPE_WIFIBIND_FAILED");

			collectLog("dialogManagerCallBack", "DIALOG_TYPE_WIFIBIND_FAILED---onclick");

			ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setType(ReportLog.TYPE_FAILED);
			reportLog();

			LiveDialogManager.getManagerInstance().dismissSingleBtnDialog();
			SysApplication.getInstance().exit();
			if (null != mWac) {
				mWac.unbind();
			}
			mWac = null;
		}

	}

}
