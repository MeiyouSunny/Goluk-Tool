package com.rd.veuisdk.base;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.rd.veuisdk.mvp.persenter.BasePresenter;

/**
 * @author JIAN
 * @date 2019/2/21
 * @Description
 */
public abstract class BaseMvpActivity<T extends BasePresenter> extends BaseActivity {

    protected String TAG = BaseActivity.class.getName();
    protected T mPresenter;

    /**
     * 初始化视图
     */
    public abstract T initPersenter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = initPersenter();
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }
}
