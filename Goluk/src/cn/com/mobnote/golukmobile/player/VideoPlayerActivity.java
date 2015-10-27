package cn.com.mobnote.golukmobile.player;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

@SuppressLint("NewApi")
public class VideoPlayerActivity extends BaseActivity implements OnClickListener, OnInfoListener, OnErrorListener,
		OnCompletionListener, OnPreparedListener {
	/** 自定义VideoView */
	private FullScreenVideoView mVideo;
	/** 头部View */
	private View mTopView;
	/** 底部View */
	private View mBottomView;
	/** 视频播放拖动条 */
	private SeekBar mSeekBar;
	private ImageView mPlay;
	private TextView mPlayTime;
	private TextView mDurationTime;
	/** 屏幕宽高 */
	private float width;
	private float height;
	/** 视频播放时间 */
	private int playTime = 0;
	/** 视频播放地址 */
	private String videoUrl = "";
	/** 自动隐藏顶部和底部View的时间 */
	private static final int HIDE_TIME = 3000;
	/** 原始屏幕亮度 */
	private int orginalLight;
	/** 来源标志 */
	private String from;
	/** 视频第一帧图片地址 */
	private String image = "";
	/** 文件名字 */
	private String filename = "";
	/** 加载中布局 */
	private LinearLayout mLoadingLayout = null;
	/** 加载中动画显示控件 */
	private ImageView mLoading = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	private boolean isShow = false;
	/** 播放器报错标识 */
	private boolean error = false;
	/** 播放重置标识 */
	private boolean reset = false;
	/** 网络连接超时 */
	private int networkConnectTimeOut = 0;
	/** 暂停标识 */
	private boolean isPause = false;
	/** 缓冲标识 */
	private boolean isBuffering = false;
	private int duration = 0;

	private RelativeLayout mImageLayout = null;
	private Bitmap mBitmap = null;
	private ImageView mImageView = null;
	private boolean isStop = false;
	private boolean mIsExit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoplayer);
		mIsExit = false;
		getPlayAddr();
		initView();
	}

	/**
	 * 初始化控件
	 * 
	 * @author xuhw
	 * @date 2015年6月24日
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

		mImageLayout = (RelativeLayout) findViewById(R.id.mImageLayout);
		mImageLayout.removeAllViews();
		RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		if (from.equals("suqare")) {
			SimpleDraweeView view = new SimpleDraweeView(this);
			GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());
			GenericDraweeHierarchy hierarchy = builder.setFadeDuration(300)
					.setPlaceholderImage(getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
					.setFailureImage(getResources().getDrawable(R.drawable.tacitly_pic), ScaleType.FIT_XY)
					.setActualImageScaleType(ScaleType.FIT_XY).build();
			view.setHierarchy(hierarchy);
			view.setImageURI(Uri.parse(image));
			mImageLayout.addView(view, mPreLoadingParams);
		} else {
			mImageView = new ImageView(this);
			mImageView.setImageResource(R.drawable.tacitly_pic);
			mBitmap = ImageManager.getBitmapFromCache(image, 400, 400);
			if (null != mBitmap) {
				mImageView.setImageBitmap(mBitmap);
			}
			mImageLayout.addView(mImageView, mPreLoadingParams);
		}

		showLoading();
		setListener();
		playVideo();
	}

	/**
	 * 监听设置
	 * 
	 * @author xuhw
	 * @date 2015年6月24日
	 */
	private void setListener() {
		mPlay.setOnClickListener(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.title).setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mVideo.setOnPreparedListener(this);
		mVideo.setOnErrorListener(this);
		if (GolukUtils.getSystemSDK() >= 17) {
			try {
				mVideo.setOnInfoListener(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mVideo.setOnCompletionListener(this);
		mVideo.setOnTouchListener(mTouchListener);
	}

	/**
	 * 显示加载中布局
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
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
	 * 隐藏加载中显示画面
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void hideLoading() {
		if (isShow) {
			isShow = false;

			mImageLayout.setVisibility(View.GONE);
			if (mAnimationDrawable != null) {
				if (mAnimationDrawable.isRunning()) {
					mAnimationDrawable.stop();
				}
			}
			mLoadingLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 获取播放地址
	 * 
	 * @author xuhw
	 * @date 2015年6月5日
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

		GolukDebugUtils.e("xuhw", "YYYYYY==VideoPlayerActivity==playUrl=" + videoUrl);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			height = DensityUtil.getWidthInPx(this);
			width = DensityUtil.getHeightInPx(this);
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			width = DensityUtil.getWidthInPx(this);
			height = DensityUtil.getHeightInPx(this);
		}
		super.onConfigurationChanged(newConfig);
	}

	private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (mIsExit) {
				return;
			}
			mHandler.postDelayed(hideRunnable, HIDE_TIME);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
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
		int current = mVideo.getCurrentPosition();
		int backwardTime = (int) (delataX / width * mVideo.getDuration());
		int currentTime = current - backwardTime;
		mVideo.seekTo(currentTime);
		mSeekBar.setProgress(currentTime * 100 / mVideo.getDuration());
		mPlayTime.setText(formatTime(currentTime));
	}

	private void forward(float delataX) {
		if (mIsExit) {
			return;
		}
		if (null == mVideo) {
			return;
		}
		int current = mVideo.getCurrentPosition();
		int forwardTime = (int) (delataX / width * mVideo.getDuration());
		int currentTime = current + forwardTime;
		mVideo.seekTo(currentTime);
		mSeekBar.setProgress(currentTime * 100 / mVideo.getDuration());
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
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if (error) {
					return;
				}
				if (mIsExit) {
					return;
				}
				netWorkTimeoutCheck();
				if (null == mVideo) {
					return;
				}
				if (mVideo.getCurrentPosition() > 0) {
					if (!mVideo.isPlaying()) {
						return;
					}

					if (!isBuffering) {
						hideLoading();
					}

					playTime = 0;
					duration = mVideo.getDuration();
					mPlayTime.setText(formatTime(mVideo.getCurrentPosition()));
					int progress = mVideo.getCurrentPosition() * 100 / mVideo.getDuration();
					GolukDebugUtils.e("", "TTTT============progress==" + progress);
					mSeekBar.setProgress(progress);
					if (mVideo.getCurrentPosition() > mVideo.getDuration() - 100) {
						mPlayTime.setText("00:00");
						mSeekBar.setProgress(0);
						GolukDebugUtils.e("", "TTTT======00000======progress==");
					}
					// mSeekBar.setSecondaryProgress(mVideo.getBufferPercentage());
					mPlay.setImageResource(R.drawable.player_pause_btn);
				} else {
					// mPreLoading.setVisibility(View.VISIBLE);
					mPlay.setImageResource(R.drawable.player_play_btn);
					mPlayTime.setText(formatTime(playTime));
					if (0 != duration) {
						mSeekBar.setProgress(playTime * 100 / duration);
					} else {
						mSeekBar.setProgress(0);
					}

					GolukDebugUtils.e("", "TTTT=====111 000=======playTime==" + playTime + "==duration=" + duration);
				}

				break;
			case 2:
				showOrHide();
				break;
			default:
				break;
			}
		}
	};

	private void playVideo() {
		if (mIsExit) {
			return;
		}
		if (null == mVideo) {
			return;
		}
		mVideo.setVideoPath(videoUrl);
		mVideo.requestFocus();
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
				// 声音调节标识
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
		switch (v.getId()) {
		case R.id.back_btn:
		case R.id.title:
			exit();
			break;
		case R.id.play_btn:
			if (isBuffering) {
				return;
			}

			if (null == mVideo) {
				return;
			}

			if (mVideo.isPlaying()) {
				mVideo.pause();
				mPlay.setImageResource(R.drawable.player_play_btn);
			} else {
				mVideo.start();
				mPlay.setImageResource(R.drawable.player_pause_btn);
				hideOperator();
			}
			break;
		default:
			break;
		}
	}

	private void exit() {
		if (mIsExit) {
			return;
		}
		mIsExit = true;
		this.cancelTimer();
		mHandler.removeMessages(1);
		// if (null != mBitmap) {
		// if (!mBitmap.isRecycled()) {
		// mBitmap.recycle();
		// mBitmap = null;
		// }
		// }

		// if (null != mImageView) {
		// mImageView.setImageBitmap(null);
		// }

		if (null != mVideo) {

			// 判断下video是否在播放中 如果在播放 先暂停播放器
			if (mVideo.isPlaying()) {
				mVideo.pause();
			}
			mVideo.stopPlayback();
			mVideo = null;
		}

		mHandler.removeMessages(0);
		mHandler.removeCallbacksAndMessages(null);
		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, 200);
	}

	private void freeBitmap() {
		if (null != mBitmap) {
			if (!mBitmap.isRecycled()) {
				mBitmap.recycle();
				mBitmap = null;
			}
		}
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {

			VideoPlayerActivity.this.finish();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		freeBitmap();
	}

	/**
	 * 显示上下操作栏
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
	}

	/**
	 * 隐藏上下操作栏
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
	 * 显示隐藏顶部底部布局
	 * 
	 * @author xuhw
	 * @date 2015年6月24日
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

	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onInfo----arg1:" + arg1 + "   arg2:" + arg2);
		if (mIsExit) {
			return false;
		}
		switch (arg1) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			isBuffering = true;
			if (0 == mVideo.getCurrentPosition()) {
				mImageLayout.setVisibility(View.VISIBLE);
			}
			showLoading();
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			isBuffering = false;
			hideLoading();
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onError----");
		if (mIsExit) {
			return true;
		}
		if (error) {
			return false;
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

		if (!from.equals("local")) {
			if (!isNetworkConnected()) {
				msg = "网络访问异常，请重试！";
			}
		}

		error = true;
		GolukDebugUtils.e("xuhw", "BBBBBB=====onError==arg1=" + arg1 + "==arg2=" + arg2);
		hideLoading();
		mPlayTime.setText("00:00");
		mImageLayout.setVisibility(View.VISIBLE);
		dialog(msg);

		return true;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onCompletion----");
		if (mIsExit) {
			return;
		}
		if (error) {
			return;
		}
		showOperator();
		mVideo.seekTo(0);
		mPlay.setImageResource(R.drawable.player_play_btn);
		mPlayTime.setText("00:00");
		mSeekBar.setProgress(0);
		GolukDebugUtils.e("", "TTTT======2220000=====progress==");
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (mIsExit) {
			return;
		}
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onPrepared----=");
		mVideo.setVideoWidth(mp.getVideoWidth());
		mVideo.setVideoHeight(mp.getVideoHeight());

		mVideo.start();
		if (playTime != 0) {
			mVideo.seekTo(playTime);
		}

		// mHandler.removeCallbacks(hideRunnable);
		// mHandler.postDelayed(hideRunnable, HIDE_TIME);
		mDurationTime.setText(formatTime(mVideo.getDuration()));

		startTimer();
	}

	/** 保证错误提示框只显示一次 */
	private boolean isShowDialog = false;

	/**
	 * 提示对话框
	 * 
	 * @param msg
	 *            提示信息
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	private void dialog(String msg) {
		if (mIsExit) {
			return;
		}
		if (isShowDialog) {
			return;
		}
		isShowDialog = true;
		CustomDialog mCustomDialog = new CustomDialog(this);
		mCustomDialog.setCancelable(true);
		mCustomDialog.setMessage(msg, Gravity.CENTER);
		mCustomDialog.setLeftButton("确定", new OnLeftClickListener() {
			@Override
			public void onClickListener() {
				exit();
			}
		});
		mCustomDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				exit();
			}
		});
		mCustomDialog.show();
	}

	/**
	 * 检查是否有可用网络
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	/**
	 * 无网络超时检查
	 * 
	 * @author xuhw
	 * @date 2015年6月5日
	 */
	private void netWorkTimeoutCheck() {
		if (!from.equals("suqare")) {
			return;
		}

		if (!isNetworkConnected()) {
			networkConnectTimeOut++;
			if (networkConnectTimeOut > 15) {
				if (!reset) {
					hideLoading();
					mImageLayout.setVisibility(View.VISIBLE);
					dialog("网络访问异常，请重试！");
					if (null != mVideo) {
						mVideo.stopPlayback();
						mVideo = null;
					}
					return;
				}
			}
		} else {
			networkConnectTimeOut = 0;
		}

	}

	@Override
	protected void onPause() {
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onPause----");
		super.onPause();
		LightnessController.setLightness(this, orginalLight);

		if (null == mVideo) {
			return;
		}

		if (mVideo.isPlaying()) {
			isPause = true;
			playTime = mVideo.getCurrentPosition();
			mVideo.pause();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onResume----");
		if (isStop) {
			isStop = false;
			showLoading();
			mImageLayout.setVisibility(View.VISIBLE);
		}

		if (isPause) {
			isPause = false;
			if (playTime != 0) {
				GolukDebugUtils.e("", "TTTT============playTime==" + playTime + "==duration=" + duration);
				if (0 != duration) {
					mSeekBar.setProgress(playTime * 100 / duration);
				}
				mVideo.seekTo(playTime);

			}
			mVideo.start();
		}

	}

	private Timer mTimer = null;

	private void cancelTimer() {
		if (null != mTimer) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	private void startTimer() {
		cancelTimer();
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				mHandler.sendEmptyMessage(1);
			}
		}, 0, 1000);
	}

	@Override
	protected void onStop() {
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onStop----");
		super.onStop();
		if (isBackground(this)) {
			isStop = true;
		}
	}

	public boolean isBackground(final Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

}
