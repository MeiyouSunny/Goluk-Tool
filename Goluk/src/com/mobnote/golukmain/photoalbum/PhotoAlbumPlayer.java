package com.mobnote.golukmain.photoalbum;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventDownloadIpcVid;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnRightClickListener;
import com.mobnote.golukmain.photoalbum.OrientationManager.IOrientationFn;
import com.mobnote.golukmain.player.DensityUtil;
import com.mobnote.golukmain.player.FullScreenVideoView;
import com.mobnote.golukmain.player.factory.GolukPlayer;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnCompletionListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnErrorListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnPreparedListener;
import com.mobnote.golukmain.startshare.VideoEditActivity;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

import de.greenrobot.event.EventBus;

public class PhotoAlbumPlayer extends BaseActivity implements OnClickListener, OnPreparedListener, OnErrorListener,
		OnCompletionListener, IOrientationFn {
	private static final String TAG = "PhotoAlbumPlayer";

	public static final String VIDEO_FROM = "video_from";
	public static final String PATH = "path";
	public static final String DATE = "date";
	public static final String HP = "hp";
	public static final String SIZE = "size";
	public static final String FILENAME = "file_name";
	public static final String TYPE = "type";

	private GolukApplication mApp = null;
	private ImageButton mBackBtn = null;
	private String mDate, mHP, mPath, mVideoFrom, mSize, mFileName, mVideoUrl, mImageUrl;
	private int mType;
	private RelativeLayout mVideoViewLayout;
	private FullScreenVideoView mVideoView;
	private boolean mIsFullScreen = false;

	private Handler mHandler = new Handler();
	private CustomDialog mCustomDialog;
	private int mScreenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	/** ?????????????????? */
	private int mPlayTime = 0;
	/** ??????View */
	private View mTopView;
	/** ??????View */
	private View mBottomView;
	/** ????????????????????? */
	private SeekBar mSeekBar, mVtSeekBar;
	private ImageView mPlayImageView;
	private TextView mPlayTimeTextView, mVtPlayTimeTextView;
	private TextView mDurationTime, mVtDurationTime;
	private ImageView mPlayImg = null;
	private Button mBtnVtPlay;
	/** ??????????????? */
	private LinearLayout mLoadingLayout = null;
	/** ??????????????????????????? */
	private ImageView mLoading = null;
	/** ????????????????????? */
	private AnimationDrawable mAnimationDrawable = null;
	/** ???????????????????????????View????????? */
	private static final int HIDE_TIME = 3000;
	private boolean mDragging;

	/** ??????????????? */
	private PlayerMoreDialog mPlayerMoreDialog;

	private OnRightClickListener OnDeleteVidListener;

	private OrientationManager mOrignManager = null;

	private final Runnable mPlayingChecker = new Runnable() {
		@Override
		public void run() {
			if (mVideoView.isPlaying()) {
				hideLoading();
			} else {
				mHandler.postDelayed(mPlayingChecker, 250);
			}
		}
	};

	private final Runnable mProgressChecker = new Runnable() {
		@Override
		public void run() {
			if (!mDragging || mVideoView.isPlaying()) {
				int duration = mVideoView.getDuration();
				int position = mVideoView.getCurrentPosition();
				if (duration > 0) {
					int progress = position * 100 / duration;
					mPlayTimeTextView.setText(formatTime(position));
					mVtPlayTimeTextView.setText(formatTime(position));
					mSeekBar.setProgress(progress);
					mVtSeekBar.setProgress(progress);
				}
			}
			mHandler.postDelayed(mProgressChecker, 500);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photoalbum_player);
		mApp = (GolukApplication) getApplication();
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			mDate = intent.getStringExtra(DATE);
			mHP = intent.getStringExtra(HP);
			mPath = intent.getStringExtra(PATH);
			Log.i("path", "path:" + mPath);
			mVideoFrom = intent.getStringExtra(VIDEO_FROM);
			mSize = intent.getStringExtra(SIZE);
			mFileName = intent.getStringExtra(FILENAME);
			mType = intent.getIntExtra(TYPE, 0);
		} else {
			mDate = savedInstanceState.getString(DATE);
			mHP = savedInstanceState.getString(HP);
			mPath = savedInstanceState.getString(PATH);
			mVideoFrom = savedInstanceState.getString(VIDEO_FROM);
			mSize = savedInstanceState.getString(SIZE);
			mFileName = savedInstanceState.getString(FILENAME);
			mType = savedInstanceState.getInt(TYPE);
			mPlayTime = savedInstanceState.getInt("playtime");
		}
		threshold = DensityUtil.dip2px(this, 18);
		initView();

		setOrientation(true);
		mOrignManager = new OrientationManager(this, this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mDate != null) {
			outState.putString(DATE, mDate);
		}
		if (mHP != null) {
			outState.putString(HP, mHP);
		}
		if (mPath != null) {
			outState.putString(PATH, mPath);
		}
		if (mVideoFrom != null) {
			outState.putString(VIDEO_FROM, mVideoFrom);
		}
		if (mSize != null) {
			outState.putString(SIZE, mSize);
		}
		if (mFileName != null) {
			outState.putString(FILENAME, mFileName);
		}
		outState.putInt(TYPE, mType);
		outState.putInt("playtime", mPlayTime);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(this, TAG);
		if (mResume) {
			mVideoView.seekTo(mPlayTime);
			mVideoView.resume();
		}
		mHandler.post(mProgressChecker);
		mHandler.post(mPlayingChecker);
	}

	private boolean mResume = false;

	@Override
	protected void onPause() {
		super.onPause();
		mResume = true;
		mHandler.removeCallbacksAndMessages(null);
		mPlayTime = mVideoView.getCurrentPosition();
		mVideoView.suspend();
	}

	@Override
	protected void onDestroy() {
		mVideoView.stopPlayback();
		GolukDebugUtils.e("", "jyf----VideoPlayerActivity--------onDestroy----");
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		hideLoading();
		if (mCustomDialog != null && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
		}
		mCustomDialog = null;

		if (mPlayerMoreDialog != null && mPlayerMoreDialog.isShowing()) {
			mPlayerMoreDialog.dismiss();
		}
		mPlayerMoreDialog = null;
		super.onDestroy();
	}

	private void initView() {
		mPlayTimeTextView = (TextView) findViewById(R.id.play_time);
		mVtPlayTimeTextView = (TextView) findViewById(R.id.vt_play_time);
		mDurationTime = (TextView) findViewById(R.id.total_time);
		mVtDurationTime = (TextView) findViewById(R.id.vt_total_time);
		mVtSeekBar = (SeekBar) findViewById(R.id.vt_seekbar);
		mPlayImageView = (ImageView) findViewById(R.id.play_btn);
		mPlayImageView.setOnClickListener(this);
		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
		mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mVtSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mTopView = findViewById(R.id.upper_layout);
		mBottomView = findViewById(R.id.bottom_layout);
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();

		mPlayImg = (ImageView) findViewById(R.id.play_img);
		mBtnVtPlay = (Button) findViewById(R.id.btn_vt_play);
		mBtnVtPlay.setOnClickListener(this);
		mBackBtn = (ImageButton) findViewById(R.id.imagebutton_back);
		mBackBtn.setOnClickListener(this);
		findViewById(R.id.mMoreBtn).setOnClickListener(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		if (mSize != null) {
			TextView tvSize = (TextView) findViewById(R.id.tv_size);
			tvSize.setText(mSize);
		}

		if (mHP != null) {
			TextView tvHP = (TextView) findViewById(R.id.tv_hp);
			tvHP.setText(mHP);
		}

		if (mDate != null) {
			TextView tvTitle = (TextView) findViewById(R.id.textview_title);
			tvTitle.setText(mDate);
			TextView title = (TextView) findViewById(R.id.title);
			title.setText(mDate);
		}

		findViewById(R.id.btn_full_screen).setOnClickListener(this);
		Button shareBtn = (Button) findViewById(R.id.btn_download);
		shareBtn.setOnClickListener(this);
		if (mVideoFrom.equals("local")) {
			if (mType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG || mType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
				shareBtn.setBackgroundResource(R.drawable.btn_photoalbum_share);
			} else {
				shareBtn.setVisibility(View.INVISIBLE);
			}
		} else {
			shareBtn.setBackgroundResource(R.drawable.btn_photoalbum_download);
		}
		mVideoViewLayout = (RelativeLayout) findViewById(R.id.rv_video_player);
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVideoViewLayout.getLayoutParams();
		lp.width = mScreenWidth;
		lp.height = (int) (lp.width / 1.777);
		lp.leftMargin = 0;
		mVideoViewLayout.setLayoutParams(lp);
		mVideoView = (FullScreenVideoView) findViewById(R.id.fullscreen_video_view);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnErrorListener(this);

		mVideoView.setOnCompletionListener(this);

		getPlayAddr();
		GlideUtils.loadImage(this, mPlayImg, mImageUrl, R.drawable.tacitly_pic);
		mVideoView.setVideoPath(mVideoUrl);
		mVideoView.requestFocus();
		mVideoView.start();
		showLoading();
		mHandler.postDelayed(mPlayingChecker, 250);
	}

	private void exit() {
		finish();
		mOrignManager.clearListener();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (GolukUtils.isFastDoubleClick()) {
			return;
		}
		if (id == R.id.imagebutton_back) {
			// ??????
			exit();
		} else if (id == R.id.mMoreBtn) {
			if (mPlayerMoreDialog == null) {
				String tempPath = "";

				if (!TextUtils.isEmpty(mVideoFrom)) {
					if ("local".equals(mVideoFrom)) {
						tempPath = mPath;
					} else {
						tempPath = mFileName;
					}
				}

				mPlayerMoreDialog = new PlayerMoreDialog(PhotoAlbumPlayer.this, tempPath, getType(), mVideoFrom, mType);
			}
			mPlayerMoreDialog.show();
		} else if (id == R.id.btn_full_screen) {
			click_btnFullScreen();

		} else if (id == R.id.back_btn) {

			click_back();
		} else if (id == R.id.play_btn || id == R.id.btn_vt_play) {
			if (mVideoView.isPlaying() && mVideoView.canPause()) {
				mVideoView.pause();
				mPlayImageView.setImageResource(R.drawable.player_play_btn);
				mBtnVtPlay.setBackgroundResource(R.drawable.btn_vt_play);
			} else {
				mVideoView.start();
				mPlayImageView.setImageResource(R.drawable.player_pause_btn);
				mBtnVtPlay.setBackgroundResource(R.drawable.btn_vt_pause);
				if (id == R.id.play_btn) {
					hideOperator();
				}
			}
		} else if (id == R.id.btn_download) {
			if (mVideoFrom.equals("local")) {
				Intent intent = new Intent(this, VideoEditActivity.class);

				int tempType = 2;
				if (mType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG/*
																 * IPCManagerFn.
																 * TYPE_URGENT
																 */) {
					tempType = 3;
				}

				intent.putExtra("type", tempType);
				intent.putExtra("cn.com.mobnote.video.path", mPath);
				startActivity(intent);
				finish();
			} else {
				EventBus.getDefault().post(new EventDownloadIpcVid(mFileName, getType()));
			}
		} else {
			Log.e(TAG, "id = " + id);
		}
	}

	private int getType() {
		int tempType = 0;
		if ("local".equals(mVideoFrom)) {
			tempType = PhotoAlbumConfig.PHOTO_BUM_LOCAL;
		} else {
			tempType = mType;
		}
		return tempType;
	}

	/**
	 * ?????????????????????
	 * 
	 * @param bFull
	 *            true:?????????false:??????
	 */
	public void setFullScreen(boolean isAuto, boolean bFull) {
		if (bFull == mIsFullScreen) {
			// GolukUtils.showToast(this, "?????????????????????.");
			return;
		}
		if (bFull) {

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideoViewLayout.getLayoutParams();
			params.width = metrics.widthPixels;
			params.height = metrics.heightPixels;
			params.leftMargin = 0;
			if (Build.VERSION.SDK_INT > 16) {
				params.removeRule(RelativeLayout.BELOW);
			} else {
				params.addRule(RelativeLayout.BELOW, 0);
			}

			mVideoViewLayout.setLayoutParams(params);
			RelativeLayout.LayoutParams norParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			norParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			norParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			mVideoView.setOnTouchListener(mTouchListener);

		} else {
			mVideoView.setOnTouchListener(null);
			try {
				mHandler.removeCallbacks(hideRunnable);
			} catch (Exception e) {
				
			}
			
			hideOperator();
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVideoViewLayout.getLayoutParams();
			lp.width = mScreenWidth;
			lp.height = (int) (lp.width / 1.777);
			lp.leftMargin = 0;
			lp.addRule(RelativeLayout.BELOW, R.id.RelativeLayout_videoinfo);
			mVideoViewLayout.setLayoutParams(lp);

		}
		mIsFullScreen = bFull;
	}

	/**
	 * ??????????????????
	 * 
	 * @author xuhw
	 * @date 2015???6???5???
	 */
	private void getPlayAddr() {

		String ip = SettingUtils.getInstance().getString("IPC_IP");

		if (TextUtils.isEmpty(mVideoFrom)) {
			return;
		}

		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk" + File.separator
				+ "goluk_carrecorder";
		GFileUtils.makedir(path);
		String filePath = path + File.separator + "image";
		GolukDebugUtils.e(TAG, "YYYYYY==VideoPlayerActivity==filePath=" + filePath);
		if (mVideoFrom.equals("local")) {
			mVideoUrl = mPath;
			String fileName = mPath.substring(mPath.lastIndexOf("/") + 1);
			fileName = fileName.replace(".mp4", ".jpg");
			mImageUrl = filePath + File.separator + fileName;

		} else if (mVideoFrom.equals("ipc")) {
			if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)) {
				String fileName = mFileName;
				String[] names = fileName.split("_");
				if (names.length > 3) {
					if (names[0].equals("NRM")) {
						fileName = names[0] + "_" + names[1];
					} else {
						fileName = names[0] + "_" + names[2];
					}
				}
				mVideoUrl = "http://" + ip + "/api/video?id=" + fileName;
				mImageUrl = "http://" + ip + "/api/thumb?id=" + fileName;
			} else {
				String fileName = mFileName;
				fileName = fileName.replace(".mp4", ".jpg");
				mImageUrl = filePath + File.separator + fileName;
				if (PhotoAlbumConfig.PHOTO_BUM_IPC_WND == mType/* 4 == mType */) {
					mVideoUrl = "http://" + ip + ":5080/rec/wonderful/" + mFileName;
				} else if (PhotoAlbumConfig.PHOTO_BUM_IPC_URG == mType/*
																	 * 2 ==
																	 * mType
																	 */) {
					mVideoUrl = "http://" + ip + ":5080/rec/urgent/" + mFileName;
				} else {
					mVideoUrl = "http://" + ip + ":5080/rec/normal/" + mFileName;
				}
			}
		}

		GolukDebugUtils.e(TAG, "YYYYYY==VideoPlayerActivity==playUrl=" + mVideoUrl);
	}

	/**
	 * ???????????????
	 * 
	 * @param msg
	 *            ????????????
	 * @author xuhw
	 * @date 2015???6???5???
	 */
	private void dialog(String msg) {

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
	public void onCompletion(GolukPlayer mp) {
		mVideoView.seekTo(0);
		mPlayTimeTextView.setText("00:00");
		mVtPlayTimeTextView.setText("00:00");
		mSeekBar.setProgress(0);
		mVtSeekBar.setProgress(0);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onError(GolukPlayer mp, int what, int extra) {
		String msg = this.getString(R.string.str_play_error);
		switch (what) {
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

		if (!mVideoFrom.equals("local")) {
			if (!isNetworkConnected()) {
				msg = this.getString(R.string.str_play_video_network_error);
			}
		}

		hideLoading();
		mPlayTimeTextView.setText("00:00");
		mVtPlayTimeTextView.setText("00:00");
		mPlayImg.setVisibility(View.VISIBLE);
		dialog(msg);

		return true;
	}

	@SuppressLint("SimpleDateFormat")
	private String formatTime(long time) {
		DateFormat formatter = new SimpleDateFormat("mm:ss");
		return formatter.format(new Date(time));
	}

	@Override
	public void onPrepared(GolukPlayer mp) {
		if (null != mDurationTime) {
			mDurationTime.setText(formatTime(mVideoView.getDuration()));
			mVtDurationTime.setText(formatTime(mVideoView.getDuration()));
		}
	}

	private boolean isShow = false;

	/**
	 * ?????????????????????
	 * 
	 */
	private void showLoading() {
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

	private void backward(float delataX) {

		int duration = mVideoView.getDuration();
		if (0 >= duration || !mVideoView.canSeekBackward()) {
			return;
		}
		int current = mVideoView.getCurrentPosition();
		int backwardTime = (int) (delataX / mScreenWidth * duration);
		int currentTime = current - backwardTime;
		mVideoView.seekTo(currentTime);
		mSeekBar.setProgress(currentTime * 100 / duration);
		mVtSeekBar.setProgress(currentTime * 100 / duration);
		mPlayTimeTextView.setText(formatTime(currentTime));
		mVtPlayTimeTextView.setText(formatTime(currentTime));
	}

	private void forward(float delataX) {

		int duration = mVideoView.getDuration();
		if (0 >= duration || !mVideoView.canSeekForward()) {
			return;
		}
		int current = mVideoView.getCurrentPosition();
		int forwardTime = (int) (delataX / mScreenWidth * duration);
		int currentTime = current + forwardTime;
		mVideoView.seekTo(currentTime);
		mSeekBar.setProgress(currentTime * 100 / duration);
		mVtSeekBar.setProgress(currentTime * 100 / duration);
		mPlayTimeTextView.setText(formatTime(currentTime));
		mVtPlayTimeTextView.setText(formatTime(currentTime));
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
				// // ??????????????????
				// boolean isAdjustAudio = false;
				// if (absDeltaX > threshold && absDeltaY > threshold) {
				// if (absDeltaX < absDeltaY) {
				// isAdjustAudio = true;
				// } else {
				// isAdjustAudio = false;
				// }
				// } else if (absDeltaX < threshold && absDeltaY > threshold) {
				// isAdjustAudio = true;
				// } else if (absDeltaX > threshold && absDeltaY < threshold) {
				// isAdjustAudio = false;
				// } else {
				// return true;
				// }
				// if (isAdjustAudio) {
				// if (x < width / 2) {
				// if (deltaY > 0) {
				// lightDown(absDeltaY);
				// } else if (deltaY < 0) {
				// lightUp(absDeltaY);
				// }
				// } else {
				// if (deltaY > 0) {
				// volumeDown(absDeltaY);
				// } else if (deltaY < 0) {
				// volumeUp(absDeltaY);
				// }
				// }
				//
				// } else {
				if (deltaX > 0) {
					forward(absDeltaX);
				} else if (deltaX < 0) {
					backward(absDeltaX);
				}
				// }
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
		if (mVideoView.isPlaying()) {
			mPlayImageView.setImageResource(R.drawable.player_pause_btn);
			mBtnVtPlay.setBackgroundResource(R.drawable.btn_vt_pause);
		} else {
			mPlayImageView.setImageResource(R.drawable.player_play_btn);
			mBtnVtPlay.setBackgroundResource(R.drawable.btn_vt_play);
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

	private Runnable hideRunnable = new Runnable() {

		@Override
		public void run() {
			showOrHide();
		}
	};
	
	/** ???????????? */
	private boolean isCanRotate = true;
	
	@Override
	protected void hMessage(Message msg) {
		if(100 == msg.what) {
			isCanRotate = true;
		}
	}
	
	private void lockRotate() {
		isCanRotate = false;
		mBaseHandler.sendEmptyMessageDelayed(100, 1000);
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???6???24???
	 */
	private void showOrHide() {
		if (mTopView.getVisibility() == View.VISIBLE) {
			hideOperator();
		} else {
			showOperator();
			try {
				mHandler.removeCallbacks(hideRunnable);
			} catch (Exception e) {
				
			}
			
			mHandler.postDelayed(hideRunnable, HIDE_TIME);
		}
	}

	private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (mIsLand) {
				mDragging = false;
				mHandler.postDelayed(hideRunnable, HIDE_TIME);
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if (mIsLand) {
				mDragging = true;
				mHandler.removeCallbacks(hideRunnable);
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				int time = progress * mVideoView.getDuration() / 100;
				mVideoView.seekTo(time);
			}
		}
	};

	/** ??????????????? */
	private boolean mIsLand = false;
	/** ???????????? */
	private boolean mClick = false;
	/** ?????????????????? */
	private boolean mClickLand = true;
	/** ?????????????????? */
	private boolean mClickPort = true;

	private void auto_port() {
		this.lockRotate();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setFullScreen(false, false);
	}

	private void auto_land(boolean isLeft) {
		this.lockRotate();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (isLeft) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		}
		
		setFullScreen(false, true);
	}

	// ????????????
	private void click_btnFullScreen() {
		if (!isCanRotate) {
			return;
		}
		lockRotate();
		this.mClick = true;
		mIsLand = true;
		mClickLand = false;
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setFullScreen(false, true);
	}

	// ????????????
	private void click_back() {
		if (!isCanRotate) {
			return;
		}
		lockRotate();
		this.mClick = true;
		mIsLand = false;
		mClickPort = true;
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setFullScreen(false, false);
	}

	private void setOrientation(boolean isAuto) {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// land
			GolukDebugUtils.e("", "player---------------------land");
			mIsLand = true;
			mClick = false;
			setFullScreen(isAuto, true);
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// port
			GolukDebugUtils.e("", "player---------------------port");
			setFullScreen(isAuto, false);
		}
	}

	@Override
	public void landscape() {
		if (!isCanRotate) {
			return;
		}
		// ????????????????????????
		if (mClick) {
			if (!mIsLand && !mClickPort) {
				return;
			} else {
				mClickLand = true;
				mClick = false;
				mIsLand = true;
			}
		} else {
			if (!mIsLand) {
				auto_land(true);
				mIsLand = true;
				mClick = false;
			}
		}
	}

	@Override
	public void portrait() {
		if (!isCanRotate) {
			return;
		}
		// ??????????????????
		if (mClick) {
			if (mIsLand && !mClickLand) {
				return;
			} else {
				mClickPort = true;
				mClick = false;
				mIsLand = false;
			}
		} else {
			if (mIsLand) {
				auto_port();
				mIsLand = false;
				mClick = false;
			}
		}
	}

	@Override
	public void landscape_left() {
		if (!isCanRotate) {
			return;
		}
		if (mClick) {
			if (!mIsLand && !mClickPort) {
				return;
			} else {
				mClickLand = true;
				mClick = false;
				mIsLand = true;
			}
		} else {
			if (!mIsLand) {
				auto_land(false);
				mIsLand = true;
				mClick = false;
			}
		}
		
	}
}
