package com.mobnote.golukmain.newest.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class NewestLabelBean {
    /* 蓝V认证标识 是否认证0.否 1.是 */
    @JSONField(name="approvelabel")
    public String approvelabel;
    /* 蓝V认证 认证类型 如果approve为空的话取 */
    @JSONField(name="approve")
    public String approve;
    /* 达人 是否达人0. 否 1. 是 */
    @JSONField(name="tarento")
    public String tarento;
    /* 黄V认证标识 是否加v 0.否 1.是 */
    @JSONField(name="headplusv")
    public String headplusv;
    /* 黄V认证 认证类型 如果headplusvdes为空的话取 */
    @JSONField(name="headplusvdes")
    public String headplusvdes;
}
