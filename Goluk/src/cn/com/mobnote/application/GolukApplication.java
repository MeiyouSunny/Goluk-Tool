package cn.com.mobnote.application;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.LiveVideoListActivity;
import cn.com.mobnote.golukmobile.LiveVideoPlayActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserLoginActivity;
import cn.com.mobnote.golukmobile.UserManager;
import cn.com.mobnote.golukmobile.UserPersonalEditActivity;
import cn.com.mobnote.golukmobile.UserRepwdActivity;
import cn.com.mobnote.golukmobile.UserRegistActivity;
import cn.com.mobnote.golukmobile.VideoShareActivity;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.PreferencesReader;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.wifimanage.WifiApAdmin;
import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.util.console;
import cn.com.mobnote.wifi.WiFiConnection;
import cn.com.tiros.api.Const;
import cn.com.tiros.utils.LogUtil;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;

public class GolukApplication extends Application implements IPageNotifyFn, IPCManagerFn, ITalkFn , ILocationFn{
	/** JIN接口类 */
	public GolukLogic mGoluk = null;
	/** 保存上下文 */
	private Context mContext = null;
	/** 来源标示,用来强转activity */
	private String mPageSource = "";
	/** 主页activity */
	private MainActivity mMainActivity = null;
	/** 视频保存地址 fs1:指向->sd卡/tiros-com-cn-ext目录*/
	private String mVideoSavePath = "fs1:/video/";
	/** wifi管理类*/
	private WifiManager mWifiManage = null;
	/** wifi链接 */
	private WiFiConnection mWiFiConnection = null;
	
	private static GolukApplication instance=null;
	private IPCControlManager mIPCControlManager=null;
	private VideoSquareManager mVideoSquareManager=null;
	/** 登录IPC是否登录成功 */
	private boolean isIpcLoginSuccess = false;
	/**　用户是否登录小车本服务器成功 */
	public boolean isUserLoginSucess = false;
	/** CC视频上传地址 */
	public String mCCUrl = null;
	/** 当前登录用户的UID */
	public String mCurrentUId = null;
	/** 当前登录用户的Aid */
	public String mCurrentAid = null;
	/** 行车记录仪缓冲路径 */
	private String carrecorderCachePath="";
	/** 爱滔客回调 */
	private ITalkFn mTalkListener = null;
	/** 音视频配置信息 */
	private VideoConfigState mVideoConfigState=null;
	/** 自动循环录制状态标识 */
	private boolean autoRecordFlag=false;
	/** 停车安防配置 */
	private int[] motioncfg;
	
	
	private WifiApAdmin wifiAp;
	/** 当前地址 */
	public String mCurAddr = null;
	/** 全局提示框 */
	public WindowManager mWindowManager = null;
	public WindowManager.LayoutParams mWMParams = null;
	public RelativeLayout mVideoUploadLayout = null;
	private TextView tv = null;
	
	/**登陆管理类**/
	public UserManager userManager;
	
	private HashMap<String, ILocationFn> mLocationHashMap = new HashMap<String,ILocationFn>();
	
	static {
		System.loadLibrary("golukmobile");
	}
	
	
	@Override
	public void onCreate(){
		super.onCreate();
		instance=this;
		Const.setAppContext(this);
		initRdCardSDK();
		initCachePath();
//		createWifi();
		//实例化JIN接口,请求网络数据
		initImageLoader(getApplicationContext());

	}
	
	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

		};
	};
	
	public void initLogic() {
		if (null != mGoluk) {
			return;
		}
		mGoluk = new GolukLogic();

		mIPCControlManager = new IPCControlManager(this);
		mIPCControlManager.addIPCManagerListener("application", this);
		
		mVideoSquareManager = new VideoSquareManager(this);
		// 注册回调
		mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_HttpPage, this);
		// 注册爱滔客回调协议
		mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_Talk, this);
		// 注册定位回调
		mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_Location, this);
		GlobalWindow.getInstance().setApplication(this);
		motioncfg = new int[2];
	}
	
	/**
	 * 创建行车记录仪缓冲路径
	 * @author xuhw
	 * @date 2015年3月19日
	 */
	private void initCachePath(){
		carrecorderCachePath = Environment
				.getExternalStorageDirectory()
				+ File.separator
				+ "tiros-com-cn-ext"
				+ File.separator
				+ "goluk_carrecorder";
		GFileUtils.makedir(carrecorderCachePath);
	}
	
	/**
	 * 获取行车记录仪缓冲路径
	 * @return
	 * @author xuhw
	 * @date 2015年3月19日
	 */
	public String getCarrecorderCachePath(){
		return this.carrecorderCachePath;
	}
	
	/**
	 * 设置音视频配置信息
	 * @param videocfg
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public void setVideoConfigState(VideoConfigState videocfg){
		this.mVideoConfigState=videocfg;
	}
	
	/**
	 * 获取音视频配置信息
	 * @return
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public VideoConfigState getVideoConfigState(){
		return this.mVideoConfigState;
	}
	
	/**
	 * 设置自动循环录制开关
	 * @param auto
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public void setAutoRecordState(boolean auto){
		this.autoRecordFlag=auto;
	}
	
	/**
	 * 获取自动循环录制状态
	 * @return
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public boolean getAutoRecordState(){
		return this.autoRecordFlag;
	}
	
	/**
	 * 获取停车安防状态
	 * @return
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public int[] getMotionCfg(){
		return this.motioncfg;
	}
	
	/**
	 * 创建wifi热点
	 * @author xuhw
	 * @date 2015年3月23日
	 */
	private void createWifi(){
//		FileManage mFileMange = new FileManage(this, null);
		
		String wifi_ssid= SettingUtils.getInstance().getString("wifi_ssid", "ipc_dev3");
		String wifi_password = SettingUtils.getInstance().getString("wifi_password", "123456789");
		wifiAp = new WifiApAdmin(this, mHandler);
		if(!wifiAp.isWifiApEnabled()){
			wifiAp.startWifiAp(wifi_ssid, wifi_password);
		}
	}
	

	public void editWifi(String wifiName, String password){
		SettingUtils.getInstance().putString("wifi_ssid", wifiName); 
		SettingUtils.getInstance().putString("wifi_password", password); 
		
		wifiAp.startWifiAp(wifiName, password); 
	}
	
	/**
	 * 初始化锐动SDK
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	private void initRdCardSDK() {
		try {
			// 初始CarRecorderManager
			CarRecorderManager.initilize(this);
			// 设置配置信息
			CarRecorderManager.setConfiguration(new PreferencesReader(this)
					.getConfig());
			// 注册OSD
			// CarRecorderManager.registerOSDBuilder(RecordOSDBuilder.class);
			// 是否强制使用旧录制方式
			// 不调用以下方法，或设置为false时，将在android4.3+ 启用新录制
//			CarRecorderManager.enableComptibleMode(true);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (RecorderStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取IPC控制管理类
	 * @return
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public IPCControlManager getIPCControlManager(){
		return mIPCControlManager;
	}
	
	/**
	 * 获取视频广场管理类
	 * @return
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public VideoSquareManager getVideoSquareManager(){
		return mVideoSquareManager;
	}
	
	public static GolukApplication getInstance(){
		return instance;
	}
	
	/**
	 * 获取IPC登录状态
	 * @return
	 * @author xuhw
	 * @date 2015年3月18日
	 */
	public boolean getIpcIsLogin() {
		return isIpcLoginSuccess;
	}
	
	/**
	 * 保存上下文
	 * @param context
	 */
	public void setContext(Context context,String source){
		this.mContext = context;
		this.mPageSource = source;
		
		//保存MainActivity,用来解决离开主页传输进度
		if(source == "Main"){
			mMainActivity = ((MainActivity)mContext);
		}
	}
	
	public Context getContext(){
		return this.mContext;
	}
	
	public void setTalkListener(ITalkFn fn) {
		this.mTalkListener = fn;
	}
	
	/**
	 * 验证wifi链接状态
	 */
	public void VerifyWiFiConnect(){
		//判断小车本wifi是否链接成功
//		mWifiManage = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
//		mWiFiConnection = new WiFiConnection(mWifiManage,mContext);
//		boolean b = mWiFiConnection.WiFiLinkStatus();
//		if(b){
//			console.log("wifi---通知logic链接成功---" + b);
//			//通知logic链接成功
////			mGoluk.GoLuk_WifiStateChanged(true);
//		}
//		else{
//			console.log("wifi---通知login断开链接--" + b);
//			//通知login断开链接
////			mGoluk.GoLuk_WifiStateChanged(false);
//			if(null != mMainActivity){
//				mMainActivity.WiFiLinkStatus(3);
//			}
//		}
	}
	
	private boolean isShowGlobalwindow = false;
	
	public void dimissGlobalWindow() {
		if (isShowGlobalwindow) {
			mWindowManager.removeView(mVideoUploadLayout);
			isShowGlobalwindow = false;
		}
	}
	
	@SuppressLint("InflateParams")
	public void createVideoUploadWindow(){
		console.log("upload service--VideoShareActivity-handleCancel----顶层窗口显示---111 ");
		if (isShowGlobalwindow) {
			dimissGlobalWindow();
		}
		
		console.log("upload service--VideoShareActivity-handleCancel----顶层窗口显示 ");
		
		isShowGlobalwindow = true;
		
		//获取LayoutParams对象
		mWMParams = new WindowManager.LayoutParams();
		//获取的是CompatModeWrapper对象
		mWindowManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		
		mWMParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
		//mWMParams.type = LayoutParams.TYPE_PRIORITY_PHONE;
		mWMParams.format = PixelFormat.RGBA_8888;
		//mWMParams.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mWMParams.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_NOT_TOUCH_MODAL |LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		mWMParams.gravity = Gravity.LEFT | Gravity.TOP;
		mWMParams.x = 0;
		mWMParams.y = 30;
		mWMParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		///获得根视图
		View v = ((Activity) mContext).getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		///状态栏标题栏的总高度,所以标题栏的高度为top2-top
		int top2 = v.getTop();
		mWMParams.height = 50;
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mVideoUploadLayout = (RelativeLayout) inflater.inflate(R.layout.video_share_upload_window, null);
		tv = (TextView) mVideoUploadLayout.findViewById(R.id.video_upload_percent);
		mWindowManager.addView(mVideoUploadLayout,mWMParams);
		ImageView view = (ImageView)mVideoUploadLayout.findViewById(R.id.video_loading_img);
		Animation rotateAnimation = AnimationUtils.loadAnimation(this.getContext(), R.anim.upload_loading);
		LinearInterpolator lin = new LinearInterpolator();
		rotateAnimation.setInterpolator(lin);
		view.startAnimation(rotateAnimation);
		//setContentView(R.layout.main);
		//mFloatView = (Button)mFloatLayout.findViewById(R.id.float_id);
	}
	
	public void refreshPercent(int percent) {
		console.log("upload service--VideoShareActivity-handleCancel----Application---refreshPercent: " + percent);
		if (null == mVideoUploadLayout) {
			return;
		 }
		console.log("upload service--VideoShareActivity-handleCancel----Application---refreshPercent222: ");
		TextView tvtv = (TextView)mVideoUploadLayout.findViewById(R.id.video_upload_percent);
		if (null != tvtv) {
			console.log("upload service--VideoShareActivity-handleCancel----Application---refreshPercent3333: ");
			tvtv.setText("" + percent +"%");
		}
	}
	
	public void topWindowSucess(String message){
		if (null == mVideoUploadLayout) {
			return;
		}
		TextView tvtv = (TextView)mVideoUploadLayout.findViewById(R.id.video_upload_percent);
		tvtv.setVisibility(View.GONE);
		ImageView img = (ImageView) mVideoUploadLayout.findViewById(R.id.video_loading_img);
		img.getAnimation().cancel();
		img.setVisibility(View.GONE);
		TextView msgTv = (TextView) mVideoUploadLayout.findViewById(R.id.video_upload_text);
		msgTv.setVisibility(View.VISIBLE);
		msgTv.setText(message);	
	}
	
	/**
	 * 首页,在线视频基础数据,图片下载数据回调
	 * @param status,0/1,基础数据/图片下载
	 * @param data
	 */
	public void onLineVideoCallBack(int status,Object data){
		if(null != mMainActivity){
			switch(status){
				case 0:
					//在线视频基础数据回调
					mMainActivity.onLineVideoCallBack(data);
				break;
				case 1:
					//在线视频图片下载完成回调
					mMainActivity.onLineVideoImageCallBack(data);
				break;
			}
		}
	}
	
	/**
	 * 本地视频上传回调
	 * @param vid,视频ID
	 */
	public void localVideoUpLoadCallBack(int success,String vid){
		if(mPageSource == "VideoShare"){
			((VideoShareActivity)mContext).videoUploadCallBack(success,vid);
		}
	}
	
	/**
	 * 本地视频分享回调
	 * @param data,分享json数据,
	 * {"code":"200","vurl":"http://cdn3.lbs8.com/files/cdcvideo/3dfa8172-8fdc-4acd-b882-f191608f236720141124183820.mp4","vid":"3dfa8172-8fdc-4acd-b882-f191608f236720141124183820"}
	 */
	public void localVideoShareCallBack(int success,String data){
		if(mPageSource == "VideoShare"){
			((VideoShareActivity)mContext).videoShareCallBack(success,data);
		}
	}
	
	/**
	 * ipc视频截取查询成功回调函数
	 * @param success
	 * @param data
	 * @author chenxy
	 */
	public void ipcVideoSingleQueryCallBack(int success,String data){
		if(0 == success){
			//查询成功,解析文件名去下载
			//{"time": 1262275832, "id": 845., "period": 8, "resolution": 14, "type": 4, "size": 5865250., "location": "WND1_100101001032_0008.mp4", "withSnapshot": 1, "withGps": 0}
			try{
				JSONObject json = new JSONObject(data);
				String fileName = json.getString("location");
				console.log("调用ipc视频下载接口---ipcVideoSingleQueryCallBack---downloadFile---" + fileName);
				int type = json.getInt("type");
				String savePath = "";
				if(type == 2){
					//紧急视频
					savePath = mVideoSavePath + "urgent/";
				}
				else{
					//精彩视频
					savePath = mVideoSavePath + "wonderful/";
				}
				//调用下载视频接口
				mIPCControlManager.downloadFile(fileName,"videodownload",savePath);
			}
			catch(Exception e){
				console.log("解析视频下载JSON数据错误");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ipc视频下载回调函数
	 * @param success
	 * @param data
	 * @author chenxy
	 */
	public void ipcVideoDownLoadCallBack(int success,String data){
		if(1 == success){
			//下载中
		}
		else if(0 == success){
			//下载完成
			if(null != mMainActivity){
				//{"filename":"WND1_150402183837_0012.mp4", "tag":"videodownload"}
				//地图大头针图片
				console.log("视频下载完成---ipcVideoDownLoadCallBack---" + data);
				mMainActivity.videoAnalyzeComplete(data);
			}
		}
	}
	
	/**
	 * 网络请求数据回调
	 */
	@Override
	public void pageNotifyCallBack(int type, int success, Object param1,Object param2) {
		console.log("chxy send pageNotifyCallBack--" + "type:" + type + ",success:" + success + ",param1:" + param1 + ",param2:" + param2);
		//null{"code":"200","json":[{"vid":"test11","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test11.mp4","purl":"http://img2.3lian.com/img2007/18/18/003.png","desc":"陈真暴揍小日本","comment":"215","ilike":"123"},{"vid":"test12","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test12.mp4","purl":"http://img.cool80.com/i/png/217/02.png","desc":"轮椅女孩环游世界","comment":"17","ilike":"111"},{"vid":"test13","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test13.mp4","purl":"http://img2.3lian.com/img2007/14/03/20080405141042281.png","desc":"万年不毕业小学生，每次出现引发各种血案","comment":"207","ilike":"90"}]}
		//null{'vid':'test11','path':'fs1:/Cache/test11.png'}
		//null{'vid':'test12','path':'fs1:/Cache/test12.png'}
		//null{'vid':'test13','path':'fs1:/Cache/test13.png'}
		switch(type){
			case 0:
				if(success == 1){
					//首页,在线视频基础数据,图片下载数据回调
					onLineVideoCallBack((Integer)param1,param2);
				}
			break;
			case 1:
				//本地视频编辑页面,点击下一步,在上传页面上传本地视频回调
				localVideoUpLoadCallBack(success,String.valueOf(param2));
			break;
			case 2:
				//本地视频分享链接请求回调
				localVideoShareCallBack(success,String.valueOf(param2));
			break;
			case 7:
				if(null != mMainActivity){
					//地图大头针图片
					console.log("pageNotifyCallBack---地图大头针数据---" + String.valueOf(param2));
					//地图大头针
					mMainActivity.pointDataCallback(success,param2);
				}
				if(mPageSource == "LiveVideoList"){
					console.log("pageNotifyCallBack---直播列表数据---" + String.valueOf(param2));
					((LiveVideoListActivity)mContext).LiveListDataCallback(success,param2);
				}
				
				// 为了更新直播界面的别人的位置信息
				if (null != mContext && mContext instanceof LiveActivity) {
					((LiveActivity) mContext).pointDataCallback(success, param2);
				}
			break;
			case 8:
				if(mPageSource == "Main"){
					//地图大头针图片
					console.log("pageNotifyCallBack---地图大头针图片---" + String.valueOf(param2));
					((MainActivity)mContext).downloadBubbleImageCallBack(success,param2);
				}
				if(mPageSource == "LiveVideoList"){
					//地图大头针图片
					console.log("pageNotifyCallBack---直播列表图片---" + String.valueOf(param2));
					((LiveVideoListActivity)mContext).downloadVideoImageCallBack(success,param2);
				}
			break;
			case 9:
				LogUtil.e(null, "jyf----20150406----application----999999999999---- : ");
				if(mPageSource == "LiveVideo"){
					console.log("pageNotifyCallBack---直播视频数据--" + String.valueOf(param2));
					if (mContext instanceof LiveVideoPlayActivity) {
						((LiveVideoPlayActivity)mContext).LiveVideoDataCallBack(success,param2);
					} else if (mContext instanceof LiveActivity) {
						((LiveActivity)mContext).LiveVideoDataCallBack(success,param2);
					}					
				}
			break;
			//登陆
			case 11:
				parseLoginData(success, param2);
				if(null != mMainActivity){
					//地图大头针图片
					console.log("pageNotifyCallBack---登录---" + String.valueOf(param2));
					mMainActivity.loginCallBack(success,param2);
				}
				if(mPageSource == "UserLogin"){
					((UserLoginActivity)mContext).loginCallBack(success, param2);
				}
			break;
			//自动登录
			case 12:
				parseLoginData(success, param2);
				if(mPageSource == "Main"){
					((MainActivity)mContext).autoLoginCallback(success, param2);
				}
				break;
			//验证码PageType_GetVCode
			case 15:
				//注册获取验证码
				if(mPageSource == "UserRegist"){
					((UserRegistActivity)mContext).identifyCallback(success, param2);
				}
				//重置密码获取验证码
				if(mPageSource == "UserRepwd"){
					((UserRepwdActivity)mContext).isRepwdCallBack(success,param2);
				}
				break;
			//注册PageType_Register
			case 16:
				if(mPageSource == "UserRegist"){
					((UserRegistActivity)mContext).registCallback(success, param2);
				}
				break;
			//重置密码PageType_ModifyPwd
			case 17:
				if(mPageSource == "UserRepwd"){
					((UserRepwdActivity)mContext).repwdCallBack(success,param2);
				}
				break;	
			case IPageNotifyFn.PageType_ModifyUserInfo:
				if(mPageSource == "UserPersonalEdit"){
					((UserPersonalEditActivity)mContext).saveInfoCallBack(success, param2);
				}
			case PageType_LiveStart:
				// 获取直播信息成功
				if (null != mContext) {
					if (mContext instanceof MainActivity) {
						((MainActivity) mContext).callBack_LiveLookStart(true, success, param1, param2);
					} else if (mContext instanceof LiveActivity) {
						((LiveActivity) mContext).callBack_LiveLookStart(true, success, param1, param2);
					}	
				}
				
				break;
			case PageType_PlayStart:
				// 看别人直播
				if (null != mContext && mContext instanceof MainActivity) {
					((MainActivity) mContext).callBack_LiveLookStart(false,success, param1, param2);
				}
				break;
			case PageType_LiveLike:
				// 直播点赞
				if (null != mContext && mContext instanceof LiveActivity) {
					((LiveActivity) mContext ).callBack_clickOK(success, param1, param2);
				}
				break;
			
		}
	}

	
	/**
	 * 处理登录结果
	 * 
	 * @param success
	 *            1/其它 成功/失败
	 * @param param2
	 *            登录回调数据
	 * @author jiayf
	 * @date Apr 20, 2015
	 */
	private void parseLoginData(int success, Object param2) {
		if (1 != success || null == param2) {
			return;
		}

		try {
			JSONObject rootObj = new JSONObject((String) param2);
			int code = Integer.valueOf(rootObj.getString("code"));
			if (200 != code) {
				return;
			}

			JSONObject dataObj = rootObj.getJSONObject("data");
			// 获得CC上传视频接口
			mCCUrl = dataObj.getString("ccbackurl");
			mCurrentUId = dataObj.getString("uid");
			mCurrentAid = dataObj.getString("aid");
			isUserLoginSucess = true;
			
			LogUtil.e(null, "jyf---------GolukApplication---------mCCurl:" + mCCUrl +" uid:" + mCurrentUId +" aid:" + mCurrentAid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
//		System.out.println("IPC_TTTTTT========event="+event+"===msg="+msg+"===param1="+param1+"=========param2="+param2);
		/*
		System.out.println("IPC_TTTTTT========event="+event+"===msg="+msg+"===param1="+param1+"=========param2="+param2);
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			if (IPCManagerFn.ConnectionStateMsg_Connected != msg) {
				isIpcLoginSuccess = false;
			}
		}
		
		if (ENetTransEvent_IPC_VDCP_CommandResp == event
				&& IPC_VDCP_Msg_Init == msg && 0 == param1) {
			isIpcLoginSuccess = true;
			System.out.println("IPC_TTTTTT=================Login Success===============");
		}
		*/
		
		//console.log("IPC_TTTTTT========event="+event+"===msg="+msg+"===param1="+param1+"=========param2="+param2);
		//IPC控制连接状态 event = 0
		if(ENetTransEvent_IPC_VDCP_ConnectState == event){
			//如果不是连接成功,都标识为失败
			switch(msg){
				case ConnectionStateMsg_Idle:
					//msg = 0 空闲
					isIpcLoginSuccess = false;
					if(null != mMainActivity){
						mMainActivity.wiFiLinkStatus(3);
					}
				break;
				case ConnectionStateMsg_Connecting:
					//msg = 1 连接中
					isIpcLoginSuccess = false;
					if(null != mMainActivity){
						mMainActivity.wiFiLinkStatus(3);
					}
				break;
				case ConnectionStateMsg_Connected:
					//msg = 2 连接成功
					//只是,ipc信号连接了,初始化的东西还没完成,所以要等到ipc初始化成功,才能把isIpcLoginSuccess=true
				break;
				case ConnectionStateMsg_DisConnected:
					//msg = 3 连接断开
					isIpcLoginSuccess = false;
					if(null != mMainActivity){
						mMainActivity.wiFiLinkStatus(3);
					}
				break;
			}
		}
		
		// IPC控制命令应答 event = 1
		if(ENetTransEvent_IPC_VDCP_CommandResp == event){
			switch(msg){
				case IPC_VDCP_Msg_Init:
					//msg = 0 初始化消息
					//param1 = 0 成功 | 失败
					if(0 == param1){
						//ipc控制初始化成功,可以看画面和拍摄8s视频
						isIpcLoginSuccess = true;
						//获取音视频配置信息
						getVideoEncodeCfg();
						//发起获取自动循环录制状态
						updateAutoRecordState();
						//获取停车安防配置信息
						updateMotionCfg();
						//自动同步系统时间
						if(SettingUtils.getInstance().getBoolean("systemtime", true)){
							boolean a = GolukApplication.getInstance().getIPCControlManager().setIPCSystemTime(System.currentTimeMillis()/1000);
							System.out.println("IPC_TTTTTT===========setIPCSystemTime===============a="+a);
						}
						console.log("IPC_TTTTTT=================Login Success===============");
						//Toast.makeText(mContext, "IPC登录成功", Toast.LENGTH_SHORT).show();
						//改变首页链接状态
						if(null != mMainActivity){
							mMainActivity.wiFiLinkStatus(2);
						}
					}
					else{
						isIpcLoginSuccess = false;
					}
				break;
				case IPC_VDCP_Msg_Query:
					//msg = 1000 多文件目录查询
				break;
				case IPC_VDCP_Msg_SingleQuery:
					//msg = 1001 单文件查询
					//拍摄8秒视频成功之后,接口会自动调用查询这个文件,收到这个回调之后可以根据文件名去下载视频
					//event=1,msg=1001,param1=0,param2={"time": 1262275832, "id": 845., "period": 8, "resolution": 14, "type": 4, "size": 5865250., "location": "WND1_100101001032_0008.mp4", "withSnapshot": 1, "withGps": 0}
					ipcVideoSingleQueryCallBack(param1,(String)param2);
				break;
				case IPC_VDCP_Msg_Erase:
					//msg = 1002 删除文件
				break;
				case IPC_VDCP_Msg_TriggerRecord:
					//msg = 1003 请求紧急、精彩视频录制
					//发送拍摄指令后,会立即收到视频文件名称的回调,暂时无用
					//event=1,msg=1003,param1=0,param2={"type":4, "filename":"WND1_100101001032_0008.mp4"}
				break;
				case IPC_VDCP_Msg_SnapPic:
					//msg = 1004 实时抓图
				break;
				case IPC_VDCP_Msg_RecPicUsage:
					//msg = 1005 查询录制存储状态
				break;
				case IPC_VDCP_Msg_DeviceStatus:
					//msg = 1006 查询设备状态
				break;
				case IPC_VDCP_Msg_GetVedioEncodeCfg:
					if(param1 == RESULE_SUCESS){
						VideoConfigState videocfg = IpcDataParser.parseVideoConfigState((String)param2);
						if(null != videocfg){
							mVideoConfigState = videocfg;
						}
					}
				break;
				case IPC_VDCP_Msg_SetVedioEncodeCfg:
					if(param1 == RESULE_SUCESS){
						getVideoEncodeCfg();
						updateAutoRecordState();
					}
					break;
				case IPC_VDCP_Msg_GetRecordState:
					if(param1 == RESULE_SUCESS){
						autoRecordFlag = IpcDataParser.getAutoRecordState((String)param2);
					}
					break;
				case IPC_VDCP_Msg_StartRecord:
					autoRecordFlag = true;
					break;
				case IPC_VDCP_Msg_StopRecord:
					autoRecordFlag = false;
					break;
				case IPC_VDCP_Msg_GetMotionCfg:
					if(param1 == RESULE_SUCESS){
						try {
							JSONObject json = new JSONObject((String)param2);
							if(null != json){
								int enableSecurity = json.optInt("enableSecurity");
								int snapInterval = json.optInt("snapInterval");
								
								motioncfg[0] = enableSecurity;
								motioncfg[1] = snapInterval;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					break;
				case IPC_VDCP_Msg_SetMotionCfg:
					if(param1 == RESULE_SUCESS){
						updateMotionCfg();
					}
					break;
			}
		}
		
		//IPC下载连接状态 event = 2
		if(ENetTransEvent_IPC_VDTP_ConnectState == event){
			//msg = 1 | 连接中 or msg = 2 | 连接成功
			//当前不需要处理这些状态
		}
		
		//IPC下载结果应答,开始下载视频文件 event = 3
		if(ENetTransEvent_IPC_VDTP_Resp == event){
			switch(msg){
				case IPC_VDTP_Msg_File:
					//文件传输中消息 msg = 0
					//param1 = 0,下载完成
					//param1 = 1,下载中
					ipcVideoDownLoadCallBack(param1,(String)param2);
				break;
			}
		}
	}

	@Override
	public void TalkNotifyCallBack(int type, String data) {
		if(null == mTalkListener) {
			return;
		}
		mTalkListener.TalkNotifyCallBack(type, data);
	}
	private static SharedPreferences preferences;
	private static Editor editor;
	/**
	 * 进行缓存用户的登陆状态
	 * @param context
	 * @param key
	 * @param remeberLoginState
	 */
	public static void cacheRemeberLoginState(Context context, String key, boolean remeberLoginState,String name,String pass) {
		if (preferences == null) {
			preferences = context.getSharedPreferences("application", Context.MODE_PRIVATE);
		}
		editor = preferences.edit();
		editor.putBoolean(key, remeberLoginState);
		editor.putString("name", name);
		editor.putString("pass", pass);
		editor.commit();
	}

	/*
	 * 获取判断是否为第一次进入APP的缓存值
	 * 
	 * @param context 对应上下文
	 * 
	 * @param key 对应缓存值得key
	 */
	public static boolean getIsFirstComeApp(Context context, String key) {
		if (preferences == null) {
			preferences = context.getSharedPreferences("application", Context.MODE_PRIVATE);
		}
		boolean isFirstComeApp = preferences.getBoolean(key, false); 
		return isFirstComeApp;
	}
	
	/**
	 * 获取音视频配置信息
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	private void getVideoEncodeCfg(){
		if(GolukApplication.getInstance().getIpcIsLogin()){
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean flag = GolukApplication.getInstance().getIPCControlManager().getVideoEncodeCfg(0);
					System.out.println("YYY============getVideoEncodeCfg=========flag="+flag);
				}
			}).start();
		}
	}
	
	/**
	 * 发起获取自动循环录制状态
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	private void updateAutoRecordState(){
		if(GolukApplication.getInstance().getIpcIsLogin()){
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean record = GolukApplication.getInstance().getIPCControlManager().getRecordState();
					System.out.println("YYY=========getRecordState========="+record);
				}
			}).start();
		}
	}
	
	/**
	 * 获取停车安防配置
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	private void updateMotionCfg(){
		if(GolukApplication.getInstance().getIpcIsLogin()){
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean record = GolukApplication.getInstance().getIPCControlManager().getMotionCfg();
					System.out.println("YYY=========getMotionCfg========="+record);
				}
			}).start();
		}
	}
	
	/**
	* 初始化ImageLoader
	 * @param context
	 * @author xuhw
	 * @date 2015年4月16日
	 */
	private void initImageLoader(Context context) {
		String httpcache = Environment
				.getExternalStorageDirectory()
				+ File.separator
				+ "tiros-com-cn-ext"
				+ File.separator
				+ "VideoSquare"
				+ File.separator
				+ "cache";
		GFileUtils.makedir(carrecorderCachePath);
		File cache = new File(httpcache);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration   
				.Builder(this)   
//		          .memoryCacheExtraOptions(480, 800) //即保存的每个缓存文件的最大长宽   
				.threadPoolSize(3)//线程池内加载的数量   
				.threadPriority(Thread.NORM_PRIORITY -2)   
				.denyCacheImageMultipleSizesInMemory()   
				.memoryCache(new UsingFreqLimitedMemoryCache(2* 1024 * 1024)) //你可以通过自己的内存缓存实现   
				.memoryCacheSize(8 * 1024 * 1024)     
				.discCacheSize(50 * 1024 * 1024)     
				.discCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密   
				.tasksProcessingOrder(QueueProcessingType.LIFO)   
				.discCacheFileCount(200) //缓存的文件数量   
				.discCache(new UnlimitedDiscCache(cache))//自定义缓存路径   
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())   
				.imageDownloader(new BaseImageDownloader(this,5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间   
				.writeDebugLogs() // Remove for releaseapp   
				.build();//开始构建   
		
		ImageLoader.getInstance().init(config);
	}

	public void addLocationListener(String key, ILocationFn fn) {
		if (!mLocationHashMap.containsValue(key)){
			return;
		}
		mLocationHashMap.put(key, fn);
	}
	
	public void removeLocationListener(String key) {
		mLocationHashMap.remove(key);
	}

	@Override
	public void LocationCallBack(String locationJson) {
		// TODO 定位回调
		LogUtil.e("" , "jyf-------Application   LocationCallBack: " + locationJson) ;
		if (null == mLocationHashMap) {
			return;
		}
		
		Iterator <Entry<String, ILocationFn>> it = mLocationHashMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, ILocationFn> entry = it.next();
			entry.getValue().LocationCallBack(locationJson);
		}
	}
	
}
