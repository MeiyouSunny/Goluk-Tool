package com.rd.veuisdk.model;

import android.graphics.Rect;

import com.rd.vecore.models.EffectInfo;
import com.rd.vecore.models.EffectType;

/**
 * 特效类
 *
 * @author scott
 */
public class FilterEffectItem {
    private EffectType type;
    private int color;
    private float startTime;
    private float endTime;
    private Rect specialRect = new Rect();
    private int filterId = EffectInfo.Unknown;

    public void setColor(int color) {
        this.color = color;
    }

    public int getFilterId() {
        return filterId;
    }


    @Override
    public String toString() {
        return "FilterEffectItem{" +
                "type=" + type +
                ", color=" + color +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", specialRect=" + specialRect +
                ", filterId=" + filterId +
                '}';
    }


    /**
     * 滤镜特效
     *
     * @param filterId
     * @param startTime
     * @param endTime
     * @param color
     */
    public FilterEffectItem(int filterId, float startTime, float endTime, int color) {
        this.type = EffectType.NONE;
        this.filterId = filterId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
    }


    public int getColor() {
        return color;
    }

    public EffectType getType() {
        return type;
    }

    public void setSpecialRect(int left, int top, int right, int bottom) {
        specialRect.set(left, top, right, bottom);
    }

    public Rect getSpecialRect() {
        return specialRect;
    }

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public void setEndTime(float endTime) {
        this.endTime = endTime;
    }
}
