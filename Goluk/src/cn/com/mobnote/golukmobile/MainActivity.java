package cn.com.mobnote.golukmobile;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabWidget;
import android.widget.Toast;
import cn.com.mobnote.application.GlobalWindow;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventBindFinish;
import cn.com.mobnote.eventbus.EventBindResult;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventMapQuery;
import cn.com.mobnote.eventbus.EventMessageUpdate;
import cn.com.mobnote.eventbus.EventPhotoUpdateDate;
import cn.com.mobnote.eventbus.EventUpdateAddr;
import cn.com.mobnote.eventbus.EventWifiAuto;
import cn.com.mobnote.eventbus.EventWifiConnect;
import cn.com.mobnote.eventbus.EventWifiState;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.comment.CommentTimerManager;
import cn.com.mobnote.golukmobile.fileinfo.GolukVideoInfoDbManager;
import cn.com.mobnote.golukmobile.followed.FragmentFollowed;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress.IBaiduGeoCoderFn;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.msg.MessageBadger;
import cn.com.mobnote.golukmobile.msg.MsgCenterCounterRequest;
import cn.com.mobnote.golukmobile.msg.bean.MessageCounterBean;
import cn.com.mobnote.golukmobile.photoalbum.FragmentAlbum;
import cn.com.mobnote.golukmobile.special.SpecialListActivity;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindDataCenter;
import cn.com.mobnote.golukmobile.wifidatacenter.WifiBindHistoryBean;
import cn.com.mobnote.golukmobile.xdpush.GolukNotification;
import cn.com.mobnote.golukmobile.xdpush.StartAppBean;
import cn.com.mobnote.golukmobile.xdpush.XingGeMsgBean;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.manager.MessageManager;
import cn.com.mobnote.module.ipcmanager.IPCManagerAdapter;
import cn.com.mobnote.module.location.LocationNotifyAdapter;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.page.PageNotifyAdapter;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.talk.TalkNotifyAdapter;
import cn.com.mobnote.module.videosquare.VideoSquareManagerAdapter;
import cn.com.mobnote.receiver.NetworkStateReceiver;
import cn.com.mobnote.util.CrashReportUtil;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.SharedPrefUtil;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.api.Tapi;
import cn.com.tiros.debug.GolukDebugUtils;

import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.rd.car.CarRecorderManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import de.greenrobot.event.EventBus;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class MainActivity extends BaseActivity implements OnClickListener, WifiConnCallBack, OnTouchListener,
		ILiveDialogManagerFn, IBaiduGeoCoderFn, IRequestResultListener {

	/** 程序启动需要20秒的时间用来等待IPC连接 */
	private final int MSG_H_WIFICONN_TIME = 100;
	/** application */
	public GolukApplication mApp = null;

	/** 更多按钮 */
//	private Button mMoreBtn = null;
	/** 视频广场按钮 */
//	private Button msquareBtn = null;
	/** wifi列表manage */
	private WifiConnectManager mWac = null;

	/** 记录登录状态 **/
	public SharedPreferences mPreferencesAuto;
	public boolean isFirstLogin;

//	private LinearLayout mRootLayout = null;

//	private View videoSquareLayout = null;

//	private View userInfoLayout = null;

	/** 未连接 */
	public static final int WIFI_STATE_FAILED = 0;
	/** 连接中 */
	public static final int WIFI_STATE_CONNING = 1;
	/** 连接 */
	public static final int WIFI_STATE_SUCCESS = 2;

	public CustomLoadingDialog mCustomProgressDialog;
	public String shareVideoId;
	/** 链接行车记录仪 */
//	private ImageButton indexCarrecoderBtn = null;
	/** 连接ipc时的动画 */
	Animation anim = null;

	private SharedPreferences mPreferences = null;
	private Editor mEditor = null;
	private long exitTime = 0;
	
	private View mUnreadTips;
	/** 首次进入的引导div */
//	private RelativeLayout indexDiv = null;
//	private ImageView mIndexImg = null;
//	private int divIndex = 0;

//	public VideoSquareActivity mVideoSquareActivity;

//	private IndexMoreActivity indexMoreActivity;

//	private RelativeLayout indexCarrecoderBtnlayout;
	private WifiManager mWifiManager = null;
	// Play video sync from camera completion sound
	private SoundPool mSoundPool;
	private final static String TAG = "MainActivity";
//	private String mCityCode;
	private boolean mBannerLoaded;
	private StartAppBean mStartAppBean = null;
	/** 把当前连接的设备保存起来，主要是为了兼容以前的连接状态 */
	private WifiRsBean mCurrentConnBean = null;
	private FragmentTabHost mTabHost;
	
	ImageView mCarrecorderIv;

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
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		GolukDebugUtils.e("", "start App ------ MainActivity-----onCreate------------:");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
		
		initTab();
		
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		mRootLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.index, null);
//		setContentView(mRootLayout);
		mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		// Register EventBus
		EventBus.getDefault().register(this);
		initThirdSDK();

		mBannerLoaded = false;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "Main");
		mApp.initLogic();
		// 页面初始化,获取页面控件
		mApp.startTime = System.currentTimeMillis();
		// 页面初始化,获取页面控件
		init();
		
		UserInfo userInfo = mApp.getMyInfo();
		if (null != userInfo) {
			mApp.mCurrentUId = userInfo.uid;
			mApp.mCurrentAid = userInfo.aid;
		}

		// 读取SharedPreFerences中需要的数据,使用SharedPreFerences来记录程序启动的使用次数
		SharedPreferences preferences = getSharedPreferences("golukmark", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用true作为默认值
		boolean isFirstIndex = preferences.getBoolean("isFirstIndex", true);
		if (isFirstIndex) { // 如果是第一次启动
//			indexDiv.setVisibility(View.VISIBLE);
			Editor editor = preferences.edit();
			editor.putBoolean("isFirstIndex", false);
			// 提交修改
			editor.commit();
		}
		// 初始化视频广场
		initVideoSquare();
		// 初始化个人中心
		initUserInfo();

		// 为了兼容以前的版本， 把旧的绑定信息读取出来
		mWac = new WifiConnectManager(mWifiManager, this);
		mCurrentConnBean = mWac.readConfig();
		refreshIpcDataToFile();

		// 初始化连接与綁定状态
		if (mApp.isBindSucess()) {
			startWifi();
			// 启动创建热点
			autoConnWifi();
			// 等待IPC连接时间
			mBaseHandler.sendEmptyMessageDelayed(MSG_H_WIFICONN_TIME, 40 * 1000);
			
			
		} else {
			wifiConnectFailed();
		}

		// 不是第一次登录，并且上次登录成功过，进行自动登录
		mPreferencesAuto = getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferencesAuto.getBoolean("FirstLogin", true);
		if (!isFirstLogin && !mApp.isUserLoginSucess) {
			mApp.mUser.initAutoLogin();
		}

		GetBaiduAddress.getInstance().setCallBackListener(this);

		// 未登录跳转登录
		Intent itStart_have = getIntent();
		if (null != itStart_have.getStringExtra("userstart")) {
			String start_have = itStart_have.getStringExtra("userstart").toString();
			if ("start_have".equals(start_have)) {
				Intent it = new Intent(MainActivity.this, UserLoginActivity.class);
				// 登录页回调判断
				it.putExtra("isInfo", "main");
				mPreferences = getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
				mEditor = mPreferences.edit();
				mEditor.putString("toRepwd", "start");
				mEditor.commit();
				// 在黑页面判断是注销进来的还是首次登录进来的
				if (!mApp.loginoutStatus) {// 注销
					// 获取注销成功后传来的信息
					mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
					String phone = mPreferences.getString("setupPhone", "");// 最后一个参数为默认值
					it.putExtra("startActivity", phone);
					startActivity(it);
				} else {
					startActivity(it);
				}
			}
		}

//		mCityCode = SharedPrefUtil.getCityIDString();
		dealPush(itStart_have);

		if (NetworkStateReceiver.isNetworkAvailable(this)) {
			notifyLogicNetWorkState(true);
		}
		GolukUtils.getMobileInfo(this);

		// msgRequest();
		
	}

	private void initTab() {
		
		LayoutInflater inflater = LayoutInflater.from(this);
		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		Bundle b = new Bundle();
		b.putString("key", "Discover");
		LinearLayout discover = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_discover, null);
		mTabHost.addTab(mTabHost.newTabSpec("Discover").setIndicator(discover),
				FragmentDiscover.class, b);

		b = new Bundle();
		b.putString("key", "Follow");
		LinearLayout follow = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_follow, null);
		mTabHost.addTab(mTabHost.newTabSpec("Follow")
				.setIndicator(follow), FragmentFollowed.class, b);

		b = new Bundle();
		b.putString("key", "CarRecorder");
		LinearLayout carRecorder = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_carrecorder, null);
		mCarrecorderIv = (ImageView)carRecorder.findViewById(R.id.carrecorder_iv);
		mTabHost.addTab(mTabHost.newTabSpec("CarRecorder").setIndicator(carRecorder),
				null, b);

		b = new Bundle();
		b.putString("key", "Album");
		b.putString("platform", "0");
		LinearLayout album = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_album, null);
		mTabHost.addTab(mTabHost.newTabSpec("Album").setIndicator(album),
				FragmentAlbum.class, b);

		b = new Bundle();
		b.putString("key", "Mine");
		RelativeLayout mine = (RelativeLayout) inflater.inflate(R.layout.main_tab_indicator_mine, null);
		mUnreadTips = mine.findViewById(R.id.iv_unread_tips);
		mTabHost.addTab(mTabHost.newTabSpec("Mine").setIndicator(mine),
				FragmentMine.class, b);
		TabWidget widget = mTabHost.getTabWidget();
		widget.setDividerDrawable(null);
		mTabHost.getTabWidget().setBackgroundResource(R.color.color_main_tab_bg);
		View lineView = new View(this);
		lineView.setBackgroundResource(R.color.color_list_divider);
		LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3);
		lineView.setLayoutParams(lineParams);
		mTabHost.addView(lineView);

		for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
			mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 141;
		}

		mTabHost.getTabWidget().getChildTabViewAt(2)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(MainActivity.this,
								CarRecorderActivity.class);
						startActivity(intent);
					}
				});
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
//				Drawable user_down = this.getResources().getDrawable(R.drawable.index_user_btn_press);
//				mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_down, null, null);
//				mMoreBtn.setTextColor(Color.rgb(59, 151, 245));
//
//				Drawable square_up = this.getResources().getDrawable(R.drawable.index_find_btn);
//				msquareBtn.setCompoundDrawablesWithIntrinsicBounds(null, square_up, null, null);
//				msquareBtn.setTextColor(Color.rgb(204, 204, 204));
//
//				userInfoLayout.setVisibility(View.VISIBLE);
//				videoSquareLayout.setVisibility(View.GONE);
				Fragment fragment = getSupportFragmentManager().findFragmentByTag("Mine");
//				indexMoreActivity.showView();
				Log.d("CK1", "11111111111111111111111111111: " + fragment);
			}
		}

		dealPush(intent);
	}

	/**
	 * 处理推送消息
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
			// GolukUtils.showToast(this, "处理推送数据 :" + pushJson);
		}
		// 处理网页启动App
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
			// 单视频
			Intent intent = new Intent(this, VideoDetailActivity.class);
			intent.putExtra(VideoDetailActivity.VIDEO_ID, id);
			intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
			startActivity(intent);
		} else if ("2".equals(type)) {
			// 专题
			Intent intent = new Intent(this, SpecialListActivity.class);
			intent.putExtra("ztid", id);
			intent.putExtra("title", title);
			startActivity(intent);
		}

		mStartAppBean = null;
	}

	/**
	 * 初始化第三方SDK
	 * 
	 * @author jyf
	 * @date 2015年6月17日
	 */
	private void initThirdSDK() {
		// 关闭umeng错误统计(只使用友盟的行为分析，不使用错误统计)
		MobclickAgent.setDebugMode(false);
		MobclickAgent.setCatchUncaughtExceptions(false);
		// 添加腾讯崩溃统计 初始化SDK
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
//		mVideoSquareActivity.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 页面初始化,获取页面元素,注册事件
	 */
	private void init() {
//		indexDiv = (RelativeLayout) findViewById(R.id.index_div);
//		mIndexImg = (ImageView) findViewById(R.id.index_img);

//		mMoreBtn = (Button) findViewById(R.id.more_btn);
//		msquareBtn = (Button) findViewById(R.id.index_square_btn);
//		videoSquareLayout = findViewById(R.id.video_square_layout);

//		indexCarrecoderBtn = (ImageButton) findViewById(R.id.index_carrecoder_btn);
//		this.updateRecoderBtn(mApp.mWiFiStatus);// 设置行测记录仪状态

//		indexCarrecoderBtnlayout = (RelativeLayout) findViewById(R.id.index_carrecoder_btn_layout);
//		userInfoLayout = findViewById(R.id.user_info);

//		indexCarrecoderBtn.setOnClickListener(this);
//		indexCarrecoderBtnlayout.setOnClickListener(this);

//		indexDiv.setOnClickListener(this);
//		mMoreBtn.setOnClickListener(this);
//		mMoreBtn.setOnTouchListener(this);
//		msquareBtn.setOnClickListener(this);

		boolean hotPointState = SettingUtils.getInstance().getBoolean("HotPointState", false);
		updateHotPointState(hotPointState);
	}

	@Override
	protected void hMessage(Message msg) {
		switch (msg.what) {
		case MSG_H_WIFICONN_TIME:
			// 设置未连接状态
			this.wifiConnectFailed();
			break;
		}
	}

	/**
	 * 通知Logic，网络恢复
	 * 
	 * @param isConnected
	 *            true/false 网络恢复/不可用
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

	private void initVideoSquare() {
//		mVideoSquareActivity = new VideoSquareActivity(mRootLayout, this);
	}

	private void initUserInfo() {
//		indexMoreActivity = new IndexMoreActivity(mRootLayout, this);
	}

	/**
	 * 视频同步完成
	 */
	public void videoAnalyzeComplete(String str) {
		try {
			JSONObject json = new JSONObject(str);
			String tag = json.getString("tag");
			String filename = json.optString("filename");
			long time = json.optLong("filetime");
			if (tag.equals("videodownload")) {
				// 只有视频下载才提示音频
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

				// 更新最新下载文件的时间
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
	 * 链接中断更新页面
	 */
	public void wiFiLinkStatus(int status) {
		GolukDebugUtils
				.e("", "jyf-----MainActivity----wifiConn----wiFiLinkStatus-------------wiFiLinkStatus:" + status);
		mApp.mWiFiStatus = 0;
		switch (status) {
		case 1:
			// 连接中
			this.updateRecoderBtn(1);
			mApp.mWiFiStatus = WIFI_STATE_CONNING;
			EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_CONNING));
			break;
		case 2:
			// 已连接
			this.updateRecoderBtn(2);
			mApp.mWiFiStatus = WIFI_STATE_SUCCESS;
			wifiConnectedSucess();
			break;
		case 3:
			// 未连接
			this.updateRecoderBtn(0);
			mApp.mWiFiStatus = WIFI_STATE_FAILED;
			wifiConnectFailed();
			break;
		}
	}

	/**
	 * 更新行车记录仪按钮 1:连接中 2：已连接 0：未连接
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
				Toast.makeText(MainActivity.this, "连接成功！", Toast.LENGTH_LONG).show();
				mCarrecorderIv.setImageResource(R.drawable.index_video_icon);
			} else if (state == WIFI_STATE_FAILED) {
				//Toast.makeText(MainActivity.this, "连接失败！", Toast.LENGTH_LONG).show();
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

	// 连接成功
	private void wifiConnectedSucess() {
		GolukDebugUtils.e("", "wifiCallBack-------------wifiConnectedSucess:");
		mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
		mApp.mWiFiStatus = WIFI_STATE_SUCCESS;
		refreshIpcDataToFile();
		EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_SUCCESS));
		//Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_LONG).show();
	}

	// 连接失败
	private void wifiConnectFailed() {
		GolukDebugUtils.e("", "wifiCallBack-------------wifiConnectFailed:");
		mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
		mApp.mWiFiStatus = WIFI_STATE_FAILED;
		updateRecoderBtn(mApp.mWiFiStatus);
		
		EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_FAILED));
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
	 * 启动软件创建wifi热点
	 */
	private void autoConnWifi() {
		GolukDebugUtils.e("", "自动连接小车本wifi---linkMobnoteWiFi---1");
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
		// 创建热点之前先断开ipc连接
		mApp.setIpcDisconnect();
		final String wifiName = bean.mobile_ssid;
		final String pwd = bean.mobile_pwd;
		String ipcssid = bean.ipc_ssid;
		String ipcmac = bean.ipc_mac;
		// 调用韩峥接口创建手机热点
		startWifi();
		// 等待IPC连接时间
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

//	public void onEventMainThread(EventLocationFinish event) {
//		if (null == event) {
//			return;
//		}
//
//		switch (event.getOpCode()) {
//		case EventConfig.LOCATION_FINISH:
//			Log.d(TAG, "Location Finished: " + event.getCityCode());
//			// Start load banner
//			VideoSquareAdapter videoSquareAdapter = mVideoSquareActivity.getVideoSquareAdapter();
//			FragmentDiscover fragmentDiscover = (FragmentDiscover)getSupportFragmentManager().findFragmentByTag("Discover");
//			FragmentDiscover fragmentDiscover = (FragmentDiscover)getSupportFragmentManager()
//					.findFragmentByTag("tabsfragment")
//					.getChildFragmentManager().findFragmentByTag("Discover");
//			VideoSquareAdapter videoSquareAdapter = fragmentDiscover.getVideoSquareAdapter();
//			if (null == videoSquareAdapter) {
//				return;
//			}
//			WonderfulSelectedListView listView = videoSquareAdapter.getWonderfulSelectedListView();
//
//			if (null == listView) {
//				return;
//			}
//
//			if (!mBannerLoaded) {
//				Log.d(TAG, "Activity first start, fill everything anyway");
//				if (event.getCityCode().equals("-1")) {
//					if (null == mCityCode || mCityCode.trim().equals("")) {
//						mCityCode = event.getCityCode();
//						SharedPrefUtil.setCityIDString(mCityCode);
//						listView.loadBannerData(mCityCode);
//					} else {
//						listView.loadBannerData(mCityCode);
//					}
//				} else {
//					mCityCode = event.getCityCode();
//					SharedPrefUtil.setCityIDString(mCityCode);
//					listView.loadBannerData(mCityCode);
//				}
//				mBannerLoaded = true;
//			}
//
//			if (null == mCityCode || mCityCode.trim().equals("")) {
//				Log.d(TAG, "First located, fill everything anyway");
//				mCityCode = event.getCityCode();
//				SharedPrefUtil.setCityIDString(mCityCode);
//				listView.loadBannerData(mCityCode);
//			} else {
//				// In whole nation
//				if ("-1".equals(mCityCode)) {
//					if (event.getCityCode().equals("-1")) {
//						// do nothing
//					} else {
//						mCityCode = event.getCityCode();
//						SharedPrefUtil.setCityIDString(mCityCode);
//						listView.loadBannerData(mCityCode);
//					}
//				} else { // In city
//					if (event.getCityCode().equals("-1")) {
//						// do nothing
//					} else {
//						if (!mCityCode.equals(event.getCityCode())) {
//							mCityCode = event.getCityCode();
//							SharedPrefUtil.setCityIDString(mCityCode);
//							listView.loadBannerData(mCityCode);
//						}
//					}
//				}
//			}
//
//			break;
//		default:
//			break;
//		}
//	}

	public void onEventMainThread(EventMapQuery event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.LIVE_MAP_QUERY:
			// 请求在线视频轮播数据
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
			// 检测是否已连接小车本热点
			// 网络状态改变
			notifyLogicNetWorkState(event.getMsg());
			break;
		default:
			break;
		}
	}

	/**
	 * 检测wifi链接状态
	 */
	public void checkWiFiStatus() {
		GolukDebugUtils.e("", "wifiCallBack-------------checkWiFiStatus   type:" + mApp.mWiFiStatus);
		Intent i = new Intent(MainActivity.this, CarRecorderActivity.class);
		startActivity(i);
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
			// 应用退出时调用
			CarRecorderManager.onExit(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Unregister EventBus
		EventBus.getDefault().unregister(this);
		mBannerLoaded = false;
	}

	@Override
	protected void onResume() {
//		GolukApplication.getInstance().queryNewFileList();
		mApp.setContext(this, "Main");
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		mApp.setBinding(false);

//		if (null != mVideoSquareActivity) {
//			mVideoSquareActivity.onResume();
//		}

		GetBaiduAddress.getInstance().setCallBackListener(this);

		if (mApp.isNeedCheckLive) {
			mApp.isNeedCheckLive = false;
			mApp.isCheckContinuteLiveFinish = true;
			showContinuteLive();
		}

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("isIPCMatch");
		}

		this.updateRecoderBtn(mApp.mWiFiStatus);

//		indexMoreActivity.showView();
		super.onResume();
	}

	public void showContinuteLive() {
		GolukDebugUtils.e("", "jyf----20150406----showContinuteLive----showContinuteLive :");
		// 标识正常退出
		SharedPrefUtil.setIsLiveNormalExit(true);
		if (mApp.getIpcIsLogin()) {
			LiveDialogManager.getManagerInstance().showTwoBtnDialog(this, LiveDialogManager.DIALOG_TYPE_LIVE_CONTINUE,
					getString(R.string.user_dialog_hint_title), getString(R.string.str_live_continue));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
//		if (null != mVideoSquareActivity) {
//			mVideoSquareActivity.onPause();
//		}
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
	 * 关闭WIFI热点
	 * 
	 * @author jyf
	 * @date 2015年7月20日
	 */
	private void closeWifiHot() {
		if (null == mWac) {
			mWac = new WifiConnectManager(mWifiManager, this);
		}
		mWac.closeAp();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		int action = event.getAction();
//		switch (v.getId()) {
//
//		case R.id.more_btn:
//			switch (action) {
//			case MotionEvent.ACTION_DOWN:
//				Drawable user_down = this.getResources().getDrawable(R.drawable.index_user_btn_press);
//				mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_down, null, null);
//				mMoreBtn.setTextColor(Color.rgb(11, 89, 190));
//				break;
//			case MotionEvent.ACTION_UP:
//				Drawable user_up = this.getResources().getDrawable(R.drawable.index_user_btn);
//				mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_up, null, null);
//				mMoreBtn.setTextColor(Color.rgb(204, 204, 204));
//				break;
//			}
//			break;
//		}
		return false;
	}

	@Override
	public void onClick(View v) {
//		int id = v.getId();
//		switch (id) {
//		case R.id.more_btn:
//			// 更多页面
//			Drawable user_down = this.getResources().getDrawable(R.drawable.index_user_btn_press);
//			mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_down, null, null);
//			mMoreBtn.setTextColor(Color.rgb(11, 89, 190));
//
//			Drawable square_up = this.getResources().getDrawable(R.drawable.index_find_btn);
//			msquareBtn.setCompoundDrawablesWithIntrinsicBounds(null, square_up, null, null);
//			msquareBtn.setTextColor(Color.rgb(204, 204, 204));
//
//			userInfoLayout.setVisibility(View.VISIBLE);
//			videoSquareLayout.setVisibility(View.GONE);
//
//			indexMoreActivity.showView();
//			break;
//		case R.id.index_square_btn:
//			// 视频广场
//			Drawable square_down = this.getResources().getDrawable(R.drawable.index_find_btn_press);
//			msquareBtn.setCompoundDrawablesWithIntrinsicBounds(null, square_down, null, null);
//			msquareBtn.setTextColor(Color.rgb(11, 89, 190));
//
//			Drawable user_up = this.getResources().getDrawable(R.drawable.index_user_btn);
//			mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_up, null, null);
//			mMoreBtn.setTextColor(Color.rgb(204, 204, 204));
//
//			userInfoLayout.setVisibility(View.GONE);
//			videoSquareLayout.setVisibility(View.VISIBLE);
//			setBelowItem(R.id.index_square_btn);
//			break;
//		case R.id.index_carrecoder_btn_layout:
//		case R.id.index_carrecoder_btn:
//			checkWiFiStatus();
//			break;
//		case R.id.index_div:
//			if (divIndex == 0) {
//				GolukUtils.freeBitmap(mIndexImg.getBackground());
//				indexDiv.setVisibility(View.GONE);
//			}
//			break;
//		}
	}

	public void setBelowItem(int id) {
//		Drawable drawable;
//		if (id == R.id.index_square_btn) {
//			videoSquareLayout.setVisibility(View.VISIBLE);
//			mVideoSquareActivity.onResume();
//			drawable = this.getResources().getDrawable(R.drawable.index_find_btn_press);
//			msquareBtn.setTextColor(Color.rgb(11, 89, 180));
//			msquareBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
//		}
	}

	/**
	 * 重置红点显示状态
	 * 
	 * @param isShow
	 *            true:显示　false:隐藏
	 * @author xuhw
	 * @date 2015年6月2日
	 */
	private void updateHotPointState(boolean isShow) {
		SettingUtils.getInstance().putBoolean("HotPointState", isShow);
	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (dialogType == LiveDialogManager.DIALOG_TYPE_LOGIN) {
			if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
				Intent intent = new Intent(this, UserLoginActivity.class);
				intent.putExtra("isInfo", "back");
				startActivity(intent);
			}
		} else if (LiveDialogManager.DIALOG_TYPE_LIVE_CONTINUE == dialogType) {
			if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
				// 继续直播
				Intent intent = new Intent(this, LiveActivity.class);
				intent.putExtra(LiveActivity.KEY_IS_LIVE, true);
				intent.putExtra(LiveActivity.KEY_LIVE_CONTINUE, true);
				intent.putExtra(LiveActivity.KEY_GROUPID, "");
				intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
				intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
				startActivity(intent);
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
			GolukDebugUtils.e("", "jyf----20150406----LiveActivity----CallBack_BaiduGeoCoder----获取反地理编码  : "
					+ (String) obj);
			return;
		}

		final String address = ((ReverseGeoCodeResult) obj).getAddress();
		GolukApplication.getInstance().mCurAddr = address;
		// 更新行车记录仪地址
		EventBus.getDefault().post(new EventUpdateAddr(EventConfig.CAR_RECORDER_UPDATE_ADDR, address));
	}

	/**
	 * 清空本地的配置文件
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
			// 判断，是否设置过IPC地址
			if (null == GolukApplication.mIpcIp) {
				// 连接失败
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
		// 如果本地文件中已经有记录了，则不再保存
		if (WifiBindDataCenter.getInstance().isHasIpc(mCurrentConnBean.getIpc_ssid())) {
			WifiBindDataCenter.getInstance().editBindStatus(mCurrentConnBean.getIpc_ssid(),
					WifiBindHistoryBean.CONN_USE);
			mCurrentConnBean = null;
			return;
		}
		GolukDebugUtils.e("",
				"select wifibind---MainActivity------refreshIpcDataToFile: " + mCurrentConnBean.getIpc_ssid());
		// 添加新记录
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
		// 创建热点成功后，需要设置连接方式
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
				// 创建热点成功
				createHotSuccess();
				break;
			case 1:
				// ipc成功连接上热点
				wifiCallBack_ipcConnHotSucess(message, arrays);
				break;
			case 2:
				// 用户已经创建与配置文件相同的热点，
				wifiCallBack_sameHot();
				break;
			case 3:
				// 用户已经连接到其它wifi，按连接失败处理
				wifiConnectFailed();
				break;
			default:
				break;
			}
		} else {
			// 未连接
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
		// 通知选择设备界面，做更新
		EventBus.getDefault().post(autoBean);

		if (state == 0) {
			switch (process) {
			case 0:
				// 创建热点成功
				createHotSuccess();
				break;
			case 1:
				// ipc成功连接上热点
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
	 * 通知logic连接ipc
	 */
	private void sendLogicLinkIpc(String ip, String ipcmac) {
		// 连接ipc热点wifi---调用ipc接口
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

				if (null != bean.data.messagecount.user) {
					praiseCount = bean.data.messagecount.user.like;
					commentCount = bean.data.messagecount.user.comment;
				}
				if (null != bean.data.messagecount.system) {
					systemCount = bean.data.messagecount.system.total;
				}

				MessageManager.getMessageManager().setMessageEveryCount(praiseCount, commentCount, systemCount);
			}
		}
	}
}
