package com.rd.veuisdk.model;

import android.graphics.Point;

/**
 * 分割时highLight
 * 
 * @author JIAN
 * @date 2016-11-25 上午11:26:35
 */
public class PointHighLight {

	private int nTime;
	private Point nPoint;

	public int getTime() {
		return nTime;
	}

	public void setTime(int nTime) {
		this.nTime = nTime;
	}

	public Point getPoint() {
		return nPoint;
	}

	public void setPoint(Point nPoint) {
		this.nPoint = nPoint;
	}

	public PointHighLight(int nTime, Point nPoint) {
		this.nTime = nTime;
		this.nPoint = nPoint;
	}

}
