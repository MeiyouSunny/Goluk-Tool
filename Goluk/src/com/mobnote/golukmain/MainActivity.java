package com.mobnote.golukmain;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.Toast;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerAdapter;
import cn.com.mobnote.module.location.LocationNotifyAdapter;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.page.PageNotifyAdapter;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.talk.TalkNotifyAdapter;
import cn.com.mobnote.module.videosquare.VideoSquareManagerAdapter;
import cn.com.tiros.api.Tapi;
import cn.com.tiros.baidu.LocationAddressDetailBean;
import cn.com.tiros.debug.GolukDebugUtils;

import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.widget.FragmentTabHost;
import com.mobnote.application.GlobalWindow;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBindFinish;
import com.mobnote.eventbus.EventBindResult;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeleteVideo;
import com.mobnote.eventbus.EventFollowPush;
import com.mobnote.eventbus.EventMapQuery;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.eventbus.EventPhotoUpdateDate;
import com.mobnote.eventbus.EventUpdateAddr;
import com.mobnote.eventbus.EventUserLoginRet;
import com.mobnote.eventbus.EventWifiAuto;
import com.mobnote.eventbus.EventWifiConnect;
import com.mobnote.eventbus.EventWifiState;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.comment.CommentTimerManager;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.followed.FragmentFollowed;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.live.GetBaiduAddress;
import com.mobnote.golukmain.live.LiveActivity;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.live.GetBaiduAddress.IBaiduGeoCoderFn;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.livevideo.AbstractLiveActivity;
import com.mobnote.golukmain.livevideo.BaidumapLiveActivity;
import com.mobnote.golukmain.livevideo.GooglemapLiveActivity;
import com.mobnote.golukmain.msg.MessageBadger;
import com.mobnote.golukmain.msg.MsgCenterCounterRequest;
import com.mobnote.golukmain.msg.bean.MessageCounterBean;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.golukmain.special.SpecialListActivity;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.golukmain.xdpush.GolukNotification;
import com.mobnote.golukmain.xdpush.StartAppBean;
import com.mobnote.golukmain.xdpush.XingGeMsgBean;
import com.mobnote.golukmobile.GuideActivity;
import com.mobnote.manager.MessageManager;
import com.mobnote.receiver.NetworkStateReceiver;
import com.mobnote.util.CrashReportUtil;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.wifibind.WifiConnCallBack;
import com.mobnote.wifibind.WifiConnectManager;
import com.mobnote.wifibind.WifiRsBean;
import com.rd.car.CarRecorderManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import de.greenrobot.event.EventBus;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class MainActivity extends BaseActivity implements WifiConnCallBack, ILiveDialogManagerFn, IBaiduGeoCoderFn,
		IRequestResultListener {

	/** ??????????????????20????????????????????????IPC?????? */
	private final int MSG_H_WIFICONN_TIME = 100;
	/** application */
	public GolukApplication mApp = null;

	/** wifi??????manage */
	private WifiConnectManager mWac = null;

	/** ?????????????????? **/
	public SharedPreferences mPreferencesAuto;
	public boolean isFirstLogin;

	/** ????????? */
	public static final int WIFI_STATE_FAILED = 0;
	/** ????????? */
	public static final int WIFI_STATE_CONNING = 1;
	/** ?????? */
	public static final int WIFI_STATE_SUCCESS = 2;

	/** ??????ipc???????????? */
	Animation anim = null;

	private SharedPreferences mPreferences = null;
	private Editor mEditor = null;
	private long exitTime = 0;

	private View mUnreadTips;
	private ImageView mFollowedVideoTipIV;

	private WifiManager mWifiManager = null;
	// Play video sync from camera completion sound
	private SoundPool mSoundPool;
	private final static String TAG = "MainActivity";
	private StartAppBean mStartAppBean = null;
	/** ????????????????????????????????????????????????????????????????????????????????? */
	private WifiRsBean mCurrentConnBean = null;
	private FragmentTabHost mTabHost;

	private ImageView mCarrecorderIv;
	private ViewStub mGuideMainViewStub;
	private SharePlatformUtil mSharePlatform = null;

	private void playDownLoadedSound() {
		if (null != mSoundPool) {
			mSoundPool.load(this, R.raw.ec_alert5, 1);

			mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					soundPool.play(sampleId, 1, 1, 1, 0, 1);
				}
			});
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		// super.onSaveInstanceState(outState);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		GolukDebugUtils.e("", "crash zh start App ------ MainActivity-----onCreate------------:");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);

		initView();

		// ?????????SDK????????????????????????context???????????????ApplicationContext
		// ?????????????????????setContentView??????????????????
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		// Register EventBus
		EventBus.getDefault().register(this);
		initThirdSDK();

		// ??????GolukApplication??????
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "Main");
		mApp.initLogic();
		// ???????????????,??????????????????
		mApp.startTime = System.currentTimeMillis();
		// ???????????????,??????????????????
		init();

		UserInfo userInfo = mApp.getMyInfo();
		if (null != userInfo) {
			mApp.mCurrentUId = userInfo.uid;
			mApp.mCurrentAid = userInfo.aid;
		}

		// ??????SharedPreFerences??????????????????,??????SharedPreFerences????????????????????????????????????
		SharedPreferences preferences = getSharedPreferences("golukmark", MODE_PRIVATE);
		// ??????????????????,??????????????????,??????????????????,???true???????????????
		boolean isFirstIndex = preferences.getBoolean("isFirstIndex", true);
		if (isFirstIndex) { // ????????????????????????
			mGuideMainViewStub.inflate();
			Editor editor = preferences.edit();
			editor.putBoolean("isFirstIndex", false);
			// ????????????
			editor.commit();
		}

		// ?????????????????????????????? ?????????????????????????????????
		mWac = new WifiConnectManager(mWifiManager, this);
		mCurrentConnBean = mWac.readConfig();
		refreshIpcDataToFile();

		// ??????????????????????????????
		if (mApp.isBindSucess()) {
			startWifi();
			// ??????????????????
			autoConnWifi();
			// ??????IPC????????????
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_WIFICONN_TIME, 40 * 1000);
		} else {
			wifiConnectFailed();
		}

		// ????????????????????????????????????????????????????????????????????????
		mPreferencesAuto = getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferencesAuto.getBoolean("FirstLogin", true);
		if (!isFirstLogin && !mApp.isUserLoginSucess) {
			mApp.mUser.initAutoLogin();
		}

		GetBaiduAddress.getInstance().setCallBackListener(this);

		// ?????????????????????
		Intent itStart_have = getIntent();
		if (null != itStart_have.getStringExtra("userstart")) {
			String start_have = itStart_have.getStringExtra("userstart").toString();
			if ("start_have".equals(start_have)) {
				Intent intent = null;
				if (GolukApplication.getInstance().isInteral() == false) {
					intent = new Intent(this, InternationUserLoginActivity.class);
				} else {
					intent = new Intent(this, UserLoginActivity.class);
				}
				// ?????????????????????
				intent.putExtra("isInfo", "main");
				mPreferences = getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
				mEditor = mPreferences.edit();
				mEditor.putString("toRepwd", "start");
				mEditor.commit();
				// ???????????????????????????????????????????????????????????????
				if (!mApp.loginoutStatus) {// ??????
					// ????????????????????????????????????
					mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
					String phone = mPreferences.getString("setupPhone", "");// ??????????????????????????????
					intent.putExtra("startActivity", phone);
					startActivity(intent);
				} else {
					startActivity(intent);
				}
			}
		}

		dealPush(itStart_have);

		if (NetworkStateReceiver.isNetworkAvailable(this)) {
			notifyLogicNetWorkState(true);
		}
		GolukUtils.getMobileInfo(this);

		// msgRequest();
		mSharePlatform = new SharePlatformUtil(this);
	}

	private void initView() {
		mGuideMainViewStub = (ViewStub) findViewById(R.id.viewstub_guide_main);
		mGuideMainViewStub.setOnInflateListener(new OnInflateListener() {

			@Override
			public void onInflate(ViewStub stub, View inflated) {
				inflated.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						mGuideMainViewStub.setVisibility(View.GONE);
						return false;
					}
				});
			}
		});

		LayoutInflater inflater = LayoutInflater.from(this);
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.fl_main_tab_content);

		Bundle b = new Bundle();
		b.putString("key", "Discover");
		LinearLayout discover = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_discover, null);
		mTabHost.addTab(mTabHost.newTabSpec("Discover").setIndicator(discover), FragmentDiscover.class, b);

		b = new Bundle();
		b.putString("key", "Follow");
		RelativeLayout follow = (RelativeLayout) inflater.inflate(R.layout.main_tab_indicator_follow, null);
		mTabHost.addTab(mTabHost.newTabSpec("Follow").setIndicator(follow), FragmentFollowed.class, b);
		mFollowedVideoTipIV = (ImageView) follow.findViewById(R.id.iv_new_followed_video_tips);

		b = new Bundle();
		b.putString("key", "CarRecorder");
		LinearLayout carRecorder = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_carrecorder, null);
		mCarrecorderIv = (ImageView) carRecorder.findViewById(R.id.tab_host_carrecorder_iv);
		mTabHost.addTab(mTabHost.newTabSpec("CarRecorder").setIndicator(carRecorder), null, b);

		b = new Bundle();
		b.putString("key", "Album");
		b.putString("platform", "0");
		LinearLayout album = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_album, null);
		mTabHost.addTab(mTabHost.newTabSpec("Album").setIndicator(album), FragmentAlbum.class, b);

		b = new Bundle();
		b.putString("key", "Mine");
		RelativeLayout mine = (RelativeLayout) inflater.inflate(R.layout.main_tab_indicator_mine, null);
		mUnreadTips = mine.findViewById(R.id.iv_unread_tips);
		mTabHost.addTab(mTabHost.newTabSpec("Mine").setIndicator(mine), FragmentMine.class, b);
		TabWidget widget = mTabHost.getTabWidget();
		widget.setDividerDrawable(null);
		mTabHost.getTabWidget().setBackgroundResource(R.color.color_main_tab_bg);
		View lineView = new View(this);
		lineView.setBackgroundResource(R.color.color_list_divider);
		LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3);
		lineView.setLayoutParams(lineParams);
		mTabHost.addView(lineView);

		for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
			mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = (int) this.getResources().getDimension(
					R.dimen.mainactivity_bottom_height);
		}

		mTabHost.getTabWidget().getChildTabViewAt(2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CarRecorderActivity.class);
				startActivity(intent);
			}
		});

		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if ("Follow".equals(tabId)) {
					mFollowedVideoTipIV.setVisibility(View.GONE);
				}
			}
		});
	}

	public void setTabHostVisibility(boolean visible) {
		if(visible) {
			mTabHost.setVisibility(View.VISIBLE);
		} else {
			mTabHost.setVisibility(View.GONE);
		}
	}

	private void msgRequest() {
		if (GolukApplication.getInstance().isUserLoginSucess) {
			MsgCenterCounterRequest msgCounterReq = new MsgCenterCounterRequest(IPageNotifyFn.PageType_MsgCounter, this);
			msgCounterReq.get("100", GolukApplication.getInstance().mCurrentUId, "", "", "");
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		GolukDebugUtils.i("newintent", "-------str------------" + intent.getStringExtra("showMe"));
		if (null != intent.getStringExtra("showMe")) {
			String str = intent.getStringExtra("showMe").toString();
			if ("showMe".equals(str)) {

			}
		}

		dealPush(intent);
	}

	/**
	 * ??????????????????
	 * 
	 * @param intent
	 * @author jyf
	 */
	private void dealPush(Intent intent) {
		if (null == intent) {
			return;
		}
		final String from = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_FROM);
		GolukDebugUtils.e("", "jyf----MainActivity-----from: " + from);
		if (null != from && !"".equals(from) && from.equals("notication")) {
			String pushJson = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_JSON);

			GolukDebugUtils.e("", "jyf----MainActivity-----pushJson: " + pushJson);
			XingGeMsgBean bean = JsonUtil.parseXingGePushMsg(pushJson);
			if (null != bean) {
				GolukNotification.getInstance().dealAppinnerClick(this, bean);
			}
			// GolukUtils.showToast(this, "?????????????????? :" + pushJson);
		}
		// ??????????????????App
		mStartAppBean = (StartAppBean) intent.getSerializableExtra(GuideActivity.KEY_WEB_START);
		dealWebStart();
	}

	private void dealWebStart() {
		GolukDebugUtils.e("", "start App: MainActivity:------------: 11111");
		if (null == mStartAppBean) {
			return;
		}
		String type = mStartAppBean.type;
		String title = mStartAppBean.title;
		String id = mStartAppBean.id;

		if ("1".equals(type)) {
			// ?????????
			Intent intent = new Intent(this, VideoDetailActivity.class);
			intent.putExtra(VideoDetailActivity.VIDEO_ID, id);
			intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
			startActivity(intent);
		} else if ("2".equals(type)) {
			// ??????
			Intent intent = new Intent(this, SpecialListActivity.class);
			intent.putExtra("ztid", id);
			intent.putExtra("title", title);
			startActivity(intent);
		}

		mStartAppBean = null;
	}

	/**
	 * ??????????????????SDK
	 * 
	 * @author jyf
	 * @date 2015???6???17???
	 */
	private void initThirdSDK() {
		// ??????umeng????????????(??????????????????????????????????????????????????????)
		MobclickAgent.setDebugMode(false);
		MobclickAgent.setCatchUncaughtExceptions(false);
		// ???????????????????????? ?????????SDK
		String appId = CrashReportUtil.BUGLY_RELEASE_APPID_GOLUK;
		boolean isDebug = false;
		if (GolukUtils.isTestServer()) {
			appId = CrashReportUtil.BUGLY_DEV_APPID_GOLUK;
			isDebug = true;
		}
		CrashReport.initCrashReport(getApplicationContext(), appId, isDebug);
		final String mobileId = Tapi.getMobileId();
		CrashReport.setUserId(mobileId);
		GolukDebugUtils.e("", "jyf-----MainActivity-----mobileId:" + mobileId);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Sina or facebook sso callback
		if (null != mSharePlatform) {
			mSharePlatform.onActivityResult(requestCode, resultCode, data);
		}
	}

	public SharePlatformUtil getSharePlatform() {
		return mSharePlatform;
	}

	/**
	 * ???????????????,??????????????????,????????????
	 */
	private void init() {
		boolean hotPointState = SettingUtils.getInstance().getBoolean("HotPointState", false);
		updateHotPointState(hotPointState);
	}

	@Override
	protected void hMessage(Message msg) {
		switch (msg.what) {
		case MSG_H_WIFICONN_TIME:
			// ?????????????????????
			this.wifiConnectFailed();
			break;
		}
	}

	/**
	 * ??????Logic???????????????
	 * 
	 * @param isConnected
	 *            true/false ????????????/?????????
	 * @author jiayf
	 * @date Apr 13, 2015
	 */
	private void notifyLogicNetWorkState(boolean isConnected) {
		if (null == mApp.mGoluk) {
			return;
		}
		GolukDebugUtils.e("", "net-----state-----11111");
		final String connJson = JsonUtil.getNetStateJson(isConnected);
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_MessageReport,
				IMessageReportFn.REPORT_CMD_NET_STATA_CHG, connJson);
		if (isConnected) {
			GolukDebugUtils.e("", "net-----state-----2222");
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_CommCmd_RecoveryNetwork, "");
		}
	}

	/**
	 * ??????????????????
	 */
	public void videoAnalyzeComplete(String str) {
		try {
			JSONObject json = new JSONObject(str);
			String tag = json.getString("tag");
			String filename = json.optString("filename");
			long time = json.optLong("filetime");
			if (tag.equals("videodownload")) {
				// ?????????????????????????????????
				playDownLoadedSound();

				if (!IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
					try {
						if (filename.length() >= 22) {
							String t = filename.substring(18, 22);
							int tt = Integer.parseInt(t) + 1;
							time += tt;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					time += 1;
				}

				// ?????????????????????????????????
				long oldtime = SettingUtils.getInstance().getLong("downloadfiletime");
				time = time > oldtime ? time : oldtime;
				SettingUtils.getInstance().putLong("downloadfiletime", time);

				GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==8888===stopDownloadList" + time);
				updateHotPointState(true);

				EventBus.getDefault().post(new EventPhotoUpdateDate(EventConfig.PHOTO_ALBUM_UPDATE_DATE, filename));

				GFileUtils.writeIPCLog("YYYYYY===@@@@@@==2222==downloadfiletime=" + time);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ????????????????????????
	 */
	public void wiFiLinkStatus(int status) {
		GolukDebugUtils
				.e("", "jyf-----MainActivity----wifiConn----wiFiLinkStatus-------------wiFiLinkStatus:" + status);
		mApp.mWiFiStatus = 0;
		switch (status) {
		case 1:
			// ?????????
			this.updateRecoderBtn(1);
			mApp.mWiFiStatus = WIFI_STATE_CONNING;
			EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_CONNING));
			break;
		case 2:
			// ?????????
			this.updateRecoderBtn(2);
			mApp.mWiFiStatus = WIFI_STATE_SUCCESS;
			wifiConnectedSucess();
			break;
		case 3:
			// ?????????
			this.updateRecoderBtn(0);
			mApp.mWiFiStatus = WIFI_STATE_FAILED;
			wifiConnectFailed();
			break;
		}
	}

	/**
	 * ??????????????????????????? 1:????????? 2???????????? 0????????????
	 */
	public void updateRecoderBtn(int state) {

		if (this.isFinishing() == false) {
			AnimationDrawable ad = null;
			if (state == WIFI_STATE_CONNING && mApp.isBindSucess()) {
				mCarrecorderIv.setImageResource(R.anim.carrecoder_btn);
				ad = (AnimationDrawable) mCarrecorderIv.getDrawable();
				if (ad.isRunning() == false) {
					ad.setOneShot(false);
					ad.start();
				}
			} else if (state == WIFI_STATE_SUCCESS) {
				Toast.makeText(MainActivity.this, getResources().getString(R.string.wifi_link_success_conn),
						Toast.LENGTH_LONG).show();
				mCarrecorderIv.setImageResource(R.drawable.index_video_icon);
			} else if (state == WIFI_STATE_FAILED) {
				mCarrecorderIv.setImageResource(R.drawable.tb_notconnected);
			} else {
				mCarrecorderIv.setImageResource(R.drawable.tb_notconnected);
			}
		}
	}

	private void startWifi() {
		GolukDebugUtils.e("", "wifiCallBack-------------startWifi:");
		if (WIFI_STATE_CONNING == mApp.mWiFiStatus) {
			return;
		}
		mApp.mWiFiStatus = WIFI_STATE_CONNING;
	}

	// ????????????
	private void wifiConnectedSucess() {
		GolukDebugUtils.e("", "wifiCallBack-------------wifiConnectedSucess:");
		mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
		mApp.mWiFiStatus = WIFI_STATE_SUCCESS;
		refreshIpcDataToFile();
		EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_SUCCESS));
	}

	// ????????????
	private void wifiConnectFailed() {
		GolukDebugUtils.e("", "wifiCallBack-------------wifiConnectFailed:");
		mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
		mApp.mWiFiStatus = WIFI_STATE_FAILED;
		updateRecoderBtn(mApp.mWiFiStatus);

		EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_FAILED));
	}

	public void onEventMainThread(EventFollowPush event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.FOLLOW_PUSH:
			if (mTabHost != null) {
				mTabHost.setCurrentTab(4);
			}
			break;
		default:
			break;
		}
	}

	public void onEventMainThread(EventUserLoginRet event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.USER_LOGIN_RET:
			if (mFollowedVideoTipIV != null) {
				if (event.getFollowedVideoNum() > 0) {
					mFollowedVideoTipIV.setVisibility(View.VISIBLE);
				} else {
					mFollowedVideoTipIV.setVisibility(View.GONE);
				}
			}
			break;
		default:
			break;
		}
	}

	public void onEventMainThread(EventBindFinish event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.CAR_RECORDER_BIND_CREATEAP:
			createPhoneHot(event.bean);
			break;
		case EventConfig.BIND_LIST_DELETE_CONFIG:
			this.clearWifiConfig();
			break;
		default:
			break;
		}
	}

	public void onEventMainThread(EventMessageUpdate event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.MESSAGE_UPDATE:
			int msgCount = MessageManager.getMessageManager().getMessageTotalCount();
			setMessageTipCount(msgCount);
			MessageBadger.sendBadgeNumber(msgCount, this);
			break;
		case EventConfig.MESSAGE_REQUEST:
			msgRequest();
			break;
		default:
			break;
		}
	}

	public void onEventMainThread(EventBindResult event) {
		GolukDebugUtils.e("", "wifilist----MainActivity----onEventMainThread----EventBindResult----1");
		if (null == event) {
			return;
		}
		if (EventConfig.BIND_COMPLETE == event.getOpCode()) {
			GolukDebugUtils.e("", "wifilist----MainActivity----onEventMainThread----EventBindResult----set NULL");
			mCurrentConnBean = null;
		}
	}

	/**
	 * ??????????????????wifi??????
	 */
	private void autoConnWifi() {
		GolukDebugUtils.e("", "?????????????????????wifi---linkMobnoteWiFi---1");
		if (null == mWac) {
			mWac = new WifiConnectManager(mWifiManager, this);
		}
		mWac.autoWifiManage();
	}

	public void closeAp() {
		if (null != mWac) {
			mWac.closeAp();
		}
	}

	private void createPhoneHot(WifiBindHistoryBean bean) {
		mApp.setBinding(false);
		if (null == bean) {
			return;
		}

		GolukDebugUtils.e("", "wifibind----MainActivity  createPhoneHot--------ssid:" + bean.ipc_ssid);
		// ???????????????????????????ipc??????
		mApp.setIpcDisconnect();
		final String wifiName = bean.mobile_ssid;
		final String pwd = bean.mobile_pwd;
		String ipcssid = bean.ipc_ssid;
		String ipcmac = bean.ipc_mac;
		// ????????????????????????????????????
		startWifi();
		// ??????IPC????????????
		mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
		mBaseHandler.sendEmptyMessageDelayed(MSG_H_WIFICONN_TIME, 40 * 1000);
		mWac = new WifiConnectManager(mWifiManager, this);

		WifiRsBean beans = new WifiRsBean();
		beans.setIpc_mac(bean.ipc_mac);
		beans.setIpc_ssid(bean.ipc_ssid);
		beans.setIpc_pass(bean.ipc_pwd);
		beans.setIpc_ip(bean.ipc_ip);
		beans.setPh_ssid(bean.mobile_ssid);
		beans.setPh_pass(bean.mobile_pwd);
		mWac.saveConfiguration(beans);

		mWac.createWifiAP(wifiName, pwd, ipcssid, ipcmac);
	}

	public void onEventMainThread(EventMapQuery event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.LIVE_MAP_QUERY:
			// ??????????????????????????????
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPinData, "");
			break;
		default:
			break;
		}
	}

	public void onEventMainThread(EventWifiState event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.WIFI_STATE:
			// ????????????????????????????????????
			// ??????????????????
			notifyLogicNetWorkState(event.getMsg());
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("isIPCMatch");
		}
		if (null != mSoundPool) {
			mSoundPool.release();
			mSoundPool = null;
		}

		try {
			// ?????????????????????
			CarRecorderManager.onExit(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Unregister EventBus
		EventBus.getDefault().unregister(this);
		// mBannerLoaded = false;
	}

	@Override
	protected void onResume() {
		// GolukApplication.getInstance().queryNewFileList();
		GolukDebugUtils.e("", "crash zh start App ------ MainActivity-----onResume------------:");
		mApp.setContext(this, "Main");
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		mApp.setBinding(false);

		GetBaiduAddress.getInstance().setCallBackListener(this);

		if (mApp.isNeedCheckLive) {
			mApp.isNeedCheckLive = false;
			mApp.isCheckContinuteLiveFinish = true;
			showContinuteLive();
		}

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("isIPCMatch");
		}

		if (!mApp.isIpcLoginSuccess) {
			this.updateRecoderBtn(mApp.mWiFiStatus);
		}

		super.onResume();
	}

	public void showContinuteLive() {
		GolukDebugUtils.e("", "jyf----20150406----showContinuteLive----showContinuteLive :");
		// ??????????????????
		SharedPrefUtil.setIsLiveNormalExit(true);
		if (mApp.getIpcIsLogin()) {
			LiveDialogManager.getManagerInstance().showTwoBtnDialog(this, LiveDialogManager.DIALOG_TYPE_LIVE_CONTINUE,
					getString(R.string.user_dialog_hint_title), getString(R.string.str_live_continue));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);

	}

	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			GolukUtils.showToast(getApplicationContext(), getString(R.string.str_double_click_to_exit_app));
			exitTime = System.currentTimeMillis();
		} else {
			mApp.setExit(true);
			mApp.mHandler.removeMessages(1001);
			mApp.mHandler.removeMessages(1002);
			mApp.mHandler.removeMessages(1003);
			GetBaiduAddress.getInstance().exit();
			GolukVideoInfoDbManager.getInstance().destroy();
			unregisterListener();
			mApp.mIPCControlManager.setVdcpDisconnect();
			mApp.setIpcLoginOut();
			mApp.mUser.exitApp();
			mApp.mTimerManage.timerCancel();
			closeWifiHot();
			GlobalWindow.getInstance().dimissGlobalWindow();
			mApp.destroyLogic();
			MobclickAgent.onKillProcess(this);
			mApp.appFree();
			finish();
			GolukNotification.getInstance().destroy();
			CommentTimerManager.getInstance().cancelTimer();
		}
	}

	private void unregisterListener() {
		PageNotifyAdapter.setNotify(null);
		TalkNotifyAdapter.setNotify(null);
		IPCManagerAdapter.setIPcManageListener(null);
		VideoSquareManagerAdapter.setVideoSuqareListener(null);
		LocationNotifyAdapter.setLocationNotifyListener(null);
	}

	/**
	 * ??????WIFI??????
	 * 
	 * @author jyf
	 * @date 2015???7???20???
	 */
	private void closeWifiHot() {
		if (null == mWac) {
			mWac = new WifiConnectManager(mWifiManager, this);
		}
		mWac.closeAp();
	}

	/**
	 * ????????????????????????
	 * 
	 * @param isShow
	 *            true:?????????false:??????
	 * @author xuhw
	 * @date 2015???6???2???
	 */
	private void updateHotPointState(boolean isShow) {
		SettingUtils.getInstance().putBoolean("HotPointState", isShow);
	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (dialogType == LiveDialogManager.DIALOG_TYPE_LOGIN) {
			if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
				Intent intent = null;
				if (GolukApplication.getInstance().isInteral() == false) {
					intent = new Intent(this, InternationUserLoginActivity.class);
				} else {
					intent = new Intent(this, UserLoginActivity.class);
				}
				intent.putExtra("isInfo", "back");
				startActivity(intent);
			}
		} else if (LiveDialogManager.DIALOG_TYPE_LIVE_CONTINUE == dialogType) {
			if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
				// ????????????
				Intent intent;
				if (GolukApplication.getInstance().isInteral()) {
					intent = new Intent(this, BaidumapLiveActivity.class);
				} else {
					intent = new Intent(this, GooglemapLiveActivity.class);
				}

				intent.putExtra(AbstractLiveActivity.KEY_IS_LIVE, true);
				intent.putExtra(AbstractLiveActivity.KEY_LIVE_CONTINUE, true);
				intent.putExtra(AbstractLiveActivity.KEY_GROUPID, "");
				intent.putExtra(AbstractLiveActivity.KEY_PLAY_URL, "");
				intent.putExtra(AbstractLiveActivity.KEY_JOIN_GROUP, "");
				startActivity(intent);
			} else if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {
				if (mApp.mIPCControlManager.isT1Relative()) {
					mApp.mIPCControlManager.stopLive();
				}
			}
		} else if (LiveDialogManager.DIALOG_TYPE_APP_EXIT == dialogType) {
			if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
				exit();
			}
		}

	}

	@Override
	public void CallBack_BaiduGeoCoder(int function, Object obj) {
		if (null == obj) {
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----CallBack_BaiduGeoCoder----?????????????????????  : "
					+ (String) obj);
			return;
		}

		String address = "";
		if (GolukApplication.getInstance().isInteral()) {
			address = ((ReverseGeoCodeResult) obj).getAddress();// ??????
		} else {
			address = ((LocationAddressDetailBean) obj).detail;// ??????
		}
		GolukDebugUtils.e("", "-----------CallBack_BaiduGeoCoder----MainActivity------address: " + address);
		GolukApplication.getInstance().mCurAddr = address;
		// ???????????????????????????
		EventBus.getDefault().post(new EventUpdateAddr(EventConfig.CAR_RECORDER_UPDATE_ADDR, address));
	}

	/**
	 * ???????????????????????????
	 * 
	 * @author jyf
	 */
	private void clearWifiConfig() {
		GolukDebugUtils.e("", "wifibind----WifiUnbindSelect----clearWifiConfig");
		if (null != mWac) {
			mWac.saveConfiguration(null);
		}
	}

	private void wifiCallBack_sameHot() {
		if (mApp.getIpcIsLogin()) {
			wifiConnectedSucess();
		} else {
			// ????????????????????????IPC??????
			if (null == GolukApplication.mIpcIp) {
				// ????????????
				// wifiConnectFailed();
			} else {
				mApp.mIPCControlManager.setVdcpDisconnect();
				mApp.mIPCControlManager.setIPCWifiState(true, GolukApplication.mIpcIp);
			}
		}
	}

	private void refreshIpcDataToFile() {
		if (null == mCurrentConnBean) {
			return;
		}
		mCurrentConnBean = mWac.readConfig();
		if (null == mCurrentConnBean) {
			return;
		}
		GolukDebugUtils.e("",
				"select wifibind---MainActivity------refreshIpcDataToFile1: " + mCurrentConnBean.getIpc_ssid());
		// ?????????????????????????????????????????????????????????
		if (WifiBindDataCenter.getInstance().isHasIpc(mCurrentConnBean.getIpc_ssid())) {
			WifiBindDataCenter.getInstance().editBindStatus(mCurrentConnBean.getIpc_ssid(),
					WifiBindHistoryBean.CONN_USE);
			mCurrentConnBean = null;
			return;
		}
		GolukDebugUtils.e("",
				"select wifibind---MainActivity------refreshIpcDataToFile: " + mCurrentConnBean.getIpc_ssid());
		// ???????????????
		addHistoryIpcToDb();
	}

	private void addHistoryIpcToDb() {
		if (null == mCurrentConnBean) {
			return;
		}
		WifiBindHistoryBean historyBean = new WifiBindHistoryBean();
		historyBean.ipc_ssid = mCurrentConnBean.getIpc_ssid();
		historyBean.ipc_mac = mCurrentConnBean.getIpc_bssid();
		historyBean.ipc_pwd = mCurrentConnBean.getIpc_pass();

		historyBean.mobile_ssid = mCurrentConnBean.getPh_ssid();
		historyBean.mobile_pwd = mCurrentConnBean.getPh_pass();

		WifiBindDataCenter.getInstance().saveBindData(historyBean);
		mCurrentConnBean = null;
	}

	private void wifiCallBack_ipcConnHotSucess(String message, Object arrays) {
		WifiRsBean[] bean = (WifiRsBean[]) arrays;
		if (null != bean && bean.length > 0) {
			mCurrentConnBean = mWac.readConfig();
			sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
		}
	}

	private void createHotSuccess() {
		// ????????????????????????????????????????????????
		WifiBindHistoryBean currentBean = WifiBindDataCenter.getInstance().getCurrentUseIpc();
		if (currentBean != null) {
			String type = GolukUtils.getIpcTypeFromName(currentBean.ipc_ssid);
			mApp.mIPCControlManager.setIpcMode(type);
			GolukDebugUtils.e("", "wifibind----MainActivity--------createHotSuccess:  type:" + type);
		}
	}

	private void wifiCallBack_5(int state, int process, String message, Object arrays) {
		if (state == 0) {
			switch (process) {
			case 0:
				// ??????????????????
				createHotSuccess();
				break;
			case 1:
				// ipc?????????????????????
				wifiCallBack_ipcConnHotSucess(message, arrays);
				break;
			case 2:
				// ???????????????????????????????????????????????????
				wifiCallBack_sameHot();
				break;
			case 3:
				// ???????????????????????????wifi????????????????????????
				wifiConnectFailed();
				break;
			default:
				break;
			}
		} else {
			// ?????????
			wifiConnectFailed();
		}
	}

	private void wifiCallBack_3(int state, int process, String message, Object arrays) {
		EventWifiAuto autoBean = new EventWifiAuto();
		autoBean.eCode = EventConfig.CAR_RECORDER_RESULT;
		autoBean.state = state;
		autoBean.process = process;
		autoBean.message = message;
		autoBean.arrays = arrays;
		// ????????????????????????????????????
		EventBus.getDefault().post(autoBean);

		if (state == 0) {
			switch (process) {
			case 0:
				// ??????????????????
				createHotSuccess();
				break;
			case 1:
				// ipc?????????????????????
				try {
					WifiRsBean[] bean = (WifiRsBean[]) arrays;
					if (null != bean && bean.length > 0) {
						sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
					}
				} catch (Exception e) {
				}
				break;
			default:
				break;
			}
		} else {
			// connFailed();
		}
	}

	@Override
	public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
		GolukDebugUtils.e("", "wifibind----MainActivity--------wifiConn----wifiCallBack:  type:" + type + "  state:"
				+ state + "  process:" + process);
		if (!mApp.isBindSucess()) {
			return;
		}
		switch (type) {
		case 3:
			wifiCallBack_3(state, process, message, arrays);
			break;
		case 5:
			wifiCallBack_5(state, process, message, arrays);
			break;
		default:
			break;
		}
	}

	/**
	 * ??????logic??????ipc
	 */
	private void sendLogicLinkIpc(String ip, String ipcmac) {
		// ??????ipc??????wifi---??????ipc??????
		GolukApplication.mIpcIp = ip;
		mApp.mIPCControlManager.setIPCWifiState(true, ip);
	}

	private void setMessageTipCount(int total) {
		if (total > 0) {
			mUnreadTips.setVisibility(View.VISIBLE);
		} else {
			mUnreadTips.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		if (null == result) {
			return;
		}

		if (requestType == IPageNotifyFn.PageType_MsgCounter) {
			MessageCounterBean bean = (MessageCounterBean) result;
			if (null == bean.data) {
				return;
			}

			if (null != bean.data.messagecount) {
				int praiseCount = 0;
				int commentCount = 0;
				int systemCount = 0;
				int followCount = 0;
				if (null != bean.data.messagecount.user) {
					praiseCount = bean.data.messagecount.user.like;
					commentCount = bean.data.messagecount.user.comment;
				}
				if (null != bean.data.messagecount.system) {
					systemCount = bean.data.messagecount.system.total;
				}

				MessageManager.getMessageManager().setMessageEveryCount(praiseCount, commentCount, followCount,
						systemCount);
			}
		}
	}
}
