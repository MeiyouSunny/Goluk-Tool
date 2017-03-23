package com.mobnote.util;

import com.mobnote.golukmain.carrecorder.util.ImageManager;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;

public class GolukAnimal {
    private int[] mResArray = null;
    private Bitmap[] mBitmapArray = null;
    private AnimationDrawable mAnimation = null;
    private int mDuration;

    public GolukAnimal(int[] resArray) {
        this(resArray,500);
    }

    public GolukAnimal(int[] resArray,int duration) {
        this.mDuration = duration;
        mResArray = resArray;
        initBitmap();
    }

    private void initBitmap() {
        if (null == mResArray) {
            return;
        }
        final int size = mResArray.length;
        mBitmapArray = new Bitmap[size];
        mAnimation = new AnimationDrawable();
        for (int i = 0; i < size; i++) {
            mBitmapArray[i] = ImageManager.getBitmapFromResource(mResArray[i]);
            mAnimation.addFrame(new BitmapDrawable(mBitmapArray[i]), mDuration);
            mAnimation.setOneShot(false);
        }
    }


    public AnimationDrawable getAnimationDrawable() {
        return mAnimation;
    }

    public void start() {
        if (mAnimation != null) {
            if (!mAnimation.isRunning()) {
                mAnimation.start();
            }
        }
    }

    public void stopAnimal() {
        if (null != mAnimation) {
            mAnimation.stop();
        }
    }

    public void free() {
        if (null != mAnimation) {
            mAnimation.stop();
            mAnimation = null;
        }
        final int size = mBitmapArray.length;
        for (int i = 0; i < size; i++) {
            if (mBitmapArray[i] != null) {
                if (!mBitmapArray[i].isRecycled()) {
                    mBitmapArray[i].recycle();
                    mBitmapArray[i] = null;
                }
            }
        }
    }

}
