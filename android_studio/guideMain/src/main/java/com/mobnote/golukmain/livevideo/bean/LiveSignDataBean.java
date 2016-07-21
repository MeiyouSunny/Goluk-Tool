package com.mobnote.golukmain.livevideo.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 2016/7/21.
 */
public class LiveSignDataBean {
    @JSONField(name="envsync")
    public String envsync;
    @JSONField(name="liveurl")
    public String liveurl;
    @JSONField(name="livesign")
    public String livesign;
    @JSONField(name="signtime")
    public String signtime;
    @JSONField(name="videoid")
    public String videoid;
}
