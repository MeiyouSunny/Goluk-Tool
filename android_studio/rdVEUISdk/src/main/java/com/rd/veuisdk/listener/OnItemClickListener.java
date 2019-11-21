package com.rd.veuisdk.listener;


/**
 *
 * 所有的RecycleView 单击回调
 * @param <T>
 */
public interface OnItemClickListener<T> {

    void onItemClick(int position, T item);
}
