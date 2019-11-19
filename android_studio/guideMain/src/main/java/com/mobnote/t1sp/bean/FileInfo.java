package com.mobnote.t1sp.bean;

import com.mobnote.t1sp.util.Const;

/**
 * 记录仪文件信息
 */
public class FileInfo {

    public String path;
    public String name;
    public String format;
    public String resolution;
    public String fps;
    public int videoTime;
    public String size;
    public String attr;
    public String time;

    /**
     * 获取网络完整URL
     */
    public String getUrl() {
        return Const.HTTP_SCHEMA + Const.IP + path;
    }

}
