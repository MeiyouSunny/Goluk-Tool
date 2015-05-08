package cn.com.mobnote.application;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import cn.com.mobnote.golukmobile.LiveVideoListActivity;
import cn.com.mobnote.golukmobile.LiveVideoPlayActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.UserPersonalEditActivity;
import cn.com.mobnote.golukmobile.UserRegistActivity;
import cn.com.mobnote.golukmobile.UserRepwdActivity;
import cn.com.mobnote.golukmobile.UserSetupActivity;
import cn.com.mobnote.golukmobile.VideoShareActivity;
import cn.com.mobnote.golukmobile.WiFiLinkCompleteActivity;
import cn.com.mobnote.golukmobile.WiFiLinkCreateHotActivity;
import cn.com.mobnote.golukmobile.WiFiLinkListActivity;
import cn.com.mobnote.golukmobile.carrecorder.CarRecorderActivity;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.PreferencesReader;
import cn.com.mobnote.golukmobile.carrecorder.entity.ExternalEventsDataInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnRightClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomFormatDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.wifimanage.WifiApAdmin;
import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.user.User;
import cn.com.mobnote.user.UserLoginManage;
import cn.com.mobnote.user.UserRegistManage;
import cn.com.mobnote.util.SharedPrefUtil;
import cn.com.mobnote.util.console;
import cn.com.mobnote.wifi.WiFiConnection;
import cn.com.tiros.api.Const;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.utils.LogUtil;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;

public class GolukApplication extends Application implements IPageNotifyFn,
		IPCManagerFn, ITalkFn, ILocationFn {
	/** JIN接口类 */
	public GolukLogic mGoluk = null;
	/** ip地址 */
	public static String mIpcIp = null;
	/** 保存上下文 */
	private Context mContext = null;
	/** 来源标示,用来强转activity */
	private String mPageSource = "";
	/** 主页activity */
	private MainActivity mMainActivity = null;
	/** 视频保存地址 fs1:指向->sd卡/tiros-com-cn-ext目录 */
	private String mVideoSavePath = "fs1:/video/";
	/** wifi管理类 */
	private WifiManager mWifiManage = null;
	/** wifi链接 */
	private WiFiConnection mWiFiConnection = null;

	private static GolukApplication instance = null;
	public IPCControlManager mIPCControlManager = null;
	private VideoSquareManager mVideoSquareManager = null;

	/** 登录IPC是否登录成功 */
	private boolean isIpcLoginSuccess = false;
	/** 　用户是否登录小车本服务器成功 */
	public boolean isUserLoginSucess = false;
	/** CC视频上传地址 */
	public String mCCUrl = null;
	/** 当前登录用户的UID */
	public String mCurrentUId = null;
	/** 当前登录用户的Aid */
	public String mCurrentAid = null;
	/** 行车记录仪缓冲路径 */
	private String carrecorderCachePath = "";
	/** 爱滔客回调 */
	private ITalkFn mTalkListener = null;
	/** 音视频配置信息 */
	private VideoConfigState mVideoConfigState = null;
	/** 自动循环录制状态标识 */
	private boolean autoRecordFlag = false;
	/** 停车安防配置 */
	private int[] motioncfg;
	/** 保存数据 */
	public SharedPrefUtil mSharedPreUtil = null;

	private WifiApAdmin wifiAp;
	/** 当前地址 */
	public String mCurAddr = null;
	/** 全局提示框 */
	public WindowManager mWindowManager = null;
	public WindowManager.LayoutParams mWMParams = null;
	public RelativeLayout mVideoUploadLayout = null;
	
	/**登录的五个状态  0登录中  1 登录成功  2登录失败  3手机号未注册，跳转注册页面  4超时   5密码错误达上限去重置密码**/
	public int loginStatus ;
	/** 注册的三个状态 1注册中 2注册成功   3注册失败 **/
	public int registStatus;
	/** 自动登录的四个状态 1自动登录中 2自动登录成功 3自动登录失败 4自动登录超时 5密码错误 **/
	public int autoLoginStatus;
	/** 注销状态 **/
	public boolean loginoutStatus = false;
	/** 获取验证码的四个状态 0 获取中 1获取成功 2获取失败 3手机号未注册 **/
	public int identifyStatus;

	/** User管理类 **/
	public User mUser = null;
	/** 登录管理类 **/
	public UserLoginManage mLoginManage = null;
	/** 注册管理类 **/
	public UserRegistManage mRegistManage = null;

	private HashMap<String, ILocationFn> mLocationHashMap = new HashMap<String, ILocationFn>();
	/** 未下载文件列表 */
	private List<String> mNoDownLoadFileList;
	/** 所有下载文件列表 */
	private List<String> mDownLoadFileList;

	/** ipc连接失败弹出的dailog */
	private CustomFormatDialog mconnection;
	/** 是否已经连接成功过 */
	private boolean isconnection = false;

	static {
		System.loadLibrary("golukmobile");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		Const.setAppContext(this);
		// TODO 此处不要做初始化相关的工作
	}

	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1001:
				tips();
				break;
			case 1002:
				backHomeDialog();// 弹出dialog 回到首页
				break;
			default:
				break;
			}
		};
	};

	public void initSharedPreUtil(Activity activity) {
		if (null == mSharedPreUtil) {
			mSharedPreUtil = new SharedPrefUtil(activity);
		}
	}

	public void initLogic() {
		if (null != mGoluk) {
			return;
		}
		initRdCardSDK();
		initCachePath();
		// createWifi();
		// 实例化JIN接口,请求网络数据

		mGoluk = new GolukLogic();

		/**
		 * 自动登录、登录、注册、重置密码、注销的管理类
		 */
		mUser = new User(this);
		mLoginManage = new UserLoginManage(this);
		mRegistManage = new UserRegistManage(this);

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
		mDownLoadFileList = new ArrayList<String>();
		mNoDownLoadFileList = new ArrayList<String>();
	}

	/**
	 * 创建行车记录仪缓冲路径
	 * 
	 * @author xuhw
	 * @date 2015年3月19日
	 */
	private void initCachePath() {
		carrecorderCachePath = Environment.getExternalStorageDirectory()
				+ File.separator + "tiros-com-cn-ext" + File.separator
				+ "goluk_carrecorder";
		GFileUtils.makedir(carrecorderCachePath);
	}

	/**
	 * 获取行车记录仪缓冲路径
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年3月19日
	 */
	public String getCarrecorderCachePath() {
		return this.carrecorderCachePath;
	}

	/**
	 * 设置音视频配置信息
	 * 
	 * @param videocfg
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public void setVideoConfigState(VideoConfigState videocfg) {
		this.mVideoConfigState = videocfg;
	}

	/**
	 * 获取音视频配置信息
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public VideoConfigState getVideoConfigState() {
		return this.mVideoConfigState;
	}

	/**
	 * 设置自动循环录制开关
	 * 
	 * @param auto
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public void setAutoRecordState(boolean auto) {
		this.autoRecordFlag = auto;
	}

	/**
	 * 获取自动循环录制状态
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public boolean getAutoRecordState() {
		return this.autoRecordFlag;
	}

	/**
	 * 获取停车安防状态
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	public int[] getMotionCfg() {
		return this.motioncfg;
	}

	/**
	 * 创建wifi热点
	 * 
	 * @author xuhw
	 * @date 2015年3月23日
	 */
	private void createWifi() {
		// FileManage mFileMange = new FileManage(this, null);

		String wifi_ssid = SettingUtils.getInstance().getString("wifi_ssid",
				"ipc_dev3");
		String wifi_password = SettingUtils.getInstance().getString(
				"wifi_password", "123456789");
		wifiAp = new WifiApAdmin(this, mHandler);
		if (!wifiAp.isWifiApEnabled()) {
			wifiAp.startWifiAp(wifi_ssid, wifi_password);
		}
	}

	public void editWifi(String wifiName, String password) {
		SettingUtils.getInstance().putString("wifi_ssid", wifiName);
		SettingUtils.getInstance().putString("wifi_password", password);

		wifiAp.startWifiAp(wifiName, password);
	}

	/**
	 * 初始化锐动SDK
	 * 
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	private void initRdCardSDK() {
		try {
			// 初始CarRecorderManager
			CarRecorderManager.initilize(this);
			// 设置配置信息
			CarRecorderManager.setConfiguration(new PreferencesReader(this, true)
					.getConfig());
			// 注册OSD
			// CarRecorderManager.registerOSDBuilder(RecordOSDBuilder.class);
			// 是否强制使用旧录制方式
			// 不调用以下方法，或设置为false时，将在android4.3+ 启用新录制
			// CarRecorderManager.enableComptibleMode(true);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (RecorderStateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取IPC控制管理类
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年3月21日
	 */
	public IPCControlManager getIPCControlManager() {
		return mIPCControlManager;
	}

	/**
	 * 获取视频广场管理类
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public VideoSquareManager getVideoSquareManager() {
		return mVideoSquareManager;
	}

	public static GolukApplication getInstance() {
		return instance;
	}

	/**
	 * 获取IPC登录状态
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年3月18日
	 */
	public boolean getIpcIsLogin() {
		return isIpcLoginSuccess;
	}

	/**
	 * 设置IPC退出登录
	 */
	public void setIpcLoginOut(){
		isIpcLoginSuccess = false;
		if(null != mMainActivity){
			mMainActivity.wiFiLinkStatus(3);
		}
	}
	
	/**
	 * 保存上下文
	 * 
	 * @param context
	 */
	public void setContext(Context context, String source) {
		this.mContext = context;
		this.mPageSource = source;

		// 保存MainActivity,用来解决离开主页传输进度
		if (source == "Main") {
			mMainActivity = ((MainActivity) mContext);
		}
	}

	public Context getContext() {
		return this.mContext;
	}

	public void setTalkListener(ITalkFn fn) {
		this.mTalkListener = fn;
	}

	/**
	 * 验证wifi链接状态
	 */
	public void VerifyWiFiConnect() {
		// 判断小车本wifi是否链接成功
		// mWifiManage =
		// (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		// mWiFiConnection = new WiFiConnection(mWifiManage,mContext);
		// boolean b = mWiFiConnection.WiFiLinkStatus();
		// if(b){
		// console.log("wifi---通知logic链接成功---" + b);
		// //通知logic链接成功
		// // mGoluk.GoLuk_WifiStateChanged(true);
		// }
		// else{
		// console.log("wifi---通知login断开链接--" + b);
		// //通知login断开链接
		// // mGoluk.GoLuk_WifiStateChanged(false);
		// if(null != mMainActivity){
		// mMainActivity.WiFiLinkStatus(3);
		// }
		// }
	}

	/**
	 * 首页,在线视频基础数据,图片下载数据回调
	 * 
	 * @param status
	 *            ,0/1,基础数据/图片下载
	 * @param data
	 */
	public void onLineVideoCallBack(int status, Object data) {
		if (null != mMainActivity) {
			switch (status) {
			case 0:
				// 在线视频基础数据回调
				mMainActivity.onLineVideoCallBack(data);
				break;
			case 1:
				// 在线视频图片下载完成回调
				mMainActivity.onLineVideoImageCallBack(data);
				break;
			}
		}
	}

	/**
	 * 本地视频上传回调
	 * 
	 * @param vid
	 *            ,视频ID
	 */
	public void localVideoUpLoadCallBack(int success, String vid) {
		if (mPageSource == "VideoShare") {
			((VideoShareActivity) mContext).videoUploadCallBack(success, vid);
		}
	}

	/**
	 * 本地视频分享回调
	 * 
	 * @param data
	 *            ,分享json数据, {"code":"200","vurl":
	 *            "http://cdn3.lbs8.com/files/cdcvideo/3dfa8172-8fdc-4acd-b882-f191608f236720141124183820.mp4"
	 *            ,"vid":"3dfa8172-8fdc-4acd-b882-f191608f236720141124183820"}
	 */
	public void localVideoShareCallBack(int success, String data) {
		if (mPageSource == "VideoShare") {
			((VideoShareActivity) mContext).videoShareCallBack(success, data);
		}
	}

	/**
	 * ipc视频截取查询成功回调函数
	 * 
	 * @param success
	 * @param data
	 * @author chenxy
	 */
	public void ipcVideoSingleQueryCallBack(int success, String data) {
		if (0 == success) {
			// 查询成功,解析文件名去下载
			// {"time": 1262275832, "id": 845., "period": 8, "resolution": 14,
			// "type": 4, "size": 5865250., "location":
			// "WND1_100101001032_0008.mp4", "withSnapshot": 1, "withGps": 0}
			try {
				JSONObject json = new JSONObject(data);
				String fileName = json.getString("location");
				long time = json.optLong("time");
				console.log("调用ipc视频下载接口---ipcVideoSingleQueryCallBack---downloadFile---"
						+ fileName);
				int type = json.getInt("type");
				String savePath = "";
//				if (type == 2) {a
//					// 紧急视频
//					savePath = mVideoSavePath + "urgent/";
//				} else {
//					// 精彩视频
//					savePath = mVideoSavePath + "wonderful/";
//				}
				if(IPCManagerFn.TYPE_SHORTCUT == type){
					savePath = mVideoSavePath + "wonderful/";
				}else if(IPCManagerFn.TYPE_URGENT == type){
					savePath = mVideoSavePath + "urgent/";
				}else{
					savePath = mVideoSavePath + "loop/";
				}
				// 调用下载视频接口
				boolean a = mIPCControlManager.downloadFile(fileName, "videodownload", savePath, time);
				LogUtil.e("xuhw", "YYYYYY====start==VideoDownLoad===flag="+a+"===data="+data);
				if (!mDownLoadFileList.contains(fileName)) {
					mDownLoadFileList.add(fileName);
				}
				if (!GlobalWindow.getInstance().isShow()) {
					LogUtil.e("xuhw", "YYYYYY======1111111111=========");
					GlobalWindow.getInstance().createVideoUploadWindow(
							"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size()
									+ "/" + mDownLoadFileList.size());
				} else {
					LogUtil.e("xuhw", "YYYYYY======22222=========");
					GlobalWindow.getInstance().updateText(
							"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size()
									+ "/" + mDownLoadFileList.size());
				}
			} catch (Exception e) {
				console.log("解析视频下载JSON数据错误");
				e.printStackTrace();
			}
		}
	}

	/**
	 * ipc视频下载回调函数
	 * 
	 * @param success
	 * @param data
	 * @author chenxy
	 */
	public void ipcVideoDownLoadCallBack(int success, String data) {

		if (TextUtils.isEmpty(data)) {
			return;
		}

		try {
			JSONObject jsonobj = new JSONObject(data);
			String tag = jsonobj.optString("tag");
			if (tag.equals("videodownload")) {
				if (1 == success) {
					// 下载中
					int percent = 0;
					JSONObject json = new JSONObject(data);
					String filename = json.optString("filename");
					long filesize = json.optLong("filesize");
					long filerecvsize = json.optLong("filerecvsize");
					percent = (int) ((filerecvsize * 100) / filesize);

					if (!mNoDownLoadFileList.contains(filename)) {
						mNoDownLoadFileList.add(filename);
					}
					if (!mDownLoadFileList.contains(filename)) {
						mDownLoadFileList.add(filename);
					}

					if (GlobalWindow.getInstance().isShow()) {
						GlobalWindow.getInstance().refreshPercent(percent);
						GlobalWindow.getInstance().updateText(
								"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size()
										+ "/" + mDownLoadFileList.size());
					} else {
						GlobalWindow.getInstance().createVideoUploadWindow(
								"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size()
										+ "/" + mDownLoadFileList.size());
					}

				} else if (0 == success) {
					// 下载完成
					if (null != mMainActivity) {
						// {"filename":"WND1_150402183837_0012.mp4",
						// "tag":"videodownload"}
						// 地图大头针图片
						console.log("视频下载完成---ipcVideoDownLoadCallBack---"+ data);
						LogUtil.e("xuhw", "YYYYYY======VideoDownLoad=====data="+data);
						mMainActivity.videoAnalyzeComplete(data);
					}

					if (checkDownloadCompleteState()) {
						mDownLoadFileList.clear();
						mNoDownLoadFileList.clear();
						GlobalWindow.getInstance().topWindowSucess("视频传输完成");
					}

					
				} else {
					LogUtil.e("xuhw", "YYYYYY=＠＠＠＠===download==fail===success="+success+"==data="+data);
					JSONObject json = new JSONObject(data);
					String filename = json.optString("filename");
					
					if (!mNoDownLoadFileList.contains(filename)) {
						mNoDownLoadFileList.add(filename);
					}
					
					if (checkDownloadCompleteState()) {
						mDownLoadFileList.clear();
						mNoDownLoadFileList.clear();
						GlobalWindow.getInstance().toFailed("视频传输失败");
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 检查下载是否全部完成
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年4月23日
	 */
	private boolean checkDownloadCompleteState() {
		boolean result = true;
		if (mDownLoadFileList.size() == mNoDownLoadFileList.size()) {
			for (int i = 0; i < mDownLoadFileList.size(); i++) {
				String name = mDownLoadFileList.get(i);
				if (mNoDownLoadFileList.contains(name)) {
					continue;
				} else {
					result = false;
					break;
				}
			}
		} else {
			result = false;
		}

		return result;
	}

	/**
	 * 网络请求数据回调
	 */
	@Override
	public void pageNotifyCallBack(int type, int success, Object param1,
			Object param2) {
		console.log("chxy send pageNotifyCallBack--" + "type:" + type
				+ ",success:" + success + ",param1:" + param1 + ",param2:"
				+ param2);
		// null{"code":"200","json":[{"vid":"test11","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test11.mp4","purl":"http://img2.3lian.com/img2007/18/18/003.png","desc":"陈真暴揍小日本","comment":"215","ilike":"123"},{"vid":"test12","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test12.mp4","purl":"http://img.cool80.com/i/png/217/02.png","desc":"轮椅女孩环游世界","comment":"17","ilike":"111"},{"vid":"test13","vurl":"http://cdn3.lbs8.com/files/cdcvideo/test13.mp4","purl":"http://img2.3lian.com/img2007/14/03/20080405141042281.png","desc":"万年不毕业小学生，每次出现引发各种血案","comment":"207","ilike":"90"}]}
		// null{'vid':'test11','path':'fs1:/Cache/test11.png'}
		// null{'vid':'test12','path':'fs1:/Cache/test12.png'}
		// null{'vid':'test13','path':'fs1:/Cache/test13.png'}
		switch (type) {
		case 0:
			if (success == 1) {
				// 首页,在线视频基础数据,图片下载数据回调
				onLineVideoCallBack((Integer) param1, param2);
			}
			break;
		case 1:
			// 本地视频编辑页面,点击下一步,在上传页面上传本地视频回调
			localVideoUpLoadCallBack(success, String.valueOf(param2));
			break;
		case 2:
			// 本地视频分享链接请求回调
			localVideoShareCallBack(success, String.valueOf(param2));
			break;
		case 7:
			if (null != mMainActivity) {
				// 地图大头针图片
				console.log("pageNotifyCallBack---地图大头针数据---"
						+ String.valueOf(param2));
				// 地图大头针
				mMainActivity.pointDataCallback(success, param2);
			}
			if (mPageSource == "LiveVideoList") {
				console.log("pageNotifyCallBack---直播列表数据---"
						+ String.valueOf(param2));
				((LiveVideoListActivity) mContext).LiveListDataCallback(
						success, param2);
			}

			// 为了更新直播界面的别人的位置信息
			if (null != mContext && mContext instanceof LiveActivity) {
				((LiveActivity) mContext).pointDataCallback(success, param2);
			}
			break;
		case 8:
			if (mPageSource == "Main") {
				// 地图大头针图片
				console.log("pageNotifyCallBack---地图大头针图片---"
						+ String.valueOf(param2));
				((MainActivity) mContext).downloadBubbleImageCallBack(success,
						param2);
			}
			if (mPageSource == "LiveVideoList") {
				// 地图大头针图片
				console.log("pageNotifyCallBack---直播列表图片---"
						+ String.valueOf(param2));
				((LiveVideoListActivity) mContext).downloadVideoImageCallBack(
						success, param2);
			}
			break;
		case 9:
			LogUtil.e(null,
					"jyf----20150406----application----999999999999---- : ");
			if (mPageSource == "LiveVideo") {
				console.log("pageNotifyCallBack---直播视频数据--"
						+ String.valueOf(param2));
				if (mContext instanceof LiveVideoPlayActivity) {
					((LiveVideoPlayActivity) mContext).LiveVideoDataCallBack(
							success, param2);
				} else if (mContext instanceof LiveActivity) {
					((LiveActivity) mContext).LiveVideoDataCallBack(success,
							param2);
				}
			}
			break;
		// 登陆

		case PageType_Login:

			if (null != mMainActivity) {
				// 地图大头针图片
				console.log("pageNotifyCallBack---登录---"+ String.valueOf(param2));
				mMainActivity.loginCallBack(success, param2);
			}
			// 登录
			if (mPageSource != "UserRegist") {
				mLoginManage.loginCallBack(success, param1, param2);
			}

			if (mPageSource == "UserRegist") {
				((UserRegistActivity) mContext).registLoginCallBack(success,param2);
			}

			parseLoginData(success, param2);
			break;
		// 自动登录

		case PageType_AutoLogin:

			mUser.initAutoLoginCallback(success, param1, param2);

			break;
		// 验证码PageType_GetVCode
		case PageType_GetVCode:
			// 注册获取验证码
			if (mPageSource == "UserRegist") {
				((UserRegistActivity) mContext).identifyCallback(success,
						param2);
			}
			// 重置密码获取验证码
			if (mPageSource == "UserRepwd") {
				((UserRepwdActivity) mContext).isRepwdCallBack(success, param2);
			}
			break;
		// 注册PageType_Register
		case PageType_Register:
			if (mPageSource == "UserRegist") {
				((UserRegistActivity) mContext).registCallback(success, param1,
						param2);
			}
			break;
		// 重置密码PageType_ModifyPwd
		case PageType_ModifyPwd:
			if (mPageSource == "UserRepwd") {
				((UserRepwdActivity) mContext).repwdCallBack(success,param1, param2);
			}
			break;
		case IPageNotifyFn.PageType_ModifyUserInfo:
			if (mPageSource == "UserPersonalEdit") {
				((UserPersonalEditActivity) mContext).saveInfoCallBack(success,param2);
			}
		case PageType_LiveStart:
			// 获取直播信息成功
			if (null != mContext) {
				if (mContext instanceof LiveActivity) {
					((LiveActivity) mContext).callBack_LiveLookStart(true,
							success, param1, param2);
				}
			}

			break;
		case PageType_PlayStart:

			break;
		case PageType_LiveLike:
			// 直播点赞
			if (null != mContext && mContext instanceof LiveActivity) {
				((LiveActivity) mContext).callBack_clickOK(success, param1,
						param2);
			}
			break;

		// 注销
		case PageType_SignOut:
			Log.i("loginout", "======application======");
			if (mPageSource == "UserSetup") {
				((UserSetupActivity) mContext).getLogintoutCallback(success,
						param2);
			}
			break;
		}
	}

	public boolean isNeedCheckLive = false;
	private boolean isCallContinue = false;

	// 显示
	public void showContinuteLive() {

		if (isCallContinue) {
			return;
		}
		LogUtil.e(null, "jyf----20150406----showContinuteLive----mApp :"
				+ mSharedPreUtil.getIsLiveNormalExit());
		isCallContinue = true;
		if (mContext instanceof MainActivity) {
			LogUtil.e(null,
					"jyf----20150406----showContinuteLive----mApp2222 :");
			isNeedCheckLive = false;
			if (!mSharedPreUtil.getIsLiveNormalExit()) {
				((MainActivity) mContext).showContinuteLive();
			}

		} else {
			LogUtil.e(null,
					"jyf----20150406----showContinuteLive----mApp33333 :");
			if (!mSharedPreUtil.getIsLiveNormalExit()) {
				isNeedCheckLive = true;
			}
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

			this.showContinuteLive();
			LogUtil.e(null, "jyf---------GolukApplication---------mCCurl:"
					+ mCCUrl + " uid:" + mCurrentUId + " aid:" + mCurrentAid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		// System.out.println("IPC_TTTTTT========event="+event+"===msg="+msg+"===param1="+param1+"=========param2="+param2);
		/*
		 * System.out.println("IPC_TTTTTT========event="+event+"===msg="+msg+
		 * "===param1="+param1+"=========param2="+param2); if
		 * (ENetTransEvent_IPC_VDCP_ConnectState == event) { if
		 * (IPCManagerFn.ConnectionStateMsg_Connected != msg) {
		 * isIpcLoginSuccess = false; } }
		 * 
		 * if (ENetTransEvent_IPC_VDCP_CommandResp == event && IPC_VDCP_Msg_Init
		 * == msg && 0 == param1) { isIpcLoginSuccess = true;
		 * System.out.println(
		 * "IPC_TTTTTT=================Login Success==============="); }
		 */

		// console.log("IPC_TTTTTT========event="+event+"===msg="+msg+"===param1="+param1+"=========param2="+param2);
		// IPC控制连接状态 event = 0
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			// 如果不是连接成功,都标识为失败
			switch (msg) {
			case ConnectionStateMsg_Idle:
				// msg = 0 空闲
				isIpcLoginSuccess = false;
				ipcDisconnect();
				// 已经连接成功过
				if (isconnection) {
					connectionDialog();
				}

				if (null != mMainActivity) {
					mMainActivity.wiFiLinkStatus(3);
				}
				break;
			case ConnectionStateMsg_Connecting:
				// msg = 1 连接中
				isIpcLoginSuccess = false;
				ipcDisconnect();
				// 已经连接成功过
				if (isconnection) {
					connectionDialog();
				}
				if (null != mMainActivity) {
					mMainActivity.wiFiLinkStatus(3);
				}
				break;
			case ConnectionStateMsg_Connected:
				// msg = 2 连接成功
				// 只是,ipc信号连接了,初始化的东西还没完成,所以要等到ipc初始化成功,才能把isIpcLoginSuccess=true
				break;
			case ConnectionStateMsg_DisConnected:
				// msg = 3 连接断开
				isIpcLoginSuccess = false;
				ipcDisconnect();
				// 已经连接成功过
				if (isconnection) {
					connectionDialog();
				}
				if (null != mMainActivity) {
					mMainActivity.wiFiLinkStatus(3);
				}
				break;
			}
		}
	
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
						isconnection = true;// 连接成功
						closeConnectionDialog();// 关闭连接的dialog
						boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCSystemTime();
						LogUtil.e("xuhw","YYYYYYY========getIPCSystemTime=======a="+a);
						
						SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
						boolean isbind = preferences.getBoolean("isbind", false);
						if (isbind) {
							//查询新文件列表（最多10条）
							long time = SettingUtils.getInstance().getLong("querytime", 0);
							long curtime = System.currentTimeMillis()/1000;
							LogUtil.e("xuhw", "YYYYYYY===start==queryNewFileList=1111==time="+time+"=curtime="+curtime);
							if(Math.abs(curtime - time) > 5*60){//五分钟以内断开重新连接的不做处理
								SettingUtils.getInstance().putLong("querytime", curtime);
								queryNewFileList();
								LogUtil.e("xuhw", "YYYYYYY===start==queryNewFileList====");
							}
						}
						
						console.log("IPC_TTTTTT=================Login Success===============");
						//Toast.makeText(mContext, "IPC登录成功", Toast.LENGTH_SHORT).show();
						//改变首页链接状态
						if(null != mMainActivity){
							mMainActivity.wiFiLinkStatus(2);
						}
						
						//如果在wifi连接页面,通知连接成功
						if(mPageSource == "WiFiLinkList"){
							((WiFiLinkListActivity)mContext).ipcLinkedCallBack();
						}
						
						//如果在wifi连接页面,通知连接成功
						if(mPageSource == "WiFiLinkComplete"){
							((WiFiLinkCompleteActivity)mContext).ipcLinkWiFiCallBack();
						}
					}
					else{

						isIpcLoginSuccess = false;
						ipcDisconnect();
					}
				break;
			case IPC_VDCP_Msg_Query:
					//msg = 1000 多文件目录查询
					if(RESULE_SUCESS == param1){
						LogUtil.e("xuhw", "YYYYYY=====IPC_VDCP_Msg_Query==mPageSource="+mPageSource+"=param2="+param2);
						if("ipcfilemanager".equals(mPageSource)){
							return;
						}
						LogUtil.e("xuhw", "YYYYYY===@@@@@@=###====11111===");
						if(TextUtils.isEmpty((String)param2)){
							return;
						}
						LogUtil.e("xuhw", "YYYYYY===@@@@@@===####==2222===");
						fileList = IpcDataParser.parseMoreFile((String) param2);
						mHandler.removeMessages(1001);
						mHandler.sendEmptyMessageDelayed(1001, 1000);
					}
				break;
			case IPC_VDCP_Msg_SingleQuery:
				// msg = 1001 单文件查询
				// 拍摄8秒视频成功之后,接口会自动调用查询这个文件,收到这个回调之后可以根据文件名去下载视频
				// event=1,msg=1001,param1=0,param2={"time": 1262275832, "id":
				// 845., "period": 8, "resolution": 14, "type": 4, "size":
				// 5865250., "location": "WND1_100101001032_0008.mp4",
				// "withSnapshot": 1, "withGps": 0}
				LogUtil.e("xuhw", "YYYYYY==@@@@==IPC_VDCP_Msg_SingleQuery==param1="+param1+"==param2="+param2);
				GFileUtils.writeIPCLog("YYYYYY====IPC_VDCP_Msg_SingleQuery==param1="+param1+"==param2="+param2);
				
				ipcVideoSingleQueryCallBack(param1, (String) param2);
				break;
			case IPC_VDCP_Msg_Erase:
				// msg = 1002 删除文件
				break;
			case IPC_VDCP_Msg_TriggerRecord:
				// msg = 1003 请求紧急、精彩视频录制
				// 发送拍摄指令后,会立即收到视频文件名称的回调,暂时无用
				// event=1,msg=1003,param1=0,param2={"type":4,
				// "filename":"WND1_100101001032_0008.mp4"}
				break;
			case IPC_VDCP_Msg_SnapPic:
				// msg = 1004 实时抓图
				break;
			case IPC_VDCP_Msg_RecPicUsage:
				// msg = 1005 查询录制存储状态
				break;
			case IPC_VDCP_Msg_DeviceStatus:
				// msg = 1006 查询设备状态
				break;
				case IPC_VDCPCmd_SetWifiCfg:
					//msg = 1012 设置IPC系统WIFI配置
					//param1 = 0 成功 | 失败
					if(0 == param1){
						//如果在wifi连接页面,通知设置成功
						if(mPageSource == "WiFiLinkCreateHot"){
							((WiFiLinkCreateHotActivity)mContext).setIpcLinkWiFiCallBack();
						}
					}
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
				if (param1 == RESULE_SUCESS) {
					getVideoEncodeCfg();
					updateAutoRecordState();
				}
				break;
			case IPC_VDCP_Msg_GetRecordState:
				LogUtil.e("xuhw", "video===IPC_VDCP_Msg_GetRecordState===param1="+param1+"=param2="+param2);
				if (param1 == RESULE_SUCESS) {
					autoRecordFlag = IpcDataParser
							.getAutoRecordState((String) param2);
				}
				break;
			case IPC_VDCP_Msg_StartRecord:
				LogUtil.e("xuhw", "video===IPC_VDCP_Msg_StartRecord===param1="+param1+"=param2="+param2);
				autoRecordFlag = true;
				break;
			case IPC_VDCP_Msg_StopRecord:
				LogUtil.e("xuhw", "video===IPC_VDCP_Msg_StopRecord===param1="+param1+"=param2="+param2);
				autoRecordFlag = false;
				break;
			case IPC_VDCP_Msg_GetMotionCfg:
				if (param1 == RESULE_SUCESS) {
					try {
						JSONObject json = new JSONObject((String) param2);
						if (null != json) {
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
				if (param1 == RESULE_SUCESS) {
					updateMotionCfg();
				}
				break;
			case IPC_VDCP_Msg_IPCKit:
				SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
				boolean isbind = preferences.getBoolean("isbind", false);
				if (!isbind) {
					return;
				}
				LogUtil.e("xuhw", "YYYYYY======IPC_VDCP_Msg_IPCKit=====param1="
						+ param1 + "===param2=" + param2);
				GFileUtils.writeIPCLog("YYYYYY======IPC_VDCP_Msg_IPCKit=====param1="
						+ param1 + "===param2=" + param2);
				
				if (param1 == RESULE_SUCESS) {
					List<ExternalEventsDataInfo> kit = IpcDataParser
							.parseKitData((String) param2);
					if (kit.size() > 0) {
						for (int i = 0; i < kit.size(); i++) {
							ExternalEventsDataInfo info = kit.get(i);
							if (!mDownLoadFileList.contains(info.location)) {
								mDownLoadFileList.add(info.location);

								boolean flag = GolukApplication.getInstance()
										.getIPCControlManager()
										.querySingleFile(info.location);
								LogUtil.e("xuhw",
										"YYYYYY=====querySingleFile=====type="
												+ info.type + "==flag=" + flag);
							}
						}
					}
				}
					break;
				case IPC_VDCP_Msg_GetVersion:
					//{"product": 67698688, "model": "", "macid": "", "serial": "", "version": "V1.4.21_tzz_vb_rootfs"}
					break;
				case IPC_VDCP_Msg_GetTime:
					if(param1 == RESULE_SUCESS){
						if(TextUtils.isEmpty((String)param2)){
							return;
						}
						long curtime = IpcDataParser.parseIPCTime((String)param2);
						//自动同步系统时间
						if(SettingUtils.getInstance().getBoolean("systemtime", true)){
							long time = SettingUtils.getInstance().getLong("cursystemtime");
							LogUtil.e("xuhw", "YYYYYY===getIPCSystemTime==time="+time+"=curtime="+curtime);
							if(Math.abs(curtime - time) > 60){//60秒内不自动同步
								SettingUtils.getInstance().putLong("cursystemtime", curtime);
								boolean a = GolukApplication.getInstance().getIPCControlManager().setIPCSystemTime(System.currentTimeMillis()/1000);
								LogUtil.e("xuhw", "YYYYYY===========setIPCSystemTime===============a="+a);
							}
						}
					}
					break;
				
			}
		}

		// IPC下载连接状态 event = 2
		if (ENetTransEvent_IPC_VDTP_ConnectState == event) {
			// msg = 1 | 连接中 or msg = 2 | 连接成功
			// 当前不需要处理这些状态
		}

		// IPC下载结果应答,开始下载视频文件 event = 3
		if (ENetTransEvent_IPC_VDTP_Resp == event) {
			switch (msg) {
			case IPC_VDTP_Msg_File:
				// 文件传输中消息 msg = 0
				// param1 = 0,下载完成
				// param1 = 1,下载中
				LogUtil.e("xuhw", "YYYYYY==@@@@@==IPC_VDTP_Msg_File===param1="+param1);
				if(((String)param2).equals(".mp4"))
					GFileUtils.writeIPCLog("===IPC_VDTP_Msg_File===param1="+param1+"=param2="+param2);
				
				ipcVideoDownLoadCallBack(param1, (String) param2);
				break;
			}
		}
	}

	@Override
	public void TalkNotifyCallBack(int type, String data) {
		if (null == mTalkListener) {
			return;
		}
		mTalkListener.TalkNotifyCallBack(type, data);
	}

	private static SharedPreferences preferences;
	private static Editor editor;

	/**
	 * 进行缓存用户的登陆状态
	 * 
	 * @param context
	 * @param key
	 * @param remeberLoginState
	 */
	public static void cacheRemeberLoginState(Context context, String key,
			boolean remeberLoginState, String name, String pass) {
		if (preferences == null) {
			preferences = context.getSharedPreferences("application",
					Context.MODE_PRIVATE);
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
			preferences = context.getSharedPreferences("application",
					Context.MODE_PRIVATE);
		}
		boolean isFirstComeApp = preferences.getBoolean(key, false);
		return isFirstComeApp;
	}

	/**
	 * 获取音视频配置信息
	 * 
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	private void getVideoEncodeCfg() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean flag = GolukApplication.getInstance()
							.getIPCControlManager().getVideoEncodeCfg(0);
					System.out
							.println("YYY============getVideoEncodeCfg=========flag="
									+ flag);
				}
			}).start();
		}
	}

	/**
	 * 发起获取自动循环录制状态
	 * 
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	private void updateAutoRecordState() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean record = GolukApplication.getInstance()
							.getIPCControlManager().getRecordState();
					System.out.println("YYY=========getRecordState========="
							+ record);
				}
			}).start();
		}
	}

	/**
	 * 获取停车安防配置
	 * 
	 * @author xuhw
	 * @date 2015年4月10日
	 */
	private void updateMotionCfg() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean record = GolukApplication.getInstance()
							.getIPCControlManager().getMotionCfg();
					System.out.println("YYY=========getMotionCfg========="
							+ record);
				}
			}).start();
		}
	}

	public void addLocationListener(String key, ILocationFn fn) {
		if (mLocationHashMap.containsValue(key)) {
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
		LogUtil.e("", "jyf-------Application   LocationCallBack: "
				+ locationJson);
		if (null == mLocationHashMap) {
			return;
		}

		Iterator<Entry<String, ILocationFn>> it = mLocationHashMap.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, ILocationFn> entry = it.next();
			entry.getValue().LocationCallBack(locationJson);
		}
	}

	/**
	 * 查询新文件列表（最多10条）
	 * 
	 * @author xuhw
	 * @date 2015年4月24日
	 */
	private void queryNewFileList() {
		long starttime = SettingUtils.getInstance().getLong("downloadfiletime", 0);
		mIPCControlManager.queryFileListInfo(6, 10, starttime, 2147483647);
	}

	/**
	 * IPC断开连接处理
	 * 
	 * @author xuhw
	 * @date 2015年4月24日
	 */
	private void ipcDisconnect() {
		mDownLoadFileList.clear();
		mNoDownLoadFileList.clear();
		if (GlobalWindow.getInstance().isShow()) {
			GlobalWindow.getInstance().dimissGlobalWindow();
		}

		if (null != mCustomDialog && mCustomDialog.isShowing()) {
			mCustomDialog.dismiss();
		}
	}

	ArrayList<VideoFileInfo> fileList;
	CustomDialog mCustomDialog = null;

	private void tips() {
		LogUtil.e("xuhw", "YYYYYY===@@@@@@=====11111===");
		if (null != mCustomDialog && mCustomDialog.isShowing()) {
			return;
		}
		LogUtil.e("xuhw", "YYYYYY===@@@@@@=====2222===");
		if (mContext instanceof Activity && fileList.size() > 0) {
			LogUtil.e("xuhw", "YYYYYY===@@@@@@=====33333===");
			Activity a = (Activity)mContext;
			if(!a.isFinishing()){
				LogUtil.e("xuhw", "YYYYYY===@@@@@@=====44444===");
				int size = fileList.size();
				for (int i = 0; i < fileList.size(); i++) {
					VideoFileInfo info = fileList.get(i);
					String filename = info.location;
					
					String filePath = "";
					if (filename.contains("WND")) {
						filePath = "fs1:/video/wonderful/";
					} else if (filename.contains("URG")) {
						filePath = "fs1:/video/urgent/";
					}
				
					if (TextUtils.isEmpty(filePath)) {
						break;
					}

					filePath = FileUtils.javaToLibPath(filePath);
					String path = filePath + File.separator + filename;
					File file = new File(path);
					if (file.exists()) {
						size -= 1;
					}else{
						if (!mDownLoadFileList.contains(info.location)) {
							mDownLoadFileList.add(info.location);
						}
					}
					
				}
				LogUtil.e("xuhw", "YYYYYY===@@@@@@=====5555===");
				if(size <= 0){
					return;
				}
				LogUtil.e("xuhw", "YYYYYY===@@@@@@=====6666===");
				mCustomDialog = new CustomDialog(mContext);
				mCustomDialog.setMessage("有" + size + "个新文件，确定要下载吗？",
						Gravity.CENTER);
				mCustomDialog.setLeftButton("确定", new OnLeftClickListener() {
					@Override
					public void onClickListener() {
						for(String name : mDownLoadFileList){
							boolean flag = GolukApplication.getInstance()
									.getIPCControlManager()
									.querySingleFile(name);
							LogUtil.e("xuhw",
									"YYYYYY=====querySingleFile=====name="+name+"==flag=" + flag);
						}
					}
				});
				mCustomDialog.setRightButton("取消", new OnRightClickListener() {
					@Override
					public void onClickListener() {
						if(null != fileList && fileList.size() > 0){
							long t1 = fileList.get(0).time;
							long t2 = fileList.get(fileList.size()-1).time;
							long time = t1 > t2 ? t1:t2;
							SettingUtils.getInstance().putLong("downloadfiletime", time);
							
							mDownLoadFileList.clear();
							mNoDownLoadFileList.clear();
						}
					}
				});
				mCustomDialog.show();
			}
			
		}

	}

	public void connectionDialog() {
		if (mconnection != null && mconnection.isShowing()) {
			return;
		}
		
		if (this.testActivity()){
			Activity a = (Activity)mContext;
			if(!a.isFinishing()){
				mconnection = new CustomFormatDialog(mContext);
				mconnection.setCancelable(false);
				mconnection.setMessage("摄像头断开，正在为您重连…");
				mconnection.show();
				mHandler.removeMessages(1002);
				mHandler.sendEmptyMessageDelayed(1002, 10000);
			}

		}
	}
	
	/**
	 * 验证固定的几个activity 可以弹框
	  * @Title: testActivity 
	  * @Description: TODO
	  * @return boolean 
	  * @author 曾浩 
	  * @throws
	 */
	public boolean testActivity(){
		if(mContext instanceof CarRecorderActivity){
			return true;
		}else{
			return false;
		}
		
	}

	private CustomDialog backHomedialog;

	public void backHomeDialog() {
		isconnection = false;
		closeConnectionDialog();// 关闭上一个dialog
		if (backHomedialog != null && backHomedialog.isShowing()) {
			return;
		}else{
			Activity a = (Activity)mContext;
			if(!a.isFinishing()){
				backHomedialog = new CustomDialog(mContext);
				backHomedialog.setMessage("您好像没有连接摄像头哦。", Gravity.CENTER);
				backHomedialog.setLeftButton("确定", new OnLeftClickListener() {
					@Override
					public void onClickListener() {

						Intent it = new Intent(mContext, MainActivity.class);
						it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						mContext.startActivity(it);
					}
				});
				
				if (backHomedialog.isShowing() == false) {
					if(!((Activity)mContext).isFinishing()){
						backHomedialog.show();
					}
				}
			}
		}

	}

	public void closeConnectionDialog() {
		mHandler.removeMessages(1002);
		if (mconnection != null) {
			if (mconnection.isShowing()) {
				mconnection.dismiss();
			}
		}

	}

}
