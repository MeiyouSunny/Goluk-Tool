package com.rd.veuisdk.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 统计fragment的基类
 *
 * @author JIAN
 */
public class BaseFragment extends Fragment {

    protected String mPageName = "baseFragment";
    protected String TAG = BaseFragment.this.toString();
    protected View mRoot;
    protected Context mContext;
    protected boolean isRunning = false; //判断当前界面是否在栈顶，防止异步更新UI

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        isRunning = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        isRunning = true;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    public void onResume() {
        isRunning = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        isRunning = false;
        super.onDestroyView();

    }

    @Override
    public void onStop() {
        isRunning = false;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();

    }

    /**
     * 查找fragment内的组件
     *
     * @param id
     * @return
     */
    public View findViewById(int id) {
        return mRoot.findViewById(id);
    }


    /**
     * 返回
     */
    public int onBackPressed() {
        return 0;
    }

}
