package com.rd.veuisdk;

/**
 * 每一个短视频(草稿箱)
 */
public interface IShortVideoInfo {


    int getId();
    /**
     * 存为草稿的时刻 (基于系统时间) 单位：毫秒
     *
     * @return
     */
    long getCreateTime();

    /**
     * 虚拟视频时长 单位:秒
     *
     * @return
     */
    float getDuration();


    /**
     * 虚拟视频封面
     *
     * @return
     */
    String getCover();


}
