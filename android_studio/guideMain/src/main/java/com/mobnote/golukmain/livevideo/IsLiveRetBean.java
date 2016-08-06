package com.mobnote.golukmain.livevideo;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 2016/8/6.
 */
public class IsLiveRetBean {
    @JSONField (name = "code")
    public String code;
    @JSONField (name = "state")
    public boolean state;
    @JSONField (name = "msg")
    public String msg;
}
