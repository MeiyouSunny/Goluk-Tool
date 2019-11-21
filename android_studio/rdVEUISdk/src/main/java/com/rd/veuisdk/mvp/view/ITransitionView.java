package com.rd.veuisdk.mvp.view;

import com.rd.veuisdk.base.BaseView;
import com.rd.veuisdk.model.TransitionInfo;

import java.util.List;

/**
 *
 */
public interface ITransitionView<E> extends BaseView {
    //下载失败
    void downFailed(int itemId, int strId);

    //下载成功
    void downSuccessed(int itemId, TransitionInfo info);

    //加载数据成功
    void onSuccess(List<E> list);

}
