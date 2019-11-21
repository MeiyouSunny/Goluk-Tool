package com.rd.veuisdk.utils;

import com.rd.vecore.models.caption.CaptionLiteObject;
import com.rd.veuisdk.model.GraffitiInfo;

import java.util.ArrayList;

/**
 * 公共参数
 *
 * @create 2019/5/14
 */
public interface IShortParamData {
    /**
     * 音效
     *
     * @param effectId
     */
    void setSoundEffectId(int effectId);

    /**
     * @return
     */
    int getSoundEffectId();

    /**
     * 涂鸦
     */
    void setGraffitiList(ArrayList<GraffitiInfo> list);

    /**
     * 已添加的涂鸦
     */
    ArrayList<GraffitiInfo> getGraffitiList();


    /**
     * 封面
     */
    void setCoverCaption(CaptionLiteObject captionLiteObject);

    CaptionLiteObject getCoverCaption();


    /**
     * 音调
     */
    void setMusicPitch(float musicPitch);

    float getMusicPitch();


    /**
     * 设置图片放大
     */

    void setZoomOut(boolean isZoomOut);

    /**
     * 图片是否有放大动画
     */
    boolean isZoomOut();

    /**
     * 设置是否有背景
     */
    void enableBackground(boolean enable);

    /**
     * 是否有背景
     */
    boolean isEnableBackground();


    int getBgColor();

    void setBgColor(int bgColor);


    /**
     * 预览比例
     */
    void setProportionAsp(float asp);

    float getProportionAsp();

}
