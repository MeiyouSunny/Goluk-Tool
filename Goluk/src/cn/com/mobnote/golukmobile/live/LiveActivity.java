package cn.com.mobnote.golukmobile.live;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserLoginActivity;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser.TriggerRecord;
import cn.com.mobnote.golukmobile.carrecorder.PreferencesReader;
import cn.com.mobnote.golukmobile.carrecorder.RecorderMsgReceiverBase;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress.IBaiduGeoCoderFn;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.live.LiveSettingPopWindow.IPopwindowFn;
import cn.com.mobnote.golukmobile.live.TimerManager.ITimerManagerFn;
import cn.com.mobnote.golukmobile.videosuqare.JsonCreateUtils;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.console;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.utils.LogUtil;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.ResultConstants;
import com.rd.car.player.RtmpPlayerView;

public class LiveActivity extends Activity implements OnClickListener, RtmpPlayerView.RtmpPlayerViewLisener,
		View.OnTouchListener, ITalkFn, IPopwindowFn, ILiveDialogManagerFn, ITimerManagerFn, ILocationFn,
		IBaiduGeoCoderFn, IPCManagerFn {

	private static final String TAG = "LiveActivity";
	/** 是否是直播 */
	public static final String KEY_IS_LIVE = "isLive";
	/** 要加入的群组ID */
	public static final String KEY_GROUPID = "groupID";
	/** 播放与直播地址 */
	public static final String KEY_PLAY_URL = "key_play_url";
	public static final String KEY_JOIN_GROUP = "key_join_group";
	public static final String KEY_USERINFO = "key_userinfo";
	public static final String KEY_LIVE_DATA = "key_livedata";
	public static final String KEY_LIVE_CONTINUE = "key_live_continue";

	final int[] shootImg = { R.drawable.live_btn_6s_record, R.drawable.live_btn_5s_record,
			R.drawable.live_btn_4s_record, R.drawable.live_btn_3s_record, R.drawable.live_btn_2s_record,
			R.drawable.live_btn_1s_record };

	final int[] mHeadImg = { 0, R.drawable.editor_boy_one, R.drawable.editor_boy_two, R.drawable.editor_boy_three,
			R.drawable.editor_girl_one, R.drawable.editor_girl_two, R.drawable.editor_girl_two, R.drawable.head_unknown };

	/** 视频上传地址 */
	private final String UPLOAD_VOIDE_PRE = "rtmp://goluk.8686c.com/live/";

	/** 自己预览地址 */
	private static final String VIEW_SELF_PLAY = "rtsp://admin:123456@192.168.43.234/sub";

	private static final int LOCATION_TYPE_UNKNOW = -1;
	private static final int LOCATION_TYPE_POINT = 0;
	private static final int LOCATION_TYPE_HEAD = 1;

	private final int DURATION_TIMEOUT = 90 * 1000;

	/** application */
	private GolukApplication mApp = null;
	/** 返回按钮 */
	private Button mLiveBackBtn = null;
	/** 刷新按钮 */
	private Button mRefirshBtn = null;
	/** 暂停按钮 */
	private Button mPauseBtn = null;
	/** title */
	private TextView mTitleTv = null;
	/** 当前地址 */
	private TextView mAddressTv = null;

	/** 点赞 显示 */
	private TextView mZancountTv = null;
	/** 观看人数 */
	private TextView mLookCountTv = null;
	private ImageView mLiveOk = null;;
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
	/** 直播录制定时器 */
	private Timer mRecordTimer = null;
	/** 直播录制时间 */
	private int curRecordTime = 0;
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
	/** 登录布局 */
	private RelativeLayout mLoginLayout = null;
	private Button mLoginBtn = null;
	/** 视频描述 */
	private TextView mDescTv = null;
	/** 分享 */
	private ImageView mShareImg = null;

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

	private boolean isKaiGeSucess = false;

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

	/** 8s视频 */
	public static final int MOUNTS = 114;
	/** 是否支持声音 */
	private boolean isCanVoice = true;
	private ImageView mHead = null;

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
			mLoginLayout.setVisibility(View.GONE);
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
			if (!mApp.isUserLoginSucess) {
				// 未登录成功
				mLoginLayout.setVisibility(View.VISIBLE);
			} else {
				mLoginLayout.setVisibility(View.GONE);
			}
			setUserHeadImage(currentUserInfo.head);
		}
		drawPersonsHead();
		// 加入爱滔客群组
		joinAitalkGroup();

		mliveSettingWindow = new LiveSettingPopWindow(this, mRootLayout);
		mliveSettingWindow.setCallBackNotify(this);

		if (isShareLive) {
			if (isContinueLive) {
				// TODO 续直播
				startLiveLook(myInfo);
				LiveDialogManager.getManagerInstance().showProgressDialog(this, "提示", "正在恢复上次直播");
			} else {
				// 显示设置窗口
				mLiveVideoHandler.sendEmptyMessageDelayed(100, 600);
			}

			updateCount(0, 0);
		} else {
			startLiveLook(currentUserInfo);
			updateCount(Integer.parseInt(currentUserInfo.zanCount), Integer.parseInt(currentUserInfo.persons));
		}

		// 在没有进入群组时，按钮不可按
		refreshPPtState(false);

		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		mLiveManager = new TimerManager(10);
		mLiveManager.setListener(this);

		mApp.addLocationListener(TAG, this);

		GetBaiduAddress.getInstance().setCallBackListener(this);
		// 注册回调监听
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("live", this);
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

	// 开启自己的直播,请求服务器 (在用户点击完设置后开始请求)
	private void startLiveForServer() {
		if (null == mApp) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----startLiveForServer----111 NULL: ");
		}
		if (null == mApp.mGoluk) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----startLiveForServer----2222 NULL: ");
		}
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_LiveStart, JsonUtil.getStartLiveJson(mCurrentVideoId, mSettingData));

		if (!isSucess) {
			startLiveFailed();
		} else {
			LiveDialogManager.getManagerInstance().setProgressDialogMessage("正在请求服务器...");
		}
	}

	// 查看他人的直播
	public void startLiveLook(UserInfo userInfo) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----startLiveLook----111 uid: " + userInfo.uid + " aid:"
				+ userInfo.aid);

		String condi = "{\"uid\":\"" + userInfo.uid + "\",\"desAid\":\"" + userInfo.aid + "\"}";

		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVideoDetail, condi);
		if (!isSucess) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----startLiveLook----22 : FASE False FAlse");
			startLiveLookFailed();
		} else {
			// TODO 弹对话框
			showToast("查看他人直播：" + userInfo.uid);
			LogUtil.e(null, "jyf----20150406----LiveActivity----startLiveLook----22 : TRUE TRUE");
		}
	}

	private void startLiveFailed() {
		// TODO 开启直接失败
		showToast("开启直播失败");
	}

	private void startLiveLookFailed() {
		// TODO 开启直接失败
	}

	/** IPC登录是否成功 */
	private boolean ipcIsOk = false;

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void initView() {
		mLiveBackBtn = (Button) findViewById(R.id.live_back_btn);
		mTitleTv = (TextView) findViewById(R.id.live_title);
		mRefirshBtn = (Button) findViewById(R.id.live_refirsh_btn);

		mTimeOutText = (TextView) findViewById(R.id.live_time_out_text);
		mVideoLoading = (RelativeLayout) findViewById(R.id.live_video_loading);
		mPlayLayout = (RelativeLayout) findViewById(R.id.live_play_layout);

		mZancountTv = (TextView) findViewById(R.id.live_okcount);
		mLookCountTv = (TextView) findViewById(R.id.live_lookcount);
		mLiveOk = (ImageView) findViewById(R.id.live_ok);
		mLiveOk.setOnClickListener(this);

		mHead = (ImageView) findViewById(R.id.live_userhead);

		mLiveCountDownTv = (TextView) findViewById(R.id.live_countdown);
		mDescTv = (TextView) findViewById(R.id.live_desc);

		mPauseBtn = (Button) findViewById(R.id.live_pause);
		mPauseBtn.setOnClickListener(this);

		mShareImg = (ImageView) findViewById(R.id.live_share);
		mShareImg.setOnClickListener(this);

		mBottomLayout = (RelativeLayout) findViewById(R.id.live_bottomlayout);
		mLiveLookTalk = (ImageButton) findViewById(R.id.livelook_ppt);
		mLiveTalk = (ImageButton) findViewById(R.id.live_ppt);
		mQiangPaiLayout = (LinearLayout) findViewById(R.id.live_qiangpai);
		mExitLayout = (LinearLayout) findViewById(R.id.live_exit);
		mExitBtn = (ImageView) findViewById(R.id.live_exit_btn);
		mQiangPaiLayout.setOnClickListener(this);
		mExitLayout.setOnTouchListener(this);
		mLiveLookTalk.setOnTouchListener(this);
		mLiveTalk.setOnTouchListener(this);

		mAddressTv = (TextView) findViewById(R.id.live_address);
		mTalkingTv = (TextView) findViewById(R.id.live_talking);
		mSpeakingLayout = (RelativeLayout) findViewById(R.id.live_speaklayout);
		mTalkingSign = (ImageView) findViewById(R.id.live_talking_sign);
		mTalkingTimeTv = (TextView) findViewById(R.id.live_talktime);

		mQiangpaiImg = (ImageView) findViewById(R.id.qiangpai_img);

		mLoginLayout = (RelativeLayout) findViewById(R.id.loginlayout);
		mLoginBtn = (Button) findViewById(R.id.live_login);
		mLoginBtn.setOnClickListener(this);
		// mQiangpaiImg.setOnClickListener(this);

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
				case 102:
					// 更新时间
					// updateCountDown((String) msg.obj);
					break;
				}
			}
		};
	}

	/**
	 * 响应视频Manager消息
	 */
	private BroadcastReceiver managerReceiver = new RecorderMsgReceiverBase() {
		@Override
		public void onManagerBind(Context context, int nResult, String strResultInfo) {
		}

		public void onLiveRecordBegin(Context context, int nResult, String strResultInfo) {
			String message;
			if (nResult >= ResultConstants.SUCCESS) {
				message = "onLiveRecordBegin　视频录制上传成功" + strResultInfo;
				showToast("视频上传成功");
				if (!isContinueLive) {
					mLiveVideoHandler.sendEmptyMessage(101);
					mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
				}
				LogUtil.e("", "jyf------TTTTT------------managerReceiver----1111:" + message);
			} else {
				message = "onLiveRecordBegin　视频录制上传失败 = " + strResultInfo;
				LogUtil.e("", "jyf------TTTTT------------managerReceiver----2222:" + message);
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
		mHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 3 * 1000);
		// 计时，90秒后，提示用户上传失败
		mHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
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
			mLiveLookTalk.setVisibility(View.GONE);
			mLiveTalk.setVisibility(View.GONE);
		} else {
			// 看别人直播
			mBottomLayout.setVisibility(View.GONE);
			mLiveLookTalk.setVisibility(View.GONE);
			mLiveTalk.setVisibility(View.GONE);
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

	// 看别人直播的UI更新
	// 看别人直播，分为　主动直播　与　被动直播
	// 主动直播，需要根据协议来区分对方是否支持对讲，被动直播不支持对讲
	private void switchLookShareTalkView(boolean isActiveLive, boolean isSupportTalk) {
		if (isActiveLive && isSupportTalk) {
			mLiveLookTalk.setVisibility(View.VISIBLE);
		} else {
			// 　被动直播
			mLiveLookTalk.setVisibility(View.GONE);
		}
	}

	private void initMap() {
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.live_bmapView);

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
		LogUtil.e(null, "jyf-------live------aitalk:join: " + mJoinGroupJson);
		if (null != mJoinGroupJson && !"".equals(mJoinGroupJson)) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----joinAitalkGroup----111 : " + mJoinGroupJson);

			final int cmd = isShareLive ? ITalkFn.Talk_CommCmd_JoinGroupWithInfo
					: ITalkFn.Talk_CommCmd_JoinGroupWithInfo;

			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, cmd, mJoinGroupJson);
		}

	}

	private void cancelTimer() {
		if (null != mRecordTimer) {
			mRecordTimer.cancel();
			mRecordTimer.purge();
			mRecordTimer = null;
		}
	}

	/**
	 * 视频播放初始化
	 */
	private void startVideoAndLive(String url) {

		// 设置视频源
		if (isShareLive) {
			// 预览自己的图像
			mFilePath = VIEW_SELF_PLAY;
			mRPVPalyVideo.setDataSource(mFilePath);
			mRPVPalyVideo.setAudioMute(true);
		} else {
			mRPVPalyVideo.setDataSource(url);
			if (isCanVoice) {
				mRPVPalyVideo.setAudioMute(false);
			} else {
				mRPVPalyVideo.setAudioMute(true);
			}
		}
		mRPVPalyVideo.start();
	}

	private void updateCountDown(String msg) {
		if (null != mLiveCountDownTv) {
			mLiveCountDownTv.setText(msg);
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
		liveVid = aid;

		LogUtil.e("", "jyf------TTTTT------------开始上传直播----1111 : " + aid);
		if (isStartLive) {
			return;
		}
		LogUtil.e("", "jyf------TTTTT------------开始上传直播----2222");
		if (CarRecorderManager.isRTSPLiving()) {
			LogUtil.e("", "jyf------TTTTT------------RTSP正在播放，不可以开始");
			showToast("RTSP正在直播，不可以开始");
			return;
		}
		try {
			LogUtil.e("", "jyf------TTTTT------------开始上传直播----3333");
			SharedPreferences sp = getSharedPreferences("CarRecorderPreferaces", Context.MODE_PRIVATE);
			// sp.edit().putString("url_live", "rtmp://211.103.234.234/live/" +
			// liveVid).apply();

			sp.edit().putString("url_live", UPLOAD_VOIDE_PRE + liveVid).apply();
			sp.edit().commit();
			CarRecorderManager.updateLiveConfiguration(new PreferencesReader(this).getConfig());
			if (null != mSettingData) {
				CarRecorderManager.setLiveMute(!mSettingData.isCanVoice);
			}
			LogUtil.e("", "jyf------TTTTT------------开始上传直播----44444444");
			CarRecorderManager.startRTSPLive();
			isStartLive = true;

			showToast("开始上传视频");

			LogUtil.e("", "jyf------TTTTT------------开始上传直播----555555");

		} catch (RecorderStateException e) {
			e.printStackTrace();
			showToast("上传视频报异常");
			this.liveUploadVideoFailed();
			LogUtil.e("", "jyf------TTTTT------------开始上传直播----666666666---报异常");
		}

	}

	private void stopRTSPUpload() {
		if (CarRecorderManager.isRTSPLiving()) {
			try {
				showToast("停止上传直播");
				isStartLive = false;
				CarRecorderManager.stopRTSPLive();
			} catch (RecorderStateException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置视频直播地址
	 * 
	 * @param data
	 */
	private void setVideoLiveUrl(JSONObject data) {
		try {
			JSONObject json = data.getJSONObject("data");
			// 请求成功
			// 视频直播流地址
			String vurl = json.getString("vurl");
			if (!"".equals(vurl) && null != vurl) {
				// 把视频直播流给到播放器
				mFilePath = vurl;
				// 视频初始化
				// videoInit();
			} else {
				// 获取图片数据
				String picUrl = json.getString("picurl");
				if (!"".equals(picUrl) && null != picUrl) {
					// 调用图片下载接口
				}
			}
			// 获取经纬度数据
			String lon = json.getString("lon");
			String lat = json.getString("lat");
			String head = json.getString("head");
			if (!"".equals(lon) && null != lon && !"".equals(lat) && null != lat) {
				// 添加地图大头针
				mBaiduMapManage.AddMapPoint(lon, lat, head);
				mBaiduMapManage.SetMapCenter(Double.parseDouble(lon), Double.parseDouble(lat));
			}
		} catch (Exception e) {

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

		if (null == dataInfo.groupId || "".equals(dataInfo.groupId)) {
			// 不支持对讲
			switchShareTalkView(false);
			return;
		}

		mJoinGroupJson = JsonUtil.getJoinGroup(dataInfo.groupType, dataInfo.membercount, dataInfo.title,
				dataInfo.groupId, dataInfo.groupnumber);

		if (null != mJoinGroupJson) {
			// 加入群组
			this.joinAitalkGroup();
		}
	}

	// 上报位置
	private void startUploadMyPosition() {
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StartUploadPosition, "");
		Toast.makeText(this, "开始上报位置", Toast.LENGTH_LONG).show();
	}

	LiveDataInfo liveData = null;

	// 判断自己发起的直播是否有效
	private void liveCallBack_startLiveIsValid(int success, Object obj) {
		// 是自己的直播是否有效
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		if (1 != success) {
			mLiveVideoHandler.sendEmptyMessage(100);
			showToast("需要重新开启直播");
			return;
		}
		final String data = (String) obj;
		LogUtil.e(null, "jyf----20150406----LiveActivity----liveCallBack_startLiveIsValid----222222 : " + data);
		// 数据成功
		liveData = JsonUtil.parseLiveDataJson(data);
		if (null == liveData) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----liveCallBack_startLiveIsValid----333333333 : ");
			mLiveVideoHandler.sendEmptyMessage(100);
			showToast("需要重新开启直播");
			return;
		}

		if (200 != liveData.code) {
			// 视频无效下线
			// 弹设置框,重新发起直播
			mLiveVideoHandler.sendEmptyMessage(100);
			showToast("需要重新开启直播");
		} else {
			// 上次的视频还有效,开始上传直播，调用上报位置
			startLive(mCurrentVideoId);
			startUploadMyPosition();
			if (null == liveData.groupId || "".equals(liveData.groupId)) {
				// 上次的直播不支持加入群組
				showToast("成功恢复直播, 不支持加入群组");
				mLiveTalk.setVisibility(View.GONE);
			} else {
				// 调用爱滔客加入群组
				mJoinGroupJson = JsonUtil.getJoinGroup(liveData.groupType, liveData.membercount, liveData.title,
						liveData.groupId, liveData.groupnumber);
				// 支持加入群组，显示对讲按钮
				mLiveTalk.setVisibility(View.VISIBLE);
				joinAitalkGroup();
				showToast("成功恢复直播, 加入群组");
			}

		}
	}

	private boolean isSupportJoinGroup = false;

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
			videoInValid();
			// 视频无效下线
			return;
		}
		LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----5555 : ");
		isCanVoice = liveData.voice.equals("1") ? true : false;
		this.isKaiGeSucess = true;
		mLiveCountSecond = liveData.restTime;

		mDescTv.setText(liveData.desc);

		if (1 == liveData.active) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----6666 : ");
			// 主动直播
			showToast("看别人地址：" + liveData.playUrl);

			startVideoAndLive(liveData.playUrl);

			// 开始直播
			String groupId = liveData.groupId;
			if (null == groupId || "".equals(groupId) || 0 >= groupId.length()) {
				LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----7777 : ");
				showToast("对方不支持加入群组");
				// 不支持加入群组
				switchLookShareTalkView(true, false);
			} else {
				showToast("加入对方的群组");

				mJoinGroupJson = JsonUtil.getJoinGroup(liveData.groupType, liveData.membercount, liveData.title,
						liveData.groupId, liveData.groupnumber);

				isSupportJoinGroup = true;

				if (mApp.isUserLoginSucess) {
					// 调用爱滔客加入群组
					LogUtil.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----8888 : 开始加入群组 :"
							+ mJoinGroupJson);
					// 支持加入群组，显示对讲按钮
					switchLookShareTalkView(true, true);

					joinAitalkGroup();
				} else {

				}

			}
		} else {
			// 被动直播
			switchLookShareTalkView(false, false);
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
			Toast.makeText(this, "查看别人直播服务器返回数据异常", Toast.LENGTH_LONG).show();
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
	protected void onPause() {
		console.log("liveplay---onPause");
		// mPlayLayout.setVisibility(View.VISIBLE);
		super.onPause();
	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.live_back_btn:
			// 返回
			preExit();
			break;
		case R.id.live_refirsh_btn:
			showDialog();
			break;
		case R.id.live_play_layout:
			// 继续观看
			mPlayLayout.setVisibility(View.GONE);
			mPauseBtn.setVisibility(View.VISIBLE);
			startLive(mCurrentVideoId);
			// 如果是开启直播，则停止上报自己的位置
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StopUploadPosition,
					"");

			if (null != mLiveManager) {
				mLiveManager.timerResume();
			}
			break;
		case R.id.live_ok:
			click_OK();
			break;
		case R.id.live_pause:
			Toast.makeText(this, "暂停", Toast.LENGTH_LONG).show();
			mPlayLayout.setVisibility(View.VISIBLE);
			mPauseBtn.setVisibility(View.GONE);

			if (null != mLiveManager) {
				mLiveManager.timerPause();
			}

			stopRTSPUpload();
			cancelTimer();
			// 如果是开启直播，则停止上报自己的位置
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StopUploadPosition,
					"");
			break;
		case R.id.live_login:
			click_login();
			break;
		case R.id.live_share:
			click_share();
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

				// 开启请求服务器举报

				// boolean flog =
				// GolukApplication.getInstance().getVideoSquareManager().report("1",
				// mVideoSquareInfo.mVideoEntity.videoid, reporttype);
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
		showToast("分享");
		// boolean isSucess =
		// mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
		// IPageNotifyFn.PageType_LiveShare, param);
	}

	private void loginSucess() {
		if (this.isShareLive) {
			return;
		}

		LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 111111");

		if (mApp.isUserLoginSucess) {
			// 登录成功

			LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 222222");
			mLoginLayout.setVisibility(View.GONE);

			if (!mIsJoinGroupSucess) {
				LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 3333333");
				if (isKaiGeSucess) {
					LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 4444444");
					if (this.isSupportJoinGroup) {
						LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 5555555");
						// 支持加入群組
						// 支持加入群组，显示对讲按钮
						switchLookShareTalkView(true, true);
						joinAitalkGroup();

						LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 6666666");
					}
				} else {
					LogUtil.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 7777777");
					startLiveLook(currentUserInfo);
				}
			}
		}

	}

	private void click_login() {
		showToast("去登录界面");
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
			showToast("不能重复点赞");
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
			if (null != mRPVPalyVideo) {
				if (isShareLive) {
					mRPVPalyVideo.setDataSource(VIEW_SELF_PLAY);
					mRPVPalyVideo.start();
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
	}

	@Override
	public boolean onPlayerError(RtmpPlayerView rpv, int arg1, int arg2, String arg3) {
		// 视频播放出错
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerError : " + arg2 + "  " + arg3);
		playerError();
		// 加载画面
		rpv.removeCallbacks(retryRunnable);
		rpv.postDelayed(retryRunnable, 5000);
		return false;
	}

	@Override
	public void onPlayerCompletion(RtmpPlayerView rpv) {
		// 视频播放完成
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerCompletion : ");
		playerError();
		rpv.removeCallbacks(retryRunnable);
		rpv.postDelayed(retryRunnable, 5000);
	}

	@Override
	public void onPlayerBegin(RtmpPlayerView rpv) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerBegin : ");
		mVideoLoading.setVisibility(View.GONE);
		mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		if (!isShareLive) {
			mLiveManager.cancelTimer();
			// 开启timer开始计时
			updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
			mLiveManager.startTimer(mLiveCountSecond, true);
		}
	}

	@Override
	public void onPlayBuffering(RtmpPlayerView arg0, boolean start) {
		LogUtil.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayBuffering : ");
		if (start) {
			// 缓冲开始
		} else {
			// 缓冲结束
		}
	}

	@Override
	public void onGetCurrentPosition(RtmpPlayerView arg0, int arg1) {
	}

	// 播放器错误
	private void playerError() {
		if (this.isShareLive) {
			// 重新加载播放器预览
			mHandler.sendEmptyMessage(MSG_H_RETRY_SHOW_VIEW);
			// UI需要转圈
			mHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
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
			// SoundUtils.getInstance().play(SoundUtils.RECORD_SEC);
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_6s_record);
			break;
		case 2:

			break;
		case 3:
			// SoundUtils.getInstance().play(SoundUtils.RECORD_SEC);
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_5s_record);
			break;
		case 4:

			break;
		case 5:
			// SoundUtils.getInstance().play(SoundUtils.RECORD_SEC);
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_4s_record);
			break;
		case 6:

			break;
		case 7:
			// SoundUtils.getInstance().play(SoundUtils.RECORD_SEC);
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_3s_record);
			break;
		case 8:

			break;
		case 9:
			// SoundUtils.getInstance().play(SoundUtils.RECORD_SEC);
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_2s_record);
			break;
		case 10:

			break;
		case 11:
			// SoundUtils.getInstance().play(SoundUtils.RECORD_SEC);
			mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_1s_record);
			break;
		case 13:
			// SoundUtils.getInstance().play(SoundUtils.RECORD_CAMERA);
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
		mApp.mSharedPreUtil.setIsLiveNormalExit(true);
		// 注册回调监听
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("live");
		// 移除监听
		mApp.removeLocationListener(TAG);
		mJoinGroupJson = null;
		isAlreadExit = true;

		mHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		mHandler.removeMessages(MSG_H_RETRY_UPLOAD);
		mHandler.removeMessages(MSG_H_RETRY_SHOW_VIEW);
		mHandler.removeMessages(MSG_H_RETRY_REQUEST_DETAIL);
		mHandler.removeMessages(MSG_H_PLAY_LOADING);

		if (null != mRPVPalyVideo) {
			mRPVPalyVideo.removeCallbacks(retryRunnable);
			mRPVPalyVideo.cleanUp();
			mRPVPalyVideo = null;
		}

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
		String message = this.isShareLive ? "您当前正在直播中，是否退出直播？" : "是否退出观看直播?";
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

	/** 开启超时记录定时器 */
	private static final int MSG_H_SPEECH_OUT_TIME = 1;
	/** 对讲倒计时定时器 */
	private static final int MSG_H_SPEECH_COUNT_DOWN = 2;
	/** 定时查询录制视频文件是否存在 */
	public static final int MSG_H_QUERYFILEEXIT = 3;
	/** 视频上传失败 */
	public static final int MSG_H_UPLOAD_TIMEOUT = 4;
	/** 重新上传视频 */
	public static final int MSG_H_RETRY_UPLOAD = 5;
	/** 重新加载预览界面 */
	public static final int MSG_H_RETRY_SHOW_VIEW = 6;
	/** 重新请求看别人详情 */
	public static final int MSG_H_RETRY_REQUEST_DETAIL = 7;
	/** 播放器错误里，UI需要更新 */
	public static final int MSG_H_PLAY_LOADING = 8;
	/** 文件查询时间 */
	public static final int QUERYFILETIME = 500;

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
				LiveDialogManager.getManagerInstance().dismissProgressDialog();
				LiveDialogManager.getManagerInstance().showSingleBtnDialog(LiveActivity.this,
						LiveDialogManager.DIALOG_TYPE_LIVE_TIMEOUT, "提示", "直播网络异常");
				break;
			case MSG_H_RETRY_UPLOAD:
				showToast("直播失败，重新上传视频");
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
			} else if (id == R.id.livelook_ppt || id == R.id.live_ppt) {
				if (mIsJoinGroupSucess) {

				} else {

				}
				pptTouchDown();
			}
			break;
		case MotionEvent.ACTION_UP:
			if (id == R.id.live_qiangpai) {
				pre_startTrimVideo();
				// startTrimVideo();
				LogUtil.e(null, "mobile-----onTouch-----:  qiangpai up");
			} else if (id == R.id.live_exit) {
				LogUtil.e(null, "mobile-----onTouch-----:  exit up");
				preExit();
			} else if (id == R.id.livelook_ppt || id == R.id.live_ppt) {
				pptTouchUp();
			}
			break;
		default:
			break;
		}
		return false;
	}

	private void pptTouchDown() {
		if (mTimeOutEnable) {
			// 用户上次说话超时，此时不能按下按钮
			return;
		}
		if (isShareLive) {
			mLiveTalk.setBackgroundResource(R.drawable.live_btn_ptt_press);
		} else {
			mLiveLookTalk.setBackgroundResource(R.drawable.livelook_btn_ptt_press);
		}

		speekingUIRefresh(0, "", false);

		LogUtil.e(null, "jyf-------live------pptTouchDown type: ----1111");

		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk,
				ITalkFn.Talk_CommCmd_TalkRequest, "");

		LogUtil.e(null, "jyf-------live------pptTouchDown type: ----2222: " + isSucess);

	}

	private void pptTouchUp() {
		if (mTimeOutEnable) {
			// 用户上次说话超时，此时不处理任何事件
			return;
		}

		if (null != mSpeakName && !"".equals(mSpeakName)) {
			// 当前有人说话
			speekingUIRefresh(MSG_SPEAKING_START_SPEAK, mSpeakName, false);
		} else {
			speekingUIRefresh(2, "", false);
		}
		if (isShareLive) {
			mLiveTalk.setBackgroundResource(R.drawable.live_btn_ptt_normal);
		} else {
			mLiveLookTalk.setBackgroundResource(R.drawable.livelook_btn_ptt_normal);
		}

		talkrelease();
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

	/** 开始说话 */
	private final int MSG_SPEAKING_START_SPEAK = 1;
	/** 其它人说话结束 */
	private final int MSG_SPEAKING_OTHER_END = 3;
	/** 说话超时 */
	private final int MSG_SPEEKING_TIMEOUT = 4;
	private final int MSG_SPEEKING_BUSY = 5;

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
		LogUtil.e(null, "jyf-------live------TalkNotifyCallBack type: " + type + "  data:" + data);
		int state = -1;
		switch (type) {
		case EVENT_AID:
			state = JsonUtil.getJsonIntValue(data, "state", -1);
			loginAndHeartEvent(state);
			break;
		case Talk_Event_ChanleIn:
			// 进入频道相关
			state = JsonUtil.getJsonIntValue(data, "state", -1);
			intoChannelEvent(state, data);
			break;
		case Talk_Event_ChanleInterAction:
			// 频道内交互事件
			state = JsonUtil.getJsonIntValue(data, "state", -1);
			channelInteractionEvent(state, data);
			break;
		default:
			break;
		}
	}

	/**
	 * 登录及心跳相关事件
	 * 
	 * @param event
	 * @author qianwei
	 * @date 2014/04/08
	 */
	private void loginAndHeartEvent(int event) {
		switch (event) {
		case -1:
			// 网络掉线
			refreshPPtState(false);
			break;
		case 0:
			// 正在获取aid及其配置参数 UI--弹出提示框
			break;
		case 1:
			/** 获取aid及其配置参数成功 */
			break;
		case 2:
			/** 获取aid及其配置参数失败 */
			break;
		case 3:
			// 正在登录爱淘客
			break;
		case 4:
			// 登录爱淘客成功
			mIsAirtalkLoginSucess = true;
			break;
		case 6:// 登录爱淘客失败
			break;
		case 7:// 用户重复登录爱淘客
			break;
		case 9:
			// aid登录中
			break;
		case 10:
			// aid超时
			break;
		}
	}

	/**
	 * 进入频道相关事件
	 * 
	 * @param event
	 * 
	 * @author qianwei
	 * @date 2014/04/08
	 */
	private void intoChannelEvent(int event, String message) {
		switch (event) {
		case 0:// 正在获取频道信息
			LogUtil.e(null, "jyf-------live------TalkintoChannelEvent type: 正在获取频道信息");
			break;
		case 1:// 获取频道信息成功
			mCurrentGroupInfo = message;
			break;
		case 2:// 获取频道信息失败
			break;
		case 3:// 正在进入爱淘客频道
			break;
		case 4:// 进入爱淘客频道成功
			callBack_JoinGroupSucess();
			break;
		case 5:// 自动重新进入爱淘客频道成功
			mLinkEnable = false;
			// 改为说话可用,更新UI
			break;
		case 6:// 进入爱淘客频道失败
			mIsJoinGroupSucess = false;
			break;
		case 7:// 频道退出
			break;
		}
	}

	/**
	 * 频道内交互事件
	 * 
	 * @param event
	 * 
	 * @author qianwei
	 * @date 2014/04/08
	 */
	private void channelInteractionEvent(int event, String message) {
		switch (event) {
		case 0:
			// 有人开始说话
			callBack_startSpeak(message);
			break;
		case 1:
			// 有人结束说话
			callBack_endSpeak(message);
			break;
		case 2:// 本人说话请求被拒绝
		case 3:// 本人说话请求正在排队
				// 本人说话请求被拒绝
			// 战线中
			speekingUIRefresh(MSG_SPEEKING_BUSY, "", false);
			break;
		case 4:// 本人说话请求状态错误
				// 说话请求状态错误
			// 占线中
			speekingUIRefresh(MSG_SPEEKING_BUSY, "", false);
			break;
		}
	}

	/**
	 * 加入群组成功后的处理
	 * 
	 * @author jiayf
	 * @date Apr 13, 2015
	 */
	private void callBack_JoinGroupSucess() {
		mIsJoinGroupSucess = true;
		showToast("加入群组成功");
		// 加入群组成功后的对讲按钮的变化
		if (this.isShareLive) {
			if (null != mSettingData && mSettingData.isCanTalk) {
				// 可以支持对讲
				// 对讲按钮可以显示
				mBottomLayout.setVisibility(View.VISIBLE);
				mLiveTalk.setVisibility(View.VISIBLE);
				mLiveLookTalk.setVisibility(View.GONE);
			} else {
				mBottomLayout.setVisibility(View.VISIBLE);
				mLiveTalk.setVisibility(View.GONE);
				mLiveLookTalk.setVisibility(View.GONE);
			}
		} else {
			mBottomLayout.setVisibility(View.GONE);
			mLiveTalk.setVisibility(View.GONE);
			mLiveLookTalk.setVisibility(View.VISIBLE);
		}

		// 按钮可以按下
		refreshPPtState(true);
	}

	// 有人开始说话
	private void callBack_startSpeak(String message) {
		mIsMe = JsonUtil.getJsonBooleanValue(message, "isme", false);
		mSpeakName = JsonUtil.getJsonStringValue(message, "name", "");
		final String aid = JsonUtil.getJsonStringValue(message, "aid", "");

		if (mIsMe) {
			mSpeakName = "";
		}
		mSpeechOutTime = 0;
		speekingUIRefresh(MSG_SPEAKING_START_SPEAK, mSpeakName, mIsMe);

		refreshTimerTv("00:00");
		// 不管是其它人　还是　自己，开始说话，就倒计时30秒
		mHandler.removeMessages(MSG_H_SPEECH_OUT_TIME);
		mHandler.sendEmptyMessageDelayed(MSG_H_SPEECH_OUT_TIME, 1000);

	}

	// 有人结束说话
	private void callBack_endSpeak(String message) {
		String aidEnd = JsonUtil.getJsonStringValue(message, "aid", null);
		boolean isMeEnd = JsonUtil.getJsonBooleanValue(message, "isme", false);
		mSpeakName = "";
		mIsMe = false;
		if (isMeEnd) {
			mSpeechOutTime = 0;
			refreshTimerTv("00:00");
			speekingUIRefresh(MSG_SPEAKING_OTHER_END, "", true);
		} else {
			speekingUIRefresh(MSG_SPEAKING_OTHER_END, "", false);
		}

		mHandler.removeMessages(MSG_H_SPEECH_OUT_TIME);
	}

	/** 用户设置数据 */
	LiveSettingBean mSettingData = null;

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
			switchShareTalkView(mSettingData.isCanTalk);
			mLiveCountSecond = mSettingData.duration;
			if (null != mDescTv && null != mSettingData.desc && !"".equals(mSettingData.desc)) {
				mDescTv.setText(mSettingData.desc);
			} else {
				mDescTv.setVisibility(View.GONE);
			}
			LiveDialogManager.getManagerInstance().showProgressDialog(this, "提示", "正在上传视频，请稍候");
			// 在没有进入群组时，按钮不可按
			refreshPPtState(false);
			// 开始视频上传
			// 计时，90秒后，提示用户上传失败
			mHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
			startLive(mCurrentVideoId);
			updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
			// 开始计时
			mLiveManager.startTimer(mLiveCountSecond, true);
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
				return;
			}

			LogUtil.e(null, "jyf----20150406----LiveActivity----pointDataCallback----aid  : " + tempUserInfo.aid
					+ " lon:" + tempUserInfo.lon + " lat:" + tempUserInfo.lat);

			mBaiduMapManage.updatePosition(tempUserInfo.aid, Double.parseDouble(tempUserInfo.lon),
					Double.parseDouble(tempUserInfo.lat));

			currentUserInfo.lat = tempUserInfo.lat;
			currentUserInfo.lon = tempUserInfo.lon;

			// 设置“赞”的人数，和观看人数
			this.updateCount(Integer.parseInt(tempUserInfo.zanCount), Integer.parseInt(tempUserInfo.persons));

			if (!isShareLive) {
				if (!isGetingAddress) {
					isGetingAddress = true;
					GetBaiduAddress.getInstance().searchAddress(Double.parseDouble(currentUserInfo.lat),
							Double.parseDouble(currentUserInfo.lon));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 对话框操作回调
	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		switch (dialogType) {
		case LiveDialogManager.DIALOG_TYPE_EXIT_LIVE:
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				// 按了退出按钮
				this.exit();
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVEBACK:
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				// 按了退出按钮
				this.exit();
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVE_TIMEOUT:
		case LiveDialogManager.DIALOG_TYPE_LIVE_OFFLINE:
			exit();
			break;
		}
	}

	// timer回调操作
	@Override
	public void CallBack_timer(int function, int result, int current) {
		if (isShareLive) {
			if (10 == function) {
				if (TimerManager.RESULT_FINISH == result) {
					// 计时器完成
					stopRTSPUpload();
					LiveDialogManager.getManagerInstance().showLiveExitDialog(LiveActivity.this, "直播结束");
					// 停止上报功能
					mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk,
							ITalkFn.Talk_Command_StopUploadPosition, "");
				}
				LogUtil.e("aaaaaa", "-------------aaaaa-----stop------");

				// 直播功能
				updateCountDown(GolukUtils.secondToString(current));
			}
		} else {
			// 看别人直播
			if (10 == function) {
				if (TimerManager.RESULT_FINISH == result) {
					showToast("查看别人直播结束");
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
		LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack----0000  : ");
		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
		LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack----111  : ");

		if (null != location) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack----updatePositon  : ");
			if (mApp.isUserLoginSucess) {
				if (null == myInfo) {
					this.getMyInfo();
				}
				LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack---draw MY Head: "
						+ myInfo.nickName);
				if (null != myInfo) {
					if (LOCATION_TYPE_UNKNOW == this.mCurrentLocationType) {
						// 当前是未定位的,　直接画气泡

					} else if (LOCATION_TYPE_POINT == mCurrentLocationType) {
						// 当前画的是蓝点，需要清除掉蓝点，再画气泡

					} else {
						// 当前是画的气泡，直接更新气泡的位置即可
						LogUtil.e(null, "jyf----20150406----LiveActivity----LocationCallBack---lon:: "
								+ location.rawLon + "	lat:" + location.rawLat);
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

		if (!isGetingAddress) {
			// 调用百度的反地理编码
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
		if (this.isShareLive) {
			// 如果是自己直播，则直接更新地址
			if (null != mAddressTv) {
				mAddressTv.setText(currentAddress);
			}
		}
		LogUtil.e(null, "jyf----20150406----LiveActivity----CallBack_BaiduGeoCoder----获取反地理编码  : " + (String) obj);
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			if (ConnectionStateMsg_Connected != msg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ipcIsOk = false;
						mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_6s_press1);
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

	/** 精彩视频名称 */
	private String wonderfulVideoName = null;

	private void dealSingleFile(int param1, Object param2) {
		GFileUtils.writeIPCLog("===========IPC_VDCPCmd_SingleQuery===11111=========param1=" + param1 + "=====param2="
				+ param2);
		if (RESULE_SUCESS != param1) {
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
			if (RESULE_SUCESS == param1) {
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

		if (RESULE_SUCESS != param1) {
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

			// TachographApplication.getInstance().uploadPicture(
			// picName);
		} else {

		}
	}
}
