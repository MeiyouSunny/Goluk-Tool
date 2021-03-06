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
import com.mobnote.golukmain.BuildConfig;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpinionActivity;
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
import com.mobnote.golukmain.userlogin.UserData;
import com.mobnote.golukmain.userlogin.UserInfo;
import com.mobnote.golukmain.userlogin.UserResult;
import com.mobnote.golukmain.videosuqare.VideoCategoryActivity;
import com.mobnote.golukmain.videosuqare.VideoSquareManager;
import com.mobnote.golukmain.wifibind.IpcConnSuccessInfo;
import com.mobnote.golukmain.wifibind.WiFiLinkCompleteActivity;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifidatacenter.JsonWifiBindManager;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifimanage.WifiApAdmin;
import com.mobnote.log.app.AppLogOpreater;
import com.mobnote.log.app.AppLogOpreaterImpl;
import com.mobnote.log.app.LogConst;
import com.mobnote.map.LngLat;
import com.mobnote.t1sp.base.ui.BaseOnViewBindListener;
import com.mobnote.t1sp.connect.T1SPConnecter;
import com.mobnote.t1sp.download.DownloaderT1spImpl;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.user.IpcUpdateManage;
import com.mobnote.user.TimerManage;
import com.mobnote.user.User;
import com.mobnote.user.UserIdentifyManage;
import com.mobnote.user.UserLoginManage;
import com.mobnote.util.GolukFastJsonUtil;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.SortByDate;
import com.mobnote.util.ZhugeUtils;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.tencent.bugly.crashreport.CrashReport;
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

import androidx.multidex.MultiDexApplication;
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
import likly.dollar.$;
import likly.mvp.MVP;

//import com.mobnote.golukmain.xdpush.GolukNotification;

public class GolukApplication extends MultiDexApplication implements IPageNotifyFn, IPCManagerFn, ITalkFn, ILocationFn {
    /**
     * ????????????AppKey
     */
    public static final String RD_APP_KEY = "3ac4ec3b83ff2b46";
    /**
     * ????????????AppSecret
     */
    public static final String RD_APP_SECRET = "51b6e9f5866392a68f7515fe51746459y/UU+RE+9GGaQLMTsBmzZZ9XHD+pzBEmJZsUXfvf7Q9hRV/MhfXYlZt6GpPv/ESqFD4oaaXyQhkmOx19fTZDjtRUsQZcX4SlefXh+UHjrYrNVbglIUPiy97bTbS37twxWOffk4b/XNd8Wg3lTZkmiO2wgkB34JaD4CFfL15VsYPDY63zJ+wTH+wpFX5RpgAQ9aCEluTR601d6C88V+3JaXz9Knt0AsVOkpXK0f9GkZ5QseTeCSeA2EQqq8OFOW7IcV29dt29ENckb1g5+XqPhhSdqfCR5CUPNxZ74uk77tiJydXRAeV6T+oaTbnMm60MtVlx41iejAjCHvX9xL/vbrKrpu5HxBQKZT4hNFiJjHXDg5tFAKwIM/DEsL3TrMaK+qv0nEBDxXGUD8PQ3gzC/1Lv7YNSDo6TlcboVDH0M8B+e8iIsHkJEYsCfPw4c6N/okKZZ9Ofn75oSogZNiw252VzEgqfoCKWBkGQ+wm6NqkePR1icPN9VqXHGPzgFStYCMvNpZZ9HbitRwA9uV3Lu+KghzPcOEaS+VSo3sWDzh0=";

    /**
     * ????????????AppKey
     */
    public static final String RD_APP_KEY_INNATIONAL = "dc8c35492a5e8ccc";
    /**
     * ????????????AppSecret
     */
    public static final String RD_APP_SECRET_INNATIONAL = "bc1881461c91257c72d0559d6f66ef6dRYUWjkdE6/gct1k1nEGRMDNqGhylT2nMZyJRTzyhGruSYoW6Rn28rvy8E0OGkJgPCUWN19DpXnqzh6E0M8Z0OO7Ef0jwh2LwH2UR8YLY0tJ7c28erjR8N3mi9OncE4KRUqWi3hYodqPyeqQHQq/YvJp/lhMA+EhBeWPFQ8Nrk3YbgpeqgfdCUF4rOl6shl5f51piEXW6xd1gdOr7savvb4VTuxlg1X3bGmwzZzhcqzybDDqbj0AT8xWEbMoziId547elt6KeHHGiEEWrHwA6H6IFVOa26LSFyVBsefXr7Vf/yw7cIN+2NTLJpu8AtEmcyWplYFEvbrY9XKk2ZoQTiXI8hrRvPVTTpOCImbqt0l9BIKP1qFz1Tev1K8Yd4fpqyfDmQq6WI2GAf8YLqA42mj7V7DaPdAr42i1I6euQJGnhcnDSDJ3HT67NF3pnwqxMXZdjcBdfcigTaU800wpIiTMV40gJyRGDIu+LGIpprlal0Ekh04i53QTs/6WfF4u8TZMVDU6XL1W3cidmTWb49ozyWNapQL64nCBcHbZmZVo=";

    /**
     * JIN?????????
     */
    public GolukLogic mGoluk = null;
    /**
     * ip??????
     */
    public static String mIpcIp = null;
    /**
     * ???????????????
     */
    private Context mContext = null;
    /**
     * ????????????,????????????activity
     */
    private String mPageSource = "";
    /**
     * ??????activity
     */
    public static MainActivity mMainActivity = null;
    /**
     * ?????????????????? fs1:??????->sd???/goluk??????
     */
    private String mVideoSavePath = "fs1:/video/";

    private static GolukApplication instance = null;
    public IPCControlManager mIPCControlManager = null;
    private VideoSquareManager mVideoSquareManager = null;

    /**
     * ????????????????????????, ???????????????????????????????????????????????????
     */
    private boolean isBinding = false;
    /**
     * ??????IPC??????????????????
     */
    public boolean isIpcLoginSuccess = false;
    /**
     * ????????????IPC????????????
     */
    public boolean isIpcConnSuccess = false;
    /**
     * ?????????????????????????????????????????????
     */
    public boolean isUserLoginSucess = false;
    /**
     * CC??????????????????
     */
    public String mCCUrl = null;
    /**
     * ?????????????????????UID
     */
    public String mCurrentUId = null;
    /**
     * ?????????????????????Aid
     */
    public String mCurrentAid = null;

    /**
     * ???????????????????????????
     **/
    public String mCurrentPhoneNum = null;
    /**
     * ???????????????????????????
     */
    private String carrecorderCachePath = "";
    /**
     * ?????????????????????
     */
    private VideoConfigState mVideoConfigState = null;
    /**
     * ??????????????????????????????
     */
    private boolean autoRecordFlag = false;
    /**
     * ??????????????????
     */
    private int[] motioncfg;

    private WifiApAdmin wifiAp;
    /**
     * ????????????
     */
    public String mCurAddr = null;
    /**
     * ????????????????????? 0????????? 1 ???????????? 2???????????? 3??????????????????????????????????????? 4?????? 5????????????????????????????????????
     **/
    public int loginStatus;
    /**
     * ????????????????????? 1----??????/?????? ??? 2----??????/?????? ?????? 3---??????/?????? ?????? 4---code=500 5---code=405
     * 6----code=406 7----code=407 8---code=480 9---??????
     **/
    public int registStatus;
    /**
     * ??????????????????????????? 1??????????????? 2?????????????????? 3?????????????????? 4?????????????????? 5????????????
     **/
    public int autoLoginStatus;
    /**
     * ????????????
     **/
    public boolean loginoutStatus = false;
    /**
     * ?????????????????????????????? 0----????????? 1----???????????? 2----???????????? 3---code=201 4----code=500
     * 5----code=405 6----code=440 7----code=480 8----code=470
     **/
    public int identifyStatus;

    /**
     * User?????????
     **/
    public User mUser = null;
    /**
     * ???????????????
     **/
    public UserLoginManage mLoginManage = null;
    /**
     * ???????????????
     **/
    public IpcUpdateManage mIpcUpdateManage = null;
    /**
     * ????????????????????????
     **/
    public UserIdentifyManage mIdentifyManage = null;
    /**
     * ??????????????????
     **/
    public TimerManage mTimerManage = null;

    private HashMap<String, ILocationFn> mLocationHashMap = new HashMap<String, ILocationFn>();
    /**
     * ?????????????????????
     */
    private List<String> mNoDownLoadFileList;
    /**
     * ????????????????????????
     */
    private List<String> mDownLoadFileList;

    /**
     * ???????????????????????????
     */
    public boolean isconnection = false;
    /**
     * ????????????
     */
    private boolean isBackground = false;
    public long startTime = 0;
    public boolean autodownloadfile = false;
    /**
     * ?????????????????????????????????
     **/
    public boolean flag = false;
    /**
     * SD??????????????????
     */
    private boolean isSDCardFull = false;
    /**
     * ?????????????????????
     */
    private boolean isDownloading = false;
    /**
     * ??????????????????
     */
    private int downloadCount = 0;
    /**
     * ??????????????????????????????
     **/
    public boolean updateSuccess = false;
    /**
     * wifi????????????
     */
    public int mWiFiStatus = 0;


    private ArrayList<VideoFileInfo> fileList;

    private boolean mIsExit = true;
    /**
     * T1???????????????????????????????????????
     **/
    public int mT1RecAudioCfg = 1;
    /**
     * ????????????????????????
     */
    private boolean enableSingleWifi;
    /**
     * ?????????????????????
     */
    public boolean isAlreadyLive = false;

    private boolean mIsQuery = false;

    /**
     * ?????????????????????????????????????????? ?????????????????? T1U ????????? T1
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

//    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
//        @Override
//        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
//            if (newToken == null) {
//                // VKAccessToken is invalid
//            }
//        }
//    };

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
            initializeSDK();
        }

        // TODO ???????????????????????????????????????
    }

    /**
     * ??????SDK??????
     */
    public void initializeSDK() {
        if (isInitializeSDK) return;
        //??????sdk
        SDKInitializer.initialize(this);
        // ????????????????????????????????????
        WifiBindDataCenter.getInstance().setAdatper(new JsonWifiBindManager());
        GolukVideoInfoDbManager.getInstance().initDb(this.getApplicationContext());
        initXLog();
        ZhugeSDK.getInstance().init(getApplicationContext());

        // T1SP
        MVP.registerOnViewBindListener(new BaseOnViewBindListener());
        $.initialize(this);
        com.mobnote.t1sp.api.HttpManager.initHttp();
        goluk.com.t1s.api.HttpManager.initHttp();
        // Downloader
        DownloaderT1spImpl.init(this);
        // Connecter
        T1SPConnecter.instance().init(this);

        // Bugly
        String buglyId = isMainland() ? "900012751" : "900021946";
        CrashReport.initCrashReport(getApplicationContext(), buglyId, BuildConfig.DEBUG);

        isInitializeSDK = true;
    }


    /* ??????IPC?????? */
    private static final int MSG_TYPE_QUERY_IPC_EXCEPTION_LIST = 11;
    public Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
//            if (msg.what == MSG_TYPE_QUERY_IPC_EXCEPTION_LIST) {
//                // ????????????Exception??????
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

    public boolean XLogIsInit;
    private void initXLog() {
        AppLogOpreater appLogOpreater = new AppLogOpreaterImpl();
        appLogOpreater.deleteSurplusLogFile();

        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)            // Specify log level, logs below this level won't be printed, default: LogLevel.ALL
                .tag("goluk")                                         // Specify TAG, default: "X-LOG"
                .nt()                                                   // Enable thread info, disabled by default
                .st(1)                                                 // Enable stack trace info with depth 2, disabled by default
                .b()                                                   // Enable border, disabled by default
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
        XLogIsInit = true;
    }

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
        if (mTimerManage != null)
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
    public boolean isMainland() {
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
            GlobalWindow.getInstance().reset();
            JSONObject json = new JSONObject(data);
            final String fileName = json.getString("location");
            final long time = json.optLong("time");
            final double filesize = json.optDouble("size");
            final int type = json.getInt("type");
            final String savePath = getSavePath(type);
            //??????2.10 ?????????????????????????????????
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
                    if (filesize != 0)
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

                        // ?????????????????????
                        String filePath = FileUtil.getVideoSavePath(filename);
                        if (!TextUtils.isEmpty(filePath)) {
                            FileUtil.mediaScan(filePath);
                        }

                    } catch (Exception e) {
                    }

                    if (checkDownloadCompleteState()) {
                        //?????????--??????????????????
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
                        // ??????Event
                        EventUtil.sendDownloadCompleteEvent();
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
                    if (mContext instanceof VideoCategoryActivity) {
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
            case PageType_GetVCode:
                // ?????????????????????
                mIdentifyManage.getIdentifyCallback(success, param1, param2);
                break;
            // ??????PageType_Register
            case PageType_BindInfo:
                break;
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
//                GolukNotification.getInstance().getXg().golukServerRegisterCallBack(success, param1, param2);
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
     * T1???????????????????????????
     */
    private boolean isT1Success = false;

    private boolean isCanLive() {
        if (isCheckContinueLiveFinish) {
            GolukDebugUtils.e("", "newlive----Application---isCanLive----0");
            // ????????????
            return false;
        }
        if (!isIpcLoginSuccess || !isUserLoginSucess) {
            GolukDebugUtils.e("", "newlive----Application---isCanLive----1");
            return false;
        }
        return true;
    }

    //??????????????????
    public void checkContinueLive() {
        // ?????????T1??????IPC??????????????????????????????
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
     * ??????????????????
     * <p/>
     * 1/?????? ??????/??????
     * ??????????????????
     *
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
            this.checkContinueLive();
            GolukDebugUtils.e(null, "jyf---------GolukApplication---------mCCurl:" + mCCUrl + " uid:" + mCurrentUId
                    + " aid:" + mCurrentAid);
        }
    }

    // ???????????????????????????????????????
    private boolean mCanNotUse;

    public void setCanNotUse(boolean canNotUse) {
        mCanNotUse = canNotUse;
    }

    public boolean canNotUse() {
        return mCanNotUse;
    }

    // ??????????????????
    public void setIpcLoginState(boolean isSucess) {
        isIpcLoginSuccess = isSucess;
        isIpcConnSuccess = isSucess;
        if (isSucess) {
            checkContinueLive();
        }
        if (!isSucess) {
            mCanNotUse = false;
        }
    }

    /**
     * ???????????????T1SP??????????????????
     */
    private boolean isT1SPAndConnected() {
        return mIPCControlManager.isT2S() && isIpcConnSuccess;
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
                XLog.tag(LogConst.TAG_CONNECTION).i("IPC_VDCP_Connect_CallBack: state idel");
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
//                    if (null != mMainActivity) {
//                        mMainActivity.wiFiLinkStatus(1);
//                    }
                }
                XLog.tag(LogConst.TAG_CONNECTION).i("IPC_VDCP_Connect_CallBack: state connecting");
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
                XLog.tag(LogConst.TAG_CONNECTION).i("IPC_VDCP_Connect_CallBack: state disconnected");
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
            if (!((WiFiLinkListActivity) mContext).ipcSucessCallBack(param2)) {
                return;
            }
        }
        // ?????????wifi????????????,??????????????????
        if (mPageSource.equals("WiFiLinkBindAll")) {
            ((WiFiLinkCompleteActivity) mContext).ipcLinkWiFiCallBack(param2);
        }

        XLog.tag(LogConst.TAG_CONNECTION).i("Ipc connection success");

        if (isBindSucess() || getEnableSingleWifi()) {
            GolukDebugUtils.e("", "=========IPC_VDCP_Command_Init_CallBack???" + param2);
            IpcConnSuccessInfo ipcInfo = null;
            if (null != param2) {
                ipcInfo = GolukFastJsonUtil.getParseObj((String) param2, IpcConnSuccessInfo.class);
                ipcInfo.lasttime = String.valueOf(System.currentTimeMillis());
                mIpcVersion = ipcInfo.version;
            }
            if (getEnableSingleWifi()) {
                mIPCControlManager.setBindStatus();
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
            XLog.tag(LogConst.TAG_CONNECTION).i("Ipc info: %s %s %s",ipcInfo.productname, ipcInfo.serial, ipcInfo.version);
        }
    }

    // msg = 1000 ?????????????????????
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
                    GolukDebugUtils.i("lily", "=====???????????????ipcVersion=====" + ipcVersion);
                    // ??????ipc?????????
                    SharedPrefUtil.saveIPCVersion(ipcVersion);
                    // ????????????Event
                    EventBus.getDefault().post(new IpcInfoUpdate());

                    // ????????????Exception??????
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
                    if (!mIPCControlManager.isT3Relative() && !mIPCControlManager.isG1Relative()) {
                        getVideoEncodeCfg();
                    }
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
                break;
            case IPC_VDCP_Msg_LiveStop:

                break;
        }
    }

    private void IPC_VDCP_PushEvent_Comm(int msg, int param1, Object param2) {
        if (RESULE_SUCESS != param1) {
            GolukDebugUtils.e("", "newlive-----GolukApplication----IPC_VDCP_PushEvent_Comm:  " + param2);
            return;
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
        // T1SP??????
        if (ENetTransEvent_IPC_VDCP_ConnectState == event && !isT1SPAndConnected()) {
            IPC_VDCP_Connect_CallBack(msg, param1, param2);
        }
        // T1SP??????
        if (ENetTransEvent_IPC_VDCP_CommandResp == event && !isT1SPAndConnected()) {
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
                // ????????????Event
                EventBus.getDefault().post(new IpcInfoUpdate());

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
        if (mIPCControlManager.isT3Relative() || mIPCControlManager.isG1Relative()) {
            mVideoConfigState = new VideoConfigState();
            mVideoConfigState.bitstreams = 0;
            mVideoConfigState.resolution = "1080P";
            mVideoConfigState.frameRate = 30;
            mVideoConfigState.bitrate = 10240;
            mVideoConfigState.AudioEnabled = 1;
        } else {
            getIPCControlManager().getVideoEncodeCfg(0);
        }
    }

    /**
     * ??????adas????????????
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

    // ??????????????????
    private void setSyncCount() {
    }

    /**
     * ????????????????????????5??????????????????
     *
     * @return true/false ??????/?????????
     * @author jyf
     */
    private boolean isCanQueryNewFile() {
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
     * @param isbind true/false ?????????/????????????
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
            if (mDownLoadFileList.size() >= mNoDownLoadFileList.size()) {
                //????????????--??????????????????
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
        //????????????
        ZhugeUtils.eventLive(this, this.getString(R.string.str_zhuge_share_video_network_other));

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
