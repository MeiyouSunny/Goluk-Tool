package cn.com.mobnote.golukmobile.comment;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;

public class CommentListViewAdapter extends BaseAdapter {
	private LayoutInflater mLayoutFlater = null;
	private Context mContext = null;
	private ArrayList<CommentBean> mData = new ArrayList<CommentBean>();

	public CommentListViewAdapter(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
	}

	public void setData(ArrayList<CommentBean> data) {
		mData.clear();
		mData = data;
		this.notifyDataSetChanged();
	}

	public void appendData(ArrayList<CommentBean> data) {
		mData.addAll(data);
		this.notifyDataSetChanged();
	}

	public void addFirstData(CommentBean data) {
		mData.add(0, data);
		this.notifyDataSetChanged();
	}

	// 获取最后一条数据的时间戳
	public String getLastDataTime() {
		if (null == mData || mData.size() <= 0) {
			return "";
		}
		return mData.get(mData.size() - 1).mCommentTime;
	}

	@Override
	public int getCount() {
		return null == mData ? 0 : mData.size();
	}

	@Override
	public Object getItem(final int index) {
		if (null == mData || index < 0 || index > mData.size() - 1) {
			return null;
		}

		return mData.get(index);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View converView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == converView) {
			converView = mLayoutFlater.inflate(R.layout.comment_list_item, null);
			holder = new ViewHolder();
			holder.mHead = (ImageView) converView.findViewById(R.id.comment_item_head);
			holder.mName = (TextView) converView.findViewById(R.id.comment_item_name);
			holder.mTime = (TextView) converView.findViewById(R.id.comment_item_time);
			holder.mContent = (TextView) converView.findViewById(R.id.comment_item_content);
			converView.setTag(holder);
		} else {
			holder = (ViewHolder) converView.getTag();
		}

		final CommentBean temp = mData.get(position);

		holder.mHead.setBackgroundResource(UserUtils.getUserHeadImageResourceId(temp.mUserHead));

		holder.mName.setText(temp.mUserName);
		holder.mContent.setText(temp.mCommentTxt);
		
		holder.mTime.setText(GolukUtils.getCommentShowFormatTime(temp.mCommentTime));
		return converView;
	}

	class ViewHolder {
		ImageView mHead = null;
		TextView mName = null;
		TextView mContent = null;
		TextView mTime = null;
	}

}
