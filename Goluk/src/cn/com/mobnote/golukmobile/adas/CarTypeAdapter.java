package cn.com.mobnote.golukmobile.adas;

import java.util.ArrayList;

import cn.com.mobnote.golukmobile.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CarTypeAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<VehicleParamterBean> mData;
	private LayoutInflater mInflater;
	private int mSelectedId;
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData == null ? 0: mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mData == null ? null: mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		VehicleParamterBean item = (VehicleParamterBean) getItem(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item_car_type, null);
			viewHolder = new ViewHolder();
			viewHolder.mTitle = (TextView) convertView.findViewById(R.id.textview_name);
			viewHolder.mSelectedIcon = (ImageView) convertView.findViewById(R.id.imageview_selected_icon);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		if (mSelectedId != position) {
			viewHolder.mSelectedIcon.setVisibility(View.GONE);
		} else {
			viewHolder.mSelectedIcon.setVisibility(View.VISIBLE);
		}
		viewHolder.mTitle.setText(item.name);
		return convertView;
	}

	public CarTypeAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<VehicleParamterBean> data) {
		mData = data;
		notifyDataSetChanged();
	}
	
	public void setSelectedId(int position) {
		mSelectedId = position;
		notifyDataSetChanged();
	}

	public int getSelectedId() {
		return mSelectedId;
	}

	private static class ViewHolder {
		TextView mTitle;
		ImageView mSelectedIcon;
	}

}
