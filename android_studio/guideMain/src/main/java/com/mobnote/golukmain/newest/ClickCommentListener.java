package com.mobnote.golukmain.newest;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.ZhugeUtils;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

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

		//视频详情页访问
		ZhugeUtils.eventVideoDetail(mContext, mSource);
		Intent intent = new Intent(mContext, VideoDetailActivity.class);
		intent.putExtra(VideoDetailActivity.VIDEO_ID, mVideoSquareInfo.mVideoEntity.videoid);
		boolean iscomment = false;
		if ("1".equals(mVideoSquareInfo.mVideoEntity.iscomment)) {
			iscomment = true;
		}
		intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, iscomment);

		mContext.startActivity(intent);
	}

}
