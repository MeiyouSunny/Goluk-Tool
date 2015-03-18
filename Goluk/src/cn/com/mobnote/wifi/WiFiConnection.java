package cn.com.mobnote.wifi;

import java.util.List;

import cn.com.mobnote.util.console;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.text.TextUtils;
import android.util.Log;
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
 * @ 功能描述:wifi热点链接
 * 
 * @author 陈宣宇
 * 
 */

public class WiFiConnection {
	/** 上下文 */
	public Context mContext;
	/** 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况 */
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}
	/** goluk-wifi名称 */
	public static String MGOLUKWIFI = "";
	public static String MGOLUKWIFI_NEW = "";
	
	/** wifi管理类 */
	public WifiManager wifiManager;
	
	/**
	 * 保存连接小车本wifi,用来校验
	 * @param wifiName
	 */
	public static void SaveWiFiName(String wifiName){
		//保存wifi校验名称
		WiFiConnection.MGOLUKWIFI = wifiName;
		WiFiConnection.MGOLUKWIFI_NEW = "\"" + wifiName + "\"";
	}
	
	public WiFiConnection(WifiManager wifiManager,Context context){
		this.wifiManager = wifiManager;
		mContext = context;
	}
	
	public void connect(String ssid,String password,WifiCipherType type){
		if(this.wifiManager.isWifiEnabled()){
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			/*
			查看已经连接上的WIFI信息，在Android的SDK中为我们提供了一个叫做WifiInfo的对象，
			这个对象可以通过WifiManager.getConnectionInfo()来获取。WifiInfo中包含了当前连接中的相关信息。
			getBSSID()  获取BSSID属性
			getDetailedStateOf()  获取客户端的连通性
			getHiddenSSID()  获取SSID 是否被隐藏
			getIpAddress()  获取IP 地址
			getLinkSpeed()  获取连接的速度
			getMacAddress()  获取Mac 地址
			getRssi()  获取802.11n 网络的信号
			getSSID()  获取SSID
			getSupplicanState()  获取具体客户端状态的信息
			*/
			
			//获取已链接的wifi名称
			String wifiName = wifiInfo.getSSID();
			if(wifiName.equals(MGOLUKWIFI)){
				//发消息给UI线程,请求服务器数据
				console.log("chxy wifi 已链接" + wifiName);
			}
			else{
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				console.log("系统版本---" + currentapiVersion);
				//系统版本大于4.0,wifi名称多了一对引号,4.0以上为15
				if(currentapiVersion > 15){
					if(wifiName.equals(MGOLUKWIFI_NEW)){
						console.log("chxy wifi 已链接" + wifiName);
					}
				}
				else{
					
				}
			}
		}
		else{
		}
	}
	
	/**
	 * 打开wifi功能
	 * @return
	 */
	private boolean openWifi(){
		boolean bRet = true;
		if(!wifiManager.isWifiEnabled()){
			bRet = wifiManager.setWifiEnabled(true);
		}
		return bRet;
	}
	
	/**
	 * 构建wifi连接信息
	 * @param SSID
	 * @param Password
	 * @param Type
	 * @return
	 */
	private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		//没有密码
		if(Type == WifiCipherType.WIFICIPHER_NOPASS){
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		//wep加密
		if(Type == WifiCipherType.WIFICIPHER_WEP){
			if(!TextUtils.isEmpty(Password)){
				if(isHexWepKey(Password)){
					config.wepKeys[0] = Password;
				}
				else{
					config.wepKeys[0] = "\"" + Password + "\"";
				}
			}
			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		//wpa加密
		if(Type == WifiCipherType.WIFICIPHER_WPA){
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			//此处需要修改否则不能自动重联
			//config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); 
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}
	
	private static boolean isHexWepKey(String wepKey){
		final int len = wepKey.length();
		// WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
		if(len != 10 && len != 26 && len != 58){
			return false;
		}
		return isHex(wepKey);
	}

	private static boolean isHex(String key) {
		for (int i = key.length() - 1; i >= 0; i--) {
			final char c = key.charAt(i);
			if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 查看以前是否也配置过这个网络
	 * @param SSID
	 * @return
	 */
	private WifiConfiguration isExsits(String SSID){
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for(WifiConfiguration existingConfig : existingConfigs){
			if(existingConfig.SSID.equals("\"" + SSID + "\"")){
				return existingConfig;
			}
		}
		return null;
	}
	
	/**
	 * 判断当前网络是否链接了小车本wifi
	 * @return
	 */
	public boolean WiFiLinkStatus(){
		boolean b = false;
		if(this.wifiManager.isWifiEnabled()){
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			/*
			查看已经连接上的WIFI信息，在Android的SDK中为我们提供了一个叫做WifiInfo的对象，
			这个对象可以通过WifiManager.getConnectionInfo()来获取.WifiInfo中包含了当前连接中的相关信息。
			getBSSID()  获取BSSID属性
			getDetailedStateOf()  获取客户端的连通性
			getHiddenSSID()  获取SSID 是否被隐藏
			getIpAddress()  获取IP 地址
			getLinkSpeed()  获取连接的速度
			getMacAddress()  获取Mac 地址
			getRssi()  获取802.11n 网络的信号
			getSSID()  获取SSID
			getSupplicanState()  获取具体客户端状态的信息
			*/
			
			if(null != wifiInfo){
				//获取已链接的wifi名称
				String wifiName = wifiInfo.getSSID();
				if(null != wifiName){
					console.log("已链接wifi---" + wifiName);
					if(wifiName.equals(MGOLUKWIFI)){
						console.log("已链接wifi---" + wifiName);
						b = true;
					}
					
					int currentapiVersion = android.os.Build.VERSION.SDK_INT;
					console.log("系统版本---" + currentapiVersion);
					//系统版本大于4.0,wifi名称多了一对引号,4.0以上为15
					if(currentapiVersion > 15){
						if(wifiName.equals(MGOLUKWIFI_NEW)){
							console.log("已链接wifi---" + wifiName);
							b = true;
						}
					}
				}
			}
		}
		return b;
	}
	
	/**
	 * 获取wifi信息
	 * @return
	 */
	public WifiInfo getWiFiInfo(){
		WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
		return wifiInfo;
	}
	
	class ConnectRunnable implements Runnable {
		private String ssid;
		private String password;
		private WifiCipherType type;

		public ConnectRunnable(String ssid,String password,WifiCipherType type){
			this.ssid = ssid;
			this.password = password;
			this.type = type;
		}

		@Override
		public void run() {
			//打开wifi
			openWifi();
			//开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
			//状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
			while(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
				try{
					//为了避免程序一直while循环，让它睡个100毫秒检测……
					Thread.sleep(100);
				}
				catch(InterruptedException ie){
				}
			}
			
			//构建连接信息
			WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
			
			if(wifiConfig == null){
				Log.d("wifi", "创建wifi连接信息错误");
				return;
			}
			
			//判断以前是否配置过这个连接
			WifiConfiguration tempConfig = isExsits(ssid);
			
			//如果配置了,需要先删除
			if(tempConfig != null){
				wifiManager.removeNetwork(tempConfig.networkId);
			}

			int netID = wifiManager.addNetwork(wifiConfig);
			boolean enabled = wifiManager.enableNetwork(netID,true);
			Log.e("wifi", "enableNetwork status enable=" + enabled);
			boolean connected = wifiManager.reconnect();
			Log.e("wifi", "enableNetwork connected=" + connected);
		}
	}
}
