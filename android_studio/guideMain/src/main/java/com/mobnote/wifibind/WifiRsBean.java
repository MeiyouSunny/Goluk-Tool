package com.mobnote.wifibind;

public class WifiRsBean {
    String ipc_bssid = ""; // IPC的bssid
    String ipc_ssid = ""; // IPC的ssid
    String ipc_mac = ""; // IPC的mac地址
    String ipc_model = "";

    String ipc_ip = ""; // IPC的IP地址
    String ipc_pass = ""; // IPC PASS

    public void setIpc_pass(String ipc_pass) {
        this.ipc_pass = ipc_pass;
    }

    String ph_ssid = ""; // 手机的ssid
    String ph_mac = ""; // 手机的mac地址
    String ph_pass = ""; // 手机的pass

    public String getIpc_pass() {
        return ipc_pass;
    }

    public String getPh_pass() {
        return ph_pass;
    }

    public void setPh_pass(String ph_pass) {
        this.ph_pass = ph_pass;
    }

    public String getPh_bssid() {
        return ph_bssid;
    }

    public void setPh_bssid(String ph_bssid) {
        this.ph_bssid = ph_bssid;
    }

    String ph_ip = ""; // 手机的IP地址
    String ph_bssid = ""; // 手机的bssid
    boolean isconn = false;
    boolean passnull = false; // 是否无密码类型 true 是 false 不是；
    Integer wifiSignal = 0; // 信号等级 4 3 2 1

    public String getIpc_bssid() {
        return ipc_bssid;
    }

    public void setIpc_bssid(String ipc_bssid) {
        this.ipc_bssid = ipc_bssid;
    }

    public String getIpc_ssid() {
        return ipc_ssid;
    }

    public void setIpc_ssid(String ipc_ssid) {
        this.ipc_ssid = ipc_ssid;
    }

    public String getIpc_mac() {
        return ipc_mac;
    }

    public void setIpc_mac(String ipc_mac) {
        this.ipc_mac = ipc_mac;
    }

    public String getIpc_ip() {
        return ipc_ip;
    }

    public void setIpc_ip(String ipc_ip) {
        this.ipc_ip = ipc_ip;
    }

    public String getPh_ssid() {
        return ph_ssid;
    }

    public void setPh_ssid(String ph_ssid) {
        this.ph_ssid = ph_ssid;
    }

    public String getPh_mac() {
        return ph_mac;
    }

    public void setPh_mac(String ph_mac) {
        this.ph_mac = ph_mac;
    }

    public String getPh_ip() {
        return ph_ip;
    }

    public void setPh_ip(String ph_ip) {
        this.ph_ip = ph_ip;
    }

    public boolean isIsconn() {
        return isconn;
    }

    public void setIsconn(boolean isconn) {
        this.isconn = isconn;
    }

    public boolean isPassnull() {
        return passnull;
    }

    public void setPassnull(boolean passnull) {
        this.passnull = passnull;
    }

    public Integer getWifiSignal() {
        return wifiSignal;
    }

    public void setWifiSignal(Integer wifiSignal) {
        this.wifiSignal = wifiSignal;
    }

    public String getIpc_model() {
        return ipc_model;
    }

    public void setIpc_model(String ipc_model) {
        this.ipc_model = ipc_model;
    }

    public WifiRsBean(String ipAddr, String hWAddr, boolean isReachable) {
        super();
        this.ipc_ip = ipAddr;
        this.ipc_mac = hWAddr;

        this.isconn = isReachable;
    }

    public WifiRsBean() {

    }


    @Override
    public String toString() {
        return "WifiRsBean{" +
                "ipc_bssid='" + ipc_bssid + '\'' +
                ", ipc_ssid='" + ipc_ssid + '\'' +
                ", ipc_mac='" + ipc_mac + '\'' +
                ", ipc_model='" + ipc_model + '\'' +
                ", ipc_ip='" + ipc_ip + '\'' +
                ", ipc_pass='" + ipc_pass + '\'' +
                ", ph_ssid='" + ph_ssid + '\'' +
                ", ph_mac='" + ph_mac + '\'' +
                ", ph_pass='" + ph_pass + '\'' +
                ", ph_ip='" + ph_ip + '\'' +
                ", ph_bssid='" + ph_bssid + '\'' +
                ", isconn=" + isconn +
                ", passnull=" + passnull +
                ", wifiSignal=" + wifiSignal +
                '}';
    }
}
