package com.mobnote.golukmain.videodetail;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by lily on 16-5-26.
 */
public class DeleteJson {

    @JSONField(name = "success")
    public boolean success;

    @JSONField(name = "data")
    public DeleteData data;

    @JSONField(name = "msg")
    public String msg;
}
