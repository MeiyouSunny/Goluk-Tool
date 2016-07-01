package com.mobnote.golukmain.internation.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by lily on 16-6-24.
 */
public class CheckVcodeBean {

    /** 请求返回码 **/
    @JSONField(name = "code")
    public int code;

    /** 请求返回数据 **/
    @JSONField(name = "data")
    public CheckVcodeData data;

    /** 返回调试信息 **/
    @JSONField(name = "msg")
    public String msg;

    /** 协议版本 **/
    @JSONField(name = "xieyi")
    public int xieyi;

}
