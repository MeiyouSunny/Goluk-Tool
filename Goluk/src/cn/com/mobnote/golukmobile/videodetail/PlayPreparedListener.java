package cn.com.mobnote.golukmobile.videodetail;

import java.util.Timer;
import java.util.TimerTask;

import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import cn.com.tiros.debug.GolukDebugUtils;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;

public class PlayPreparedListener implements OnPreparedListener {

	private ViewHolder holder;
	private VideoDetailAdapter mAdapter;

	public PlayPreparedListener(ViewHolder holder,VideoDetailAdapter adapter) {
		this.holder = holder;
		this.mAdapter = adapter;
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO OnPreparedListener 视频播放之前的一个视频准备工作，准备完成后调用此方法
		if (null == holder.mVideoView) {
			return;
		}
		GolukDebugUtils.e("videostate", "VideoDetailActivity-------------------------onPrepared :  ");
		holder.mVideoView.setVideoWidth(mp.getVideoWidth());
		holder.mVideoView.setVideoHeight(mp.getVideoHeight());
		if ((null != mAdapter.netInfo) && (mAdapter.netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
			mp.setLooping(true);
		}
		if (mAdapter.playTime != 0) {
			holder.mVideoView.seekTo(mAdapter.playTime);
		}
		mAdapter.timer = new Timer();
		mAdapter.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				mAdapter.mHandler.sendEmptyMessage(1);
			}
		}, 0, 1000);
	}

}
