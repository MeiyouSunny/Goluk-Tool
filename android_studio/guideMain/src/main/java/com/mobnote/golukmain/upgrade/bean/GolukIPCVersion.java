package com.mobnote.golukmain.upgrade.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by pavkoo on 2016/7/18.
 */
public class GolukIPCVersion {
    /**
     * 版本
     */
    @JSONField(name = "version")
    public String version;
    /**
     * 本地文件存储路径
     */
    @JSONField(name = "path")
    public String path;
    /**
     * 下载路径(cdn)
     */
    @JSONField(name = "url")
    public String url;
    /**
     * MD5加密
     */
    @JSONField(name = "md5")
    public String md5;
    /**
     * 文件大小(单位字节)
     */
    @JSONField(name = "filesize")
    public String fileSize;
    /**
     * 发布时间
     */
    @JSONField(name = "releasetime")
    public String releaseTime;
    /**
     * 升级描述
     */
    @JSONField(name = "appcontent")
    public String appContent;
    /**
     * 是否升级:
     * 0.不升级\\1.升级
     */
    @JSONField(name = "isnew")
    public String isNew;
    /**
     * goluk硬件型号
     */
    @JSONField(name = "hdtype")
    public String ipcType;
}
