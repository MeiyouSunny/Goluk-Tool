package com.mobnote.t1sp.ui.preview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.mobnote.t1sp.api.setting.IPCConfigListener;
import com.mobnote.t1sp.api.setting.IpcConfigOption;
import com.mobnote.t1sp.api.setting.IpcConfigOptionF4;
import com.mobnote.t1sp.base.ui.AbsActivity;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.connect.T1SPConnecter;
import com.mobnote.t1sp.connect.T1SPConntectListener;
import com.mobnote.t1sp.listener.OnCaptureListener;
import com.mobnote.t1sp.ui.album.PhotoAlbumT1SPActivity;
import com.mobnote.t1sp.ui.setting.DeviceSettingsActivity;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.Const;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.t1sp.util.ThumbAsyncTask;
import com.mobnote.t1sp.util.ViewUtil;
import com.mobnote.t2s.files.IpcFileQueryListener;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.GolukVideoUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.wifibind.WifiConnectManager;
import com.mobnote.wifibind.WifiRsBean;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.player.RtspPlayerView;
import com.rd.car.player.RtspPlayerView.RtspPlayerLisener;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackCmd;
import goluk.com.t1s.api.callback.CallbackSDCardStatus;
import goluk.com.t1s.api.callback.CallbackSetting;
import goluk.com.t1s.api.callback.CallbackVersion;
import goluk.com.t1s.api.callback.CallbackWifiInfo;
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
public class CarRecorderT1SPActivity extends AbsActivity<CarRecorderT1SPPresenter> implements CarRecorderT1SPView, OnClickListener, OnCaptureListener, T1SPConntectListener, IpcFileQueryListener, IPCConfigListener {

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
    private TextView mVideoResolutions;

    private boolean m_bIsFullScreen = false;
    /* 全屏/旋转 */
    private ImageButton mFullScreen, mBtnRotate, mBtnSound;

    private RelativeLayout mPalyerLayout, mLayoutVideo;
    private View mNotconnected, mConncetLayout, mLayoutTitle, mLayoutState,
            mLayoutAlumb, mLayoutCapture, mLayoutOptions;
    private ImageView new1, new2, mChangeBtn;
    private String SelfContextTag = "carrecordert1sp";
    private AlertDialog mExitAlertDialog;

    private GolukApplication mApp;
    private WifiManager mWifiManager;
    private WifiConnectManager mWifiConnectManager;
    private boolean isShowPlayer;
    private boolean isBackGroundStart = true;
    /* 是否发起预览链接 */
    private boolean isConnecting;
    private SettingInfo mSettingInfo;
    private Handler mHandlerCapture;
    private int mCaptureTime;
    // 最新2视频(精彩视频和紧急视频)
    private List<String> mLatestTwoVideos;
    // 是否连接上IPC
    private boolean mConnectedIpc;
    // 是否正在抓拍
    private boolean mIsInCapture;

//    private IpcQuery mIpcQuery;
//    private ArrayList<VideoInfo> mCaptueList;

    private List<VideoInfo> mWonderfulVideos;
    private IpcConfigOption mConfigOption;

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
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiConnectManager = new WifiConnectManager(mWifiManager, this);

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

//        mIpcQuery = new IpcFileQueryF4(this, this);
//        startPlay();

        mConfigOption = new IpcConfigOptionF4(this);

        // 查询设备版本和型号信息
        getIpcVersionAndTypeInfo();
        // 查询并下载最近3个抓怕视频
        getPresenter().queryRecent3WonderfulVideo(this);
        // 获取本地最新2个抓拍视频
        getPresenter().refreshWonderfulVideos();
    }

    private void changeToRecordMode() {
        ApiUtil.changeToMovieMode(new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                startRecord();
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
    }

    private void startRecord() {
        ApiUtil.startRecord(true, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                startPlay();
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
    }

    /**
     * 获取分辨率信息
     */
    private void getResolutionInfo() {
        ApiUtil.getSettingInfo(new CallbackSetting() {
            @Override
            public void onGetSettingInfo(goluk.com.t1s.api.bean.SettingInfo settingInfo) {
                if (settingInfo != null) {
                    String[] videoQulities = getResources().getStringArray(R.array.video_qulity_lables);
                    mVideoResolutions.setText(videoQulities[settingInfo.recordSize]);
                }
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
    }

    private boolean mIsSoundOn;
    private void getSoundRecord() {
        ApiUtil.getSettingInfo(new CallbackSetting() {
            @Override
            public void onGetSettingInfo(goluk.com.t1s.api.bean.SettingInfo settingInfo) {
                if (settingInfo != null) {
                    mIsSoundOn = (settingInfo.audioRecord == 1) ? true : false;
                    mBtnSound.setBackgroundResource(mIsSoundOn ? R.drawable.recorder_btn_sound : R.drawable.recorder_btn_nosound);
                }
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
    }

    /**
     * 精彩视频抓拍计时处理
     */
    private void handleCaptureTime() {
        mCaptureTime--;
        mBtnCapture.setText(mCaptureTime + "");

        if (mCaptureTime > 0) {
            mHandlerCapture.sendEmptyMessageDelayed(0, 1000);
        } else {
            GolukDebugUtils.e(Const.LOG_TAG, "Timer count over: " + System.currentTimeMillis() / 1000);
            resetCaptureButton();

            mHandlerCapture.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsInCapture = false;
                    // 下载抓拍视频
                    getPresenter().queryRecent3WonderfulVideo(CarRecorderT1SPActivity.this);
                }
            }, 3000);

        }

    }

    @Override
    public void onCaptureStart() {
        mIsInCapture = true;
        // 抓拍精彩视频开始
        mHandlerCapture.removeMessages(0);
        mCaptureTime = CAPTURE_TIME_16_COUNT_TIME;
        mHandlerCapture.sendEmptyMessage(0);
    }

    @Override
    public void onNoSDCarcChecked() {
        $.toast().text("没有检测到SD卡").show();
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
    public void onCapturePic(String path) {
    }

    @Override
    public void onCaptureVideo(String path) {
        GolukDebugUtils.e(Const.LOG_TAG, "Received capture video path, time:" + System.currentTimeMillis() / 1000 + ", 路径:" + path);

        mIsInCapture = false;
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
        mBtnSound = (ImageButton) findViewById(R.id.ic_sound);
        mVideoResolutions = (TextView) findViewById(R.id.mVideoResolutions);
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
        mBtnSound.setOnClickListener(this);
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
                if (!mConnectedIpc)
                    return false;
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
//                rpv.removeCallbacks(retryRunnable);
//                showLoading();
//                rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
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
                //mBtnRotate.setVisibility(View.VISIBLE);
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
        showLoading();
        WifiRsBean wrb = ReadWifiConfig.readConfig();
        if (wrb != null && GolukApplication.getInstance().getIpcIsLogin()) {
            mConnectTip.setText(getCurrentIpcSsid());
        }
        if (null != mRtspPlayerView) {
            mRtspPlayerView.setVisibility(View.VISIBLE);
            String url = PlayUrlManager.T2S_RTSP_URL;
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
            if (mCaptureTime > 0)
                return;
            getPresenter().captureVideo();
        } else if (id == R.id.mSettingBtn) {
//            if (m_bIsFullScreen || mIsInCapture || !mConnectedIpc) {
//                return;
//            }
            // 进入设置模式
            //mCurrentMode = MODE_SETTING;
            if (mCaptureTime > 0 || !mApp.isIpcConnSuccess)
                return;
            ViewUtil.goActivity(this, DeviceSettingsActivity.class);
        } else if (id == R.id.mFullScreen) {
            setFullScreen(true);
        } else if (id == R.id.ic_rotate || id == R.id.ic_rotate_full_screen) {
            if (mIsInCapture)
                return;
        } else if (id == R.id.ic_exit_full_screen) {
            if (mIsInCapture)
                return;
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
//            if (CollectionUtils.isEmpty(mLatestTwoVideos) || mIsInCapture)
//                return;
//            new1.setVisibility(View.GONE);
//            String videoName = mLatestTwoVideos.get(mLatestTwoVideos.size() == 2 ? 1 : 0);
//            videoName = videoName.substring(videoName.lastIndexOf("/") + 1);
//            gotoPlayVideo(videoName);

            if (mCaptureTime > 0 || !mApp.isIpcConnSuccess)
                return;
            if (CollectionUtils.isEmpty(mWonderfulVideos))
                return;
            VideoInfo videoInfo = mWonderfulVideos.get(0);
            playCaptureVideo(videoInfo);
        } else if (id == R.id.image2) {
//            new2.setVisibility(View.GONE);
//            if (mLatestTwoVideos != null && mLatestTwoVideos.size() == 2) {
//                String videoName = mLatestTwoVideos.get(0);
//                videoName = videoName.substring(videoName.lastIndexOf("/") + 1);
//                gotoPlayVideo(videoName);
//            }

            if (mCaptureTime > 0 || !mApp.isIpcConnSuccess)
                return;
            if (CollectionUtils.isEmpty(mWonderfulVideos) || mWonderfulVideos.size() < 2)
                return;
            VideoInfo videoInfo = mWonderfulVideos.get(1);
            playCaptureVideo(videoInfo);
        } else if (id == R.id.image3) {
//            if (mIsInCapture || !mConnectedIpc)
//                return;
            if (mCaptureTime > 0 || !mApp.isIpcConnSuccess)
                return;
            enterRemoteAlbumPage();
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
        } else if (id == R.id.ic_sound) {
            mIsSoundOn = !mIsSoundOn;
            mBtnSound.setBackgroundResource(mIsSoundOn ? R.drawable.recorder_btn_sound : R.drawable.recorder_btn_nosound);
            mConfigOption.setSoundRecordStatus(mIsSoundOn);
        }
    }

    private void enterRemoteAlbumPage() {
        goluk.com.t1s.api.ApiUtil.checkSDCardStatus(new CallbackSDCardStatus() {

            @Override
            public void onSuccess(int status) {
                if (status == 1) {
                    Intent photoalbum = new Intent(CarRecorderT1SPActivity.this, PhotoAlbumT1SPActivity.class);
                    photoalbum.putExtra("from", "cloud");
                    startActivity(photoalbum);
                } else {
                    onNoSDCarcChecked();
                }
            }
        });
    }

    private void playCaptureVideo(VideoInfo videoInfo) {
//        GolukUtils.startPhotoAlbumPlayerActivityT2S(this, PhotoAlbumConfig.PHOTO_BUM_IPC_WND, "ipc", videoInfo.videoUrl, videoInfo.relativePath, videoInfo.filename, videoInfo.videoCreateDate, videoInfo.videoHP, videoInfo.videoSize, null, true, videoInfo);

        GolukUtils.startPhotoAlbumPlayerActivity(this, PhotoAlbumConfig.PHOTO_BUM_IPC_WND, "local", videoInfo.videoPath,
                videoInfo.filename, videoInfo.videoCreateDate, videoInfo.videoHP, videoInfo.videoSize, null);
    }

    /**
     * 显示加载中布局
     *
     * @author xuhw
     * @date 2015年3月8日
     */
    public void showLoading() {
//        if (!NetUtil.isWifiConnected(this) || !mConnectedIpc)
//            return;
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

    @Override
    public void onRefreshWonderfulVideos(List<VideoInfo> videoInfos) {
        if (CollectionUtils.isEmpty(videoInfos))
            return;
        mWonderfulVideos = videoInfos;
        if (videoInfos.size() == 1) {
            new ThumbAsyncTask(this, mLocalAlbumOne).execute(videoInfos.get(0).videoPath);
        } else if (videoInfos.size() == 2) {
            new ThumbAsyncTask(this, mLocalalbumTwo).execute(videoInfos.get(0).videoPath);
            new ThumbAsyncTask(this, mLocalAlbumOne).execute(videoInfos.get(1).videoPath);
        }
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
        mVideoResolutions.setVisibility(View.GONE);
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
        mVideoResolutions.setVisibility(View.GONE);

        disableCaptureButton();
        hideLoading();
    }

    private void ipcConnSucess() {
        mConnectedIpc = true;
        if (isShowPlayer || isConnecting) {
            //showLoading();
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
        mVideoResolutions.setVisibility(View.VISIBLE);

        resetCaptureButton();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (GolukApplication.getInstance().getIpcIsLogin()) {
            changeToRecordMode();
            getResolutionInfo();
            getSoundRecord();
//            mIpcQuery.queryCaptureVideoList();
        }
//        if (isShowPlayer) {
//            if (!isConnecting) {
//                showLoading();
//                hidePlayer();
//                isConnecting = true;
//                //startPlay();
//            }
//        }
//
//        //GolukApplication.getInstance().setContext(this, "carrecorder_t1sp");
//        if (isBackGroundStart) {
//            this.moveTaskToBack(true);
//            isBackGroundStart = false;
//        }
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
//        if (mApp.isIpcLoginSuccess && T1SPConnecter.instance().needDisconnectWIFI()) {
//            mApp.mIPCControlManager.setVdcpDisconnect();
//            mApp.setIpcLoginOut();
//            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
//            if (wifiInfo != null) {
//                mWifiManager.disableNetwork(wifiInfo.getNetworkId());
//            }
//        }
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
        if (mWifiConnectManager != null) {
            mWifiConnectManager.unbind();
            mWifiConnectManager = null;
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
                if (!NetUtil.isWIFIConnected(this)) {
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
    }

    @Override
    public void onOpenLoopModeSuccess() {
        if (!mConnectedIpc)
            return;
        GolukDebugUtils.e(Const.LOG_TAG, "Open LoopRecord success");
        //$.toast().text(R.string.recovery_to_record).show();
        resetCaptureButton();
        //startPlay();
    }

    @Override
    public void onOpenLoopModeFailed() {
        if (!mConnectedIpc || isDestroyed())
            return;
        GolukDebugUtils.e(Const.LOG_TAG, "Open LoopRecord failed");
        $.toast().text("进入录像模式失败").show();
        finish();
    }

    @Override
    public void onOpenLoopModeErrorNoSdCard() {
        GolukDebugUtils.e(Const.LOG_TAG, "Open LoopRecord failed, No SdCard");
        resetCaptureButton();
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
        }
    }

    @Override
    public void onNormalVideoListQueryed(ArrayList<VideoInfo> fileList) {
    }

    @Override
    public void onUrgentVideoListQueryed(ArrayList<VideoInfo> fileList) {
    }

    @Override
    public void onCaptureVideoListQueryed(ArrayList<VideoInfo> fileList) {
//        mCaptueList = fileList;
//        if (fileList.size() <= 1) {
//            mLocalAlbumOne.setVisibility(View.VISIBLE);
//            mLocalalbumTwo.setVisibility(View.GONE);
//        } else if (fileList.size() >= 2) {
//            mLocalAlbumOne.setVisibility(View.VISIBLE);
//            mLocalAlbumOne.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void onTimeslapseVideoListQueryed(ArrayList<VideoInfo> fileList) {
    }

    @Override
    public void onGetVideoListIsEmpty() {
    }

    @Override
    public void onQueryVideoListFailed() {
    }

    /**
     * 查询设备版本和型号信息
     */
    private void getIpcVersionAndTypeInfo() {
        ApiUtil.getVersion(new CallbackVersion() {
            @Override
            public void onSuccess(String version) {
                System.out.println("");
                SharedPrefUtil.saveIPCVersion(version);
            }

            @Override
            public void onFail() {
                System.out.println("");
            }
        });

        ApiUtil.queryWifiInfo(new CallbackWifiInfo() {
            @Override
            public void onSuccess(String wifiName, String wifiPwd) {
                System.out.println("");
                saveModeTypeByWifiName(wifiName);
            }

            @Override
            public void onFail() {
                System.out.println("");
            }
        });

        ApiUtil.getSN(new CallbackVersion() {
            @Override
            public void onSuccess(String sn) {
                SharedPrefUtil.saveIPCNumber(sn);
            }

            @Override
            public void onFail() {
            }
        });
    }

    private void saveModeTypeByWifiName(String wifiName) {
        if (TextUtils.isEmpty(wifiName))
            return;
        String type = "";
        if (wifiName.startsWith("Goluk_T4U")) {
            type = "T4U";
        } else if (wifiName.startsWith("Goluk_T4")) {
            type = "T4";
        } else if (wifiName.startsWith("Goluk_T2SU")) {
            type = "T2SU";
        } else if (wifiName.startsWith("Goluk_T2S")) {
            type = "T2S";
        } else if (wifiName.startsWith("Goluk_T1S")) {
            type = "T1S";
        }

        SharedPrefUtil.saveIpcModel(type);
        mApp.getIPCControlManager().setProduceName(type);
    }

    @Override
    public void onDeviceTimeSet(boolean success) {

    }

    @Override
    public void onDeviceTimeGet(long timestamp) {

    }

    @Override
    public void onParkSleepModeSet(boolean success) {

    }

    @Override
    public void onDriveFatigueSet(boolean success) {

    }

    @Override
    public void onParkSleepModeGet(boolean enable) {

    }

    @Override
    public void onDriveFatigueGet(boolean enable) {

    }

    @Override
    public void onParkSecurityModeSet(boolean success) {

    }

    @Override
    public void onParkSecurityModeGet(boolean enable) {

    }

    @Override
    public void onRecordStatusGet(boolean enable) {

    }

    @Override
    public void onRecordStatusSet(boolean success) {

    }

    @Override
    public void onSoundRecordStatusGet(boolean enable) {

    }

    @Override
    public void onSoundRecordStatusSet(boolean success) {

    }

    @Override
    public void onWatermarkStatusGet(boolean enable) {

    }

    @Override
    public void onWatermarkStatusSet(boolean success) {

    }

    @Override
    public void onSoundPowerStatusGet(boolean enable) {

    }

    @Override
    public void onSoundPowerAndCaptureStatusSet(boolean success) {

    }

    @Override
    public void onSoundCaptureStatusGet(boolean enable) {

    }

    @Override
    public void onSoundUrgentStatusGet(boolean enable) {

    }

    @Override
    public void onSoundUrgentStatusSet(boolean success) {

    }

    @Override
    public void onVolumeValueGet(int value) {

    }

    @Override
    public void onVolumeValueSet(boolean success) {

    }

    @Override
    public void onCaptureVideoQulityGet(int index) {

    }

    @Override
    public void onCaptureVideoQulitySet(boolean success) {

    }

    @Override
    public void onCaptureVideoTypeGet(int value) {

    }

    @Override
    public void onCaptureVideoTypeSet(boolean success) {

    }

    @Override
    public void onCollisionSensityGet(int value) {

    }

    @Override
    public void onCollisionSensitySet(boolean success) {

    }

    @Override
    public void onVideoEncodeConfigGet(int index) {

    }

    @Override
    public void onVideoEncodeConfigSet(boolean success) {

    }

    @Override
    public void onSDCapacityGet(double total, double free) {

    }

    @Override
    public void onFormatSDCardResult(boolean success) {

    }

    @Override
    public void onResetFactoryResult(boolean success) {

    }

    @Override
    public void onTimeslapseConfigGet(boolean enable) {

    }

    @Override
    public void onTimeslapseConfigSet(boolean success) {

    }

    @Override
    public void onLanguageGet(int type) {

    }

    @Override
    public void onLanguageSet(boolean success) {

    }

    @Override
    public void onAutoRotateGet(boolean enable) {

    }

    @Override
    public void onAutoRotateSet(boolean success) {

    }

    @Override
    public void onCycleRecTimeGet(int timeType) {

    }

    @Override
    public void onCycleRecTimeSet(boolean success) {

    }
}
