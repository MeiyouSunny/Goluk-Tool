package com.rd.veuisdk.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 如果是云音乐单项-音乐进度滑动过程中，屏蔽掉viewpage的左右滑动功能，防止各种冲突
 *
 * @author JIAN
 * @create 2019/7/1
 * @Describe
 */
public class ExtViewPager extends ViewPager {

    public ExtViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setNoScroll(false);
    }

    private static boolean bNoScroll = false;

    /**
     * 是否屏蔽滑动事件
     *
     * @param noScroll true 屏蔽touch相关的事件 （触摸播放进度条时，需要屏蔽viewpage的左右滑动功能），false 保留touch事件
     */
    public static void setNoScroll(boolean noScroll) {
        bNoScroll = noScroll;
    }


    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (bNoScroll)
            return false;
        else
            return super.onTouchEvent(arg0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (bNoScroll)
            return false;
        else
            return super.onInterceptTouchEvent(arg0);
    }

}
