package com.rd.veuisdk.model;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.models.MediaObject;

import java.util.Arrays;

/**
 * 拼接-单个小画框绑定媒体
 */
public class SpliceGridMediaInfo {


    public GridInfo getGridInfo() {
        return mGridInfo;
    }

    public void setGridInfo(GridInfo gridInfo) {
        mGridInfo = gridInfo;
    }

    private GridInfo mGridInfo;


    public MediaObject getMediaObject() {
        return mMediaObject;
    }

    private MediaObject mMediaObject;

    public String getThumbPath() {
        return thumbPath;
    }

    private String thumbPath;

    public Bitmap getThumbBmp() {
        return thumbBmp;
    }

    private Bitmap thumbBmp;


    public SpliceGridMediaInfo() {
    }

    /**
     * @param src       原始媒体
     * @param bmp       原始媒体bitmap
     * @param thumbPath 原始媒体bitmap封面
     */
    public void updateMedia(MediaObject src, Bitmap bmp, String thumbPath) {
        this.mMediaObject = src;
        this.thumbBmp = bmp;
        this.thumbPath = thumbPath;
    }

    public void recycle() {
        if (null != thumbBmp && !thumbBmp.isRecycled()) {
            thumbBmp.recycle();
        }
        thumbBmp = null;
    }



    /**
     * 要保留的部分视频 单位:像素 0~宽、0~高
     */
    private RectF clipRectF;

    public RectF getClipRectFThumb() {
        return clipRectFThumb;
    }

    private RectF clipRectFThumb;

    public RectF getClipRectF() {
        return clipRectF;
    }

    public Rect getSize() {
        return mSize;
    }

    /**
     * 媒体的宽高、缩略图的宽高
     */
    public void setSize(Rect size) {
        this.mSize = size;
    }

    /***
     *  video的真实宽高
     */
    private Rect mSize = null;


    /***
     * 要保留的区域(视频中的真实像素)
     * @param clipRectF  单位：像素  0~宽、0~高 (相对于原视频要裁剪得到的像素)
     * @param clipRectFThumb    缩略图的保留区域（与原始媒体的大小不一致720*720 范围内） 单位：像素  0~宽、0~高 (相对于原视频要裁剪得到的像素)
     */
    public void setClipRectF(RectF clipRectF,RectF clipRectFThumb) {
        this.clipRectF = clipRectF;
        this.clipRectFThumb=clipRectFThumb;
    }


    public float[] getClipValue() {
        return mClipValue;
    }

    /**
     * 裁剪时的Matrix值
     *
     * @param clipValue  矩阵信息
     */
    public void setClipValue(float[] clipValue  ) {
        mClipValue = clipValue;
    }

    //记录clip时的Matrix 信息
    private float[] mClipValue = null;

    @Override
    public String toString() {
        return "SpliceGridMediaInfo{" +
                "mGridInfo=" + mGridInfo +
                ", mMediaObject=" + mMediaObject +
                ", thumbPath='" + thumbPath + '\'' +
                ", thumbBmp=" + thumbBmp +
                ", clipRectF=" + clipRectF +
                ", mSize=" + mSize +
                ", mClipValue=" + Arrays.toString(mClipValue) +
                '}';
    }
}
