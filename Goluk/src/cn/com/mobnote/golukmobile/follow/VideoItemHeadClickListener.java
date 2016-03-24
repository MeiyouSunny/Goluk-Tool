package cn.com.mobnote.golukmobile.follow;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.follow.bean.FollowVideoObjectBean;
import cn.com.mobnote.golukmobile.usercenter.NewUserCenterActivity;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;

public class VideoItemHeadClickListener implements OnClickListener {
	private FollowVideoObjectBean mVideoSquareInfo;
	private Context mContext;

	public VideoItemHeadClickListener(Context context, FollowVideoObjectBean info) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		// 防止重复点击
		if (null != mContext && mContext instanceof BaseActivity) {
			if (!((BaseActivity) mContext).isAllowedClicked()) {
				return;
			}
			((BaseActivity) mContext).setJumpToNext();
		}

//		Intent intent = new Intent(mContext, UserCenterActivity.class);
		Intent intent = new Intent(mContext, NewUserCenterActivity.class);
		intent.putExtra("userinfo", getUserInfo());
		mContext.startActivity(intent);
	}

	public UCUserInfo getUserInfo() {
		if (null == mVideoSquareInfo) {
			return null;
		}
		UCUserInfo userInfo = new UCUserInfo();
		userInfo.uid = mVideoSquareInfo.user.uid;
		userInfo.nickname = mVideoSquareInfo.user.nickname;
		userInfo.headportrait = mVideoSquareInfo.user.headportrait;
		userInfo.introduce = "";
		userInfo.sex = mVideoSquareInfo.user.sex;
		userInfo.customavatar = mVideoSquareInfo.user.customavatar;

		return userInfo;
	}

}
