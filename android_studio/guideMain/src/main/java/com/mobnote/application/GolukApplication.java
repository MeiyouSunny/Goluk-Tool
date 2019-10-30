package com.mobnote.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.SDKInitializer;
import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventIpcConnState;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.eventbus.EventPhotoUpdateLoginState;
import com.mobnote.eventbus.EventUserLoginRet;
import com.mobnote.eventbus.EventUtil;
import com.mobnote.eventbus.IpcInfoUpdate;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpinionActivity;
import com.mobnote.golukmain.UserSetupActivity;
import com.mobnote.golukmain.UserSetupChangeWifiActivity;
import com.mobnote.golukmain.UserSetupWifiActivity;
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
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.livevideo.LiveOperateVdcp;
import com.mobnote.golukmain.livevideo.VdcpLiveBean;
import com.mobnote.golukmain.player.SdkHandler;
import com.mobnote.golukmain.thirdshare.GolukUmConfig;
import com.mobnote.golukmain.userlogin.UserData;
import com.mobnote.golukmain.userlogin.UserResult;
import com.mobnote.golukmain.videosuqare.VideoCategoryActivity;
import com.mobnote.golukmain.videosuqare.VideoSquareManager;
import com.mobnote.golukmain.wifibind.IpcConnSuccessInfo;
import com.mobnote.golukmain.wifibind.WiFiLinkCompleteActivity;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifidatacenter.JsonWifiBindManager;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifimanage.WifiApAdmin;
import com.mobnote.golukmain.xdpush.GolukNotification;
import com.mobnote.log.app.AppLogOpreater;
import com.mobnote.log.app.AppLogOpreaterImpl;
import com.mobnote.log.app.LogConst;
import com.mobnote.log.ipc.IpcExceptionOperater;
import com.mobnote.log.ipc.IpcExceptionOperaterImpl;
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
import com.mobnote.util.ZhugeUtils;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.veuisdk.SdkEntry;
import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.location.GolukPosition;
import cn.com.mobnote.module.location.ILocationFn;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.tiros.api.Const;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.baidu.BaiduLocation;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

import static com.mobnote.videoedit.constant.VideoEditConstant.EXPORT_FOLDER_NAME;

public class GolukApplication extends MultiDexApplication implements IPageNotifyFn, IPCManagerFn, ITalkFn, ILocationFn {
    /**
     * 已获取的AppKey
     */
    public static final String RD_APP_KEY = "3ac4ec3b83ff2b46";
    /**
     * 已获取的AppSecret
     */
    public static final String RD_APP_SECRET = "51b6e9f5866392a68f7515fe51746459y/UU+RE+9GGaQLMTsBmzZZ9XHD+pzBEmJZsUXfvf7Q9hRV/MhfXYlZt6GpPv/ESqFD4oaaXyQhkmOx19fTZDjtRUsQZcX4SlefXh+UHjrYrNVbglIUPiy97bTbS37twxWOffk4b/XNd8Wg3lTZkmiO2wgkB34JaD4CFfL15VsYPDY63zJ+wTH+wpFX5RpgAQ9aCEluTR601d6C88V+3JaXz9Knt0AsVOkpXK0f9GkZ5QseTeCSeA2EQqq8OFOW7IcV29dt29ENckb1g5+XqPhhSdqfCR5CUPNxZ74uk77tiJydXRAeV6T+oaTbnMm60MtVlx41iejAjCHvX9xL/vbrKrpu5HxBQKZT4hNFiJjHXDg5tFAKwIM/DEsL3TrMaK+qv0nEBDxXGUD8PQ3gzC/1Lv7YNSDo6TlcboVDH0M8B+e8iIsHkJEYsCfPw4c6N/okKZZ9Ofn75oSogZNiw252VzEgqfoCKWBkGQ+wm6NqkePR1icPN9VqXHGPzgFStYCMvNpZZ9HbitRwA9uV3Lu+KghzPcOEaS+VSo3sWDzh0=";

    /**
     * 已获取的AppKey
     */
    public static final String RD_APP_KEY_INNATIONAL = "dc8c35492a5e8ccc";
    /**
     * 已获取的AppSecret
     */
    public static final String RD_APP_SECRET_INNATIONAL = "bc1881461c91257c72d0559d6f66ef6dRYUWjkdE6/gct1k1nEGRMDNqGhylT2nMZyJRTzyhGruSYoW6Rn28rvy8E0OGkJgPCUWN19DpXnqzh6E0M8Z0OO7Ef0jwh2LwH2UR8YLY0tJ7c28erjR8N3mi9OncE4KRUqWi3hYodqPyeqQHQq/YvJp/lhMA+EhBeWPFQ8Nrk3YbgpeqgfdCUF4rOl6shl5f51piEXW6xd1gdOr7savvb4VTuxlg1X3bGmwzZzhcqzybDDqbj0AT8xWEbMoziId547elt6KeHHGiEEWrHwA6H6IFVOa26LSFyVBsefXr7Vf/yw7cIN+2NTLJpu8AtEmcyWplYFEvbrY9XKk2ZoQTiXI8hrRvPVTTpOCImbqt0l9BIKP1qFz1Tev1K8Yd4fpqyfDmQq6WI2GAf8YLqA42mj7V7DaPdAr42i1I6euQJGnhcnDSDJ3HT67NF3pnwqxMXZdjcBdfcigTaU800wpIiTMV40gJyRGDIu+LGIpprlal0Ekh04i53QTs/6WfF4u8TZMVDU6XL1W3cidmTWb49ozyWNapQL64nCBcHbZmZVo=";

    /**
     * JIN接口类
     */
    public GolukLogic mGoluk = null;
    /**
     * ip地址
     */
    public static String mIpcIp = null;
    /**
     * 保存上下文
     */
    private Context mContext = null;
    /**
     * 来源标示,用来强转activity
     */
    private String mPageSource = "";
    /**
     * 主页activity
     */
    public static MainActivity mMainActivity = null;
    /**
     * 视频保存地址 fs1:指向->sd卡/goluk目录
     */
    private String mVideoSavePath = "fs1:/video/";

    private static GolukApplication instance = null;
    public IPCControlManager mIPCControlManager = null;
    private VideoSquareManager mVideoSquareManager = null;

    /**
     * 是否正在绑定过程, 如果在绑定过程中，则不接受任何信息
     */
    private boolean isBinding = false;
    /**
     * 登录IPC是否登录成功
     */
    public boolean isIpcLoginSuccess = false;
    /**
     * 实时反应IPC连接状态
     */
    public boolean isIpcConnSuccess = false;
    /**
     * 　用户是否登录小车本服务器成功
     */
    public boolean isUserLoginSucess = false;
    /**
     * CC视频上传地址
     */
    public String mCCUrl = null;
    /**
     * 当前登录用户的UID
     */
    public String mCurrentUId = null;
    /**
     * 当前登录用户的Aid
     */
    public String mCurrentAid = null;

    /**
     * 当前用户绑定手机号
     **/
    public String mCurrentPhoneNum = null;
    /**
     * 行车记录仪缓冲路径
     */
    private String carrecorderCachePath = "";
    /**
     * 音视频配置信息
     */
    private VideoConfigState mVideoConfigState = null;
    /**
     * 自动循环录制状态标识
     */
    private boolean autoRecordFlag = false;
    /**
     * 停车安防配置
     */
    private int[] motioncfg;

    private WifiApAdmin wifiAp;
    /**
     * 当前地址
     */
    public String mCurAddr = null;
    /**
     * 登录的五个状态 0登录中 1 登录成功 2登录失败 3手机号未注册，跳转注册页面 4超时 5密码错误达上限去重置密码
     **/
    public int loginStatus;
    /**
     * 注册的三个状态 1----注册/重置 中 2----注册/重置 成功 3---注册/重置 失败 4---code=500 5---code=405
     * 6----code=406 7----code=407 8---code=480 9---超时
     **/
    public int registStatus;
    /**
     * 自动登录的四个状态 1自动登录中 2自动登录成功 3自动登录失败 4自动登录超时 5密码错误
     **/
    public int autoLoginStatus;
    /**
     * 注销状态
     **/
    public boolean loginoutStatus = false;
    /**
     * 获取验证码的四个状态 0----获取中 1----获取成功 2----获取失败 3---code=201 4----code=500
     * 5----code=405 6----code=440 7----code=480 8----code=470
     **/
    public int identifyStatus;

    /**
     * User管理类
     **/
    public User mUser = null;
    /**
     * 登录管理类
     **/
    public UserLoginManage mLoginManage = null;
    /**
     * 升级管理类
     **/
    public IpcUpdateManage mIpcUpdateManage = null;
    /**
     * 获取验证码管理类
     **/
    public UserIdentifyManage mIdentifyManage = null;
    /**
     * 注册/重置密码管理类
     **/
    public UserRegistAndRepwdManage mRegistAndRepwdManage = null;
    /**
     * 计时器管理类
     **/
    public TimerManage mTimerManage = null;

    private HashMap<String, ILocationFn> mLocationHashMap = new HashMap<String, ILocationFn>();
    /**
     * 未下载文件列表
     */
    private List<String> mNoDownLoadFileList;
    /**
     * 所有下载文件列表
     */
    private List<String> mDownLoadFileList;

    /**
     * 是否已经连接成功过
     */
    public boolean isconnection = false;
    /**
     * 后台标识
     */
    private boolean isBackground = false;
    public long startTime = 0;
    public boolean autodownloadfile = false;
    /**
     * 点击设置页版本检测标识
     **/
    public boolean flag = false;
    /**
     * SD卡无容量标识
     */
    private boolean isSDCardFull = false;
    /**
     * 文件下载中标识
     */
    private boolean isDownloading = false;
    /**
     * 下载列表个数
     */
    private int downloadCount = 0;
    /**
     * 极路客升级成功的状态
     **/
    public boolean updateSuccess = false;
    /**
     * wifi连接状态
     */
    public int mWiFiStatus = 0;


    private ArrayList<VideoFileInfo> fileList;

    private boolean mIsExit = true;
    /**
     * T1声音录制开关　０关闭１打开
     **/
    public int mT1RecAudioCfg = 1;
    /**
     * 是否启用单项连接
     */
    private boolean enableSingleWifi;
    /**
     * 当前的国家区号
     **/
    public CountryBean mLocationCityCode = null;
    /**
     * 是否发起过直播
     */
    public boolean isAlreadyLive = false;

    private boolean mIsQuery = false;

    /**
     * 保存区分国际版与国内版的标示 例如：国际版 T1U 国内版 T1
     **/
    public String mIpcVersion = "";

    static {
//		System.loadLibrary("golukmobile");
    }

    public void setExit(boolean isExit) {
        mIsExit = isExit;
    }

    public boolean isExit() {
        return mIsExit;
    }

    private boolean isInitializeSDK = false;

    @Override
    public void onCreate() {
        super.onCreate();
        BaiduLocation.mServerFlag = isMainland();
        System.loadLibrary("golukmobile");

        instance = this;
        Const.setAppContext(this);

        if (isMainProcess()) {
            HttpManager.getInstance();

//            initializeSDK();
        }

        // TODO 此处不要做初始化相关的工作
    }

    /**
     * 初始化SDK
     */
    public void initializeSDK() {
        if (isInitializeSDK) return;
        //百度sdk
        SDKInitializer.initialize(this);
        // 初始化绑定信息的数据保存
        WifiBindDataCenter.getInstance().setAdatper(new JsonWifiBindManager());
        GolukVideoInfoDbManager.getInstance().initDb(this.getApplicationContext());
        GolukUmConfig.UmInit();
        initXLog();
        GolukMobUtils.initMob(this);
        ZhugeSDK.getInstance().init(getApplicationContext());

        //锐动SDK
        SdkEntry.enableDebugLog(true);
        String videoPath = android.os.Environment.getExternalStorageDirectory().getPath() + EXPORT_FOLDER_NAME;
        if (isMainland()) {
            SdkEntry.initialize(this, videoPath, RD_APP_KEY, RD_APP_SECRET, new SdkHandler().getCallBack());
        } else {
            SdkEntry.initialize(this, videoPath, RD_APP_KEY_INNATIONAL, RD_APP_SECRET_INNATIONAL, new SdkHandler().getCallBack());
        }
        isInitializeSDK = true;
    }


    /* 获取IPC日志 */
    private static final int MSG_TYPE_QUERY_IPC_EXCEPTION_LIST = 11;
    public Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
//            if (msg.what == MSG_TYPE_QUERY_IPC_EXCEPTION_LIST) {
//                // 获取设备Exception信息
//                IpcExceptionOperater ipcExceptionOperater = new IpcExceptionOperaterImpl(GolukApplication.getInstance().getApplicationContext());
//                ipcExceptionOperater.getIpcExceptionList();
//                return;
//            }

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
        }

        ;
    };

    private void initXLog() {
        AppLogOpreater appLogOpreater = new AppLogOpreaterImpl();
        appLogOpreater.deleteSurplusLogFile();

        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)            // Specify log level, logs below this level won't be printed, default: LogLevel.ALL
                .tag("goluk")                                         // Specify TAG, default: "X-LOG"
                .nt()                                                  // Enable thread info, disabled by default
                //.st(1)                                                 // Enable stack trace info with depth 2, disabled by default
                //.b()                                                   // Enable border, disabled by default
                .build();

        Printer androidPrinter = new AndroidPrinter();             // Printer that print the log using android.util.Log
        Printer consolePrinter = new ConsolePrinter();             // Printer that print the log to console using System.out
        Printer filePrinter = new FilePrinter                      // Printer that print the log to the file system
                .Builder(new File(Environment.getExternalStorageDirectory(), GolukFileUtils.GOLUK_LOG_PATH).getPath())// Specify the path to save log file
                .fileNameGenerator(new DateFileNameGenerator())        // Default: ChangelessFileNameGenerator("log")
                .backupStrategy(new NeverBackupStrategy())             // Default: FileSizeBackupStrategy(1024 * 1024)
                .logFlattener(new PatternFlattener("{d yyyy/MM/dd hh:mm:ss}|{l}|{t}| {m}"))
                .build();

        XLog.init(                                                 // Initialize XLog
                config,                                                // Specify the log configuration, if not specified, will use new LogConfiguration.Builder().build()
                androidPrinter,                                        // Specify printers, if no printer is specified, AndroidPrinter(for Android)/ConsolePrinter(for java) will be used.
                //consolePrinter,
                filePrinter);
    }

    public void initLogic() {
        if (null != mGoluk) {
            return;
        }

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
            mGoluk.GolukLogicDestroy();
            mGoluk = null;
        }
    }

    public void appFree() {
        SdkEntry.onExitApp(this);
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

        ZhugeSDK.getInstance().flush(getApplicationContext());

    }

    /**
     * 升级
     */
    public void startUpgrade() {
        // app升级+ipc升级
        String vIpc = SharedPrefUtil.getIPCVersion();
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
     * 设置T1声音录制开关
     *
     * @param state
     */
    public void setT1VideoCfgState(int state) {
        this.mT1RecAudioCfg = state;
    }

    /**
     * 获取T1声音录制开关
     *
     * @return
     */
    public int getT1VideoCfgState() {
        return mT1RecAudioCfg;
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
     * 是否是国内版
     */
    public boolean isMainland() {
        if (null != this.getPackageName() && "cn.com.mobnote.golukmobile".equals(this.getPackageName())) {
            return true;
        } else {
            return false;
        }
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
            GlobalWindow.getInstance().toFailed(this.getResources().getString(R.string.str_video_transfer_fail));
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

    private String getSavePath(int type) {
        if (IPCManagerFn.TYPE_SHORTCUT == type) {
            return mVideoSavePath + "wonderful/";
        } else if (IPCManagerFn.TYPE_URGENT == type) {
            return mVideoSavePath + "urgent/";
        } else if (IPCManagerFn.TYPE_CIRCULATE == type) {
            return mVideoSavePath + "loop/";
        } else {
            return mVideoSavePath + "reduce/";
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
            GlobalWindow.getInstance().reset();
            JSONObject json = new JSONObject(data);
            final String fileName = json.getString("location");
            final long time = json.optLong("time");
            final double filesize = json.optDouble("size");
            final int type = json.getInt("type");
            final String savePath = getSavePath(type);
            //需求2.10 只有手动才能够下载紧急
            if (!SharedPrefUtil.getManualDownloadVideo() && IPCManagerFn.TYPE_URGENT == type) {
                return;
            }
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

            downloadCount++;
            // 保存文件信息到数据库
            VideoFileInfoBean bean = JsonUtil.jsonToVideoFileInfoBean(data, mIPCControlManager.mProduceName);
            GolukVideoInfoDbManager.getInstance().addVideoInfoData(bean);
            // 调用下载视频接口
            Log.i("download start", "download start");
            mIPCControlManager.downloadFile(fileName, "videodownload", savePath, time);
            // 下载视频第一帧截图
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
            GolukDebugUtils.e("", "解析视频下载JSON数据错误");
            e.printStackTrace();
        }
    }

    // 下载视频第一帧图片
    private void downLoadVideoThumbnail(String videoFileName, long filetime) {
        final String imgFileName = videoFileName.replace("mp4", "jpg");
        final String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
        File file = new File(filePath + File.separator + imgFileName);
        if (!file.exists()) {
            mIPCControlManager.downloadFile(imgFileName, "imgdownload", FileUtils.javaToLibPath(filePath), filetime);
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
            GlobalWindow.getInstance().createVideoUploadWindow(
                    this.getResources().getString(R.string.str_video_transfer_cancle));
        }
        GlobalWindow.getInstance().toFailed(this.getResources().getString(R.string.str_video_transfer_cancle));
        GolukUtils.showToast(getApplicationContext(), this.getResources().getString(R.string.str_no_space));
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
            GolukDebugUtils.e("", "GolukApplication-----ipcVideoDownLoadCallback:  success:" + success + "  data:"
                    + data);
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

                    XLog.tag(LogConst.TAG_DOWNLOAD).i("Count %d/%d, current progress:%s, %d", mNoDownLoadFileList.size(),
                            mDownLoadFileList.size(), filename, percent);
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
                        //未中止--视频自动同步
                        ZhugeUtils.eventAutoSynchronizeVideo(mContext, mContext.getString(R.string.str_zhuge_synchronize_video_not), mDownLoadFileList.size());
                        autodownloadfile = false;
                        mDownLoadFileList.clear();
                        mNoDownLoadFileList.clear();
                        if (SharedPrefUtil.getManualDownloadVideo()) {
                            SharedPrefUtil.setManualDownloadVideo(false);
                        }
                        GlobalWindow.getInstance().topWindowSucess(
                                this.getResources().getString(R.string.str_video_transfer_success));
                        XLog.tag(LogConst.TAG_DOWNLOAD).i("Download complete");
                        // 发送Event
                        EventUtil.sendDownloadCompleteEvent();
                    }

                    resetSDCheckState();
                } else {
                    resetSDCheckState();
                    GolukDebugUtils.e("xuhw", "YYYYYY=＠＠＠＠===download==fail===success=" + success + "==data=" + data);
                    JSONObject json = new JSONObject(data);
                    final String filename = json.optString("filename");

                    if (mDownLoadFileList.contains(filename)) {
                        if (!mNoDownLoadFileList.contains(filename)) {
                            mNoDownLoadFileList.add(filename);
                        }
                    }
                    // 下载文件失败，删除数据库中的信息
                    GolukVideoInfoDbManager.getInstance().delVideoInfo(filename);
                    XLog.tag(LogConst.TAG_DOWNLOAD).i("Download video %s failed", filename);

                    GolukDebugUtils.e("xuhw", "BBBBBBB=======down==fail====" + mNoDownLoadFileList.size());
                    if (checkDownloadCompleteState()) {
                        mDownLoadFileList.clear();
                        mNoDownLoadFileList.clear();
                        GlobalWindow.getInstance().toFailed(
                                this.getResources().getString(R.string.str_video_transfer_fail));
                    }
                }
            } else if (tag.equals("imgdownload") && 0 == success) {
                // 其次把文件插入到系统图库
                if (!GolukFileUtils.loadBoolean(GolukFileUtils.PROMOTION_AUTO_PHOTO, true)) {
                    return;
                }

                String path = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
                try {
                    JSONObject json = new JSONObject(data);
                    String filename = json.optString("filename");
                    MediaStore.Images.Media.insertImage(getContentResolver(), path + File.separator + filename,
                            filename, "Goluk");
                    // 最后通知图库更新
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
                } catch (Exception e) {
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
//		GolukDebugUtils.e("", "chxy send pageNotifyCallBack--" + "type:" + type + ",success:" + success + ",param1:"
//				+ param1 + ",param2:" + param2);

        if (this.isExit()) {
            return;
        }

        switch (type) {
            case 7:
                // 地图大头针数据
                if (null != mContext) {
                    if (mContext instanceof VideoCategoryActivity) {
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
            case PageType_GetVCode:
                // 注册获取验证码
                mIdentifyManage.getIdentifyCallback(success, param1, param2);
                break;
            // 注册PageType_Register
            case PageType_BindInfo:
                mRegistAndRepwdManage.bindPhoneNumCallback(success, param1, param2);
                break;
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
//            case PageType_GetPushCfg:
//            case PageType_SetPushCfg:
//                if (null != mContext && mContext instanceof PushSettingActivity) {
//                    ((PushSettingActivity) mContext).page_CallBack(type, success, param1, param2);
//                }
//                break;
        }
    }

    public boolean isNeedCheckLive = false;
    private boolean isCallContinue = false;
    public boolean isCheckContinueLiveFinish = false;
    /**
     * T1回调的消息是否回来
     */
    private boolean isT1Success = false;

    private boolean isCanLive() {
        if (isCheckContinueLiveFinish) {
            GolukDebugUtils.e("", "newlive----Application---isCanLive----0");
            // 已经完成
            return false;
        }
        if (!isIpcLoginSuccess || !isUserLoginSucess) {
            GolukDebugUtils.e("", "newlive----Application---isCanLive----1");
            return false;
        }
        return true;
    }

    //判断是否需要
    public void checkContinueLive() {
        // 如果是T1，在IPC回调的时候，发起直播
        if (!mIPCControlManager.isT1Relative()) {
            return;
        }
        if (!isCanLive()) {
            return;
        }
        if (this.isAlreadyLive) {
            isCheckContinueLiveFinish = true;
            return;
        }
        if (!isT1Success) {
            return;
        }
        if (isCallContinue) {
            return;
        }
        isCallContinue = true;
        if (mContext instanceof MainActivity) {
            isNeedCheckLive = false;
            isCheckContinueLiveFinish = true;
            ((MainActivity) mContext).requestIsAlive();
        } else {
            isNeedCheckLive = true;
        }
        isCallContinue = false;
    }

    /**
     * 处理登录结果
     * <p/>
     * 1/其它 成功/失败
     * 登录回调数据
     *
     * @author jiayf
     * @date Apr 20, 2015
     */
    public void parseLoginData(UserData userdata) {
        if (userdata != null) {
            // 获得CC上传视频接口
            mCCUrl = userdata.ccbackurl;
            mCurrentUId = userdata.uid;
            mCurrentAid = userdata.aid;
            mCurrentPhoneNum = userdata.phone;
            // New video number published by he followed
            int followedVideoNum = userdata.followvideo;
            isUserLoginSucess = true;
            EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_REQUEST));
            EventBus.getDefault().post(new EventUserLoginRet(EventConfig.USER_LOGIN_RET, true, followedVideoNum));
            this.checkContinueLive();
            GolukDebugUtils.e(null, "jyf---------GolukApplication---------mCCurl:" + mCCUrl + " uid:" + mCurrentUId
                    + " aid:" + mCurrentAid);
        }
    }

    // 海外版不能使用国内设备标识
    private boolean mCanNotUse;

    public void setCanNotUse(boolean canNotUse) {
        mCanNotUse = canNotUse;
    }

    public boolean canNotUse() {
        return mCanNotUse;
    }

    // 设置连接状态
    private void setIpcLoginState(boolean isSucess) {
        isIpcLoginSuccess = isSucess;
        isIpcConnSuccess = isSucess;
        if (isSucess) {
            checkContinueLive();
        }
        if (!isSucess) {
            mCanNotUse = false;
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
                XLog.tag(LogConst.TAG_CONNECTION).i("IPC_VDCP_Connect_CallBack: state idel");
                break;
            case ConnectionStateMsg_Connecting:
                GolukDebugUtils
                        .e("", "newlive-----GolukApplication----wifiConn----IPC_VDCP_Connect_CallBack----连接中... :");
                setIpcLoginState(false);
                ipcDisconnect();
                // 已经连接成功过
                if (isconnection) {
                    connectionDialog();
                }
                if (this.isBindSucess()) {
//                    if (null != mMainActivity) {
//                        mMainActivity.wiFiLinkStatus(1);
//                    }
                }
                XLog.tag(LogConst.TAG_CONNECTION).i("IPC_VDCP_Connect_CallBack: state connecting");
                break;
            case ConnectionStateMsg_Connected:
                // 只是,ipc信号连接了,初始化的东西还没完成,所以要等到ipc初始化成功,才能把isIpcLoginSuccess=true
                break;
            case ConnectionStateMsg_DisConnected:
                GolukDebugUtils
                        .e("", "newlive-----GolukApplication----wifiConn----IPC_VDCP_Connect_CallBack----连接失败... :");
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
                    ((WiFiLinkListActivity) mContext).ipcFailedCallBack();
                }
                XLog.tag(LogConst.TAG_CONNECTION).i("IPC_VDCP_Connect_CallBack: state disconnected");
                break;
        }
    }

    private void IPC_VDCP_Command_Init_CallBack(int msg, int param1, Object param2) {
        GolukDebugUtils.e("", "wifilist----GolukApplication----wifiConn----IPC_VDCP_Init_CallBack-------msg :" + msg);
        // msg = 0 初始化消息 param1 = 0 成功 | 失败

        if (0 != param1) {
            // 连接失败
            setIpcLoginState(false);
            ipcDisconnect();
            return;
        }
        isIpcConnSuccess = true;
        // 如果在wifi连接页面,通知连接成功
        if (mPageSource == "WiFiLinkList") {
            if (!((WiFiLinkListActivity) mContext).ipcSucessCallBack(param2)) {
                return;
            }
        }
        // 如果在wifi连接页面,通知连接成功
        if (mPageSource.equals("WiFiLinkBindAll")) {
            ((WiFiLinkCompleteActivity) mContext).ipcLinkWiFiCallBack(param2);
        }

        XLog.tag(LogConst.TAG_CONNECTION).i("Ipc connection success");

        if (isBindSucess() || getEnableSingleWifi()) {
            GolukDebugUtils.e("", "=========IPC_VDCP_Command_Init_CallBack：" + param2);
            IpcConnSuccessInfo ipcInfo = null;
            if (null != param2) {
                ipcInfo = GolukFastJsonUtil.getParseObj((String) param2, IpcConnSuccessInfo.class);
                ipcInfo.lasttime = String.valueOf(System.currentTimeMillis());
                mIpcVersion = ipcInfo.version;
            }
            if (getEnableSingleWifi()) {
                mIPCControlManager.setBindStatus();
            }

            // 保存ipc设备型号,是G1, G2 还是T1
            saveIpcProductName(ipcInfo);
            // ipc控制初始化成功,可以看画面和拍摄8s视频
            setIpcLoginState(true);
            // 获取音视频配置信息
            getVideoEncodeCfg();
            // 获取Ｔ1声音录制开关状态
            getVideoEncoderCtg_T1();
            /** 获取adas配置 **/
            getAdasCfg();
            // 获取设备编号
            getIPCNumber();
            isconnection = true;// 连接成功
            setSyncCount();
            EventBus.getDefault().post(new EventPhotoUpdateLoginState(EventConfig.PHOTO_ALBUM_UPDATE_LOGIN_STATE));
            EventBus.getDefault().post(new EventIpcConnState(EventConfig.IPC_CONNECT));
            GolukApplication.getInstance().getIPCControlManager().getIPCSystemTime();
            // 获取ipc版本号
            GolukApplication.getInstance().getIPCControlManager().getVersion();
            queryNewFileList();
            if (null != mMainActivity) {
                mMainActivity.wiFiLinkStatus(2);
            }
            WifiBindDataCenter.getInstance().updateConnIpcType(mIPCControlManager.mProduceName);
            WifiBindDataCenter.getInstance().updateConnIpcType(ipcInfo);
        }
    }

    // 保存ipc设备型号
    private void saveIpcProductName(IpcConnSuccessInfo ipcInfo) {
        if (null != ipcInfo && !TextUtils.isEmpty(ipcInfo.productname)) {
            mIPCControlManager.setProduceName(ipcInfo.productname);
            // 保存设备型号
            SharedPrefUtil.saveIpcModel(mIPCControlManager.mProduceName);
            XLog.tag(LogConst.TAG_CONNECTION).i("Ipc info: %s %s %s",ipcInfo.productname, ipcInfo.serial, ipcInfo.version);
        }
    }

    // msg = 1000 多文件目录查询
    private void IPC_VDCP_Resp_Query_CallBack(int msg, int param1, Object param2) {
        if (RESULE_SUCESS == param1) {
            GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==5555===stopDownloadList" + param2);
            if ("ipcfilemanager".equals(mPageSource)) {
                return;
            }
            if (!mIsQuery) {
                return;
            } else {
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
                    GolukDebugUtils.i("lily", "=====保存当前的ipcVersion=====" + ipcVersion);
                    // 保存ipc版本号
                    SharedPrefUtil.saveIPCVersion(ipcVersion);
                    // 发送更新Event
                    EventBus.getDefault().post(new IpcInfoUpdate());

                    // 获取设备Exception信息
                    mHandler.removeMessages(MSG_TYPE_QUERY_IPC_EXCEPTION_LIST);
                    mHandler.sendEmptyMessageDelayed(MSG_TYPE_QUERY_IPC_EXCEPTION_LIST, 6000);
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
                // msg = 1000 多文件目录查询
                IPC_VDCP_Resp_Query_CallBack(msg, param1, param2);
                break;
            case IPC_VDCP_Msg_SingleQuery:
                // msg = 1001 单文件查询
                // 拍摄8秒视频成功之后,接口会自动调用查询这个文件,收到这个回调之后可以根据文件名去下载视频
                ipcVideoSingleQueryCallBack(param1, (String) param2);
                break;
            case IPC_VDCPCmd_SetWifiCfg:
                // msg = 1012 设置IPC系统WIFI配置
                // param1 = 0 成功 | 失败
                // 如果在wifi连接页面,通知设置成功
                if (mPageSource.equals("WiFiLinkBindAll")) {
                    ((WiFiLinkCompleteActivity) mContext).setIpcLinkWiFiCallBack(param1);
                } else if (mPageSource.equals("changePassword")) {
                    ((UserSetupChangeWifiActivity) mContext).setIpcLinkWiFiCallBack(param1);
                } else if (mPageSource.equals("changewifi")) {
                    ((UserSetupWifiActivity) mContext).setIpcLinkWiFiCallBack(param1);
                }
                break;
            case IPC_VDCPCmd_SetWirelessMode:
                if (mPageSource.equals("WiFiLinkBindAll")) {
                    ((WiFiLinkCompleteActivity) mContext).changeT3WifiMode(param1);
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
                // T1的设置声音录制开关回调
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
                // 开始直播
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
            // 未发起过直播
            try {
                VdcpLiveBean bean = GolukFastJsonUtil.getParseObj((String) param2, VdcpLiveBean.class);
                if ("sending".equals(bean.content)) {
                    isT1Success = true;
                    checkContinueLive();
                }
            } catch (Exception e) {
            }
        }

        if (null != mLiveOperater) {
            mLiveOperater.CallBack_Ipc(msg, param1, param2);
        }
    }

    private void IPC_VDTP_ConnectState_CallBack(int msg, int param1, Object param2) {
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

    private void IPC_VDTP_Resp_CallBack(int msg, int param1, Object param2) {
        switch (msg) {
            case IPC_VDTP_Msg_File:
                // 文件传输中消息 msg = 0
                // param1 = 0,下载完成
                // param1 = 1,下载中
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
        // IPC下载连接状态 event = 2
        if (ENetTransEvent_IPC_VDTP_ConnectState == event) {
            IPC_VDTP_ConnectState_CallBack(msg, param1, param2);
        }
        // IPC下载结果应答,开始下载视频文件 event = 3
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
                // 发送更新Event
                EventBus.getDefault().post(new IpcInfoUpdate());

                mIPCControlManager.reportBindMsg();
            }
        }
    }

    @Override
    public void TalkNotifyCallBack(int type, String data) {
    }

    // 获取T1设备的 声音录制的开关
    private void getVideoEncoderCtg_T1() {
        if (GolukApplication.getInstance().getIpcIsLogin()) {
            mIPCControlManager.getAudioCfg_T1();
        }
    }

    /**
     * 获取音视频配置信息
     *
     * @author xuhw
     * @date 2015年4月10日
     */
    private void getVideoEncodeCfg() {
        if (GolukApplication.getInstance().getIpcIsLogin()) {
            getIPCControlManager().getVideoEncodeCfg(0);
        }
    }

    /**
     * 获取adas配置信息
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
        // 定位回调
        if (null == mLocationHashMap) {
            return;
        }

        GolukPosition location = JsonUtil.parseLocatoinJson(locationJson);
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

    // 设置同步数量
    private void setSyncCount() {
        GolukDebugUtils.e("", "sync count ---application-----setSyncCount ---1");
        if (!isBindSucess()) {
            return;
        }
        int syncFlag = SettingUtils.getInstance().getInt(UserSetupActivity.MANUAL_SWITCH, -1);

        GolukDebugUtils.e("", "sync count ---application-----setSyncCount ---2:  " + syncFlag + "   nane: "
                + mIPCControlManager.mProduceName);
        /** 初始没有设置同步数量，根据连接设备类型G1，T1S设置自动同步5条，其他设备自动同步20条 **/
        if (syncFlag == -1) {
            // if (IPCControlManager.G1_SIGN.equals(mIPCControlManager.mProduceName)
            //     || IPCControlManager.T1s_SIGN.equalsIgnoreCase(mIPCControlManager.mProduceName)) {
            SettingUtils.getInstance().putInt(UserSetupActivity.MANUAL_SWITCH, 5);
//            } else {
//                SettingUtils.getInstance().putInt(UserSetupActivity.MANUAL_SWITCH, 20);
//            }
        }
    }

    /**
     * 判断是否可以同步5个最新的视频
     *
     * @return true/false 可以/不可以
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
     * 设置是否在绑定过程中
     *
     * @param isbind true/false 绑定中/未绑定中
     * @author jyf
     */
    public void setBinding(boolean isbind) {
        isBinding = isbind;
    }

    public boolean isBindSucess() {
        return WifiBindDataCenter.getInstance().isHasDataHistory() && !isBinding;
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
        int syncFlag = SettingUtils.getInstance().getInt(UserSetupActivity.MANUAL_SWITCH, 5);
        GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==4444===stopDownloadList:   " + starttime + "  syncFlag: "
                + syncFlag);

        boolean flog = mIPCControlManager.queryFileListInfo(6, syncFlag, starttime, 2147483647, "0");
        if (flog) {
            mIsQuery = true;
        }
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
                }
            }
            mIPCControlManager.stopDownloadFile();
        }
    }

    public void userStopDownLoadList() {

        autodownloadfile = false;
        mIPCControlManager.stopDownloadFile();
        if (mDownLoadFileList.size() > 0) {
            if (mDownLoadFileList.size() >= mNoDownLoadFileList.size()) {
                //中止同步--视频自动同步
                ZhugeUtils.eventAutoSynchronizeVideo(mContext, mContext.getString(R.string.str_zhuge_synchronize_video_stop), (mDownLoadFileList.size() - mNoDownLoadFileList.size()));
            }
            mDownLoadFileList.clear();
            mNoDownLoadFileList.clear();
            if (GlobalWindow.getInstance().isShow()) {
                GlobalWindow.getInstance().toFailed(mContext.getString(R.string.str_global_cancel_success));
            }
        }

    }

    /**
     * IPC断开连接处理
     *
     * @author xuhw
     * @date 2015年4月24日
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
                    }

                    if (TextUtils.isEmpty(filePath)) {
                        continue;
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
     * 验证固定的几个activity 可以弹框
     *
     * @Description:
     * @return boolean
     * @author 曾浩
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
     * 获取当前登录用户的信息,　未登录則返回NULL
     *
     * @return 用户信息类 UserInfo
     * @author jyf
     * @date 2015年8月7日
     */
    public UserInfo getMyInfo() {
        UserInfo myInfo = null;
        try {
            String user = SharedPrefUtil.getUserInfo();

            Log.e("dengting", "getUserInfo------------------logic-userInfo2:" + user);

            if (null != user && !"".equals(user)) {
                myInfo = JsonUtil.parseSingleUserInfoJson(new JSONObject(user));
                XLog.i("User info: nickname:%s, userName:%s, uid:%s", myInfo.nickname, myInfo.phone, myInfo.uid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return myInfo;
    }

    public void setMyinfo(String name, String head, String desc, String url) {

        String user = SharedPrefUtil.getUserInfo();

        Log.e("dengting", "getUserInfo------------------logic-userInfo3:" + user);

        try {
            if (user != null && !"".equals(user)) {
                UserInfo myInfo = JsonUtil.parseSingleUserInfoJson(new JSONObject(user));
                if (name != null && !"".equals(name)) {
                    myInfo.nickname = name;
                }
                if (head != null && !"".equals(head)) {
                    myInfo.head = head;
                }
                if (!TextUtils.isEmpty(desc)) {
                    myInfo.desc = desc;
                }
                if (url != null) {
                    myInfo.customavatar = url;
                }

                SharedPrefUtil.saveUserInfo(JSON.toJSONString(myInfo));
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void setMyPhone(String phone) {
        String user = SharedPrefUtil.getUserInfo();
        try {
            if (user != null && !"".equals(user)) {
                UserInfo myInfo = JsonUtil.parseSingleUserInfoJson(new JSONObject(user));
                if (!TextUtils.isEmpty(phone)) {
                    myInfo.phone = phone;
                }
                SharedPrefUtil.saveUserInfo(JSON.toJSONString(myInfo));
            }
        } catch (JSONException e) {
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
        //直播页面
        ZhugeUtils.eventLive(this, this.getString(R.string.str_zhuge_share_video_network_other));

        GolukUtils.startPublishOrWatchLiveActivity(mContext, false, false, null, null, userInfo);
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
        try {
            if (!TextUtils.isEmpty(info)) {
                UserResult result = GolukFastJsonUtil.getParseObj(info, UserResult.class);
                result.data.nickname = "";
                result.data.desc = "";
                info = GolukFastJsonUtil.setParseObj(result);
            }
            GolukDebugUtils.e("", "login----GolukApplication---setLoginRespInfo----info: " + info);
            mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_SetLoginRespInfo,
                    info);
        } catch (Exception e) {

        }
    }

    public boolean getEnableSingleWifi() {
        enableSingleWifi = SharedPrefUtil.getEnableSingleWifi();
        return enableSingleWifi;
    }

    public void setEnableSingleWifi(boolean value) {
        if (value == enableSingleWifi) {
            return;
        }
        SharedPrefUtil.setEnableSingleWifi(value);
        enableSingleWifi = value;
    }

    public boolean isUserLoginToServerSuccess() {
        return (loginStatus == 1) || (autoLoginStatus == 2) || (autoLoginStatus == 1);
    }

    public boolean isDownloading() {
        return isDownloading;
    }


    public void disableWiFiAndLogOutDevice() {
        mIPCControlManager.setVdcpDisconnect();
        setIpcLoginOut();
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            //wifiManager.disableNetwork(wifiInfo.getNetworkId());
            wifiManager.disconnect();
        }
    }

}
