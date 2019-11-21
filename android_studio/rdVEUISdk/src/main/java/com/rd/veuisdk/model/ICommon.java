package com.rd.veuisdk.model;

import android.os.Parcelable;

import com.rd.veuisdk.net.SubUtils;

/**
 * 字幕、贴纸、马赛克、水印 基础类
 */
public abstract class ICommon implements Parcelable {
    public int id = -1;
    //绑定的样式
    protected int styleId = SubUtils.DEFAULT_ID;
    protected boolean changed = false;


    public void setStyleId(int _styleId) {
        styleId = _styleId;
        setChanged();
    }

    public int getStyleId() {
        return styleId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setChanged() {
        changed = true;
    }

    public boolean IsChanged() {
        return changed;
    }

    public void resetChanged() {
        changed = false;
    }


    public abstract long getStart();

    public abstract void setStart(long start);

    public abstract long getEnd();

    public abstract void setEnd(long end);

    /**
     * 设置时间线 (单位:毫秒)
     *
     * @param start 开始
     * @param end   结束
     */
    public abstract void setTimelineRange(long start, long end);

}
