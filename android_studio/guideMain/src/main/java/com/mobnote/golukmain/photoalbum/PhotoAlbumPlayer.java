package com.mobnote.golukmain.photoalbum;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
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
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventAddTailer;
import com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import com.mobnote.eventbus.EventDownloadIpcVid;
import com.mobnote.eventbus.EventShareCompleted;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.photoalbum.OrientationManager.IOrientationFn;
import com.mobnote.golukmain.player.ConfigData;
import com.mobnote.golukmain.player.DensityUtil;
import com.mobnote.golukmain.player.FullScreenVideoView;
import com.mobnote.golukmain.player.factory.GolukPlayer;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnCompletionListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnErrorListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnPreparedListener;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.t1sp.download.DownloaderT1spImpl;
import com.mobnote.t1sp.download.Task;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.Const;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SDKUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.exception.InvalidArgumentException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.Trailer;
import com.rd.vecore.models.VideoConfig;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.SdkService;
import com.rd.veuisdk.manager.CameraConfiguration;
import com.rd.veuisdk.manager.ExportConfiguration;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.manager.VEOSDBuilder;
import com.rd.veuisdk.utils.PathUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;
import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.SimpleExporter;
import cn.npnt.ae.core.MediaUtils;
import cn.npnt.ae.model.VideoEncoderCapability;
import cn.npnt.ae.model.VideoFile;
import de.greenrobot.event.EventBus;

import static com.rd.veuisdk.SdkEntry.editMedia;

public class PhotoAlbumPlayer extends BaseActivity implements OnClickListener, OnPreparedListener, OnErrorListener,
        OnCompletionListener, IOrientationFn {
    private static final String TAG = "PhotoAlbumPlayer";
    private final String MICRO_RESOLUTION = "240p";

    public static final String VIDEO_FROM = "video_from";
    public static final String PATH = "path";
    public static final String DATE = "date";
    public static final String HP = "hp";
    public static final String SIZE = "size";
    public static final String FILENAME = "file_name";
    public static final String TYPE = "type";
    private final int EDIT_REQUEST_CODE = 102;

    /**
     * 活动分享
     */
    public static final String ACTIVITY_INFO = "activityinfo";

    private GolukApplication mApp = null;
    private ImageButton mBackBtn = null;
    private String mDate, mHP, mPath, mVideoFrom, mSize, mFileName, mVideoUrl, mMicroVideoUrl, mImageUrl;
    private int mType;
    private RelativeLayout mVideoViewLayout;
    private FullScreenVideoView mVideoView;
    private boolean mIsFullScreen = false;

    private Handler mHandler = new Handler();
    private CustomDialog mCustomDialog;
    private CustomDialog mConfirmDeleteDialog;
    private int mScreenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
    /**
     * 视频播放时间
     */
    private int mPlayTime = 0;
    /**
     * 头部View
     */
    private View mTopView;
    /**
     * 底部View
     */
    private View mBottomView;
    /**
     * 视频播放拖动条
     */
    private SeekBar mSeekBar, mVtSeekBar;
    private ImageView mPlayImageView;
    private TextView mPlayTimeTextView, mVtPlayTimeTextView;
    private TextView mDurationTime, mVtDurationTime;
    private ImageView mPlayImg = null;

    private Button mBtnVtPlay;
    private Button mBtnDelete;
    private Button mBtnDownload;

    private TextView mTvShareRightnow;
    private TextView mTvT3Hint;
    private LinearLayout mStartVideoeditLl;

    private ViewStub mResolutionHUDViewStub;

    private TextView mResolutionTV;
    private PopupWindow mResolutionPopupWindow;
    private TextView mOriginResolutionTV; //原始码流的分辨率
    private TextView mMicroResolutionTV; //微码流的分辨率

    /**
     * 加载中布局
     */
    private LinearLayout mLoadingLayout = null;
    /**
     * 加载中动画显示控件
     */
    private ProgressBar mLoading = null;
    /**
     * 加载中动画对象
     */
    //private AnimationDrawable mAnimationDrawable = null;
    /**
     * 自动隐藏顶部和底部View的时间
     */
    private static final int HIDE_TIME = 3000;
    private boolean mDragging;
    private OrientationManager mOrignManager = null;
    private AddTailerDialogFragment mAddTailerDialog;

    /**
     * 是否正在使用微码流
     */
    private boolean mIsUsingMicro;

    private boolean isExporting;
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

    private ConfigData configData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoalbum_player);
        EventBus.getDefault().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData(savedInstanceState);

        loadMedia();
    }

    private void loadMedia() {
        initView();

        setOrientation(true);

        getPlayAddr();

        GlideUtils.loadImage(this, mPlayImg, mImageUrl, R.drawable.tacitly_pic);
        startPlay();
        mHandler.postDelayed(mPlayingChecker, 250);
        initEditorUIAndExportConfig();
        registerAllResultHandlers();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData(intent.getExtras());
        loadMedia();
    }

    private void startPlay() {
        if (!TextUtils.isEmpty(mMicroVideoUrl)) {
            mVideoView.setVideoPath(mMicroVideoUrl);
            mIsUsingMicro = true;
            mResolutionTV.setText(MICRO_RESOLUTION);
            XLog.i("start with microSolution");
        } else {
            mVideoView.setVideoPath(mVideoUrl);
            mIsUsingMicro = false;
            mResolutionTV.setText(mHP);
            XLog.i("start with originSolution");
        }
        showLoading();
        mVideoView.requestFocus();
        mVideoView.start();
    }

    private void changeResolution(boolean playMicro) {
        if (mIsUsingMicro == playMicro) {
            // if curr resolution equals changeTo resolution， do nothing
            return;
        }
        if (playMicro) {
            mVideoView.setVideoPath(mMicroVideoUrl);
            mIsUsingMicro = true;
            mResolutionTV.setText(MICRO_RESOLUTION);
            XLog.i("change 2 microSolution");
        } else {
            mVideoView.setVideoPath(mVideoUrl);
            mIsUsingMicro = false;
            mResolutionTV.setText(mHP);
            XLog.i("change 2 originSolution");
        }
        mVideoView.requestFocus();
        if (mVideoView.isPlaying()) {
            mVideoView.start();
        }
    }

    private void initData(Bundle savedInstanceState) {
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
        mOrignManager = new OrientationManager(this, this);
    }

    public void onEventMainThread(EventShareCompleted event) {
        if (event != null) {
            //Toast.makeText(this, getString(R.string.str_share_success), Toast.LENGTH_SHORT).show();
            exit();
        }
    }

    public void onEventMainThread(EventAddTailer event) {
        if (event != null) {
            if (event.getExportStatus() == EventAddTailer.EXPORT_STATUS_EXPORTING) {

            } else if (event.getExportStatus() == EventAddTailer.EXPORT_STATUS_FINISH) {

                isExporting = false;
                //竖屏播放页访问即刻分享页面统计
                ZhugeUtils.eventShare(this, this.getString(R.string.str_zhuge_share_video_player));

                GolukUtils.startVideoShareActivity(this, mType, event.getExportPath(), mExportedFilename,
                        true, mVideoView.getDuration(), mHP, (PromotionSelectItem) getIntent().getSerializableExtra(ACTIVITY_INFO));
                if (mAddTailerDialog != null && mAddTailerDialog.isVisible()) {
                    mAddTailerDialog.dismissAllowingStateLoss();
                }
            } else if (event.getExportStatus() == EventAddTailer.EXPORT_STATUS_FAILED) {
                //竖屏播放页访问即刻分享页面统计
                ZhugeUtils.eventShare(this, this.getString(R.string.str_zhuge_share_video_player));

                //导出失败，则直接分享原视频
                GolukUtils.startVideoShareActivity(this, mType, mPath, mFileName,
                        false, mVideoView.getDuration(), mHP, (PromotionSelectItem) getIntent().getSerializableExtra(ACTIVITY_INFO));
                if (mAddTailerDialog != null && mAddTailerDialog.isVisible()) {
                    mAddTailerDialog.dismissAllowingStateLoss();
                }
                isExporting = false;
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
        isCanRotate = true;
        mApp.setContext(this, TAG);
        if (mResume) {
            mVideoView.seekTo(mPlayTime);
            mVideoView.resume();
        }
        mHandler.post(mProgressChecker);
        mHandler.post(mPlayingChecker);

        /**
         * if it`is the first time show the page, show the resolution HUD
         */
        if (!SharedPrefUtil.getIsShowedResolutionHud() && !TextUtils.isEmpty(mMicroVideoUrl)) {
            mResolutionHUDViewStub = (ViewStub) findViewById(R.id.stub_resolution_hud);
            View view = mResolutionHUDViewStub.inflate();
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mResolutionHUDViewStub.setVisibility(View.GONE);
                }
            });
            SharedPrefUtil.setIsShowedResolutionHud(true);
        }
    }

    private boolean mResume = false;

    @Override
    protected void onPause() {
        super.onPause();
        mResume = true;
        isCanRotate = false;
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

        if (mAddTailerDialog != null && mAddTailerDialog.isVisible()) {
            mAddTailerDialog.dismissAllowingStateLoss();
        }
        mAddTailerDialog = null;

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
        mResolutionTV = (TextView) findViewById(R.id.tv_resolution);
        mLoading = (ProgressBar) findViewById(R.id.mLoading);
        //   mLoading.setBackgroundResource(R.anim.video_loading);
        //     mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();

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

        if (!TextUtils.isEmpty(mHP)) {
            mResolutionTV.setText(mHP);
        }

        TextView tvTitleData = (TextView) findViewById(R.id.textview_title_date);
        TextView tvTitleTime = (TextView) findViewById(R.id.textview_title_time);
        // eg: 20180131135629
        if (!TextUtils.isEmpty(mDate)) {
            if (mDate.length() >= 19) {
                tvTitleData.setText(mDate.substring(0, 10));
                tvTitleTime.setText(mDate.substring(11, 19));
            } else if (mDate.length() >= 14) {
                String dateText = mDate.substring(0, 4) + "-" + mDate.substring(4, 6) + "-" + mDate.substring(6, 8);
                tvTitleData.setText(dateText);
                String timeText = mDate.substring(8, 10) + ":" + mDate.substring(10, 12) + ":" + mDate.substring(12, 14);
                tvTitleTime.setText(timeText);
            }
        }

        mBtnDownload = (Button) findViewById(R.id.btn_download);
        if (!TextUtils.isEmpty(mVideoFrom) && "local".equals(mVideoFrom)) {
            mBtnDownload.setVisibility(View.GONE);
        }
        mBtnDelete = (Button) findViewById(R.id.btn_delete);
        mStartVideoeditLl = (LinearLayout) findViewById(R.id.ll_start_videoedit);
        mTvShareRightnow = (TextView) findViewById(R.id.tv_share_video_rightnow);

        mResolutionTV.setOnClickListener(this);
        mBtnDownload.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mStartVideoeditLl.setOnClickListener(this);
        mTvShareRightnow.setOnClickListener(this);
        if (videoEditSupport()) {
            mStartVideoeditLl.setVisibility(View.VISIBLE);
        } else {
            mStartVideoeditLl.setVisibility(View.GONE);
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

        if (mVideoFrom.equals("local")) {
            /** 如果是本地循环视频，不显示分享按钮，但要显示编辑按钮 */
            if (!TextUtils.isEmpty(mFileName) && mFileName.startsWith("NRM")) {
                mTvShareRightnow.setVisibility(View.GONE);
                mStartVideoeditLl.setVisibility(View.VISIBLE);
            }
        } else {
            mStartVideoeditLl.setVisibility(View.GONE);
            mTvShareRightnow.setVisibility(View.GONE);
        }
        mTvT3Hint = (TextView) findViewById(R.id.tv_t3_hint);
        if (mType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP && mVideoFrom.equals("ipc") && GolukApplication.getInstance().getIPCControlManager().needShowT3Hint()) {
            mTvT3Hint.setVisibility(View.VISIBLE);
        } else {
            mTvT3Hint.setVisibility(View.GONE);
        }
    }

    private boolean videoEditSupport() {
        // judge android os
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }

        // forbid some manufacturers, now htc
//        if(android.os.Build.MANUFACTURER.toLowerCase().contains("htc")) {
//            return false;
//        }
        return true;
    }

    private void exit() {
        finish();
        mOrignManager.clearListener();
    }

    private void pauseVideo() {
        if (mVideoView.isPlaying() && mVideoView.canPause()) {
            mVideoView.pause();
            mPlayImageView.setImageResource(R.drawable.player_play_btn);
            mBtnVtPlay.setBackgroundResource(R.drawable.btn_vt_play);
        }
    }

    private void showOrHideResolutionPopupwindow() {
        if (TextUtils.isEmpty(mMicroVideoUrl)) {
            return;
        }
        if (mVideoFrom.equals("local")) {
            return;
        }
        if (mType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG || mType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
            return;
        }

        if (mResolutionPopupWindow == null) {
            View resolutionView = LayoutInflater.from(this).inflate(R.layout.resolution_pop_window, null);
            mOriginResolutionTV = (TextView) resolutionView.findViewById(R.id.tv_resolution_origin);
            mMicroResolutionTV = (TextView) resolutionView.findViewById(R.id.tv_resolution_micro);

            mOriginResolutionTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mResolutionPopupWindow.dismiss();
                    changeResolution(false);
                }
            });
            mMicroResolutionTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mResolutionPopupWindow.dismiss();
                    changeResolution(true);
                }
            });

            if (!TextUtils.isEmpty(mHP)) {
                mOriginResolutionTV.setText(mHP);
            }

            mResolutionPopupWindow = new PopupWindow(this);
            mResolutionPopupWindow.setContentView(resolutionView);
            mResolutionPopupWindow.setBackgroundDrawable(null);
        }
        if (mResolutionPopupWindow.isShowing()) {
            mResolutionPopupWindow.dismiss();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                int[] a = new int[2];
                mResolutionTV.getLocationInWindow(a);
                mResolutionPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, a[1] + mResolutionTV.getHeight());
            } else {
                mResolutionPopupWindow.showAsDropDown(mResolutionTV);
            }
        }
        mResolutionTV.post(new Runnable() {
            @Override
            public void run() {
                mResolutionPopupWindow.showAsDropDown(mResolutionTV);
                mResolutionPopupWindow.update(mResolutionTV, 0, 0, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (GolukUtils.isFastDoubleClick()) {
            return;
        }
        if (id == R.id.tv_resolution) {
            showOrHideResolutionPopupwindow();
        }
        if (id == R.id.ll_start_videoedit) {
            pauseVideo();
            startEditVideo(mPath);
        } else if (id == R.id.imagebutton_back) {
            // 返回
            exit();
        } else if (id == R.id.tv_share_video_rightnow) {
            if (!SharePlatformUtil.checkShareableWhenNotHotspot(PhotoAlbumPlayer.this)) return;
            pauseVideo();
            ZhugeUtils.eventShare(this, this.getString(R.string.str_zhuge_share_video_player));
            //addTailerByRd();
            GolukUtils.startVideoShareActivity(PhotoAlbumPlayer.this, mType, mPath, mFileName, false,
                    mVideoView.getDuration(), mHP, (PromotionSelectItem) getIntent().getSerializableExtra(ACTIVITY_INFO));
        } else if (id == R.id.back_btn) {

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
                //相册详情页面-下载到本地
                ZhugeUtils.eventAlbumDownloadVideo(PhotoAlbumPlayer.this);
                EventBus.getDefault().post(new EventDownloadIpcVid(mPath, getType()));
            }
        } else if (id == R.id.btn_delete) {
//            String tempPath = "";
//
//            if (!TextUtils.isEmpty(mVideoFrom)) {
//                if ("local".equals(mVideoFrom)) {
//                    tempPath = mPath;
//                } else {
//                    tempPath = mFileName;
//                }
//            }
            showConfimDeleteDialog(mPath);
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
     * 设置播放器全屏
     *
     * @param bFull true:全屏　false:普通
     */
    public void setFullScreen(boolean isAuto, boolean bFull) {
        if (mResolutionHUDViewStub != null) {
            mResolutionHUDViewStub.setVisibility(View.GONE);
        }
        if (mResolutionPopupWindow != null && mResolutionPopupWindow.isShowing()) {
            mResolutionPopupWindow.dismiss();
        }
        if (bFull == mIsFullScreen) {
            // GolukUtils.showToast(this, "已处于全屏状态.");
            return;
        }
        if (bFull) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideoViewLayout.getLayoutParams();
            params.width = LayoutParams.MATCH_PARENT;
            params.height = LayoutParams.MATCH_PARENT;
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
            mStartVideoeditLl.setVisibility(View.GONE);
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
            int heightSet = (int) (((float) mVideoHeight / mVideoWidth) * mScreenWidth);
            //lp.height = (int) (lp.width / 1.777);
            lp.height = heightSet;
            lp.leftMargin = 0;
            lp.addRule(RelativeLayout.BELOW, R.id.RelativeLayout_videoinfo);
            mVideoViewLayout.setLayoutParams(lp);
            if (!mVideoFrom.equals("local")) {
                mStartVideoeditLl.setVisibility(View.GONE);
            } else {
                mStartVideoeditLl.setVisibility(View.VISIBLE);
            }

        }
        mIsFullScreen = bFull;
    }

    private boolean isAllowedDelete(String path) {
        List<Task> tasks = DownloaderT1spImpl.getInstance().getDownloadList();
        if (!CollectionUtils.isEmpty(tasks)) {
            for (Task downloadTask : tasks) {
                if (TextUtils.equals(path, downloadTask.downloadPath))
                    return false;
            }
        }

        return true;
    }

    private void showConfimDeleteDialog(final String path) {
        if (mConfirmDeleteDialog == null) {
            mConfirmDeleteDialog = new CustomDialog(this);
        }

        mConfirmDeleteDialog.setMessage(this.getString(R.string.str_photo_delete_confirm), Gravity.CENTER);
        mConfirmDeleteDialog.setLeftButton(this.getString(R.string.dialog_str_cancel), null);
        mConfirmDeleteDialog.setRightButton(this.getString(R.string.str_button_ok), new CustomDialog.OnRightClickListener() {

            @Override
            public void onClickListener() {
                // TODO Auto-generated method stub
                mConfirmDeleteDialog.dismiss();
                if (!"local".equals(mVideoFrom)) {
                    if (isAllowedDelete(path)) {
                        if (!GolukApplication.getInstance().getIpcIsLogin()) {
                            GolukUtils.showToast(PhotoAlbumPlayer.this, PhotoAlbumPlayer.this.getResources().getString(R.string.str_photo_check_ipc_state));
                        } else {
                            //相册详情页面-删除视频
                            ZhugeUtils.eventAlbumDeleteVideo(PhotoAlbumPlayer.this);
                            EventBus.getDefault().post(new EventDeletePhotoAlbumVid(path, getType()));
                            GolukUtils.showToast(PhotoAlbumPlayer.this, PhotoAlbumPlayer.this.getResources().getString(R.string.str_photo_delete_ok));
                        }

                        PhotoAlbumPlayer.this.finish();
                    } else {
                        GolukUtils.showToast(PhotoAlbumPlayer.this, PhotoAlbumPlayer.this.getResources().getString(R.string.str_photo_downing));
                    }
                } else {
                    //相册详情页面-删除视频
                    ZhugeUtils.eventAlbumDeleteVideo(PhotoAlbumPlayer.this);
                    EventBus.getDefault().post(new EventDeletePhotoAlbumVid(path, getType()));
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
        // T1SP
        if (mPath.contains(Const.HTTP_SCHEMA + Const.IP)) {
            mVideoUrl = mPath;
            return;
        }

        // Other
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
            if (IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)
                    || IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)) {
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

                // 仅远程循环视频支持微码流播放
                if (mType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP) {
                    mMicroVideoUrl = "http://" + ip + "/api/mini_mp4?id=" + fileName;
                }
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

        GolukDebugUtils.e(TAG, "YYYYYY==VideoPlayerActivity==vurl=" + mVideoUrl);
    }

    /**
     * 提示对话框
     *
     * @param msg 提示信息
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

    private int mVideoWidth, mVideoHeight;

    @Override
    public void onPrepared(GolukPlayer mp) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();
        updateLayoutSize(mVideoWidth, mVideoHeight);
        if (null != mDurationTime) {
            mDurationTime.setText(formatTime(mVideoView.getDuration()));
            mVtDurationTime.setText(formatTime(mVideoView.getDuration()));
        }
    }

    /**
     * 根据视频实际大小来设置视频控件大小
     */
    private void updateLayoutSize(int videoWidth, int videoHeight) {
        int playerWidth = mVideoViewLayout.getWidth();
        int heightSet = (int) (((float) videoHeight / videoWidth) * playerWidth);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVideoViewLayout.getLayoutParams();
        layoutParams.height = heightSet;
        mVideoViewLayout.setLayoutParams(layoutParams);
    }

    private boolean isShow = false;

    /**
     * 显示加载中布局
     */
    private void showLoading() {
        if (!isShow) {
            isShow = true;
            mLoadingLayout.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.VISIBLE);
//            mLoading.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (mAnimationDrawable != null) {
//                        if (!mAnimationDrawable.isRunning()) {
//                            mAnimationDrawable.start();
//                        }
//                    }
//                }
//            }, 100);
        }
    }

    /**
     * 隐藏加载中显示画面
     */
    private void hideLoading() {
        mPlayImg.setVisibility(View.GONE);
        if (isShow) {
            isShow = false;

//            if (mAnimationDrawable != null) {
//                if (mAnimationDrawable.isRunning()) {
//                    mAnimationDrawable.stop();
//                }
//            }
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
     */
    private void doSimpleExport(String srcPath, String qualityStr) {
        if (isExporting) {
            return;
        }
        if (TextUtils.isEmpty(qualityStr)) {
            return;
        }
        if (mAddTailerDialog == null) {
            mAddTailerDialog = new AddTailerDialogFragment();
        }
        int quality = 0;//0,1,2 分别代表低(480P)，中(720P)，高(1080P)。
        if (!TextUtils.isEmpty(mHP)) {
            if ("480p".equalsIgnoreCase(mHP)) {
                quality = 0;
            } else if ("720p".equalsIgnoreCase(mHP)) {
                quality = 1;
            } else if ("1080p".equalsIgnoreCase(mHP)) {
                quality = 2;
            }
        }
        isExporting = true;

        // 初始化，读取gl脚本需要
        MediaUtils.getInstance(this);
        if (mSimpleExporter == null)
            mSimpleExporter = new SimpleExporter(this, mAddTailerDialog);

        try {
            mSimpleExporter.setSourceVideoPath(srcPath);
        } catch (Exception e1) {
            e1.printStackTrace();
            Toast.makeText(this, getString(R.string.load_video_fail), Toast.LENGTH_SHORT);
            isExporting = false;
            GolukUtils.startVideoShareActivity(this, mType, mPath, mFileName,
                    false, mVideoView.getDuration(), mHP, (PromotionSelectItem) getIntent().getSerializableExtra(ACTIVITY_INFO));
            return;
        }
        VideoFile videoFileInfo = mSimpleExporter.getVideoFileInfo();

        List<VideoEncoderCapability> capaList = AfterEffect.getSuportedCapability(videoFileInfo.getWidth());
        if (capaList == null || capaList.size() == 0) {
            Toast.makeText(this, getString(R.string.not_supported_resolution), Toast.LENGTH_SHORT).show();
            isExporting = false;
            GolukUtils.startVideoShareActivity(this, mType, mPath, mFileName,
                    false, mVideoView.getDuration(), mHP, (PromotionSelectItem) getIntent().getSerializableExtra(ACTIVITY_INFO));
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
        mAddTailerDialog.setCancelable(false);
        mAddTailerDialog.show(getSupportFragmentManager(), "dialog_fragment");
        mSimpleExporter.export(destPath, width, height, (int) fps, (int) bitrate);

    }

    private String getExportFilePath() {
        String destPath = Environment.getExternalStorageDirectory() + "/goluk/export";//

        File dir = new File(destPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String currTime = df.format(new Date());
        if (currTime == null) {
            currTime = "";
        }

        mExportedFilename = "vid" + currTime + ".mp4";
        destPath = destPath + "/" + "vid" + currTime + ".mp4";
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
            if (currDate == null) {
                currDate = "";
            }
            String nickName;
            if (GolukApplication.getInstance().isUserLoginSucess) {
                UserInfo userInfo = mApp.getMyInfo();
                nickName = userInfo.nickname;
            } else {
                nickName = getString(R.string.str_default_video_edit_user_name);
            }
            Bitmap tailerBitmap = mSimpleExporter.createTailer(bitmap, nickName, currDate, font);
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

    /**
     * 控制旋转
     */
    private boolean isCanRotate = true;

    @Override
    protected void hMessage(Message msg) {
        if (100 == msg.what) {
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

    /**
     * 是否是横屏
     */
    private boolean mIsLand = false;
    /**
     * 是否点击
     */
    private boolean mClick = false;
    /**
     * 点击进入横屏
     */
    private boolean mClickLand = true;
    /**
     * 点击进入竖屏
     */
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

    private SparseArray<ActivityResultHandler> registeredActivityResultHandlers;

    private ConfigData initAndGetConfigData() {
        if (configData == null) {
            configData = new ConfigData();
        }
//        Uri fileUri = Uri.parse("android.resource://"+  BuildConfig.APPLICATION_ID +"/" + R.drawable.img_ae_trailer);
        String nickName;
        if (GolukApplication.getInstance().isUserLoginSucess) {
            UserInfo userInfo = mApp.getMyInfo();
            nickName = userInfo.nickname;
        } else {
            nickName = getString(R.string.str_default_video_edit_user_name);
        }
        configData.videoTrailerPath = SDKUtils.createVideoTrailerImage(this, nickName, 480, 50, 50);
        return configData;
    }

    private void initEditorUIAndExportConfig() {
        initAndGetConfigData();
        // 视频编辑UI配置
        UIConfiguration uiConfig = new UIConfiguration.Builder()
                // 设置是否使用自定义相册
                .useCustomAlbum(true)
                // 设置向导化
                .enableWizard(configData.enableWizard)
                // 设置自动播放
                .enableAutoRepeat(configData.enableAutoRepeat)
                // 设置MV和mv网络地址
                .enableMV(configData.enableMV, ConfigData.WEB_MV_URL)
                // 配音模式
                .setVoiceLayoutType(configData.voiceLayoutType)
                // 设置秀拍客相册支持格式
                .setAlbumSupportFormat(configData.albumSupportFormatType)
                // 设置默认进入界面画面比例
                .setVideoProportion(configData.videoProportionType)
                // 设置滤镜界面风格
                .setFilterType(configData.filterLayoutType)
                // 设置相册媒体选择数量上限(目前只对相册接口生效)
                .setMediaCountLimit(configData.albumMediaCountLimit)
                // 设置相册是否显示跳转拍摄按钮(目前只对相册接口生效)
                .enableAlbumCamera(configData.enableAlbumCamera)
                // 编辑与导出模块显示与隐藏（默认不设置为显示）
                .setEditAndExportModuleVisibility(
                        UIConfiguration.EditAndExportModules.SOUNDTRACK,
                        configData.enableSoundTrack)
                .setEditAndExportModuleVisibility(UIConfiguration.EditAndExportModules.DUBBING,
                        configData.enableDubbing)
                .setEditAndExportModuleVisibility(UIConfiguration.EditAndExportModules.FILTER,
                        configData.enableFilter)
                .setEditAndExportModuleVisibility(UIConfiguration.EditAndExportModules.TITLING,
                        configData.enableTitling)
                .setEditAndExportModuleVisibility(
                        UIConfiguration.EditAndExportModules.SPECIAL_EFFECTS,
                        configData.enableSpecialEffects)
                .setEditAndExportModuleVisibility(
                        UIConfiguration.EditAndExportModules.CLIP_EDITING,
                        configData.enableClipEditing)
                // 片段编辑模块显示与隐藏（默认不设置为显示）
                .setClipEditingModuleVisibility(
                        UIConfiguration.ClipEditingModules.IMAGE_DURATION_CONTROL,
                        configData.enableImageDuration)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.EDIT,
                        configData.enableEdit)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.TRIM,
                        configData.enableTrim)
                .setClipEditingModuleVisibility(
                        UIConfiguration.ClipEditingModules.VIDEO_SPEED_CONTROL,
                        configData.enableVideoSpeed)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.SPLIT,
                        configData.enableSplit)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.COPY,
                        configData.enableCopy)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.PROPORTION,
                        configData.enableProportion)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.SORT,
                        configData.enableSort)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.TEXT,
                        configData.enableText)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.REVERSE,
                        configData.enableReverse)
                .setClipEditingModuleVisibility(UIConfiguration.ClipEditingModules.TRANSITION, false)
                .enableLocalMusic(configData.enableLocalMusic)
                // 设置自定义的网络音乐
                .setMusicUrl(ConfigData.MUSIC_URL)
                // 设置云音乐
                .setCloudMusicUrl("")
                // 字幕、特效在mv的上面
                .enableTitlingAndSpecialEffectOuter(configData.enableTitlingAndSpecialEffectOuter)
                .get();

        // 导出视频参数配置
        ExportConfiguration exportConfig = new ExportConfiguration.Builder()
                // 设置保存路径，传null或不设置
                // 将保存至默认路径(即调用SdkEntry.initialize初始时自定义路径）
                //.setSavePath(null)
                // 设置片尾图片路径，传null或者不设置 将没有片尾
                .setTrailerPath(configData.videoTrailerPath)
                // 设置片尾时长 单位s 默认2s
                .setTrailerDuration(2)
                // 设置导出视频时长 单位ms 传0或者不设置 将导出完整视频
                .setVideoDuration(configData.exportVideoDuration)
                // 设置水印路径
                //.setWatermarkPath(configData.enableWatermark ? EDIT_WATERMARK_PATH : null)
                // 设置水印位置
                .setWatermarkPosition(configData.watermarkShowRectF).get();

        // 获取秀拍客配置服务器
        SdkService sdkService = SdkEntry.getSdkService();
        if (null != sdkService) {
            // 初始化所有配置
            sdkService.initConfiguration(exportConfig, uiConfig);
        }
    }

    private void registerAllResultHandlers() {
        registerActivityResultHandler(EDIT_REQUEST_CODE, editResultHandler);
    }

    private void registerActivityResultHandler(int requestCode,
                                               ActivityResultHandler handler) {
        if (null == registeredActivityResultHandlers) {
            registeredActivityResultHandlers = new SparseArray<ActivityResultHandler>();
        }
        registeredActivityResultHandlers.put(requestCode, handler);
    }

    private ActivityResultHandler editResultHandler = new ActivityResultHandler() {

        @Override
        public void onActivityResult(Context context, int resultCode,
                                     Intent data) {
            if (resultCode == RESULT_OK && null != data) {
                String mediaPath = data.getStringExtra(SdkEntry.EDIT_RESULT);
                if (mediaPath != null) {
                    Log.d(TAG, mediaPath);
                    //Toast.makeText(context, mediaPath, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private interface ActivityResultHandler {
        /**
         * 响应
         *
         * @param context
         * @param resultCode The integer result code returned by the child activity
         *                   through its setResult().
         * @param data       An Intent, which can return result data to the caller
         *                   (various data can be attached to Intent "extras").
         */
        void onActivityResult(Context context, int resultCode, Intent data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != registeredActivityResultHandlers) {
            ActivityResultHandler handler = registeredActivityResultHandlers
                    .get(requestCode);
            if (null != handler) {
                handler.onActivityResult(this, resultCode, data);
            }
        }
    }

    private void startEditVideo(String videoPath) {
        //GolukUtils.startAEActivity(this, mType, mPath, (PromotionSelectItem) getIntent().getSerializableExtra(ACTIVITY_INFO));
        initCameraConfig(CameraConfiguration.SQUARE_SCREEN_CAN_CHANGE);
//                SdkEntry.getSdkService().initConfiguration(
//                        new CameraConfiguration.Builder().get());
        ArrayList<String> list = new ArrayList<String>();
        list.add(videoPath);
        try {
            editMedia(this, list, EDIT_REQUEST_CODE);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    private void initCameraConfig(int UIType) {
        SdkEntry.getSdkService().initConfiguration(
                new CameraConfiguration.Builder()
                        // 可设置最小录制时长,0代表不限制
                        .setVideoMinTime(configData.cameraMinTime)
                        // 可设置最大录制时长,0代表不限制
                        .setVideoMaxTime(configData.cameraMaxTime)
                        // 为true代表多次拍摄，拍摄完成一段之后，将保存至相册并开始下一段拍摄，默认为false单次拍摄，拍摄完成后返回资源地址
                        .useMultiShoot(configData.useMultiShoot)
                        /**
                         * 设置录制时默认界面:<br>
                         * 默认16：9录制:<br>
                         * CameraConfiguration. WIDE_SCREEN_CAN_CHANGE<br>
                         * 默认1：1:<br>
                         * CameraConfiguration. SQUARE_SCREEN_CAN_CHANGE<br>
                         * 仅16：9录制:<br>
                         * CameraConfiguration.ONLY_SCREEN_SCREEN 仅1：1录制:<br>
                         * CameraConfiguration.ONLY_SQUARE_SCREEN
                         */
                        .setCameraUIType(UIType)
                        // 设置拍摄完成后，是否保存至相册（仅单次拍摄方式有效），同时通过onActivityResult及SIMPLE_CAMERA_REQUEST_CODE返回
                        .setSingleCameraSaveToAlbum(configData.isSaveToAlbum)
                        // 设置录制时是否静音，true代表录制后无声音
                        .setAudioMute(false)
                        // 设置是否启用人脸贴纸功能
                        .enableFaceu(configData.isDefaultFace)
                        // 设置人脸贴纸鉴权证书
                        //.setPack(authpack.A())
                        // 设置是否默认为后置摄像头
                        .setDefaultRearCamera(configData.isDefaultRearCamera)
                        // 是否显示相册按钮
                        .enableAlbum(configData.enableAlbum)
                        // 是否使用自定义相册
                        .useCustomAlbum(configData.useCustomAlbum)
                        // 设置隐藏拍摄功能（全部隐藏将强制开启视频拍摄）
                        .hideMV(configData.hideMV)
                        .hidePhoto(configData.hidePhoto)
                        .hideRec(configData.hideRec)
                        // 设置mv最小时长
                        .setCameraMVMinTime(configData.cameraMVMinTime)
                        // 设置mv最大时长
                        .setCameraMVMaxTime(configData.cameraMVMaxTime)
                        // 开启相机水印时需注册水印
                        // SdkEntry.registerOSDBuilder(CameraWatermarkBuilder.class);
                        // 相机录制水印
                        .enableWatermark(configData.enableCameraWatermark)
                        // 相机水印片头
                        .setCameraTrailerTime(VEOSDBuilder.OSDState.header, 2f)
                        // 相机录制结束时片尾水印时长(0-1.0 单位：秒)
                        .setCameraTrailerTime(VEOSDBuilder.OSDState.end,
                                configData.cameraWatermarkEnd)
                        // 是否启用防篡改录制
                        .enableAntiChange(configData.enableAntiChange)
                        // 启用前置输出时镜像
                        .enableFrontMirror(configData.enableFrontMirror)
                        // 固定录制界面的方向
                        .setOrientation(configData.mRecordOrientation)
                        // 是否支持录制时播放音乐
                        .enablePlayMusic(configData.enablePlayMusic)
                        // 是否美颜
                        .enableBeauty(configData.enableBeauty).get());
    }

    private void addTailerByRd() {
        VideoConfig config = new VideoConfig();
        config.enableHWEncoder(CoreUtils.hasJELLY_BEAN_MR2());
        config.enableHWDecoder(CoreUtils.hasJELLY_BEAN_MR2());
        config.setVideoSize(mVideoView.getVideoWidth(), mVideoView.getVideoHeight());
        String strSaveMp4FileName = PathUtils.getMp4FileNameForSdcard();
        String nickName;
        if (GolukApplication.getInstance().isUserLoginSucess) {
            UserInfo userInfo = mApp.getMyInfo();
            nickName = userInfo.nickname;
        } else {
            nickName = getString(R.string.str_default_video_edit_user_name);
        }
        configData.videoTrailerPath = SDKUtils.createVideoTrailerImage(this, nickName, 480, 50, 50);
        Trailer trailer = new Trailer(configData.videoTrailerPath, 2, 0.5f);
        ArrayList<String> videos = new ArrayList<>();
        videos.add(mPath);
        ExportVideoLisenter mExportListener = new ExportVideoLisenter(strSaveMp4FileName);
        try {
            SdkEntry.exportVideo(this, config, videos, strSaveMp4FileName, null, trailer, mExportListener);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出视频的回调演示
     */
    private class ExportVideoLisenter implements ExportListener {
        private String mPath;
        private AlertDialog mDialog;
        private ProgressBar mProgressBar;
        private Button mBtnCancel;
        private TextView mTvTitle;

        public ExportVideoLisenter(String videoPath) {
            mPath = videoPath;
        }

        @Override
        public void onExportStart() {
            View v = LayoutInflater.from(PhotoAlbumPlayer.this).inflate(
                    R.layout.progress_view, null);
            mTvTitle = (TextView) v.findViewById(R.id.tvTitle);
            mTvTitle.setText(R.string.exporting);
            mProgressBar = (ProgressBar) v.findViewById(R.id.pbCompress);
            mBtnCancel = (Button) v.findViewById(R.id.btnCancelCompress);
            mBtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SdkEntry.cancelExport();
                }
            });
            mDialog = new AlertDialog.Builder(PhotoAlbumPlayer.this).setView(v)
                    .show();
            mDialog.setCanceledOnTouchOutside(false);
        }

        /**
         * 导出进度回调
         *
         * @param progress 当前进度
         * @param max      最大进度
         * @return 返回是否继续执行，false为终止导出
         */
        @Override
        public boolean onExporting(int progress, int max) {
            if (mProgressBar != null) {
                mProgressBar.setMax(max);
                mProgressBar.setProgress(progress);
            }
            return true;
        }

        @Override
        public void onExportEnd(int result) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            if (result >= VirtualVideo.RESULT_SUCCESS) {
                GolukUtils.startVideoShareActivity(PhotoAlbumPlayer.this, mType, mPath, mFileName, false,
                        mVideoView.getDuration(), mHP, (PromotionSelectItem) getIntent().getSerializableExtra(ACTIVITY_INFO));
            } else if (result != VirtualVideo.RESULT_SAVE_CANCEL) {
                Log.e(TAG, "onExportEnd: " + result);
                Toast.makeText(PhotoAlbumPlayer.this, getString(R.string.export_error) + result, Toast.LENGTH_LONG).show();
            }
        }
    }

}
