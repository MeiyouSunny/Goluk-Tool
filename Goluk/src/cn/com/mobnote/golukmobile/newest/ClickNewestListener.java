package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.VideoSquareDeatilActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickNewestListener implements OnClickListener{
	private VideoSquareInfo mVideoSquareInfo;
	private Context mContext;
	
	public ClickNewestListener(Context context, VideoSquareInfo info) {
		this.mVideoSquareInfo = info;
		this.mContext = context;
	}

	@Override
	public void onClick(View arg0) {
//		Intent intent = new Intent(mContext, VideoSquareDeatilActivity.class);
//		intent.putExtra("ztid", mVideoSquareInfo.);
//		mContext.startActivity(intent);
	}
	
}
