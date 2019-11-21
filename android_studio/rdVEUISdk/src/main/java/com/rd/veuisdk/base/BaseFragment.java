package com.rd.veuisdk.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author JIAN
 * @date 2019/2/21
 * @Description
 */
public abstract class BaseFragment extends Fragment {

    protected View mRoot;
    protected String TAG = BaseFragment.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(getLayoutId(), container, false);
        initView(mRoot);
        return mRoot;
    }

    public <T extends View> T $(int resId) {
        return mRoot.findViewById(resId);
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    public abstract void initView(View view);

    public abstract int getLayoutId();
}
