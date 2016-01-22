package cn.com.mobnote.golukmobile.newest;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.VideoCategoryActivity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickLiveListener implements OnClickListener{
	private Context mContext;

	public ClickLiveListener(Context context) {
		this.mContext = context;
	}
	
	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(mContext, VideoCategoryActivity.class);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TYPE, VideoCategoryActivity.CATEGORY_TYPE_LIVE);
		// 此处attribute一定要写 0 ,　否则直播查不出来
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_ATTRIBUTE, VideoCategoryActivity.LIVE_ATTRIBUTE_VALUE);
		intent.putExtra(VideoCategoryActivity.KEY_VIDEO_CATEGORY_TITLE, mContext.getString(R.string.video_square_text));
		mContext.startActivity(intent);
	}

}
