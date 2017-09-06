package com.rd.veuisdk.model;

import android.graphics.Rect;

import java.util.ArrayList;

public class SplitItem {

	public int getStart() {
		return start;
	}

	public int getTlstart() {
		return tlstart;
	}

	public void setTlstart(int tlstart) {
		this.tlstart = tlstart;
	}

	public int getTlend() {
		return tlend;
	}

	public void setTlend(int tlend) {
		this.tlend = tlend;
	}

	private int tlstart, tlend; // 相对于原始视频(path)的起始 ，换算成速率为1f

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;

	}

	/**
	 * demo :从原始视频截取一个时间段的视频，调速 speed=2,再分割
	 */
	private int start, end; // 调速后的起始位置，注意截取片段 （0--duration）
	// 该片段相对于调速后的视频(videoobjet speed!=1f)的位置

	private Rect mRect;

	public Rect getRect() {
		return mRect;
	}

	public int getDuration() {
		return (end - start);
	}

	public void setRect(Rect mRect) {
		this.mRect = mRect;
	}

	@Override
	public String toString() {
		return "SplitItem [tlstart=" + tlstart + ", tlend=" + tlend
				+ ", start=" + start + ", end=" + end + ", mRect=" + mRect
				+ ", list=" + list + "]";
	}

	private ArrayList<SplitThumbItemInfo> list = new ArrayList<SplitThumbItemInfo>();

	public ArrayList<SplitThumbItemInfo> getList() {
		return list;
	}

	public void setList(ArrayList<SplitThumbItemInfo> list) {
		this.list = list;
	}

}
