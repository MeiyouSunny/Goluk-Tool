package cn.com.mobnote.golukmobile.promotion;

import java.util.ArrayList;

import cn.com.mobnote.golukmobile.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PromotionDataAdapter extends BaseAdapter {

	private static final int TYPE_CATEGORY_ITEM = 0;
	private static final int TYPE_ITEM = 1;
	Context mContext;
	ArrayList<PromotionData> mListData;
	private LayoutInflater mInflater;
	private PromotionSelectItem mSelectItem;
	private int mSelectId = -1;
	PromotionDataAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mSelectItem = new PromotionSelectItem();
	}

	public void setData(ArrayList<PromotionData> data) {
		mListData = data;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int count = 0;

		if (null != mListData) {

			// 所有分类中item的总和是ListVIew Item的总个数
			for (PromotionData category : mListData) {
				count += category.getItemCount();
			}
		}

		return count;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		// 异常情况处理
		if (null == mListData || position < 0 || position > getCount()) {
			return null;
		}

		// 同一分类内，第一个元素的索引值
		int categroyFirstIndex = 0;

		for (PromotionData category : mListData) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = position - categroyFirstIndex;
			// item在当前分类内
			if (categoryIndex < size) {
				mSelectItem.channelid = category.channelid;
				mSelectItem.selectid = position;
				mSelectItem.channelname = category.channelname;
				PromotionItem item = category.getItem(categoryIndex);
				if (item != null) {
					mSelectItem.activitytitle = item.name;
					mSelectItem.activityid = item.id;
				} else {
					mSelectItem.activitytitle = null;
					mSelectItem.activityid = null;
				}
				return mSelectItem;
			}

			// 索引移动到当前分类结尾，即下一个分类第一个元素索引
			categroyFirstIndex += size;
		}

		return null;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		if (null == mListData || position < 0 || position > getCount()) {
			return TYPE_ITEM;
		}

		int categroyFirstIndex = 0;

		for (PromotionData category : mListData) {
			int size = category.getItemCount();
			// 在当前分类中的索引值
			int categoryIndex = position - categroyFirstIndex;
			if (categoryIndex == 0) {
				return TYPE_CATEGORY_ITEM;
			}

			categroyFirstIndex += size;
		}

		return TYPE_ITEM;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GroupViewHolder groupHolder;
		ItemViewHolder itemHolder;
		
		int itemViewType = getItemViewType(position);
		String itemValue;
		PromotionSelectItem item = (PromotionSelectItem) getItem(position);
		switch (itemViewType) {
		case TYPE_CATEGORY_ITEM:
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.promotion_group, null);
				groupHolder = new GroupViewHolder();
				groupHolder.title = (TextView) convertView
						.findViewById(R.id.title_text);
				convertView.setTag(groupHolder);
			} else {
				groupHolder = (GroupViewHolder) convertView.getTag();
			}

			itemValue = item.channelname + ":";
			groupHolder.title.setText(itemValue);
			break;

		case TYPE_ITEM:
			if (null == convertView) {

				convertView = mInflater.inflate(R.layout.promotion_item, null);

				itemHolder = new ItemViewHolder();
				itemHolder.title = (TextView) convertView
						.findViewById(R.id.title);
				itemHolder.itemLayout = (LinearLayout) convertView
						.findViewById(R.id.item_layout);
				convertView.setTag(itemHolder);
			} else {
				itemHolder = (ItemViewHolder) convertView.getTag();
			}

			// 绑定数据
			itemValue = "#" + item.activitytitle + "#";
			itemHolder.title.setText(itemValue);
			if (position == mSelectId) {
				convertView.requestFocus();
				itemHolder.title.setTextColor(Color.parseColor("#0080ff"));
				itemHolder.itemLayout.setBackgroundResource(R.drawable.share_promotion_frame_selected);
			} else {
				itemHolder.title.setTextColor(Color.parseColor("#808080"));
				itemHolder.itemLayout.setBackgroundResource(R.drawable.share_promotion_frame);
			}
			break;
		}

		return convertView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) != TYPE_CATEGORY_ITEM;
	}

	private class ItemViewHolder {
		TextView title;
		LinearLayout itemLayout;
	}

	private class GroupViewHolder {
		TextView title;
	}

	public void setSelectId(int id) {
		mSelectId = id;
	}
}
