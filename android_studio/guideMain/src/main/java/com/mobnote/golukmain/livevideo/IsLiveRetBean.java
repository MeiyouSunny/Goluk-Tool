package com.mobnote.golukmain.livevideo;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 2016/8/6.
 */
public class IsLiveRetBean {
    @JSONField (name = "code")
    String code;
    @JSONField (name = "state")
    boolean state;
    @JSONField (name = "msg")
    String msg;
}
