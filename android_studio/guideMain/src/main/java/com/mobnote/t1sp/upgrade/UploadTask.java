package com.mobnote.t1sp.upgrade;

import android.os.AsyncTask;

import com.mobnote.t1sp.util.Const;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * T1SP 固件上传Task
 */
public class UploadTask extends AsyncTask<File, Void, Boolean> {

    private static final String UPLOAD_URL = Const.HTTP_SCHEMA_ADD_IP + "/cgi-bin/FWupload.cgi";
    private static final String DEFAULT_BIN_FILE_NAME = "SD_CarDV.bin";
    private static final int TIME_OUT = 300 * 1000;

    private UploadListener mListener;

    @Override
    protected Boolean doInBackground(File... files) {
        if (files == null || files.length <= 0)
            return false;

        final File binFile = files[0];

        long totalSize = binFile.length();
        // 边界标识 随机生成
        String BOUNDARY = "---------------------------7d4a6d158c9";
        String PREFIX = "--", LINE_END = "\r\n";
        // 内容类型
        String CONTENT_TYPE = "multipart/form-data";

        DataOutputStream dataOutputStream = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(UPLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIME_OUT);
            //conn.setReadTimeout(TIME_OUT);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("connection", "keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");
            conn.connect();
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
            byte[] bytes = new byte[1024 * 4];
            long lengthUploaded = 0;
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                dataOutputStream.write(bytes, 0, len);
                //dataOutputStream.flush();
                lengthUploaded += len;
                //updateProgress(lengthUploaded, totalSize);
            }

            inputStream.close();

            dataOutputStream.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dataOutputStream.write(end_data);
            dataOutputStream.flush();

            conn.getInputStream();
            int responseCode = conn.getResponseCode();
//            if (responseCode == 200) {
//                conn.getInputStream();
//            }
            dataOutputStream.close();
            conn.disconnect();
            return (responseCode == 200);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//                inputStream.close();
            //dataOutputStream.close();
        }

        return false;
    }

    private void updateProgress(long lengthUploaded, long totalSize) {
        int progress = (int) (((float) lengthUploaded / totalSize) * 100);
        if (mListener != null)
            mListener.onUploadProgress(progress);
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

        void onUploadProgress(int progress);
    }

}
