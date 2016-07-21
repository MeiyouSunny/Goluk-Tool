package com.mobnote.golukmain.watermark.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by pavkoo on 2016/7/20.
 */
public class CarBrandsResultBean {
    @JSONField(name = "code")
    public int code;
    @JSONField(name = "msg")
    public String msg;
    @JSONField(name = "data")
    public CarBrands carBrands;
    @JSONField(name = "xieyi")
    public int protocalType;
}
