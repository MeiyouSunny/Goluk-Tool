package com.mobnote.golukmain.upgrade.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by pavkoo on 2016/7/18.
 */
public class UpgradeResultbean {
    /**
     * true：成功；false：失败
     */
    @JSONField(name = "success")
    public boolean resultFlag;
    @JSONField(name = "data")
    public UpgradeDataInfo dataInfo;
    /**
     * 内容：成功或参数错误等信息
     */
    @JSONField(name = "msg")
    public String msg;
}
