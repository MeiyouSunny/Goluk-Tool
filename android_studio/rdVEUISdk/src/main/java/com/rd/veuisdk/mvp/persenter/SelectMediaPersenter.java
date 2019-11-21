package com.rd.veuisdk.mvp.persenter;


import android.content.Context;

import com.rd.veuisdk.model.IDirInfo;
import com.rd.veuisdk.mvp.model.GalleryModel;
import com.rd.veuisdk.mvp.model.IGalleryModel;
import com.rd.veuisdk.mvp.view.ISelectMediaView;

import java.util.List;


/**
 * 一个文件夹对应一个媒体列表
 */
public class SelectMediaPersenter<V extends ISelectMediaView> extends BasePresenter<V> {
    private IGalleryModel model;

    public SelectMediaPersenter(Context context) {
        model = new GalleryModel(context);
    }

    /**
     *
     * @param isVideo
     */
    public void initData(boolean isVideo) {
        //View是否绑定 如果没有绑定，就不执行网络请求
        if (!isViewAttached()) {
            return;
        }
        mViewRef.get().showLoading();
        model.initData(false, isVideo, new IGalleryModel.ICallBack() {
            @Override
            public void onSuccess(List<IDirInfo> list) {
                if (!isViewAttached()) {
                    return;
                }
                mViewRef.get().onSuccess(list);
            }
        });

    }

    public void recycle() {
        if (!isViewAttached()) {
            return;
        }
        model.recycle();
    }


}
