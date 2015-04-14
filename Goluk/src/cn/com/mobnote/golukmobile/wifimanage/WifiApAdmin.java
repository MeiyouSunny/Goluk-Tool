package cn.com.mobnote.golukmobile.wifimanage;

 
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
 

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
 

/**
 * 管理ＡＰ热点
 * 
 */
public class WifiApAdmin {
	public static final String TAG = "WifiApAdmin";
	public static final String BSSID = "dk:96:33:59:39:48";

	public void closeWifiAp(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		 
		closeWifiAp(wifiManager);
		if(ss!=null){
				ss.closeServerSocket();
				ss.start=0;
		 
				ss.serverSocket=null;
				ss=null;
			 
		}
	}
	SocketServer ss=null;
	private WifiManager mWifiManager = null;
	private WifiInfo mWifiInfo;
	private Context mContext = null;
	private Handler handler = null;
 

	public WifiApAdmin(Context context, Handler _handler) {
		mContext = context;
		handler = _handler;
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();
		 
	
	}

	public WifiApAdmin(Context context) {
		mContext = context;
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();

	}

	private String mSSID = "";
	private String mPasswd = "";

	public void startWifiAp(String ssid, String passwd) {
		mSSID = ssid;
		mPasswd = passwd;
		closeWifiAp(mWifiManager);
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}

		stratWifiAp();
		// 启动后15秒监听
		MyTimerCheck timerCheck = new MyTimerCheck() {

			@Override
			public void doTimerCheckWork() {
				// TODO Auto-generated method stub

				if (isWifiApEnabled()) {
					// 11wp代表连接成功
				
					Message msg = new Message();
	                 msg.what = 11;        
	                 msg.obj ="wifi热点创建成功";
	                 //发送消息
	                 handler.sendMessage(msg);
	       
				 if(ss==null ){
		 
					//成功后调用启动sock管理 
//					 new Thread(SocketServer.getInstance(mContext,handler)).start();
				 
				 }
					this.exit();
				}
			}

			@Override
			public void doTimeOutWork() {
				// 11wp代表连接成功
				Message msg = new Message();
				msg.what = 10;
				msg.obj ="wifi热点创建失败";
				//发送消息
				handler.sendMessage(msg);
				this.exit();
			}
		};
		timerCheck.start(15, 1000);

	}

	public void stratWifiAp() {
		Method method1 = null;
		try {
			method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			WifiConfiguration netConfig = new WifiConfiguration();

			netConfig.SSID = mSSID;
			netConfig.preSharedKey = mPasswd;
			//设置组id
			netConfig.BSSID=BSSID;
			 
			netConfig.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			netConfig.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			netConfig.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			netConfig.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			netConfig.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.CCMP);
			netConfig.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.TKIP);

			method1.invoke(mWifiManager, netConfig, true);

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void closeWifiAp(WifiManager wifiManager) {
		if (isWifiApEnabled()) {
			try {
				Method method = wifiManager.getClass().getMethod(
						"getWifiApConfiguration");
				method.setAccessible(true);

				WifiConfiguration config = (WifiConfiguration) method
						.invoke(wifiManager);

				Method method2 = wifiManager.getClass().getMethod(
						"setWifiApEnabled", WifiConfiguration.class,
						boolean.class);
				method2.invoke(wifiManager, config, false);
		
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public boolean isWifiApEnabled() {
		try {
			Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
			method.setAccessible(true);
			return (Boolean) method.invoke(mWifiManager);

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	// 得到MAC地址
	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的BSSID
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	// 得到IP地址
	public String getIPAddress() {
		return getNetworkIpAddress(getApName(mContext));
	}

	// 得到连接的ID
	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}
	// 得到IP地址
	public int getIPAddress1() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}
	// 得到WifiInfo的所有信息包
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	/**
	 * 将ip的整数形式转换成ip形式
	 * 
	 * @param ipInt
	 * @return
	 */
	public String int2ip(int ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}
 
	public static String getNetworkIpAddress(String name) {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces
						.nextElement();
				Enumeration<InetAddress> enumeration = networkInterface
						.getInetAddresses();
				while (enumeration.hasMoreElements()) {
					InetAddress inetAddress = (InetAddress) enumeration
							.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address
							&& TextUtils.equals(name,
									networkInterface.getDisplayName())) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getApName(Context context) {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			Method method = connectivityManager.getClass().getMethod(
					"getTetheredIfaces");
			String[] names = (String[]) method.invoke(connectivityManager);
			return names[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}