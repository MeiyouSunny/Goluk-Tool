package com.mobnote.golukmain.newest.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class NewestVideoDataBean {
    /* 爱淘客id */
    @JSONField(name="aid")
    public String aid;
    /* 设备id */
    @JSONField(name="mid")
    public String mid;
    /* String */
    @JSONField(name="active")
    public String active;
    /* String */
    @JSONField(name="tag")
    public String tag;
    /* String */
    @JSONField(name="open")
    public String open;
    /* 经度 */
    @JSONField(name="lon")
    public String lon;
    /* 纬度 */
    @JSONField(name="lat")
    public String lat;
    /* 速度 */
    @JSONField(name="speed")
    public String speed;
    /* 是否开启对讲 */
    @JSONField(name="talk")
    public String talk;
    /* 是否静音 0为静音 1为有声 */
    @JSONField(name="voice")
    public String voice;
    /* 视频分类 */
    @JSONField(name="vtype")
    public String vtype;
    /* 耗时 */
    @JSONField(name="restime")
    public String restime;
    /* 耗流量 */
    @JSONField(name="flux")
    public String flux;
}
