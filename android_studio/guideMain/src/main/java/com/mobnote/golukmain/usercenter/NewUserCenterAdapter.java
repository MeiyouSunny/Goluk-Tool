package com.mobnote.golukmain.usercenter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.usercenter.bean.HomeVideoList;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

import java.util.List;

public class NewUserCenterAdapter extends BaseAdapter {

	private Context mContext = null;
	private List<HomeVideoList> mList = null;

	public NewUserCenterAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public void setData(List<HomeVideoList> list) {
		this.mList = list;
		this.notifyDataSetChanged();
	}

	public void appendData(List<HomeVideoList> list) {
		if (mList != null)
			mList.addAll(list);
		this.notifyDataSetChanged();
	}

	public void deleteVideo(String vid) {
		if (TextUtils.isEmpty(vid) || null == mList || mList.size() <= 0) {
			return;
		}
		boolean isDelSuccess = false;
		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i).videoid.equals(vid)) {
				mList.remove(i);
				isDelSuccess = true;
			}
		}
		if (isDelSuccess) {
			this.notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return null == mList ? 0 : mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		if (null == mList || arg0 < 0 || arg0 > mList.size() - 1) {
			return null;
		}
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.videocategory_item, null);
			viewHolder.mThumbImage = (ImageView) convertView.findViewById(R.id.iv_videocategory_item_image);
			viewHolder.mTimeText = (TextView) convertView.findViewById(R.id.tv_videocategory_item_time);
			viewHolder.mDescribeText = (TextView) convertView.findViewById(R.id.tv_videocategory_item_describe);
			viewHolder.mCommentCountText = (TextView) convertView.findViewById(R.id.tv_videocategory_item_comment);
			viewHolder.mLookCountText = (TextView) convertView.findViewById(R.id.tv_videocategory_item_look);
			viewHolder.mLockImage = (ImageView) convertView.findViewById(R.id.iv_videocategory_item_lock);
			viewHolder.mVideoType = (TextView) convertView.findViewById(R.id.tv_videocategory_playback);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		HomeVideoList videos = mList.get(arg0);
		if(null != videos) {
			GlideUtils.loadImage(mContext, viewHolder.mThumbImage, videos.pictureurl, R.drawable.tacitly_pic);
			viewHolder.mTimeText.setText(GolukUtils.getCommentShowFormatTime(mContext, videos.addts));
			viewHolder.mDescribeText.setText(videos.description);
			viewHolder.mCommentCountText.setText(GolukUtils.getFormatNumber(videos.commentcount));
			viewHolder.mLookCountText.setText(GolukUtils.getFormatNumber(videos.clickcount));
			if (mContext instanceof NewUserCenterActivity) {
				if (((NewUserCenterActivity) mContext).testUser()) {
					if (videos.isopen == 0) {
						viewHolder.mLockImage.setVisibility(View.VISIBLE);
					} else {
						viewHolder.mLockImage.setVisibility(View.GONE);
					}
				}
			}
			if (videos.type == 1) {
				viewHolder.mVideoType.setVisibility(View.VISIBLE);
			} else {
				viewHolder.mVideoType.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

	static class ViewHolder {
		ImageView mThumbImage;
		TextView mTimeText;
		TextView mDescribeText;
		TextView mCommentCountText;
		TextView mLookCountText;
		ImageView mLockImage;
		TextView mVideoType;
	}

}
