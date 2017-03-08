package com.mobnote.golukmain;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.userlogin.UploadUtil;

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

        Map<String, File> files = new HashMap<String, File>();
        File file = new File(filePath);
        if (file.exists()) {
            files.put("file", file);
        }

        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("xieyi", "200");
        requestParams.put("commuid", uid);
        requestParams.put("commmid", mid);

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
