package cn.com.mobnote.golukmobile.live;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.golukmobile.UserLoginActivity;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser.TriggerRecord;
import cn.com.mobnote.golukmobile.carrecorder.PreferencesReader;
import cn.com.mobnote.golukmobile.carrecorder.RecorderMsgReceiverBase;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress.IBaiduGeoCoderFn;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.live.LiveSettingPopWindow.IPopwindowFn;
import cn.com.mobnote.golukmobile.live.TimerManager.ITimerManagerFn;
import cn.com.mobnote.golukmobile.videosuqare.JsonCreateUtils;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.umeng.widget.CustomShareBoard;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.console;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.utils.LogUtil;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.ResultConstants;
import com.rd.car.player.RtmpPlayerView;

public class LiveActivity extends BaseActivity implements OnClickListener, RtmpPlayerView.RtmpPlayerViewLisener,
		View.OnTouchListener, ITalkFn, IPopwindowFn, ILiveDialogManagerFn, ITimerManagerFn, ILocationFn,
		IBaiduGeoCoderFn, IPCManagerFn, ILive, VideoSuqareManagerFn {

	/** 自己预览地址 */
	private static String VIEW_SELF_PLAY = "";

	/** application */
	private GolukApplication mApp = null;
	/** 返回按钮 */
	private ImageButton mLiveBackBtn = null;
	/** 刷新按钮 */
	private Button mRefirshBtn = null;
	/** 暂停按钮 */
	private Button mPauseBtn = null;
	/** title */
	private TextView mTitleTv = null;
	/** 当前地址 */
	private TextView mAddressTv = null;

	/** 视频loading */
	private RelativeLayout mVideoLoading = null;
	/** 播放布局 */
	private RelativeLayout mPlayLayout = null;
	/** 直播超时提示文字 */
	private TextView mTimeOutText = null;
	/** 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private BaiduMapManage mBaiduMapManage = null;
	/** 自定义播放器支持特效 */
	public RtmpPlayerView mRPVPalyVideo = null;
	/** 用户aid */
	private String mAid = "";
	/** 用户uid */
	private String mUid = "";
	/** 视频地址 */
	private String mFilePath = "";
	/** 首页handler用来接收消息,更新UI */
	public static Handler mLiveVideoHandler = null;
	/** 是否直播 还是　看别人直播 true/false 直播/看别人直播 */
	private boolean isShareLive = true;
	/** 直播开启标志 */
	private boolean isStartLive = false;
	/** 直播视频id */
	private String liveVid;
	/** 单次直播录制时间 (秒)(包括自己的时间与看别人的时间) */
	private int mLiveCountSecond = 60;
	/** 直播倒计时显示 */
	private TextView mLiveCountDownTv = null;

	private RelativeLayout mBottomLayout = null;
	/** 直播界面说话按钮 */
	private ImageButton mLiveTalk = null;
	/** 观看直播界面说话按钮 */
	private ImageButton mLiveLookTalk = null;
	private LinearLayout mQiangPaiLayout = null;
	private LinearLayout mExitLayout = null;
	private ImageView mQiangpaiImg = null;
	private ImageView mExitBtn = null;
	/** 说话状态标识 */
	private ImageView mTalkingSign = null;
	/** 当前正在说话的 */
	private TextView mTalkingTv = null;
	/** 说话中计时显示 */
	private TextView mTalkingTimeTv = null;
	private RelativeLayout mSpeakingLayout = null;
	/** 视频描述 */
	private TextView mDescTv = null;

	/** 观看人数 */
	private TextView mLookCountTv = null;
	/** 点赞 显示 */
	private TextView mZancountTv = null;
	private LinearLayout mOkLayout = null;
	private ImageView mLiveOk = null;
	/** 分享 */
	private ImageView mShareImg = null;
	private LinearLayout mShareLayout = null;

	private String mJoinGroupJson = null;
	private UserInfo currentUserInfo = null;
	private LiveDataInfo mDataInfo = null;
	/** 我的登录信息 如果未登录，则为空 */
	private UserInfo myInfo = null;

	private boolean isStart = false;
	/** 是否续直播 */
	private boolean isContinueLive = false;

	private LayoutInflater mLayoutFlater = null;

	private LiveSettingPopWindow mliveSettingWindow = null;
	private RelativeLayout mRootLayout = null;
	boolean isShowPop = false;

	/** 8s视频定时器 */
	private Timer m8sTimer = null;
	/** 当前拍摄时间 */
	private int mShootTime = 0;

	private boolean isSucessBind = false;

	/** 是否已经点过“赞” */
	private boolean isAlreadClickOK = false;

	private TimerManager mLiveManager = null;

	/** 防止多次按退出键 */
	private boolean isAlreadExit = false;

	private String mCurrentVideoId = null;

	/** 标识是否正在获取地址 */
	private boolean isGetingAddress = false;

	/** -1/0/1 未定位/小蓝点/气泡 */
	private int mCurrentLocationType = LOCATION_TYPE_UNKNOW;
	/** 当前点赞次数 */
	private int mCurrentOKCount = 0;

	/** 是否支持声音 */
	private boolean isCanVoice = true;
	private ImageView mHead = null;

	/** */
	private RelativeLayout mMapRootLayout = null;
	/** 精彩视频名称 */
	private String wonderfulVideoName = null;

	/** IPC登录是否成功 */
	private boolean ipcIsOk = false;

	/** 是否成功上传过视频 */
	private boolean isUploadSucessed = false;
	/** 是否请求服务器成功过 */
	private boolean isKaiGeSucess = false;

	LiveDataInfo liveData = null;

	/** 是否正在重連 */
	private boolean isTryingReUpload = false;

	private RelativeLayout mVLayout = null;

	/** 用户设置数据 */
	LiveSettingBean mSettingData = null;

	private boolean isSupportJoinGroup = false;

	/** 用户是否成功加入爱滔客频道 */
	private boolean mIsJoinGroupSucess = false;
	/** 爱滔客登录是否成功 */
	private boolean mIsAirtalkLoginSucess = false;
	/** 用户加入的爱滔客群组信息 */
	private String mCurrentGroupInfo = null;
	/** 网络连接是否可用 */
	private boolean mLinkEnable = true;
	/** 别人说话时可以　不能说话　 */
	private boolean isCanSpeak = true;
	/** 当前正在说话的人 */
	private String mSpeakName = "";

	/** 记录对讲超时时间(30s) */
	private int mSpeechOutTime = 0;
	/** 记录对讲超时计时(3s) */
	private int mSpeechCountDownTime = 0;
	/** 表示用户是否正处于上次超时的3秒内，如果上次说话超时，则3秒内不能说话 */
	private boolean mTimeOutEnable = false;
	/** 对讲按钮按下状态 */
	private boolean mTalkTouchDown = false;

	private boolean mIsMe = false;
	/** 标识直播上传是否超时 */
	private boolean isLiveUploadTimeOut = false;

	/** 视频文件生成查询时间（10s超时） */
	private int videoFileQueryTime = 0;
	/** 保存录制的文件名字 */
	public String mRecordVideFileName = "";
	/** 保存录制中的状态 */
	public boolean isRecording = false;

	public enum VideoType {
		mounts, emergency, idle
	};

	/** 保存当前录制的视频类型 */
	public VideoType mCurVideoType = VideoType.idle;

	VideoSquareManager mVideoSquareManager = null;

	SharePlatformUtil sharePlatform;
	/** 设置是否返回 */
	private boolean isSettingCallBack = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		mLayoutFlater = LayoutInflater.from(this);
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.live, null);
		getWindow().setContentView(mRootLayout);
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "LiveVideo");

		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();// 设置分享平台的参数

		VIEW_SELF_PLAY = "rtsp://admin:123456@" + mApp.mIpcIp + "/sub";

		// 获取数据
		getIntentData();
		// 界面初始化
		initView();
		// 根据不同的状态显示不同的界面
		switchView();
		// 显示数据
		setViewInitData();
		// 地图初始化
		initMap();
		// 获取我的登录信息
		getMyInfo();
		// 开始预览或开始直播
		if (isShareLive) {
			mApp.mSharedPreUtil.setIsLiveNormalExit(false);
			mCurrentVideoId = getVideoId();
			startVideoAndLive("");
			mTitleTv.setText("我的直播");
			if (null != mRefirshBtn) {
				mRefirshBtn.setVisibility(View.GONE);
			}
			setUserHeadImage(myInfo.head);
		} else {
			if (null != currentUserInfo && null != currentUserInfo.desc) {
				mDescTv.setText(currentUserInfo.desc);
			}
			mApp.mSharedPreUtil.setIsLiveNormalExit(true);
			if (null != currentUserInfo) {
				mLiveCountSecond = currentUserInfo.liveDuration;
			}
			mTitleTv.setText(currentUserInfo.nickName + " 的直播");

			setUserHeadImage(currentUserInfo.head);
		}
		drawPersonsHead();
		// 加入爱滔客群组
		joinAitalkGroup();

		mliveSettingWindow = new LiveSettingPopWindow(this, mRootLayout);
		mliveSettingWindow.setCallBackNotify(this);

		if (isShareLive) {
			if (isContinueLive) {
				// 续直播
				startLiveLook(myInfo);
				LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE, LIVE_RETRY_LIVE);
			} else {
				// 显示设置窗口
				mLiveVideoHandler.sendEmptyMessageDelayed(100, 600);
			}

			updateCount(0, 0);
		} else {
			// 计时，90秒后，防止用户进入时没网
			mHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
			startLiveLook(currentUserInfo);
			updateCount(Integer.parseInt(currentUserInfo.zanCount), Integer.parseInt(currentUserInfo.persons));
			// 获取地址
			isGetingAddress = true;
			GetBaiduAddress.getInstance().searchAddress(Double.parseDouble(currentUserInfo.lat),
					Double.parseDouble(currentUserInfo.lon));
		}
		GetBaiduAddress.getInstance().setCallBackListener(this);
		// 在没有进入群组时，按钮不可按
		refreshPPtState(false);

		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		mLiveManager = new TimerManager(10);
		mLiveManager.setListener(this);

		mApp.addLocationListener(TAG, this);

		// 注册回调监听
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("live", this);

		mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("live", this);
		}
	}

	/**
	 * 视频截图
	 * 
	 * @author xuhw
	 * @date 2015年3月4日
	 */
	private void screenShoot() {
		GolukApplication.getInstance().getIPCControlManager().screenShot();
	}

	// 更新观看人数和点赞人数
	private void updateCount(int okCount, int lookCount) {
		mCurrentOKCount = okCount;
		if (null != mZancountTv) {
			mZancountTv.setText("" + okCount);
		}
		if (null != mLookCountTv) {
			mLookCountTv.setText("" + lookCount);
		}
	}

	// 获取当前登录用户的信息
	private void getMyInfo() {
		try {
			LogUtil.e(null, "jyf----20150406----LiveActivity----getMyInfo111 :" + mApp.isUserLoginSucess);
			if (mApp.isUserLoginSucess) {
				String userInfo = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
						IPageNotifyFn.PageType_GetUserInfo_Get, "");
				if (null != userInfo) {
					myInfo = JsonUtil.parseSingleUserInfoJson(new JSONObject(userInfo));
					LogUtil.e(null, "jyf----20150406----LiveActivity----getMyInfo :" + userInfo);
				}
			}
			LogUtil.e(null, "jyf----20150406----LiveActivity----getMyInfo 333:");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getIntentData() {
		// 获取视频路径
		Intent intent = getIntent();
		mAid = intent.getStringExtra("cn.com.mobnote.map.aid");
		mUid = intent.getStringExtra("cn.com.mobnote.map.uid");

		isShareLive = intent.getBooleanExtra(KEY_IS_LIVE, true);
		mJoinGroupJson = intent.getStringExtra(KEY_JOIN_GROUP);
		currentUserInfo = (UserInfo) intent.getSerializableExtra(KEY_USERINFO);
		mDataInfo = (LiveDataInfo) intent.getSerializableExtra(KEY_LIVE_DATA);
		isContinueLive = intent.getBooleanExtra(KEY_LIVE_CONTINUE, false);
	}

	private void setViewInitData() {
		if (null != currentUserInfo) {
			mZancountTv.setText(currentUserInfo.zanCount);
			mLookCountTv.setText(currentUserInfo.persons);
		}
	}

	private String getVideoId() {
		if (null != myInfo) {
			return myInfo.uid;
		}
		Date dt = new Date();
		long time = dt.getTime();

		return "live" + time;
	}

	boolean isRequestedForServer = false;

	// 开启自己的直播,请求服务器 (在用户点击完设置后开始请求)
	private void startLiveForServer() {
		isRequestedForServer = true;
		String json = null;
		if (this.isContinueLive) {

		} else {
			json = JsonUtil.getStartLiveJson(mCurrentVideoId, mSettingData);
		}
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_LiveStart, json);
		if (!isSucess) {
			startLiveFailed();
		} else {
			if (!isAlreadExit) {
				LiveDialogManager.getManagerInstance().setProgressDialogMessage(LIVE_CREATE);
			}
		}
	}

	// 查看他人的直播
	public void startLiveLook(UserInfo userInfo) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----startLiveLook----111 uid: " + userInfo.uid + " aid:"
				+ userInfo.aid);
		if (isLiveUploadTimeOut) {
			return;
		}

		String condi = "{\"uid\":\"" + userInfo.uid + "\",\"desAid\":\"" + userInfo.aid + "\"}";

		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVideoDetail, condi);
		if (!isSucess) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----startLiveLook----22 : FASE False FAlse");
			startLiveLookFailed();
		} else {
			// TODO 弹对话框
			// showToast("查看他人直播：" + userInfo.uid);
			LogUtil.e(null, "jyf----20150406----LiveActivity----startLiveLook----22 : TRUE TRUE");
		}
	}

	private void startLiveFailed() {
		if (!isAlreadExit) {
			LiveDialogManager.getManagerInstance().showTwoBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_LIVE_REQUEST_SERVER, LIVE_DIALOG_TITLE, LIVE_UPLOAD_FIRST_ERROR);
		}
	}

	private void startLiveLookFailed() {
		// showToast("查看他人直播失败");
	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void initView() {
		mLiveBackBtn = (ImageButton) findViewById(R.id.live_back_btn);
		mTitleTv = (TextView) findViewById(R.id.live_title);
		mRefirshBtn = (Button) findViewById(R.id.live_refirsh_btn);

		mVLayout = (RelativeLayout) findViewById(R.id.vLayout);

		mTimeOutText = (TextView) findViewById(R.id.live_time_out_text);
		mVideoLoading = (RelativeLayout) findViewById(R.id.live_video_loading);
		mPlayLayout = (RelativeLayout) findViewById(R.id.live_play_layout);

		mZancountTv = (TextView) findViewById(R.id.live_okcount);
		mLookCountTv = (TextView) findViewById(R.id.live_lookcount);
		mOkLayout = (LinearLayout) findViewById(R.id.live_oklayout);
		mOkLayout.setOnClickListener(this);
		mLiveOk = (ImageView) findViewById(R.id.live_ok);

		mHead = (ImageView) findViewById(R.id.live_userhead);

		mLiveCountDownTv = (TextView) findViewById(R.id.live_countdown);
		mDescTv = (TextView) findViewById(R.id.live_desc);

		mPauseBtn = (Button) findViewById(R.id.live_pause);
		mPauseBtn.setOnClickListener(this);

		mShareLayout = (LinearLayout) findViewById(R.id.live_sharelayout);
		mShareLayout.setOnClickListener(this);
		mShareImg = (ImageView) findViewById(R.id.live_share);

		mBottomLayout = (RelativeLayout) findViewById(R.id.live_bottomlayout);
		mLiveLookTalk = (ImageButton) findViewById(R.id.livelook_ppt);
		mLiveTalk = (ImageButton) findViewById(R.id.live_ppt);
		mQiangPaiLayout = (LinearLayout) findViewById(R.id.live_qiangpai);
		mExitLayout = (LinearLayout) findViewById(R.id.live_exit);
		mExitBtn = (ImageView) findViewById(R.id.live_exit_btn);
		mQiangPaiLayout.setOnClickListener(this);
		mExitLayout.setOnTouchListener(this);
		mLiveTalk.setOnTouchListener(this);

		mAddressTv = (TextView) findViewById(R.id.live_address);
		mTalkingTv = (TextView) findViewById(R.id.live_talking);
		mSpeakingLayout = (RelativeLayout) findViewById(R.id.live_speaklayout);
		mTalkingSign = (ImageView) findViewById(R.id.live_talking_sign);
		mTalkingTimeTv = (TextView) findViewById(R.id.live_talktime);

		mQiangpaiImg = (ImageView) findViewById(R.id.qiangpai_img);

		mMapRootLayout = (RelativeLayout) findViewById(R.id.live_map_layout);

		mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_6s_press1);

		if (GolukApplication.getInstance().getIpcIsLogin()) {
			ipcIsOk = true;
			mQiangpaiImg.setBackgroundResource(R.drawable.btn_live_6s);
		}

		mRPVPalyVideo = (RtmpPlayerView) findViewById(R.id.live_vRtmpPlayVideo);
		// 视频事件回调注册
		mRPVPalyVideo.setPlayerListener(this);
		mRPVPalyVideo.setBufferTime(1000);
		mRPVPalyVideo.setConnectionTimeout(30000);
		// 先显示气泡上的默认图片

		// 注册事件
		mLiveBackBtn.setOnClickListener(this);
		mRefirshBtn.setOnClickListener(this);
		mPlayLayout.setOnClickListener(this);

		hidePlayer();

		// 更新UI handler
		mLiveVideoHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case 1:
					// 测试获取视频详情
					Object obj = new Object();
					LiveVideoDataCallBack(1, obj);
					break;
				case 2:
					// 5秒超时显示,提示文字
					mTimeOutText.setVisibility(View.VISIBLE);
					break;
				case 100:
					isContinueLive = false;
					mliveSettingWindow.show();
					break;
				case 101:
					// 直播视频上传成功，现在请求服务器
					// 请求直播
					startLiveForServer();
					break;
				}
			}
		};
	}

	/**
	 * 开启直播录制上传
	 * 
	 * @param aid
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void startLive(String aid) {
		liveVid = aid;
		LogUtil.e("", "jyf------TTTTT------------开始上传直播----1111 : " + aid);
		if (isStartLive) {
			return;
		}
		LogUtil.e("", "jyf------TTTTT------------开始上传直播----2222");
		if (CarRecorderManager.isRTSPLiving()) {
			LogUtil.e("", "jyf------TTTTT------------RTSP正在播放，不可以开始");
			// showToast("RTSP正在直播，不可以开始");
			liveUploadVideoFailed();
			return;
		}
		try {
			LogUtil.e("", "jyf------TTTTT------------开始上传直播----3333");
			SharedPreferences sp = getSharedPreferences("CarRecorderPreferaces", Context.MODE_PRIVATE);
			sp.edit().putString("url_live", UPLOAD_VOIDE_PRE + liveVid).apply();
			sp.edit().commit();
			CarRecorderManager.updateLiveConfiguration(new PreferencesReader(this, false).getConfig());
			if (null != mSettingData) {
				CarRecorderManager.setLiveMute(!mSettingData.isCanVoice);
			}
			LogUtil.e("", "jyf------TTTTT------------开始上传直播----44444444");
			CarRecorderManager.startRTSPLive();
			isStartLive = true;
			// showToast("开始上传视频");
			LogUtil.e("", "jyf------TTTTT------------开始上传直播----start--------");
		} catch (RecorderStateException e) {
			e.printStackTrace();
			liveUploadVideoFailed();
			LogUtil.e("", "jyf------TTTTT------------开始上传直播----Exception ");
		}
	}

	private void stopRTSPUpload() {
		if (CarRecorderManager.isRTSPLiving()) {
			try {
				// showToast("停止上传直播");
				isStartLive = false;
				CarRecorderManager.stopRTSPLive();
			} catch (RecorderStateException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 响应视频Manager消息
	 */
	private BroadcastReceiver managerReceiver = new RecorderMsgReceiverBase() {
		@Override
		public void onManagerBind(Context context, int nResult, String strResultInfo) {
		}

		public void onLiveRecordBegin(Context context, int nResult, String strResultInfo) {
			if (nResult >= ResultConstants.SUCCESS) {
				// 视频录制上传成功
				isUploadSucessed = true;
				isTryingReUpload = false;
				// 取消90秒
				mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);

				if (!isRequestedForServer) {
					// 没有请求过服务器
					if (!isContinueLive) {
						// 不是续播，才可以请求
						mLiveVideoHandler.sendEmptyMessage(101);
					}

				} else {
					LiveDialogManager.getManagerInstance().dismissProgressDialog();
				}

			} else {
				// 视频录制上传失败
				liveUploadVideoFailed();
			}
		}

		@Override
		public void onLiveRecordFailed(Context context, int nResult, String strResultInfo) {
			LogUtil.e("", "jyf------TTTTT------------onLiveRecordFailed----2222:" + nResult);
			liveUploadVideoFailed();
		}
	};

	private void setUserHeadImage(String headStr) {
		try {
			if (null != mHead && null != headStr && !"".equals(headStr)) {
				int utype = Integer.valueOf(headStr);
				int head = mHeadImg[utype];
				mHead.setBackgroundResource(head);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 直播上传失败
	private void liveUploadVideoFailed() {
		liveStopUploadVideo();
		if (isLiveUploadTimeOut) {
			mHandler.removeMessages(MSG_H_RETRY_UPLOAD);
			return;
		}

		if (isUploadSucessed) {

			if (!isTryingReUpload) {
				// 显示Loading
				if (!isAlreadExit) {
					LiveDialogManager.getManagerInstance().showProgressDialog(this, "提示", LIVE_RETRY_UPLOAD_MSG);
				}
			}

			// 计时，90秒后，提示用户上传失败
			mHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
			// 重新上传直播视频
			mHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 1000);

			isTryingReUpload = true;
		} else {
			LiveDialogManager.getManagerInstance().dismissProgressDialog();
			// 断开，提示用户是否继续上传
			if (!isAlreadExit) {
				LiveDialogManager.getManagerInstance().showTwoBtnDialog(this,
						LiveDialogManager.DIALOG_TYPE_LIVE_RELOAD_UPLOAD, LIVE_DIALOG_TITLE, LIVE_UPLOAD_FIRST_ERROR);
			}
		}
	}

	// 停止传视频直播
	private void liveStopUploadVideo() {
		try {
			CarRecorderManager.stopRTSPLive();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (!CarRecorderManager.isRTSPLiving()) {
				isStartLive = false;
				startLive(mCurrentVideoId);
				LogUtil.e("", "YYYYYY===onLiveRecordFailed=====222222====");
			}
		}
	};

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			if (!isStart) {
				isStart = true;
				CarRecorderManager.onStartRTSP(this);
			}
		} catch (RecorderStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(this, "LiveVideo");
		isSucessBind = true;
		registerReceiver(managerReceiver, new IntentFilter(CarRecorderManager.ACTION_RECORDER_MESSAGE));
		mApp.setTalkListener(this);
		if (!isShowPop) {
			isShowPop = true;
		}
		loginSucess();

	}

	// 初次进入
	private void switchView() {
		if (isShareLive) {
			// 直播
			mBottomLayout.setVisibility(View.VISIBLE);
		} else {
			// 看别人直播
			mBottomLayout.setVisibility(View.GONE);
		}
	}

	// 自己主动开启直播，更新UI
	private void switchShareTalkView(boolean isUserSetTalk) {
		if (isUserSetTalk) {
			mLiveTalk.setVisibility(View.VISIBLE);
		} else {
			mLiveTalk.setVisibility(View.GONE);
		}
	}

	private void initMap() {
		BaiduMapOptions options = new BaiduMapOptions();
		options.rotateGesturesEnabled(false); // 不允许手势
		options.overlookingGesturesEnabled(false);
		mMapView = new MapView(this, options);
		mMapView.setClickable(true);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mMapRootLayout.addView(mMapView, 0, params);

		mMapView.showZoomControls(false);
		mMapView.showScaleControl(false);
		mBaiduMap = mMapView.getMap();
		// 找开定位图层，可以显示我的位置小蓝点
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMapManage = new BaiduMapManage(this, mBaiduMap, "LiveVideo");
	}

	/**
	 * 加入爱滔客群组
	 * 
	 * @author jiayf
	 * @date Apr 2, 2015
	 */
	private void joinAitalkGroup() {

	}

	boolean isSetAudioMute = false;

	/**
	 * 视频播放初始化
	 */
	private void startVideoAndLive(String url) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----startVideoAndLive----url : " + url);
		if (null == mRPVPalyVideo) {
			return;
		}
		// 设置视频源
		if (isShareLive) {
			// 预览自己的图像
			mFilePath = VIEW_SELF_PLAY;
			if (null != mRPVPalyVideo) {
				mRPVPalyVideo.setDataSource(mFilePath);
				if (!isSetAudioMute) {
					mRPVPalyVideo.setAudioMute(true);
				}
				isSetAudioMute = true;
			}

		} else {
			mRPVPalyVideo.setDataSource(url);
			if (!isSetAudioMute) {
				if (isCanVoice) {
					mRPVPalyVideo.setAudioMute(false);
				} else {
					mRPVPalyVideo.setAudioMute(true);
				}
			}

			isSetAudioMute = true;

		}

		mRPVPalyVideo.start();

	}

	private void updateCountDown(String msg) {
		if (null != mLiveCountDownTv) {
			mLiveCountDownTv.setText(msg);
		}
	}

	private void drawPersonsHead() {
		LogUtil.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----1111111: ");
		try {
			String jsonMyPos = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Location,
					ILocationFn.LOCATION_CMD_GET_POSITION, "");
			LogUtil.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----2222: " + jsonMyPos);
			if (null != jsonMyPos) {
				BaiduPosition myPosition = JsonUtil.parseLocatoinJson(jsonMyPos);
				if (null != myPosition) {
					// 开始绘制我的位置

					if (mApp.isUserLoginSucess) {
						if (null == myInfo) {
							this.getMyInfo();
						}
						LogUtil.e(null, "jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head 333333: "
								+ myInfo.nickName);
						if (null != myInfo) {
							LogUtil.e(null,
									"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head4444444444: ");
							mCurrentLocationType = LOCATION_TYPE_HEAD;
							myInfo.lon = String.valueOf(myPosition.rawLon);
							myInfo.lat = String.valueOf(myPosition.rawLat);
							String drawTxt = JsonUtil.UserInfoToString(myInfo);
							mBaiduMapManage.addSinglePoint(drawTxt);
							LogUtil.e(null,
									"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head555555555: "
											+ drawTxt);
						}

						LogUtil.e(null, "jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head6666666666: ");

					} else {
						LogUtil.e(null,
								"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head777777777777777: ");
						mCurrentLocationType = LOCATION_TYPE_POINT;
						// 画小蓝点
						MyLocationData locData = new MyLocationData.Builder().accuracy((float) myPosition.radius)
								.direction(100).latitude(myPosition.rawLat).longitude(myPosition.rawLon).build();
						// 确认地图我的位置点是否更新位置
						mBaiduMap.setMyLocationData(locData);
					}

				} else {
					showToast("无法获取我的位置信息");
					LogUtil.e(null, "jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head8888888888: ");
				}
			} else {
				showToast("无法获取我的位置信息");
			}
			LogUtil.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----999999999999: ");
			if (isShareLive) {
				// 自己直播不再绘制其它人的点
				return;
			}
			if (null == currentUserInfo) {
				showToast("无法获取看直播人的经纬度");
				return;
			}
			LogUtil.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----AAAAAAAAAA  : "
					+ currentUserInfo.aid);
			mBaiduMapManage.addSinglePoint(JsonUtil.UserInfoToString(currentUserInfo));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(null, "jyf----20150406----LiveActivity----drawPersonsHead---BBBBBBBB-Exception : ");
		}

	}

	private void liveFailedStart(boolean isLive) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----liveFailedStart---- 直播回调失败: ");
		if (isLive) {
			startLiveFailed();
		} else {
			startLiveLookFailed();
		}
	}

	// 自己开启直播，返回接口
	public void callBack_LiveLookStart(boolean isLive, int success, Object param1, Object param2) {
		if (IPageNotifyFn.PAGE_RESULT_SUCESS != success) {
			liveFailedStart(isLive);
			return;
		}
		final String data = (String) param2;
		// 解析回调数据
		LiveDataInfo dataInfo = JsonUtil.parseLiveDataJson2(data);
		if (null == dataInfo) {
			liveFailedStart(isLive);
			return;
		}
		if (200 != dataInfo.code) {
			liveFailedStart(isLive);
			LiveDialogManager.getManagerInstance().dismissProgressDialog();
			LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_LIVE_OFFLINE, "提示", "直播已经结束");
			return;
		}
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		isKaiGeSucess = true;
		if (this.isShareLive) {
			// 开始视频，上传图片
			screenShoot();
		}
		startUploadMyPosition();

	}

	// 上报位置
	private void startUploadMyPosition() {
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StartUploadPosition, "");
		// Toast.makeText(this, "开始上报位置", Toast.LENGTH_LONG).show();
	}

	// 判断自己发起的直播是否有效
	private void liveCallBack_startLiveIsValid(int success, Object obj) {
		// 是自己的直播是否有效
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		if (1 != success) {
			mLiveVideoHandler.sendEmptyMessage(100);
			// showToast("需要重新开启直播");
			return;
		}
		final String data = (String) obj;
		LogUtil.e(null, "jyf----20150406----LiveActivity----liveCallBack_startLiveIsValid----222222 : " + data);
		// 数据成功
		liveData = JsonUtil.parseLiveDataJson(data);
		if (null == liveData) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----liveCallBack_startLiveIsValid----333333333 : ");
			mLiveVideoHandler.sendEmptyMessage(100);
			// showToast("需要重新开启直播");
			return;
		}

		if (200 != liveData.code) {
			// 视频无效下线
			// 弹设置框,重新发起直播
			mLiveVideoHandler.sendEmptyMessage(100);
			// showToast("需要重新开启直播");
		} else {
			// 上次的视频还有效,开始上传直播，调用上报位置
			startLive(mCurrentVideoId);
			startUploadMyPosition();

			this.isKaiGeSucess = true;
			mLiveCountSecond = liveData.restTime;

			mDescTv.setText(liveData.desc);

			mLiveManager.cancelTimer();
			// 开启timer开始计时
			updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
			mLiveManager.startTimer(mLiveCountSecond, true);

		}
	}

	/**
	 * 查看别人直播
	 * 
	 * @param obj
	 */
	public void LiveVideoDataCallBack(int success, Object obj) {
		console.log("视频直播数据返回--LiveVideoDataCallBack: success: " + success);

		LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----111 : " + success);
		if (isShareLive) {
			liveCallBack_startLiveIsValid(success, obj);
			return;
		}

		if (1 != success) {
			liveCallBackError(true);
			mHandler.sendEmptyMessageDelayed(MSG_H_RETRY_REQUEST_DETAIL, 4 * 1000);
			return;
		}
		final String data = (String) obj;
		LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----222222 : " + data);
		// 数据成功
		liveData = JsonUtil.parseLiveDataJson(data);
		if (null == liveData) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----333333333 : ");
			mHandler.sendEmptyMessageDelayed(MSG_H_RETRY_REQUEST_DETAIL, 4 * 1000);
			liveCallBackError(false);
			return;
		}
		LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----4444 : " + (String) obj);
		if (200 != liveData.code) {
			mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
			videoInValid();
			// 视频无效下线
			return;
		}
		LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----5555 : ");
		// mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		isCanVoice = liveData.voice.equals("1") ? true : false;
		this.isKaiGeSucess = true;
		mLiveCountSecond = liveData.restTime;

		mDescTv.setText(liveData.desc);

		if (1 == liveData.active) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----6666 : ");
			// 主动直播
			// showToast("看别人地址：" + liveData.playUrl);
			if (!mRPVPalyVideo.isPlaying()) {
				startVideoAndLive(liveData.playUrl);
			}
			// 开始直播
			String groupId = liveData.groupId;
			if (null == groupId || "".equals(groupId) || 0 >= groupId.length()) {
				// LogUtil.e(null,
				// "jyf----20150406----LiveActivity----LiveVideoDataCallBack----7777 : ");
				// showToast("对方不支持加入群组");
				// // 不支持加入群组
				// switchLookShareTalkView(true, false);
			} else {
				// showToast("加入对方的群组");
				//
				// mJoinGroupJson = JsonUtil.getJoinGroup(liveData.groupType,
				// liveData.membercount, liveData.title,
				// liveData.groupId, liveData.groupnumber);
				//
				// isSupportJoinGroup = true;
				//
				// if (mApp.isUserLoginSucess) {
				// // 调用爱滔客加入群组
				// LogUtil.e(null,
				// "jyf----20150406----LiveActivity----LiveVideoDataCallBack----8888 : 开始加入群组 :"
				// + mJoinGroupJson);
				// // 支持加入群组，显示对讲按钮
				// switchLookShareTalkView(true, true);
				//
				// joinAitalkGroup();
				// } else {
				//
				// }

			}
		} else {
			// 被动直播
			// switchLookShareTalkView(false, false);
		}
	}

	// 视频已经下线
	private void videoInValid() {
		LiveDialogManager.getManagerInstance().showSingleBtnDialog(LiveActivity.this,
				LiveDialogManager.DIALOG_TYPE_LIVE_OFFLINE, "提示", "该用户直播己结束，谢谢观看");
		mHandler.removeMessages(MSG_H_RETRY_REQUEST_DETAIL);
		mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		if (null != mLiveManager) {
			mLiveManager.cancelTimer();
		}
		mVideoLoading.setVisibility(View.GONE);
	}

	private void liveCallBackError(boolean isprompt) {
		if (isprompt) {
			Toast.makeText(this, "查看直播服务器返回数据异常", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onDestroy() {
		console.log("liveplay---onDestroy");
		if (null != mRPVPalyVideo) {
			mRPVPalyVideo.stopPlayback();
			mRPVPalyVideo.cleanUp();
			mRPVPalyVideo = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.live_back_btn:
			// 返回
			preExit();
			break;
		case R.id.live_refirsh_btn:
			if (this.isShareLive) {
				if (isSettingCallBack) {
					showDialog();
				}
			} else {
				showDialog();
			}
			break;
		case R.id.live_oklayout:
			if (this.isShareLive) {
				if (isSettingCallBack) {
					click_OK();
				}
			} else {
				click_OK();
			}

			break;
		case R.id.live_sharelayout:
			if (this.isShareLive) {
				if (isSettingCallBack) {
					click_share();
				}
			} else {
				click_share();
			}

			break;
		case R.id.live_qiangpai:
			pre_startTrimVideo();
			break;
		default:
			break;
		}
	}

	/**
	 * 点赞
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @param type
	 *            点赞类型：0.取消点赞 1.点赞
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean clickPraise(String channel, String videoid, String type) {
		String json = JsonCreateUtils.getClickPraiseRequestJson(channel, videoid, type);
		return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.SquareCmd_Req_Praise, json);
	}

	/**
	 * 举报
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @param reporttype
	 *            举报类型：1.色情低俗 2.谣言惑众 3.政治敏感 4.其他原因
	 * @return true:命令发送成功 false:失败
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public boolean report(String channel, String videoid, String reporttype) {
		String json = JsonCreateUtils.getReportRequestJson(channel, videoid, reporttype);
		return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.SquareCmd_Req_ReportUp, json);
	}

	private AlertDialog dialog = null;
	private AlertDialog ad = null;
	private AlertDialog confirmation = null;

	/**
	 * 弹出举报的窗口
	 * 
	 * @Title: showDialog
	 * @Description: TODO void
	 * @author 曾浩
	 * @throws
	 */
	public void showDialog() {
		dialog = new AlertDialog.Builder(LiveActivity.this).create();
		dialog.show();
		dialog.getWindow().setContentView(R.layout.video_square_dialog_main);
		dialog.getWindow().findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				ad = new AlertDialog.Builder(LiveActivity.this).create();
				ad.show();
				ad.getWindow().setContentView(R.layout.video_square_dialog_selected);
				ad.getWindow().findViewById(R.id.sqds).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						confirmation("1");
					}
				});
				ad.getWindow().findViewById(R.id.yyhz).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						confirmation("2");
					}
				});
				ad.getWindow().findViewById(R.id.zzmg).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						confirmation("3");
					}
				});
				ad.getWindow().findViewById(R.id.qtyy).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						confirmation("4");
					}
				});
				ad.getWindow().findViewById(R.id.qx).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ad.dismiss();
					}
				});
			}
		});

		dialog.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	public void confirmation(final String reporttype) {
		ad.dismiss();
		confirmation = new AlertDialog.Builder(LiveActivity.this).create();
		confirmation.show();
		confirmation.getWindow().setContentView(R.layout.video_square_dialog_confirmation);
		confirmation.getWindow().findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				boolean isSucess = report("1", getCurrentVideoId(), reporttype);
				if (isSucess) {
					showToast("举报成功,我们稍后会进行处理");
				} else {
					showToast("举报失败!");
				}
				confirmation.dismiss();
			}
		});
		confirmation.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmation.dismiss();
			}
		});
	}

	private void click_share() {
		String vid = null;
		if (isShareLive) {
			vid = mCurrentVideoId;

		} else {
			if (!isKaiGeSucess) {

				return;
			}
			vid = liveData.vid;
		}

		boolean isSucess = mVideoSquareManager.getShareUrl(vid, "1");
		if (!isSucess) {
			showToast("分享失败");
		} else {
			LiveDialogManager.getManagerInstance().showShareProgressDialog(this,
					LiveDialogManager.DIALOG_TYPE_LIVE_SHARE, "提示", "正在请求分享地址");
		}
	}

	private void loginSucess() {
		if (this.isShareLive) {
			return;
		}
		LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 111111");
		if (!mApp.isUserLoginSucess) {
			// 登录失败
			return;
		}
		LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 222222");
	}

	private void click_login() {
		// showToast("去登录界面");
		Intent intent = new Intent(this, UserLoginActivity.class);
		startActivity(intent);
	}

	private String getCurrentVideoId() {
		if (isShareLive) {
			return mCurrentVideoId;
		}
		if (null != liveData) {
			return liveData.vid;
		}
		return "";
	}

	private void click_OK() {
		if (!this.isKaiGeSucess) {
			return;
		}
		if (isAlreadClickOK) {
			// showToast("不能重复点赞");
			return;
		}
		Toast.makeText(this, "点赞", Toast.LENGTH_LONG).show();
		isAlreadClickOK = true;
		mLiveOk.setBackgroundResource(R.drawable.live_icon_heart);
		mCurrentOKCount++;
		if (null != mZancountTv) {
			mZancountTv.setText("" + mCurrentOKCount);
		}
		boolean isSucess = clickPraise("1", getCurrentVideoId(), "1");
		LogUtil.e(null, "jyf----20150406----LiveActivity----click_OK----isSucess : " + isSucess);
	}

	// 点赞接口回调
	public void callBack_clickOK(int success, Object param1, Object param2) {

	}

	/**
	 * 重连runnable
	 */
	private Runnable retryRunnable = new Runnable() {
		@Override
		public void run() {
			LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----retryRunnable--1111 : ");
			if (null != mRPVPalyVideo) {
				LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----retryRunnable--22222 : ");
				if (isShareLive) {
					LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----retryRunnable--3333 : ");
					mRPVPalyVideo.setDataSource(VIEW_SELF_PLAY);
					mRPVPalyVideo.start();

					LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----retryRunnable--44444 : ");
				} else {
					if (null != liveData) {
						mRPVPalyVideo.setDataSource(liveData.playUrl);
						mRPVPalyVideo.start();
					}
				}
			}
		}
	};

	@Override
	public void onPlayerPrepared(RtmpPlayerView arg0) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerPrepared : ");
		mRPVPalyVideo.setHideSurfaceWhilePlaying(true);
		if (!this.isShareLive) {
			mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		}
	}

	@Override
	public boolean onPlayerError(RtmpPlayerView rpv, int arg1, int arg2, String arg3) {
		// 视频播放出错
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerError : " + arg2 + "  " + arg3);
		playerError(rpv);
		// 加载画面
		// rpv.removeCallbacks(retryRunnable);
		// rpv.postDelayed(retryRunnable, 5000);
		return false;
	}

	@Override
	public void onPlayerCompletion(RtmpPlayerView rpv) {
		// 视频播放完成
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerCompletion : ");
		playerError(rpv);
		// rpv.removeCallbacks(retryRunnable);
		// rpv.postDelayed(retryRunnable, 5000);
	}

	@Override
	public void onPlayerBegin(RtmpPlayerView rpv) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerBegin : ");
		mVideoLoading.setVisibility(View.GONE);
		// mRPVPalyVideo.setVisibility(View.VISIBLE);
		// // mRPVPalyVideo.invalidate();
		// mRPVPalyVideo.postInvalidate();

		showPlayer();

		if (!isShareLive) {
			mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
			mLiveManager.cancelTimer();
			// 开启timer开始计时
			updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
			mLiveManager.startTimer(mLiveCountSecond, true);
		}
	}

	@Override
	public void onPlayBuffering(RtmpPlayerView arg0, boolean start) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayBuffering : " + start);
		if (start) {
			// 缓冲开始
			mHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
		} else {
			// 缓冲结束
			mVideoLoading.setVisibility(View.GONE);
		}
	}

	@Override
	public void onGetCurrentPosition(RtmpPlayerView arg0, int arg1) {
	}

	// 播放器错误
	private void playerError(RtmpPlayerView rpv) {
		if (isLiveUploadTimeOut) {
			// 90秒超时，直播结束
			return;
		}
		hidePlayer();
		if (isShareLive) {
			// UI需要转圈loading
			mHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
			// 重新加载播放器预览
			mHandler.removeMessages(MSG_H_RETRY_SHOW_VIEW);
			mHandler.sendEmptyMessageDelayed(MSG_H_RETRY_SHOW_VIEW, 5000);
		} else {
			// UI需要转圈
			mHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
			// 计时90秒
			mHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
			// 重新请求直播详情
			mHandler.sendEmptyMessage(MSG_H_RETRY_REQUEST_DETAIL);
		}
	}

	private void pre_startTrimVideo() {
		LogUtil.e(null, "jyf----20150406----LiveActivity----pre_startTrimVideo----11111 : ");
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----pre_startTrimVideo----2222 : " + isRecording);
			if (!isRecording) {
				// 设置按下状态
				mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_6s_press1);
				isRecording = true;
				mCurVideoType = VideoType.mounts;
				boolean isSucess = GolukApplication.getInstance().getIPCControlManager().startWonderfulVideo();

				LogUtil.e(null, "jyf----20150406----LiveActivity----pre_startTrimVideo----4444444 : " + isSucess);

				if (!isSucess) {
					videoTriggerFail();
				}
			}
		} else {
			LogUtil.e(null, "jyf----20150406----LiveActivity----pre_startTrimVideo----333333 : ");
			dialog();
			// 未登录

		}
	}

	private void qiangpaiState(boolean isCan) {
		if (isCan) {
			mQiangpaiImg.setBackgroundResource(R.drawable.btn_live_6s);
		} else {
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_6s1);
		}
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
		lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
		lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
		lp.leftMargin = 0;
		mVLayout.setLayoutParams(lp);
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
	 * 8s视频一键抢拍
	 * 
	 * @author xuhw
	 * @date 2015年3月4日
	 */
	private void startTrimVideo() {
		if (null != m8sTimer) {
			// 正在录制
			return;
		}
		mShootTime = 0;
		m8sTimer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				mShootTime++;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LogUtil.e("", "mShootTime-------: " + mShootTime);
						refre8second(mShootTime);
					}
				});
			}
		};
		m8sTimer.schedule(task, 500, 500);
	}

	private void refre8second(int mShootTime) {
		switch (mShootTime) {
		case 1:
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_6s_record);
			break;
		case 2:
			break;
		case 3:
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_5s_record);
			break;
		case 4:
			break;
		case 5:
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_4s_record);
			break;
		case 6:
			break;
		case 7:
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_3s_record);
			break;
		case 8:
			break;
		case 9:
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_2s_record);
			break;
		case 10:
			break;
		case 11:
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_1s_record);
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

	/**
	 * 停止８s视频操作
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void stopTrimVideo() {
		mHandler.sendEmptyMessageDelayed(MSG_H_QUERYFILEEXIT, QUERYFILETIME);

		mShootTime = 0;
		mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_6s_press1);
		if (null != m8sTimer) {
			m8sTimer.cancel();
			m8sTimer.purge();
			m8sTimer = null;
		}
	}

	private void freePlayer() {
		if (null != mRPVPalyVideo) {
			mRPVPalyVideo.removeCallbacks(retryRunnable);
			mRPVPalyVideo.cleanUp();
			mRPVPalyVideo = null;
		}
	}

	/**
	 * 退出直播或观看直播
	 * 
	 * @author jiayf
	 * @date Apr 2, 2015
	 */
	public void exit() {
		if (isAlreadExit) {
			return;
		}
		isAlreadExit = true;
		mApp.mSharedPreUtil.setIsLiveNormalExit(true);
		// 注册回调监听
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("live");
		mVideoSquareManager.removeVideoSquareManagerListener("live");
		// 移除监听
		mApp.removeLocationListener(TAG);
		mJoinGroupJson = null;

		mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		mHandler.removeMessages(MSG_H_RETRY_UPLOAD);
		mHandler.removeMessages(MSG_H_RETRY_SHOW_VIEW);
		mHandler.removeMessages(MSG_H_RETRY_REQUEST_DETAIL);
		mHandler.removeMessages(MSG_H_PLAY_LOADING);

		dissmissAllDialog();

		freePlayer();

		LiveDialogManager.getManagerInstance().setDialogManageFn(null);
		if (isShareLive) {
			// 如果是开启直播，则停止上报自己的位置
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StopUploadPosition,
					"");
			if (isKaiGeSucess) {
				// 如果没有开启直播，则不需要调用服务器的退出直播
				mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_LiveStop,
						JsonUtil.getStopLiveJson());
			}
			liveStopUploadVideo();

		} else {
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_CommCmd_QuitGroup, "");
		}

		if (isSucessBind) {
			unregisterReceiver(managerReceiver);
			isSucessBind = false;
		}
		if (null != mLiveManager) {
			mLiveManager.cancelTimer();
		}
		// 停止计时器
		stopTrimVideo();
		finish();
	}

	private void dissmissAllDialog() {
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		LiveDialogManager.getManagerInstance().dismissSingleBtnDialog();
	}

	/**
	 * 单个文件查询
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void queryFileExit() {
		LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----111111 : ");
		videoFileQueryTime++;
		mHandler.removeMessages(MSG_H_QUERYFILEEXIT);

		LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----222222 : ");

		if (!TextUtils.isEmpty(mRecordVideFileName)) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----333333 : ");
			if (videoFileQueryTime <= 15) {
				LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----444444 : ");
				if (GolukApplication.getInstance().getIpcIsLogin()) {
					LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----555555 : ");
					boolean isSucess = GolukApplication.getInstance().getIPCControlManager()
							.querySingleFile(mRecordVideFileName);

					LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----66666 : " + isSucess);

					if (!isSucess) {
						mHandler.sendEmptyMessageDelayed(MSG_H_QUERYFILEEXIT, 1000);
					}

					LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----777777 : ");

				} else {
					// IPC未登录
					LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----88888 : ");
				}
			} else {
				videoFileQueryTime = 0;
				videoTriggerFail();

				LogUtil.e(null, "jyf----20150406----LiveActivity----queryFileExit----999999 : ");
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
				mQiangpaiImg.setBackgroundResource(R.drawable.btn_live_6s);
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

	private void preExit() {
		String message = this.isShareLive ? LIVE_EXIT_PROMPT : LIVE_EXIT_PROMPT2;
		LiveDialogManager.getManagerInstance()
				.showLiveBackDialog(this, LiveDialogManager.DIALOG_TYPE_LIVEBACK, message);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----onKeyDown----111111 : ");
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			if (mliveSettingWindow.isShowing()) {
				// 直接退出
				mliveSettingWindow.close();
				this.exit();
			} else {
				preExit();
			}

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_H_SPEECH_OUT_TIME:
				mSpeechOutTime++;
				if (mSpeechOutTime >= 30) {
					talkrelease();
					if (mIsMe) {
						mTimeOutEnable = true;
						mSpeechCountDownTime = 3;
						mHandler.sendEmptyMessage(MSG_H_SPEECH_COUNT_DOWN);
					} else {
						mSpeechOutTime = 0;
					}
				} else {
					String str = "";
					if (mSpeechOutTime < 10) {
						str = "00:0" + mSpeechOutTime;
					} else {
						str = "00:" + mSpeechOutTime;
					}
					refreshTimerTv(str);
					mHandler.removeMessages(MSG_H_SPEECH_OUT_TIME);
					mHandler.sendEmptyMessageDelayed(MSG_H_SPEECH_OUT_TIME, 1000);
				}
				break;
			case MSG_H_SPEECH_COUNT_DOWN:
				if (mSpeechCountDownTime < 0) {
					mTalkTouchDown = false;
					mTimeOutEnable = false;
					mHandler.removeMessages(MSG_H_SPEECH_COUNT_DOWN);
					// 目前为可按状态
					refreshPPtState(true);
					mSpeakingLayout.setVisibility(View.GONE);

				} else {
					speekingUIRefresh(4, "", false);
					final String showTimeStr = "00:0" + mSpeechCountDownTime;
					refreshTimerTv(showTimeStr);
					mSpeechCountDownTime--;
					mHandler.removeMessages(MSG_H_SPEECH_COUNT_DOWN);
					mHandler.sendEmptyMessageDelayed(MSG_H_SPEECH_COUNT_DOWN, 1000);
				}
				break;
			case MSG_H_QUERYFILEEXIT:
				queryFileExit();
				break;
			case MOUNTS:
				startTrimVideo();
				break;
			case MSG_H_UPLOAD_TIMEOUT:
				// 上传视频超时，提示用户上传失败，退出程序
				isLiveUploadTimeOut = true;
				mLiveManager.cancelTimer();
				mVideoLoading.setVisibility(View.GONE);
				freePlayer();
				liveEnd();
				LiveDialogManager.getManagerInstance().dismissProgressDialog();
				LiveDialogManager.getManagerInstance().showSingleBtnDialog(LiveActivity.this,
						LiveDialogManager.DIALOG_TYPE_LIVE_TIMEOUT, LIVE_DIALOG_TITLE, LIVE_NET_ERROR);
				break;
			case MSG_H_RETRY_UPLOAD:
				// showToast("直播失败，重新上传视频");
				isStartLive = false;
				startLive(mCurrentVideoId);
				break;
			case MSG_H_RETRY_SHOW_VIEW:
				startVideoAndLive("");
				break;
			case MSG_H_RETRY_REQUEST_DETAIL:
				startLiveLook(currentUserInfo);
				break;
			case MSG_H_PLAY_LOADING:
				mVideoLoading.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int id = v.getId();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (this.isShareLive) {
				if (!isSettingCallBack) {
					return true;
				}
			}

			if (id == R.id.live_qiangpai) {
				if (null != m8sTimer) {
					// 正在录制
					return true;
				}
				// mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_8s_press);
				LogUtil.e(null, "mobile-----onTouch-----:  qiangpai down");
			} else if (id == R.id.live_exit) {
				LogUtil.e(null, "mobile-----onTouch-----:  exit down");
				mExitBtn.setBackgroundResource(R.drawable.live_btn_off_press);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (this.isShareLive) {
				if (!isSettingCallBack) {
					return true;
				}
			}
			if (id == R.id.live_qiangpai) {
				pre_startTrimVideo();
				// startTrimVideo();
				LogUtil.e(null, "mobile-----onTouch-----:  qiangpai up");
			} else if (id == R.id.live_exit) {
				LogUtil.e(null, "mobile-----onTouch-----:  exit up");
				preExit();
			}
			break;
		default:
			break;
		}
		return false;
	}

	private void talkrelease() {
		LogUtil.e(null, "jyf-------live------talkrelease: ----1111");
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk,
				ITalkFn.Talk_CommCmd_TalkRelease, "");

		LogUtil.e(null, "jyf-------live------talkrelease: ----2222: " + isSucess);
	}

	/**
	 * 更新PPT的状态，
	 * 
	 * @param isEnable
	 *            　true/false 目前可用/不可用
	 * @author jiayf
	 * @date Apr 2, 2015
	 */
	private void refreshPPtState(boolean isEnable) {
		if (isShareLive) {
			mLiveTalk.setEnabled(isEnable);
			if (isEnable) {
				mLiveTalk.setBackgroundResource(R.drawable.live_btn_ptt_normal);
			} else {
				mLiveTalk.setBackgroundResource(R.drawable.live_btn_ptt_disable);
			}
		} else {
			mLiveLookTalk.setEnabled(isEnable);
			if (isEnable) {
				mLiveLookTalk.setBackgroundResource(R.drawable.livelook_btn_ptt_normal);
			} else {
				mLiveLookTalk.setBackgroundResource(R.drawable.livelook_btn_ptt_disable);
			}
		}
	}

	// 更新定时内容
	private void refreshTimerTv(String content) {
		if (null != mTalkingTimeTv) {
			mTalkingTimeTv.setVisibility(View.VISIBLE);
			mTalkingTimeTv.setText(content);
		}
	}

	// 刷新说话中的状态, name表示说话的名字 isMe　表示是否是自己说话
	private void speekingUIRefresh(int event, String name, boolean isMe) {
		switch (event) {
		case 0:
			// 用户按下申请
			mSpeakingLayout.setVisibility(View.VISIBLE);
			mTalkingSign.setVisibility(View.VISIBLE);
			mTalkingSign.setImageResource(R.drawable.live_icon_ptt_yellow);
			mTalkingTv.setVisibility(View.VISIBLE);
			mTalkingTv.setTextColor(getResources().getColor(R.color.live_speaking));
			mTalkingTv.setText("准备中...");
			break;
		case MSG_SPEAKING_START_SPEAK:
			// 自己说话　或　其它人说话中
			mSpeakingLayout.setVisibility(View.VISIBLE);
			mTalkingSign.setVisibility(View.VISIBLE);
			mTalkingSign.setImageResource(R.drawable.live_icon_ptt_green);
			mTalkingTv.setVisibility(View.VISIBLE);
			mTalkingTv.setTextColor(getResources().getColor(R.color.live_speaking));
			if (isMe) {
				mTalkingTv.setText("说话中...");
			} else {
				mTalkingTv.setText(name + "说话中...");
			}
			break;
		case 2:
			// 自己说话结束
			mSpeakingLayout.setVisibility(View.GONE);
			mTalkingSign.setVisibility(View.GONE);
			mTalkingTv.setVisibility(View.GONE);
			mTalkingTimeTv.setVisibility(View.GONE);
			break;
		case MSG_SPEAKING_OTHER_END:
			// 别人说话结束
			mSpeakingLayout.setVisibility(View.GONE);
			mTalkingSign.setVisibility(View.GONE);
			mTalkingTv.setVisibility(View.GONE);
			mTalkingTimeTv.setVisibility(View.GONE);
			break;
		case MSG_SPEEKING_TIMEOUT:
			// 超时说话
			mSpeakingLayout.setVisibility(View.VISIBLE);
			mTalkingSign.setVisibility(View.VISIBLE);
			mTalkingSign.setImageResource(R.drawable.live_icon_ptt_red);
			mTalkingTv.setVisibility(View.VISIBLE);
			mTalkingTv.setTextColor(Color.RED);
			mTalkingTv.setText("超时禁用中，请稍后...");
			refreshPPtState(false);
			break;
		case MSG_SPEEKING_BUSY:
			mSpeakingLayout.setVisibility(View.VISIBLE);
			mTalkingSign.setVisibility(View.VISIBLE);
			mTalkingSign.setImageResource(R.drawable.live_icon_ptt_red);
			mTalkingTv.setVisibility(View.VISIBLE);
			mTalkingTv.setTextColor(Color.RED);
			mTalkingTv.setText("占线中，请稍候再试...");
			break;
		}

	}

	@Override
	public void TalkNotifyCallBack(int type, String data) {

	}

	// POPWindow回调操作
	@Override
	public void callBackPopWindow(int event, Object data) {
		if (LiveSettingPopWindow.EVENT_ENTER == event) {
			if (null != mliveSettingWindow) {
				mliveSettingWindow.close();
			}
			if (null == data) {
				showToast("用户设置出错");
				return;
			}
			mSettingData = (LiveSettingBean) data;
			// 通过用户的设置，判断用户是否支持对讲
			switchShareTalkView(false);
			mLiveCountSecond = mSettingData.duration;
			if (null != mDescTv && null != mSettingData.desc && !"".equals(mSettingData.desc)) {
				mDescTv.setText(mSettingData.desc);
			} else {
				mDescTv.setVisibility(View.GONE);
			}
			if (!isAlreadExit) {
				LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE,
						LIVE_START_PROGRESS_MSG);
			}

			// 开始视频上传
			startLive(mCurrentVideoId);
			updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
			// 开始计时
			mLiveManager.startTimer(mLiveCountSecond, true);

			isSettingCallBack = true;

			isGetingAddress = true;
			GetBaiduAddress.getInstance().searchAddress(Double.parseDouble(myInfo.lat), Double.parseDouble(myInfo.lon));
		}
	}

	/**
	 * 首页大头针数据返回
	 */
	public void pointDataCallback(int success, Object obj) {
		if (1 != success) {
			LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback type:  sucess:" + success);
			return;
		}

		final String str = (String) obj;

		LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback type111:  str：" + str);
		// String str =
		// "{\"code\":\"200\",\"state\":\"true\",\"info\":[{\"utype\":\"1\",\"aid\":\"1\",\"nickname\":\"张三\",\"lon\":\"116.357428\",\"lat\":\"39.93923\",\"picurl\":\"http://img2.3lian.com/img2007/18/18/003.png\",\"speed\":\"34公里/小时\"},{\"aid\":\"2\",\"utype\":\"2\",\"nickname\":\"李四\",\"lon\":\"116.327428\",\"lat\":\"39.91923\",\"picurl\":\"http://img.cool80.com/i/png/217/02.png\",\"speed\":\"342公里/小时\"}]}";
		try {
			JSONObject json = new JSONObject(str);
			// 请求成功
			JSONArray memberJsonArray = json.getJSONArray("info");

			UserInfo tempUserInfo = null;
			UserInfo tempMyInfo = null;

			int length = memberJsonArray.length();

			for (int i = 0; i < length; i++) {
				JSONObject tempObj = memberJsonArray.getJSONObject(i);
				String aid = tempObj.getString("aid");
				if (this.isShareLive) {
					if (aid.equals(myInfo.aid)) {
						// 我自己的信息
						tempMyInfo = JsonUtil.parseSingleUserInfoJson(tempObj);
						break;
					}
				} else {
					if (aid.equals(currentUserInfo.aid)) {
						tempUserInfo = JsonUtil.parseSingleUserInfoJson(tempObj);
						break;
					}
				}
			}

			if (this.isShareLive) {
				// 如果是我发起的直播,更新我的信息即可
				if (null == tempMyInfo) {
					return;
				}
				this.updateCount(Integer.parseInt(tempMyInfo.zanCount), Integer.parseInt(tempMyInfo.persons));

				LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback 3333333:  更新我自己的赞 zanCount："
						+ tempMyInfo.zanCount + "	permson:" + tempMyInfo.persons);
				return;
			}
			if (null == tempUserInfo) {
				// showToast("未找到用户信息");

				LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback type44444:  str：" + str);
				return;
			}

			LogUtil.e(null, "jyf----20150406----LiveActivity----pointDataCallback----aid  : " + tempUserInfo.aid
					+ " lon:" + tempUserInfo.lon + " lat:" + tempUserInfo.lat);

			mBaiduMapManage.updatePosition(tempUserInfo.aid, Double.parseDouble(tempUserInfo.lon),
					Double.parseDouble(tempUserInfo.lat));

			currentUserInfo.lat = tempUserInfo.lat;
			currentUserInfo.lon = tempUserInfo.lon;

			LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback type55555:  str：" + str);

			// 设置“赞”的人数，和观看人数
			this.updateCount(Integer.parseInt(tempUserInfo.zanCount), Integer.parseInt(tempUserInfo.persons));

			LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback type66666:  str：" + str);

			if (!isShareLive) {
				LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback type777777:  str：" + str);
				if (!isGetingAddress) {
					LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback type88888  str：" + str);
					isGetingAddress = true;
					GetBaiduAddress.getInstance().searchAddress(Double.parseDouble(currentUserInfo.lat),
							Double.parseDouble(currentUserInfo.lon));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(null, "jyf-------live----LiveActivity--pointDataCallback type999999:  Exception ：");
		}

	}

	// 对话框操作回调
	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		switch (dialogType) {
		case LiveDialogManager.DIALOG_TYPE_EXIT_LIVE:
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				// 按了退出按钮
				exit();
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVEBACK:
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				// 按了退出按钮
				exit();
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVE_TIMEOUT:
		case LiveDialogManager.DIALOG_TYPE_LIVE_OFFLINE:
			exit();
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVE_RELOAD_UPLOAD:
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				// OK
				// 重新上传
				mHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 1000);
				if (!isAlreadExit) {
					LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE, LIVE_CREATE);
				}
			} else if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {
				// Cancel
				exit();
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVE_REQUEST_SERVER:
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				// OK
				if (!isAlreadExit) {
					startLiveForServer();
					LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE, LIVE_CREATE);
				}
			} else if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {
				exit();
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVE_SHARE:
			if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {
				// 取消
				// showToast("取消分享");
				// boolean b =
				// mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				// IPageNotifyFn.PageType_Share, JsonUtil.getCancelJson());
			}
			break;
		}
	}

	private void liveEnd() {
		isLiveUploadTimeOut = true;
		mLiveManager.cancelTimer();
		mVideoLoading.setVisibility(View.GONE);
		freePlayer();
		if (isShareLive) {
			stopRTSPUpload();
			// 停止上报自己的位置
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StopUploadPosition,
					"");
			if (isKaiGeSucess) {
				// 调用服务器的退出直播
				mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_LiveStop,
						JsonUtil.getStopLiveJson());
			}
			liveStopUploadVideo();
		} else {
			// mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk,
			// ITalkFn.Talk_CommCmd_QuitGroup, "");
		}
	}

	// timer回调操作
	@Override
	public void CallBack_timer(int function, int result, int current) {
		if (isShareLive) {
			if (10 == function) {
				if (TimerManager.RESULT_FINISH == result) {
					// 计时器完成
					liveEnd();
					if (!isAlreadExit) {
						LiveDialogManager.getManagerInstance().showLiveExitDialog(LiveActivity.this, LIVE_DIALOG_TITLE,
								LIVE_TIME_END);
					}
				}
				LogUtil.e("aaaaaa", "-------------aaaaa-----stop------");

				// 直播功能
				updateCountDown(GolukUtils.secondToString(current));
			}
		} else {
			// 看别人直播
			if (10 == function) {
				if (TimerManager.RESULT_FINISH == result) {
					liveEnd();
					if (!isAlreadExit) {
						LiveDialogManager.getManagerInstance().showLiveExitDialog(LiveActivity.this, LIVE_DIALOG_TITLE,
								LIVE_TIME_END);
					}
				}
				updateCountDown(GolukUtils.secondToString(current));
			}
		}
	}

	private void baiduDrawMyPosition(double lon, double lat, double radius) {
		MyLocationData locData = new MyLocationData.Builder().accuracy((float) radius).direction(100).latitude(lat)
				.longitude(lon).build();
		// 确认地图我的位置点是否更新位置
		mBaiduMap.setMyLocationData(locData);
	}

	@Override
	public void LocationCallBack(String gpsJson) {
		if (isLiveUploadTimeOut) {
			// 不更新数据
			return;
		}
		LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack----0000  : ");
		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
		LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack----111  : ");

		if (null != location) {
			// LogUtil.e(null,
			// "jyf----20150406----LiveActivity----LocationCallBack----updatePositon  : ");
			if (mApp.isUserLoginSucess) {
				if (null == myInfo) {
					this.getMyInfo();
				}
				// LogUtil.e(null,
				// "jyf----20150406----LiveActivity----LocationCallBack---draw MY Head: "
				// + myInfo.nickName);
				if (null != myInfo) {
					if (LOCATION_TYPE_UNKNOW == this.mCurrentLocationType) {
						// 当前是未定位的,　直接画气泡

					} else if (LOCATION_TYPE_POINT == mCurrentLocationType) {
						// 当前画的是蓝点，需要清除掉蓝点，再画气泡

					} else {
						// 当前是画的气泡，直接更新气泡的位置即可
						// LogUtil.e(null,
						// "jyf----20150406----LiveActivity----LocationCallBack---lon:: "
						// + location.rawLon + "	lat:" + location.rawLat);
						mBaiduMapManage.updatePosition(myInfo.aid, location.rawLon, location.rawLat);
					}

					// 设置当前画的是头像
					mCurrentLocationType = LOCATION_TYPE_HEAD;

				}
			} else {
				baiduDrawMyPosition(location.rawLon, location.rawLat, location.radius);
			}
		}

		if (!isShareLive) {
			// 只有在直播界面时才获取自己的位置
			return;
		}

		LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack----565656  : ");

		if (!isGetingAddress) {
			// 调用百度的反地理编码
			LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack----57575757  : ");
			GetBaiduAddress.getInstance().searchAddress(location.rawLat, location.rawLon);
		}
	}

	@Override
	public void CallBack_BaiduGeoCoder(int function, Object obj) {
		isGetingAddress = false;
		if (null == obj) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----CallBack_BaiduGeoCoder----获取反地理编码  : " + (String) obj);
			return;
		}
		final String currentAddress = (String) obj;
		if (null != mAddressTv) {
			mAddressTv.setText(currentAddress);
		}
		// if (this.isShareLive) {
		// // 如果是自己直播，则直接更新地址
		// if (null != mAddressTv) {
		// mAddressTv.setText(currentAddress);
		// }
		// }
		LogUtil.e(null, "jyf----20150406----LiveActivity----CallBack_BaiduGeoCoder----获取反地理编码  : " + (String) obj);
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----IPCManage_CallBack----event  : " + event + " msg:" + msg);

		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			if (ConnectionStateMsg_Connected != msg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ipcIsOk = false;
						mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_6s1);
					}
				});
			}
		}

		if (ENetTransEvent_IPC_VDCP_CommandResp == event && IPC_VDCP_Msg_Init == msg && 0 == param1) {
			ipcIsOk = true;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mQiangpaiImg.setBackgroundResource(R.drawable.btn_live_6s);
				}
			});
		}

		if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
			callBack_VDCP(msg, param1, param2);
		}
	}

	/**
	 * 处理VDCP命令回调
	 * 
	 * @param msg
	 *            　命令id
	 * @param param1
	 *            ¨¨ 0:命令发送成功 非0:发送失败
	 * @param param2
	 *            命令对应的json字符串
	 * @author xuhw
	 * @date 2015年3月17日
	 */
	private void callBack_VDCP(int msg, int param1, Object param2) {
		LogUtils.d("m8sBtn===IPC_VDCPCmd_TriggerRecord===callBack_VDCP=====param1=   " + param1 + "     ==param2="
				+ param2 + "	msg:" + msg);
		switch (msg) {
		// 实时抓图
		case IPC_VDCPCmd_SnapPic:
			dealSnapCallBack(param1, param2);
			break;
		// 请求紧急、精彩视频录制
		case IPC_VDCPCmd_TriggerRecord:
			dealTriggerRecordCallBack(param1, param2);
			break;
		case IPC_VDCPCmd_SingleQuery:
			dealSingleFile(param1, param2);
			break;
		default:
			break;
		}
	}

	private void dealSingleFile(int param1, Object param2) {
		GFileUtils.writeIPCLog("===========IPC_VDCPCmd_SingleQuery===11111=========param1=" + param1 + "=====param2="
				+ param2);
		if (0 != param1) {
			mHandler.sendEmptyMessageDelayed(MSG_H_QUERYFILEEXIT, 1000);
			return;
		}
		VideoFileInfo fileInfo = IpcDataParser.parseSingleFileResult((String) param2);
		if (null == fileInfo) {
			mHandler.sendEmptyMessageDelayed(MSG_H_QUERYFILEEXIT, 1000);
			return;
		}
		if (!TextUtils.isEmpty(fileInfo.location)) {
			Intent mIntent = new Intent("sendfile");
			if (TYPE_SHORTCUT == fileInfo.type) {// 精彩
				mIntent.putExtra("filetype", "mounts");
				mIntent.putExtra("filename", fileInfo.location);

				String path = Environment.getExternalStorageDirectory() + File.separator + "tiros-com-cn-ext"
						+ File.separator + "video" + File.separator + "wonderful";
				wonderfulVideoName = path + File.separator + mRecordVideFileName;

				LogUtils.d("m8sBtn===IPC_VDCPCmd_TriggerRecord===callBack_VDCP=====param1=   查询文件成功");

			}

			mRecordVideFileName = "";
			videoFileQueryTime = 0;
			resetTrimVideoState();
		} else {
			mHandler.sendEmptyMessageDelayed(MSG_H_QUERYFILEEXIT, 1000);
		}

	}

	private void dealTriggerRecordCallBack(int param1, Object param2) {
		LogUtils.d("m8sBtn===IPC_VDCPCmd_TriggerRecord===4444=====param1=" + param1 + "==param2=" + param2);

		TriggerRecord record = IpcDataParser.parseTriggerRecordResult((String) param2);
		if (null != record) {
			if (0 == param1) {
				mRecordVideFileName = record.fileName;
				LogUtils.d("m8sBtn===IPC_VDCPCmd_TriggerRecord===555555========type=" + record.type);
				// 精彩视频
				if (TYPE_SHORTCUT == record.type) {
					mHandler.sendEmptyMessage(MOUNTS);
				}
			} else {
				videoTriggerFail();
			}
		} else {
			LogUtils.d("m8sBtn===IPC_VDCPCmd_TriggerRecord===6666====not success====");
			videoTriggerFail();
		}
	}

	private void dealSnapCallBack(int param1, Object param2) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----callBack_VDCP----接收图片命令回调");

		if (0 != param1) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----callBack_VDCP----接收图片命令失败-------");
			return;
		}
		// 文件路径格式：fs1:/IPC_Snap_Pic/snapPic.jpg
		String imageFilePath = (String) param2;

		if (TextUtils.isEmpty(imageFilePath)) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----callBack_VDCP----接收图片路径为空");
			return;
		}

		String uploadJson = JsonUtil.getUploadSnapJson(mCurrentVideoId, imageFilePath);
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_LiveUploadPic, uploadJson);

		LogUtil.e(null, "jyf----20150406----LiveActivity----callBack_VDCP----343434 startUpload Img:   " + isSucess);

		LogUtil.e(null, "jyf----20150406----LiveActivity----callBack_VDCP----333 imagePath:   " + imageFilePath);

		String path = FileUtils.libToJavaPath(imageFilePath);
		if (TextUtils.isEmpty(path)) {
			return;
		}

		LogUtil.e(null, "jyf----20150406----LiveActivity----callBack_VDCP----4444 path:   " + path);

		long time = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String timename = format.format(new Date(time));

		// 创建文件夹
		String dirname = Environment.getExternalStorageDirectory() + File.separator + "tiros-com-cn-ext"
				+ File.separator + "goluk" + File.separator + "screenshot";
		GFileUtils.makedir(dirname);

		String picName = dirname + File.separator + timename + ".jpg";
		// 保存原始图片
		String orgPicName = dirname + File.separator + "original_" + timename + ".jpg";

		LogUtil.e(null, "jyf----20150406----LiveActivity----callBack_VDCP----55555 picName:   " + picName);

		GFileUtils.copyFile(path, orgPicName);
		GFileUtils.compressImageToDisk(path, picName);

		File file = new File(picName);
		if (file.exists()) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----callBack_VDCP----接收图片命令成功------22222");
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == SquareCmd_Req_GetShareUrl) {
			// 销毁对话框
			LiveDialogManager.getManagerInstance().dismissShareProgressDialog();
			if (1 != msg) {
				showToast("分享失败");
				return;
			}
			try {
				JSONObject result = new JSONObject((String) param2);
				System.out.println("YYYY+RESULT00000000");
				if (!result.getBoolean("success")) {
					return;
				}
				JSONObject data = result.getJSONObject("data");
				String shareurl = data.getString("shorturl");
				String coverurl = data.getString("coverurl");
				String describe = "";
				if (!data.isNull("describe")) {
					describe = data.getString("describe");
				}
				if ("".equals(coverurl)) {
				}
				// 设置分享内容
				sharePlatform.setShareContent(shareurl, coverurl, describe);
				CustomShareBoard shareBoard = new CustomShareBoard(this);
				shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
