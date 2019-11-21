package com.rd.veuisdk.model;

/**
 * MV
 */
public class MVWebInfo extends IApiInfo {
    //MV未注册时的标识
    public static final int DEFAULT_MV_NO_REGISTED = -1;
    /**
     * 注册MV之后得到的id
     */
    private int id = DEFAULT_MV_NO_REGISTED;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /***
     * MV片头片尾时长，单位：秒
     */
    private int headDuration = 0, lastDuration = 0;

    public int getHeadDuration() {
        return headDuration;
    }

    public int getLastDuration() {
        return lastDuration;
    }


    public void setHeadDuration(int head) {
        this.headDuration = head;

    }

    public void setLastDuration(int last) {
        this.lastDuration = last;

    }

    public MVWebInfo(String url, String img, String name, String _localPath) {
        super(name, url, img, _localPath, 0);
    }

    /**
     * @param url
     * @param img
     * @param name
     * @param updatetime
     */
    public MVWebInfo(String url, String img, String name, long updatetime) {
        super(name, url, img, updatetime);
    }


    public MVWebInfo(int mvId, String url, String img, String name,
                     String _localPath) {
        super(name, url, img, _localPath, 0);
        setId(mvId);

    }

    /**
     * @param mvId
     * @param url
     * @param img
     * @param name
     * @param _localPath
     * @param updateTime
     */
    public MVWebInfo(int mvId, String url, String img, String name,
                     String _localPath, long updateTime) {
        super(name, url, img, _localPath, updateTime);
        setId(mvId);

    }


}
