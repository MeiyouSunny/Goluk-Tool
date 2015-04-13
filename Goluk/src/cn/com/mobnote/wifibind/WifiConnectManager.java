package cn.com.mobnote.wifibind;


import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

 
import cn.com.mobnote.wifibind.WifiConnectManagerSupport.WifiCipherType;

import android.annotation.SuppressLint;
import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;

import android.os.Handler;
import android.os.Message;

import android.util.Log;

public class WifiConnectManager implements WifiConnectInterface {

	private static final String TAG = "testhan";
	private static final String WIFICONFIG = "wifi.config";
	private WifiConnCallBack callback = null;

	private WifiManager wifiManager = null;
	private WifiConnectManagerSupport wifiSupport = null;
	private Context context = null;
	WifiConnectManagerSupport support = null;
	ConnectivityManager connectivity = null;

	// 构造函数
	public WifiConnectManager(WifiManager wifiManager, Object callback) {
		this.wifiManager = wifiManager;
		this.wifiSupport = new WifiConnectManagerSupport(wifiManager);
		this.callback = (WifiConnCallBack) callback;
		this.context = (Context) callback;
		// 初始化wifi工具类
		support = new WifiConnectManagerSupport(wifiManager);
	}

	// // 构造函数
	// public WifiConnectManager(WifiManager wifiManager, WifiConnCallBack
	// callback,ConnectivityManager _connectivity) {
	// this.wifiManager = wifiManager;
	// this.wifiSupport = new WifiConnectManagerSupport(wifiManager);
	// this.callback = callback;
	// this.context =context;
	// this.connectivity=_connectivity;
	// // 初始化wifi工具类
	// support = new WifiConnectManagerSupport(wifiManager);
	// }
	/**
	 * 通过用户名，密码连接ipc
	 * 
	 * @param ssid
	 * @param password
	 * @param type
	 */
	public void connectWifi(String ssid, String password, WifiCipherType type) {
		connectWifi(ssid, password, "", type, 30000);
	}

	/**
	 * 启动软件后自动管理wifi
	 */
	public void autoWifiManage() {
		autoWifiManage(300000);
	}

	/**
	 * 通过用户名密码创建wifi热点
	 * 
	 * @param ssid
	 * @param password
	 */
	public void createWifiAP(String ph_ssid, String ph_password,
			String ipc_ssid, String ipc_mac) {
		createWifiAP("3", ph_ssid, ph_password, ipc_ssid, ipc_mac, "", 30000);
	}

	/**
	 * 通过关键字查询列表信息
	 * 
	 * @param matching
	 *            关键字
	 */
	public void scanWifiList(String matching, boolean reset) {
		scanWifiList(matching, reset, 30000);
	}

	/**
	 * 保存配置信息
	 * 
	 * @param beans
	 */
	public void saveConfiguration(WifiRsBean beans) {
		saveConfiguration(beans, 30000);
	}

	/**
	 * 关闭wifi 及AP
	 * 
	 * @return
	 */
	public boolean closeWifiAP() {
		wifiSupport.closeWifi();
		wifiSupport.closeWifiAp(wifiManager);
		return true;
	}

	public void isConnectIPC() {
		isConnectIPC(30000);
	}

	// -------------------------------以上为封装后的对外接口----------------------------------------//
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {

			switch (msg.what) {
			// 扫描列表成功
			case 11: {
				Log.e(TAG, "return --------------ok--------");
				callback.wifiCallBack(1, 0, 0, "wifi扫描成功", msg.obj);
				break;
			}
			// 连接wifi成功
			case 21: {
				Log.e(TAG, "return --------------ok--------");
				callback.wifiCallBack(2, 0, 0, "wifi连接成功", msg.obj);
				break;
			}
			// 创建热点成功
			case 31: {
				callback.wifiCallBack(3, 0, 0, "创建wifiap成功", msg.obj);
				break;
			}
			// 获取加入热点信息
			case 32: {
				callback.wifiCallBack(3, 0, 1, "创建wifiap有客户端加入", msg.obj);
				break;
			}
			// 保存wifi配置成功
			case 41: {
				callback.wifiCallBack(4, 0, 0, " 保存wifi配置成功", msg.obj);
				break;
			}
			case 51: {
				callback.wifiCallBack(5, 0, 0, "自动连接成功", msg.obj);
				break;
			}
			case 52: {
				callback.wifiCallBack(5, 0, 1, "自动连接--客户端已经连接", msg.obj);
				break;
			}
			case 53: {
				callback.wifiCallBack(5, 0, 1, "自动连接--当前已经连接", msg.obj);
				break;
			}

			// --------------------------------失败-----------------------//
			// 负数失败
			case -11: {
				callback.wifiCallBack(1, -1, msg.what, "扫描wifi列表超时", null);
				break;
			}

			case -21: {
				callback.wifiCallBack(2, -1, msg.what, "连接wifi超时", null);
				break;
			}
			case -22: {
				callback.wifiCallBack(2, -1, msg.what, "附近没有可连接的wifi", null);
				break;
			}
			case -23: {
				callback.wifiCallBack(2, -1, msg.what, "连接wifi不匹配", null);
				break;
			}
			case -24: {
				callback.wifiCallBack(2, -1, msg.what, "连接wifi失败", null);
				break;
			}

			case -41: {
				callback.wifiCallBack(4, -1, 0, "保存wifi配置失败", msg.obj);
				break;
			}
			case -51: {
				callback.wifiCallBack(5, -1, msg.what, "自动连接超时", msg.obj);
				break;
			}
			case -52: {
				callback.wifiCallBack(5, -1, msg.what, "附近没有可连接的ipc", msg.obj);
				break;
			}
			case -53: {
				callback.wifiCallBack(5, -1, msg.what, "自动连接失败", msg.obj);
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
	private void connectWifi(final String ssid, final String password,
			final String mac, final WifiCipherType type, final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {

				wifiSupport.closeWifiAp(wifiManager);
				// 如果当前网络未开启 连接失败后 需要再关闭网络
				boolean doClose = false;
				WifiRsBean[] beans = null;
				// 1：打开wifi
				int openTime = openWifi(false, outTime);
				// 代表当前网络已经开启
				if (openTime != outTime) {
					doClose = true;
				}
				// 处理超时
				if (openTime == 0) {
					if (doClose) {
						wifiSupport.closeWifi();
					}
					msg.what = -21;
					handler.sendMessage(msg);
					return;
				}
				// wifi 打开后进行连接 检查用时
				List<WifiRsBean> list = new ArrayList<WifiRsBean>();
				openTime = getwifiList(list, "", openTime);
				if (list.size() > 0) {
					beans = (WifiRsBean[]) list.toArray(new WifiRsBean[0]);
				}
				// 超时 报错返回
				if (openTime == 0) {
					if (doClose) {
						wifiSupport.closeWifi();
					}
					msg.what = -21;
					handler.sendMessage(msg);
					return;
				}
				// 如果周围没有连接不再继续进行--------------------------------------
				if (beans == null) {
					if (doClose) {
						wifiSupport.closeWifi();
					}
					msg.what = -22;
					msg.obj = null;
					handler.sendMessage(msg);
					return;
				}
				//
				// 如果要连接的用户不在列表中或者mac地址匹配错误--------------------------------------
				if (!wifiSupport.inWifiGroup(ssid, beans)) {
					if (doClose) {
						wifiSupport.closeWifi();
					}
					msg.what = -23;
					msg.obj = null;
					handler.sendMessage(msg);
					return;
				}

				// openWifi(openTime);
				// 将链接置null
				list = null;
				boolean connFlag = wifiSupport.joinWifiInfo(ssid, password,
						type);

				// 连接wifi指令成功
				if (connFlag) {

					openTime = getConnState(ssid, openTime);
					// 超时 报错返回
					if (openTime == 0) {
						wifiSupport.disConnWifi();
						if (doClose) {
							wifiSupport.closeWifi();
						}

						msg.what = -24;
						handler.sendMessage(msg);

						return;
					}
					msg.what = 21;
					msg.obj = wifiSupport.getConnResult();
					handler.sendMessage(msg);
					return;
				} else { // 连接失败
					wifiSupport.disConnWifi();

					wifiSupport.closeWifi();

					msg.what = -24;
					msg.obj = null;
					handler.sendMessage(msg);
				}
			};
		};
		Thread mythread = new Thread(runnable);
		mythread.start();

	}

	/**
	 * 获取是否连接状态
	 * 
	 * @param outTime
	 * @return
	 */
	private int getConnState(String ssid, int outTime) {
		int tempTime = 0;
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

		while (state != State.CONNECTED) {

			Log.e(TAG, "crssssssssss----------------" + state + "");
			try {
				int temp_2 = 200;
				Thread.sleep(temp_2);
				tempTime += temp_2;
				if (tempTime > outTime) {
					return 0;
				}
				state = connectivity.getNetworkInfo(
						ConnectivityManager.TYPE_WIFI).getState();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return outTime - tempTime;
	}

	/**
	 * 通过关键字查询列表信息
	 * 
	 * @param matching
	 *            关键字
	 * @param outTime
	 *            超时时间
	 */
	private Thread scanWifiList(final String matching, final boolean reset,
			final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {
				// 1：打开wifi
				int openTime = openWifi(reset, outTime);
				// 超时错误
				if (openTime == 0) {
					msg.what = -11;

					handler.sendMessage(msg);
					return;
				}

				// 如果wifi打开了
				if (wifiScan(matching, openTime) == 0) {
					msg.what = -11;
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
	private int openWifi(boolean restart, int outTime) {
		int tempTime = 0;

		// 开启wifi 等待结果
		boolean flag = wifiSupport.openWifi(restart);
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
	private int getwifiList(List<WifiRsBean> beans, String ssid, int outTime) {
		int tempTime = 0;
		Log.e(TAG, "sagetwifiListn----------------start-------------");
		// 扫描了表不为null
		while (wifiManager.getScanResults() == null
				|| wifiManager.getScanResults().size() == 0) {
			try {
				int temp_1 = 200;
				Thread.sleep(temp_1);
				tempTime += temp_1;
				if (tempTime > outTime) {
					Log.e(TAG,
							"sagetwifiListn----------------chaoshi-------------");
					return 0;
				}
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e(TAG, "sagetwifiListn----------------" + tempTime
				+ "-------------");

		WifiRsBean[] wifiArray = wifiSupport.getScanResult(ssid);
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
		int tempTime = 0;
		// 先提至500毫秒再进行查询
		try {
			Thread.sleep(500);
			tempTime = 500;
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// 扫描了表不为null

		while (wifiManager.getScanResults() == null
				|| wifiManager.getScanResults().size() == 0) {
			try {
				int temp_1 = 200;
				Thread.sleep(temp_1);
				tempTime += temp_1;
				if (tempTime > outTime) {
					Log.e(TAG, "san----------------outTime-------------");
					return 0;
				}
			} catch (InterruptedException e) {
				Log.e(TAG, "san-----------------" + e + "------------");
				e.printStackTrace();
			}
		}
		Log.e(TAG, "san----------------no empty-------------");
		WifiRsBean[] wifiBean = wifiSupport.getScanResult(matching);
		// 将得到的扫描列表返回
		Message msg = new Message();
		msg.what = 11;
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
	private void createWifiAP(final String type, final String ssid,
			final String password, final String ipc_ssid, final String ipc_mac,
			final String ipc_ip, final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {
				int tempTime = 0;
				try {
					tempTime = openWifi(false, outTime);
					wifiSupport.closeWifi();
					wifiSupport.closeWifiAp(null);
					wifiSupport.createWifiHot(ssid, password);
				} catch (Exception e) {
					// TODO Auto-generated catch block 这里需要异常处理
					e.printStackTrace();
				}

				// 如果wifi打开了
				while (wifiSupport.getConnResult() == null) {
					try {
						int temp_2 = 100;
						Thread.sleep(temp_2);
						tempTime += temp_2;
						// 如果超时了 直接返回
						if (tempTime > outTime) {
							wifiSupport.closeWifi();
							msg.what = Integer.parseInt("-" + type + "1");
							msg.obj = null;
							return;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				msg.what = Integer.parseInt(type + "1");
				msg.obj = wifiSupport.getConnResult();
				handler.sendMessage(msg);
				// 获取wifi连接列表
				getClientList(ipc_ssid, ipc_mac, ipc_ip, type, 40000);

			};

		};
		Thread mythread = new Thread(runnable);
		mythread.start();
	}

	/**
	 * 获取加入wifiAP的用户列表
	 * 
	 */
	private void getClientList(final String ipc_ssid, final String ipc_mac,
			final String ipc_ip, final String type, final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {
				int tempTime = 0;
				// 获取wifi 扫描列表
				WifiRsBean[] beans = null;
				while (true) {
					int temp_2 = 400;
					try {
						
						beans = wifiSupport.getJoinApList(false, 300);
						if (beans != null) {
							for (WifiRsBean temp : beans) {
								Log.e(TAG, "list ipc_ssid----------------"
										+ ipc_ssid + "-------------");
								Log.e(TAG, "list ipc_mac----------------"
										+ ipc_mac + "-------------");
								if (temp.getIpc_mac()
										.substring(2)
										.toLowerCase()
										.equals(ipc_mac.substring(2)
												.toLowerCase())) {
									beans = new WifiRsBean[1];
									beans[0]=temp;
									msg.what = Integer.parseInt(type + "2");
									msg.obj = beans;
									handler.sendMessage(msg);
									return;
								}
							}
						}
						// 如果ip ping通了 返回可连接状态
						if (!"".equals(ipc_ip)) {
							Log.e(TAG, "list ipc_ip----------------" + ipc_ip
									+ "-------------");
							boolean isReachable;

							isReachable = InetAddress.getByName(ipc_ip)
									.isReachable(300);
							Thread.sleep(temp_2);

							tempTime += temp_2;
							// 如果 ping通了 跳出循环
							if (isReachable) {
								Log.e(TAG, "ipc getpin     -------ipc_ip"
										+ ipc_ip + "--------ipc_mac" + ipc_mac
										+ "-----------------");
								beans = new WifiRsBean[1];
								beans[0] = new WifiRsBean();
								beans[0].setIpc_ip(ipc_ip);
								beans[0].setIpc_mac(ipc_mac);
								msg.what = Integer.parseInt(type + "2");
								msg.obj = beans;
								handler.sendMessage(msg);
								return;
							}
						}
						// 如果超时了 直接返回
						if (tempTime > outTime) {
							wifiSupport.closeWifi();
							msg.what = Integer.parseInt("-" + type + "2");
							msg.obj = null;
							return;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};

		};

		Thread mythread = new Thread(runnable);
		mythread.start();
	}

	/**
	 * 保存配置文件
	 * 
	 * @param beans
	 */
	public void saveConfiguration(final WifiRsBean beans, int outTime) {
		Runnable runnable = new Runnable() {
			public void run() {
				Message msg = new Message();

				JSONObject config = new JSONObject();
				try {

					config.put("ipc_ssid", beans.getIpc_ssid());
					config.put("ipc_mac", beans.getIpc_mac());
					config.put("ph_ssid", beans.getPh_ssid());
					config.put("ph_pass", beans.getPh_pass());
					config.put("ipc_ip", beans.getIpc_ip());
					config.toString();
					try {
						wifiSupport
								.writePassFile(WIFICONFIG, config.toString());
						msg.what = 41;
						msg.obj = null;
						handler.sendMessage(msg);
					} catch (Exception e) {
						msg.what = -41;
						msg.obj = null;
						handler.sendMessage(msg);
						e.printStackTrace();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			};

		};

		Thread mythread = new Thread(runnable);
		mythread.start();
		// TODO Auto-generated method stub

	}

	/**
	 * 自动管理wifi
	 * 
	 * @param outTime
	 */
	private void autoWifiManage(final int outTime) {
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					int openTime = 0;
					Message msg = new Message();

					String configString;

					configString = wifiSupport.readPassFile(WIFICONFIG);

					// 如果 文件中没有 配置 报错
					if (configString == null) {
						msg.what = -51;
						msg.obj = null;
						handler.sendMessage(msg);
						return;
					}
					JSONObject config = new JSONObject(configString);

					// 如果 文件中没有 配置 报错
					if ("".equals(config.getString("ipc_ssid"))
							|| "".equals(config.getString("ipc_mac"))
							|| "".equals(config.getString("ph_ssid"))
							|| "".equals(config.getString("ph_pass"))
							|| "".equals(config.getString("ipc_ip"))) {
						msg.what = -51;
						msg.obj = null;
						handler.sendMessage(msg);
						return;
					}

					String ipc_ssid = config.getString("ipc_ssid");
					String ipc_mac = config.getString("ipc_mac");
					String ipc_ip = config.getString("ipc_ip");
					String ph_ssid = config.getString("ph_ssid");
					String ph_pass = config.getString("ph_pass");
					// wifiSupport.
					ConnectivityManager cm = (ConnectivityManager) context
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo mWifi = cm
							.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					NetworkInfo mMobile = cm
							.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

					// NetworkInfo networkINfo = cm.getActiveNetworkInfo();
					// 如果 wifi 和AP都没有打开
					if ((mWifi == null || !mWifi.isConnected())
							&& (mMobile == null || !mMobile.isConnected())) {
						WifiRsBean bb = wifiSupport.getConnResult();
						if (bb != null) {
							// 当前已经有IPC热点
							if (bb.getPh_ssid().equals(ph_ssid)) {
								msg.what = 53;
								msg.obj = bb;
								handler.sendMessage(msg);
								openTime = vaviAutoWifi(ipc_ssid, ipc_mac,
										outTime);
								if (openTime == 0) {
									return;
								}
								// 返回列表
								getClientList(ipc_ssid, ipc_mac, ipc_ip, "5",
										outTime);
								return;
							} else {
								wifiSupport.closeWifi();
								// 创建热点
								createWifiAP("5", ph_ssid, ph_pass, ipc_ssid,
										ipc_mac, ipc_ip, openTime);
								return;
							}
						}
						Log.e(TAG,
								"autoconnn----------------networkINfo nulllll------------");
						openTime = vaviAutoWifi(ipc_ssid, ipc_mac, outTime);
						if (openTime == 0) {

							return;
						}
						// 创建热点
						createWifiAP("5", ph_ssid, ph_pass, ipc_ssid, ipc_mac,
								ipc_ip, openTime);
						return;
					} else

					if (mWifi != null && mWifi.isConnected()) {
						Log.e(TAG,
								"autoconnn----------------networkINfo TYPE_WIFI------------");
						openTime = vaviAutoWifi(ipc_ssid, ipc_mac, outTime);
						if (openTime == 0) {

							return;
						}

						wifiSupport.closeWifi();
						// 创建热点
						createWifiAP("5", ph_ssid, ph_pass, ipc_ssid, ipc_mac,
								ipc_ip, openTime);
						return;
					} else

					{ // 不是wifi 和热点 关了直接连
						Log.e(TAG,
								"autoconnn----------------networkINfo nostate------------");
						openTime = vaviAutoWifi(ipc_ssid, ipc_mac, outTime);
						if (openTime == 0) {
							// 扫描超时
							msg.what = -52;
							msg.obj = null;
							handler.sendMessage(msg);
							return;
						}
						// 不管开着什么 都关掉
						wifiSupport.closeWifi();
						wifiSupport.closeWifiAp(wifiManager);
						// 创建热点
						createWifiAP("5", ph_ssid, ph_pass, ipc_ssid, ipc_mac,
								ipc_ip, openTime);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};

		};

		Thread mythread = new Thread(runnable);
		mythread.start();
	}

	private void isConnectIPC(int outTime) {
		// TODO Auto-generated method stub

	}

	private int vaviAutoWifi(String ssid, String mac, int outTime) {
		Message msg = new Message();
		WifiRsBean[] beans = null;
		int openTime = 0;
		// 么有开启网络
		openTime = this.openWifi(true, outTime);
		if (openTime == 0) { // 超时了
			msg.what = -51;
			msg.obj = null;
			handler.sendMessage(msg);
			return 0;
		}
		// wifi 打开后进行连接 检查用时
		List<WifiRsBean> list = new ArrayList<WifiRsBean>();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		openTime += 500;

		openTime = getwifiList(list, ssid, openTime);
		if (list.size() > 0) {
			beans = (WifiRsBean[]) list.toArray(new WifiRsBean[0]);
		}
		// 超时 报错返回
		if (openTime == 0) {
			msg.what = -51;
			msg.obj = null;
			handler.sendMessage(msg);
			return 0;
		}
		// 如果周围没有连接不再继续进行--------------------------------------
		if (beans == null) {
			msg.what = -52;
			msg.obj = null;
			handler.sendMessage(msg);
			return 0;
		}
		//

		return outTime;
	}

}