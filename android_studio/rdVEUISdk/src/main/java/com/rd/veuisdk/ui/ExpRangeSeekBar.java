package com.rd.veuisdk.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.rd.veuisdk.R;

/**
 * 云音乐的截取播放
 */
public class ExpRangeSeekBar extends RangeSeekBar {

    public ExpRangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setDuration(10000);
        super.initdata(0, 10000, context);
        progressBetweenColor = getResources().getColor(R.color.main_orange);
        progressNormalCcolor = getResources().getColor(R.color.progress_n);
        progressProgressColor = getResources().getColor(R.color.white);
        progresTextColor = getResources().getColor(R.color.borderline_color);
        initColor();
    }

}
