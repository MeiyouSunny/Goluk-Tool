package com.rd.veuisdk.mvp.model;

import android.support.annotation.MainThread;

import java.util.List;

/**
 * model数据回调
 *
 * @create 2019/6/19
 */
public interface ICallBack<E> {

    /**
     * 请求网络成功，返回的数据
     *
     * @param list
     */
    @MainThread
    void onSuccess(List<E> list);

    /**
     * 请求失败
     */
    @MainThread
    void onFailed();
}
