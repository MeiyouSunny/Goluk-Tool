package com.rd.veuisdk.mvp.persenter;


import android.content.Context;

import com.rd.veuisdk.model.IDirInfo;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.mvp.model.GalleryModel;
import com.rd.veuisdk.mvp.model.IGalleryModel;
import com.rd.veuisdk.mvp.view.IGalleryView;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class GalleryPersenter<V extends IGalleryView> extends BasePresenter<V> {
    private IGalleryModel model;

    public GalleryPersenter(Context context) {
        model = new GalleryModel(context);
    }
    /**
     *
     * @param isVideo
     */
    public void initData(  boolean isVideo) {
        //View是否绑定 如果没有绑定，就不执行网络请求
        if (!isViewAttached()) {
            return;
        }
        mViewRef.get().showLoading();
        model.initData(true,isVideo, new IGalleryModel.ICallBack() {
            @Override
            public void onSuccess(List<IDirInfo> list) {
                List<ImageItem> items = new ArrayList<>();
                int len = list.size();
                for (int i = 0; i < len; i++) {
                    items.addAll(list.get(i).getList());
                }
                if (!isViewAttached()) {
                    return;
                }
                mViewRef.get().onSuccess(items);
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
