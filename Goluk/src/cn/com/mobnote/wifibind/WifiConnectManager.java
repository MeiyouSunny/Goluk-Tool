package cn.com.mobnote.wifibind;



import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManagerSupport.WifiCipherType;

import android.annotation.SuppressLint;

import android.net.wifi.WifiManager;

import android.os.Handler;
import android.os.Message;

import android.util.Log;

public class WifiConnectManager {

	private static final String TAG = "testhan";

	private WifiConnCallBack callback = null;

	private WifiManager wifiManager = null;
	private WifiConnectManagerSupport wifiSupport = null;

	WifiConnectManagerSupport support = null;

	// 构造函数
	public WifiConnectManager(WifiManager wifiManager, WifiConnCallBack callback) {
		this.wifiManager = wifiManager;
		this.wifiSupport = new WifiConnectManagerSupport(wifiManager);
		this.callback = callback;

		// 初始化wifi工具类
		support = new WifiConnectManagerSupport(wifiManager);
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {

			switch (msg.what) {
			// 扫描列表成功
			case 1: {
				Log.e(TAG, "return --------------ok--------");
				callback.wifiCallBack(msg.what, "wifi扫描成功", msg.obj);
				break;
			}
			// 连接wifi成功
			case 11: {
				Log.e(TAG, "return --------------ok--------");
				callback.wifiCallBack(msg.what, "wifi连接成功", msg.obj);
				break;
			}
			// 创建热点成功
			case 21: {
				callback.wifiCallBack(msg.what, "获取wifiap列表成功", msg.obj);
				break;
			}
			// 获取加入热点信息
			case 22: {
				callback.wifiCallBack(msg.what, "wifi匹配成功", msg.obj);
				break;
			}

			// --------------------------------失败-----------------------//
			// 负数失败 
			case -1: {
				callback.wifiCallBack(msg.what, "扫描wifi列表超时", null);
				break;
			}

			case -11: {
				callback.wifiCallBack(msg.what, "连接wifi超时", null);
				break;
			}
			case -12: {
				callback.wifiCallBack(msg.what, "附近没有可连接的wifi", null);
				break;
			}
			case -13: {
				callback.wifiCallBack(msg.what, "连接wifi不匹配", null);
				break;
			}
			case -14: {
				callback.wifiCallBack(msg.what, "连接wifi失败", null);
				break;
			}
			}
		}
	};

	/**
	 * 提供一个外部接口，传入要连接的无线网
	 * 
	 * @param ssid
	 * @param password
	 * @param type
	 * @param outTime
	 */
	public void connectWifi(final String ssid, final String password,
			final String mac, final WifiCipherType type, final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {
				WifiRsBean[] beans = null;
				// 1：打开wifi
				int openTime = openWifi(outTime);
				// 处理超时
				if (openTime == 0) {
					msg.what = -11;
					handler.sendMessage(msg);
					return;
				}
				// wifi 打开后进行连接 检查用时
				List<WifiRsBean> list = new ArrayList<WifiRsBean>();
				openTime = getwifiList(openTime, list);
				if (list.size() > 0) {
					beans = (WifiRsBean[]) list.toArray(new WifiRsBean[0]);
				}
				// 超时 报错返回
				if (openTime == 0) {
					msg.what = -11;
					handler.sendMessage(msg);
					return;
				}
				// 如果周围没有连接不再继续进行--------------------------------------
				if (beans == null) {
					msg.what = -12;
					msg.obj = null;
					handler.sendMessage(msg);
					return;
				}
				// //
				// //如果要连接的用户不在列表中或者mac地址匹配错误--------------------------------------
				// if (!wifiSupport.inWifiGroup(ssid, mac, beans)) {
				// msg.what = -13;
				// msg.obj = null;
				// handler.sendMessage(msg);
				// return;
				// }

				// openWifi(openTime);
				//将链接置null
				list=null;
				boolean connFlag = wifiSupport.joinWifiInfo(ssid, password,
						type);
				// 连接成功
				if (connFlag) {
					msg.what = 11;
					msg.obj = wifiSupport.getConnResult();
					handler.sendMessage(msg);
				} else { // 连接失败
					msg.what = -14;
					msg.obj = null;
					handler.sendMessage(msg);
				}
			};
		};
		Thread mythread = new Thread(runnable);
		mythread.start();

	}

	/**
	 * 通过关键字查询列表信息
	 * 
	 * @param matching
	 *            关键字
	 * @param outTime
	 *            超时时间
	 */
	public Thread scanWifiList(final String matching, final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {
				// 1：打开wifi
				int openTime = openWifi(outTime);
				// 超时错误
				if (openTime == 0) {
					msg.what = -1;

					handler.sendMessage(msg);
					return;
				}
				// 如果wifi打开了
				if (wifiScan(matching, openTime) == 0) {
					msg.what = -1;
					handler.sendMessage(msg);
					return;
				}
			};

		};
		Thread mythread = new Thread(runnable);
		mythread.start();
		return mythread;
	}

	/**
	 * 打开wifi 状态;
	 */
	private int openWifi(int outTime) {
		int tempTime = 0;
		// 开启wifi 等待结果
		boolean flag = wifiSupport.openWifi();
		// 如果没有开启wifi功能 等待1.5秒后检查wifi 的链接状态
		// 有可能手机当前状态已经开启wifi
		if (!flag) {
			try {
				int temp_1 = 1500;
				Thread.sleep(temp_1);
				// 耗时 1500毫秒
				tempTime += temp_1;
				if (tempTime > outTime) {
					return 0;
				}
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		// 如果没有扫描到列表 循环等待
		while (!wifiManager.isWifiEnabled()) {
			try {
				int temp_2 = 100;
				Thread.sleep(temp_2);
				tempTime += temp_2;
				if (tempTime > outTime) {
					return 0;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.e(TAG, "opentime----------------" + (outTime - tempTime)
				+ "-------------");
		return outTime - tempTime;
	}

	/**
	 * 获取wifi列表
	 * 
	 * @param outTime
	 * @param beans
	 *            用于返回
	 * @return
	 */
	private int getwifiList(int outTime, List<WifiRsBean> beans) {
		int tempTime = 0;
		Log.e(TAG, "sagetwifiListn----------------start-------------");
		// 扫描了表不为null
		while (wifiManager.getScanResults() == null) {
			try {
				int temp_1 = 200;
				Thread.sleep(temp_1);
				tempTime += temp_1;
				if (tempTime > outTime) {
					return 0;
				}
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		WifiRsBean[] wifiArray = wifiSupport.getScanResult("");
		if (wifiArray != null) {
			for (WifiRsBean temp : wifiArray) {
				beans.add(temp);
			}

		}
		// beans= wifiSupport.getScanResult("");
		return outTime - tempTime;
	}

	/**
	 * 扫描wifi android 4.x以上matching
	 * 
	 * @param matching
	 * @param type
	 */
	private int wifiScan(String matching, int outTime) {
		Log.e(TAG, "san----------------start-------------");
		// 扫描了表不为null
		int tempTime = 0;
		while (wifiManager.getScanResults() == null) {
			try {
				int temp_1 = 200;
				Thread.sleep(temp_1);
				tempTime += temp_1;
				if (tempTime > outTime) {
					return 0;
				}
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		WifiRsBean[] wifiBean = wifiSupport.getScanResult(matching);
		// 将得到的扫描列表返回
		Message msg = new Message();
		msg.what = 1;
		msg.obj = wifiBean;
		handler.sendMessage(msg);
		return outTime - tempTime;
	}

	/**
	 * 创建热点并返回连接列表
	 * 
	 * @param ssid
	 * @param password
	 * @param outTime
	 */
	public void createWifiAP(final String ssid, final String password,
			int outTime) {

		Runnable runnable = new Runnable() {
			public void run() {
				try {
					wifiSupport.closeWifiAp(wifiManager);
					wifiSupport.createWifiHot(ssid, password);
				} catch (Exception e) {
					// TODO Auto-generated catch block 这里需要异常处理
					e.printStackTrace();
				}

				// 如果wifi打开了
				while (!wifiManager.isWifiEnabled()) {

				}
				Message msg = new Message();
				msg.what = 21;
				msg.obj = wifiSupport.getConnResult();
				handler.sendMessage(msg);
				// 获取wifi连接列表
				getClientList();

			};

		};
		Thread mythread = new Thread(runnable);
		mythread.start();
	}

	/**
	 * 获取加入wifiAP的用户列表
	 * 
	 */
	public void getClientList() {

		Runnable runnable = new Runnable() {
			public void run() {
				// 获取wifi 扫描列表
				while (wifiSupport.getJoinApList(false, 300) == null) {

				}

				Message msg = new Message();
				msg.what = 22;
				msg.obj = wifiSupport.getJoinApList(false, 300);
				handler.sendMessage(msg);

			};

		};

		Thread mythread = new Thread(runnable);
		mythread.start();
	}

}