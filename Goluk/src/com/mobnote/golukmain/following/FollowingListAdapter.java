package com.mobnote.golukmain.following;

import java.util.List;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R.color;
import com.mobnote.golukmain.userbase.bean.SimpleUserItemBean;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FollowingListAdapter extends BaseAdapter{

	List<SimpleUserItemBean> mFollowingList;
	FollowingListActivity mFollowingActivity;
	private final static String TAG = "FollowingListAdapter";

	public FollowingListAdapter(Activity activity,List<SimpleUserItemBean> list) {
		super();
		this.mFollowingActivity = (FollowingListActivity) activity;
		this.mFollowingList = list;
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return null == mFollowingList?0:mFollowingList.size();
	}

	public void setData(List<SimpleUserItemBean> list) {
		this.mFollowingList = list;
		notifyDataSetChanged();
	}

	public void appendData(List<SimpleUserItemBean> list) {
		mFollowingList.addAll(list);
		this.notifyDataSetChanged();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (null == mFollowingList || position < 0 || position > mFollowingList.size() - 1) {
			return null;
		}
		return mFollowingList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		VHFollowing holderFollowing = null;
		if (null == convertView) {
			holderFollowing = new VHFollowing();
			convertView = LayoutInflater.from(mFollowingActivity).inflate(R.layout.following_list_item, null);
			holderFollowing.ivFollowinglistAvatar = (ImageView) convertView.findViewById(R.id.iv_followinglist_avatar);
			holderFollowing.tvFollowinglistNickname = (TextView) convertView.findViewById(R.id.tv_followinglist_nickname);
			holderFollowing.tvFollowinglistShareFollowedAndFans = (TextView) convertView.findViewById(R.id.tv_followinglist_share_followed_fans);
			holderFollowing.tvFollowinglistLink = (TextView) convertView.findViewById(R.id.tv_followinglist_link);
			holderFollowing.llFollowinglistLink = (LinearLayout)convertView.findViewById(R.id.ll_followinglist_link);
			holderFollowing.ivFollowinglistLink = (ImageView) convertView.findViewById(R.id.iv_followinglist_link);
			convertView.setTag(holderFollowing);
		} else {
			holderFollowing = (VHFollowing)convertView.getTag();
		}

		if (position < 0 || position >= mFollowingList.size()) {
			return convertView;
		}

		SimpleUserItemBean followingItemBean = (SimpleUserItemBean)mFollowingList.get(position);
		if(null == followingItemBean) {
			return convertView;
		}

		// 设置头像
		String netHeadUrl = followingItemBean.customavatar;
		if (null != netHeadUrl && !"".equals(netHeadUrl)) {
			// 使用网络地址
			GlideUtils.loadNetHead(mFollowingActivity, holderFollowing.ivFollowinglistAvatar, netHeadUrl, R.drawable.head_unknown);
		} else {
			// 使用本地头像
			GlideUtils.loadLocalHead(mFollowingActivity, holderFollowing.ivFollowinglistAvatar, UserUtils.getUserHeadImageResourceId(followingItemBean.avatar));
		}
		
		//设置昵称
		String nickname = followingItemBean.nickname;
		if(null == nickname || "".equals(nickname)){
			holderFollowing.tvFollowinglistNickname.setText("");
		}else{
			holderFollowing.tvFollowinglistNickname.setText(nickname);
		}

		//设置分享/关注和粉丝数量
		String shareCount = GolukUtils.getFormatNumber(followingItemBean.share);
		String followCount = GolukUtils.getFormatNumber(followingItemBean.following);
		String fansCount = GolukUtils.getFormatNumber(followingItemBean.fans);

		String shareFolowingAndFans = "<font color='#0080ff'>" + shareCount + "</font>"
			   + "<font color='#808080'>"  + " " + mFollowingActivity.getResources().getString(R.string.share_text).toString() + " / " + "</font>"
			   + "<font color='#0080ff'>" + followCount + "</font>"
			   + "<font color='#808080'>" + " " + mFollowingActivity.getResources().getString(R.string.str_follow).toString() + " / " + "</font>"
			   + "<font color='#0080ff'>" + fansCount + "</font>"
			   + "<font color='#808080'>" + " " + mFollowingActivity.getResources().getString(R.string.str_fans).toString() + "</font>";

		holderFollowing.tvFollowinglistShareFollowedAndFans.setText(Html.fromHtml(shareFolowingAndFans));

		//设置连接状态图片及文字

		if(followingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY){
			holderFollowing.llFollowinglistLink.setVisibility(View.VISIBLE);
			holderFollowing.llFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_followed);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_usercenter_header_attention_already_text);
			holderFollowing.tvFollowinglistLink.setTextColor(mFollowingActivity.getResources().getColor(R.color.white));
			holderFollowing.ivFollowinglistLink.setImageResource(R.drawable.icon_followed);

		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_FAN_ONLY){
			holderFollowing.llFollowinglistLink.setVisibility(View.VISIBLE);
			holderFollowing.llFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_normal);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_follow);
			holderFollowing.tvFollowinglistLink.setTextColor(Color.parseColor("#0080ff"));
			holderFollowing.ivFollowinglistLink.setImageResource(R.drawable.icon_follow_normal);

		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
			holderFollowing.llFollowinglistLink.setVisibility(View.VISIBLE);
			holderFollowing.llFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_mutual);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_usercenter_header_attention_each_other_text);
			holderFollowing.tvFollowinglistLink.setTextColor(mFollowingActivity.getResources().getColor(R.color.white));
			holderFollowing.ivFollowinglistLink.setImageResource(R.drawable.icon_follow_mutual);

		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_SELF){
			holderFollowing.llFollowinglistLink.setVisibility(View.GONE);
		}else{
			holderFollowing.llFollowinglistLink.setVisibility(View.VISIBLE);
			holderFollowing.llFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_normal);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_follow);
			holderFollowing.tvFollowinglistLink.setTextColor(Color.parseColor("#0080ff"));
			holderFollowing.ivFollowinglistLink.setImageResource(R.drawable.icon_follow_normal);
		}

		initFollowingItemListener(position, holderFollowing);
		return convertView;
	}

	private void initFollowingItemListener(final int index, VHFollowing viewHolder) {

		if (index < 0 || index >= mFollowingList.size()) {
			return;
		}

		viewHolder.tvFollowinglistNickname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SimpleUserItemBean tempFollowingItemBean = mFollowingList.get(index);
				GolukUtils.startUserCenterActivity(mFollowingActivity, 
						tempFollowingItemBean.uid, 
						tempFollowingItemBean.nickname,
						tempFollowingItemBean.avatar, 
						tempFollowingItemBean.customavatar,
						tempFollowingItemBean.sex, 
						tempFollowingItemBean.introduction);
			}
		});

		viewHolder.ivFollowinglistAvatar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SimpleUserItemBean tempFollowingItemBean = mFollowingList.get(index);
				GolukUtils.startUserCenterActivity(mFollowingActivity, 
						tempFollowingItemBean.uid, 
						tempFollowingItemBean.nickname,
						tempFollowingItemBean.avatar, 
						tempFollowingItemBean.customavatar,
						tempFollowingItemBean.sex, 
						tempFollowingItemBean.introduction);
			}
		});

		viewHolder.tvFollowinglistShareFollowedAndFans.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SimpleUserItemBean tempFollowingItemBean = mFollowingList.get(index);
				GolukUtils.startUserCenterActivity(mFollowingActivity, 
						tempFollowingItemBean.uid, 
						tempFollowingItemBean.nickname,
						tempFollowingItemBean.avatar, 
						tempFollowingItemBean.customavatar,
						tempFollowingItemBean.sex, 
						tempFollowingItemBean.introduction);
			}
		});

		viewHolder.llFollowinglistLink.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SimpleUserItemBean tempFollowingItemBean = mFollowingList.get(index);
				if(tempFollowingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY||
						tempFollowingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
					mFollowingActivity.follow(tempFollowingItemBean.uid, "0");
				}else{
					mFollowingActivity.follow(tempFollowingItemBean.uid, "1");
				}
				
			}
		});

	}

	static class VHFollowing{
		ImageView ivFollowinglistAvatar;
		TextView tvFollowinglistNickname;
		TextView tvFollowinglistShareFollowedAndFans;//分享/关注和赞
		
		LinearLayout llFollowinglistLink;
		ImageView ivFollowinglistLink;
		TextView tvFollowinglistLink;//连接状态

	}

}
