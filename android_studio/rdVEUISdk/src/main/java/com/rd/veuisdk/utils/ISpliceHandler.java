package com.rd.veuisdk.utils;

import com.rd.veuisdk.model.SpliceGridMediaInfo;
import com.rd.veuisdk.model.SpliceModeInfo;

import java.util.List;

/**
 * 拼接回调
 */
public interface ISpliceHandler {

    int getCheckedModeIndex();

    /**
     * 切换模板
     */
    void onMode(int index, SpliceModeInfo info);

    /**
     * 切换比例
     *
     * @param asp
     */
    void onProportion(float asp);

    /**
     * 比例
     */
    float getProportion();


    /**
     * 切换item播放顺序
     *
     * @param isOrder true 顺序播放，false 同时播放
     */
    void onSpliceOrder(boolean isOrder);


    /**
     * 播放模式
     *
     * @return true 顺序播放，false 同时播放
     */
    boolean isOrderPlay();

    /**
     * 媒体
     */
    List<SpliceGridMediaInfo> getMediaList();


    /**
     * 旋转
     */
    void onRotate();

    /**
     * 裁剪
     */
    void onTrim();

    /**
     * 替换
     */
    void onReplace();

    /**
     * 退出编辑单个画框
     */
    void onExitEdit();


    /**
     * 当前缩放比
     *
     * @return
     */
    float getScale();

    /**
     * 缩放比
     *
     * @param scale
     */
    void setScale(float scale);

    /**
     * 背景颜色值
     */
    void setBackgroundColor(int color);

    /**
     * 当前背景色
     * @return
     */
    int getBgColor();

}
