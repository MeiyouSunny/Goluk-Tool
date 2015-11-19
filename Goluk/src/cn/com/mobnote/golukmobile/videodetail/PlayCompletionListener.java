package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.view.View;

public class PlayCompletionListener implements OnCompletionListener {

	private VideoDetailAdapter mAdapter;
	private ViewHolder mHolder;
	
	public PlayCompletionListener(VideoDetailAdapter adapter,ViewHolder holder) {
		this.mAdapter = adapter;
		this.mHolder = holder;
	}
	
	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO OnCompletionListener视频播放完后进度条回到初始位置
		GolukDebugUtils.e("videostate", "VideoDetailActivity-------------------------onCompletion :  ");
		if (mAdapter.error || null == mHolder.mVideoView) {
			return;
		}
		mAdapter.connectivityManager = (ConnectivityManager) mAdapter.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mAdapter.netInfo = mAdapter.connectivityManager.getActiveNetworkInfo();
		if ((null != mAdapter.netInfo) && (mAdapter.netInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
			arg0.setLooping(false);
			mHolder.mPlayBtn.setVisibility(View.VISIBLE);
			mHolder.mImageLayout.setVisibility(View.VISIBLE);
			mHolder.mVideoView.seekTo(0);
			mHolder.mSeekBar.setProgress(0);
		}else{
			try {
				mHolder.mVideoView.setVideoURI(mHolder.url);
				mHolder.mVideoView.start();
			} catch(IllegalStateException ise) {
				ise.printStackTrace();
			}
		}
	}

}
