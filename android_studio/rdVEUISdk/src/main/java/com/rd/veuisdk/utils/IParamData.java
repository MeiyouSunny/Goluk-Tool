package com.rd.veuisdk.utils;

/**
 * VideoEditActivity 绑定的参数
 */
public interface IParamData extends IMediaParam, IShortParamData {

    /**
     * mv
     */
    void setMVId(int mvId);

    int getMVId();


    /**
     * 配乐菜单下标
     *
     * @param index     下标
     * @param musicName 名称
     */
    void setMusicIndex(int index, String musicName);


    /**
     * 配乐菜单下标
     */
    int getMusicIndex();

    /**
     * 配乐名称
     */
    String getMusicName();


    /**
     * 原音比例
     */
    void setFactor(int factor);

    /**
     * 配乐比例
     */
    void setMusicFactor(int musicFactor);

    int getFactor();

    int getMusicFactor();


    boolean isMediaMute();



}
