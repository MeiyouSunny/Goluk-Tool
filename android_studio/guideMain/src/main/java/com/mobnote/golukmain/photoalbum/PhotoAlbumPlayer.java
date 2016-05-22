package com.mobnote.golukmain.photoalbum;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
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
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventAddTailer;
import com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import com.mobnote.eventbus.EventDownloadIpcVid;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.photoalbum.OrientationManager.IOrientationFn;
import com.mobnote.golukmain.player.DensityUtil;
import com.mobnote.golukmain.player.FullScreenVideoView;
import com.mobnote.golukmain.player.factory.GolukPlayer;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnCompletionListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnErrorListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnPreparedListener;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;
import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.SimpleExporter;
import cn.npnt.ae.core.MediaUtils;
import cn.npnt.ae.model.VideoEncoderCapability;
import cn.npnt.ae.model.VideoFile;
import de.greenrobot.event.EventBus;

public class PhotoAlbumPlayer extends BaseActivity implements OnClickListener, OnPreparedListener, OnErrorListener,
		OnCompletionListener, IOrientationFn{
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
    private CustomDialog mConfirmDeleteDialog;
	private int mScreenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	/** 视频播放时间 */
	private int mPlayTime = 0;
	/** 头部View */
	private View mTopView;
	/** 底部View */
	private View mBottomView;
	/** 视频播放拖动条 */
	private SeekBar mSeekBar, mVtSeekBar;
	private ImageView mPlayImageView;
	private TextView mPlayTimeTextView, mVtPlayTimeTextView;
	private TextView mDurationTime, mVtDurationTime;
	private ImageView mPlayImg = null;

	private Button mBtnVtPlay;
	private Button mBtnDelete;
	private Button mBtnDownload;

    private TextView mTvShareRightnow;
    private TextView mTvStartVideoEdit;

	/** 加载中布局 */
	private LinearLayout mLoadingLayout = null;
	/** 加载中动画显示控件 */
	private ImageView mLoading = null;
	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	/** 自动隐藏顶部和底部View的时间 */
	private static final int HIDE_TIME = 3000;
	private boolean mDragging;
	private OrientationManager mOrignManager = null;
    private AddTailerDialogFragment mAddTailerDialog;

	private String mExportedFilename;
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
        EventBus.getDefault().register(this);
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
        mAddTailerDialog = new AddTailerDialogFragment();
		initView();

		setOrientation(true);
		mOrignManager = new OrientationManager(this, this);
	}

    public void onEventMainThread(EventAddTailer event){
        if(event != null){
            if(event.getExportStatus() == EventAddTailer.EXPORT_STATUS_EXPORTING){

            }else if(event.getExportStatus() == EventAddTailer.EXPORT_STATUS_FINISH){
                GolukUtils.startVideoEditActivity(this,mType,event.getExprotPath(),mExportedFilename);
                if(mAddTailerDialog != null && mAddTailerDialog.isVisible()){
                    mAddTailerDialog.dismiss();
                }
            }else if(event.getExportStatus() == EventAddTailer.EXPORT_STATUS_FAILED){
            }
        }
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
        EventBus.getDefault().unregister(this);
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

        if (mConfirmDeleteDialog != null && mConfirmDeleteDialog.isShowing()) {
            mConfirmDeleteDialog.dismiss();
        }
        mConfirmDeleteDialog = null;

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
			TextView tvTitleData = (TextView) findViewById(R.id.textview_title_date);
			tvTitleData.setText(mDate.substring(0,10));
            TextView tvTitleTime = (TextView) findViewById(R.id.textview_title_time);
            tvTitleTime.setText(mDate.substring(11,19));
			TextView title = (TextView) findViewById(R.id.title);
			title.setText(mDate);
		}
		mBtnDownload = (Button) findViewById(R.id.btn_download);
		mBtnDelete = (Button) findViewById(R.id.btn_delete);
        mTvStartVideoEdit = (TextView) findViewById(R.id.tv_start_videoedit);
        mTvShareRightnow = (TextView) findViewById(R.id.tv_share_video_rightnow);

        mBtnDownload.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mTvStartVideoEdit.setOnClickListener(this);
        mTvShareRightnow.setOnClickListener(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            mTvStartVideoEdit.setVisibility(View.VISIBLE);
        }else{
            mTvStartVideoEdit.setVisibility(View.GONE);
        }

        if (mVideoFrom.equals("local")) {
			if (mType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG || mType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
			} else {
			}
		} else {
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

        if (!mVideoFrom.equals("local")) {
            mTvStartVideoEdit.setVisibility(View.GONE);
            mTvShareRightnow.setVisibility(View.GONE);
        }
	}

	private void exit() {
		finish();
		mOrignManager.clearListener();
	}

    private void pauseVideo(){
        if (mVideoView.isPlaying() && mVideoView.canPause()) {
            mVideoView.pause();
            mPlayImageView.setImageResource(R.drawable.player_play_btn);
            mBtnVtPlay.setBackgroundResource(R.drawable.btn_vt_play);
        }
    }

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (GolukUtils.isFastDoubleClick()) {
			return;
		}
        if (id == R.id.tv_start_videoedit){
            pauseVideo();
            GolukUtils.startAEActivity(this,mType,mPath);
        }else if (id == R.id.imagebutton_back) {
			// 返回
			exit();
		} else if (id == R.id.tv_share_video_rightnow) {
            pauseVideo();
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
                GolukUtils.startVideoEditActivity(this,mType,mPath,mFileName);
            }else {
                doSimpleExport(mPath,mHP);
            }
		}else if (id == R.id.back_btn) {

			click_back();
		} else if (id == R.id.play_btn || id == R.id.btn_vt_play) {
			if (mVideoView.isPlaying() && mVideoView.canPause()) {
                pauseVideo();
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
                GolukUtils.showToast(this, getString(R.string.str_synchronous_video_loaded));
			} else {
				EventBus.getDefault().post(new EventDownloadIpcVid(mFileName, getType()));
			}
		} else if (id == R.id.btn_delete){
            String tempPath = "";

            if (!TextUtils.isEmpty(mVideoFrom)) {
                if ("local".equals(mVideoFrom)) {
                    tempPath = mPath;
                } else {
                    tempPath = mFileName;
                }
            }
            showConfimDeleteDialog(tempPath);
        }else {
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
	 * 设置播放器全屏
	 *
	 * @param bFull
	 *            true:全屏　false:普通
	 */
	public void setFullScreen(boolean isAuto, boolean bFull) {
		if (bFull == mIsFullScreen) {
			// GolukUtils.showToast(this, "已处于全屏状态.");
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
            mTvStartVideoEdit.setVisibility(View.GONE);
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
            mTvStartVideoEdit.setVisibility(View.VISIBLE);

		}
		mIsFullScreen = bFull;
	}

    private boolean isAllowedDelete(String path) {
        List<String> dlist = GolukApplication.getInstance().getDownLoadList();
        if (dlist.contains(path)) {
            return false;
        }else{
            return true;
        }


    }

    private void showConfimDeleteDialog(final String path) {
        if(mConfirmDeleteDialog==null){
            mConfirmDeleteDialog = new CustomDialog(this);
        }

        mConfirmDeleteDialog.setMessage(this.getString(R.string.str_photo_delete_confirm), Gravity.CENTER);
        mConfirmDeleteDialog.setLeftButton(this.getString(R.string.dialog_str_cancel), null);
        mConfirmDeleteDialog.setRightButton(this.getString(R.string.str_button_ok), new CustomDialog.OnRightClickListener() {

            @Override
            public void onClickListener() {
                // TODO Auto-generated method stub
                mConfirmDeleteDialog.dismiss();
                if(!"local".equals(mVideoFrom)){
                    if(isAllowedDelete(path)){
                        if (!GolukApplication.getInstance().getIpcIsLogin()) {
                            GolukUtils.showToast(PhotoAlbumPlayer.this, PhotoAlbumPlayer.this.getResources().getString(R.string.str_photo_check_ipc_state));
                        }else{
                            EventBus.getDefault().post(new EventDeletePhotoAlbumVid(path,getType()));
                            GolukUtils.showToast(PhotoAlbumPlayer.this, PhotoAlbumPlayer.this.getResources().getString(R.string.str_photo_delete_ok));
                        }

                        PhotoAlbumPlayer.this.finish();
                    }else{
                        GolukUtils.showToast(PhotoAlbumPlayer.this, PhotoAlbumPlayer.this.getResources().getString(R.string.str_photo_downing));
                    }
                }else{
                    EventBus.getDefault().post(new EventDeletePhotoAlbumVid(path,getType()));
                    GolukUtils.showToast(PhotoAlbumPlayer.this, PhotoAlbumPlayer.this.getResources().getString(R.string.str_photo_delete_ok));
                    PhotoAlbumPlayer.this.finish();
                }
            }
        });
        mConfirmDeleteDialog.show();
    }

	/**
	 * 获取播放地址
	 *
	 * @author xuhw
	 * @date 2015年6月5日
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
	 * 提示对话框
	 *
	 * @param msg
	 *            提示信息
	 * @author xuhw
	 * @date 2015年6月5日
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
	 * 显示加载中布局
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
	 * 隐藏加载中显示画面
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
		if (mVideoView.isPlaying()) {
			mPlayImageView.setImageResource(R.drawable.player_pause_btn);
			mBtnVtPlay.setBackgroundResource(R.drawable.btn_vt_pause);
		} else {
			mPlayImageView.setImageResource(R.drawable.player_play_btn);
			mBtnVtPlay.setBackgroundResource(R.drawable.btn_vt_play);
		}
	}

    SimpleExporter mSimpleExporter;
    /**
     * @param srcPath
     * @param qualityStr
     *
     */
    private void doSimpleExport(String srcPath, String qualityStr) {
        if(TextUtils.isEmpty(qualityStr)){
            return;
        }

        mAddTailerDialog.setCancelable(false);
        mAddTailerDialog.show(getSupportFragmentManager(), "dialog_fragment");

        int quality = 0;//0,1,2 分别代表低(480P)，中(720P)，高(1080P)。
        // 初始化，读取gl脚本需要
        MediaUtils.getInstance(this);
        if (mSimpleExporter == null)
            mSimpleExporter = new SimpleExporter(this, mAddTailerDialog);

        try {
            mSimpleExporter.setSourceVideoPath(srcPath);
        } catch (Exception e1) {
            e1.printStackTrace();
            Toast.makeText(this," \"视频源文件加载失败\" + e1.getMessage()",Toast.LENGTH_SHORT);
            return;
        }
        VideoFile videoFileInfo = mSimpleExporter.getVideoFileInfo();

        List<VideoEncoderCapability> capaList = AfterEffect.getSuportedCapability(videoFileInfo.getWidth());
        if (capaList == null || capaList.size() == 0) {
            Toast.makeText(this,"手机不支持合适的分辨率",Toast.LENGTH_SHORT).show();
            return;
        }
        VideoEncoderCapability vc = null;
        if (quality < capaList.size()) {
            vc = capaList.get(quality);
        } else {
            vc = capaList.get(capaList.size() - 1);
        }

        int width = vc.getWidth();
        int height = vc.getHeight();
        float fps = vc.getFps();
        int bitrate = vc.getBitrate();
        String destPath = getExportFilePath();

        addTailerMask(mSimpleExporter);

        Log.i("destPath", "export to:" + destPath);
        mSimpleExporter.export(destPath, width, height, (int) fps, (int) bitrate);

    }
    private String getExportFilePath() {
        String destPath = Environment.getExternalStorageDirectory() + "/Movies/export";//

        File dir = new File(destPath);
        int index = 0;
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            for (String fn : dir.list()) {
                if (!fn.endsWith(".mp4")) {
                    continue;
                }
                try {
                    String name = fn.substring(0, fn.length() - 4);
                    if (name.length() != 2)
                        continue;
                    int i = Integer.valueOf(name);
                    index = Math.max(i, index);
                } catch (Exception e) {

                }

            }
            index++;
        }

        String fileName = String.format("%02d", index);
		mExportedFilename = fileName + ".mp4";
        destPath = destPath + "/" + fileName + ".mp4";
        return destPath;
    }

    private void addTailerMask(SimpleExporter mSimpleExporter) {
        InputStream istr = null;
        try {
            istr = getAssets().open("tailer.png");
            Bitmap bitmap = BitmapFactory.decodeStream(istr);
            Typeface font = Typeface.createFromAsset(this.getAssets(), "PingFang Regular.ttf");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String currDate = df.format(new Date());
            if(currDate == null){
                currDate = "";
			}
            Bitmap tailerBitmap = mSimpleExporter.createTailer(bitmap, getText(R.string.str_default_video_edit_user_name).toString(), currDate, font);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (istr != null)
                try {
                    istr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

	private Runnable hideRunnable = new Runnable() {

		@Override
		public void run() {
			showOrHide();
		}
	};

	/** 控制旋转 */
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
	 * 显示隐藏顶部底部布局
	 *
	 * @author xuhw
	 * @date 2015年6月24日
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

	/** 是否是横屏 */
	private boolean mIsLand = false;
	/** 是否点击 */
	private boolean mClick = false;
	/** 点击进入横屏 */
	private boolean mClickLand = true;
	/** 点击进入竖屏 */
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

	// 开始全屏
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

	// 返回小屏
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
		// 重力感应设置横屏
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
		// 重力感应竖屏
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
