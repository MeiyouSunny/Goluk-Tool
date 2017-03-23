package com.mobnote.golukmain.videodetail;

import java.util.ArrayList;
import java.util.List;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.comment.CommentBean;
import com.mobnote.golukmain.usercenter.NewUserCenterActivity;
import com.mobnote.golukmain.usercenter.UCUserInfo;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

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
import cn.com.tiros.debug.GolukDebugUtils;

public class VideoDetailAdapter extends BaseAdapter {
	private static final String COMMENT_ANONYMOUSE_PRE = "anonymous_";

	public Context mContext = null;

	private List<CommentBean> mDataList = null;

	/** 评论holder **/
	private ViewHolder commentHolder = null;

	public CustomLoadingDialog mCustomLoadingDialog;

	public VideoDetailAdapter(Context context, int type) {
		mContext = context;
		mDataList = new ArrayList<CommentBean>();
	}

	public void setData(List<CommentBean> commentData) {
		mDataList.clear();

		if (null != commentData) {
			mDataList.addAll(commentData);
		}
		this.notifyDataSetChanged();
	}

	public void appendData(ArrayList<CommentBean> data) {
		mDataList.addAll(data);
		this.notifyDataSetChanged();
	}

	public void addFirstData(CommentBean data) {
		mDataList.add(0, data);
		this.notifyDataSetChanged();
	}

	public void deleteData(CommentBean delBean) {
		if (null == delBean) {
			return;
		}
		boolean isDelSuces = false;
		int size = mDataList.size();
		for (int i = 0; i < size; i++) {
			if (mDataList.get(i).mCommentId.equals(delBean.mCommentId)) {
				mDataList.remove(i);
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
		if (null == mDataList || mDataList.size() <= 0) {
			return "";
		}
		return mDataList.get(mDataList.size() - 1).mCommentTime;
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int arg0) {
		if (null == mDataList || arg0 < 0 || arg0 > mDataList.size() - 1) {
			return null;
		}

		return mDataList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		String s = (null == convertView) ? "convertView == NULL" : "converView Not null";
		GolukDebugUtils.e("newadapter", "VideoDetailActivity===getView=  positon:" + arg0 + "  " + s);
		convertView = loadLayout(convertView, arg0);
		return convertView;
	}

	/**
	 * 评论
	 * 
	 * @return
	 */
	private View getCommentView(View convertView) {
		commentHolder = new ViewHolder();
		convertView = LayoutInflater.from(mContext).inflate(R.layout.comment_list_item, null);

		commentHolder.mCommentHead = (ImageView) convertView.findViewById(R.id.comment_item_head);
		commentHolder.mCommentTime = (TextView) convertView.findViewById(R.id.comment_item_time);
		commentHolder.mCommentName = (TextView) convertView.findViewById(R.id.comment_item_name);
		commentHolder.mCommentConennt = (TextView) convertView.findViewById(R.id.comment_item_content);
		commentHolder.nCommentAuthentication = (ImageView) convertView
				.findViewById(R.id.im_listview_item_comment_authentication);

		commentHolder.mListLayout = (RelativeLayout) convertView.findViewById(R.id.comment_list_layout);
		commentHolder.nTextCommentFloor = (TextView) convertView.findViewById(R.id.tv_listview_item_floor);

		convertView.setTag(commentHolder);
		return convertView;
	}

	private View loadLayout(View convertView, int arg0) {
		if (null == convertView) {
			convertView = getCommentView(convertView);
		} else {
			commentHolder = (ViewHolder) convertView.getTag();
			if (null == commentHolder) {
				convertView = getCommentView(convertView);
			}
		}
		getCommentData(arg0);
		return convertView;
	}

	// 设置评论数据
	private void getCommentData(final int index) {
		commentHolder.mListLayout.setVisibility(View.VISIBLE);

		if (null != mDataList && 0 == mDataList.size()) {
			return;
		}
		CommentBean temp = mDataList.get(index);
		String netHeadUrl = temp.customavatar;
		if (null != netHeadUrl && !"".equals(netHeadUrl)) {
			// 使用网络地址
			GlideUtils.loadNetHead(mContext, commentHolder.mCommentHead, netHeadUrl, -1);
		} else {
			// 使用本地头像
			GlideUtils.loadLocalHead(mContext, commentHolder.mCommentHead,
					UserUtils.getUserHeadImageResourceId(temp.mUserHead));
		}
		commentHolder.nCommentAuthentication.setVisibility(View.VISIBLE);
		if (null != temp) {
			if ("1".equals(temp.mApprovelabel)) {
				commentHolder.nCommentAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
			} else if ("1".equals(temp.mHeadplusv)) {
				commentHolder.nCommentAuthentication.setImageResource(R.drawable.authentication_yellowv_icon);
			} else if ("1".equals(temp.mTarento)) {
				commentHolder.nCommentAuthentication.setImageResource(R.drawable.authentication_star_icon);
			} else {
				commentHolder.nCommentAuthentication.setVisibility(View.GONE);
			}
		}
		commentHolder.mCommentName.setText(temp.mUserName);
		if (!"".equals(temp.mReplyId) && null != temp.mReplyId && !"".equals(temp.mReplyName)
				&& null != temp.mReplyName) {
			// 评论回复
			UserUtils.showText(mContext, commentHolder.mCommentConennt, temp.mReplyName, temp.mCommentTxt);
		} else {
			// 普通评论
			commentHolder.mCommentConennt.setText(temp.mCommentTxt);
		}

		commentHolder.mCommentTime.setText(GolukUtils.getCommentShowFormatTime(mContext, temp.mCommentTime));
		if (null != temp.mSeq && !"".equals(temp.mSeq)) {
			commentHolder.nTextCommentFloor.setVisibility(View.VISIBLE);
			commentHolder.nTextCommentFloor.setText(mContext.getString(R.string.str_floor_text, temp.mSeq));
		} else {
			commentHolder.nTextCommentFloor.setVisibility(View.GONE);
		}

		commentHolder.mCommentHead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				CommentBean bean = mDataList.get(index);
				if (bean !=null && bean.mUserId.startsWith(COMMENT_ANONYMOUSE_PRE)){
					return;
				}
				GolukUtils.startUserCenterActivity(mContext,bean.mUserId);
			}
		});

	}


	public static class ViewHolder {
		// 评论
		ImageView mCommentHead = null;
		TextView mCommentTime, mCommentName, mCommentConennt;
		RelativeLayout mListLayout;
		TextView nTextCommentFloor;
		ImageView nCommentAuthentication;

	}

}
