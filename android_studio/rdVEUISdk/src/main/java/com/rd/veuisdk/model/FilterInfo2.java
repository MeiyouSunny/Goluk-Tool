package com.rd.veuisdk.model;

import java.io.Serializable;
import java.util.Arrays;

import android.graphics.Color;

/**
 * 字幕样式信息 可编辑矩形信息
 * 
 * @author ADMIN
 * 
 */
public class FilterInfo2 implements Serializable {

    public String getHint() {
	return hint;
    }

    private void setHint(String hint) {
	this.hint = hint;
    }

    public int getTextDefaultColor() {
	return textDefaultColor;
    }

    private void setTextDefaultColor(int textDefaultColor) {
	this.textDefaultColor = textDefaultColor;
    }

    private String hint;
    private int textDefaultColor = Color.WHITE;

    public FilterInfo2(String hint, int textDefaultColor, float[] start,
	    float[] end, int filterIndex) {
	setHint(hint);
	setFilterIndex(filterIndex);
	setEnd(end);
	setStart(start);
	setTextDefaultColor(textDefaultColor);
    }

    private int filterIndex; // 字幕样式下标0--15

    public int getFilterIndex() {
	return filterIndex;
    }

    public void setFilterIndex(int filterIndex) {
	this.filterIndex = filterIndex;
    }

    @Override
    public String toString() {
	return "FilterInfo2 [hint=" + hint + ", textDefaultColor="
		+ textDefaultColor + ", filterIndex=" + filterIndex
		+ ", start=" + Arrays.toString(start) + ", end="
		+ Arrays.toString(end) + "]";
    }

    public float[] getStart() {
	return start;
    }

    private void setStart(float[] start) {
	this.start = start;
    }

    public float[] getEnd() {
	return end;
    }

    private void setEnd(float[] end) {
	this.end = end;
    }

    private float[] start = new float[] { 0.3f, 0.3f }; // 可编辑文字的矩形的左上顶点坐标对应的大小
							// values 0 -1
    private float[] end = new float[] { 0.6f, 0.6f }; // 可编辑文字的矩形的右下顶点坐标对应的大小
						      // values 0 -1

}
