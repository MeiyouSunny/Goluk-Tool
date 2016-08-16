package com.mobnote.golukmain.livevideo.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 2016/8/12.
 */
public class GetPositionInfoBean {
    @JSONField (name = "uid")
    public String uid;
    @JSONField (name = "aid")
    public String aid;
    @JSONField (name = "mid")
    public String mid;
    @JSONField (name = "active")
    public String active;
    @JSONField (name = "tag")
    public String tag;
    @JSONField (name = "open")
    public String open;
    @JSONField (name = "lon")
    public String lon;
    @JSONField (name = "lat")
    public String lat;
    @JSONField (name = "speed")
    public int speed;
    @JSONField (name = "desc")
    public String desc;
    @JSONField (name = "talk")
    public String talk;
    @JSONField (name = "voice")
    public String voice;
    @JSONField (name = "vtype")
    public String vtype;
    @JSONField (name = "restime")
    public String restime;
    @JSONField (name = "flux")
    public String flux;
    @JSONField (name = "vid")
    public String vid;
    @JSONField (name = "zan")
    public int zan;
    @JSONField (name = "shows")
    public int shows;
    @JSONField (name = "persons")
    public String persons;
    @JSONField (name = "picurl")
    public String picurl;
    @JSONField (name = "nickname")
    public String nickname;
    @JSONField (name = "sex")
    public String sex;
    @JSONField (name = "head")
    public String head;
}
