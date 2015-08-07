package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickCommentListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	
	public ClickCommentListener(Context context, VideoSquareInfo info) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
//		Intent intent = new Intent(mContext, CommentActivity.class);
//		intent.putExtra("from", "suqare");
//		mContext.startActivity(intent);
	}
	
}
