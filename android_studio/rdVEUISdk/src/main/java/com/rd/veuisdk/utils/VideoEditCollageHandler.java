package com.rd.veuisdk.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.rd.veuisdk.R;
import com.rd.veuisdk.listener.ICollageListener;

/**
 * 编辑1->画中画  (辅助)
 *
 * @author JIAN
 * @create 2019/3/22
 * @Describe
 */
public class VideoEditCollageHandler implements ICollageListener {
    private View otherFragment;
    private ViewGroup container;
    private FragmentManager mFragmentManager;
    private String TAG = "VideoEditCollageHandler";

    /**
     * @param otherContainer  配乐、字幕 等fragment所在的容器
     * @param container
     * @param fragmentManager
     */
    public VideoEditCollageHandler(View otherContainer, ViewGroup container, FragmentManager fragmentManager) {
        otherFragment = otherContainer;
        this.container = container;
        mFragmentManager = fragmentManager;
    }


    private Fragment mFragment;

    @Override
    public void onCollage(Fragment fragment) {
        this.mFragment = fragment;
        otherFragment.setVisibility(View.INVISIBLE);
        container.setVisibility(View.VISIBLE);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.mixContainer, fragment);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onCollageExit(final CallBack callBack) {
        // 禁用动画
        if (null != mFragment) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.remove(mFragment);
            ft.commitAllowingStateLoss();
        }
        container.setVisibility(View.GONE);

        otherFragment.setVisibility(View.VISIBLE);
        Animation aniSlideIn = AnimationUtils.loadAnimation(otherFragment.getContext(), R.anim.editor_preview_slide_in);
        otherFragment.startAnimation(aniSlideIn);
        callBack.onAnimationComplete();


    }


}
