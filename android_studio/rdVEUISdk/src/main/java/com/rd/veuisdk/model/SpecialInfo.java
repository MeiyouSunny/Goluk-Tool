package com.rd.veuisdk.model;

import com.rd.vecore.models.SubtitleObject;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;

/**
 * 特效item
 *
 * @author JIAN
 */
public class SpecialInfo {

    public ArrayList<SubtitleObject> getList() {
        return list;
    }

    public SpecialInfo(SpecialInfo copy) {
        list = new ArrayList<SubtitleObject>();
        list.addAll(copy.getList());
        this.timelineFrom = copy.getTimelineFrom();
        this.timelineTo = copy.getTimelineTo();
    }

    public void setList(ArrayList<SubtitleObject> list) {
        this.list = list;
    }

    public void offTimeLine(int poff) {

        timelineFrom = timelineFrom + poff;
        timelineTo = timelineTo + poff;

        if (null != list) {
            int len = list.size();
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    SubtitleObject info = list.get(i);
                    float fpoff = Utils.ms2s(poff);
                    float nstart = info.getTimelineStart() + fpoff, nend = info
                            .getTimelineEnd() + fpoff;
                    info.setTimelineRange(nstart, nend);
                }
            }
        }
    }

    public int getTimelineFrom() {
        return timelineFrom;
    }

    public int getTimelineTo() {
        return timelineTo;
    }

    private int timelineFrom, timelineTo;

    public SpecialInfo(int timelineFrom, int timelineTo) {
        this.timelineFrom = timelineFrom;
        this.timelineTo = timelineTo;
    }

    public void setTimelineTo(int timelineTo) {
        this.timelineTo = timelineTo;
    }

    public void add(SubtitleObject sub) {
        list.add(sub);
    }

    private ArrayList<SubtitleObject> list = new ArrayList<SubtitleObject>();

    public void recycle() {
        if (null != list) {
//            int len = list.size();
//            for (int i = 0; i < len; i++) {
////				list.get(i).recycle();
//            }
            list.clear();
        }
    }

    /**
     * 场景:修正转场添加之后,视频时长变短，修正最后的特效
     *
     * @param duration
     */
    public void fixLast(int duration) {

        if (null != list) {
            SubtitleObject item;
            for (int i = 0; i < list.size(); i++) {
                item = list.get(i);
                if (item.getTimelineEnd() > duration) {
//					item.recycle();
                    list.remove(i);
                    i--;
                }
            }
            list.clear();
        }

    }

    @Override
    public String toString() {
        return "SpecialInfo [timelineFrom=" + timelineFrom + ", timelineTo="
                + timelineTo + " ]";
    }
}
