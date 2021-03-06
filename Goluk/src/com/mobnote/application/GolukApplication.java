package com.mobnote.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.BaiduPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.tiros.api.Const;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.baidu.BaiduLocation;
import cn.com.tiros.debug.GolukDebugUtils;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.SDKInitializer;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventIpcConnState;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.eventbus.EventPhotoUpdateLoginState;
import com.mobnote.eventbus.EventUserLoginRet;
import com.mobnote.golukmain.ImageClipActivity;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.PushSettingActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpinionActivity;
import com.mobnote.golukmain.UserPersonalHeadActivity;
import com.mobnote.golukmain.UserPersonalNameActivity;
import com.mobnote.golukmain.UserPersonalSignActivity;
import com.mobnote.golukmain.UserSetupActivity;
import com.mobnote.golukmain.UserSetupChangeWifiActivity;
import com.mobnote.golukmain.adas.AdasConfigParamterBean;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.IpcDataParser;
import com.mobnote.golukmain.carrecorder.PreferencesReader;
import com.mobnote.golukmain.carrecorder.entity.ExternalEventsDataInfo;
import com.mobnote.golukmain.carrecorder.entity.IPCIdentityState;
import com.mobnote.golukmain.carrecorder.entity.VideoConfigState;
import com.mobnote.golukmain.carrecorder.entity.VideoFileInfo;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.fileinfo.VideoFileInfoBean;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.internation.login.CountryBean;
import com.mobnote.golukmain.internation.login.GolukMobUtils;
import com.mobnote.golukmain.live.LiveActivity;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.livevideo.AbstractLiveActivity;
import com.mobnote.golukmain.livevideo.BaidumapLiveActivity;
import com.mobnote.golukmain.livevideo.GooglemapLiveActivity;
import com.mobnote.golukmain.livevideo.LiveOperateVdcp;
import com.mobnote.golukmain.livevideo.VdcpLiveBean;
import com.mobnote.golukmain.thirdshare.GolukUmConfig;
import com.mobnote.golukmain.userlogin.UserData;
import com.mobnote.golukmain.videosuqare.VideoCategoryActivity;
import com.mobnote.golukmain.videosuqare.VideoSquareManager;
import com.mobnote.golukmain.wifibind.IpcConnSuccessInfo;
import com.mobnote.golukmain.wifibind.WiFiLinkCompleteActivity;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifidatacenter.JsonWifiBindManager;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifimanage.WifiApAdmin;
import com.mobnote.golukmain.xdpush.GolukNotification;
import com.mobnote.map.LngLat;
import com.mobnote.user.IpcUpdateManage;
import com.mobnote.user.TimerManage;
import com.mobnote.user.User;
import com.mobnote.user.UserIdentifyManage;
import com.mobnote.user.UserLoginManage;
import com.mobnote.user.UserRegistAndRepwdManage;
import com.mobnote.util.GolukFastJsonUtil;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.SortByDate;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;

import de.greenrobot.event.EventBus;

public class GolukApplication extends Application implements IPageNotifyFn, IPCManagerFn, ITalkFn, ILocationFn {
	/** JIN????????? */
	public GolukLogic mGoluk = null;
	/** ip?????? */
	public static String mIpcIp = null;
	/** ??????????????? */
	private Context mContext = null;
	/** ????????????,????????????activity */
	private String mPageSource = "";
	/** ??????activity */
	public static MainActivity mMainActivity = null;
	/** ?????????????????? fs1:??????->sd???/goluk?????? */
	private String mVideoSavePath = "fs1:/video/";

	private static GolukApplication instance = null;
	public IPCControlManager mIPCControlManager = null;
	private VideoSquareManager mVideoSquareManager = null;

	/** ????????????????????????, ??????????????????????????????????????????????????? */
	private boolean isBinding = false;
	/** ??????IPC?????????????????? */
	public boolean isIpcLoginSuccess = false;
	/** ????????????IPC???????????? */
	public boolean isIpcConnSuccess = false;
	/** ????????????????????????????????????????????? */
	public boolean isUserLoginSucess = false;
	/** CC?????????????????? */
	public String mCCUrl = null;
	/** ?????????????????????UID */
	public String mCurrentUId = null;
	/** ?????????????????????Aid */
	public String mCurrentAid = null;

	/** ??????????????????????????? **/
	public String mCurrentPhoneNum = null;
	/** ??????????????????????????? */
	private String carrecorderCachePath = "";
	/** ????????????????????? */
	private VideoConfigState mVideoConfigState = null;
	/** ?????????????????????????????? */
	private boolean autoRecordFlag = false;
	/** ?????????????????? */
	private int[] motioncfg;

	private WifiApAdmin wifiAp;
	/** ???????????? */
	public String mCurAddr = null;
	/** ????????????????????? 0????????? 1 ???????????? 2???????????? 3??????????????????????????????????????? 4?????? 5???????????????????????????????????? **/
	public int loginStatus;
	/**
	 * ????????????????????? 1----??????/?????? ??? 2----??????/?????? ?????? 3---??????/?????? ?????? 4---code=500 5---code=405
	 * 6----code=406 7----code=407 8---code=480 9---??????
	 **/
	public int registStatus;
	/** ??????????????????????????? 1??????????????? 2?????????????????? 3?????????????????? 4?????????????????? 5???????????? **/
	public int autoLoginStatus;
	/** ???????????? **/
	public boolean loginoutStatus = false;
	/**
	 * ?????????????????????????????? 0----????????? 1----???????????? 2----???????????? 3---code=201 4----code=500
	 * 5----code=405 6----code=440 7----code=480 8----code=470
	 **/
	public int identifyStatus;

	/** User????????? **/
	public User mUser = null;
	/** ??????????????? **/
	public UserLoginManage mLoginManage = null;
	/** ??????????????? **/
	public IpcUpdateManage mIpcUpdateManage = null;
	/** ???????????????????????? **/
	public UserIdentifyManage mIdentifyManage = null;
	/** ??????/????????????????????? **/
	public UserRegistAndRepwdManage mRegistAndRepwdManage = null;
	/** ?????????????????? **/
	public TimerManage mTimerManage = null;

	private HashMap<String, ILocationFn> mLocationHashMap = new HashMap<String, ILocationFn>();
	/** ????????????????????? */
	private List<String> mNoDownLoadFileList;
	/** ???????????????????????? */
	private List<String> mDownLoadFileList;

	/** ??????????????????????????? */
	public boolean isconnection = false;
	/** ???????????? */
	private boolean isBackground = false;
	public long startTime = 0;
	public boolean autodownloadfile = false;
	/** ????????????????????????????????? **/
	public boolean flag = false;
	/** SD?????????????????? */
	private boolean isSDCardFull = false;
	/** ????????????????????? */
	private boolean isDownloading = false;
	/** ?????????????????? */
	private int downloadCount = 0;
	/** ????????????????????????????????????????????? **/
	public boolean mLoadStatus = false;
	/** ????????????????????????????????????????????? **/
	public int mLoadProgress = 0;
	/** ?????????????????????????????? **/
	public boolean updateSuccess = false;
	/** wifi???????????? */
	public int mWiFiStatus = 0;

	private ArrayList<VideoFileInfo> fileList;

	private boolean mIsExit = true;
	/** T1??????????????????????????????????????? **/
	public int mT1RecAudioCfg = 1;

	/** ????????????????????? **/
	public CountryBean mLocationCityCode = null;
	/** ????????????????????? */
	public boolean isAlreadyLive = false;
	
	private boolean mIsQuery = false;

	private static final String SNAPSHOT_DIR = "fs1:/pic/";
	static {
//		System.loadLibrary("golukmobile");
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
		BaiduLocation.mServerFlag = isInteral();
		System.loadLibrary("golukmobile");

		instance = this;
		Const.setAppContext(this);

		if (isMainProcess()) {
			HttpManager.getInstance();
			SDKInitializer.initialize(this);
			// ????????????????????????????????????
			WifiBindDataCenter.getInstance().setAdatper(new JsonWifiBindManager());
			GolukVideoInfoDbManager.getInstance().initDb(this.getApplicationContext());
			GolukUmConfig.UmInit();

			GolukMobUtils.initMob(this);
		}

		// TODO ???????????????????????????????????????
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

	public void initLogic() {
		if (null != mGoluk) {
			return;
		}

		initRdCardSDK();
		initCachePath();
		// ?????????JIN??????,??????????????????

		mGoluk = new GolukLogic();

		/**
		 * ??????????????????????????????????????????????????????????????????
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
		// ????????????
		mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_HttpPage, this);
		// ???????????????????????????
		mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_Talk, this);
		// ??????????????????
		mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_Location, this);
		GlobalWindow.getInstance().setApplication(this);
		motioncfg = new int[2];
		mDownLoadFileList = new ArrayList<String>();
		mNoDownLoadFileList = new ArrayList<String>();

		setExit(false);
	}

	public void destroyLogic() {
		if (null != mGoluk) {
			mGoluk.GolukLogicDestroy();
			mGoluk = null;
		}
	}

	public void appFree() {
		mIpcIp = null;
		mContext = null;
		mPageSource = "";
		isBinding = false;
		mMainActivity = null;
		isIpcLoginSuccess = false;
		isIpcConnSuccess = false;
		isUserLoginSucess = false;
		mCCUrl = null;
		// mCurrentUId = null;
		mCurrentAid = null;
		mCurrentPhoneNum = null;
		carrecorderCachePath = "";
		autoRecordFlag = false;
		motioncfg = null;
		wifiAp = null;
		mCurAddr = null;
		registStatus = 0;
		autoLoginStatus = 0;
		loginoutStatus = false;
		identifyStatus = 0;
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
	 * ??????
	 */
	public void startUpgrade() {
		// app??????+ipc??????
		String vIpc = SharedPrefUtil.getIPCVersion();
		GolukDebugUtils.i("lily", "=====???????????????vIpc=====" + vIpc);
		mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_AUTO, vIpc);
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???3???19???
	 */
	private void initCachePath() {
		carrecorderCachePath = Environment.getExternalStorageDirectory() + File.separator + "goluk" + File.separator
				+ "goluk_carrecorder";
		GFileUtils.makedir(carrecorderCachePath);
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???3???19???
	 */
	public String getCarrecorderCachePath() {
		return this.carrecorderCachePath;
	}

	/**
	 * ???????????????????????????
	 * 
	 * @param videocfg
	 * @author xuhw
	 * @date 2015???4???10???
	 */
	public void setVideoConfigState(VideoConfigState videocfg) {
		this.mVideoConfigState = videocfg;
	}

	/**
	 * ???????????????????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???4???10???
	 */
	public VideoConfigState getVideoConfigState() {
		return this.mVideoConfigState;
	}

	/**
	 * ??????T1??????????????????
	 * 
	 * @param state
	 */
	public void setT1VideoCfgState(int state) {
		this.mT1RecAudioCfg = state;
	}

	/**
	 * ??????T1??????????????????
	 * 
	 * @return
	 */
	public int getT1VideoCfgState() {
		return mT1RecAudioCfg;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param auto
	 * @author xuhw
	 * @date 2015???4???10???
	 */
	public void setAutoRecordState(boolean auto) {
		this.autoRecordFlag = auto;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???4???10???
	 */
	public boolean getAutoRecordState() {
		return this.autoRecordFlag;
	}

	/**
	 * ????????????????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???4???10???
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
	 * ???????????????SDK
	 * 
	 * @author xuhw
	 * @date 2015???3???21???
	 */
	private void initRdCardSDK() {
		try {
			// ??????CarRecorderManager
			CarRecorderManager.initilize(this);
			// ??????????????????
			CarRecorderManager.setConfiguration(new PreferencesReader(this, true).getConfig());
			// ??????OSD
			// CarRecorderManager.registerOSDBuilder(RecordOSDBuilder.class);
			// ?????????????????????????????????
			// ????????????????????????????????????false????????????android4.3+ ???????????????
			// CarRecorderManager.enableComptibleMode(true);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (RecorderStateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ??????IPC???????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???3???21???
	 */
	public IPCControlManager getIPCControlManager() {
		return mIPCControlManager;
	}

	/**
	 * ???????????????????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???4???14???
	 */
	public VideoSquareManager getVideoSquareManager() {
		return mVideoSquareManager;
	}

	public static GolukApplication getInstance() {
		return instance;
	}

	/**
	 * ??????????????????
	 */
	public boolean isInteral() {
		if (null != this.getPackageName() && "cn.com.mobnote.golukmobile".equals(this.getPackageName())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ??????IPC????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???3???18???
	 */
	public boolean getIpcIsLogin() {
		return isIpcLoginSuccess;
	}

	/**
	 * ??????IPC????????????
	 */
	public void setIpcLoginOut() {
		setIpcLoginState(false);
		if (null != mMainActivity) {
			mMainActivity.wiFiLinkStatus(3);
		}

		if (GlobalWindow.getInstance().isShow()) {
			mDownLoadFileList.clear();
			mNoDownLoadFileList.clear();
			GlobalWindow.getInstance().toFailed(this.getResources().getString(R.string.str_video_transfer_fail));
			GolukDebugUtils.e("xuhw", "BBBBBB===1111==m=====setIpcLoginOut=");
		}
	}

	/**
	 * ???????????????
	 * 
	 * @param context
	 */
	public void setContext(Context context, String source) {
		this.mContext = context;
		this.mPageSource = source;

		// ??????MainActivity,????????????????????????????????????
		if (source == "Main") {
			mMainActivity = ((MainActivity) mContext);
		}
	}

	public Context getContext() {
		return this.mContext;
	}

	private String getSavePath(int type) {
		if (IPCManagerFn.TYPE_SHORTCUT == type) {
			return mVideoSavePath + "wonderful/";
		} else if (IPCManagerFn.TYPE_URGENT == type) {
			return mVideoSavePath + "urgent/";
		} else {
			return mVideoSavePath + "loop/";
		}
	}

	/**
	 * ipc????????????????????????????????????
	 * 
	 * @param success
	 * @param data
	 * @author chenxy
	 */
	public void ipcVideoSingleQueryCallBack(int success, String data) {
		if (0 != success) {
			return;
		}
		GolukDebugUtils.e("xuhw", "YYYYYY====start==VideoDownLoad===isSDCardFull=" + isSDCardFull + "==isDownloading="
				+ isDownloading);
		if (isSDCardFull && !isDownloading) {
			return;
		}
		GolukDebugUtils.e("", "ipcVideoSingleQueryCallBack------:  " + data);
		try {
			JSONObject json = new JSONObject(data);
			final String fileName = json.getString("location");
			final long time = json.optLong("time");
			final double filesize = json.optDouble("size");
			final int type = json.getInt("type");
			final String savePath = getSavePath(type);

			if (!GolukUtils.checkSDStorageCapacity(filesize)) {
				isSDCardFull = true;
				if (!mDownLoadFileList.contains(fileName)) {
					mDownLoadFileList.add(fileName);
				}
				if (GlobalWindow.getInstance().isShow()) {
					GlobalWindow.getInstance().updateText(
							this.getResources().getString(R.string.str_video_transfer_ongoing)
									+ mNoDownLoadFileList.size() + this.getResources().getString(R.string.str_slash)
									+ mDownLoadFileList.size());
				}
				if (!isDownloading) {
					sdCardFull();
					mHandler.sendEmptyMessageDelayed(1003, 1000);
				}
				return;
			}
			isDownloading = true;
			downloadCount++;
			// ??????????????????????????????
			VideoFileInfoBean bean = JsonUtil.jsonToVideoFileInfoBean(data, mIPCControlManager.mProduceName);
			GolukVideoInfoDbManager.getInstance().addVideoInfoData(bean);
			// ????????????????????????
			Log.i("download start", "download start");
			mIPCControlManager.downloadFile(fileName, "videodownload", savePath, time);
			// ???????????????????????????
			downLoadVideoThumbnail(fileName, time);
			if (!mDownLoadFileList.contains(fileName)) {
				mDownLoadFileList.add(fileName);
			}

			if (!isBackground) {
				final String showTxt = this.getResources().getString(R.string.str_video_transfer_ongoing)
						+ mNoDownLoadFileList.size() + this.getResources().getString(R.string.str_slash)
						+ mDownLoadFileList.size();
				if (!GlobalWindow.getInstance().isShow()) {
					GlobalWindow.getInstance().createVideoUploadWindow(showTxt);
				} else {
					GlobalWindow.getInstance().updateText(showTxt);
				}
			}
		} catch (Exception e) {
			GolukDebugUtils.e("", "??????????????????JSON????????????");
			e.printStackTrace();
		}
	}

	// ???????????????????????????
	private void downLoadVideoThumbnail(String videoFileName, long filetime) {
		final String imgFileName = videoFileName.replace("mp4", "jpg");
		final String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
		File file = new File(filePath + File.separator + imgFileName);
		if (!file.exists()) {
			mIPCControlManager.downloadFile(imgFileName, "imgdownload", FileUtils.javaToLibPath(filePath), filetime);
		}
	}

	/**
	 * sd????????????????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???6???11???
	 */
	private void sdCardFull() {
		if (mDownLoadFileList.size() > 0) {
			mDownLoadFileList.clear();
			mNoDownLoadFileList.clear();
		}

		mIPCControlManager.stopDownloadFile();

		if (!GlobalWindow.getInstance().isShow()) {
			GlobalWindow.getInstance().createVideoUploadWindow(
					this.getResources().getString(R.string.str_video_transfer_cancle));
		}
		GlobalWindow.getInstance().toFailed(this.getResources().getString(R.string.str_video_transfer_cancle));
		GolukUtils.showToast(getApplicationContext(), this.getResources().getString(R.string.str_no_space));
	}

	/**
	 * ??????sd???????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???6???11???
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
	 * ipc????????????????????????
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
			GolukDebugUtils.e("", "GolukApplication-----ipcVideoDownLoadCallback:  success:" + success + "  data:"
					+ data);
			JSONObject jsonobj = new JSONObject(data);
			String tag = jsonobj.optString("tag");
			if (tag.equals("videodownload")) {
				if (1 == success) {
					// ?????????
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
									this.getResources().getString(R.string.str_video_transfer_ongoing)
											+ mNoDownLoadFileList.size()
											+ this.getResources().getString(R.string.str_slash)
											+ mDownLoadFileList.size());
							GolukDebugUtils.e("xuhw", "BBBBBB===2222=updateText=11111=");
						} else {
							GlobalWindow.getInstance().createVideoUploadWindow(
									this.getResources().getString(R.string.str_video_transfer_ongoing)
											+ mNoDownLoadFileList.size()
											+ this.getResources().getString(R.string.str_slash)
											+ mDownLoadFileList.size());
							GolukDebugUtils.e("xuhw", "BBBBBB===2222=updateText=22222=");
						}
					} else {
						if (GlobalWindow.getInstance().isShow()) {
							GlobalWindow.getInstance().dimissGlobalWindow();
							GolukDebugUtils.e("xuhw", "BBBBBB===1111==m=====isBackground=");
						}
					}

				} else if (0 == success) {
					// ????????????
					if (null != mMainActivity) {
						// {"filename":"WND1_150402183837_0012.mp4",
						// "tag":"videodownload"}
						// ?????????????????????
						GolukDebugUtils.e("", "??????????????????---ipcVideoDownLoadCallBack---" + data);
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
						GlobalWindow.getInstance().topWindowSucess(
								this.getResources().getString(R.string.str_video_transfer_success));
					}

					resetSDCheckState();
				} else {
					resetSDCheckState();
					GolukDebugUtils.e("xuhw", "YYYYYY=????????????===download==fail===success=" + success + "==data=" + data);
					JSONObject json = new JSONObject(data);
					final String filename = json.optString("filename");

					if (mDownLoadFileList.contains(filename)) {
						if (!mNoDownLoadFileList.contains(filename)) {
							mNoDownLoadFileList.add(filename);
						}
					}
					// ????????????????????????????????????????????????
					GolukVideoInfoDbManager.getInstance().delVideoInfo(filename);

					GolukDebugUtils.e("xuhw", "BBBBBBB=======down==fail====" + mNoDownLoadFileList.size());
					if (checkDownloadCompleteState()) {
						mDownLoadFileList.clear();
						mNoDownLoadFileList.clear();
						GlobalWindow.getInstance().toFailed(
								this.getResources().getString(R.string.str_video_transfer_fail));
					}
				}
			} else if (tag.equals("imgdownload") && 0 == success) {
				// ????????????????????????????????????
				if (!GolukFileUtils.loadBoolean(GolukFileUtils.PROMOTION_AUTO_PHOTO, true)) {
					return;
				}

				String path = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
				try {
					JSONObject json = new JSONObject(data);
					String filename = json.optString("filename");
					MediaStore.Images.Media.insertImage(getContentResolver(), path + File.separator + filename,
							filename, "Goluk");
					// ????????????????????????
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
				} catch (Exception e) {
				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???4???23???
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
	 * ????????????????????????
	 */
	@Override
	public void pageNotifyCallBack(int type, int success, Object param1, Object param2) {
//		GolukDebugUtils.e("", "chxy send pageNotifyCallBack--" + "type:" + type + ",success:" + success + ",param1:"
//				+ param1 + ",param2:" + param2);

		if (this.isExit()) {
			return;
		}

		switch (type) {
		case 7:
			// ?????????????????????
			if (null != mContext) {
				if (mContext instanceof AbstractLiveActivity) {
					// ????????????????????????????????????????????????
					((AbstractLiveActivity) mContext).pointDataCallback(success, param2);
				} else if (mContext instanceof VideoCategoryActivity) {
					((VideoCategoryActivity) mContext).pointDataCallback(success, param2);
				}
			}
			break;
		case 8:
			// ?????????????????????????????????
			if (mContext instanceof VideoCategoryActivity) {
				((VideoCategoryActivity) mContext).downloadBubbleImageCallBack(success, param2);
			}
			break;
		case 9:
			GolukDebugUtils.e(null, "jyf----20150406----application----999999999999---- : ");
			if (mPageSource == "LiveVideo") {
				GolukDebugUtils.e("", "pageNotifyCallBack---??????????????????--" + String.valueOf(param2));
				if (mContext instanceof AbstractLiveActivity) {
					((AbstractLiveActivity) mContext).LiveVideoDataCallBack(success, param2);
				}
			}
			break;
		// ??????
		// case PageType_Login:
		// // ??????????????????
		// mUser.timerCancel();
		// // ??????
		// if (mPageSource != "UserIdentify") {
		// mLoginManage.loginCallBack(success, param1, param2);
		// } else {
		// ((UserIdentifyActivity) mContext).registLoginCallBack(success,
		// param2);
		// }
		// parseLoginData(success, param2);
		//
		// break;
		// // ???????????????
		// case PageType_OauthLogin:
		// // ??????????????????
		// mUser.timerCancel();
		// // ??????
		// mLoginManage.loginCallBack(success, param1, param2);
		// parseLoginData(success, param2);
		// break;
		// ????????????
		// case PageType_AutoLogin:
		// mUser.initAutoLoginCallback(success, param1, param2);
		// parseLoginData(success, param2);
		// break;
		// ?????????PageType_GetVCode
		case PageType_GetVCode:
			// ?????????????????????
			mIdentifyManage.getIdentifyCallback(success, param1, param2);
			break;
		// ??????PageType_Register
		case PageType_BindInfo:
			mRegistAndRepwdManage.bindPhoneNumCallback(success, param1, param2);
			break;
		case PageType_Register:
			mRegistAndRepwdManage.registAndRepwdCallback(success, param1, param2);
			break;
		// ????????????PageType_ModifyPwd
		case PageType_ModifyPwd:
			mRegistAndRepwdManage.registAndRepwdCallback(success, param1, param2);
			break;

//		case PageType_ModifyNickName:
//			if (mPageSource == "UserPersonalName") {
//				((UserPersonalNameActivity) mContext).saveNameCallBack(success, param2);
//			}
//			break;
//		case PageType_ModifySignature:
//			if (mPageSource == "UserPersonalSign") {
//				((UserPersonalSignActivity) mContext).saveSignCallBack(success, param2);
//			}
//			break;
		case PageType_LiveStart:
			// ????????????????????????
			if (null != mContext && mContext instanceof AbstractLiveActivity) {
				((AbstractLiveActivity) mContext).callBack_LiveLookStart(true, success, param1, param2);
			}

			break;
//		// ??????
//		case PageType_SignOut:
//			if (mPageSource == "UserSetup") {
//				((UserSetupActivity) mContext).getLogintoutCallback(success, param2);
//			}
//			break;
//		// APP??????+IPC????????????
		case PageType_CheckUpgrade:
			mIpcUpdateManage.requestInfoCallback(success, param1, param2);
			break;
		// ipc??????????????????
		// case PageType_CommDownloadFile:
		// mIpcUpdateManage.downloadCallback(success, param1, param2);
		// break;
		case PageType_DownloadIPCFile:
			mIpcUpdateManage.downloadCallback(success, param1, param2);
			break;
		// ????????????
		case PageType_FeedBack:
			if (mPageSource == "UserOpinion") {
				((UserOpinionActivity) mContext).requestOpinionCallback(success, param1, param2);
			}
			break;
		case PageType_PushReg:
			// token????????????
			GolukNotification.getInstance().getXg().golukServerRegisterCallBack(success, param1, param2);
			break;
		case PageType_GetPushCfg:
		case PageType_SetPushCfg:
			if (null != mContext && mContext instanceof PushSettingActivity) {
				((PushSettingActivity) mContext).page_CallBack(type, success, param1, param2);
			}
			break;
//		case PageType_ModifyHeadPic:
//			if (mContext instanceof ImageClipActivity) {
//				((ImageClipActivity) mContext).pageNotifyCallBack(type, success, param1, param2);
//			}
//
//			if (mContext instanceof UserPersonalHeadActivity) {
//				((UserPersonalHeadActivity) mContext).pageNotifyCallBack(type, success, param1, param2);
//			}
//			break;
		case IPageNotifyFn.PageType_LiveUploadPic:
			if (mContext instanceof AbstractLiveActivity) {
				((AbstractLiveActivity) mContext).uploadImgCallBack(success, param1, param2);
			}
			break;
		}
	}

	public boolean isNeedCheckLive = false;
	private boolean isCallContinue = false;
	public boolean isCheckContinuteLiveFinish = false;
	private final int CONTINUTE_TIME_OUT = 15 * 1000;
	/** T1??????????????????????????? */
	private boolean isT1Success = false;

	private boolean isCanLive() {
		if (isCheckContinuteLiveFinish) {
			GolukDebugUtils.e("", "newlive----Application---isCanLive----0");
			// ????????????
			return false;
		}
		if (!isIpcLoginSuccess || !isUserLoginSucess) {
			GolukDebugUtils.e("", "newlive----Application---isCanLive----1");
			return false;
		}
		if (System.currentTimeMillis() - startTime > CONTINUTE_TIME_OUT) {
			// ??????,???????????????
			isCheckContinuteLiveFinish = true;
			GolukDebugUtils.e("", "newlive----Application---isCanLive----2");
			return false;
		}
		return true;
	}

	// T1 ?????????
	private void T1ContinuteLive() {
		GolukDebugUtils.e("", "newlive----Application---T1ContinuteLive----0");
		if (!isCanLive()) {
			GolukDebugUtils.e("", "newlive----Application---T1ContinuteLive----1");
			return;
		}
		if (this.isAlreadyLive) {
			GolukDebugUtils.e("", "newlive----Application---T1ContinuteLive----2");
			isCheckContinuteLiveFinish = true;
			return;
		}
		if (!isT1Success) {
			GolukDebugUtils.e("", "newlive----Application---T1ContinuteLive----3");
			return;
		}
		if (isCallContinue) {
			return;
		}
		isCallContinue = true;
		realStartContinuteLive();
		isCallContinue = false;
	}

	// ??????
	public void showContinuteLive() {
		GolukDebugUtils.e("", "newlive----Application---showContinuteLive----0");
		// ?????????T1??????IPC??????????????????????????????
		if (mIPCControlManager.isT1Relative()) {
			T1ContinuteLive();
			return;
		}
		// ??????????????????????????????
		if (SharedPrefUtil.getIsLiveNormalExit()) {
			isCheckContinuteLiveFinish = true;
			// ??????????????????
			return;
		}
		if (!isCanLive()) {
			return;
		}
		if (isCallContinue) {
			return;
		}
		isCallContinue = true;
		realStartContinuteLive();
		isCallContinue = false;
	}

	private void realStartContinuteLive() {
		if (mContext instanceof MainActivity) {
			isNeedCheckLive = false;
			isCheckContinuteLiveFinish = true;
			((MainActivity) mContext).showContinuteLive();
		} else {
			isNeedCheckLive = true;
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param success
	 *            1/?????? ??????/??????
	 * @param param2
	 *            ??????????????????
	 * @author jiayf
	 * @date Apr 20, 2015
	 */
	public void parseLoginData(UserData userdata) {
		if (userdata != null) {
			// ??????CC??????????????????
			mCCUrl = userdata.ccbackurl;
			mCurrentUId = userdata.uid;
			mCurrentAid = userdata.aid;
			mCurrentPhoneNum = userdata.phone;
			// New video number published by he followed
			int followedVideoNum = userdata.followvideo;
			isUserLoginSucess = true;
			EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_REQUEST));
			EventBus.getDefault().post(new EventUserLoginRet(EventConfig.USER_LOGIN_RET, true, followedVideoNum));
			this.showContinuteLive();
			GolukDebugUtils.e(null, "jyf---------GolukApplication---------mCCurl:" + mCCUrl + " uid:" + mCurrentUId
					+ " aid:" + mCurrentAid);
		}
	}

	// ??????????????????
	private void setIpcLoginState(boolean isSucess) {
		isIpcLoginSuccess = isSucess;
		isIpcConnSuccess = isSucess;
		if (isSucess) {
			showContinuteLive();
		}
	}

	// VDCP ???????????? ??????
	private void IPC_VDCP_Connect_CallBack(int msg, int param1, Object param2) {
		
		// ????????????????????????,??????????????????
		switch (msg) {
		case ConnectionStateMsg_Idle:
			setIpcLoginState(false);
			ipcDisconnect();
			// ?????????????????????
			if (isconnection) {
				connectionDialog();
			}
			if (null != mMainActivity) {
				mMainActivity.wiFiLinkStatus(3);
			}
			break;
		case ConnectionStateMsg_Connecting:
			GolukDebugUtils
			.e("", "newlive-----GolukApplication----wifiConn----IPC_VDCP_Connect_CallBack----?????????... :");
			setIpcLoginState(false);
			ipcDisconnect();
			// ?????????????????????
			if (isconnection) {
				connectionDialog();
			}
			if (this.isBindSucess()) {
				if (null != mMainActivity) {
					mMainActivity.wiFiLinkStatus(1);
				}
			}
			break;
		case ConnectionStateMsg_Connected:
			// ??????,ipc???????????????,??????????????????????????????,???????????????ipc???????????????,?????????isIpcLoginSuccess=true
			break;
		case ConnectionStateMsg_DisConnected:
			GolukDebugUtils
			.e("", "newlive-----GolukApplication----wifiConn----IPC_VDCP_Connect_CallBack----????????????... :");
			setIpcLoginState(false);
			ipcDisconnect();
			// ?????????????????????
			if (isconnection) {
				connectionDialog();
			}
			if (null != mMainActivity) {
				mMainActivity.wiFiLinkStatus(3);
			}
			// ?????????wifi????????????,??????????????????
			if (mPageSource == "WiFiLinkList") {
				((WiFiLinkListActivity) mContext).ipcFailedCallBack();
			}
			break;
		}
	}

	private void IPC_VDCP_Command_Init_CallBack(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "wifilist----GolukApplication----wifiConn----IPC_VDCP_Init_CallBack-------msg :" + msg);
		// msg = 0 ??????????????? param1 = 0 ?????? | ??????

		if (0 != param1) {
			// ????????????
			setIpcLoginState(false);
			ipcDisconnect();
			return;
		}
		isIpcConnSuccess = true;
		// ?????????wifi????????????,??????????????????
		if (mPageSource == "WiFiLinkList") {
			((WiFiLinkListActivity) mContext).ipcSucessCallBack();
		}
		// ?????????wifi????????????,??????????????????
		if (mPageSource.equals("WiFiLinkBindAll")) {
			((WiFiLinkCompleteActivity) mContext).ipcLinkWiFiCallBack(param2);
		}

		if (isBindSucess()) {
			GolukDebugUtils.e("", "=========IPC_VDCP_Command_Init_CallBack???" + param2);
			IpcConnSuccessInfo ipcInfo = null;
			if (null != param2) {
				ipcInfo = GolukFastJsonUtil.getParseObj((String) param2, IpcConnSuccessInfo.class);
				ipcInfo.lasttime = String.valueOf(System.currentTimeMillis());
			}

			// ??????ipc????????????,???G1, G2 ??????T1
			saveIpcProductName(ipcInfo);
			// ipc?????????????????????,????????????????????????8s??????
			setIpcLoginState(true);
			// ???????????????????????????
			getVideoEncodeCfg();
			// ?????????1????????????????????????
			getVideoEncoderCtg_T1();
			/** ??????adas?????? **/
			getAdasCfg();
			// ??????????????????
			getIPCNumber();
			isconnection = true;// ????????????
			setSyncCount();
			EventBus.getDefault().post(new EventPhotoUpdateLoginState(EventConfig.PHOTO_ALBUM_UPDATE_LOGIN_STATE));
			EventBus.getDefault().post(new EventIpcConnState(EventConfig.IPC_CONNECT));
			GolukApplication.getInstance().getIPCControlManager().getIPCSystemTime();
			// ??????ipc?????????
			GolukApplication.getInstance().getIPCControlManager().getVersion();
			queryNewFileList();
			if (null != mMainActivity) {
				mMainActivity.wiFiLinkStatus(2);
			}
			WifiBindDataCenter.getInstance().updateConnIpcType(mIPCControlManager.mProduceName);
			WifiBindDataCenter.getInstance().updateConnIpcType(ipcInfo);
		}
	}

	// ??????ipc????????????
	private void saveIpcProductName(IpcConnSuccessInfo ipcInfo) {
		if (null != ipcInfo && !TextUtils.isEmpty(ipcInfo.productname)) {
			mIPCControlManager.setProduceName(ipcInfo.productname);
			// ??????????????????
			SharedPrefUtil.saveIpcModel(mIPCControlManager.mProduceName);
		}
	}

	// msg = 1000 ?????????????????????
	private void IPC_VDCP_Resp_Query_CallBack(int msg, int param1, Object param2) {
		if (RESULE_SUCESS == param1) {
			GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==5555===stopDownloadList" + param2);
			if ("ipcfilemanager".equals(mPageSource)) {
				return;
			}
			if(!mIsQuery){
				return;
			}else{
				mIsQuery = false;
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
	}

	private void IPC_VDCP_Msg_IPC_GetVersion_CallBack(int msg, int param1, Object param2) {
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
					GolukDebugUtils.i("lily", "=====???????????????ipcVersion=====" + ipcVersion);
					// ??????ipc?????????
					SharedPrefUtil.saveIPCVersion(ipcVersion);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void IPC_VDCP_Msg_IPC_GetTime_CallBack(int msg, int param1, Object param2) {
		if (param1 == RESULE_SUCESS) {
			if (TextUtils.isEmpty((String) param2)) {
				return;
			}
			long curtime = IpcDataParser.parseIPCTime((String) param2);
			// ????????????????????????
			if (SettingUtils.getInstance().getBoolean("systemtime", true)) {
				long time = SettingUtils.getInstance().getLong("cursystemtime");
				GolukDebugUtils.e("xuhw", "YYYYYY===getIPCSystemTime==time=" + time + "=curtime=" + curtime);
				if (Math.abs(curtime - time) > 60) {// 60?????????????????????
					SettingUtils.getInstance().putLong("cursystemtime", curtime);
					boolean a = GolukApplication.getInstance().getIPCControlManager()
							.setIPCSystemTime(System.currentTimeMillis() / 1000);
					GolukDebugUtils.e("xuhw", "YYYYYY===========setIPCSystemTime===============a=" + a);
				}
			}
		}
	}

	private void IPC_VDCP_Command_IPCKit_CallBack(int msg, int param1, Object param2) {
		if (!isBindSucess()) {
			return;
		}
		if (param1 != RESULE_SUCESS) {
			return;
		}
		List<ExternalEventsDataInfo> kit = IpcDataParser.parseKitData((String) param2);
		if (null == kit || kit.size() <= 0) {
			return;
		}
		for (int i = 0; i < kit.size(); i++) {
			ExternalEventsDataInfo info = kit.get(i);
			if (info.type == 9) {
				// if
				// (!GolukFileUtils.loadBoolean(GolukFileUtils.PROMOTION_AUTO_PHOTO,
				// true)) {
				// return;
				// }
				// File file = new File(FileUtils.libToJavaPath(SNAPSHOT_DIR));
				// if (!file.exists()) {
				// file.mkdirs();
				// }
				// mIPCControlManager.downloadFile(info.location,
				// "snapshotdownload", SNAPSHOT_DIR, IPC_VDCP_Msg_IPCKit);
			} else {
				GolukApplication.getInstance().getIPCControlManager().querySingleFile(info.location);
			}
		}
	}

	private void IPC_VDC_CommandResp_CallBack(int event, int msg, int param1, Object param2) {
//		GolukDebugUtils.e("", "newlive----Application-IPC_VDC_CommandResp_CallBack msg: " + msg + "  param1: " + param1
//				+ "   param2: " + param2);
		switch (msg) {
		case IPC_VDCP_Msg_Init:
			IPC_VDCP_Command_Init_CallBack(msg, param1, param2);
			break;
		case IPC_VDCP_Msg_Query:
			// msg = 1000 ?????????????????????
			IPC_VDCP_Resp_Query_CallBack(msg, param1, param2);
			break;
		case IPC_VDCP_Msg_SingleQuery:
			// msg = 1001 ???????????????
			// ??????8?????????????????????,???????????????????????????????????????,????????????????????????????????????????????????????????????
			ipcVideoSingleQueryCallBack(param1, (String) param2);
			break;
		case IPC_VDCPCmd_SetWifiCfg:
			// msg = 1012 ??????IPC??????WIFI??????
			// param1 = 0 ?????? | ??????
			// ?????????wifi????????????,??????????????????
			if (mPageSource.equals("WiFiLinkBindAll")) {
				((WiFiLinkCompleteActivity) mContext).setIpcLinkWiFiCallBack(param1);
			} else if (mPageSource.equals("changePassword")) {
				((UserSetupChangeWifiActivity) mContext).setIpcLinkWiFiCallBack(param1);
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
		case IPC_VDCP_Msg_GetRecAudioCfg:
			if (param1 == RESULE_SUCESS) {
				try {
					JSONObject obj = new JSONObject((String) param2);
					mT1RecAudioCfg = Integer.parseInt(obj.optString("AudioEnable"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		case IPC_VDCPCmd_SetRecAudioCfg:
			// T1?????????????????????????????????
			if (param1 == RESULE_SUCESS) {
				getVideoEncoderCtg_T1();
			}
			break;
		case IPC_VDCP_Msg_IPCKit:
			IPC_VDCP_Command_IPCKit_CallBack(msg, param1, param2);
			break;
		case IPC_VDCP_Msg_GetVersion:
			// {"product": 67698688, "model": "", "macid": "", "serial": "",
			// "version": "V1.4.21_tzz_vb_rootfs"}
			if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
				IPC_VDCP_Msg_IPC_GetVersion_CallBack(msg, param1, param2);
			}
			break;
		case IPC_VDCP_Msg_GetTime:
			IPC_VDCP_Msg_IPC_GetTime_CallBack(msg, param1, param2);
			break;
		case IPC_VDCP_Msg_GetIdentity:
			IPC_CallBack_GetIdentity(msg, param1, param2);
			break;
		case IPC_VDCP_Msg_GetADASConfig:
			if (param1 == RESULE_SUCESS) {
				AdasConfigParamterBean item = JSON.parseObject((String) param2, AdasConfigParamterBean.class);
				if (item != null) {
					GolukFileUtils.saveInt(GolukFileUtils.ADAS_FLAG, item.enable);
				}
			}
			break;
		case IPC_VDCP_Msg_PushEvent_Comm:
			IPC_VDCP_PushEvent_Comm(msg, param1, param2);
			break;
		case IPC_VDCP_Msg_LiveStart:
			// ????????????
			if (null != mLiveOperater) {
				mLiveOperater.CallBack_Ipc(msg, param1, param2);
			}
			break;
		case IPC_VDCP_Msg_LiveStop:

			break;
		}
	}

	public LiveOperateVdcp mLiveOperater = null;

	private void IPC_VDCP_PushEvent_Comm(int msg, int param1, Object param2) {
		if (RESULE_SUCESS != param1) {
			GolukDebugUtils.e("", "newlive-----GolukApplication----IPC_VDCP_PushEvent_Comm:  " + param2);
			return;
		}
		if (!this.isAlreadyLive) {
			// ??????????????????
			try {
				VdcpLiveBean bean = GolukFastJsonUtil.getParseObj((String) param2, VdcpLiveBean.class);
				if ("sending".equals(bean.content)) {
					isT1Success = true;
					showContinuteLive();
				}
			} catch (Exception e) {
			}
		}

		if (null != mLiveOperater) {
			mLiveOperater.CallBack_Ipc(msg, param1, param2);
		}
	}

	private void IPC_VDTP_ConnectState_CallBack(int msg, int param1, Object param2) {
		// msg = 1 | ????????? or msg = 2 | ????????????
		// ?????????????????????????????????
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

	private void IPC_VDTP_Resp_CallBack(int msg, int param1, Object param2) {
		switch (msg) {
		case IPC_VDTP_Msg_File:
			// ????????????????????? msg = 0
			// param1 = 0,????????????
			// param1 = 1,?????????
			ipcVideoDownLoadCallBack(param1, (String) param2);
			break;
		}
	}

	@Override
	public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
		if (this.isExit()) {
			return;
		}
		if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
			IPC_VDCP_Connect_CallBack(msg, param1, param2);
		}
		if (ENetTransEvent_IPC_VDCP_CommandResp == event) {
			IPC_VDC_CommandResp_CallBack(event, msg, param1, param2);
		}
		// IPC?????????????????? event = 2
		if (ENetTransEvent_IPC_VDTP_ConnectState == event) {
			IPC_VDTP_ConnectState_CallBack(msg, param1, param2);
		}
		// IPC??????????????????,???????????????????????? event = 3
		if (ENetTransEvent_IPC_VDTP_Resp == event) {
			IPC_VDTP_Resp_CallBack(msg, param1, param2);
		}
	}

	private void IPC_CallBack_GetIdentity(int msg, int param1, Object param2) {
		if (param1 == RESULE_SUCESS) {
			final IPCIdentityState mVersionState = IpcDataParser.parseVersionState((String) param2);
			if (null != mVersionState && null != mIPCControlManager) {
				mIPCControlManager.mDeviceSn = mVersionState.name;
				SharedPrefUtil.saveIPCNumber(mIPCControlManager.mDeviceSn);
				mIPCControlManager.reportBindMsg();
			}
		}
	}

	@Override
	public void TalkNotifyCallBack(int type, String data) {
	}

	// ??????T1????????? ?????????????????????
	private void getVideoEncoderCtg_T1() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			mIPCControlManager.getAudioCfg_T1();
		}
	}

	/**
	 * ???????????????????????????
	 * 
	 * @author xuhw
	 * @date 2015???4???10???
	 */
	private void getVideoEncodeCfg() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			getIPCControlManager().getVideoEncodeCfg(0);
		}
	}

	/**
	 * ??????adas????????????
	 * 
	 * 
	 */
	private void getAdasCfg() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			getIPCControlManager().getT1AdasConfig();
		}
	}

	private void getIPCNumber() {
		if (GolukApplication.getInstance().getIpcIsLogin()) {
			getIPCControlManager().getIPCIdentity();
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
		// ????????????
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

	// ??????????????????
	private void setSyncCount() {
		GolukDebugUtils.e("", "sync count ---application-----setSyncCount ---1");
		if (!isBindSucess()) {
			return;
		}
		int syncFlag = SettingUtils.getInstance().getInt(UserSetupActivity.MANUAL_SWITCH, -1);

		GolukDebugUtils.e("", "sync count ---application-----setSyncCount ---2:  " + syncFlag + "   nane: "
				+ mIPCControlManager.mProduceName);
		/** ?????????????????????????????????????????????????????????G1???T1S??????????????????5??????????????????????????????20??? **/
		if (syncFlag == -1) {
			if (IPCControlManager.G1_SIGN.equals(mIPCControlManager.mProduceName)
					|| IPCControlManager.T1s_SIGN.equalsIgnoreCase(mIPCControlManager.mProduceName)) {
				SettingUtils.getInstance().putInt(UserSetupActivity.MANUAL_SWITCH, 5);
			} else {
				SettingUtils.getInstance().putInt(UserSetupActivity.MANUAL_SWITCH, 20);
			}
		}
	}

	/**
	 * ????????????????????????5??????????????????
	 * 
	 * @return true/false ??????/?????????
	 * @author jyf
	 */
	private boolean isCanQueryNewFile() {

		int syncFlag = SettingUtils.getInstance().getInt(UserSetupActivity.MANUAL_SWITCH, -1);
		if (syncFlag <= 0) {
			return false;
		}

		if (!isBindSucess()) {
			return false;
		}

		if (!isIpcLoginSuccess) {
			return false;
		}

		if (mDownLoadFileList.size() > 0) {
			return false;
		}

		if ("carrecorder".equals(mPageSource)) {
			if (mIPCControlManager.isG1Relative()) {
				return false;
			}
		}
		return true;
	}

	public void setIpcDisconnect() {
		mIPCControlManager.setVdcpDisconnect();
		if (null != mMainActivity) {
			mMainActivity.closeAp();
		}
		stopDownloadList();
		setIpcLoginOut();
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param isbind
	 *            true/false ?????????/????????????
	 * @author jyf
	 */
	public void setBinding(boolean isbind) {
		isBinding = isbind;
	}

	public boolean isBindSucess() {
		return WifiBindDataCenter.getInstance().isHasDataHistory() && !isBinding;
	}

	/**
	 * ??????????????????????????????10??????
	 * 
	 * @author xuhw
	 * @date 2015???4???24???
	 */
	public void queryNewFileList() {
		if (!isCanQueryNewFile()) {
			// ?????????????????????
			return;
		}
		
		long starttime = SettingUtils.getInstance().getLong("downloadfiletime", 0);
		int syncFlag = SettingUtils.getInstance().getInt(UserSetupActivity.MANUAL_SWITCH, 5);
		GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==4444===stopDownloadList:   " + starttime + "  syncFlag: "
				+ syncFlag);

		boolean flog = mIPCControlManager.queryFileListInfo(6, syncFlag, starttime, 2147483647, "0");
		if(flog){
			mIsQuery = true;
		}
	}

	/**
	 * ??????IPC????????????
	 * 
	 * @author xuhw
	 * @date 2015???5???19???
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
				}
			}
			mIPCControlManager.stopDownloadFile();
		}
	}

	public void userStopDownLoadList() {

		autodownloadfile = false;
		mIPCControlManager.stopDownloadFile();
		if (mDownLoadFileList.size() > 0) {
			mDownLoadFileList.clear();
			mNoDownLoadFileList.clear();
			if (GlobalWindow.getInstance().isShow()) {
				GlobalWindow.getInstance().toFailed(mContext.getString(R.string.str_global_cancel_success));
			}
		}

	}

	/**
	 * IPC??????????????????
	 * 
	 * @author xuhw
	 * @date 2015???4???24???
	 */
	private void ipcDisconnect() {
		EventBus.getDefault().post(new EventPhotoUpdateLoginState(EventConfig.PHOTO_ALBUM_UPDATE_LOGIN_STATE));
		if (mDownLoadFileList.size() > 0) {
			mDownLoadFileList.clear();
			mNoDownLoadFileList.clear();
			if (GlobalWindow.getInstance().isShow()) {
				GlobalWindow.getInstance().dimissGlobalWindow();
			}
		}
	}

	private void tips() {
		GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==fuck===stopDownloadList");
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

				Collections.sort(mDownLoadFileList, new SortByDate());
				GolukDebugUtils.e("xuhw",
						"BBBB=====stopDownloadList==22222===stopDownloadList" + mDownLoadFileList.size()
								+ mDownLoadFileList);
				if (mDownLoadFileList.size() > 0) {
					GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList=====stopDownloadList");
					autodownloadfile = true;
				}
				int len = mDownLoadFileList.size() - 1;
				for (int i = len; i >= 0; i--) {
					String name = mDownLoadFileList.get(i);
					boolean flag = GolukApplication.getInstance().getIPCControlManager().querySingleFile(name);
					GolukDebugUtils.e("xuhw", "YYYYYY=====querySingleFile=====name=" + name + "==flag=" + flag);
				}
			}
		}
	}

	public void connectionDialog() {
		EventBus.getDefault().post(new EventIpcConnState(EventConfig.IPC_DISCONNECT));
	}

	/**
	 * ?????????????????????activity ????????????
	 * 
	 * @Description:
	 * @return boolean
	 * @author ??????
	 */
	// public boolean isCanShowConnectDialog() {
	// //////// CK start
	// // if (mContext instanceof FragmentAlbum) {
	// // return true;
	// // } else {
	// // return false;
	// // }
	// return true;
	// //////// CK End
	// }

	/**
	 * ?????????????????????????????????,?????????????????????NULL
	 * 
	 * @return ??????????????? UserInfo
	 * @author jyf
	 * @date 2015???8???7???
	 */
	public UserInfo getMyInfo() {
		UserInfo myInfo = null;
		try {
			String user = SharedPrefUtil.getUserInfo();

			Log.e("dengting", "getUserInfo------------------logic-userInfo:" + user);

			if (null != user) {
				myInfo = JsonUtil.parseSingleUserInfoJson(new JSONObject(user));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return myInfo;
	}
	
	public void setMyinfo(String name,String head,String desc,String url){
		
		String user = SharedPrefUtil.getUserInfo();

		Log.e("dengting", "getUserInfo------------------logic-userInfo:" + user);

		try {
			if(user != null && !"".equals(user)){
				UserInfo myInfo = JsonUtil.parseSingleUserInfoJson(new JSONObject(user));
				if(name !=null && !"".equals(name)){
					myInfo.nickname = name;
				}
				if(head !=null && !"".equals(head)){
					myInfo.head = head;
				}
				if(desc !=null && !"".equals(desc)){
					myInfo.desc = desc;
				}
				if(url !=null){
					myInfo.customavatar = url;
				}
				
				SharedPrefUtil.saveUserInfo(JSON.toJSONString(myInfo));
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

	/**
	 * ??????????????????
	 * 
	 * @return
	 * @author xuhw
	 * @date 2015???5???13???
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

	// isReal ??????????????????
	public void uploadMsg(String msg, boolean isReal) {
		if (null == mGoluk || null == msg || "".equals(msg)) {
			return;
		}
		GolukDebugUtils.e("", "jyf------logReport-------GolukApplicaiton-------: " + msg);
		final int which = isReal ? IMessageReportFn.REPORT_CMD_LOG_REPORT_REAL
				: IMessageReportFn.REPORT_CMD_LOG_REPORT_HTTP;

		mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_MessageReport, which, msg);
	}

	// ?????????????????????
	public void startLiveLook(UserInfo userInfo) {
		GolukDebugUtils.e("", "jyf-----click------666666");
		if (null == userInfo) {
			return;
		}
		// ?????????????????????

		Intent intent;
		if (isInteral()) {
			intent = new Intent(mContext, BaidumapLiveActivity.class);
		} else {
			intent = new Intent(mContext, GooglemapLiveActivity.class);
		}

		intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
		intent.putExtra(LiveActivity.KEY_GROUPID, "");
		intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
		intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
		intent.putExtra(LiveActivity.KEY_USERINFO, userInfo);
		mContext.startActivity(intent);
		GolukDebugUtils.e(null, "jyf----20150406----MainActivity----startLiveLook");
	}

	private boolean isMainProcess() {
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
			if (appProcess.pid == pid && this.getPackageName().equals(appProcess.processName)) {
				return true;
			}
		}
		return false;
	}
	
	public void setLoginRespInfo(String info) {
		GolukDebugUtils.e("","login----GolukApplication---setLoginRespInfo----info: " + info);
		mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_SetLoginRespInfo,
				info);
	}

	
}
