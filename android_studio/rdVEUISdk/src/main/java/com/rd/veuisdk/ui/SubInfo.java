package com.rd.veuisdk.ui;

import android.graphics.Rect;

import com.rd.veuisdk.model.WordInfo;

/**
 * 记录每一块字幕 、特效的区域
 */
public class SubInfo {
    private int id;
    private String str = "";
    private int timelinefrom = 0, timelineTo = 0;
    private Rect rect = new Rect();

    private void SubInfoData(int startP, int endP, int height, String str,
                             int id) {

        this.str = str;
        this.id = id;
        rect.set(startP, 0, endP, height);
    }

    public SubInfo(int timelinefrom, int timelineto, int id) {
        setTimeLine(timelinefrom, timelineto);
        this.id = id;
    }

    public SubInfo(WordInfo info) {
        setTimeLine((int) info.getStart(), (int) info.getEnd());
        this.id = info.getId();
    }

    public SubInfo(int startP, int endP, int height, String str, int id) {

        SubInfoData(startP, endP, height, str, id);
    }

    public SubInfo(SubInfo info, int height) {
        SubInfoData(info.getStart(), info.getEnd(), height, info.str, info.id);
    }

    public int getEnd() {
        return rect.right;
    }

    public int getStart() {
        return rect.left;
    }

    public void setStart(int startPx) {
        rect.left = startPx;
    }

    public void setEnd(int endPx) {
        rect.right = endPx;
    }


    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return "SubInfo [str=" + str + ", id=" + id + ", rect=" + rect.toShortString()
                + ", timelinefrom=" + timelinefrom + ", timelineTo="
                + timelineTo + "]";
    }

    public String getStr() {
        return str;
    }


    public int getId() {
        return id;
    }


    public Rect getRect() {
        return rect;
    }

    public void setTimeLine(int nstart, int nend) {
        this.timelinefrom = nstart;
        this.timelineTo = nend;
    }

    public int getTimelinefrom() {
        return timelinefrom;
    }

    public int getTimelineTo() {
        return timelineTo;
    }

}
