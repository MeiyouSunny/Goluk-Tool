package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class TagGeneralRetBean {
    /* 请求返回码 */
    @JSONField(name="code")
    public int code;
    /* 请求返回数据 */
    @JSONField(name="data")
    public TagGeneralDataBean data;
    /* 返回调试信息 内容: 成功或参数错误等信息 */
    @JSONField(name="msg")
    public String msg;
    /* 协议版本 V1: 100; V2: 200 */
    @JSONField(name="xieyi")
    public int xieyi;
}
