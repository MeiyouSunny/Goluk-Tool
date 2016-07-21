package com.mobnote.golukmain.livevideo;

import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by leege100 on 16/7/12.
 */
public class UploadLiveScreenShotTask extends AsyncTask<String, Integer, String>{
    private static String mRequestUrl = HttpManager.getInstance().getWebDirectHost() + "/navidog4MeetTrans/uploadpic.htm";

    private String filePath;
    private String uid;
    public UploadLiveScreenShotTask(String fPath,String uid) {
        this.filePath = fPath;
        this.uid = uid;
    }

    @Override
    protected String doInBackground(String... params) {

        final Map<String, File> files = new HashMap<String, File>();
        File file = new File(filePath);
        files.put("pic", file);

        final Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("md5", GolukUtils.getFileMD5(file));
        requestParams.put("uid",uid);
        requestParams.put("vid",uid);

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

}
