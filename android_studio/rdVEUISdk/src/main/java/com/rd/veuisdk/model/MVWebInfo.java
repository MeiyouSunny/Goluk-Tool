package com.rd.veuisdk.model;

/**
 * MV
 * Created by JIAN on 2017/7/12.
 */

public class MVWebInfo extends WebInfo {
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
        super(url, img, name, _localPath);
    }

    public MVWebInfo(int mvId, String url, String img, String name,
                     String _localPath) {
        super(url, img, name, _localPath);
        setId(mvId);

    }


}
