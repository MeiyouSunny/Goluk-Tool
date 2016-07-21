package com.mobnote.golukmain.livevideo.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 2016/7/21.
 */
public class LiveSignRetBean {
    @JSONField(name="code")
    public int code;
    @JSONField(name="data")
    public LiveSignDataBean data;
    @JSONField(name="msg")
    public String msg;
    @JSONField(name="xieyi")
    public int xieyi;
}
