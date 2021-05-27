package com.rd.veuisdk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import androidx.fragment.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.rd.vecore.utils.MiscUtils;
import com.rd.veuisdk.ui.PopViewUtil;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

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

    /**
     * 秒->毫秒
     */
    protected int s2ms(float ns) {
        return MiscUtils.s2ms(ns);
    }

    /**
     * 毫秒->秒
     */
    protected float ms2s(int ms) {
        return MiscUtils.ms2s(ms);
    }

    /**
     * 视频时长
     *
     * @param ms
     * @return
     */
    public final String getTime(int ms) {
        return DateTimeUtils.stringForMillisecondTime(ms, false, true);
    }

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

    protected LinearLayoutManager createLinearLayoutManager(int orientation) {
        return new LinearLayoutManager(getContext(), orientation, false);
    }

    protected GridLayoutManager createGridLayoutManager(int spanCount) {
        return new GridLayoutManager(getContext(), spanCount);
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
        isRunning = false;
    }

    @Override
    public void onDestroyView() {
        isRunning = false;
        PopViewUtil.cancelPopWind();
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
     * @param resId
     * @return
     */
    @Deprecated
    public View findViewById(int resId) {
        return $(resId);
    }

    /**
     * 查找fragment内的组件
     *
     * @param resId
     * @param <T>
     * @return
     */
    public <T extends View> T $(int resId) {
        return mRoot.findViewById(resId);
    }

    /**
     * 设置View是否显示
     *
     * @param nViewId
     * @param bVisiable
     */
    public void setViewVisibility(int nViewId, boolean bVisiable) {
        setViewVisibility(nViewId, bVisiable ? View.VISIBLE : View.GONE, 0);
    }

    /**
     * 设置View是否显示
     *
     * @param nViewId    View id
     * @param visibility 是否显示
     */
    protected void setViewVisibility(int nViewId, int visibility, int nAnimationResId) {
        View v = $(nViewId);
        if (null != v) {
            v.clearAnimation();
            if (nAnimationResId > 0 && v.getVisibility() != visibility) {
                v.setAnimation(AnimationUtils.loadAnimation(getContext(), nAnimationResId));
            }
            v.setVisibility(visibility);
        }
    }

    protected void onToast(@StringRes int msgId) {
        SysAlertDialog.showAutoHideDialog(getContext(), 0, msgId, Toast.LENGTH_SHORT);
    }

    protected void onToast(String msg) {
        SysAlertDialog.showAutoHideDialog(getContext(), "", msg, Toast.LENGTH_SHORT);
    }

    /**
     * 返回
     */
    public int onBackPressed() {
        return 0;
    }

    public void recycle() {

    }
}
