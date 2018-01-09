package com.mobnote.golukmain.msg;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.msg.bean.MessageMsgsBean;
import com.mobnote.golukmain.msg.bean.MessageSenderBean;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import java.util.List;

public class MsgCenterPraiseAdapter extends BaseAdapter {

	private Context mContext;
	private List<MessageMsgsBean> mList;

	public MsgCenterPraiseAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public void setData(List<MessageMsgsBean> list) {
		this.mList = list;
		this.notifyDataSetChanged();
	}

	public void appendData(List<MessageMsgsBean> list) {
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_msgcenter_praise, null);
			viewHolder = new ViewHolder();
			viewHolder.nImageHead = (ImageView) convertView.findViewById(R.id.iv_listview_item_praise_head);
			viewHolder.nImageAuthentication = (ImageView) convertView
					.findViewById(R.id.iv_listview_item_praise_head_authentication);
			viewHolder.nTextName = (TextView) convertView.findViewById(R.id.tv_listview_item_praise_name);
			viewHolder.nTextTime = (TextView) convertView.findViewById(R.id.tv_listview_item_praise_time);
			viewHolder.nImageThumbnail = (ImageView) convertView.findViewById(R.id.iv_listview_item_praise_thumbnail);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final MessageMsgsBean praiseBean = mList.get(arg0);
		if (null != praiseBean && 102 == praiseBean.type && null != praiseBean.content && null != praiseBean.sender) {
			convertView.setVisibility(View.VISIBLE);
			viewHolder.nTextName.setText(praiseBean.sender.name);
			viewHolder.nTextTime.setText(GolukUtils.getCommentShowFormatTime(mContext, praiseBean.content.ts));
			String netUrlHead = praiseBean.sender.customavatar;
			if (null != netUrlHead && !"".equals(netUrlHead)) {
				GlideUtils.loadNetHead(mContext, viewHolder.nImageHead, netUrlHead, R.drawable.my_head_moren7);
			} else {
				UserUtils.focusHead(mContext, praiseBean.sender.avatar, viewHolder.nImageHead);
			}
			if (null != praiseBean.sender.label) {
				String approvelabel = praiseBean.sender.label.approvelabel;
				String headplusv = praiseBean.sender.label.headplusv;
				String tarento = praiseBean.sender.label.tarento;
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
			}
			GlideUtils.loadImage(mContext, viewHolder.nImageThumbnail, praiseBean.content.picture,
					R.drawable.tacitly_pic);
			// 点击事件
			viewHolder.nImageHead.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					intentToOthers(arg0.getId(), praiseBean);
				}
			});
			viewHolder.nTextName.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					intentToOthers(arg0.getId(), praiseBean);
				}
			});
			viewHolder.nImageThumbnail.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					intentToOthers(arg0.getId(), praiseBean);
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
		TextView nTextName;
		TextView nTextTime;
		ImageView nImageThumbnail;
	}

	/**
	 * 页面跳转
	 * 
	 * @param id
	 * @param bean
	 */
	private void intentToOthers(int id, MessageMsgsBean bean) {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unavailable));
			return;
		}
		if (id == R.id.iv_listview_item_praise_head || id == R.id.tv_listview_item_praise_name) {
			if (null != bean && null != bean.sender) {
				MessageSenderBean sender = bean.sender;
				GolukUtils.startUserCenterActivity(mContext,sender.uid);
			}
		} else {
			if (null != bean && null != bean.content) {
				//视频详情页访问
				ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_msg_center));
				Intent itVideoDetail = new Intent(mContext, VideoDetailActivity.class);
				itVideoDetail.putExtra(VideoDetailActivity.VIDEO_ID, bean.content.access);
				mContext.startActivity(itVideoDetail);
			}
		}
	}

}
