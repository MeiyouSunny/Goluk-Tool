package com.rd.veuisdk;

import com.rd.veuisdk.quik.QuikHandler;

/**
 * @author JIAN
 * @create 2018/10/9
 * @Describe
 */
public interface IVideoEditorQuikHandler extends IVideoEditorHandler {

    /**
     * 切换quik效果
     *
     * @param effectInfo
     */
    public void onQuik(QuikHandler.EffectInfo effectInfo);


    /**
     * 切换比例
     *
     * @param asp
     */
    public void onProportion(float asp);


    /**
     * 当前预览比列
     * @return
     */
    public float getProportion();

    /**
     * 辅助类
     * @return
     */
    public QuikHandler getQuikHandler();

}
