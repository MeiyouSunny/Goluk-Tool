package cn.com.mobnote.wifi;

public class WifiRsBean {
	String wifiName=null;
	boolean isConn=false;
	Integer wifiSignal=0;
	String ipAddress = null;
	public String getWifiName() {
		return wifiName;
	}
	public void setWifiName(String wifiName) {
		this.wifiName = wifiName;
	}
	public boolean isConn() {
		return isConn;
	}
	public void setConn(boolean isConn) {
		this.isConn = isConn;
	}
	public Integer getWifiSignal() {
		return wifiSignal;
	}
	public void setWifiSignal(Integer wifiSignal) {
		this.wifiSignal = wifiSignal;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}
