package com.mobnote.user;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by hanzheng on 2016/6/29.
 */
public class UserRegistBean {

    /** 请求返回码  */
    @JSONField(name="code")
    public String code;

    /** 请求是否成功 true: 成功; false: 失败 */
    @JSONField(name="state")
    public String state;

    /** 请求类型  */
    @JSONField(name="type")
    public String type;

    /** 返回调试信息  */
    @JSONField(name="msg")
    public String msg;
}
