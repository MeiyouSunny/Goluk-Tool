package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import cn.com.mobnote.user.UserUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.view.View;

public class PlayErrorListener implements OnErrorListener {

	private Context mContext;
	private ViewHolder mHolder;
	private VideoDetailAdapter mAdapter;

	public PlayErrorListener(Context context, ViewHolder holder, VideoDetailAdapter mAdapter) {
		this.mContext = context;
		this.mHolder = holder;
		this.mAdapter = mAdapter;
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO onErrorListener
		GolukDebugUtils.e("videostate", "VideoDetailActivity-------------------------onError :  ");
		if (mAdapter.error) {
			return true;
		}
		String msg = "播放错误";
		switch (arg1) {
		case 1:
		case -1010:
			msg = "视频出错，请重试！";
			break;
		case -110:
			msg = "网络访问异常，请重试！";
			break;

		default:
			break;
		}
		if (!UserUtils.isNetDeviceAvailable(mContext)) {
			msg = "网络访问异常，请重试！";
		}
		mAdapter.error = true;
		mAdapter.cancleTimer();
		mAdapter.hideLoading();
		GolukDebugUtils.e("videoview", "VideoDetailActivity-------------------------onError : hideLoading ");
		mAdapter.dialog(msg,mHolder);
		return true;
	}

}
