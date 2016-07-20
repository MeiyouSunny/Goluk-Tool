package com.mobnote.golukmain.specialsetting.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class CarBrands {
    @JSONField(name = "list")
    public List<CarBrandBean> list;
}
