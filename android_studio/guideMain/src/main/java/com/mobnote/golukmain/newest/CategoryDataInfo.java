package com.mobnote.golukmain.newest;

import org.json.JSONObject;

public class CategoryDataInfo {
    public String id;
    public String name;
    public String coverurl;
    public String time;
    // 新增字段,处理不同时区时间问题,该值为标准时区的毫秒值
    public long ts;

    public CategoryDataInfo() {

    }

    public CategoryDataInfo(JSONObject json) {
        this.id = json.optString("id");
        this.name = json.optString("name");
        this.coverurl = json.optString("coverurl");
        this.time = json.optString("time");
        this.ts = json.optLong("ts");
    }

}
