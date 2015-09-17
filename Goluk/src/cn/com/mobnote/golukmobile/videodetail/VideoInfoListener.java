package cn.com.mobnote.golukmobile.videodetail;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.view.View;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import cn.com.tiros.debug.GolukDebugUtils;

public class VideoInfoListener implements OnInfoListener {

	private Context mContext ;
	private ViewHolder mHolder;
	private boolean mBuffering;
	private boolean isShow;
	
	public VideoInfoListener(Context context,ViewHolder holder,boolean buffering,boolean show){
		this.mContext = context;
		this.mHolder = holder;
		this.mBuffering = buffering;
		this.isShow = show;
	}
	
	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		// TODO onInfoListener有警告或者错误信息时调用（开始缓冲、缓冲结束）
				GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onInfo : arg1 " + arg1);
				switch (arg1) {
				case 3:
					callBack_realStart();
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_START:
					mBuffering = true;
					 if (0 == mHolder.mVideoView.getCurrentPosition()) {
						 mHolder.mImageLayout.setVisibility(View.VISIBLE);
					 }
					showLoading();
					GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onInfo  showLoading");
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END:
					mBuffering = false;
					hideLoading();
					GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onInfo : hideLoading ");
					break;
				default:
					break;
				}
				return true;
	}
	
	private void callBack_realStart() {
		mHolder.mPlayBtn.setVisibility(View.GONE);
		mHolder.mImageLayout.setVisibility(View.GONE);
		hideLoading();
		GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------callBack_realStart : hideLoading ");
	}
	
	/**
	 * 隐藏加载中显示画面
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void hideLoading() {
		if (isShow) {
			isShow = false;
			mHolder.mImageLayout.setVisibility(View.GONE);
			if (mHolder.mAnimationDrawable != null) {
				if (mHolder.mAnimationDrawable.isRunning()) {
					mHolder.mAnimationDrawable.stop();
				}
			}
			mHolder.mVideoLoading.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 显示加载中布局
	 */
	private void showLoading() {
		GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------showLoading : isShow " + isShow);
		if (!isShow) {
			isShow = true;
			mHolder.mVideoLoading.setVisibility(View.VISIBLE);
			mHolder.mLoading.setVisibility(View.VISIBLE);
			mHolder.mLoading.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mHolder.mAnimationDrawable != null) {
						if (!mHolder.mAnimationDrawable.isRunning()) {
							mHolder.mAnimationDrawable.start();
						}
					}
				}
			}, 100);
		}
	}

}
