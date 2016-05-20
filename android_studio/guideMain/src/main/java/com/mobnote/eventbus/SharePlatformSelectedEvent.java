package com.mobnote.eventbus;

/**
 * Created by leege100 on 16/5/19.
 */
public class SharePlatformSelectedEvent {
    int sharePlatform;

    public SharePlatformSelectedEvent(int type){
        this.sharePlatform = type;
    }
    public int getSharePlatform() {
        return sharePlatform;
    }

    public void setSharePlatform(int sharePlatform) {
        this.sharePlatform = sharePlatform;
    }
}
