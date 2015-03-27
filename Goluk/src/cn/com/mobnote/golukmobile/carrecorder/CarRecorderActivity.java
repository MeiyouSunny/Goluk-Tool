package cn.com.mobnote.golukmobile.carrecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser.DeviceState;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser.TriggerRecord;
import cn.com.mobnote.golukmobile.carrecorder.SensorDetector.AccelerometerListener;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.FileUtils;

import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.ResultConstants;
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
public class CarRecorderActivity extends Activity implements OnClickListener,
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

	public enum VideoType {
		mounts, emergency, idle
	};

	/** 定时截图 */
	public static final int SCREENSHOOT = 111;
	/** 传感器控制管理 */
	private SensorDetector mSensorDetector = null;
	/** 定时截图时间 */
	public static final int SCREENSHOOTTIME = 5 * 60000;
	/** 紧急录制定时器 */
	private Timer mEmergencyRecordingTimer = null;
	/** 紧急录制时间 */
	private int emergencyRecordingTime = 6;
	/** 8s视频定时器 */
	private Timer m8sTimer = null;
	/** 当前拍摄时间 */
	private int mShootTime = 0;
	/** 一键抢拍按钮 */
	private ImageButton m8sBtn = null;
	/** 文件管理按钮 */
	private ImageButton mFileBtn = null;
	/** 设置按钮 */
	private ImageButton mSettingBtn = null;
	/** 录制时间显示 */
	private TextView mTime = null;
	/** 当前地址显示 */
	private TextView mAddr = null;
//	/** 速度百位显示 */
//	private ImageView mSpeed1 = null;
//	/** 速度十位显示 */
//	private ImageView mSpeed2 = null;
//	/** 速度个位显示 */
//	private ImageView mSpeed3 = null;
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
	/** 重新连接IPC最多15次 */
	private final int RECONNECTIONMAXBUMNER = 15;
	/** 记录重新连接IPC次数 */
	private int reconnectionMaxNumber = 0;
	/** 视频文件生成查询时间（10s超时） */
	private int videoFileQueryTime = 0;
	/** 直播开启标志 */
	private boolean isStartLive = false;
	/** 直播视频id */
	private String liveVid;
	/** 直播录制定时器 */
	private Timer mRecordTimer = null;
	/** 直播录制时间 */
	private int curRecordTime = 0;
	/** 单次直播录制时间 */
	private final int LIVERECORDINGTIME = 45;
	/** 紧急视频排队中标识 */
	private boolean emergencyQueuing = false;
	/** 发起紧急视频后查询紧急视频排队情况 */
	public static final int EMERGENCYQUERY = 115;
	/** 紧急视频排队查询记录时间（10s超时） */
	private int emergencyQueryTimeout = 0;
	/** 开启直播上传 */
	public static final int STARTLIVE = 116;
	/** 更新速度 */
	public static final int SPEED = 117;
	/** 更新位置信息 */
	public static final int ADDR = 118;
	/** 图像预览是否成功 */
	private boolean rtmpIsOk = false;
	/** IPC登录是否成功 */
	private boolean ipcIsOk = false;
	/** IPC连接状态 */
	private ImageView mIPCConnectState;
	/** 修复按钮 */
//	private ImageButton mIpcRepair;
	/** 视频录制状态显示 */
//	private ImageView mRecordState;
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
	/** 连接状态 */
	private TextView mConnectTip=null;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carrecorder_main);

		mHandler = new Handler() {
			public void handleMessage(final android.os.Message msg) {
				switch (msg.what) {
				case SCREENSHOOT:
					screenShoot();
					break;
				case QUERYFILEEXIT:
					queryFileExit();
					break;
				case EMERGENCY:
					startEmergencyRecording();
					break;
				case MOUNTS:
					startTrimVideo();
					break;
				case EMERGENCYQUERY:
					emergencyQuery();
					break;
				case STARTLIVE:
					String aid = (String) msg.obj;
					startLive(aid);
					break;
				case SPEED:
//					updateSpeed(msg.arg1);
					break;
				case ADDR:
					String addr = (String) msg.obj;
					mAddr.setText(addr);
					break;
				case STARTVIDEORECORD:
					updateVideoRecordTime();
					break;
				}
			};
		};

		initSensor();
		initView();
		setListener();
		// 开启视频录制计时器
		mHandler.sendEmptyMessageDelayed(STARTVIDEORECORD, 1000);

		// 获取是否是后台启动
		Intent receiveIntent = getIntent();
		isBackGroundStart = receiveIntent.getBooleanExtra("isBackGroundStart",
				false);

		// 注册回调监听
		GolukApplication.getInstance().getIPCControlManager()
				.addIPCManagerListener("main", this);
	}

	/**
	 * 数字转数码管图片
	 * 
	 * @param index
	 *            显示数字
	 * @param view
	 *            显示数字的控件
	 * @author xuhw
	 * @date 2015年3月11日
	 */
	private void showSpeedView(int index, View view) {
		switch (index) {
		case 0:
			view.setBackgroundResource(R.drawable.speed_0);
			break;
		case 1:
			view.setBackgroundResource(R.drawable.speed_1);
			break;
		case 2:
			view.setBackgroundResource(R.drawable.speed_2);
			break;
		case 3:
			view.setBackgroundResource(R.drawable.speed_3);
			break;
		case 4:
			view.setBackgroundResource(R.drawable.speed_4);
			break;
		case 5:
			view.setBackgroundResource(R.drawable.speed_5);
			break;
		case 6:
			view.setBackgroundResource(R.drawable.speed_6);
			break;
		case 7:
			view.setBackgroundResource(R.drawable.speed_7);
			break;
		case 8:
			view.setBackgroundResource(R.drawable.speed_8);
			break;
		case 9:
			view.setBackgroundResource(R.drawable.speed_9);
			break;

		default:
			break;
		}
	}

	/**
	 * 初始化控件
	 * 
	 * @author xuhw
	 * @date 2015年3月9日
	 */
	private void initView() {
		mRtmpPlayerLayout = (RelativeLayout) findViewById(R.id.mRtmpPlayerLayout);
		mVLayout = (RelativeLayout) findViewById(R.id.vLayout);
		m8sBtn = (ImageButton) findViewById(R.id.m8sBtn);
		mFileBtn = (ImageButton) findViewById(R.id.mFileBtn);
		mSettingBtn = (ImageButton) findViewById(R.id.mSettingBtn);
		mTime = (TextView) findViewById(R.id.mTime);
		mAddr = (TextView) findViewById(R.id.mAddr);
		mConnectTip = (TextView) findViewById(R.id.mConnectTip);
//		mSpeed1 = (ImageView) findViewById(R.id.mSpeed1);
//		mSpeed2 = (ImageView) findViewById(R.id.mSpeed2);
//		mSpeed3 = (ImageView) findViewById(R.id.mSpeed3);
		mIPCConnectState = (ImageView) findViewById(R.id.mIPCConnectState);
//		mIpcRepair = (ImageButton) findViewById(R.id.mIpcRepair);
//		mRecordState = (ImageView) findViewById(R.id.mRecordState);
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
		lp.height = (int) (screenWidth / 1.777);
		lp.leftMargin = 0;
		mRtmpPlayerLayout.setLayoutParams(lp);

		if (!isConnecting) {
			isConnecting = true;
			start();
		}

		mConnectTip.setText("摄像头未连接");
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			ipcIsOk = true;
			updateVideoState();
			m8sBtn.setBackgroundResource(R.drawable.btn_ipc_8s);
//			mFileBtn.setBackgroundResource(R.drawable.btn_filemanager);
			
		} 
		
		showLoading();
		hidePlayer();
		
		
		
		Button wifi = (Button)findViewById(R.id.wifi);
		wifi.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new CustomWifiDialog(CarRecorderActivity.this).show();
			}
		});
	}

	/**
	 * 设置监听事件
	 * 
	 * @author xuhw
	 * @date 2015年3月11日
	 */
	private void setListener() {
		m8sBtn.setOnClickListener(this);
		mFileBtn.setOnClickListener(this);
		mSettingBtn.setOnClickListener(this);
		findViewById(R.id.mFileText).setOnClickListener(this);
		findViewById(R.id.mSettingText).setOnClickListener(this);
		findViewById(R.id.mFileLayout).setOnClickListener(this);
		findViewById(R.id.mSettingLayout).setOnClickListener(this);
//		mIpcRepair.setOnClickListener(this);
//		mRecordState.setOnClickListener(this);
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
						reconnectionMaxNumber++;
						if (reconnectionMaxNumber <= RECONNECTIONMAXBUMNER) {
							showLoading();
							rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
						} else {
							mLoading.setVisibility(View.GONE);
							mLoadingText.setText("您的摄像头好像没有连接哦！");
						}

						return false;
					}

					@Override
					public void onPlayerCompletion(RtmpPlayerView rpv) {
						hidePlayer();
						rtmpIsOk = false;
						updateVideoState();
						rpv.removeCallbacks(retryRunnable);
						reconnectionMaxNumber++;
						if (reconnectionMaxNumber <= RECONNECTIONMAXBUMNER) {
							showLoading();
							rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
						} else {
							mLoading.setVisibility(View.GONE);
							mLoadingText.setText("您的摄像头好像没有连接哦！");
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
						reconnectionMaxNumber = 0;
						rtmpIsOk = true;
						updateVideoState();
						showPlayer();
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

//	/**
//	 * 更新速度显示信息
//	 * 
//	 * @param speed
//	 *            当前速度
//	 * @author xuhw
//	 * @date 2015年3月11日
//	 */
//	private void updateSpeed(int speed) {
//		int a1 = (speed + 1) / 100;
//		int a2 = ((speed + 1) / 10) % 10;
//		int a3 = (speed + 1) % 10;
//		if (a1 > 0) {
//			mSpeed1.setVisibility(View.VISIBLE);
//			showSpeedView(a1, mSpeed1);
//		} else {
//			mSpeed1.setVisibility(View.GONE);
//		}
//		if (a2 > 0) {
//			mSpeed2.setVisibility(View.VISIBLE);
//			showSpeedView(a2, mSpeed2);
//		} else {
//			mSpeed2.setVisibility(View.GONE);
//		}
//		showSpeedView(a3, mSpeed3);
//	}

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
	 * 初始化传感器监听
	 * 
	 * @author xuhw
	 * @date 2015年3月9日
	 */
	private void initSensor() {
		if (SensorDetector.isSupportAccelerometerSensor(this)) {
			mSensorDetector = new SensorDetector(this);
			mSensorDetector
					.registerAccelerometerListener(new AccelerometerListener() {
						@Override
						public void onChanged() {

							if (GolukApplication.getInstance().getIpcIsLogin()) {
								if (!isRecording) {
									sendEmergencyCommitId();
								} else {
									if (!emergencyQueuing) {
										GFileUtils
												.writeIPCLog("=====================紧急视频开始排队====================");
										emergencyQueuing = true;
										mHandler.sendEmptyMessage(EMERGENCYQUERY);
									}
								}
							} else {
								// 未登录
							}

						}
					});
		}
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
		mRtmpPlayerView.setVisibility(View.VISIBLE);
		mRtmpPlayerView.setDataSource(getResources().getString(
				R.string.default_rtsp_url));
		mRtmpPlayerView.start();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.m8sBtn:
			GFileUtils
					.writeIPCLog("=============================发起精彩视频命令===========m8sBtn=============");
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				if (!isRecording) {
					m8sBtn.setBackgroundResource(R.drawable.btn_8s_press);
					isRecording = true;
					updateRecordState(false, 0);
					mCurVideoType = VideoType.mounts;

					GFileUtils
							.writeIPCLog("=============================发起精彩视频命令================queryParam=");
					boolean isSucess = GolukApplication.getInstance()
							.getIPCControlManager().startWonderfulVideo();

					if (!isSucess) {
						videoTriggerFail();
						GFileUtils
								.writeIPCLog("=============================发起精彩视频命令============fail===============");
					}
				}
			} else {
				// 未登录
				GFileUtils
						.writeIPCLog("=============================发起精彩视频命令===========未登录=============");
			}
			break;
		case R.id.mFileBtn:
		case R.id.mFileText:
		case R.id.mFileLayout:
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				 Intent intent = new Intent(CarRecorderActivity.this, IPCFileManagerActivity.class);
				 startActivity(intent);
			} else {
				// 未登录
			}
			break;
		case R.id.mSettingBtn:
		case R.id.mSettingText:
		case R.id.mSettingLayout:
			 Intent setting = new Intent(CarRecorderActivity.this,SettingsActivity.class);
			 startActivity(setting);
			break;

		default:
			break;
		}
	}

	/**
	 * 发送紧急视频截取命令
	 * 
	 * @author xuhw
	 * @date 2015年3月5日
	 */
	private void sendEmergencyCommitId() {
		m8sBtn.setBackgroundResource(R.drawable.btn_8s_press);
		isRecording = true;
		updateRecordState(true, 0);
		mCurVideoType = VideoType.emergency;

		String queryParam = IpcDataParser.getTriggerRecordJson(TYPE_URGENT, 8,
				8);
		GFileUtils
				.writeIPCLog("=============================发起紧急视频命令=====1111===========queryParam="
						+ queryParam);
		boolean isSucess = GolukApplication.getInstance()
				.getIPCControlManager().startEmergencyVideo();
		if (!isSucess) {
			videoTriggerFail();
			GFileUtils
					.writeIPCLog("=============================发起紧急视频命令============fail===============");
		}
	}

	/**
	 * 显示加载中布局
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void showLoading() {
		mLoadingText.setText("图像加载中，请稍候...");
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
		registerReceiver(managerReceiver, new IntentFilter(
				CarRecorderManager.ACTION_RECORDER_MESSAGE));
		if (isBackGroundStart) {
			this.moveTaskToBack(true);
			isBackGroundStart = false;
		}

		Intent intent = new Intent();
		intent.setAction("cn.com.mobnote.broadcast.tachograph");
		intent.putExtra("type", "speed");
		intent.putExtra("state", 100);
		sendBroadcast(intent);

	};

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(managerReceiver);

		Intent intent = new Intent();
		intent.setAction("cn.com.mobnote.broadcast.tachograph");
		intent.putExtra("type", "speed");
		intent.putExtra("state", 101);
		sendBroadcast(intent);

	};

	@Override
	protected void onStop() {
		super.onStop();

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		GolukApplication.getInstance().getIPCControlManager()
				.removeIPCManagerListener("main");

		if (null != mRtmpPlayerView) {
			mRtmpPlayerView.stopPlayback();
			mRtmpPlayerView.cleanUp();
			mRtmpPlayerView = null;
		}

		// closeApp();

	};

	/**
	 * 关闭程序
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void closeApp() {
		android.os.Process.killProcess(android.os.Process.myPid());
		CarRecorderActivity.this.finish();
	}

	/**
	 * 响应视频Manager消息
	 */
	private BroadcastReceiver managerReceiver = new RecorderMsgReceiverBase() {
		@Override
		public void onManagerBind(Context context, int nResult,
				String strResultInfo) {
			if (nResult >= ResultConstants.SUCCESS) {

			}
		}

		public void onLiveRecordBegin(Context context, int nResult,
				String strResultInfo) {
			String message;
			if (nResult >= ResultConstants.SUCCESS) {
				message = "onLiveRecordBegin　视频录制上传成功" + strResultInfo;
			} else {
				message = "onLiveRecordBegin　视频录制上传失败 = " + strResultInfo;
				// isStartLive = false;
			}
			GFileUtils.writeLiveLog(message);
		};

		@Override
		public void onLiveRecordFailed(Context context, int nResult,
				String strResultInfo) {
			// isStartLive = false;
			// if (nResult != ResultConstants.ERROR_LIVE_UPLOAD_FAILED) {
			// LuaUtil.RecordVideoError(mApplication.mLuaState, 100);
			// System.out.println("KKKKKK 直播失败 : " + nResult + strResultInfo);
			// }

			GFileUtils.writeLiveLog("直播失败" + "======onLiveRecordFailed======="
					+ strResultInfo);

		}

	};

	/**
	 * 视频截图
	 * 
	 * @author xuhw
	 * @date 2015年3月4日
	 */
	private void screenShoot() {
		// if (GolukApplication.getInstance().getIsLogin()) {
		// GFileUtils.writeShootLog("========发起ipc图片截图========");
		// boolean isSuccess = GolukApplication.getInstance()
		// .getIpcManagerClass()
		// .IPCManager_VDCP_CommRequest(IPC_VDCP_Msg_SnapPic, "");
		// if (!isSuccess) {
		// GFileUtils.writeShootLog("========ipc截图命令 　发送失败========");
		// }
		// } else {
		// GFileUtils.writeShootLog("========ipc截图命令失败　未登录========");
		// }

		GolukApplication.getInstance().getIPCControlManager().screenShot();
		mHandler.removeMessages(SCREENSHOOT);
		mHandler.sendEmptyMessageDelayed(SCREENSHOOT, SCREENSHOOTTIME);
	}

	/**
	 * 开启紧急录制
	 * 
	 * @author xuhw
	 * @date 2015年3月4日
	 */
	private void startEmergencyRecording() {
		if (null == mEmergencyRecordingTimer) {
			updateRecordState(true, 0);
			System.out.println("PPPPPPPPPPP   emergency 222222");
			isRecording = true;
			SoundUtils.getInstance().play(SoundUtils.RECORD_EMERGENT);
			mEmergencyRecordingTimer = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					stopEmergencyRecording();
				}
			};
			mEmergencyRecordingTimer.schedule(task,
					emergencyRecordingTime * 1000,
					emergencyRecordingTime * 1000);
		}
	}

	/**
	 * 关闭紧急录制
	 * 
	 * @author xuhw
	 * @date 2015年3月4日
	 */
	private void stopEmergencyRecording() {
		isRecording = false;
		mHandler.sendEmptyMessageDelayed(CarRecorderActivity.QUERYFILEEXIT,
				QUERYFILETIME);
		SoundUtils.getInstance().play(SoundUtils.RECORD_EMERGENT, 2);
		if (null != mEmergencyRecordingTimer) {
			mEmergencyRecordingTimer.cancel();
			mEmergencyRecordingTimer.purge();
			mEmergencyRecordingTimer = null;
		}
	}

	/**
	 * 处理紧急视频排队情况
	 * 
	 * @author xuhw
	 * @date 2015年3月9日
	 */
	private void emergencyQuery() {
		mHandler.removeMessages(EMERGENCYQUERY);
		if (!isRecording) {
			emergencyQueryTimeout = 0;
			emergencyQueuing = false;
			sendEmergencyCommitId();
		} else {
			emergencyQueryTimeout++;
			if (emergencyQueryTimeout <= 15) {
				GFileUtils
						.writeIPCLog("=====================紧急视频开始排队===============emergencyQueryTimeout=="
								+ emergencyQueryTimeout);
				mHandler.sendEmptyMessageDelayed(EMERGENCYQUERY, 1000);
			} else {
				emergencyQueryTimeout = 0;
			}
		}
	}

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
								m8sBtn.setBackgroundResource(R.drawable.btn_8s_grey);
								break;
							case 2:
								SoundUtils.getInstance().play(
										SoundUtils.RECORD_SEC);
								break;
							case 3:
								m8sBtn.setBackgroundResource(R.drawable.btn_7s);
								break;
							case 4:
								SoundUtils.getInstance().play(
										SoundUtils.RECORD_SEC);
								break;
							case 5:
								m8sBtn.setBackgroundResource(R.drawable.btn_6s);
								break;
							case 6:
								SoundUtils.getInstance().play(
										SoundUtils.RECORD_SEC);
								break;
							case 7:
								m8sBtn.setBackgroundResource(R.drawable.btn_5s);
								break;
							case 8:
								SoundUtils.getInstance().play(
										SoundUtils.RECORD_SEC);
								break;
							case 9:
								m8sBtn.setBackgroundResource(R.drawable.btn_4s);
								break;
							case 10:
								SoundUtils.getInstance().play(
										SoundUtils.RECORD_SEC);
								break;
							case 11:
								m8sBtn.setBackgroundResource(R.drawable.btn_3s);
								break;
							case 12:
								SoundUtils.getInstance().play(
										SoundUtils.RECORD_SEC);
								break;
							case 13:
								m8sBtn.setBackgroundResource(R.drawable.btn_2s);
								break;
							case 14:
								SoundUtils.getInstance().play(
										SoundUtils.RECORD_SEC);
								break;
							case 15:
								m8sBtn.setBackgroundResource(R.drawable.btn_1s);
								break;
							case 16:
								SoundUtils.getInstance().play(
										SoundUtils.RECORD_CAMERA);
								break;

							default:
								break;
							}

							if (mShootTime > 16) {
								stopTrimVideo();
							}
						}
					});

				}
			};
			m8sTimer.schedule(task, 500, 500);

		} else {
			// Toast.makeText(mActivity, "视频抢拍中，请稍后再试...",
			// Toast.LENGTH_SHORT).show();
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
		m8sBtn.setBackgroundResource(R.drawable.btn_ipc_8s);
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
	 * 开启直播录制上传
	 * 
	 * @param aid
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void startLive(String aid) {
		GFileUtils
				.writeLiveLog("===========行车记录仪=============startLive========111111==================aid="
						+ aid);
		liveVid = aid;

		curRecordTime = 0;
		if (null != mRecordTimer) {
			mRecordTimer.cancel();
			mRecordTimer.purge();
			mRecordTimer = null;
		}
		mRecordTimer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {

				curRecordTime++;
				if (curRecordTime >= LIVERECORDINGTIME) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							if (CarRecorderManager.isRTSPLiving()) {
								try {
									isStartLive = false;
									CarRecorderManager.stopRTSPLive();
								} catch (RecorderStateException e) {
									e.printStackTrace();
								}

							}

						}
					});

					if (null != mRecordTimer) {
						mRecordTimer.cancel();
						mRecordTimer.purge();
						mRecordTimer = null;
					}

				}
			}
		};
		mRecordTimer.schedule(task, 1000, 1000);

		GFileUtils
				.writeLiveLog("===========行车记录仪=============startLive========22222====================");
		// GolukApplication.getInstance().notifyVideoID(liveVid);
		GFileUtils
				.writeLiveLog("===========行车记录仪=============startLive========555555====================");

		if (!isStartLive) {
			if (!CarRecorderManager.isRTSPLiving()) {
				System.out.println("KKKKKK 111111111111111111111");
				try {
					SharedPreferences sp = getSharedPreferences(
							"CarRecorderPreferaces", Context.MODE_PRIVATE);
					sp.edit()
							.putString("url_live",
									"rtmp://211.103.234.234/live/" + liveVid)
							.apply();
					sp.edit().commit();

					CarRecorderManager
							.updateLiveConfiguration(new PreferencesReader(this)
									.getConfig());

					CarRecorderManager.setLiveMute(true);
					CarRecorderManager.startRTSPLive();
					GFileUtils
							.writeLiveLog("===========行车记录仪=============startLive========666666========发起直播成功============");
					isStartLive = true;
					System.out.println("KKKKKK 开启直播");
				} catch (RecorderStateException e) {
					System.out.println("KKKKKK 直播异常 : " + e.toString());
					// LuaUtil.RecordVideoError(mApplication.mLuaState, 101);
					e.printStackTrace();
					// Toast.makeText(TalkActivity.this, "开启直播失败"+e.toString(),
					// Toast.LENGTH_SHORT).show();
				}
			}
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
			updateRecordState(true, 3);
		} else if (mCurVideoType == VideoType.mounts) {
			updateRecordState(false, 3);
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
		if (rtmpIsOk == false && ipcIsOk == false) {
			mConnectTip.setText("摄像头未连接");
//			mIPCConnectState.setBackgroundResource(R.drawable.ipc_ununited);
		} else if (rtmpIsOk == true && ipcIsOk == true) {
			mConnectTip.setText("摄像头已连接");
//			mIPCConnectState.setBackgroundResource(R.drawable.ipc_normal);
		} else {
			mConnectTip.setText("摄像头连接中...");
//			mIPCConnectState.setBackgroundResource(R.drawable.ipc_error);
		}
	}

	private void updateRecordState(boolean emcrgency, int state) {
//		switch (state) {
//		case 0:
//			if (emcrgency) {
//				mRecordState.setBackgroundResource(R.drawable.emergency_record);
//			} else {
//				mRecordState.setBackgroundResource(R.drawable.video_8s_record);
//			}
//			break;
//		case 1:
//			if (emcrgency) {
//				mRecordState.setBackgroundResource(R.drawable.emergency_read);
//			} else {
//				mRecordState.setBackgroundResource(R.drawable.video_8s_read);
//			}
//			break;
//		case 2:
//			if (emcrgency) {
//				mRecordState
//						.setBackgroundResource(R.drawable.emergency_success);
//			} else {
//				mRecordState.setBackgroundResource(R.drawable.video_8s_success);
//			}
//			mRecordState.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					mRecordState.setBackgroundResource(0);
//				}
//			}, 1500);
//			break;
//		case 3:
//			if (emcrgency) {
//				mRecordState.setBackgroundResource(R.drawable.emergency_fail);
//			} else {
//				mRecordState.setBackgroundResource(R.drawable.video_8s_fail);
//			}
//			mRecordState.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					mRecordState.setBackgroundResource(0);
//				}
//			}, 1500);
//			break;
//
//		default:
//			break;
//		}

	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			if (ConnectionStateMsg_Connected != msg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						m8sBtn.setBackgroundResource(R.drawable.btn_8s_press);
//						mFileBtn.setBackgroundResource(R.drawable.btn_file_sel);
					}
				});
			}
		}

		if (ENetTransEvent_IPC_VDCP_CommandResp == event
				&& IPC_VDCP_Msg_Init == msg && 0 == param1) {
			ipcIsOk = true;
			updateVideoState();
			if (!ipcFirstLogin) {
				ipcFirstLogin = true;
				if (!isConnecting) {
					isConnecting = true;
					start();
				}
				// mHandler.sendEmptyMessageDelayed(SCREENSHOOT, 1000);
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					m8sBtn.setBackgroundResource(R.drawable.btn_ipc_8s);
//					mFileBtn.setBackgroundResource(R.drawable.btn_filemanager);
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
								+ "tiros-com-cn-ext"
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
							// TachographApplication.getInstance().uploadPicture(
							// picName);
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
			GFileUtils
					.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord====1111111========param1="
							+ param1 + "=====param2=" + param2);
			TriggerRecord record = IpcDataParser
					.parseTriggerRecordResult((String) param2);
			if (null != record) {
				if (RESULE_SUCESS == param1) {
					mRecordVideFileName = record.fileName;
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
				GFileUtils
						.writeIPCLog("===========IPC_VDCPCmd_TriggerRecord===77777======= not success ==========");
				videoTriggerFail();
			}

			break;
		// 单文件查询
		case IPC_VDCPCmd_SingleQuery:
			GFileUtils
					.writeIPCLog("===========IPC_VDCPCmd_SingleQuery===11111=========param1="
							+ param1 + "=====param2=" + param2);
			if (RESULE_SUCESS == param1) {
				VideoFileInfo fileInfo = IpcDataParser
						.parseSingleFileResult((String) param2);
				if (null != fileInfo) {
					if (!TextUtils.isEmpty(fileInfo.location)) {
						Intent mIntent = new Intent("sendfile");
						if (TYPE_SHORTCUT == fileInfo.type) {// 精彩
							updateRecordState(false, 2);
							mIntent.putExtra("filetype", "mounts");
							mIntent.putExtra("filename", fileInfo.location);
							sendBroadcast(mIntent);
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SingleQuery==2222=======精彩视频查询成功===========");
						} else if (TYPE_URGENT == fileInfo.type) {// 紧急
							updateRecordState(true, 2);
							mIntent.putExtra("filetype", "emergency");
							mIntent.putExtra("filename", fileInfo.location);
							sendBroadcast(mIntent);
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
		// 删除文件
		case IPC_VDCPCmd_Erase:
			if (RESULE_SUCESS == param1) {
				try {
					JSONObject json = new JSONObject((String) param2);
					int result = json.getInt("result");
					if (RESULE_SUCESS == result) {
						// 删除文件成功
					} else if (1 == result) {
						// 文件不存在，
					} else {
						// 文件删除失败
					}
				} catch (Exception e) {
				}
			} else {

			}
			break;
		// 查询设备状态
		case IPC_VDCPCmd_DeviceStatus:
			if (RESULE_SUCESS == param1) {
				DeviceState deviceState = IpcDataParser
						.parseDeviceState((String) param2);
			} else {

			}
			break;
		// 多文件目录查询
		case IPC_VDCPCmd_Query:
			if (RESULE_SUCESS == param1) {
				ArrayList<VideoFileInfo> fileList = IpcDataParser
						.parseMoreFile((String) param2);
				if (null != fileList) {

				} else {

				}
			} else {

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

			break;

		default:
			break;
		}
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if(keyCode==KeyEvent.KEYCODE_BACK){
	// this.finish();
	// return true;
	// }else{
	// return super.onKeyDown(keyCode, event);
	// }
	// }

}
