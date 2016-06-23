package com.mobnote.golukmain.wifibind;

import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventBindResult;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventFinishWifiActivity;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.reportlog.ReportLog;
import com.mobnote.golukmain.reportlog.ReportLogManager;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.util.GolukFastJsonUtil;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.wifibind.WifiConnCallBack;
import com.mobnote.wifibind.WifiConnectManager;
import com.mobnote.wifibind.WifiRsBean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import cn.com.mobnote.module.msgreport.IMessageReportFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class WiFiLinkCompleteActivity extends BaseActivity implements OnClickListener, WifiConnCallBack,
        ILiveDialogManagerFn {

    private static final String TAG = "WiFiLinkBindAll";
    private static final String TAG_LOG = "WiFiLinkCompleteActivity";
    /**
     * 创建手机热点消息
     */
    private static final int MSG_H_CREATE_HOT = 100;
    /**
     * 计时时间到，跳转到“等待”界面
     */
    private static final int MSG_H_TO_WAITING_VIEW = 101;
    /**
     * 释放前两个界面
     */
    private static final int MSG_H_FREE_1 = 102;
    /**
     * 释放第二个界面
     */
    private static final int MSG_H_FREE_2 = 103;
    /**
     * 设置IPC配置消息超时时间
     */
    private static final int TIMEOUT_SETIPC = 6 * 1000;
    /**
     * application
     */
    private GolukApplication mApp = null;
    /**
     * 上下文
     */
    private Context mContext = null;
    /**
     * 返回按钮
     */
    private ImageButton mBackBtn = null;
    private WifiConnectManager mWac = null;
    private boolean isExit = false;
    /**
     * 中间的根布局
     */
    private FrameLayout mMiddleLayout = null;
    private WifiLinkSetIpcLayout layout1 = null;
    private WifiLinkWaitConnLayout layout2 = null;
    private WifiLinkSucessLayout layout3 = null;
    /**
     * 当前正在显示的布局
     */
    private ViewFrame mCurrentLayout = null;
    private Button mCompleteBtn = null;
    private ImageView mProgressImg = null;
    private int connectCount = 0;
    /**
     * ipc连接mac地址
     */
    private String mWiFiIp = "";
    private final int STATE_SET_IPC_INFO = 0;
    private final int STATE_WAIT_CONN = 1;
    private final int STATE_SUCESS = 2;
    private int mState = STATE_SET_IPC_INFO;
    private WifiManager mWifiManager = null;
    private int mStep = 0;
    /**
     * 用户要绑定的设备类型
     */
    private String mIPcType = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_link_complete2);
        // 获得GolukApplication对象
        mApp = (GolukApplication) getApplication();
        mApp.setContext(mContext, TAG);
        getIntentData();
        collectLog("onCreate", "-----1");
        mContext = this;
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWac = new WifiConnectManager(mWifiManager, this);
        initChildView();
        mMiddleLayout = (FrameLayout) findViewById(R.id.wifi_link_complete_frmelayout);
        mCompleteBtn = (Button) findViewById(R.id.complete_btn);
        mCompleteBtn.setOnClickListener(this);
        mProgressImg = (ImageView) findViewById(R.id.wifilink_progress);
        init();
        toSetIPCInfoView();
        setIpcLinkInfo();
        // 6秒后，没有配置成功，直接跳转“等待连接”界面
        mBaseHandler.sendEmptyMessageDelayed(MSG_H_TO_WAITING_VIEW, TIMEOUT_SETIPC);
        EventBus.getDefault().register(this);
    }

    public String getCurrentIpcType() {
        return mIPcType;
    }

    private void getIntentData() {
        Intent intent = this.getIntent();
        if (null != intent) {
            mIPcType = intent.getStringExtra(WifiUnbindSelectTypeActivity.KEY_IPC_TYPE);
        }
    }

    private void initChildView() {
        layout1 = new WifiLinkSetIpcLayout(this);
        layout2 = new WifiLinkWaitConnLayout(this);
        layout3 = new WifiLinkSucessLayout(this);
    }

    private void collectLog(String method, String msg) {
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
                .addLogData(JsonUtil.getReportData(TAG_LOG, method, msg));
    }

    @Override
    protected void hMessage(Message msg) {
        if (MSG_H_CREATE_HOT == msg.what) {
            createPhoneHot();
        } else if (MSG_H_TO_WAITING_VIEW == msg.what) {
            if (STATE_SET_IPC_INFO == mState) {
                // 直接跳转下个界面
                toWaitConnView();
                // 开始创建手机热点
                mBaseHandler.sendEmptyMessageDelayed(MSG_H_CREATE_HOT, 3 * 1000);
            }
        } else if (MSG_H_FREE_1 == msg.what) {
            freeLayout1();
        } else if (MSG_H_FREE_2 == msg.what) {
            freeLayout2();
        }
    }

    /**
     * 页面初始化
     */
    @SuppressLint("HandlerLeak")
    private void init() {
        // 获取页面元素
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        // 注册事件
        mBackBtn.setOnClickListener(this);
    }

    /**
     * 设置ipc信息，包括修改IPC的密码，IPC连接手机的热点信息
     */
    private void setIpcLinkInfo() {
        connectCount++;
        // 连接ipc热点wifi---调用ipc接口
        GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---1");
        collectLog("setIpcLinkInfo", "--setIpcLinkPhoneHot---1");
        final String json = getSetIPCJson();
        GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---2---josn---" + json);
        collectLog("setIpcLinkInfo", "--setIpcLinkPhoneHot---2---josn---" + json);
        boolean b = mApp.mIPCControlManager.setIpcLinkPhoneHot(json);
        GolukDebugUtils.e("", "通知ipc连接手机热点--setIpcLinkPhoneHot---3---b---" + b);
        collectLog("setIpcLinkInfo", "--setIpcLinkPhoneHot---3---b---" + b);
    }

    private String getSetIPCJson() {
        collectLog("getSetIPCJson", "----1");
        String testJson = getSetIpcJson11();
        GolukDebugUtils.e("", "WifiLink----------setIpc----Json:" + testJson);
        return testJson;
    }

    private String getSetIpcJson11() {
        try {
            JSONObject rootObj = new JSONObject();
            // if
            // (!IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName))
            // {
            if (!TextUtils.isEmpty(WiFiInfo.IPC_PWD)) {
                rootObj.put("AP_SSID", WiFiInfo.IPC_SSID);
                rootObj.put("AP_PWD", WiFiInfo.IPC_PWD);
            }
            // }
            // Station模式
            rootObj.put("GolukSSID", WiFiInfo.MOBILE_SSID);
            rootObj.put("GolukPWD", WiFiInfo.MOBILE_PWD);
            rootObj.put("GolukIP", DEFAULT_IP);
            rootObj.put("GolukGateway", DEFAULT_WAY);
            return rootObj.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 设置IPC信息成功回调
     */
    public void setIpcLinkWiFiCallBack(int state) {
        if (STATE_SET_IPC_INFO != mState) {
            return;
        }
        collectLog("setIpcLinkWiFiCallBack", "---1 :   " + state);
        if (0 == state) {
            mBaseHandler.removeMessages(MSG_H_TO_WAITING_VIEW);
            // 隐藏loading
            toWaitConnView();
            // 开始创建手机热点
            mBaseHandler.sendEmptyMessageDelayed(MSG_H_CREATE_HOT, 3 * 1000);
            collectLog("setIpcLinkWiFiCallBack", "--------: 2");
        } else {
            collectLog("setIpcLinkWiFiCallBack", "---: 3 failed:  " + connectCount);
            if (connectCount > 3) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.wifi_link_bind_failed));
            } else {
                GolukUtils.showToast(this, this.getResources().getString(R.string.wifi_link_bind_failed_retry));
                setIpcLinkInfo();
            }
        }
    }

    /**
     * 创建手机热点
     */
    private void createPhoneHot() {
        String wifiName = WiFiInfo.MOBILE_SSID;
        String pwd = WiFiInfo.MOBILE_PWD;
        String ipcssid = WiFiInfo.IPC_SSID;
        String ipcmac = WiFiInfo.IPC_MAC;
        // 创建热点之前先断开ipc连接
        mApp.mIPCControlManager.setVdcpDisconnect();
        // 改变Application-IPC退出登录
        mApp.setIpcLoginOut();
        // 调用韩峥接口创建手机热点
        GolukDebugUtils
                .e("", "创建手机热点---startWifiAp---1---" + wifiName + "---" + pwd + "---" + ipcssid + "---" + ipcmac);

        collectLog("createPhoneHot", wifiName + "---" + pwd + "---" + ipcssid + "---" + ipcmac);

        mWac.createWifiAP(wifiName, pwd, ipcssid, ipcmac);
    }

    /**
     * 通知logic连接ipc
     */
    private void sendLogicLinkIpc(String ip, String ipcmac) {
        // 连接ipc热点wifi---调用ipc接口
        GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1---ip---" + ip);
        collectLog("sendLogicLinkIpc", "11--ip: " + ip + "   ipcmac:" + ipcmac);
        GolukApplication.mIpcIp = ip;
        mWiFiIp = ip;
        boolean b = mApp.mIPCControlManager.setIPCWifiState(true, ip);
        GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---2---b---" + b);

        collectLog("sendLogicLinkIpc", "2---b:  " + b);
    }

    /**
     * ipc连接成功回调
     */
    public void ipcLinkWiFiCallBack(Object param2) {
        collectLog("ipcLinkWiFiCallBack", "*****   Bind Sucess ! *****");

        IpcConnSuccessInfo ipcInfo = null;
        if (null != param2) {
            ipcInfo = GolukFastJsonUtil.getParseObj((String) param2, IpcConnSuccessInfo.class);
            ipcInfo.lasttime = String.valueOf(System.currentTimeMillis());
        }

        // 设置绑定成功
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setType(ReportLog.TYPE_SUCESS);
        final String hdtype = null != ipcInfo ? ipcInfo.productname : "";
        ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setHdType(hdtype);
        reportLog();
        mApp.mIPCControlManager.isNeedReportSn = true;
        if (mApp.mIPCControlManager.mDeviceSn != null) {
            mApp.mIPCControlManager.reportBindMsg();
        } else {
            // 异步获取 SN
            mApp.mIPCControlManager.getIPCIdentity();
        }

        this.toSucessView();
        // 保存连接数据
        WifiRsBean beans = new WifiRsBean();
        beans.setIpc_mac(WiFiInfo.IPC_MAC);
        beans.setIpc_ssid(WiFiInfo.IPC_SSID);
        beans.setIpc_ip(mWiFiIp);
        beans.setIpc_pass(WiFiInfo.IPC_PWD);

        beans.setPh_ssid(WiFiInfo.MOBILE_SSID);
        beans.setPh_pass(WiFiInfo.MOBILE_PWD);
        mWac.saveConfiguration(beans);

        // 保存绑定历史记录
        WifiBindHistoryBean historyBean = new WifiBindHistoryBean();
        historyBean.ipc_ssid = WiFiInfo.IPC_SSID;
        historyBean.ipc_pwd = WiFiInfo.IPC_PWD;
        historyBean.ipc_mac = WiFiInfo.IPC_MAC;
        historyBean.ipc_ip = mWiFiIp;
        historyBean.mobile_ssid = WiFiInfo.MOBILE_SSID;
        historyBean.mobile_pwd = WiFiInfo.MOBILE_PWD;
        historyBean.state = WifiBindHistoryBean.CONN_USE;

        if (null != ipcInfo) {
            historyBean.ipcSign = ipcInfo.productname;
            historyBean.serial = ipcInfo.serial;
            historyBean.version = ipcInfo.version;
            historyBean.lasttime = ipcInfo.lasttime;
        }
        WifiBindDataCenter.getInstance().saveBindData(historyBean);
        // 发送绑定成功的消息
        EventBus.getDefault().post(new EventBindResult(EventConfig.BIND_COMPLETE));
        mApp.setBinding(false);
    }

    private void reportLog() {
        final String jsonData = ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND)
                .getReportData();
        mApp.uploadMsg(jsonData, false);
        ReportLogManager.getInstance().removeKey(IMessageReportFn.KEY_WIFI_BIND);
    }

    /**
     * 退出页面设置
     */
    private void backSetup() {
        if (isExit) {
            return;
        }
        isExit = true;
        if (this.STATE_SET_IPC_INFO == mState) {
            // 不支持返回
        } else if (this.STATE_WAIT_CONN == mState) {
            removeHMsg();
            this.finish();
            // 没连接,关闭热点
            if (null != mWac) {
                mWac.closeWifiAP();
            }
        } else if (this.STATE_SUCESS == mState) {
            // 不支持返回
        }
    }

    private void removeHMsg() {
        this.mBaseHandler.removeMessages(MSG_H_FREE_1);
        this.mBaseHandler.removeMessages(MSG_H_FREE_2);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            GolukDebugUtils.e("", "按下系统返回键---WiFiLinkCompleteActivity---1");
            collectLog("onKeyDown", " 11111");
            // 返回关闭全部页面
            backSetup();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    ;

    public void onEventMainThread(EventFinishWifiActivity event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeHMsg();
        EventBus.getDefault().unregister(this);
        if (null != mWac) {
            mWac.unbind();
            mWac = null;
        }
        LiveDialogManager.getManagerInstance().dismissSingleBtnDialog();
        GolukDebugUtils.e("", "通知logic停止连接ipc---WiFiLinkCompleteActivity---onDestroy---1");

        GolukDebugUtils.e("", "jyf-----WifiBind-----WifiCompelete-----onDestroy----");
        mMiddleLayout.removeAllViews();
        mMiddleLayout = null;
        mCurrentLayout = null;
        collectLog("onDestroy", "1");
        freeLayout1();
        freeLayout2();
        freeLayout3();
    }

    private void freeLayout1() {
        if (null != layout1) {
            layout1.free();
            layout1 = null;
        }
    }

    private void freeLayout2() {
        if (null != layout2) {
            layout2.free();
            layout2 = null;
        }
    }

    private void freeLayout3() {
        if (null != layout3) {
            layout3.free();
            layout3 = null;
        }
    }

    @Override
    protected void onResume() {
        mApp.setContext(this, TAG);
        LiveDialogManager.getManagerInstance().setDialogManageFn(this);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_btn) {
            backSetup();
        } else if (id == R.id.complete_btn) {
            click_complete();
        }
    }

    private void click_complete() {
        if (this.STATE_SUCESS == mState) {
            // 綁定成功后，可以进入行车记录仪
            // 关闭wifi绑定全部页面
            EventBus.getDefault().post(new EventFinishWifiActivity());
            if (null != mWac) {
                mWac.unbind();
            }
            mWac = null;
            // GolukApplication.getInstance().stopDownloadList();// 停止视频同步

            //IPC页面访问统计
            ZhugeUtils.eventIpc(WiFiLinkCompleteActivity.this);

            Intent it = new Intent(WiFiLinkCompleteActivity.this, CarRecorderActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(it);
        }
    }

    private void toSetIPCInfoView() {
        mBackBtn.setVisibility(View.GONE);
        this.mState = STATE_SET_IPC_INFO;
        mMiddleLayout.removeAllViews();
        freeLayout();
        mMiddleLayout.addView(layout1.getRootLayout());
        mCurrentLayout = layout1;
        layout1.start();
    }

    private void toWaitConnView() {
        mBackBtn.setVisibility(View.VISIBLE);
        this.mState = STATE_WAIT_CONN;
        mMiddleLayout.removeAllViews();
        freeLayout();
        mMiddleLayout.addView(layout2.getRootLayout());
        mCurrentLayout = layout2;
        layout2.start();
        // 释放第一个界面
        mBaseHandler.sendEmptyMessageDelayed(MSG_H_FREE_1, 500);
    }

    private void toSucessView() {
        mBackBtn.setVisibility(View.GONE);
        this.mState = STATE_SUCESS;
        mMiddleLayout.removeAllViews();
        mMiddleLayout.addView(layout3.getRootLayout());
        mCurrentLayout = layout3;
        layout3.start();
        mCompleteBtn.setBackgroundResource(R.drawable.ipcbind_btn_finish);
        mProgressImg.setBackgroundResource(R.drawable.setp_4);
        this.mBaseHandler.sendEmptyMessageDelayed(MSG_H_FREE_2, 500);
    }

    private void freeLayout() {
        if (null != mCurrentLayout) {
            // mCurrentLayout.getRootLayout().setVisibility(View.GONE);
            mCurrentLayout.stop();
        }
    }

    private void wifiCallBack_3(int state, int process, String message, Object arrays) {
        if (state == 0) {
            switch (process) {
                case 0:
                    // 创建热点成功
                    break;
                case 1:
                    // ipc成功连接上热点
                    try {
                        WifiRsBean[] bean = (WifiRsBean[]) arrays;
                        if (null != bean) {
                            GolukDebugUtils.e("", "IPC连接上WIFI热点回调---length---" + bean.length);
                            collectLog("wifiCallBack_3", "IPC conn hot---length:" + bean.length);
                            if (bean.length > 0) {
                                sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
                            }
                        }
                    } catch (Exception e) {
                    }
                    break;
                default:
                    GolukUtils.showToast(mContext, message);
                    break;
            }
        } else {
            GolukUtils.showToast(mContext, message);
            connFailed();
        }
    }

    private void connFailed() {
        collectLog("connFailed", "WifiLinkCompleteActivity-----------connFailed : " + mStep);
        if (0 == mStep) {
            collectLog("connFailed", "connFailed show Dialog  please 5~10s");
            // 弹框提示用户重启GoLUK
            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
                    LiveDialogManager.DIALOG_TYPE_WIFIBIND_RESTART_IPC,
                    getResources().getString(R.string.wifi_link_prompt),
                    getResources().getString(R.string.wifi_link_blackout));
            mStep++;
        } else {
            collectLog("connFailed", "connFailed show Dialog Conn Failed");
            // 提示用户绑定失败，重新退出程序绑定
            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
                    LiveDialogManager.DIALOG_TYPE_WIFIBIND_FAILED,
                    this.getResources().getString(R.string.wifi_link_prompt),
                    getResources().getString(R.string.wifi_link_goluk_bind_failed));
        }
    }

    private void wifiCallBack_5(int state, int process, String message, Object arrays) {
        if (state == 0) {
            switch (process) {
                case 0:
                    // 创建热点成功
                    break;
                case 1:
                    // ipc成功连接上热点
                    wifiCallBack_ipcConnHotSucess(message, arrays);
                    break;
                case 2:
                    // 用户已经创建与配置文件相同的热点，
                    break;
                case 3:
                    // 用户已经连接到其它wifi，按连接失败处理
                    connFailed();
                    break;
                default:
                    break;
            }
        } else {
            // 未连接
            connFailed();
        }
    }

    private void wifiCallBack_ipcConnHotSucess(String message, Object arrays) {
        WifiRsBean[] bean = (WifiRsBean[]) arrays;
        if (null == bean) {
            return;
        }
        GolukDebugUtils.e("", "自动wifi链接IPC连接上WIFI热点回调---length---" + bean.length);
        collectLog("wifiCallBack_ipcConnHotSucess", "自动wifi链接IPC连接上WIFI热点回调---length---" + bean.length);
        if (bean.length > 0) {
            GolukDebugUtils.e("", "通知logic连接ipc---sendLogicLinkIpc---1---ip---");
            collectLog("wifiCallBack_ipcConnHotSucess", "1");
            sendLogicLinkIpc(bean[0].getIpc_ip(), bean[0].getIpc_mac());
        } else {
            collectLog("wifiCallBack_ipcConnHotSucess", "2 ");
        }
    }

    @Override
    public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
        final String log = " type---" + type + "---state---" + state + "---process---" + process + "---message---"
                + message;
        collectLog("wifiCallBack", log);
        GolukDebugUtils.e("", log);

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

    @Override
    public void dialogManagerCallBack(int dialogType, int function, String data) {
        if (LiveDialogManager.DIALOG_TYPE_WIFIBIND_RESTART_IPC == dialogType) {
            if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
                collectLog("dialogManagerCallBack", "DIALOG_TYPE_WIFIBIND_RESTART_IPC---clickOK");
                mWac = new WifiConnectManager(mWifiManager, this);
                mWac.autoWifiManage(WiFiInfo.IPC_SSID, WiFiInfo.IPC_PWD, WiFiInfo.MOBILE_SSID, WiFiInfo.MOBILE_PWD);
                mStep++;
            }
        } else if (LiveDialogManager.DIALOG_TYPE_WIFIBIND_FAILED == dialogType) {
            collectLog("dialogManagerCallBack", "DIALOG_TYPE_WIFIBIND_FAILED---onclick");

            ReportLogManager.getInstance().getReport(IMessageReportFn.KEY_WIFI_BIND).setType(ReportLog.TYPE_FAILED);
            reportLog();

            LiveDialogManager.getManagerInstance().dismissSingleBtnDialog();
            EventBus.getDefault().post(new EventFinishWifiActivity());
            if (null != mWac) {
                mWac.unbind();
            }
            mWac = null;
        }
    }

}
