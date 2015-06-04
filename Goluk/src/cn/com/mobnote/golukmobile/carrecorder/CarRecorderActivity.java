package cn.com.mobnote.golukmobile.carrecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserLoginActivity;
import cn.com.mobnote.golukmobile.VideoEditActivity;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser.TriggerRecord;
import cn.com.mobnote.golukmobile.carrecorder.entity.DeviceState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.settings.SettingsActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomWifiDialog;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;
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
public class CarRecorderActivity extends BaseActivity implements OnClickListener,
		IPCManagerFn {
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
	private Button mShareBtn = null;
	/** 精彩视频下载完成 */
	private boolean downloadFinish = false;
	/** 控制显示精彩视频下载中提示 */
	private int downloadNumber = 0;
	/** 精彩视频下载文件个数 */
	private int downloadFileNumber = 0;
	/** 音视频信息 */
	private VideoConfigState mVideoConfigState=null;
	/** 视频分辨率显示 */
	private ImageView mVideoResolutions=null;
	
	private GolukApplication mApp = null;
	private boolean m_bIsFullScreen=false;
	private ViewGroup m_vgNormalParent;
	private ImageButton mFullScreen=null;
	private RelativeLayout mPlayerLayout=null;
	private Button mNormalScreen=null;
	private final int BTN_NORMALSCREEN=231;
	
	private RelativeLayout mPalyerLayout = null;
	private boolean isShowPlayer = false;
	
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_main);
		mApp = (GolukApplication) getApplication();
		
		mPlayerLayout = new RelativeLayout(this);
		mNormalScreen = new Button(this);
		mNormalScreen.setId(BTN_NORMALSCREEN);
		mNormalScreen.setBackgroundResource(R.drawable.btn_player_normal);
		mNormalScreen.setOnClickListener(this);
		
		
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
					if(!TextUtils.isEmpty(addr)){
						mAddr.setText(addr);
					}
					break;
				case STARTVIDEORECORD:
					updateVideoRecordTime();
					break;
				case DOWNLOADWONDERFULVIDEO:
					wonderfulVideoDownloadShow();
					break;

				}
			};
		};

		initView();
		setListener();

		// 获取是否是后台启动
		Intent receiveIntent = getIntent();
		isBackGroundStart = receiveIntent.getBooleanExtra("isBackGroundStart",
				false);

		// 注册回调监听
		if(null != GolukApplication.getInstance().getIPCControlManager()){
			GolukApplication.getInstance().getIPCControlManager()
			.addIPCManagerListener("main", this);
		}
		
	}

	/**
	 * 精彩视频下载显示
	 * 
	 * @author xuhw
	 * @date 2015年4月8日
	 */
	private void wonderfulVideoDownloadShow() {
		if(!TextUtils.isEmpty(wonderfulVideoName)){
			downloadNumber++;
			mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
			mShareBtn.setVisibility(View.VISIBLE);

			if (!downloadFinish) {
				if (1 == downloadNumber) {
					mShareBtn.setBackgroundResource(R.drawable.screen_loading_1);
				} else if (2 == downloadNumber) {
					mShareBtn.setBackgroundResource(R.drawable.screen_loading_2);
				} else {
					downloadNumber = 0;
					mShareBtn.setBackgroundResource(R.drawable.screen_loading_3);
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
		mPalyerLayout = (RelativeLayout)findViewById(R.id.mPalyerLayout);
		mFullScreen = (ImageButton)findViewById(R.id.mFullScreen);
		mFullScreen.setVisibility(View.GONE);
		mVideoResolutions = (ImageView)findViewById(R.id.mVideoResolutions);
		mShareBtn = (Button) findViewById(R.id.mShareBtn);
		mShareBtn.setVisibility(View.GONE);
		mRtmpPlayerLayout = (RelativeLayout) findViewById(R.id.mRtmpPlayerLayout);
		mVLayout = (RelativeLayout) findViewById(R.id.vLayout);
		m8sBtn = (ImageButton) findViewById(R.id.m8sBtn);
		mFileBtn = (ImageView) findViewById(R.id.mFileBtn);
		mSettingBtn = (ImageView) findViewById(R.id.mSettingBtn);
		mTime = (TextView) findViewById(R.id.mTime);
		mAddr = (TextView) findViewById(R.id.mAddr);
		mConnectTip = (TextView) findViewById(R.id.mConnectTip);
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();
		mLoadingText = (TextView) findViewById(R.id.mLoadingText);
		mRtmpPlayerView = (RtmpPlayerView) findViewById(R.id.mRtmpPlayerView);
		mRtmpPlayerView.setAudioMute(true);
		mRtmpPlayerView.setZOrderMediaOverlay(true);
		// mRtmpPlayerView.requestFocus();
		mRtmpPlayerView.setBufferTime(1000);
		mRtmpPlayerView.setConnectionTimeout(30000);
		mRtmpPlayerView.setVisibility(View.VISIBLE);

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRtmpPlayerLayout
				.getLayoutParams();
		lp.width = screenWidth;
		lp.height = (int) (screenWidth / 1.7833);
		lp.leftMargin = 0;
		mRtmpPlayerLayout.setLayoutParams(lp);

		m8sBtn.setBackgroundResource(R.drawable.screen_btn_6s_grey);
		mConnectTip.setText("摄像头未连接");
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			m8sBtn.setBackgroundResource(R.drawable.btn_ipc_8s);
		}

		String addr = GolukApplication.getInstance().mCurAddr;
		if(!TextUtils.isEmpty(addr)){
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
		mShareBtn.setOnClickListener(this);
		m8sBtn.setOnClickListener(this);
		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.mFileLayout).setOnClickListener(this);
		findViewById(R.id.mSettingLayout).setOnClickListener(this);
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
						updateVideoState();
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
						updateVideoState();
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
						updateVideoState();
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
		updateVideoState();
		if (null != mRtmpPlayerView) {
			mRtmpPlayerView.setVisibility(View.VISIBLE);
			String preUrl = getResources().getString(R.string.default_rtsp_pre);
			String backUrl = getResources().getString(R.string.default_rtsp_back);
			String url = preUrl + mApp.mIpcIp + backUrl;
			
			GolukDebugUtils.e("", "CarRecorset Datasource URL:" + url);
			
			mRtmpPlayerView.setDataSource(url);

			mRtmpPlayerView.start();
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.back_btn:
			if(m_bIsFullScreen){
				return;
			}
			
			exit();
			break;
		case R.id.mShareBtn:
			if(m_bIsFullScreen){
				return;
			}
			
			if (downloadFinish) {
				
				click_ConnFailed();
				
				mShareBtn.postDelayed(new Runnable() {
					@Override
					public void run() {
						downloadFinish = false;
						mShareBtn.setVisibility(View.GONE);
					}
				}, 1000);
				
			}
			break;
		case R.id.m8sBtn:
			if(m_bIsFullScreen){
				return;
			}
			
			GolukDebugUtils.e("xuhw", "m8sBtn========================11111======");
			GFileUtils
					.writeIPCLog("=============================发起精彩视频命令===========m8sBtn=============");
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				if (!isRecording) {
					m8sBtn.setBackgroundResource(R.drawable.screen_btn_6s_grey);
					isRecording = true;
					mCurVideoType = VideoType.mounts;
					GolukDebugUtils.e("xuhw", "m8sBtn========================2222======");
					GFileUtils
							.writeIPCLog("=============================发起精彩视频命令================queryParam=");
					boolean isSucess = GolukApplication.getInstance()
							.getIPCControlManager().startWonderfulVideo();

					GolukDebugUtils.e("xuhw", "m8sBtn========================333===isSucess==="+isSucess);
					if (!isSucess) {
						videoTriggerFail();
						GFileUtils
								.writeIPCLog("=============================发起精彩视频命令============fail===============");
					}
				}
			} else {
				dialog();
				// 未登录
				GFileUtils
						.writeIPCLog("=============================发起精彩视频命令===========未登录=============");
			}
			break;
		case R.id.mFileBtn:
		case R.id.mFileText:
		case R.id.mFileLayout:
			if(m_bIsFullScreen){
				return;
			}
			
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				Intent intent = new Intent(CarRecorderActivity.this,
						IPCFileManagerActivity.class);
				startActivity(intent);
			} else {
				// 未登录
				dialog();
			}
			break;
		case R.id.mSettingBtn:
		case R.id.mSettingText:
		case R.id.mSettingLayout:
			if(m_bIsFullScreen){
				return;
			}
			
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				Intent setting = new Intent(CarRecorderActivity.this,
						SettingsActivity.class);
				startActivity(setting);
			} else {
				dialog();
			}
			break;
		case R.id.mFullScreen:
			setFullScreen(true);
			break;
		case BTN_NORMALSCREEN:
			setFullScreen(false);
			break;
		case R.id.mPlayBtn:
			if(!isShowPlayer){
				if (!isConnecting) {
					isConnecting = true;
					start();
				}
				
				showLoading();
				hidePlayer();
				mPalyerLayout.setVisibility(View.GONE);
			}
			break;

		default:
			break;
		}
	}
	
	/**
	 * 验证当前是否是登陆状态，如果没登陆跳转到登陆页面
	  * @Title: click_ConnFailed 
	  * @Description: TODO void 
	  * @author 曾浩 
	  * @throws
	 */
	private void click_ConnFailed() {
			// 跳转到wifi连接首页
			if (mApp.isUserLoginSucess) {
				String path = Environment.getExternalStorageDirectory().getPath() + "/goluk/video/wonderful/"+wonderfulVideoName;
				GolukDebugUtils.e("xuhw", "YYY====mShareBtn===path="+path);
				
				Intent i = new Intent(CarRecorderActivity.this,
						VideoEditActivity.class);
				i.putExtra("cn.com.mobnote.video.path", path);
				startActivity(i);
			} else {
				Intent intent = new Intent(this, UserLoginActivity.class);
				intent.putExtra("isInfo", "back");
				startActivity(intent);
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
		mLoadingText.setText("无码的视频也是高潮迭起");
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
		
		if(isShowPlayer){
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
		
		if(!downloadFinish){
			if (downloadFileNumber <= 0) {
				downloadFileNumber = 0;
				downloadFinish = false;
				mShareBtn
						.setVisibility(View.GONE);
			} else {
				GolukDebugUtils.e("xuhw", "KKKK=================================");
				downloadFinish = false;
				mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
				mHandler.sendEmptyMessage(DOWNLOADWONDERFULVIDEO);
			}
		}
		
		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
		if(null != mVideoConfigState){
			if("1080P".equals(mVideoConfigState.resolution)){
				mVideoResolutions.setBackgroundResource(R.drawable.icon_hd1080);
			}else{
				mVideoResolutions.setBackgroundResource(R.drawable.icon_hd720);
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		GolukDebugUtils.e("xuhw", "YYYYYY======onPause======");
	};

	@Override
	protected void onStop() {
		super.onStop();
		GolukDebugUtils.e("xuhw", "YYYYYY======onStop======");
		if(isShowPlayer){
			if (null != mRtmpPlayerView) {
				if (null != mRtmpPlayerView) {
					rtmpIsOk = false;
					updateVideoState();
					mFullScreen.setVisibility(View.GONE);
					mRtmpPlayerView.removeCallbacks(retryRunnable);
					if(mRtmpPlayerView.isPlaying()) {
						mRtmpPlayerView.stopPlayback();
					}
					hidePlayer();
				}
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukDebugUtils.e("xuhw", "YYYYYY======onDestroy======");
		if (null != mRtmpPlayerView) {
			mRtmpPlayerView.removeCallbacks(retryRunnable);
			mRtmpPlayerView.cleanUp();
		}

		if(null != GolukApplication.getInstance().getIPCControlManager()){
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

								m8sBtn.setBackgroundResource(R.drawable.btn_6s);
								break;
							case 2:

								break;
							case 3:

								m8sBtn.setBackgroundResource(R.drawable.btn_5s);
								break;
							case 4:

								break;
							case 5:

								m8sBtn.setBackgroundResource(R.drawable.btn_4s);
								break;
							case 6:

								break;
							case 7:

								m8sBtn.setBackgroundResource(R.drawable.btn_3s);
								break;
							case 8:

								break;
							case 9:

								m8sBtn.setBackgroundResource(R.drawable.btn_2s);
								break;
							case 10:

								break;
							case 11:

								m8sBtn.setBackgroundResource(R.drawable.btn_1s);
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
		m8sBtn.setBackgroundResource(R.drawable.screen_btn_6s_grey);
		if (null != m8sTimer) {
			m8sTimer.cancel();
			m8sTimer.purge();
			m8sTimer = null;
		}
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
				m8sBtn.setBackgroundResource(R.drawable.btn_ipc_8s);
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
		if (rtmpIsOk == true){
			mConnectTip.setText("摄像头影像正常");
		}else{
			mConnectTip.setText("摄像头影像加载中…");
		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			if (ConnectionStateMsg_Connected != msg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mShareBtn.setVisibility(View.GONE);
						m8sBtn.setBackgroundResource(R.drawable.screen_btn_6s_grey);

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
				if(isShowPlayer){
					if (!isConnecting) {
						isConnecting = true;
						start();
					}
				}
				
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					m8sBtn.setBackgroundResource(R.drawable.btn_ipc_8s);
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
			GolukDebugUtils.e("xuhw", "m8sBtn===IPC_VDCPCmd_TriggerRecord===4444=====param1="+param1+"==param2="+param2);
			GFileUtils
					.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord====1111111========param1="
							+ param1 + "=====param2=" + param2);
			TriggerRecord record = IpcDataParser
					.parseTriggerRecordResult((String) param2);
			if (null != record) {
				if (RESULE_SUCESS == param1) {
					mRecordVideFileName = record.fileName;
					GolukDebugUtils.e("xuhw", "m8sBtn===IPC_VDCPCmd_TriggerRecord===555555========type="+record.type);
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
				GolukDebugUtils.e("xuhw", "m8sBtn===IPC_VDCPCmd_TriggerRecord===6666====not success====");
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
									+ "video"
									+ File.separator
									+ "wonderful";
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
		//获取IPC系统音视频编码配置
		case IPC_VDCP_Msg_GetVedioEncodeCfg:
			if(param1 == RESULE_SUCESS){
				final VideoConfigState videocfg = IpcDataParser.parseVideoConfigState((String)param2);
				if(null != videocfg){
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if("1080P".equals(videocfg.resolution)){
								mVideoResolutions.setBackgroundResource(R.drawable.icon_hd1080);
							}else{
								mVideoResolutions.setBackgroundResource(R.drawable.icon_hd720);
							}
						}
					});
				}else{
					//获取失败
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
					if (null != json) {
						String filename = json.optString("filename");
						GolukDebugUtils.e("xuhw", "YYY====IPC_VDTP_Msg_File===="+filename);
						String tag = json.optString("tag");
						GolukDebugUtils.e("xuhw", "YYY==111===wonderfulVideoName="
								+ wonderfulVideoName);
						if (tag.equals("videodownload") && filename.contains("WND")) {
							wonderfulVideoName=filename;
							downloadFinish = true;
							downloadFileNumber--;
							
							downloadNumber = 0;

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mShareBtn.setVisibility(View.VISIBLE);
									mShareBtn
											.setBackgroundResource(R.drawable.screen_share);
									mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
									
									mShareBtn.removeCallbacks(mRunnable);
									mShareBtn.postDelayed(mRunnable, 12000);
								}
							});

						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
				mShareBtn
						.setVisibility(View.GONE);
			} else {
				GolukDebugUtils.e("xuhw", "KKKK=================================");
				downloadFinish = false;
				mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
				mHandler.sendEmptyMessage(DOWNLOADWONDERFULVIDEO);
			}
		}
	};
	
    /**
     * 设置播放器全屏
     * @param bFull true:全屏　false:普通
     * @author xuhw
     * @date 2015年5月12日
     */
    public void setFullScreen(boolean bFull) {
		if (bFull == m_bIsFullScreen) {
			GolukUtils.showToast(this, "已处于全屏状态.");
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
		    RelativeLayout.LayoutParams norParams = new RelativeLayout.LayoutParams((int)(38.66*density), (int)(30*density));
		    norParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		    norParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		    norParams.setMargins(0, 0, (int)(10*density), (int)(10*density));
		    mPlayerLayout.addView(mNormalScreen, norParams);
		    vgRoot.addView(mPlayerLayout);
		    
		    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
    	GolukDebugUtils.e("xuhw", "YYYYYY======onBackPressed=====m_bIsFullScreen="+m_bIsFullScreen);
		if (m_bIsFullScreen) {
		    // 全屏时，退出全屏
		    setFullScreen(false);
		} else {
		    super.onBackPressed();
		}
    }
    
	public void exit(){
		
		finish();
	}
	
}
