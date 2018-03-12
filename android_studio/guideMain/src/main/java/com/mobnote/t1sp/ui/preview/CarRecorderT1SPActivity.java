package com.mobnote.t1sp.ui.preview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.CaptureTimeEvent;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventWifiState;
import com.mobnote.eventbus.RestoreFactoryEvent;
import com.mobnote.eventbus.VideoResEvent;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.PlayUrlManager;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.ReadWifiConfig;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.multicast.NetUtil;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.reportlog.ReportLogManager;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifibind.WifiHistorySelectListActivity;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.t1sp.base.ui.AbsActivity;
import com.mobnote.t1sp.bean.DeviceMode;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.connect.T1SPConnecter;
import com.mobnote.t1sp.connect.T1SPConntectListener;
import com.mobnote.t1sp.listener.OnCaptureListener;
import com.mobnote.t1sp.service.T1SPUdpService;
import com.mobnote.t1sp.ui.album.PhotoAlbumT1SPActivity;
import com.mobnote.t1sp.ui.setting.DeviceSettingsActivity;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.Const;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.t1sp.util.ThumbUtil;
import com.mobnote.t1sp.util.ViewUtil;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.GolukVideoUtils;
import com.mobnote.wifibind.WifiRsBean;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.player.RtspPlayerView;
import com.rd.car.player.RtspPlayerView.RtspPlayerLisener;

import java.util.List;

import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import likly.dollar.$;
import likly.mvp.MvpBinder;

/**
 * T1SP实时预览界面
 */
@MvpBinder(
        presenter = CarRecorderT1SPPresenterImpl.class,
        model = CarRecorderT1SPModelImpl.class
)
@SuppressLint("NewApi")
public class CarRecorderT1SPActivity extends AbsActivity<CarRecorderT1SPPresenter> implements CarRecorderT1SPView, OnClickListener, OnCaptureListener, T1SPConntectListener {

    // 录像模式
    private static final int MODE_RECORDING = 1;
    // 设置模式
    private static final int MODE_SETTING = 2;
    // 回放模式
    private static final int MODE_PLAYBACK = 3;

    // 抓拍按钮
    private Button mBtnCapture = null;

    /* 设置 */
    private ImageView mSettingBtn;
    /* 加载中布局 */
    private LinearLayout mLoadingLayout;
    private ProgressBar mLoadingProgressBar;
    private TextView mLoadingText;

    /* 本地最新两个相册 */
    private ImageView mLocalAlbumOne, mLocalalbumTwo;
    /* 远程相册 */
    private TextView mRemoteAlbum;

    /* RTSP视频播放器 */
    private RtspPlayerView mRtspPlayerView;
    /* 重新连接IPC时间间隔 */
    private final int RECONNECTIONTIME = 5000;

    private RelativeLayout mVLayout, mRtmpPlayerLayout;
    private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
    /**
     * 连接状态
     */
    private TextView mConnectTip;
    /**
     * 视频分辨率显示
     */
    private ImageView mVideoResolutions;

    private boolean m_bIsFullScreen = false;
    /* 全屏/旋转 */
    private ImageButton mFullScreen, mBtnRotate;

    private RelativeLayout mPalyerLayout, mLayoutVideo;
    private View mNotconnected, mConncetLayout, mLayoutTitle, mLayoutState,
            mLayoutAlumb, mLayoutCapture, mLayoutOptions;
    private ImageView new1, new2, mChangeBtn;
    private String SelfContextTag = "carrecordert1sp";
    private AlertDialog mExitAlertDialog;

    private GolukApplication mApp;
    private boolean isShowPlayer;
    private boolean isBackGroundStart = true;
    /* 是否发起预览链接 */
    private boolean isConnecting;
    private SettingInfo mSettingInfo;
    private Handler mHandlerCapture;
    private int mCaptureTime;
    // 最新2视频(精彩视频和紧急视频)
    private List<String> mLatestTwoVideos;
    // 模式: 1:录像; 2:设置; 3:回放
    private int mCurrentMode = MODE_RECORDING;
    // 是否连接上IPC
    private boolean mConnectedIpc;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_car_recorder_t1sp;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        EventBus.getDefault().register(this);
        T1SPConnecter.instance().addListener(this);
        T1SPConnecter.instance().mRecordActivity = this;

        mApp = (GolukApplication) getApplication();

        mHandlerCapture = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleCaptureTime();
            }
        };

        initView();
        setListener();

        // 获取是否是后台启动
        Intent receiveIntent = getIntent();
        isBackGroundStart = receiveIntent.getBooleanExtra("isBackGroundStart", false);

        //startPlay();

        // 设置抓拍回调
        T1SPUdpService.setCaptureListener(this);
        // 获取设备信息
        getPresenter().getVideoSettingInfo(true);
        // 自动同步时间
        syncSystemTime();
    }

    private void syncSystemTime() {
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getPresenter().autoSyncSystemTime();
            }
        }, 3500);
    }

    /**
     * 精彩视频抓拍计时处理
     */
    private void handleCaptureTime() {
        if (mSettingInfo == null)
            return;

        mCaptureTime--;
        mBtnCapture.setText(mCaptureTime + "");

        if (mCaptureTime > 0) {
            mHandlerCapture.sendEmptyMessageDelayed(0, 1000);
        } else {
            GolukDebugUtils.e(Const.LOG_TAG, "Timer count over: " + System.currentTimeMillis() / 1000);
            resetCaptureButton();
        }

    }

    private void resetCaptureButton() {
        mBtnCapture.setEnabled(true);
        mBtnCapture.setText("");
        mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
    }

    private void disableCaptureButton() {
        mBtnCapture.setEnabled(false);
        mBtnCapture.setText("");
        mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
    }

    @Override
    public void onGetVideoSettingInfo(SettingInfo settingInfo, boolean onlySettingInfo) {
        if (settingInfo == null)
            return;
        mConnectedIpc = true;
        mSettingInfo = settingInfo;
        mVideoResolutions.setBackgroundResource(settingInfo.is1080P() ? R.drawable.icon_hd1080 : R.drawable.icon_hd720);

        if (!onlySettingInfo) {
            queryDeviceMode();
        }
    }

    @Override
    public void onCaptureStart() {
        GolukDebugUtils.e(Const.LOG_TAG, "Capture command send success, time:" + System.currentTimeMillis() / 1000);
        // 抓拍精彩视频开始
        mHandlerCapture.removeMessages(0);
        mCaptureTime = mSettingInfo.captureTimeIs12S() ? CAPTURE_TIME_12 : CAPTURE_TIME_30;
        mCaptureTime = mCaptureTime - 5;
        mHandlerCapture.sendEmptyMessage(0);
    }

    @Override
    public void onGetLatestTwoVideos(List<String> videos) {
        if (videos == null || videos.isEmpty()) {
            mLocalAlbumOne.setVisibility(View.GONE);
            mLocalalbumTwo.setVisibility(View.GONE);
            return;
        }

        mLatestTwoVideos = videos;
        mLocalAlbumOne.setVisibility(View.GONE);
        mLocalalbumTwo.setVisibility(View.GONE);
        final String imagePath1 = videos.get(videos.size() == 2 ? 1 : 0);
        final String imagePath2 = videos.size() == 2 ? videos.get(0) : "";
        if (!TextUtils.isEmpty(imagePath1)) {
            mLocalAlbumOne.setVisibility(View.VISIBLE);
            mLocalAlbumOne.setImageBitmap(ThumbUtil.getLocalVideoThumb(imagePath1));
            new1.setVisibility(isNewByName(imagePath1) ? View.VISIBLE : View.GONE);
        }
        if (!TextUtils.isEmpty(imagePath2)) {
            mLocalalbumTwo.setVisibility(View.VISIBLE);
            mLocalalbumTwo.setImageBitmap(ThumbUtil.getLocalVideoThumb(imagePath2));
            new2.setVisibility(isNewByName(imagePath2) ? View.VISIBLE : View.GONE);
        }

    }

    /**
     * 根据视频路径判断视频文件是否为new
     */
    private boolean isNewByName(String videoPath) {
        videoPath = videoPath.substring(videoPath.lastIndexOf("/") + 1);
        return SettingUtils.getInstance().getBoolean("Local_" + videoPath, true);
    }

    @Override
    public void onCapturePic(String path) {
    }

    @Override
    public void onCaptureVideo(String path) {
        GolukDebugUtils.e(Const.LOG_TAG, "Received capture video path, time:" + System.currentTimeMillis() / 1000 + ", 路径:" + path);
        // 停止计时
        mHandlerCapture.removeMessages(0);
        mCaptureTime = 0;
        mBtnCapture.setText("");
        // 抓拍精彩视频回调
        if (TextUtils.isEmpty(path))
            return;
        // T1SP不需要下载
        GolukUtils.showToast(this, getString(R.string.capture_success_hint, path), Toast.LENGTH_LONG);
    }

    /* 是否在已经显示预览Activity的情况下,进入其他页面,断开连接,再次进入预览Activity */
    private boolean mIsReEnter;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIsReEnter = true;
        exitOtherMode();
    }

    /**
     * 跳转到播放本地视频页面
     */
    private void gotoPlayVideo(String videoName) {
        int type = 0;
        if (videoName.indexOf(FileUtil.URGENT_VIDEO_PREFIX) >= 0) {
            type = PhotoAlbumConfig.PHOTO_BUM_IPC_URG;
        } else if (videoName.indexOf(FileUtil.WONDERFUL_VIDEO_PREFIX) >= 0) {
            type = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
        }
        SettingUtils.getInstance().putBoolean("Local_" + videoName, false);
        VideoInfo mVideoInfo = GolukVideoUtils.getVideoInfo(videoName);
        if (mVideoInfo != null) {
            GolukUtils.startPhotoAlbumPlayerActivity(this, type, "local", mVideoInfo.videoPath,
                    mVideoInfo.filename, mVideoInfo.videoCreateDate, mVideoInfo.videoHP, mVideoInfo.videoSize, null);
            overridePendingTransition(R.anim.shortshare_start, 0);
        }
    }

    private void click_ConnFailed() {
        toSelectIpcActivity();
    }

    private void toSelectIpcActivity() {
        if (mApp.getEnableSingleWifi() || !WifiBindDataCenter.getInstance().isHasDataHistory()) {
            Intent intent = new Intent(this, WiFiLinkListActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, WifiHistorySelectListActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 初始化控件
     *
     * @author xuhw
     * @date 2015年3月9日
     */
    private void initView() {
        mPalyerLayout = (RelativeLayout) findViewById(R.id.mPalyerLayout);
        mFullScreen = (ImageButton) findViewById(R.id.mFullScreen);
        mBtnRotate = (ImageButton) findViewById(R.id.ic_rotate);
        mVideoResolutions = (ImageView) findViewById(R.id.mVideoResolutions);
        mRtmpPlayerLayout = (RelativeLayout) findViewById(R.id.mRtmpPlayerLayout);
        mVLayout = (RelativeLayout) findViewById(R.id.vLayout);
        mBtnCapture = (Button) findViewById(R.id.btn_capture);
        mSettingBtn = (ImageView) findViewById(R.id.mSettingBtn);
        mConnectTip = (TextView) findViewById(R.id.mConnectTip);
        mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.mLoading);
        mLoadingText = (TextView) findViewById(R.id.mLoadingText);
        mRtspPlayerView = (RtspPlayerView) findViewById(R.id.mRtmpPlayerView);
        mLocalAlbumOne = (ImageView) findViewById(R.id.image1);
        mLocalalbumTwo = (ImageView) findViewById(R.id.image2);
        mRemoteAlbum = (TextView) findViewById(R.id.image3);
        mChangeBtn = (ImageView) findViewById(R.id.changeBtn);

        new1 = (ImageView) findViewById(R.id.new1);
        new2 = (ImageView) findViewById(R.id.new2);

        mRtspPlayerView.setEnableHWCodec(true);
        mRtspPlayerView.setAudioMute(true);
        mRtspPlayerView.setZOrderMediaOverlay(true);
        mRtspPlayerView.setBufferTime(500);
        mRtspPlayerView.setConnectionTimeout(30000);
        mRtspPlayerView.setVisibility(View.VISIBLE);
        mConncetLayout = findViewById(R.id.mConncetLayout);
        mNotconnected = findViewById(R.id.mNotconnected);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRtmpPlayerLayout.getLayoutParams();
        lp.width = screenWidth;
        lp.height = (int) (screenWidth / 1.7833);
        lp.leftMargin = 0;
        mRtmpPlayerLayout.setLayoutParams(lp);

        mConnectTip.setText(getCurrentIpcSsid());
        if (GolukApplication.getInstance().getIpcIsLogin()) {
            mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
        }

        mLayoutVideo = (RelativeLayout) findViewById(R.id.ipclive);
        mLayoutTitle = findViewById(R.id.title_layout);
        mLayoutState = findViewById(R.id.rl_carrecorder_connection_state);
        mLayoutAlumb = findViewById(R.id.jcqp_info);
        mLayoutCapture = findViewById(R.id.layout_capture);
        mLayoutOptions = findViewById(R.id.layout_full_screen_options);
    }

    /**
     * 设置监听事件
     *
     * @author xuhw
     * @date 2015年3月11日
     */
    private void setListener() {
        findViewById(R.id.mPlayBtn).setOnClickListener(this);
        mPalyerLayout.setOnClickListener(this);
        mFullScreen.setOnClickListener(this);
        mBtnRotate.setOnClickListener(this);
        mBtnCapture.setOnClickListener(this);
        mNotconnected.setOnClickListener(this);
        mLocalAlbumOne.setOnClickListener(this);
        mLocalalbumTwo.setOnClickListener(this);
        mRemoteAlbum.setOnClickListener(this);
        mRtspPlayerView.setOnClickListener(this);
        mConncetLayout.setOnClickListener(this);
        mChangeBtn.setOnClickListener(this);

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.mSettingBtn).setOnClickListener(this);
        findViewById(R.id.ic_rotate_full_screen).setOnClickListener(this);
        findViewById(R.id.ic_exit_full_screen).setOnClickListener(this);
        mRtspPlayerView.setPlayerListener(new RtspPlayerLisener() {

            @Override
            public void onPlayerPrepared(RtspPlayerView rpv) {
                mRtspPlayerView.setHideSurfaceWhilePlaying(true);
            }

            @Override
            public boolean onPlayerError(RtspPlayerView rpv, int what, int extra, String strErrorInfo) {
                hidePlayer();
                rpv.removeCallbacks(retryRunnable);
                showLoading();
                rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
                if (m_bIsFullScreen) {
                    setFullScreen(false);
                }
                return false;
            }

            @Override
            public void onPlayerCompletion(RtspPlayerView rpv) {
                hidePlayer();
                rpv.removeCallbacks(retryRunnable);
                showLoading();
                rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
                if (m_bIsFullScreen) {
                    setFullScreen(false);
                }
            }

            @Override
            public void onPlayBuffering(RtspPlayerView rpv, boolean start) {
                if (start) {
                    // 缓冲开始
                    showLoading();
                } else {
                    // 缓冲结束
                    hideLoading();
                }
            }

            @Override
            public void onGetCurrentPosition(RtspPlayerView rpv, int nPosition) {
            }

            @Override
            public void onPlayerBegin(RtspPlayerView arg0) {
                hideLoading();
                // 显示播放器
                showPlayer();
                // 隐藏
                mPalyerLayout.setVisibility(View.GONE);
                mFullScreen.setVisibility(View.VISIBLE);
                mBtnRotate.setVisibility(View.VISIBLE);
                // 抓拍按钮
                //resetCaptureButton();
            }
        });
    }

    private void reportLog() {
        final String jsonData = ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_RTSP_REVIEW)
                .getReportData();
        mApp.uploadMsg(jsonData, false);
        ReportLogManager.getInstance().removeKey(IMessageReportFn.KEY_RTSP_REVIEW);
    }

    /**
     * 隐藏播放器
     *
     * @author xuhw
     * @date 2015年3月21日
     */
    private void hidePlayer() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVLayout.getLayoutParams();
        lp.width = lp.height = 1;
        lp.leftMargin = 2000;
        mVLayout.setLayoutParams(lp);
    }

    /**
     * 显示播放器
     *
     * @author xuhw
     * @date 2015年3月21日
     */
    private void showPlayer() {
        int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVLayout.getLayoutParams();
        lp.width = width;
        lp.height = (int) (width / 1.777);
        lp.leftMargin = 0;
        mVLayout.setLayoutParams(lp);

        isShowPlayer = true;
    }

    /**
     * 重连runnable
     */
    private Runnable retryRunnable = new Runnable() {
        @Override
        public void run() {
            isConnecting = true;
            startPlay();
        }
    };

    /**
     * 启动视频预览
     */
    public void startPlay() {
        WifiRsBean wrb = ReadWifiConfig.readConfig();
        if (wrb != null && GolukApplication.getInstance().getIpcIsLogin()) {
            mConnectTip.setText(getCurrentIpcSsid());
        }
        if (null != mRtspPlayerView) {
            mRtspPlayerView.setVisibility(View.VISIBLE);
            String url = PlayUrlManager.T1SP_RTSP_URL;
            if (TextUtils.isEmpty(url)) {
                return;
            }
            mRtspPlayerView.setDataSource(url);
            mRtspPlayerView.start();
        }
    }

    /**
     * 获取当前IPC SSID
     */
    private String getCurrentIpcSsid() {
        WifiBindHistoryBean currentIpcInfo = WifiBindDataCenter.getInstance().getCurrentUseIpc();
        if (currentIpcInfo == null)
            return "";
        return currentIpcInfo.ipc_ssid;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_btn) {
            if (m_bIsFullScreen) {
                return;
            }
            preExit();
        } else if (id == R.id.btn_capture) {
            // 视频抓拍
            if (!GolukApplication.getInstance().getIpcIsLogin() || mCaptureTime > 0)
                return;
            getPresenter().captureVideo();
        } else if (id == R.id.mSettingBtn) {
            if (m_bIsFullScreen) {
                return;
            }
            // 进入设置模式
            //mCurrentMode = MODE_SETTING;
            ViewUtil.goActivity(this, DeviceSettingsActivity.class);
        } else if (id == R.id.mFullScreen) {
            setFullScreen(true);
        } else if (id == R.id.ic_rotate || id == R.id.ic_rotate_full_screen) {
            rotatePreviewVideo();
        } else if (id == R.id.ic_exit_full_screen) {
            setFullScreen(false);
        } else if (id == R.id.mPlayBtn) {
            if (!isShowPlayer) {
                if (!isConnecting) {
                    isConnecting = true;
                    startPlay();
                }

                showLoading();
                hidePlayer();
                mPalyerLayout.setVisibility(View.GONE);
            }
        } else if (id == R.id.mNotconnected) {
            click_ConnFailed();
        } else if (id == R.id.image1) {
            if (CollectionUtils.isEmpty(mLatestTwoVideos))
                return;
            new1.setVisibility(View.GONE);
            String videoName = mLatestTwoVideos.get(mLatestTwoVideos.size() == 2 ? 1 : 0);
            videoName = videoName.substring(videoName.lastIndexOf("/") + 1);
            gotoPlayVideo(videoName);
        } else if (id == R.id.image2) {
            new2.setVisibility(View.GONE);
            if (mLatestTwoVideos != null && mLatestTwoVideos.size() == 2) {
                String videoName = mLatestTwoVideos.get(0);
                videoName = videoName.substring(videoName.lastIndexOf("/") + 1);
                gotoPlayVideo(videoName);
            }
        } else if (id == R.id.image3) {
            // 进入回放模式
            mCurrentMode = MODE_PLAYBACK;
            Intent photoalbum = new Intent(CarRecorderT1SPActivity.this, PhotoAlbumT1SPActivity.class);
            photoalbum.putExtra("from", "cloud");
            startActivity(photoalbum);
        } else if (id == R.id.mRtmpPlayerView) {
            if (m_bIsFullScreen) {
                setFullScreen(false);
            } else {
                mRtspPlayerView.removeCallbacks(retryRunnable);
                mRtspPlayerView.stopPlayback();
                hidePlayer();
                isShowPlayer = false;
                isConnecting = false;
                mPalyerLayout.setVisibility(View.VISIBLE);
                mNotconnected.setVisibility(View.GONE);
                mConncetLayout.setVisibility(View.GONE);
                mFullScreen.setVisibility(View.GONE);
                mBtnRotate.setVisibility(View.GONE);
            }
        } else if (id == R.id.mConncetLayout) {
            Intent intent = new Intent(this, WiFiLinkListActivity.class);
            startActivity(intent);
        } else if (id == R.id.changeBtn) {
            Intent intent = new Intent(this, WiFiLinkListActivity.class);
            intent.putExtra(WiFiLinkListActivity.ACTION_FROM_CAM, false);
            startActivity(intent);
        }
    }

    /**
     * 旋转视频预览
     */
    private void rotatePreviewVideo() {
        if (mLoadingLayout.getVisibility() == View.VISIBLE)
            return;

        showLoading();
        getPresenter().rotateVideo();
    }

    private boolean mCanSwitchMode = true;

    /**
     * 显示加载中布局
     *
     * @author xuhw
     * @date 2015年3月8日
     */
    public void showLoading() {
        if (!NetUtil.isWifiConnected(this))
            return;
        mLoadingText.setText(this.getResources().getString(R.string.str_video_loading));
        mLoadingLayout.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏加载中显示画面
     *
     * @author xuhw
     * @date 2015年3月8日
     */
    public void hideLoading() {
        mLoadingLayout.setVisibility(View.GONE);
    }

    boolean isstart = false;

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (!isstart) {
                isstart = true;
                CarRecorderManager.onStartRTSP(this);
            }
        } catch (RecorderStateException e) {
            e.printStackTrace();
        }
    }

    private void ipcConnecting() {
        mFullScreen.setVisibility(View.GONE);
        mBtnRotate.setVisibility(View.GONE);
        mSettingBtn.setVisibility(View.GONE);
        if (mApp.isBindSucess()) {
            mPalyerLayout.setVisibility(View.GONE);
            mNotconnected.setVisibility(View.GONE);
            mConncetLayout.setVisibility(View.VISIBLE);

            mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
        } else {
            mPalyerLayout.setVisibility(View.GONE);
            mNotconnected.setVisibility(View.VISIBLE);
            mConncetLayout.setVisibility(View.GONE);

            mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
        }
    }

    private void ipcConnFailed() {
        mConnectedIpc = false;
        mFullScreen.setVisibility(View.GONE);
        mBtnRotate.setVisibility(View.GONE);
        mConnectTip.setText(R.string.str_disconnect_ipc);
        mPalyerLayout.setVisibility(View.GONE);
        mNotconnected.setVisibility(View.VISIBLE);
        mConncetLayout.setVisibility(View.GONE);
        mSettingBtn.setVisibility(View.GONE);

        disableCaptureButton();
    }

    private void ipcConnSucess() {
        mConnectedIpc = true;
        if (isShowPlayer || isConnecting) {
            showLoading();
            hidePlayer();
            mRtspPlayerView.removeCallbacks(retryRunnable);
            isConnecting = true;
            startPlay();
        } else {
            isShowPlayer = false;
            isConnecting = false;
            mPalyerLayout.setVisibility(View.VISIBLE);
        }
        mNotconnected.setVisibility(View.GONE);
        mConncetLayout.setVisibility(View.GONE);
        mConnectTip.setText(getCurrentIpcSsid());
        mSettingBtn.setVisibility(View.VISIBLE);
        mCanSwitchMode = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isShowPlayer) {
            if (!isConnecting) {
                showLoading();
                hidePlayer();
                isConnecting = true;
                //startPlay();
            }
        }

        //GolukApplication.getInstance().setContext(this, "carrecorder_t1sp");
        if (isBackGroundStart) {
            this.moveTaskToBack(true);
            isBackGroundStart = false;
        }

        // 获取本地最近2个视频(精彩视频和紧急视频综合)
        getPresenter().getLatestTwoVideos();
        // 获取当前模式
        showLoading();

        exitOtherMode();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isShowPlayer) {
            if (null != mRtspPlayerView) {
                mFullScreen.setVisibility(View.GONE);
                mBtnRotate.setVisibility(View.GONE);
                mRtspPlayerView.removeCallbacks(retryRunnable);
                if (mRtspPlayerView.isPlaying()) {
                    isConnecting = false;
                    mRtspPlayerView.stopPlayback();
                }
                hidePlayer();
            }
        }
    }

    @Override
    public void onDestroy() {
        //disable wifi if ipcConnected
        if (mApp.isIpcLoginSuccess && T1SPConnecter.instance().needDisconnectWIFI()) {
            mApp.mIPCControlManager.setVdcpDisconnect();
            mApp.setIpcLoginOut();
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                wifiManager.disableNetwork(wifiInfo.getNetworkId());
            }
        }
        if (null != mRtspPlayerView) {
            mRtspPlayerView.removeCallbacks(retryRunnable);
            mRtspPlayerView.cleanUp();
        }

        //if (null != GolukApplication.getInstance().getIPCControlManager()) {
        //    GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("main");
        //}
        EventBus.getDefault().unregister(this);
        if (mExitAlertDialog != null) {
            if (mExitAlertDialog.isShowing()) {
                mExitAlertDialog.dismiss();
            }
            mExitAlertDialog = null;
        }
        super.onDestroy();
        T1SPConnecter.instance().removeListener(this);
        T1SPConnecter.instance().needDisconnectWIFI(true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 设置播放器全屏
     *
     * @param bFull true:全屏　false:普通
     * @author xuhw
     * @date 2015年5月12日
     */
    public void setFullScreen(boolean bFull) {
        if (bFull == m_bIsFullScreen) {
            return;
        }
        if (bFull) {
            if (!mRtspPlayerView.isPlaying()) {
                return;
            }

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

            mConncetLayout.setVisibility(View.GONE);
            mLayoutTitle.setVisibility(View.GONE);
            mLayoutState.setVisibility(View.GONE);
            mLayoutAlumb.setVisibility(View.GONE);
            mLayoutCapture.setVisibility(View.GONE);
            mVLayout.setVisibility(View.GONE);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLayoutVideo.getLayoutParams();
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            mLayoutVideo.setLayoutParams(layoutParams);
            ((ViewGroup) mRtspPlayerView.getParent()).removeView(mRtspPlayerView);
            mLayoutVideo.addView(mRtspPlayerView, 0);
            mLayoutOptions.setVisibility(View.VISIBLE);

        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            //mConncetLayout.setVisibility(View.VISIBLE);
            mLayoutTitle.setVisibility(View.VISIBLE);
            mLayoutState.setVisibility(View.VISIBLE);
            mLayoutAlumb.setVisibility(View.VISIBLE);
            mLayoutCapture.setVisibility(View.VISIBLE);
            mLayoutOptions.setVisibility(View.GONE);
            mVLayout.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLayoutVideo.getLayoutParams();
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            mLayoutVideo.setLayoutParams(layoutParams);

            mLayoutVideo.removeView(mRtspPlayerView);
            mVLayout.addView(mRtspPlayerView, 0);
        }
        m_bIsFullScreen = bFull;
    }

    @Override
    public void onBackPressed() {
        if (m_bIsFullScreen) {
            // 全屏时，退出全屏
            setFullScreen(false);
        } else {
            preExit();
        }
    }

    private void preExit() {
        if (mApp.getDownLoadList() == null || mApp.getDownLoadList().size() == 0 || !mApp.isDownloading()) {
            exit();
            return;
        }
        if (mExitAlertDialog == null) {
            mExitAlertDialog = new AlertDialog.Builder(this).create();
            mExitAlertDialog.setTitle(getString(R.string.str_global_dialog_title));
            mExitAlertDialog.setMessage(getString(R.string.msg_of_exit_when_download));
            mExitAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.str_button_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mExitAlertDialog.dismiss();
                    exit();
                }
            });
            mExitAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_str_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mExitAlertDialog.dismiss();
                }
            });
        }
        if (mExitAlertDialog.isShowing()) {
            return;
        }

        mExitAlertDialog.show();
        mExitAlertDialog.setCancelable(true);
        mExitAlertDialog.setCanceledOnTouchOutside(true);

    }

    private void exit() {
        reportLog();
        finish();
    }

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public void onEventMainThread(EventWifiState event) {
        if (EventConfig.WIFI_STATE == event.getOpCode()) {
            if (!event.getMsg()) {
                if (!NetUtil.isWifiConnected(this)) {
                    // WIFI 断开
                    startActivity(new Intent(this, CarRecorderT1SPActivity.class));
                }
            }
        }
    }

    /**
     * 视频质量变化
     */
    public void onEventMainThread(VideoResEvent event) {
        if (event != null && !TextUtils.isEmpty(event.value)) {
            if (mSettingInfo != null) {
                mSettingInfo.videoRes = event.value;
                mVideoResolutions.setBackgroundResource(mSettingInfo.is1080P() ? R.drawable.icon_hd1080 : R.drawable.icon_hd720);
            }
        }
    }

    /**
     * 抓拍时间变化
     */
    public void onEventMainThread(CaptureTimeEvent event) {
        if (event != null && !TextUtils.isEmpty(event.value)) {
            if (mSettingInfo != null) {
                mSettingInfo.captureTime = event.value;
            }
        }
    }

    /**
     * 恢复出厂设置成功
     */
    public void onEventMainThread(RestoreFactoryEvent event) {
        // 获取设备信息
        getPresenter().getVideoSettingInfo(true);
        // 同步时间
        syncSystemTime();
    }

    /**
     * 如果处于设置模式或回放模式,需要先退出该模式
     */
    private void exitOtherMode() {
        showLoading();
        disableCaptureButton();

        // 如果是断开连接返回预览页面
//        if (mIsReEnter) {
//            mIsReEnter = false;
//            return;
//        }

        if (isInSettingMode()) {
            getPresenter().exitSetMode();
            mCanSwitchMode = false;
            GolukDebugUtils.e(Const.LOG_TAG, "Exit SetMode");
        } else if (isInPlaybackMode()) {
            getPresenter().exitPlaybackMode();
            mCanSwitchMode = false;
            GolukDebugUtils.e(Const.LOG_TAG, "Exit PlaybackMode");
        } else {
            startPlay();
            queryDeviceMode();
        }
    }

    private boolean isInSettingMode() {
        return mCurrentMode == MODE_SETTING;
    }

    private boolean isInPlaybackMode() {
        return mCurrentMode == MODE_PLAYBACK;
    }

    private void setModeToRecordMode() {
        mCurrentMode = MODE_RECORDING;
    }

    @Override
    public void onGetDeviceModeInfo(DeviceMode deviceMode) {
        if (deviceMode != null) {
            GolukDebugUtils.e(Const.LOG_TAG, "DeviceMode:" + deviceMode.mode + " - " + deviceMode.recordState);
            if (deviceMode.isInPlaybackMode()) {
                // 如果是处于文件模式,必须先退出文件模式
                mCurrentMode = MODE_PLAYBACK;
                exitOtherMode();
            } else if (deviceMode.needOpenLoopVideo()) {
                GolukDebugUtils.e(Const.LOG_TAG, "Need open loop video mode");
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPresenter().openLoopMode();
                    }
                }, 500);
            } else {
                //$.toast().text(R.string.recovery_to_record).show();
                mCanSwitchMode = true;
                resetCaptureButton();
                //startPlay();
            }
        }
    }

    @Override
    public void onEnterVideoMode() {
        GolukDebugUtils.e(Const.LOG_TAG, "Exit playback mode success");
    }

    @Override
    public void onExitOtherModeSuccess() {
        GolukDebugUtils.e(Const.LOG_TAG, "Exit other mode success");
        mCanSwitchMode = true;
        setModeToRecordMode();
        // 开始预览
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startPlay();
            }
        }, 1500);

        queryDeviceMode();
    }

    /**
     * 查询设备当前状态
     */
    private void queryDeviceMode() {
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mConnectedIpc)
                    getPresenter().getDeviceMode();
            }
        }, 1000);
    }

    @Override
    public void onExitOtherModeFailed() {
        GolukDebugUtils.e(Const.LOG_TAG, "Exist other mode failed");
        $.toast().text("进入录像模式失败").show();
        finish();
    }

    @Override
    public void onOpenLoopModeSuccess() {
        GolukDebugUtils.e(Const.LOG_TAG, "Open LoopRecord success");
        $.toast().text(R.string.recovery_to_record).show();
        mCanSwitchMode = true;
        resetCaptureButton();
        //startPlay();
    }

    @Override
    public void onOpenLoopModeFailed() {
        GolukDebugUtils.e(Const.LOG_TAG, "Open LoopRecord failed");
        $.toast().text("进入录像模式失败").show();
        finish();
    }

    @Override
    public void onT1SPDisconnected() {
        ipcConnFailed();
    }

    @Override
    public void onT1SPConnectStart() {
        ipcConnecting();
    }

    @Override
    public void onT1SPConnectResult(boolean success) {
        if (success) {
            ipcConnSucess();
            queryDeviceMode();
        }
    }

}
