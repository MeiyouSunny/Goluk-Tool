package com.rd.veuisdk.mvp.persenter;

import java.lang.ref.WeakReference;

/**
 * @author JIAN
 * @date 2019/2/21
 * @Description
 */
public class BasePresenter<V> {
    protected WeakReference<V> mViewRef;


    /**
     * 绑定view，一般在初始化中调用该方法
     *
     * @param view view
     */
    public void attachView(V view) {

        this.mViewRef = new WeakReference<>(view);
    }

    /**
     * 解除绑定view，一般在onDestroy中调用
     */

    public void detachView() {
        this.mViewRef.clear();
    }

    /**
     * View是否绑定
     *
     * @return
     */
    public boolean isViewAttached() {
        return mViewRef.get() != null;
    }


}
