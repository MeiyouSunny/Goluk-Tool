package cn.com.mobnote.golukmobile.praised;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.praised.bean.MyPraisedVideoBean;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;

public class MyPraisedListAdapter extends BaseAdapter {
	private Context mContext;
	private List<MyPraisedVideoBean> mList;
	private int width;

	private final static String TAG = "MyPraisedListAdapter";

	public MyPraisedListAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	}

	public void setData(List<MyPraisedVideoBean> list) {
		this.mList = list;
		notifyDataSetChanged();
	}

	public void appendData(List<MyPraisedVideoBean> list) {
		mList.addAll(list);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return null == mList ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		if (null == mList || position < 0 || position > mList.size() - 1) {
			return null;
		}
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		ViewHolder viewHolder = null;
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.praised_list_item, null);
			viewHolder.nTimeTV = (TextView) convertView.findViewById(R.id.tv_praised_list_item_time);
			viewHolder.nCoverIV = (ImageView) convertView.findViewById(R.id.tv_praised_list_item_cover);
			viewHolder.nAuthorTV = (TextView) convertView.findViewById(R.id.tv_praised_list_item_author);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		MyPraisedVideoBean praisedVideo = mList.get(position);

		int height = (int) ((float) width / 1.78f);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
		//layoutParams.addRule(RelativeLayout.BELOW, R.id.headlayout);
		viewHolder.nCoverIV.setLayoutParams(layoutParams);

		GlideUtils.loadImage(
				mContext, viewHolder.nCoverIV, praisedVideo.picture, R.drawable.tacitly_pic);

		if(!TextUtils.isEmpty(praisedVideo.time)) {
			viewHolder.nTimeTV.setText(
				GolukUtils.getCommentShowFormatTime(mContext, praisedVideo.time));
		}

		return convertView;
	}

	static class ViewHolder {
		TextView nTimeTV;
		ImageView nCoverIV;
		TextView nAuthorTV;
	}
}
