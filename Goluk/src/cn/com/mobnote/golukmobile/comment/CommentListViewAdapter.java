package cn.com.mobnote.golukmobile.comment;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;

public class CommentListViewAdapter extends BaseAdapter {
	private LayoutInflater mLayoutFlater = null;
	private Context mContext = null;
	private ArrayList<CommentBean> mData = new ArrayList<CommentBean>();
	/** 此视频的发布者 */
	private String mVideoUserId = null;

	public CommentListViewAdapter(Context context) {
		mContext = context;
		mLayoutFlater = LayoutInflater.from(mContext);
	}

	public void setVideoUserId(String userid) {
		mVideoUserId = userid;
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

	public void deleteData(CommentBean delBean) {
		if (null == delBean) {
			return;
		}
		boolean isDelSuces = false;
		int size = mData.size();
		for (int i = 0; i < size; i++) {
			if (mData.get(i).mCommentId.equals(delBean.mCommentId)) {
				mData.remove(i);
				isDelSuces = true;
				break;
			}
		}
		if (isDelSuces) {
			this.notifyDataSetChanged();
		}
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
			holder.mNodataLayout = (RelativeLayout) converView.findViewById(R.id.show_nodata_layout);
			holder.nTextCommentFloor = (TextView) converView.findViewById(R.id.tv_listview_item_floor);
			holder.nImageCommentAuthentication = (ImageView) converView.findViewById(R.id.im_listview_item_comment_authentication);
			converView.setTag(holder);
		} else {
			holder = (ViewHolder) converView.getTag();
		}

		holder.mNodataLayout.setVisibility(View.GONE);
		final CommentBean temp = mData.get(position);
		// 设置头像
		String netHeadUrl = temp.customavatar;
		if (null != netHeadUrl && !"".equals(netHeadUrl)) {
			// 使用网络地址
			GlideUtils.loadNetHead(mContext, holder.mHead, netHeadUrl, R.drawable.head_unknown);
		} else {
			// 使用本地头像
			GlideUtils.loadLocalHead(mContext, holder.mHead, UserUtils.getUserHeadImageResourceId(temp.mUserHead));
		}
		holder.nImageCommentAuthentication.setVisibility(View.VISIBLE);
		if ("1".equals(temp.mApprovelabel)) {
			holder.nImageCommentAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
		} else if ("1".equals(temp.mHeadplusv)) {
			holder.nImageCommentAuthentication.setImageResource(R.drawable.authentication_yellowv_icon);
		} else if ("1".equals(temp.mTarento)) {
			holder.nImageCommentAuthentication.setImageResource(R.drawable.authentication_star_icon);
		} else {
			holder.nImageCommentAuthentication.setVisibility(View.GONE);
		}

		// 设置名称
		holder.mName.setText(getShowUserName(temp));
		// 设置评论内容
		if (null != temp.mReplyId && !"".equals(temp.mReplyId) && !"".equals(temp.mReplyName)
				&& null != temp.mReplyName) {
			UserUtils.showText(holder.mContent, temp.mReplyName, temp.mCommentTxt);
		} else {
			holder.mContent.setText(temp.mCommentTxt);
		}
		// 设置显示时间
		holder.mTime.setText(GolukUtils.getCommentShowFormatTime(temp.mCommentTime));
		if (null != temp.mSeq && !"".equals(temp.mSeq)) {
			holder.nTextCommentFloor.setVisibility(View.VISIBLE);
			holder.nTextCommentFloor.setText(temp.mSeq + "楼");
		} else {
			holder.nTextCommentFloor.setVisibility(View.GONE);
		}
		// 点击头像跳转到个人主页
		holder.mHead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent it = new Intent(mContext, UserCenterActivity.class);

				UCUserInfo user = new UCUserInfo();
				user.uid = temp.mUserId;
				user.nickname = temp.mUserName;
				user.headportrait = temp.mUserHead;
				user.introduce = "";
				user.sex = "";
				user.customavatar = temp.customavatar;
				user.praisemenumber = "0";
				user.sharevideonumber = "0";

				it.putExtra("userinfo", user);
				it.putExtra("type", 0);
				mContext.startActivity(it);
			}
		});
		return converView;
	}

	class ViewHolder {
		ImageView mHead = null;
		TextView mName = null;
		TextView mContent = null;
		TextView mTime = null;
		RelativeLayout mNodataLayout = null;
		TextView nTextCommentFloor ;
		ImageView nImageCommentAuthentication;
	}

	private String getShowUserName(CommentBean temp) {
		if (null == mContext || null == mVideoUserId || "".equals(mVideoUserId)) {
			return temp.mUserName;
		}
		// if (mVideoUserId.equals(temp.mUserId)) {
		// return "车主";
		// }
		return temp.mUserName;
	}
}
