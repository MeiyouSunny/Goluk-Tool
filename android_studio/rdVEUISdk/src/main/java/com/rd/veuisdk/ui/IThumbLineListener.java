package com.rd.veuisdk.ui;

/**
 * 支持 com.rd.ui.*{ThumbNailLine,ThumbNailLineMusic}的回调
 *
 * @author JIAN
 */
public interface IThumbLineListener {
    /**
     * 按住放开或设置区域成功
     *
     * @param id
     * @param start
     * @param end
     */
    void updateThumb(int id, int start, int end);



    /**
     * 点击选中的项
     *
     * @param changed id已经改变 3中情况会促发（ 1 currentsub==null(changed=true) ， 2 切换选择项
     *                (changed=true) ，3选中之后再次选中该项(changed=false)）
     * @param id
     */

    void onCheckItem(boolean changed, int id);

    void onTouchUp();
}
