package cn.com.mobnote.golukmobile.videodetail;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.view.View;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import cn.com.tiros.debug.GolukDebugUtils;

public class PlayInfoListener implements OnInfoListener {

	private VideoDetailAdapter mAdapter;
	private ViewHolder mHolder;

	public PlayInfoListener(VideoDetailAdapter adapter, ViewHolder holder) {
		this.mAdapter = adapter;
		this.mHolder = holder;
	}

	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		// TODO onInfoListener有警告或者错误信息时调用（开始缓冲、缓冲结束）
		GolukDebugUtils.e("videostate", "VideoDetailActivity-----------FullVideoView--------------onInfo : arg1 " + arg1);
		switch (arg1) {
		case 3:
			callBack_realStart();
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			mAdapter.isBuffering = true;
			if (0 == mHolder.mVideoView.getCurrentPosition()) {
				mHolder.mImageLayout.setVisibility(View.VISIBLE);
			}
			mAdapter.showLoading();
			GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------onInfo  showLoading");
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			mAdapter.isBuffering = false;
			mAdapter.hideLoading();
			GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------onInfo : hideLoading ");
			break;
		default:
			break;
		}
		return true;
	}

	private void callBack_realStart() {
		mHolder.mPlayBtn.setVisibility(View.GONE);
		mHolder.mImageLayout.setVisibility(View.GONE);
		mAdapter.hideLoading();
		GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------callBack_realStart : hideLoading ");
	}

}
