package com.rd.veuisdk.utils.apng.assist;


import com.rd.veuisdk.utils.apng.ApngDrawable;

public abstract class ApngListener {

    public void onAnimationStart(ApngDrawable apngDrawable) {}

    public void onAnimationRepeat(ApngDrawable apngDrawable) {}

    public void onAnimationEnd(ApngDrawable apngDrawable) {}

}
