package cn.com.mobnote.wifibind;

public class WifiRsBean {
	boolean passnull =false;  //是否无密码类型  true 是  false 不是；
	public boolean isPassnull() {
		return passnull;
	}
	public void setPassnull(boolean passnull) {
		this.passnull = passnull;
	}
	public boolean isIsconn() {
		return isconn;
	}
	public void setIsconn(boolean isconn) {
		this.isconn = isconn;
	}
	boolean isconn=false;
    String ssid="";   // ssid
    Integer wifiSignal=0;   //信号等级  4 3 2 1
    String bssid=null;    //暂时无用
    public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	String ipaddress=null;    //ip地址
    String macaddress=null;  //  mac地址
	public Integer getWifiSignal() {
		return wifiSignal;
	}
	public void setWifiSignal(Integer wifiSignal) {
		this.wifiSignal = wifiSignal;
	}
	public String getBssid() {
		return bssid;
	}
	public void setBssid(String bssid) {
		this.bssid = bssid;
	}
	public String getIpaddress() {
		return ipaddress;
	}
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	public String getMacaddress() {
		return macaddress;
	}
	public void setMacaddress(String macaddress) {
		this.macaddress = macaddress;
	}
	public WifiRsBean(String ipAddr, String hWAddr, boolean isReachable) {
		super();
		this.ipaddress = ipAddr;
		this.macaddress = hWAddr;
		 
		this.isconn = isReachable;
	}
	public WifiRsBean(){
		
	}
}
