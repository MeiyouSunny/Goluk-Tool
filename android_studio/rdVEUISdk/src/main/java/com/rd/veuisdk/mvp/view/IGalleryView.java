package com.rd.veuisdk.mvp.view;

import com.rd.veuisdk.base.BaseView;
import com.rd.veuisdk.model.ImageItem;

import java.util.List;

/**
 *  选中视频
 */
public interface IGalleryView extends BaseView {


    //加载数据成功
    void onSuccess(List<ImageItem> list);
}
