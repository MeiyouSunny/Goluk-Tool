package com.mobnote.eventbus;

/**
 * Created by leege100 on 16/5/17.
 */
public class EventSharetypeSelected {
    private int shareType;
    private String shareName;
    public EventSharetypeSelected(String name,int type){
        this.shareName = name;
        this.shareType = type;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public String getShareName() {
        return shareName;
    }

    public void setShareName(String shareName) {
        this.shareName = shareName;
    }
}
