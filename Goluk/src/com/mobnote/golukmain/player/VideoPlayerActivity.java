package com.mobnote.golukmain.player;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.player.factory.GolukPlayer;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnCompletionListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnErrorListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnInfoListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnPreparedListener;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.com.tiros.debug.GolukDebugUtils;

@SuppressLint("NewApi")
public class VideoPlayerActivity extends BaseActivity implements OnClickListener, OnErrorListener,
		OnCompletionListener, OnPreparedListener {
	/** ?????????VideoView */
	private FullScreenVideoView mVideo;
	/** ??????View */
	private View mTopView;
	/** ??????View */
	private View mBottomView;
	/** ????????????????????? */
	private SeekBar mSeekBar;
	private ImageView mPlay;
	private TextView mPlayTime;
	private TextView mDurationTime;
	/** ???????????? */
	private float width;
	private float height;
	/** ?????????????????? */
	private int playTime = 0;
	/** ?????????????????? */
	private String videoUrl = "";
	/** ???????????????????????????View????????? */
	private static final int HIDE_TIME = 3000;
	/** ?????????????????? */
	private int orginalLight;
	/** ???????????? */
	private String from;
	/** ??????????????????????????? */
	private String image = "";
	/** ???????????? */
	private String filename = "";
	/** ??????????????? */
	private LinearLayout mLoadingLayout = null;
	/** ??????????????????????????? */
	private ImageView mLoading = null;
	/** ????????????????????? */
	private AnimationDrawable mAnimationDrawable = null;
	private boolean isShow = false;
	/** ????????????????????? */
	private boolean error = false;
	/** ?????????????????? */
	private boolean reset = false;
	/** ?????????????????? */
	private int networkConnectTimeOut = 0;
	/** ???????????? */
	private boolean isPause = false;
	/** ???????????? */
//	private boolean isBuffering = false;
	private int duration = 0;

	private ImageView mPlayImg = null;
	private boolean isStop = false;
	private boolean mIsExit = false;
	private boolean mDragging;
	CustomDialog mCustomDialog;
    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            if (mVideo.isPlaying()) {
    			hideLoading();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };

    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
			if (!mDragging || mVideo.isPlaying()) {
				int duration = mVideo.getDuration();
				int position = mVideo.getCurrentPosition();
				if (duration > 0) {
					int progress = position * 100 / duration;
					mPlayTime.setText(formatTime(position));
					mSeekBar.setProgress(progress);
				}
			}
            mHandler.postDelayed(mProgressChecker, 500);
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoplayer);
		mIsExit = false;
		if (savedInstanceState == null) {
			getPlayAddr();
		} else {
			videoUrl = savedInstanceState.getString("playUrl");
			image = savedInstanceState.getString("image");
			playTime = savedInstanceState.getInt("playtime");
		}
		initView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		if (!TextUtils.isEmpty(videoUrl)) {
			outState.putString("playUrl", videoUrl);
		}
		if (!TextUtils.isEmpty(image)) {
			outState.putString("image", image);
		}
		outState.putInt("playtime", playTime);
		super.onSaveInstanceState(outState);
	}
	/**
	 * ???????????????
	 * 
	 * @author xuhw
	 * @date 2015???6???24???
	 */
	private void initView() {
		mVideo = (FullScreenVideoView) findViewById(R.id.videoview);
		mPlayTime = (TextView) findViewById(R.id.play_time);
		mDurationTime = (TextView) findViewById(R.id.total_time);
		mPlay = (ImageView) findViewById(R.id.play_btn);
		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
		mTopView = findViewById(R.id.top_layout);
		mBottomView = findViewById(R.id.bottom_layout);

		width = DensityUtil.getWidthInPx(this);
		height = DensityUtil.getHeightInPx(this);
		threshold = DensityUtil.dip2px(this, 18);

		orginalLight = LightnessController.getLightness(this);

		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(filename);

		mPlayImg = (ImageView) findViewById(R.id.play_img);

		GlideUtils.loadImage(this, mPlayImg, image, R.drawable.tacitly_pic);

		showLoading();
		setListener();
		playVideo();
	}

	/**
	 * ????????????
	 * 
	 * @author xuhw
	 * @date 2015???6???24???
	 */
	private void setListener() {
		mPlay.setOnClickListener(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.title).setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mVideo.setOnPreparedListener(this);
		mVideo.setOnErrorListener(this);

		mVideo.setOnCompletionListener(this);
		mVideo.setOnTouchListener(mTouchListener);
	}

	/**
	 * ?????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???8???
	 */
	private void showLoading() {
		if (mIsExit) {
			return;
		}
		if (!isShow) {
			isShow = true;
			mLoadingLayout.setVisibility(View.VISIBLE);
			mLoading.setVisibility(View.VISIBLE);
			mLoading.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mAnimationDrawable != null) {
						if (!mAnimationDrawable.isRunning()) {
							mAnimationDrawable.start();
						}
					}
				}
			}, 100);
		}
	}

	/**
	 * ???????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???8???
	 */
	private void hideLoading() {
		mPlayImg.setVisibility(View.GONE);
		if (isShow) {
			isShow = false;

			if (mAnimationDrawable != null) {
				if (mAnimationDrawable.isRunning()) {
					mAnimationDrawable.stop();
				}
			}
			mLoadingLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @author xuhw
	 * @date 2015???6???5???
	 */
	private void getPlayAddr() {
		from = getIntent().getStringExtra("from");
		image = getIntent().getStringExtra("image");
		filename = getIntent().getStringExtra("filename");
		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==2222===filename=" + filename + "===from=" + from);
		String ip = SettingUtils.getInstance().getString("IPC_IP");

		if (TextUtils.isEmpty(from)) {
			return;
		}

		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk" + File.separator
				+ "goluk_carrecorder";
		GFileUtils.makedir(path);
		String filePath = path + File.separator + "image";
		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==filePath=" + filePath);
		if (from.equals("local")) {
			videoUrl = getIntent().getStringExtra("path");
			String fileName = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
			fileName = fileName.replace(".mp4", ".jpg");
			image = filePath + File.separator + fileName;
			GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==image=" + image);
		} else if (from.equals("suqare")) {
			videoUrl = getIntent().getStringExtra("playUrl");
		} else if (from.equals("ipc")) {
			if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)) {
				String fileName = filename;
				String[] names = fileName.split("_");
				if (names.length > 3) {
					if (names[0].equals("NRM")) {
						fileName = names[0] + "_" + names[1];
					} else {
						fileName = names[0] + "_" + names[2];
					}
				}
				videoUrl = "http://" + ip + "/api/video?id=" + fileName;
				image = "http://" + ip + "/api/thumb?id=" + fileName;
			} else {
				String fileName = filename;
				fileName = fileName.replace(".mp4", ".jpg");
				image = filePath + File.separator + fileName;
				int type = getIntent().getIntExtra("type", -1);
				if (4 == type) {
					videoUrl = "http://" + ip + ":5080/rec/wonderful/" + filename;
				} else if (2 == type) {
					videoUrl = "http://" + ip + ":5080/rec/urgent/" + filename;
				} else {
					videoUrl = "http://" + ip + ":5080/rec/normal/" + filename;
				}
			}
		}

		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==playUrl=" + videoUrl);
	}

//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			height = DensityUtil.getWidthInPx(this);
//			width = DensityUtil.getHeightInPx(this);
//		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//			width = DensityUtil.getWidthInPx(this);
//			height = DensityUtil.getHeightInPx(this);
//		}
//		super.onConfigurationChanged(newConfig);
//	}

	private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mDragging = false;
			if (mIsExit) {
				return;
			}
			mHandler.postDelayed(hideRunnable, HIDE_TIME);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mDragging = true;
			mHandler.removeCallbacks(hideRunnable);
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (mIsExit) {
				return;
			}
			if (fromUser) {
				if (null == mVideo) {
					return;
				}
				int time = progress * mVideo.getDuration() / 100;
				mVideo.seekTo(time);
			}
		}
	};

	private void backward(float delataX) {
		if (mIsExit) {
			return;
		}
		if (null == mVideo) {
			return;
		}
		int duration = mVideo.getDuration();
		if(0 >= duration || 0f >= width || !mVideo.canSeekBackward()) {
			return;
		}
		int current = mVideo.getCurrentPosition();
		int backwardTime = (int) (delataX / width * duration);
		int currentTime = current - backwardTime;
		mVideo.seekTo(currentTime);
		mSeekBar.setProgress(currentTime * 100 / duration);
		mPlayTime.setText(formatTime(currentTime));
	}

	private void forward(float delataX) {
		if (mIsExit) {
			return;
		}
		if (null == mVideo) {
			return;
		}
		int duration = mVideo.getDuration();
		if(0 >= duration || 0f >= width || !mVideo.canSeekForward()) {
			return;
		}
		int current = mVideo.getCurrentPosition();
		int forwardTime = (int) (delataX / width * duration);
		int currentTime = current + forwardTime;
		mVideo.seekTo(currentTime);
		mSeekBar.setProgress(currentTime * 100 / duration);
		mPlayTime.setText(formatTime(currentTime));
	}

	private void volumeDown(float delatY) {

	}

	private void volumeUp(float delatY) {

	}

	private void lightDown(float delatY) {

	}

	private void lightUp(float delatY) {

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler();

	private void playVideo() {
		if (mIsExit) {
			return;
		}
		if (null == mVideo) {
			return;
		}
		mVideo.setVideoPath(videoUrl);
		mVideo.requestFocus();
		mVideo.start();
		showLoading();
        mHandler.removeCallbacks(mPlayingChecker);
        mHandler.postDelayed(mPlayingChecker, 250);
	}

	private Runnable hideRunnable = new Runnable() {

		@Override
		public void run() {
			showOrHide();
		}
	};

	@SuppressLint("SimpleDateFormat")
	private String formatTime(long time) {
		DateFormat formatter = new SimpleDateFormat("mm:ss");
		return formatter.format(new Date(time));
	}

	private float mLastMotionX;
	private float mLastMotionY;
	private int startX;
	private int startY;
	private int threshold;
	private boolean isClick = true;

	private OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			final float x = event.getX();
			final float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastMotionX = x;
				mLastMotionY = y;
				startX = (int) x;
				startY = (int) y;
				break;
			case MotionEvent.ACTION_MOVE:
				float deltaX = x - mLastMotionX;
				float deltaY = y - mLastMotionY;
				float absDeltaX = Math.abs(deltaX);
				float absDeltaY = Math.abs(deltaY);
				// ??????????????????
				boolean isAdjustAudio = false;
				if (absDeltaX > threshold && absDeltaY > threshold) {
					if (absDeltaX < absDeltaY) {
						isAdjustAudio = true;
					} else {
						isAdjustAudio = false;
					}
				} else if (absDeltaX < threshold && absDeltaY > threshold) {
					isAdjustAudio = true;
				} else if (absDeltaX > threshold && absDeltaY < threshold) {
					isAdjustAudio = false;
				} else {
					return true;
				}
				if (isAdjustAudio) {
					if (x < width / 2) {
						if (deltaY > 0) {
							lightDown(absDeltaY);
						} else if (deltaY < 0) {
							lightUp(absDeltaY);
						}
					} else {
						if (deltaY > 0) {
							volumeDown(absDeltaY);
						} else if (deltaY < 0) {
							volumeUp(absDeltaY);
						}
					}

				} else {
					if (deltaX > 0) {
						forward(absDeltaX);
					} else if (deltaX < 0) {
						backward(absDeltaX);
					}
				}
				mLastMotionX = x;
				mLastMotionY = y;
				break;
			case MotionEvent.ACTION_UP:
				if (Math.abs(x - startX) > threshold || Math.abs(y - startY) > threshold) {
					isClick = false;
				}
				mLastMotionX = 0;
				mLastMotionY = 0;
				startX = (int) 0;
				if (isClick) {
					showOrHide();
				}
				isClick = true;
				break;

			default:
				break;
			}
			return true;
		}

	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.back_btn
				|| id == R.id.title) {
			finish();
		} else if (id == R.id.play_btn) {
			if (null == mVideo) {
				return;
			}
			if (mVideo.isPlaying() && mVideo.canPause()) {
				mVideo.pause();
				mPlay.setImageResource(R.drawable.player_play_btn);
			} else {
				mVideo.start();
				mPlay.setImageResource(R.drawable.player_pause_btn);
				hideOperator();
			}
		} else {
		}
	}

//	private void exit() {
//		if (mIsExit) {
//			return;
//		}
//		mIsExit = true;
////		this.cancelTimer();
//		mHandler.removeMessages(1);
//
//		// if (null != mVideo) {
//		//
//		// // ?????????video?????????????????? ??????????????? ??????????????????
//		// if (mVideo.isPlaying()) {
//		// mVideo.pause();
//		// }
//		// mVideo.stopPlayback();
//		// mVideo = null;
//		// }
//
//		mHandler.removeMessages(0);
//		mHandler.removeCallbacksAndMessages(null);
//		mHandler.removeCallbacks(mRunnable);
//		mHandler.postDelayed(mRunnable, 200);
//	}
//
//	Runnable mRunnable = new Runnable() {
//		@Override
//		public void run() {
//
//			VideoPlayerActivity.this.finish();
//		}
//	};

	/**
	 * ?????????????????????
	 * 
	 * @author jyf
	 */
	private void showOperator() {
		mTopView.setVisibility(View.VISIBLE);
		mTopView.clearAnimation();
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.option_entry_from_top);
		mTopView.startAnimation(animation);

		mBottomView.setVisibility(View.VISIBLE);
		mBottomView.clearAnimation();
		Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.option_entry_from_bottom);
		mBottomView.startAnimation(animation1);
		if (mVideo.isPlaying()) {
			mPlay.setImageResource(R.drawable.player_pause_btn);
		} else {
			mPlay.setImageResource(R.drawable.player_play_btn);
		}
	}

	/**
	 * ?????????????????????
	 * 
	 * @author jyf
	 */
	private void hideOperator() {
		mTopView.clearAnimation();
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.option_leave_from_top);
		animation.setAnimationListener(new AnimationImp() {
			@Override
			public void onAnimationEnd(Animation animation) {
				super.onAnimationEnd(animation);
				mTopView.setVisibility(View.GONE);
			}
		});
		mTopView.startAnimation(animation);

		mBottomView.clearAnimation();
		Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.option_leave_from_bottom);
		animation1.setAnimationListener(new AnimationImp() {
			@Override
			public void onAnimationEnd(Animation animation) {
				super.onAnimationEnd(animation);
				mBottomView.setVisibility(View.GONE);
			}
		});
		mBottomView.startAnimation(animation1);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???6???24???
	 */
	private void showOrHide() {
		if (mIsExit) {
			return;
		}
		if (mTopView.getVisibility() == View.VISIBLE) {
			hideOperator();
		} else {
			showOperator();
			mHandler.removeCallbacks(hideRunnable);
			mHandler.postDelayed(hideRunnable, HIDE_TIME);
		}
	}

	private class AnimationImp implements AnimationListener {

		@Override
		public void onAnimationEnd(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

	}

//	@Override
//	public boolean onInfo(GolukPlayer arg0, int arg1, int arg2) {
//
//		if (mIsExit) {
//			return false;
//		}
//		switch (arg1) {
//		case 3:
//			hideLoading();
//			break;
//
//		default:
//			break;
//		}
//		return false;
//	}

	@Override
	public boolean onError(GolukPlayer arg0, int arg1, int arg2) {
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onError----");
		if (mIsExit) {
			return true;
		}
		if (error) {
			return false;
		}

		String msg = this.getString(R.string.str_play_error);
		switch (arg1) {
		case 1:
		case -1010:
			msg = this.getString(R.string.str_play_video_error);
			break;
		case -110:
			msg = this.getString(R.string.str_play_video_network_error);
			break;

		default:
			break;
		}

		if (!from.equals("local")) {
			if (!isNetworkConnected()) {
				msg = this.getString(R.string.str_play_video_network_error);
			}
		}

		error = true;
		GolukDebugUtils.e("xuhw", "BBBBBB=====onError==arg1=" + arg1 + "==arg2=" + arg2);
		hideLoading();
		mPlayTime.setText("00:00");
		mPlayImg.setVisibility(View.VISIBLE);
		dialog(msg);

		return true;
	}

	/**
	 * ???????????????????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???6???5???
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	@Override
	public void onCompletion(GolukPlayer arg0) {
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onCompletion----");
		if (mIsExit) {
			return;
		}
		if (error) {
			return;
		}
//		showOperator();
		mVideo.seekTo(0);
		mPlay.setImageResource(R.drawable.player_play_btn);
		mPlayTime.setText("00:00");
		mSeekBar.setProgress(0);
		GolukDebugUtils.e("", "TTTT======2220000=====progress==");
	}

	@Override
	public void onPrepared(GolukPlayer mp) {
		if (mIsExit) {
			return;
		}
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onPrepared----=");
//		if(null == mVideo || null == mp) {
//			return;
//		}
//
//		mVideo.setVideoWidth(mp.getVideoWidth());
//		mVideo.setVideoHeight(mp.getVideoHeight());
//		mPlay.setImageResource(R.drawable.player_pause_btn);
//		if (GolukUtils.getSystemSDK() < 17) {
//			hideLoading();
//		}
		// mHandler.removeCallbacks(hideRunnable);
		// mHandler.postDelayed(hideRunnable, HIDE_TIME);
		if(null != mDurationTime) {
			mDurationTime.setText(formatTime(mVideo.getDuration()));
		}
//		mHandler.sendEmptyMessage(1);
//		startTimer();
	}

	/** ???????????????????????????????????? */
	private boolean isShowDialog = false;

	/**
	 * ???????????????
	 * 
	 * @param msg
	 *            ????????????
	 * @author xuhw
	 * @date 2015???6???5???
	 */
	private void dialog(String msg) {
		if (mIsExit) {
			return;
		}
		if (isShowDialog) {
			return;
		}
		isShowDialog = true;
		if (mCustomDialog == null) {
			mCustomDialog = new CustomDialog(this);
			mCustomDialog.setCancelable(true);
			mCustomDialog.setMessage(msg, Gravity.CENTER);
			mCustomDialog.setLeftButton(this.getString(R.string.str_button_ok), new OnLeftClickListener() {
				@Override
				public void onClickListener() {
					finish();
				}
			});
			mCustomDialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface arg0) {
					finish();
				}
			});
		}
		mCustomDialog.show();
	}

	private boolean mResume = false;
	@Override
	protected void onPause() {
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onPause----");
		super.onPause();
		LightnessController.setLightness(this, orginalLight);

		if (null == mVideo) {
			return;
		}
		playTime = mVideo.getCurrentPosition();
		
		mResume = true;
		mHandler.removeCallbacksAndMessages(null);
		playTime = mVideo.getCurrentPosition();
		mPlayImg.setVisibility(View.VISIBLE);
		mVideo.suspend();
	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onResume----");
		if (mResume) {
			mVideo.seekTo(playTime);
			mVideo.resume();;
//			showLoading();
		}
        mHandler.post(mProgressChecker);
        mHandler.post(mPlayingChecker);
	}


	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		if (mIsExit) {
			return;
		}
		mVideo.stopPlayback();
		mIsExit = true;
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onDestroy----");
		if (mHandler != null){
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		error = false;
		hideLoading();
		if (mCustomDialog != null && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
		}
		mCustomDialog = null;
		super.onDestroy();
	}
	
}
