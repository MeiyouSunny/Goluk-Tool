package com.mobnote.golukmain.thirdshare.bean;

/**
 * Created by leege100 on 16/5/13.
 */
public class SharePlatformBean {
    public static final int SHARE_PLATFORM_NULL = -1;
    public static final int SHARE_PLATFORM_QQ = 1;
    public static final int SHARE_PLATFORM_QQ_ZONE = 2;
    public static final int SHARE_PLATFORM_WEXIN = 3;
    public static final int SHARE_PLATFORM_WEXIN_CIRCLE = 4;
    public static final int SHARE_PLATFORM_WEIBO_SINA = 5;
    public static final int SHARE_PLATFORM_FACEBOOK = 101;
    public static final int SHARE_PLATFORM_LINE = 102;
    public static final int SHARE_PLATFORM_WHATSAPP = 103;
    public static final int SHARE_PLATFORM_TWITTER = 104;
    public static final int SHARE_PLATFORM_INSTAGRAM = 105;
    public static final int SHARE_PLATFORM_COPYLINK = 201;//拷贝链接

    private int platformType;
    public SharePlatformBean(int type){
        this.platformType = type;
    }

    public int getPlatformType() {
        return platformType;
    }

    public void setPlatformType(int platformType) {
        this.platformType = platformType;
    }
}
