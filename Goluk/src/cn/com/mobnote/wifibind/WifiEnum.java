package cn.com.mobnote.wifibind;

/**
 * wifi配置
 * @author hanzheng
 *
 */
public class WifiEnum {
	/**
	 * wifi属性
	 * 
	 * @author hanzheng
	 * 
	 */
	public enum WifTypeEnum {
		/**
		 * SSID
		 */
		SSID
		/**
		 * MAC地址
		 */
		, MAC
	}
	
	/**
	 * wifi密码类型
	 * [WPA-PSK-CCMP+TKIP][WPA2-PSK-CCMP+TKIP][WPS][ESS]
	 * @author hanzheng
	 * 
	 */
	public enum WifTypePassEnum {
		/**
		 * WPA-PSK-CCMP+TKIP
		 */
		WPA_PSK_CCMP_TKIP
		/**
		 * WPA2-PSK-CCMP+TKIP
		 */
		,WPA2_PSK_CCMP_TKIP
		/**
		 * WPS
		 */
		,WPS
		/**
		 * ESS
		 */
		, ESS
	}
	/**
	 * 获得wifiEnum
	 * @param args
	 * @return
	 */
	public static int getWifTypeEnum(WifTypeEnum args) {
		switch (args) {
		case SSID:
			return 1;
		case MAC:
			return 2;
		default:
			break;
		}
		return 0;
	}
}
