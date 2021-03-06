package com.mobnote.golukmain;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;

import com.google.widget.FragmentTabHost;
import com.mobnote.application.GlobalWindow;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.Event;
import com.mobnote.eventbus.EventBindFinish;
import com.mobnote.eventbus.EventBindResult;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDownloadVideoFinish;
import com.mobnote.eventbus.EventFollowPush;
import com.mobnote.eventbus.EventMapQuery;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.eventbus.EventPhotoUpdateDate;
import com.mobnote.eventbus.EventUserLoginRet;
import com.mobnote.eventbus.EventUtil;
import com.mobnote.eventbus.EventWifiAuto;
import com.mobnote.eventbus.EventWifiConnect;
import com.mobnote.eventbus.EventWifiState;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.golukmain.wifibind.WiFiInfo;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.golukmain.xdpush.StartAppBean;
import com.mobnote.manager.MessageManager;
import com.mobnote.receiver.NetworkStateReceiver;
import com.mobnote.t1sp.ui.preview.CarRecorderT1SPActivity;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.util.CrashReportUtil;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.wifibind.WifiConnCallBack;
import com.mobnote.wifibind.WifiConnectManager;
import com.mobnote.wifibind.WifiRsBean;
import com.rd.car.CarRecorderManager;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerAdapter;
import cn.com.mobnote.module.location.LocationNotifyAdapter;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.page.PageNotifyAdapter;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.module.talk.TalkNotifyAdapter;
import cn.com.mobnote.module.videosquare.VideoSquareManagerAdapter;
import cn.com.tiros.api.Tapi;
import cn.com.tiros.baidu.BaiduLocation;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

@SuppressLint({"HandlerLeak", "NewApi"})
public class MainActivity extends BaseActivity implements WifiConnCallBack, IRequestResultListener {
    public static final String INTENT_ACTION_RETURN_MAIN_ALBUM = "returnToAlbum";

    /**
     * ??????????????????20????????????????????????IPC??????
     */
    private final int MSG_H_WIFICONN_TIME = 100;
    /**
     * application
     */
    public GolukApplication mApp = null;

    /**
     * wifi??????manage
     */
    private WifiConnectManager mWac = null;

    /**
     * ??????????????????
     **/
    public SharedPreferences mPreferencesAuto;
    public boolean isFirstLogin;

    /**
     * ?????????
     */
    public static final int WIFI_STATE_FAILED = 0;
    /**
     * ?????????
     */
    public static final int WIFI_STATE_CONNING = 1;
    /**
     * ??????
     */
    public static final int WIFI_STATE_SUCCESS = 2;

    /**
     * ??????ipc????????????
     */
    Animation anim = null;

    private SharedPreferences mPreferences = null;
    private Editor mEditor = null;
    private long exitTime = 0;

    private View mUnreadTips;
    private ImageView mFollowedVideoTipIV;

    private WifiManager mWifiManager = null;
    // Play video sync from camera completion sound
    private SoundPool mSoundPool;
    private final static String TAG = "MainActivity";
    private StartAppBean mStartAppBean = null;
    /**
     * ?????????????????????????????????????????????????????????????????????????????????
     */
    private WifiRsBean mCurrentConnBean = null;
    private FragmentTabHost mTabHost;

    private ImageView mCarrecorderIv;
    //private ViewStub mGuideMainViewStub;

    private void playDownLoadedSound() {
        if (null != mSoundPool) {
            mSoundPool.load(this, R.raw.ec_alert5, 1);

            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    soundPool.play(sampleId, 1, 1, 1, 0, 1);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        GolukDebugUtils.e("", "crash zh start App ------ MainActivity-----onCreate------------:");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        initView();
        // ?????????SDK????????????????????????context???????????????ApplicationContext
        // ?????????????????????setContentView??????????????????
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        // Register EventBus
        EventBus.getDefault().register(this);
        initThirdSDK();

        // ??????GolukApplication??????
        mApp = (GolukApplication) getApplication();
        mApp.setContext(this, "Main");
        mApp.getEnableSingleWifi();
        initConfig();
        // ???????????????,??????????????????
        mApp.startTime = System.currentTimeMillis();
        // ???????????????,??????????????????
        init();

        // ?????????????????????????????? ?????????????????????????????????
        mWac = new WifiConnectManager(mWifiManager, this);
        mCurrentConnBean = mWac.readConfig();
        refreshIpcDataToFile();

        // ????????????????????????????????????????????????????????????????????????
        mPreferencesAuto = getSharedPreferences("firstLogin", MODE_PRIVATE);
        isFirstLogin = mPreferencesAuto.getBoolean("FirstLogin", true);
        if (!isFirstLogin && !mApp.isUserLoginSucess) {
            mApp.mUser.initAutoLogin();
        }

        if (NetworkStateReceiver.isNetworkAvailable(this)) {
            notifyLogicNetWorkState(true);
        }
        GolukUtils.getMobileInfo(this);

        BaiduLocation.getInstance().startLocation();

        // ????????????
        networkStateReceiver = new NetworkStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkStateReceiver, intentFilter);

        // ?????????????????????
        requestOverlayAuthority();
        requestWriteStorage();
    }

    private void initConfig() {
        if (mApp == null)
            return;
        mApp.initializeSDK();
        mApp.setContext(this, "GuideActivity");
        mApp.initLogic();
        mApp.startUpgrade();
    }

    NetworkStateReceiver networkStateReceiver;

    public void onEventMainThread(Event event) {
        if (EventUtil.isNotInChinaEvent(event) && GolukApplication.getInstance().isMainland()) {
            // ????????????App???????????????????????????
            showChinaAppInOverseasAlert();
        }
    }

    AlertDialog mDialogNotInChina;

    private synchronized void showChinaAppInOverseasAlert() {
        if (mDialogNotInChina == null)
            mDialogNotInChina = new AlertDialog.Builder(this)
                    .setMessage(R.string.not_in_china_alert)
                    .setPositiveButton(R.string.close_app, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                            // ????????????
                            Process.killProcess(Process.myPid());
                        }
                    })
                    .setCancelable(false)
                    .create();
        if (!mDialogNotInChina.isShowing())
            mDialogNotInChina.show();
    }

    private void initView() {
//        mGuideMainViewStub = (ViewStub) findViewById(R.id.viewstub_guide_main);
//        mGuideMainViewStub.setOnInflateListener(new OnInflateListener() {
//
//            @Override
//            public void onInflate(ViewStub stub, View inflated) {
//                inflated.setOnTouchListener(new OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        mGuideMainViewStub.setVisibility(View.GONE);
//                        return false;
//                    }
//                });
//            }
//        });

        LayoutInflater inflater = LayoutInflater.from(this);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.fl_main_tab_content);

        Bundle b = new Bundle();
//        b.putString("key", "Discover");
//        LinearLayout discover = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_discover, null);
//        mTabHost.addTab(mTabHost.newTabSpec("Discover").setIndicator(discover), FragmentDiscover.class, b);

//        b = new Bundle();
//        b.putString("key", "Follow");
//        RelativeLayout follow = (RelativeLayout) inflater.inflate(R.layout.main_tab_indicator_follow, null);
//        mTabHost.addTab(mTabHost.newTabSpec("Follow").setIndicator(follow), FragmentFollowed.class, b);
//        mFollowedVideoTipIV = (ImageView) follow.findViewById(R.id.iv_new_followed_video_tips);

//        b = new Bundle();
//        b.putString("key", "CarRecorder");
//        LinearLayout carRecorder = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_carrecorder, null);
//        mCarrecorderIv = (ImageView) carRecorder.findViewById(R.id.tab_host_carrecorder_iv);
//        mTabHost.addTab(mTabHost.newTabSpec("CarRecorder").setIndicator(carRecorder), null, b);

        b = new Bundle();
        b.putString("key", "CarRecorder");
        LinearLayout carRecorder = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_device, null);
        mTabHost.addTab(mTabHost.newTabSpec("CarRecorder").setIndicator(carRecorder), FragmentDevice.class, b);

        b = new Bundle();
        b.putString("key", "Album");
        b.putString("platform", "0");
        LinearLayout album = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_album, null);
        mTabHost.addTab(mTabHost.newTabSpec("Album").setIndicator(album), FragmentAlbum.class, b);

        b = new Bundle();
        b.putString("key", "Mine");
        RelativeLayout mine = (RelativeLayout) inflater.inflate(R.layout.main_tab_indicator_mine, null);
        mUnreadTips = mine.findViewById(R.id.iv_unread_tips);
        mTabHost.addTab(mTabHost.newTabSpec("Mine").setIndicator(mine), FragmentMine.class, b);

        TabWidget widget = mTabHost.getTabWidget();
        widget.setDividerDrawable(null);
        mTabHost.getTabWidget().setBackgroundResource(R.color.color_main_tab_bg);
        View lineView = new View(this);
        lineView.setBackgroundResource(R.color.color_list_divider);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3);
        lineView.setLayoutParams(lineParams);
        mTabHost.addView(lineView);

        for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
            mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = (int) this.getResources().getDimension(
                    R.dimen.mainactivity_bottom_height);
        }

//        mTabHost.getTabWidget().getChildTabViewAt(0).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                connectGoluk(false);
//            }
//        });

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if ("Follow".equals(tabId)) {
                    mFollowedVideoTipIV.setVisibility(View.GONE);
                }
            }
        });
    }

    public void connectGoluk(boolean returnToMainActivityWhenSuccess) {
        if (mApp.isIpcLoginSuccess) {
//            if (mApp.canNotUse()) {
//                GolukUtils.showToast(this, getResources().getString(R.string.interantion_ban_mainland_goluk));
//            } else {
            Intent intent = new Intent();
            if (mApp.getIPCControlManager().isT2S()) {
                // T2S
                intent.setClass(this, CarRecorderT1SPActivity.class);
            } else {
                // Other
                intent.setClass(this, CarRecorderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
            startActivity(intent);
//            }
            return;
        }
        Intent intent = new Intent(MainActivity.this, WiFiLinkListActivity.class);
        intent.putExtra(INTENT_ACTION_RETURN_MAIN_ALBUM, returnToMainActivityWhenSuccess);
        startActivity(intent);

//        } else {
//            Intent intent = new Intent(this, WifiHistorySelectListActivity.class);
//            intent.putExtra(INTENT_ACTION_RETURN_MAIN_ALBUM, returnToMainActivityWhenSuccess);
//            startActivity(intent);
//        }
    }

    public void setTabHostVisibility(boolean visible) {
        if (visible) {
            mTabHost.setVisibility(View.VISIBLE);
        } else {
            mTabHost.setVisibility(View.GONE);
        }
    }

    private void msgRequest() {
        if (GolukApplication.getInstance().isUserLoginSucess) {
        }
    }

    private void followCountRequest() {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GolukDebugUtils.i("newintent", "-------str------------" + intent.getStringExtra("showMe"));
        if (null != intent.getStringExtra("showMe")) {
            String str = intent.getStringExtra("showMe").toString();
            if ("showMe".equals(str)) {

            }
        }
    }

    /**
     * ??????????????????SDK
     *
     * @author jyf
     * @date 2015???6???17???
     */
    private void initThirdSDK() {
        // ???????????????????????? ?????????SDK
        String appId = null;
        if (GolukApplication.getInstance().isMainland()) {
            appId = CrashReportUtil.BUGLY_RELEASE_APPID_GOLUK_INTERNAL;
        } else {
            appId = CrashReportUtil.BUGLY_RELEASE_APPID_GOLUK_INTERNATIONAL;
        }
        boolean isDebug = false;
        if (GolukUtils.isTestServer()) {
            appId = CrashReportUtil.BUGLY_DEV_APPID_GOLUK;
            isDebug = true;
        }
        CrashReport.initCrashReport(getApplicationContext(), appId, isDebug);
        //GolukDebugUtils.BUGLY_ENABLE = isDebug;
        final String mobileId = Tapi.getMobileId();
        CrashReport.setUserId(mobileId);
        GolukDebugUtils.e("", "jyf-----MainActivity-----mobileId:" + mobileId);
    }

    /**
     * ???????????????,??????????????????,????????????
     */
    private void init() {
        boolean hotPointState = SettingUtils.getInstance().getBoolean("HotPointState", false);
        updateHotPointState(hotPointState);
    }

    @Override
    protected void hMessage(Message msg) {
        switch (msg.what) {
            case MSG_H_WIFICONN_TIME:
                // ?????????????????????
                try {
                    throw new RuntimeException("Main Activity auto connect time out :40 s");
                } catch (Exception e) {
                    CrashReport.postCatchedException(e);
                }
                this.wifiConnectFailed();
                break;
        }
    }

    /**
     * ??????Logic???????????????
     *
     * @param isConnected true/false ????????????/?????????
     * @author jiayf
     * @date Apr 13, 2015
     */
    private void notifyLogicNetWorkState(boolean isConnected) {
        if (null == mApp.mGoluk) {
            return;
        }
        GolukDebugUtils.e("", "net-----state-----11111");
        final String connJson = JsonUtil.getNetStateJson(isConnected);
        mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_MessageReport,
                IMessageReportFn.REPORT_CMD_NET_STATA_CHG, connJson);
        if (isConnected) {
            GolukDebugUtils.e("", "net-----state-----2222");
            mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, ITalkFn.Talk_CommCmd_RecoveryNetwork, "");
        }
    }

    /**
     * ??????????????????
     */
    public void videoAnalyzeComplete(String str) {
        try {
            JSONObject json = new JSONObject(str);
            String tag = json.getString("tag");
            String filename = json.optString("filename");
            long time = json.optLong("filetime");
            if (tag.equals("videodownload")) {
                // ?????????????????????????????????
                playDownLoadedSound();

                if (!IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                        && !IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
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
                } else {
                    time += 1;
                }

                // ?????????????????????????????????
                long oldtime = SettingUtils.getInstance().getLong("downloadfiletime");
                time = time > oldtime ? time : oldtime;
                SettingUtils.getInstance().putLong("downloadfiletime", time);

                GolukDebugUtils.e("xuhw", "BBBB=====stopDownloadList==8888===stopDownloadList" + time);
                updateHotPointState(true);

                EventBus.getDefault().post(new EventPhotoUpdateDate(EventConfig.PHOTO_ALBUM_UPDATE_DATE, filename));
                EventBus.getDefault().post(new EventDownloadVideoFinish());
                GFileUtils.writeIPCLog("YYYYYY===@@@@@@==2222==downloadfiletime=" + time);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????????????????
     */
    public void wiFiLinkStatus(int status) {
        GolukDebugUtils
                .e("", "jyf-----MainActivity----wifiConn----wiFiLinkStatus-------------wiFiLinkStatus:" + status);
        mApp.mWiFiStatus = 0;
        switch (status) {
            case 1:
                // ?????????
                this.updateRecoderBtn(1);
                mApp.mWiFiStatus = WIFI_STATE_CONNING;
                EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_CONNING));
                break;
            case 2:
                // ?????????
                this.updateRecoderBtn(2);
                mApp.mWiFiStatus = WIFI_STATE_SUCCESS;
                wifiConnectedSucess();
                break;
            case 3:
                // ?????????
                this.updateRecoderBtn(0);
                mApp.mWiFiStatus = WIFI_STATE_FAILED;
                wifiConnectFailed();
                break;
        }
    }

    /**
     * ??????????????????????????? 1:????????? 2???????????? 0????????????
     */
    public void updateRecoderBtn(int state) {

//        if (this.isFinishing() == false) {
//            AnimationDrawable ad = null;
//            if (state == WIFI_STATE_CONNING && mApp.isBindSucess()) {
//                mCarrecorderIv.setImageResource(R.drawable.carrecoder_btn);
//                ad = (AnimationDrawable) mCarrecorderIv.getDrawable();
//                if (ad.isRunning() == false) {
//                    ad.setOneShot(false);
//                    ad.start();
//                }
//            } else if (state == WIFI_STATE_SUCCESS) {
//                Toast.makeText(MainActivity.this, getResources().getString(R.string.wifi_link_success_conn),
//                        Toast.LENGTH_LONG).show();
//                mCarrecorderIv.setImageResource(R.drawable.tb_car_recorder_connected);
//            } else if (state == WIFI_STATE_FAILED) {
//                mCarrecorderIv.setImageResource(R.drawable.tb_car_recorder_not_connected);
//            } else {
//                mCarrecorderIv.setImageResource(R.drawable.tb_car_recorder_not_connected);
//            }
//        }
    }

    private void startWifi() {
        GolukDebugUtils.e("", "wifiCallBack-------------startWifi:");
        if (WIFI_STATE_CONNING == mApp.mWiFiStatus) {
            return;
        }
        mApp.mWiFiStatus = WIFI_STATE_CONNING;
    }

    // ????????????
    private void wifiConnectedSucess() {
        GolukDebugUtils.e("", "wifiCallBack-------------wifiConnectedSucess:");
        mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
        mApp.mWiFiStatus = WIFI_STATE_SUCCESS;
        refreshIpcDataToFile();
        EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_SUCCESS));
    }

    // ????????????
    private void wifiConnectFailed() {
        GolukDebugUtils.e("", "wifiCallBack-------------wifiConnectFailed:");
        mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
        mApp.mWiFiStatus = WIFI_STATE_FAILED;
        updateRecoderBtn(mApp.mWiFiStatus);

        EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_FAILED));
    }

    public void onEventMainThread(EventFollowPush event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.FOLLOW_PUSH:
                if (mTabHost != null) {
                    mTabHost.setCurrentTab(4);
                }
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(EventUserLoginRet event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.USER_LOGIN_RET:
                if (mFollowedVideoTipIV != null) {
                    if (event.getFollowedVideoNum() > 0) {
                        mFollowedVideoTipIV.setVisibility(View.VISIBLE);
                    } else {
                        mFollowedVideoTipIV.setVisibility(View.GONE);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(EventBindFinish event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.CAR_RECORDER_BIND_CREATEAP:
                createPhoneHot(event.bean);
                break;
            case EventConfig.BIND_LIST_DELETE_CONFIG:
                this.clearWifiConfig();
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(EventMessageUpdate event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.MESSAGE_UPDATE:
                int msgCount = MessageManager.getMessageManager().getMessageTotalCount();
                setMessageTipCount(msgCount);
                break;
            case EventConfig.MESSAGE_REQUEST:
                msgRequest();
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(EventBindResult event) {
        GolukDebugUtils.e("", "wifilist----MainActivity----onEventMainThread----EventBindResult----1");
        if (null == event) {
            return;
        }
        if (EventConfig.BIND_COMPLETE == event.getOpCode()) {
            GolukDebugUtils.e("", "wifilist----MainActivity----onEventMainThread----EventBindResult----set NULL");
            mCurrentConnBean = null;
        }
    }

    /**
     * ??????????????????wifi??????
     */
    private void autoConnWifi() {
        GolukDebugUtils.e("", "?????????????????????wifi---linkMobnoteWiFi---1");
        if (null == mWac) {
            mWac = new WifiConnectManager(mWifiManager, this);
        }
        mWac.autoWifiManage();
    }

    public void closeAp() {
        if (null != mWac) {
            mWac.closeAp();
        }
    }

    private void createPhoneHot(WifiBindHistoryBean bean) {
        mApp.setBinding(false);
        if (null == bean) {
            return;
        }

        GolukDebugUtils.e("", "wifibind----MainActivity  createPhoneHot--------ssid:" + bean.ipc_ssid);
        // ???????????????????????????ipc??????
        mApp.setIpcDisconnect();
        final String wifiName = bean.mobile_ssid;
        final String pwd = bean.mobile_pwd;
        String ipcssid = bean.ipc_ssid;
        String ipcmac = bean.ipc_mac;
        // ????????????????????????????????????
        startWifi();
        // ??????IPC????????????
        mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
        mBaseHandler.sendEmptyMessageDelayed(MSG_H_WIFICONN_TIME, 40 * 1000);
        mWac = new WifiConnectManager(mWifiManager, this);

        WifiRsBean beans = new WifiRsBean();
        beans.setIpc_mac(bean.ipc_mac);
        beans.setIpc_ssid(bean.ipc_ssid);
        beans.setIpc_pass(bean.ipc_pwd);
        beans.setIpc_ip(bean.ipc_ip);
        beans.setPh_ssid(bean.mobile_ssid);
        beans.setPh_pass(bean.mobile_pwd);
        mWac.saveConfiguration(beans);

        mWac.createWifiAP(wifiName, pwd, ipcssid, ipcmac);
    }

    public void onEventMainThread(EventMapQuery event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.LIVE_MAP_QUERY:
                // ??????????????????????????????
                mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetPinData, "");
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(EventWifiState event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.WIFI_STATE:
                // ????????????????????????????????????
                // ??????????????????
                notifyLogicNetWorkState(event.getMsg());
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("isIPCMatch");
        }
        if (null != mSoundPool) {
            mSoundPool.release();
            mSoundPool = null;
        }

        try {
            // ?????????????????????
            CarRecorderManager.onExit(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Unregister EventBus
        EventBus.getDefault().unregister(this);
        // mBannerLoaded = false;

        unregisterReceiver(networkStateReceiver);

        // ????????????????????????
        FileUtil.deleteThumbCache();
    }

    @Override
    protected void onResume() {
        // GolukApplication.getInstance().queryNewFileList();
        GolukDebugUtils.e("", "crash zh start App ------ MainActivity-----onResume------------:");
        mApp.setContext(this, "Main");

        mApp.setBinding(false);

        if (mApp.isIpcLoginSuccess && mApp.isNeedCheckLive) {
            mApp.isNeedCheckLive = false;
            mApp.isCheckContinueLiveFinish = true;
            //requestIsAlive();
        }

        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("isIPCMatch");
        }

        if (!mApp.isIpcLoginSuccess) {
            this.updateRecoderBtn(mApp.mWiFiStatus);
        }

        followCountRequest();

        super.onResume();
    }

    public void requestIsAlive() {
        SharedPrefUtil.setIsLiveNormalExit(true);
        mApp.isNeedCheckLive = false;
        mApp.isCheckContinueLiveFinish = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            GolukUtils.showToast(getApplicationContext(), getString(R.string.str_double_click_to_exit_app));
            exitTime = System.currentTimeMillis();
        } else {
            FileUtil.deleteThumbCache();

            mApp.setExit(true);
            mApp.mHandler.removeMessages(1001);
            mApp.mHandler.removeMessages(1002);
            mApp.mHandler.removeMessages(1003);
            GolukVideoInfoDbManager.getInstance().destroy();
            unregisterListener();
            mApp.mIPCControlManager.setVdcpDisconnect();
            mApp.setIpcLoginOut();
            mApp.mUser.exitApp();
            mApp.mTimerManage.timerCancel();
            closeWifiHot();
            GlobalWindow.getInstance().dimissGlobalWindow();
            mApp.destroyLogic();
            mApp.appFree();
            if (!isDestroyed())
                finish();
        }
    }

    private void unregisterListener() {
        PageNotifyAdapter.setNotify(null);
        TalkNotifyAdapter.setNotify(null);
        IPCManagerAdapter.setIPcManageListener(null);
        VideoSquareManagerAdapter.setVideoSuqareListener(null);
        LocationNotifyAdapter.setLocationNotifyListener(null);
    }

    /**
     * ??????WIFI??????
     *
     * @author jyf
     * @date 2015???7???20???
     */
    private void closeWifiHot() {
        if (null == mWac) {
            mWac = new WifiConnectManager(mWifiManager, this);
        }
        mWac.closeAp();
    }

    /**
     * ????????????????????????
     *
     * @param isShow true:?????????false:??????
     * @author xuhw
     * @date 2015???6???2???
     */
    private void updateHotPointState(boolean isShow) {
        SettingUtils.getInstance().putBoolean("HotPointState", isShow);
    }

    /**
     * ???????????????????????????
     *
     * @author jyf
     */
    private void clearWifiConfig() {
        GolukDebugUtils.e("", "wifibind----WifiUnbindSelect----clearWifiConfig");
        if (null != mWac) {
            mWac.saveConfiguration(null);
        }
    }

    private void wifiCallBack_sameHot() {
        if (mApp.getIpcIsLogin()) {
            wifiConnectedSucess();
        } else {
            // ????????????????????????IPC??????
            if (null == GolukApplication.mIpcIp) {
                // ????????????
                // wifiConnectFailed();
            } else {
                mApp.mIPCControlManager.setVdcpDisconnect();
                mApp.mIPCControlManager.setIPCWifiState(true, GolukApplication.mIpcIp);
            }
        }
    }

    private void refreshIpcDataToFile() {
        if (null == mCurrentConnBean) {
            return;
        }
        mCurrentConnBean = mWac.readConfig();
        if (null == mCurrentConnBean) {
            return;
        }
        //??????????????????????????????????????????IPC??????
        WiFiInfo.IPC_MAC = mCurrentConnBean.getIpc_mac();
        WiFiInfo.IPC_SSID = mCurrentConnBean.getIpc_ssid();
        WiFiInfo.IPC_PWD = mCurrentConnBean.getIpc_pass();
        WiFiInfo.MOBILE_SSID = mCurrentConnBean.getPh_ssid();
        WiFiInfo.MOBILE_PWD = mCurrentConnBean.getPh_pass();
        WiFiInfo.IPC_MODEL = mCurrentConnBean.getIpc_model();
        GolukDebugUtils.e("",
                "select wifibind---MainActivity------refreshIpcDataToFile1: " + mCurrentConnBean.getIpc_ssid());
        // ?????????????????????????????????????????????????????????
        if (WifiBindDataCenter.getInstance().isHasIpc(mCurrentConnBean.getIpc_ssid())) {
            WifiBindDataCenter.getInstance().editBindStatus(mCurrentConnBean.getIpc_ssid(),
                    WifiBindHistoryBean.CONN_USE);
            mCurrentConnBean = null;
            return;
        }
        GolukDebugUtils.e("",
                "select wifibind---MainActivity------refreshIpcDataToFile: " + mCurrentConnBean.getIpc_ssid());
        // ???????????????
        addHistoryIpcToDb();
    }

    private void addHistoryIpcToDb() {
        if (null == mCurrentConnBean) {
            return;
        }
        WifiBindHistoryBean historyBean = new WifiBindHistoryBean();
        historyBean.ipc_ssid = mCurrentConnBean.getIpc_ssid();
        historyBean.ipc_mac = mCurrentConnBean.getIpc_bssid();
        historyBean.ipc_pwd = mCurrentConnBean.getIpc_pass();

        historyBean.mobile_ssid = mCurrentConnBean.getPh_ssid();
        historyBean.mobile_pwd = mCurrentConnBean.getPh_pass();

        WifiBindDataCenter.getInstance().saveBindData(historyBean);
        mCurrentConnBean = null;
    }

    private void wifiCallBack_ipcConnHotSucess(String message, Object arrays) {
        WifiRsBean[] bean = (WifiRsBean[]) arrays;
        if (null != bean && bean.length > 0) {
            mCurrentConnBean = mWac.readConfig();
            sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
        }
    }

    private void createHotSuccess() {
        // ????????????????????????????????????????????????
        WifiBindHistoryBean currentBean = WifiBindDataCenter.getInstance().getCurrentUseIpc();
        if (currentBean != null) {
            String type = GolukUtils.getIpcTypeFromName(currentBean.ipc_ssid);
            mApp.mIPCControlManager.setIpcMode(type);
            GolukDebugUtils.e("", "wifibind----MainActivity--------createHotSuccess:  type:" + type);
        }
    }

    private void wifiCallBack_5(int state, int process, String message, Object arrays) {
        if (state == 0) {
            switch (process) {
                case 0:
                    // ??????????????????
                    createHotSuccess();
                    break;
                case 1:
                    // ipc?????????????????????
                    wifiCallBack_ipcConnHotSucess(message, arrays);
                    break;
                case 2:
                    // ???????????????????????????????????????????????????
                    wifiCallBack_sameHot();
                    break;
                case 3:
                    // ???????????????????????????wifi????????????????????????
                    wifiConnectFailed();
                    break;
                default:
                    break;
            }
        } else {
            // ?????????
            wifiConnectFailed();
        }
    }

    private void wifiCallBack_3(int state, int process, String message, Object arrays) {
        EventWifiAuto autoBean = new EventWifiAuto();
        autoBean.eCode = EventConfig.CAR_RECORDER_RESULT;
        autoBean.state = state;
        autoBean.process = process;
        autoBean.message = message;
        autoBean.arrays = arrays;
        // ????????????????????????????????????
        EventBus.getDefault().post(autoBean);

        if (state == 0) {
            switch (process) {
                case 0:
                    // ??????????????????
                    createHotSuccess();
                    break;
                case 1:
                    // ipc?????????????????????
                    try {
                        WifiRsBean[] bean = (WifiRsBean[]) arrays;
                        if (null != bean && bean.length > 0) {
                            sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
                        }
                    } catch (Exception e) {
                    }
                    break;
                default:
                    break;
            }
        } else {
            // connFailed();
        }
    }

    @Override
    public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
        GolukDebugUtils.e("", "wifibind----MainActivity--------wifiConn----wifiCallBack:  type:" + type + "  state:"
                + state + "  process:" + process);
        if (!mApp.isBindSucess()) {
            return;
        }
        switch (type) {
            case 3:
                wifiCallBack_3(state, process, message, arrays);
                break;
            case 5:
                wifiCallBack_5(state, process, message, arrays);
                break;
            default:
                break;
        }
    }

    /**
     * ??????logic??????ipc
     */
    private void sendLogicLinkIpc(String ip, String ipcmac) {
        // ??????ipc??????wifi---??????ipc??????
        GolukApplication.mIpcIp = ip;
        mApp.mIPCControlManager.setIPCWifiState(true, ip);
    }

    private void setMessageTipCount(int total) {
        if (total > 0) {
            mUnreadTips.setVisibility(View.VISIBLE);
        } else {
            mUnreadTips.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
    }

    private final int REQUEST_CODE_OVERLAYS = 9;

    /**
     * ?????????????????????
     */
    private void requestOverlayAuthority() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    //??????Activity???????????????
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE_OVERLAYS);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestWriteStorage() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
