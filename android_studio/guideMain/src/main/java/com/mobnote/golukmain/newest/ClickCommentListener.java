package com.mobnote.golukmain.newest;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;

public class ClickCommentListener implements OnClickListener {
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private boolean showft = false;
	private String mSource = "";

	public ClickCommentListener(Context context, VideoSquareInfo info, boolean showft, String source) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		this.showft = showft;
		this.mSource = source;
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
	}

}
