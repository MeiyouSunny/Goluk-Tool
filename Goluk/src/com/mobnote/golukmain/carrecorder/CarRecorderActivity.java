package com.mobnote.golukmain.carrecorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.eventbus.EventLocationFinish;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.api.Image;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventUpdateAddr;
import com.mobnote.eventbus.EventWifiConnect;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.golukmain.carrecorder.IpcDataParser.TriggerRecord;
import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;
import com.mobnote.golukmain.carrecorder.entity.VideoFileInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoShareInfo;
import com.mobnote.golukmain.carrecorder.settings.SettingsActivity;
import com.mobnote.golukmain.carrecorder.settings.TSettingsActivity;
import com.mobnote.golukmain.carrecorder.settings.bean.WonderfulVideoJson;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.carrecorder.util.ReadWifiConfig;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.live.GetBaiduAddress;
import com.mobnote.golukmain.live.LiveActivity;
import com.mobnote.golukmain.live.LiveSettingBean;
import com.mobnote.golukmain.live.LiveSettingPopWindow;
import com.mobnote.golukmain.live.LiveSettingPopWindow.IPopwindowFn;
import com.mobnote.golukmain.livevideo.AbstractLiveActivity;
import com.mobnote.golukmain.livevideo.BaidumapLiveActivity;
import com.mobnote.golukmain.livevideo.GooglemapLiveActivity;
import com.mobnote.golukmain.photoalbum.FileInfoManagerUtils;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.golukmain.photoalbum.PhotoAlbumActivity;
import com.mobnote.golukmain.startshare.VideoEditActivity;
import com.mobnote.golukmain.videosuqare.RingView;
import com.mobnote.golukmain.wifibind.WifiUnbindSelectListActivity;
import com.mobnote.util.GolukFastJsonUtil;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SortByDate;
import com.mobnote.wifibind.WifiRsBean;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.player.RtspPlayerView;
import com.rd.car.player.RtspPlayerView.RtspPlayerLisener;

import de.greenrobot.event.EventBus;

/**
 * 
 * ????????????????????????
 * 
 * 2015???3???8???
 * 
 * @author xuhw
 */
@SuppressLint("NewApi")
public class CarRecorderActivity extends BaseActivity implements OnClickListener, OnTouchListener, IPCManagerFn,
		IPopwindowFn {
	private Handler mHandler = null;
	/** ????????????????????????????????? */
	public VideoType mCurVideoType = VideoType.idle;
	/** ??????????????????????????? */
	public String mRecordVideFileName = "";
	/** ???????????????????????? */
	public boolean isRecording = false;
	/** ?????????????????? */
	public static final int QUERYFILETIME = 500;
	/** ?????????????????????????????????????????? */
	public static final int QUERYFILEEXIT = 112;
	/** ???????????? */
	public static final int EMERGENCY = 113;
	/** 8s?????? */
	public static final int MOUNTS = 114;
	/** ????????????30??? **/
	public static final int CLASSIC = 115;
	/** ?????????????????????????????? */
	public static final int DOWNLOADWONDERFULVIDEO = 119;
	/** ??????adasView **/
	private static final int CLOSE_ADAS_VIEW = 120;

	public enum VideoType {
		mounts, emergency, idle, classic
	};

	/** ???????????? */
	public static final int SCREENSHOOT = 111;
	/** ?????????????????? */
	public static final int SCREENSHOOTTIME = 5 * 60000;
	/** 8s??????????????? */
	private Timer m8sTimer = null;
	/** ?????????????????? */
	private int mShootTime = 0;
	/** ?????????????????? */
	private Button m8sBtn = null;
	/** ???????????? **/
	private ImageButton liveBtn = null;
	/** ?????????????????? */
	private ImageView mFileBtn = null;
	/** ???????????? */
	private ImageView mSettingBtn = null;
	/** ?????????????????? */
	private TextView mTime = null;
	/** ?????????????????? */
	private TextView mAddr = null;
	/** ??????????????? */
	private LinearLayout mLoadingLayout = null;
	/** ??????????????????????????? */
	private ImageView mLoading = null;

	private RingView downloadSize = null;

	private String videoname = "";

	/** ????????????????????????????????? **/
	private String mNowDownloadName = "";

	private int videoType = 1;

	/**
	 * ???????????????????????????????????????
	 */
	private ImageView image1 = null;
	private ImageView image2 = null;

	/** ???????????? **/
	private ImageView image3 = null;

	/** ????????????????????? */
	private AnimationDrawable mAnimationDrawable = null;
	/** ????????????????????? */
	private TextView mLoadingText = null;
	/** rtsp??????????????? */
	private RtspPlayerView mRtspPlayerView = null;
	/** ????????????IPC???????????? */
	private final int RECONNECTIONTIME = 5000;
	/** ?????????????????????????????????10s????????? */
	private int videoFileQueryTime = 0;
	/** ?????????????????? */
	public static final int ADDR = 118;
	/** ???????????????????????? */
	private boolean rtmpIsOk = false;
	/** ?????????????????? */
	private int showRecordTime = 0;
	/** ??????????????????????????? */
	private final int STARTVIDEORECORD = 100;

	private boolean isBackGroundStart = true;
	/** ????????????????????? */
	private boolean ipcFirstLogin = false;
	/** ???????????????????????? */
	private boolean isConnecting = false;
	private RelativeLayout mVLayout = null;
	private RelativeLayout mRtmpPlayerLayout = null;
	private int screenWidth = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	private float density = SoundUtils.getInstance().getDisplayMetrics().density;
	/** ???????????? */
	private TextView mConnectTip = null;
	/** ?????????????????? */
	private String wonderfulVideoName = null;
	/** ???????????????????????? */
	private boolean downloadFinish = false;
	/** ??????????????????????????????????????? */
	private int downloadNumber = 0;
	/** ?????????????????????????????? */
	private int downloadFileNumber = 0;
	/** ??????????????? */
	private VideoConfigState mVideoConfigState = null;
	/** ????????????????????? */
	private ImageView mVideoResolutions = null;

	private GolukApplication mApp = null;
	private boolean m_bIsFullScreen = false;
	private ViewGroup m_vgNormalParent;
	private ImageButton mFullScreen = null;
	private ImageButton mVideoOff = null;
	private RelativeLayout mPlayerLayout = null;
	private Button mNormalScreen = null;
	private final int BTN_NORMALSCREEN = 231;

	private RelativeLayout mPalyerLayout = null;

	private TextView jcqp = null;

	private TextView fqzb = null;

	private boolean isShowPlayer = false;

	private LiveSettingPopWindow lsp;

	private RelativeLayout mRootLayout = null;

	private LayoutInflater mLayoutFlater = null;

	private TextView liveTime = null;

	/** ?????????????????? */
	LiveSettingBean mSettingData = null;

	/** ?????? **/
	private Button more = null;

	private int ipcState = 0;

	/** ????????? */
	private final int WIFI_STATE_FAILED = 0;
	/** ????????? */
	private final int WIFI_STATE_CONNING = 1;
	/** ?????? */
	private final int WIFI_STATE_SUCCESS = 2;

	private ImageView liveVideo;

	private View mNotconnected = null;

	private View mConncetLayout = null;

	private EditText et = null;

	private VideoShareInfo[] images = null;

	private ImageView live_gps;
	private ImageView live_talk;
	private ImageView live_release;

	private String wifiname;

	/** ?????????????????????????????? */
	private static final String APP_FOLDER = android.os.Environment.getExternalStorageDirectory().getPath();

	private String mFilePath = APP_FOLDER + "/" + "goluk/video/";

	private String mImagePath = APP_FOLDER + "/" + "goluk/goluk_carrecorder/image/";

	private int[] a = new int[2];
	private int[] e = new int[2];

	private ImageView new1;

	private ImageView new2;

	private String SelfContextTag = "carrecorder";

	private String mLocationAddress = "";

	private ImageView mChangeBtn;

	private ImageView mRedRoom;
	
	private boolean isRecVideo = false;
	
	private ImageView mAdasImg = null;
	
	private RelativeLayout mAdasStatusLayout = null;
	
	private ImageView mAdasIcon = null;
	public static final String ADAS_LINE_ST_LEFT = "adas_line_st_left";
	public static final String ADAS_LINE_ST_RIGHT = "adas_line_st_right";
	public static final String ADAS_DISTANCE_ST_LEFT = "adas_distance_left";
	public static final String ADAS_DISTANCE_ST_RIGHT = "adas_distance_right";
	public static final String ADAS_TARGET_STATE = "adas_target_state";
	public static final String ADAS_TARGET_DISTANCE = "adas_target_distance";
	public static final String ADAS_TARGET_SPEED = "adas_target_speed";
	public static final String ADAS_FONT_STARTUP = "adas_font_startup";
	
	/**??????????????????????????????**/
	public static final String ADASCONTENTSTATE = "2";
	
	/**?????????????????? ??????**/
	public static final String ADASCONTENTDISTANCE = "300";
	
	/**?????????????????? **/
	public static final String ADASCONTENTTARGETSTATE2 = "2";
	
	/**????????????????????? **/
	public static final String ADASCONTENTTARGETSTATE3 = "3";
	/**adas???????????????**/
	public static final long ADASTIMER = 2000;
	/**??????????????????**/
	private int mWonderfulTime;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLayoutFlater = LayoutInflater.from(this);
		mRootLayout = (RelativeLayout) mLayoutFlater.inflate(R.layout.carrecorder_main, null);

		setContentView(mRootLayout);
		mApp = (GolukApplication) getApplication();
		
		wifiname = this.getResources().getString(R.string.str_disconnect_ipc);

		mPlayerLayout = new RelativeLayout(this);
		mNormalScreen = new Button(this);
		mNormalScreen.setId(BTN_NORMALSCREEN);
		mNormalScreen.setBackgroundResource(R.drawable.btn_player_normal);
		mNormalScreen.setOnClickListener(this);
		ipcState = mApp.mWiFiStatus;

		lsp = new LiveSettingPopWindow(this, mRootLayout);
		lsp.setCallBackNotify(this);

		mSettingData = lsp.getCurrentSetting();
		mLocationAddress = com.mobnote.util.GolukFileUtils.loadString("loactionAddress", "");

		EventBus.getDefault().register(this);

		mHandler = new Handler() {
			public void handleMessage(final android.os.Message msg) {
				switch (msg.what) {
				case QUERYFILEEXIT:
					queryFileExit();
					break;
				case MOUNTS:
					startTrimVideo();
					break;
				case CLASSIC:
					start30TrimVideo();
					break;
				case STARTVIDEORECORD:
					updateVideoRecordTime();
					break;
				case DOWNLOADWONDERFULVIDEO:
					wonderfulVideoDownloadShow();
					break;
				case CLOSE_ADAS_VIEW:
					if(mAdasStatusLayout!= null){
						mAdasStatusLayout.setVisibility(View.GONE);
					}
					break;
				default:
					break;
				}
			};
		};

		initView();
		setListener();
		initIpcState(ipcState);// ?????????ipc???????????????

		// ???????????????????????????
		Intent receiveIntent = getIntent();
		isBackGroundStart = receiveIntent.getBooleanExtra("isBackGroundStart", false);

		// ??????????????????
		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("main", this);
		}

	}

	/**
	 * ??????ipc????????????
	 * 
	 * @param ipcS
	 * @author ??????
	 */
	private void initIpcState(int ipcS) {
		switch (ipcS) {
		case WIFI_STATE_FAILED:
			ipcConnFailed();
			break;
		case WIFI_STATE_CONNING:
			mConnectTip.setText(wifiname);
			mPalyerLayout.setVisibility(View.GONE);
			if (mApp.isBindSucess()) {
				mNotconnected.setVisibility(View.GONE);
				mConncetLayout.setVisibility(View.VISIBLE);
			} else {
				mNotconnected.setVisibility(View.VISIBLE);
				mConncetLayout.setVisibility(View.GONE);
			}
			mSettingBtn.setVisibility(View.GONE);
			mChangeBtn.setVisibility(View.GONE);
			m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
			liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
			setVideoBtnState(false);
			break;
		case WIFI_STATE_SUCCESS:
			// GolukApplication.getInstance().stopDownloadList();
			WifiRsBean wrb = ReadWifiConfig.readConfig();
			if (wrb != null) {
				mConnectTip.setText(wrb.getIpc_ssid());
			}
			mSettingBtn.setVisibility(View.VISIBLE);
			mPalyerLayout.setVisibility(View.VISIBLE);
			mNotconnected.setVisibility(View.GONE);
			mConncetLayout.setVisibility(View.GONE);
			mChangeBtn.setVisibility(View.VISIBLE);
			if (mApp.isIpcLoginSuccess) {
				liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon);
			} else {
				liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
			}

			setVideoBtnState(true);
			break;
		default:
			break;
		}
	}

	private void click_ConnFailed() {
		toSelectIpcActivity();
//		if (!mApp.isBindSucess()) {
//			Intent wifiIndex = new Intent(CarRecorderActivity.this, WiFiLinkIndexActivity.class);
//			startActivity(wifiIndex);
//		} else {
//			mNotconnected.setVisibility(View.GONE);
//			mConncetLayout.setVisibility(View.VISIBLE);
//			mPalyerLayout.setVisibility(View.GONE);
//			// if (null != MainActivity.mMainHandler) {
//			// MainActivity.mMainHandler.sendEmptyMessage(400);
//			// }
//			EventBus.getDefault().post(new EventBindFinish(EventConfig.CAR_RECORDER_BIND_SUCESS));
//		}
	}
	
	private void toSelectIpcActivity() {
		Intent intent = new Intent(this,WifiUnbindSelectListActivity.class);
		startActivity(intent);
	}

	private void open_shareVideo(String vname) {
		// // ?????????wifi????????????
		String path = Environment.getExternalStorageDirectory().getPath();
		int type = 2;
		if (vname.indexOf("URG") >= 0) {
			path = path + "/goluk/video/urgent/" + vname;
			type = 3;
		} else {
			path = path + "/goluk/video/wonderful/" + vname;
		}
		GolukDebugUtils.e("xuhw", "YYY====mShareBtn===path=" + path);

		SettingUtils.getInstance().putBoolean("Local_" + vname, false);

		Intent i = new Intent(CarRecorderActivity.this, VideoEditActivity.class);
		i.putExtra("cn.com.mobnote.video.path", path);
		i.putExtra("type", type);
		startActivity(i);
		overridePendingTransition(R.anim.shortshare_start, 0);

	}

	/**
	 * ????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???4???8???
	 */
	private void wonderfulVideoDownloadShow() {
		if (!TextUtils.isEmpty(wonderfulVideoName)) {
			downloadNumber++;
			mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);

			if (!downloadFinish) {
				if (1 == downloadNumber) {
					// mShareBtn.setBackgroundResource(R.drawable.screen_loading_1);
				} else if (2 == downloadNumber) {
					// mShareBtn.setBackgroundResource(R.drawable.screen_loading_2);
				} else {
					downloadNumber = 0;
					// mShareBtn.setBackgroundResource(R.drawable.screen_loading_3);
				}

				mHandler.sendEmptyMessageDelayed(DOWNLOADWONDERFULVIDEO, 600);
			}
		}

	}

	/**
	 * ???????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???9???
	 */
	private void initView() {
		mPalyerLayout = (RelativeLayout) findViewById(R.id.mPalyerLayout);
		mFullScreen = (ImageButton) findViewById(R.id.mFullScreen);
		mVideoOff = (ImageButton) findViewById(R.id.video_off);
		mVideoOff.setVisibility(View.GONE);
		mFullScreen.setVisibility(View.GONE);
		mVideoResolutions = (ImageView) findViewById(R.id.mVideoResolutions);
		mRtmpPlayerLayout = (RelativeLayout) findViewById(R.id.mRtmpPlayerLayout);
		mVLayout = (RelativeLayout) findViewById(R.id.vLayout);
		m8sBtn = (Button) findViewById(R.id.m8sBtn);
		mSettingBtn = (ImageView) findViewById(R.id.mSettingBtn);
		mTime = (TextView) findViewById(R.id.mTime);
		mAddr = (TextView) findViewById(R.id.mAddr);
		mConnectTip = (TextView) findViewById(R.id.mConnectTip);
		mLoadingLayout = (LinearLayout) findViewById(R.id.mLoadingLayout);
		mLoading = (ImageView) findViewById(R.id.mLoading);
		mLoading.setBackgroundResource(R.anim.video_loading);
		mAnimationDrawable = (AnimationDrawable) mLoading.getBackground();
		mLoadingText = (TextView) findViewById(R.id.mLoadingText);
		jcqp = (TextView) findViewById(R.id.jcqp);
		fqzb = (TextView) findViewById(R.id.fqzb);
		liveTime = (TextView) findViewById(R.id.live_time);
		more = (Button) findViewById(R.id.car_recorder);

		liveBtn = (ImageButton) findViewById(R.id.liveBtn);
		mRtspPlayerView = (RtspPlayerView) findViewById(R.id.mRtmpPlayerView);
		image1 = (ImageView) findViewById(R.id.image1);
		image2 = (ImageView) findViewById(R.id.image2);
		image3 = (ImageView) findViewById(R.id.image3);
		liveVideo = (ImageView) findViewById(R.id.live_video);
		downloadSize = (RingView) findViewById(R.id.downloadSize);

		live_gps = (ImageView) findViewById(R.id.live_gps_icon);
		live_talk = (ImageView) findViewById(R.id.live_talk_icon);
		live_release = (ImageView) findViewById(R.id.live_release_icon);
		mChangeBtn = (ImageView) findViewById(R.id.changeBtn);
		mRedRoom = (ImageView) findViewById(R.id.red_room);

		new1 = (ImageView) findViewById(R.id.new1);
		new2 = (ImageView) findViewById(R.id.new2);
		mAdasImg = (ImageView) findViewById(R.id.adas_status_img);
		mAdasStatusLayout = (RelativeLayout) findViewById(R.id.adas_status);
		mAdasIcon = (ImageView) findViewById(R.id.adas_icon);

		liveVideo.setBackgroundResource(R.drawable.driving_voice_off_icon);

		mRtspPlayerView.setAudioMute(true);
		mRtspPlayerView.setZOrderMediaOverlay(true);
		mRtspPlayerView.setBufferTime(1000);
		mRtspPlayerView.setConnectionTimeout(30000);
		mRtspPlayerView.setVisibility(View.VISIBLE);
		mConncetLayout = findViewById(R.id.mConncetLayout);
		mNotconnected = findViewById(R.id.mNotconnected);
		et = (EditText) findViewById(R.id.assess);
		liveTime.setText(GolukUtils.secondToString(mSettingData.duration));
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRtmpPlayerLayout.getLayoutParams();
		lp.width = screenWidth;
		lp.height = (int) (screenWidth / 1.7833);
		lp.leftMargin = 0;
		mRtmpPlayerLayout.setLayoutParams(lp);

		mConnectTip.setText(wifiname);
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
		}

		if ("".equals(mLocationAddress)) {
			mAddr.setText(this.getResources().getString(R.string.str_localization_ongoing));
		} else {
			mAddr.setText(mLocationAddress);
		}

	}

	/**
	 * ??????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???11???
	 */
	private void setListener() {
		findViewById(R.id.mPlayBtn).setOnClickListener(this);
		mPalyerLayout.setOnClickListener(this);
		mFullScreen.setOnClickListener(this);
		mVideoOff.setOnClickListener(this);
		m8sBtn.setOnClickListener(this);
		jcqp.setOnClickListener(this);
		fqzb.setOnClickListener(this);
		more.setOnClickListener(this);
		liveBtn.setOnClickListener(this);
		mNotconnected.setOnClickListener(this);
		image1.setOnClickListener(this);
		image2.setOnClickListener(this);
		image3.setOnClickListener(this);
		more.setOnTouchListener(this);
		live_gps.setOnClickListener(this);
		live_release.setOnClickListener(this);
		live_talk.setOnClickListener(this);
		liveVideo.setOnClickListener(this);
		liveTime.setOnClickListener(this);
		mRtspPlayerView.setOnClickListener(this);
		mConncetLayout.setOnClickListener(this);
		mChangeBtn.setOnClickListener(this);

		findViewById(R.id.back_btn).setOnClickListener(this);
		findViewById(R.id.mSettingBtn).setOnClickListener(this);
		mRtspPlayerView.setPlayerListener(new RtspPlayerLisener() {

			@Override
			public void onPlayerPrepared(RtspPlayerView rpv) {
				mRtspPlayerView.setHideSurfaceWhilePlaying(true);
			}

			@Override
			public boolean onPlayerError(RtspPlayerView rpv, int what, int extra, String strErrorInfo) {
				GolukDebugUtils.e("xuhw", "CarrecorderActivity-------onPlayerError=======");
				hidePlayer();
				rtmpIsOk = false;
				rpv.removeCallbacks(retryRunnable);
				showLoading();
				rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
				if (m_bIsFullScreen) {
					setFullScreen(false);
				}
				return false;
			}

			@Override
			public void onPlayerCompletion(RtspPlayerView rpv) {
				GolukDebugUtils.e("xuhw", "CarrecorderActivity-------onPlayerCompletion=======");
				hidePlayer();
				rtmpIsOk = false;
				rpv.removeCallbacks(retryRunnable);
				showLoading();
				rpv.postDelayed(retryRunnable, RECONNECTIONTIME);
				if (m_bIsFullScreen) {
					setFullScreen(false);
				}
			}

			@Override
			public void onPlayBuffering(RtspPlayerView rpv, boolean start) {
				if (start) {
					// ????????????
					showLoading();
				} else {
					// ????????????
					hideLoading();
				}
			}

			@Override
			public void onGetCurrentPosition(RtspPlayerView rpv, int nPosition) {
			}

			@Override
			public void onPlayerBegin(RtspPlayerView arg0) {
				GolukDebugUtils.e("xuhw", "CarrecorderActivity-------onPlayerBegin=======");
				hideLoading();
				rtmpIsOk = true;
				// ???????????????
				showPlayer();
				// ??????
				mPalyerLayout.setVisibility(View.GONE);
				mFullScreen.setVisibility(View.VISIBLE);
				
				
				mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
				if(IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)){//T1
					if(GolukApplication.getInstance().getT1VideoCfgState() == 1){
						isRecVideo = true;
					}else{
						isRecVideo = false;
					}
				}else{//G1  G2  G1S
					if(mVideoConfigState != null && 1 == mVideoConfigState.AudioEnabled){
						isRecVideo = true;
					}else{
						isRecVideo = false;
					}
				}
				
				
				
				if(isRecVideo == false){
					mVideoOff.setBackgroundResource(R.drawable.recorder_btn_nosound);
				}else{
					mVideoOff.setBackgroundResource(R.drawable.recorder_btn_sound);
				}
				mVideoOff.setVisibility(View.VISIBLE);

			}
		});
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
		int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVLayout.getLayoutParams();
		lp.width = width;
		lp.height = (int) (width / 1.777);
		lp.leftMargin = 0;
		mVLayout.setLayoutParams(lp);

		isShowPlayer = true;
	}

	/**
	 * ????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???11???
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
				minStr = min + this.getResources().getString(R.string.str_colon_english);
			} else {
				minStr = "0" + min + this.getResources().getString(R.string.str_colon_english);
			}
			if (sec >= 10) {
				secStr = sec + "";
			} else {
				secStr = "0" + sec;
			}

			timeStr = minStr + secStr;
		} else {
			if (showRecordTime >= 10) {
				timeStr = this.getResources().getString(R.string.str_recorder_time1) + showRecordTime;
			} else {
				timeStr = this.getResources().getString(R.string.str_recorder_time2) + showRecordTime;
			}
		}
		mTime.setText(timeStr);
		mHandler.removeMessages(STARTVIDEORECORD);
		mHandler.sendEmptyMessageDelayed(STARTVIDEORECORD, 1000);
	}

	/**
	 * ??????runnable
	 */
	private Runnable retryRunnable = new Runnable() {
		@Override
		public void run() {
			isConnecting = true;
			start();
		}
	};

	/**
	 * ??????????????????
	 * 
	 * @author xuhw
	 * @date 2015???1???28???
	 */
	public void start() {
		if (null != mRtspPlayerView) {
			mRtspPlayerView.setVisibility(View.VISIBLE);
			String url = PlayUrlManager.getRtspUrl();
			GolukDebugUtils.e("xuhw", "CarrecorderActivity-------start--YYYYYY======url==" + url + "   "
					+ mApp.mIPCControlManager.mProduceName);
			mRtspPlayerView.setDataSource(url);
			mRtspPlayerView.start();
		}
	}

	public void onEventMainThread(EventWifiConnect event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.WIFI_STATE_FAILED:
			ipcConnFailed();
			break;
		case EventConfig.WIFI_STATE_CONNING:
			ipcConnecting();
			break;
		case EventConfig.WIFI_STATE_SUCCESS:
			ipcConnSucess();
			break;
		default:
			break;
		}
	}

	public void onEventMainThread(EventUpdateAddr event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.CAR_RECORDER_UPDATE_ADDR:
			String addr = event.getMsg();
			if (!TextUtils.isEmpty(addr)) {
				mAddr.setText(addr);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * ????????????????????????????????????????????? ????????????????????????
	 * 
	 * @param flog
	 */
	private void setVideoBtnState(Boolean flog) {

		if (flog) {
			mRedRoom.setImageResource(R.drawable.driving_car_red_point);
			if (videoType == 1) {// ????????????
				jcqp.setTextColor(getResources().getColor(R.color.text_select_color));
			} else {
				fqzb.setTextColor(getResources().getColor(R.color.text_select_color));
			}
		} else {
			mRedRoom.setImageResource(R.drawable.carrecorder_icon_record_1);
			if (videoType == 1) {// ????????????
				jcqp.setTextColor(getResources().getColor(R.color.text_diff_color));
			} else {
				selectJcqp();
				fqzb.setTextColor(getResources().getColor(R.color.text_diff_color));
			}

		}
	}

	@Override
	public void onClick(View arg0) {
		if (!isAllowedClicked())
			return;

		int id = arg0.getId();
		if (id == R.id.back_btn) {
			if (m_bIsFullScreen) {
				return;
			}
			exit();
		} else if (id == R.id.m8sBtn) {
			if (m_bIsFullScreen) {
				return;
			}
			GolukDebugUtils.e("xuhw", "m8sBtn========================11111======");
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				if (!isRecording) {
					m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
					isRecording = true;
					if (mWonderfulTime == 30) {
						mCurVideoType = VideoType.classic;
					} else {
						mCurVideoType = VideoType.mounts;
					}
					GolukDebugUtils.e("xuhw", "m8sBtn========================2222======");
					boolean isSucess = GolukApplication.getInstance().getIPCControlManager().startWonderfulVideo();

					GolukDebugUtils.e("xuhw", "m8sBtn========================333===isSucess===" + isSucess);
					if (!isSucess) {
						videoTriggerFail();
					}
				}
			}
		} else if (id == R.id.mSettingBtn) {
			if (m_bIsFullScreen) {
				return;
			}
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				Intent setting = null;
				if (IPCControlManager.T1_SIGN
						.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
					setting = new Intent(CarRecorderActivity.this, TSettingsActivity.class);
					startActivity(setting);
				} else {
					setting = new Intent(CarRecorderActivity.this, SettingsActivity.class);
					startActivity(setting);
				}
			}
		} else if (id == R.id.mFullScreen) {
			setFullScreen(true);
		} else if (id == R.id.video_off) {
			int videoState = 0;
			if(isRecVideo == true){
				videoState = 0;
			}else{
				videoState = 1;
			}
			if(IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)){
				boolean isSuccess = GolukApplication.getInstance().getIPCControlManager().setAudioCfg_T1(videoState);
				if(isSuccess){
					if(videoState == 1){
						isRecVideo = true;
						mVideoOff.setBackgroundResource(R.drawable.recorder_btn_sound);
					}else{
						isRecVideo = false;
						mVideoOff.setBackgroundResource(R.drawable.recorder_btn_nosound);
					}
					
				}else{
					GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
				}
			}else{
				mVideoConfigState.AudioEnabled = videoState;
				boolean flog = GolukApplication.getInstance().getIPCControlManager().setAudioCfg(mVideoConfigState);
				if(flog){
					if(videoState == 1){
						isRecVideo = true;
						mVideoOff.setBackgroundResource(R.drawable.recorder_btn_sound);
					}else{
						isRecVideo = false;
						mVideoOff.setBackgroundResource(R.drawable.recorder_btn_nosound);
					}
					
				}else{
					GolukUtils.showToast(this, getResources().getString(R.string.str_carrecoder_setting_failed));
				}
			}
		} else if (id == BTN_NORMALSCREEN) {
			setFullScreen(false);
		} else if (id == R.id.jcqp) {
			if (videoType == 0) {
				selectJcqp();
			}
		} else if (id == R.id.fqzb) {
			if (GolukApplication.getInstance().getIpcIsLogin()) {

				if (videoType == 1) {
					jcqp.setTextColor(getResources().getColor(R.color.text_diff_color));
					fqzb.setTextColor(getResources().getColor(R.color.text_select_color));
					liveBtn.setVisibility(View.VISIBLE);
					m8sBtn.setVisibility(View.INVISIBLE);

					findViewById(R.id.fqzb_info).setVisibility(View.VISIBLE);
					findViewById(R.id.jcqp_info).setVisibility(View.INVISIBLE);

					a.clone();// ????????????
					e.clone();// ????????????

					jcqp.getLocationInWindow(a);
					fqzb.getLocationInWindow(e);

					slideview(a[0] - e[0], jcqp);
					slideview(a[0] - e[0], fqzb);
					videoType = 0;
				}
			}
		} else if (id == R.id.mPlayBtn) {
			GolukDebugUtils.e("xuhw", "CarrecorderActivity-------onclick======isShowPlayer==" + isShowPlayer + "   "
					+ isConnecting);
			if (!isShowPlayer) {
				if (!isConnecting) {
					isConnecting = true;
					start();
				}

				showLoading();
				hidePlayer();
				mPalyerLayout.setVisibility(View.GONE);
			}
		} else if (id == R.id.car_recorder) {
			lsp.show();
		} else if (id == R.id.mNotconnected) {
			click_ConnFailed();
		} else if (id == R.id.liveBtn) {
			if (GolukApplication.getInstance().getIpcIsLogin()) {
				if (mApp.isUserLoginSucess == false) {
					Intent it = null;
					if(GolukApplication.getInstance().isInteral()  == false){
						it = new Intent(this, InternationUserLoginActivity.class);
					}else{
						it = new Intent(this, UserLoginActivity.class);
					}
					it.putExtra("isInfo", "back");
					startActivity(it);
				} else {
//					if (!this.isT1()) {
						toLive();
//					}

				}

			}
		} else if (id == R.id.image1) {
			new1.setVisibility(View.GONE);
			if (images[0] != null) {
				// ???????????????????????????????????????????????????????????????
				if (downloadSize.getVisibility() != View.VISIBLE) {
					open_shareVideo(images[0].getName());
				}
			}
		} else if (id == R.id.image2) {
			new2.setVisibility(View.GONE);
			if (images[1] == null || images[1].getName().equals("")) {
				return;
			} else {
				open_shareVideo(images[1].getName());
			}
		} else if (id == R.id.image3) {
			Intent photoalbum = new Intent(CarRecorderActivity.this, PhotoAlbumActivity.class);
			photoalbum.putExtra("from", "cloud");
			startActivity(photoalbum);
		} else if (id == R.id.live_video
				|| id == R.id.live_gps_icon
				|| id == R.id.live_talk_icon
				|| id == R.id.live_release_icon
				|| id == R.id.live_time) {
			lsp.show();
		} else if (id == R.id.mRtmpPlayerView) {
			if (!GolukApplication.getInstance().getIpcIsLogin())
				return;
			if (m_bIsFullScreen) {
				setFullScreen(false);
			} else {
				rtmpIsOk = false;
				mRtspPlayerView.removeCallbacks(retryRunnable);
				GolukDebugUtils.e("xuhw", "YYYYYY======stopPlayback");
				mRtspPlayerView.stopPlayback();
				hidePlayer();
				isShowPlayer = false;
				isConnecting = false;
				mPalyerLayout.setVisibility(View.VISIBLE);
				mNotconnected.setVisibility(View.GONE);
				mConncetLayout.setVisibility(View.GONE);
				mFullScreen.setVisibility(View.GONE);
				mVideoOff.setVisibility(View.GONE);
			}
		} else if (id == R.id.mConncetLayout
				|| id == R.id.changeBtn) {
			Intent intent = new Intent(this, WifiUnbindSelectListActivity.class);
			startActivity(intent);
		} else {
		}
	}

	private void selectJcqp() {
		jcqp.setTextColor(getResources().getColor(R.color.text_select_color));
		fqzb.setTextColor(getResources().getColor(R.color.text_diff_color));
		liveBtn.setVisibility(View.INVISIBLE);
		m8sBtn.setVisibility(View.VISIBLE);

		a.clone();// ????????????
		e.clone();// ????????????

		jcqp.getLocationInWindow(a);
		fqzb.getLocationInWindow(e);

		slideview(e[0] - a[0], jcqp);
		slideview(e[0] - a[0], fqzb);

		findViewById(R.id.fqzb_info).setVisibility(View.INVISIBLE);
		findViewById(R.id.jcqp_info).setVisibility(View.VISIBLE);

		videoType = 1;
	}

	/**
	 * ????????????
	 * 
	 * @author jyf
	 */
	private void toLive() {
		
		Intent intent;
		if(GolukApplication.getInstance().isInteral()){
			intent = new Intent(this, BaidumapLiveActivity.class);
		}else{
			intent = new Intent(this, GooglemapLiveActivity.class);
		}
		
		String desc = et.getText().toString();
		if (null == desc || "".equals(desc)) {
			desc = this.getResources().getString(R.string.str_wonderful_live);
		}
		mSettingData.desc = desc;
		intent.putExtra(AbstractLiveActivity.KEY_IS_LIVE, true);
		intent.putExtra(AbstractLiveActivity.KEY_GROUPID, "");
		intent.putExtra(AbstractLiveActivity.KEY_PLAY_URL, "");
		intent.putExtra(AbstractLiveActivity.KEY_JOIN_GROUP, "");
		intent.putExtra(AbstractLiveActivity.KEY_LIVE_SETTING_DATA, mSettingData);
		startActivity(intent);
	}

	/**
	 * ?????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???8???
	 */
	private void showLoading() {
		mLoadingText.setText(this.getResources().getString(R.string.str_video_loading));
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
	 * ???????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???8???
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

	private void ipcConnecting() {
		mConnectTip.setText(wifiname);
		mFullScreen.setVisibility(View.GONE);
		mVideoOff.setVisibility(View.GONE);
		mSettingBtn.setVisibility(View.GONE);
		mChangeBtn.setVisibility(View.GONE);
		setVideoBtnState(false);
		if (mApp.isBindSucess()) {
			mPalyerLayout.setVisibility(View.GONE);
			mNotconnected.setVisibility(View.GONE);
			mConncetLayout.setVisibility(View.VISIBLE);

			m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
			liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
		} else {
			mPalyerLayout.setVisibility(View.GONE);
			mNotconnected.setVisibility(View.VISIBLE);
			mConncetLayout.setVisibility(View.GONE);

			m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
			liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
		}
	}

	private void ipcConnFailed() {
		mFullScreen.setVisibility(View.GONE);
		mVideoOff.setVisibility(View.GONE);
		mConnectTip.setText(wifiname);
		mPalyerLayout.setVisibility(View.GONE);
		mNotconnected.setVisibility(View.VISIBLE);
		mConncetLayout.setVisibility(View.GONE);
		mSettingBtn.setVisibility(View.GONE);
		mChangeBtn.setVisibility(View.GONE);

		setVideoBtnState(false);

		m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon_1);
		liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
	}

	private void ipcConnSucess() {
		if (isShowPlayer || isConnecting) {
			showLoading();
			hidePlayer();
			mRtspPlayerView.removeCallbacks(retryRunnable);
			isConnecting = true;
			start();
		} else {
			isShowPlayer = false;
			isConnecting = false;
			mPalyerLayout.setVisibility(View.VISIBLE);
		}

		WifiRsBean wrb = ReadWifiConfig.readConfig();
		if (wrb != null) {
			mConnectTip.setText(wrb.getIpc_ssid());
		}
		setVideoBtnState(true);
		mNotconnected.setVisibility(View.GONE);
		mConncetLayout.setVisibility(View.GONE);
		mChangeBtn.setVisibility(View.VISIBLE);
		mSettingBtn.setVisibility(View.VISIBLE);
		m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
//		if (!isT1()) {
			liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon);
//		} else {
//			liveBtn.setBackgroundResource(R.drawable.driving_car_living_icon_1);
//		}

	}

	private boolean isT1() {
		return IPCControlManager.T1_SIGN.equals(mApp.getIPCControlManager().mProduceName);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mApp.mIPCControlManager.isG1Relative()) {
			GolukApplication.getInstance().stopDownloadList();// ??????????????????
		}
		
		if(mApp.mIPCControlManager.mProduceName.equals(IPCControlManager.T1_SIGN)){
			mVideoResolutions.setVisibility(View.GONE);
			setAdasIconState(true);
		}else{
			mVideoResolutions.setVisibility(View.VISIBLE);
			setAdasIconState(false);
		}

		if (isShowPlayer) {
			GolukDebugUtils.e("xuhw", "YYYYYY======isConnecting==" + isConnecting);
			if (!isConnecting) {
				showLoading();
				hidePlayer();
				isConnecting = true;
				start();
			}
		}

		GolukApplication.getInstance().setContext(this, "carrecorder");
		if (isBackGroundStart) {
			this.moveTaskToBack(true);
			isBackGroundStart = false;
		}

		if (!downloadFinish) {
			if (downloadFileNumber <= 0) {
				downloadFileNumber = 0;
				downloadFinish = false;
			} else {
				GolukDebugUtils.e("xuhw", "KKKK=================================");
				downloadFinish = false;
				mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
				mHandler.sendEmptyMessage(DOWNLOADWONDERFULVIDEO);
			}
		}

		mVideoConfigState = GolukApplication.getInstance().getVideoConfigState();
		if (null != mVideoConfigState) {
			
			if ("1080P".equals(mVideoConfigState.resolution)) {
				mVideoResolutions.setBackgroundResource(R.drawable.icon_hd1080);
			} else {
				mVideoResolutions.setBackgroundResource(R.drawable.icon_hd720);
			}
		}
		// ????????????????????????????????????
		// mApp.addLocationListener(SelfContextTag, this);
		// GetBaiduAddress.getInstance().setCallBackListener(mBaiduGeoCoderFn);
		initVideoImage();// ?????????????????????
		
		// ????????????????????????
		boolean wonderfulType = GolukApplication.getInstance().getIPCControlManager().getWonderfulVideoType();
		GolukDebugUtils.e("", "CarRecorderActivity-------------------wonderfulType???" + wonderfulType);
	}

	@Override
	protected void onPause() {
		super.onPause();
		GolukDebugUtils.e("xuhw", "YYYYYY======onPause======");
		if (isShowPlayer) {
			if (null != mRtspPlayerView) {
				rtmpIsOk = false;
				mFullScreen.setVisibility(View.GONE);
				mVideoOff.setVisibility(View.GONE);
				mRtspPlayerView.removeCallbacks(retryRunnable);
				if (mRtspPlayerView.isPlaying()) {
					isConnecting = false;
					mRtspPlayerView.stopPlayback();
					GolukDebugUtils.e("xuhw", "YYYYYY======stopPlayback======");
				}
				hidePlayer();
			}
		}
		// ????????????????????????????????????
		mApp.removeLocationListener(SelfContextTag);
		GetBaiduAddress.getInstance().setCallBackListener(null);
	}

	@Override
	protected void onStop() {
		super.onStop();
		GolukDebugUtils.e("xuhw", "YYYYYY======onStop======");
	}

	@Override
	protected void onDestroy() {
		GolukDebugUtils.e("xuhw", "YYYYYY======onDestroy======");
		if (null != mRtspPlayerView) {
			mRtspPlayerView.removeCallbacks(retryRunnable);
			mRtspPlayerView.cleanUp();
		}

		if (null != GolukApplication.getInstance().getIPCControlManager()) {
			GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("main");
		}
		if (mHandler != null) {
			mHandler.removeMessages(QUERYFILEEXIT);
			mHandler.removeMessages(MOUNTS);
			mHandler.removeMessages(STARTVIDEORECORD);
			mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
			mHandler = null;
		}
		if (m8sTimer != null) {
			m8sTimer.cancel();
			m8sTimer.purge();
			m8sTimer = null;
		}
		mWonderfulTime = 0;
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	/**
	 * 8s??????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???4???
	 */
	private void startTrimVideo() {
		if (null == m8sTimer) {
			m8sBtn.setText("");
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
								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon6);
								break;
							case 2:
								break;
							case 3:
								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon5);
								break;
							case 4:
								break;
							case 5:
								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon4);
								break;
							case 6:
								break;
							case 7:
								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon3);
								break;
							case 8:
								break;
							case 9:
								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon2);
								break;
							case 10:
								break;
							case 11:
								m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon1);
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
	 * 30s????????????
	 */
	private void start30TrimVideo(){
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
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("30");
								break;
							case 2:
								break;
							case 3:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("29");
								break;
							case 4:
								break;
							case 5:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("28");
								break;
							case 6:
								break;
							case 7:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("27");
								break;
							case 8:
								break;
							case 9:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("26");
								break;
							case 10:
								break;
							case 11:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("25");
								break;
							case 13:
								break;
							case 14:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("24");
								break;
							case 15:
								break;
							case 16:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("23");
								break;
							case 17:
								break;
							case 18:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("22");
								break;
							case 19:
								break;
							case 20:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("21");
								break;
							case 21:
								break;
							case 22:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("20");
								break;
							case 23:
								break;
							case 24:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("19");
								break;
							case 25:
								break;
							case 26:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("18");
								break;
							case 27:
								break;
							case 28:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("17");
								break;
							case 29:
								break;
							case 30:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("16");
								break;
							case 31:
								break;
							case 32:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("15");
								break;
							case 33:
								break;
							case 34:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("14");
								break;
							case 35:
								break;
							case 36:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("13");
								break;
							case 37:
								break;
							case 38:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("12");
								break;
							case 39:
								break;
							case 40:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("11");
								break;
							case 41:
								break;
							case 42:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("10");
								break;
							case 43:
								break;
							case 44:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("9");
								break;
							case 45:
								break;
							case 46:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("8");
								break;
							case 47:
								break;
							case 48:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("7");
								break;
							case 49:
								break;
							case 50:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("6");
								break;
							case 51:
								break;
							case 52:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("5");
								break;
							case 53:
								break;
							case 54:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("4");
								break;
							case 55:
								break;
							case 56:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("3");
								break;
							case 57:
								break;
							case 58:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("2");
								break;
							case 61:
								break;
							case 59:
								m8sBtn.setBackgroundResource(R.drawable.btn_12s);
								m8sBtn.setText("1");
								break;
							case 60:
								break;
							default:
								break;
							}

							if (mShootTime > 60) {
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
	 * ?????????s????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???8???
	 */
	private void stopTrimVideo() {
		if (!IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
			mHandler.sendEmptyMessageDelayed(CarRecorderActivity.QUERYFILEEXIT, CarRecorderActivity.QUERYFILETIME);
		}
		mShootTime = 0;
		m8sBtn.setText("");
		m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
		if (null != m8sTimer) {
			m8sTimer.cancel();
			m8sTimer.purge();
			m8sTimer = null;
		}

	}

	/**
	 * ?????????????????????view
	 */
	private void canvasProcess() {
		downloadSize.setProcess(0);
		downloadSize.setVisibility(View.VISIBLE);
	}

	/**
	 * ??????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???8???
	 */
	private void queryFileExit() {
		videoFileQueryTime++;
		mHandler.removeMessages(QUERYFILEEXIT);
		if (!TextUtils.isEmpty(mRecordVideFileName)) {
			if (videoFileQueryTime <= 15) {
				if (GolukApplication.getInstance().getIpcIsLogin()) {
					boolean isSucess = GolukApplication.getInstance().getIPCControlManager()
							.querySingleFile(mRecordVideFileName);
					GFileUtils.writeIPCLog("===============queryFileExit==================videoFileQueryTime="
							+ videoFileQueryTime);
					if (!isSucess) {
						GFileUtils
								.writeIPCLog("===============queryFileExit=============isSucess  fail======================");
						mHandler.sendEmptyMessageDelayed(QUERYFILEEXIT, 1000);
					}
				} else {
					// IPC?????????
				}
			} else {
				videoFileQueryTime = 0;
				videoTriggerFail();

				GFileUtils.writeIPCLog("============queryFileExit=====111111111111111??????????????????========================");

			}
		} else {
			videoFileQueryTime = 0;
			resetTrimVideoState();
		}

	}

	/**
	 * ????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???5???
	 */
	private void resetTrimVideoState() {
		isRecording = false;
		mCurVideoType = VideoType.idle;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
			}
		});
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???18???
	 */
	private void videoTriggerFail() {
		if (mCurVideoType == VideoType.emergency) {
		} else if (mCurVideoType == VideoType.mounts) {
		}
		resetTrimVideoState();
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			if (ConnectionStateMsg_Connected != msg) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						downloadFileNumber = 0;
						mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
						if (m_bIsFullScreen) {
							setFullScreen(false);
						}
					}
				});
			}
		}

		if (ENetTransEvent_IPC_VDCP_CommandResp == event && IPC_VDCP_Msg_Init == msg && 0 == param1) {
			if (!ipcFirstLogin) {
				ipcFirstLogin = true;
				if (isShowPlayer) {
					if (!isConnecting) {
						isConnecting = true;
						start();
					}
				}

			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mApp.isBindSucess()) {
						m8sBtn.setBackgroundResource(R.drawable.driving_car_living_defalut_icon);
					}
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
	 * ??????VDCP????????????
	 * 
	 * @param msg
	 *            ?????????id
	 * @param param1
	 *            0:?????????????????? ???0:????????????
	 * @param param2
	 *            ???????????????json?????????
	 * @author xuhw
	 * @date 2015???3???17???
	 */
	@SuppressLint("SimpleDateFormat")
	private void callBack_VDCP(int msg, int param1, Object param2) {
		switch (msg) {
		// ????????????
		case IPC_VDCPCmd_SnapPic:
			GFileUtils.writeIPCLog("============???????????????=======????????????????????????========222222=====param1=" + param1
					+ "=====param2=" + param2);
			if (RESULE_SUCESS == param1) {
				// ?????????????????????fs1:/IPC_Snap_Pic/snapPic.jpg
				String imageFilePath = (String) param2;
				if (!TextUtils.isEmpty(imageFilePath)) {
					String path = FileUtils.libToJavaPath(imageFilePath);
					if (!TextUtils.isEmpty(path)) {
						long time = System.currentTimeMillis();
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
						String timename = format.format(new Date(time));

						String dirname = Environment.getExternalStorageDirectory() + File.separator + "goluk"
								+ File.separator + "goluk" + File.separator + "screenshot";
						GFileUtils.makedir(dirname);

						String picName = dirname + File.separator + timename + ".jpg";
						// ??????????????????
						String orgPicName = dirname + File.separator + "original_" + timename + ".jpg";

						GFileUtils.copyFile(path, orgPicName);
						GFileUtils.compressImageToDisk(path, picName);

						File file = new File(picName);
						if (file.exists()) {
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SnapPic======333333333333333====uploadPicture=======path="
											+ path);
						} else {
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SnapPic======??????????????????====333333======11111========");
						}

					} else {
					}
				} else {
				}
			} else {
			}
			break;
		// ?????????????????????????????????
		case IPC_VDCPCmd_TriggerRecord:
			GolukDebugUtils.e("xuhw", "m8sBtn===IPC_VDCPCmd_TriggerRecord===4444=====param1=" + param1 + "==param2="
					+ param2);
			if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
				if (RESULE_SUCESS == param1) {
					if (mWonderfulTime == 30) {
						mHandler.sendEmptyMessage(CLASSIC);
					} else {
						mHandler.sendEmptyMessage(MOUNTS);
					}
				}  else {
					videoTriggerFail();
				}
			} else {
				TriggerRecord record = IpcDataParser.parseTriggerRecordResult((String) param2);
				if (null != record) {
					if (RESULE_SUCESS == param1) {
						mRecordVideFileName = videoname = record.fileName;
						GolukDebugUtils.e("xuhw", "m8sBtn===IPC_VDCPCmd_TriggerRecord===555555========type="
								+ record.type);
						// ????????????
						if (TYPE_SHORTCUT == record.type) {
							if (mWonderfulTime == 30) {
								mHandler.sendEmptyMessage(CLASSIC);
							} else {
								mHandler.sendEmptyMessage(MOUNTS);
							}
						} else {
							mHandler.sendEmptyMessage(EMERGENCY);
						}
					} else {
						videoTriggerFail();
					}
				} else {
					GolukDebugUtils.e("xuhw", "m8sBtn===IPC_VDCPCmd_TriggerRecord===6666====not success====");
					videoTriggerFail();
				}
			}
			break;
		// ???????????????
		case IPC_VDCPCmd_SingleQuery:
			if (RESULE_SUCESS == param1) {
				VideoFileInfo fileInfo = IpcDataParser.parseSingleFileResult((String) param2);
				if (null != fileInfo) {
					if (!TextUtils.isEmpty(fileInfo.location)) {
						Intent mIntent = new Intent("sendfile");
						if (TYPE_SHORTCUT == fileInfo.type) {// ??????
							mIntent.putExtra("filetype", "mounts");
							mIntent.putExtra("filename", fileInfo.location);

							downloadFileNumber++;
							String path = Environment.getExternalStorageDirectory() + File.separator + "goluk"
									+ File.separator + "video" + File.separator + "wonderful";
							wonderfulVideoName = path + File.separator + mRecordVideFileName;

							if (downloadFileNumber <= 1)
								mHandler.sendEmptyMessage(DOWNLOADWONDERFULVIDEO);
						} else if (TYPE_URGENT == fileInfo.type) {// ??????
							mIntent.putExtra("filetype", "emergency");
							mIntent.putExtra("filename", fileInfo.location);
							// sendBroadcast(mIntent);
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SingleQuery===3333=======????????????????????????==============");
						} else {
							// ?????????????????????????????????
							GFileUtils
									.writeIPCLog("===========IPC_VDCPCmd_SingleQuery===44444=======????????????========????????????========");
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
		// ??????IPC???????????????????????????
		case IPC_VDCP_Msg_GetVedioEncodeCfg:
			if (param1 == RESULE_SUCESS) {
				final VideoConfigState videocfg = IpcDataParser.parseVideoConfigState((String) param2);
				if (null != videocfg) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if ("1080P".equals(videocfg.resolution)) {
								mVideoResolutions.setBackgroundResource(R.drawable.icon_hd1080);
							} else {
								mVideoResolutions.setBackgroundResource(R.drawable.icon_hd720);
							}
						}
					});
				} else {
					// ????????????
				}
			}
			break;
		case IPC_VDCP_Msg_PushEvent_ADAS://adas?????????
			GFileUtils.writeIPCLog("============???????????????=======adas????????????========222222=====param1=" + param1
					+ "=====param2=" + param2);
			if (param1 == RESULE_SUCESS) {
				try {
					JSONObject data = new JSONObject(param2.toString());
					String topic = data.optString("topic");
					if(ADAS_LINE_ST_LEFT.equals(topic)){//???????????????
						setAdasStatusImage(true, 1);
						mHandler.sendEmptyMessageDelayed(CLOSE_ADAS_VIEW, ADASTIMER);
					}else if(ADAS_LINE_ST_RIGHT.equals(topic)){//????????????
						setAdasStatusImage(true, 2);
						mHandler.sendEmptyMessageDelayed(CLOSE_ADAS_VIEW, ADASTIMER);
					}else if(ADAS_DISTANCE_ST_LEFT.equals(topic)){
						//????????????
					}else if(ADAS_DISTANCE_ST_RIGHT.equals(topic)){
						//????????????
					}else if(ADAS_TARGET_STATE.equals(topic)){//???????????????
						setAdasStatusImage(true, 3);
						mHandler.sendEmptyMessageDelayed(CLOSE_ADAS_VIEW, ADASTIMER);
					}else if(ADAS_TARGET_DISTANCE.equals(topic)){//??????????????????
						setAdasStatusImage(true, 4);
						mHandler.sendEmptyMessageDelayed(CLOSE_ADAS_VIEW, ADASTIMER);
					}else if(ADAS_TARGET_SPEED.equals(topic)){
						//????????????
					}else if(ADAS_FONT_STARTUP.equals(topic)){
						setAdasStatusImage(true, 5);
						mHandler.sendEmptyMessageDelayed(CLOSE_ADAS_VIEW, ADASTIMER);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			break;
		case IPC_VDCP_Msg_GetVideoTimeConf:
			GolukDebugUtils.e("", "CarRecorderActivity-----------callback_getWonderfulVideoType-----param2: " + param2);
			if (RESULE_SUCESS == param1) {
				if (IPCControlManager.T1_SIGN
						.equals(GolukApplication.getInstance().getIPCControlManager().mProduceName)) {
					WonderfulVideoJson videoJson = GolukFastJsonUtil.getParseObj((String) param2,
							WonderfulVideoJson.class);
					if (null != videoJson && null != videoJson.data) {
						if (videoJson.data.wonder_history_time == 6 && videoJson.data.wonder_future_time == 6) {
							// ??????????????????6???6???
							mWonderfulTime = videoJson.data.wonder_future_time;
						} else if (videoJson.data.wonder_history_time == 0 && videoJson.data.wonder_future_time == 30) {
							// ????????????
							mWonderfulTime = videoJson.data.wonder_future_time;
						}
					}
				} else {
					try {
						JSONObject json = new JSONObject((String) param2);
						int wonder_history_time = json.getInt("wonder_history_time");
						int wonder_future_time = json.getInt("wonder_future_time");
						if (wonder_history_time == 6 && wonder_future_time == 6) {
							// ??????????????????6???6???
							mWonderfulTime = wonder_future_time;
						} else if (wonder_history_time == 0 && wonder_future_time == 30) {
							// ????????????
							mWonderfulTime = wonder_future_time;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			break;
		default:
			break;
		}

	}

	/**
	 * ??????VDTP????????????
	 * 
	 * @param msg
	 *            ?????????id
	 * @param param1
	 *            0:?????????????????? ???0:????????????
	 * @param param2
	 *            ???????????????json?????????
	 * @author xuhw
	 * @date 2015???3???17???
	 */
	private void callBack_VDTP(int msg, int param1, Object param2) {
		switch (msg) {
		// ??????????????????
		case IPC_VDTP_Msg_File:
			if (RESULE_SUCESS == param1) {
				try {
					JSONObject json = new JSONObject((String) param2);
					String filename = json.optString("filename");
					// ?????????????????????????????????UI????????????
					if (filename.indexOf("NRM") >= 0) {
						return;
					}
					if (null != json) {
						String imagename = "";

						imagename = mNowDownloadName.replace("mp4", "jpg");


						if (filename.equals(imagename)) {
							VideoShareInfo vsi = new VideoShareInfo();
							vsi.setName(filename.replace("jpg", "mp4"));
							vsi.setBitmap(ImageManager.getBitmapFromCache(mImagePath + filename, 114, 64));
							new1.setVisibility(View.VISIBLE);
							if (images[0] == null) {
								if (images[1] == null || "".equals(images[1].getName())) {
									images[1] = vsi;
									image2.setImageBitmap(vsi.getBitmap());
									new2.setVisibility(View.VISIBLE);
									image1.setVisibility(View.GONE);
									new1.setVisibility(View.GONE);
								} else {
									images[0] = vsi;
									image1.setVisibility(View.VISIBLE);
									image1.setImageBitmap(vsi.getBitmap());
									new1.setVisibility(View.VISIBLE);
								}

							} else {
								images[1] = images[0];
								image2.setImageBitmap(images[1].getBitmap());
								boolean flog2 = SettingUtils.getInstance().getBoolean("Local_" + images[0].getName(),
										true);
								if (flog2) {
									new2.setVisibility(View.VISIBLE);
								} else {
									new2.setVisibility(View.GONE);
								}
								images[0] = vsi;
								image1.setImageBitmap(vsi.getBitmap());
								new1.setVisibility(View.VISIBLE);
							}

						}
						downloadSize.setVisibility(View.GONE);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (1 == param1) {

				try {
					JSONObject json = new JSONObject((String) param2);
					if (null != json) {
						String filename = json.optString("filename");
						// ?????????????????????????????????????????????????????????UI????????????
						if (filename.indexOf("NRM") >= 0 || filename.indexOf(".jpg") >= 0) {
							return;
						}

						/**
						 * ????????????????????????????????????????????????????????????
						 */
						if (!filename.equals(mNowDownloadName)) {
							this.canvasProcess();
							mNowDownloadName = filename;
							image1.setVisibility(View.VISIBLE);
							image1.setImageResource(R.drawable.album_default_img);//(images[2].getBitmap());
						} else {
							if (image1.getVisibility() != View.VISIBLE) {
								image1.setVisibility(View.VISIBLE);
								image1.setImageBitmap(images[2].getBitmap());
							}
							int filesize = json.getInt("filesize");
							int filerecvsize = json.getInt("filerecvsize");
							int process = (filerecvsize * 100) / filesize;
							downloadSize.setProcess(process);
						}
						// }

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// ?????????
			} else {
				// ????????????
				downloadSize.setVisibility(View.GONE);
				if (images[0] == null) {
					image1.setVisibility(View.INVISIBLE);
				} else {
					image1.setImageBitmap(images[0].getBitmap());
				}
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
				// mShareBtn.setVisibility(View.GONE);
			} else {
				GolukDebugUtils.e("xuhw", "KKKK=================================");
				downloadFinish = false;
				mHandler.removeMessages(DOWNLOADWONDERFULVIDEO);
				mHandler.sendEmptyMessage(DOWNLOADWONDERFULVIDEO);
			}
		}
	};

	/**
	 * ?????????????????????
	 * 
	 * @param bFull
	 *            true:?????????false:??????
	 * @author xuhw
	 * @date 2015???5???12???
	 */
	public void setFullScreen(boolean bFull) {
		if (bFull == m_bIsFullScreen) {
			// GolukUtils.showToast(this, "?????????????????????.");
			return;
		}
		if (bFull) {
			if (!mRtspPlayerView.isPlaying()) {
				return;
			}
			m_vgNormalParent = (ViewGroup) mRtspPlayerView.getParent();
			if (null == m_vgNormalParent) {
				return;
			}
			ViewGroup vgRoot = (ViewGroup) mRtspPlayerView.getRootView(); // ???????????????
			m_vgNormalParent.removeView(mRtspPlayerView);
			mPlayerLayout.addView(mRtspPlayerView);
			RelativeLayout.LayoutParams norParams = new RelativeLayout.LayoutParams((int) (38.66 * density),
					(int) (30 * density));
			norParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			norParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			norParams.setMargins(0, 0, (int) (10 * density), (int) (10 * density));
			mPlayerLayout.addView(mNormalScreen, norParams);
			vgRoot.addView(mPlayerLayout);

			getWindow()
					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else if (m_vgNormalParent != null) {
			ViewGroup vgRoot = (ViewGroup) mRtspPlayerView.getRootView();
			vgRoot.removeView(mPlayerLayout);
			mPlayerLayout.removeView(mRtspPlayerView);
			mPlayerLayout.removeView(mNormalScreen);
			m_vgNormalParent.addView(mRtspPlayerView);

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		m_bIsFullScreen = bFull;
	}

	@Override
	public void onBackPressed() {
		GolukDebugUtils.e("xuhw", "YYYYYY======onBackPressed=====m_bIsFullScreen=" + m_bIsFullScreen);
		if (m_bIsFullScreen) {
			// ????????????????????????
			setFullScreen(false);
		} else {
			super.onBackPressed();
		}
	}

	public void exit() {
		mWonderfulTime = 0;
		finish();
	}

	public void slideview(final float p, final View view) {

		ViewPropertyAnimator animator = view.animate();
		animator.setDuration(200);
		animator.translationXBy(p);
		animator.start();
	}

	// POPWindow????????????
	@Override
	public void callBackPopWindow(int event, Object data) {
		if (LiveSettingPopWindow.EVENT_ENTER == event) {
			if (null != lsp) {
				lsp.close();
			}
			if (null == data) {
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_set_error));
				return;
			}
			mSettingData = (LiveSettingBean) data;
			if (mSettingData.isCanVoice) {
				liveVideo.setBackgroundResource(R.drawable.driving_voice_icon);
			} else {
				liveVideo.setBackgroundResource(R.drawable.driving_voice_off_icon);
			}
			liveTime.setText(GolukUtils.secondToString(mSettingData.duration));
		}
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @Title: initVideoImage
	 * @Description: TODO void
	 * @author ??????
	 * @throws
	 */
	public void initVideoImage() {

		images = new VideoShareInfo[3];

		// String[] filePaths = { "wonderful/wonderful.txt", "urgent/urgent.txt"
		// };
		Bitmap bitmap = ImageManager.getBitmapFromResource(R.drawable.tacitly_pic);

		List<String> wonderfuls = this.getNewVideoByType(1);// ?????????????????????
		List<String> urgents = this.getNewVideoByType(2);// ?????????????????????
		List<String> names = new ArrayList<String>();

		VideoShareInfo defpic = new VideoShareInfo();
		defpic.setBitmap(bitmap);
		defpic.setName("");

		if (wonderfuls != null) {
			names.addAll(wonderfuls);
		}
		if (urgents != null) {
			names.addAll(urgents);
		}

		Collections.sort(names, new SortByDate());

		String videoname1 = "";
		String videoname2 = "";

		if (names != null && names.size() > 0) {
			videoname1 = names.get(0);
			if (names.size() > 1) {
				videoname2 = names.get(1);
			}
		}

		if (!"".equals(videoname1)) {

			Boolean flog = SettingUtils.getInstance().getBoolean("Local_" + videoname1, true);

			String name1 = mImagePath + videoname1.replace("mp4", "jpg");
			File video1 = new File(name1);
			VideoShareInfo vsi1 = new VideoShareInfo();
			if (video1.exists()) {
				vsi1.setBitmap(ImageManager.getBitmapFromCache(name1, 114, 64));
			} else {
				vsi1.setBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.album_default_img));
			}

			vsi1.setName(videoname1);

			if (!"".equals(videoname2)) {

				images[0] = vsi1;
				image1.setImageBitmap(vsi1.getBitmap());
				image1.setVisibility(View.VISIBLE);
				if (flog) {
					new1.setVisibility(View.VISIBLE);
				} else {
					new1.setVisibility(View.GONE);
				}

			} else {
				images[1] = vsi1;
				image1.setVisibility(View.INVISIBLE);
				new1.setVisibility(View.GONE);

				image2.setImageBitmap(vsi1.getBitmap());
				if (flog) {
					new2.setVisibility(View.VISIBLE);
				} else {
					new2.setVisibility(View.GONE);
				}
			}

		} else {
			image1.setVisibility(View.INVISIBLE);
			new1.setVisibility(View.GONE);
		}

		if (!"".equals(videoname2)) {
			String name2 = mImagePath + videoname2.replace("mp4", "jpg");
			File video2 = new File(name2);
			VideoShareInfo vsi2 = new VideoShareInfo();
			vsi2.setName(videoname2);
			if (video2.exists()) {
				vsi2.setBitmap(ImageManager.getBitmapFromCache(name2, 114, 64));
			} else {
				vsi2.setBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.album_default_img));
			}

			images[1] = vsi2;
			image2.setImageBitmap(vsi2.getBitmap());

			boolean flog2 = SettingUtils.getInstance().getBoolean("Local_" + videoname2, true);
			if (flog2) {
				new2.setVisibility(View.VISIBLE);
			} else {
				new2.setVisibility(View.GONE);
			}
		} else {
			if ("".equals(videoname1)) {
				images[1] = defpic;
				image2.setImageBitmap(defpic.getBitmap());
				new2.setVisibility(View.GONE);
			}
		}

		images[2] = defpic;
	}

	public List<String> getNewVideoByType(int type) {
		// String file = mFilePath + uri;
		// List<String> list = this.getVideoConfigFile(file);

		// String videoname1 = "";
		// String videoname2 = "";

		String path = "";
		if (type == 1) {
			path = Environment.getExternalStorageDirectory().getPath() + "/goluk/video/wonderful/";
		} else if (type == 2) {
			path = Environment.getExternalStorageDirectory().getPath() + "/goluk/video/urgent/";
		}
		List<String> list = FileInfoManagerUtils.getFileNames(path, "(.+?mp4)");

		Collections.sort(list, new SortByDate());

		List<String> result = new ArrayList<String>();
		if (list.size() > 0) {
			result.add(list.get(0));
		}
		if (list.size() > 1) {
			result.add(list.get(1));
		}
		return result;

	}

	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 */
	private List<String> getVideoConfigFile(String path) {
		List<String> data = new ArrayList<String>();

		File file = new File(path);
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				String str = br.readLine();
				if (TextUtils.isEmpty(str)) {
					br.close();
					return data;
				}

				String[] files = str.split(this.getResources().getString(R.string.str_comma));

				// ??????
				for (String f : files) {
					if (!data.contains(f)) {
						data.add(f);
					}
				}

				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return data;
		} else {
			return null;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		int id = v.getId();
		if (id == R.id.car_recorder) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				Drawable more_down = this.getResources().getDrawable(R.drawable.driving_car_next_btn1);
				more.setCompoundDrawablesWithIntrinsicBounds(null, null, more_down, null);
				more.setTextColor(Color.rgb(59, 151, 245));
				break;
			case MotionEvent.ACTION_UP:
				Drawable more_up = this.getResources().getDrawable(R.drawable.driving_car_next_btn);
				more.setCompoundDrawablesWithIntrinsicBounds(null, null, more_up, null);
				more.setTextColor(Color.rgb(204, 204, 204));
				break;
			}
		}
		return false;
	}

	/*
	 * @Override public void LocationCallBack(String gpsJson) { BaiduPosition
	 * location = JsonUtil.parseLocatoinJson(gpsJson); if (location == null) {
	 * return; } // ??????????????? LngLat.lng = location.rawLon; LngLat.lat =
	 * location.rawLat;
	 * 
	 * if (mApp.getContext() instanceof CarRecorderActivity) {
	 * GetBaiduAddress.getInstance().searchAddress(location.rawLat,
	 * location.rawLon); } }
	 */

	public void onEventMainThread(EventLocationFinish event) {
		if (null == event) {
			return;
		}

		if (event.getCityCode().equals("-1")  && TextUtils.isEmpty(event.getAddress())) {// ????????????
			if (mLocationAddress.equals("")) {
				mAddr.setText(this.getResources().getString(R.string.str_unknow_street));
			} else {
				mAddr.setText(mLocationAddress);
			}
		} else {// ????????????
			if (event.getAddress() != null && !"".equals(event.getAddress())) {
				mLocationAddress = event.getAddress();
				com.mobnote.util.GolukFileUtils.saveString("loactionAddress", mLocationAddress);
				mAddr.setText(mLocationAddress);
			}
		}

	}
	
	/**
	 * ??????adas??????????????????  ?????? adas???????????????
	 * @param flog
	 * @param status
	 */
	public void setAdasStatusImage(boolean flog,int status){
		if(flog){
			mAdasStatusLayout.setVisibility(View.VISIBLE);
		}else{
			mAdasStatusLayout.setVisibility(View.GONE);
		}
		
		if(status == 1){
			mAdasImg.setImageResource(R.drawable.recorder_carleft_img);//???????????????
		}else if(status == 2){
			mAdasImg.setImageResource(R.drawable.recorder_carright_img);//???????????????
		}else if(status == 3){
			AnimationDrawable photoAnimation;
			mAdasImg.setBackgroundResource(R.anim.adas_warning_animation_front_nearby);//????????????
			photoAnimation = (AnimationDrawable)mAdasImg.getBackground();
			photoAnimation.start();
		}else if(status == 4){
			mAdasImg.setImageResource(R.drawable.recorder_verynear_img);//???????????????
		} else if(status == 5){
			AnimationDrawable photoAnimation;
			mAdasImg.setBackgroundResource(R.anim.adas_warning_animation_front_startup);//????????????
			photoAnimation = (AnimationDrawable)mAdasImg.getBackground();
			photoAnimation.start();
		}
		
	}
	
	
	/**
	 * ??????adas???icon
	 */
	public void setAdasIconState(boolean isT1){
		if(isT1){
			mAdasIcon.setVisibility(View.VISIBLE);
			int  flag = GolukFileUtils.loadInt(GolukFileUtils.ADAS_FLAG, 0);//0 ???   1??????
			if(GolukApplication.getInstance().getIpcIsLogin() && flag == 1){
				mAdasIcon.setImageResource(R.drawable.recorder_adas_on);
			}else{
				mAdasIcon.setImageResource(R.drawable.recorder_adas_off);
			}
		}else{
			mAdasIcon.setVisibility(View.GONE);
		}
		
	}
}
