package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import org.json.JSONArray;
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
import cn.com.mobnote.entity.LngLat;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.LocalVideoListAdapter;
import cn.com.mobnote.video.LocalVideoManage;
import cn.com.mobnote.video.LocalVideoManage.LocalVideoData;
import cn.com.mobnote.video.OnLineVideoManage;
import cn.com.mobnote.view.LoadingView;
import cn.com.mobnote.view.MyGridView;
import cn.com.mobnote.wifi.WiFiConnection;
import cn.com.mobnote.wifi.WifiAutoConnectManager;
import cn.com.mobnote.wifi.WifiConnCallBack;
import cn.com.mobnote.wifi.WifiRsBean;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.entity.LngLat;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.module.page.IPageNotifyFn;
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
public class MainActivity extends Activity implements OnClickListener , WifiConnCallBack, OnTouchListener{
	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	
	/** 地图layout */
	private RelativeLayout mMapLayout = null;
	/** 我的位置按钮 */
	private Button mMapLocationBtn = null;
	/** 直播marker列表按钮 */
	private Button mMapMarkeListBtn = null;
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
	/** 本地视频按钮 */
	private Button mLocalVideoListBtn = null;
	
	
	
	/** 登录状态 */
	private Button mLoginStatusBtn = null;
	/** wifi连接状态文本 */
	private Button mWiFiLinkStatus = null;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext
		//注意该方法要再setContentView方法之前实现  
		SDKInitializer.initialize(getApplicationContext());
		
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
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(this,"Main");
		
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
	}
	
	/**
	 * 页面初始化,获取页面元素,注册事件
	 */
	private void init(){
		mLayoutInflater = LayoutInflater.from(mContext);
		mMapMarkeListBtn = (Button)findViewById(R.id.map_marke_list_btn);
		//地图我的位置按钮
		mMapLocationBtn = (Button) findViewById(R.id.map_location_btn);
		//分享按钮
		mShareBtn = (ImageButton) findViewById(R.id.share_btn);
		mDrivingShareText = (TextView) findViewById(R.id.driving_share_text);
		mShareLayout = (RelativeLayout) findViewById(R.id.share_layout);
		mCloseShareBtn = (ImageButton) findViewById(R.id.close_share_btn);
		
		mIpcWiFiBtn = (Button) findViewById(R.id.wifi_status_btn);
		mMoreBtn = (Button) findViewById(R.id.more_btn);
		
		//本地视频更多按钮
		mLocalVideoListBtn = (Button)findViewById(R.id.share_local_video_btn);
		
//		mLoginStatusBtn = (Button) findViewById(R.id.login_status_btn);
//		mWiFiLinkStatus = (Button) findViewById(R.id.wifi_link_text);

//		mDefaultTipLayout = (RelativeLayout) findViewById(R.id.defaulttiplayout);
//		mScrollView = (ScrollView)findViewById(R.id.index_scroll);
//		mIndexLayout = (LinearLayout)findViewById(R.id.index_layout);
		
		//注册事件
		mMapMarkeListBtn.setOnClickListener(this);
		mMapLocationBtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mShareBtn.setOnTouchListener(this);
		mCloseShareBtn.setOnClickListener(this);
		mIpcWiFiBtn.setOnClickListener(this);
		mIpcWiFiBtn.setOnTouchListener(this);
		mMoreBtn.setOnClickListener(this);
		mMoreBtn.setOnTouchListener(this);
		mLocalVideoListBtn.setOnClickListener(this);
		
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
						boolean b = checkLinkWiFi();
						//网络状态改变
						mApp.VerifyWiFiConnect(b);
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
	 * 初始化地图
	 */
	private void initMap(){
		mMapLayout = (RelativeLayout) findViewById(R.id.map_layout);
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
		// 设置坐标类型
		// 返回国测局经纬度坐标系 coor=gcj02
		// 返回百度墨卡托坐标系 coor=bd09
		// 返回百度经纬度坐标系 coor=bd09ll
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		//注册touch拦截事件
		//为了解决地图拖动事件冲突问题
//		RelativeLayout mapBlankView = (RelativeLayout)findViewById(R.id.map_blankview);
//		mapBlankView.setOnTouchListener(new View.OnTouchListener() {
//			@SuppressLint("ClickableViewAccessibility")
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if(event.getAction() == MotionEvent.ACTION_UP){
//					mScrollView.requestDisallowInterceptTouchEvent(false);
//				}else{
//					mScrollView.requestDisallowInterceptTouchEvent(true);
//				}
//				return false;
//			}
//		});
		
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
	 * 检测登录状态
	 */
	@SuppressLint("InflateParams")
	private void checkLoginStatus(){
		if(mLoginDialog == null){
			mLoginLayout = mLayoutInflater.inflate(R.layout.login, null);
			
			mLoginPhoneText = (EditText) mLoginLayout.findViewById(R.id.login_phone_text);
			mLoginPwdText = (EditText) mLoginLayout.findViewById(R.id.login_pwd_text);
			mLoginBtn = (Button) mLoginLayout.findViewById(R.id.login_btn);
			
			mLoginPhoneText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					String pwd = mLoginPwdText.getText().toString();
					if("".equals(arg0.toString())){
						if("".equals(pwd)){
							//显示普通按钮
							mLoginBtn.setBackgroundResource(R.drawable.btn_log);
						}
					}
					else{
						if(!"".equals(pwd)){
							//显示高亮登录按钮
							mLoginBtn.setBackgroundResource(R.drawable.btn_log_btn);
						}
					}
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
				@Override
				public void afterTextChanged(Editable arg0) {}
			});
			mLoginPwdText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					String phone = mLoginPhoneText.getText().toString();
					if("".equals(arg0.toString())){
						if("".equals(phone)){
							//显示普通按钮
							mLoginBtn.setBackgroundResource(R.drawable.btn_log);
						}
					}
					else{
						if(!"".equals(phone)){
							//显示高亮登录按钮
							mLoginBtn.setBackgroundResource(R.drawable.btn_log_btn);
						}
					}
				}
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
				@Override
				public void afterTextChanged(Editable arg0) {}
			});
			mLoginBtn.setOnClickListener(this);
			
			AlertDialog.Builder builder =new AlertDialog.Builder(mContext);
			builder.setView(mLoginLayout);
			mLoginDialog = builder.create();
			mLoginDialog.show();
			//showKeybord();
		}
		else{
			mLoginDialog.show();
			//showKeybord();
		}
	}
	
	/**
	 * 登录
	 */
	private void login(){
		String phone = mLoginPhoneText.getText().toString();
		String pwd = mLoginPwdText.getText().toString();
		if(!"".equals(phone) && !"".equals(pwd)){
			if(phone.length() == 11){
				if(pwd.length() > 5){
					console.log("调用登录接口---login---" + phone + "---" + pwd);
					String condi = "{\"PNumber\":\"" + phone + "\",\"Password\":\"" + pwd + "\",\"tag\":\"android\"}";
					boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_Login, condi);
					if(!b){
						console.log("调用登录接口失败---b---" + b);
					}else{
						Intent login = new Intent(MainActivity.this,UserCenterActivity.class);
						startActivity(login);
						mLoginDialog.hide();
					}
				}
				else{
					console.toast("密码少于6位", mContext);
				}
			}
			else{
				console.toast("手机号输入错误", mContext);
			}
		}
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
	private boolean checkLinkWiFi(){
		WifiManager mWifiManage = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WiFiConnection connection = new WiFiConnection(mWifiManage,mContext);
		WifiInfo info = connection.getWiFiInfo();
		WifiAutoConnectManager wac = new WifiAutoConnectManager(mWifiManage,this);
		boolean b = wac.getEffectiveWifi(info);
		return b;
//		if(b){
//			String wifiName = info.getSSID();
//			//保存wifi校验名称
//			WiFiConnection.SaveWiFiName(wifiName);
//		}
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
	public void videoAnalyzeComplete(){
//		mLocalVideoManage.videoUploadCallBack();
//		mLocalVideoListAdapter.notifyDataSetChanged();
		playDownLoadedSound();
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
				mIpcWiFiBtn.setText("已连接");
				mIpcWiFiBtn.setTextColor(Color.rgb(0,197,177));
				img = res.getDrawable(R.drawable.index_icon_xingche_connect);
				mWiFiStatus = 2;
			break;
			case 3:
				//未连接
				mIpcWiFiBtn.setText("未连接");
				mIpcWiFiBtn.setTextColor(Color.rgb(103,103,103));
				img = res.getDrawable(R.drawable.index_icon_xingche_btn);
				mWiFiStatus = 0;
			break;
		}
		//调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
		img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
		mIpcWiFiBtn.setCompoundDrawables(null, img, null, null);
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
						Intent login = new Intent(MainActivity.this,UserCenterActivity.class);
						startActivity(login);
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
			console.toast("登录失败", mContext);
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
		else{
			//跳转到ipc页面
			Intent i = new Intent(MainActivity.this, CarRecorderActivity.class);
			startActivity(i);
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
		
		/*
		//回到页面重新检测wifi状态,只有未连接的情况下才重新检测
		if(mWiFiStatus == 0){
			mApp.VerifyWiFiConnect();
		}
		*/
		super.onResume();
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		if(null != mMapView){
			mMapView.onPause();
		}
		isCurrent = false;
		mMainHandler.removeMessages(2);
		
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
			int PID = android.os.Process.myPid();
			android.os.Process.killProcess(PID);
			android.os.Process.sendSignal(PID, 9);
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
			case R.id.wifi_status_btn:
				//如果已连接,不改变文字颜色
				if(mWiFiStatus != 2){
					switch (action) {
						case MotionEvent.ACTION_DOWN:
							mIpcWiFiBtn.setTextColor(Color.rgb(0,197,177));
						break;
						case MotionEvent.ACTION_UP:
							mIpcWiFiBtn.setTextColor(Color.rgb(103,103,103));
						break;
					}
				}
			break;
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
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.map_location_btn:
				//回到我的位置
				//移动地图中心点
				LatLng ll = new LatLng(LngLat.lat,LngLat.lng);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			break;
			case R.id.map_marke_list_btn:
				//跳转到视频直播点列表
				Intent liveList = new Intent(MainActivity.this,LiveVideoListActivity.class);
				startActivity(liveList);
			break;
			case R.id.share_btn:
				//视频分享
				mShareLayout.setVisibility(View.VISIBLE);
			break;
			case R.id.close_share_btn:
				//关闭视频分享
				mShareLayout.setVisibility(View.GONE);
			break;
			case R.id.wifi_status_btn:
				//wifi链接状态
				checkWiFiStatus();
			break;
			case R.id.more_btn:
				//更多页面
				Intent more = new Intent(MainActivity.this,IndexMoreActivity.class);
				startActivity(more);
			break;
			case R.id.share_local_video_btn:
				//跳转到本地视频列表
				Intent localVideoList = new Intent(MainActivity.this,LocalVideoListActivity.class);
				startActivity(localVideoList);
				//关闭视频分享
				mShareLayout.setVisibility(View.GONE);
			break;
			
			
			
			
			case R.id.video_square_more_btn:
				//跳转到视频广场页面
				Intent videoSquare= new Intent(MainActivity.this,VideoSquareActivity.class);
				startActivity(videoSquare);
			break;
			case R.id.login_status_btn:
				//登录状态
				checkLoginStatus();
			break;
			case R.id.wifi_link_text:
				//wifi链接状态
				checkWiFiStatus();
			break;
			case R.id.login_btn:
				//登录
				login();
			break;
		}
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
}





