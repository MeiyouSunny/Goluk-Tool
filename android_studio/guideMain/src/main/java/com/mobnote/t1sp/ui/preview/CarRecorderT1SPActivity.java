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
import android.view.ViewStub;
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
import com.mobnote.eventbus.EventExitMode;
import com.mobnote.eventbus.EventUpdateAddr;
import com.mobnote.eventbus.RestoreFactoryEvent;
import com.mobnote.eventbus.VideoResEvent;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.PlayUrlManager;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.ReadWifiConfig;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.live.GetBaiduAddress;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.reportlog.ReportLogManager;
import com.mobnote.golukmain.wifibind.WiFiInfo;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifibind.WifiHistorySelectListActivity;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
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
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.GolukVideoUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.wifibind.WifiRsBean;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.player.RtspPlayerView;
import com.rd.car.player.RtspPlayerView.RtspPlayerLisener;

import java.util.List;

import cn.com.mobnote.eventbus.EventLocationFinish;
import cn.com.mobnote.eventbus.EventShortLocationFinish;
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

    // 抓拍按钮
    private Button mBtnCapture = null;

    /**
     * 设置按钮
     */
    private ImageView mSettingBtn = null;
    /**
     * 当前地址显示
     */
    private TextView mAddr = null;
    /**
     * 加载中布局
     */
    private LinearLayout mLoadingLayout = null;
    /**
     * 加载中动画显示控件
     */
    private ProgressBar mLoading = null;

    /**
     * 最新两个精彩视频或抢拍视频
     */
    private ImageView image1 = null;
    private ImageView image2 = null;

    /**
     * 进入相册
     **/
    private TextView image3 = null;

    /**
     * 加载中显示文字
     */
    private TextView mLoadingText = null;
    /**
     * rtsp视频播放器
     */
    private RtspPlayerView mRtspPlayerView = null;
    /**
     * 重新连接IPC时间间隔
     */
    private final int RECONNECTIONTIME = 5000;

    private boolean isBackGroundStart = true;
    /**
     * 是否发起预览链接
     */
    private boolean isConnecting = false;
    private RelativeLayout mVLayout = null;
    private RelativeLayout mRtmpPlayerLayout = null;
    private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
    private float density = SoundUtils.getInstance().getDisplayMetrics().density;
    /**
     * 连接状态
     */
    private TextView mConnectTip = null;
    /**
     * 视频分辨率显示
     */
    private ImageView mVideoResolutions = null;

    private GolukApplication mApp = null;
    private boolean m_bIsFullScreen = false;
    private ViewGroup m_vgNormalParent;
    private ImageButton mFullScreen = null;
    private RelativeLayout mPlayerLayout = null;
    private Button mNormalScreen = null;
    private final int BTN_NORMALSCREEN = 231;

    private RelativeLayout mPalyerLayout = null;

    private boolean isShowPlayer = false;

    private View mNotconnected = null;

    private View mConncetLayout = null;

    private ImageView new1;

    private ImageView new2;

    private String SelfContextTag = "carrecordert1sp";

    private String mLocationAddress = "";

    private ImageView mChangeBtn;

    private AlertDialog mExitAlertDialog;

    private SettingInfo mSettingInfo;
    private Handler mHandlerCapture;
    private int mCaptureTime;
    // 最新2视频(精彩视频和紧急视频)
    private List<String> mLatestTwoVideos;
    // 是否处于其他模式(录像模式意外的模式)
    private boolean isInOtherMode;

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

        mPlayerLayout = new RelativeLayout(this);
        mNormalScreen = new Button(this);
        mNormalScreen.setId(BTN_NORMALSCREEN);
        mNormalScreen.setBackgroundResource(R.drawable.btn_player_normal);
        mNormalScreen.setOnClickListener(this);

        mLocationAddress = GolukFileUtils.loadString("loactionAddress", "");

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

        start();

        // 设置抓拍回调
        T1SPUdpService.setCaptureListener(this);
        // 获取设备信息
        getPresenter().getVideoSettingInfo(false);
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
        mSettingInfo = settingInfo;
        mVideoResolutions.setBackgroundResource(settingInfo.is1080P() ? R.drawable.icon_hd1080 : R.drawable.icon_hd720);

        if (!onlySettingInfo) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPresenter().getDeviceMode();
                }
            }, 500);
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
            image1.setVisibility(View.GONE);
            image2.setVisibility(View.GONE);
            return;
        }

        mLatestTwoVideos = videos;
        image1.setVisibility(View.GONE);
        image2.setVisibility(View.GONE);
        final String imagePath1 = videos.get(videos.size() == 2 ? 1 : 0);
        final String imagePath2 = videos.size() == 2 ? videos.get(0) : "";
        if (!TextUtils.isEmpty(imagePath1)) {
            image1.setVisibility(View.VISIBLE);
            image1.setImageBitmap(ThumbUtil.getLocalVideoThumb(imagePath1));
            new1.setVisibility(isNewByName(imagePath1) ? View.VISIBLE : View.GONE);
        }
        if (!TextUtils.isEmpty(imagePath2)) {
            image2.setVisibility(View.VISIBLE);
            image2.setImageBitmap(ThumbUtil.getLocalVideoThumb(imagePath2));
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
        mFullScreen.setVisibility(View.GONE);
        mVideoResolutions = (ImageView) findViewById(R.id.mVideoResolutions);
        mRtmpPlayerLayout = (RelativeLayout) findViewById(R.id.mRtmpPlayerLayout);
        mVLayout = (RelativeLayout) findViewById(R.id.vLayout);
        mBtnCapture = (Button) findViewById(R.id.btn_capture);
        mSettingBtn = (ImageView) findViewById(R.id.mSettingBtn);
        mAddr = (TextView) findViewById(R.id.mAddr);
        if (GolukApplication.getInstance().isMainland()) {
            //mAddr.setVisibility(View.VISIBLE);
        } else {
            mAddr.setVisibility(View.GONE);
        }
        mConnectTip = (TextView) findViewById(R.id.mConnectTip);
        mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
        mLoading = (ProgressBar) findViewById(R.id.mLoading);
        mLoadingText = (TextView) findViewById(R.id.mLoadingText);
        mRtspPlayerView = (RtspPlayerView) findViewById(R.id.mRtmpPlayerView);
        image1 = (ImageView) findViewById(R.id.image1);
        image2 = (ImageView) findViewById(R.id.image2);
        image3 = (TextView) findViewById(R.id.image3);
        mChangeBtn = (ImageView) findViewById(R.id.changeBtn);

        new1 = (ImageView) findViewById(R.id.new1);
        new2 = (ImageView) findViewById(R.id.new2);

        mRtspPlayerView.setAudioMute(true);
        mRtspPlayerView.setZOrderMediaOverlay(true);
        mRtspPlayerView.setBufferTime(1000);
        mRtspPlayerView.setConnectionTimeout(30000);
        mRtspPlayerView.setVisibility(View.VISIBLE);
        mConncetLayout = findViewById(R.id.mConncetLayout);
        mNotconnected = findViewById(R.id.mNotconnected);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRtmpPlayerLayout.getLayoutParams();
        lp.width = screenWidth;
        lp.height = (int) (screenWidth / 1.7833);
        lp.leftMargin = 0;
        mRtmpPlayerLayout.setLayoutParams(lp);

        mConnectTip.setText(WiFiInfo.IPC_SSID);
        if (GolukApplication.getInstance().getIpcIsLogin()) {
            mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
        }

        if ("".equals(mLocationAddress)) {
            mAddr.setText(this.getResources().getString(R.string.str_localization_ongoing));
        } else {
            mAddr.setText(mLocationAddress);
        }

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
        mBtnCapture.setOnClickListener(this);
        mNotconnected.setOnClickListener(this);
        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        mRtspPlayerView.setOnClickListener(this);
        mConncetLayout.setOnClickListener(this);
        mChangeBtn.setOnClickListener(this);

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.mSettingBtn).setOnClickListener(this);
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
                // 抓拍按钮
                resetCaptureButton();
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
            start();
        }
    };

    /**
     * 启动视频预览
     */
    public void start() {
        WifiRsBean wrb = ReadWifiConfig.readConfig();
        if (wrb != null && GolukApplication.getInstance().getIpcIsLogin()) {
            mConnectTip.setText(WiFiInfo.IPC_SSID);
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

    public void onEventMainThread(EventUpdateAddr event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.CAR_RECORDER_UPDATE_ADDR:
                String addr = event.getMsg();
                if (!TextUtils.isEmpty(addr)) {
                    mAddr.setText(addr);
                }
                break;
            default:
                break;
        }
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
            if (m_bIsFullScreen || !mCanSwitchMode) {
                return;
            }
            ViewUtil.goActivity(this, DeviceSettingsActivity.class);
        } else if (id == R.id.mFullScreen) {
            setFullScreen(true);
        } else if (id == BTN_NORMALSCREEN) {
            setFullScreen(false);
        } else if (id == R.id.mPlayBtn) {
            if (!isShowPlayer) {
                if (!isConnecting) {
                    isConnecting = true;
                    start();
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
            if (!mCanSwitchMode)
                return;
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

    private boolean mCanSwitchMode;

    /**
     * 显示加载中布局
     *
     * @author xuhw
     * @date 2015年3月8日
     */
    private void showLoading() {
        mCanSwitchMode = false;
        mLoadingText.setText(this.getResources().getString(R.string.str_video_loading));
        mLoadingLayout.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏加载中显示画面
     *
     * @author xuhw
     * @date 2015年3月8日
     */
    private void hideLoading() {
        mCanSwitchMode = true;
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
        mFullScreen.setVisibility(View.GONE);
        mConnectTip.setText(R.string.str_disconnect_ipc);
        mPalyerLayout.setVisibility(View.GONE);
        mNotconnected.setVisibility(View.VISIBLE);
        mConncetLayout.setVisibility(View.GONE);
        mSettingBtn.setVisibility(View.GONE);

        mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
    }

    private void ipcConnSucess() {
        if (isShowPlayer || isConnecting) {
            showLoading();
            hidePlayer();
            mRtspPlayerView.removeCallbacks(retryRunnable);
            isConnecting = true;
            start();
        } else {
            isShowPlayer = false;
            isConnecting = false;
            mPalyerLayout.setVisibility(View.VISIBLE);
        }
        mNotconnected.setVisibility(View.GONE);
        mConncetLayout.setVisibility(View.GONE);
        mConnectTip.setText(WiFiInfo.IPC_SSID);
        mSettingBtn.setVisibility(View.VISIBLE);
        mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);

    }

    @Override
    public void onResume() {
        super.onResume();

        if (isShowPlayer) {
            if (!isConnecting) {
                showLoading();
                hidePlayer();
                isConnecting = true;
                start();
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
//        new Handler() {
//        }.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getPresenter().getDeviceMode();
//            }
//        }, 1500);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isShowPlayer) {
            if (null != mRtspPlayerView) {
                mFullScreen.setVisibility(View.GONE);
                mRtspPlayerView.removeCallbacks(retryRunnable);
                if (mRtspPlayerView.isPlaying()) {
                    isConnecting = false;
                    mRtspPlayerView.stopPlayback();
                }
                hidePlayer();
            }
        }
        // 移除定位通知及反编码通知
        mApp.removeLocationListener(SelfContextTag);
        GetBaiduAddress.getInstance().setCallBackListener(null);
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
            m_vgNormalParent = (ViewGroup) mRtspPlayerView.getParent();
            if (null == m_vgNormalParent) {
                return;
            }
            ViewGroup vgRoot = (ViewGroup) mRtspPlayerView.getRootView(); // 获取根布局
            m_vgNormalParent.removeView(mRtspPlayerView);
            mPlayerLayout.addView(mRtspPlayerView);
            RelativeLayout.LayoutParams norParams = new RelativeLayout.LayoutParams((int) (38.66 * density),
                    (int) (30 * density));
            norParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            norParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            norParams.setMargins(0, 0, (int) (10 * density), (int) (10 * density));
            mPlayerLayout.addView(mNormalScreen, norParams);
            vgRoot.addView(mPlayerLayout);

            getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else if (m_vgNormalParent != null) {
            ViewGroup vgRoot = (ViewGroup) mRtspPlayerView.getRootView();
            vgRoot.removeView(mPlayerLayout);
            mPlayerLayout.removeView(mRtspPlayerView);
            mPlayerLayout.removeView(mNormalScreen);
            m_vgNormalParent.addView(mRtspPlayerView);

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    public void onEventMainThread(EventShortLocationFinish eventShortLocationFinish) {
        if (null == eventShortLocationFinish) {
            return;
        }
    }

    public void onEventMainThread(EventLocationFinish event) {
        if (null == event) {
            return;
        }

        if (event.getCityCode().equals("-1") && TextUtils.isEmpty(event.getAddress())) {// 定位失败
            if (mLocationAddress.equals("")) {
                mAddr.setText(this.getResources().getString(R.string.str_unknow_street));
            } else {
                mAddr.setText(mLocationAddress);
            }
        } else {// 定位成功
            if (event.getAddress() != null && !"".equals(event.getAddress())) {
                mLocationAddress = event.getAddress();
                //mLocationLat = event.getLat();
                //mLocationLon = event.getLon();
                GolukFileUtils.saveString("loactionAddress", mLocationAddress);
                mAddr.setText(mLocationAddress);
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
    }

    /**
     * 退出模式Event
     */
    public void onEventMainThread(final EventExitMode event) {
        showLoading();
        disableCaptureButton();

        if (event != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (event.isSetMode()) {
                        getPresenter().exitSetMode();
                        GolukDebugUtils.e(Const.LOG_TAG, "Exit SetMode");
                    } else if (event.isPlaybackMode()) {
                        getPresenter().exitPlaybackMode();
                        GolukDebugUtils.e(Const.LOG_TAG, "Exit PlaybackMode");
                    }
                }
            }, 1000);
        }
    }

    /**
     * 首次显示使用提示
     */
    private void firstShowHint() {
        if (!SharedPrefUtil.isShowChangeIpc()) {
            SharedPrefUtil.setShowChangeIpc(true);
            final ViewStub stub = (ViewStub) findViewById(R.id.stub_change);
            View view = stub.inflate();
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    stub.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onGetDeviceModeInfo(DeviceMode deviceMode) {
        if (deviceMode != null) {
            GolukDebugUtils.e(Const.LOG_TAG, "DeviceMode:" + deviceMode.mode + " - " + deviceMode.recordState);
            if (deviceMode.needOpenLoopVideo()) {
                GolukDebugUtils.e(Const.LOG_TAG, "NeedOpenLoopVideo");
                getPresenter().openLoopMode();
            } else {
                resetCaptureButton();
            }
        }
    }

    @Override
    public void onEnterVideoMode() {
        GolukDebugUtils.e(Const.LOG_TAG, "Exist playback mode success");
    }

    @Override
    public void onExitOtherModeSuccess() {
        GolukDebugUtils.e(Const.LOG_TAG, "Exist other mode success");
        $.toast().text(R.string.recovery_to_record).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getPresenter().getDeviceMode();
            }
        }, 100);
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
        resetCaptureButton();
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
        ipcConnSucess();
    }

}
