package cn.com.mobnote.wifi;

public class WifiRsBean {
	String wifiName=null;
	boolean isConn=false;
    Integer wifiSignal=0;
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

}
