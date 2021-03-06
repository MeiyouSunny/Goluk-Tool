package com.mobnote.golukmain.livevideo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
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
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.serveraddress.IGetServerAddressType;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.baidu.mapapi.SDKInitializer;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMapQuery;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.PlayUrlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.cluster.bean.UserLabelBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.ILive;
import com.mobnote.golukmain.live.LiveDataInfo;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.live.LiveSettingBean;
import com.mobnote.golukmain.live.LiveSettingPopWindow;
import com.mobnote.golukmain.live.TimerManager;
import com.mobnote.golukmain.live.TimerManager.ITimerManagerFn;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.videosuqare.BaiduMapView;
import com.mobnote.golukmain.videosuqare.JsonCreateUtils;
import com.mobnote.golukmain.videosuqare.ShareDataBean;
import com.mobnote.golukmain.videosuqare.VideoSquareManager;
import com.mobnote.map.IMapTools;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;
import com.rd.car.player.RtmpPlayerView;

import de.greenrobot.event.EventBus;

public abstract class AbstractLiveActivity extends BaseActivity implements OnClickListener,
		RtmpPlayerView.RtmpPlayerViewLisener, ILiveDialogManagerFn, ITimerManagerFn, ILocationFn, IPCManagerFn, ILive,
		VideoSuqareManagerFn, ILiveVideo, ILiveFnAdapter, IRequestResultListener {

	/** 自己预览地址 */
	private static String VIEW_SELF_PLAY = "";
	/** application */
	protected GolukApplication mApp = null;
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
	/** 自定义播放器支持特效 */
	public RtmpPlayerView mRPVPalyVideo = null;
	/** 视频地址 */
	private String mFilePath = "";
	/** 是否直播 还是　看别人直播 true/false 直播/看别人直播 */
	protected boolean isShareLive = true;
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
	protected UserInfo currentUserInfo = null;
	/** 我的登录信息 如果未登录，则为空 */
	protected UserInfo myInfo = null;
	/** 是否续直播 */
	private boolean isContinueLive = false;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
	/** 是否已经点过“赞” */
	private boolean isAlreadClickOK = false;
	private TimerManager mLiveManager = null;
	/** 防止多次按退出键 */
	private boolean isAlreadExit = false;
	private String mCurrentVideoId = null;
	/** -1/0/1 未定位/小蓝点/气泡 */
	protected int mCurrentLocationType = LOCATION_TYPE_UNKNOW;
	/** 当前点赞次数 */
	private int mCurrentOKCount = 0;
	/** 是否支持声音 */
	private boolean isCanVoice = true;
	private ImageView mHead = null;
	/** 头像认证 */
	private ImageView mAuthenticationImg = null;
	protected RelativeLayout mMapRootLayout = null;
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
	protected boolean isLiveUploadTimeOut = false;
	/** 保存录制的文件名字 */
	public String mRecordVideFileName = "";
	/** 保存录制中的状态 */
	public boolean isRecording = false;
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
	private Bitmap mThumbBitmap = null;
	private boolean isRequestedForServer = false;
	private boolean mIsFirstSucess = true;
	private String mRtmpUrl = null;

	protected IMapTools mMapTools;
	private ILiveOperateFn mLiveOperator = null;
	private boolean isSetAudioMute = false;

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

		// 获取直播所需的地址
		getURL();
		// 获取数据
		getIntentData();
		// 界面初始化
		initView();
		// 显示数据
		setViewInitData();
		// 地图初始化
		initMap();
		// 获取我的登录信息
		myInfo = mApp.getMyInfo();
		// 开始预览或开始直播
		if (isShareLive) {
			if (mApp.mIPCControlManager.isT1Relative()) {
				mLiveOperator = new LiveOperateVdcp(this);
			} else {
				mLiveOperator = new LiveOperateCarrecord(this, this);
			}
			mBaseApp.isAlreadyLive = true;
			SharedPrefUtil.setIsLiveNormalExit(false);
			mCurrentVideoId = getVideoId();
			startVideoAndLive("");
			mTitleTv.setText(this.getString(R.string.str_mylive_text));
			mMoreImg.setVisibility(View.GONE);
			mNickName.setText(myInfo.nickname);
			setUserHeadImage(myInfo.head, myInfo.customavatar);
			setAuthentication(myInfo.mUserLabel);
		} else {
			if (null != currentUserInfo && null != currentUserInfo.desc) {
				mDescTv.setText(currentUserInfo.desc);
			}
			mMoreImg.setVisibility(View.VISIBLE);
			SharedPrefUtil.setIsLiveNormalExit(true);
			if (null != currentUserInfo) {
				mLiveCountSecond = currentUserInfo.liveDuration;
			}
			mTitleTv.setText(currentUserInfo.nickname + this.getString(R.string.str_live_someone));
			mNickName.setText(currentUserInfo.nickname);
			setUserHeadImage(currentUserInfo.head, currentUserInfo.customavatar);
			setAuthentication(currentUserInfo.mUserLabel);
		}
		// drawPersonsHead();
		mLiveManager = new TimerManager(10);
		mLiveManager.setListener(this);
		mApp.addLocationListener(TAG, this);
		if (isShareLive) {
			if (isContinueLive) {
				GolukDebugUtils.e("", "newlive-----LiveActivity----onCreate---开始续播---: ");
				// 续直播
				// 获取墨认的设置
				LiveSettingPopWindow lpw = new LiveSettingPopWindow(this, mRootLayout);
				mSettingData = lpw.getCurrentSetting();
				startLiveLook(myInfo);
				LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE,
						this.getString(R.string.str_live_retry_live));
				isSettingCallBack = true;
			} else {
				// 显示设置窗口
				if (null == mSettingData) {
					this.finish();
					return;
				}
				if (null != mLiveOperator) {
					mLiveOperator.stopLive();
				}
				startLiveForSetting();
			}
			updateCount(0, 0);
		} else {
			// 计时，90秒后，防止用户进入时没网
			start90Timer();
			startLiveLook(currentUserInfo);
			updateCount(Integer.parseInt(currentUserInfo.zanCount), Integer.parseInt(currentUserInfo.persons));
		}
		setCallBackListener();
	}

	// 计时，90秒后
	private void start90Timer() {
		mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		mBaseHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
	}

	private void setCallBackListener() {
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
		// mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_MYLOCATION, 10 * 1000);
	}

	private void getURL() {
		VIEW_SELF_PLAY = PlayUrlManager.getRtspUrl();
		mRtmpUrl = this.getRtmpAddress();
		if (null == mRtmpUrl) {
			mRtmpUrl = PlayUrlManager.UPLOAD_VOIDE_PRE;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null != sharePlatform) {
			sharePlatform.onActivityResult(requestCode, resultCode, data);
		}
	}

	// 更新观看人数和点赞人数
	private void updateCount(int okCount, int lookCount) {
		mCurrentOKCount = okCount;
		if (null != zanBtn) {
			zanBtn.setText("" + GolukUtils.getFormatNumber("" + okCount));
		}
		if (null != mLookCountTv) {
			mLookCountTv.setText("" + GolukUtils.getFormatNumber("" + lookCount));
		}
	}

	/**
	 * 首页大头针数据返回
	 */
	public void pointDataCallback(int success, Object obj) {
		if (1 != success) {
			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type:  sucess:" + success);
			// 重新請求大头針数据
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);
			return;
		}
		final String str = (String) obj;
		GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type111:  str：" + str);
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
					// 重新請求大头針数据
					mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);
					return;
				}
				this.updateCount(Integer.parseInt(tempMyInfo.zanCount), Integer.parseInt(tempMyInfo.persons));
				GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback 3333333:  更新我自己的赞 zanCount："
						+ tempMyInfo.zanCount + "	permson:" + tempMyInfo.persons);
				// 重新請求大头針数据
				mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);
				return;
			}
			if (null == tempUserInfo) {
				GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type44444:  ：");
				// 重新請求大头針数据
				mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);
				return;
			}
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----pointDataCallback----aid  : " + tempUserInfo.aid
					+ " lon:" + tempUserInfo.lon + " lat:" + tempUserInfo.lat);
			mMapTools.updatePosition(tempUserInfo.aid, Double.parseDouble(tempUserInfo.lon),
					Double.parseDouble(tempUserInfo.lat), true);
			currentUserInfo.lat = tempUserInfo.lat;
			currentUserInfo.lon = tempUserInfo.lon;
			GolukDebugUtils.e(null, "jyf-------live----LiveActivity--pointDataCallback type55555:  ：");
			// 设置“赞”的人数，和观看人数
			this.updateCount(Integer.parseInt(tempUserInfo.zanCount), Integer.parseInt(tempUserInfo.persons));
			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type66666:  ：");
		} catch (Exception e) {
			e.printStackTrace();
			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type999999:  Exception ：");
		}

		// 重新請求大头針数据
		mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);

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
		mStartTimeTv.setText(this.getString(R.string.str_today) + " " + GolukUtils.getCurrentTime());
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
		isRequestedForServer = true;
		String json = null;
		if (this.isContinueLive) {
		} else {
			json = JsonUtil.getStartLiveJson(mCurrentVideoId, mSettingData);
		}
		if (null == json) {
			return;
		}
		// 请求发起直播
		LiveStartRequest liveRequest = new LiveStartRequest(IPageNotifyFn.PageType_LiveStart, this);
		boolean isSucess = liveRequest.get(json);

		if (!isSucess) {
			GolukDebugUtils.e("", "newlive-----LiveActivity-----startLiveForServer :--failed");
			startLiveFailed();
		} else {
			if (!isAlreadExit) {
				LiveDialogManager.getManagerInstance().setProgressDialogMessage(
						this.getString(R.string.str_live_create));
			}
		}
	}

	// 查看他人的直播
	public void startLiveLook(UserInfo userInfo) {
		if (isLiveUploadTimeOut) {
			return;
		}
		String condi = "{\"uid\":\"" + userInfo.uid + "\",\"desAid\":\"" + userInfo.aid + "\"}";
		GolukDebugUtils.e("", "newlive-----LiveActivity----startLiveLook---查看他人直播---: " + condi);
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVideoDetail, condi);
		if (!isSucess) {
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----startLiveLook----22 : FASE False FAlse");
		}
	}

	private void startLiveFailed() {
		GolukDebugUtils.e("", "newlive-----LiveActivity-----startLiveFailed :--");
		if (!isAlreadExit) {
			LiveDialogManager.getManagerInstance().showTwoBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_LIVE_REQUEST_SERVER, LIVE_DIALOG_TITLE,
					getString(R.string.str_live_upload_first_error));
		}
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
		mAuthenticationImg = (ImageView) findViewById(R.id.live_head_authentication);
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
			if (!isAlreadExit) {
				LiveDialogManager.getManagerInstance().dismissProgressDialog();
				LiveDialogManager.getManagerInstance().showSingleBtnDialog(AbstractLiveActivity.this,
						LiveDialogManager.DIALOG_TYPE_LIVE_TIMEOUT, LIVE_DIALOG_TITLE,
						this.getString(R.string.str_live_net_error));
			}
			break;
		case MSG_H_RETRY_UPLOAD:
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
		case MSG_H_START_NEW_LIVE:
			isContinueLive = false;
			if (null == mSettingData) {
				this.finish();
				return;
			}
			startLiveForSetting();
			break;
		case MSG_H_REQUEST_SERVER:
			// 直播视频上传成功，现在请求服务器
			// 请求直播
			startLiveForServer();
			break;
		case MSG_H_TO_MYLOCATION:
			// toMyLocation();
			break;
		case MSG_H_TO_GETMAP_PERSONS:
			EventBus.getDefault().post(new EventMapQuery(EventConfig.LIVE_MAP_QUERY));
			break;
		case MSG_H_MAPLOAD_FINISH:
			// 地图加载完成
			drawPersonsHead();
			break;
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
		GolukDebugUtils.e("", "newlive-----LiveActivity-----startLive  ---1");
		liveVid = aid;
		if (null != mLiveOperator) {
			StartLiveBean bean = new StartLiveBean();
			bean.url = mRtmpUrl + liveVid;
			bean.isVoice = !mSettingData.isCanVoice;
			bean.stream = "1";
			bean.time = "" + mLiveCountSecond;
			mLiveOperator.startLive(bean);
		}
	}

	private String getRtmpAddress() {
		String rtmpUrl = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_GetServerAddress,
				IGetServerAddressType.GetServerAddress_RtmpServer, "UploadVedioPre");
		GolukDebugUtils.e("", "jyf-----MainActivity-----test:" + rtmpUrl);
		return rtmpUrl;
	}

	boolean isStartTimer = false;

	private void uploadLiveSuccess() {
		// 正常发起直播 ，开始计时
		if (!isStartTimer) {
			isStartTimer = true;
			if (!this.isContinueLive) {
				mLiveManager.startTimer(mLiveCountSecond, true);
			}
		}
		isUploadSucessed = true;
		isTryingReUpload = false;
		// 取消90秒
		mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		if (!isRequestedForServer) {
			// 没有请求过服务器
			if (!isContinueLive) {
				// 不是续播，才可以请求
				mBaseHandler.sendEmptyMessage(MSG_H_REQUEST_SERVER);
			}
		}
	}

	/**
	 * 设置用户标识，包括 认证，加V, 达人
	 * 
	 * @param userLabel
	 *            用户标签实体类
	 * @author jyf
	 */
	private void setAuthentication(UserLabelBean userLabel) {
		if (null == userLabel) {
			mAuthenticationImg.setVisibility(View.GONE);
			return;
		}
		// 判断是否是认证
		if (null != userLabel.approvelabel && "1".equals(userLabel.approvelabel)) {
			mAuthenticationImg.setVisibility(View.VISIBLE);
			mAuthenticationImg.setBackgroundResource(R.drawable.authentication_bluev_icon);
			return;
		}
		// 判断是否是加V
		if (null != userLabel.headplusv && "1".equals(userLabel.headplusv)) {
			mAuthenticationImg.setVisibility(View.VISIBLE);
			mAuthenticationImg.setBackgroundResource(R.drawable.authentication_yellowv_icon);
			return;
		}
		// 判断是否是达人
		if (null != userLabel.tarento && "1".equals(userLabel.tarento)) {
			mAuthenticationImg.setVisibility(View.VISIBLE);
			mAuthenticationImg.setBackgroundResource(R.drawable.authentication_star_icon);
			return;
		}

		mAuthenticationImg.setVisibility(View.GONE);
	}

	/**
	 * 设置左上角视频发布者头像
	 * 
	 * @param headStr
	 * @param neturl
	 */
	private void setUserHeadImage(String headStr, String neturl) {
		try {
			if (null == mHead) {
				return;
			}
			if (null != neturl && !"".equals(neturl)) {
				GlideUtils.loadNetHead(this, mHead, neturl, R.drawable.live_icon_portrait);
			} else {
				if (null != headStr && !"".equals(headStr)) {
					int utype = Integer.valueOf(headStr);
					int head = mHeadImg[utype];
					GlideUtils.loadLocalHead(this, mHead, head);
				}
			}
		} catch (Exception e) {
		}
	}

	// 直播上传失败
	private void liveUploadVideoFailed() {
		GolukDebugUtils.e("", "newlive-----LiveActivity-----liveUploadVideoFailed :");
		if (!mApp.mIPCControlManager.isT1Relative()) {
			// T1不需要重新发起直播
			liveStopUploadVideo();
		} else {
			if (!mLiveOperator.liveState()) {
				liveStopUploadVideo();
			}
		}

		if (isLiveUploadTimeOut) {
			mBaseHandler.removeMessages(MSG_H_RETRY_UPLOAD);
			return;
		}
		if (isUploadSucessed) {
			if (!isTryingReUpload) {
				// 显示Loading
				if (!isAlreadExit) {
					LiveDialogManager.getManagerInstance().showProgressDialog(this, "提示",
							getString(R.string.str_live_retry_upload_msg));
				}
			}
			// 计时，90秒后，提示用户上传失败
			start90Timer();
			if (!mApp.mIPCControlManager.isT1Relative()) {
				// 重新上传直播视频
				mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 1000);
			} else {
				if (!mLiveOperator.liveState()) {
					// 重新上传直播视频
					mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 1000);
				}
			}
			isTryingReUpload = true;
		} else {
			LiveDialogManager.getManagerInstance().dismissProgressDialog();
			// 断开，提示用户是否继续上传
			GolukDebugUtils.e("", "newlive-----LiveActivity-----liveUploadVideoFailed :---alread");
			if (!isAlreadExit && GolukUtils.isActivityAlive(this)) {
				LiveDialogManager.getManagerInstance().showTwoBtnDialog(this,
						LiveDialogManager.DIALOG_TYPE_LIVE_RELOAD_UPLOAD, LIVE_DIALOG_TITLE,
						getString(R.string.str_live_upload_first_error));
			}
		}
	}

	// 停止传视频直播
	private void liveStopUploadVideo() {
		if (null != mLiveOperator) {
			this.mLiveOperator.stopLive();
		}
	}

	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			// if (!CarRecorderManager.isRTSPLiving()) {
			isStartLive = false;
			startLive(mCurrentVideoId);
			GolukDebugUtils.e("", "YYYYYY===onLiveRecordFailed=====222222====");
			// }
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		if (null != mLiveOperator) {
			mLiveOperator.onStart();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(this, "LiveVideo");
		if (null != mLiveOperator) {
			mLiveOperator.onResume();
		}

	}

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

	private void liveFailedStart(boolean isLive) {
		GolukDebugUtils.e("", "newlive-----LiveActivity-----liveFailedStart :--直播回调失败");
		if (isLive) {
			startLiveFailed();
		}
	}

	public void CallBack_StartLiveServer(boolean isLive, LiveDataInfo dataInfo) {
		if (isAlreadExit) {
			// 界面已经退出
			return;
		}
		if (null == dataInfo) {
			liveFailedStart(isLive);
			return;
		}
		if (200 != dataInfo.code) {
			liveFailedStart(isLive);
			LiveDialogManager.getManagerInstance().dismissProgressDialog();
			LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_LIVE_OFFLINE, this.getString(R.string.user_dialog_hint_title),
					this.getString(R.string.str_live_over));
			return;
		}
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		isKaiGeSucess = true;
		startScreenShot();
		startUploadMyPosition();
		if (mIsFirstSucess) {
			this.click_share(false);
			mIsFirstSucess = false;
		}
	}

	// 抓取第一帧图
	private void startScreenShot() {
		if (this.isShareLive) {
			// 视频截图 开始视频，上传图片
			GolukApplication.getInstance().getIPCControlManager().screenShot();
		}
	}

	// 自己开启直播，返回接口
	public void callBack_LiveLookStart(boolean isLive, int success, Object param1, Object param2) {
		if (isAlreadExit) {
			// 界面已经退出
			return;
		}
		if (IPageNotifyFn.PAGE_RESULT_SUCESS != success) {
			GolukDebugUtils.e("", "newlive-----LiveOperateVdcp-----callBack_LiveLookStart-------:  ");
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
					LiveDialogManager.DIALOG_TYPE_LIVE_OFFLINE, this.getString(R.string.user_dialog_hint_title),
					this.getString(R.string.str_live_over));
			return;
		}
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		isKaiGeSucess = true;
		if (this.isShareLive) {
			// 视频截图 开始视频，上传图片
			GolukApplication.getInstance().getIPCControlManager().screenShot();
		}
		startUploadMyPosition();
		if (mIsFirstSucess) {
			this.click_share(false);
			mIsFirstSucess = false;
		}
	}

	// 上报位置
	private void startUploadMyPosition() {
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StartUploadPosition, "");
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
			// 需要重新开启直播， 把之前的直播停止掉
			newStartLive();
			return;
		}
		final String data = (String) obj;
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----liveCallBack_startLiveIsValid----222222 : " + data);
		// 数据成功
		liveData = JsonUtil.parseLiveDataJson(data);
		if (null == liveData) {
			newStartLive();
			return;
		}

		if (200 != liveData.code) {
			// 视频无效下线
			// 弹设置框,重新发起直播
			newStartLive();
		} else {
			// 上次的视频还有效,开始上传直播，调用上报位置
			if (!mApp.mIPCControlManager.isT1Relative()) {
				// 如果是T1,则不需要重新开启
				startLive(mCurrentVideoId);
			}
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
	 * 续直播失败，重新发起一个新的直播
	 * 
	 * @author jyf
	 */
	private void newStartLive() {
		GolukDebugUtils.e("", "newlive-----LiveActivity----liveCallBack_startLiveIsValid 服务器续播失败，需要重新开启直播:");
		isContinueLive = false;
		if (mApp.mIPCControlManager.isT1Relative()) {
			uploadLiveSuccess();
		} else {
			if (null != mLiveOperator) {
				mLiveOperator.stopLive();
			}
			mBaseHandler.sendEmptyMessage(MSG_H_START_NEW_LIVE);
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
			GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----LiveVideoDataCallBack----6666 : "
					+ liveData.playUrl);
			if (null != mRPVPalyVideo) {
				// 主动直播
				if (!mRPVPalyVideo.isPlaying()) {
					startVideoAndLive(liveData.playUrl);
				}
			}
		}
	}

	// 视频已经下线
	private void videoInValid() {
		LiveDialogManager.getManagerInstance().showSingleBtnDialog(AbstractLiveActivity.this,
				LiveDialogManager.DIALOG_TYPE_LIVE_OFFLINE, this.getString(R.string.user_dialog_hint_title),
				this.getString(R.string.str_live_over2));
		mBaseHandler.removeMessages(MSG_H_RETRY_REQUEST_DETAIL);
		mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		if (null != mLiveManager) {
			mLiveManager.cancelTimer();
		}
		mVideoLoading.setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy() {
		GolukDebugUtils.e("", "liveplay---onDestroy");
		if (null != mRPVPalyVideo) {
			mRPVPalyVideo.stopPlayback();
			mRPVPalyVideo.cleanUp();
			mRPVPalyVideo = null;
		}
		LiveDialogManager.getManagerInstance().dismissLiveBackDialog();
		dissmissAllDialog();
		LiveDialogManager.getManagerInstance().dismissTwoButtonDialog();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.live_back_btn) {
			// 返回
			preExit();
		} else if (id == R.id.share_btn) {
			if (this.isShareLive) {
				if (isSettingCallBack) {
					click_share(true);
				}
			} else {
				click_share(true);
			}
		} else if (id == R.id.live_more) {
			click_juBao();
		} else if (id == R.id.like_btn) {
			click_Like();
		} else if (id == R.id.live_location_btn) {
			// 定位
			toMyLocation();
		} else {
		}
	}

	// 点击 "举报"
	private void click_juBao() {
		if (!isShareLive) {
			LiveDialogManager.getManagerInstance().showDialog(this, LiveDialogManager.DIALOG_TYPE_CONFIRM);
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
				VideoSuqareManagerFn.VSquare_Req_VOP_Praise, json);
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
		return GolukApplication.getInstance().getVideoSquareManager().report(channel, videoid, reporttype);
	}

	/** 判断当前是否是直播 */
	public boolean isShareLive() {
		return isShareLive;
	}

	private void click_share(boolean isClick) {
		GolukDebugUtils.e("", "newlive-----share-------click_share ");
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
				GolukUtils.showToast(this, this.getString(R.string.str_share_fail));
			}
		} else {
			if (isClick) {
				LiveDialogManager.getManagerInstance().showShareProgressDialog(this,
						LiveDialogManager.DIALOG_TYPE_LIVE_SHARE, this.getString(R.string.user_dialog_hint_title),
						this.getString(R.string.str_request_share_address));
			}
		}
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
			return;
		}
		isAlreadClickOK = true;

		Drawable drawable = getResources().getDrawable(R.drawable.videodetail_like_press);
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
		return false;
	}

	@Override
	public void onPlayerCompletion(RtmpPlayerView rpv) {
		// 视频播放完成
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerCompletion : ");
		playerError(rpv);
	}

	@Override
	public void onPlayerBegin(RtmpPlayerView rpv) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerBegin : ");
		mVideoLoading.setVisibility(View.GONE);
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
			start90Timer();
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
		SharedPrefUtil.setIsLiveNormalExit(true);
		// 注册回调监听
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("live");
		mVideoSquareManager.removeVideoSquareManagerListener("live");
		// 移除监听
		mApp.removeLocationListener(TAG);
		mBaseHandler.removeMessages(MSG_H_TO_MYLOCATION);
		mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		mBaseHandler.removeMessages(MSG_H_RETRY_UPLOAD);
		mBaseHandler.removeMessages(MSG_H_RETRY_SHOW_VIEW);
		mBaseHandler.removeMessages(MSG_H_RETRY_REQUEST_DETAIL);
		mBaseHandler.removeMessages(MSG_H_PLAY_LOADING);
		mBaseHandler.removeMessages(MSG_H_TO_GETMAP_PERSONS);

		if (null != mLiveOperator) {
			mLiveOperator.stopLive();
		}

		dissmissAllDialog();

		freePlayer();

		LiveDialogManager.getManagerInstance().setDialogManageFn(null);
		GolukDebugUtils.e("", "next live------------------LIve----setDialogManageFn: set NULL");
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
		String message = this.isShareLive ? this.getString(R.string.str_live_exit_prompt) : this
				.getString(R.string.str_live_exit_prompt2);
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
			LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE,
					this.getString(R.string.str_live_start_progress_msg));
		}

		// 开始视频上传
		startLive(mCurrentVideoId);
		updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
		// 开始计时
		isSettingCallBack = true;
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
					LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE,
							this.getString(R.string.str_live_create));
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
					LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE,
							this.getString(R.string.str_live_create));
				}
			} else if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {
				exit();
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVE_SHARE:
			if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {
				// 取消
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_CONFIRM:

			final String reporttype = (String) data;
			boolean isSucess = report("1", getCurrentVideoId(), reporttype);
			if (isSucess) {
				GolukUtils.showToast(AbstractLiveActivity.this, this.getString(R.string.str_report_success));
			} else {
				GolukUtils.showToast(AbstractLiveActivity.this, this.getString(R.string.str_report_fail));
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
			// stopRTSPUpload();
			if (null != mLiveOperator) {
				this.mLiveOperator.stopLive();
			}
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
						LiveDialogManager.getManagerInstance().showLiveExitDialog(AbstractLiveActivity.this,
								LIVE_DIALOG_TITLE, this.getString(R.string.str_live_time_end));
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
						LiveDialogManager.getManagerInstance().showLiveExitDialog(AbstractLiveActivity.this,
								LIVE_DIALOG_TITLE, this.getString(R.string.str_live_over2));
					}
				}
				updateCountDown(GolukUtils.secondToString(current));
			}
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
		GolukDebugUtils.e("", "newlive-----share-------dealSnapCallBack callBack");
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
		mThumbBitmap = ImageManager.getBitmapFromCache(picName, 100, 100);
		String newFilePath = FileUtils.javaToLibPath(picName);
		String uploadJson = JsonUtil.getUploadSnapJson(mCurrentVideoId, newFilePath);
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----uploadImg-----6: " + uploadJson);
		boolean isSuccess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_LiveUploadPic, uploadJson);

		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----uploadImg-----7: " + isSuccess);

	}

	public void uploadImgCallBack(int success, Object param1, Object param2) {
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----uploadImgCallBack-----0: " + success + "  param1: "
				+ param1 + "   param2: " + param2);
	}

	private String getShareVideoId() {
		String vid = null;
		if (isShareLive) {
			vid = mCurrentVideoId;
		} else {
			if (!isKaiGeSucess) {
				vid = "";
			} else {
				vid = liveData.vid;
			}
		}
		return vid;
	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		if (!isSucess) {
			GolukUtils.showToast(this, this.getString(R.string.str_share_fail));
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
		if (event == VSquare_Req_VOP_GetShareURL_Video) {
			// 销毁对话框
			// 销毁对话框
			LiveDialogManager.getManagerInstance().dismissShareProgressDialog();
			if (1 != msg) {
				GolukUtils.showToast(this, this.getString(R.string.str_share_fail));
				return;
			}

			ShareDataBean dataBean = JsonUtil.parseShareCallBackData((String) param2);
			if (!dataBean.isSucess) {
				GolukUtils.showToast(this, this.getString(R.string.str_share_fail));
				return;
			}
			final String title = this.getString(R.string.str_wonderful_live);
			final String describe = getLiveUserName() + this.getString(R.string.str_colon)
					+ getShareDes(dataBean.describe);
			final String sinaTxt = title + this.getString(R.string.str_user_goluk);
			// 设置分享内容
			ThirdShareBean bean = new ThirdShareBean();
			bean.surl = dataBean.shareurl;
			bean.curl = dataBean.coverurl;
			bean.db = describe;
			bean.tl = title;
			bean.bitmap = mThumbBitmap;
			bean.realDesc = sinaTxt;
			bean.videoId = getShareVideoId();
			ProxyThirdShare sb = new ProxyThirdShare(AbstractLiveActivity.this, sharePlatform, bean);
			sb.showAtLocation(AbstractLiveActivity.this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
		}
	}

	/**
	 * 得到分享中视频描述(便于异常处理)
	 * 
	 * @param des
	 *            视频的原描述
	 * @author jyf
	 */
	private String getShareDes(String des) {
		if (TextUtils.isEmpty(des)) {
			return this.getString(R.string.str_live_default_describe);
		}
		return des;
	}

	/**
	 * 得到当前发起直播的用户名称
	 * 
	 * @author jyf
	 */
	private String getLiveUserName() {
		if (this.isShareLive) {
			return this.myInfo.nickname;
		} else {
			return this.currentUserInfo.nickname;
		}
	}

	@Override
	public void Live_CallBack(int state) {
		if (!this.isShareLive) {
			return;
		}
		GolukDebugUtils.e("", "newlive-----LiveActivity-----Live_CallBack state:" + state);
		switch (state) {
		case ILiveFnAdapter.STATE_SUCCESS:
			uploadLiveSuccess();
			break;
		case ILiveFnAdapter.STATE_FAILED:
			liveUploadVideoFailed();
			break;
		case ILiveFnAdapter.STATE_TIME_END:
			// 直播时间到
			liveEnd();
			if (!isAlreadExit) {
				LiveDialogManager.getManagerInstance().showLiveExitDialog(this, LIVE_DIALOG_TITLE,
						getString(R.string.str_live_time_end));
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		GolukDebugUtils.e("", "newlive-----LiveActivity----********-onLoadComplete requestType:" + requestType);
		if (IPageNotifyFn.PageType_LiveStart == requestType) {
			LiveDataInfo liveInfo = (LiveDataInfo) result;
			CallBack_StartLiveServer(true, liveInfo);
		}
	}

}