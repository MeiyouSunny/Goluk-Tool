package com.mobnote.golukmain.wifibind;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBinding;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventFinishWifiActivity;
import com.mobnote.eventbus.EventSingleConnSuccess;
import com.mobnote.eventbus.EventWifiState;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UpdateActivity;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.golukmain.multicast.NetUtil;
import com.mobnote.golukmain.photoalbum.PhotoAlbumActivity;
import com.mobnote.golukmain.reportlog.ReportLogManager;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.log.app.LogConst;
import com.mobnote.t1sp.connect.T1SPConnecter;
import com.mobnote.t1sp.ui.album.PhotoAlbumT1SPActivity;
import com.mobnote.t1sp.ui.preview.CarRecorderT1SPActivity;
import com.mobnote.t1sp.util.Const;
import com.mobnote.t1sp.util.TimeSync;
import com.mobnote.t1sp.util.ViewUtil;
import com.mobnote.user.IPCInfo;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.wifibind.WifiConnCallBack;
import com.mobnote.wifibind.WifiConnectManager;
import com.mobnote.wifibind.WifiRsBean;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackCmd;

/**
 * Wifi扫描列表
 */
public class WiFiLinkListActivity extends BaseActivity implements OnClickListener, WifiConnCallBack, ForbidBack, IPCManagerFn {

    private static final String TAG = "WiFiLinkList";
    public static final String CONNECT_IPC_IP = "192.168.62.1";
    private static final int SHOW_TOAST = 10;
    private final String GOLUK_COMMON_SHOW_NAME = " Goluk_xx_xxxxxx ";
    /**
     * IPC默认要修改的密码
     */
    public static final String IPC_PWD_DEFAULT = "123456789";
    /**
     * 手机创建热点默认密码
     */
    public static final String MOBILE_HOT_PWD_DEFAULT = "123456789";

    /**
     * 未连接或连接失败
     */
    private static final int STATE_FAILED = 0;
    /**
     * 连接中
     */
    private static final int STATE_CONNING = 1;
    /**
     * 连接成功
     */
    private static final int STATE_SUCCESS = 2;

    private String mWillConnName = null;
    private String mWillConnMac = null;
    /**
     * application
     */
    protected GolukApplication mApp = null;
    /**
     * 返回按钮
     */
    private ImageButton mBackBtn = null;
    /**
     * 说明文字
     */
    private TextView mDescTitleText = null;
    private TextView mDescTitleText2 = null;
    private TextView mDescTitleText4 = null;
    /**
     * IPC信号动画
     */
    private ImageView mIpcSignalImage = null;
    private AnimationDrawable mIpcSignalAnim = null;
    /**
     * 下一步按钮
     */
    private Button mNextBtn = null;
    private TextView mHelpTv = null;

    private WifiManager mWifiManager = null;
    private WifiConnectManager mWac = null;
    /**
     * 连接wifi名称
     */
    public String mLinkWiFiName = null;

    private boolean mIsCanAcceptIPC = false;
    private boolean mIsCanAcceptNetState = false;
    private boolean mStartSystemWifi = false;
    protected boolean mReturnToMainAlbum;
    protected boolean mGotoAlbum = false;

    /**
     * 用于表示当前的状态 0/1/2 未连接/连接中/已连接
     */
    private int mCurrentState = STATE_FAILED;
    /**
     * 连接中对话框
     */
    private CustomLoadingDialog mConnectingDialog = null;
    /**
     * 用户要绑定的设备类型
     */
    private String mIPcType = "";
    private boolean mNeverReceiveMessage;
//    private String mIpcRealtype = null;

    public static final String ACTION_FROM_CAM_SETTING = "action_from_cam_setting";
    public static final String ACTION_FROM_MANAGER = "action_from_cam_manager";
    public static final String ACTION_GO_To_ALBUM = "action_go_to_album";
    public static final String ACTION_FROM_REMOTE_ALBUM = "action_from_remote_album";
    public static final String ACTION_FROM_CAM = "action_from_cam";
    private boolean mIsFromUpgrade;
    private boolean mIsFromRemoteAlbum;
    private boolean mAutoConn;
    private boolean mIsFromManagerToUpgrade;
    private IPCInfo mIpcInfo;

    private boolean isBackFromWifiListPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResourceId());
        // 获得GolukApplication对象
        mApp = (GolukApplication) getApplication();
        mApp.setContext(this, TAG);
        if (!mApp.isMainland()) {
            if (null != mApp.getIPCControlManager()) {
                mApp.getIPCControlManager().addIPCManagerListener("carversion", this);
            }
        }
        // 清除数据
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).clear();
        // 写日志，表示绑定失败
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setType("2");
        collectLog("onCreate", "---1");
        XLog.tag(LogConst.TAG_CONNECTION).i("Enter device connect page");

        getIntentData();
        //IPC-待连接页面
        ZhugeUtils.eventWaitConnect(this, mReturnToMainAlbum);

//        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setHdType(mIpcRealtype);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWac = new WifiConnectManager(mWifiManager, this);
        // 页面初始化
        initView();
        EventBus.getDefault().register(this);

        // 发送绑定中的消息
        EventBus.getDefault().post(new EventBinding(EventConfig.BINDING, false));
        // 设置当前正在绑定中
        mApp.setBinding(true);
        // 断开前面的所有连接
        mApp.setIpcDisconnect();
    }

    private void getIntentData() {
        Intent intent = this.getIntent();
        if (null != intent) {
            mIPcType = intent.getStringExtra(WifiUnbindSelectTypeActivity.KEY_IPC_TYPE);
            mIsFromUpgrade = intent.getBooleanExtra(ACTION_FROM_CAM_SETTING, false);
            mIsFromRemoteAlbum = intent.getBooleanExtra(ACTION_FROM_REMOTE_ALBUM, false);
//            mIpcRealtype = intent.getStringExtra(WifiUnbindSelectTypeActivity.KEY_IPC_REAL_TYPE);
            mReturnToMainAlbum = intent.getBooleanExtra(MainActivity.INTENT_ACTION_RETURN_MAIN_ALBUM, false);
            mGotoAlbum = intent.getBooleanExtra(ACTION_GO_To_ALBUM, false);
            mAutoConn = intent.getBooleanExtra(ACTION_FROM_CAM, true);
            mIsFromManagerToUpgrade = intent.getBooleanExtra(ACTION_FROM_MANAGER, false);
            mIpcInfo = (IPCInfo) intent.getSerializableExtra(UpdateActivity.UPDATE_DATA);
        }
    }

    protected int getContentViewResourceId() {
        return R.layout.wifi_link_list;
    }

    private String getWifiShowName() {
        return GOLUK_COMMON_SHOW_NAME;
    }

    /**
     * 页面初始化
     */
    protected void initView() {
        // 获取页面元素
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        mIpcSignalImage = (ImageView) findViewById(R.id.imageView1);
        mDescTitleText = (TextView) findViewById(R.id.wifilist_textView1);
        mDescTitleText2 = (TextView) findViewById(R.id.wifilist_textView3);
        mDescTitleText4 = (TextView) findViewById(R.id.wifilist_textView4);
        mNextBtn = (Button) findViewById(R.id.next_btn);
        mHelpTv = (TextView) findViewById(R.id.wifi_link_list_help);
        mHelpTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线

        switchView();

        // 注册事件
        mBackBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mHelpTv.setOnClickListener(this);

        // 修改title说明文字颜色
        final String connStr1 = getResources().getString(R.string.wifi_link_ok) + "<font color=\"#0587ff\"> "
                + getResources().getString(R.string.wifi_link_wifi_light) + " </font>"
                + getResources().getString(R.string.wifi_link_twinkle);
        mDescTitleText.setText(Html.fromHtml(connStr1));
        final String connStr2 = getResources().getString(R.string.wifi_link_wifi_name) + "<font color=\"#0587ff\">"
                + getWifiShowName() + "</font>" + getResources().getString(R.string.wifi_link_wifi_name2);
        mDescTitleText2.setText(Html.fromHtml(connStr2));

        this.nextCan();
        setStateSwitch();
    }

    /**
     * 根据不同的硬件系列，切换不同的背景图片
     *
     * @author jyf
     */
    private void switchView() {
        mIpcSignalImage.setBackgroundResource(getAnimal());
        mIpcSignalAnim = (AnimationDrawable) mIpcSignalImage.getBackground();
        // 启动动画
        mIpcSignalAnim.start();
    }

    protected void setStateSwitch() {
        switch (mCurrentState) {
            case STATE_FAILED:
                mNextBtn.setText(getResources().getString(R.string.wifi_link_go_system));
                mDescTitleText4.setVisibility(View.INVISIBLE);
                break;
            case STATE_CONNING:
                break;
            case STATE_SUCCESS:
                mNextBtn.setText(getResources().getString(R.string.wifi_link_next));
                setSucessWifiName(mWillConnName);
                break;
            default:
                break;
        }
    }

    private int getAnimal() {
        if (IPCControlManager.MODEL_T.equals(mIPcType)) {
            return R.anim.anim_ipcbind_t_direct_connect;
        } else {
            return R.anim.anim_ipcbind_g_direct_connect;
        }
    }

    /**
     * 连接成功后，设置成功的wifi名称
     *
     * @param wifiname
     * @author jyf
     */
    private void setSucessWifiName(String wifiname) {
        mDescTitleText4.setVisibility(View.VISIBLE);
        final String connStr2 = getResources().getString(R.string.wifi_link_success_conn) + " <font color=\"#0587ff\">"
                + wifiname + "</font>";
        mDescTitleText4.setText(Html.fromHtml(connStr2));
    }

    private boolean isGetWifiBean() {
        if (null == mWac) {
            connFailed();
            return false;
        }
        WifiRsBean bean = mWac.getConnResult();
        collectLog("isGetWifiBean", "-----1");
        XLog.tag(LogConst.TAG_CONNECTION).i("Get current WIFI info");
        if (null == bean) {
            showToast(getString(R.string.no_wifi_selected));
            GolukDebugUtils.e("", "bindbind-------------isGetWifiBean---failed2  :");
            collectLog("isGetWifiBean", "-----2");
            XLog.tag(LogConst.TAG_CONNECTION).i("Current WIFI info is null");
            connFailed();
            return false;
        }
        collectLog("dealAutoConn", "-----5 NOT  NULL");
        XLog.tag(LogConst.TAG_CONNECTION).i("Current WIFI info not null");
        mWillConnName = bean.getIpc_ssid();
        mWillConnMac = bean.getIpc_bssid();
        if (mWillConnName == null || mWillConnName.length() <= 0) {
            showToast(getString(R.string.no_wifi_selected));
            GolukDebugUtils.e("", "bindbind-------------isGetWifiBean---failed3  :");
            collectLog("isGetWifiBean", "-----3");
            XLog.tag(LogConst.TAG_CONNECTION).i("Current WIFI name is null");
            // 连接失败
            connFailed();
            return false;
        }
        if (!mWillConnName.startsWith("Goluk")) {
            showToast(getString(R.string.not_goluk_device), Toast.LENGTH_LONG);
            collectLog("isGetWifiBean", "-----4 wifiname" + mWillConnName);
            XLog.tag(LogConst.TAG_CONNECTION).i("Current WIFI is not Goluk WIFI: %s", mWillConnName);
            // 连接失败
            connFailed();
            return false;
        }

        GolukDebugUtils.e("", "WifiBindList----sWillConnName2: " + mWillConnName);

        collectLog("isGetWifiBean", "willConnName2:" + mWillConnName + "  willConnMac2:" + mWillConnMac);
        XLog.tag(LogConst.TAG_CONNECTION).i("Current WIFI info: WIFI Name:%s, macAddress:%s", mWillConnName, mWillConnMac);
        saveConnectWifiMsg(mWillConnName, IPC_PWD_DEFAULT, mWillConnMac);
        setIpcMode(mWillConnName);
        collectLog(GolukDebugUtils.CHOOSE_WIFI_LOG_TAG, "1.2 selected wifi :" + mWillConnName);
        return true;
    }

    private boolean isGolukDeviceWifi() {
        if (null == mWac) {
            return false;
        }
        WifiRsBean bean = mWac.getConnResult();
        if (null == bean) {
            return false;
        }
        String currentWifiName = bean.getIpc_ssid();
        if (TextUtils.isEmpty(currentWifiName)) {
            return false;
        }
        if (!currentWifiName.startsWith("Goluk")) {
            return false;
        }
        return true;
    }

    private AlertDialog connectFailedDialog;

    private void showConnectFailedDialog() {
        if (isDestroyed())
            return;

        if (connectFailedDialog == null) {
            connectFailedDialog = new AlertDialog
                    .Builder(this)
                    .setTitle(R.string.wifi_link_conn_failed)
                    .setMessage(R.string.connect_failed_upload_log)
                    .setNegativeButton(R.string.wifi_link_ok, null)
                    .setCancelable(false)
                    .create();
        }
        connectFailedDialog.show();

        GolukUtils.showToast(WiFiLinkListActivity.this, getResources().getString(R.string.connect_fail_hint_msg));
    }

    private void hideConnectFailedDialog() {
        if (connectFailedDialog != null)
            connectFailedDialog.dismiss();
    }

    private void setIpcMode(String wifiSsid) {
        if (null == wifiSsid) {
            return;
        }
        String type = GolukUtils.getIpcTypeFromName(wifiSsid);
        mApp.mIPCControlManager.setIpcMode(type);
    }

    private void dealAutoConn() {
        collectLog("dealAutoConn", "-----1");
        collectLog(GolukDebugUtils.CHOOSE_WIFI_LOG_TAG, "1.1 get selected wifi");
        // 获取当前连接的wifi
        if (!isGetWifiBean()) {
            collectLog(GolukDebugUtils.CHOOSE_WIFI_LOG_TAG, "1.2 No selected wifi :");
            return;
        }
        collectLog("dealAutoConn", "-----2");
        // 去连接IPC
        // isAutoConn = true;

        // T1SP连接
        connectT2S();
        // Other
        sendLogicLinkIpc();
    }

    private void collectLog(String method, String msg) {
        JSONObject logJson = JsonUtil.getReportData(TAG, method, msg);
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
                .addLogData(logJson);
    }

    private void reportLog() {
        final String jsonData = ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
                .getReportData();
        mApp.uploadMsg(jsonData, false);
        ReportLogManager.getInstance().removeKey(IMessageReportFn.KEY_WIFI_BIND);
    }

    private void saveConnectWifiMsg(String wifiName, String pwd, String mac) {
        mLinkWiFiName = wifiName;
        WiFiInfo.IPC_SSID = mLinkWiFiName;
        WiFiInfo.IPC_PWD = pwd;
        WiFiInfo.IPC_MAC = mac;
    }

    /**
     * 通知logic连接ipc
     */
    private void sendLogicLinkIpc() {
        collectLog("sendLogicLinkIpc", "--------1");
        // 连接ipc热点wifi---调用ipc接口
        mIsCanAcceptIPC = true;
        if (mApp.getEnableSingleWifi()) {
            mApp.mIpcIp = CONNECT_IPC_IP;
        } else {
            mApp.mIpcIp = "";
        }
        collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.1 setIpcWifiState");
        boolean b = mApp.mIPCControlManager.setIPCWifiState(true, CONNECT_IPC_IP);
        GolukDebugUtils.e("", "bindbind-------------sendLogicLinkIpc  :" + b);
        collectLog("sendLogicLinkIpc", "--------3------: " + b);
        XLog.tag(LogConst.TAG_CONNECTION).i("Send logic to connect IPS, setIPCWifiState, result: " + b);
        if (b) {
            collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.2 setIpcWifiState return true");
            this.showLoadingDialog();
            mCurrentState = STATE_CONNING;
            this.setStateSwitch();
        } else {
            collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.2 setIpcWifiState return false");
            connFailed();
        }
        GolukDebugUtils.e("", "WiFiLinkListActivity 通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
    }

    private void connFailed() {
        collectLog("connFailed", "--------1------: ");
        XLog.tag(LogConst.TAG_CONNECTION).i("Connect device failed");
        this.dimissLoadingDialog();
        mApp.mIPCControlManager.setVdcpDisconnect();
        mApp.isIpcConnSuccess = false;
        mCurrentState = STATE_FAILED;
        setStateSwitch();
    }

    private void showLoadingDialog() {
        if (null != mConnectingDialog) {
            return;
        }
        hideConnectFailedDialog();
        mConnectingDialog = new CustomLoadingDialog(this, getResources().getString(R.string.wifi_link_38_text));
        mConnectingDialog.setListener(this);
        mConnectingDialog.show();
    }

    private void dimissLoadingDialog() {
        if (null != mConnectingDialog) {
            mConnectingDialog.close();
            mConnectingDialog = null;
        }
    }

    int POST_FAILED_DELAY = 1 * 1000;

    public void ipcFailedCallBack() {
        collectLog("ipcLinkFailedCallBack", "--------1");
        XLog.tag(LogConst.TAG_CONNECTION).i("Connect device failed");
        GolukDebugUtils.e("", "WiFiLinkListActivity  通知logic连接ipc---dealAutoConn--------ipcLinkFailedCallBack：");
        mApp.mIPCControlManager.setVdcpDisconnect();
        // if (!mIsCanAcceptIPC) {
        // return;
        // }

        GolukDebugUtils.e("", "bindbind-------------ipcFailedCallBack  :");
        collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3 Ipc Wifi Failed");
        collectLog("ipcLinkFailedCallBack", "--------2");
        mIsCanAcceptIPC = false;
        this.dimissLoadingDialog();
        if (mAutoConn) {
            mBaseHandler.sendEmptyMessageDelayed(SHOW_TOAST, POST_FAILED_DELAY);
        }
        mCurrentState = STATE_FAILED;
        this.setStateSwitch();
        this.nextNotCan();
        collectLog("ipcLinkFailedCallBack", "--------3");
    }

    /**
     * ipc连接成功回调
     */
    public boolean ipcSucessCallBack(Object param2) {
        if (!mIsCanAcceptIPC) {
            return true;
        }
        collectLog("ipcLinkedCallBack", "ipc Conn----sucess!!!: ");
        XLog.tag(LogConst.TAG_CONNECTION).i("Connect device success");
        GolukDebugUtils.e("", "bindbind-------------ipcSucessCallBack  :");
        mIsCanAcceptIPC = false;
        // isAutoConn = false;
        GolukDebugUtils.e("", "WiFiLinkListActivity   ipc连接成功回调---ipcLinkedCallBack---1");
        this.dimissLoadingDialog();
        // 标识已连接ipc热点,可以点击下一步
//        this.nextCan();
//        this.setStateSwitch();
        collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3 Ipc Wifi Connected");
        if (!mApp.isMainland()) {
//            mApp.setBinding(true);
//            mCurrentState = STATE_FAILED;
//            mNeverReceiveMessage = true;
//            collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3.1 International check version");
//            mApp.getIPCControlManager().getVersion();
            String str = (String) param2;
            if (TextUtils.isEmpty(str)) {
                collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3.2 International check version Failed");
                return true;
            }
            try {
                JSONObject json = new JSONObject(str);
                String version = json.optString("version");
                String model = json.optString("productname");
                WiFiInfo.IPC_MODEL = model;
                XLog.tag(LogConst.TAG_CONNECTION).i("Connect IPC info: model:%s, version:%s", model, version);
                int regionType = GolukUtils.judgeIPCDistrict(model, version);
//                if (regionType == GolukUtils.GOLUK_APP_VERSION_MAINLAND && !mApp.isMainland()) {
//                    mApp.isIpcConnSuccess = false;
//                    mCurrentState = STATE_FAILED;
//                    mBaseHandler.sendEmptyMessage(MSG_H_REGION);
//                    collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3.2 International check version forbidden");
//                    return false;
//                } else {
                    mCurrentState = STATE_SUCCESS;
                    mNeverReceiveMessage = false;
                    collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3.2 International check version allowed");
                    doConnect();
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            mCurrentState = STATE_SUCCESS;
            doConnect();
        }
        return true;
    }

    @Override
    protected void onResume() {
        mApp.setContext(this, "WiFiLinkList");
        super.onResume();
        XLog.tag(LogConst.TAG_CONNECTION).i("Device connect page onResume()");

        // 选了设备型号并跳转到WIFI页面回来后
        if (mNeedAutoConnectAfterSelectDeviceType) {
            // T2S
            if (mDeviceTypeSelected != null &&
                    (mDeviceTypeSelected.startsWith("T2S") || mDeviceTypeSelected.startsWith("F30")
                            || mDeviceTypeSelected.startsWith("T4") || mDeviceTypeSelected.startsWith("T4U")
                            || mDeviceTypeSelected.startsWith("F20") || mDeviceTypeSelected.startsWith("T3Pro")
                            || mDeviceTypeSelected.startsWith("F5"))) {
                connectT2S();
            } else {
                sendLogicLinkIpc();
            }
            mNeedAutoConnectAfterSelectDeviceType = false;
            return;
        }

        // 如果是自动连接并且已经连上Goluk_T1s热点
        if (mAutoConn && mWac.isConnectedT1sWifi()) {
            GolukDebugUtils.e(Const.LOG_TAG, "Current connected WIFI is T1SP type");
            connectT2S();
            return;
        }

        // 自动获取WIFI信息,自动连接
        if (mAutoConn) {
            autoConnWifi();
        }

        if (!mAutoConn) {
            mStartSystemWifi = true;
            isBackFromWifiListPage = true;
            GolukUtils.startSystemWifiList(this);
            mAutoConn = true;
            return;
        }
//        if (!isBackFromWifiListPage && !isGolukDeviceWifi()) {
//            return;
//        }
//        collectLog("onResume", "----1:");
//        if (WifiBindDataCenter.getInstance().isHasDataHistory() || mStartSystemWifi)
//            autoConnWifi();
        mIsCanAcceptNetState = true;
        isBackFromWifiListPage = false;
    }

    /**
     * T1SP连接
     */
    private void connectT2S() {
        ApiUtil.sendConnectTest(new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                // T2S 连接结果回调
                // 保存WIFI信息
                saveT2SInfo();
                // 同步时间
                TimeSync timeSync = new TimeSync();
                timeSync.syncTime();
                // 页面跳转
                goNextAfterT1SPConnected();
                GolukApplication.getInstance().setIpcLoginState(true);
            }

            @Override
            public void onFail(int i, int i1) {
            }
        });
    }

    /**
     * T1SP连接成功后跳转
     */
    private void goNextAfterT1SPConnected() {
        if (mIsFromUpgrade) {
            finish();
            return;
        }
        if (mIsFromManagerToUpgrade) {
            finish();
            return;
        }
        if (mGotoAlbum) {
            Intent photoalbum = new Intent(WiFiLinkListActivity.this, PhotoAlbumT1SPActivity.class);
            photoalbum.putExtra(PhotoAlbumT1SPActivity.CLOSE_WHEN_EXIT, true);
            photoalbum.putExtra("from", "cloud");
            startActivity(photoalbum);
            finish();
            return;
        }
        if (mIsFromRemoteAlbum) {
            EventBus.getDefault().post(new EventSingleConnSuccess());
            finish();
            return;
        }
        if (mReturnToMainAlbum) {
            Intent mainIntent = new Intent(WiFiLinkListActivity.this, MainActivity.class);
            startActivity(mainIntent);
            EventBus.getDefault().post(new EventFinishWifiActivity());
            EventBus.getDefault().post(new EventSingleConnSuccess());
            return;
        } else {
            T1SPConnecter.instance().finishRecordActivity(CarRecorderActivity.class);
            ViewUtil.goActivityAndClearTop(WiFiLinkListActivity.this, CarRecorderT1SPActivity.class);
            finish();
        }
    }

    /**
     * 保存T2S信息
     */
    private void saveT2SInfo() {
        // Product
        //getApp().getIPCControlManager().setProduceName(IPCControlManager.T2S_SIGN);
        // WIFI
        if (null == mWac)
            return;
        WifiRsBean wifiResult = mWac.getConnResult();
        if (null == wifiResult)
            return;

        mWillConnName = wifiResult.getIpc_ssid();
        mWillConnMac = wifiResult.getIpc_bssid();
        if (mWillConnName == null || mWillConnName.length() <= 0)
            return;

        saveConnectWifiMsg(mWillConnName, IPC_PWD_DEFAULT, mWillConnMac);

        // 保存绑定历史记录
        WifiRsBean beans = new WifiRsBean();
        beans.setIpc_mac(WiFiInfo.IPC_MAC);
        beans.setIpc_ssid(WiFiInfo.IPC_SSID);
        //beans.setIpc_ip(CONNECT_IPC_IP);
        beans.setIpc_pass(WiFiInfo.IPC_PWD);
        //beans.setPh_ssid(WiFiInfo.MOBILE_SSID);
        //beans.setPh_pass(WiFiInfo.MOBILE_PWD);
        mWac.saveConfiguration(beans);

        WifiBindHistoryBean historyBean = new WifiBindHistoryBean();
        historyBean.ipc_ssid = WiFiInfo.IPC_SSID;
        historyBean.ipc_pwd = WiFiInfo.IPC_PWD;
        historyBean.ipc_mac = WiFiInfo.IPC_MAC;
        historyBean.ipc_ip = CONNECT_IPC_IP;
        historyBean.ipcSign = WiFiInfo.IPC_MODEL;
        historyBean.mobile_ssid = WiFiInfo.IPC_SSID;
        historyBean.mobile_pwd = WiFiInfo.MOBILE_PWD;
        historyBean.state = WifiBindHistoryBean.CONN_USE;
        WifiBindDataCenter.getInstance().saveBindData(historyBean);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsCanAcceptNetState = false;
    }

    protected void autoConnWifi() {
        collectLog("autoConnWifi", "----auto Conn--1:" + mApp.isIpcConnSuccess);
        if (mApp.isIpcConnSuccess) {
            GolukDebugUtils.e("", "bindbind-------------onResume:" + mApp.isIpcLoginSuccess);
            if (isGetWifiBean()) {
                collectLog("autoConnWifi", "----auto Conn--2");
                // 连接成功，直接改变状态
                this.nextCan();
                mCurrentState = STATE_SUCCESS;
                this.setStateSwitch();
            }
        } else {
            GolukDebugUtils.e("", "bindbind-------------onResume 22 :" + mCurrentState);
            collectLog("autoConnWifi", "----auto Conn--3: " + mCurrentState);
            if (STATE_FAILED == mCurrentState || mCurrentState == STATE_SUCCESS) {
                if (mCurrentState == STATE_SUCCESS) {
                    mApp.mIPCControlManager.setVdcpDisconnect();
                }
                dealAutoConn();
            }
        }
    }

    public void onEventMainThread(EventWifiState event) {
        if (!mIsCanAcceptNetState) {
            collectLog("onEventMainThread", "wifi-----111");
            return;
        }
        if (EventConfig.WIFI_STATE == event.getOpCode() && event.getMsg()) {
            // 连接网络成功
            if (NetUtil.isWifiConnected(this)) {
                GolukDebugUtils.e("", "WifiLinkList------------wifi Change wifi");
                collectLog("onEventMainThread", "wifi-----222");
                XLog.tag(LogConst.TAG_CONNECTION).i("Net type changed, WIFI is connected");
                this.autoConnWifi();
            }
        }
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiNetworkInfo && wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_btn) {
            collectLog("onClick", "click Back: ");
            // 返回
            exit();
        } else if (id == R.id.next_btn) {
            doConnect();
        } else if (id == R.id.wifi_link_list_help) {
            Intent itSkill = new Intent(this, UserOpenUrlActivity.class);
            itSkill.putExtra(UserOpenUrlActivity.FROM_TAG, "skill");
            this.startActivity(itSkill);
        }
    }

    protected void doConnect() {
        // 已连接ipc热点,可以跳转到修改密码页面
        // 使用单向连接方式
        mApp.setEnableSingleWifi(true);
        if (STATE_SUCCESS == mCurrentState) {
            mApp.setBinding(false);
            toNextView();
        } else if (STATE_FAILED == mCurrentState) {
            collectLog("dialogManagerCallBack", "-Jump----System WifiLIst");
            XLog.tag(LogConst.TAG_CONNECTION).i("Go to system WIFI list page");
            mStartSystemWifi = true;
            isBackFromWifiListPage = true;
//            GolukUtils.startSystemWifiList(this);
            showSelectDeviceType(true);
        }
    }

    protected void setDefaultInfo() {
        WifiRsBean wifiInfo = mWac.getConnResult();
        if (wifiInfo != null) {
            mWillConnName = wifiInfo.getIpc_ssid();
            WiFiInfo.IPC_SSID = mWillConnName;
        }
        // 保存默认的信息
        WiFiInfo.IPC_PWD = IPC_PWD_DEFAULT;
        String wifiName = WiFiInfo.IPC_SSID;
        if (TextUtils.isEmpty(wifiName)) {
            //TODO 为什么会存在为空的状态，还搞不清楚。暂时加入这个判断避免crash
            return;
        }
        String name = wifiName.replace("Goluk", "GOLUK");
        WiFiInfo.MOBILE_SSID = name + "_s";
        WiFiInfo.MOBILE_PWD = MOBILE_HOT_PWD_DEFAULT;

        //当程序使用过单向连接之后，以后就一直使用单向连接
        if (mApp.getEnableSingleWifi()) {
            WifiRsBean beans = new WifiRsBean();
            beans.setIpc_mac(WiFiInfo.IPC_MAC);
            beans.setIpc_ssid(WiFiInfo.IPC_SSID);
            beans.setIpc_ip(CONNECT_IPC_IP);
            beans.setIpc_pass(WiFiInfo.IPC_PWD);
            beans.setPh_ssid(WiFiInfo.MOBILE_SSID);
            beans.setPh_pass(WiFiInfo.MOBILE_PWD);
            if (mWac != null)
                mWac.saveConfiguration(beans);

            // 保存绑定历史记录
            WifiBindHistoryBean historyBean = new WifiBindHistoryBean();
            historyBean.ipc_ssid = WiFiInfo.IPC_SSID;
            historyBean.ipc_pwd = WiFiInfo.IPC_PWD;
            historyBean.ipc_mac = WiFiInfo.IPC_MAC;
            historyBean.ipc_ip = CONNECT_IPC_IP;
            historyBean.ipcSign = WiFiInfo.IPC_MODEL;
            historyBean.mobile_ssid = WiFiInfo.MOBILE_SSID;
            historyBean.mobile_pwd = WiFiInfo.MOBILE_PWD;
            historyBean.state = WifiBindHistoryBean.CONN_USE;
            WifiBindDataCenter.getInstance().saveBindData(historyBean);
        }
    }

    protected void toNextView() {
        setDefaultInfo();
        mBaseHandler.removeMessages(SHOW_TOAST);
        if (mApp.getEnableSingleWifi()) {
            collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.4 Only Wifi Connected success");
            if (mIsFromUpgrade) {
                //EventBus.getDefault().post(new EventSingleConnSuccess());
                finish();
                return;
            }
            if (mIsFromManagerToUpgrade) {
                finish();
                return;
            }
            if (mGotoAlbum) {
                Intent photoalbum = new Intent(this, PhotoAlbumActivity.class);
                photoalbum.putExtra(PhotoAlbumActivity.CLOSE_WHEN_EXIT, true);
                photoalbum.putExtra("from", "cloud");
                startActivity(photoalbum);
                finish();
                return;
            }
            if (mIsFromRemoteAlbum) {
                EventBus.getDefault().post(new EventSingleConnSuccess());
                finish();
                return;
            }
            if (mReturnToMainAlbum) {
                Intent mainIntent = new Intent(WiFiLinkListActivity.this, MainActivity.class);
                startActivity(mainIntent);
                EventBus.getDefault().post(new EventFinishWifiActivity());
                EventBus.getDefault().post(new EventSingleConnSuccess());
                return;
            }
            Intent mainIntent = new Intent(WiFiLinkListActivity.this, CarRecorderActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mainIntent);
            EventBus.getDefault().post(new EventFinishWifiActivity());
        } else {
            collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.4 to Hotspot");
            Intent modifyPwd = new Intent(WiFiLinkListActivity.this, WiFiLinkCompleteActivity.class);
            modifyPwd.putExtra("com.mobnote.golukmain.wifiname", WiFiInfo.IPC_SSID);
            modifyPwd.putExtra(MainActivity.INTENT_ACTION_RETURN_MAIN_ALBUM, mReturnToMainAlbum);
//        modifyPwd.putExtra(WifiUnbindSelectTypeActivity.KEY_IPC_TYPE, mIPcType);
            startActivity(modifyPwd);
        }
    }

    protected void nextCan() {
        // mIsConnSucess = true;
        mNextBtn.setBackgroundResource(R.drawable.ipcbind_btn_able);
    }

    private void nextNotCan() {
        // mNextBtn.setBackgroundResource(R.drawable.connect_btn_finish_grey);
        // mIsConnSucess = false;
    }

    private void exit() {
        mApp.setBinding(false);
        mApp.mIPCControlManager.setVdcpDisconnect();
        reportLog();
        finish();
        this.dimissLoadingDialog();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            collectLog("onKeyDown", "--WiFiLinkListActivity-------onKeyDown---------Back---111");
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onEventMainThread(EventFinishWifiActivity event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        GolukDebugUtils.e("", "jyf-----WifiBind-----List-----onDestroy----");
        collectLog("onDestroy", "onDestroy--1");
        XLog.tag(LogConst.TAG_CONNECTION).i("Leave device connect page");
        if (null != mWac) {
            mWac.unbind();
            mWac = null;
        }
        if (null != mIpcSignalAnim) {
            mIpcSignalAnim.stop();
        }
        if (!mApp.isMainland()) {
            mApp.getIPCControlManager().removeIPCManagerListener("carversion");
        }
        this.dimissLoadingDialog();
    }

    @Override
    public void wifiCallBack(int type, int state, int process, String message, Object arrays) {

    }

    @Override
    public void forbidBackKey(int backKey) {
        if (1 == backKey) {
            connFailed();
        }
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
//        if (IPC_VDCP_Msg_GetVersion == msg && mNeverReceiveMessage) {
//            if (param1 == RESULE_SUCESS) {
//                String str = (String) param2;
//                if (TextUtils.isEmpty(str)) {
//                    collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3.2 International check version Failed");
//                    return;
//                }
//                try {
//                    JSONObject json = new JSONObject(str);
//                    String version = json.optString("version");
//                    String model = json.optString("productname");
//                    WiFiInfo.IPC_MODEL = model;
//                    int regionType = GolukUtils.judgeIPCDistrict(model, version);
//                    if (regionType == GolukUtils.GOLUK_APP_VERSION_MAINLAND && !mApp.isMainland()) {
//                        mApp.isIpcConnSuccess = false;
//                        mCurrentState = STATE_FAILED;
//                        mBaseHandler.sendEmptyMessage(MSG_H_REGION);
//                        collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3.2 International check version forbidden");
//                    } else {
//                        mCurrentState = STATE_SUCCESS;
//                        mNeverReceiveMessage = false;
//                        collectLog(GolukDebugUtils.WIFI_CONNECT_LOG_TAG, "2.3.2 International check version allowed");
//                        doConnect();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    private final int MSG_H_REGION = 1;

    @Override
    protected void hMessage(Message msg) {
        if (MSG_H_REGION == msg.what) {
            this.dimissLoadingDialog();
            mApp.setCanNotUse(true);
            GolukUtils.showToast(WiFiLinkListActivity.this, getResources().getString(R.string.interantion_ban_mainland_goluk));
            //mApp.disableWiFiAndLogOutDevice();
            finish();
        } else if (SHOW_TOAST == msg.what) {
//            GolukUtils.showToast(this, getResources().getString(R.string.wifi_link_conn_failed));
            showConnectFailedDialog();
        }
    }

    public void selectDeviceType(View view) {
        showSelectDeviceType(true);
    }

    private String mDeviceTypeSelected = "";
    private boolean mNeedAutoConnectAfterSelectDeviceType;

    public void showSelectDeviceType(final boolean needGotoSystemWifiList) {
        mNeedAutoConnectAfterSelectDeviceType = needGotoSystemWifiList;
        DeviceTypeSelector typeSelector = new DeviceTypeSelector();
        typeSelector.showDeviceTypeList(this, new DeviceTypeSelector.OnDeviceTypeSelectListener() {
            @Override
            public void onTypeSelected(String type) {
                mDeviceTypeSelected = type;
                IPCControlManager ipcControlManager = mApp.getIPCControlManager();
                if (ipcControlManager != null) {
                    if (!type.startsWith("T4") && !type.startsWith("T2S") && !type.startsWith("F30")
                            && !type.startsWith("T4U") && !type.startsWith("F20") && !type.startsWith("T3Pro") && !type.startsWith("F5"))
                        ipcControlManager.setProduceName(type);
                    ipcControlManager.setIpcMode();
                    if (!needGotoSystemWifiList) {
                        // T2S
                        if (type.startsWith("T2S") || type.startsWith("T4") || type.startsWith("T4U")
                                || type.startsWith("F30") || type.startsWith("F20") || type.startsWith("T3Pro")
                                || type.startsWith("F5")) {
                            connectT2S();
                        } else {
                            sendLogicLinkIpc();
                        }
                    } else {
                        GolukUtils.startSystemWifiList(WiFiLinkListActivity.this);
                    }
                }
            }
        });
    }

}
