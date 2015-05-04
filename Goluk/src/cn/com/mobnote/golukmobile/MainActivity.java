package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
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

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.entity.LngLat;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.user.UserInterface;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.LocalVideoListAdapter;
import cn.com.mobnote.video.LocalVideoManage;
import cn.com.mobnote.video.LocalVideoManage.LocalVideoData;
import cn.com.mobnote.video.OnLineVideoManage;
import cn.com.mobnote.view.MyGridView;
import cn.com.mobnote.wifi.WiFiConnection;
import cn.com.mobnote.wifi.WifiAutoConnectManager;
import cn.com.mobnote.wifi.WifiConnCallBack;
import cn.com.mobnote.wifi.WifiRsBean;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress;
import cn.com.mobnote.golukmobile.live.GetBaiduAddress.IBaiduGeoCoderFn;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.utils.LogUtil;

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

@SuppressLint("HandlerLeak")

public class MainActivity extends Activity implements OnClickListener , WifiConnCallBack, OnTouchListener, ILiveDialogManagerFn, 
			ILocationFn, IBaiduGeoCoderFn ,UserInterface{

	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	
	/** 地图layout */
	private LinearLayout mMapLayout = null;
	/** 我的位置按钮 */
	private Button mMapLocationBtn = null;
	/** 直播marker列表按钮 */
//	private Button mMapMarkeListBtn = null;
	/** 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	/** 定位相关 */
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	/** 是否首次定位 */
	private boolean isFirstLoc = true;
	private BaiduMapManage mBaiduMapManage = null;
	/** 控制离开页面不自动请求大头针数据 */
	private boolean isCurrent = true;
	/** 分享按钮 */
	private ImageButton mShareBtn = null;
	/** 分享按钮文字 */
	private TextView mDrivingShareText = null;
	/** 分享按钮布局 */
	private RelativeLayout mShareLayout = null;
	/** 关闭分享布局 */
	private ImageButton mCloseShareBtn = null;
	/** ipc-wifi状态按钮 */
	private Button mIpcWiFiBtn = null;
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
	/** 登录手机号 */
	private EditText mLoginPhoneText = null;
	/** 登录密码 */
	private EditText mLoginPwdText = null;
	/** 登录按钮 */
	private Button mLoginBtn = null;
	
	/** 视频广场更多按钮 */
	private Button mVideoSquareMoreBtn = null;
	/** 在线视频管理类 */
	private OnLineVideoManage mOnLineVideoManage = null;
	
	/** 本地视频列表layout */
	private LinearLayout mLocalVideoListLayout = null;
	/** 本地视频管理类 */
	public LocalVideoManage mLocalVideoManage = null;
	/** 本地视频列表数据适配器 */
	public LocalVideoListAdapter mLocalVideoListAdapter = null;
	/** 本地视频列表数据 */
	private ArrayList<LocalVideoData> mLocalVideoData = null;
	/** 本地视频列表 */
	private MyGridView mLocalVideoGridView = null;
	
	/** 本地视频无数据显示提示 */
	private RelativeLayout mDefaultTipLayout = null;
	private WifiAutoConnectManager mWac = null;
	/** 定时请求直播点时间 */
	private int mTiming = 1 * 60 * 1000;
	/** 首页handler用来接收消息,更新UI*/
	public static Handler mMainHandler = null;
	/** 下载完成播放声音文件 */
	public String mVideoDownloadSoundFile = "ec_alert5.wav";
	/** 下载完成播放音频 */
	public MediaPlayer mMediaPlayer = new MediaPlayer();
	
	/**记录登录状态**/
	public SharedPreferences mPreferencesAuto;
	public boolean isFirstLogin;
	/**记录行车分享   分享精彩视频为false  点击分享网络直播为true*/
	private boolean isClickShareVideo = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext
		//注意该方法要再setContentView方法之前实现  
		SDKInitializer.initialize(getApplicationContext());
		
		((GolukApplication)this.getApplication()).initSharedPreUtil(this);
		
		setContentView(R.layout.index);
		
		//添加umeng错误统计
		MobclickAgent.setCatchUncaughtExceptions(true);
		//添加腾讯崩溃统计
		String appId = "900002451";
		//true代表App处于调试阶段，false代表App发布阶段
		boolean isDebug = true;
		//初始化SDK
		CrashReport.initCrashReport(this,appId ,isDebug);
		
		mContext = this;
		SysApplication.getInstance().addActivity(this);
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(this,"Main");
		
		mApp.initLogic();
		
		//mApp.mGoluk.GoLuk_WifiStateChanged(true);
		
		//页面初始化,获取页面控件
		init();
		//初始化地图
		initMap();
		//加载在线视频轮播
		//initViewPager();
		//加载本地视屏列表
		//initLocalVideoList();
		
		//连接小车本wifi
		//linkMobnoteWiFi();
		
		mApp.VerifyWiFiConnect();
		
		
//		Button ipc = (Button)findViewById(R.id.mIPCBtn);
//		ipc.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				Intent i = new Intent(MainActivity.this, CarRecorderActivity.class);
//				startActivity(i);
//			}
//		});
		
		

		//不是第一次登录，并且上次登录成功过，进行自动登录
		mPreferencesAuto = getSharedPreferences("firstLogin", MODE_PRIVATE);
		isFirstLogin = mPreferencesAuto.getBoolean("FirstLogin", true);
		if(!isFirstLogin && !mApp.isUserLoginSucess){
			mApp.mUser.initAutoLogin();
		}

		GetBaiduAddress.getInstance().setCallBackListener(this);
	
	}
	
	/**
	 * 页面初始化,获取页面元素,注册事件
	 */
	private void init(){
		mLayoutInflater = LayoutInflater.from(mContext);
//		mMapMarkeListBtn = (Button)findViewById(R.id.map_marke_list_btn);
		//地图我的位置按钮
		mMapLocationBtn = (Button) findViewById(R.id.map_location_btn);
		//分享按钮
		mShareBtn = (ImageButton) findViewById(R.id.share_btn);
		mDrivingShareText = (TextView) findViewById(R.id.driving_share_text);
		mShareLayout = (RelativeLayout) findViewById(R.id.share_layout);
		mCloseShareBtn = (ImageButton) findViewById(R.id.close_share_btn);
		
//		mIpcWiFiBtn = (Button) findViewById(R.id.wifi_status_btn);
		mMoreBtn = (Button) findViewById(R.id.more_btn);
		msquareBtn = (Button) findViewById(R.id.index_square_btn);
		mWifiLayout = (RelativeLayout) findViewById(R.id.index_wifi_layout);
		mWifiState = (ImageView) findViewById(R.id.index_wiifstate);
		mWifiStateTv = (TextView) findViewById(R.id.wifi_conn_txt);
		
		mWifiLayout.setOnClickListener(this);
		
		//本地视频更多按钮
		mLocalVideoListBtn = (Button)findViewById(R.id.share_local_video_btn);
		mShareLiveBtn = (Button) findViewById(R.id.share_mylive_btn);
		mShareLiveBtn.setOnClickListener(this);
		
		//自动登录时，loading显示
//		mLoginStatusBtn = (Button) findViewById(R.id.login_status_btn);
//		mWiFiLinkStatus = (Button) findViewById(R.id.wifi_link_text);

//		mDefaultTipLayout = (RelativeLayout) findViewById(R.id.defaulttiplayout);
//		mScrollView = (ScrollView)findViewById(R.id.index_scroll);
//		mIndexLayout = (LinearLayout)findViewById(R.id.index_layout);
		
		//注册事件
//		mMapMarkeListBtn.setOnClickListener(this);
		mMapLocationBtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mShareBtn.setOnTouchListener(this);
		mCloseShareBtn.setOnClickListener(this);
//		mWifiState.setOnClickListener(this);
//		mIpcWiFiBtn.setOnClickListener(this);
//		mIpcWiFiBtn.setOnTouchListener(this);
		mMoreBtn.setOnClickListener(this);
		mMoreBtn.setOnTouchListener(this);
		msquareBtn.setOnClickListener(this);
		msquareBtn.setOnTouchListener(this);
		mLocalVideoListBtn.setOnClickListener(this);
		findViewById(R.id.share_mylive_btn).setOnClickListener(this);
		
		//更新UI handler
		mMainHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 1:
						//视频第一针截取成功,刷新页面UI
						mLocalVideoListAdapter.notifyDataSetChanged();
					break;
					case 2:
						//5分钟更新一次大头针数据
						mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPinData,"");
					break;
					case 3:
						//检测是否已连接小车本热点
						checkLinkWiFi();
						//网络状态改变
						mApp.VerifyWiFiConnect();

						/*android.util.Log.i("setauto","------自动登录网络状态变化1111------");
						if(mApp.isUserLoginSucess == true || mApp.autoLoginStatus !=2){
							android.util.Log.i("setauto","------自动登录网络状态变化2222------");
							mApp.mUser.initAutoLogin();							
						}*/

						notifyLogicNetWorkState((Boolean) msg.obj);

					break;
					
					
					case 99:
						//测试加点
						//Object obj = new Object();
						//pointDataCallback(1,obj);
						//请求在线视频轮播数据
						console.log("PageType_GetPinData:");
						mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPinData,"");
					break;
					case 98:
						//测试,气泡图片下载完成
						Object obj2 = new Object();
						downloadBubbleImageCallBack(1,obj2);
					break;
				}
			}
		};
		
		//Message msg = new Message();
		//msg.what = 99;
		//MainActivity.mMainHandler.sendMessageDelayed(msg,5000);
	}
	
	
	/**
	 * 通知Logic，网络恢复
	 * 
	 * @param isConnected true/false 网络恢复/不可用
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
	
	/**
	 * 初始化地图
	 */
	private void initMap(){
		mMapLayout = (LinearLayout) findViewById(R.id.map_layout);
		//获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		
		//隐藏缩放按钮
		mMapView.showZoomControls(false);
		//缩放标尺
		mMapView.showScaleControl(false);
		
		//获取map对象
		mBaiduMap = mMapView.getMap();
		mBaiduMapManage = new BaiduMapManage(this,mBaiduMap,"Main");
		
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		// 设置定位模式,没有设置定位模式接口setLocationMode
		// 打开gps
		option.setOpenGps(true);
		option.setIsNeedAddress(true);
		// 设置坐标类型
		// 返回国测局经纬度坐标系 coor=gcj02
		// 返回百度墨卡托坐标系 coor=bd09
		// 返回百度经纬度坐标系 coor=bd09ll
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		//地图加载完成事件
		mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				//地图加载完成,请求大头针数据
				console.log("PageType_GetPinData:地图加载完成,请求大头针数据");
				mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPinData, "");
			}
		});
		
		mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				//console.log("onMapStatusChangeStart");
				//隐藏气泡,大头针
				mBaiduMapManage.mapStatusChange();
				//移动了地图,第一次不改变地图中心点位置
				isFirstLoc = false;
			}
			
			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
				//console.log("onMapStatusChangeFinish");
			}
			
			@Override
			public void onMapStatusChange(MapStatus arg0) {
				//console.log("onMapStatusChange");
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
	 * 创建wifi回调广播
	 * @param wac
	 */
	private void createReceiver(WifiAutoConnectManager wac){
		String  action = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
		IntentFilter filter = new IntentFilter();
		filter.addAction(action);
		registerReceiver(wac, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}
	
	/**
	 * 自动连接小车本wifi
	 */
	private void linkMobnoteWiFi(){
		console.log("自动连接小车本wifi---linkMobnoteWiFi---1");
		WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mWac = new WifiAutoConnectManager(wm,this);
		createReceiver(mWac);
		//连接wifi
		mWac.connect();
	}
	
	/**
	 * 判断已连接的wifi是否是小车本热点
	 */
	private void checkLinkWiFi(){
		WifiManager mWifiManage = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WiFiConnection connection = new WiFiConnection(mWifiManage,mContext);
		WifiInfo info = connection.getWiFiInfo();
		WifiAutoConnectManager wac = new WifiAutoConnectManager(mWifiManage,this);
		boolean b = wac.getEffectiveWifi(info);
		if(b){
			String wifiName = info.getSSID();
			//保存wifi校验名称
			WiFiConnection.SaveWiFiName(wifiName);
		}
	}
	
	/**
	 * 在线视频基础数据回调
	 * @param obj={"code":"200","json":[{"vid":"test11","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test11.mp4","purl":"http://img2.3lian.com/img2007/18/18/003.png","desc":"陈真暴揍小日本","comment":"215","ilike":"123"},{"vid":"test12","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test12.mp4","purl":"http://img.cool80.com/i/png/217/02.png","desc":"轮椅女孩环游世界","comment":"17","ilike":"111"},{"vid":"test13","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test13.mp4","purl":"http://img2.3lian.com/img2007/14/03/20080405141042281.png","desc":"万年不毕业小学生，每次出现引发各种血案","comment":"207","ilike":"90"}]}
	 */
	public void onLineVideoCallBack(Object obj){
		String videoJson = (String)obj;
		//更新在线视频
		//mOnLineVideoManage.onLineVideoDataCallback(videoJson);
	}
	
	/**
	 * 在线视频图片下载
	 * @param obj={'vid':'test11','path':'fs1:/Cache/test11.png'}
	 */
	public void onLineVideoImageCallBack(Object obj){
		//更新在线视频图片
		String imgJson = (String)obj;
		//mOnLineVideoManage.onLineVideoImgCallback(imgJson);
	}
	
	
	/**
	 * 本地视频需要同步目录
	 * @param str="{\"filepath\":[\"test1111.mp4\",\"test1112.mp4\",\"test1113.mp4\"]}";
	 */
	public void videoFileCallBack(Object obj){
		//接收到本地视频文件目录
		String str = (String)obj;
		//mLocalVideoManage.analyzeVideoFile(str);
		
		//隐藏默认提示
		if(null != mDefaultTipLayout){
			//mDefaultTipLayout.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 本地视频传输中...
	 * @param str={"filesize": 32814978, "fileid": 0, "filerecvsize": 903}
	 */
	public void videoDataAnalyze(Object obj){
		//本地视频上传中
//		try {
//			String str =  (String)obj;
//			JSONObject json = new JSONObject(str);
//			int filesz = json.getInt("filesize");
//			int filerecvSize = json.getInt("filerecvsize");
//			
//			LoadingView view = (LoadingView)findViewById(R.id.video_upload_loading);
//			console.log("wifi---视频同步进度---修改进度view---" + view + "---进度值---" + filerecvSize*100 / filesz);
//			if (view != null)
//			{
//				view.setCurrentProgress(filerecvSize*100 / filesz);
//			}
//			
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * 视频同步完成
	 */
	public void videoAnalyzeComplete(String str){
//		mLocalVideoManage.videoUploadCallBack();
//		mLocalVideoListAdapter.notifyDataSetChanged();
		try {
			JSONObject json = new JSONObject(str);
			String tag = json.getString("tag");
			if(tag.equals("videodownload")){
				//只有视频下载才提示音频
				playDownLoadedSound();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新视频view
	 */
	public void videoDataUpdate(){
//		mLocalVideoListAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 链接中断更新页面
	 */
	public void socketLinkOff(){
//		boolean b = mLocalVideoManage.removeVideoListByLinkOff();
//		if(b){
//			mLocalVideoListAdapter.notifyDataSetChanged();
//		}
	}
	
	/**
	 * 首页大头针数据返回
	 */
	public void pointDataCallback(int success,Object obj){
		if(1 == success){
			String str = (String)obj;
			console.log("大头针数据返回---" + str);
			//记录大头针日志
			//console.print("mapmarker", str);
			//String str = "{\"code\":\"200\",\"state\":\"true\",\"info\":[{\"utype\":\"1\",\"aid\":\"1\",\"nickname\":\"张三\",\"lon\":\"116.357428\",\"lat\":\"39.93923\",\"picurl\":\"http://img2.3lian.com/img2007/18/18/003.png\",\"speed\":\"34公里/小时\"},{\"aid\":\"2\",\"utype\":\"2\",\"nickname\":\"李四\",\"lon\":\"116.327428\",\"lat\":\"39.91923\",\"picurl\":\"http://img.cool80.com/i/png/217/02.png\",\"speed\":\"342公里/小时\"}]}";
			try {
				JSONObject json = new JSONObject(str);
				//请求成功
				JSONArray list = json.getJSONArray("info");
				mBaiduMapManage.AddMapPoint(list);
			}
			catch(Exception e){
				
			}
		}
		else{
			console.log("请求大头针数据错误");
		}
		
		if(isCurrent){
			//不管大头针数据请求成功/失败,都需要定时5分钟请求下一次数据
			boolean b = mMainHandler.hasMessages(2);
			if(!b){
				Message msg = new Message();
				msg.what = 2;
				MainActivity.mMainHandler.sendMessageDelayed(msg,mTiming);
			}
		}
	}
	
	/**
	 * 下载气泡图片
	 * @param url
	 * @param aid
	 */
	@SuppressWarnings("static-access")
	public void downloadBubbleImg(String url,String aid){
		console.log("下载气泡图片downloadBubbleImg:" + url + ",aid" + aid);
		String json = "{\"purl\":\"" + url + "\",\"aid\":\"" + aid + "\",\"type\":\"1\"}";
		console.log("downloadBubbleImg---json" + json);
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPictureByURL, json);
	}
	
	/**
	 * 下载气泡图片完成
	 * @param obj
	 */
	public void downloadBubbleImageCallBack(int success,Object obj){
		if(1 == success){
			//更新在线视频图片
			String imgJson = (String)obj;
			//String imgJson = "{\"path\":\"fs1:/Cache/test11.png\"}";
			console.log("下载气泡图片完成downloadBubbleImageCallBack:" +imgJson);
			mBaiduMapManage.bubbleImageDownload(imgJson);
		}
		else{
			Toast.makeText(mContext,"气泡图片下载失败",Toast.LENGTH_SHORT).show();
		}
	}
	
	/** 连接中 */
	private final int WIFI_STATE_CONNING = 1;
	/** 连接*/
	private final int WIFI_STATE_SUCCESS = 2;
	/** 未连接 */
	private final int WIFI_STATE_FAILED = 3;
	
	/**
	 * 链接中断更新页面
	 */
	public void wiFiLinkStatus(int status){
		Drawable img = null;
		Resources res = getResources();
		mWiFiStatus = 0;
		switch(status){
			case 1:
				//连接中
				mWiFiStatus = 1;
			break;
			case 2:
				//已连接
//				mIpcWiFiBtn.setText("已连接");
//				mIpcWiFiBtn.setTextColor(Color.rgb(0,197,177));
//				img = res.getDrawable(R.drawable.index_icon_xingche_connect);
				wifiConnectedSucess();
				mWiFiStatus = 2;
			break;
			case 3:
				//未连接
//				mIpcWiFiBtn.setText("未连接");
//				mIpcWiFiBtn.setTextColor(Color.rgb(103,103,103));
//				img = res.getDrawable(R.drawable.index_icon_xingche_btn);
				mWiFiStatus = 0;
				wifiConnectFailed();
			break;
		}
		//调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
//		img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
		//mIpcWiFiBtn.setCompoundDrawables(null, img, null, null);
	}
	
	/** 音量图片动画 */
	private AnimationDrawable mVolumeImgAnimation = null;
	private void startWifi() {
		mWifiState.setBackgroundResource(R.anim.anim_wifi);
		this.mVolumeImgAnimation = (AnimationDrawable) this.mWifiState.getBackground();
		this.mVolumeImgAnimation.start();
	}
	
	private void wifiConnectedSucess() {
		if (null != mVolumeImgAnimation) {
			mVolumeImgAnimation.stop();
			mVolumeImgAnimation = null;
		}
		mWifiStateTv.setText("已连接");
		mWifiLayout.setBackgroundResource(R.drawable.index_linked);
		mWifiState.setBackgroundResource(R.drawable.index_wifi_four);
	}
	
	private void wifiConnectFailed() {
		if (null != mVolumeImgAnimation) {
			mVolumeImgAnimation.stop();
			mVolumeImgAnimation = null;
		}
		mWifiState.setBackgroundResource(R.drawable.index_wifi_five);
		mWifiStateTv.setText("未连接");
		mWifiLayout.setBackgroundResource(R.drawable.index_no_link);
		mWifiState.setBackgroundResource(R.drawable.index_wifi_four);
	}
	
	
	/**
	 * 登录回调
	 * @param obj
	 */
	public void loginCallBack(int success,Object obj){
		console.log("登录回调---loginCallBack---" + success + "---" + obj);
		if(1 == success){
			try{
				String data = (String)obj;
				Log.i("eee", data);
				JSONObject json = new JSONObject(data);
				//JSONObject userJson = json.getJSONObject("data");
				int code = Integer.valueOf(json.getString("code"));
				String msg = json.getString("msg");
				switch(code){
					case 200:
						//登录成功跳转到个人中心页面
//						Intent login = new Intent(MainActivity.this,UserCenterActivity.class);
//						startActivity(login);
						mLoginDialog.hide();
					break;
					/*default:
						//登录失败
						console.toast("登录失败:"+ msg + code, mContext);
					break;*/
				}
			}
			catch(Exception ex){}
		}
		else{
//			console.toast("登录失败", mContext);
		}
	}
	
	/**
	 * 检测wifi链接状态
	 */
	public void checkWiFiStatus(){
		if(mWiFiStatus == 0){
			//wifi未链接
			//跳转到wifi连接首页
			Intent wifiIndex = new Intent(MainActivity.this,WiFiLinkIndexActivity.class);
			startActivity(wifiIndex);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		if(null != mMapView){
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
		mApp.setContext(this,"Main");
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
		
		//在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		if(null != mMapView){
			mMapView.onResume();
		}
		isCurrent = true;
		
		boolean b = mMainHandler.hasMessages(2);
		if(!b){
			Message msg = new Message();
			msg.what = 2;
			MainActivity.mMainHandler.sendMessageDelayed(msg,mTiming);
		}
		
		//回到页面启动定位
		if(null != mLocClient){
			mLocClient.start();
		}
		
		if (mApp.isNeedCheckLive) {
			mApp.isNeedCheckLive = false;
			showContinuteLive();
		}
		
		/*
		//回到页面重新检测wifi状态,只有未连接的情况下才重新检测
		if(mWiFiStatus == 0){
			mApp.VerifyWiFiConnect();
		}
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
		//在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		if(null != mMapView){
			mMapView.onPause();
		}
//		isCurrent = false;
//		mMainHandler.removeMessages(2);
		
		//离开页面停止定位
		if(null != mLocClient){
			mLocClient.stop();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK )
		{
			//退出对话框
//			int PID = android.os.Process.myPid();
//			android.os.Process.killProcess(PID);
//			android.os.Process.sendSignal(PID, 9);
			if(mApp.isUserLoginSucess){
				SysApplication.getInstance().exit();
			}
			finish();
		}
		return false;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		switch (v.getId()) {
//			case R.id.wifi_status_btn:
//				//如果已连接,不改变文字颜色
//				if(mWiFiStatus != 2){
//					switch (action) {
//						case MotionEvent.ACTION_DOWN:
//							mIpcWiFiBtn.setTextColor(Color.rgb(0,197,177));
//						break;
//						case MotionEvent.ACTION_UP:
//							mIpcWiFiBtn.setTextColor(Color.rgb(103,103,103));
//						break;
//					}
//				}
//			break;
			case R.id.more_btn:
				switch (action) {
					case MotionEvent.ACTION_DOWN:
						mMoreBtn.setTextColor(Color.rgb(0,197,177));
					break;
					case MotionEvent.ACTION_UP:
						mMoreBtn.setTextColor(Color.rgb(103,103,103));
					break;
				}
			break;
			case R.id.share_btn:
				switch (action) {
					case MotionEvent.ACTION_DOWN:
						mDrivingShareText.setTextColor(Color.rgb(0,197,177));
					break;
					case MotionEvent.ACTION_UP:
						mDrivingShareText.setTextColor(Color.rgb(103,103,103));
					break;
				}
			break;
			case R.id.index_square_btn:
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					msquareBtn.setTextColor(Color.rgb(0,197,177));
					break;
				case MotionEvent.ACTION_UP:
					msquareBtn.setTextColor(Color.rgb(103,103,103));
					break;
				}
				break;
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id){
			case R.id.map_location_btn:
				//回到我的位置
				//移动地图中心点
				LatLng ll = new LatLng(LngLat.lat,LngLat.lng);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			break;
//			case R.id.map_marke_list_btn:
//				//跳转到视频直播点列表
//				Intent liveList = new Intent(MainActivity.this,LiveVideoListActivity.class);
//				startActivity(liveList);
//			break;
			case R.id.share_btn:
				click_share();
			break;
			case R.id.close_share_btn:
				//关闭视频分享
				mShareLayout.setVisibility(View.GONE);
			break;
//			case R.id.wifi_status_btn:
//				//跳转到ipc页面
//				Intent i = new Intent(MainActivity.this, CarRecorderActivity.class);
//				startActivity(i);
//			break;
			case R.id.more_btn:

				//更多页面
				Intent more = new Intent(MainActivity.this, IndexMoreActivity.class);
				startActivity(more);
			break;
			case R.id.share_local_video_btn:
				//点击精彩视频
				isClickShareVideo = false;
				click_toLocalVideoShare();
			break;
			case R.id.share_mylive_btn:
				//点击视频直播
				isClickShareVideo = true;
				toShareLive();
			break;
			case R.id.video_square_more_btn:
				//跳转到视频广场页面
				Intent videoSquare= new Intent(MainActivity.this,VideoSquareActivity.class);
				startActivity(videoSquare);
			break;
			case R.id.login_status_btn:
				//登录状态
//				checkLoginStatus();
			break;
			case R.id.wifi_link_text:
				//wifi链接状态
				checkWiFiStatus();
			break;
			case R.id.login_btn:
				//登录
//				login();
				break;
			case R.id.index_square_btn:
				// 视频广场
				toSqu();
				break;
			case R.id.index_wiifstate:
				//startWifi();
			
				break;
			case R.id.index_wifi_layout:
				if(GolukApplication.getInstance().getIpcIsLogin()){
					toCard();
				}
				break;
		}
	}
	
	private void click_share() {
		Log.i("lily", "----------click------");
		if (!mApp.isUserLoginSucess) {
			mShareLayout.setVisibility(View.GONE);
			mApp.mUser.setUserInterface(this);
			if(mApp.autoLoginStatus == 1){
				mBuilder = new AlertDialog.Builder(mContext);
				 dialog = mBuilder.setMessage("正在为您登录，请稍候……")
				.setCancelable(false)
				.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if(keyCode == KeyEvent.KEYCODE_BACK){
							return true;
						}
						return false;
					}
				}).create();
				dialog	.show();
				return ;
			}else if(mApp.autoLoginStatus == 3 || mApp.autoLoginStatus == 4){
				mShareLayout.setVisibility(View.GONE);
				Intent intent = new Intent(this, UserLoginActivity.class);
				intent.putExtra("isInfo", "back");
				startActivity(intent);
				return ;
			}
			mShareLayout.setVisibility(View.VISIBLE);
			return;
		}
		//视频分享
		mShareLayout.setVisibility(View.VISIBLE);
	}
	
	private void toCard() {
		Intent i = new Intent(MainActivity.this, CarRecorderActivity.class);
		startActivity(i);
	}
	
	private void toSqu() {
		Intent more = new Intent(MainActivity.this,VideoSquareActivity.class);
		startActivity(more);
	}
	private Builder mBuilder = null;
	private AlertDialog dialog = null;
	private void click_toLocalVideoShare() {
		Log.i("lily", "-------isUserLoginSuccess------"+mApp.isUserLoginSucess+"------autologinStatus-----"+mApp.autoLoginStatus);
		if (!mApp.isUserLoginSucess) {
			// TODO 未登录成功
			mShareLayout.setVisibility(View.GONE);
			mApp.mUser.setUserInterface(this);
			if(mApp.autoLoginStatus == 1){
				mBuilder = new AlertDialog.Builder(mContext);
				 dialog = mBuilder.setMessage("正在为您登录，请稍候……")
				.setCancelable(false)
				.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if(keyCode == KeyEvent.KEYCODE_BACK){
							return true;
						}
						return false;
					}
				}).create();
				dialog	.show();
				return ;
			}else if(mApp.autoLoginStatus == 3 || mApp.autoLoginStatus == 4){
//				console.toast("网络连接异常，请重试", mContext);
				return ;
			}
			Intent intent = new Intent(this, UserLoginActivity.class);
			intent.putExtra("isInfo", "back");
			startActivity(intent);
			return;
		}
		
		//跳转到本地视频分享列表
		Intent localVideoShareList = new Intent(MainActivity.this,LocalVideoShareListActivity.class);
		startActivity(localVideoShareList);
		//关闭视频分享
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
				// TODO 未登录成功
			mShareLayout.setVisibility(View.GONE);
			mApp.mUser.setUserInterface(this);
			if(mApp.autoLoginStatus == 1){
				mBuilder = new AlertDialog.Builder(mContext);
				 dialog = mBuilder.setMessage("正在为您登录，请稍候……")
				.setCancelable(false)
				.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if(keyCode == KeyEvent.KEYCODE_BACK){
							return true;
						}
						return false;
					}
				}).create();
				dialog	.show();
				return ;
			}else if(mApp.autoLoginStatus == 3 || mApp.autoLoginStatus == 4){
//				console.toast("网络连接异常，请重试", mContext);
				return ;
			}
			Intent intent = new Intent(this, UserLoginActivity.class);
			intent.putExtra("isInfo", "back");
			startActivity(intent);
			return;
		}
		
		if (!mApp.getIpcIsLogin()) {
			Toast.makeText(this, "IPC未登录", Toast.LENGTH_SHORT).show();
			return;
		}

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
		LogUtil.e(null,"jyf-----click------666666");
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
	
	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null){
				return;
			}
			//console.log("radius:" + location.getRadius() + "---lat:" + location.getLatitude() + "---lon:" + location.getLongitude());
			// 此处设置开发者获取到的方向信息，顺时针0-360
			MyLocationData locData = new MyLocationData.Builder()
				.accuracy(location.getRadius()).direction(100)
				.latitude(location.getLatitude()).longitude(location.getLongitude()).build();
			//确认地图我的位置点是否更新位置
			mBaiduMap.setMyLocationData(locData);
			
			//移动了地图,第一次不改变地图中心点位置
			if (isFirstLoc) {
				isFirstLoc = false;
				//移动地图中心点
				LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			
			//保存经纬度
			LngLat.lng = location.getLongitude();
			LngLat.lat = location.getLatitude();
			
			//保存地址信息
			GolukApplication.getInstance().mCurAddr = location.getAddrStr();
			System.out.println("YYY=========mCurAddr="+location.getAddrStr()+"==lon="+LngLat.lng+"==lat="+LngLat.lat);
			//更新IPC经纬度
//			if(GolukApplication.getInstance().getIpcIsLogin()){
//				long lon = (long)(location.getLongitude()*3600000);
//				long lat = (long)(location.getLatitude()*3600000);
//				int speed = (int)location.getSpeed();
//				int direction = (int)location.getDirection();
//				boolean a = GolukApplication.getInstance().getIPCControlManager().updateGPS(lon, lat, speed, direction);
//				System.out.println("YYY=====updateGPS====a="+a+"===lon="+lon+"===lat="+lat);
//			}
			
			//更新行车记录仪地址
			if(null != CarRecorderActivity.mHandler){
				Message msg = CarRecorderActivity.mHandler.obtainMessage(CarRecorderActivity.ADDR);
				msg.obj = location.getAddrStr();
				CarRecorderActivity.mHandler.sendMessage(msg);
			}
			
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	public void wifiCallBack(int state, String message, WifiRsBean[] arrays) {
		// TODO Auto-generated method stub
		console.log("首页wifi自动连接接口回调---state---" + state + "---message---" + message + "---arrays---" + arrays);
//		switch (state) {
//			case -1:
//				//console.toast(message, mContext);
//			break;
//			case -3:
//				//已连接
//				if(null != arrays){
//					if(arrays.length > 0){
//						String wifiName = arrays[0].getWifiName();
//						console.log("自动连接小车本wifi---wifiName---" + wifiName);
//						//保存wifi校验名称
//						WiFiConnection.SaveWiFiName(wifiName);
//					}
//				}
//			break;
//			case 1:
//				if(null != arrays){
//					if(arrays.length > 0){
//						String wifiName = arrays[0].getWifiName();
//						console.log("自动连接小车本wifi---wifiName---" + wifiName);
//						//保存wifi校验名称
//						WiFiConnection.SaveWiFiName(wifiName);
//					}
//				}
//			break;
//			case 11:
//			break;
//		}
//		unregisterReceiver(mWac);
//		//校验wifi连接状态
//		mApp.VerifyWiFiConnect();
	}	
	
	public void dismissAutoDialog(){
		if (null != dialog){
			dialog.dismiss();
			dialog = null;
		}
	}
	@Override
	public void statusChange() {
		// TODO Auto-generated method stub
		/*if(mApp.autoLoginStatus !=1){
			dismissAutoDialog();
			Intent it = null;
			if(mApp.autoLoginStatus == 2){
				if(!isClickShareVideo){
					it = new Intent(MainActivity.this,LocalVideoShareListActivity.class);
				}else{
					it = new Intent(MainActivity.this,LiveActivity.class);
				}
				startActivity(it);
			}
		}*/
		if(mApp.autoLoginStatus != 1){
			dismissAutoDialog();
			if(mApp.autoLoginStatus == 2){
				mShareLayout.setVisibility(View.VISIBLE);
			}
		}
		
	}


	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (dialogType == LiveDialogManager.DIALOG_TYPE_LOGIN) {
			if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
				// TODO 去登录界面
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
		}
		
	}

	@Override
	public void LocationCallBack(String gpsJson) {
		BaiduPosition location = JsonUtil.parseLocatoinJson(gpsJson);

		if (location == null || mMapView == null){
			return;
		}
		
	}

	@Override
	public void CallBack_BaiduGeoCoder(int function, Object obj) {
		if (null == obj) {
			LogUtil.e(null, "jyf----20150406----LiveActivity----CallBack_BaiduGeoCoder----获取反地理编码  : " + (String) obj);
			return;
		}
		
	}
	
}





