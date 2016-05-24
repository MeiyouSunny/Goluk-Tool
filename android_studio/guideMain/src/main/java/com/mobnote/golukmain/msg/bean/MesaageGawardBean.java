package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 16/5/24.
 */
public class MesaageGawardBean {
    @JSONField(name="count")
    public int count;
    @JSONField(name="reason")
    public String reason;
}
