package com.mobnote.t1sp.ui.preview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
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

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventHotSpotSuccess;
import com.mobnote.eventbus.EventUpdateAddr;
import com.mobnote.eventbus.EventWifiConnect;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.PlayUrlManager;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.ReadWifiConfig;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.live.GetBaiduAddress;
import com.mobnote.golukmain.photoalbum.PhotoAlbumActivity;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.golukmain.reportlog.ReportLogManager;
import com.mobnote.golukmain.videosuqare.RingView;
import com.mobnote.golukmain.wifibind.WiFiInfo;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifibind.WifiHistorySelectListActivity;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.t1sp.base.ui.AbsActivity;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.listener.OnCaptureListener;
import com.mobnote.t1sp.service.T1SPUdpService;
import com.mobnote.t1sp.ui.setting.DeviceSettingsActivity;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.t1sp.util.ThumbUtil;
import com.mobnote.t1sp.util.ViewUtil;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.GolukVideoUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.wifibind.WifiRsBean;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.player.RtspPlayerView;
import com.rd.car.player.RtspPlayerView.RtspPlayerLisener;

import org.succlz123.okdownload.OkDownloadEnqueueListener;
import org.succlz123.okdownload.OkDownloadError;
import org.succlz123.okdownload.OkDownloadManager;
import org.succlz123.okdownload.OkDownloadRequest;

import java.io.File;
import java.util.List;
import java.util.Timer;

import cn.com.mobnote.eventbus.EventLocationFinish;
import cn.com.mobnote.eventbus.EventShortLocationFinish;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import likly.mvp.MvpBinder;

/**
 * T1SP实时预览界面
 */
@MvpBinder(
        presenter = CarRecorderT1SPPresenterImpl.class,
        model = CarRecorderT1SPModelImpl.class
)
@SuppressLint("NewApi")
public class CarRecorderT1SPActivity extends AbsActivity<CarRecorderT1SPPresenter> implements CarRecorderT1SPView, OnClickListener, OnCaptureListener {
    private Handler mHandler = null;
    /**
     * 保存当前录制的视频类型
     */
    public VideoType mCurVideoType = VideoType.idle;
    /**
     * 保存录制的文件名字
     */
    public String mRecordVideFileName = "";
    /**
     * 保存录制中的状态
     */
    public boolean isRecording = false;
    /**
     * 文件查询时间
     */
    public static final int QUERYFILETIME = 500;
    /**
     * 紧急视频
     */
    public static final int EMERGENCY = 113;

    /**
     * 精彩视频下载检查计时
     */
    public static final int DOWNLOADWONDERFULVIDEO = 119;
    /**
     * 隐藏adasView
     **/
    private static final int CLOSE_ADAS_VIEW = 120;
    private boolean mIsLive = false;

    public enum VideoType {
        mounts, emergency, idle, classic
    }

    /**
     * 8s视频定时器
     */
    private Timer m8sTimer = null;
    // 抓拍按钮
    private Button mBtnCapture = null;

    /**
     * 设置按钮
     */
    private ImageView mSettingBtn = null;
    /**
     * 录制时间显示
     */
    private TextView mTime = null;
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

    private RingView downloadSize = null;

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
     * 加载中动画对象
     */
    //private AnimationDrawable mAnimationDrawable = null;
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
    /**
     * 视频文件生成查询时间（10s超时）
     */
    private int videoFileQueryTime = 0;

    /**
     * 当前录制时间
     */
    private int showRecordTime = 0;
    /**
     * 开启视频录制计时器
     */
    private final int STARTVIDEORECORD = 100;

    private boolean isBackGroundStart = true;
    /**
     * 第一次登录标识
     */
    private boolean ipcFirstLogin = false;
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
     * 精彩视频名称
     */
    private String wonderfulVideoName = null;
    /**
     * 精彩视频下载完成
     */
    private boolean downloadFinish = false;
    /**
     * 控制显示精彩视频下载中提示
     */
    private int downloadNumber = 0;
    /**
     * 精彩视频下载文件个数
     */
    private int downloadFileNumber = 0;
    /**
     * 视频分辨率显示
     */
    private ImageView mVideoResolutions = null;

    private GolukApplication mApp = null;
    private boolean m_bIsFullScreen = false;
    private ViewGroup m_vgNormalParent;
    private ImageButton mFullScreen = null;
    private ImageButton mVideoOff = null;
    private RelativeLayout mPlayerLayout = null;
    private Button mNormalScreen = null;
    private final int BTN_NORMALSCREEN = 231;

    private RelativeLayout mPalyerLayout = null;

    private boolean isShowPlayer = false;

    private int ipcState = 0;

    /**
     * 未连接
     */
    private final int WIFI_STATE_FAILED = 0;
    /**
     * 连接中
     */
    private final int WIFI_STATE_CONNING = 1;
    /**
     * 连接
     */
    private final int WIFI_STATE_SUCCESS = 2;

    private View mNotconnected = null;

    private View mConncetLayout = null;

    private String wifiname;

    /**
     * 视频存放外卡文件路径
     */
    private static final String APP_FOLDER = Environment.getExternalStorageDirectory().getPath();

    private String mImagePath = APP_FOLDER + "/" + "goluk/goluk_carrecorder/image/";

    private ImageView new1;

    private ImageView new2;

    private String SelfContextTag = "carrecorder";

    private String mLocationAddress = "";

    private ImageView mChangeBtn;

    private boolean canReceiveFailed = true;

    private ImageView mAdasImg = null;

    private RelativeLayout mAdasStatusLayout = null;

    private ImageView mAdasIcon = null;
    private AlertDialog mExitAlertDialog;
    /**
     * 精彩视频类型
     **/
    private int mWonderfulTime;

    private SettingInfo mSettingInfo;
    private Handler mHandlerCapture;
    private int mCaptureTime;
    // 最新2视频(精彩视频和紧急视频)
    private List<String> mLatestTwoVideos;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_car_recorder_t1sp;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        EventBus.getDefault().register(this);

        mApp = (GolukApplication) getApplication();

        wifiname = this.getResources().getString(R.string.str_disconnect_ipc);

        mPlayerLayout = new RelativeLayout(this);
        mNormalScreen = new Button(this);
        mNormalScreen.setId(BTN_NORMALSCREEN);
        mNormalScreen.setBackgroundResource(R.drawable.btn_player_normal);
        mNormalScreen.setOnClickListener(this);
        ipcState = mApp.mWiFiStatus;

        mLocationAddress = GolukFileUtils.loadString("loactionAddress", "");

        mHandler = new Handler() {
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                    case STARTVIDEORECORD:
                        updateVideoRecordTime();
                        break;
                    case DOWNLOADWONDERFULVIDEO:
                        wonderfulVideoDownloadShow();
                        break;
                    case CLOSE_ADAS_VIEW:
                        if (mAdasStatusLayout != null) {
                            mAdasStatusLayout.setVisibility(View.GONE);
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        mHandlerCapture = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleCaptureTime();
            }
        };

        initView();
        setListener();
        //initIpcState(ipcState);// 初始化ipc的连接状态

        // 获取是否是后台启动
        Intent receiveIntent = getIntent();
        isBackGroundStart = receiveIntent.getBooleanExtra("isBackGroundStart", false);

        firstShowHint();

        start();

        //getPresenter().getVideoSettingInfo();

        // 设置抓拍回调
        T1SPUdpService.setCaptureListener(this);
        // 获取本地最近2个视频(精彩视频和紧急视频综合)
        getPresenter().getLatestTwoVideos();
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

        }

    }

    @Override
    public void onGetVideoSettingInfo(SettingInfo settingInfo) {
        if (settingInfo == null)
            return;
        mSettingInfo = settingInfo;
        mVideoOff.setBackgroundResource(settingInfo.soundRecord ? R.drawable.recorder_btn_sound : R.drawable.recorder_btn_nosound);
        mVideoResolutions.setBackgroundResource(settingInfo.is1080P() ? R.drawable.icon_hd1080 : R.drawable.icon_hd720);
    }

    @Override
    public void onSetRecordSoundSuccess(boolean onOff) {
        mVideoOff.setBackgroundResource(onOff ? R.drawable.recorder_btn_sound : R.drawable.recorder_btn_nosound);
        if (mSettingInfo != null)
            mSettingInfo.soundRecord = onOff;
    }

    @Override
    public void onCaptureStart() {
        // 抓拍精彩视频开始
        mHandlerCapture.removeMessages(0);
        mCaptureTime = mSettingInfo.captureTimeIs30S() ? CAPTURE_TIME_30 : CAPTURE_TIME_12;
        mHandlerCapture.sendEmptyMessage(0);
    }

    @Override
    public void onGetLatestTwoVideos(List<String> videos) {
        if (videos == null || videos.isEmpty())
            return;

        mLatestTwoVideos = videos;
        image1.setImageBitmap(ThumbUtil.getLocalVideoThumb(videos.get(0)));
        new1.setVisibility(View.VISIBLE);
        if (videos.size() >= 2) {
            image2.setImageBitmap(ThumbUtil.getLocalVideoThumb(videos.get(1)));
            new2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCapturePic(String path) {
    }

    @Override
    public void onCaptureVideo(String path) {
        // 抓拍精彩视频回调
        if (TextUtils.isEmpty(path))
            return;
        // 开始下载
        path = FileUtil.getVideoUrlByPath(path);
        path = path.replace("\\", "/");
        FileDownloader.setup(this);
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Wonderful.MP4";
        FileDownloader.getImpl().create(path).setPath(filePath).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void started(BaseDownloadTask task) {
                // 开始下载
                downloadSize.setVisibility(View.VISIBLE);
                image2.setImageResource(R.drawable.share_video_no_pic);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                int percent = (int) ((double) soFarBytes / (double) totalBytes * 100);
                System.out.print("");
                downloadSize.setProcess(percent);
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                System.out.print("");
                downloadSize.setVisibility(View.GONE);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                System.out.print("");
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                System.out.print("");
            }
        }).start();


        OkDownloadRequest request = new OkDownloadRequest.Builder()
                .url(url)
                .filePath(filePath)
                .build();

        OkDownloadManager.getInstance(mContext).enqueue(request, listener);

        OkDownloadEnqueueListener listener = new OkDownloadEnqueueListener() {

            @Override
            public void onStart(int id) {
                Log.e("OkDownload", "onStart : the download request id = "+id);
                // 开始下载
                downloadSize.setVisibility(View.VISIBLE);
                image2.setImageResource(R.drawable.share_video_no_pic);
            }

            @Override
            public void onProgress(int progress, long cacheSize, long totalSize) {
                Log.e("OkDownload", cacheSize + "/" + totalSize);
                downloadSize.setProcess(progress);
            }

            @Override
            public void onRestart() {
                Log.e("OkDownload", "onRestart");
            }

            @Override
            public void onPause() {
                Log.e("OkDownload", "onPause");
            }

            @Override
            public void onCancel() {
                Log.e("OkDownload", "onCancel");
            }

            @Override
            public void onFinish() {
                Log.e("OkDownload", "onFinish");
                downloadSize.setVisibility(View.GONE);
            }

            @Override
            public void onError(OkDownloadError error) {
                Log.e("OkDownload", error.getMessage());
            }
        };
    }

    @Override
    public void onLockVideo(String path, boolean isLock) {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ipcState = mApp.mWiFiStatus;
        //initIpcState(ipcState);// 初始化ipc的连接状态
    }

//    /**
//     * 验证ipc连接情况
//     *
//     * @param ipcS
//     * @author 曾浩
//     */
//    private void initIpcState(int ipcS) {
//        if (mApp.getEnableSingleWifi() && mApp.isIpcConnSuccess) {
//            //mllStartLive.setVisibility(View.GONE);
//            startPlayVideo();
//            return;
//        }
//        switch (ipcS) {
//            case WIFI_STATE_FAILED:
//                ipcConnFailed();
//                break;
//            case WIFI_STATE_CONNING:
//                mPalyerLayout.setVisibility(View.GONE);
//                if (mApp.isBindSucess()) {
//                    mNotconnected.setVisibility(View.GONE);
//                    mConncetLayout.setVisibility(View.VISIBLE);
//                } else {
//                    mNotconnected.setVisibility(View.VISIBLE);
//                    mConncetLayout.setVisibility(View.GONE);
//                }
//                mSettingBtn.setVisibility(View.GONE);
////                mChangeBtn.setVisibility(View.GONE);
//                mBtnCapture.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
//                setVideoBtnState(false);
//                break;
//            case WIFI_STATE_SUCCESS:
//                mConnectTip.setText(wifiname);
//                startPlayVideo();
//                break;
//            default:
//                break;
//        }
//    }

    /**
     * 跳转到播放本地视频页面
     */
    private void gotoPlayVideo(String videoName) {
        int type;
        if (videoName.indexOf("URG") >= 0) {
            type = PhotoAlbumConfig.PHOTO_BUM_IPC_URG;
        } else if (videoName.indexOf("WND") >= 0) {
            type = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
        } else {
            type = PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP;
        }
        SettingUtils.getInstance().putBoolean("Local_" + videoName, false);
        VideoInfo mVideoInfo = GolukVideoUtils.getVideoInfo(videoName);
        if (mVideoInfo != null) {
            GolukUtils.startPhotoAlbumPlayerActivity(this, type, "local", mVideoInfo.videoPath,
                    mVideoInfo.filename, mVideoInfo.videoCreateDate, mVideoInfo.videoHP, mVideoInfo.videoSize, null);
            overridePendingTransition(R.anim.shortshare_start, 0);
        }
    }

    private void startPlayVideo() {
        mSettingBtn.setVisibility(View.VISIBLE);
        mPalyerLayout.setVisibility(View.VISIBLE);
        mNotconnected.setVisibility(View.GONE);
        mConncetLayout.setVisibility(View.GONE);
        mChangeBtn.setVisibility(View.VISIBLE);

        //setVideoBtnState(true);
        onClick(findViewById(R.id.mPlayBtn));
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
        mVideoOff = (ImageButton) findViewById(R.id.video_off);
        mFullScreen.setVisibility(View.GONE);
        mVideoResolutions = (ImageView) findViewById(R.id.mVideoResolutions);
        mRtmpPlayerLayout = (RelativeLayout) findViewById(R.id.mRtmpPlayerLayout);
        mVLayout = (RelativeLayout) findViewById(R.id.vLayout);
        mBtnCapture = (Button) findViewById(R.id.m8sBtn);
        mSettingBtn = (ImageView) findViewById(R.id.mSettingBtn);
        mTime = (TextView) findViewById(R.id
                .mTime);
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
        downloadSize = (RingView) findViewById(R.id.downloadSize);
        mChangeBtn = (ImageView) findViewById(R.id.changeBtn);

        new1 = (ImageView) findViewById(R.id.new1);
        new2 = (ImageView) findViewById(R.id.new2);
        mAdasImg = (ImageView) findViewById(R.id.adas_status_img);
        mAdasStatusLayout = (RelativeLayout) findViewById(R.id.adas_status);
        mAdasIcon = (ImageView) findViewById(R.id.adas_icon);

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

        //mConnectTip.setText(wifiname);
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
        mVideoOff.setOnClickListener(this);
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

                getPresenter().getVideoSettingInfo();
            }
        });
    }

    private void reportLog() {
        final String jsonData = ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_RTSP_REVIEW)
                .getReportData();
        mApp.uploadMsg(jsonData, false);
        ReportLogManager.getInstance().removeKey(IMessageReportFn.KEY_RTSP_REVIEW);
    }

    private void collectLog(String msg) {
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_RTSP_REVIEW)
                .addLogData(JsonUtil.getReportData("CarRecorderActivity", "rtsp", msg));
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
     * 更新视频录制时间
     *
     * @author xuhw
     * @date 2015年3月11日
     */
    private void updateVideoRecordTime() {
        showRecordTime++;
        if (showRecordTime >= 5 * 60) {
            showRecordTime = 0;
        }

        String timeStr = "";
        if (showRecordTime >= 60) {
            int min = showRecordTime / 60;
            int sec = showRecordTime % 60;

            String minStr = "";
            String secStr = "";
            if (min >= 10) {
                minStr = min + this.getResources().getString(R.string.str_colon_english);
            } else {
                minStr = "0" + min + this.getResources().getString(R.string.str_colon_english);
            }
            if (sec >= 10) {
                secStr = sec + "";
            } else {
                secStr = "0" + sec;
            }

            timeStr = minStr + secStr;
        } else {
            if (showRecordTime >= 10) {
                timeStr = this.getResources().getString(R.string.str_recorder_time1) + showRecordTime;
            } else {
                timeStr = this.getResources().getString(R.string.str_recorder_time2) + showRecordTime;
            }
        }
        mTime.setText(timeStr);
        mHandler.removeMessages(STARTVIDEORECORD);
        mHandler.sendEmptyMessageDelayed(STARTVIDEORECORD, 1000);
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
     *
     * @author xuhw
     * @date 2015年1月28日
     */
    public void start() {
        WifiRsBean wrb = ReadWifiConfig.readConfig();
        if (wrb != null && GolukApplication.getInstance().getIpcIsLogin()) {
            mConnectTip.setText(wrb.getIpc_ssid());
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

    public void onEventMainThread(EventWifiConnect event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.WIFI_STATE_FAILED:
                if (canReceiveFailed) {
                    mApp.setIpcDisconnect();
                    canReceiveFailed = false;
                }
                ipcConnFailed();
                break;
            case EventConfig.WIFI_STATE_CONNING:
                ipcConnecting();

                break;
            case EventConfig.WIFI_STATE_SUCCESS:
                ipcConnSucess();
                break;
            default:
                break;
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
        } else if (id == R.id.m8sBtn) {
            // 视频抓拍
            getPresenter().captureVideo();
        } else if (id == R.id.mSettingBtn) {
            if (m_bIsFullScreen) {
                return;
            }
            ViewUtil.goActivity(this, DeviceSettingsActivity.class);
        } else if (id == R.id.mFullScreen) {
            setFullScreen(true);
        } else if (id == R.id.video_off) {
            // 设置声音录制开关
            if (mSettingInfo == null)
                return;
            getPresenter().setRecordSound(!mSettingInfo.soundRecord);
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
            if (mLatestTwoVideos.size() > 0) {
                new1.setVisibility(View.GONE);
                String videoName = mLatestTwoVideos.get(0);
                videoName = videoName.substring(videoName.lastIndexOf("/") + 1);
                gotoPlayVideo(videoName);
            }
        } else if (id == R.id.image2) {
            if (mLatestTwoVideos.size() > 1) {
                new2.setVisibility(View.GONE);
                String videoName = mLatestTwoVideos.get(1);
                videoName = videoName.substring(videoName.lastIndexOf("/") + 1);
                gotoPlayVideo(videoName);
            }
        } else if (id == R.id.image3) {
            //相册页面访问统计
            ZhugeUtils.eventCallAlbum(this, this.getString(R.string.str_zhuge_call_album_source_ipc));

            Intent photoalbum = new Intent(CarRecorderT1SPActivity.this, PhotoAlbumActivity.class);
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

    /**
     * 显示加载中布局
     *
     * @author xuhw
     * @date 2015年3月8日
     */
    private void showLoading() {
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
//        mConnectTip.setText(wifiname);
        mFullScreen.setVisibility(View.GONE);
        mSettingBtn.setVisibility(View.GONE);
//        mChangeBtn.setVisibility(View.GONE);
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
//        mChangeBtn.setVisibility(View.GONE);

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
//        mChangeBtn.setVisibility(View.VISIBLE);
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

        GolukApplication.getInstance().setContext(this, "carrecorder_t1sp");
        if (isBackGroundStart) {
            this.moveTaskToBack(true);
            isBackGroundStart = false;
        }
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
        if (mApp.isIpcLoginSuccess && !mIsLive) {
            mApp.mIPCControlManager.setVdcpDisconnect();
            mApp.setIpcLoginOut();
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                wifiManager.disableNetwork(wifiInfo.getNetworkId());
            }
        }
        GolukDebugUtils.e("xuhw", "YYYYYY======onDestroy======");
        if (null != mRtspPlayerView) {
            mRtspPlayerView.removeCallbacks(retryRunnable);
            mRtspPlayerView.cleanUp();
        }

        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("main");
        }
        if (mHandler != null) {
            mHandler.removeMessages(STARTVIDEORECORD);
            mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
            mHandler = null;
        }
        if (m8sTimer != null) {
            m8sTimer.cancel();
            m8sTimer.purge();
            m8sTimer = null;
        }
        mWonderfulTime = 0;
        EventBus.getDefault().unregister(this);
        if (mExitAlertDialog != null) {
            if (mExitAlertDialog.isShowing()) {
                mExitAlertDialog.dismiss();
            }
            mExitAlertDialog = null;
        }
        super.onDestroy();
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
        mWonderfulTime = 0;
        reportLog();
        finish();
    }

    public void onEventMainThread(EventShortLocationFinish eventShortLocationFinish) {
        if (null == eventShortLocationFinish) {
            return;
        }
        //mShortLocation = eventShortLocationFinish.getShortAddress();
        //mLocationLon = eventShortLocationFinish.getLon();
        //mLocationLat = eventShortLocationFinish.getLat();
    }

    public void onEventMainThread(EventHotSpotSuccess eventShortLocationFinish) {
        mIsLive = true;
        this.finish();
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

    private void open_shareVideo(String vname) {
        String path = Environment.getExternalStorageDirectory().getPath();
        int type;
        if (vname.indexOf("URG") >= 0) {
            path = path + "/goluk/video/urgent/" + vname;
            type = PhotoAlbumConfig.PHOTO_BUM_IPC_URG;
        } else if (vname.indexOf("WND") >= 0) {
            type = PhotoAlbumConfig.PHOTO_BUM_IPC_WND;
            path = path + "/goluk/video/wonderful/" + vname;
        } else {
            type = PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP;
            path = path + "/goluk/video/loop/" + vname;
        }

        SettingUtils.getInstance().putBoolean("Local_" + vname, false);

        VideoInfo mVideoInfo = GolukVideoUtils.getVideoInfo(vname);
        if (mVideoInfo != null) {
            switch (type) {
                case PhotoAlbumConfig.PHOTO_BUM_IPC_LOOP:
                    ZhugeUtils.eventAlbumPlayer(this,
                            getString(R.string.str_zhuge_video_player_ipc),
                            getString(R.string.str_zhuge_video_player_recycle));
                    break;
                case PhotoAlbumConfig.PHOTO_BUM_IPC_URG:
                    ZhugeUtils.eventAlbumPlayer(this,
                            getString(R.string.str_zhuge_video_player_ipc),
                            getString(R.string.str_zhuge_video_player_urgent));
                    break;
                case PhotoAlbumConfig.PHOTO_BUM_IPC_WND:
                    ZhugeUtils.eventAlbumPlayer(this,
                            getString(R.string.str_zhuge_video_player_ipc),
                            getString(R.string.str_zhuge_video_player_wonderful));
                    break;
                default:
                    break;
            }

            GolukUtils.startPhotoAlbumPlayerActivity(this, type, "local", mVideoInfo.videoPath,
                    mVideoInfo.filename, mVideoInfo.videoCreateDate, mVideoInfo.videoHP, mVideoInfo.videoSize, null);
        }
        overridePendingTransition(R.anim.shortshare_start, 0);
    }

    /**
     * 精彩视频下载显示
     *
     * @author xuhw
     * @date 2015年4月8日
     */
    private void wonderfulVideoDownloadShow() {
        if (!TextUtils.isEmpty(wonderfulVideoName)) {
            downloadNumber++;
            mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);

            if (!downloadFinish) {
                if (1 == downloadNumber) {
                    // mShareBtn.setBackgroundResource(R.drawable.screen_loading_1);
                } else if (2 == downloadNumber) {
                    // mShareBtn.setBackgroundResource(R.drawable.screen_loading_2);
                } else {
                    downloadNumber = 0;
                    // mShareBtn.setBackgroundResource(R.drawable.screen_loading_3);
                }

                mHandler.sendEmptyMessageDelayed(DOWNLOADWONDERFULVIDEO, 600);
            }
        }

    }

}
