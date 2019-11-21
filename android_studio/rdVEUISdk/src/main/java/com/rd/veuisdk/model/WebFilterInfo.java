package com.rd.veuisdk.model;

/**
 * 网络滤镜
 */
public class WebFilterInfo extends IApiInfo {
    //MV未注册时的标识
    public static final int DEFAULT_FILTER_NO_REGISTED = -1;
    /**
     * 注册MV之后得到的id
     */
    private int id = DEFAULT_FILTER_NO_REGISTED;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResId() {
        return resId;
    }

    private int resId;


    /**
     * @param url
     * @param img
     * @param name
     * @param _localPath
     * @param updatetime
     */
    public WebFilterInfo(String url, String img, String name, String _localPath, long updatetime) {
        super(name, url, img, _localPath,updatetime);
    }

    /**
     * 本地 acv 滤镜
     *
     * @param id
     * @param resId
     * @param name
     */
    public WebFilterInfo(int id, int resId, String name) {
        super(name, "", "", 0);
        setId(id);
        this.resId = resId;
    }

    /**
     * @param id
     * @param url
     * @param img
     * @param name
     * @param _localPath
     * @param updatetime
     */
    public WebFilterInfo(int id, String url, String img, String name, String _localPath, long updatetime) {
        super(name, url, img,_localPath, updatetime);
        this.id = id;
    }

    /**
     * @return
     */
//    public WebFilterInfo clone() {
//        return new WebFilterInfo(getId(), getUrl(), getCover(), getName(), getLocalPath(), getUpdatetime());
//
//    }
}
