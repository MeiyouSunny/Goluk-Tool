package com.rd.veuisdk.mvp.model;

import android.support.annotation.NonNull;

import com.rd.veuisdk.model.IDirInfo;

import java.util.List;

/**
 * 图库-视频
 *
 * @author JIAN
 * @create 2019/3/15
 * @Describe
 */
public interface IGalleryModel {


    /**
     * @param isResultAsOneIDir true ( <-- 图库-视频时，扫描的全部磁盘视 |  图库-图片时，扫描整个DCIM下的全部文件夹中的图片   -->  并排序返回1个IDirData )
     *                          ;其余全部返回false
     * @param loadVideo         是否只扫描视频
     * @param callBack
     */
    void initData(boolean isResultAsOneIDir, boolean loadVideo, @NonNull ICallBack callBack);


    void recycle();


    interface ICallBack {
        //加载DCIM 图片资源| 全部视频资源
        void onSuccess(List<IDirInfo> list);
    }


}
