package com.rd.veuisdk.fragment.helper;

import com.rd.vecore.models.VisualFilterConfig;
import com.rd.veuisdk.IPlayer;

/**
 * 更改滤镜
 *
 * @author JIAN
 * @create 2018/12/4
 * @Describe
 */
public interface IFilterHandler extends IPlayer {

    /**
     * 改变滤镜
     *
     * @param index
     * @param nFilterType 滤镜类型:<br>
     */
    void changeFilterType(int index, int nFilterType);


    /**
     * @param lookup lookup滤镜
     * @param index  当前滤镜的下标
     */
    void changeFilterLookup(VisualFilterConfig lookup, int index);


    /**
     * 选择的lookup下标
     *
     * @return
     */
    int getCurrentLookupIndex();
}
