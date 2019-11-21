package com.rd.veuisdk.faceu;

/**
 * @author JIAN
 * @date 2017-3-30 下午3:04:36
 */
public interface FaceuListener {
    /**
     * 贴纸改变
     *
     * @param filePath
     * @param lastPosition
     */
    void onFUChanged(String filePath, int lastPosition);
}
