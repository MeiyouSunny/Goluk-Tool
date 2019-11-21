package com.rd.veuisdk.ui;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 防止频繁点击 返回、下一步
 */
public class ExtImageButton extends android.support.v7.widget.AppCompatImageButton {
    private int m_nRepeatClickIntervalTime = 1000;
    private long m_lastClickTime;

    @Override
    public boolean performClick() {
        long time = System.currentTimeMillis();
        long timeD = time - m_lastClickTime;
        if (0 < timeD && timeD < m_nRepeatClickIntervalTime) {
            return false;
        }
        m_lastClickTime = time;
        return super.performClick();
    }

    public ExtImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
