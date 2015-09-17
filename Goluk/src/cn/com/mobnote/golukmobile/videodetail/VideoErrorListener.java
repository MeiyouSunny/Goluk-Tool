package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import cn.com.mobnote.user.UserUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.view.Gravity;
import android.view.View;

public class VideoErrorListener implements OnErrorListener {

	private Context mContext;
	private boolean mError;
	private boolean isShow;
	private ViewHolder mHolder;
	private CustomDialog mCustomDialog;
	
	public VideoErrorListener(Context context,boolean error,ViewHolder holder,boolean show) {
		this.mContext = context;
		this.mError = error;
		this.mHolder = holder;
		this.isShow = show;
	}
	
	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO onErrorListener
				if (mError) {
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
				mError = true;
				hideLoading();
				GolukDebugUtils.e("", "VideoSquareDetailActivity-------------------------onInfo : hideLoading ");
				mHolder.mImageLayout.setVisibility(View.VISIBLE);
				dialog(msg);
				return true;
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
	 * 提示对话框
	 * 
	 * @param msg
	 *            提示信息
	 */
	private void dialog(String msg) {
		if (null == mCustomDialog) {
			mCustomDialog = new CustomDialog(mContext);
			mCustomDialog.setCancelable(false);
			mCustomDialog.setMessage(msg, Gravity.CENTER);
			mCustomDialog.setLeftButton("确定", new OnLeftClickListener() {
				@Override
				public void onClickListener() {
//					cancleTimer();
					mHolder.mImageLayout.setVisibility(View.VISIBLE);
					mHolder.mPlayerLayout.setEnabled(false);
					mHolder.mSeekBar.setProgress(0);
					// finish();
				}
			});
			if (!((VideoDetailActivity)mContext).isFinishing()) {
				mCustomDialog.show();
			}
		}
	}
	
	/**
	 * 取消计时
	 */
//	private void cancleTimer() {
//		if (null != timer) {
//			timer.cancel();
//		}
//	}
	
}
