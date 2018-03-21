package com.mobnote.wifibind;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.multicast.NetUtil;
import com.mobnote.golukmain.reportlog.ReportLogManager;
import com.mobnote.util.JsonUtil;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.tiros.api.Const;
import cn.com.tiros.debug.GolukDebugUtils;

public class WifiConnectManagerSupport {

    // private static final String FILEPATH = Environment
    // .getExternalStorageDirectory().getPath() + "/wificonfig/"; // 配置文件存储路径
    private static final String FILEPATH = Const.getAppContext().getFilesDir().getPath() + "/wificonfig/";

    private static final int BUF_SIZE = 1024;
    private static final String TAG = "WifiConnectManagerSupport";
    private WifiManager wifiManager = null;

    public WifiConnectManagerSupport(WifiManager _wifiManager) {
        this.wifiManager = _wifiManager;
    }

    // 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    // 查看以前是否也配置过这个网络
    WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs == null) {
            return null;
        }
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    public boolean joinWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration wifiConfig = this.setWifiInfo(SSID, Password, Type);
        if (wifiConfig == null) {
            return false;
        }

        WifiConfiguration tempConfig = this.isExsits(SSID);

        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        int netID = wifiManager.addNetwork(wifiConfig);
        // 为负数时是连接失败
        if (netID < 0) {
            return false;
        }

        boolean bRet = wifiManager.enableNetwork(netID, true);
        Log.e(TAG, "networkconn--------------------" + bRet + "--------");
        // 网络状态连接失败
        if (!bRet) {
            return false;
        }

        bRet = wifiManager.reconnect();

        Log.e(TAG, "wificonn----------------" + bRet + "-------------");
        // wifi 连接失败
        if (!bRet) {
            return false;
        }
        return bRet;
    }

    WifiConfiguration setWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // nopass
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "\"\"";
            // config.
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        // wep
        if (Type == WifiCipherType.WIFICIPHER_WEP) {
            if (!TextUtils.isEmpty(Password)) {
                if (isHexWepKey(Password)) {
                    config.wepKeys[0] = Password;
                } else {
                    config.wepKeys[0] = "\"" + Password + "\"";
                }
            }
            config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        // wpa
        if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // 此处需要修改否则不能自动重联
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    public boolean disConnWifi() {
        return wifiManager.disconnect();
    }

    // 打开wifi功能
    boolean openWifi(boolean restart) {
        // 打开 wifi 功能
        boolean bRet = true;

        // 如果强制重启
        if (restart) {
            bRet = wifiManager.setWifiEnabled(false);
            bRet = wifiManager.setWifiEnabled(true);
        } else {
            if (!wifiManager.isWifiEnabled()) {
                collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "setWifiEnabled started");
                bRet = wifiManager.setWifiEnabled(true);
                collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "setWifiEnabled finished");
            }
        }
        return bRet;
    }

    // 关闭wifi功能
    boolean closeWifi() {
        collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "setWifi not Enabled");
        setWifiApEnabled(null, false);
        boolean bRet = wifiManager.isWifiEnabled();
        int count = 0;
        while (bRet) {
            try {
                if (count == 5) {
                    return false;
                }
                Thread.sleep(500);
                collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "setWifi not Enabled waiting 500 MS");
                bRet = wifiManager.isWifiEnabled();
                count++;
            } catch (Exception e) {
            }

        }
        collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "setWifi not Enabled");
        return bRet;
    }

    private static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
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
     * 得到扫描结果
     */
    public WifiRsBean[] getScanResult(String title, Integer type) {
        WifiRsBean bean = null;
        WifiRsBean[] rs = null;
        Collection<WifiRsBean> listRs = new ArrayList<WifiRsBean>();
        boolean result = false;
        String regEx = "^" + title;
        WifiInfo info = wifiManager.getConnectionInfo();
        String conSSid = info.getSSID();
        // 开始扫描网络
        wifiManager.startScan();
        List<ScanResult> scanResult = wifiManager.getScanResults();
        Log.e(TAG, "sanrs-----------------" + (scanResult == null) + "------------");

        if (scanResult != null) {
            for (ScanResult tempResult : scanResult) {
                // 判断是相等还是模糊匹配
                if (type == null) {

                    result = Pattern.compile(regEx).matcher(tempResult.SSID).find();
                } else {
                    result = title.equals(tempResult.SSID);
                }

                if (result) {
                    bean = new WifiRsBean();
                    bean.setIpc_mac(tempResult.BSSID); // ssid
                    bean.setIpc_ssid(tempResult.SSID); // ssid
                    bean.setWifiSignal(WifiManager.calculateSignalLevel(tempResult.level, 4)); // 信号等级
                    bean.setIpc_bssid(tempResult.BSSID); // 设置mac地址
                    bean.setPassnull(ispassnullType(tempResult.capabilities)); // 是否是无密码类型
                    if (conSSid != null && !"".equals(conSSid)) {
                        if (("\"" + bean.getIpc_ssid() + "\"").equals(conSSid)) {
                            bean.setIsconn(true);
                        }
                    }
                    listRs.add(bean);
                }
            }

        }
        if (listRs.size() > 0) {
            rs = (WifiRsBean[]) listRs.toArray(new WifiRsBean[0]);
            listRs = null;
        }

        return rs;
    }

    /**
     * 获得连接wifi的信息
     */
    public WifiRsBean getConnResult() {
        WifiInfo info = wifiManager.getConnectionInfo();
        WifiRsBean bean = new WifiRsBean();
        bean.setPh_ssid(info.getSSID());
        bean.setPh_bssid(info.getBSSID());
        bean.setPh_mac(info.getMacAddress());
        bean.setWifiSignal(getWifiLevel(info.getRssi()));
        bean.setPh_ip(int2ip(info.getIpAddress()));
        return bean;
    }

    /**
     * 获得匹配连接wifi的信息是否有效
     */
    public WifiRsBean getConnResult(String title) {
        String regEx = "^" + title;
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).addLogData(JsonUtil.getReportData(TAG, "getConnResult", "before getConnectionInfo"));

        // 先判断当前WIFI是否已经连接上
        boolean isWifiConnected = NetUtil.isWIFIConnected(GolukApplication.getInstance().getApplicationContext());
        if (!isWifiConnected) {
            return null;
        }

        WifiInfo info = wifiManager.getConnectionInfo();
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).addLogData(JsonUtil.getReportData(TAG, "getConnResult", "after getConnectionInfo"));
        WifiRsBean bean = null;
        if (info != null && info.getSSID() != null && !info.getSSID().equals("<unknown ssid>")) {

            String tmpSsid = info.getSSID().replace("\"", "");
            ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).addLogData(JsonUtil.getReportData(TAG, "getConnResult", "info true :ssid " + tmpSsid));
            //Matcher matcher = Pattern.compile(regEx).matcher(tmpSsid);
            //if (matcher != null && matcher.find()) {
                ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).addLogData(JsonUtil.getReportData(TAG, "getConnResult", "matcher.find() true "));
                bean = new WifiRsBean();
                bean.setIpc_ssid(tmpSsid);
                bean.setIpc_bssid(info.getMacAddress());
                bean.setWifiSignal(getWifiLevel(info.getRssi()));
                bean.setIpc_ip(int2ip(info.getIpAddress()));
            //}
        }
        String msg ;
        if(bean == null){
            msg = "bean Result: is null ";
        }else{
            msg = "bean Result: "+ bean.toString();
        }
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).addLogData(JsonUtil.getReportData(TAG, "getConnResult", msg));
        return bean;
    }

    /**
     * 判断ipc是否在列表中
     *
     * @param ssid
     * @param beans
     * @return
     */
    public boolean inWifiGroup(String ssid, WifiRsBean[] beans) {
        boolean flag = false;
        if (beans == null) {
            flag = false;
        } else {
            for (WifiRsBean temp : beans) {
                if (temp.getIpc_ssid().equals(ssid)) {
                    flag = true;
                    break;
                }

            }
        }
        return flag;
    }

    /**
     * 运行状态
     *
     * @param state ConnectivityManager.TYPE_WIFI
     */
    public State isWifiConnected(Context context, int type_wifi) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(type_wifi);
            if (mWiFiNetworkInfo != null) {
                // Log.d(TAG,
                // "mWiFiNetworkInfo----------------"+mWiFiNetworkInfo.getState());

                return mWiFiNetworkInfo.getState();
            }
        }
        return null;
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    private static String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    public static int getWifiLevel(int level) {

        if (level >= -50) {
            return 4;
        } else {
            // 信号二级
            if (level < -50 && level > -70) {
                return 3;
            } else {
                // 信号二级
                if (level <= -70) {
                    return 2;
                } else {

                }
            }
        }
        return 0;
    }

    public boolean isWifiApEnabled(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);

        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 检查是否是无密码类型
     *
     * @param value
     * @return
     */
    private boolean ispassnullType(String value) {
        if ((value.indexOf("[ESS]") == 0 && value.length() == 5)
                || (value.indexOf("[WPS][ESS]") == 0 && value.length() == 10)) {

            // 只有一种类型 并且是ESS类型 认为是无密码
            return true;
        }
        return false;
    }

    /**
     * 写文件
     *
     * @param fileName
     * @param ssid
     * @param passWord
     * @return
     */
    boolean writePassFile(String fileName, String value) throws Exception {
        String tempPath = FILEPATH + fileName;

        File dir = new File(FILEPATH);
        // 先檢查該目錄是否存在
        if (!dir.exists()) {
            // 若不存在則建立它
            dir.mkdir();
        }
        byte[] srcByte = (value).getBytes();
        byte[] writeBytes = ThreeDES.encryptMode(srcByte);

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(new File(tempPath));
            out.write(writeBytes);
            return true;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    /**
     * 从文件
     *
     * @param fileName
     * @return
     */
    public WifiRsBean readConfig(String fileName) {
        String configString;
        try {
            configString = readPassFile(fileName);
            if (TextUtils.isEmpty(configString)) {
                return null;
            }
            JSONObject config = new JSONObject(configString);
            WifiRsBean rs = new WifiRsBean();
            rs.setIpc_ssid(config.getString("ipc_ssid"));
            rs.setIpc_ip(config.getString("ipc_ip"));
            rs.setIpc_mac(config.getString("ipc_mac"));
            rs.setPh_ssid(config.getString("ph_ssid"));
            rs.setPh_pass(config.getString("ph_pass"));
            rs.setIpc_pass(config.getString("ipc_pass"));
            rs.setIpc_model(config.getString("ipc_model"));
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 从文件中读取
     *
     * @param fileName
     * @return
     */
    public String readPassFile(String fileName) throws Exception {

        String tempPath = FILEPATH + fileName;
        File file = null;
        byte[] types = null;
        BufferedInputStream in = null;
        ByteArrayOutputStream bos = null;
        try {

            file = new File(tempPath);
            if (!file.exists()) {
                return null;
            }

            bos = new ByteArrayOutputStream((int) file.length());

            in = new BufferedInputStream(new FileInputStream(file));

            byte[] buffer = new byte[BUF_SIZE];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, BUF_SIZE))) {
                bos.write(buffer, 0, len);
            }
            types = bos.toByteArray();
            byte[] rs = ThreeDES.decryptMode(types);
            if (rs != null) {
                String ssid_pass = new String(rs);
                return ssid_pass;
            }
            return null;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            file = null;
        }
    }

    public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        try {

            if (wifiManager != null && wifiManager.getConnectionInfo() != null) { // disable
                // WiFi
                // in
                // any
                // case
                wifiManager.setWifiEnabled(false);
            }

            Method method = wifiManager.getClass()
                    .getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    // 获得当前WIFI热点信息
    public WifiRsBean getNetworkSSID(Context mContext) {
        WifiConfiguration ation = getWifiApConfiguration();
        WifiRsBean bean = new WifiRsBean();
        bean.setPh_ssid(ation.SSID);
        return bean;
    }

    public WifiConfiguration getWifiApConfiguration() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(wifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return null;
        }
    }

    /**
     * Return whether Wi-Fi AP is enabled or disabled.
     *
     * @return {@code true} if Wi-Fi AP is enabled
     * @hide Dont open yet
     * @see #getWifiApState()
     */
    public boolean isWifiApEnabled() {
        return getWifiApState();
    }

    /**
     * Gets the Wi-Fi enabled state.
     *
     * @return {@link WIFI_AP_STATE}
     * @see #isWifiApEnabled()
     */
    public boolean getWifiApState() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");

            int tmp = ((Integer) method.invoke(wifiManager));

            // Fix for Android 4
            if (tmp >= 10) {
                tmp = tmp - 10;
            }

            return true;
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    private void collectLog(String method, String msg) {
        JSONObject logJson = JsonUtil.getReportData(TAG, method, msg);
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
                .addLogData(JsonUtil.getReportData(TAG, method, msg));

        // XLog
        XLog.i(logJson);
    }
}
