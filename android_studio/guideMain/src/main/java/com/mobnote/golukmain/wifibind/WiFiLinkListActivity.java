package com.mobnote.golukmain.wifibind;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBinding;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventFinishWifiActivity;
import com.mobnote.eventbus.EventWifiState;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.reportlog.ReportLogManager;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.wifibind.WifiConnCallBack;
import com.mobnote.wifibind.WifiConnectManager;
import com.mobnote.wifibind.WifiRsBean;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * Wifi扫描列表
 */
public class WiFiLinkListActivity extends BaseActivity implements OnClickListener, WifiConnCallBack, ForbidBack {

    private static final String TAG = "WiFiLinkList";
    public static final String CONNECT_IPC_IP = "192.168.62.1";

    private final String G1G2_ShowName = " Goluk xxxxxx ";
    private final String T1_ShowName = " Goluk_T1_xxxxxx ";
    private final String T1S_ShowName = " Goluk_T1S_xxxxxx ";
    private final String T2_ShowName = " Goluk_T2_xxxxxx ";
    private final String GOLUK_COMMON_SHOW_NAME = " Goluk_xx_xxxxxx ";

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
//    private String mIpcRealtype = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(getContentViewResourceId());
        // 获得GolukApplication对象
        mApp = (GolukApplication) getApplication();
        mApp.setContext(this, TAG);
        // 清除数据
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).clear();
        // 写日志，表示绑定失败
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setType("2");
        collectLog("onCreate", "---1");

        getIntentData();

//        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setHdType(mIpcRealtype);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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
//            mIpcRealtype = intent.getStringExtra(WifiUnbindSelectTypeActivity.KEY_IPC_REAL_TYPE);
            mReturnToMainAlbum = intent.getBooleanExtra(MainActivity.INTENT_ACTION_RETURN_MAIN_ALBUM, false);
        }
    }

    protected int getContentViewResourceId() {
        return R.layout.wifi_link_list;
    }

    private String getWifiShowName() {
        return GOLUK_COMMON_SHOW_NAME;

//        if (TextUtils.isEmpty(mIpcRealtype)) {
//            return G1G2_ShowName;
//        }
//        if (IPCControlManager.T1_SIGN.equals(mIpcRealtype)) {
//            return T1_ShowName;
//        } else if (IPCControlManager.T1s_SIGN.equals(mIpcRealtype)) {
//            return T1S_ShowName;
//        } else if (IPCControlManager.T2_SIGN.equals(mIpcRealtype)) {
//            return T2_ShowName;
//        } else {
//            return G1G2_ShowName;
//        }
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
        if (null == bean) {
            GolukDebugUtils.e("", "bindbind-------------isGetWifiBean---failed2  :");
            collectLog("isGetWifiBean", "-----2");
            connFailed();
            return false;
        }
        collectLog("dealAutoConn", "-----5 NOT  NULL");
        mWillConnName = bean.getIpc_ssid();
        mWillConnMac = bean.getIpc_bssid();
        if (mWillConnName == null || null == mWillConnMac || mWillConnName.length() <= 0 || mWillConnMac.length() <= 0) {
            GolukDebugUtils.e("", "bindbind-------------isGetWifiBean---failed3  :");
            collectLog("isGetWifiBean", "-----3");
            // 连接失败
            connFailed();
            return false;
        }

        GolukDebugUtils.e("", "WifiBindList----sWillConnName2: " + mWillConnName);
        String t_type = IPCControlManager.MODEL_G;
        if (mWillConnName.startsWith(GolukUtils.T1S_WIFINAME_SIGN)) {
            t_type = IPCControlManager.MODEL_T;
        } else {
            t_type = GolukUtils.getIpcTypeFromName(mWillConnName);
        }
        //不再限定类型
//        if (!t_type.equals(mIPcType)) {
//            connFailed();
//            return false;
//        }

        collectLog("isGetWifiBean", "willConnName2:" + mWillConnName + "  willConnMac2:" + mWillConnMac);
        saveConnectWifiMsg(mWillConnName, IPC_PWD_DEFAULT, mWillConnMac);
        setIpcMode(mWillConnName);
        return true;
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
        // 获取当前连接的wifi
        if (!isGetWifiBean()) {
            return;
        }
        collectLog("dealAutoConn", "-----2");
        // 去连接IPC
        // isAutoConn = true;
        sendLogicLinkIpc();
    }

    private void collectLog(String method, String msg) {
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
                .addLogData(JsonUtil.getReportData(TAG, method, msg));
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
        boolean b = mApp.mIPCControlManager.setIPCWifiState(true, CONNECT_IPC_IP);
        GolukDebugUtils.e("", "bindbind-------------sendLogicLinkIpc  :" + b);
        collectLog("sendLogicLinkIpc", "--------3------: " + b);
        if (b) {
            this.showLoadingDialog();
            mCurrentState = STATE_CONNING;
            this.setStateSwitch();
        } else {
            connFailed();
        }
        GolukDebugUtils.e("", "WiFiLinkListActivity 通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);
    }

    private void connFailed() {
        collectLog("connFailed", "--------1------: ");
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

    public void ipcFailedCallBack() {
        collectLog("ipcLinkFailedCallBack", "--------1");
        GolukDebugUtils.e("", "WiFiLinkListActivity  通知logic连接ipc---dealAutoConn--------ipcLinkFailedCallBack：");
        mApp.mIPCControlManager.setVdcpDisconnect();
        // if (!mIsCanAcceptIPC) {
        // return;
        // }

        GolukDebugUtils.e("", "bindbind-------------ipcFailedCallBack  :");

        collectLog("ipcLinkFailedCallBack", "--------2");
        mIsCanAcceptIPC = false;
        this.dimissLoadingDialog();
        GolukUtils.showToast(this, getResources().getString(R.string.wifi_link_conn_failed));
        mCurrentState = STATE_FAILED;
        this.setStateSwitch();
        this.nextNotCan();
        collectLog("ipcLinkFailedCallBack", "--------3");
    }

    /**
     * ipc连接成功回调
     */
    public void ipcSucessCallBack() {
        if (!mIsCanAcceptIPC) {
            return;
        }
        collectLog("ipcLinkedCallBack", "ipc Conn----sucess!!!: ");
        GolukDebugUtils.e("", "bindbind-------------ipcSucessCallBack  :");
        mIsCanAcceptIPC = false;
        // isAutoConn = false;
        GolukDebugUtils.e("", "WiFiLinkListActivity   ipc连接成功回调---ipcLinkedCallBack---1");
        this.dimissLoadingDialog();
        // 标识已连接ipc热点,可以点击下一步
//        this.nextCan();
        mCurrentState = STATE_SUCCESS;
//        this.setStateSwitch();
        doConnect();
    }

    @Override
    protected void onResume() {
        mApp.setContext(this, "WiFiLinkList");
        super.onResume();
        collectLog("onResume", "----1:");
        if (WifiBindDataCenter.getInstance().isHasDataHistory() || mStartSystemWifi)
            autoConnWifi();
        mIsCanAcceptNetState = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsCanAcceptNetState = false;
    }

    private void autoConnWifi() {
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
            if (isWifiConnected(this)) {
                GolukDebugUtils.e("", "WifiLinkList------------wifi Change wifi");
                collectLog("onEventMainThread", "wifi-----222");
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
        } else {
        }
    }

    protected void doConnect() {
        // 已连接ipc热点,可以跳转到修改密码页面
        if (STATE_SUCCESS == mCurrentState) {
            toNextView();
        } else if (STATE_FAILED == mCurrentState) {
            collectLog("dialogManagerCallBack", "-Jump----System WifiLIst");
            mStartSystemWifi = true;
            GolukUtils.startSystemWifiList(this);
        } else {

        }
    }

    protected void setDefaultInfo() {
        // 保存默认的信息
        WiFiInfo.IPC_PWD = IPC_PWD_DEFAULT;
        String wifiName = WiFiInfo.IPC_SSID;
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
            mWac.saveConfiguration(beans);

            // 保存绑定历史记录
            WifiBindHistoryBean historyBean = new WifiBindHistoryBean();
            historyBean.ipc_ssid = WiFiInfo.IPC_SSID;
            historyBean.ipc_pwd = WiFiInfo.IPC_PWD;
            historyBean.ipc_mac = WiFiInfo.IPC_MAC;
            historyBean.ipc_ip = CONNECT_IPC_IP;
            historyBean.mobile_ssid = WiFiInfo.MOBILE_SSID;
            historyBean.mobile_pwd = WiFiInfo.MOBILE_PWD;
            historyBean.state = WifiBindHistoryBean.CONN_USE;
            WifiBindDataCenter.getInstance().saveBindData(historyBean);
        }
    }

    protected void toNextView() {
        setDefaultInfo();
        if (mApp.getEnableSingleWifi()) {
            if (mReturnToMainAlbum) {
                Intent mainIntent = new Intent(WiFiLinkListActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            } else {
                Intent mainIntent = new Intent(WiFiLinkListActivity.this, CarRecorderActivity.class);
                startActivity(mainIntent);
                finish();
            }
        } else {
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
        LiveDialogManager.getManagerInstance().dismissTwoButtonDialog();
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
        if (null != mWac) {
            mWac.unbind();
            mWac = null;
        }
        if (null != mIpcSignalAnim) {
            mIpcSignalAnim.stop();
        }
        LiveDialogManager.getManagerInstance().dismissTwoButtonDialog();
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

}
