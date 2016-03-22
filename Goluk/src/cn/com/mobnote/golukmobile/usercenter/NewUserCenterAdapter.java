package cn.com.mobnote.golukmobile.usercenter;

import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeVideoList;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
		mList.addAll(list);
		this.notifyDataSetChanged();
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
		return 0;
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
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		HomeVideoList videos = mList.get(arg0);
		GlideUtils.loadImage(mContext, viewHolder.mThumbImage, videos.pictureurl, R.drawable.tacitly_pic);
		viewHolder.mTimeText.setText(GolukUtils.getCommentShowFormatTime(mContext, videos.addtime));
		viewHolder.mDescribeText.setText(videos.description);
		viewHolder.mCommentCountText.setText(videos.commentcount+"");
		viewHolder.mLookCountText.setText(videos.clickcount+"");
		
		return convertView;
	}

	static class ViewHolder {
		ImageView mThumbImage;
		TextView mTimeText;
		TextView mDescribeText;
		TextView mCommentCountText;
		TextView mLookCountText;
	}

}
