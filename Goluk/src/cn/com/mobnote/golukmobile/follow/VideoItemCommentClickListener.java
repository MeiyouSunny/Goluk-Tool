package cn.com.mobnote.golukmobile.follow;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.follow.bean.FollowVideoObjectBean;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class VideoItemCommentClickListener implements OnClickListener {
	private FollowVideoObjectBean mVideoSquareInfo;
	private Context mContext;

	public VideoItemCommentClickListener(Context context, FollowVideoObjectBean info, boolean showft) {
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
