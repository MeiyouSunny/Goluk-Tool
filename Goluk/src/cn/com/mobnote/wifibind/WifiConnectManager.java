package cn.com.mobnote.wifibind;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManagerSupport.WifiCipherType;

import android.annotation.SuppressLint;
import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import android.os.Handler;
import android.os.Message;

import android.util.Log;

public class WifiConnectManager implements WifiConnectInterface {

	private static final String TAG = "testhan";

	private WifiConnCallBack callback = null;

	private WifiManager wifiManager = null;
	private WifiConnectManagerSupport wifiSupport = null;
	private Context context = null;
	WifiConnectManagerSupport support = null;

	// 构造函数
	public WifiConnectManager(WifiManager wifiManager, WifiConnCallBack callback) {
		this.wifiManager = wifiManager;
		this.wifiSupport = new WifiConnectManagerSupport(wifiManager);
		this.callback = callback;
		this.context = (Context) callback;
		// 初始化wifi工具类
		support = new WifiConnectManagerSupport(wifiManager);
	}

	/**
	 * 通过用户名，密码连接ipc
	 * 
	 * @param ssid
	 * @param password
	 * @param type
	 */
	public void connectWifi(String ssid, String password, WifiCipherType type) {
		connectWifi(ssid, password, "", type, 12000);
	}

	/**
	 * 启动软件后自动管理wifi
	 */
	public void autoWifiManage() {
		autoWifiManage(4000);
	}

	/**
	 * 通过用户名密码创建wifi热点
	 * 
	 * @param ssid
	 * @param password
	 */
	public void createWifiAP(String ssid, String password) {
		createWifiAP(ssid, password, 14000);
	}

	/**
	 * 通过关键字查询列表信息
	 * 
	 * @param matching
	 *            关键字
	 */
	public void scanWifiList(String matching) {
		scanWifiList(matching, 14000);
	}

	/**
	 * 保存配置信息
	 * 
	 * @param beans
	 */
	public void saveConfiguration(WifiRsBean beans) {
		saveConfiguration(beans, 14000);
	}

	public void isConnectIPC() {
		isConnectIPC(14000);
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
				callback.wifiCallBack(3, 0, 0, "创建wifi热点成功", msg.obj);
				break;
			}
			// 获取加入热点信息
			case 32: {
				callback.wifiCallBack(3, 0, 1, "创建wifi热点列表成功", msg.obj);
				break;
			}
			// 保存wifi配置成功
			case 41: {
				callback.wifiCallBack(4, 0, 0, " 保存wifi配置成功", msg.obj);
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
		 
			case -31: {
				callback.wifiCallBack(3, 0, 0, "创建wifi热点失败", msg.obj);
				break;
			}
	 
			case -32: {
				callback.wifiCallBack(3, 0, 1, "创建wifi热点列表失败", msg.obj);
				break;
			}
			case -41: {
				callback.wifiCallBack(4, -1, 0, "保存wifi配置失败", msg.obj);
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
				//如果当前网络未开启 连接失败后 需要再关闭网络
				boolean doClose=false;
				WifiRsBean[] beans = null;
				// 1：打开wifi
				int openTime = openWifi(outTime);
				//代表当前网络已经开启
				if(openTime!=outTime){
					doClose=true;
				}
				// 处理超时
				if (openTime == 0) {
					if(doClose){
						wifiSupport.closeWifi();
					}
					msg.what = -21;
					handler.sendMessage(msg);
					return;
				}
				// wifi 打开后进行连接 检查用时
				List<WifiRsBean> list = new ArrayList<WifiRsBean>();
				openTime = getwifiList(list, openTime);
				if (list.size() > 0) {
					beans = (WifiRsBean[]) list.toArray(new WifiRsBean[0]);
				}
				// 超时 报错返回
				if (openTime == 0) {
					if(doClose){
						wifiSupport.closeWifi();
					}
					msg.what = -21;
					handler.sendMessage(msg);
					return;
				}
				// 如果周围没有连接不再继续进行--------------------------------------
				if (beans == null) {
					if(doClose){
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
					if(doClose){
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
					
					 openTime= getConnState( ssid,openTime);
						// 超时 报错返回
						if (openTime == 0) {
							wifiSupport.disConnWifi();
							if(doClose){
								wifiSupport.closeWifi();
							}
							
							msg.what = -24;
							handler.sendMessage(msg);
							
							return;
						}
					msg.what = 21;
					msg.obj = wifiSupport.getConnResult();
					handler.sendMessage(msg);
					return ;
				} else { // 连接失败
					wifiSupport.disConnWifi();
					if(doClose){
						wifiSupport.closeWifi();
					}
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
	 * @param outTime
	 * @return
	 */
	private int getConnState(String ssid,int outTime) {
		int tempTime = 0;
		while (wifiManager.getConnectionInfo() == null ||("\""+ssid+"\"").equals(wifiManager.getConnectionInfo().getSSID())) {
			
			Log.e(TAG, "crssssssssss----------------"+wifiManager.getConnectionInfo().getSSID()+"");
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
	private Thread scanWifiList(final String matching, final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();

			public void run() {
				// 1：打开wifi
				int openTime = openWifi(outTime);
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
	private int getwifiList(List<WifiRsBean> beans, int outTime) {
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

		Log.e(TAG, "sagetwifiListn----------------" + tempTime
				+ "-------------");
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
	private void createWifiAP(final String ssid, final String password,
			final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();
			public void run() {
				try {
					wifiSupport.closeWifiAp(wifiManager);
					wifiSupport.createWifiHot(ssid, password);
				} catch (Exception e) {
					// TODO Auto-generated catch block 这里需要异常处理
					e.printStackTrace();
				}
				int tempTime=0;
				// 如果wifi打开了
				while (!wifiManager.isWifiEnabled()) {
					try {
						int temp_2 = 100;
						Thread.sleep(temp_2);
						tempTime += temp_2;
						//如果超时了 直接返回
						if (tempTime > outTime) {
							wifiSupport.closeWifi();
							msg.what = -31;
							msg.obj = null;
						 return;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			
				msg.what = 31;
				msg.obj = wifiSupport.getConnResult();
				handler.sendMessage(msg);
				// 获取wifi连接列表
				getClientList(40000);

			};

		};
		Thread mythread = new Thread(runnable);
		mythread.start();
	}

	/**
	 * 获取加入wifiAP的用户列表
	 * 
	 */
	private void getClientList(final int outTime) {

		Runnable runnable = new Runnable() {
			Message msg = new Message();
			public void run() {
				int tempTime=0;
				// 获取wifi 扫描列表
				while (wifiSupport.getJoinApList(false, 300) == null) {
					int temp_2 = 100;
					try {
						Thread.sleep(temp_2);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					tempTime += temp_2;
					//如果超时了 直接返回
					if (tempTime > outTime) {
						wifiSupport.closeWifi();
						msg.what = -32;
						msg.obj = null;
					 return;
					}
				}

				Message msg = new Message();
				msg.what = 32;
				msg.obj = wifiSupport.getJoinApList(false, 300);
				handler.sendMessage(msg);

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
					config.put("ipc_ssid", beans.getIpc_bssid());
					config.put("ipc_ip", beans.getIpc_ip());
					config.put("ipc_mac", beans.getIpc_mac());
					config.put("ph_ssid", beans.getPh_bssid());
					config.put("ph_ip", beans.getPh_ip());
					config.put("ph_mac", beans.getPh_mac());
					config.toString();
					try {
						wifiSupport.writePassFile("abc", config.toString());
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
	private void autoWifiManage(int outTime) {
		try {
			WifiRsBean beans = new WifiRsBean();
			String configString = wifiSupport.readPassFile("");
			if (configString == null) {
				return;
			}
			JSONObject config = new JSONObject(configString);
			beans.setIpc_ssid(config.getString("ipc_ssid"));
			beans.setIpc_ip(config.getString("ipc_ip"));
			beans.setIpc_mac(config.getString("ipc_mac"));
			beans.setPh_ssid(config.getString("ph_ssid"));
			beans.setPh_ip(config.getString("ph_ip"));
			beans.setPh_mac(config.getString("ph_mac"));

			// wifiSupport.
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkINfo = cm.getActiveNetworkInfo();
			if (networkINfo == null) {
				// 么有开启网络
			}
			if (networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
				// 如果是开启wifi 网络 判断周围有没有 ipc 如果有 就连接 没有就保持现有连接
			} else if (networkINfo.getType() == ConnectivityManager.TYPE_MOBILE) {

			} else {
				// 其他网络 处理
			}
			// networkINfo.get
			int typeWifi = ConnectivityManager.TYPE_WIFI;

			// 1：打开wifi
			int openTime = openWifi(outTime);
			// 超时错误
			if (openTime == 0) {
				// msg.what = -1;
				//
				// handler.sendMessage(msg);
				return;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void isConnectIPC(int outTime) {
		// TODO Auto-generated method stub

	}

}