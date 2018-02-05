package com.mobnote.t1sp.api;

import com.mobnote.t1sp.util.Const;

import java.lang.reflect.Method;

import cn.com.tiros.debug.GolukDebugUtils;
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
        GolukDebugUtils.e(Const.LOG_TAG, "" + requestHolder.url());

        return requestHolder;
    }
}
