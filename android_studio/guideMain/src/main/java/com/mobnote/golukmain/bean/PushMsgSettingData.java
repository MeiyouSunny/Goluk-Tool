package com.mobnote.golukmain.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class PushMsgSettingData {
    @JSONField(name = "result")
    public String result;
    @JSONField(name = "uid")
    public String uid;
    @JSONField(name = "iscomment")
    public String iscomment;
    @JSONField(name = "ispraise")
    public String ispraise;
    @JSONField(name = "isfollow")
    public String isfollow;
    @JSONField(name = "isfriend")
    public String isfriend;
}
