package com.mobnote.user;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventIPCCheckUpgradeResult;
import com.mobnote.eventbus.EventIPCUpdate;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UpdateActivity;
import com.mobnote.golukmain.UserSetupActivity;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.upgrade.CheckUpgradeRequest;
import com.mobnote.golukmain.upgrade.bean.UpgradeResultbean;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Set;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * app升级+ipc升级
 *
 * @author mobnote
 */
public class IpcUpdateManage implements IPCManagerFn, IRequestResultListener {

    /**
     * 下载失败
     **/
    public static final int DOWNLOAD_STATUS_FAIL = 0;
    /**
     * 下载成功
     **/
    public static final int DOWNLOAD_STATUS_SUCCESS = 1;
    /**
     * 下载中
     **/
    public static final int DOWNLOAD_STATUS = 2;
    private static final String TAG = "lily";

    /**
     * BIN文件下载目录
     */
    public static final String BIN_PATH_PRE = "fs1:/update";

    /**
     * 启动APP
     **/
    public static final int FUNCTION_AUTO = 0;
    /**
     * 连接ipc
     **/
    public static final int FUNCTION_CONNECTIPC = 1;
    /**
     * 设置中点击版本检测
     **/
    public static final int FUNCTION_SETTING_APP = 2;
    /**
     * 设置中点击ipc升级
     **/
    public static final int FUNCTION_SETTING_IPC = 3;

    /**
     * version文件的路径
     */
    public static final String VERSION_PATH = "fs6:/version";
    /**
     * 下载类型
     */
    public static final int TYPE_DOWNLOAD = 0;
    /**
     * 安装
     **/
    public static final int TYPE_INSTALL = 1;
    /**
     * 读取本地ipc匹配列表
     **/
    private static final String ASSETS_IPC_FILE = "ipc_update.txt";

    /**
     * 设置中点击版本检测 / 固件升级中loading
     **/
    private CustomLoadingDialog mCustomLoadingDialog = null;
    private GolukApplication mApp = null;

    /**
     * 没有升级请求（没有任何操作）
     **/
    private int mFunction = -1;
    /**
     * 下载提示框
     */
    private AlertDialog mDownloadDialog = null;
    /**
     * 保存当前正在下载的BIN文件版本信息
     */
    public IPCInfo mDownLoadIpcInfo = null;
    private Builder mBuilder;
    private AlertDialog mAppUpdateDialog;

    /**
     * int state, Object param1, Object param2
     **/
    private int mState = -1;
    /**
     * 保存下载进度
     **/
    public Object mParam1 = -1;

    public IpcUpdateManage(GolukApplication mApp) {
        super();
        this.mApp = mApp;
    }

    /**
     * 升级 将appVersion和ipcVersion信息请求给服务器
     */
    public boolean requestInfo(int function, String vIpc) {
        XLog.i("start versionCheckRequest, curr function = " + function);
        CheckUpgradeRequest checkRequest = new CheckUpgradeRequest(IPageNotifyFn.PageType_CheckUpgrade, this);
        if (!UserUtils.isNetDeviceAvailable(mApp.getContext())) {
            if (function == FUNCTION_SETTING_APP || function == FUNCTION_SETTING_IPC) {
                GolukUtils.showToast(mApp.getContext(),
                        mApp.getContext().getResources().getString(R.string.user_net_unavailable));
            }
            return false;
        } else {
            if (isHasUpdateDialogShow()) {
                // 上次检查结果的Dialog还存在，不进行下次操作
                return false;
            }
//            this.cancelHttpRequest();
//            String ipcString = JsonUtil.putIPC(VERSION_PATH, vipc, mApp.mIPCControlManager.mProduceName);
//            boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
//                    IPageNotifyFn.PageType_CheckUpgrade, ipcString);
//            GolukDebugUtils.i(TAG, "=====" + b + "===ipcUpdateManage======");
            String vApp = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_GetVersion, VERSION_PATH);
            checkRequest.get(vApp, vIpc);
            mState = -1; //每次重新下载就重置状态
            mFunction = function;
            if (mFunction == FUNCTION_SETTING_APP || mFunction == FUNCTION_SETTING_IPC) {
                showLoadingDialog();
            }
            return true;
        }
    }

    public void showLoadingDialog() {
        dismissLoadingDialog();
        if (null == mCustomLoadingDialog) {
            mCustomLoadingDialog = new CustomLoadingDialog(this.mApp.getContext(), mApp.getContext().getResources()
                    .getString(R.string.str_ipc_update_checking));
        }
        mCustomLoadingDialog.show();
    }

    public void dismissLoadingDialog() {
        if (null != mCustomLoadingDialog) {
            mCustomLoadingDialog.close();
            mCustomLoadingDialog = null;
        }
    }

    /**
     * 取消升级Http请求
     */
    public void cancelHttpRequest() {
        mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_CheckUpgrade,
                JsonUtil.getCancelJson());
    }

    // ipc连接成功后调用
    public int connectIpc() {
        return mFunction;
    }

    /**
     * 通过版本号，获取本地文件，如果本地文件存在，則不下载，直接提示安装，如果不存在，則要提示下载
     *
     * @param ipcVersion IPC版本号
     * @return null　表示文件不存在
     */
    public String getLocalFile(String ipcVersion) {
        String fileInfo = SharedPrefUtil.getIpcLocalFileInfo();
        if (null == fileInfo || "".equals(fileInfo)) {
            return null;
        }
        IPCInfo ipcInfo = JsonUtil.getSingleIPCInfo(fileInfo);
        if (null == ipcInfo) {
            return null;
        }
        if (ipcVersion.equals(ipcInfo.version)) {
            try {
                final String filePath = BIN_PATH_PRE + "/" + ipcInfo.version + ".bin";
                final String abFilePath = FileUtils.libToJavaPath(filePath);
                File file = new File(abFilePath);
                if (file.exists()) {
                    return filePath;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 查询IPC升级文件的存放路径
     * 文件不存在，返回空串；
     * 文件存在，返回文件路径
     */
    public String isHasIPCFile(String ipcVersion) {
        String ipcStr = JsonUtil.selectIPCFile(ipcVersion, mApp.mIPCControlManager.mProduceName);
        String isExist = mApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
                IPageNotifyFn.PageType_GetIPCFile, ipcStr);
        GolukDebugUtils.e("", "---------------isHasIPCFile------isExist：" + isExist);
        return isExist;
    }

    /**
     * 获取下载的文件路径 (下载前获取一次，升级安装时获取一次)
     */
    public String getBinFilePath(String filename) {
        return BIN_PATH_PRE + "/" + filename + ".bin";
    }

    /**
     * 下载ipc文件
     *
     * @param url        IPC文件的url
     * @param ipcVersion IPC版本号
     */
    public boolean download(String url, String ipcVersion) {
        SharedPrefUtil.saveDownloadIpcModel(mApp.mIPCControlManager.mProduceName);
        String str = JsonUtil.ipcDownLoad(url, ipcVersion, mApp.mIPCControlManager.mProduceName);
        return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
                IPageNotifyFn.PageType_DownloadIPCFile, str);
    }

    /**
     * 下载ipc文件回调 void* pvUser, int type ,int state , unsigned long param1,
     * unsigned long param2 其中的state==2时，param1为下载进度数值（0~100）
     */
    public void downloadCallback(int state, Object param1, Object param2) {
        GolukDebugUtils.i("update", "--------IpcUpdateManage------state------" + state + "--------param1-----" + param1
                + "------param2------" + param2);
        mState = state;
        if (state == DOWNLOAD_STATUS) {
            mParam1 = param1;
        } else if (state == DOWNLOAD_STATUS_SUCCESS) {
            // 下载成功删除文件
            // downIpcSucess();
            SharedPrefUtil.saveNewFirmware(SharedPrefUtil.getIPCVersion(), true);
        } else if (state == DOWNLOAD_STATUS_FAIL) {
            mParam1 = -1;
        }
        if (mApp.getContext() != null && mApp.getContext() instanceof UpdateActivity) {
            ((UpdateActivity) mApp.getContext()).downloadCallback(state, param1, param2);
        }
    }

    /**
     * 判断下载提示框是否正在显示
     *
     * @return true/false 显示／未显示
     */
    public boolean isHasUpdateDialogShow() {
        return null != mDownloadDialog && mDownloadDialog.isShowing() || null != mAppUpdateDialog && mAppUpdateDialog.isShowing();
    }

    private void dimissDownLoadDialog() {
        if (null != mDownloadDialog) {
            mDownloadDialog.dismiss();
            mDownloadDialog = null;
        }
    }

    private void dimissAppDialog() {
        if (null != mAppUpdateDialog) {
            mAppUpdateDialog.dismiss();
            mAppUpdateDialog = null;
        }
    }

    /**
     * type: 0/1 下载／安装 ipc升级
     * <p/>
     * ipc下载文件
     */
    public void ipcUpgrade(final int type, final IPCInfo ipcInfo, String message) {
        if (ipcInfo == null || TextUtils.isEmpty(ipcInfo.version)) {
            return;
        }
        // 如果当前的请求来自启动页，且当前版本被忽略更新过，则不弹框
        String latestIgnoredIpcUpgradeVersion = SharedPrefUtil.getLatestIgnoredIpcUpgradeVersion();
        if (!TextUtils.isEmpty(latestIgnoredIpcUpgradeVersion)
                && mFunction == FUNCTION_AUTO
                && latestIgnoredIpcUpgradeVersion.equals(ipcInfo.version)) {
            return;
        }

        GolukDebugUtils.i(TAG, "------------isConnect-----------" + mApp.isIpcLoginSuccess);
        GolukDebugUtils.i(TAG, "=======弹出Dialog=======type：" + type);
        final String[] operate = {""};
        final String msg = TYPE_DOWNLOAD == type ? mApp.getContext().getResources().getString(R.string.str_download)
                : mApp.getContext().getResources().getString(R.string.str_update);

        LayoutInflater layoutInflater = LayoutInflater.from(mApp.getContext());
        View ignoreView = layoutInflater.inflate(R.layout.upgrade_ignore, null);
        TextView ignoreTV = (TextView) ignoreView.findViewById(R.id.tv_ignore);
        ignoreTV.setText(R.string.ignore_curr_ipc_version);
        final CheckBox ignoreCheckbox = (CheckBox) ignoreView.findViewById(R.id.checkbox_ignore);

        mDownloadDialog = new AlertDialog.Builder(mApp.getContext())
                .setTitle(mApp.getContext().getResources().getString(R.string.str_ipc_update_prompt))
                .setMessage(message).setPositiveButton(msg, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // 跳转升级界面
                        dimissDownLoadDialog();
                        operate[0] = mApp.getContext().getString(R.string.str_zhuge_ipc_update_dialog_operate_download);
                        GolukUtils.startUpdateActivity(mApp.getContext(), type, ipcInfo, true);
                    }
                }).setNegativeButton(mApp.getContext().getResources().getString(R.string.str_update_later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (mFunction == FUNCTION_AUTO && ignoreCheckbox.isChecked()) {
                            SharedPrefUtil.setLatestIgnoredIpcUpgradeVersion(ipcInfo.version);
                        }
                        operate[0] = mApp.getContext().getString(R.string.str_zhuge_ipc_update_dialog_operate_ignore);
                        if (type == 0) {
                            EventBus.getDefault().post(new EventIPCCheckUpgradeResult(EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW_DELAY, ipcInfo));
                        } else {
                            EventBus.getDefault().post(new EventIPCCheckUpgradeResult(EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW_INSTALL_DELAY, ipcInfo));
                        }
                    }
                })
                .setCancelable(false).create();
        if (mFunction == FUNCTION_AUTO) {
            mDownloadDialog.setView(ignoreView, 20,0,0,0);
        }
        ZhugeUtils.eventIpcUpdateDialog(mApp.getContext(), operate[0]);
        XLog.i("show the ipc upgrade dialog");

        mDownloadDialog.show();
    }

    /**
     * ipc升级解析
     */
    public IPCInfo ipcUpdateUtils(JSONArray ipc) {
        /**
         * APP不用升级的话，判断ipc是否需要升级 isnew 0不需要升级 1需要升级
         */
        IPCInfo ipcInfo = null;
        IPCInfo[] upgradeArray = JsonUtil.upgradeJson(ipc);
        if (upgradeArray != null) {
            for (IPCInfo ipcInstance : upgradeArray) {
                String ipc_filesize = ipcInstance.filesize;
                String ipc_content = ipcInstance.appcontent;
                // 保存ipc文件大小
                SharedPrefUtil.saveIpcFileSize(ipc_filesize);
                // 保存ipc更新信息
                SharedPrefUtil.saveIpcContent(ipc_content);

                String ipc_isnew = ipcInstance.isnew;
                if ("0".equals(ipc_isnew)) {
                    GolukDebugUtils.i(TAG, "ipc当前已是最新版本，不需要升级");
                } else if ("1".equals(ipc_isnew)) {
                    GolukDebugUtils.i(TAG, "ipc需要升级");
                    ipcInfo = ipcInstance;
                }
            }
        }

        return ipcInfo;
    }

    /**
     * 检测ipc需要升级后，进行升级
     */
    public void ipcUpgradeNext(IPCInfo ipcInfo) {
        if (null != ipcInfo) {
            //查询ipc升级文件是否存在
            String ipcFile = isHasIPCFile(ipcInfo.version);
            GolukDebugUtils.i(TAG, "------------ipcUpgrade-----------" + ipcFile);
            GolukDebugUtils.i(TAG, "=======弹出Dialog=======ipcFile：" + ipcFile);
            if ("".equals(ipcFile) || null == ipcFile) {
                // 提示用户下载文件Dialog
                ipcUpgrade(TYPE_DOWNLOAD, ipcInfo, ipcInfo.appcontent);
            } else {
                // 弹框提示用户安装本地的文件 (Dialog)
                ipcUpgrade(TYPE_INSTALL, ipcInfo, ipcInfo.appcontent);
            }
        }
    }

    /**
     * 下载BIN文件成功
     */
    public void downIpcSucess() {
        final String path = FileUtils.libToJavaPath(BIN_PATH_PRE);
        // 遍历文件夹，删除其它安装包
        try {
            if (null == mDownLoadIpcInfo) {
                return;
            }
            File fileDir = new File(path);
            if (!fileDir.isDirectory()) {
                return;
            }
            File[] allFile = fileDir.listFiles();
            if (null != allFile && 0 < allFile.length) {
                for (File anAllFile : allFile) {
                    if (!anAllFile.getName().contains(mDownLoadIpcInfo.version)) {
                        // 需要把文件删除
                        if (anAllFile.delete()) {
                            Log.e(TAG, "Can not delete file " + anAllFile.getAbsolutePath());
                        }
                    }
                }
            }
            if (null == mDownLoadIpcInfo) {
                return;
            }
            // 更新当前信息
            final String downInfo = JsonUtil.getSingleIPCInfoJson(mDownLoadIpcInfo);
            if (null != downInfo) {
                SharedPrefUtil.setIpcLocalFileInfo(downInfo);
            }
            // 当前文件

            mDownLoadIpcInfo = null;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * app升级解析
     */
    public void appUpgradeUtils(String goluk) {
        try {
            JSONObject jsonGoluk = new JSONObject(goluk);
            APPInfo appInfo = JsonUtil.appUpgradeJson(jsonGoluk);
            if (appInfo == null) {
                return;
            }
            String appcontent = appInfo.appcontent;
            String url = appInfo.url;
            String isUpdate = appInfo.isupdate;
            /**
             * APP升级 0非强制升级 1强制升级 非强制升级不退出程序，强制升级退出程序
             */
            if (isUpdate.equals("1")) {
                showUpgradeGoluk(mApp.getContext(), appcontent, url);
            } else if (isUpdate.equals("0")) {
                showUpgradeGoluk2(mApp.getContext(), appcontent, url);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 不匹配升级提示
     */
    public void showUnMatchDialog(final Context mContext, String message, final String vIpc) {
        Builder mBuilder = new AlertDialog.Builder(mContext);
        AlertDialog dialog = mBuilder
                .setTitle(mApp.getContext().getResources().getString(R.string.str_update_prompt))
                .setMessage(message)
                .setPositiveButton(mApp.getContext().getResources().getString(R.string.str_update_later), null)
                .setNegativeButton(mApp.getContext().getResources().getString(R.string.str_update),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                showLoadingDialog();
                                requestInfo(FUNCTION_CONNECTIPC, vIpc);
                            }
                        }).setCancelable(false).setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                }).create();
        dialog.show();
    }

    /**
     * ipc安装升级
     */
    public boolean ipcInstall(String filePath) {
        // 判断摄像头是否连接
        if (GolukApplication.getInstance().getIpcIsLogin()) {
            return update(filePath);
        } else {
            EventBus.getDefault().post(new EventIPCUpdate(EventConfig.UPDATE_IPC_UNUNITED));
            return false;
        }

    }

    /**
     * 升级流程
     */
    public boolean update(String filePath) {
        // 当前的版本号
        String current_version = SharedPrefUtil.getIPCVersion();
        // 获取升级ipc版本号
        try {
            String matchInfo = SharedPrefUtil.getIPCMatchInfo();
            JSONArray jsonArray = new JSONArray(matchInfo);
            IPCInfo[] upgradeArray = JsonUtil.upgradeJson(jsonArray);
            int length;
            if (null == upgradeArray) {
                length = 0;
            } else {
                length = upgradeArray.length;
            }
            for (int i = 0; i < length; i++) {
                String update_version = upgradeArray[i].version;
                if (current_version.equals(update_version)) {
//					GolukUtils.showToast(mApp.getContext(), "极路客固件版本号" + current_version + "，当前已是最新版本");
                    if (mApp.getContext() != null && mApp.getContext() instanceof UpdateActivity) {
                        ((UpdateActivity) mApp.getContext()).isNewVersion();
                    }
                } else {
                    // 判断是否有升级文件
                    boolean isHasFile = UserUtils.fileIsExists(filePath);
                    if (isHasFile) {
                        Boolean u = GolukApplication.getInstance().getIPCControlManager().ipcUpgrade(filePath);
                        if (u) {
                            // 正在准备文件，请稍候……
                            EventBus.getDefault().post(new EventIPCUpdate(EventConfig.UPDATE_PREPARE_FILE));
                        }
                        return u;
                    } else {
                        // 提示没有升级文件
                        EventBus.getDefault().post(new EventIPCUpdate(EventConfig.UPDATE_FILE_NOT_EXISTS));
                        return false;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ipc安装升级回调
     */
    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        GolukDebugUtils.i(TAG, "=========ipcInstallCallback===========");
        if (mApp.getContext() != null && mApp.getContext() instanceof UpdateActivity) {
            ((UpdateActivity) mApp.getContext()).IPCManage_CallBack(event, msg, param1, param2);
        }
    }

    /**
     * 强制升级提示
     */
    public void showUpgradeGoluk(final Context mContext, String message, final String url) {
        mBuilder = new AlertDialog.Builder(mContext);
        mAppUpdateDialog = mBuilder
                .setTitle(mApp.getContext().getResources().getString(R.string.str_app_update_prompt))
                .setMessage(message)
                .setPositiveButton(mApp.getContext().getResources().getString(R.string.str_app_download),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                dimissAppDialog();
                                // 浏览器打开url
                                GolukUtils.openUrl(url, mContext);

                                if (GolukApplication.mMainActivity != null) {
                                    GolukApplication.mMainActivity.finish();
                                    GolukApplication.mMainActivity = null;
                                }

                                mApp.setExit(true);
                                mApp.mIPCControlManager.setIPCWifiState(false, "");
                                mApp.destroyLogic();
                                mApp.appFree();

                                int PID = android.os.Process.myPid();
                                android.os.Process.killProcess(PID);
                                System.exit(0);

                            }
                        }).setCancelable(false).setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                }).create();
        mAppUpdateDialog.show();
        XLog.i("show the app force upgrade dialog");
    }

    /**
     * 非强制升级提示
     */
    public void showUpgradeGoluk2(final Context mContext, String message, final String url) {
        mBuilder = new AlertDialog.Builder(mContext);
        mAppUpdateDialog = mBuilder
                .setTitle(mApp.getContext().getResources().getString(R.string.str_app_update_prompt))
                .setMessage(message)
                .setPositiveButton(mApp.getContext().getResources().getString(R.string.str_update_later),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                dimissAppDialog();
                            }
                        })
                .setNegativeButton(mApp.getContext().getResources().getString(R.string.str_app_download),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                dimissAppDialog();
                                // 浏览器打开url
                                GolukUtils.openUrl(url, mContext);
                            }
                        }).setCancelable(false).setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                }).create();
        mAppUpdateDialog.show();
        XLog.i("show the app normal upgrade dialog");
    }

    /**
     * 升级停止
     */
    public boolean stopIpcUpgrade() {
        return !(null == mApp || null == mApp.mGoluk) && mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_StopIPCUpgrade, "");
    }

    /**
     * ipc自动连接后
     */
    public boolean ipcConnect() {
        try {
            String ipcVersion = SharedPrefUtil.getIPCVersion();
            GolukDebugUtils.i(TAG, "-----------match-----111-------" + ipcVersion);
            String matchInfo = SharedPrefUtil.getIPCMatchInfo();
            GolukDebugUtils.i("aaa", "------ipc匹配列表-----" + matchInfo);
            // 读取本地匹配列表
            JSONArray jsonArray;
            if ("".equals(matchInfo) || null == matchInfo) {
                String assetsMatchInfo = getMatchInfoFromAssets(ASSETS_IPC_FILE);
                GolukDebugUtils.i("aaa", "----assets------" + assetsMatchInfo);
                jsonArray = new JSONArray(assetsMatchInfo);
            } else {
                GolukDebugUtils.i(TAG, "----matchInfo----" + matchInfo);
                jsonArray = new JSONArray(matchInfo);
            }

            boolean isMatch = false;
            IPCInfo[] upgradeArray = JsonUtil.upgradeJson(jsonArray);
            int length;
            if (null == upgradeArray) {
                length = 0;
            } else {
                length = upgradeArray.length;
            }
            for (int i = 0; i < length; i++) {
                String version = upgradeArray[i].version;
                GolukDebugUtils.i(TAG, "--------match--222-------" + version);
                if (ipcVersion.equals(version)) {
                    // 匹配
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                // 判断app升级和ipc升级框是否弹出，如果都没有弹，弹不匹配的框，点击确定，请求数据
                if (!mApp.mIpcUpdateManage.isHasUpdateDialogShow()) {
                    mApp.mIpcUpdateManage.showUnMatchDialog(mApp.getContext(), mApp.getContext().getResources()
                            .getString(R.string.str_update_not_match), ipcVersion);
                }
            }
            return isMatch;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 读取本地匹配列表
     */
    public String getMatchInfoFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(mApp.getContext().getResources().getAssets()
                    .open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            String result = "";
            while ((line = bufReader.readLine()) != null) {
                result += line;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 防止重复点击
     */
    public boolean isCanClick() {
        if (GolukUtils.isCanClick) {
            GolukUtils.startTimer(500);
            return true;
        }

        return false;
    }

    public boolean isDownloading() {
        return mState == DOWNLOAD_STATUS;
    }

    public boolean isDownloadSuccess() {
        return mState == DOWNLOAD_STATUS_SUCCESS;
    }

    public boolean isDownloadCached(String vIPC) {
        if (TextUtils.isEmpty(vIPC)) {
            return false;
        }
        Set<String> cache = SharedPrefUtil.getNewFirmware();
        if (cache == null) {
            return false;
        }
        for (String temp : cache) {
            if (vIPC.equals(temp)) {
                if (!TextUtils.isEmpty(isHasIPCFile(SharedPrefUtil.getIPCDownVersion()))) {
//                    SharedPrefUtil.saveNewFirmware(vIPC, false);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        dismissLoadingDialog();
        if (requestType != IPageNotifyFn.PageType_CheckUpgrade) {
            Log.e(TAG, "This is really strange . how could be");
            return;
        }
        if (mApp.getContext() == null) {
            return;
        }
        if (result == null) {
            if (mFunction == FUNCTION_SETTING_APP || mFunction == FUNCTION_SETTING_IPC) {
                GolukUtils.showToast(mApp.getContext(), mApp.getContext().getResources().getString(R.string.str_timeout));
            }
            return;
        }
        UpgradeResultbean bean = (UpgradeResultbean) result;
        if (!bean.resultFlag) {
            return;
        }
        try {
            JSONObject jsonData = new JSONObject(JSON.toJSONString(bean.dataInfo));
            if (jsonData != null) {
                XLog.i("follow is versionCheckResponse");
                XLog.json(JSON.toJSONString(bean.dataInfo));
            }
            final String goluk = jsonData.getString("goluk");
            JSONArray ipc = jsonData.getJSONArray("ipc");
            // 保存ipc匹配信息
            SharedPrefUtil.saveIPCMatchInfo(ipc.toString());

            GolukDebugUtils.i(TAG, "===ipc----goluk=------" + goluk);

            if (FUNCTION_AUTO == mFunction) {

                if (goluk.equals("{}")) {
                    // APP不需要升级，判断ipc是否需要升级
                    IPCInfo ipcInfo = ipcUpdateUtils(ipc);
                    ipcUpgradeNext(ipcInfo);
                } else {
                    appUpgradeUtils(goluk);
                }

            } else if (FUNCTION_CONNECTIPC == mFunction) {
                // ipc连接后不匹配
                if (goluk.equals("{}")) {
                    // APP不需要升级，判断ipc是否需要升级
                    GolukDebugUtils.i("aaa", "--------ipc匹配列表------" + ipc);
                    if (0 == ipc.length()) {
                        String assetsMatchInfo = getMatchInfoFromAssets(ASSETS_IPC_FILE);
                        GolukDebugUtils.i("aaa", "----assets------" + assetsMatchInfo);
                        JSONArray jsonArray = new JSONArray(assetsMatchInfo);
                        IPCInfo ipcInfo = ipcUpdateUtils(jsonArray);
                        ipcUpgradeNext(ipcInfo);
                    } else {
                        IPCInfo ipcInfo = ipcUpdateUtils(ipc);
                        ipcUpgradeNext(ipcInfo);
                    }

                } else {
                    appUpgradeUtils(goluk);
                }
            } else if (FUNCTION_SETTING_APP == mFunction) {
                final Context tempContext = mApp.getContext();
                if (tempContext != null && tempContext instanceof UserSetupActivity) {
                    ((UserSetupActivity) tempContext).updateCallBack(FUNCTION_SETTING_APP, goluk);
                }

                if (goluk.equals("{}")) {
                    // 设置页版本检测需要提示
                    GolukUtils.showToast(mApp.getContext(),
                            mApp.getContext().getResources().getString(R.string.str_update_app_newest));
                } else {
                    appUpgradeUtils(goluk);
                }
            } else if (FUNCTION_SETTING_IPC == mFunction) {
                mApp.getContext();
                if (!mApp.isIpcLoginSuccess && !mApp.isBindSucess()) {
                    GolukUtils.showToast(mApp.getContext(),
                            mApp.getContext().getResources().getString(R.string.str_ipc_no_connect));
                    return;
                }

                IPCInfo ipcInfo = ipcUpdateUtils(ipc);
                if (ipcInfo == null) {
                    // ipc不需要升级
                    if (!mApp.isIpcLoginSuccess && !mApp.isBindSucess()) {
                        GolukUtils.showToast(mApp.getContext(),
                                mApp.getContext().getResources().getString(R.string.str_ipc_no_connect));
                    } else {
                        EventBus.getDefault().post(new EventIPCCheckUpgradeResult(EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW));
                    }
                } else {
                    /**
                     * ipc需要升级，前提是判断app是否需要升级 app不需要升级，直接下载并升级ipc
                     * app需要升级，先升级app
                     */
                    if (goluk.equals("{}")) {
                        // APP不需要升级
                        // 提示下载并升级ipc
                        ipcUpgradeNext(ipcInfo);
                    } else {
                        GolukDebugUtils.i(TAG, "--------ipcInfo.version-----" + ipcInfo.version);
                        new AlertDialog.Builder(mApp.getContext())
                                .setTitle(mApp.getContext().getResources().getString(R.string.str_update_prompt))
                                .setMessage(mApp.getContext().getResources()
                                        .getString(R.string.str_update_find_new_first)
                                        + ipcInfo.version
                                        + mApp.getContext().getResources()
                                        .getString(R.string.str_update_find_new_second))
                                .setNegativeButton(mApp.getContext().getResources().getString(R.string.str_update_later), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        EventBus.getDefault().post(new EventIPCCheckUpgradeResult(EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW_DELAY));
                                    }
                                })
                                .setPositiveButton(mApp.getContext().getResources().getString(R.string.str_button_ok),
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                appUpgradeUtils(goluk);
                                            }
                                        }).show();
                    }
                }

            }

        } catch (Exception e) {
            if (FUNCTION_AUTO != mFunction) {
                GolukDebugUtils.i(TAG, "ipc匹配列表为空");
                if (mApp.getContext() != null && mApp.getContext() instanceof UpdateActivity) {
                    ((UpdateActivity) mApp.getContext()).isNewVersion();
                }
            }
            e.printStackTrace();
        }
    }
}
