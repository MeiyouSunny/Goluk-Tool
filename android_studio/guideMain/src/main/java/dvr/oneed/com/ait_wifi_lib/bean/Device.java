package dvr.oneed.com.ait_wifi_lib.bean;

import java.io.Serializable;

/**
 * Created by hyde on 2016/6/17 0017.
 */
public class Device implements Serializable {
    public String ssid;  //wifi 名称
    public String pwd;   //wifi 密码
    public String ip;    //ip
    public String gateway  ;   //链接的网关
    public String networkId;   //网络id
    public String dvrWifiType;  //链接协议 类型 ait,联涌,
    public String productId;//厂商代号
    public String uuid;  //设备唯一号
    public String macdress;   //地址
    public int    selectStatus;  //选择状态
    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getDvrWifiType() {
        return dvrWifiType;
    }

    public void setDvrWifiType(String dvrWifiType) {
        this.dvrWifiType = dvrWifiType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMacdress() {
        return macdress;
    }

    public void setMacdress(String macdress) {
        this.macdress = macdress;
    }

    public int getSelectStatus() {
        return selectStatus;
    }

    public void setSelectStatus(int selectStatus) {
        this.selectStatus = selectStatus;
    }



}
