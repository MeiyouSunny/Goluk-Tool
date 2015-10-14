package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class ClickVideoListener implements OnClickListener {

	private Context mContext ;
	private VideoDetailAdapter mAdapter;
	
	public ClickVideoListener(Context context,VideoDetailAdapter adapter) {
		this.mContext = context;
		this.mAdapter = adapter;
	}
	
	@Override
	public void onClick(View view) {
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			GolukUtils.showToast(mContext, mContext.getResources().getString(R.string.user_net_unavailable));
			return;
		}
		if (mAdapter.isBuffering) {
			return;
		}
		if (mAdapter.headHolder.mVideoView.isPlaying()) {
			mAdapter.headHolder.mVideoView.pause();
			mAdapter.isPause = true;
			mAdapter.headHolder.mPlayBtn.setVisibility(View.VISIBLE);
		} else {
			mAdapter.playVideo();
			mAdapter.headHolder.mVideoView.start();
			mAdapter.showLoading();
			GolukDebugUtils.e("", "VideoDetailActivity-------------------------onClick  showLoading");
			mAdapter.headHolder.mPlayBtn.setVisibility(View.GONE);
		}
	}

}
