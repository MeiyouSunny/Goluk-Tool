package com.rd.veuisdk.model;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.SparseArray;

import com.rd.veuisdk.utils.CommonStyleUtils;

/**
 * 字幕特效 type==0,可以写字
 * 
 * @author JIAN
 * 
 */

public class StyleInfo {

	public int index = 0;
	public String code, caption;
	public String mlocalpath;
	public long nTime = 0;// 记录当前下载的版本
	public boolean isdownloaded = false;

	/**
	 * 缩放系数 = disf
	 */
	public float zoomFactor;
	public double left, top, right, buttom; // new add

	public int pid, type, pExtend, extendSection, lashen, onlyone, shadow;
	public float disf = 1f;
	public double x, y, w, h;
	public float rotateAngle = 0.0f;// 旋转角度
	public int fid, du;
	public int tLeft, tTop, tWidth, tHeight, tRight, tButtom;
	public String tFont;
	public double a, fx, fy, fw, fh, c;
	public String n;
	public SparseArray<StyleT> frameArry = new SparseArray<StyleT>();
	public ArrayList<TimeArray> timeArrays = new ArrayList<TimeArray>();
	private FilterInfo2 filterinfo2 = null;
	public int strokeColor = 0, strokeWidth = 0;

	public FilterInfo2 getFilterInfo2() {
		return filterinfo2;
	}

	public void setFilterInfo2(FilterInfo2 filterinfo2) {
		this.filterinfo2 = filterinfo2;
	}

	public float[] centerxy = new float[] { 0.5f, 0.5f }; // 图片旋转中心点坐标在x，y的比例

	@Override
	public String toString() {
		return "StyleInfo [code=" + code + ", caption=" + caption
				+ ", mlocalpath=" + mlocalpath + ", nTime=" + nTime
				+ ", isdownloaded=" + isdownloaded + ", pid=" + pid + ", type="
				+ type + ", x=" + x + ", y=" + y + ", pExtend=" + pExtend
				+ ", extendSection=" + extendSection + ", disf=" + disf
				+ ", w=" + w + ", h=" + h + ", rotateAngle=" + rotateAngle
				+ ", fid=" + fid + ", du=" + du + ", tLeft=" + tLeft
				+ ", tTop=" + tTop + ", tWidth=" + tWidth + ", tHeight="
				+ tHeight + ", tFont=" + tFont + ", a=" + a + ", fx=" + fx
				+ ", fy=" + fy + ", fw=" + fw + ", fh=" + fh + ", c=" + c
				+ ", n=" + n + ", frameArry=" + frameArry + ", timeArrays="
				+ timeArrays + ", filterinfo2=" + filterinfo2
				+ ", strokeColor=" + strokeColor + ", strokeWidth="
				+ strokeWidth + ", centerxy=" + Arrays.toString(centerxy)
				+ ", st=" + st + "]";
	}

	public CommonStyleUtils.STYPE st = CommonStyleUtils.STYPE.sub;

}
