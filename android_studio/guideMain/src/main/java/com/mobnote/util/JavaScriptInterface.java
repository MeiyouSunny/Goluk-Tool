package com.mobnote.util;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.mobnote.application.GolukApplication;
import com.mobnote.map.LngLat;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.Tapi;

/**
 * Created by zenghao on 2016/5/24.
 */
public class JavaScriptInterface {
    /*
         * 绑定的object对象
         * */
    private Context mContext;

    public JavaScriptInterface(Context context) {
        this.mContext = context;
    }

    @JavascriptInterface
    public String getAppCommData() {
        JSONObject result = new JSONObject();
        GolukApplication app = GolukApplication.getInstance();
        result.put("commversion", GolukUtils.getCommversion());
        result.put("commmid", "" + Tapi.getMobileId());
        result.put("commipcversion", SharedPrefUtil.getIPCVersion());
        if (app.mIPCControlManager != null) {
            result.put("commhdtype", app.mIPCControlManager.mProduceName);
        } else {
            result.put("commhdtype", "");
        }
        result.put("commdevmodel", android.os.Build.MODEL);
        result.put("commostag", "android");
        result.put("commosversion", android.os.Build.VERSION.RELEASE);
        result.put("commsysversion", android.os.Build.VERSION.RELEASE);
        result.put("commappversion", GolukUtils.getAppVersionName(mContext));
        if(GolukUtils.checkWifiStatus(mContext)) {
            result.put("commwifi", "1");
        } else {
            result.put("commwifi", "0");
        }

        String uid = GolukApplication.getInstance().mCurrentUId;
        if (TextUtils.isEmpty(uid)) {
            result.put("commuid", "");
        } else {
            result.put("commuid", uid);
        }

        result.put("commlat", "" + LngLat.lat);
        result.put("commlon", "" + LngLat.lng);
        result.put("commlocale", GolukUtils.getLanguageAndCountry());
        result.put("commticket", SharedPrefUtil.getUserToken());
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(
                System.currentTimeMillis()));
        result.put("commtimestamp", timeStamp);

        return result.toString();
    }

}
