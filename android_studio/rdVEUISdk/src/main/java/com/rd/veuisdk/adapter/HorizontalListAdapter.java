package com.rd.veuisdk.adapter;

import android.annotation.SuppressLint;

import com.rd.veuisdk.model.HorizontalListItem;
import com.rd.veuisdk.model.HorizontalListItem.OnListItemClickListener;
import com.rd.veuisdk.model.HorizontalListItem.OnListItemTransitionClickListener;
import com.rd.veuisdk.ui.HorizontalListViewEx;

import java.util.ArrayList;

public class HorizontalListAdapter extends ArrayList<HorizontalListItem>
        implements OnListItemClickListener, OnListItemTransitionClickListener {

    public interface AdapterItemClickListener {
        void onListItemClick(int mSelectedItemId);
    }

    /**
     * 序列号
     */
    private static final long serialVersionUID = -1202374072519245637L;

    /**
     * 标示
     */
    private static final String TAG = "--HorizontalListAdapter-->";

    /**
     * 记录那个ｉｔｅｍ被选中
     */
    private int mSelectedItemId = -1;

    private AdapterItemClickListener mOnItemClickListener;

    private HorizontalListViewEx mHsvList;

    @SuppressLint("NewApi")
    public HorizontalListAdapter() {

    }

    /**
     * 这个函数需要再添加完数据之后才可以调用,否则没办法注册
     */
    public void registerItemClickListerner() {

        for (HorizontalListItem item : this) {

            item.registerListItemClickListener(this);
        }
    }

    /**
     * 只支持转场链表
     */
    public void registerItemTranClickListerner() {

        for (HorizontalListItem item : this) {

            item.registerTransitionClickListener(this);
        }
    }

    public ArrayList<HorizontalListItem> getItems() {
        return this;
    }

    @Override
    public void setOnListItemClick(int itemId) {
        boolean bChanged = true;//itemId != mSelectedItemId;
        doOnClickListener(bChanged, setOnListItemCheck(itemId));
    }

//	public void setOnListItemClickDoListener(int itemId) {
//		doOnClickListener(true, setOnListItemCheck(itemId));
//	}

    public void checkTo(int itemId) {
        doOnClickListener(true, setOnListItemCheck(itemId));
    }

    public HorizontalListItem getChild() {
        HorizontalListItem child = null;

        for (HorizontalListItem item : this) {

            if (item.getItemId() == mSelectedItemId) {
                child = item;
                break;
            }
        }
        return child;

    }

    public ArrayList<HorizontalListItem> getChilds() {
        return this;
    }

    public int getSelectedItemId() {
        return mSelectedItemId;
    }

    public void setOnItemClickListener(AdapterItemClickListener onClickListener) {
        mOnItemClickListener = onClickListener;
    }

    public void setScrollView(HorizontalListViewEx hsv) {
        mHsvList = hsv;
    }

    @Override
    public void setOnListItemClick(int itemId, int tranStrId) {
        boolean bChanged = true;//itemId != mSelectedItemId;
        doOnClickListener(bChanged, setOnListItemCheck(itemId));
    }

    public HorizontalListItem setOnListItemCheck(int targetId) {
        HorizontalListItem itemSelected = null;
        mSelectedItemId = targetId;
        for (HorizontalListItem item : this) {
            if (item.getItemId() == targetId) {
                item.setSelected(true);
                itemSelected = item;
                item.showSelectedFlag(0);
            } else {
                item.hideSelectedFlag();
                item.setSelected(false);
            }

        }
        return itemSelected;

    }

    public void scrollByItem(HorizontalListItem itemSelected) {
        if (itemSelected != null && mHsvList != null) {
            mHsvList.scrollByItem(itemSelected);
        }
    }

    /**
     * 执行回调
     *
     * @param bChanged
     * @param itemSelected
     */
    private void doOnClickListener(boolean bChanged,
                                   HorizontalListItem itemSelected) {
        if (null != mOnItemClickListener && bChanged) {
            mOnItemClickListener.onListItemClick(mSelectedItemId);
        }
        scrollByItem(itemSelected);
    }

}
