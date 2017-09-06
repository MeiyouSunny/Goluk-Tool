package com.rd.veuisdk.model;

import android.graphics.Point;
import android.graphics.RectF;

public class ChartProgressInfo {

    public int getProgress() {
	return nProgress;
    }

    public void setProgress(int nProgress) {
	this.nProgress = nProgress;
    }

    public Point getPoint() {
	return pPoint;
    }

    public void setPoint(Point pPoint) {
	this.pPoint = pPoint;
    }

    public RectF getRectF() {
	return mRectF;
    }

    public void setRectF(RectF mRectF) {
	this.mRectF = mRectF;
    }

    private int nProgress;
    private Point pPoint;
    private RectF mRectF;// 相对屏幕px

    public double py;

    @Override
    public String toString() {
	return "ChartProgressInfo [nProgress=" + nProgress + ", pPoint="
		+ pPoint + ", mRectF=" + mRectF.toShortString() + ", py=" + py
		+ "]";
    }
}
