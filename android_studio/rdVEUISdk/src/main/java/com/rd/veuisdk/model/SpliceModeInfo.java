package com.rd.veuisdk.model;

import java.util.List;

/**
 * 拼接模板
 */
public class SpliceModeInfo {


    public SpliceModeInfo(String bg_n, String bg_p, List<GridInfo> list) {
        this.bg_n = bg_n;
        this.bg_p = bg_p;
        mGridInfoList = list;
    }

    public String getBg_n() {
        return bg_n;
    }

    public String getBg_p() {
        return bg_p;
    }

    private String bg_n, bg_p;

    public List<GridInfo> getGridInfoList() {
        return mGridInfoList;
    }

    private List<GridInfo> mGridInfoList;


}
