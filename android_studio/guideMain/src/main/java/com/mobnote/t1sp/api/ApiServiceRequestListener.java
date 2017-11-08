package com.mobnote.t1sp.api;

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
        System.out.print("");
//
//        TOKEN_KEY = AppConfig.isDriverClient() ? "dtoken" : "ctoken";
//
//        //重新设置url
//        requestHolder.baseUrl(host);
//
//        // 添加User-Agent
//        requestHolder.headers().put("User-Agent", AppDataUtil.getUserAgent());
//
//        String JSESSIONID = SettingUtil.getJsessionid();
//        if (!TextUtils.isEmpty(JSESSIONID)) {
//            requestHolder.headers().put("Cookie", JSESSIONID);
//        }
//
//        if (method.getName().endsWith("Upload")) {
//            //图片上传接口，不需要处理
//            Object body = requestHolder.body();
//            String token = SettingUtil.getToken();
//            if (!TextUtils.isEmpty(token)) {
//
//                ((Map) body).put(TOKEN_KEY, token);
//            }
//            $.debug().e("URL: " + requestHolder.url());
//            $.debug().e("请求：" + body.toString());
//            return requestHolder;
//        }
//
//        Object body = requestHolder.body();
//
//        if (body == null) {
//            body = new LinkedHashMap<>();
//            requestHolder.body(body);
//        }
//
//        if (body instanceof Map) {
//            Map<String, Object> paramMap = (Map<String, Object>) body;
//            Object params = paramMap.get("params");
//            String json = null;
//            if (params != null) {
//                json = $.json().toJson(params);
//            } else {
//                json = $.json().toJson(body);
//            }
//            $.debug().e("URL: " + requestHolder.url());
//            $.debug().e("REQUEST:" + json);
//            json = DESUtil.encode(json, AppConfig.DES_KEY);
//            $.debug().e("params=" + json);
//            String token = SettingUtil.getToken();
//            Map<String, String> args = new LinkedHashMap<>();
//            args.put("params", json);
//            if (!TextUtils.isEmpty(token)) {
//                args.put(TOKEN_KEY, token);
//            }
//            $.debug().e("请求：" + args.toString());
//            $.debug().e("REQUEST:" + $.json().toJson(args));
//            requestHolder.body(args);
//        } else {
//            $.debug().e("URL: " + requestHolder.url());
//            $.debug().e("请求====：" + body.toString());
//            $.debug().e("REQUEST:====" + $.json().toJson(body));
//        }

        return requestHolder;
    }
}
