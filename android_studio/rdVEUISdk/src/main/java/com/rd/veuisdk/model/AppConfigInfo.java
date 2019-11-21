package com.rd.veuisdk.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 记录全局的设置配置
 *
 * @create 2019/10/10
 */
public class AppConfigInfo implements Parcelable {

    //设置-启用图片动画
    private boolean isZoomOut = true;

    /**
     * 设置-是否有背景
     * true 有背景 （按钮未选中） ；false 无背景 ，默认（按钮选中）
     */
    private boolean isEnableBGMode = false;

    //背景颜色
    private int nBgColor = Color.BLACK;

    //预览比例
    private float nProportionAsp = 0;


    public AppConfigInfo() {

    }


    public boolean isEnableBGMode() {
        return isEnableBGMode;
    }

    public void setEnableBGMode(boolean enableBGMode) {
        isEnableBGMode = enableBGMode;
    }

    public int getBgColor() {
        return nBgColor;
    }

    public void setBgColor(int nBgColor) {
        this.nBgColor = nBgColor;
    }

    public boolean isZoomOut() {
        return isZoomOut;
    }

    public void setZoomOut(boolean zoomOut) {
        isZoomOut = zoomOut;
    }


    public float getProportionAsp() {
        return nProportionAsp;
    }

    public void setProportionAsp(float nProportionAsp) {
        this.nProportionAsp = nProportionAsp;
    }


    protected AppConfigInfo(Parcel in) {
        isZoomOut = in.readByte() != 0;
        isEnableBGMode = in.readByte() != 0;
        nBgColor = in.readInt();
        nProportionAsp = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isZoomOut ? 1 : 0));
        dest.writeByte((byte) (isEnableBGMode ? 1 : 0));
        dest.writeInt(nBgColor);
        dest.writeFloat(nProportionAsp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppConfigInfo> CREATOR = new Creator<AppConfigInfo>() {
        @Override
        public AppConfigInfo createFromParcel(Parcel in) {
            return new AppConfigInfo(in);
        }

        @Override
        public AppConfigInfo[] newArray(int size) {
            return new AppConfigInfo[size];
        }
    };

}
