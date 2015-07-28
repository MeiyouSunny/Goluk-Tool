package cn.com.mobnote.golukmobile.live;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.entity.LngLat;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.golukmobile.carrecorder.PreferencesReader;
import cn.com.mobnote.golukmobile.carrecorder.RecorderMsgReceiverBase;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.live.TimerManager.ITimerManagerFn;
import cn.com.mobnote.golukmobile.videosuqare.JsonCreateUtils;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.umeng.widget.CustomShareBoard;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.ResultConstants;
import com.rd.car.player.RtmpPlayerView;
import com.umeng.socialize.sso.UMSsoHandler;

public class LiveActivity extends BaseActivity implements OnClickListener, RtmpPlayerView.RtmpPlayerViewLisener,
		ILiveDialogManagerFn, ITimerManagerFn, ILocationFn, IPCManagerFn, ILive, VideoSuqareManagerFn {

	/** 自己预览地址 */
	private static String VIEW_SELF_PLAY = "";
	/** application */
	private GolukApplication mApp = null;
	/** 返回按钮 */
	private TextView mLiveBackBtn = null;
	/** 暂停按钮 */
	private Button mPauseBtn = null;
	/** title */
	private TextView mTitleTv = null;
	/** 视频loading */
	private RelativeLayout mVideoLoading = null;
	/** 播放布局 */
	private RelativeLayout mPlayLayout = null;
	/** 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private BaiduMapManage mBaiduMapManage = null;
	/** 自定义播放器支持特效 */
	public RtmpPlayerView mRPVPalyVideo = null;
	/** 视频地址 */
	private String mFilePath = "";
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
	/** 视频描述 */
	private TextView mDescTv = null;
	/** 观看人数 */
	private TextView mLookCountTv = null;
	private ImageView mMoreImg = null;
	private UserInfo currentUserInfo = null;
	/** 我的登录信息 如果未登录，则为空 */
	private UserInfo myInfo = null;
	private boolean isStart = false;
	/** 是否续直播 */
	private boolean isContinueLive = false;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
	private boolean isShowPop = false;
	private boolean isSucessBind = false;
	/** 是否已经点过“赞” */
	private boolean isAlreadClickOK = false;
	private TimerManager mLiveManager = null;
	/** 防止多次按退出键 */
	private boolean isAlreadExit = false;
	private String mCurrentVideoId = null;
	/** -1/0/1 未定位/小蓝点/气泡 */
	private int mCurrentLocationType = LOCATION_TYPE_UNKNOW;
	/** 当前点赞次数 */
	private int mCurrentOKCount = 0;
	/** 是否支持声音 */
	private boolean isCanVoice = true;
	private ImageView mHead = null;
	/** */
	private RelativeLayout mMapRootLayout = null;
	/** 是否成功上传过视频 */
	private boolean isUploadSucessed = false;
	/** 是否请求服务器成功过 */
	private boolean isKaiGeSucess = false;
	private LiveDataInfo liveData = null;
	/** 是否正在重連 */
	private boolean isTryingReUpload = false;
	private RelativeLayout mVLayout = null;
	/** 用户设置数据 */
	private LiveSettingBean mSettingData = null;
	/** 标识直播上传是否超时 */
	private boolean isLiveUploadTimeOut = false;

	/** 保存录制的文件名字 */
	public String mRecordVideFileName = "";
	/** 保存录制中的状态 */
	public boolean isRecording = false;

	public enum VideoType {
		mounts, emergency, idle
	};

	/** 保存当前录制的视频类型 */
	public VideoType mCurVideoType = VideoType.idle;

	private VideoSquareManager mVideoSquareManager = null;

	private SharePlatformUtil sharePlatform;
	/** 设置是否返回 */
	private boolean isSettingCallBack = false;
	/** 定位按钮 */
	private Button mLocationBtn = null;
	/** 用户昵称 */
	private TextView mNickName = null;
	/** 点赞 */
	private Button zanBtn = null;
	private Button mShareBtn = null;
	/** 直播发起时间 */
	private TextView mStartTimeTv = null;

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

		VIEW_SELF_PLAY = "rtsp://admin:123456@" + GolukApplication.mIpcIp + "/sub";
		mRtmpUrl = this.getRtmpAddress();
		if (null == mRtmpUrl) {
			mRtmpUrl = UPLOAD_VOIDE_PRE;
		}
		// 获取数据
		getIntentData();
		// 界面初始化
		initView();
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
			mMoreImg.setVisibility(View.GONE);
			mNickName.setText(myInfo.nickName);
			setUserHeadImage(myInfo.head);

		} else {
			if (null != currentUserInfo && null != currentUserInfo.desc) {
				mDescTv.setText(currentUserInfo.desc);
			}
			mMoreImg.setVisibility(View.VISIBLE);
			mApp.mSharedPreUtil.setIsLiveNormalExit(true);
			if (null != currentUserInfo) {
				mLiveCountSecond = currentUserInfo.liveDuration;
			}
			mTitleTv.setText(currentUserInfo.nickName + " 的直播");
			mNickName.setText(currentUserInfo.nickName);
			setUserHeadImage(currentUserInfo.head);
		}
		drawPersonsHead();
		mLiveManager = new TimerManager(10);
		mLiveManager.setListener(this);

		mApp.addLocationListener(TAG, this);

		if (isShareLive) {
			if (isContinueLive) {
				// 续直播
				startLiveLook(myInfo);
				LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE, LIVE_RETRY_LIVE);
				isSettingCallBack = true;
			} else {
				// 显示设置窗口
				if (null == mSettingData) {
					this.finish();
					return;
				}
				startLiveForSetting();
			}
			updateCount(0, 0);
		} else {
			// 计时，90秒后，防止用户进入时没网
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
			startLiveLook(currentUserInfo);
			updateCount(Integer.parseInt(currentUserInfo.zanCount), Integer.parseInt(currentUserInfo.persons));

		}

		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		// 注册回调监听
		GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("live", this);

		mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			if (mVideoSquareManager.checkVideoSquareManagerListener("videosharehotlist")) {
				mVideoSquareManager.removeVideoSquareManagerListener("videosharehotlist");
			}
			mVideoSquareManager.addVideoSquareManagerListener("live", this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = sharePlatform.mController.getConfig().getSsoHandler(requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
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
		if (null != zanBtn) {
			zanBtn.setText("" + okCount);
		}
		if (null != mLookCountTv) {
			mLookCountTv.setText("" + lookCount);
		}
	}

	// 获取当前登录用户的信息
	private void getMyInfo() {
		try {
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----getMyInfo111 :" + mApp.isUserLoginSucess);
			if (mApp.isUserLoginSucess) {
				String userInfo = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
						IPageNotifyFn.PageType_GetUserInfo_Get, "");
				if (null != userInfo) {
					myInfo = JsonUtil.parseSingleUserInfoJson(new JSONObject(userInfo));
					GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----getMyInfo :" + userInfo);
				}
			}
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----getMyInfo 333:");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getIntentData() {
		// 获取视频路径
		Intent intent = getIntent();
		isShareLive = intent.getBooleanExtra(KEY_IS_LIVE, true);
		currentUserInfo = (UserInfo) intent.getSerializableExtra(KEY_USERINFO);
		isContinueLive = intent.getBooleanExtra(KEY_LIVE_CONTINUE, false);
		mSettingData = (LiveSettingBean) intent.getSerializableExtra(KEY_LIVE_SETTING_DATA);
	}

	private void setViewInitData() {
		if (null != currentUserInfo) {
			zanBtn.setText(currentUserInfo.zanCount);
			mLookCountTv.setText(currentUserInfo.persons);
		}

		mStartTimeTv.setText("今天 " + GolukUtils.getCurrentTime());
	}

	private String getVideoId() {
		if (null != myInfo) {
			return myInfo.uid;
		}
		Date dt = new Date();
		long time = dt.getTime();

		return "live" + time;
	}

	private boolean isRequestedForServer = false;

	// 开启自己的直播,请求服务器 (在用户点击完设置后开始请求)
	private void startLiveForServer() {
		isRequestedForServer = true;
		String json = null;
		if (this.isContinueLive) {

		} else {
			json = JsonUtil.getStartLiveJson(mCurrentVideoId, mSettingData);
		}
		if (null == json) {
			return;
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
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----startLiveLook----111 uid: " + userInfo.uid
				+ " aid:" + userInfo.aid);
		if (isLiveUploadTimeOut) {
			return;
		}

		String condi = "{\"uid\":\"" + userInfo.uid + "\",\"desAid\":\"" + userInfo.aid + "\"}";

		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVideoDetail, condi);
		if (!isSucess) {
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----startLiveLook----22 : FASE False FAlse");
			startLiveLookFailed();
		} else {
			// TODO 弹对话框
			// showToast("查看他人直播：" + userInfo.uid);
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----startLiveLook----22 : TRUE TRUE");
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
		mLocationBtn = (Button) findViewById(R.id.live_location_btn);
		mLiveBackBtn = (TextView) findViewById(R.id.live_back_btn);
		mTitleTv = (TextView) findViewById(R.id.live_title);
		mMoreImg = (ImageView) findViewById(R.id.live_more);
		mVLayout = (RelativeLayout) findViewById(R.id.vLayout);
		mVideoLoading = (RelativeLayout) findViewById(R.id.live_video_loading);
		mPlayLayout = (RelativeLayout) findViewById(R.id.live_play_layout);
		mLookCountTv = (TextView) findViewById(R.id.live_lookcount);
		zanBtn = (Button) findViewById(R.id.like_btn);
		mShareBtn = (Button) findViewById(R.id.share_btn);
		mHead = (ImageView) findViewById(R.id.live_userhead);
		mLiveCountDownTv = (TextView) findViewById(R.id.live_countdown);
		mDescTv = (TextView) findViewById(R.id.live_desc);
		mPauseBtn = (Button) findViewById(R.id.live_pause);
		mMapRootLayout = (RelativeLayout) findViewById(R.id.live_map_layout);
		mRPVPalyVideo = (RtmpPlayerView) findViewById(R.id.live_vRtmpPlayVideo);
		mNickName = (TextView) findViewById(R.id.live_nickname);
		mStartTimeTv = (TextView) findViewById(R.id.live_start_time);
		// 视频事件回调注册
		mRPVPalyVideo.setPlayerListener(this);
		mRPVPalyVideo.setBufferTime(1000);
		mRPVPalyVideo.setConnectionTimeout(30000);
		// 先显示气泡上的默认图片
		// 注册事件
		mLiveBackBtn.setOnClickListener(this);
		mPlayLayout.setOnClickListener(this);
		mLocationBtn.setOnClickListener(this);
		mMoreImg.setOnClickListener(this);
		zanBtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mPauseBtn.setOnClickListener(this);

		hidePlayer();
	}

	@Override
	protected void hMessage(Message msg) {
		if (null == msg) {
			return;
		}
		final int what = msg.what;
		switch (what) {
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
		case 100:
			isContinueLive = false;
			break;
		case 101:
			// 直播视频上传成功，现在请求服务器
			// 请求直播
			startLiveForServer();
			break;
		}
	}

	private String mRtmpUrl = null;

	/**
	 * 开启直播录制上传
	 * 
	 * @param aid
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void startLive(String aid) {
		liveVid = aid;
		GolukDebugUtils.e("", "jyf------TTTTT------------开始上传直播----1111 : " + aid);
		if (isStartLive) {
			return;
		}
		GolukDebugUtils.e("", "jyf------TTTTT------------开始上传直播----2222");
		if (CarRecorderManager.isRTSPLiving()) {
			GolukDebugUtils.e("", "jyf------TTTTT------------RTSP正在播放，不可以开始");
			// showToast("RTSP正在直播，不可以开始");
			liveUploadVideoFailed();
			return;
		}
		try {
			GolukDebugUtils.e("", "jyf------TTTTT------------开始上传直播----3333mRtmpUrl: " + mRtmpUrl);
			SharedPreferences sp = getSharedPreferences("CarRecorderPreferaces", Context.MODE_PRIVATE);
			sp.edit().putString("url_live", mRtmpUrl + liveVid).apply();
			sp.edit().commit();
			CarRecorderManager.updateLiveConfiguration(new PreferencesReader(this, false).getConfig());
			if (null != mSettingData) {
				CarRecorderManager.setLiveMute(!mSettingData.isCanVoice);
			}
			GolukDebugUtils.e("", "jyf------TTTTT------------开始上传直播----44444444");
			CarRecorderManager.startRTSPLive();
			isStartLive = true;
			// showToast("开始上传视频");
			GolukDebugUtils.e("", "jyf------TTTTT------------开始上传直播----start--------");
		} catch (RecorderStateException e) {
			e.printStackTrace();
			liveUploadVideoFailed();
			GolukDebugUtils.e("", "jyf------TTTTT------------开始上传直播----Exception ");
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

	private String getRtmpAddress() {
		String rtmpUrl = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_GetServerAddress,
				IGetServerAddressType.GetServerAddress_RtmpServer, "UploadVedioPre");
		GolukDebugUtils.e("", "jyf-----MainActivity-----test:" + rtmpUrl);
		return rtmpUrl;
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
				mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);

				if (!isRequestedForServer) {
					// 没有请求过服务器
					if (!isContinueLive) {
						// 不是续播，才可以请求
						mBaseHandler.sendEmptyMessage(101);
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
			GolukDebugUtils.e("", "jyf------TTTTT------------onLiveRecordFailed----2222:" + nResult);
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
			mBaseHandler.removeMessages(MSG_H_RETRY_UPLOAD);
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
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
			// 重新上传直播视频
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 1000);

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
				GolukDebugUtils.e("", "YYYYYY===onLiveRecordFailed=====222222====");
			}
		}
	};

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
		if (!isSucessBind) {
			registerReceiver(managerReceiver, new IntentFilter(CarRecorderManager.ACTION_RECORDER_MESSAGE));
		}

		isSucessBind = true;
		if (!isShowPop) {
			isShowPop = true;
		}
		loginSucess();

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

	private boolean isSetAudioMute = false;

	/**
	 * 视频播放初始化
	 */
	private void startVideoAndLive(String url) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----startVideoAndLive----url : " + url);
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
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----1111111: ");
		try {
			String jsonMyPos = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Location,
					ILocationFn.LOCATION_CMD_GET_POSITION, "");
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----2222: " + jsonMyPos);
			if (null != jsonMyPos) {
				BaiduPosition myPosition = JsonUtil.parseLocatoinJson(jsonMyPos);
				if (null != myPosition) {
					// 开始绘制我的位置

					if (mApp.isUserLoginSucess) {
						if (null == myInfo) {
							this.getMyInfo();
						}
						GolukDebugUtils.e(null,
								"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head 333333: "
										+ myInfo.nickName);
						if (null != myInfo) {
							GolukDebugUtils.e(null,
									"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head4444444444: ");
							mCurrentLocationType = LOCATION_TYPE_HEAD;
							myInfo.lon = String.valueOf(myPosition.rawLon);
							myInfo.lat = String.valueOf(myPosition.rawLat);
							String drawTxt = JsonUtil.UserInfoToString(myInfo);
							mBaiduMapManage.addSinglePoint(drawTxt);
							GolukDebugUtils.e(null,
									"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head555555555: "
											+ drawTxt);
						}

						GolukDebugUtils.e(null,
								"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head6666666666: ");

					} else {
						GolukDebugUtils.e(null,
								"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head777777777777777: ");
						mCurrentLocationType = LOCATION_TYPE_POINT;
						// 画小蓝点
						MyLocationData locData = new MyLocationData.Builder().accuracy((float) myPosition.radius)
								.direction(100).latitude(myPosition.rawLat).longitude(myPosition.rawLon).build();
						// 确认地图我的位置点是否更新位置
						mBaiduMap.setMyLocationData(locData);
					}

				} else {
					GolukUtils.showToast(this, "无法获取我的位置信息");
					GolukDebugUtils.e(null,
							"jyf----20150406----LiveActivity----drawPersonsHead---draw MY Head8888888888: ");
				}
			} else {
				GolukUtils.showToast(this, "无法获取我的位置信息");
			}
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----999999999999: ");
			if (isShareLive) {
				// 自己直播不再绘制其它人的点
				return;
			}
			if (null == currentUserInfo) {
				GolukUtils.showToast(this, "无法获取看直播人的经纬度");
				return;
			}
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawPersonsHead----AAAAAAAAAA  : "
					+ currentUserInfo.aid);
			mBaiduMapManage.addSinglePoint(JsonUtil.UserInfoToString(currentUserInfo));
		} catch (Exception e) {
			e.printStackTrace();
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----drawPersonsHead---BBBBBBBB-Exception : ");
		}

	}

	private void liveFailedStart(boolean isLive) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----liveFailedStart---- 直播回调失败: ");
		if (isLive) {
			startLiveFailed();
		} else {
			startLiveLookFailed();
		}
	}

	// 自己开启直播，返回接口
	public void callBack_LiveLookStart(boolean isLive, int success, Object param1, Object param2) {
		if (isAlreadExit) {
			// 界面已经退出
			return;
		}
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

		if (mIsFirstSucess) {
			this.click_share(false);
			mIsFirstSucess = false;
		}

	}

	private boolean mIsFirstSucess = true;

	// 上报位置
	private void startUploadMyPosition() {
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StartUploadPosition, "");
		// Toast.makeText(this, "开始上报位置", Toast.LENGTH_LONG).show();
	}

	// 判断自己发起的直播是否有效
	private void liveCallBack_startLiveIsValid(int success, Object obj) {
		if (isAlreadExit) {
			// 界面已经退出
			return;
		}
		// 是自己的直播是否有效
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		if (1 != success) {
			mBaseHandler.sendEmptyMessage(100);
			// showToast("需要重新开启直播");
			return;
		}
		final String data = (String) obj;
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----liveCallBack_startLiveIsValid----222222 : " + data);
		// 数据成功
		liveData = JsonUtil.parseLiveDataJson(data);
		if (null == liveData) {
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----liveCallBack_startLiveIsValid----333333333 : ");
			mBaseHandler.sendEmptyMessage(100);
			// showToast("需要重新开启直播");
			return;
		}

		if (200 != liveData.code) {
			// 视频无效下线
			// 弹设置框,重新发起直播
			mBaseHandler.sendEmptyMessage(100);
			// showToast("需要重新开启直播");
		} else {
			// 上次的视频还有效,开始上传直播，调用上报位置
			startLive(mCurrentVideoId);
			startUploadMyPosition();
			isSettingCallBack = true;

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
		GolukDebugUtils.e("", "视频直播数据返回--LiveVideoDataCallBack: success: " + success);
		if (isAlreadExit) {
			// 界面已经退出
			return;
		}

		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----111 : " + success);
		if (isShareLive) {
			liveCallBack_startLiveIsValid(success, obj);
			return;
		}

		if (1 != success) {
			liveCallBackError(true);
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_REQUEST_DETAIL, 4 * 1000);
			return;
		}
		final String data = (String) obj;
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----222222 : " + data);
		// 数据成功
		liveData = JsonUtil.parseLiveDataJson(data);
		if (null == liveData) {
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----333333333 : ");
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_REQUEST_DETAIL, 4 * 1000);
			liveCallBackError(false);
			return;
		}
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----4444 : " + (String) obj);
		if (200 != liveData.code) {
			mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
			videoInValid();
			// 视频无效下线
			return;
		}
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----5555 : ");
		isCanVoice = liveData.voice.equals("1") ? true : false;
		this.isKaiGeSucess = true;
		mLiveCountSecond = liveData.restTime;

		mDescTv.setText(liveData.desc);

		if (1 == liveData.active) {
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----6666 : ");
			// 主动直播
			if (!mRPVPalyVideo.isPlaying()) {
				startVideoAndLive(liveData.playUrl);
			}
		} else {
			// 被动直播
		}
	}

	// 视频已经下线
	private void videoInValid() {
		LiveDialogManager.getManagerInstance().showSingleBtnDialog(LiveActivity.this,
				LiveDialogManager.DIALOG_TYPE_LIVE_OFFLINE, "提示", "该用户直播己结束，谢谢观看");
		mBaseHandler.removeMessages(MSG_H_RETRY_REQUEST_DETAIL);
		mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		if (null != mLiveManager) {
			mLiveManager.cancelTimer();
		}
		mVideoLoading.setVisibility(View.GONE);
	}

	private void liveCallBackError(boolean isprompt) {
		if (isprompt) {
			GolukUtils.showToast(this, "查看直播服务器返回数据异常");
		}
	}

	@Override
	protected void onDestroy() {
		GolukDebugUtils.e("", "liveplay---onDestroy");
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
		case R.id.share_btn:
			if (this.isShareLive) {
				if (isSettingCallBack) {
					click_share(true);
				}
			} else {
				click_share(true);
			}
			break;
		case R.id.live_more:
			click_juBao();
			break;
		case R.id.like_btn:
			click_Like();
			break;
		case R.id.live_location_btn:
			// 定位
			LatLng ll = new LatLng(LngLat.lat, LngLat.lng);
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
			break;
		default:
			break;
		}
	}

	// 点击 "举报"
	private void click_juBao() {
		if (!isShareLive) {
			showDialog();
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
		dialog = new AlertDialog.Builder(LiveActivity.this, R.style.CustomDialog).create();
		dialog.show();
		dialog.getWindow().setContentView(R.layout.video_square_dialog_main);
		dialog.getWindow().findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				ad = new AlertDialog.Builder(LiveActivity.this, R.style.CustomDialog).create();
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
		confirmation = new AlertDialog.Builder(LiveActivity.this, R.style.CustomDialog).create();
		confirmation.show();
		confirmation.getWindow().setContentView(R.layout.video_square_dialog_confirmation);
		confirmation.getWindow().findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				boolean isSucess = report("1", getCurrentVideoId(), reporttype);
				if (isSucess) {
					GolukUtils.showToast(LiveActivity.this, "举报成功,我们稍后会进行处理");
				} else {
					GolukUtils.showToast(LiveActivity.this, "举报失败!");
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

	private void click_share(boolean isClick) {
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
			if (isClick) {
				GolukUtils.showToast(this, "分享失败");
			}

		} else {
			if (isClick) {
				LiveDialogManager.getManagerInstance().showShareProgressDialog(this,
						LiveDialogManager.DIALOG_TYPE_LIVE_SHARE, "提示", "正在请求分享地址");
			}

		}
	}

	private void loginSucess() {
		if (this.isShareLive) {
			return;
		}
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 111111");
		if (!mApp.isUserLoginSucess) {
			// 登录失败
			return;
		}
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----loginSucess----22 : 222222");
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

	private void click_Like() {
		if (!this.isKaiGeSucess) {
			return;
		}
		if (isAlreadClickOK) {
			// showToast("不能重复点赞");
			return;
		}
		isAlreadClickOK = true;

		Drawable drawable = getResources().getDrawable(R.drawable.like_btn_press);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		zanBtn.setCompoundDrawables(drawable, null, null, null); // 设置点赞背景
		mCurrentOKCount++;
		if (null != zanBtn) {
			zanBtn.setText("" + mCurrentOKCount);
			zanBtn.setText("" + mCurrentOKCount);
		}
		boolean isSucess = clickPraise("1", getCurrentVideoId(), "1");
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----click_OK----isSucess : " + isSucess);
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
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----retryRunnable--1111 : ");
			if (null != mRPVPalyVideo) {
				GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----retryRunnable--22222 : ");
				if (isShareLive) {
					GolukDebugUtils.e(null,
							"jyf----20150406----LiveActivity----PlayerCallback----retryRunnable--3333 : ");
					mRPVPalyVideo.setDataSource(VIEW_SELF_PLAY);
					mRPVPalyVideo.start();

					GolukDebugUtils.e(null,
							"jyf----20150406----LiveActivity----PlayerCallback----retryRunnable--44444 : ");
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
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerPrepared : ");
		mRPVPalyVideo.setHideSurfaceWhilePlaying(true);
		if (!this.isShareLive) {
			mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		}
	}

	@Override
	public boolean onPlayerError(RtmpPlayerView rpv, int arg1, int arg2, String arg3) {
		// 视频播放出错
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerError : " + arg2 + "  "
				+ arg3);
		playerError(rpv);
		// 加载画面
		// rpv.removeCallbacks(retryRunnable);
		// rpv.postDelayed(retryRunnable, 5000);
		return false;
	}

	@Override
	public void onPlayerCompletion(RtmpPlayerView rpv) {
		// 视频播放完成
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerCompletion : ");
		playerError(rpv);
		// rpv.removeCallbacks(retryRunnable);
		// rpv.postDelayed(retryRunnable, 5000);
	}

	@Override
	public void onPlayerBegin(RtmpPlayerView rpv) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerBegin : ");
		mVideoLoading.setVisibility(View.GONE);
		// mRPVPalyVideo.setVisibility(View.VISIBLE);
		// // mRPVPalyVideo.invalidate();
		// mRPVPalyVideo.postInvalidate();

		showPlayer();

		if (!isShareLive) {
			mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
			mLiveManager.cancelTimer();
			// 开启timer开始计时
			updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
			mLiveManager.startTimer(mLiveCountSecond, true);
		}
	}

	@Override
	public void onPlayBuffering(RtmpPlayerView arg0, boolean start) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayBuffering : " + start);
		if (start) {
			// 缓冲开始
			mBaseHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
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
			mBaseHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
			// 重新加载播放器预览
			mBaseHandler.removeMessages(MSG_H_RETRY_SHOW_VIEW);
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_SHOW_VIEW, 5000);
		} else {
			// UI需要转圈
			mBaseHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
			// 计时90秒
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
			// 重新请求直播详情
			mBaseHandler.sendEmptyMessage(MSG_H_RETRY_REQUEST_DETAIL);
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

		mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		mBaseHandler.removeMessages(MSG_H_RETRY_UPLOAD);
		mBaseHandler.removeMessages(MSG_H_RETRY_SHOW_VIEW);
		mBaseHandler.removeMessages(MSG_H_RETRY_REQUEST_DETAIL);
		mBaseHandler.removeMessages(MSG_H_PLAY_LOADING);

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
		finish();
	}

	private void dissmissAllDialog() {
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		LiveDialogManager.getManagerInstance().dismissSingleBtnDialog();
	}

	private void preExit() {
		String message = this.isShareLive ? LIVE_EXIT_PROMPT : LIVE_EXIT_PROMPT2;
		if (isAlreadExit) {
			return;
		}
		LiveDialogManager.getManagerInstance()
				.showLiveBackDialog(this, LiveDialogManager.DIALOG_TYPE_LIVEBACK, message);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----onKeyDown----111111 : ");
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			preExit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startLiveForSetting() {
		mLiveCountSecond = mSettingData.duration;
		if (null != mDescTv && null != mSettingData.desc && !"".equals(mSettingData.desc)) {
			mDescTv.setText(mSettingData.desc);
		} else {
			mDescTv.setVisibility(View.GONE);
		}
		if (!isAlreadExit) {
			LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE, LIVE_START_PROGRESS_MSG);
		}

		// 开始视频上传
		startLive(mCurrentVideoId);
		updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
		// 开始计时
		mLiveManager.startTimer(mLiveCountSecond, true);

		isSettingCallBack = true;
	}

	/**
	 * 首页大头针数据返回
	 */
	public void pointDataCallback(int success, Object obj) {
		if (1 != success) {
			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type:  sucess:" + success);
			return;
		}

		final String str = (String) obj;

		GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type111:  str：" + str);
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

				GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback 3333333:  更新我自己的赞 zanCount："
						+ tempMyInfo.zanCount + "	permson:" + tempMyInfo.persons);
				return;
			}
			if (null == tempUserInfo) {
				// showToast("未找到用户信息");

				GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type44444:  str：" + str);
				return;
			}

			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----pointDataCallback----aid  : " + tempUserInfo.aid
					+ " lon:" + tempUserInfo.lon + " lat:" + tempUserInfo.lat);

			mBaiduMapManage.updatePosition(tempUserInfo.aid, Double.parseDouble(tempUserInfo.lon),
					Double.parseDouble(tempUserInfo.lat));

			currentUserInfo.lat = tempUserInfo.lat;
			currentUserInfo.lon = tempUserInfo.lon;

			GolukDebugUtils.e(null, "jyf-------live----LiveActivity--pointDataCallback type55555:  str：" + str);

			// 设置“赞”的人数，和观看人数
			this.updateCount(Integer.parseInt(tempUserInfo.zanCount), Integer.parseInt(tempUserInfo.persons));

			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type66666:  str：" + str);

			if (!isShareLive) {
				GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type777777:  str：" + str);
				// if (!isGetingAddress) {
				GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type88888  str：" + str);
				// isGetingAddress = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type999999:  Exception ：");
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
				mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 1000);
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
				GolukDebugUtils.e("aaaaaa", "-------------aaaaa-----stop------");

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
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----LocationCallBack----0000  : ");
		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----LocationCallBack----111  : ");

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
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----IPCManage_CallBack----event  : " + event + " msg:"
				+ msg);
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
		GolukDebugUtils.d("", "m8sBtn===IPC_VDCPCmd_TriggerRecord===callBack_VDCP=====param1=   " + param1
				+ "     ==param2=" + param2 + "	msg:" + msg);
		switch (msg) {
		// 实时抓图
		case IPC_VDCPCmd_SnapPic:
			dealSnapCallBack(param1, param2);
			break;
		default:
			break;
		}
	}

	private void dealSnapCallBack(int param1, Object param2) {
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----接收图片命令回调");
		if (0 != param1) {
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----接收图片命令失败-------");
			return;
		}
		// 文件路径格式：fs1:/IPC_Snap_Pic/snapPic.jpg
		final String imageFilePath = (String) param2;
		if (TextUtils.isEmpty(imageFilePath)) {
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----接收图片路径为空");
			return;
		}
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----333 imagePath:   " + imageFilePath);
		String path = FileUtils.libToJavaPath(imageFilePath);
		if (TextUtils.isEmpty(path)) {
			return;
		}
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----4444 path:   " + path);
		long time = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String timename = format.format(new Date(time));

		// 创建文件夹
		String dirname = Environment.getExternalStorageDirectory() + File.separator + "goluk" + File.separator
				+ "goluk" + File.separator + "screenshot";
		GFileUtils.makedir(dirname);
		String picName = dirname + File.separator + timename + ".jpg";
		GFileUtils.compressImageToDisk(path, picName);
		File file = new File(picName);
		if (!file.exists()) {
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----接收图片命令失败------22222");
			return;
		}
		String newFilePath = FileUtils.javaToLibPath(picName);
		String uploadJson = JsonUtil.getUploadSnapJson(mCurrentVideoId, newFilePath);
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_LiveUploadPic,
				uploadJson);

	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		if (!isSucess) {
			GolukUtils.showToast(this, "分享失败");
			return;
		}

		String vid = null;
		if (isShareLive) {
			vid = mCurrentVideoId;
		} else {
			if (!isKaiGeSucess) {
				return;
			}
			vid = liveData.vid;
		}

		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, vid);
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == SquareCmd_Req_GetShareUrl) {
			// 销毁对话框
			LiveDialogManager.getManagerInstance().dismissShareProgressDialog();
			if (1 != msg) {
				GolukUtils.showToast(this, "分享失败");
				return;
			}
			try {
				JSONObject result = new JSONObject((String) param2);
				GolukDebugUtils.e("", "YYYY+RESULT00000000");
				if (!result.getBoolean("success")) {
					return;
				}
				JSONObject data = result.getJSONObject("data");
				String shareurl = data.getString("shorturl");
				String coverurl = data.getString("coverurl");
				GolukDebugUtils.e("", "zhdata===" + data);
				String describe = data.optString("describe");
				if (TextUtils.isEmpty(describe)) {
					describe = "#极路客直播#";
				}
				String name = "";
				if (this.isShareLive) {
					name = this.myInfo.nickName;
				} else {
					name = this.currentUserInfo.nickName;
				}
				String ttl = name + "的直播视频分享";
				if ("".equals(coverurl)) {
				}
				// 设置分享内容
				CustomShareBoard sb = new CustomShareBoard(LiveActivity.this, sharePlatform, shareurl, coverurl,
						describe, ttl);
				sb.showAtLocation(LiveActivity.this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
