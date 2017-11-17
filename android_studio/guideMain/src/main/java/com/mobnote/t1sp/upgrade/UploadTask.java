package com.mobnote.t1sp.upgrade;

import android.os.AsyncTask;

import com.mobnote.t1sp.util.Const;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * T1SP 固件上传Task
 */
public class UploadTask extends AsyncTask<File, Void, Boolean> {

    private static final String UPLOAD_URL = Const.HTTP_SCHEMA_ADD_IP + "/cgi-bin/FWupload.cgi";
    private static final String DEFAULT_BIN_FILE_NAME = "SD_CarDV.bin";
    private static final int TIME_OUT = 15 * 1000;

    private UploadListener mListener;

    @Override
    protected Boolean doInBackground(File... files) {
        if (files == null || files.length <= 0)
            return false;

        final File binFile = files[0];

        // 边界标识 随机生成
        String BOUNDARY = UUID.randomUUID().toString();
        String PREFIX = "--", LINE_END = "\r\n";
        // 内容类型
        String CONTENT_TYPE = "multipart/form-data";

        DataOutputStream dataOutputStream = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(UPLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            // 文件包装上传
            dataOutputStream = new DataOutputStream(conn.getOutputStream());
            StringBuffer sb = new StringBuffer();
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINE_END);
            // name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
            // filename是文件的名字，包含后缀名的 比如:abc.png
            //sb.append("Content-Disposition: form-data; name=\"fileupload\"; filename=\""
            //        + file.getName() + "\"" + LINE_END);
            sb.append("Content-Disposition: form-data; name=\"fileupload\";filename=\""
                    + DEFAULT_BIN_FILE_NAME + "\"" + LINE_END);
            sb.append("Content-Type: application/octet-stream; charset=" + "UTF-8" + LINE_END);
            sb.append(LINE_END);
            dataOutputStream.write(sb.toString().getBytes());
            inputStream = new FileInputStream(binFile);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                dataOutputStream.write(bytes, 0, len);
                //dataOutputStream.flush();
            }

            dataOutputStream.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dataOutputStream.write(end_data);
            dataOutputStream.flush();

            conn.getInputStream();
            int responseCode = conn.getResponseCode();
            return (responseCode == 200);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                //dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (mListener != null)
            mListener.onUploaded(result);
    }

    public void setListener(UploadListener listener) {
        this.mListener = listener;
    }

    public interface UploadListener {
        void onUploaded(boolean success);
    }

}
