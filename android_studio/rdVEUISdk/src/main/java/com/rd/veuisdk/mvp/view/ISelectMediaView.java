package com.rd.veuisdk.mvp.view;

import com.rd.veuisdk.base.BaseView;
import com.rd.veuisdk.model.IDirInfo;

import java.util.List;

public interface ISelectMediaView extends BaseView {

    //加载数据成功
    void onSuccess(List<IDirInfo> list);
}
