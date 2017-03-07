package com.mobnote.golukmain;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by leege100 on 2017/3/7.
 */

public class LogUploadTask extends AsyncTask<String, Integer, String> {
//    private static String mRequestUrl = HttpManager.getInstance().getWebDirectHost() + "/navidog4MeetTrans/uploadpic.htm";
    private static String mRequestUrl = "http://service.crazypandacam.com/system/log";
    private String filePath;
    private String uid;
    private String mid;
    private CallbackLogUpload uploadLogListener;
    public LogUploadTask(String fPath,String uid,String mid,CallbackLogUpload listener) {
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
        requestParams.put("xieyi", "100");
        requestParams.put("commuid",uid);
        requestParams.put("commmid",mid);

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
                if (!TextUtils.isEmpty(code) && "0".equals(code)) {
                    uploadLogListener.onUploadLogSuccess();
                    return;
                }
            }
        }
        uploadLogListener.onUploadLogFail();
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
        conn.setRequestProperty("Charset", "UTF-8");
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
        int res = conn.getResponseCode();
        InputStream in = conn.getInputStream();
        StringBuilder sb2 = new StringBuilder();
        if (res == 200) {
            int ch;
            while ((ch = in.read()) != -1) {
                sb2.append((char) ch);
            }
        }
        outStream.close();
        conn.disconnect();
        return sb2.toString();
    }

    public interface CallbackLogUpload{
        void onUploadLogSuccess();
        void onUploadLogFail();
    }
}
