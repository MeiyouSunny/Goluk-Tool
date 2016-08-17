package com.goluk.crazy.panda.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.goluk.crazy.panda.R;

/**
 * Created by DELL-PC on 2016/8/16.
 */
public class Test {
    private String mTest = "";
    public void setTest(String test) {
        mTest = test;
    }

    public String getTest() {
        return mTest;
    }

    public void showImage(Context context, ImageView imageView) {
        Glide.with(context).load("http://pic.goluk.cn/cdcavatar/defaultavatar.png").placeholder(android.R.drawable.btn_dialog).into(imageView);
    }
}
