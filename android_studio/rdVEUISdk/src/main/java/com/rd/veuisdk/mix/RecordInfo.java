package com.rd.veuisdk.mix;

/**
 * 记录录制信息
 * Created by JIAN on 2017/9/18.
 */

public class RecordInfo {
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private  String path;
    private  int duration;

    public RecordInfo(String path, int duration) {
        this.path = path;
        this.duration = duration;
    }
}
