package com.mobnote.t1sp.ui.album;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
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
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventAddTailer;
import com.mobnote.eventbus.EventDeletePhotoAlbumVid;
import com.mobnote.eventbus.EventShareCompleted;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomDialog.OnLeftClickListener;
import com.mobnote.golukmain.photoalbum.AddTailerDialogFragment;
import com.mobnote.golukmain.photoalbum.OrientationManager;
import com.mobnote.golukmain.photoalbum.OrientationManager.IOrientationFn;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.player.DensityUtil;
import com.mobnote.golukmain.player.FullScreenVideoView;
import com.mobnote.golukmain.player.factory.GolukPlayer;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnCompletionListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnErrorListener;
import com.mobnote.golukmain.player.factory.GolukPlayer.OnPreparedListener;
import com.mobnote.t1sp.gps.F5GpsDataParser;
import com.mobnote.t1sp.gps.GPSData;
import com.mobnote.t1sp.map.MapTrackView;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.GpsUtil;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * T1SP 本地视频播放页面
 * 上部分值视频播放
 * 下部分是地图轨迹
 */
public class PhotoAlbumPlayerF5 extends BaseActivity implements OnClickListener, OnPreparedListener, OnErrorListener,
        OnCompletionListener, IOrientationFn, F5GpsDataParser.GpsDataListener {
    private static final String TAG = "PhotoAlbumPlayerF5";
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

    private ImageView mBtnDelete;

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

    // 总时长(毫秒)
    private long mDuration;
    private ImageView mBtnPlay;
    // 总里程, 总用时, 平均速度
    private TextView mTvTotalMails, mTvTotalTime, mTvAverageSpeed;
    private LinearLayout mLayoutGpsInfo, mLayoutOptions;
    private RelativeLayout mLayoutTitle, mLayoutMap;
    // 轨迹数据
    private List<GPSData> mGpsList;

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

    /////////////////
    private MapTrackView mMapTrackView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoalbum_player_f5);
        EventBus.getDefault().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData(savedInstanceState);

        loadMedia();

        /////////////////////
        mMapTrackView = MapTrackView.create(this);
        mMapTrackView.onCreate(savedInstanceState);
        //mMapTrackView.setMapListener(this);
        RelativeLayout mapParent = (RelativeLayout) findViewById(R.id.tarck_map_parent_view);
        mapParent.addView(mMapTrackView);

        gpsDataTest();
    }

    private void gpsDataTest() {
        File gpsFile = new File(mPath);
        if (gpsFile.exists()) {
            F5GpsDataParser gpsTask = new F5GpsDataParser(this);
            gpsTask.execute(mPath);
        }
    }

    @Override
    public void getGpsData(List<GPSData> list) {
        mGpsList = list;
        mMapTrackView.drawTrackLine(list);

        mTvTotalMails.setText(GpsUtil.totalMails(list));
        updateAvgSpeed();
    }

    private void loadMedia() {
        initView();

        setOrientation(true);

        GlideUtils.loadImage(this, mPlayImg, mImageUrl, R.drawable.tacitly_pic);
        startPlay();
        mHandler.postDelayed(mPlayingChecker, 250);
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
            mVideoUrl = mPath;
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
                if (mAddTailerDialog != null && mAddTailerDialog.isVisible()) {
                    mAddTailerDialog.dismissAllowingStateLoss();
                }
            } else if (event.getExportStatus() == EventAddTailer.EXPORT_STATUS_FAILED) {
                //竖屏播放页访问即刻分享页面统计
                ZhugeUtils.eventShare(this, this.getString(R.string.str_zhuge_share_video_player));

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
        //mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mVtSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mTopView = findViewById(R.id.upper_layout);
        mBottomView = findViewById(R.id.bottom_layout);
        mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
        mResolutionTV = (TextView) findViewById(R.id.tv_resolution);
        mLoading = (ProgressBar) findViewById(R.id.mLoading);
        //   mLoading.setBackgroundResource(R.anim.video_loading);
        //     mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();

        mPlayImg = (ImageView) findViewById(R.id.play_img);
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

        if (mDate != null) {
            TextView tvTitleData = (TextView) findViewById(R.id.textview_title_date);
            tvTitleData.setText(mDate.substring(0, 10));
            TextView tvTitleTime = (TextView) findViewById(R.id.textview_title_time);
            tvTitleTime.setText(mDate.substring(11, 19));
            TextView title = (TextView) findViewById(R.id.title);
            title.setText(mDate);
        }
        mBtnDelete = (ImageView) findViewById(R.id.btn_delete);
        mStartVideoeditLl = (LinearLayout) findViewById(R.id.ll_start_videoedit);
        mTvShareRightnow = (TextView) findViewById(R.id.tv_share_video_rightnow);

        mResolutionTV.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mStartVideoeditLl.setOnClickListener(this);
        mTvShareRightnow.setOnClickListener(this);
//        if (videoEditSupport()) {
//            mStartVideoeditLl.setVisibility(View.VISIBLE);
//        } else {
//            mStartVideoeditLl.setVisibility(View.GONE);
//        }

        if (mVideoFrom.equals("local")) {
            if (mType == PhotoAlbumConfig.PHOTO_BUM_IPC_URG || mType == PhotoAlbumConfig.PHOTO_BUM_IPC_WND) {
            } else {
            }
        } else {
        }
        mVideoViewLayout = (RelativeLayout) findViewById(R.id.rv_video_player);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mVideoViewLayout.getLayoutParams();
        lp.width = mScreenWidth;
        lp.height = (int) (lp.width / 1.777);
        lp.leftMargin = 0;
        mVideoViewLayout.setLayoutParams(lp);
        mVideoView = (FullScreenVideoView) findViewById(R.id.fullscreen_video_view);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnTouchListener(mVideoTouchListener);

//        if (mVideoFrom.equals("local")) {
//            /** 如果是本地循环视频，不显示分享按钮，但要显示编辑按钮 */
//            if (!TextUtils.isEmpty(mFileName) && mFileName.startsWith("NRM")) {
//                mTvShareRightnow.setVisibility(View.GONE);
//                mStartVideoeditLl.setVisibility(View.VISIBLE);
//            }
//        } else {
//            mStartVideoeditLl.setVisibility(View.GONE);
//            mTvShareRightnow.setVisibility(View.GONE);
//        }
        mTvT3Hint = (TextView) findViewById(R.id.tv_t3_hint);
        if (mType == PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP && mVideoFrom.equals("ipc") && GolukApplication.getInstance().getIPCControlManager().needShowT3Hint()) {
            mTvT3Hint.setVisibility(View.VISIBLE);
        } else {
            mTvT3Hint.setVisibility(View.GONE);
        }

        mBtnPlay = (ImageView) findViewById(R.id.btn_play);
        mTvTotalMails = (TextView) findViewById(R.id.tv_total_mails);
        mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);
        mTvAverageSpeed = (TextView) findViewById(R.id.tv_average_speed);
        mLayoutTitle = (RelativeLayout) findViewById(R.id.relativelayout_title);
        mLayoutGpsInfo = (LinearLayout) findViewById(R.id.layout_gps_info);
        mLayoutMap = (RelativeLayout) findViewById(R.id.video_map_container);
        mLayoutOptions = (LinearLayout) findViewById(R.id.rl_operation);
        mBtnPlay.setOnClickListener(this);
    }

    private boolean videoEditSupport() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }
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
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (GolukUtils.isFastDoubleClick()) {
            return;
        }
        if (id == R.id.ll_start_videoedit) {
            pauseVideo();
        } else if (id == R.id.imagebutton_back) {
            // 返回
            exit();
        } else if (id == R.id.tv_share_video_rightnow) {
            pauseVideo();
        } else if (id == R.id.back_btn) {

            click_back();
        } else if (id == R.id.btn_play) {
            mVideoView.start();
            mBtnPlay.setVisibility(View.GONE);
        } else if (id == R.id.play_btn) {
            if (mVideoView.isPlaying() && mVideoView.canPause()) {
                pauseVideo();
            } else {
                mVideoView.start();
                mPlayImageView.setImageResource(R.drawable.player_pause_btn);
                if (id == R.id.play_btn) {
                    hideOperator();
                }
            }
        } else if (id == R.id.btn_delete) {
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
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mVideoViewLayout.getLayoutParams();
            params.width = metrics.widthPixels;
            params.height = metrics.heightPixels;
            params.leftMargin = 0;

            mVideoViewLayout.setLayoutParams(params);
//            LayoutParams norParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
//                    LayoutParams.WRAP_CONTENT);
//            norParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            norParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mStartVideoeditLl.setVisibility(View.GONE);
            mLayoutTitle.setVisibility(View.GONE);
            mLayoutGpsInfo.setVisibility(View.GONE);
            mLayoutMap.setVisibility(View.GONE);
            mLayoutOptions.setVisibility(View.GONE);
            //mVideoView.setOnTouchListener(mVideoTouchListener);

        } else {
            //mVideoView.setOnTouchListener(null);
            try {
                mHandler.removeCallbacks(hideRunnable);
            } catch (Exception e) {

            }

            hideOperator();
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mVideoViewLayout.getLayoutParams();
            lp.width = mScreenWidth;
            lp.height = (int) (lp.width / 1.777);
            lp.leftMargin = 0;
            //lp.addRule(RelativeLayout.BELOW, R.id.RelativeLayout_videoinfo);
            mVideoViewLayout.setLayoutParams(lp);
//            if (!mVideoFrom.equals("local")) {
//                mStartVideoeditLl.setVisibility(View.GONE);
//            } else {
//                mStartVideoeditLl.setVisibility(View.VISIBLE);
//            }
            mLayoutTitle.setVisibility(View.VISIBLE);
            mLayoutGpsInfo.setVisibility(View.VISIBLE);
            mLayoutMap.setVisibility(View.VISIBLE);
            mLayoutOptions.setVisibility(View.VISIBLE);

        }
        mIsFullScreen = bFull;
    }

    private boolean isAllowedDelete(String path) {
        List<String> dlist = GolukApplication.getInstance().getDownLoadList();
        if (dlist.contains(path)) {
            return false;
        } else {
            return true;
        }

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
                mConfirmDeleteDialog.dismiss();
                if (!"local".equals(mVideoFrom)) {
                    if (isAllowedDelete(path)) {
                        if (!GolukApplication.getInstance().getIpcIsLogin()) {
                            GolukUtils.showToast(PhotoAlbumPlayerF5.this, PhotoAlbumPlayerF5.this.getResources().getString(R.string.str_photo_check_ipc_state));
                        } else {
                            //相册详情页面-删除视频
                            ZhugeUtils.eventAlbumDeleteVideo(PhotoAlbumPlayerF5.this);
                            EventBus.getDefault().post(new EventDeletePhotoAlbumVid(path, getType()));
                            GolukUtils.showToast(PhotoAlbumPlayerF5.this, PhotoAlbumPlayerF5.this.getResources().getString(R.string.str_photo_delete_ok));
                        }

                        PhotoAlbumPlayerF5.this.finish();
                    } else {
                        GolukUtils.showToast(PhotoAlbumPlayerF5.this, PhotoAlbumPlayerF5.this.getResources().getString(R.string.str_photo_downing));
                    }
                } else {
                    //相册详情页面-删除视频
                    ZhugeUtils.eventAlbumDeleteVideo(PhotoAlbumPlayerF5.this);
                    EventBus.getDefault().post(new EventDeletePhotoAlbumVid(path, getType()));
                    GolukUtils.showToast(PhotoAlbumPlayerF5.this, PhotoAlbumPlayerF5.this.getResources().getString(R.string.str_photo_delete_ok));
                    PhotoAlbumPlayerF5.this.finish();
                }
            }
        });
        mConfirmDeleteDialog.show();
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
        mDuration = mVideoView.getDuration();
        mDurationTime.setText(formatTime(mDuration));
        mVtDurationTime.setText(formatTime(mDuration));
        mTvTotalTime.setText(formatTime(mDuration));
        updateAvgSpeed();
    }

    /**
     * 更新平均速度
     */
    private void updateAvgSpeed() {
        mTvAverageSpeed.setText(GpsUtil.avgSpeed(mGpsList, mDuration) + "");
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
        }
    }

    /**
     * 隐藏加载中显示画面
     */
    private void hideLoading() {
        mPlayImg.setVisibility(View.GONE);
        if (isShow) {
            isShow = false;
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
    private OnTouchListener mVideoTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mIsLand) {
                // 横屏
                mVideoView.pause();
                mBtnPlay.setVisibility(View.VISIBLE);
                return false;
            }

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
        } else {
            mPlayImageView.setImageResource(R.drawable.player_play_btn);
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
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTopView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mTopView.startAnimation(animation);

        mBottomView.clearAnimation();
        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.option_leave_from_bottom);
        animation1.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBottomView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
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
            // 画当前车辆位置
            if (mMapTrackView == null || CollectionUtils.isEmpty(mGpsList))
                return;
            final int position = progress * mGpsList.size() / 100;
            GPSData carGps = mGpsList.get(position);
            mMapTrackView.drawTrackCar(carGps);
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
            mIsLand = true;
            mClick = false;
            setFullScreen(isAuto, true);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
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
