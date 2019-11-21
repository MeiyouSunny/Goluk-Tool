package com.rd.veuisdk.model;

import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;

import java.util.List;

/**
 * 拼接->每一个小区域的位置
 */
public class GridInfo {

    /**
     * 相对于父容器的0~1.0f;
     */
    private RectF mRectF;

    /**
     * 如果是异形就有黑白图
     */
    private String mGrayPath;
    //UI遮罩图
    private String mTransPath;
    //是否是异形
    private boolean isAlien = false;


    /**
     * 有效的顶点位置
     * @param pointFList 0~1.0f
     */
    public void setPointFList(List<PointF> pointFList) {
        if (null != pointFList && pointFList.size() >= 3) {
            mPointFList = pointFList;
        }
    }

    public List<PointF> getPointFList() {
        return mPointFList;
    }

    //几个顶点位置，相对于当前容器
    private List<PointF> mPointFList;


    /**
     * 单个小画框
     *
     * @param rectF     默认显示位置 0~1.0f
     * @param grayPath  黑白图 （播放器使用）
     * @param transPath 颜色混淆图（UI使用）
     */
    public GridInfo(RectF rectF, String grayPath, String transPath) {
        mRectF = rectF;
        mGrayPath = grayPath;
        mTransPath = transPath;
        isAlien = !TextUtils.isEmpty(mGrayPath);
    }

    public RectF getRectF() {
        return mRectF;
    }

    public String getGrayPath() {
        return mGrayPath;
    }


    public String getTransPath() {
        return mTransPath;
    }

    /**
     * 是否是异形
     */
    public boolean isAlien() {
        return isAlien;
    }

    @Override
    public String toString() {
        return "GridInfo{" +
                "isAlien=" + isAlien +
                ", mRectF=" + mRectF +
                ", mGrayPath='" + mGrayPath + '\'' +
                ", mTransPath='" + mTransPath + '\'' +
                '}';
    }
}
