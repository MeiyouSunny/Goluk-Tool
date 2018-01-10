package com.mobnote.golukmain.thirdshare;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 获取真实Url地址
 */
public class GetRealUrlTask extends AsyncTask<String, Void, String> {

    private OnRealUrlListener mListener;

    public GetRealUrlTask(OnRealUrlListener listener) {
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(String... strings) {
        if (strings == null || strings.length <= 0)
            return "";
        String urlAddress = strings[0];
        try {
            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);
            conn.getResponseCode();
            final String realUrl = conn.getHeaderField("Location");
            conn.disconnect();
            if (!TextUtils.isEmpty(realUrl))
                return realUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return urlAddress;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String realUrl) {
        super.onPostExecute(realUrl);
        if (mListener != null)
            mListener.onGetRealUrl(realUrl);
    }

    public interface OnRealUrlListener {
        void onGetRealUrl(String realUrl);
    }

}
