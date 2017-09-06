package com.rd.veuisdk.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * 自定义的listitem都需要继承ListItem这个抽象类.然后实现initContentView这个方法,
 * 初始化布局,然后可以根据自己的实际情况,添加自己的各种item
 * 
 * @author jeck
 * 
 */
public abstract class HorizontalListItem {

    /** 布局资源 */
    protected View contentView;

    /** 资源填充器 */
    protected LayoutInflater inflater;

    /** itemId */
    protected int itemId = -1;

    /** 相关id */
    protected int relativeId = -1;

    /** 点击事件监听器 */
    protected OnListItemClickListener listItemClickListener;
    protected OnListItemTransitionClickListener listItemTranClickListener;

    protected Context _context;

    /** 是否为选中状态 */
    protected boolean isSelected = false;

    public HorizontalListItem(Context context, int itemId) {

	this._context = context;
	this.itemId = itemId;

	inflater = (LayoutInflater) _context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	initContentView();
    }

    /**
     * 用于初始化我们的listItem布局,通过findViewById将我们布局当中的控件实例化
     */
    public abstract void initContentView();

    /**
     * 显示选中状态, 这个可以根据自己的情况,添加或者删除这个抽象函数
     */
    public abstract void showSelectedFlag(int strId);


    /**
     * 隐藏选中状态
     */
    public abstract void hideSelectedFlag();

    /**
     * 返回item布局,因为我们的父容器需要将每一个item布局添加到父容器布局中
     * 
     * @return
     */
    public View getContentView() {

	return contentView;
    }

    /**
     * 为每一个item注册一个点击响应事件监听器,用于处理点击事件
     * 
     * @param listener
     */
    public void registerListItemClickListener(OnListItemClickListener listener) {

	this.listItemClickListener = listener;
    }

    public void registerTransitionClickListener(
	    OnListItemTransitionClickListener listener) {

	this.listItemTranClickListener = listener;
    }

    public int getItemId() {
	return itemId;
    }

    public boolean isSelected() {
	return isSelected;
    }

    public void setSelected(boolean isSelected) {
	this.isSelected = isSelected;
    }

    public int getRelativeId() {
	return relativeId;
    }

    public void setRelativeId(int relativeId) {
	this.relativeId = relativeId;
    }

    /**
     * 响应点击事件
     * 
     * @author jeck
     * 
     */
    public interface OnListItemClickListener {

	public void setOnListItemClick(int itemId);

    }

    /**
     * 只支持转场
     * 
     * @author JIAN
     * 
     */
    public interface OnListItemTransitionClickListener {

	public void setOnListItemClick(int itemId, int tranStrId);

    }

    /**
     * 点击显示配乐布局
     * 
     * @author jeck
     * 
     */
    public interface OnFilterClickListener {

	/**
	 * 切换配乐
	 */
	public void onSwitchFilterClick(int filterType, HorizontalListItem item);

    }

}
