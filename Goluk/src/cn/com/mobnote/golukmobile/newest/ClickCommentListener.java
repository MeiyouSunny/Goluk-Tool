package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.comment.CommentActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickCommentListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	private boolean showft = false;
	
	public ClickCommentListener(Context context, VideoSquareInfo info, boolean showft) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
		this.showft = showft;
	}
	
	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(mContext, CommentActivity.class);
		intent.putExtra(CommentActivity.COMMENT_KEY_MID, mVideoSquareInfo.mVideoEntity.videoid);
		intent.putExtra(CommentActivity.COMMENT_KEY_TYPE, "1");
		intent.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, showft);
		boolean iscomment = false;
		if ("1".equals(mVideoSquareInfo.mVideoEntity.iscomment)) {
			iscomment = true;
		}
		intent.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, iscomment);
		
		mContext.startActivity(intent);
	}
	
}
