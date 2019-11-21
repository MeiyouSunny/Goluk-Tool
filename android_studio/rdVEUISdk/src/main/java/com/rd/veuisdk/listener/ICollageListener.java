package com.rd.veuisdk.listener;

import android.support.v4.app.Fragment;

import com.rd.veuisdk.model.CollageInfo;

import java.util.List;

/**
 * 画中画 控制图库的接口
 *
 * @author JIAN
 * @create 2019/3/22
 * @Describe
 */
public interface ICollageListener {

    /**
     * 进入画中画
     */
    void onCollage(Fragment fragment);


    /**
     * 退出画中画
     */
    void onCollageExit(CallBack callBack);


    interface CallBack {

        void onAnimationComplete();
    }


}
