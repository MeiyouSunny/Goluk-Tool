package cn.com.mobnote.application;

import java.io.File;

import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;

import cn.com.mobnote.golukmobile.LiveVideoListActivity;
import cn.com.mobnote.golukmobile.LiveVideoPlayActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.VideoEditActivity;
import cn.com.mobnote.golukmobile.VideoShareActivity;
import cn.com.mobnote.golukmobile.carrecorder.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.PreferencesReader;
import cn.com.mobnote.golukmobile.carrecorder.SettingUtils;
import cn.com.mobnote.golukmobile.wifimanage.FileManage;
import cn.com.mobnote.golukmobile.wifimanage.WifiApAdmin;
import cn.com.mobnote.tachograph.comm.IPCManagerFn;
import cn.com.mobnote.util.console;
import cn.com.mobnote.wifi.WiFiConnection;
import cn.com.mobonote.golukmobile.comm.GolukMobile;
import cn.com.mobonote.golukmobile.comm.INetTransNotifyFn;
import cn.com.mobonote.golukmobile.comm.IPageNotifyFn;
import cn.com.tiros.api.Const;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;

public class GolukApplication extends Application implements IPageNotifyFn,INetTransNotifyFn, IPCManagerFn{
	/** JIN接口类 */
	public GolukMobile mGoluk = null;
	/** 保存上下文 */
	private Context mContext = null;
	/** 来源标示,用来强转activity */
	private String mPageSource = "";
	/** 主页activity */
	private MainActivity mMainActivity = null;
	/** wifi管理类*/
	private WifiManager mWifiManage = null;
	/** wifi链接 */
	private WiFiConnection mWiFiConnection = null;
	
	private static GolukApplication instance=null;
	private IPCControlManager mIPCControlManager=null;
	/** 登录IPC是否登录成功 */
	private boolean isIpcLoginSuccess = false;
	/** 行车记录仪缓冲路径 */
	private String carrecorderCachePath="";
	
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
		createWifi();
		mIPCControlManager = new IPCControlManager();
		mIPCControlManager.addIPCManagerListener("application", this);
		
		//实例化JIN接口,请求网络数据
		mGoluk = new GolukMobile();
		//JIN接口创建
		mGoluk.GolukMobile_Create();
		
		//http请求监听
//		mGoluk.GoLuk_RegistPageNotify(this);
		//socket文件传输监听
//		mGoluk.GoLuk_RegistNetTransNotify(this);
	}
	
	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
//				case value:
//					
//					break;

			default:
				break;
			}
		};
	};
	
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
	
	WifiApAdmin wifiAp;
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
			CarRecorderManager.enableComptibleMode(true);
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
		if(mPageSource == "VideoEdit"){
			((VideoEditActivity)mContext).videoUploadCallBack(success,vid);
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
				//本地视频编辑页面,点击下一步上传本地视频回调
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
				if(mPageSource == "LiveVideo"){
					console.log("pageNotifyCallBack---直播视频数据--" + String.valueOf(param2));
					((LiveVideoPlayActivity)mContext).LiveVideoDataCallBack(success,param2);
				}
			break;
			case 11:
				if(null != mMainActivity){
					//地图大头针图片
					console.log("pageNotifyCallBack---登录---" + String.valueOf(param2));
					mMainActivity.loginCallBack(success,param2);
				}
			break;
		}
	}
	
	/**
	 * 本地视频数据传输回调
	 */
	@Override
	public void netTransNotifyCallBack(int event, int msg, Object param1,Object param2) {
//		//console.log("chxy send netTransNotifyCallBack--" + "event:" + event + ",msg:" + msg + ",param1:" + param1 + ",param2:" + param2);
//		
//		if(event == GolukMobile.ENetTransEvent_ConnectionState){
//			//连接状态事件
//			if(msg == GolukMobile.ConnectionStateMsg_Connecting){
//				//Toast.makeText(mContext,"正在连接服务器...",Toast.LENGTH_SHORT).show();
//				console.log("wifi---正在连接服务器..." + msg);
//				if(null != mMainActivity){
//					mMainActivity.WiFiLinkStatus(1);
//				}
//			}
//			else if(msg == GolukMobile.ConnectionStateMsg_Connected){
//				//Toast.makeText(mContext,"连接服务器成功",Toast.LENGTH_SHORT).show();
//				console.log("wifi---连接服务器成功..." + msg);
//				if(null != mMainActivity){
//					mMainActivity.WiFiLinkStatus(2);
//				}
//			}
//			else if(msg == GolukMobile.ConnectionStateMsg_DisConnected){
//				//Toast.makeText(mContext,"与服务器断开连接",Toast.LENGTH_SHORT).show();
//				console.log("wifi---与服务器断开连接..." + msg);
//				if(null != mMainActivity){
//					mMainActivity.socketLinkOff();
//					if(null != mMainActivity){
//						mMainActivity.WiFiLinkStatus(3);
//					}
//				}
//			}
//		}
//		else if(event == GolukMobile.ENetTransEvent_TransmissionState){
//			//传输事件
//			if(msg == GolukMobile.TransmissionStateMsg_CheckList){
//				String result = (String)param2;
//				if(!"".equals(result)){
//					//Toast.makeText(mContext,"文件列表同步完成",Toast.LENGTH_SHORT).show();
//					//需要传输的文件目录数组
//					//{"filepath":["test13.mp4","test12.mp4","test115.mp4","test1112.mp4","test1111.mp4","test11.mp4"]}
//					if(null != mMainActivity){
//						console.log("wifi---得到同步视频目录---analyzeVideoFile---" + result);
//						mMainActivity.videoFileCallBack(param2);
//					}
//				}
//				else{
//				}
//			}
//			else if(msg == GolukMobile.TransmissionStateMsg_File){
//				if((Integer)param1 == 0){
//					//文件下载进度
//					//msg:2,param1:0,param2:{"filesize": 32814978, "fileid": 0, "filerecvsize": 903}
//					if(null != mMainActivity){
//						console.log("wifi---视频同步进度---" + "event:" + event + ",msg:" + msg + ",param1:" + param1 + ",param2:" + param2);
//						mMainActivity.videoDataAnalyze(param2);
//					}
//				}
//				else if((Integer)param1 == 1){
//					//文件下载完毕
//					//String file = (String)param2;
//					//Toast.makeText(mContext,"新接收文件接收成功" ,Toast.LENGTH_SHORT).show();
//					console.log("wifi---视频同步完成---");
//					if(null != mMainActivity){
//						mMainActivity.videoAnalyzeComplete();
//					}
//					if(mPageSource == "LocalVideoList"){
//						((LocalVideoListActivity)mContext).videoAnalyzeComplete();
//					}
//				}
//				else if ((Integer)param1 == 2){
//					console.log("wifi---视频同步失败---");
//					//文件传输失败
//					if(null != mMainActivity){
//						mMainActivity.socketLinkOff();
//					}
//				}
//			}
//		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		System.out.println("IPC_TTTTTT========event="+event+"===msg="+msg+"===param1="+param1+"=========param2="+param2);
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			if (ConnectionStateMsg_Connected != msg) {
				isIpcLoginSuccess = false;
			}
		}
		
		if (ENetTransEvent_IPC_VDCP_CommandResp == event
				&& IPC_VDCP_Msg_Init == msg && 0 == param1) {
			isIpcLoginSuccess = true;
			System.out.println("IPC_TTTTTT=================Login Success===============");
		}
		
	}
	
}
