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
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import cn.com.mobnote.golukmobile.ImageClipActivity;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.PushSettingActivity;
import cn.com.mobnote.golukmobile.UserIdentifyActivity;
import cn.com.mobnote.golukmobile.UserOpinionActivity;
import cn.com.mobnote.golukmobile.UserPersonalHeadActivity;
import cn.com.mobnote.golukmobile.UserPersonalNameActivity;
import cn.com.mobnote.golukmobile.UserPersonalSignActivity;
import cn.com.mobnote.golukmobile.UserSetupActivity;
import cn.com.mobnote.golukmobile.UserSetupChangeWifiActivity;
import cn.com.mobnote.golukmobile.carrecorder.IPCControlManager;
import cn.com.mobnote.golukmobile.carrecorder.IpcDataParser;
import cn.com.mobnote.golukmobile.carrecorder.PreferencesReader;
import cn.com.mobnote.golukmobile.carrecorder.entity.ExternalEventsDataInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.IPCIdentityState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoConfigState;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoFileInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.GFileUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomDialog.OnLeftClickListener;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomFormatDialog;
import cn.com.mobnote.golukmobile.fresco.ConfigConstants;
import cn.com.mobnote.golukmobile.live.LiveActivity;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.photoalbum.FileInfoManagerUtils;
import cn.com.mobnote.golukmobile.photoalbum.PhotoAlbumActivity;
import cn.com.mobnote.golukmobile.startshare.VideoEditActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoCategoryActivity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.wifibind.WiFiLinkCompleteActivity;
import cn.com.mobnote.golukmobile.wifibind.WiFiLinkListActivity;
import cn.com.mobnote.golukmobile.wifimanage.WifiApAdmin;
import cn.com.mobnote.golukmobile.xdpush.GolukNotification;
import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.LngLat;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.user.IpcUpdateManage;
import cn.com.mobnote.user.TimerManage;
import cn.com.mobnote.user.User;
import cn.com.mobnote.user.UserIdentifyManage;
import cn.com.mobnote.user.UserLoginManage;
import cn.com.mobnote.user.UserRegistAndRepwdManage;
import cn.com.mobnote.util.AssetsFileUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.mobnote.util.SharedPrefUtil;
import cn.com.tiros.api.Const;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;

public class GolukApplication extends Application implements IPageNotifyFn, IPCManagerFn, ITalkFn, ILocationFn {
	/** JIN接口类 */
	public GolukLogic mGoluk = null;
	/** ip地址 */
	public static String mIpcIp = null;
	/** 保存上下文 */
	private Context mContext = null;
	/** 来源标示,用来强转activity */
	private String mPageSource = "";
	/** 主页activity */
	public static MainActivity mMainActivity = null;
	/** 视频保存地址 fs1:指向->sd卡/goluk目录 */
	private String mVideoSavePath = "fs1:/video/";

	private static GolukApplication instance = null;
	public IPCControlManager mIPCControlManager = null;
	private VideoSquareManager mVideoSquareManager = null;

	/** 登录IPC是否登录成功 */
	public boolean isIpcLoginSuccess = false;
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
	/** 登录的五个状态 0登录中 1 登录成功 2登录失败 3手机号未注册，跳转注册页面 4超时 5密码错误达上限去重置密码 **/
	public int loginStatus;
	/**
	 * 注册的三个状态 1----注册/重置 中 2----注册/重置 成功 3---注册/重置 失败 4---code=500 5---code=405
	 * 6----code=406 7----code=407 8---code=480 9---超时
	 **/
	public int registStatus;
	/** 自动登录的四个状态 1自动登录中 2自动登录成功 3自动登录失败 4自动登录超时 5密码错误 **/
	public int autoLoginStatus;
	/** 注销状态 **/
	public boolean loginoutStatus = false;
	/**
	 * 获取验证码的四个状态 0----获取中 1----获取成功 2----获取失败 3---code=201 4----code=500
	 * 5----code=405 6----code=440 7----code=480 8----code=470
	 **/
	public int identifyStatus;

	/** User管理类 **/
	public User mUser = null;
	/** 登录管理类 **/
	public UserLoginManage mLoginManage = null;
	/** 升级管理类 **/
	public IpcUpdateManage mIpcUpdateManage = null;
	/** 获取验证码管理类 **/
	public UserIdentifyManage mIdentifyManage = null;
	/** 注册/重置密码管理类 **/
	public UserRegistAndRepwdManage mRegistAndRepwdManage = null;
	/** 计时器管理类 **/
	public TimerManage mTimerManage = null;

	private HashMap<String, ILocationFn> mLocationHashMap = new HashMap<String, ILocationFn>();
	/** 未下载文件列表 */
	private List<String> mNoDownLoadFileList;
	/** 所有下载文件列表 */
	private List<String> mDownLoadFileList;

	/** ipc连接失败弹出的dailog */
	private CustomFormatDialog mconnection;
	/** 是否已经连接成功过 */
	private boolean isconnection = false;
	/** 后台标识 */
	private boolean isBackground = false;
	public long startTime = 0;
	public boolean autodownloadfile = false;
	/** 点击设置页版本检测标识 **/
	public boolean flag = false;
	/** SD卡无容量标识 */
	private boolean isSDCardFull = false;
	/** 文件下载中标识 */
	private boolean isDownloading = false;
	/** 下载列表个数 */
	private int downloadCount = 0;
	/** 极路客固件升级文件下载中的状态 **/
	public boolean mLoadStatus = false;
	/** 极路客固件升级文件下载中的进度 **/
	public int mLoadProgress = 0;
	/** 极路客升级成功的状态 **/
	public boolean updateSuccess = false;
	/** wifi连接状态 */
	public int mWiFiStatus = 0;
	/** 当前连接的Goluk设备 */
	public String mGolukName = "";

	private ArrayList<VideoFileInfo> fileList;

	private boolean mIsExit = true;

	static {
		System.loadLibrary("golukmobile");
	}

	public void setExit(boolean isExit) {
		mIsExit = isExit;
	}

	public boolean isExit() {
		return mIsExit;
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
			if (isExit()) {
				return;
			}
			switch (msg.what) {
			case 1001:
				tips();
				break;
			case 1002:
				backHomeDialog();// 弹出dialog 回到首页
				break;
			case 1003:
				isSDCardFull = false;
				isDownloading = false;
				downloadCount = 0;
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
		Fresco.initialize(this, ConfigConstants.getImagePipelineConfig(this));
		initRdCardSDK();
		initCachePath();
		// 实例化JIN接口,请求网络数据

		mGoluk = new GolukLogic();

		/**
		 * 自动登录、登录、注册、重置密码、注销的管理类
		 */
		mUser = new User(this);
		mLoginManage = new UserLoginManage(this);
		mIpcUpdateManage = new IpcUpdateManage(this);
		mIdentifyManage = new UserIdentifyManage(this);
		mRegistAndRepwdManage = new UserRegistAndRepwdManage(this);
		mTimerManage = new TimerManage(this);

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

		setExit(false);
	}

	public void destroyLogic() {
		if (null != mGoluk) {
			GolukDebugUtils.e("", "GolukApplication--------------------------destroy");
			mGoluk.GolukLogicDestroy();
			mGoluk = null;
		}
	}

	public void appFree() {
		mIpcIp = null;
		mContext = null;
		mPageSource = "";
		mMainActivity = null;
		isIpcLoginSuccess = false;
		isUserLoginSucess = false;
		mCCUrl = null;
		mCurrentUId = null;
		mCurrentAid = null;
		carrecorderCachePath = "";
		autoRecordFlag = false;
		motioncfg = null;
		wifiAp = null;
		mCurAddr = null;
		registStatus = 0;
		autoLoginStatus = 0;
		loginoutStatus = false;
		identifyStatus = 0;
		// mUser = null;
		// mLoginManage = null;
		// mIpcUpdateManage = null;
		// mIdentifyManage = null;
		// mRegistAndRepwdManage = null;
		mTimerManage.timerCancel();
		isconnection = false;
		isBackground = false;
		startTime = 0;
		autodownloadfile = false;
		flag = false;
		isSDCardFull = false;
		isDownloading = false;
		downloadCount = 0;
		mLoadStatus = false;
		mLoadProgress = 0;
		updateSuccess = false;
		mWiFiStatus = 0;
		mGolukName = "";
		if (null != fileList) {
			fileList.clear();
		}
		if (null != mNoDownLoadFileList) {
			mNoDownLoadFileList.clear();
		}
		if (null != mDownLoadFileList) {
			mDownLoadFileList.clear();
		}

	}

	/**
	 * 升级
	 */
	public void startUpgrade() {
		// app升级+ipc升级
		String vIpc = mSharedPreUtil.getIPCVersion();
		GolukDebugUtils.i("lily", "=====获取当前的vIpc=====" + vIpc);
		mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_AUTO, vIpc);
	}

	/**
	 * 创建行车记录仪缓冲路径
	 * 
	 * @author xuhw
	 * @date 2015年3月19日
	 */
	private void initCachePath() {
		carrecorderCachePath = Environment.getExternalStorageDirectory() + File.separator + "goluk" + File.separator
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
			CarRecorderManager.setConfiguration(new PreferencesReader(this, true).getConfig());
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
	public void setIpcLoginOut() {
		setIpcLoginState(false);
		if (null != mMainActivity) {
			mMainActivity.wiFiLinkStatus(3);
		}

		if (GlobalWindow.getInstance().isShow()) {
			mDownLoadFileList.clear();
			mNoDownLoadFileList.clear();
			GlobalWindow.getInstance().toFailed("视频传输失败");
			GolukDebugUtils.e("xuhw", "BBBBBB===1111==m=====setIpcLoginOut=");
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

	/**
	 * 本地视频上传回调
	 * 
	 * @param vid
	 *            ,视频ID
	 */
	public void localVideoUpLoadCallBack(int success, Object param1, Object param2) {
		if (mPageSource == "VideoEdit") {
			((VideoEditActivity) mContext).videoUploadCallBack(success, param1, param2);
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
		if (mPageSource == "VideoEdit") {
			((VideoEditActivity) mContext).videoShareCallBack(success, data);
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
				double filesize = json.optDouble("size");
				GolukDebugUtils.e("", "调用ipc视频下载接口---ipcVideoSingleQueryCallBack---downloadFile---" + fileName);
				int type = json.getInt("type");
				String savePath = "";
				String configPath = "";

				if (IPCManagerFn.TYPE_SHORTCUT == type) {
					savePath = mVideoSavePath + "wonderful/";
					configPath = savePath + "wonderful.txt";
				} else if (IPCManagerFn.TYPE_URGENT == type) {
					savePath = mVideoSavePath + "urgent/";
					configPath = savePath + "urgent.txt";
				} else {
					savePath = mVideoSavePath + "loop/";
					configPath = savePath + "loop.txt";
				}

				GolukDebugUtils.e("xuhw", "YYYYYY====start==VideoDownLoad===isSDCardFull=" + isSDCardFull
						+ "==isDownloading=" + isDownloading);
				if (isSDCardFull && !isDownloading) {
					return;
				}

				if (!GolukUtils.checkSDStorageCapacity(filesize)) {
					isSDCardFull = true;

					if (!mDownLoadFileList.contains(fileName)) {
						mDownLoadFileList.add(fileName);
					}

					if (GlobalWindow.getInstance().isShow()) {
						GlobalWindow.getInstance().updateText(
								"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size() + "/" + mDownLoadFileList.size());
						GolukDebugUtils.e("xuhw", "BBBBBB===2222=updateText=33333=");
					}

					if (!isDownloading) {
						sdCardFull();
						mHandler.sendEmptyMessageDelayed(1003, 1000);
					}

					GolukDebugUtils.e("xuhw", "YYYYYY====start==VideoDownLoad=@@@@==isSDCardFull=" + isSDCardFull);

					return;
				}

				isDownloading = true;
				AssetsFileUtils.appendFileData(FileUtils.libToJavaPath(configPath), fileName + ",");

				// 调用下载视频接口
				downloadCount++;
				boolean a = mIPCControlManager.downloadFile(fileName, "videodownload", savePath, time);
				GolukDebugUtils.e("xuhw", "YYYYYY====start==VideoDownLoad===flag=" + a + "===data=" + data);
				// 下载视频第一帧截图
				String imgFileName = fileName.replace("mp4", "jpg");
				String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
				File file = new File(filePath + File.separator + fileName);
				if (!file.exists()) {
					boolean img = mIPCControlManager.downloadFile(imgFileName, "imgdownload",
							FileUtils.javaToLibPath(filePath), time);
					GolukDebugUtils.e("xuhw", "YYYYYY====start==imgdownload===flag=" + img + "===imgFileName="
							+ imgFileName);
				}

				if (!mDownLoadFileList.contains(fileName)) {
					mDownLoadFileList.add(fileName);
				}

				if (!isBackground) {
					if (!GlobalWindow.getInstance().isShow()) {
						GolukDebugUtils.e("xuhw", "YYYYYY======1111111111=========");
						GlobalWindow.getInstance().createVideoUploadWindow(
								"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size() + "/" + mDownLoadFileList.size());
						GolukDebugUtils.e("xuhw",
								"BBBBBB===2222=updateText=4444==nosize==" + mNoDownLoadFileList.size());
					} else {
						GolukDebugUtils.e("xuhw", "YYYYYY======22222=========");
						GlobalWindow.getInstance().updateText(
								"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size() + "/" + mDownLoadFileList.size());
						GolukDebugUtils.e("xuhw",
								"BBBBBB===2222=updateText=55555=====nosize==" + mNoDownLoadFileList.size());
					}
				}
			} catch (Exception e) {
				GolukDebugUtils.e("", "解析视频下载JSON数据错误");
				e.printStackTrace();
			}
		}
	}

	/**
	 * sd卡满后停止下载并显示提示
	 * 
	 * @author xuhw
	 * @date 2015年6月11日
	 */
	private void sdCardFull() {
		if (mDownLoadFileList.size() > 0) {
			mDownLoadFileList.clear();
			mNoDownLoadFileList.clear();
		}

		mIPCControlManager.stopDownloadFile();

		if (!GlobalWindow.getInstance().isShow()) {
			GlobalWindow.getInstance().createVideoUploadWindow("视频传输取消");
		}
		GlobalWindow.getInstance().toFailed("视频传输取消");
		GolukUtils.showToast(mContext, "剩余空间不足");
	}

	/**
	 * 重置sd卡存储容量检查状态
	 * 
	 * @author xuhw
	 * @date 2015年6月11日
	 */
	private void resetSDCheckState() {
		downloadCount--;
		if (downloadCount <= 0) {
			downloadCount = 0;
		}

		GolukDebugUtils.e("xuhw", "YYYYYYY===resetSDCheckState==downloadCount=" + downloadCount);

		if (downloadCount > 0) {
			return;
		}

		if (isSDCardFull) {
			sdCardFull();
			isSDCardFull = false;
			isDownloading = false;
			downloadCount = 0;
		}
	}

	/**
	 * ipc视频下载回调函数
	 * 
	 * @param success
	 * @param data
	 * @author chenxy
	 */
	List<String> freeList = new ArrayList<String>();

	public void ipcVideoDownLoadCallBack(int success, String data) {
		freeList.clear();
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
					isDownloading = true;

					if (!mNoDownLoadFileList.contains(filename)) {
						mNoDownLoadFileList.add(filename);
					}
					if (!mDownLoadFileList.contains(filename)) {
						mDownLoadFileList.add(filename);
					}

					for (int i = 0; i < mNoDownLoadFileList.size(); i++) {
						String name = mNoDownLoadFileList.get(i);
						if (!mDownLoadFileList.contains(name)) {
							freeList.add(name);
						}
					}

					for (String name : freeList) {
						mNoDownLoadFileList.remove(name);
					}

					if (!isBackground) {
						if (GlobalWindow.getInstance().isShow()) {
							GlobalWindow.getInstance().refreshPercent(percent);
							GlobalWindow.getInstance().updateText(
									"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size() + "/" + mDownLoadFileList.size());
							GolukDebugUtils.e("xuhw", "BBBBBB===2222=updateText=11111=");
						} else {
							GlobalWindow.getInstance().createVideoUploadWindow(
									"正在从Goluk中传输视频到手机" + mNoDownLoadFileList.size() + "/" + mDownLoadFileList.size());
							GolukDebugUtils.e("xuhw", "BBBBBB===2222=updateText=22222=");
						}
					} else {
						if (GlobalWindow.getInstance().isShow()) {
							GlobalWindow.getInstance().dimissGlobalWindow();
							GolukDebugUtils.e("xuhw", "BBBBBB===1111==m=====isBackground=");
						}
					}

				} else if (0 == success) {
					// 下载完成
					if (null != mMainActivity) {
						// {"filename":"WND1_150402183837_0012.mp4",
						// "tag":"videodownload"}
						// 地图大头针图片
						GolukDebugUtils.e("", "视频下载完成---ipcVideoDownLoadCallBack---" + data);
						GolukDebugUtils.e("xuhw", "YYYYYY======VideoDownLoad=====data=" + data);
						mMainActivity.videoAnalyzeComplete(data);
					}

					try {
						JSONObject json = new JSONObject(data);
						String filename = json.optString("filename");
						if (mDownLoadFileList.contains(filename)) {
							if (!mNoDownLoadFileList.contains(filename)) {
								mNoDownLoadFileList.add(filename);
							}
						}
					} catch (Exception e) {
					}

					if (checkDownloadCompleteState()) {
						autodownloadfile = false;
						mDownLoadFileList.clear();
						mNoDownLoadFileList.clear();
						GlobalWindow.getInstance().topWindowSucess("视频传输完成");
					}

					resetSDCheckState();
				} else {
					resetSDCheckState();
					GolukDebugUtils.e("xuhw", "YYYYYY=＠＠＠＠===download==fail===success=" + success + "==data=" + data);
					JSONObject json = new JSONObject(data);
					String filename = json.optString("filename");

					if (mDownLoadFileList.contains(filename)) {
						if (!mNoDownLoadFileList.contains(filename)) {
							mNoDownLoadFileList.add(filename);
						}
					}

					GolukDebugUtils.e("xuhw", "BBBBBBB=======down==fail====" + mNoDownLoadFileList.size());
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
	public void pageNotifyCallBack(int type, int success, Object param1, Object param2) {
		GolukDebugUtils.e("", "chxy send pageNotifyCallBack--" + "type:" + type + ",success:" + success + ",param1:"
				+ param1 + ",param2:" + param2);

		if (this.isExit()) {
			return;
		}

		switch (type) {
		case PageType_UploadVideo:
			// 本地视频编辑页面,点击下一步,在上传页面上传本地视频回调
			localVideoUpLoadCallBack(success, param1, param2);
			break;
		case 2:
			// 本地视频分享链接请求回调
			localVideoShareCallBack(success, String.valueOf(param2));
			break;
		case 7:
			// 地图大头针数据
			if (null != mContext) {
				if (mContext instanceof LiveActivity) {
					// 为了更新直播界面的别人的位置信息
					((LiveActivity) mContext).pointDataCallback(success, param2);
				} else if (mContext instanceof VideoCategoryActivity) {
					((VideoCategoryActivity) mContext).pointDataCallback(success, param2);
				}
			}
			break;
		case 8:
			// 直播大头针图片下载完成
			if (mContext instanceof VideoCategoryActivity) {
				((VideoCategoryActivity) mContext).downloadBubbleImageCallBack(success, param2);
			}
			break;
		case 9:
			GolukDebugUtils.e(null, "jyf----20150406----application----999999999999---- : ");
			if (mPageSource == "LiveVideo") {
				GolukDebugUtils.e("", "pageNotifyCallBack---直播视频数据--" + String.valueOf(param2));
				if (mContext instanceof LiveActivity) {
					((LiveActivity) mContext).LiveVideoDataCallBack(success, param2);
				}
			}
			break;
		// 登陆
		case PageType_Login:
			// 登录
			if (mPageSource != "UserIdentify") {
				mLoginManage.loginCallBack(success, param1, param2);
			} else {
				((UserIdentifyActivity) mContext).registLoginCallBack(success, param2);
			}
			parseLoginData(success, param2);

			break;
		// 自动登录
		case PageType_AutoLogin:
			mUser.initAutoLoginCallback(success, param1, param2);
			parseLoginData(success, param2);
			break;
		// 验证码PageType_GetVCode
		case PageType_GetVCode:
			// 注册获取验证码
			mIdentifyManage.getIdentifyCallback(success, param1, param2);
			break;
		// 注册PageType_Register
		case PageType_Register:
			mRegistAndRepwdManage.registAndRepwdCallback(success, param1, param2);
			break;
		// 重置密码PageType_ModifyPwd
		case PageType_ModifyPwd:
			mRegistAndRepwdManage.registAndRepwdCallback(success, param1, param2);
			break;

		case PageType_ModifyNickName:
			if (mPageSource == "UserPersonalName") {
				((UserPersonalNameActivity) mContext).saveNameCallBack(success, param2);
			}
			break;
		case  PageType_ModifySignature:
			if (mPageSource == "UserPersonalSign") {
				((UserPersonalSignActivity) mContext).saveSignCallBack(success, param2);
			}
			break;
		case PageType_LiveStart:
			// 获取直播信息成功
			if (null != mContext && mContext instanceof LiveActivity) {
				((LiveActivity) mContext).callBack_LiveLookStart(true, success, param1, param2);
			}

			break;
		// 注销
		case PageType_SignOut:
			if (mPageSource == "UserSetup") {
				((UserSetupActivity) mContext).getLogintoutCallback(success, param2);
			}
			break;
		// APP升级+IPC升级检测
		case PageType_CheckUpgrade:
			mIpcUpdateManage.requestInfoCallback(success, param1, param2);
			break;
		// ipc升级文件下载
//		case PageType_CommDownloadFile:
//			mIpcUpdateManage.downloadCallback(success, param1, param2);
//			break;
		case PageType_DownloadIPCFile:
			mIpcUpdateManage.downloadCallback(success, param1, param2);
			break;
		// 意见反馈
		case PageType_FeedBack:
			if (mPageSource == "UserOpinion") {
				((UserOpinionActivity) mContext).requestOpinionCallback(success, param1, param2);
			}
			break;
		case PageType_PushReg:
			// token上传回调
			GolukNotification.getInstance().getXg().golukServerRegisterCallBack(success, param1, param2);
			break;
		case PageType_GetPushCfg:
		case PageType_SetPushCfg:
			if (null != mContext && mContext instanceof PushSettingActivity) {
				((PushSettingActivity) mContext).page_CallBack(type, success, param1, param2);
			}
			break;
		case PageType_ModifyHeadPic:
			if (mContext instanceof ImageClipActivity) {
				((ImageClipActivity) mContext).pageNotifyCallBack(type, success, param1, param2);
			}
			
			if (mContext instanceof UserPersonalHeadActivity) {
				((UserPersonalHeadActivity) mContext).pageNotifyCallBack(type, success, param1, param2);
			}
			break;
		}
	}

	public boolean isNeedCheckLive = false;
	private boolean isCallContinue = false;
	public boolean isCheckContinuteLiveFinish = false;
	private final int CONTINUTE_TIME_OUT = 15 * 1000;

	// 显示
	public void showContinuteLive() {
		if (null == mSharedPreUtil) {
			return;
		}
		if (mSharedPreUtil.getIsLiveNormalExit()) {
			isCheckContinuteLiveFinish = true;
			// 不需要续直播
			return;
		}
		if (System.currentTimeMillis() - startTime > CONTINUTE_TIME_OUT) {
			// 超时
			isCheckContinuteLiveFinish = true;
			return;
		}
		if (isCheckContinuteLiveFinish) {
			// 已经完成
			return;
		}
		if (!isIpcLoginSuccess || !isUserLoginSucess) {
			return;
		}

		if (isCallContinue) {
			return;
		}
		isCallContinue = true;
		GolukDebugUtils
				.e(null, "jyf----20150406----showContinuteLive----mApp :" + mSharedPreUtil.getIsLiveNormalExit());

		if (mContext instanceof MainActivity) {
			GolukDebugUtils.e(null, "jyf----20150406----showContinuteLive----mApp2222 :");
			isNeedCheckLive = false;
			isCheckContinuteLiveFinish = true;
			((MainActivity) mContext).showContinuteLive();
		} else {
			GolukDebugUtils.e(null, "jyf----20150406----showContinuteLive----mApp33333 :");
			isNeedCheckLive = true;
		}

		isCallContinue = false;
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
			GolukDebugUtils.e(null, "jyf---------GolukApplication---------mCCurl:" + mCCUrl + " uid:" + mCurrentUId
					+ " aid:" + mCurrentAid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 设置连接状态
	private void setIpcLoginState(boolean isSucess) {
		isIpcLoginSuccess = isSucess;
		if (isSucess) {
			showContinuteLive();
		}
	}

	// VDCP 连接状态 回调
	private void IPC_VDCP_Connect_CallBack(int msg, int param1, Object param2) {
		// 如果不是连接成功,都标识为失败
		switch (msg) {
		case ConnectionStateMsg_Idle:
			setIpcLoginState(false);
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
			setIpcLoginState(false);
			ipcDisconnect();
			// 已经连接成功过
			if (isconnection) {
				connectionDialog();
			}
			if (null != mMainActivity) {
				mMainActivity.wiFiLinkStatus(1);
			}
			break;
		case ConnectionStateMsg_Connected:
			// 只是,ipc信号连接了,初始化的东西还没完成,所以要等到ipc初始化成功,才能把isIpcLoginSuccess=true
			break;
		case ConnectionStateMsg_DisConnected:
			setIpcLoginState(false);
			ipcDisconnect();
			// 已经连接成功过
			if (isconnection) {
				connectionDialog();
			}
			if (null != mMainActivity) {
				mMainActivity.wiFiLinkStatus(3);
			}
			// 如果在wifi连接页面,通知连接成功
			if (mPageSource == "WiFiLinkList") {
				((WiFiLinkListActivity) mContext).ipcLinkFailedCallBack();
			}
			break;
		}
	}

	private void IPC_VDCP_Command_Init_CallBack(int msg, int param1, Object param2) {
		// msg = 0 初始化消息
		// param1 = 0 成功 | 失败
		if (0 == param1) {
			// 如果在wifi连接页面,通知连接成功
			if (mPageSource == "WiFiLinkList") {
				((WiFiLinkListActivity) mContext).ipcLinkedCallBack();
			}
			// 如果在wifi连接页面,通知连接成功
			if (mPageSource.equals("WiFiLinkBindAll")) {
				((WiFiLinkCompleteActivity) mContext).ipcLinkWiFiCallBack();
			}
			SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
			boolean isbind = preferences.getBoolean("isbind", false);
			if (isbind) {
				GolukDebugUtils.e("", "=========IPC_VDCP_Command_Init_CallBack：" + param2);
				//保存ipc设备型号
				try {
					JSONObject json = new JSONObject((String)param2);
					mIPCControlManager.mProduceName = json.getString("productname");
					mSharedPreUtil.saveIpcModel(mIPCControlManager.mProduceName);
					GolukDebugUtils.e("", "=========IPC_VDCP_Command_Init_CallBack：" + mSharedPreUtil.getIpcModel());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// ipc控制初始化成功,可以看画面和拍摄8s视频
				setIpcLoginState(true);
				// 获取音视频配置信息
				getVideoEncodeCfg();
				// 发起获取自动循环录制状态
				// updateAutoRecordState();
				// 获取停车安防配置信息
				// updateMotionCfg();
				isconnection = true;// 连接成功
				if (null != PhotoAlbumActivity.mHandler) {
					PhotoAlbumActivity.mHandler.sendEmptyMessage(PhotoAlbumActivity.UPDATELOGINSTATE);
				}
				closeConnectionDialog();// 关闭连接的dialog
				boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCSystemTime();
				GolukDebugUtils.e("xuhw", "YYYYYYY========getIPCSystemTime=======a=" + a);

				// 获取ipc版本号
				boolean v = GolukApplication.getInstance().getIPCControlManager().getVersion();
				GolukDebugUtils.i("lily", v + "========getIPCControlManager=====getIPCVersion");

				queryNewFileList();
				if (null != mMainActivity) {
					mMainActivity.wiFiLinkStatus(2);
				}
			}
			GolukDebugUtils.e("", "IPC_TTTTTT=================Login Success===============");
		} else {
			setIpcLoginState(false);
			ipcDisconnect();
		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		// System.out.println("IPC_TTTTTT========event="+event+"===msg="+msg+"===param1="+param1+"=========param2="+param2);
		if (this.isExit()) {
			return;
		}
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			IPC_VDCP_Connect_CallBack(msg, param1, param2);
		}

		if (ENetTransEvent_IPC_VDCP_CommandResp == event) {
			switch (msg) {
			case IPC_VDCP_Msg_Init:
				IPC_VDCP_Command_Init_CallBack(msg, param1, param2);
				break;
			case IPC_VDCP_Msg_Query:
				// msg = 1000 多文件目录查询
				if (RESULE_SUCESS == param1) {
					GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==5555===stopDownloadList" + param2);
					if ("ipcfilemanager".equals(mPageSource)) {
						return;
					}
					GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==6666===stopDownloadList");
					if (TextUtils.isEmpty((String) param2)) {
						return;
					}
					GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==7777===stopDownloadList");
					fileList = IpcDataParser.parseMoreFile((String) param2);
					GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==8888===stopDownloadList===fileList.size()="
							+ fileList.size());
					mHandler.removeMessages(1001);
					mHandler.sendEmptyMessageDelayed(1001, 1000);
				}
				break;
			case IPC_VDCP_Msg_SingleQuery:
				// msg = 1001 单文件查询
				// 拍摄8秒视频成功之后,接口会自动调用查询这个文件,收到这个回调之后可以根据文件名去下载视频
				GolukDebugUtils.e("xuhw", "YYYYYY==@@@@==IPC_VDCP_Msg_SingleQuery==param1=" + param1 + "==param2="
						+ param2);
				ipcVideoSingleQueryCallBack(param1, (String) param2);
				break;
			case IPC_VDCP_Msg_Erase:
				// msg = 1002 删除文件
				break;
			case IPC_VDCP_Msg_TriggerRecord:
				// msg = 1003 请求紧急、精彩视频录制
				// 发送拍摄指令后,会立即收到视频文件名称的回调,暂时无用
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
				// msg = 1012 设置IPC系统WIFI配置
				// param1 = 0 成功 | 失败
				// 如果在wifi连接页面,通知设置成功
				if (mPageSource.equals("WiFiLinkBindAll")) {
					((WiFiLinkCompleteActivity) mContext).setIpcLinkWiFiCallBack(param1);
				} else if (mPageSource.equals("changePassword")) {
					((UserSetupChangeWifiActivity) mContext).setIpcLinkWiFiCallBack(param1);
				} else if ("Main".equals(mPageSource)) {
					((MainActivity) mContext).setIpcLinkWiFiCallBack(param1);
				}
				break;
			case IPC_VDCP_Msg_GetVedioEncodeCfg:
				if (param1 == RESULE_SUCESS) {
					VideoConfigState videocfg = IpcDataParser.parseVideoConfigState((String) param2);
					if (null != videocfg) {
						mVideoConfigState = videocfg;
					}
				}

				break;
			case IPC_VDCP_Msg_SetVedioEncodeCfg:
				if (param1 == RESULE_SUCESS) {
					getVideoEncodeCfg();
				}
				break;
			case IPC_VDCP_Msg_IPCKit:
				SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
				boolean isbind = preferences.getBoolean("isbind", false);
				if (!isbind) {
					return;
				}
				GolukDebugUtils.e("xuhw", "YYYYYY======IPC_VDCP_Msg_IPCKit=====param1=" + param1 + "===param2="
						+ param2);
				if (param1 == RESULE_SUCESS) {
					List<ExternalEventsDataInfo> kit = IpcDataParser.parseKitData((String) param2);
					if (kit.size() > 0) {
						for (int i = 0; i < kit.size(); i++) {
							ExternalEventsDataInfo info = kit.get(i);
							if (!mDownLoadFileList.contains(info.location)) {
								mDownLoadFileList.add(info.location);

								boolean flag = GolukApplication.getInstance().getIPCControlManager()
										.querySingleFile(info.location);
								GolukDebugUtils.e("xuhw", "YYYYYY=====querySingleFile=====type=" + info.type
										+ "==flag=" + flag);
							}
						}
					}
				}
				break;
			case IPC_VDCP_Msg_GetVersion:
				// {"product": 67698688, "model": "", "macid": "", "serial": "",
				// "version": "V1.4.21_tzz_vb_rootfs"}
				if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
					if (IPC_VDCP_Msg_GetVersion == msg) {
						if (param1 == RESULE_SUCESS) {
							// ipcConnect(param2);
							String str = (String) param2;
							if (TextUtils.isEmpty(str)) {
								return;
							}
							try {
								JSONObject json = new JSONObject(str);
								String ipcVersion = json.optString("version");
								GolukDebugUtils.i("lily", "=====保存当前的ipcVersion=====" + ipcVersion);
								// 保存ipc版本号
								mSharedPreUtil.saveIPCVersion(ipcVersion);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				break;
			case IPC_VDCP_Msg_GetTime:
				if (param1 == RESULE_SUCESS) {
					if (TextUtils.isEmpty((String) param2)) {
						return;
					}
					long curtime = IpcDataParser.parseIPCTime((String) param2);
					// 自动同步系统时间
					if (SettingUtils.getInstance().getBoolean("systemtime", true)) {
						long time = SettingUtils.getInstance().getLong("cursystemtime");
						GolukDebugUtils.e("xuhw", "YYYYYY===getIPCSystemTime==time=" + time + "=curtime=" + curtime);
						if (Math.abs(curtime - time) > 60) {// 60秒内不自动同步
							SettingUtils.getInstance().putLong("cursystemtime", curtime);
							boolean a = GolukApplication.getInstance().getIPCControlManager()
									.setIPCSystemTime(System.currentTimeMillis() / 1000);
							GolukDebugUtils.e("xuhw", "YYYYYY===========setIPCSystemTime===============a=" + a);
						}
					}
				}
				break;
			case IPC_VDCP_Msg_GetIdentity:
				IPC_CallBack_GetIdentity(msg, param1, param2);
				break;
			}
		}

		// IPC下载连接状态 event = 2
		if (ENetTransEvent_IPC_VDTP_ConnectState == event) {
			// msg = 1 | 连接中 or msg = 2 | 连接成功
			// 当前不需要处理这些状态
			if (ConnectionStateMsg_DisConnected == msg) {
				if (mDownLoadFileList.size() > 0) {
					mDownLoadFileList.clear();
					mNoDownLoadFileList.clear();
					if (GlobalWindow.getInstance().isShow()) {
						GlobalWindow.getInstance().dimissGlobalWindow();
					}
				}
			}
		}

		// IPC下载结果应答,开始下载视频文件 event = 3
		if (ENetTransEvent_IPC_VDTP_Resp == event) {
			switch (msg) {
			case IPC_VDTP_Msg_File:
				// 文件传输中消息 msg = 0
				// param1 = 0,下载完成
				// param1 = 1,下载中
				GolukDebugUtils.e("xuhw", "YYYYYY==@@@@@==IPC_VDTP_Msg_File===param1=" + param1);
				ipcVideoDownLoadCallBack(param1, (String) param2);
				break;
			}
		}
	}

	private void IPC_CallBack_GetIdentity(int msg, int param1, Object param2) {
		if (param1 == RESULE_SUCESS) {
			final IPCIdentityState mVersionState = IpcDataParser.parseVersionState((String) param2);
			if (null != mVersionState && null != mIPCControlManager) {
				mIPCControlManager.mDeviceSn = mVersionState.name;
				mIPCControlManager.reportBindMsg();
			}
		}
	}

	@Override
	public void TalkNotifyCallBack(int type, String data) {
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
					boolean flag = GolukApplication.getInstance().getIPCControlManager().getVideoEncodeCfg(0);
					System.out.println("YYY============getVideoEncodeCfg=========flag=" + flag);
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
		// 定位回调
		if (null == mLocationHashMap) {
			return;
		}

		BaiduPosition location = JsonUtil.parseLocatoinJson(locationJson);
		if (null != location) {
			LngLat.lat = location.rawLat;
			LngLat.lng = location.rawLon;
			LngLat.radius = location.radius;
		}

		Iterator<Entry<String, ILocationFn>> it = mLocationHashMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, ILocationFn> entry = it.next();
			entry.getValue().LocationCallBack(locationJson);
		}
	}

	/**
	 * 判断是否可以同步5个最新的视频
	 * 
	 * @return true/false 可以/不可以
	 * @author jyf
	 */
	private boolean isCanQueryNewFile() {
		if (!SettingUtils.getInstance().getBoolean(UserSetupActivity.AUTO_SWITCH, true)) {
			return false;
		}
		SharedPreferences preferences = getSharedPreferences("ipc_wifi_bind", MODE_PRIVATE);
		boolean isbind = preferences.getBoolean("isbind", false);
		if (!isbind) {
			return false;
		}
		if ("carrecorder".equals(mPageSource)) {
			return false;
		}
		if (!isIpcLoginSuccess) {
			return false;
		}
		if (mDownLoadFileList.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 查询新文件列表（最多10条）
	 * 
	 * @author xuhw
	 * @date 2015年4月24日
	 */
	public void queryNewFileList() {
		if (!isCanQueryNewFile()) {
			// 不允许同步视频
			return;
		}
		long starttime = SettingUtils.getInstance().getLong("downloadfiletime", 0);
		GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==4444===stopDownloadList" + starttime);
		mIPCControlManager.queryFileListInfo(6, 5, starttime, 2147483647);
	}

	/**
	 * 通知IPC同步文件
	 * 
	 * @author xuhw
	 * @date 2015年5月19日
	 */
	public void stopDownloadList() {
		GolukDebugUtils.e("xuhw", "BBBBBB===0000==m=====stopDownloadList=" + autodownloadfile);
		if (autodownloadfile) {
			autodownloadfile = false;
			if (mDownLoadFileList.size() > 0) {
				mDownLoadFileList.clear();
				mNoDownLoadFileList.clear();
				if (GlobalWindow.getInstance().isShow()) {
					GlobalWindow.getInstance().dimissGlobalWindow();
					GolukDebugUtils.e("xuhw", "BBBBBB===1111==m=====stopDownloadList=");
				}
			}
			GolukDebugUtils.e("xuhw", "BBBBBB===2222==m=====stopDownloadList=");
			mIPCControlManager.stopDownloadFile();
			GolukDebugUtils.e("xuhw", "BBBBBB===3333==m=====stopDownloadList=");
		}
	}

	/**
	 * IPC断开连接处理
	 * 
	 * @author xuhw
	 * @date 2015年4月24日
	 */
	private void ipcDisconnect() {
		if (null != PhotoAlbumActivity.mHandler) {
			PhotoAlbumActivity.mHandler.sendEmptyMessage(PhotoAlbumActivity.UPDATELOGINSTATE);
		}

		if (mDownLoadFileList.size() > 0) {
			mDownLoadFileList.clear();
			mNoDownLoadFileList.clear();
			if (GlobalWindow.getInstance().isShow()) {
				GlobalWindow.getInstance().dimissGlobalWindow();
				GolukDebugUtils.e("xuhw", "BBBBBB===1111==m=====ipcDisconnect=" + mNoDownLoadFileList.size());
			}
		}
	}

	private void tips() {
		GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==fuck===stopDownloadList");
		if ("carrecorder".equals(mPageSource)) {
			return;
		}
		if (null == fileList || fileList.size() <= 0) {
			return;
		}

		GolukDebugUtils.e("xuhw",
				"BBBB=====stopDownloadList==fuck===stopDownloadList==fileList.size()=" + fileList.size());
		if (mContext instanceof Activity) {
			GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==fuck");
			Activity a = (Activity) mContext;
			if (!a.isFinishing()) {
				GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==fuck");
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
						if (mDownLoadFileList.contains(info.location)) {
							mDownLoadFileList.remove(info.location);
						}
						if (mNoDownLoadFileList.contains(info.location)) {
							mNoDownLoadFileList.remove(info.location);
						}
						GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==fuck===mNoDownLoadFileList="
								+ mNoDownLoadFileList.size() + "==mDownLoadFileList=" + mDownLoadFileList.size());
					} else {
						if (!mDownLoadFileList.contains(info.location)) {
							mDownLoadFileList.add(info.location);
						}
					}

				}
				GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==fuck");
				if (size <= 0) {
					return;
				}

				GolukDebugUtils.e("xuhw",
						"BBBB=====stopDownloadList==11111===stopDownloadList" + mDownLoadFileList.size()
								+ mDownLoadFileList);
				List<String> order = FileInfoManagerUtils.bubbleSort(mDownLoadFileList, true);
				mDownLoadFileList.clear();
				mDownLoadFileList.addAll(order);
				GolukDebugUtils.e("xuhw",
						"BBBB=====stopDownloadList==22222===stopDownloadList" + mDownLoadFileList.size()
								+ mDownLoadFileList);
				if (mDownLoadFileList.size() > 0) {
					GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList=====stopDownloadList");
					autodownloadfile = true;
				}

				for (int i = 0; i < mDownLoadFileList.size(); i++) {
					String name = mDownLoadFileList.get(i);
					boolean flag = GolukApplication.getInstance().getIPCControlManager().querySingleFile(name);
					GolukDebugUtils.e("xuhw", "YYYYYY=====querySingleFile=====name=" + name + "==flag=" + flag);
				}
			}
		}
	}

	public void connectionDialog() {
		if (mconnection != null && mconnection.isShowing()) {
			return;
		}

		if (this.testActivity()) {
			Activity a = (Activity) mContext;
			if (!a.isFinishing()) {
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
	 * 
	 * @Title: testActivity
	 * @Description:
	 * @return boolean
	 * @author 曾浩
	 * @throws
	 */
	public boolean testActivity() {
		if (mContext instanceof PhotoAlbumActivity) {
			return true;
		} else {
			return false;
		}

	}

	private CustomDialog backHomedialog;

	public void backHomeDialog() {
		isconnection = false;
		closeConnectionDialog();// 关闭上一个dialog
		if (backHomedialog != null && backHomedialog.isShowing()) {
			return;
		} else {
			Activity a = (Activity) mContext;
			if (!a.isFinishing()) {
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
					if (!((Activity) mContext).isFinishing()) {
						backHomedialog.show();
					}
				}
			}
		}
	}

	/**
	 * 获取当前登录用户的信息,　未登录則返回NULL
	 * 
	 * @return 用户信息类 UserInfo
	 * @author jyf
	 * @date 2015年8月7日
	 */
	public UserInfo getMyInfo() {
		try {
			if (!isUserLoginSucess) {
				return null;
			}
			UserInfo myInfo = null;
			String userInfo = mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
					IPageNotifyFn.PageType_GetUserInfo_Get, "");
			if (null != userInfo) {
				myInfo = JsonUtil.parseSingleUserInfoJson(new JSONObject(userInfo));
			}
			return myInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void closeConnectionDialog() {
		mHandler.removeMessages(1002);
		if (mconnection != null) {
			if (mconnection.isShowing()) {
				mconnection.dismiss();
			}
		}

	}

	/**
	 * 获取下载列表
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015年5月13日
	 */
	public List<String> getDownLoadList() {
		return mDownLoadFileList;
	}

	public void setIsBackgroundState(boolean flag) {
		isBackground = flag;
	}

	public boolean getIsBackgroundState() {
		return isBackground;
	}

	// isReal 是否立刻上传
	public void uploadMsg(String msg, boolean isReal) {
		if (null == mGoluk || null == msg || "".equals(msg)) {
			return;
		}

		GolukDebugUtils.e("", "jyf------logReport-------GolukApplicaiton-------: " + msg);

		final int which = isReal ? IMessageReportFn.REPORT_CMD_LOG_REPORT_REAL
				: IMessageReportFn.REPORT_CMD_LOG_REPORT_HTTP;

		mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_MessageReport, which, msg);
	}

	// 查看他人的直播
	public void startLiveLook(UserInfo userInfo) {
		GolukDebugUtils.e("", "jyf-----click------666666");
		if (null == userInfo) {
			return;
		}
		// 跳转看他人界面
		Intent intent = new Intent(mContext, LiveActivity.class);
		intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
		intent.putExtra(LiveActivity.KEY_GROUPID, "");
		intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
		intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
		intent.putExtra(LiveActivity.KEY_USERINFO, userInfo);
		mContext.startActivity(intent);
		GolukDebugUtils.e(null, "jyf----20150406----MainActivity----startLiveLook");
	}

}
