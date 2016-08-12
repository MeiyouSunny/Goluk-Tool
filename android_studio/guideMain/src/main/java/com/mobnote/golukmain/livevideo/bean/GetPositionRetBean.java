package com.mobnote.golukmain.livevideo.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;

/**
 * Created by leege100 on 2016/8/12.
 */
public class GetPositionRetBean {
    @JSONField (name = "code")
    public String code;
    @JSONField (name = "info")
    public ArrayList<GetPositionInfoBean> info;
    @JSONField (name = "state")
    public boolean state;

}
