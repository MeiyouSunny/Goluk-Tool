package cn.com.tiros.api;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * WIFI Manager
 */
public class IpcWifiManager {

    private static Context mContext;
    private static WifiManager wifiManager;
    private static IpcWifiManager mInstance;

    public static void init(Context context) {
        mContext = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

    }

    public static IpcWifiManager getInstance() {
        if (mInstance == null)
            mInstance = new IpcWifiManager();
        return mInstance;
    }

    private IpcWifiManager() {
    }

    /**
     * Get current connected WIFI info
     */
    public static WIFIInfo getCurrentWifiInfo() {
        if (wifiManager == null) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return null;
        }
        if (wifiInfo.getSSID() != null && wifiInfo.getBSSID() != null) {
            WIFIInfo info = new WIFIInfo();
            info.mName = wifiInfo.getSSID();
            info.mMac = wifiInfo.getBSSID().replaceAll(":", "");
            info.mIp = "" + wifiInfo.getIpAddress();
            info.mSignalstrength = (short) wifiInfo.getRssi();
            return info;
        }
        return null;
    }

    /**
     * Is T1 or T2
     */
    public static boolean isT1T2() {
        WIFIInfo wifiInfo = getCurrentWifiInfo();
        return wifiInfo != null && (wifiInfo.mName.contains("T1") || wifiInfo.mName.contains("T2"));
    }

    /**
     * Is T3
     */
    public static boolean isT3() {
        WIFIInfo wifiInfo = getCurrentWifiInfo();
        return wifiInfo != null && wifiInfo.mName.contains("T3");
    }

    public static int getIpcModeValue() {
        if (isT1T2()) {
            return 2;
        } else if (isT3()) {
            return 0;
        }

        return -1;
    }

}
