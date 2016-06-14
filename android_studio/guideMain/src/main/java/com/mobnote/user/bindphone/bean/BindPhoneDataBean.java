package com.mobnote.user.bindphone.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by crack on 2016/6/14.
 */
public class BindPhoneDataBean {
    /** 结果代码 0:成功; 1:参数错误; 2:未知异常; 3:验证码错误; 4:验证码超时 */
    @JSONField(name="result")
    public String result;
    /** 用户id */
    @JSONField(name="uid")
    public String uid;
    /** 手机号 */
    @JSONField(name="phone")
    public String phone;
}
