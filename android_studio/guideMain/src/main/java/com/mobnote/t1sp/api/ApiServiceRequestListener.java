package com.mobnote.t1sp.api;

import android.util.Log;

import java.lang.reflect.Method;

import likly.reverse.OnServiceInvokeListener;
import likly.reverse.RequestHolder;

/**
 * 网络请求发送前处理
 * 可对请求Header,参数作处理
 */
public class ApiServiceRequestListener implements OnServiceInvokeListener {

    private String TOKEN_KEY;

    @Override
    public RequestHolder onServiceInvoke(Method method, RequestHolder requestHolder) {
        Log.e("T1SP", "" + requestHolder.url());

        return requestHolder;
    }
}
