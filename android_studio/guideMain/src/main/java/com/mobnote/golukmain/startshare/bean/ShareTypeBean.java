package com.mobnote.golukmain.startshare.bean;

/**
 * Created by leege100 on 16/5/17.
 */
public class ShareTypeBean {

    /** 曝光台 */
    public static final int SHARE_TYPE_BGT = 1;
    /** 美丽风景 */
    public static final int SHARE_TYPE_MLFJ = 3;
    /** 随手拍 */
    public static final int SHARE_TYPE_SSP = 4;
    /** 事故爆料 */
    public static final int SHARE_TYPE_SGBL = 5;

    private int shareType;
    public ShareTypeBean(int type){
        this.shareType = type;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }
}
