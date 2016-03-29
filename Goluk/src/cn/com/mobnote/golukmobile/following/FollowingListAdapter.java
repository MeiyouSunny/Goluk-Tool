package cn.com.mobnote.golukmobile.following;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.following.bean.FollowingItemBean;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;

public class FollowingListAdapter extends BaseAdapter{
	
	List<FollowingItemBean> mFollowingList;
	FollowingListActivity mFollowingActivity;
	private final static String TAG = "FollowingListAdapter";
	
	public FollowingListAdapter(Activity activity,List<FollowingItemBean> list) {
		super();
		this.mFollowingActivity = (FollowingListActivity) activity;
		this.mFollowingList = list;
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return null == mFollowingList?0:mFollowingList.size();
	}
	
	public void setData(List<FollowingItemBean> list) {
		this.mFollowingList = list;
		notifyDataSetChanged();
	}

	public void appendData(List<FollowingItemBean> list) {
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

		FollowingItemBean followingItemBean = (FollowingItemBean)mFollowingList.get(position);
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
		//holderFollowing.tvFollowinglistShareFollowedAndFans.setText(Html.fromHtml("<div><div>&followingItemBean.share</div><div>当前分类内容列表...</div></div>"));
		String shareFolowingAndFans = followingItemBean.share 
				+ mFollowingActivity.getString(R.string.share_text) 
				+ followingItemBean.following 
				+ mFollowingActivity.getString(R.string.str_follow) 
				+ followingItemBean.fans 
				+ mFollowingActivity.getString(R.string.str_fans);
		
		holderFollowing.tvFollowinglistShareFollowedAndFans.setText(shareFolowingAndFans);
		
		//设置连接状态图片及文字
		holderFollowing.tvFollowinglistLink.setVisibility(View.VISIBLE);
		if(followingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY){
			holderFollowing.llFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_followed);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_usercenter_header_attention_already_text);
			holderFollowing.ivFollowinglistLink.setImageResource(R.drawable.icon_followed);
//			Drawable drawableFollowed = mFollowingActivity.getResources().getDrawable(R.drawable.icon_followed);
//			drawableFollowed.setBounds(0, 0, drawableFollowed.getMinimumWidth(), drawableFollowed.getMinimumHeight());
//			holderFollowing.tvFollowinglistLink.setCompoundDrawables(drawableFollowed, null, null, null);
		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_FAN_ONLY){
			holderFollowing.llFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_normal);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_follow);
			holderFollowing.ivFollowinglistLink.setImageResource(R.drawable.icon_follow_normal);
//			Drawable drawableFollowNormal = mFollowingActivity.getResources().getDrawable(R.drawable.icon_follow_normal);
//			drawableFollowNormal.setBounds(0, 0, drawableFollowNormal.getMinimumWidth(), drawableFollowNormal.getMinimumHeight());
//			holderFollowing.tvFollowinglistLink.setCompoundDrawables(drawableFollowNormal, null, null, null);
		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
			holderFollowing.llFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_mutual);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_usercenter_header_attention_each_other_text);
			holderFollowing.ivFollowinglistLink.setImageResource(R.drawable.icon_follow_mutual);
//			Drawable drawableFollowMutual = mFollowingActivity.getResources().getDrawable(R.drawable.icon_follow_mutual);
//			drawableFollowMutual.setBounds(0, 0, drawableFollowMutual.getMinimumWidth(), drawableFollowMutual.getMinimumHeight());
//			holderFollowing.tvFollowinglistLink.setCompoundDrawables(drawableFollowMutual, null, null, null);
		}else{
			holderFollowing.llFollowinglistLink.setVisibility(View.GONE);
		}
		
		initFollowingItemListener(position, holderFollowing);
		return convertView;
	}
	
	private void initFollowingItemListener(final int index, VHFollowing viewHolder) {
		
		if (index < 0 || index >= mFollowingList.size()) {
			return;
		}

		final FollowingItemBean tempFollowingItemBean = mFollowingList.get(index);
		
		viewHolder.tvFollowinglistNickname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
