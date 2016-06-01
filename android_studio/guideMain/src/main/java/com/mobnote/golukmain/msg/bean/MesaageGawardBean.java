package com.mobnote.golukmain.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 16/5/24.
 */
public class MesaageGawardBean {

    /** 奖品名称 **/
    @JSONField(name="name")
    public String name;

    /** 获奖理由 **/
    @JSONField(name="reason")
    public String reason;

}
