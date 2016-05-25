package com.mobnote.util;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.mobnote.application.GolukApplication;
import com.mobnote.map.LngLat;

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
    private Context context;

    public JavaScriptInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public String getData() {
        JSONObject result = new JSONObject();
        GolukApplication app = GolukApplication.getInstance();
        if (app.mGoluk != null) {
            String verName = app.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
                    IPageNotifyFn.PageType_GetVersion, "fs6:/version");
            result.put("commappversion", verName);
        } else {
            result.put("commappversion", "");
        }
        if (app.mIPCControlManager != null) {
            result.put("commhdtype",app.mIPCControlManager.mProduceName);
        } else {
            result.put("commhdtype", "");
        }
        result.put("commipcversion", SharedPrefUtil.getIPCVersion());
        result.put("commdevmodel", android.os.Build.MODEL);
        result.put("commlat", "" + LngLat.lat);
        result.put("commlon", "" + LngLat.lng);
        result.put("commmid", "" + Tapi.getMobileId());
        result.put("commostag", "android");
        result.put("commosversion", android.os.Build.VERSION.RELEASE);
        result.put("commticket", SharedPrefUtil.getUserToken());
        String uid = GolukApplication.getInstance().mCurrentUId;
        if (TextUtils.isEmpty(uid)) {
            result.put("commuid", "");
        } else {
            result.put("commuid", uid);
        }
        if(app.getMyInfo() != null){
            result.put("uid",app.getMyInfo().uid);
        }
        result.put("commversion", GolukUtils.getCommversion());
        result.put("commlocale", GolukUtils.getLanguageAndCountry());
        result.put("",context);
        return result.toString();
    }

}
