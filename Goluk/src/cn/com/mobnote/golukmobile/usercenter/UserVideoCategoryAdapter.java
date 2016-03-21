package cn.com.mobnote.golukmobile.usercenter;

import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.usercenter.bean.VideoList;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserVideoCategoryAdapter extends BaseAdapter {

	private Context mContext = null;
	private List<VideoList> mListData = null;
	
	public UserVideoCategoryAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}
	
	public void setDataInfo(List<VideoList> list) {
		this.mListData = list;
		this.notifyDataSetChanged();
	}
	
	public void appendData(List<VideoList> list) {
		this.mListData.addAll(mListData);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		
		return null == mListData ? 0 : mListData.size();
	}

	@Override
	public Object getItem(int arg0) {
		if (null == mListData || mListData.size() <= 0) {
			return "";
		}
		return null == mListData ? 0 : mListData.size();
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.videocategory_item, null);
			holder.mThumbImage = (ImageView) convertView.findViewById(R.id.iv_videocategory_item_image);
			holder.mTimeText = (TextView) convertView.findViewById(R.id.tv_videocategory_item_time);
			holder.mDescribeText = (TextView) convertView.findViewById(R.id.tv_videocategory_item_describe);
			holder.mCommentCountText = (TextView) convertView.findViewById(R.id.tv_videocategory_item_comment);
			holder.mLookCountText = (TextView) convertView.findViewById(R.id.tv_videocategory_item_look);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		VideoList data = mListData.get(arg0);
		
		GlideUtils.loadImage(mContext, holder.mThumbImage, data.pictureurl, R.drawable.tacitly_pic);
		String sharingTime = GolukUtils.getCommentShowFormatTime(mContext, data.addtime);
		holder.mTimeText.setText(GolukUtils.getCommentShowFormatTime(mContext, sharingTime));
		holder.mDescribeText.setText(data.description);
		holder.mCommentCountText.setText(data.commentcount+"");
		holder.mLookCountText.setText(data.clickcount+"");
		
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
