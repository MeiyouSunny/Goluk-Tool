package com.rd.veuisdk.mvp.model;

/**
 * 文字、贴纸
 */
public interface ISSCallBack<E> extends ICallBack<E> {
    /**
     * 第一次图片下载成功
     */
    @Deprecated
    void onIconSuccess();
}
