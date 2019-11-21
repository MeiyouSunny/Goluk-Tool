package com.rd.veuisdk.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rd.veuisdk.mvp.persenter.BasePresenter;

/**
 * mvp->fragment的基类
 *
 * @param <T>
 */
public abstract class BaseMvpFragment<T extends BasePresenter> extends BaseFragment {

    protected T mPresenter;


    /**
     * 初始化视图
     */
    public abstract T initPersenter();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mPresenter = initPersenter();
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        return mRoot;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }
}
