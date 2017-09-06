package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

/***
 * 颜色选择器
 */
public class ExtColorPicker extends ColorPicker {

    public ExtColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs );

        colorArr[colorArr.length - 1] = Color.parseColor("#000000");
        mChangleLastStoke = true;
    }

}
