package com.rd.veuisdk.model;

/**
 * 转场INFO
 * Created by JIAN on 2017/7/10.
 */

public class TransitionInfo {

    private String text;
    private int drawableId;
    private int nId;

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }


    private String iconPath;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }

    public int getId() {
        return nId;
    }

//
//    public TransitionInfo(int id, String text, int drawableId) {
//        this.nId = id;
//        this.text = text;
//        this.drawableId = drawableId;
//    }

    public TransitionInfo(int id, String text, String iconPath) {
        this.nId = id;
        this.text = text;
        this.iconPath = iconPath;
    }

}
