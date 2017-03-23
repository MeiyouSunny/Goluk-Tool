package com.mobnote.golukmain.upgrade.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by pavkoo on 2016/7/18.
 */
public class UpgradeDataInfo {
    public static final String RESULT_TYPE_SUCCESS = "0";
    /**
     * 参数错误
     */
    public static final String RESULT_TYPE_PARAERROR = "1";
    /**
     * 未知异常
     */
    public static final String RESULT_TYPE_UNKNOW_ERROR = "2";

    @JSONField(name = "result")
    public String result;
    @JSONField(name = "goluk")
    public GolukAppVersion appVersion;
    @JSONField(name = "ipc")
    public List<GolukIPCVersion> ipcVersionList;
}
