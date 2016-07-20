package com.mobnote.golukmain.followed;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.followed.bean.FollowedVideoObjectBean;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.util.ZhugeUtils;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class VideoItemCommentClickListener implements OnClickListener {
	private FollowedVideoObjectBean mVideoSquareInfo;
	private Context mContext;

	public VideoItemCommentClickListener(Context context, FollowedVideoObjectBean info, boolean showft) {
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

		//视频详情页访问
		ZhugeUtils.eventVideoDetail(mContext, mContext.getString(R.string.str_zhuge_follow));
		Intent intent = new Intent(mContext, VideoDetailActivity.class);
		intent.putExtra(VideoDetailActivity.VIDEO_ID, mVideoSquareInfo.video.videoid);
		boolean iscomment = false;
		if ("1".equals(mVideoSquareInfo.video.comment.iscomment)) {
			iscomment = true;
		}
		intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, iscomment);

		mContext.startActivity(intent);
	}

}
