package com.mobnote.golukmain.newest;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.usercenter.NewUserCenterActivity;
import com.mobnote.golukmain.usercenter.UCUserInfo;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GolukUtils;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 点击用户头像 ，跳转“个人中心”
 * 
 * @author jyf
 */
public class ClickHeadListener implements OnClickListener {
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;

	public ClickHeadListener(Context context, VideoSquareInfo info) {
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
		userInfo.uid = mVideoSquareInfo.mUserEntity.uid;
		userInfo.nickname = mVideoSquareInfo.mUserEntity.nickname;
		userInfo.headportrait = mVideoSquareInfo.mUserEntity.headportrait;
		userInfo.introduce = "";
		userInfo.sex = mVideoSquareInfo.mUserEntity.sex;
		userInfo.customavatar = mVideoSquareInfo.mUserEntity.mCustomAvatar;

		return userInfo;
	}

}
