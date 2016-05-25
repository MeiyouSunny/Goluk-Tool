package com.mobnote.golukmain.followed;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.followed.bean.FollowedVideoObjectBean;
import com.mobnote.golukmain.usercenter.NewUserCenterActivity;
import com.mobnote.golukmain.usercenter.UCUserInfo;
import com.mobnote.util.GolukUtils;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class VideoItemHeadClickListener implements OnClickListener {
	private FollowedVideoObjectBean mVideoSquareInfo;
	private Context mContext;

	public VideoItemHeadClickListener(Context context, FollowedVideoObjectBean info) {
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
		GolukUtils.startUserCenterActivity(mContext,mVideoSquareInfo.user.uid);
	}

}
