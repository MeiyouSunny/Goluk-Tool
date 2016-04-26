package com.mobnote.golukmain.search;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.following.FollowingConfig;
import com.mobnote.golukmain.following.bean.FollowingItemBean;
import com.mobnote.golukmain.search.bean.SearchListBean;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

public class SearchListAdapter extends BaseAdapter{

	List<SearchListBean> mResultList;
	SearchUserAcivity mSearchActivity;
	private final static String TAG = "SearchUserListAdapter";

	public SearchListAdapter(Activity activity,List<SearchListBean> list) {
		super();
		this.mSearchActivity = (SearchUserAcivity) activity;
		this.mResultList = list;
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return null == mResultList?0:mResultList.size();
	}

	public void setData(List<SearchListBean> list) {
		this.mResultList = list;
		notifyDataSetChanged();
	}

	public void appendData(List<SearchListBean> list) {
		mResultList.addAll(list);
		this.notifyDataSetChanged();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (null == mResultList || position < 0 || position > mResultList.size() - 1) {
			return null;
		}
		return mResultList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		VHUser holderFollowing = null;
		if (null == convertView) {
			holderFollowing = new VHUser();
			convertView = LayoutInflater.from(mSearchActivity).inflate(R.layout.search_list_item, null);
			holderFollowing.ivUserlistAvatar = (ImageView) convertView.findViewById(R.id.iv_userlist_avatar);
			holderFollowing.tvUserlistNickname = (TextView) convertView.findViewById(R.id.tv_userlist_nickname);
			holderFollowing.tvUserlistShareFollowedAndFans = (TextView) convertView.findViewById(R.id.tv_userlist_share_followed_fans);
			holderFollowing.tvUserlistLink = (TextView) convertView.findViewById(R.id.tv_userlist_link);
			holderFollowing.llUserlistLink = (LinearLayout)convertView.findViewById(R.id.ll_userlist_link);
			holderFollowing.ivUserlistLink = (ImageView) convertView.findViewById(R.id.iv_userlist_link);
			convertView.setTag(holderFollowing);
		} else {
			holderFollowing = (VHUser)convertView.getTag();
		}

		if (position < 0 || position >= mResultList.size()) {
			return convertView;
		}

		FollowingItemBean followingItemBean = (FollowingItemBean)(mResultList.get(position).getUserItemBean());
		if(null == followingItemBean) {
			return convertView;
		}

		// 设置头像
		String netHeadUrl = followingItemBean.customavatar;
		if (null != netHeadUrl && !"".equals(netHeadUrl)) {
			// 使用网络地址
			GlideUtils.loadNetHead(mSearchActivity, holderFollowing.ivUserlistAvatar, netHeadUrl, R.drawable.head_unknown);
		} else {
			// 使用本地头像
			GlideUtils.loadLocalHead(mSearchActivity, holderFollowing.ivUserlistAvatar, UserUtils.getUserHeadImageResourceId(followingItemBean.avatar));
		}

		//设置昵称
		String nickname = followingItemBean.nickname;
		if(null == nickname || "".equals(nickname)){
			holderFollowing.tvUserlistNickname.setText("");
		}else{
			holderFollowing.tvUserlistNickname.setText(nickname);
		}

		//设置分享/关注和粉丝数量
		String shareCount = GolukUtils.getFormatNumber(followingItemBean.share);
		String followCount = GolukUtils.getFormatNumber(followingItemBean.following);
		String fansCount = GolukUtils.getFormatNumber(followingItemBean.fans);

		String shareFolowingAndFans = "<font color='#0080ff'>" + shareCount + "</font>"
			   + "<font color='#808080'>"  + " " + mSearchActivity.getResources().getString(R.string.share_text).toString() + " / " + "</font>"
			   + "<font color='#0080ff'>" + followCount + "</font>"
			   + "<font color='#808080'>" + " " + mSearchActivity.getResources().getString(R.string.str_follow).toString() + " / " + "</font>"
			   + "<font color='#0080ff'>" + fansCount + "</font>"
			   + "<font color='#808080'>" + " " + mSearchActivity.getResources().getString(R.string.str_fans).toString() + "</font>";

		holderFollowing.tvUserlistShareFollowedAndFans.setText(Html.fromHtml(shareFolowingAndFans));

		//设置连接状态图片及文字

		if(followingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY){
			holderFollowing.llUserlistLink.setVisibility(View.VISIBLE);
			holderFollowing.llUserlistLink.setBackgroundResource(R.drawable.follow_button_border_followed);
			holderFollowing.tvUserlistLink.setText(R.string.str_usercenter_header_attention_already_text);
			holderFollowing.tvUserlistLink.setTextColor(mSearchActivity.getResources().getColor(R.color.white));
			holderFollowing.ivUserlistLink.setImageResource(R.drawable.icon_followed);

		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_FAN_ONLY){
			holderFollowing.llUserlistLink.setVisibility(View.VISIBLE);
			holderFollowing.llUserlistLink.setBackgroundResource(R.drawable.follow_button_border_normal);
			holderFollowing.tvUserlistLink.setText(R.string.str_follow);
			holderFollowing.tvUserlistLink.setTextColor(Color.parseColor("#0080ff"));
			holderFollowing.ivUserlistLink.setImageResource(R.drawable.icon_follow_normal);

		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
			holderFollowing.llUserlistLink.setVisibility(View.VISIBLE);
			holderFollowing.llUserlistLink.setBackgroundResource(R.drawable.follow_button_border_mutual);
			holderFollowing.tvUserlistLink.setText(R.string.str_usercenter_header_attention_each_other_text);
			holderFollowing.tvUserlistLink.setTextColor(mSearchActivity.getResources().getColor(R.color.white));
			holderFollowing.ivUserlistLink.setImageResource(R.drawable.icon_follow_mutual);

		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_SELF){
			holderFollowing.llUserlistLink.setVisibility(View.GONE);
		}else{
			holderFollowing.llUserlistLink.setVisibility(View.VISIBLE);
			holderFollowing.llUserlistLink.setBackgroundResource(R.drawable.follow_button_border_normal);
			holderFollowing.tvUserlistLink.setText(R.string.str_follow);
			holderFollowing.tvUserlistLink.setTextColor(Color.parseColor("#0080ff"));
			holderFollowing.ivUserlistLink.setImageResource(R.drawable.icon_follow_normal);
		}

		initFollowingItemListener(position, holderFollowing);
		return convertView;
	}

	private void initFollowingItemListener(final int index, VHUser viewHolder) {

		if (index < 0 || index >= mResultList.size()) {
			return;
		}

		viewHolder.tvUserlistNickname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FollowingItemBean tempFollowingItemBean = mResultList.get(index).getUserItemBean();
				GolukUtils.startUserCenterActivity(mSearchActivity, 
						tempFollowingItemBean.uid, 
						tempFollowingItemBean.nickname,
						tempFollowingItemBean.avatar, 
						tempFollowingItemBean.customavatar,
						tempFollowingItemBean.sex, 
						tempFollowingItemBean.introduction);
			}
		});

		viewHolder.ivUserlistAvatar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FollowingItemBean tempFollowingItemBean = mResultList.get(index).getUserItemBean();
				GolukUtils.startUserCenterActivity(mSearchActivity, 
						tempFollowingItemBean.uid, 
						tempFollowingItemBean.nickname,
						tempFollowingItemBean.avatar, 
						tempFollowingItemBean.customavatar,
						tempFollowingItemBean.sex, 
						tempFollowingItemBean.introduction);
			}
		});

		viewHolder.tvUserlistShareFollowedAndFans.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FollowingItemBean tempFollowingItemBean = mResultList.get(index).getUserItemBean();
				GolukUtils.startUserCenterActivity(mSearchActivity, 
						tempFollowingItemBean.uid, 
						tempFollowingItemBean.nickname,
						tempFollowingItemBean.avatar, 
						tempFollowingItemBean.customavatar,
						tempFollowingItemBean.sex, 
						tempFollowingItemBean.introduction);
			}
		});

		viewHolder.llUserlistLink.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FollowingItemBean tempUserItemBean = mResultList.get(index).getUserItemBean();
				if(tempUserItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY||
						tempUserItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
					mSearchActivity.follow(tempUserItemBean.uid, "0");
				}else{
					mSearchActivity.follow(tempUserItemBean.uid, "1");
				}
			}
		});
	}


	static class VHUser{
		ImageView ivUserlistAvatar;
		TextView tvUserlistNickname;
		TextView tvUserlistShareFollowedAndFans;//分享/关注和赞

		LinearLayout llUserlistLink;
		ImageView ivUserlistLink;
		TextView tvUserlistLink;//连接状态

	}
}
