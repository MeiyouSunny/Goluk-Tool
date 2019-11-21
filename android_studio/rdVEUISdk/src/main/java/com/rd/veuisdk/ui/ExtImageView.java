package com.rd.veuisdk.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;


/**
 * 字幕新增、编辑，防止重复点击
 */
public class ExtImageView extends android.support.v7.widget.AppCompatImageView {


    public ExtImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    private int mRepeatClickIntervalTime = 1200;
    private long mLastClickTime;

    @Override
    public boolean performClick() {
        long time = System.currentTimeMillis();
        long timeD = time - mLastClickTime;
        if (0 < timeD && timeD < mRepeatClickIntervalTime) {
            return false;
        }
        mLastClickTime = time;
        return super.performClick();
    }

    public int getRepeatClickIntervalTime() {
        return mRepeatClickIntervalTime;
    }

    public ExtImageView setRepeatClickIntervalTime(int repeatClickIntervalTime) {
        mRepeatClickIntervalTime = repeatClickIntervalTime;
        return this;
    }
}
