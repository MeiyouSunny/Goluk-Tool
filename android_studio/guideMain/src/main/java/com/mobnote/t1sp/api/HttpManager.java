package com.mobnote.t1sp.api;

import java.util.concurrent.TimeUnit;

import likly.reverse.Reverse;
import likly.reverse.RxAndroidAdapter;
import okhttp3.OkHttpClient;

/**
 * OkHttpClient 配置类
 */
public class HttpManager {

    /* 默认超时时间 */
    private final static int DEFAULT_TIME_OUT = 15 * 1000;

    /**
     * 初始化网络请求
     */
    public static void initHttp() {
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        final RxAndroidAdapter rxAndroidAdapter = new RxAndroidAdapter(okHttpClientBuilder);
        new Reverse.Builder().adapter(rxAndroidAdapter).build();
    }

}
