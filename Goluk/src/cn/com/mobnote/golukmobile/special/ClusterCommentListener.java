package cn.com.mobnote.golukmobile.special;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.comment.CommentActivity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClusterCommentListener implements OnClickListener{
	private ClusterInfo clusterInfo;
	private Context mContext;
	private boolean showft = false;
	
	public ClusterCommentListener(Context context, ClusterInfo info, boolean showft) {
		this.clusterInfo = info;
		this.mContext = context;
		this.showft = showft;
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.commentLayout:
		case R.id.totalcomments:
			Intent intent = new Intent(mContext, CommentActivity.class);
			intent.putExtra(CommentActivity.COMMENT_KEY_MID, clusterInfo.videoid);
			intent.putExtra(CommentActivity.COMMENT_KEY_TYPE, "1");
			intent.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, showft);
			boolean iscomment = false;
			if ("1".equals(clusterInfo.iscomment)) {
				iscomment = true;
			}
			intent.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, iscomment);
			intent.putExtra(CommentActivity.COMMENT_KEY_USERID, clusterInfo.uid);
			
			mContext.startActivity(intent);
			break;
		default:
			break;
		}
	}
	
}
