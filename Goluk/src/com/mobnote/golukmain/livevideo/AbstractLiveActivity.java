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

	/** ?????????????????? */
	private static String VIEW_SELF_PLAY = "";
	/** application */
	protected GolukApplication mApp = null;
	/** ???????????? */
	private TextView mLiveBackBtn = null;
	/** ???????????? */
	private Button mPauseBtn = null;
	/** title */
	private TextView mTitleTv = null;
	/** ??????loading */
	private RelativeLayout mVideoLoading = null;
	/** ???????????? */
	private RelativeLayout mPlayLayout = null;
	/** ?????????????????????????????? */
	public RtmpPlayerView mRPVPalyVideo = null;
	/** ???????????? */
	private String mFilePath = "";
	/** ???????????? ???????????????????????? true/false ??????/??????????????? */
	protected boolean isShareLive = true;
	/** ?????????????????? */
	private boolean isStartLive = false;
	/** ????????????id */
	private String liveVid;
	/** ???????????????????????? (???)(??????????????????????????????????????????) */
	private int mLiveCountSecond = 60;
	/** ????????????????????? */
	private TextView mLiveCountDownTv = null;
	/** ???????????? */
	private TextView mDescTv = null;
	/** ???????????? */
	private TextView mLookCountTv = null;
	private ImageView mMoreImg = null;
	protected UserInfo currentUserInfo = null;
	/** ?????????????????? ??????????????????????????? */
	protected UserInfo myInfo = null;
	/** ??????????????? */
	private boolean isContinueLive = false;
	private LayoutInflater mLayoutFlater = null;
	private RelativeLayout mRootLayout = null;
	/** ??????????????????????????? */
	private boolean isAlreadClickOK = false;
	private TimerManager mLiveManager = null;
	/** ???????????????????????? */
	private boolean isAlreadExit = false;
	private String mCurrentVideoId = null;
	/** -1/0/1 ?????????/?????????/?????? */
	protected int mCurrentLocationType = LOCATION_TYPE_UNKNOW;
	/** ?????????????????? */
	private int mCurrentOKCount = 0;
	/** ?????????????????? */
	private boolean isCanVoice = true;
	private ImageView mHead = null;
	/** ???????????? */
	private ImageView mAuthenticationImg = null;
	protected RelativeLayout mMapRootLayout = null;
	/** ??????????????????????????? */
	private boolean isUploadSucessed = false;
	/** ?????????????????????????????? */
	private boolean isKaiGeSucess = false;
	private LiveDataInfo liveData = null;
	/** ?????????????????? */
	private boolean isTryingReUpload = false;
	private RelativeLayout mVLayout = null;
	/** ?????????????????? */
	private LiveSettingBean mSettingData = null;
	/** ?????????????????????????????? */
	protected boolean isLiveUploadTimeOut = false;
	/** ??????????????????????????? */
	public String mRecordVideFileName = "";
	/** ???????????????????????? */
	public boolean isRecording = false;
	private VideoSquareManager mVideoSquareManager = null;
	private SharePlatformUtil sharePlatform;
	/** ?????????????????? */
	private boolean isSettingCallBack = false;
	/** ???????????? */
	private Button mLocationBtn = null;
	/** ???????????? */
	private TextView mNickName = null;
	/** ?????? */
	private Button zanBtn = null;
	private Button mShareBtn = null;
	/** ?????????????????? */
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
		// ??????GolukApplication??????
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "LiveVideo");

		sharePlatform = new SharePlatformUtil(this);

		// ???????????????????????????
		getURL();
		// ????????????
		getIntentData();
		// ???????????????
		initView();
		// ????????????
		setViewInitData();
		// ???????????????
		initMap();
		// ????????????????????????
		myInfo = mApp.getMyInfo();
		// ???????????????????????????
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
				GolukDebugUtils.e("", "newlive-----LiveActivity----onCreate---????????????---: ");
				// ?????????
				// ?????????????????????
				LiveSettingPopWindow lpw = new LiveSettingPopWindow(this, mRootLayout);
				mSettingData = lpw.getCurrentSetting();
				startLiveLook(myInfo);
				LiveDialogManager.getManagerInstance().showProgressDialog(this, LIVE_DIALOG_TITLE,
						this.getString(R.string.str_live_retry_live));
				isSettingCallBack = true;
			} else {
				// ??????????????????
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
			// ?????????90????????????????????????????????????
			start90Timer();
			startLiveLook(currentUserInfo);
			updateCount(Integer.parseInt(currentUserInfo.zanCount), Integer.parseInt(currentUserInfo.persons));
		}
		setCallBackListener();
	}

	// ?????????90??????
	private void start90Timer() {
		mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		mBaseHandler.sendEmptyMessageDelayed(MSG_H_UPLOAD_TIMEOUT, DURATION_TIMEOUT);
	}

	private void setCallBackListener() {
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
		// ??????????????????
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

	// ?????????????????????????????????
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
	 * ???????????????????????????
	 */
	public void pointDataCallback(int success, Object obj) {
		if (1 != success) {
			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type:  sucess:" + success);
			// ???????????????????????????
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);
			return;
		}
		final String str = (String) obj;
		GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type111:  str???" + str);
		try {
			JSONObject json = new JSONObject(str);
			// ????????????
			JSONArray memberJsonArray = json.getJSONArray("info");

			UserInfo tempUserInfo = null;
			UserInfo tempMyInfo = null;

			int length = memberJsonArray.length();

			for (int i = 0; i < length; i++) {
				JSONObject tempObj = memberJsonArray.getJSONObject(i);
				String aid = tempObj.getString("aid");
				if (this.isShareLive) {
					if (aid.equals(myInfo.aid)) {
						// ??????????????????
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
				// ???????????????????????????,????????????????????????
				if (null == tempMyInfo) {
					// ???????????????????????????
					mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);
					return;
				}
				this.updateCount(Integer.parseInt(tempMyInfo.zanCount), Integer.parseInt(tempMyInfo.persons));
				GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback 3333333:  ????????????????????? zanCount???"
						+ tempMyInfo.zanCount + "	permson:" + tempMyInfo.persons);
				// ???????????????????????????
				mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);
				return;
			}
			if (null == tempUserInfo) {
				GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type44444:  ???");
				// ???????????????????????????
				mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);
				return;
			}
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----pointDataCallback----aid  : " + tempUserInfo.aid
					+ " lon:" + tempUserInfo.lon + " lat:" + tempUserInfo.lat);
			mMapTools.updatePosition(tempUserInfo.aid, Double.parseDouble(tempUserInfo.lon),
					Double.parseDouble(tempUserInfo.lat), true);
			currentUserInfo.lat = tempUserInfo.lat;
			currentUserInfo.lon = tempUserInfo.lon;
			GolukDebugUtils.e(null, "jyf-------live----LiveActivity--pointDataCallback type55555:  ???");
			// ??????????????????????????????????????????
			this.updateCount(Integer.parseInt(tempUserInfo.zanCount), Integer.parseInt(tempUserInfo.persons));
			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type66666:  ???");
		} catch (Exception e) {
			e.printStackTrace();
			GolukDebugUtils.e("", "jyf-------live----LiveActivity--pointDataCallback type999999:  Exception ???");
		}

		// ???????????????????????????
		mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_GETMAP_PERSONS, BaiduMapView.mTiming);

	}

	private void getIntentData() {
		// ??????????????????
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

	// ?????????????????????,??????????????? (???????????????????????????????????????)
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
		// ??????????????????
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

	// ?????????????????????
	public void startLiveLook(UserInfo userInfo) {
		if (isLiveUploadTimeOut) {
			return;
		}
		String condi = "{\"uid\":\"" + userInfo.uid + "\",\"desAid\":\"" + userInfo.aid + "\"}";
		GolukDebugUtils.e("", "newlive-----LiveActivity----startLiveLook---??????????????????---: " + condi);
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
	 * ???????????????
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
		// ????????????????????????
		mRPVPalyVideo.setPlayerListener(this);
		mRPVPalyVideo.setBufferTime(1000);
		mRPVPalyVideo.setConnectionTimeout(30000);
		// ?????????????????????????????????
		// ????????????
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
			// ????????????????????????????????????????????????????????????
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
			// ????????????????????????????????????????????????
			// ????????????
			startLiveForServer();
			break;
		case MSG_H_TO_MYLOCATION:
			// toMyLocation();
			break;
		case MSG_H_TO_GETMAP_PERSONS:
			EventBus.getDefault().post(new EventMapQuery(EventConfig.LIVE_MAP_QUERY));
			break;
		case MSG_H_MAPLOAD_FINISH:
			// ??????????????????
			drawPersonsHead();
			break;
		}
	}

	/**
	 * ????????????????????????
	 * 
	 * @param aid
	 * @author xuhw
	 * @date 2015???3???8???
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
		// ?????????????????? ???????????????
		if (!isStartTimer) {
			isStartTimer = true;
			if (!this.isContinueLive) {
				mLiveManager.startTimer(mLiveCountSecond, true);
			}
		}
		isUploadSucessed = true;
		isTryingReUpload = false;
		// ??????90???
		mBaseHandler.removeMessages(MSG_H_UPLOAD_TIMEOUT);
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		if (!isRequestedForServer) {
			// ????????????????????????
			if (!isContinueLive) {
				// ??????????????????????????????
				mBaseHandler.sendEmptyMessage(MSG_H_REQUEST_SERVER);
			}
		}
	}

	/**
	 * ??????????????????????????? ????????????V, ??????
	 * 
	 * @param userLabel
	 *            ?????????????????????
	 * @author jyf
	 */
	private void setAuthentication(UserLabelBean userLabel) {
		if (null == userLabel) {
			mAuthenticationImg.setVisibility(View.GONE);
			return;
		}
		// ?????????????????????
		if (null != userLabel.approvelabel && "1".equals(userLabel.approvelabel)) {
			mAuthenticationImg.setVisibility(View.VISIBLE);
			mAuthenticationImg.setBackgroundResource(R.drawable.authentication_bluev_icon);
			return;
		}
		// ??????????????????V
		if (null != userLabel.headplusv && "1".equals(userLabel.headplusv)) {
			mAuthenticationImg.setVisibility(View.VISIBLE);
			mAuthenticationImg.setBackgroundResource(R.drawable.authentication_yellowv_icon);
			return;
		}
		// ?????????????????????
		if (null != userLabel.tarento && "1".equals(userLabel.tarento)) {
			mAuthenticationImg.setVisibility(View.VISIBLE);
			mAuthenticationImg.setBackgroundResource(R.drawable.authentication_star_icon);
			return;
		}

		mAuthenticationImg.setVisibility(View.GONE);
	}

	/**
	 * ????????????????????????????????????
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

	// ??????????????????
	private void liveUploadVideoFailed() {
		GolukDebugUtils.e("", "newlive-----LiveActivity-----liveUploadVideoFailed :");
		if (!mApp.mIPCControlManager.isT1Relative()) {
			// T1???????????????????????????
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
				// ??????Loading
				if (!isAlreadExit) {
					LiveDialogManager.getManagerInstance().showProgressDialog(this, "??????",
							getString(R.string.str_live_retry_upload_msg));
				}
			}
			// ?????????90?????????????????????????????????
			start90Timer();
			if (!mApp.mIPCControlManager.isT1Relative()) {
				// ????????????????????????
				mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 1000);
			} else {
				if (!mLiveOperator.liveState()) {
					// ????????????????????????
					mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 1000);
				}
			}
			isTryingReUpload = true;
		} else {
			LiveDialogManager.getManagerInstance().dismissProgressDialog();
			// ???????????????????????????????????????
			GolukDebugUtils.e("", "newlive-----LiveActivity-----liveUploadVideoFailed :---alread");
			if (!isAlreadExit && GolukUtils.isActivityAlive(this)) {
				LiveDialogManager.getManagerInstance().showTwoBtnDialog(this,
						LiveDialogManager.DIALOG_TYPE_LIVE_RELOAD_UPLOAD, LIVE_DIALOG_TITLE,
						getString(R.string.str_live_upload_first_error));
			}
		}
	}

	// ?????????????????????
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
	 * ?????????????????????
	 */
	private void startVideoAndLive(String url) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----startVideoAndLive----url : " + url);
		if (null == mRPVPalyVideo) {
			return;
		}
		// ???????????????
		if (isShareLive) {
			// ?????????????????????
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
		GolukDebugUtils.e("", "newlive-----LiveActivity-----liveFailedStart :--??????????????????");
		if (isLive) {
			startLiveFailed();
		}
	}

	public void CallBack_StartLiveServer(boolean isLive, LiveDataInfo dataInfo) {
		if (isAlreadExit) {
			// ??????????????????
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

	// ??????????????????
	private void startScreenShot() {
		if (this.isShareLive) {
			// ???????????? ???????????????????????????
			GolukApplication.getInstance().getIPCControlManager().screenShot();
		}
	}

	// ?????????????????????????????????
	public void callBack_LiveLookStart(boolean isLive, int success, Object param1, Object param2) {
		if (isAlreadExit) {
			// ??????????????????
			return;
		}
		if (IPageNotifyFn.PAGE_RESULT_SUCESS != success) {
			GolukDebugUtils.e("", "newlive-----LiveOperateVdcp-----callBack_LiveLookStart-------:  ");
			liveFailedStart(isLive);
			return;
		}
		final String data = (String) param2;
		// ??????????????????
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
			// ???????????? ???????????????????????????
			GolukApplication.getInstance().getIPCControlManager().screenShot();
		}
		startUploadMyPosition();
		if (mIsFirstSucess) {
			this.click_share(false);
			mIsFirstSucess = false;
		}
	}

	// ????????????
	private void startUploadMyPosition() {
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StartUploadPosition, "");
	}

	// ???????????????????????????????????????
	private void liveCallBack_startLiveIsValid(int success, Object obj) {
		if (isAlreadExit) {
			// ??????????????????
			return;
		}
		// ??????????????????????????????
		LiveDialogManager.getManagerInstance().dismissProgressDialog();
		if (1 != success) {
			// ??????????????????????????? ???????????????????????????
			newStartLive();
			return;
		}
		final String data = (String) obj;
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----liveCallBack_startLiveIsValid----222222 : " + data);
		// ????????????
		liveData = JsonUtil.parseLiveDataJson(data);
		if (null == liveData) {
			newStartLive();
			return;
		}

		if (200 != liveData.code) {
			// ??????????????????
			// ????????????,??????????????????
			newStartLive();
		} else {
			// ????????????????????????,???????????????????????????????????????
			if (!mApp.mIPCControlManager.isT1Relative()) {
				// ?????????T1,????????????????????????
				startLive(mCurrentVideoId);
			}
			startUploadMyPosition();
			isSettingCallBack = true;
			this.isKaiGeSucess = true;
			mLiveCountSecond = liveData.restTime;
			mDescTv.setText(liveData.desc);
			mLiveManager.cancelTimer();
			// ??????timer????????????
			updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
			mLiveManager.startTimer(mLiveCountSecond, true);
		}
	}

	/**
	 * ????????????????????????????????????????????????
	 * 
	 * @author jyf
	 */
	private void newStartLive() {
		GolukDebugUtils.e("", "newlive-----LiveActivity----liveCallBack_startLiveIsValid ????????????????????????????????????????????????:");
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
	 * ??????????????????
	 * 
	 * @param obj
	 */
	public void LiveVideoDataCallBack(int success, Object obj) {
		GolukDebugUtils.e("", "????????????????????????--LiveVideoDataCallBack: success: " + success);
		if (isAlreadExit) {
			// ??????????????????
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
		// ????????????
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
			// ??????????????????
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
				// ????????????
				if (!mRPVPalyVideo.isPlaying()) {
					startVideoAndLive(liveData.playUrl);
				}
			}
		}
	}

	// ??????????????????
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
			// ??????
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
			// ??????
			toMyLocation();
		} else {
		}
	}

	// ?????? "??????"
	private void click_juBao() {
		if (!isShareLive) {
			LiveDialogManager.getManagerInstance().showDialog(this, LiveDialogManager.DIALOG_TYPE_CONFIRM);
		}
	}

	/**
	 * ??????
	 * 
	 * @param channel
	 *            ???????????????1.???????????? 2.?????? 3.?????? 4.QQ
	 * @param videoid
	 *            ??????id
	 * @param type
	 *            ???????????????0.???????????? 1.??????
	 * @return true:?????????????????? false:??????
	 * @author xuhw
	 * @date 2015???4???17???
	 */
	public boolean clickPraise(String channel, String videoid, String type) {
		String json = JsonCreateUtils.getClickPraiseRequestJson(channel, videoid, type);
		return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.VSquare_Req_VOP_Praise, json);
	}

	/**
	 * ??????
	 * 
	 * @param channel
	 *            ???????????????1.???????????? 2.?????? 3.?????? 4.QQ
	 * @param videoid
	 *            ??????id
	 * @param reporttype
	 *            ???????????????1.???????????? 2.???????????? 3.???????????? 4.????????????
	 * @return true:?????????????????? false:??????
	 * @author xuhw
	 * @date 2015???4???17???
	 */
	public boolean report(String channel, String videoid, String reporttype) {
		return GolukApplication.getInstance().getVideoSquareManager().report(channel, videoid, reporttype);
	}

	/** ??????????????????????????? */
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
		zanBtn.setCompoundDrawables(drawable, null, null, null); // ??????????????????
		mCurrentOKCount++;
		if (null != zanBtn) {
			zanBtn.setText("" + mCurrentOKCount);
			zanBtn.setText("" + mCurrentOKCount);
		}
		boolean isSucess = clickPraise("1", getCurrentVideoId(), "1");
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----click_OK----isSucess : " + isSucess);
	}

	/**
	 * ??????runnable
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
		// ??????????????????
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayerError : " + arg2 + "  "
				+ arg3);
		playerError(rpv);
		// ????????????
		return false;
	}

	@Override
	public void onPlayerCompletion(RtmpPlayerView rpv) {
		// ??????????????????
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
			// ??????timer????????????
			updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
			mLiveManager.startTimer(mLiveCountSecond, true);
		}
	}

	@Override
	public void onPlayBuffering(RtmpPlayerView arg0, boolean start) {
		GolukDebugUtils.e(null, "jyf----20150406----LiveActivity----PlayerCallback----onPlayBuffering : " + start);
		if (start) {
			// ????????????
			mBaseHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
		} else {
			// ????????????
			mVideoLoading.setVisibility(View.GONE);
		}
	}

	@Override
	public void onGetCurrentPosition(RtmpPlayerView arg0, int arg1) {
	}

	// ???????????????
	private void playerError(RtmpPlayerView rpv) {
		if (isLiveUploadTimeOut) {
			// 90????????????????????????
			return;
		}
		hidePlayer();
		if (isShareLive) {
			// UI????????????loading
			mBaseHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
			// ???????????????????????????
			mBaseHandler.removeMessages(MSG_H_RETRY_SHOW_VIEW);
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_SHOW_VIEW, 5000);
		} else {
			// UI????????????
			mBaseHandler.sendEmptyMessage(MSG_H_PLAY_LOADING);
			// ??????90???
			start90Timer();
			// ????????????????????????
			mBaseHandler.sendEmptyMessage(MSG_H_RETRY_REQUEST_DETAIL);
		}
	}

	/**
	 * ???????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???21???
	 */
	private void hidePlayer() {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVLayout.getLayoutParams();
		lp.width = lp.height = 1;
		lp.leftMargin = 2000;
		mVLayout.setLayoutParams(lp);
	}

	/**
	 * ???????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???21???
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
	 * ???????????????????????????
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
		// ??????????????????
		GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("live");
		mVideoSquareManager.removeVideoSquareManagerListener("live");
		// ????????????
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
			// ??????????????????????????????????????????????????????
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StopUploadPosition,
					"");
			if (isKaiGeSucess) {
				// ?????????????????????????????????????????????????????????????????????
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

		// ??????????????????
		startLive(mCurrentVideoId);
		updateCountDown(GolukUtils.secondToString(mLiveCountSecond));
		// ????????????
		isSettingCallBack = true;
	}

	// ?????????????????????
	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		switch (dialogType) {
		case LiveDialogManager.DIALOG_TYPE_EXIT_LIVE:
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				// ??????????????????
				exit();
			}
			break;
		case LiveDialogManager.DIALOG_TYPE_LIVEBACK:
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				// ??????????????????
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
				// ????????????
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
				// ??????
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
			// ???????????????????????????
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_Command_StopUploadPosition,
					"");
			if (isKaiGeSucess) {
				// ??????????????????????????????
				mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_LiveStop,
						JsonUtil.getStopLiveJson());
			}
			liveStopUploadVideo();
		}
	}

	// timer????????????
	@Override
	public void CallBack_timer(int function, int result, int current) {
		if (isShareLive) {
			if (10 == function) {
				if (TimerManager.RESULT_FINISH == result) {
					// ???????????????
					liveEnd();
					if (!isAlreadExit) {
						LiveDialogManager.getManagerInstance().showLiveExitDialog(AbstractLiveActivity.this,
								LIVE_DIALOG_TITLE, this.getString(R.string.str_live_time_end));
					}
				}
				GolukDebugUtils.e("aaaaaa", "-------------aaaaa-----stop------");

				// ????????????
				updateCountDown(GolukUtils.secondToString(current));
			}
		} else {
			// ???????????????
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
	 * ??????VDCP????????????
	 * 
	 * @param msg
	 *            ?????????id
	 * @param param1
	 *            ???? 0:?????????????????? ???0:????????????
	 * @param param2
	 *            ???????????????json?????????
	 * @author xuhw
	 * @date 2015???3???17???
	 */
	private void callBack_VDCP(int msg, int param1, Object param2) {
		GolukDebugUtils.d("", "m8sBtn===IPC_VDCPCmd_TriggerRecord===callBack_VDCP=====param1=   " + param1
				+ "     ==param2=" + param2 + "	msg:" + msg);
		switch (msg) {
		// ????????????
		case IPC_VDCPCmd_SnapPic:
			dealSnapCallBack(param1, param2);
			break;
		default:
			break;
		}
	}

	private void dealSnapCallBack(int param1, Object param2) {
		GolukDebugUtils.e("", "newlive-----share-------dealSnapCallBack callBack");
		GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----????????????????????????");
		if (0 != param1) {
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----????????????????????????-------");
			return;
		}
		// ?????????????????????fs1:/IPC_Snap_Pic/snapPic.jpg
		final String imageFilePath = (String) param2;
		if (TextUtils.isEmpty(imageFilePath)) {
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----????????????????????????");
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

		// ???????????????
		String dirname = Environment.getExternalStorageDirectory() + File.separator + "goluk" + File.separator
				+ "goluk" + File.separator + "screenshot";
		GFileUtils.makedir(dirname);
		String picName = dirname + File.separator + timename + ".jpg";
		GFileUtils.compressImageToDisk(path, picName);
		File file = new File(picName);
		if (!file.exists()) {
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----callBack_VDCP----????????????????????????------22222");
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

	// ????????????????????????????????????
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
			// ???????????????
			// ???????????????
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
			// ??????????????????
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
	 * ???????????????????????????(??????????????????)
	 * 
	 * @param des
	 *            ??????????????????
	 * @author jyf
	 */
	private String getShareDes(String des) {
		if (TextUtils.isEmpty(des)) {
			return this.getString(R.string.str_live_default_describe);
		}
		return des;
	}

	/**
	 * ???????????????????????????????????????
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
			// ???????????????
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