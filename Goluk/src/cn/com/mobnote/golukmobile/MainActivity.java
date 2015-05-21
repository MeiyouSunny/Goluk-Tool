package cn.com.mobnote.golukmobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.entity.LngLat;
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
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.LocalVideoListAdapter;
import cn.com.mobnote.video.LocalVideoManage;
import cn.com.mobnote.video.OnLineVideoManage;
import cn.com.mobnote.wifibind.WifiConnCallBack;
import cn.com.mobnote.wifibind.WifiConnectManager;
import cn.com.mobnote.wifibind.WifiRsBean;
import cn.com.tiros.utils.LogUtil;
import cn.com.tiros.voiceservice.PlayVoiceRemote;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.rd.car.CarRecorderManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.utils.Log;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:Goluk首页
 * 
 * @author 陈宣宇
 * 
 */

@SuppressLint({ "HandlerLeak", "NewApi" })
public class MainActivity extends BaseActivity implements OnClickListener, WifiConnCallBack, OnTouchListener,
		ILiveDialogManagerFn, ILocationFn, IBaiduGeoCoderFn, UserInterface {

	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 我的位置按钮 */
	private Button mMapLocationBtn = null;
	/** 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	/** 定位相关 */
	private LocationClient mLocClient;

	/** 是否首次定位 */
	private boolean isFirstLoc = true;
	private BaiduMapManage mBaiduMapManage = null;
	/** 控制离开页面不自动请求大头针数据 */
	private boolean isCurrent = true;
	/** 分享按钮 */
	private Button mShareBtn = null;
	/** 分享按钮布局 */
	private RelativeLayout mShareLayout = null;
	/** 关闭分享布局 */
	private ImageButton mCloseShareBtn = null;
	/** 更多按钮 */
	private Button mMoreBtn = null;
	/** 视频广场按钮 */
	private Button msquareBtn = null;
	/** 本地视频按钮 */
	private Button mLocalVideoListBtn = null;
	/** 分享网络直播 */
	private Button mShareLiveBtn = null;
	/** wifi连接状态 */
	private ImageView mWifiState = null;

	/** 登录状态 */
	private Button mLoginStatusBtn = null;
	/** wifi连接状态文本 */
	private Button mWiFiLinkStatus = null;
	private TextView mWifiStateTv = null;
	private RelativeLayout mWifiLayout = null;
	private int mWiFiStatus = 0;
	/** 登录布局 */
	private View mLoginLayout = null;
	/** 登录弹出框 */
	private AlertDialog mLoginDialog = null;

	/** 在线视频管理类 */
	private OnLineVideoManage mOnLineVideoManage = null;
	/** 本地视频管理类 */
	public LocalVideoManage mLocalVideoManage = null;
	/** 本地视频列表数据适配器 */
	public LocalVideoListAdapter mLocalVideoListAdapter = null;

	/** 本地视频无数据显示提示 */
	private RelativeLayout mDefaultTipLayout = null;
	/** wifi列表manage */
	private WifiConnectManager mWac = null;
	/** 定时请求直播点时间 */
	private int mTiming = 1 * 60 * 1000;
	/** 首页handler用来接收消息,更新UI */
	public static Handler mMainHandler = null;
	/** 下载完成播放声音文件 */
	public String mVideoDownloadSoundFile = "ec_alert5.wav";
	/** 下载完成播放音频 */
	public MediaPlayer mMediaPlayer = new MediaPlayer();

	/** 记录登录状态 **/
	public SharedPreferences mPreferencesAuto;
	public boolean isFirstLogin;
	/** 记录行车分享 分享精彩视频为false 点击分享网络直播为true */
	private boolean isClickShareVideo = false;

	private RelativeLayout mRootLayout = null;

	private RelativeLayout indexMapLayout = null;

	private View videoSquareLayout = null;

	/** 未连接 */
	private final int WIFI_STATE_FAILED = 0;
	/** 连接中 */
	private final int WIFI_STATE_CONNING = 1;
	/** 连接 */
	private final int WIFI_STATE_SUCCESS = 2;

	public CustomLoadingDialog mCustomProgressDialog;
	public String shareVideoId;

	/** 看天下按钮 */
	private Button indexLookBtn = null;
	/** 链接行车记录仪 */
	private ImageButton indexCarrecoderBtn = null;
	/** 连接ipc时的动画 */
	Animation anim = null;

	private SharedPreferences mPreferences = null;
	private Editor mEditor = null;
	private long exitTime = 0;

	/** 当前连接的Goluk设备 */
	private String mGolukName = "";

	/** 热门视频列表默认背景图片 */
	private ImageView squareDefault;
	
	SharePlatformUtil sharePlatform;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());

		((GolukApplication) this.getApplication()).initSharedPreUtil(this);

		mRootLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.index, null);

		setContentView(mRootLayout);

		// 添加umeng错误统计
		MobclickAgent.setCatchUncaughtExceptions(true);
		// 添加腾讯崩溃统计
		String appId = "900002451";
		// true代表App处于调试阶段，false代表App发布阶段
		boolean isDebug = false;
		// 初始化SDK
		CrashReport.initCrashReport(this, appId, isDebug);
		CrashReport.setUserId(getIMEI());
		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "Main");
		mApp.initLogic();
		// 页面初始化,获取页面控件
		mApp.startTime = System.currentTimeMillis();
		// 页面初始化,获取页面控件
		init();
		// 初始化地图
		initMap();
		// 初始化视频广场
		initVideoSquare();

		// 初始化连接与綁定状态
		boolean b = this.isBindSucess();
		Log.i("bind", "======bind====status===" + b);
		if (this.isBindSucess()) {
			startWifi();
			// 启动创建热点
			createWiFiHot();
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
		mApp.addLocationListener("main", this);

	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    mVideoSquareActivity.onActivityResult(requestCode, resultCode, data);
	}
	
	private String getIMEI() {
		try {
			String imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
			return imei;
		} catch (Exception e) {

		}
		return "";
	}

	/**
	 * 启动软件创建wifi热点
	 */
	private void createWiFiHot() {
		console.log("自动连接小车本wifi---linkMobnoteWiFi---1");
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiConnectManager(wm, this);
		mWac.autoWifiManage();
	}

	/**
	 * 页面初始化,获取页面元素,注册事件
	 */
	private void init() {
		// 地图我的位置按钮
		mMapLocationBtn = (Button) findViewById(R.id.map_location_btn);
		// 分享按钮
		mShareBtn = (Button) findViewById(R.id.index_share_btn);
		mShareLayout = (RelativeLayout) findViewById(R.id.share_layout);
		mCloseShareBtn = (ImageButton) findViewById(R.id.close_share_btn);

		mMoreBtn = (Button) findViewById(R.id.more_btn);
		msquareBtn = (Button) findViewById(R.id.index_square_btn);
		// mWifiLayout = (RelativeLayout)
		// findViewById(R.id.index_wifi_layout);//临时注释
		mWifiState = (ImageView) findViewById(R.id.index_wiifstate);
		mWifiStateTv = (TextView) findViewById(R.id.wifi_conn_txt);
		videoSquareLayout = findViewById(R.id.video_square_layout);
		// mWifiLayout.setOnClickListener(this);//临时注释

		// 本地视频更多按钮
		mLocalVideoListBtn = (Button) findViewById(R.id.share_local_video_btn);
		mShareLiveBtn = (Button) findViewById(R.id.share_mylive_btn);

		indexLookBtn = (Button) findViewById(R.id.index_look_btn);
		indexCarrecoderBtn = (ImageButton) findViewById(R.id.index_carrecoder_btn);
		squareDefault = (ImageView) findViewById(R.id.square_default);

		mShareLiveBtn.setOnClickListener(this);
		indexLookBtn.setOnClickListener(this);
		indexCarrecoderBtn.setOnClickListener(this);
		// 自动登录时，loading显示
		// mLoginStatusBtn = (Button) findViewById(R.id.login_status_btn);
		// mWiFiLinkStatus = (Button) findViewById(R.id.wifi_link_text);

		// mDefaultTipLayout = (RelativeLayout)
		// findViewById(R.id.defaulttiplayout);
		// mScrollView = (ScrollView)findViewById(R.id.index_scroll);
		// mIndexLayout = (LinearLayout)findViewById(R.id.index_layout);

		// 注册事件
		// mMapMarkeListBtn.setOnClickListener(this);
		mMapLocationBtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mShareBtn.setOnTouchListener(this);
		mCloseShareBtn.setOnClickListener(this);
		// mWifiState.setOnClickListener(this);
		// mIpcWiFiBtn.setOnClickListener(this);
		// mIpcWiFiBtn.setOnTouchListener(this);
		mMoreBtn.setOnClickListener(this);
		mMoreBtn.setOnTouchListener(this);
		msquareBtn.setOnClickListener(this);
		// msquareBtn.setOnTouchListener(this);
		mLocalVideoListBtn.setOnClickListener(this);
		findViewById(R.id.share_mylive_btn).setOnClickListener(this);

		// 更新UI handler
		mMainHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case 1:
					// 视频第一针截取成功,刷新页面UI
					mLocalVideoListAdapter.notifyDataSetChanged();
					break;
				case 2:
					// 5分钟更新一次大头针数据
					mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
							IPageNotifyFn.PageType_GetPinData, "");
					break;
				case 3:
					// 检测是否已连接小车本热点
					// 网络状态改变
					notifyLogicNetWorkState((Boolean) msg.obj);

					break;
				case 99:
					// 测试加点
					// Object obj = new Object();
					// pointDataCallback(1,obj);
					// 请求在线视频轮播数据
					console.log("PageType_GetPinData:");
					mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
							IPageNotifyFn.PageType_GetPinData, "");
					break;
				case 98:
					// 测试,气泡图片下载完成
					Object obj2 = new Object();
					downloadBubbleImageCallBack(1, obj2);
					break;
				}
			}
		};
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
		if (isConnected) {
			mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_CommCmd_RecoveryNetwork, "");
		}
	}

	VideoSquareActivity mVideoSquareActivity;

	private void initVideoSquare() {
		mVideoSquareActivity = new VideoSquareActivity(mRootLayout, this);
	}

	/**
	 * 初始化地图
	 */
	private void initMap() {

		indexMapLayout = (RelativeLayout) findViewById(R.id.index_map_layout);

		BaiduMapOptions options = new BaiduMapOptions();
		options.rotateGesturesEnabled(false); // 不允许手势
		options.overlookingGesturesEnabled(false);
		mMapView = new MapView(this, options);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		indexMapLayout.addView(mMapView, 0, params);

		// 隐藏缩放按钮
		mMapView.showZoomControls(false);
		// 缩放标尺
		mMapView.showScaleControl(false);

		// 获取map对象
		mBaiduMap = mMapView.getMap();
		mBaiduMapManage = new BaiduMapManage(this, mBaiduMap, "Main");

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 地图加载完成事件
		mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				// 地图加载完成,请求大头针数据
				console.log("PageType_GetPinData:地图加载完成,请求大头针数据");
				mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPinData,
						"");
			}
		});

		mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				// 隐藏气泡,大头针
				mBaiduMapManage.mapStatusChange();
				// 移动了地图,第一次不改变地图中心点位置
				isFirstLoc = false;
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
			}

			@Override
			public void onMapStatusChange(MapStatus arg0) {
			}
		});
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
	 * 在线视频基础数据回调
	 * 
	 */
	public void onLineVideoCallBack(Object obj) {
		String videoJson = (String) obj;
		// 更新在线视频
		// mOnLineVideoManage.onLineVideoDataCallback(videoJson);
	}

	/**
	 * 在线视频图片下载
	 * 
	 * @param obj
	 *            ={'vid':'test11','path':'fs1:/Cache/test11.png'}
	 */
	public void onLineVideoImageCallBack(Object obj) {
		// 更新在线视频图片
		String imgJson = (String) obj;
		// mOnLineVideoManage.onLineVideoImgCallback(imgJson);
	}

	/**
	 * 本地视频需要同步目录
	 * 
	 * @param str
	 *            =
	 *            "{\"filepath\":[\"test1111.mp4\",\"test1112.mp4\",\"test1113.mp4\"]}"
	 *            ;
	 */
	public void videoFileCallBack(Object obj) {
		// 接收到本地视频文件目录
		String str = (String) obj;
		// mLocalVideoManage.analyzeVideoFile(str);

		// 隐藏默认提示
		if (null != mDefaultTipLayout) {
			// mDefaultTipLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 本地视频传输中...
	 * 
	 * @param str
	 *            ={"filesize": 32814978, "fileid": 0, "filerecvsize": 903}
	 */
	public void videoDataAnalyze(Object obj) {
		// 本地视频上传中
		// try {
		// String str = (String)obj;
		// JSONObject json = new JSONObject(str);
		// int filesz = json.getInt("filesize");
		// int filerecvSize = json.getInt("filerecvsize");
		//
		// LoadingView view =
		// (LoadingView)findViewById(R.id.video_upload_loading);
		// console.log("wifi---视频同步进度---修改进度view---" + view + "---进度值---" +
		// filerecvSize*100 / filesz);
		// if (view != null)
		// {
		// view.setCurrentProgress(filerecvSize*100 / filesz);
		// }
		//
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * 视频同步完成
	 */
	public void videoAnalyzeComplete(String str) {
		// mLocalVideoManage.videoUploadCallBack();
		// mLocalVideoListAdapter.notifyDataSetChanged();
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
				GFileUtils.writeIPCLog("YYYYYY===@@@@@@==2222==downloadfiletime=" + time);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新视频view
	 */
	public void videoDataUpdate() {
		// mLocalVideoListAdapter.notifyDataSetChanged();
	}

	/**
	 * 链接中断更新页面
	 */
	public void socketLinkOff() {
		// boolean b = mLocalVideoManage.removeVideoListByLinkOff();
		// if(b){
		// mLocalVideoListAdapter.notifyDataSetChanged();
		// }
	}

	/**
	 * 首页大头针数据返回
	 */
	public void pointDataCallback(int success, Object obj) {
		if (1 == success) {
			String str = (String) obj;
			console.log("大头针数据返回---" + str);
			// 记录大头针日志
			// console.print("mapmarker", str);
			// String str =
			// "{\"code\":\"200\",\"state\":\"true\",\"info\":[{\"utype\":\"1\",\"aid\":\"1\",\"nickname\":\"张三\",\"lon\":\"116.357428\",\"lat\":\"39.93923\",\"picurl\":\"http://img2.3lian.com/img2007/18/18/003.png\",\"speed\":\"34公里/小时\"},{\"aid\":\"2\",\"utype\":\"2\",\"nickname\":\"李四\",\"lon\":\"116.327428\",\"lat\":\"39.91923\",\"picurl\":\"http://img.cool80.com/i/png/217/02.png\",\"speed\":\"342公里/小时\"}]}";
			try {
				JSONObject json = new JSONObject(str);
				// 请求成功
				JSONArray list = json.getJSONArray("info");
				mBaiduMapManage.AddMapPoint(list);
			} catch (Exception e) {

			}
		} else {
			console.log("请求大头针数据错误");
		}

		if (isCurrent) {
			// 不管大头针数据请求成功/失败,都需要定时5分钟请求下一次数据
			boolean b = mMainHandler.hasMessages(2);
			if (!b) {
				Message msg = new Message();
				msg.what = 2;
				MainActivity.mMainHandler.sendMessageDelayed(msg, mTiming);
			}
		}
	}

	/**
	 * 下载气泡图片
	 * 
	 * @param url
	 * @param aid
	 */
	@SuppressWarnings("static-access")
	public void downloadBubbleImg(String url, String aid) {
		console.log("下载气泡图片downloadBubbleImg:" + url + ",aid" + aid);
		String json = "{\"purl\":\"" + url + "\",\"aid\":\"" + aid + "\",\"type\":\"1\"}";
		console.log("downloadBubbleImg---json" + json);
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPictureByURL,
				json);
	}

	/**
	 * 下载气泡图片完成
	 * 
	 * @param obj
	 */
	public void downloadBubbleImageCallBack(int success, Object obj) {
		if (1 == success) {
			// 更新在线视频图片
			String imgJson = (String) obj;
			// String imgJson = "{\"path\":\"fs1:/Cache/test11.png\"}";
			console.log("下载气泡图片完成downloadBubbleImageCallBack:" + imgJson);
			mBaiduMapManage.bubbleImageDownload(imgJson);
		} else {
			Toast.makeText(mContext, "气泡图片下载失败", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 链接中断更新页面
	 */
	public void wiFiLinkStatus(int status) {
		Log.e("", "wifiCallBack-------------wiFiLinkStatus:" + status);
		mWiFiStatus = 0;
		switch (status) {
		case 1:
			// 连接中
			mWiFiStatus = WIFI_STATE_CONNING;
			break;
		case 2:
			// 已连接
			mWiFiStatus = WIFI_STATE_SUCCESS;
			wifiConnectedSucess();
			break;
		case 3:
			// 未连接
			mWiFiStatus = WIFI_STATE_FAILED;
			wifiConnectFailed();
			break;
		}
	}

	private static final String WIFI_CONNING_FAILED_STR = "未连接Goluk";
	private static final String WIFI_CONNING_STR = "正在连接Goluk...";
	private static final String WIFI_CONNED_STR = "已连接Goluk";

	private void startWifi() {
		Log.e("", "wifiCallBack-------------startWifi:");
		if (WIFI_STATE_CONNING == mWiFiStatus) {
			return;
		}
		mWiFiStatus = WIFI_STATE_CONNING;
		mWifiStateTv.setText(WIFI_CONNING_STR);

		PlayVoiceRemote.getInstance().setSpeechEngine("test", 1000, 1000, 1000, 1000);

		anim = AnimationUtils.loadAnimation(mContext, R.anim.ipc_action_loading);
		LinearInterpolator lir = new LinearInterpolator();
		anim.setInterpolator(lir);
		indexCarrecoderBtn.startAnimation(anim);

	}

	// 连接成功
	private void wifiConnectedSucess() {
		Log.e("", "wifiCallBack-------------wifiConnectedSucess:");
		mWiFiStatus = WIFI_STATE_SUCCESS;
		if (null != anim) {
			anim.cancel();
			indexCarrecoderBtn.clearAnimation();
			anim = null;
		}
		mWifiStateTv.setText(WIFI_CONNED_STR);
		// mWifiLayout.setBackgroundResource(R.drawable.index_linked);//临时注释
		mWifiState.setBackgroundResource(R.drawable.home_wifi_link_four);
	}

	// 连接失败
	private void wifiConnectFailed() {
		Log.e("", "wifiCallBack-------------wifiConnectFailed:");
		mWiFiStatus = WIFI_STATE_FAILED;
		if (null != anim) {
			anim.cancel();
			indexCarrecoderBtn.clearAnimation();
			anim = null;
		}
		mWifiState.setBackgroundResource(R.drawable.home_wifi_no_link);
		mWifiStateTv.setText(WIFI_CONNING_FAILED_STR);
		// mWifiLayout.setBackgroundResource(R.drawable.index_no_link);
	}

	// 是否綁定过 Goluk
	private boolean isBindSucess() {
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
		// 取得相应的值,如果没有该值,说明还未写入,用false作为默认值
		return preferences.getBoolean("isbind", false);
	}

	private void toLogin() {
		Intent intent = new Intent(this, UserLoginActivity.class);
		intent.putExtra("isInfo", "back");
		mShareLayout.setVisibility(View.GONE);
		mPreferences = getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		mEditor.putString("toRepwd", "mainActivity");
		mEditor.commit();
		startActivity(intent);
	}

	private void click_ConnFailed() {
		if (!isBindSucess()) {
			// 跳转到wifi连接首页
			if (mApp.isUserLoginSucess) {
				Intent wifiIndex = new Intent(MainActivity.this, WiFiLinkIndexActivity.class);
				startActivity(wifiIndex);
			} else {
				toLogin();
			}
		} else {
			// 已经绑定
			startWifi();
			if (null != mWac) {
				mWac.autoWifiManageReset();
			}
		}
	}

	/**
	 * 检测wifi链接状态
	 */
	public void checkWiFiStatus() {
		Log.e("", "wifiCallBack-------------checkWiFiStatus   type:" + mWiFiStatus);
		switch (mWiFiStatus) {
		case WIFI_STATE_FAILED:
			click_ConnFailed();
			break;
		case WIFI_STATE_CONNING:

			break;
		case WIFI_STATE_SUCCESS:
			// if(GolukApplication.getInstance().autodownloadfile){
			// Toast.makeText(MainActivity.this, "正在同步摄像头视频文件，请稍候再试…",
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			GolukApplication.getInstance().stopDownloadList();
			// 跳转到行车记录仪界面
			Intent i = new Intent(MainActivity.this, CarRecorderActivity.class);
			startActivity(i);
			break;
		default:
			break;
		}
	}

	/**
	 * 登录回调
	 * 
	 * @param obj
	 */
	public void loginCallBack(int success, Object obj) {
		console.log("登录回调---loginCallBack---" + success + "---" + obj);
		if (1 == success) {
			try {
				String data = (String) obj;
				Log.i("eee", data);
				JSONObject json = new JSONObject(data);
				// JSONObject userJson = json.getJSONObject("data");
				int code = Integer.valueOf(json.getString("code"));
				String msg = json.getString("msg");
				switch (code) {
				case 200:
					// 登录成功跳转到个人中心页面
					// Intent login = new
					// Intent(MainActivity.this,UserCenterActivity.class);
					// startActivity(login);
					mLoginDialog.hide();
					break;
				/*
				 * default: //登录失败 console.toast("登录失败:"+ msg + code, mContext);
				 * break;
				 */
				}
			} catch (Exception ex) {
			}
		} else {
			// console.toast("登录失败", mContext);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		if (null != mMapView) {
			mMapView.onDestroy();
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

		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		if (null != mMapView) {
			mMapView.onResume();
			mMapView.invalidate();
		}

		if (null != mVideoSquareActivity) {
			mVideoSquareActivity.onDestroy();
		}

		isCurrent = true;
		GetBaiduAddress.getInstance().setCallBackListener(this);

		boolean b = mMainHandler.hasMessages(2);
		if (!b) {
			Message msg = new Message();
			msg.what = 2;
			MainActivity.mMainHandler.sendMessageDelayed(msg, mTiming);
		}

		// 回到页面启动定位
		if (null != mLocClient) {
			mLocClient.start();
		}

		if (mApp.isNeedCheckLive) {
			mApp.isNeedCheckLive = false;
			mApp.isCheckContinuteLiveFinish = true;
			showContinuteLive();
		}

		/*
		 * //回到页面重新检测wifi状态,只有未连接的情况下才重新检测 if(mWiFiStatus == 0){
		 * mApp.VerifyWiFiConnect(); }
		 */
		super.onResume();

	}

	public void showContinuteLive() {
		LogUtil.e(null, "jyf----20150406----showContinuteLive----showContinuteLive :");
		// 标识正常退出
		mApp.mSharedPreUtil.setIsLiveNormalExit(true);
		if (mApp.getIpcIsLogin()) {
			LiveDialogManager.getManagerInstance().showTwoBtnDialog(this, LiveDialogManager.DIALOG_TYPE_LIVE_CONTINUE,
					"提示", "是否继续直播");
		} else {

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		if (null != mMapView) {
			mMapView.onPause();
		}

		if (null != mVideoSquareActivity) {
			mVideoSquareActivity.onDestroy();
		}

		// isCurrent = false;
		// mMainHandler.removeMessages(2);

		// 离开页面停止定位
		if (null != mLocClient) {
			mLocClient.stop();
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
		if (mShareLayout != null && mShareLayout.getVisibility() == View.VISIBLE) {
			mShareLayout.setVisibility(View.GONE);
		} else {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {

				SysApplication.getInstance().exit();
				// }
				mApp.mIPCControlManager.setIPCWifiState(false, "");
				mApp.mGoluk.GolukLogicDestroy();
				if (null != UserStartActivity.mHandler) {
					UserStartActivity.mHandler.sendEmptyMessage(UserStartActivity.EXIT);
				}
				finish();
				int PID = android.os.Process.myPid();
				android.os.Process.killProcess(PID);
				System.exit(0);
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		int action = event.getAction();
		switch (v.getId()) {

		case R.id.more_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				Drawable user_down = this.getResources().getDrawable(R.drawable.home_self_btn_click);
				mMoreBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, user_down, null, null);
				mMoreBtn.setTextColor(Color.rgb(59, 151, 245));
				break;
			case MotionEvent.ACTION_UP:
				Drawable user_up = this.getResources().getDrawable(R.drawable.home_self_btn);
				mMoreBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, user_up, null, null);
				mMoreBtn.setTextColor(Color.rgb(204, 204, 204));
				break;
			}
			break;
		case R.id.share_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				// mDrivingShareText.setTextColor(Color.rgb(0,197,177));
				break;
			case MotionEvent.ACTION_UP:
				// mDrivingShareText.setTextColor(Color.rgb(103,103,103));
				break;
			}
			break;
		case R.id.index_share_btn:
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				Drawable db_down = this.getResources().getDrawable(R.drawable.home_share_btn_click);
				mShareBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, db_down, null, null);
				mShareBtn.setTextColor(Color.rgb(59, 151, 245));
				break;
			case MotionEvent.ACTION_UP:
				Drawable db_up = this.getResources().getDrawable(R.drawable.home_share_btn);
				mShareBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, db_up, null, null);
				mShareBtn.setTextColor(Color.rgb(204, 204, 204));
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
		case R.id.map_location_btn:
			// 回到我的位置
			LatLng ll = new LatLng(LngLat.lat, LngLat.lng);
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);

			break;
		case R.id.index_share_btn:
			click_share();
			break;
		case R.id.close_share_btn:
			// 关闭视频分享
			mShareLayout.setVisibility(View.GONE);
			break;
		case R.id.more_btn:
			// 更多页面
			Intent more = new Intent(MainActivity.this, IndexMoreActivity.class);
			startActivity(more);
			break;
		case R.id.share_local_video_btn:
			// 点击精彩视频
			isClickShareVideo = false;
			click_toLocalVideoShare();
			break;
		case R.id.share_mylive_btn:
			// 点击视频直播
			isClickShareVideo = true;
			toShareLive();
			break;
		case R.id.video_square_more_btn:
			// 跳转到视频广场页面
			Intent videoSquare = new Intent(MainActivity.this, VideoSquareActivity.class);
			startActivity(videoSquare);
			break;
		case R.id.login_status_btn:
			// 登录状态
			// checkLoginStatus();
			break;
		case R.id.wifi_link_text:
			// wifi链接状态
			checkWiFiStatus();
			break;
		case R.id.login_btn:
			// 登录
			// login();
			break;
		case R.id.index_square_btn:
			// 视频广场
			setBelowItem(R.id.index_square_btn);
			break;
		case R.id.index_wiifstate:
			// startWifi();
			break;
		case R.id.index_look_btn:
			setBelowItem(R.id.index_look_btn);
			break;
		case R.id.index_carrecoder_btn:
			checkWiFiStatus();
			break;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void setBelowItem(int id) {
		Drawable drawable;
		if (id == R.id.index_look_btn) {
			if (null != mMapView) {
				mMapView.onResume();
			}
			indexMapLayout.setVisibility(View.VISIBLE);
			videoSquareLayout.setVisibility(View.GONE);
			mVideoSquareActivity.onDestroy();
			drawable = this.getResources().getDrawable(R.drawable.home_local_btn_click);
			indexLookBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
			indexLookBtn.setTextColor(Color.rgb(59, 151, 245));

			drawable = this.getResources().getDrawable(R.drawable.home_find_btn);
			msquareBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
			msquareBtn.setTextColor(Color.rgb(103, 103, 103));
		} else if (id == R.id.index_square_btn) {
			if (null != mMapView) {
				mMapView.onPause();
			}
			indexMapLayout.setVisibility(View.GONE);
			videoSquareLayout.setVisibility(View.VISIBLE);
			mVideoSquareActivity.onResume();
			drawable = this.getResources().getDrawable(R.drawable.home_local_btn);
			indexLookBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
			indexLookBtn.setTextColor(Color.rgb(103, 103, 103));

			drawable = this.getResources().getDrawable(R.drawable.home_find_btn_click);
			msquareBtn.setTextColor(Color.rgb(59, 151, 245));
			msquareBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
		}
	}

	private void click_share() {
		LogUtil.e("lily", "----------click------");
		if (!mApp.isUserLoginSucess) {
			mShareLayout.setVisibility(View.GONE);
			mApp.mUser.setUserInterface(this);
			if (mApp.autoLoginStatus == 1) {
				mBuilder = new AlertDialog.Builder(mContext);
				dialog = mBuilder.setMessage("正在为您登录，请稍候……").setCancelable(false).setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							return true;
						}
						return false;
					}
				}).create();
				dialog.show();
				return;
			}
			toLogin();
			return;
		}
		// 视频分享
		mShareLayout.setVisibility(View.VISIBLE);
	}

	private void toCard() {
		Intent i = new Intent(MainActivity.this, CarRecorderActivity.class);
		startActivity(i);
	}

	private void toSqu() {
		Intent more = new Intent(MainActivity.this, VideoSquareActivity.class);
		startActivity(more);
	}

	private Builder mBuilder = null;
	private AlertDialog dialog = null;

	private void click_toLocalVideoShare() {
		Log.i("lily", "-------isUserLoginSuccess------" + mApp.isUserLoginSucess + "------autologinStatus-----"
				+ mApp.autoLoginStatus);
		if (!mApp.isUserLoginSucess) {
			// TODO 未登录成功
			mShareLayout.setVisibility(View.GONE);
			mApp.mUser.setUserInterface(this);
			if (mApp.autoLoginStatus == 1) {
				mBuilder = new AlertDialog.Builder(mContext);
				dialog = mBuilder.setMessage("正在为您登录，请稍候……").setCancelable(false).setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							return true;
						}
						return false;
					}
				}).create();
				dialog.show();
				return;
			} else if (mApp.autoLoginStatus == 3 || mApp.autoLoginStatus == 4) {
				// console.toast("网络连接异常，请重试", mContext);
				return;
			}
			Intent intent = new Intent(this, UserLoginActivity.class);
			intent.putExtra("isInfo", "back");
			startActivity(intent);
			return;
		}

		// 跳转到本地视频分享列表
		Intent localVideoShareList = new Intent(MainActivity.this, LocalVideoShareListActivity.class);
		startActivity(localVideoShareList);
		// 关闭视频分享
		mShareLayout.setVisibility(View.GONE);
	}

	/**
	 * 发起主动直播
	 * 
	 * @author jiayf
	 * @date Apr 2, 2015
	 */
	private void toShareLive() {
		if (!mApp.isUserLoginSucess) {
			// 未登录成功
			mShareLayout.setVisibility(View.GONE);
			mApp.mUser.setUserInterface(this);
			if (mApp.autoLoginStatus == 1) {
				mBuilder = new AlertDialog.Builder(mContext);
				dialog = mBuilder.setMessage("正在为您登录，请稍候……").setCancelable(false).setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							return true;
						}
						return false;
					}
				}).create();
				dialog.show();
				return;
			} else if (mApp.autoLoginStatus == 3 || mApp.autoLoginStatus == 4) {
				// console.toast("网络连接异常，请重试", mContext);
				return;
			}
			Intent intent = new Intent(this, UserLoginActivity.class);
			intent.putExtra("isInfo", "back");
			startActivity(intent);
			return;
		}

		if (!mApp.getIpcIsLogin()) {
			// Toast.makeText(this, "IPC未登录", Toast.LENGTH_SHORT).show();
			LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_IPC_LOGINOUT, "提示", "请先连接摄像头");
			return;
		}

		// if(GolukApplication.getInstance().autodownloadfile){
		// Toast.makeText(MainActivity.this, "正在同步摄像头视频文件，请稍候再试…",
		// Toast.LENGTH_SHORT).show();
		// return;
		// }

		GolukApplication.getInstance().stopDownloadList();
		// 开启直播
		Intent intent = new Intent(this, LiveActivity.class);
		intent.putExtra(LiveActivity.KEY_IS_LIVE, true);
		intent.putExtra(LiveActivity.KEY_GROUPID, "");
		intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
		intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
		startActivity(intent);
		mShareLayout.setVisibility(View.GONE);

	}

	// 查看他人的直播
	public void startLiveLook(UserInfo userInfo) {
		LogUtil.e(null, "jyf-----click------666666");
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
		LogUtil.e(null, "jyf----20150406----MainActivity----startLiveLook");
	}

	public void dismissAutoDialog() {
		if (null != dialog) {
			dialog.dismiss();
			dialog = null;
		}
	}

	@Override
	public void statusChange() {

		if (mApp.autoLoginStatus != 1) {
			dismissAutoDialog();
			if (mApp.autoLoginStatus == 2) {
				mShareLayout.setVisibility(View.VISIBLE);
			}
		}

	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (dialogType == LiveDialogManager.DIALOG_TYPE_LOGIN) {
			if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
				mShareLayout.setVisibility(View.GONE);
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
	public void LocationCallBack(String gpsJson) {
		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);
		if (location == null || mMapView == null) {
			return;
		}
		// 此处设置开发者获取到的方向信息，顺时针0-360
		MyLocationData locData = new MyLocationData.Builder().accuracy((float) location.radius).direction(100)
				.latitude(location.rawLat).longitude(location.rawLon).build();
		// 确认地图我的位置点是否更新位置
		mBaiduMap.setMyLocationData(locData);

		// 移动了地图,第一次不改变地图中心点位置
		if (isFirstLoc) {
			isFirstLoc = false;
			// 移动地图中心点
			LatLng ll = new LatLng(location.rawLat, location.rawLon);
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
		}

		// 保存经纬度
		LngLat.lng = location.rawLon;
		LngLat.lat = location.rawLat;

		if (!(mApp.getContext() instanceof LiveActivity)) {
			GetBaiduAddress.getInstance().searchAddress(location.rawLat, location.rawLon);
		}
	}

	@Override
	public void CallBack_BaiduGeoCoder(int function, Object obj) {
		if (null == obj) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----CallBack_BaiduGeoCoder----获取反地理编码  : " + (String) obj);
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

	@Override
	public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
		Log.e("", "wifiCallBack-------------type:" + type + "	state :" + state + "	process:" + process);
		switch (type) {
		case 5:
			if (state == 0) {
				switch (process) {
				case 0:
					// 创建热点成功
					// hotWiFiCreateSuccess();
					break;
				case 1:
					// ipc成功连接上热点
					WifiRsBean[] bean = (WifiRsBean[]) arrays;
					if (null != bean) {
						console.log("自动wifi链接IPC连接上WIFI热点回调---length---" + bean.length);
						if (bean.length > 0) {
							console.log("通知logic连接ipc---sendLogicLinkIpc---1---ip---");
							mGolukName = bean[0].getIpc_ssid();
							sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
						}
					}
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

			break;
		}
	}

	/**
	 * 通知logic连接ipc
	 */
	private void sendLogicLinkIpc(String ip, String ipcmac) {
		// 连接ipc热点wifi---调用ipc接口
		console.log("通知logic连接ipc---sendLogicLinkIpc---1---ip---" + ip);
		mApp.mIpcIp = ip;
		boolean b = mApp.mIPCControlManager.setIPCWifiState(true, ip);
		console.log("通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		if (!isSucess) {
			 Toast.makeText(this, "分享失败",Toast.LENGTH_SHORT).show();
			return;
		}
		System.out.println("shareid-----" + shareVideoId + "   channel-----" + channel);
		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, shareVideoId);
	}

}
