package cn.com.mobnote.golukmobile.carrecorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.entity.LngLat;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserLoginActivity;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser.TriggerRecord;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoShareInfo;
import cn.com.mobnote.golukmobile.carrecorder.settings.SettingsActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.ReadWifiConfig;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.Utils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress.IBaiduGeoCoderFn;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.live.LiveSettingBean;
import cn.com.mobnote.golukmobile.live.LiveSettingPopWindow;
import cn.com.mobnote.golukmobile.live.LiveSettingPopWindow.IPopwindowFn;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.golukmobile.startshare.VideoEditActivity;
import cn.com.mobnote.golukmobile.videosuqare.RingView;
import cn.com.mobnote.golukmobile.wifibind.WiFiLinkIndexActivity;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.api.Image;
import cn.com.tiros.debug.GolukDebugUtils;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.player.RtmpPlayerView;

/**
 * 1.编辑器必须显示空白处
 * 
 * 2.所有代码必须使用TAB键缩进
 * 
 * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
 * 
 * 4.注释必须在行首写.(枚举除外)
 * 
 * 5.函数使用块注释,代码逻辑使用行注释
 * 
 * 6.文件头部必须写功能说明
 * 
 * 7.所有代码文件头部必须包含规则说明
 * 
 * 行车记录仪处理类
 * 
 * 2015年3月8日
 * 
 * @author xuhw
 */
@SuppressLint("NewApi")
public class CarRecorderActivity extends BaseActivity implements
		OnClickListener, OnTouchListener, IPCManagerFn, IPopwindowFn,
		ILocationFn {
	public static Handler mHandler = null;
	/** 保存当前录制的视频类型 */
	public VideoType mCurVideoType = VideoType.idle;
	/** 保存录制的文件名字 */
	public String mRecordVideFileName = "";
	/** 保存录制中的状态 */
	public boolean isRecording = false;
	/** 文件查询时间 */
	public static final int QUERYFILETIME = 500;
	/** 定时查询录制视频文件是否存在 */
	public static final int QUERYFILEEXIT = 112;
	/** 紧急视频 */
	public static final int EMERGENCY = 113;
	/** 8s视频 */
	public static final int MOUNTS = 114;
	/** 精彩视频下载检查计时 */
	public static final int DOWNLOADWONDERFULVIDEO = 119;

	public enum VideoType {
		mounts, emergency, idle
	};

	/** 定时截图 */
	public static final int SCREENSHOOT = 111;
	/** 定时截图时间 */
	public static final int SCREENSHOOTTIME = 5 * 60000;
	/** 8s视频定时器 */
	private Timer m8sTimer = null;
	/** 当前拍摄时间 */
	private int mShootTime = 0;
	/** 一键抢拍按钮 */
	private ImageButton m8sBtn = null;
	/** 发起直播 **/
	private ImageButton liveBtn = null;
	/** 文件管理按钮 */
	private ImageView mFileBtn = null;
	/** 设置按钮 */
	private ImageView mSettingBtn = null;
	/** 录制时间显示 */
	private TextView mTime = null;
	/** 当前地址显示 */
	private TextView mAddr = null;
	/** 加载中布局 */
	private LinearLayout mLoadingLayout = null;
	/** 加载中动画显示控件 */
	private ImageView mLoading = null;

	private RingView downloadSize = null;

	private String videoname = "";

	private int videoType = 1;

	/**
	 * 最新两个精彩视频或抢拍视频
	 */
	private ImageView image1 = null;
	private ImageView image2 = null;

	/** 进入相册 **/
	private ImageView image3 = null;

	/** 加载中动画对象 */
	private AnimationDrawable mAnimationDrawable = null;
	/** 加载中显示文字 */
	private TextView mLoadingText = null;
	/** rtsp视频播放器 */
	private RtmpPlayerView mRtmpPlayerView = null;
	/** 重新连接IPC时间间隔 */
	private final int RECONNECTIONTIME = 5000;
	/** 视频文件生成查询时间（10s超时） */
	private int videoFileQueryTime = 0;
	/** 更新位置信息 */
	public static final int ADDR = 118;
	/** 图像预览是否成功 */
	private boolean rtmpIsOk = false;
	/** 当前录制时间 */
	private int showRecordTime = 0;
	/** 开启视频录制计时器 */
	private final int STARTVIDEORECORD = 100;

	private boolean isBackGroundStart = true;
	/** 第一次登录标识 */
	private boolean ipcFirstLogin = false;
	/** 是否发起预览链接 */
	private boolean isConnecting = false;
	private RelativeLayout mVLayout = null;
	private RelativeLayout mRtmpPlayerLayout = null;
	private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	private float density = SoundUtils.getInstance().getDisplayMetrics().density;
	/** 连接状态 */
	private TextView mConnectTip = null;
	/** 精彩视频名称 */
	private String wonderfulVideoName = null;
	/** 分享按钮 */
	// private Button mShareBtn = null;
	/** 精彩视频下载完成 */
	private boolean downloadFinish = false;
	/** 控制显示精彩视频下载中提示 */
	private int downloadNumber = 0;
	/** 精彩视频下载文件个数 */
	private int downloadFileNumber = 0;
	/** 音视频信息 */
	private VideoConfigState mVideoConfigState = null;
	/** 视频分辨率显示 */
	private ImageView mVideoResolutions = null;

	private GolukApplication mApp = null;
	private boolean m_bIsFullScreen = false;
	private ViewGroup m_vgNormalParent;
	private ImageButton mFullScreen = null;
	private RelativeLayout mPlayerLayout = null;
	private Button mNormalScreen = null;
	private final int BTN_NORMALSCREEN = 231;

	private RelativeLayout mPalyerLayout = null;

	private TextView jcqp = null;

	private TextView fqzb = null;

	private boolean isShowPlayer = false;

	private LiveSettingPopWindow lsp;

	private RelativeLayout mRootLayout = null;

	private LayoutInflater mLayoutFlater = null;

	private TextView liveTime = null;

	/** 用户设置数据 */
	LiveSettingBean mSettingData = null;

	/** 更多 **/
	private Button more = null;

	private int ipcState = 0;

	/** 未连接 */
	private final int WIFI_STATE_FAILED = 0;
	/** 连接中 */
	private final int WIFI_STATE_CONNING = 1;
	/** 连接 */
	private final int WIFI_STATE_SUCCESS = 2;

	private ImageView liveVideo;

	private View mNotconnected = null;

	private View mConncetLayout = null;

	private EditText et = null;

	private VideoShareInfo[] images = null;

	private ImageView live_gps;
	private ImageView live_talk;
	private ImageView live_release;

	private String wifiname = "未连接到极路客";

	/** 视频存放外卡文件路径 */
	private static final String APP_FOLDER = android.os.Environment
			.getExternalStorageDirectory().getPath();

	private String mFilePath = APP_FOLDER + "/" + "goluk/video/";

	private String mImagePath = APP_FOLDER + "/"
			+ "goluk/goluk_carrecorder/image/";

	private int[] a = new int[2];
	private int[] e = new int[2];

	private ImageView new1;

	private ImageView new2;

	private String SelfContextTag = "carrecorder";

	private IBaiduGeoCoderFn mBaiduGeoCoderFn = null;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLayoutFlater = LayoutInflater.from(this);
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(
				R.layout.carrecorder_main, null);

		setContentView(R.layout.carrecorder_main);
		mApp = (GolukApplication) getApplication();

		mPlayerLayout = new RelativeLayout(this);
		mNormalScreen = new Button(this);
		mNormalScreen.setId(BTN_NORMALSCREEN);
		mNormalScreen.setBackgroundResource(R.drawable.btn_player_normal);
		mNormalScreen.setOnClickListener(this);
		ipcState = mApp.mWiFiStatus;

		lsp = new LiveSettingPopWindow(this, mRootLayout);
		lsp.setCallBackNotify(this);

		mSettingData = lsp.getCurrentSetting();

		mApp.addLocationListener(SelfContextTag, this);

		mBaiduGeoCoderFn = new IBaiduGeoCoderFn() {

			@Override
			public void CallBack_BaiduGeoCoder(int function, Object obj) {
				// TODO Auto-generated method stub
				if (function == GetBaiduAddress.FUN_GET_ADDRESS && obj != null) {
					mAddr.setText((String) obj);
				}
			}

		};

		GetBaiduAddress.getInstance().setCallBackListener(mBaiduGeoCoderFn);

		mHandler = new Handler() {
			public void handleMessage(final android.os.Message msg) {
				switch (msg.what) {
				case QUERYFILEEXIT:
					queryFileExit();
					break;
				case MOUNTS:
					startTrimVideo();
					break;
				case ADDR:
					String addr = (String) msg.obj;
					System.out.println("zh-gps = " + addr);
					if (!TextUtils.isEmpty(addr)) {
						mAddr.setText(addr);
					}
					break;
				case STARTVIDEORECORD:
					updateVideoRecordTime();
					break;
				case DOWNLOADWONDERFULVIDEO:
					wonderfulVideoDownloadShow();
					break;
				case WIFI_STATE_SUCCESS:
					GolukDebugUtils.e("zh：连接成功 ", "");
					WifiRsBean wrb = ReadWifiConfig.readConfig();
					if (wrb != null) {
						mConnectTip.setText(wrb.getIpc_ssid());
					}

					mPalyerLayout.setVisibility(View.VISIBLE);
					mNotconnected.setVisibility(View.GONE);
					mConncetLayout.setVisibility(View.GONE);

					mSettingBtn
							.setBackgroundResource(R.drawable.carrecorder_setting);
					m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
					liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon);
					break;
				case WIFI_STATE_FAILED:

					mPalyerLayout.setVisibility(View.GONE);
					mNotconnected.setVisibility(View.VISIBLE);
					mConncetLayout.setVisibility(View.GONE);

					mSettingBtn
							.setBackgroundResource(R.drawable.driving_car_setting_1);
					m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
					liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
					break;
				case WIFI_STATE_CONNING:

					if (isBindSucess()) {
						mPalyerLayout.setVisibility(View.GONE);
						mNotconnected.setVisibility(View.GONE);
						mConncetLayout.setVisibility(View.VISIBLE);

						mSettingBtn
								.setBackgroundResource(R.drawable.driving_car_setting_1);
						m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
						liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
					} else {
						mPalyerLayout.setVisibility(View.GONE);
						mNotconnected.setVisibility(View.VISIBLE);
						mConncetLayout.setVisibility(View.GONE);

						mSettingBtn
								.setBackgroundResource(R.drawable.driving_car_setting_1);
						m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
						liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
					}

					break;
				}
			};
		};

		GolukApplication.getInstance().stopDownloadList();// 停止视频同步

		initView();
		setListener();
		initIpcState(ipcState);// 初始化ipc的连接状态

		// 获取是否是后台启动
		Intent receiveIntent = getIntent();
		isBackGroundStart = receiveIntent.getBooleanExtra("isBackGroundStart",
				false);

		// 注册回调监听
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager()
					.addIPCManagerListener("main", this);
		}

	}

	// 是否綁定过 Goluk
	private boolean isBindSucess() {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind",
				MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
		return preferences.getBoolean("isbind", false);
	}

	/**
	 * 验证ipc连接情况
	 * 
	 * @Title: setInitIpc
	 * @Description: TODO
	 * @param ipcS
	 *            void
	 * @author 曾浩
	 * @throws
	 */
	@SuppressLint("NewApi")
	private void initIpcState(int ipcS) {
		System.out.println("曾浩" + ipcS);
		switch (ipcS) {
		case WIFI_STATE_FAILED:
			mPalyerLayout.setVisibility(View.GONE);
			mNotconnected.setVisibility(View.VISIBLE);
			mConncetLayout.setVisibility(View.GONE);

			mSettingBtn.setBackgroundResource(R.drawable.driving_car_setting_1);
			m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
			liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
			break;
		case WIFI_STATE_CONNING:
			if (isBindSucess()) {
				mPalyerLayout.setVisibility(View.GONE);
				mNotconnected.setVisibility(View.GONE);
				mConncetLayout.setVisibility(View.VISIBLE);
				mSettingBtn
						.setBackgroundResource(R.drawable.driving_car_setting_1);
				m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
				liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
			} else {
				mPalyerLayout.setVisibility(View.GONE);
				mNotconnected.setVisibility(View.VISIBLE);
				mConncetLayout.setVisibility(View.GONE);

				mSettingBtn
						.setBackgroundResource(R.drawable.driving_car_setting_1);
				m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
				liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
			}

			break;
		case WIFI_STATE_SUCCESS:
			GolukApplication.getInstance().stopDownloadList();

			WifiRsBean wrb = ReadWifiConfig.readConfig();
			if (wrb != null) {
				mConnectTip.setText(wrb.getIpc_ssid());
			}
			/*
			 * if (null != mApp.mIpcUpdateManage) {
			 * mApp.mIpcUpdateManage.ipcConnect(); }
			 */
			mPalyerLayout.setVisibility(View.VISIBLE);
			mNotconnected.setVisibility(View.GONE);
			mConncetLayout.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	private void click_ConnFailed() {

		if (!isBindSucess()) {
			Intent wifiIndex = new Intent(CarRecorderActivity.this,
					WiFiLinkIndexActivity.class);
			startActivity(wifiIndex);
		} else {
			mNotconnected.setVisibility(View.GONE);
			mConncetLayout.setVisibility(View.VISIBLE);
			mPalyerLayout.setVisibility(View.GONE);
			if (null != MainActivity.mMainHandler) {
				MainActivity.mMainHandler.sendEmptyMessage(400);
			}
		}
	}

	private void open_shareVideo(String videoname) {
		// // 跳转到wifi连接首页
		// if (mApp.isUserLoginSucess) {
		String path = Environment.getExternalStorageDirectory().getPath();
		int type = 2;
		if (videoname.indexOf("URG") >= 0) {
			path = path + "/goluk/video/urgent/" + videoname;
			type = 3;
		} else {
			path = path + "/goluk/video/wonderful/" + videoname;
		}
		GolukDebugUtils.e("xuhw", "YYY====mShareBtn===path=" + path);

		SettingUtils.getInstance().putBoolean("Local_" + videoname, false);

		Intent i = new Intent(CarRecorderActivity.this, VideoEditActivity.class);
		i.putExtra("cn.com.mobnote.video.path", path);
		i.putExtra("type", type);
		startActivity(i);
		overridePendingTransition(R.anim.shortshare_start, 0);
		// } else {
		// Intent intent = new Intent(this, UserLoginActivity.class);
		// intent.putExtra("isInfo", "back");
		// startActivity(intent);
		// }

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
			// mShareBtn.setVisibility(View.VISIBLE);

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
		m8sBtn = (ImageButton) findViewById(R.id.m8sBtn);
		mSettingBtn = (ImageView) findViewById(R.id.mSettingBtn);
		mTime = (TextView) findViewById(R.id.mTime);
		mAddr = (TextView) findViewById(R.id.mAddr);
		mConnectTip = (TextView) findViewById(R.id.mConnectTip);
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();
		mLoadingText = (TextView) findViewById(R.id.mLoadingText);
		jcqp = (TextView) findViewById(R.id.jcqp);
		fqzb = (TextView) findViewById(R.id.fqzb);
		liveTime = (TextView) findViewById(R.id.live_time);
		more = (Button) findViewById(R.id.car_recorder);

		liveBtn = (ImageButton) findViewById(R.id.liveBtn);
		mRtmpPlayerView = (RtmpPlayerView) findViewById(R.id.mRtmpPlayerView);
		image1 = (ImageView) findViewById(R.id.image1);
		image2 = (ImageView) findViewById(R.id.image2);
		image3 = (ImageView) findViewById(R.id.image3);
		liveVideo = (ImageView) findViewById(R.id.live_video);
		downloadSize = (RingView) findViewById(R.id.downloadSize);

		live_gps = (ImageView) findViewById(R.id.live_gps_icon);
		live_talk = (ImageView) findViewById(R.id.live_talk_icon);
		live_release = (ImageView) findViewById(R.id.live_release_icon);

		new1 = (ImageView) findViewById(R.id.new1);
		new2 = (ImageView) findViewById(R.id.new2);

		mRtmpPlayerView.setAudioMute(true);
		mRtmpPlayerView.setZOrderMediaOverlay(true);
		mRtmpPlayerView.setBufferTime(1000);
		mRtmpPlayerView.setConnectionTimeout(30000);
		mRtmpPlayerView.setVisibility(View.VISIBLE);
		mConncetLayout = findViewById(R.id.mConncetLayout);
		mNotconnected = findViewById(R.id.mNotconnected);
		et = (EditText) findViewById(R.id.assess);
		liveTime.setText(GolukUtils.secondToString(mSettingData.duration));
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRtmpPlayerLayout
				.getLayoutParams();
		lp.width = screenWidth;
		lp.height = (int) (screenWidth / 1.7833);
		lp.leftMargin = 0;
		mRtmpPlayerLayout.setLayoutParams(lp);

		mConnectTip.setText(wifiname);
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
		}

		String addr = GolukApplication.getInstance().mCurAddr;
		if (!TextUtils.isEmpty(addr)) {
			mAddr.setText(addr);
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
		m8sBtn.setOnClickListener(this);
		jcqp.setOnClickListener(this);
		fqzb.setOnClickListener(this);
		more.setOnClickListener(this);
		liveBtn.setOnClickListener(this);
		mNotconnected.setOnClickListener(this);
		image1.setOnClickListener(this);
		image2.setOnClickListener(this);
		image3.setOnClickListener(this);
		more.setOnTouchListener(this);
		live_gps.setOnClickListener(this);
		live_release.setOnClickListener(this);
		live_talk.setOnClickListener(this);
		liveVideo.setOnClickListener(this);
		liveTime.setOnClickListener(this);
		mRtmpPlayerView.setOnClickListener(this);
		mConncetLayout.setOnClickListener(this);

		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.mSettingBtn).setOnClickListener(this);
		mRtmpPlayerView
				.setPlayerListener(new RtmpPlayerView.RtmpPlayerViewLisener() {

					@Override
					public void onPlayerPrepared(RtmpPlayerView rpv) {
						mRtmpPlayerView.setHideSurfaceWhilePlaying(true);
					}

					@Override
					public boolean onPlayerError(RtmpPlayerView rpv, int what,
							int extra, String strErrorInfo) {
						hidePlayer();
						rtmpIsOk = false;
						rpv.removeCallbacks(retryRunnable);
						showLoading();
						rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
						if (m_bIsFullScreen) {
							setFullScreen(false);
						}
						return false;
					}

					@Override
					public void onPlayerCompletion(RtmpPlayerView rpv) {
						hidePlayer();
						rtmpIsOk = false;
						rpv.removeCallbacks(retryRunnable);
						showLoading();
						rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
						if (m_bIsFullScreen) {
							setFullScreen(false);
						}
					}

					@Override
					public void onPlayBuffering(RtmpPlayerView rpv,
							boolean start) {
						if (start) {
							// 缓冲开始
							showLoading();
						} else {
							// 缓冲结束
							hideLoading();
						}
					}

					@Override
					public void onGetCurrentPosition(RtmpPlayerView rpv,
							int nPosition) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onPlayerBegin(RtmpPlayerView arg0) {
						hideLoading();
						rtmpIsOk = true;
						showPlayer();
						mFullScreen.setVisibility(View.VISIBLE);
						isShowPlayer = true;
					}
				});
	}

	/**
	 * 隐藏播放器
	 * 
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	private void hidePlayer() {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVLayout
				.getLayoutParams();
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
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVLayout
				.getLayoutParams();
		lp.width = width;
		lp.height = (int) (width / 1.777);
		lp.leftMargin = 0;
		mVLayout.setLayoutParams(lp);
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
				minStr = min + ":";
			} else {
				minStr = "0" + min + ":";
			}
			if (sec >= 10) {
				secStr = sec + "";
			} else {
				secStr = "0" + sec;
			}

			timeStr = minStr + secStr;
		} else {
			if (showRecordTime >= 10) {
				timeStr = "00:" + showRecordTime;
			} else {
				timeStr = "00:0" + showRecordTime;
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
		if (null != mRtmpPlayerView) {
			mRtmpPlayerView.setVisibility(View.VISIBLE);
			String preUrl = getResources().getString(R.string.default_rtsp_pre);
			String backUrl = getResources().getString(
					R.string.default_rtsp_back);
			String url = preUrl + mApp.mIpcIp + backUrl;

			GolukDebugUtils.e("xuhw", "YYYYYY======url==" + url);

			mRtmpPlayerView.setDataSource(url);

			mRtmpPlayerView.start();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (!isAllowedClicked())
			return;
		Log.e("", "GGGGGGGGGGG-------------id=" + arg0.getId());
		switch (arg0.getId()) {
		case R.id.back_btn:
			if (m_bIsFullScreen) {
				return;
			}

			exit();
			break;
		case R.id.m8sBtn:
			if (m_bIsFullScreen) {
				return;
			}

			GolukDebugUtils.e("xuhw",
					"m8sBtn========================11111======");
			GFileUtils
					.writeIPCLog("=============================发起精彩视频命令===========m8sBtn=============");
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				if (!isRecording) {
					m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon6);
					isRecording = true;
					mCurVideoType = VideoType.mounts;
					GolukDebugUtils.e("xuhw",
							"m8sBtn========================2222======");
					GFileUtils
							.writeIPCLog("=============================发起精彩视频命令================queryParam=");
					boolean isSucess = GolukApplication.getInstance()
							.getIPCControlManager().startWonderfulVideo();

					GolukDebugUtils.e("xuhw",
							"m8sBtn========================333===isSucess==="
									+ isSucess);
					if (!isSucess) {
						videoTriggerFail();
						GFileUtils
								.writeIPCLog("=============================发起精彩视频命令============fail===============");
					}
				}
			} else {
				return;
			}
			break;
		case R.id.mSettingBtn:
			if (m_bIsFullScreen) {
				return;
			}

			if (GolukApplication.getInstance().getIpcIsLogin()) {
				Intent setting = new Intent(CarRecorderActivity.this,
						SettingsActivity.class);
				startActivity(setting);
			} else {
				return;
			}
			break;
		case R.id.mFullScreen:
			setFullScreen(true);
			break;
		case BTN_NORMALSCREEN:
			setFullScreen(false);
			break;
		case R.id.jcqp:
			if (videoType == 0) {

				jcqp.setTextColor(getResources().getColor(
						R.color.text_select_color));
				fqzb.setTextColor(getResources().getColor(
						R.color.text_diff_color));
				liveBtn.setVisibility(View.INVISIBLE);
				m8sBtn.setVisibility(View.VISIBLE);

				a.clone();// 清空数组
				e.clone();// 清空数组

				jcqp.getLocationInWindow(a);
				fqzb.getLocationInWindow(e);

				slideview(e[0] - a[0], jcqp);
				slideview(e[0] - a[0], fqzb);

				findViewById(R.id.fqzb_info).setVisibility(View.INVISIBLE);
				findViewById(R.id.jcqp_info).setVisibility(View.VISIBLE);

				videoType = 1;
			}

			break;
		case R.id.fqzb:

			if (videoType == 1) {
				jcqp.setTextColor(getResources().getColor(
						R.color.text_diff_color));
				fqzb.setTextColor(getResources().getColor(
						R.color.text_select_color));
				liveBtn.setVisibility(View.VISIBLE);
				m8sBtn.setVisibility(View.INVISIBLE);

				findViewById(R.id.fqzb_info).setVisibility(View.VISIBLE);
				findViewById(R.id.jcqp_info).setVisibility(View.INVISIBLE);

				a.clone();// 清空数组
				e.clone();// 清空数组

				jcqp.getLocationInWindow(a);
				fqzb.getLocationInWindow(e);

				slideview(a[0] - e[0], jcqp);
				slideview(a[0] - e[0], fqzb);
				videoType = 0;
			}

			break;
		case R.id.mPlayBtn:
			if (!isShowPlayer) {
				if (!isConnecting) {
					isConnecting = true;
					start();
				}

				showLoading();
				hidePlayer();
				mPalyerLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.car_recorder:
			lsp.show();
			break;
		case R.id.mNotconnected:
			click_ConnFailed();
			break;
		case R.id.mConncetLayout:
			break;
		case R.id.liveBtn:

			if (GolukApplication.getInstance().getIpcIsLogin()) {
				if (mApp.isUserLoginSucess == false) {
					Intent it = new Intent(this, UserLoginActivity.class);
					it.putExtra("isInfo", "back");
					startActivity(it);
				} else {
					Intent intent = new Intent(this, LiveActivity.class);
					String desc = et.getText().toString();
					if (null == desc || "".equals(desc)) {
						desc = "极路客精彩直播";
					}
					mSettingData.desc = desc;
					intent.putExtra(LiveActivity.KEY_IS_LIVE, true);
					intent.putExtra(LiveActivity.KEY_GROUPID, "");
					intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
					intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
					intent.putExtra(LiveActivity.KEY_LIVE_SETTING_DATA,
							mSettingData);
					startActivity(intent);
				}

			} else {
				return;
			}

			break;
		case R.id.image1:
			new1.setVisibility(View.GONE);
			open_shareVideo(images[0].getName());
			break;
		case R.id.image2:
			new2.setVisibility(View.GONE);

			if (images[1].getName().equals("")) {
				return;
			} else {
				open_shareVideo(images[1].getName());
			}
			break;
		case R.id.image3:

			// if (mApp.isUserLoginSucess == false) {
			// Intent it = new Intent(this, UserLoginActivity.class);
			// it.putExtra("isInfo", "back");
			// startActivity(it);
			// } else {
			Intent photoalbum = new Intent(CarRecorderActivity.this,
					PhotoAlbumActivity.class);
			photoalbum.putExtra("from", "cloud");
			startActivity(photoalbum);
			// }

			break;
		case R.id.live_video:
			lsp.show();
			break;
		case R.id.live_gps_icon:
			lsp.show();
			break;
		case R.id.live_talk_icon:
			lsp.show();
			break;
		case R.id.live_release_icon:
			lsp.show();
			break;
		case R.id.live_time:
			lsp.show();
			break;
		case R.id.mRtmpPlayerView: {// 停止预览
			if (!GolukApplication.getInstance().getIpcIsLogin())
				return;
			setFullScreen(false);
			rtmpIsOk = false;
			mRtmpPlayerView.removeCallbacks(retryRunnable);
			if (mRtmpPlayerView.isPlaying()) {
				GolukDebugUtils.e("xuhw", "YYYYYY======stopPlayback");
				mRtmpPlayerView.stopPlayback();
			}
			hidePlayer();
			isShowPlayer = false;
			isConnecting = false;
			mPalyerLayout.setVisibility(View.VISIBLE);
			mNotconnected.setVisibility(View.GONE);
			mConncetLayout.setVisibility(View.GONE);
			mFullScreen.setVisibility(View.GONE);
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 摄像头未连接提示
	 * 
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	private void dialog() {
		CustomDialog d = new CustomDialog(this);
		d.setMessage("请检查摄像头是否正常连接", Gravity.CENTER);
		d.setLeftButton("确定", null);
		d.show();
	}

	/**
	 * 显示加载中布局
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void showLoading() {
		mLoadingText.setText("视频加载中，请稍后...");
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

	/**
	 * 隐藏加载中显示画面
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void hideLoading() {
		if (mAnimationDrawable != null) {
			if (mAnimationDrawable.isRunning()) {
				mAnimationDrawable.stop();
			}
		}
		mLoadingLayout.setVisibility(View.GONE);
	}

	boolean isstart = false;

	@Override
	protected void onStart() {
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

	@Override
	protected void onResume() {
		super.onResume();
		if (isShowPlayer) {
			GolukDebugUtils.e("xuhw", "YYYYYY======isConnecting=="
					+ isConnecting);
			if (!isConnecting) {
				isConnecting = true;
				start();
			}

			showLoading();
			hidePlayer();
		}

		GolukApplication.getInstance().setContext(this, "carrecorder");
		if (isBackGroundStart) {
			this.moveTaskToBack(true);
			isBackGroundStart = false;
		}

		if (!downloadFinish) {
			if (downloadFileNumber <= 0) {
				downloadFileNumber = 0;
				downloadFinish = false;
				// mShareBtn.setVisibility(View.GONE);
			} else {
				GolukDebugUtils.e("xuhw",
						"KKKK=================================");
				downloadFinish = false;
				mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
				mHandler.sendEmptyMessage(DOWNLOADWONDERFULVIDEO);
			}
		}

		mVideoConfigState = GolukApplication.getInstance()
				.getVideoConfigState();
		if (null != mVideoConfigState) {
			if ("1080P".equals(mVideoConfigState.resolution)) {
				mVideoResolutions.setBackgroundResource(R.drawable.icon_hd1080);
			} else {
				mVideoResolutions.setBackgroundResource(R.drawable.icon_hd720);
			}
		}
		// 添加定位通知及反编码通知
		mApp.addLocationListener(SelfContextTag, this);
		GetBaiduAddress.getInstance().setCallBackListener(mBaiduGeoCoderFn);
		initVideoImage();// 初始化相册列表
	};

	@Override
	protected void onPause() {
		super.onPause();
		GolukDebugUtils.e("xuhw", "YYYYYY======onPause======");
		if (isShowPlayer) {
			if (null != mRtmpPlayerView) {
				rtmpIsOk = false;
				mFullScreen.setVisibility(View.GONE);
				mRtmpPlayerView.removeCallbacks(retryRunnable);
				if (mRtmpPlayerView.isPlaying()) {
					isConnecting = false;
					mRtmpPlayerView.stopPlayback();
					GolukDebugUtils.e("xuhw", "YYYYYY======stopPlayback======");
				}
				hidePlayer();
			}
		}
		// 移除定位通知及反编码通知
		mApp.removeLocationListener(SelfContextTag);
		GetBaiduAddress.getInstance().setCallBackListener(null);
	};

	@Override
	protected void onStop() {
		super.onStop();
		GolukDebugUtils.e("xuhw", "YYYYYY======onStop======");
		// if (isShowPlayer) {
		// if (null != mRtmpPlayerView) {
		// rtmpIsOk = false;
		// mFullScreen.setVisibility(View.GONE);
		// mRtmpPlayerView.removeCallbacks(retryRunnable);
		// if (mRtmpPlayerView.isPlaying()) {
		// isConnecting = false;
		// mRtmpPlayerView.stopPlayback();
		// GolukDebugUtils.e("xuhw", "YYYYYY======stopPlayback======");
		// }
		// hidePlayer();
		// }
		// }
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukDebugUtils.e("xuhw", "YYYYYY======onDestroy======");
		if (null != mRtmpPlayerView) {
			mRtmpPlayerView.removeCallbacks(retryRunnable);
			mRtmpPlayerView.cleanUp();
		}

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager()
					.removeIPCManagerListener("main");
		}

	};

	/**
	 * 8s视频一键抢拍
	 * 
	 * @author xuhw
	 * @date 2015年3月4日
	 */
	private void startTrimVideo() {
		if (null == m8sTimer) {
			mShootTime = 0;
			m8sTimer = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					mShootTime++;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							switch (mShootTime) {
							case 1:

								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon6);
								break;
							case 2:

								break;
							case 3:

								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon5);
								break;
							case 4:

								break;
							case 5:

								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon4);
								break;
							case 6:

								break;
							case 7:

								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon3);
								break;
							case 8:

								break;
							case 9:

								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon2);
								break;
							case 10:

								break;
							case 11:

								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon1);
								break;
							case 13:

								break;

							default:
								break;
							}

							if (mShootTime > 13) {
								stopTrimVideo();
							}
						}
					});

				}
			};
			m8sTimer.schedule(task, 500, 500);

		} else {

		}
	}

	/**
	 * 停止８s视频操作
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void stopTrimVideo() {
		mHandler.sendEmptyMessageDelayed(CarRecorderActivity.QUERYFILEEXIT,
				CarRecorderActivity.QUERYFILETIME);
		mShootTime = 0;
		m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
		if (null != m8sTimer) {
			m8sTimer.cancel();
			m8sTimer.purge();
			m8sTimer = null;
		}

		if (images[0] != null) {
			Boolean flog = SettingUtils.getInstance().getBoolean(
					"Local_" + images[0].getName(), true);
			if (flog) {
				new2.setVisibility(View.VISIBLE);
			} else {
				new2.setVisibility(View.GONE);
			}
			image1.setImageBitmap(images[2].getBitmap());
			image2.setImageBitmap(images[0].getBitmap());

		}
		downloadSize.setProcess(0);
		downloadSize.setVisibility(View.VISIBLE);
	}

	/**
	 * 单个文件查询
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void queryFileExit() {
		videoFileQueryTime++;
		mHandler.removeMessages(QUERYFILEEXIT);
		if (!TextUtils.isEmpty(mRecordVideFileName)) {
			if (videoFileQueryTime <= 15) {
				if (GolukApplication.getInstance().getIpcIsLogin()) {
					boolean isSucess = GolukApplication.getInstance()
							.getIPCControlManager()
							.querySingleFile(mRecordVideFileName);
					GFileUtils
							.writeIPCLog("===============queryFileExit==================videoFileQueryTime="
									+ videoFileQueryTime);
					if (!isSucess) {
						GFileUtils
								.writeIPCLog("===============queryFileExit=============isSucess  fail======================");
						mHandler.sendEmptyMessageDelayed(QUERYFILEEXIT, 1000);
					}
				} else {
					// IPC未登录
				}
			} else {
				videoFileQueryTime = 0;
				videoTriggerFail();

				GFileUtils
						.writeIPCLog("============queryFileExit=====111111111111111文件查询超时========================");

			}
		} else {
			videoFileQueryTime = 0;
			resetTrimVideoState();
		}

	}

	/**
	 * 恢复视频截取状态
	 * 
	 * @author xuhw
	 * @date 2015年3月5日
	 */
	private void resetTrimVideoState() {
		isRecording = false;
		mCurVideoType = VideoType.idle;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
			}
		});
	}

	/**
	 * 视频截取命令失败回复状态
	 * 
	 * @author xuhw
	 * @date 2015年3月18日
	 */
	private void videoTriggerFail() {
		if (mCurVideoType == VideoType.emergency) {
		} else if (mCurVideoType == VideoType.mounts) {
		}
		resetTrimVideoState();
	}

	/**
	 * 更新视频截取状态
	 * 
	 * @author xuhw
	 * @date 2015年3月10日
	 */
	private void updateVideoState() {
		if (rtmpIsOk == true) {
			mConnectTip.setText("预览影像正常");
		} else {
			mConnectTip.setText("预览影像加载中…");
		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			if (ConnectionStateMsg_Connected != msg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						downloadFileNumber = 0;
						mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
						if (m_bIsFullScreen) {
							setFullScreen(false);
						}
					}
				});
			}
		}

		if (ENetTransEvent_IPC_VDCP_CommandResp == event
				&& IPC_VDCP_Msg_Init == msg && 0 == param1) {
			if (!ipcFirstLogin) {
				ipcFirstLogin = true;
				if (isShowPlayer) {
					if (!isConnecting) {
						isConnecting = true;
						start();
					}
				}

			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (isBindSucess()) {
						m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
					}
				}
			});
		}

		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			callBack_VDCP(msg, param1, param2);
		} else if (event == ENetTransEvent_IPC_VDTP_Resp) {
			callBack_VDTP(msg, param1, param2);
		}

	}

	/**
	 * 处理VDCP命令回调
	 * 
	 * @param msg
	 *            　命令id
	 * @param param1
	 *            0:命令发送成功 非0:发送失败
	 * @param param2
	 *            命令对应的json字符串
	 * @author xuhw
	 * @date 2015年3月17日
	 */
	@SuppressLint("SimpleDateFormat")
	private void callBack_VDCP(int msg, int param1, Object param2) {
		switch (msg) {
		// 实时抓图
		case IPC_VDCPCmd_SnapPic:
			GFileUtils
					.writeIPCLog("============行车记录仪=======接收截图命令成功========222222=====param1="
							+ param1 + "=====param2=" + param2);
			if (RESULE_SUCESS == param1) {
				// 文件路径格式：fs1:/IPC_Snap_Pic/snapPic.jpg
				String imageFilePath = (String) param2;
				if (!TextUtils.isEmpty(imageFilePath)) {
					String path = FileUtils.libToJavaPath(imageFilePath);
					if (!TextUtils.isEmpty(path)) {
						long time = System.currentTimeMillis();
						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd_HH-mm-ss");
						String timename = format.format(new Date(time));

						String dirname = Environment
								.getExternalStorageDirectory()
								+ File.separator
								+ "goluk"
								+ File.separator
								+ "goluk"
								+ File.separator + "screenshot";
						GFileUtils.makedir(dirname);

						String picName = dirname + File.separator + timename
								+ ".jpg";
						// 保存原始图片
						String orgPicName = dirname + File.separator
								+ "original_" + timename + ".jpg";

						GFileUtils.copyFile(path, orgPicName);
						GFileUtils.compressImageToDisk(path, picName);

						File file = new File(picName);
						if (file.exists()) {
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SnapPic======333333333333333====uploadPicture=======path="
											+ path);
						} else {
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SnapPic======图片压缩失败====333333======11111========");
						}

					} else {
						GFileUtils
								.writeIPCLog("===========IPC_VDCPCmd_SnapPic=============image path  null=====333333====44444=====");
					}
				} else {
					GFileUtils
							.writeIPCLog("===========IPC_VDCPCmd_SnapPic=============fs1  null===333333======333333=====");
				}
			} else {
				GFileUtils
						.writeIPCLog("===========IPC_VDCPCmd_SnapPic=============fail=======333333===2222====");
			}
			break;
		// 请求紧急、精彩视频录制
		case IPC_VDCPCmd_TriggerRecord:
			GolukDebugUtils.e("xuhw",
					"m8sBtn===IPC_VDCPCmd_TriggerRecord===4444=====param1="
							+ param1 + "==param2=" + param2);
			GFileUtils
					.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord====1111111========param1="
							+ param1 + "=====param2=" + param2);
			TriggerRecord record = IpcDataParser
					.parseTriggerRecordResult((String) param2);
			if (null != record) {
				if (RESULE_SUCESS == param1) {
					mRecordVideFileName = record.fileName;
					videoname = mRecordVideFileName;
					GolukDebugUtils.e("xuhw",
							"m8sBtn===IPC_VDCPCmd_TriggerRecord===555555========type="
									+ record.type);
					GFileUtils
							.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord====222222========mRecordVideFileName="
									+ mRecordVideFileName);
					// 精彩视频
					if (TYPE_SHORTCUT == record.type) {
						GFileUtils
								.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord==333333==========MOUNTS========");
						mHandler.sendEmptyMessage(MOUNTS);
					} else {
						GFileUtils
								.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord===444444=========EMERGENCY========");
						mHandler.sendEmptyMessage(EMERGENCY);
					}
				} else {
					GFileUtils
							.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord===66666======= not success ==========");
					videoTriggerFail();
				}
			} else {
				GolukDebugUtils
						.e("xuhw",
								"m8sBtn===IPC_VDCPCmd_TriggerRecord===6666====not success====");
				GFileUtils
						.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord===77777======= not success ==========");
				videoTriggerFail();
			}

			break;
		// 单文件查询
		case IPC_VDCPCmd_SingleQuery:
			if (RESULE_SUCESS == param1) {
				VideoFileInfo fileInfo = IpcDataParser
						.parseSingleFileResult((String) param2);
				if (null != fileInfo) {
					if (!TextUtils.isEmpty(fileInfo.location)) {
						Intent mIntent = new Intent("sendfile");
						if (TYPE_SHORTCUT == fileInfo.type) {// 精彩
							mIntent.putExtra("filetype", "mounts");
							mIntent.putExtra("filename", fileInfo.location);

							downloadFileNumber++;
							String path = Environment
									.getExternalStorageDirectory()
									+ File.separator
									+ "goluk"
									+ File.separator
									+ "video" + File.separator + "wonderful";
							wonderfulVideoName = path + File.separator
									+ mRecordVideFileName;

							System.out
									.println("YYY========Finish=======1111======="
											+ downloadFileNumber);
							if (downloadFileNumber <= 1)
								mHandler.sendEmptyMessage(DOWNLOADWONDERFULVIDEO);
						} else if (TYPE_URGENT == fileInfo.type) {// 紧急
							mIntent.putExtra("filetype", "emergency");
							mIntent.putExtra("filename", fileInfo.location);
							// sendBroadcast(mIntent);
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SingleQuery===3333=======紧急视频查询成功==============");
						} else {
							// 循环影像类型，类型错误
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SingleQuery===44444=======类型错误========循环影像========");
						}

						mRecordVideFileName = "";
						videoFileQueryTime = 0;
						resetTrimVideoState();
					} else {
						mHandler.sendEmptyMessageDelayed(QUERYFILEEXIT, 1000);
					}
				} else {
					mHandler.sendEmptyMessageDelayed(QUERYFILEEXIT, 1000);
				}
			} else {
				mHandler.sendEmptyMessageDelayed(QUERYFILEEXIT, 1000);
			}
			break;
		// 获取IPC系统音视频编码配置
		case IPC_VDCP_Msg_GetVedioEncodeCfg:
			if (param1 == RESULE_SUCESS) {
				final VideoConfigState videocfg = IpcDataParser
						.parseVideoConfigState((String) param2);
				if (null != videocfg) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if ("1080P".equals(videocfg.resolution)) {
								mVideoResolutions
										.setBackgroundResource(R.drawable.icon_hd1080);
							} else {
								mVideoResolutions
										.setBackgroundResource(R.drawable.icon_hd720);
							}
						}
					});
				} else {
					// 获取失败
				}
			}
			break;

		default:
			break;
		}

	}

	/**
	 * 处理VDTP命令回调
	 * 
	 * @param msg
	 *            　命令id
	 * @param param1
	 *            0:命令发送成功 非0:发送失败
	 * @param param2
	 *            命令对应的json字符串
	 * @author xuhw
	 * @date 2015年3月17日
	 */
	private void callBack_VDTP(int msg, int param1, Object param2) {
		switch (msg) {
		// 文件传输消息
		case IPC_VDTP_Msg_File:
			if (RESULE_SUCESS == param1) {
				try {
					JSONObject json = new JSONObject((String) param2);
					String filename = json.optString("filename");
					if (null != json) {

						String imagename = videoname.replace("mp4", "jpg");

						if (filename.equals(imagename)) {
							VideoShareInfo vsi = new VideoShareInfo();
							vsi.setName(videoname);
							vsi.setBitmap(ImageManager.getBitmapFromCache(
									mImagePath + filename, 114, 64));

							if (images[0] == null) {
								if ("".equals(images[1].getName())) {
									images[1] = vsi;
									image2.setImageBitmap(vsi.getBitmap());
									new2.setVisibility(View.VISIBLE);
								} else {
									images[0] = vsi;
									image1.setVisibility(View.VISIBLE);
									image1.setImageBitmap(vsi.getBitmap());
									new1.setVisibility(View.VISIBLE);
								}

							} else {
								images[0] = vsi;
								image1.setImageBitmap(vsi.getBitmap());
								new1.setVisibility(View.VISIBLE);
							}

						} else if (filename.equals(videoname)) {
							downloadSize.setVisibility(View.GONE);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (1 == param1) {

				try {
					JSONObject json = new JSONObject((String) param2);
					if (null != json) {
						String filename = json.optString("filename");

						if (videoname.equals(filename)) {// 是当前拍的文件
							int filesize = json.getInt("filesize");
							int filerecvsize = json.getInt("filerecvsize");
							int process = (filerecvsize * 100) / filesize;
							downloadSize.setProcess(process);
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 下载中
			} else {
				// 下载失败

			}
			break;

		default:
			break;
		}
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (downloadFileNumber <= 0) {
				downloadFileNumber = 0;
				downloadFinish = false;
				// mShareBtn.setVisibility(View.GONE);
			} else {
				GolukDebugUtils.e("xuhw",
						"KKKK=================================");
				downloadFinish = false;
				mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
				mHandler.sendEmptyMessage(DOWNLOADWONDERFULVIDEO);
			}
		}
	};

	/**
	 * 设置播放器全屏
	 * 
	 * @param bFull
	 *            true:全屏　false:普通
	 * @author xuhw
	 * @date 2015年5月12日
	 */
	public void setFullScreen(boolean bFull) {
		if (bFull == m_bIsFullScreen) {
			// GolukUtils.showToast(this, "已处于全屏状态.");
			return;
		}
		if (bFull) {
			if (!mRtmpPlayerView.isPlaying()) {
				return;
			}
			m_vgNormalParent = (ViewGroup) mRtmpPlayerView.getParent();
			if (null == m_vgNormalParent) {
				return;
			}
			ViewGroup vgRoot = (ViewGroup) mRtmpPlayerView.getRootView(); // 获取根布局
			m_vgNormalParent.removeView(mRtmpPlayerView);
			mPlayerLayout.addView(mRtmpPlayerView);
			RelativeLayout.LayoutParams norParams = new RelativeLayout.LayoutParams(
					(int) (38.66 * density), (int) (30 * density));
			norParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			norParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			norParams.setMargins(0, 0, (int) (10 * density),
					(int) (10 * density));
			mPlayerLayout.addView(mNormalScreen, norParams);
			vgRoot.addView(mPlayerLayout);

			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else if (m_vgNormalParent != null) {
			ViewGroup vgRoot = (ViewGroup) mRtmpPlayerView.getRootView();
			vgRoot.removeView(mPlayerLayout);
			mPlayerLayout.removeView(mRtmpPlayerView);
			mPlayerLayout.removeView(mNormalScreen);
			m_vgNormalParent.addView(mRtmpPlayerView);

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		m_bIsFullScreen = bFull;
	}

	@Override
	public void onBackPressed() {
		GolukDebugUtils.e("xuhw",
				"YYYYYY======onBackPressed=====m_bIsFullScreen="
						+ m_bIsFullScreen);
		if (m_bIsFullScreen) {
			// 全屏时，退出全屏
			setFullScreen(false);
		} else {
			super.onBackPressed();
		}
	}

	public void exit() {

		finish();
	}

	public void slideview(final float p, final View view) {

		ViewPropertyAnimator animator = view.animate();
		animator.setDuration(200);
		animator.translationXBy(p);
		animator.start();
	}

	// POPWindow回调操作
	@Override
	public void callBackPopWindow(int event, Object data) {
		if (LiveSettingPopWindow.EVENT_ENTER == event) {
			if (null != lsp) {
				lsp.close();
			}
			if (null == data) {
				GolukUtils.showToast(this, "用户设置出错");
				return;
			}
			mSettingData = (LiveSettingBean) data;
			if (mSettingData.isCanVoice) {
				liveVideo.setBackgroundResource(R.drawable.driving_voice_icon);
			} else {
				liveVideo
						.setBackgroundResource(R.drawable.driving_voice_off_icon);
			}
			liveTime.setText(GolukUtils.secondToString(mSettingData.duration));
		}
	}

	/**
	 * 初始化文件同步和分享功能
	 * 
	 * @Title: initVideoImage
	 * @Description: TODO void
	 * @author 曾浩
	 * @throws
	 */
	public void initVideoImage() {

		images = new VideoShareInfo[3];

		String[] filePaths = { "wonderful/wonderful.txt", "urgent/urgent.txt" };
		Bitmap bitmap = ImageManager
				.getBitmapFromResource(R.drawable.tacitly_pic);

		List<String> wonderfuls = this.getNewVideoByType(filePaths[0], 1);// 最新的精彩视频
		List<String> urgents = this.getNewVideoByType(filePaths[1], 2);// 最新的紧急视频

		List<String> names = new ArrayList<String>();

		VideoShareInfo defpic = new VideoShareInfo();
		defpic.setBitmap(bitmap);
		defpic.setName("");

		if (wonderfuls != null) {
			names.addAll(wonderfuls);
		}
		if (urgents != null) {
			names.addAll(urgents);
		}

		List<String> newvideo = this.shortNames(names);// 拿到最新的4个视频

		String videoname1 = "";
		String videoname2 = "";

		if (newvideo != null && newvideo.size() > 0) {
			videoname1 = newvideo.get(0);
			if (newvideo.size() > 1) {
				videoname2 = newvideo.get(1);
			}
		}

		if (!"".equals(videoname1)) {

			Boolean flog = SettingUtils.getInstance().getBoolean(
					"Local_" + videoname1, true);

			String name1 = mImagePath + videoname1.replace("mp4", "jpg");
			File video1 = new File(name1);
			VideoShareInfo vsi1 = new VideoShareInfo();
			if (video1.exists()) {
				vsi1.setBitmap(ImageManager.getBitmapFromCache(name1, 114, 64));
			} else {
				vsi1.setBitmap(bitmap);
			}

			vsi1.setName(videoname1);

			if (!"".equals(videoname2)) {

				images[0] = vsi1;
				image1.setImageBitmap(vsi1.getBitmap());
				image1.setVisibility(View.VISIBLE);
				if (flog) {
					new1.setVisibility(View.VISIBLE);
				} else {
					new1.setVisibility(View.GONE);
				}

			} else {
				images[1] = vsi1;
				image1.setVisibility(View.INVISIBLE);
				new1.setVisibility(View.GONE);

				image2.setImageBitmap(vsi1.getBitmap());
				if (flog) {
					new2.setVisibility(View.VISIBLE);
				} else {
					new2.setVisibility(View.GONE);
				}
			}

		} else {
			image1.setVisibility(View.INVISIBLE);
			new1.setVisibility(View.GONE);
		}

		if (!"".equals(videoname2)) {
			String name2 = mImagePath + videoname2.replace("mp4", "jpg");
			File video2 = new File(name2);
			VideoShareInfo vsi2 = new VideoShareInfo();
			vsi2.setName(videoname2);
			if (video2.exists()) {
				vsi2.setBitmap(ImageManager.getBitmapFromCache(name2, 114, 64));
			} else {
				vsi2.setBitmap(bitmap);
			}

			images[1] = vsi2;
			image2.setImageBitmap(vsi2.getBitmap());

			boolean flog2 = SettingUtils.getInstance().getBoolean(
					"Local_" + videoname2, true);
			if (flog2) {
				new2.setVisibility(View.VISIBLE);
			} else {
				new2.setVisibility(View.GONE);
			}
		} else {
			if ("".equals(videoname1)) {
				images[1] = defpic;
				image2.setImageBitmap(defpic.getBitmap());
				new2.setVisibility(View.GONE);
			}
		}

		images[2] = defpic;
	}

	/**
	 * 冒泡排序把日期最新的排前面
	 * 
	 * @Title: shortNames
	 * @Description: TODO
	 * @param names
	 * @return List<String>
	 * @author 曾浩
	 * @throws
	 */
	public List<String> shortNames(List<String> names) {
		if (names != null && names.size() > 0) {
			for (int i = 0; i < names.size(); i++) {
				String[] videos = names.get(i).split("_");
				Long time = Long.parseLong(videos[1]);

				for (int j = i + 1; j < names.size(); j++) {
					Long date = Long.parseLong(names.get(j).split("_")[1]);
					if (time < date) {
						String name = names.get(i);
						names.set(i, names.get(j));
						names.set(j, name);
					}
				}
			}
		}

		return names;
	}

	public List<String> getNewVideoByType(String uri, int type) {
		String file = mFilePath + uri;
		List<String> list = this.getVideoConfigFile(file);

		String videoname1 = "";
		String videoname2 = "";

		String path = "";
		if (type == 1) {
			path = Environment.getExternalStorageDirectory().getPath()
					+ "/goluk/video/wonderful/";
		} else if (type == 2) {
			path = Environment.getExternalStorageDirectory().getPath()
					+ "/goluk/video/urgent/";
		}

		int flog = 0;
		String videoname = "";

		if (list != null && list.size() > 0) {
			File vfile = null;
			for (int i = list.size() - 1; i >= 0; i--) {
				videoname = list.get(i);
				vfile = new File(path + videoname);

				if (vfile.exists()) {
					flog++;
					if (flog <= 1) {
						videoname1 = videoname;
					} else if (flog == 2) {
						videoname2 = videoname;
					} else {
						break;
					}

				}
			}

			List<String> result = new ArrayList<String>();

			if (!"".equals(videoname1)) {
				result.add(videoname1);
			}

			if (!"".equals(videoname2)) {
				result.add(videoname2);
			}

			return result;
		} else {
			return null;
		}

	}

	/**
	 * 读取本地视频配置文件
	 * 
	 * @return
	 */
	private List<String> getVideoConfigFile(String path) {
		// String[] data = null;
		List<String> data = new ArrayList<String>();

		File file = new File(path);
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(file), "UTF-8"));
				String str = br.readLine();
				if (TextUtils.isEmpty(str)) {
					return data;
				}

				String[] files = str.split(",");

				// 去重
				for (String f : files) {
					if (!data.contains(f)) {
						data.add(f);
					}
				}

				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return data;
		} else {
			return null;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch (v.getId()) {

		case R.id.car_recorder:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				Drawable more_down = this.getResources().getDrawable(
						R.drawable.driving_car_next_btn1);
				more.setCompoundDrawablesWithIntrinsicBounds(null, null,
						more_down, null);
				more.setTextColor(Color.rgb(59, 151, 245));
				break;
			case MotionEvent.ACTION_UP:
				Drawable more_up = this.getResources().getDrawable(
						R.drawable.driving_car_next_btn);
				more.setCompoundDrawablesWithIntrinsicBounds(null, null,
						more_up, null);
				more.setTextColor(Color.rgb(204, 204, 204));
				break;
			}
			break;
		}
		return false;
	}

	@Override
	public void LocationCallBack(String gpsJson) {
		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
		if (location == null) {
			return;
		}
		// 保存经纬度
		LngLat.lng = location.rawLon;
		LngLat.lat = location.rawLat;

		if (mApp.getContext() instanceof CarRecorderActivity) {
			GetBaiduAddress.getInstance().searchAddress(location.rawLat,
					location.rawLon);
		}

	}

}
