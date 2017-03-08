package com.mobnote.golukmain.livevideo;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.util.GolukUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cn.com.tiros.debug.GolukDebugUtils;

/**
 * Created by leege100 on 16/7/12.
 */
public class UploadLiveScreenShotTask extends AsyncTask<String, Integer, String>{
    private static String mRequestUrl = HttpManager.getInstance().getWebDirectHost() + "/navidog4MeetTrans/uploadpic.htm";
    private String filePath;
    private String uid;
    private String vid;
    private CallbackUploadLiveScreenShot uploadLiveScreenShotListener;
    public UploadLiveScreenShotTask(String fPath,String uid,String vid,CallbackUploadLiveScreenShot listener) {
        this.filePath = fPath;
        this.uid = uid;
        this.vid = vid;
        this.uploadLiveScreenShotListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {

        final Map<String, File> files = new HashMap<String, File>();
        File file = new File(filePath);
        if (file.exists()) {
            files.put("pic", file);
        }

        final Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("xieyi", "200");
        requestParams.put("md5", GolukUtils.getFileMD5(file));
        requestParams.put("commuid",uid);
        requestParams.put("vid",vid);
        requestParams.put("devicetag", GolukApplication.getInstance().mIPCControlManager.mProduceName);

        String result = null;
        try {
            result = post(mRequestUrl, requestParams, files);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if(!TextUtils.isEmpty(result)) {
            JSONObject resultJson = JSON.parseObject(result);
            if (resultJson != null) {
                String code = resultJson.getString("code");
                if (!TextUtils.isEmpty(code) && "200".equals(code)) {
                    uploadLiveScreenShotListener.onUploadLiveScreenShotSuccess();
                    return;
                }
            }
            uploadLiveScreenShotListener.onUploadLiveScreenShotFail();
        }
    }
    public static String post(String url, Map<String, String> params, Map<String, File> files)
            throws IOException {
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String MULTIPART_FROM_DATA = "application/octest-stream";

        URL uri = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(10 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            conn.addRequestProperty(entry.getKey(), entry.getValue());
        }

        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
        // 发送文件数据
        if (files != null)
            for (Map.Entry<String, File> file : files.entrySet()) {

                InputStream is = new FileInputStream(file.getValue());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                is.close();
            }

        outStream.flush();
        // 得到响应码
        int statusCode = conn.getResponseCode();
        InputStream is = null;
        if (statusCode >= 200 && statusCode < 400) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }

        StringBuilder sb2 = new StringBuilder();
        int ch;
        while ((ch = is.read()) != -1) {
            sb2.append((char) ch);
        }

        outStream.close();
        conn.disconnect();
        return sb2.toString();
    }

    public interface CallbackUploadLiveScreenShot{
        public void onUploadLiveScreenShotSuccess();
        public void onUploadLiveScreenShotFail();
    }
}
