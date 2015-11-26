package cn.com.mobnote.wifibind;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import cn.com.mobnote.golukmobile.multicast.IMultiCastFn;
import cn.com.mobnote.golukmobile.multicast.NetUtil;
import cn.com.tiros.debug.GolukDebugUtils;

public class WifiConnectManager implements WifiConnectInterface, IMultiCastFn {
	public static final String TITLE = "Goluk";
	private static final String TAG = "testhan";
	private static final String WIFICONFIG = "wifi.config";
	private WifiConnCallBack callback = null;
	private static final int WAITTIME = 90 * 1000;
	private WifiManager wifiManager = null;
	private WifiConnectManagerSupport wifiSupport = null;
	private Context context = null;
	WifiConnectManagerSupport support = null;
	ConnectivityManager connectivity = null;
	WifiApManagerSupport apManagesupport = null;
	private NetUtil netUtil = null;

	// 构造函数
	public WifiConnectManager(WifiManager wifiManager, Object callback) {
		this.wifiManager = wifiManager;
		this.wifiSupport = new WifiConnectManagerSupport(wifiManager);
		this.callback = (WifiConnCallBack) callback;
		this.context = (Context) callback;
		// 初始化wifi工具类
		support = new WifiConnectManagerSupport(wifiManager);
		apManagesupport = new WifiApManagerSupport(wifiManager);
		netUtil = NetUtil.getInstance();
		netUtil.setMultiCastListener(this);
	}

	/**
	 * 启动软件后自动管理wifi
	 */
	public void autoWifiManage() {
		autoWifiManage(40 * 1000);
	}

	/**
	 * 
	 */
	public void unbind() {
		this.callback = null;
		this.context = null;
		netUtil.unRemoveMultiCastListener(this);
	}

	/**
	 * 通过用户名密码创建wifi热点
	 * 
	 * @param ssid
	 * @param password
	 */
	public void createWifiAP(String ph_ssid, String ph_password, String ipc_ssid, String ipc_mac) {
		createWifiAP("3", ph_ssid, ph_password, ipc_ssid, "", 40 * 1000);
	}

	/**
	 * 通过关键字查询列表信息
	 * 
	 * @param matching
	 *            关键字
	 */
	public void scanWifiList(String matching, boolean reset) {
		scanWifiList(TITLE, reset, 40 * 1000);
	}

	/**
	 * 保存配置信息
	 * 
	 * @param beans
	 */
	public void saveConfiguration(WifiRsBean beans) {
		saveConfiguration(beans, 40 * 1000);
	}

	public WifiRsBean readConfig() {
		return wifiSupport.readConfig(WIFICONFIG);
	}

	public void isConnectIPC() {
		isConnectIPC(30000);
	}

	/**
	 * 自动管理wifi重置
	 * 
	 * @param outTime
	 */
	public void autoWifiManageReset() {
		wifiSupport.closeWifi();

		autoWifiManage(40 * 1000);
	}

	/**
	 * 获取当前连接是否是GOLUK
	 * 
	 * @return
	 */
	public WifiRsBean getConnResult() {
		return wifiSupport.getConnResult(TITLE);
	}

	// -------------------------------以上为封装后的对外接口----------------------------------------//
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {
			if (callback == null) {
				return;
			}
			switch (msg.what) {
			// 扫描列表成功
			case 11: {
				GolukDebugUtils.e(TAG, "return --------------ok--------");
				callback.wifiCallBack(1, 0, 0, "wifi扫描成功", msg.obj);
				break;
			}
			// 连接wifi成功
			case 21: {
				GolukDebugUtils.e(TAG, "return --------------ok--------");
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
				callback.wifiCallBack(5, 0, 2, "自动连接--当前已经连接", msg.obj);
				break;
			}
			case 54: {
				callback.wifiCallBack(5, 0, 3, "自动连接--当前有活动wifi", msg.obj);
				break;
			}
			case 61: {
				callback.wifiCallBack(6, 0, 0, "热点获取网关成功", msg.obj);
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
				callback.wifiCallBack(2, -1, msg.what, "连接wifi失败,请重试", null);
				break;
			}
			case -32: {
				callback.wifiCallBack(5, -1, msg.what, "附近没有可连接的ipc", msg.obj);
				break;
			}
			case -34: {
				callback.wifiCallBack(5, -1, msg.what, "创建热点失败", msg.obj);
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
			case -54: {
				callback.wifiCallBack(5, -1, msg.what, "自动创建热点失败", msg.obj);
				break;
			}
			case -61: {
				callback.wifiCallBack(6, 0, -1, "热点获取网关失败", msg.obj);
				break;
			}
			case -62: {
				callback.wifiCallBack(6, 0, -2, "热点获取网关超时", msg.obj);
				break;
			}
			case 100:

				break;
			}
		}
	};

	/**
	 * 通过关键字查询列表信息
	 * 
	 * @param matching
	 *            关键字
	 * @param outTime
	 *            超时时间
	 */
	private Thread scanWifiList(final String matching, final boolean reset, final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {
				wifiSupport.closeWifi();
				// 1：打开wifi
				int openTime = openWifi(reset, outTime);
				// int openTime=outTime;
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
				int temp_1 = 2000;
				Thread.sleep(temp_1);
				// 耗时 2000毫秒
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
		GolukDebugUtils.e(TAG, "opentime----------------" + (outTime - tempTime) + "-------------");
		return outTime - tempTime;
	}

	/**
	 * 扫描wifi android 4.x以上matching
	 * 
	 * @param matching
	 * @param type
	 */
	private int wifiScan(String matching, int outTime) {
		wifiManager.startScan();
		GolukDebugUtils.i(TAG, "扫描wifi....11111");
		int tempTime = 0;
		// 先提至500毫秒再进行查询
		try {
			Thread.sleep(500);
			tempTime = 500;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// 扫描了表不为null
		WifiRsBean[] wifiBean = wifiSupport.getScanResult(matching, null);
		while (wifiBean == null) {
			try {
				int temp_1 = 800;
				Thread.sleep(temp_1);
				tempTime += temp_1;
				if (tempTime > outTime) {
					GolukDebugUtils.i(TAG, "扫描wifi....超时");
					return 0;
				}
				wifiBean = wifiSupport.getScanResult(matching, null);
			} catch (InterruptedException e) {
				GolukDebugUtils.i(TAG, "扫描wifi....失败");
				e.printStackTrace();
				return 0;
			}
		}
		// 需要进行模糊扫描

		GolukDebugUtils.i(TAG, "扫描wifi....已经获取列表");
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
	private void createWifiAP(final String type, final String ssid, final String password, final String ipc_ssid,
			final String ipc_ip, final int outTime) {
		GolukDebugUtils.i(TAG, "创建热点开始....11111");

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {
				int sTime = 0;
				try {
					sTime = openWifi(false, outTime);
					wifiSupport.closeWifi();
					apManagesupport.createWifiHot(ssid, password);
				} catch (Exception e) {
					e.printStackTrace();
				}
				int tempTime = 0;
				// 如果wifi打开了
				while (apManagesupport.getWifiApState() != 13) {
					try {
						GolukDebugUtils.i(TAG, "创建热点等待状态变化....22222");
						int temp_2 = 300;
						Thread.sleep(temp_2);
						tempTime += temp_2;
						// 如果超时了 直接返回
						if (tempTime > sTime) {
							wifiSupport.closeWifi();
							msg.what = Integer.parseInt("-" + type + "4");
							final int wifiState = apManagesupport.getWifiApState();
							msg.obj = null;
							handler.sendMessage(msg);
							return;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				GolukDebugUtils.i(TAG, "创建热成功");

				msg.what = Integer.parseInt(type + "1");
				WifiRsBean rs = wifiSupport.getConnResult();
				msg.obj = rs;
				handler.sendMessage(msg);

				GolukDebugUtils.i(TAG, "创建热点等待ipc接入");
				netUtil.findServerIpAddress(Integer.parseInt(type), ssid, "", WAITTIME);
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
					config.put("ipc_pass", beans.getIpc_pass());
					config.toString();
					try {
						wifiSupport.writePassFile(WIFICONFIG, config.toString());
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
					int openTime = outTime;
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
					if ("".equals(config.getString("ipc_ssid")) || "".equals(config.getString("ph_ssid"))
							|| "".equals(config.getString("ph_pass")) || "".equals(config.getString("ipc_ip"))) {
						msg.what = -51;
						msg.obj = null;
						handler.sendMessage(msg);
						return;
					}

					String ipc_ssid = config.getString("ipc_ssid");
					String ipc_ip = config.getString("ipc_ip");
					String ph_ssid = config.getString("ph_ssid");
					String ph_pass = config.getString("ph_pass");
					// wifiSupport.
					ConnectivityManager cm = (ConnectivityManager) context
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					// -----------------------------------------如果 wifi
					// 打开了-------------------------------//
					if (mWifi != null && mWifi.isConnected()) {
						msg.what = 54;
						msg.obj = null;
						handler.sendMessage(msg);
						// Log.e(TAG, "自动连接----------------开启wifi------------");
						// openTime = vaviAutoWifi(ipc_ssid, outTime);
						// if (openTime == 0) {
						//
						// return;
						// }
						//
						// wifiSupport.closeWifi();

						return;
					}
					// -----------------------------------------如果 ap打开了
					// -------------------------------//
					if (apManagesupport.getWifiApState() == 13) {
						// 当前已经有IPC热点
						WifiRsBean bb = wifiSupport.getNetworkSSID(context);
						GolukDebugUtils.e(TAG, "自动连接----------------开启热点------------");
						if (bb.getPh_ssid().equals(ph_ssid)) {
							msg.what = 53;
							msg.obj = bb;
							handler.sendMessage(msg);

							//
							netUtil.findServerIpAddress(5, "", "", WAITTIME);
							return;
						} else {
							wifiSupport.closeWifi();
							// 创建热点
							autoWifiManage();
							return;
						}
					}

					// -----------------------------------------ap wifi
					// 都没有打开-------------------------------//
					if ((mWifi == null || !mWifi.isConnected())) {
						// 关闭所有的网络
						wifiSupport.closeWifi();

						GolukDebugUtils.e(TAG, "自动连接----------------AP和wifi 都没有开启------------");
						// openTime = vaviAutoWifi(ipc_ssid, outTime);
						// if (openTime == 0) {
						// // 扫描超时
						// msg.what = -52;
						// msg.obj = null;
						// handler.sendMessage(msg);
						// return;
						// }
						// 创建热点
						createWifiAP("5", ph_ssid, ph_pass, ipc_ssid, ipc_ip, openTime);
						return;
					}

					else

					{ // 不是wifi 和热点 关了直接连
						GolukDebugUtils.e(TAG, "autoconnn----------------networkINfo nostate------------");
						// openTime = vaviAutoWifi(ipc_ssid, outTime);
						// if (openTime == 0) {
						// // 扫描超时
						// msg.what = -52;
						// msg.obj = null;
						// handler.sendMessage(msg);
						// return;
						// }
						// 不管开着什么 都关掉
						// wifiSupport.closeWifi();

						// 创建热点
						createWifiAP("5", ph_ssid, ph_pass, ipc_ssid, ipc_ip, openTime);
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

	/**
	 * 自动管理wifi
	 * 
	 * @param outTime
	 */
	public void autoWifiManage(final String ipc_ssid, final String ipc_ip, final String ph_ssid, final String ph_pass) {
		final int outTime = 40 * 1000;
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					int openTime = outTime;
					Message msg = new Message();

					// String ipc_ssid = config.getString("ipc_ssid");
					// String ipc_ip = config.getString("ipc_ip");
					// String ph_ssid = config.getString("ph_ssid");
					// String ph_pass = config.getString("ph_pass");
					// wifiSupport.
					ConnectivityManager cm = (ConnectivityManager) context
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					// -----------------------------------------如果 wifi
					// 打开了-------------------------------//
					if (mWifi != null && mWifi.isConnected()) {
						msg.what = 54;
						msg.obj = null;
						handler.sendMessage(msg);
						// Log.e(TAG, "自动连接----------------开启wifi------------");
						// openTime = vaviAutoWifi(ipc_ssid, outTime);
						// if (openTime == 0) {
						//
						// return;
						// }
						//
						// wifiSupport.closeWifi();
						// 创建热点

						return;
					}
					// -----------------------------------------如果 ap打开了
					// -------------------------------//
					if (apManagesupport.getWifiApState() == 13) {
						// 当前已经有IPC热点
						WifiRsBean bb = wifiSupport.getNetworkSSID(context);
						GolukDebugUtils.e(TAG, "自动连接----------------开启热点------------");
						if (bb.getPh_ssid().equals(ph_ssid)) {
							msg.what = 53;
							msg.obj = bb;
							handler.sendMessage(msg);

							//
							netUtil.findServerIpAddress(5, "", "", WAITTIME);
							return;
						} else {
							wifiSupport.closeWifi();
							// 创建热点
							autoWifiManage();
							return;
						}
					}

					// -----------------------------------------ap wifi
					// 都没有打开-------------------------------//
					if ((mWifi == null || !mWifi.isConnected())) {
						// // 关闭所有的网络
						// wifiSupport.closeWifi();
						//
						// Log.e(TAG,
						// "自动连接----------------AP和wifi 都没有开启------------");
						// openTime = vaviAutoWifi(ipc_ssid, outTime);
						// if (openTime == 0) {
						// // 扫描超时
						// msg.what = -52;
						// msg.obj = null;
						// handler.sendMessage(msg);
						// return;
						// }
						// 创建热点
						createWifiAP("5", ph_ssid, ph_pass, ipc_ssid, ipc_ip, openTime);
						return;
					}

					else

					{ // 不是wifi 和热点 关了直接连
						GolukDebugUtils.e(TAG, "autoconnn----------------networkINfo nostate------------");
						// openTime = vaviAutoWifi(ipc_ssid, outTime);
						// if (openTime == 0) {
						// // 扫描超时
						// msg.what = -52;
						// msg.obj = null;
						// handler.sendMessage(msg);
						// return;
						// }
						// // 不管开着什么 都关掉
						// wifiSupport.closeWifi();
						// 创建热点
						createWifiAP("5", ph_ssid, ph_pass, ipc_ssid, ipc_ip, openTime);
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

	@Override
	public void MultiCaskCallBack(int type, int sucess, Object obj) {

		int what = 0;
		if (type == 3) {
			GolukDebugUtils.i(TAG, "创建热点ipc接入结果 ：创建热点" + sucess);
			if (sucess == 1) {
				what = 32;

			} else if (2 == sucess) {
				netUtil.findServerIpAddress(type, "", "", WAITTIME);
				// TODO request
				return;
			} else {
				what = -32;
			}
		} else {
			GolukDebugUtils.i(TAG, "创建热点ipc接入结果 ：自动连接热点" + sucess);
			if (sucess == 1) {
				what = 52;
			} else if (2 == sucess) {
				netUtil.findServerIpAddress(type, "", "", WAITTIME);
				return;
			} else {
				what = -52;
			}
		}

		Message msg = new Message();

		WifiRsBean[] beans = null;
		if (obj != null) {
			beans = new WifiRsBean[1];
			beans[0] = (WifiRsBean) obj;
			msg.obj = beans;
		} else {

			beans = apManagesupport.getJoinApList(false, 300);

			if (beans != null) {

				beans[0] = (WifiRsBean) obj;
				msg.obj = beans;
				if (type == 3) {

					what = 32;
				} else {

					what = 52;
				}

			} else {
				msg.obj = null;
			}

		}
		msg.what = what;
		handler.sendMessage(msg);

	}

	/**
	 * 关闭wifi 及AP
	 * 
	 * @return
	 */
	public boolean closeWifiAP() {
		wifiSupport.closeWifi();

		this.unbind();
		return true;
	}

	public void closeAp() {
		GolukDebugUtils.e("", "jyf----20150716----111----aaaaaa :" + (wifiManager == null));
		if (wifiManager != null && wifiManager.getConnectionInfo() != null) {

			GolukDebugUtils.e("", "jyf----20150716----222222----aaaaaa :" + (wifiManager.getWifiState()));
			if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
				wifiSupport.closeWifi();
				this.unbind();
			} else {
				this.unbind();
			}

		} else {
			this.unbind();
		}
	}
}