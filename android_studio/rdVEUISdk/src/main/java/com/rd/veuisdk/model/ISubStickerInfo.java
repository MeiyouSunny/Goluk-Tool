package com.rd.veuisdk.model;

import android.graphics.Color;

/**
 * 定义字幕和特效公共参数
 */
public abstract class ISubStickerInfo extends ICommon {


    public static final int DEFAULT_WIDTH = 100;// 字幕特效默认100px宽高
    protected int width = DEFAULT_WIDTH, height = DEFAULT_WIDTH;

    /**
     * start,end 字幕的起止时间，毫秒 x,y; 表示字幕相对于视频的位置 0<x<1,0<y<1;
     * picpath 生成的字幕图片的位置sdcard/17rd/temp/word_***.png
     */
    @Deprecated
    protected double widthx = 0.5;
    @Deprecated
    protected double heighty = 0.5; // 0 <widthx ,heighty <1
    // //宽高占当前预览区域宽高的比列
    protected double left = 0.2, top = 0.5;// (0<=left,top<1)控件距离字幕区域范围(left ,top


    public double getLeft() {
        return left;
    }

    public void setLeft(Double left) {
        this.left = left;
    }

    public double getTop() {
        return top;
    }

    public void setTop(Double top) {
        this.top = top;
    }

    abstract int getShadowColor();

    abstract void setShadowColor(int shadowColor);


    public float[] getCenterxy() {
        return centerxy;
    }

    public void setCenterxy(float[] centerxy) {
        this.centerxy = centerxy;
        setChanged();
    }

    /**
     * 主题片头
     *
     * @param offTime 偏移时间量 可为正负 , +向后新增主题(主题时间变长),-向前清除主题(主题片头时间变短)
     */
    public void offTimeLine(int offTime) {
        setTimelineRange(getStart() + offTime, getEnd() + offTime);
    }

    protected float[] centerxy = new float[]{0.5f, 0.5f}; // 图片旋转中心点坐标在x，y的比例


    abstract void setRotateAngle(float rotateAngle);


    abstract String getText();

    //字幕特效各个样式默认的文本
    abstract void setText(String text);

    protected String mInputText;

    public String getInputText() {
        return mInputText;
    }

    public void setInputText(String inputText) {
        mInputText = inputText;
        setText(inputText);
    }

    public int getInputTextColor() {
        return mInputTextColor;
    }

    /***
     * 用户手动触发的改变颜色字体颜色
     * @param inputTextColor
     */
    public void setInputTextColor(int inputTextColor) {
        mInputTextColor = inputTextColor;
        setTextColor(mInputTextColor);
    }

    protected int mInputTextColor = Color.WHITE;

    protected int mTextColor;


    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }


    /**
     * 缩放比
     *
     * @return
     */
    abstract float getDisf();

    abstract void setDisf(float disf);



    public float getParentWidth() {
        return parentWidth;
    }




    public void setParent(float parentWidth, float parentHeight) {
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
    }

    //导出时，记录当前的贴纸size
    protected float parentWidth = 0, parentHeight;


}
