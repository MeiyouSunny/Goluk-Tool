package com.mobnote.wifibind;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

public class WifiUtil {

    private WifiManager wifiManager;

    public WifiUtil(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public String getWifiName() {

        // 先判断当前WIFI是否已经连接上
//        boolean isWifiConnected = NetUtil.isWIFIConnected(GolukApplication.getInstance().getApplicationContext());
//        if (!isWifiConnected) {
//            return null;
//        }
        String ssid = "";

        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null && info.getSSID() != null && !info.getSSID().equals("<unknown ssid>") && !info.getSSID().equals("0x")) {
            ssid = info.getSSID().replace("\"", "");
        }
        if (TextUtils.isEmpty(ssid)) {
            // Android 8 9
            if (Build.VERSION.SDK_INT >= 27) {
                ssid = getSSIDByNetWorkId();
            }
        }

        // 有些机型获取到的WIFI名称用引号包围了,需要去掉双引号/单引号如, Goluk_T1_xxx -> "Goluk_T1_xxx"
        if (!TextUtils.isEmpty(ssid)) {
            ssid = ssid.replaceAll("\"", "");
            ssid = ssid.replaceAll("'", "");
        }

        return ssid;
    }

    /**
     * 先获取当前WIFI的NetWorkId,在获取当前扫描到的WIFI列表,对比NetWorkId,获取对应的SSID
     *
     * @return
     */
    private String getSSIDByNetWorkId() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int netId = wifiInfo.getNetworkId();
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : list) {
                if (netId == wifiConfiguration.networkId) {
                    String ssid = wifiConfiguration.SSID;
                    return ssid;
                }
            }
        }

        return "";
    }

}
