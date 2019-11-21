package com.rd.veuisdk.demo.zishuo;


import com.rd.veuisdk.demo.zishuo.drawtext.CustomHandler;

/**
 * 字说风格 旋转、横排、竖排
 */
public class ZishuoStyle {

    private String cover;
    private CustomHandler mHandler;


    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public CustomHandler getHandler() {
        return mHandler;
    }

    public void setHandler(CustomHandler handler) {
        mHandler = handler;
    }

}
