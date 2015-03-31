package cn.com.mobnote.wifi;

import java.util.regex.Pattern;

import cn.com.mobnote.util.console;
import cn.com.mobnote.wifi.WifiConnectManagerSupport.WifiCipherType;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WifiAutoConnectManager extends BroadcastReceiver {
	String ssid = "";
	String password = "";
	private static final String TAG = WifiAutoConnectManager.class
			.getSimpleName();
	// private static final String DEF_PASS = "cellstar2001hanzhengdujing"; //
	// 默认密码
	private static final String FILENAME = "wifi.config"; // 默认文件名
	private static final String DEF_STITILE = "TC";// wifi默认前缀
	// private static final String DEF_SSID = "angmafan"; // SSid

	private static final int TIMEER = 2000; // 定时器间隔时间
	// private boolean runState = false; // 是否已经运行
	private WifiConnCallBack callback = null;

	private WifiManager wifiManager = null;
	private WifiConnectManagerSupport wifiSupport = null;
	private Context context = null;
	private int count = 0;
	private WifiRsBean[] beans = null;
	// 实现定时器动能
	Handler handlerTime = new Handler();
	Runnable timeRunnable = new Runnable() {

		@Override
		public void run() {
			// handler自带方法实现定时器
			try {
				console.log( "time----------------2------count---" + count);

				// 如果12秒没有返回 返回连接错误
				if (count == 100) {
					count = 0;
					handlerTime.removeCallbacks(timeRunnable);
					sendError();
                         return;
				}
				State wifiState = isWifiConnected(context);
				// 如果连接成功
				if (wifiState != null && wifiState.equals(State.CONNECTED)) {
					Log.d(TAG, "time----------------3");
					count = 0;
					wifiSupport.writePassFile(FILENAME, ssid, password);

					handlerTime.removeCallbacks(timeRunnable);
					handler.sendEmptyMessage(1);

				}
				else { // 非连接成功状态 轮询等待
					Log.d(TAG, "wifiState----------------" + wifiState);
					handlerTime.postDelayed(timeRunnable, TIMEER);
					count++;

				}

			}
			catch (Exception e) {
				e.printStackTrace();
				sendError();

			}
		}
	};
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {
			WifiRsBean[] beans = new WifiRsBean[1];
			WifiRsBean bean = new WifiRsBean();
			bean.setWifiName(ssid);
			beans[0] = bean;
			switch (msg.what) {
			// 成功
			case 1: {

				callback.wifiCallBack(msg.what, "wifi连接成功", beans);
				break;
			}
			// 负数失败 （wifi连接失败）
			case -1: {

				callback.wifiCallBack(msg.what, "wifi连接失败", beans);
				break;
			}// 负数失败(文件列表异常)
			case -2: {
				callback.wifiCallBack(msg.what, "获取wifi列表异常", null);
				break;
			}
			case -3: {
				callback.wifiCallBack(msg.what, "获取wifi列表异常", beans);
				break;
			}
			// 获取wifi列表成功
			case 11: {
				callback.wifiCallBack(msg.what, "获取wifi列表成功",
						(WifiRsBean[]) msg.obj);
				break;
			}
			// wifi匹配
			case 21: {
				callback.wifiCallBack(msg.what, "wifi匹配成功",
						(WifiRsBean[]) msg.obj);
				break;
			}
			}
		}
	};

	// 构造函数
	public WifiAutoConnectManager(WifiManager wifiManager,
			WifiConnCallBack callback) {
		this.wifiManager = wifiManager;
		this.wifiSupport = new WifiConnectManagerSupport(wifiManager);
		this.callback = callback;
		this.context = (Context) callback;
	}

	// 提供一个外部接口，传入要连接的无线网
	public void connect(String ssid, String password, WifiCipherType type) {
		Thread thread = new Thread(new ConnectRunnable(ssid, password, type));

		thread.start();
	}

	// 提供一个外部接口，传入要连接的无线网
	public void connect() {
		Thread thread = new Thread(new ConnectRunnable(null, null,
				WifiCipherType.WIFICIPHER_WPA));

		thread.start();
	}

	class ConnectRunnable implements Runnable {

		private WifiCipherType type;

		public ConnectRunnable(String _ssid, String _password,
				WifiCipherType type) {
			ssid = _ssid;
			password = _password;
			this.type = type;
		}

		@Override
		public void run() {
			Message msg = new Message();
			String[] ssid_pass = null;
			try {
				if(!openWifi()){
					handler.sendEmptyMessage(-1);
					return ;
				}
				

				// 如果没有ssid 从配置文件中读取
				if (ssid == null) {
					ssid_pass = wifiSupport.readPassFile(FILENAME);
					if (ssid_pass == null) {
						Log.d(TAG, "wifiConfig ssid is null!");
						// 没有ssid
						handler.sendEmptyMessage(-1);
						return;
					}
					ssid = ssid_pass[0];
					password = ssid_pass[1];
				}
				// 如果不存在待连接的ssid 返回错误
				if (!wifiSupport.inWifiGroup(ssid, beans)) {

					handler.sendEmptyMessage(-1);
					return;
				}

				WifiInfo info = wifiManager.getConnectionInfo();
				if (info != null) {
					if ((info.getSSID()).equals(("\"" + ssid) + "\"")
							|| (info.getSSID()).equals(ssid)) {
						// 如果已经连接了与ssid相同的网络 不去连接 直接返回
						handler.sendEmptyMessage(-3);
						return;
					}

				}
				WifiConfiguration tempConfig = wifiSupport.isExsits(ssid);
				if (tempConfig != null) {

					wifiManager.removeNetwork(tempConfig.networkId);
				}
				WifiConfiguration wifiConfig = wifiSupport.createWifiInfo(ssid,
						password, type);
				//
				if (wifiConfig == null) {
					Log.d(TAG, "wifiConfig is null!");
					// 配置错误
					handler.sendEmptyMessage(-1);
					return;
				}

				int netID = wifiManager.addNetwork(wifiConfig);

				wifiManager.reconnect();

				wifiManager.enableNetwork(netID, true);

				// 去定时器检查
				handlerTime.sendMessage(msg);
				Log.d(TAG, "time----------------1");
				handlerTime.postDelayed(timeRunnable, TIMEER);
				return;
			}
			catch (Exception e) {
				e.printStackTrace();
				sendError();
			}
		}

	}

	/**
	 * 写入配置文件
	 * 
	 * @param ssid
	 * @param passWord
	 * @return
	 */
	public boolean saveWifiConfig(String ssid, String passWord) {
		try {
			// 连接wifi
			this.connect(ssid, passWord, WifiCipherType.WIFICIPHER_WPA);
			// wifiSupport.writePassFile(FILENAME, DEF_STITILE + ssid,
			// passWord);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 获取前缀匹配的wifi列表
	 * 
	 * @return
	 */
	public void getWifiList() {
		Thread thread = new Thread(new ConnectOppenable());
		thread.start();
	}

	class ConnectOppenable implements Runnable {
		@Override
		public void run() {
			if(!openWifi()){
				handler.sendEmptyMessage(-1);
				return ;
			}
			Message msg = new Message();
			msg.what = 11;

			WifiRsBean[] rsArray = wifiSupport.getScanResult(DEF_STITILE);

			msg.obj = rsArray;
			handler.sendMessage(msg);

		}
	}

	public void closeWifi() {
		wifiSupport.closeWifi();
		count = 0;
	}

	// /**
	// * 运行状态
	// *
	// * @param state
	// */
	// private void checkRunState(boolean state) {
	// this.runState = state;
	// }

	private State isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				// Log.d(TAG,
				// "mWiFiNetworkInfo----------------"+mWiFiNetworkInfo.getState());

				return mWiFiNetworkInfo.getState();
			}
		}
		return null;
	}

	private void sendError() {
		handler.sendEmptyMessage(-1);
		
		//连接失败,为什么要关闭wifi?
		//closeWifi();
	}

	private boolean openWifi() {
		// 打开wifi
		wifiSupport.openWifi();
		int count = 0;
		// 判断 是否获取了wifi列表
		beans = wifiSupport.getScanResult("");
		Log.d(TAG, "wificount---------wificount11111111" + (beans == null));
		while (beans == null) {
			try {
				// 为了避免程序一直while循环，让它睡个100毫秒检测……
				Thread.sleep(500);
				// 如果循环十次附近没有wifi 返回错误

				//Log.d(TAG, "wificount---------wificount222222" + count);
				console.log("wificount---------wificount222222" + count);
				if (count == 100) {

					return false;
				}
				count++;
			}
			catch (InterruptedException ie) {
				ie.printStackTrace();
				sendError();
			}

		}
		return true;
	}

	/**
	 * 获取加入的连接是否是有效连接
	 * 
	 * @param ssid
	 */
	public boolean getEffectiveWifi(String ssid) {
		return effectiveWifi(ssid);
	}

	/**
	 * 获取加入的连接是否是有效连接
	 * 
	 * @param wifiInfo
	 */
	public boolean getEffectiveWifi(WifiInfo wifiInfo) {

		if (wifiInfo != null) {
			return effectiveWifi(wifiInfo.getSSID());
		}
		return false;
	}

	private boolean effectiveWifi(String ssid) {
		if (null != ssid) {
			WifiRsBean[] beans = new WifiRsBean[1];
			WifiRsBean bean = new WifiRsBean();
			bean.setWifiName(ssid);
			beans[0] = bean;
			String regEx = "^(" + DEF_STITILE + "|\"" + DEF_STITILE + ")";
			boolean result = Pattern.compile(regEx).matcher(ssid).find();
			return result;
		}
		else {
			return false;
		}
		// Message msg = new Message();

		// msg.obj=beans;
		// if (result) {
		// msg.what = 21;
		//
		// } else {
		// msg.what = -21;
		//
		// }
		// handler.sendMessage(msg);
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		WifiRsBean[] beans_temp = wifiSupport.getScanResult("");
		if (beans_temp != null) {
			beans = beans_temp;
		}
	}

}