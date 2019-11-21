package com.rd.veuisdk.mvp.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.rd.veuisdk.model.TransitionInfo;

import java.util.List;

/**
 * @author JIAN
 * @create 2019/3/15
 * @Describe
 */
public interface ITransitionModel {

    /**
     *
     * @param typeUrl  分类数据接口
     * @param url   查询单分类下的数据接口
     */
    void initData(String typeUrl,String url, @NonNull ICallBack callBack);




    boolean isWebTansition();


    void downTransition(Context context, int itemId, TransitionInfo info, @NonNull IDownCallBack iDownCallBack);



    /**
     * 取消下载
     */
    void onCancel();

    interface ICallBack<E> {



        /**
         * 整体转场数据
         */
        void onSuccess(List<E> list);
    }

    interface IDownCallBack {
        //下载失败
        void downFailed(int itemId, int strId);

        //下载成功
        void downSuccessed(int itemId, TransitionInfo info);
    }
}
