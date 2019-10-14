package com.mobnote.golukmain;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventDownloadState;
import com.mobnote.eventbus.EventIPCCheckUpgradeResult;
import com.mobnote.eventbus.EventIpcUpdateSuccess;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.golukmain.wifibind.WifiUnbindSelectListActivity;
import com.mobnote.golukmain.wifidatacenter.WifiBindDataCenter;
import com.mobnote.golukmain.wifidatacenter.WifiBindHistoryBean;
import com.mobnote.user.IPCInfo;
import com.mobnote.user.IpcUpdateManage;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;

import org.json.JSONObject;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;
import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackWifiInfo;

public class UnbindActivity extends BaseActivity implements OnClickListener, IPCManagerFn {
    public static final String TAG = "Unbind";
    /**
     * title
     **/
    private RelativeLayout mPwdLayout = null;
    private RelativeLayout mUpdateLayout = null;
    private TextView mTextPasswordName = null;
    private TextView mTextCameraName = null;
    private Button mUnbindBtn = null;

    private GolukApplication mApplication = null;
    private boolean isGetIPCSucess = false;
    private boolean canOfflineInstall = false;
    private boolean canOfflineInstallLater = false;
    private boolean downloadLater = false;

    private String mGolukSSID = "";
    private String mGolukPWD = "";
    private String mApSSID = "";
    private String mApPWD = "";
    /**
     * 固件版本号
     **/
    private TextView mTextVersion = null;
    /**
     * 获取版本号
     **/
    private String vIpc = "";
    private RelativeLayout mIPCViewLayout;
    private TextView mIPCModelText, mIPCNumberText, mIPCVersionText;
    private ImageView mIPCimage;
    private IPCInfo mIpcInfo;
    private RelativeLayout mNameLayout;

    // IPC固件升级成功Flag
    private boolean mIpcUpdateSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.unbind_layout);
        EventBus.getDefault().register(this);
        mApplication = (GolukApplication) getApplication();
        if (mApplication.getIPCControlManager() != null) {
            mApplication.getIPCControlManager().addIPCManagerListener(TAG, this);
        }
        initView();
    }

    // 初始化
    public void initView() {
        // title
        ImageButton mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        TextView mTextTitle = (TextView) findViewById(R.id.user_title_text);
        // body
        mTextCameraName = (TextView) findViewById(R.id.unbind_camera_name);
        mUnbindBtn = (Button) findViewById(R.id.unbind_layout_btn);
        mPwdLayout = (RelativeLayout) findViewById(R.id.unbind_layout_password);
        mNameLayout = (RelativeLayout) findViewById(R.id.unbind_layout_camera);
        mTextPasswordName = (TextView) findViewById(R.id.unbind_password_name);
        mTextVersion = (TextView) findViewById(R.id.unbind_update_name);
        mUpdateLayout = (RelativeLayout) findViewById(R.id.unbind_layout_update);
        mIPCViewLayout = (RelativeLayout) findViewById(R.id.rl_unbind_view);
        mIPCModelText = (TextView) findViewById(R.id.goluk_name);
        mIPCNumberText = (TextView) findViewById(R.id.goluk_mobile);
        mIPCVersionText = (TextView) findViewById(R.id.goluk_version);
        mIPCimage = (ImageView) findViewById(R.id.goluk_icon);
        mUnbindBtn.setVisibility(View.GONE);
        mTextTitle.setText(this.getResources().getString(R.string.my_camera_title_text));

        /**
         * 监听
         */
        mBackBtn.setOnClickListener(this);
        mUnbindBtn.setOnClickListener(this);
        mPwdLayout.setOnClickListener(this);
        mUpdateLayout.setOnClickListener(this);
        mNameLayout.setOnClickListener(this);
    }

    private void initViewData() {
        // 固件版本号
        vIpc = SharedPrefUtil.getIPCVersion();
        String ipcModel = GolukApplication.getInstance().mIPCControlManager.mProduceName;
        String ipcNumber = SharedPrefUtil.getIPCNumber();
        mUnbindBtn.setText(this.getResources().getString(R.string.str_ipc_change_others));
        mUnbindBtn.setVisibility(View.GONE);
        // 获取当前使用的信息
        WifiBindHistoryBean currentIpcInfo = WifiBindDataCenter.getInstance().getCurrentUseIpc();
        if ((currentIpcInfo != null) && (mIpcUpdateSuccess || (mApplication.isBindSucess() || mApplication.isIpcLoginSuccess))) {
            mIPCViewLayout.setVisibility(View.VISIBLE);
            mPwdLayout.setEnabled(true);
            // String ipcName = this.ipcName();
            mTextCameraName.setText(currentIpcInfo.ipc_ssid);
            mTextVersion.setText(vIpc);
            GolukApplication.getInstance().getIPCControlManager();
            if (IPCControlManager.T1_SIGN.equals(ipcModel) || IPCControlManager.T1s_SIGN.equals(ipcModel)
                    || IPCControlManager.T2_SIGN.equals(ipcModel) || IPCControlManager.T3_SIGN.equals(ipcModel) || IPCControlManager.T3U_SIGN.equals(ipcModel)) {
                mIPCimage.setImageResource(R.drawable.connect_t1_icon_1);
            } else {
                mIPCimage.setImageResource(R.drawable.ipc);
            }
            mIPCModelText.setText(this.getResources().getString(R.string.app_name) + ipcModel);
            mIPCNumberText.setText(this.getResources().getString(R.string.str_ipc_number_text) + ipcNumber);
            mIPCVersionText.setText(this.getResources().getString(R.string.str_ipc_version_text) + vIpc);
        } else {
            mUnbindBtn.setText(this.getResources().getString(R.string.str_ipc_change_bind_news));
            mIPCViewLayout.setVisibility(View.GONE);
            mPwdLayout.setEnabled(false);
            mTextVersion.setText("");
            mTextCameraName.setText("");
            mTextPasswordName.setText("");
        }
        // 密码
        if (null != mApplication && mApplication.isIpcLoginSuccess) {
            mApplication.getIPCControlManager().getIpcWifiConfig();
        }

        if (GolukApplication.getInstance().getIPCControlManager().isT2S()) {
            getWifiInfoT2S();
            return;
        }
    }

    private void getWifiInfoT2S() {
        ApiUtil.queryWifiInfo(new CallbackWifiInfo() {
            @Override
            public void onSuccess(String wifiName, String wifiPwd) {
                mTextCameraName.setText(wifiName);
                mTextPasswordName.setText(wifiPwd);
            }

            @Override
            public void onFail() {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApplication.setContext(this, TAG);
        initViewData();
        adaptDownloadState();
        ZhugeUtils.eventIpcManage(this);
    }

    private void adaptDownloadState() {
        boolean isConnectedIPC = (mApplication != null && mApplication.isIpcLoginSuccess);
        if (!isConnectedIPC) {
            mApplication.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_IPC, vIpc);
        }

        mUpdateLayout.setEnabled(true);
        if (mApplication.mIpcUpdateManage.isDownloading()) {// 下载中
            mTextVersion.setText(R.string.str_fireware_is_downloading);
        } else if (mApplication.mIpcUpdateManage.isDownloadSuccess()) {
            mTextVersion.setText(R.string.install_new_firmware);
        } else {
            if (mApplication.mIpcUpdateManage.isDownloadCached(vIpc)) {
                canOfflineInstall = true;
                downloadLater = false;
                mTextVersion.setText(R.string.install_new_firmware);
                return;
            }
            if (mApplication.isIpcLoginSuccess) {
                return;
            }
//            boolean canCheckServer = mApplication.mIpcUpdateManage.requestInfo(IpcUpdateManage.FUNCTION_SETTING_IPC, vIpc);
//            if (canCheckServer) {
//                return;
//            }
            isNewest();
        }
    }

    private void judgeIsDownloading() {
        if (mApplication.mIpcUpdateManage.isDownloading()) {
            mTextVersion.setText(R.string.str_fireware_is_downloading);
        }
    }

    private void isNewest() {
        mTextVersion.setText(R.string.newest_firmware);
        mUpdateLayout.setEnabled(false);
    }

    private void downloadLater(IPCInfo ipcInfo) {
        downloadLater = true;
        canOfflineInstall = false;
        mTextVersion.setText(R.string.str_update_find_new_first);
        mUpdateLayout.setEnabled(true);
        mIpcInfo = ipcInfo;

        judgeIsDownloading();
    }

    private void downloadNow(IPCInfo ipcInfo) {
        mTextVersion.setText(R.string.str_update_find_new_first);
        mUpdateLayout.setEnabled(true);
        canOfflineInstallLater = true;
        canOfflineInstall = false;
        mIpcInfo = ipcInfo;

        judgeIsDownloading();
    }

    private void installLater(IPCInfo ipcInfo) {
        mTextVersion.setText(R.string.install_new_firmware);
        mUpdateLayout.setEnabled(true);
        mIpcInfo = ipcInfo;
        canOfflineInstall = true;
        downloadLater = false;
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.back_btn) {
            this.finish();
        } else if (id == R.id.unbind_layout_btn) {
            Intent itWifiLink = new Intent(UnbindActivity.this, WifiUnbindSelectListActivity.class);
            startActivity(itWifiLink);
        } else if (id == R.id.unbind_layout_password) {
            if (!GolukApplication.getInstance().isIpcLoginSuccess) {
                Intent intent = new Intent();
                intent.setClass(this, WiFiLinkListActivity.class);
                intent.putExtra(WiFiLinkListActivity.ACTION_FROM_CAM_SETTING, true);
                startActivity(intent);
                return;
            }
            String password = mTextPasswordName.getText().toString();
            Intent it = new Intent(UnbindActivity.this, UserSetupChangeWifiActivity.class);
            it.putExtra("wifiPwd", password);
            it.putExtra("golukssid", mGolukSSID);
            it.putExtra("golukpwd", mGolukPWD);
            it.putExtra("apssid", mApSSID);
            it.putExtra("appwd", mApPWD);
            startActivityForResult(it, 10);
        } else if (id == R.id.unbind_layout_camera) {
            if (!GolukApplication.getInstance().isIpcLoginSuccess) {
                Intent intent = new Intent();
                intent.setClass(this, WiFiLinkListActivity.class);
                intent.putExtra(WiFiLinkListActivity.ACTION_FROM_CAM_SETTING, true);
                startActivity(intent);
                return;
            }
            String password = mTextPasswordName.getText().toString();
            Intent it = new Intent(UnbindActivity.this, UserSetupWifiActivity.class);
            it.putExtra("wifiPwd", password);
            it.putExtra("golukssid", mGolukSSID);
            it.putExtra("golukpwd", mGolukPWD);
            it.putExtra("apssid", mTextCameraName.getText().toString());
            it.putExtra("appwd", mApPWD);
            startActivityForResult(it, 11);
        } else if (id == R.id.unbind_layout_update) {
            if (mApplication.mIpcUpdateManage.isDownloadSuccess() || canOfflineInstall || canOfflineInstallLater
                    || !TextUtils.isEmpty(mApplication.mIpcUpdateManage.isHasIPCFile(mIpcInfo.version))) {
                GolukUtils.startUpdateActivity(UnbindActivity.this, 1, mIpcInfo, false);
            } else if (mApplication.mIpcUpdateManage.isDownloading() || downloadLater) {// 下载中
                GolukUtils.startUpdateActivity(UnbindActivity.this, 0, mIpcInfo, false);
            }
        }
    }

    public void onEventMainThread(EventIPCCheckUpgradeResult event) {
        mApplication.mIpcUpdateManage.setNeedShowIpcDialog(false);

        if (event.ResultType == EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW) {
            isNewest();
        } else if (event.ResultType == EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW_DELAY) {
            downloadLater(event.ipcInfo);
        } else if (event.ResultType == EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW_OFFLINE_INSTALL_DELAY) {
            downloadNow(event.ipcInfo);
        } else if (event.ResultType == EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW_INSTALL_DELAY) {
            installLater(event.ipcInfo);
        }
    }

    /**
     * 固件升级成功Event
     */
    public void onEventMainThread(EventIpcUpdateSuccess event) {
        if (event != null) {
            mIpcUpdateSuccess = true;
        }
    }

    /**
     * 固件下载状态
     */
    public void onEventMainThread(EventDownloadState event) {
        if (event == null)
            return;
        if (event.isDownloading()) {
            mTextVersion.setText(R.string.str_fireware_is_downloading);
        } else if (event.isDownloadSuccess()) {
            mTextVersion.setText(R.string.install_new_firmware);
        }
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (ENetTransEvent_IPC_VDCP_CommandResp != event) {
            return;
        }
        if (msg == IPC_VDCP_Msg_GetWifiCfg) {
            if (0 == param1) {
                // 获取成功
                callBack_getPwdSuccess((String) param2);
            } else {
                // 获取失败
                GolukUtils.showToast(this, this.getResources().getString(R.string.str_getwificfg_fail));
            }
            GolukDebugUtils.v("", "jyf-----UnbindActivity-----IPCManage_CallBack event:" + event + " msg:" + msg + " param1:" + param1 + " param2:" + param2);
        }
    }

    /**
     * 获取IPC密码成功
     *
     * @param jsonPwd 密码
     */
    private void callBack_getPwdSuccess(String jsonPwd) {
        try {
            JSONObject obj = new JSONObject(jsonPwd);
            mGolukSSID = obj.getString("GolukSSID");
            mGolukPWD = obj.getString("GolukPWD");
            // 摄像头信息
            mApSSID = obj.getString("AP_SSID");
            mApPWD = obj.getString("AP_PWD");
            // SharedPrefUtil.saveIpcPwd(mApPWD);
            if (mApplication.isBindSucess() || mApplication.isIpcLoginSuccess) {
                mTextPasswordName.setText(mApPWD);
            }
            isGetIPCSucess = true;
        } catch (Exception e) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.str_getwificfg_fail));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GolukDebugUtils.e("", "-----UnbindActivity------onActivityResult request:" + requestCode + "  resultCode:" + resultCode);
        if (10 == requestCode && 10 == resultCode) {
            this.finish();
        } else if (11 == requestCode && 11 == resultCode) {
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (mApplication.getIPCControlManager() != null) {
            mApplication.mIpcUpdateManage.setNeedShowIpcDialog(true);
            mApplication.getIPCControlManager().removeIPCManagerListener(TAG);
        }
        if (GolukApplication.getInstance().isIpcLoginSuccess) {
            GolukApplication.getInstance().mIPCControlManager.setVdcpDisconnect();
            GolukApplication.getInstance().setIpcLoginOut();
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                wifiManager.disableNetwork(wifiInfo.getNetworkId());
            }
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
