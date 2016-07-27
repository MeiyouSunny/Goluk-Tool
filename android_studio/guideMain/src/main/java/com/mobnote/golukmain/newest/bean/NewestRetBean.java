package com.mobnote.golukmain.newest.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class NewestRetBean {
    /* 请求是否成功 true: 成功; false: 失败 */
    @JSONField(name="success")
    public boolean success;
    /* 请求返回数据 */
    @JSONField(name="data")
    public NewestDataBean data;
    /* 返回调试信息 内容: 成功或参数错误等信息 */
    @JSONField(name="msg")
    public String msg;
}
