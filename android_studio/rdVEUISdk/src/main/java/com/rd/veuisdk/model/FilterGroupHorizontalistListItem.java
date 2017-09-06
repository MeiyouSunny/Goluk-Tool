package com.rd.veuisdk.model;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.HorizontalListAdapter;
import com.rd.veuisdk.fragment.FilterFragment;
import com.rd.veuisdk.ui.ExtListItemStyle;
import com.rd.veuisdk.ui.HorizontalListViewEx;

public class FilterGroupHorizontalistListItem extends HorizontalListItem {

    public FilterGroupHorizontalistListItem(Context context, int itemId) {
	super(context, itemId);
	initContentView();
    }

    private ExtListItemStyle icon;

    /** 滤镜类型链表 */
    private HorizontalListViewEx mHorizontalListView1;

    /** 容器布局 */
    private LinearLayout containerLinearLayout1;

    /** 链表适配器 */
    private HorizontalListAdapter group_childs_adapter;
    private ImageView folder;

    @Override
    public void initContentView() {
	contentView = inflater.inflate(R.layout.group_item_layout, null);
	icon = (ExtListItemStyle) contentView
		.findViewById(R.id.iv_music_network);
	containerLinearLayout1 = (LinearLayout) contentView
		.findViewById(R.id.ll_container_layout_id);
	mHorizontalListView1 = (HorizontalListViewEx) contentView
		.findViewById(R.id.hlv_show_styel_listview);
	mHorizontalListView1.setContainerLayout(containerLinearLayout1);
	folder = (ImageView) contentView.findViewById(R.id.group_filter_floder);
	initData();

	if (null != mHorizontalListView1) {
	    icon.postDelayed(new Runnable() {

		@Override
		public void run() {
		    sx = (int) icon.getX();
		}
	    }, 1050);
	}

    }

    private int sx;

    @Override
    public void showSelectedFlag(int strId) {

 
	int count = group_childs_adapter.getItems().size();
	 
	for (int i = 0; i < count; i++) {
	    MyFilterHorizontalListItem item = (MyFilterHorizontalListItem) group_childs_adapter
		    .get(i);
	    if (FilterFragment.checkFilterId != item.getItemId()) {
		item.hideSelectedFlag();
	    }

	}

	group_childs_adapter.setOnListItemClick(FilterFragment.checkFilterId);

	mHorizontalListView1.postDelayed(new Runnable() {

	    @Override
	    public void run() {
		mHorizontalListView1.setVisibility(View.VISIBLE);
		folder.setImageResource(R.drawable.folder_n);
		ViewParent vp = mHorizontalListView1.getParent().getParent();
		if (vp instanceof com.rd.veuisdk.ui.HorizontalListViewEx) {
		    ((View) vp).scrollTo(sx, 0);
		}
	    }
	}, 100);

	// }
    }

    @Override
    public void hideSelectedFlag() {
 
 
	mHorizontalListView1.setVisibility(View.GONE);
	folder.setImageResource(R.drawable.folder_p);
    }

    public FilterGroupHorizontalistListItem(Context context, int groupDrawable,
	    int nItemId, OnFilterClickListener listener,
	    ArrayList<FilterItemInfo> list) {
	super(context, nItemId);
	// mFilterListener = listener;
	icon.setbitmap(BitmapFactory.decodeResource(context.getResources(),
		groupDrawable));
	group_childs_adapter = new HorizontalListAdapter();
	int len = list.size();
	FilterItemInfo info;
	for (int i = 0; i < len; i++) {
	    info = list.get(i);
	    group_childs_adapter.add(new MyFilterHorizontalListItem(context,
		    listener, info.getData(), info.getnItemId(),true));
	}
	mHorizontalListView1.setAdapter(group_childs_adapter);
	group_childs_adapter.registerItemClickListerner();
    }

    // private OnFilterClickListener mFilterListener;

    private void initData() {

	// 处理点击事件
	icon.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {

		if (listItemClickListener != null) {
		    listItemClickListener.setOnListItemClick(itemId);
		}
		//
		// if (mFilterListener != null) {
		// mFilterListener.onSwitchFilterClick(itemId);
		// }
	    }
	});

    }

}
