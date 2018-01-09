package com.mobnote.golukmain.msg;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.cluster.ClusterActivity;
import com.mobnote.golukmain.msg.bean.MessageMsgsBean;
import com.mobnote.golukmain.msg.bean.MessageSenderBean;
import com.mobnote.golukmain.special.SpecialListActivity;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import java.util.ArrayList;
import java.util.List;

public class MsgCenterCommentAdapter extends BaseAdapter {

	private Context mContext;
	private List<MessageMsgsBean> mList;

	public MsgCenterCommentAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public void setData(List<MessageMsgsBean> list) {
		this.mList = list;
		this.notifyDataSetChanged();
	}

	public void appendData(List<MessageMsgsBean> list) {
		if (mList == null) {
			mList =new ArrayList<MessageMsgsBean>();
		}
		mList.addAll(list);
		this.notifyDataSetChanged();
	}

	// 获取最后一条数据的时间戳
	public String getLastDataTime() {
		if (null == mList || mList.size() <= 0) {
			return "";
		}
		return mList.get(mList.size() - 1).edittime;
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_msgcenter_comment, null);
			viewHolder.nImageHead = (ImageView) convertView.findViewById(R.id.iv_listview_item_comment_head);
			viewHolder.nImageAuthentication = (ImageView) convertView
					.findViewById(R.id.iv_listview_item_comment_head_authentication);
			viewHolder.nImageThumbnail = (ImageView) convertView.findViewById(R.id.iv_listview_item_comment_thumbnail);
			viewHolder.nTextName = (TextView) convertView.findViewById(R.id.tv_listview_item_comment_name);
			viewHolder.nTextContent = (TextView) convertView.findViewById(R.id.tv_listview_item_comment_content);
			viewHolder.nTextTime = (TextView) convertView.findViewById(R.id.tv_listview_item_comment_time);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final MessageMsgsBean messageBean = mList.get(arg0);
		if (null != messageBean && 101 == messageBean.type && null != messageBean.content
				&& null != messageBean.sender) {
			convertView.setVisibility(View.VISIBLE);
			viewHolder.nTextName.setText(messageBean.sender.name);
			String netUrlHead = messageBean.sender.customavatar;
			if (null != netUrlHead && !"".equals(netUrlHead)) {
				GlideUtils.loadNetHead(mContext, viewHolder.nImageHead, netUrlHead, R.drawable.my_head_moren7);
			} else {
				UserUtils.focusHead(mContext, messageBean.sender.avatar, viewHolder.nImageHead);
			}
			if (null != messageBean.sender.label) {
				String approvelabel = messageBean.sender.label.approvelabel;
				String headplusv = messageBean.sender.label.headplusv;
				String tarento = messageBean.sender.label.tarento;
				viewHolder.nImageAuthentication.setVisibility(View.VISIBLE);
				if ("1".equals(approvelabel)) {
					viewHolder.nImageAuthentication.setImageResource(R.drawable.authentication_bluev_icon);
				} else if ("1".equals(headplusv)) {
					viewHolder.nImageAuthentication.setImageResource(R.drawable.authentication_yellowv_icon);
				} else if ("1".equals(tarento)) {
					viewHolder.nImageAuthentication.setImageResource(R.drawable.authentication_star_icon);
				} else {
					viewHolder.nImageAuthentication.setVisibility(View.GONE);
				}
			} else {
				viewHolder.nImageAuthentication.setVisibility(View.GONE);
			}
			if (!TextUtils.isEmpty(messageBean.content.comment.replyid)) {
				GolukApplication app = ((MsgCenterCommentActivity)mContext).getApp();
				String name;
				if (messageBean.content.comment.replyid.equals(app.mCurrentUId)) {
					name = mContext.getResources().getString(R.string.str_msgcenter_comment_replyme);
				}else {
					name = messageBean.content.comment.replyname;
				}
				showCommentText(viewHolder.nTextContent,name,messageBean.content.comment.text);
			} else {
				viewHolder.nTextContent.setText(messageBean.content.comment.text);
			}
			viewHolder.nTextTime.setText(GolukUtils.getCommentShowFormatTime(mContext, messageBean.content.ts));
			GlideUtils.loadImage(mContext, viewHolder.nImageThumbnail, messageBean.content.picture,
					R.drawable.tacitly_pic);
			
			// 点击事件
			viewHolder.nImageHead.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					intentToOthers(arg0.getId(), messageBean);
				}
			});
			viewHolder.nTextName.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					intentToOthers(arg0.getId(), messageBean);
				}
			});
			viewHolder.nTextContent.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					intentToOthers(arg0.getId(), messageBean);
				}
			});
			viewHolder.nImageThumbnail.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					intentToOthers(arg0.getId(), messageBean);
				}
			});
		} else {
			convertView.setVisibility(View.GONE);
		}
		return convertView;
	}

	private static class ViewHolder {
		ImageView nImageHead;
		ImageView nImageAuthentication;
		ImageView nImageThumbnail;
		TextView nTextName;
		TextView nTextContent;
		TextView nTextTime;
	}
	
	/**
	 * 页面跳转
	 * @param id
	 * @param bean
	 */
	private void intentToOthers(int id, MessageMsgsBean bean) {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
			return;
		}
		if(id == R.id.iv_listview_item_comment_head || id == R.id.tv_listview_item_comment_name) {
			if (null != bean && null != bean.sender) {
				MessageSenderBean sender = bean.sender;
				GolukUtils.startUserCenterActivity(mContext,sender.uid);
			}
		} else {
			if (null != bean && 101 == bean.type && null != bean.content) {
				if ("1".equals(bean.content.type)) {
					//视频详情页访问
					ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_msg_center));
					// 视频详情
					Intent itVideoDetail = new Intent(mContext, VideoDetailActivity.class);
					itVideoDetail.putExtra(VideoDetailActivity.VIDEO_ID, bean.content.access);
					mContext.startActivity(itVideoDetail);
				} else if ("2".equals(bean.content.type)) {
					// 精选专题
					Intent itSpecial = new Intent(mContext, SpecialListActivity.class);
					itSpecial.putExtra("ztid", bean.content.access);
					itSpecial.putExtra("title", "");
					mContext.startActivity(itSpecial);
				} else if ("4".equals(bean.content.type)) {
					// 活动聚合
					Intent itCluster = new Intent(mContext, ClusterActivity.class);
					itCluster.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, bean.content.access);
					itCluster.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, "");
					mContext.startActivity(itCluster);
				} else if ("6".equals(bean.content.type)) {
					//视频详情页访问
					ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_msg_center));
					// 精选单视频
					Intent itWonderful = new Intent(mContext, VideoDetailActivity.class);
					itWonderful.putExtra("ztid", bean.content.access);
					mContext.startActivity(itWonderful);
				}
			}
		}
	}
	
	private void showCommentText(TextView view, String nikename, String text) {
		String t_str = mContext.getResources().getString(R.string.str_msgcenter_comment_replytext) + nikename
				+ mContext.getString(R.string.str_colon_english) + text;
		SpannableStringBuilder style = new SpannableStringBuilder(t_str);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 2, nikename.length() + 2,
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		view.setText(style);
	}

}
