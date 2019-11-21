package com.rd.veuisdk.model;

import java.io.Serializable;

/**
 * 配乐->我的音乐
 * 
 * @author ADMIN
 * 
 */
public class MyMusicInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public WebMusicInfo getmInfo() {
	return mInfo;
    }

    public void setmInfo(WebMusicInfo mInfo) {
	this.mInfo = mInfo;
    }

    private int type = 0; // 0已下载，1自带音乐(内置音乐),2本地音乐

    private WebMusicInfo mInfo;


    public void setDowntimes(String downtimes) {
	this.downtimes = downtimes;
    }

    private String downtimes;

    @Override
    public String toString() {
	return "MyMusicInfo [type=" + type + ", mInfo=" + mInfo.toString()
		+ ", downtimes=" + downtimes + "]";
    }

}
