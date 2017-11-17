package com.mobnote.t1sp.api;

import java.util.Map;

import likly.reverse.Call;
import likly.reverse.annotation.BaseUrl;
import likly.reverse.annotation.CallExecuteListener;
import likly.reverse.annotation.GET;
import likly.reverse.annotation.QueryMap;
import likly.reverse.annotation.ServiceInvokeListener;

/**
 * 小白API管理类
 */
@BaseUrl(ApiServiceAit.BASE_URL)
@ServiceInvokeListener(ApiServiceRequestListener.class)
@CallExecuteListener(ApiServiceResponseListener.class)
@SuppressWarnings("all")
public interface ApiServiceAit {

    /* API base url */
    String BASE_URL = "http://192.72.1.1/cgi-bin";

    /**
     * 通用接口地址
     * 请求类型通过Get参数区别
     *
     * @param params   action, property, value ...
     * @param callback 回调接口
     * @return
     */
    @GET("/Config.cgi")
    Call<String> sendRequest(@QueryMap Map<String, String> params, Callback<String> callback);

    /**
     * 升级固件
     */
    @GET("/FWupload.cgi?action=flash")
    Call<String> updateFirmware(Callback<String> callback);

    /**
     * 重启网络
     *
     * @param callback 回调接口
     * @return
     */
    @GET("/Config.cgi?action=set&property=Net.Dev.1.Type&value=AP&property=Net&value=reset")
    Call<String> resetNet(Callback<String> callback);

}
