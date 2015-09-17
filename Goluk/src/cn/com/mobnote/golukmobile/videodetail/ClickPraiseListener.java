package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.comment.CommentActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickPraiseListener implements OnClickListener {

	private VideoJson mVideoInfo;
	private Context mContext;
	private boolean showft = false;
	
	public ClickPraiseListener(VideoJson mVideoInfo,Context mContext,boolean showft) {
		this.mVideoInfo = mVideoInfo;
		this.mContext = mContext;
		this.showft = showft;
	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.play_btn:
			
			break;
		case R.id.video_detail_link:
			
			break;
		case R.id.praiseLayout:
			// 防止重复点击
			if (null != mContext && mContext instanceof BaseActivity) {
				if (!((BaseActivity) mContext).isAllowedClicked()) {
					return;
				}
				((BaseActivity) mContext).setJumpToNext();
			}
			Intent intent = new Intent(mContext, CommentActivity.class);
			intent.putExtra(CommentActivity.COMMENT_KEY_MID, mVideoInfo.data.avideo.video.videoid);
			intent.putExtra(CommentActivity.COMMENT_KEY_TYPE, "1");
			intent.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, showft);
			intent.putExtra(CommentActivity.COMMENT_KEY_USERID, mVideoInfo.data.avideo.user.uid);
			boolean iscomment = false;
			if ("1".equals(mVideoInfo.data.avideo.video.comment.iscomment)) {
				iscomment = true;
			}
			intent.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, iscomment);

			mContext.startActivity(intent);
			break;
		case R.id.shareLayout:
			
			break;

		default:
			break;
		}
	}

}
