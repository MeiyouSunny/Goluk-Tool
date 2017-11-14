package com.mobnote.golukmain;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.userlogin.UploadUtil;
import com.mobnote.map.LngLat;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.Tapi;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * Created by leege100 on 2017/3/7.
 */

public class LogUploadTask extends AsyncTask<String, Integer, String> {
    private static String mRequestUrl = HttpManager.getInstance().getWebDirectHost() + "/cdcAdmin/log.htm";
    private String filePath;
    private String uid;
    private String mid;
    private CallbackLogUpload uploadLogListener;

    public LogUploadTask(String fPath, String uid, String mid, CallbackLogUpload listener) {
        this.filePath = fPath;
        this.uid = uid;
        this.mid = mid;
        this.uploadLogListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("xieyi", "200");
        GolukApplication app = GolukApplication.getInstance();
        if (app.mGoluk != null) {
            String verName = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
                    IPageNotifyFn.PageType_GetVersion, "fs6:/version");
            requestParams.put("commappversion", verName);
        } else {
            requestParams.put("commappversion", "");
        }
        if (app.mIPCControlManager != null) {
            requestParams.put("commhdtype", GolukApplication.getInstance().mIPCControlManager.mProduceName);
        } else {
            requestParams.put("commhdtype", "");
        }
        requestParams.put("commipcversion", SharedPrefUtil.getIPCVersion());
        requestParams.put("commdevmodel", android.os.Build.MODEL);
        requestParams.put("commlat", "" + LngLat.lat);
        requestParams.put("commlon", "" + LngLat.lng);
        requestParams.put("commmid", "" + Tapi.getMobileId());
        requestParams.put("commostag", "android");
        requestParams.put("commosversion", android.os.Build.VERSION.RELEASE);
        requestParams.put("commticket", SharedPrefUtil.getUserToken());
        String uid = GolukApplication.getInstance().mCurrentUId;
        if (TextUtils.isEmpty(uid)) {
            requestParams.put("commuid", "");
        } else {
            requestParams.put("commuid", uid);
        }
        requestParams.put("commversion", GolukUtils.getCommversion());
        requestParams.put("commlocale", GolukUtils.getLanguageAndCountry());
        String result = null;
        try {
            result = UploadUtil.uploadFile(mRequestUrl, requestParams, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (!TextUtils.isEmpty(result)) {
            JSONObject resultJson = JSON.parseObject(result);
            if (resultJson != null) {
                String code = resultJson.getString("code");
                if (!TextUtils.isEmpty(code) && "0".equals(code)) {
                    uploadLogListener.onUploadLogSuccess();
                    return;
                }
            }
        }
        uploadLogListener.onUploadLogFail();
    }

    public interface CallbackLogUpload {

        /**
         * upload success
         */
        void onUploadLogSuccess();

        /**
         * upload fail
         */
        void onUploadLogFail();
    }
}
