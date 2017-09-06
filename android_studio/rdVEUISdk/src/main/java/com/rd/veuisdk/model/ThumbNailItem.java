package com.rd.veuisdk.model;

import android.graphics.Bitmap;

/**
 * 分割时每张缩略图的信息
 * 
 * @author ADMIN
 * 
 */
public class ThumbNailItem {

    public int getPaddleft() {
	return paddleft;
    }

    public void setPaddleft(int paddleft) {
	this.paddleft = paddleft;
    }

    public long getTime() {
	return time;
    }

    public void setTime(long time) {
	this.time = time;
    }

    private int paddleft; // 每张缩略图距离最左边的位置(不包含paddLeft())
    private long time; // 每张缩略图对应的中间时刻
    private Bitmap bitmap;

    public Bitmap getBitmap() {
	return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
	this.bitmap = bitmap;
    }

    /**
     * 释放
     */
    public void recycle() {
	if (null != bitmap && !bitmap.isRecycled()) {
	    bitmap.recycle();
	}
    }
}
