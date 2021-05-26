package com.mobnote.golukmain.newest;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GolukUtils;

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
				((BaseActivity) mContext).setJumpToNext();
				return;
			}
		}

		if (null == mVideoSquareInfo) {
			return;
		}
		GolukUtils.startUserCenterActivity(mContext,mVideoSquareInfo.mUserEntity.uid);
	}
}
