package com.rd.veuisdk.listener;

/**
 * 部分参数需要修正预览size （字幕、贴纸、马赛克、画中画）
 * @author JIAN
 * @create 2019/4/4
 * @Describe
 */
public interface IFixPreviewListener {

    /***
     * 修正dataSource资源成功
     */
    void onComplete();
}
