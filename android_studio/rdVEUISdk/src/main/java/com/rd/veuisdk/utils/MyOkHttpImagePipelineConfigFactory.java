package com.rd.veuisdk.utils;

import android.content.Context;

import com.facebook.imagepipeline.core.ImagePipelineConfig;

import java.security.cert.CertificateException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * support https、http
 */
public class MyOkHttpImagePipelineConfigFactory {

    public static ImagePipelineConfig.Builder newBuilder(Context context, OkHttpClient okHttpClient) {
        return ImagePipelineConfig.newBuilder(context).setNetworkFetcher(new MyOkHttpNetworkFetcher(okHttpClient));
    }

    /**
     * 忽略https证书
     *
     * @return
     */
    public static OkHttpClient getHttpClient() {
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        }};
        OkHttpClient mSyncHttpClient = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext
                    .getSocketFactory();
            mSyncHttpClient = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    // 设置超时时间
                    .readTimeout(20, TimeUnit.SECONDS)
                    // 设置读取超时时间
                    .writeTimeout(20, TimeUnit.SECONDS)
                    // .sslSocketFactory(sslSocketFactory,
                    // (X509TrustManager) trustAllCerts[0])
                    .sslSocketFactory(sslSocketFactory)
                    .hostnameVerifier(new HostnameVerifier() {

                        @Override
                        public boolean verify(String hostname,
                                              SSLSession session) {
                            return true;

                        }

                    }).build();// 设置写入超时时间

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)

        {
            e.printStackTrace();
        }

        return mSyncHttpClient;
    }
}
