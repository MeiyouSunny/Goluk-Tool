package com.mobnote.golukmain;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
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
import android.widget.Toast;

import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
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
import com.mobnote.eventbus.EventUpdateAddr;
import com.mobnote.eventbus.EventUserLoginRet;
import com.mobnote.eventbus.EventUtil;
import com.mobnote.eventbus.EventWifiAuto;
import com.mobnote.eventbus.EventWifiConnect;
import com.mobnote.eventbus.EventWifiState;
import com.mobnote.golukmain.FollowCount.FollowCountRequest;
import com.mobnote.golukmain.FollowCount.bean.FollowCountRetBean;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.cluster.ClusterActivity;
import com.mobnote.golukmain.comment.CommentTimerManager;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.followed.FragmentFollowed;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.live.GetBaiduAddress;
import com.mobnote.golukmain.live.GetBaiduAddress.IBaiduGeoCoderFn;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.livevideo.IsLiveRequest;
import com.mobnote.golukmain.livevideo.IsLiveRetBean;
import com.mobnote.golukmain.msg.MessageBadger;
import com.mobnote.golukmain.msg.MsgCenterCounterRequest;
import com.mobnote.golukmain.msg.bean.MessageCounterBean;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.golukmain.special.SpecialListActivity;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.golukmain.wifibind.WiFiInfo;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.golukmain.xdpush.GolukNotification;
import com.mobnote.golukmain.xdpush.StartAppBean;
import com.mobnote.golukmain.xdpush.XingGeMsgBean;
import com.mobnote.golukmobile.GuideActivity;
import com.mobnote.manager.MessageManager;
import com.mobnote.receiver.NetworkStateReceiver;
import com.mobnote.util.CrashReportUtil;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.wifibind.WifiConnCallBack;
import com.mobnote.wifibind.WifiConnectManager;
import com.mobnote.wifibind.WifiRsBean;
import com.rd.car.CarRecorderManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

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
import cn.com.tiros.baidu.LocationAddressDetailBean;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

@SuppressLint({"HandlerLeak", "NewApi"})
public class MainActivity extends BaseActivity implements WifiConnCallBack, ILiveDialogManagerFn, IBaiduGeoCoderFn,
        IRequestResultListener {
    public static final String INTENT_ACTION_RETURN_MAIN_ALBUM = "returnToAlbum";

    /**
     * 程序启动需要20秒的时间用来等待IPC连接
     */
    private final int MSG_H_WIFICONN_TIME = 100;
    /**
     * application
     */
    public GolukApplication mApp = null;

    /**
     * wifi列表manage
     */
    private WifiConnectManager mWac = null;

    /**
     * 记录登录状态
     **/
    public SharedPreferences mPreferencesAuto;
    public boolean isFirstLogin;

    /**
     * 未连接
     */
    public static final int WIFI_STATE_FAILED = 0;
    /**
     * 连接中
     */
    public static final int WIFI_STATE_CONNING = 1;
    /**
     * 连接
     */
    public static final int WIFI_STATE_SUCCESS = 2;

    /**
     * 连接ipc时的动画
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
     * 把当前连接的设备保存起来，主要是为了兼容以前的连接状态
     */
    private WifiRsBean mCurrentConnBean = null;
    private FragmentTabHost mTabHost;

    private ImageView mCarrecorderIv;
    //private ViewStub mGuideMainViewStub;
    private SharePlatformUtil mSharePlatform = null;

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
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext
        // 注意该方法要再setContentView方法之前实现
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        // Register EventBus
        EventBus.getDefault().register(this);
        initThirdSDK();

        // 获得GolukApplication对象
        mApp = (GolukApplication) getApplication();
        mApp.setContext(this, "Main");
        mApp.getEnableSingleWifi();
        mApp.initLogic();
        // 页面初始化,获取页面控件
        mApp.startTime = System.currentTimeMillis();
        // 页面初始化,获取页面控件
        init();

        UserInfo userInfo = mApp.getMyInfo();
        if (null != userInfo) {
            mApp.mCurrentUId = userInfo.uid;
            mApp.mCurrentAid = userInfo.aid;
        }

//        // 读取SharedPreFerences中需要的数据,使用SharedPreFerences来记录程序启动的使用次数
//        SharedPreferences preferences = getSharedPreferences("golukmark", MODE_PRIVATE);
//        // 取得相应的值,如果没有该值,说明还未写入,用true作为默认值
//        boolean isFirstIndex = preferences.getBoolean("isFirstIndex", true);
//        if (isFirstIndex) { // 如果是第一次启动
//            mGuideMainViewStub.inflate();
//            Editor editor = preferences.edit();
//            editor.putBoolean("isFirstIndex", false);
//            // 提交修改
//            editor.commit();
//        }

        // 为了兼容以前的版本， 把旧的绑定信息读取出来
        mWac = new WifiConnectManager(mWifiManager, this);
        mCurrentConnBean = mWac.readConfig();
        refreshIpcDataToFile();

        // 初始化连接与綁定状态
//        if (mApp.isBindSucess()) {
//            if (mApp.getEnableSingleWifi()) {
//                mApp.mIpcIp = WiFiLinkListActivity.CONNECT_IPC_IP;
                //什么都不干
//            } else {
//                startWifi();
                // 启动创建热点
//                autoConnWifi();
                // 等待IPC连接时间

//                mBaseHandler.sendEmptyMessageDelayed(MSG_H_WIFICONN_TIME, 40 * 1000);
//            }
//        } else {
//            wifiConnectFailed();
//        }

        // 不是第一次登录，并且上次登录成功过，进行自动登录
        mPreferencesAuto = getSharedPreferences("firstLogin", MODE_PRIVATE);
        isFirstLogin = mPreferencesAuto.getBoolean("FirstLogin", true);
        if (!isFirstLogin && !mApp.isUserLoginSucess) {
            mApp.mUser.initAutoLogin();
        }

        GetBaiduAddress.getInstance().setCallBackListener(this);

        // 未登录跳转登录
        Intent itStart_have = getIntent();
        if (null != itStart_have.getStringExtra("userstart")) {
            String start_have = itStart_have.getStringExtra("userstart").toString();
            if ("start_have".equals(start_have)) {
                Intent intent = null;
                if (GolukApplication.getInstance().isMainland() == false) {
                    intent = new Intent(this, InternationUserLoginActivity.class);
                } else {
                    intent = new Intent(this, UserLoginActivity.class);
                }
                // 登录页回调判断
                intent.putExtra("isInfo", "main");
                mPreferences = getSharedPreferences("toRepwd", Context.MODE_PRIVATE);
                mEditor = mPreferences.edit();
                mEditor.putString("toRepwd", "start");
                mEditor.commit();
                // 在黑页面判断是注销进来的还是首次登录进来的
                if (!mApp.loginoutStatus) {// 注销
                    // 获取注销成功后传来的信息
                    mPreferences = getSharedPreferences("setup", MODE_PRIVATE);
                    String phone = mPreferences.getString("setupPhone", "");// 最后一个参数为默认值
                    intent.putExtra("startActivity", phone);
                    startActivity(intent);
                } else {
                    startActivity(intent);
                }
            }
        }

        dealPush(itStart_have);

        if (NetworkStateReceiver.isNetworkAvailable(this)) {
            notifyLogicNetWorkState(true);
        }
        GolukUtils.getMobileInfo(this);

        //msgRequest();
        mSharePlatform = new SharePlatformUtil(this);

        BaiduLocation.getInstance().startLocation();
    }

    public void onEventMainThread(Event event) {
        if (EventUtil.isNotInChinaEvent(event)  && GolukApplication.getInstance().isMainland()) {
            // 显示国内App无法在海外使用提示
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
                            // 结束应用
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
        b.putString("key", "Discover");
        LinearLayout discover = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_discover, null);
        mTabHost.addTab(mTabHost.newTabSpec("Discover").setIndicator(discover), FragmentDiscover.class, b);

        b = new Bundle();
        b.putString("key", "Follow");
        RelativeLayout follow = (RelativeLayout) inflater.inflate(R.layout.main_tab_indicator_follow, null);
        mTabHost.addTab(mTabHost.newTabSpec("Follow").setIndicator(follow), FragmentFollowed.class, b);
        mFollowedVideoTipIV = (ImageView) follow.findViewById(R.id.iv_new_followed_video_tips);

        b = new Bundle();
        b.putString("key", "CarRecorder");
        LinearLayout carRecorder = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_carrecorder, null);
        mCarrecorderIv = (ImageView) carRecorder.findViewById(R.id.tab_host_carrecorder_iv);
        mTabHost.addTab(mTabHost.newTabSpec("CarRecorder").setIndicator(carRecorder), null, b);

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

        mTabHost.getTabWidget().getChildTabViewAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZhugeUtils.eventIpcCarrecorder(MainActivity.this);

                connectGoluk(false);
            }
        });

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if ("Follow".equals(tabId)) {
                    mFollowedVideoTipIV.setVisibility(View.GONE);
                } else if ("Album".equals(tabId)) {
                    //tab访问相册统计
                    ZhugeUtils.eventCallAlbum(MainActivity.this, getString(R.string.str_zhuge_call_album_source_tabbar));
                }
            }
        });
    }

    public void connectGoluk(boolean returnToMainActivityWhenSuccess) {
        if (mApp.isIpcLoginSuccess) {
            if (mApp.canNotUse()) {
                GolukUtils.showToast(this, getResources().getString(R.string.interantion_ban_mainland_goluk));
            } else {
                Intent intent = new Intent(MainActivity.this, CarRecorderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
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
            MsgCenterCounterRequest msgCounterReq = new MsgCenterCounterRequest(IPageNotifyFn.PageType_MsgCounter, this);
            msgCounterReq.get("100", GolukApplication.getInstance().mCurrentUId, "", "", "");
        }
    }

    private void followCountRequest() {
        if (GolukApplication.getInstance().isUserLoginSucess) {
            FollowCountRequest followCountRequest = new FollowCountRequest(IPageNotifyFn.PageType_FollowCount, this);
            followCountRequest.get(GolukApplication.getInstance().mCurrentUId);
        }
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

        dealPush(intent);
    }

    /**
     * 处理推送消息
     *
     * @param intent
     * @author jyf
     */
    private void dealPush(Intent intent) {
        if (null == intent) {
            return;
        }
        final String from = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_FROM);
        GolukDebugUtils.e("", "jyf----MainActivity-----from: " + from);
        if (null != from && !"".equals(from) && from.equals("notication")) {
            String pushJson = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_JSON);

            GolukDebugUtils.e("", "jyf----MainActivity-----pushJson: " + pushJson);
            XingGeMsgBean bean = JsonUtil.parseXingGePushMsg(pushJson);
            if (null != bean) {
                GolukNotification.getInstance().dealAppinnerClick(this, bean);
            }
            // GolukUtils.showToast(this, "处理推送数据 :" + pushJson);
        }
        // 处理网页启动App
        mStartAppBean = (StartAppBean) intent.getSerializableExtra(GuideActivity.KEY_WEB_START);
        dealWebStart();
    }

    private void dealWebStart() {
        GolukDebugUtils.e("", "start App: MainActivity:------------: 11111");
        if (null == mStartAppBean) {
            return;
        }
        String type = mStartAppBean.type;
        String title = mStartAppBean.title;
        String id = mStartAppBean.id;
        String voteUrl = mStartAppBean.voteUrl;

        if ("1".equals(type)) {// 单视频
            //视频详情页访问
            ZhugeUtils.eventVideoDetail(this, this.getString(R.string.str_zhuge_share_video_network_other));
            Intent intent = new Intent(this, VideoDetailActivity.class);
            intent.putExtra(VideoDetailActivity.VIDEO_ID, id);
            intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
            startActivity(intent);
        } else if ("2".equals(type)) {// 专题
            Intent intent = new Intent(this, SpecialListActivity.class);
            intent.putExtra("ztid", id);
            intent.putExtra("title", title);
            startActivity(intent);
        } else if ("3".equals(type)) {//活动聚合页面
            Intent intent = new Intent(this, ClusterActivity.class);
            intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, id);
            intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, title);
            startActivity(intent);
        } else if ("4".equals(type)) {//个人主页
            GolukUtils.startUserCenterActivity(this, id);
        } else if ("5".equals(type)) {//投票网页
            Intent intent = new Intent(this, UserOpenUrlActivity.class);
            intent.putExtra(GolukConfig.WEB_TYPE, GolukConfig.NEED_SHARE);
            intent.putExtra(GolukConfig.H5_URL, voteUrl);
            intent.putExtra(GolukConfig.URL_OPEN_PATH, "text_banner");
            if (!TextUtils.isEmpty(title)) {
                intent.putExtra(GolukConfig.NEED_H5_TITLE, title);
            }
            startActivity(intent);
        }

        mStartAppBean = null;
    }

    /**
     * 初始化第三方SDK
     *
     * @author jyf
     * @date 2015年6月17日
     */
    private void initThirdSDK() {
        // 关闭umeng错误统计(只使用友盟的行为分析，不使用错误统计)
        MobclickAgent.setDebugMode(false);
        MobclickAgent.setCatchUncaughtExceptions(false);
        // 添加腾讯崩溃统计 初始化SDK
        String appId = null;
        if(GolukApplication.getInstance().isMainland()) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Sina or facebook sso callback
        if (null != mSharePlatform) {
            mSharePlatform.onActivityResult(requestCode, resultCode, data);
        }
    }

    public SharePlatformUtil getSharePlatform() {
        return mSharePlatform;
    }

    /**
     * 页面初始化,获取页面元素,注册事件
     */
    private void init() {
        boolean hotPointState = SettingUtils.getInstance().getBoolean("HotPointState", false);
        updateHotPointState(hotPointState);
    }

    @Override
    protected void hMessage(Message msg) {
        switch (msg.what) {
            case MSG_H_WIFICONN_TIME:
                // 设置未连接状态
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
     * 视频同步完成
     */
    public void videoAnalyzeComplete(String str) {
        try {
            JSONObject json = new JSONObject(str);
            String tag = json.getString("tag");
            String filename = json.optString("filename");
            long time = json.optLong("filetime");
            if (tag.equals("videodownload")) {
                // 只有视频下载才提示音频
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

                // 更新最新下载文件的时间
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
     * 链接中断更新页面
     */
    public void wiFiLinkStatus(int status) {
        GolukDebugUtils
                .e("", "jyf-----MainActivity----wifiConn----wiFiLinkStatus-------------wiFiLinkStatus:" + status);
        mApp.mWiFiStatus = 0;
        switch (status) {
            case 1:
                // 连接中
                this.updateRecoderBtn(1);
                mApp.mWiFiStatus = WIFI_STATE_CONNING;
                EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_CONNING));
                break;
            case 2:
                // 已连接
                this.updateRecoderBtn(2);
                mApp.mWiFiStatus = WIFI_STATE_SUCCESS;
                wifiConnectedSucess();
                break;
            case 3:
                // 未连接
                this.updateRecoderBtn(0);
                mApp.mWiFiStatus = WIFI_STATE_FAILED;
                wifiConnectFailed();
                break;
        }
    }

    /**
     * 更新行车记录仪按钮 1:连接中 2：已连接 0：未连接
     */
    public void updateRecoderBtn(int state) {

        if (this.isFinishing() == false) {
            AnimationDrawable ad = null;
            if (state == WIFI_STATE_CONNING && mApp.isBindSucess()) {
                mCarrecorderIv.setImageResource(R.drawable.carrecoder_btn);
                ad = (AnimationDrawable) mCarrecorderIv.getDrawable();
                if (ad.isRunning() == false) {
                    ad.setOneShot(false);
                    ad.start();
                }
            } else if (state == WIFI_STATE_SUCCESS) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.wifi_link_success_conn),
                        Toast.LENGTH_LONG).show();
                mCarrecorderIv.setImageResource(R.drawable.tb_car_recorder_connected);
            } else if (state == WIFI_STATE_FAILED) {
                mCarrecorderIv.setImageResource(R.drawable.tb_car_recorder_not_connected);
            } else {
                mCarrecorderIv.setImageResource(R.drawable.tb_car_recorder_not_connected);
            }
        }
    }

    private void startWifi() {
        GolukDebugUtils.e("", "wifiCallBack-------------startWifi:");
        if (WIFI_STATE_CONNING == mApp.mWiFiStatus) {
            return;
        }
        mApp.mWiFiStatus = WIFI_STATE_CONNING;
    }

    // 连接成功
    private void wifiConnectedSucess() {
        GolukDebugUtils.e("", "wifiCallBack-------------wifiConnectedSucess:");
        mBaseHandler.removeMessages(MSG_H_WIFICONN_TIME);
        mApp.mWiFiStatus = WIFI_STATE_SUCCESS;
        refreshIpcDataToFile();
        EventBus.getDefault().post(new EventWifiConnect(EventConfig.WIFI_STATE_SUCCESS));
    }

    // 连接失败
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
                MessageBadger.sendBadgeNumber(msgCount, this);
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
     * 启动软件创建wifi热点
     */
    private void autoConnWifi() {
        GolukDebugUtils.e("", "自动连接小车本wifi---linkMobnoteWiFi---1");
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
        // 创建热点之前先断开ipc连接
        mApp.setIpcDisconnect();
        final String wifiName = bean.mobile_ssid;
        final String pwd = bean.mobile_pwd;
        String ipcssid = bean.ipc_ssid;
        String ipcmac = bean.ipc_mac;
        // 调用韩峥接口创建手机热点
        startWifi();
        // 等待IPC连接时间
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
                // 请求在线视频轮播数据
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
                // 检测是否已连接小车本热点
                // 网络状态改变
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
            // 应用退出时调用
            CarRecorderManager.onExit(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Unregister EventBus
        EventBus.getDefault().unregister(this);
        // mBannerLoaded = false;
    }

    @Override
    protected void onResume() {
        // GolukApplication.getInstance().queryNewFileList();
        GolukDebugUtils.e("", "crash zh start App ------ MainActivity-----onResume------------:");
        mApp.setContext(this, "Main");
        LiveDialogManager.getManagerInstance().setDialogManageFn(this);

        mApp.setBinding(false);

        GetBaiduAddress.getInstance().setCallBackListener(this);

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

    public void requestIsAlive(){
        SharedPrefUtil.setIsLiveNormalExit(true);
        mApp.isNeedCheckLive = false;
        mApp.isCheckContinueLiveFinish = true;
        IsLiveRequest isLiveRequest = new IsLiveRequest(IPageNotifyFn.PAGE_TYPE_IS_ALIVE,this);
        isLiveRequest.request();
    }
    public void showContinueLive() {
        if (mApp.getIpcIsLogin()) {
            LiveDialogManager.getManagerInstance().showTwoBtnDialog(this, LiveDialogManager.DIALOG_TYPE_LIVE_CONTINUE,
                    getString(R.string.user_dialog_hint_title), getString(R.string.str_live_continue));
        }
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
            mApp.setExit(true);
            mApp.mHandler.removeMessages(1001);
            mApp.mHandler.removeMessages(1002);
            mApp.mHandler.removeMessages(1003);
            GetBaiduAddress.getInstance().exit();
            GolukVideoInfoDbManager.getInstance().destroy();
            unregisterListener();
            mApp.mIPCControlManager.setVdcpDisconnect();
            mApp.setIpcLoginOut();
            mApp.mUser.exitApp();
            mApp.mTimerManage.timerCancel();
            closeWifiHot();
            GlobalWindow.getInstance().dimissGlobalWindow();
            mApp.destroyLogic();
            MobclickAgent.onKillProcess(this);
            mApp.appFree();
            if (!isDestroyed())
                finish();
            GolukNotification.getInstance().destroy();
            CommentTimerManager.getInstance().cancelTimer();
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
     * 关闭WIFI热点
     *
     * @author jyf
     * @date 2015年7月20日
     */
    private void closeWifiHot() {
        if (null == mWac) {
            mWac = new WifiConnectManager(mWifiManager, this);
        }
        mWac.closeAp();
    }

    /**
     * 重置红点显示状态
     *
     * @param isShow true:显示　false:隐藏
     * @author xuhw
     * @date 2015年6月2日
     */
    private void updateHotPointState(boolean isShow) {
        SettingUtils.getInstance().putBoolean("HotPointState", isShow);
    }

    @Override
    public void dialogManagerCallBack(int dialogType, int function, String data) {
        if (dialogType == LiveDialogManager.DIALOG_TYPE_LOGIN) {
            if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
                Intent intent = null;
                if (GolukApplication.getInstance().isMainland() == false) {
                    intent = new Intent(this, InternationUserLoginActivity.class);
                } else {
                    intent = new Intent(this, UserLoginActivity.class);
                }
                intent.putExtra("isInfo", "back");
                startActivity(intent);
            }
        } else if (LiveDialogManager.DIALOG_TYPE_LIVE_CONTINUE == dialogType) {
            if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
                //直播页面
                ZhugeUtils.eventLive(this, this.getString(R.string.str_zhuge_share_video_network_other));

                GolukUtils.startPublishOrWatchLiveActivity(this, true, true,null, null, null);
            } else if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {
                if (mApp.mIPCControlManager.isT1Relative()) {
                    mApp.mIPCControlManager.stopLive();
                }
            }
        } else if (LiveDialogManager.DIALOG_TYPE_APP_EXIT == dialogType) {
            if (function == LiveDialogManager.FUNCTION_DIALOG_OK) {
                exit();
            }
        }

    }

    @Override
    public void CallBack_BaiduGeoCoder(int function, Object obj) {
        if (null == obj) {
            GolukDebugUtils.e("", "jyf----20150406----LiveActivity----CallBack_BaiduGeoCoder----获取反地理编码  : "
                    + (String) obj);
            return;
        }

        String address = "";
        if (GolukApplication.getInstance().isMainland()) {
            address = ((ReverseGeoCodeResult) obj).getAddress();// 国内
        } else {
            address = ((LocationAddressDetailBean) obj).detail;// 国际
        }
        GolukDebugUtils.e("", "-----------CallBack_BaiduGeoCoder----MainActivity------address: " + address);
        GolukApplication.getInstance().mCurAddr = address;
        // 更新行车记录仪地址
        EventBus.getDefault().post(new EventUpdateAddr(EventConfig.CAR_RECORDER_UPDATE_ADDR, address));
    }

    /**
     * 清空本地的配置文件
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
            // 判断，是否设置过IPC地址
            if (null == GolukApplication.mIpcIp) {
                // 连接失败
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
        //时刻保存全局变量为上次连接的IPC设备
        WiFiInfo.IPC_MAC = mCurrentConnBean.getIpc_mac();
        WiFiInfo.IPC_SSID = mCurrentConnBean.getIpc_ssid();
        WiFiInfo.IPC_PWD = mCurrentConnBean.getIpc_pass();
        WiFiInfo.MOBILE_SSID = mCurrentConnBean.getPh_ssid();
        WiFiInfo.MOBILE_PWD = mCurrentConnBean.getPh_pass();
        WiFiInfo.IPC_MODEL = mCurrentConnBean.getIpc_model();
        GolukDebugUtils.e("",
                "select wifibind---MainActivity------refreshIpcDataToFile1: " + mCurrentConnBean.getIpc_ssid());
        // 如果本地文件中已经有记录了，则不再保存
        if (WifiBindDataCenter.getInstance().isHasIpc(mCurrentConnBean.getIpc_ssid())) {
            WifiBindDataCenter.getInstance().editBindStatus(mCurrentConnBean.getIpc_ssid(),
                    WifiBindHistoryBean.CONN_USE);
            mCurrentConnBean = null;
            return;
        }
        GolukDebugUtils.e("",
                "select wifibind---MainActivity------refreshIpcDataToFile: " + mCurrentConnBean.getIpc_ssid());
        // 添加新记录
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
        // 创建热点成功后，需要设置连接方式
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
                    // 创建热点成功
                    createHotSuccess();
                    break;
                case 1:
                    // ipc成功连接上热点
                    wifiCallBack_ipcConnHotSucess(message, arrays);
                    break;
                case 2:
                    // 用户已经创建与配置文件相同的热点，
                    wifiCallBack_sameHot();
                    break;
                case 3:
                    // 用户已经连接到其它wifi，按连接失败处理
                    wifiConnectFailed();
                    break;
                default:
                    break;
            }
        } else {
            // 未连接
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
        // 通知选择设备界面，做更新
        EventBus.getDefault().post(autoBean);

        if (state == 0) {
            switch (process) {
                case 0:
                    // 创建热点成功
                    createHotSuccess();
                    break;
                case 1:
                    // ipc成功连接上热点
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
     * 通知logic连接ipc
     */
    private void sendLogicLinkIpc(String ip, String ipcmac) {
        // 连接ipc热点wifi---调用ipc接口
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
        // TODO Auto-generated method stub
        if (null == result) {
            return;
        }

        if (requestType == IPageNotifyFn.PageType_MsgCounter) {
            MessageCounterBean bean = (MessageCounterBean) result;
            if (null == bean.data) {
                return;
            }

            if (null != bean.data.messagecount) {
                int praiseCount = 0;
                int commentCount = 0;
                int systemCount = 0;
                int followCount = 0;
                if (null != bean.data.messagecount.user) {
                    praiseCount = bean.data.messagecount.user.like;
                    commentCount = bean.data.messagecount.user.comment;
                }
                if (null != bean.data.messagecount.system) {
                    systemCount = bean.data.messagecount.system.total;
                }

                MessageManager.getMessageManager().setMessageEveryCount(praiseCount, commentCount, followCount,
                        systemCount);
            }
        } else if (requestType == IPageNotifyFn.PageType_FollowCount) {
            FollowCountRetBean bean = (FollowCountRetBean) result;
            if (null == bean || null == bean.data) {
                return;
            }
            if (bean.data.newvideo > 0) {
                mFollowedVideoTipIV.setVisibility(View.VISIBLE);
            }
        } else if (requestType == IPageNotifyFn.PAGE_TYPE_IS_ALIVE) {
            IsLiveRetBean isLiveRetBean = (IsLiveRetBean) result;
            if(isLiveRetBean == null){
                mApp.mIPCControlManager.stopLive();
                return;
            }
            if(TextUtils.isEmpty(isLiveRetBean.code)) {
                mApp.mIPCControlManager.stopLive();
                return;
            }
            if("200".equals(isLiveRetBean.code)){
                showContinueLive();
            }else {
                mApp.mIPCControlManager.stopLive();
            }
        }
    }
}
