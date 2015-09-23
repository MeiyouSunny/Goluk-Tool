package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import cn.com.tiros.debug.GolukDebugUtils;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
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
		mHolder.mPlayBtn.setVisibility(View.VISIBLE);
		mHolder.mPlayBtn.setImageResource(R.drawable.btn_player_play);
		mHolder.mImageLayout.setVisibility(View.VISIBLE);
		mHolder.mVideoView.seekTo(0);
		mHolder.mSeekBar.setProgress(0);
	}

}
