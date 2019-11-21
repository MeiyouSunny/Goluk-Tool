package com.rd.veuisdk.utils;

import com.rd.vecore.models.VisualFilterConfig;

/**
 * 媒体滤镜
 */
public interface IMediaParam {
    /**
     * 滤镜下标
     *
     * @param index
     */
    void setFilterIndex(int index);

    /**
     * 滤镜菜单下标
     *
     * @return
     */
    int getFilterIndex();


    /**
     * acv 滤镜的值
     *
     * @return
     */
    int getCurrentFilterType();

    void setCurrentFilterType(int currentFilterType);


    /**
     * lookup 滤镜
     *
     * @param lookupConfig
     */
    void setLookupConfig(VisualFilterConfig lookupConfig);

    VisualFilterConfig getLookupConfig();
}
