package cn.com.mobnote.golukmobile.following;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.following.bean.FollowingItemBean;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;

public class FollowingListAdapter extends BaseAdapter{
	
	List<FollowingItemBean> mFollowingList;
	Context mContext;
	private final static String TAG = "FollowingListAdapter";
	
	public FollowingListAdapter(Context cxt,List<FollowingItemBean> list) {
		super();
		this.mContext = cxt;
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.following_list_item, null);
			holderFollowing.ivFollowinglistAvatar = (ImageView) convertView.findViewById(R.id.iv_followinglist_avatar);
			holderFollowing.tvFollowinglistNickname = (TextView) convertView.findViewById(R.id.tv_followinglist_nickname);
			holderFollowing.tvFollowinglistShareFollowedAndFans = (TextView) convertView.findViewById(R.id.tv_followinglist_share_followed_fans);
			holderFollowing.tvFollowinglistLink = (TextView) convertView.findViewById(R.id.tv_followinglist_link);

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
			GlideUtils.loadNetHead(mContext, holderFollowing.ivFollowinglistAvatar, netHeadUrl, R.drawable.head_unknown);
		} else {
			// 使用本地头像
			GlideUtils.loadLocalHead(mContext, holderFollowing.ivFollowinglistAvatar, UserUtils.getUserHeadImageResourceId(followingItemBean.avatar));
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
				+ mContext.getString(R.string.share_text) 
				+ followingItemBean.following 
				+ mContext.getString(R.string.str_follow) 
				+ followingItemBean.fans 
				+ mContext.getString(R.string.str_fans);
		
		holderFollowing.tvFollowinglistShareFollowedAndFans.setText(shareFolowingAndFans);
		
		//设置连接状态图片及文字
		holderFollowing.tvFollowinglistLink.setVisibility(View.VISIBLE);
		if(followingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_ONLY){
			holderFollowing.tvFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_followed);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_usercenter_header_attention_already_text);
		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_FAN_ONLY){
			holderFollowing.tvFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_normal);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_follow);
		}else if(followingItemBean.link == FollowingConfig.LINK_TYPE_FOLLOW_EACHOTHER){
			holderFollowing.tvFollowinglistLink.setBackgroundResource(R.drawable.follow_button_border_mutual);
			holderFollowing.tvFollowinglistLink.setText(R.string.str_usercenter_header_attention_each_other_text);
		}else{
			holderFollowing.tvFollowinglistLink.setVisibility(View.GONE);
		}
		
		//initFollowedListener(position, holderFollow);
		return convertView;
	}
	
	
	static class VHFollowing{
		ImageView ivFollowinglistAvatar;
		TextView tvFollowinglistNickname;
		TextView tvFollowinglistShareFollowedAndFans;//分享/关注和赞
		TextView tvFollowinglistLink;//连接状态
	}

}
