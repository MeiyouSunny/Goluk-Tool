package cn.com.mobnote.golukmobile;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress.IBaiduGeoCoderFn;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareActivity;
import cn.com.mobnote.golukmobile.xdpush.GolukNotification;
import cn.com.mobnote.golukmobile.xdpush.XingGeMsgBean;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerAdapter;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.location.LocationNotifyAdapter;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.page.PageNotifyAdapter;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.talk.TalkNotifyAdapter;
import cn.com.mobnote.module.videosquare.VideoSquareManagerAdapter;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.receiver.NetworkStateReceiver;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.api.Tapi;
import cn.com.tiros.debug.GolukDebugUtils;
import cn.com.tiros.utils.CrashReportUtil;

import com.baidu.mapapi.SDKInitializer;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.rd.car.CarRecorderManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class MainActivity extends BaseActivity implements OnClickListener, WifiConnCallBack, OnTouchListener,
		ILiveDialogManagerFn, IBaiduGeoCoderFn, UserInterface {

	/** 程序启动需要20秒的时间用来等待IPC连接 */
	private final int MSG_H_WIFICONN_TIME = 100;
	/** application */
	public GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;

	/** 更多按钮 */
	private Button mMoreBtn = null;
	/** 视频广场按钮 */
	private Button msquareBtn = null;
	/** wifi列表manage */
	private WifiConnectManager mWac = null;

	/** 首页handler用来接收消息,更新UI */
	public static Handler mMainHandler = null;
	/** 下载完成播放声音文件 */
	public String mVideoDownloadSoundFile = "ec_alert5.wav";
	/** 下载完成播放音频 */
	public MediaPlayer mMediaPlayer = new MediaPlayer();

	/** 记录登录状态 **/
	public SharedPreferences mPreferencesAuto;
	public boolean isFirstLogin;

	private RelativeLayout mRootLayout = null;

	private View videoSquareLayout = null;

	private View userInfoLayout = null;

	/** 未连接 */
	private final int WIFI_STATE_FAILED = 0;
	/** 连接中 */
	private final int WIFI_STATE_CONNING = 1;
	/** 连接 */
	private final int WIFI_STATE_SUCCESS = 2;

	public CustomLoadingDialog mCustomProgressDialog;
	public String shareVideoId;
	/** 链接行车记录仪 */
	private ImageButton indexCarrecoderBtn = null;
	/** 连接ipc时的动画 */
	Animation anim = null;

	private SharedPreferences mPreferences = null;
	private Editor mEditor = null;
	private long exitTime = 0;
	/** 首次进入的引导div */
	private RelativeLayout indexDiv = null;
	private ImageView mIndexImg = null;
	private int divIndex = 0;

	public VideoSquareActivity mVideoSquareActivity;

	private IndexMoreActivity indexMoreActivity;

	private RelativeLayout indexCarrecoderBtnlayout;
	private WifiManager mWifiManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		((GolukApplication) this.getApplication()).initSharedPreUtil(this);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mRootLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.index, null);
		setContentView(mRootLayout);

		initThirdSDK();

		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "Main");
		mApp.initLogic();
		// 页面初始化,获取页面控件
		mApp.startTime = System.currentTimeMillis();
		// 页面初始化,获取页面控件
		init();

		// 读取SharedPreFerences中需要的数据,使用SharedPreFerences来记录程序启动的使用次数
		SharedPreferences preferences = getSharedPreferences("golukmark", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用true作为默认值
		boolean isFirstIndex = preferences.getBoolean("isFirstIndex", true);
		if (isFirstIndex) { // 如果是第一次启动
			indexDiv.setVisibility(View.VISIBLE);
			Editor editor = preferences.edit();
			editor.putBoolean("isFirstIndex", false);
			// 提交修改
			editor.commit();
		}
		// 初始化视频广场
		initVideoSquare();
		// 初始化个人中心
		initUserInfo();
		// 初始化连接与綁定状态
		boolean b = this.isBindSucess();
		GolukDebugUtils.i("lily", "======bind====status===" + b);
		if (b) {
			startWifi();
			// 启动创建热点
			createWiFiHot();
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

		dealPush(itStart_have);

		if (NetworkStateReceiver.isNetworkAvailable(this)) {
			notifyLogicNetWorkState(true);
		}

		GolukUtils.getMobileInfo(this);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		GolukDebugUtils.i("newintent", "-------str------------" + intent.getStringExtra("showMe"));
		if (null != intent.getStringExtra("showMe")) {
			String str = intent.getStringExtra("showMe").toString();
			if ("showMe".equals(str)) {
				Drawable user_down = this.getResources().getDrawable(R.drawable.index_user_btn_press);
				mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_down, null, null);
				mMoreBtn.setTextColor(Color.rgb(59, 151, 245));

				Drawable square_up = this.getResources().getDrawable(R.drawable.index_find_btn);
				msquareBtn.setCompoundDrawablesWithIntrinsicBounds(null, square_up, null, null);
				msquareBtn.setTextColor(Color.rgb(204, 204, 204));

				userInfoLayout.setVisibility(View.VISIBLE);
				videoSquareLayout.setVisibility(View.GONE);

				indexMoreActivity.showView();
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
			GolukUtils.showToast(this, "处理推送数据 :" + pushJson);
		}
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
		CrashReport.initCrashReport(this, CrashReportUtil.BUGLY_APPID_GOLUK, CrashReportUtil.isDebug);
		final String mobileId = Tapi.getMobileId();
		CrashReport.setUserId(mobileId);
		GolukDebugUtils.e("", "jyf-----MainActivity-----mobileId:" + mobileId);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mVideoSquareActivity.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 启动软件创建wifi热点
	 */
	private void createWiFiHot() {
		GolukDebugUtils.e("", "自动连接小车本wifi---linkMobnoteWiFi---1");
		mWac = new WifiConnectManager(mWifiManager, this);
		mWac.autoWifiManage();
	}

	/**
	 * 页面初始化,获取页面元素,注册事件
	 */
	private void init() {
		indexDiv = (RelativeLayout) findViewById(R.id.index_div);
		mIndexImg = (ImageView) findViewById(R.id.index_img);

		mMoreBtn = (Button) findViewById(R.id.more_btn);
		msquareBtn = (Button) findViewById(R.id.index_square_btn);
		videoSquareLayout = findViewById(R.id.video_square_layout);

		indexCarrecoderBtn = (ImageButton) findViewById(R.id.index_carrecoder_btn);
		this.updateRecoderBtn(mApp.mWiFiStatus);// 设置行测记录仪状态

		indexCarrecoderBtnlayout = (RelativeLayout) findViewById(R.id.index_carrecoder_btn_layout);
		userInfoLayout = findViewById(R.id.user_info);

		indexCarrecoderBtn.setOnClickListener(this);
		indexCarrecoderBtnlayout.setOnClickListener(this);

		indexDiv.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);
		mMoreBtn.setOnTouchListener(this);
		msquareBtn.setOnClickListener(this);

		boolean hotPointState = SettingUtils.getInstance().getBoolean("HotPointState", false);
		updateHotPointState(hotPointState);

		// 更新UI handler
		mMainHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case 1:
					// 视频第一针截取成功,刷新页面UI
					break;
				case 3:
					// 检测是否已连接小车本热点
					// 网络状态改变
					notifyLogicNetWorkState((Boolean) msg.obj);

					break;
				case 99:
					// 请求在线视频轮播数据
					mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
							IPageNotifyFn.PageType_GetPinData, "");
					break;
				case 400:
					// 已经绑定
					mApp.mIPCControlManager.setIPCWifiState(false, "");
					startWifi();
					if (null != mWac) {
						mWac.autoWifiManageReset();
					}
					break;
				}
			}
		};
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
		final String connJson = JsonUtil.getNetStateJson(isConnected);
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_MessageReport,
				IMessageReportFn.REPORT_CMD_NET_STATA_CHG, connJson);
		if (isConnected) {
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_CommCmd_RecoveryNetwork, "");
		}
	}

	private void initVideoSquare() {
		mVideoSquareActivity = new VideoSquareActivity(mRootLayout, this);
	}

	private void initUserInfo() {
		indexMoreActivity = new IndexMoreActivity(mRootLayout, this);
	}

	/**
	 * 播放视频下载完成声音
	 */
	private void playDownLoadedSound() {
		try {
			// 重置mediaPlayer实例，reset之后处于空闲状态
			mMediaPlayer.reset();
			// 设置需要播放的音乐文件的路径，只有设置了文件路径之后才能调用prepare
			AssetFileDescriptor fileDescriptor = this.getAssets().openFd(mVideoDownloadSoundFile);
			mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
					fileDescriptor.getLength());
			// 准备播放，只有调用了prepare之后才能调用start
			mMediaPlayer.prepare();
			// 开始播放
			mMediaPlayer.start();
		} catch (Exception ex) {

		}
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

				// 更新最新下载文件的时间
				long oldtime = SettingUtils.getInstance().getLong("downloadfiletime");
				time = time > oldtime ? time : oldtime;
				SettingUtils.getInstance().putLong("downloadfiletime", time);

				GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==8888===stopDownloadList" + time);
				updateHotPointState(true);

				if (null != PhotoAlbumActivity.mHandler) {
					Message msg = PhotoAlbumActivity.mHandler.obtainMessage(PhotoAlbumActivity.UPDATEDATE);
					msg.obj = filename;
					PhotoAlbumActivity.mHandler.sendMessage(msg);
				}

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

			if (CarRecorderActivity.mHandler != null) {
				CarRecorderActivity.mHandler.sendEmptyMessage(WIFI_STATE_CONNING);
			}
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

			if (state == WIFI_STATE_CONNING && isBindSucess()) {
				indexCarrecoderBtn.setBackgroundResource(R.anim.carrecoder_btn);
				ad = (AnimationDrawable) indexCarrecoderBtn.getBackground();
				if (ad.isRunning() == false) {
					ad.setOneShot(false);
					ad.start();
				}
			} else if (state == WIFI_STATE_SUCCESS) {
				indexCarrecoderBtn.setBackgroundResource(R.drawable.index_video_icon);
			} else if (state == WIFI_STATE_FAILED) {
				indexCarrecoderBtn.setBackgroundResource(R.drawable.tb_notconnected);
			} else {
				indexCarrecoderBtn.setBackgroundResource(R.drawable.tb_notconnected);

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
		GolukDebugUtils.e("zh：wifi连接成功 ", mApp.mWiFiStatus + "");
		if (CarRecorderActivity.mHandler != null) {
			GolukDebugUtils.e("zh：mhandler不为空 ", "");
			CarRecorderActivity.mHandler.sendEmptyMessage(WIFI_STATE_SUCCESS);
		}
	}

	// 连接失败
	private void wifiConnectFailed() {
		GolukDebugUtils.e("", "wifiCallBack-------------wifiConnectFailed:");
		mApp.mWiFiStatus = WIFI_STATE_FAILED;
		if (CarRecorderActivity.mHandler != null) {
			GolukDebugUtils.e("zh：mhandler不为空 ", "");
			CarRecorderActivity.mHandler.sendEmptyMessage(WIFI_STATE_FAILED);
		}
	}

	// 是否綁定过 Goluk
	private boolean isBindSucess() {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
		return preferences.getBoolean("isbind", false);
	}

	/**
	 * 检测wifi链接状态
	 */
	public void checkWiFiStatus() {
		// GolukDebugUtils.e("",
		// "wifiCallBack-------------checkWiFiStatus   type:" +
		// mApp.mWiFiStatus);
		// String info =
		// GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
		// 0, "");
		// GolukDebugUtils.i("lily", "---IndexMore--------" + info);
		// UCUserInfo user = new UCUserInfo();
		// try {
		// JSONObject json = new JSONObject(info);
		// user.uid = json.getString("uid");
		// user.nickname = json.getString("nickname");
		// user.headportrait = json.getString("head");
		// user.introduce = json.getString("desc");
		// user.sex = json.getString("sex");
		// user.customavatar = "";
		// user.praisemenumber = "0";
		// user.sharevideonumber = "0";
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// Intent i = new Intent(MainActivity.this, UserCenterActivity.class);
		// i.putExtra("userinfo",user);
		// startActivity(i);
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

		try {
			// 应用退出时调用
			CarRecorderManager.onExit(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		GolukApplication.getInstance().queryNewFileList();
		mApp.setContext(this, "Main");
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		if (null != mVideoSquareActivity) {
			mVideoSquareActivity.onResume();
		}

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

		indexMoreActivity.showView();
		super.onResume();
	}

	public void showContinuteLive() {
		GolukDebugUtils.e("", "jyf----20150406----showContinuteLive----showContinuteLive :");
		// 标识正常退出
		mApp.mSharedPreUtil.setIsLiveNormalExit(true);
		if (mApp.getIpcIsLogin()) {
			LiveDialogManager.getManagerInstance().showTwoBtnDialog(this, LiveDialogManager.DIALOG_TYPE_LIVE_CONTINUE,
					"提示", "是否继续直播");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (null != mVideoSquareActivity) {
			mVideoSquareActivity.onPause();
		}
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
			GolukUtils.showToast(getApplicationContext(), "再按一次退出程序");
			exitTime = System.currentTimeMillis();
		} else {
			unregisterListener();
			mApp.mIPCControlManager.setIPCWifiState(false, "");
			closeWifiHot();
			SysApplication.getInstance().exit();
			mApp.destroyLogic();
			if (null != UserStartActivity.mHandler) {
				UserStartActivity.mHandler.sendEmptyMessage(UserStartActivity.EXIT);
			}
			MobclickAgent.onKillProcess(this);
			finish();
			Fresco.shutDown();
			GolukNotification.getInstance().destroy();
			// int PID = android.os.Process.myPid();
			// android.os.Process.killProcess(PID);
			// System.exit(0);

			mApp.setExit(true);
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
		switch (v.getId()) {

		case R.id.more_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				Drawable user_down = this.getResources().getDrawable(R.drawable.index_user_btn_press);
				mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_down, null, null);
				mMoreBtn.setTextColor(Color.rgb(11, 89, 190));
				break;
			case MotionEvent.ACTION_UP:
				Drawable user_up = this.getResources().getDrawable(R.drawable.index_user_btn);
				mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_up, null, null);
				mMoreBtn.setTextColor(Color.rgb(204, 204, 204));
				break;
			}
			break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.more_btn:
			// 更多页面
			Drawable user_down = this.getResources().getDrawable(R.drawable.index_user_btn_press);
			mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_down, null, null);
			mMoreBtn.setTextColor(Color.rgb(11, 89, 190));

			Drawable square_up = this.getResources().getDrawable(R.drawable.index_find_btn);
			msquareBtn.setCompoundDrawablesWithIntrinsicBounds(null, square_up, null, null);
			msquareBtn.setTextColor(Color.rgb(204, 204, 204));

			userInfoLayout.setVisibility(View.VISIBLE);
			videoSquareLayout.setVisibility(View.GONE);

			indexMoreActivity.showView();
			break;
		case R.id.index_square_btn:
			// 视频广场
			Drawable square_down = this.getResources().getDrawable(R.drawable.index_find_btn_press);
			msquareBtn.setCompoundDrawablesWithIntrinsicBounds(null, square_down, null, null);
			msquareBtn.setTextColor(Color.rgb(11, 89, 190));

			Drawable user_up = this.getResources().getDrawable(R.drawable.index_user_btn);
			mMoreBtn.setCompoundDrawablesWithIntrinsicBounds(null, user_up, null, null);
			mMoreBtn.setTextColor(Color.rgb(204, 204, 204));

			userInfoLayout.setVisibility(View.GONE);
			videoSquareLayout.setVisibility(View.VISIBLE);
			setBelowItem(R.id.index_square_btn);
			break;
		case R.id.index_carrecoder_btn_layout:
		case R.id.index_carrecoder_btn:
			checkWiFiStatus();
			break;
		case R.id.index_div:
			if (divIndex == 0) {
				GolukUtils.freeBitmap(mIndexImg.getBackground());
				indexDiv.setVisibility(View.GONE);
			}
			break;
		}
	}

	public void setBelowItem(int id) {
		Drawable drawable;
		if (id == R.id.index_square_btn) {
			videoSquareLayout.setVisibility(View.VISIBLE);
			mVideoSquareActivity.onResume();
			drawable = this.getResources().getDrawable(R.drawable.index_find_btn_press);
			msquareBtn.setTextColor(Color.rgb(11, 89, 180));
			msquareBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
		}
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
		if (isShow) {

		} else {

		}
	}

	// 查看他人的直播
	public void startLiveLook(UserInfo userInfo) {
		GolukDebugUtils.e("", "jyf-----click------666666");
		if (null == userInfo) {
			return;
		}

		// 跳转看他人界面
		Intent intent = new Intent(this, LiveActivity.class);
		intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
		intent.putExtra(LiveActivity.KEY_GROUPID, "");
		intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
		intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
		intent.putExtra(LiveActivity.KEY_USERINFO, userInfo);

		startActivity(intent);
		GolukDebugUtils.e(null, "jyf----20150406----MainActivity----startLiveLook");
	}

	public void dismissAutoDialog() {

	}

	@Override
	public void statusChange() {
		if (mApp.autoLoginStatus != 1) {
			dismissAutoDialog();
			if (mApp.autoLoginStatus == 2) {
			}
		}
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

		final String address = (String) obj;
		GolukApplication.getInstance().mCurAddr = address;
		// 更新行车记录仪地址
		if (null != CarRecorderActivity.mHandler) {
			Message msg = CarRecorderActivity.mHandler.obtainMessage(CarRecorderActivity.ADDR);
			msg.obj = address;
			CarRecorderActivity.mHandler.sendMessage(msg);
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
				mApp.mIPCControlManager.setIPCWifiState(false, "");
				mApp.mIPCControlManager.setIPCWifiState(true, GolukApplication.mIpcIp);
			}
		}
	}

	private void wifiCallBack_ipcConnHotSucess(String message, Object arrays) {
		WifiRsBean[] bean = (WifiRsBean[]) arrays;
		if (null != bean) {
			GolukDebugUtils.e("", "自动wifi链接IPC连接上WIFI热点回调---length---" + bean.length);
			if (bean.length > 0) {
				GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1---ip---");
				mApp.mGolukName = bean[0].getIpc_ssid();
				sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
			}
		}
	}

	/**
	 * 设置IPC信息成功回调
	 */
	public void setIpcLinkWiFiCallBack(int state) {

	}

	private void wifiCallBack_3(int state, int process, String message, Object arrays) {
		if (state == 0) {
			switch (process) {
			case 0:
				// 创建热点成功
				break;
			case 1:
				// ipc成功连接上热点
				try {
					WifiRsBean[] bean = (WifiRsBean[]) arrays;
					if (null != bean) {
						GolukDebugUtils.e("", "IPC连接上WIFI热点回调---length---" + bean.length);
						if (bean.length > 0) {
							sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
						}
					}
				} catch (Exception e) {
					GolukUtils.showToast(mContext, "IPC连接热点返回信息不是数组");

				}
				break;
			default:
				GolukUtils.showToast(mContext, message);
				break;
			}
		} else {
			GolukUtils.showToast(mContext, message);
		}
	}

	private void wifiCallBack_5(int state, int process, String message, Object arrays) {
		if (state == 0) {
			switch (process) {
			case 0:
				// 创建热点成功
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

	@Override
	public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
		GolukDebugUtils.e("", "jyf-----MainActivity----wifiConn----wifiCallBack-------------type:" + type + "	state :"
				+ state + "	process:" + process);
		switch (type) {
		case 3:
			// wifiCallBack_3( state, process, message, arrays);
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
		GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1---ip---" + ip);
		GolukApplication.mIpcIp = ip;
		boolean b = mApp.mIPCControlManager.setIPCWifiState(true, ip);
		GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		if (!isSucess) {
			GolukUtils.showToast(this, "分享失败");
			return;
		}
		GolukDebugUtils.e("", "shareid-----" + shareVideoId + "   channel-----" + channel);
		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, shareVideoId);
	}

}
