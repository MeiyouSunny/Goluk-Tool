package com.mobnote.golukmain;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventIPCCheckUpgradeResult;
import com.mobnote.eventbus.EventIPCUpdate;
import com.mobnote.eventbus.EventIpcUpdateSuccess;
import com.mobnote.eventbus.EventWifiConnect;
import com.mobnote.eventbus.EventWifiState;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.wifibind.WiFiLinkListActivity;
import com.mobnote.log.app.LogConst;
import com.mobnote.t1sp.upgrade.UpgradeListener;
import com.mobnote.t1sp.upgrade.UpgradeManager;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.user.DataCleanManage;
import com.mobnote.user.IPCInfo;
import com.mobnote.user.IpcUpdateManage;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.wifibind.WifiConnCallBack;
import com.mobnote.wifibind.WifiConnectManager;
import com.mobnote.wifibind.WifiRsBean;

import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * ??????????????????
 *
 * @author mobnote
 */
public class UpdateActivity extends BaseActivity implements OnClickListener, IPCManagerFn, WifiConnCallBack, UpgradeListener {
    /**
     * ?????? / ????????????
     **/
    private TextView mBtnDownload = null;
    /**
     * ????????????????????????
     **/
    private TextView mTextIpcVersion = null;
    /**
     * ?????????????????????
     **/
    private TextView mTextIpcSize = null;
    /**
     * ????????????
     **/
    private TextView mTextUpdateContent = null;
    /**
     * ????????? / ????????? / ?????????
     **/
    private TextView mTextDowload = null;
    /**
     * ??????????????????
     **/
    private ScrollView mScrollView = null;
    /**
     * ????????????????????????????????????
     **/
    private ImageView mUpdateNewImage = null;
    /**
     * ????????????????????????????????????
     **/
    private TextView mUpdateNewText = null;
    /**
     * ???????????????????????????????????????
     **/
    private ImageView mNoBreakImage, mtfCardImage;
    // ????????????
    private TextView mTvUpgradeHint;
    /**
     * ???????????????????????????????????????
     **/
    private TextView mNoBreakText, mtfCardText;
    /**
     * GolukApplication
     **/
    private GolukApplication mApp = null;

    /**
     * 0 ?????? 1??????
     **/
    public final static String UPDATE_SIGN = "update_sign";
    /**
     * ????????????
     **/
    public final static String UPDATE_DATA = "update_data";
    /**
     * ?????????????????????
     **/
    public final static String UPDATE_PROGRESS = "update_progress";
    /**
     * ?????????????????????
     **/
//    public final static String UPDATE_IS_NEW = "update_is_new";
    public final static String DOWNLOAD_ON_CREATE = "update_when_created";

    /**
     * 0?????? / 1???????????????
     **/
    private int mSign = 0;
    /**
     * ??????
     **/
    private IPCInfo mIpcInfo = null;

    /**
     * ipc?????????????????????UI??????
     **/
    private String mStage = "";
    private String mPercent = "";
    private Handler mUpdateHandler = null;
    private Timer mTimer = null;

    /**
     * ????????????
     **/
    public static final int UPDATE_TRANSFER_FILE = 12;
    /**
     * ??????????????????
     **/
    public static final int UPDATE_TRANSFER_OK = 13;
    /**
     * ????????????
     **/
    public static final int UPDATE_UPGRADEING = 14;
    /**
     * ????????????
     **/
    public static final int UPDATE_UPGRADE_OK = 15;
    /**
     * ????????????
     **/
    public static final int UPDATE_UPGRADE_FAIL = 16;
    /**
     * ???????????????
     **/
    public static final int UPDATE_UPGRADE_CHECK = 17;
    /**
     * ipc????????????
     **/
    public static final int UPDATE_IPC_DISCONNECT = 19;
    /**
     * ??????1???????????????????????????
     **/
    public static final int UPDATE_IPC_FIRST_DISCONNECT = 20;
    /**
     * ??????2???????????????????????????
     **/
    public static final int UPDATE_IPC_SECOND_DISCONNECT = 21;
    /**
     * ????????????
     **/
    private int mDownloadStatus = 0;

    /**
     * ????????????
     */
    private AlertDialog mSendDialog = null;
    /**
     * ??????????????????
     **/
    private AlertDialog mSendOk = null;
    /**
     * ???????????????
     */
    private AlertDialog mUpdateDialog = null;
    /**
     * ????????????
     **/
    private AlertDialog mUpdateDialogSuccess = null;
    /**
     * ????????????
     **/
    private AlertDialog mUpdateDialogFail = null;
    /**
     * ???????????????
     **/
    private AlertDialog mPrepareDialog = null;
    /**
     * ??????1?????????????????????
     **/
    private AlertDialog mFirstDialog = null;
    /**
     * ??????2?????????????????????
     **/
    private AlertDialog mSecondDialog = null;

    private String mIpcVersion = "";
    private String mIpcSize = "";
    private String mIpcContent = "";
    private String mIpcUrl = "";
    /**
     * true??????????????????activity
     **/
    private boolean mIsExit = false;
    /**
     * ???????????????ipc????????????????????????????????????ipc??????????????????
     **/
    private boolean mIsDisConnect = false;
    private boolean mIsSendFileOk = false;

    private RelativeLayout mVoiceLayout, mLaterLayout;
    public static final String TAG = "carupgrade";

    /**
     * T1?????????????????????
     **/
    private AlertDialog mCheckSDCard = null;
    private boolean mIsUpgrading;

    private UpgradeManager mT1SPUpgradeManager;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.upgrade_layout);

        mApp = (GolukApplication) getApplication();
        initView();
        mIsExit = false;

        mIsUpgrading = false;
        Intent it = getIntent();
        boolean downloadOnCreate = it.getBooleanExtra(DOWNLOAD_ON_CREATE, false);
        mSign = it.getIntExtra(UPDATE_SIGN, 0);
        mIpcInfo = (IPCInfo) it.getSerializableExtra(UPDATE_DATA);

        if (null != mIpcInfo) {
            SharedPrefUtil.saveIPCDownVersion(mIpcInfo.version);
            SharedPrefUtil.saveIpcFileSize(mIpcInfo.filesize);
            SharedPrefUtil.saveIpcContent(mIpcInfo.appcontent);
            SharedPrefUtil.saveIPCURL(mIpcInfo.url);
            SharedPrefUtil.saveIPCPath(mIpcInfo.path);
        }

        mIpcVersion = SharedPrefUtil.getIPCDownVersion();
        mIpcSize = SharedPrefUtil.getIPCFileSize();
        mIpcContent = SharedPrefUtil.getIPCContent();
        mIpcUrl = SharedPrefUtil.getIPCURL();

        mTextIpcVersion.setText(mIpcVersion);
        if (!TextUtils.isEmpty(mIpcSize)) {
            String size = DataCleanManage.getFormatSize(Double.parseDouble(mIpcSize));
            mTextIpcSize.setText(size);
        }
        mTextUpdateContent.setText(mIpcContent);

        Intent itClick = getIntent();
        int progressSetup = itClick.getIntExtra(UPDATE_PROGRESS, 0);

        GolukDebugUtils.i("", "----UpdateActivity----progressSetup-----" + progressSetup);
        if (mSign == 0) {
            if (mApp.mIpcUpdateManage.isDownloading()) {
                mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
                if (!UserUtils.isNetDeviceAvailable(this)) {
                    GolukUtils.showToast(mApp.getContext(), this.getResources().getString(R.string.str_ipc_update_download_file_fail));
                    mTextDowload.setText(this.getResources().getString(R.string.str_ipc_update_undownload));
                    mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_download_file));
                    mDownloadStatus = IpcUpdateManage.DOWNLOAD_STATUS_FAIL;
                    mBtnDownload.setEnabled(true);

                    XLog.tag(LogConst.TAG_UPGRADE).i(getString(R.string.str_ipc_update_download_file_fail));
                } else {
                    mTextDowload.setText(this.getResources().getString(R.string.str_ipc_update_downloading));
                    mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_downloading_ellipsis) + progressSetup + this.getResources().getString(R.string.str_ipc_update_percent_unit));
                    mDownloadStatus = IpcUpdateManage.DOWNLOAD_STATUS;
                    mBtnDownload.setEnabled(false);

                    XLog.tag(LogConst.TAG_UPGRADE).i("Downloading " + progressSetup + "%");
                }
            } else {
                boolean b = false;
                if (downloadOnCreate) {
                    b = mApp.mIpcUpdateManage.download(mIpcUrl, mIpcVersion);
                    if (b) {
                        mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
                        mTextDowload.setText(this.getResources().getString(R.string.str_ipc_update_downloading));
                        mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_downloading_zero));
                        mDownloadStatus = IpcUpdateManage.DOWNLOAD_STATUS;
                        mBtnDownload.setEnabled(false);
                    }
                }
                if (!b || !downloadOnCreate) {
                    mTextDowload.setText(this.getResources().getString(R.string.str_ipc_update_undownload));
                    mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_download_file));
                    mDownloadStatus = IpcUpdateManage.DOWNLOAD_STATUS_FAIL;
                    mBtnDownload.setEnabled(true);

                    XLog.tag(LogConst.TAG_UPGRADE).i("Firmware not download");
                }
            }
        } else if (mSign == 1) {
            mTextDowload.setText(this.getResources().getString(R.string.ipc_download_text));
            mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_install));
            mBtnDownload.setEnabled(true);
            mNoBreakImage.setVisibility(View.GONE);
            mNoBreakText.setVisibility(View.GONE);
            if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                    || IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
                mtfCardImage.setVisibility(View.VISIBLE);
                mtfCardText.setVisibility(View.VISIBLE);
            }

            XLog.tag(LogConst.TAG_UPGRADE).i("Firmware is downloaded");
        }
        EventBus.getDefault().register(this);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_TRANSFER_FILE:
                        UserUtils.dismissUpdateDialog(mPrepareDialog);
                        mIsDisConnect = true;
                        mPrepareDialog = null;
                        if (mIsExit) {
                            return;
                        }
                        String text = getString(R.string.str_ipc_update_first_period);
                        if (!mApp.getIPCControlManager().isT2S()) {
                            text = text + ": " + mPercent + getString(R.string.str_ipc_update_percent_unit);
                        }
                        if (mSendDialog == null) {
                            mSendDialog = UserUtils.showDialogUpdate(UpdateActivity.this, text);
                        } else {
                            mSendDialog.setMessage(text);
                        }

                        XLog.tag(LogConst.TAG_UPGRADE).i("Upgrading stage 1: " + mPercent + "%");
                        break;
                    case UPDATE_TRANSFER_OK:
                        mIsUpgrading = false;
                        UserUtils.dismissUpdateDialog(mSendDialog);
                        mSendDialog = null;
                        if (mIsExit) {
                            return;
                        }
                        mSendOk = UserUtils.showDialogUpdate(UpdateActivity.this, mApp.getContext().getResources()
                                .getString(R.string.str_ipc_update_transfer_file_success));
                        mIsSendFileOk = true;

                        XLog.tag(LogConst.TAG_UPGRADE).i("Upgrading file transfered success");
                        break;
                    case UPDATE_UPGRADEING:
                        UserUtils.dismissUpdateDialog(mSendOk);
                        mSendOk = null;
                        if (mIsExit) {
                            return;
                        }
                        mIsDisConnect = true;
                        String alertMsg = mApp.getContext().getResources().getString(R.string.str_ipc_update_second_period);
                        // ???????????????????????????
                        if (!(mApp.getIPCControlManager().isT2S()))
                            alertMsg = alertMsg + ": " + mPercent + mApp.getContext().getResources().getString(R.string.str_ipc_update_percent_unit);
                        if (mUpdateDialog == null) {
                            mUpdateDialog = UserUtils.showDialogUpdate(UpdateActivity.this, alertMsg);
                        } else {
                            mUpdateDialog.setMessage(alertMsg);
                        }

                        XLog.tag(LogConst.TAG_UPGRADE).i("Upgrading stage 2: " + mPercent + "%");
                        break;
                    case UPDATE_UPGRADE_OK:
                        mIsUpgrading = false;
                        mApp.mIpcUpdateManage.stopIpcUpgrade();
                        UserUtils.dismissUpdateDialog(mUpdateDialog);
                        mUpdateDialog = null;
                        SharedPrefUtil.saveIPCDownVersion("");
                        SharedPrefUtil.saveNewFirmware(mIpcVersion, false);
                        if (mIsExit) {
                            return;
                        }
                        if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                                || IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
                            UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, mApp.getResources()
                                    .getString(R.string.str_ipc_update_success_t1));
                        } else if (mApp.getIPCControlManager().isT2S()) {
                            UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, mApp.getResources()
                                    .getString(R.string.str_ipc_update_success_t1s));
                        } else {
                            UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, mApp.getResources()
                                    .getString(R.string.str_ipc_update_success));
                        }
                        isNewVersion();
                        // ??????????????????Event
                        EventBus.getDefault().post(new EventIpcUpdateSuccess());

                        // ??????ipc?????????
                        SharedPrefUtil.saveIPCVersion(mIpcVersion);

                        // ??????Event
                        EventBus.getDefault().post(new EventIPCCheckUpgradeResult(EventIPCCheckUpgradeResult.EVENT_RESULT_TYPE_NEW));

                        XLog.tag(LogConst.TAG_UPGRADE).i("Ipc upgrade success!");
                        break;
                    case UPDATE_UPGRADE_FAIL:
                        mIsUpgrading = false;
                        mApp.mIpcUpdateManage.stopIpcUpgrade();
                        timerCancel();
                        UserUtils.dismissUpdateDialog(mPrepareDialog);
                        UserUtils.dismissUpdateDialog(mSendDialog);
                        UserUtils.dismissUpdateDialog(mUpdateDialog);
                        mPrepareDialog = null;
                        mSendDialog = null;
                        mUpdateDialog = null;
                        mFirstDialog = null;
                        mSecondDialog = null;
                        if (mIsExit) {
                            return;
                        }
                        if (mVoiceLayout.getVisibility() == View.VISIBLE) {
                            mVoiceLayout.setVisibility(View.GONE);
                        }
                        mIsDisConnect = true;
                        UserUtils.showUpdateSuccess(mUpdateDialogFail, UpdateActivity.this, mApp.getContext()
                                .getResources().getString(R.string.str_ipc_update_fail));
                        mNoBreakImage.setVisibility(View.GONE);
                        mNoBreakText.setVisibility(View.GONE);

                        XLog.tag(LogConst.TAG_UPGRADE).i("Ipc upgrade failed!");
                        break;
                    case UPDATE_UPGRADE_CHECK:
                        mIsUpgrading = false;
                        if (mIsExit) {
                            return;
                        }
                        UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, mApp.getContext()
                                .getResources().getString(R.string.str_ipc_update_check_fail));
                        mNoBreakImage.setVisibility(View.GONE);
                        mNoBreakText.setVisibility(View.GONE);

                        XLog.tag(LogConst.TAG_UPGRADE).i("Upgrading: fireware check Not pass!");
                        break;
                    case UPDATE_IPC_DISCONNECT:
                        mIsUpgrading = false;
                        timerCancel();
                        UserUtils.dismissUpdateDialog(mPrepareDialog);
                        UserUtils.dismissUpdateDialog(mSendDialog);
                        mPrepareDialog = null;
                        mUpdateDialog = null;
                        if (mIsExit) {
                            return;
                        }
                        mIsDisConnect = true;
                        UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, mApp.getResources()
                                .getString(R.string.str_ipc_update_disconnect));
                        mNoBreakImage.setVisibility(View.GONE);
                        mNoBreakText.setVisibility(View.GONE);

                        XLog.tag(LogConst.TAG_UPGRADE).i("Ipc disconnected!");
                        break;
                    case UPDATE_IPC_FIRST_DISCONNECT:
                        mIsUpgrading = false;
                        timerCancel();
                        UserUtils.dismissUpdateDialog(mPrepareDialog);
                        UserUtils.dismissUpdateDialog(mSendDialog);
                        UserUtils.dismissUpdateDialog(mSendOk);
                        mPrepareDialog = null;
                        mSendDialog = null;
                        mSendOk = null;
                        mApp.mIpcUpdateManage.stopIpcUpgrade();
                        mIsDisConnect = true;
                        showUpdateFirstDisconnect(mApp.getContext().getResources()
                                .getString(R.string.str_ipc_update_first_period_disconnect));
                        mNoBreakImage.setVisibility(View.GONE);
                        mNoBreakText.setVisibility(View.GONE);
                        if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                                || IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
                            mtfCardImage.setVisibility(View.VISIBLE);
                            mtfCardText.setVisibility(View.VISIBLE);
                        }

                        XLog.tag(LogConst.TAG_UPGRADE).i("Ipc disconnected in stage 1!");
                        break;
                    case UPDATE_IPC_SECOND_DISCONNECT:
                        mIsUpgrading = false;
                        timerCancel();
                        UserUtils.dismissUpdateDialog(mUpdateDialog);
                        mUpdateDialog = null;
                        mApp.mIpcUpdateManage.stopIpcUpgrade();
                        mIsDisConnect = true;
                        showUpdateSecondDisconnect(mApp.getContext().getResources()
                                .getString(R.string.str_ipc_update_second_period_disconnect));

                        XLog.tag(LogConst.TAG_UPGRADE).i("Ipc disconnected in stage 2!");
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        XLog.tag(LogConst.TAG_UPGRADE).i("Enter IPC upgrade page.");
    }

    public void onEventMainThread(EventIPCUpdate event) {
        if (null == event) {
            return;
        }

        switch (event.getOpCode()) {
            case EventConfig.UPDATE_FILE_NOT_EXISTS:
                if (mIsExit) {
                    return;
                }
                mIsDisConnect = true;
                UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this,
                        this.getResources().getString(R.string.str_update_file_not_exist));
                mNoBreakImage.setVisibility(View.GONE);
                mNoBreakText.setVisibility(View.GONE);
                break;
            case EventConfig.UPDATE_PREPARE_FILE:
                if (mIsExit) {
                    return;
                }
                mIsDisConnect = false;
                mPrepareDialog = UserUtils.showDialogUpdate(UpdateActivity.this,
                        this.getResources().getString(R.string.str_update_prepare_file));
                break;
            case EventConfig.UPDATE_IPC_UNUNITED:
                if (mIsExit) {
                    return;
                }
                mIsDisConnect = true;
                UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this,
                        this.getResources().getString(R.string.str_update_ipc_ununited));
                mNoBreakImage.setVisibility(View.GONE);
                mNoBreakText.setVisibility(View.GONE);
                break;
            case EventConfig.UPDATE_TRANSFER_FILE_T1:
                UserUtils.dismissUpdateDialog(mPrepareDialog);
                mIsDisConnect = true;
                mPrepareDialog = null;
                if (mIsExit) {
                    return;
                }
                mIsDisConnect = true;
                if (mSendDialog == null) {
                    mSendDialog = UserUtils.showDialogUpdate(UpdateActivity.this,
                            this.getResources().getString(R.string.str_ipc_update_transfer_file_t1) + mPercent
                                    + this.getResources().getString(R.string.str_ipc_update_percent_unit));
                } else {
                    mSendDialog.setMessage(this.getResources().getString(R.string.str_ipc_update_transfer_file_t1)
                            + mPercent + this.getResources().getString(R.string.str_ipc_update_percent_unit));
                }
                break;
            case EventConfig.UPDATE_TRANSFER_FILE_OK_T1:
                UserUtils.dismissUpdateDialog(mSendDialog);
                mSendDialog = null;
                if (mIsExit) {
                    return;
                }
                mIsSendFileOk = true;
                mBtnDownload.setEnabled(false);
                mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_install_ok));
                if (null != mFirstDialog && mFirstDialog.isShowing()) {
                    mFirstDialog.dismiss();
                    mFirstDialog = null;
                }
                mVoiceLayout.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(EventWifiConnect event) {
        if (null == event) {
            return;
        }
        switch (event.getOpCode()) {
            case EventConfig.WIFI_STATE_SUCCESS:
                if (mLaterLayout.getVisibility() == View.VISIBLE || mVoiceLayout.getVisibility() == View.VISIBLE) {
                    mLaterLayout.setVisibility(View.GONE);
                    mVoiceLayout.setVisibility(View.GONE);
                    if (null != mUpdateHandler) {
                        mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_OK);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mApp.setContext(this, "Update");

        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener(TAG, this);
        }

        ZhugeUtils.eventIpcUpdate(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // ????????????????????????????????????,??????????????????
        boolean hasIpcFile = mApp.mIpcUpdateManage.isIpcUpdateBinFileExist();
        if (!hasIpcFile) {
            mSign = 0;
            mTextDowload.setText(this.getResources().getString(R.string.str_ipc_update_undownload));
            mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_download_file));
            mDownloadStatus = IpcUpdateManage.DOWNLOAD_STATUS_FAIL;
            mBtnDownload.setEnabled(true);
        }
    }

    // ?????????view
    public void initView() {
        ImageButton mBtnBack = (ImageButton) findViewById(R.id.back_btn);
        mBtnDownload = (TextView) findViewById(R.id.update_btn);
        mTextIpcVersion = (TextView) findViewById(R.id.upgrade_ipc_name);
        mTextIpcSize = (TextView) findViewById(R.id.upgrade_ipc_size_text);
        mTextUpdateContent = (TextView) findViewById(R.id.update_info_content);
        mTextDowload = (TextView) findViewById(R.id.upgrade_ipc_size_download);
        mScrollView = (ScrollView) findViewById(R.id.sv_upgrade_body);
        mUpdateNewImage = (ImageView) findViewById(R.id.iv_upgrade_new);
        mUpdateNewText = (TextView) findViewById(R.id.tv_upgrade_new_text);
        mNoBreakImage = (ImageView) findViewById(R.id.iv_upgrade_nobreak_image);
        mNoBreakText = (TextView) findViewById(R.id.tv_upgrade_nobreak_text);
        mVoiceLayout = (RelativeLayout) findViewById(R.id.rl_update_voice);
        mtfCardImage = (ImageView) findViewById(R.id.iv_upgrade_tfcard_image);
        mtfCardText = (TextView) findViewById(R.id.tv_upgrade_tfcard_text);
        mTvUpgradeHint = (TextView) findViewById(R.id.upgrade_hint);
        mLaterLayout = (RelativeLayout) findViewById(R.id.rl_update_later);

        // ??????
        mBtnBack.setOnClickListener(this);
        mBtnDownload.setOnClickListener(this);
        mVoiceLayout.setOnClickListener(this);

        // T1SP??????????????????????????????
        if (mApp.getIPCControlManager().isT2S()) {
            String hint = getString(R.string.ipc_hint_text);
            String hintT1SP = getString(R.string.t1sp_ipc_hint_text);
            SpannableStringBuilder style = new SpannableStringBuilder(hintT1SP);
            style.setSpan(new ForegroundColorSpan(Color.RED), hint.length(), hintT1SP.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTvUpgradeHint.setText(style);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_btn) {
            exit();
        } else if (id == R.id.update_btn) {
            // ??????????????????
            if (!mApp.mIpcUpdateManage.isCanClick()) {
                return;
            }
            // ?????? / ??????
            if (mSign == 0) {
                if (IpcUpdateManage.DOWNLOAD_STATUS_FAIL == mDownloadStatus) {
                    mApp.mIpcUpdateManage.mDownLoadIpcInfo = mIpcInfo;
                    mTextDowload.setText(this.getResources().getString(R.string.str_ipc_update_downloading));
                    boolean b = mApp.mIpcUpdateManage.download(mIpcUrl, mIpcVersion);
                    GolukDebugUtils.i("", "----path------" + IpcUpdateManage.BIN_PATH_PRE + "/" + mIpcVersion
                            + ".bin");
                    if (b) {
                        mApp.mIpcUpdateManage.showLoadingDialog();
                    } else {
                        mApp.mIpcUpdateManage.dismissLoadingDialog();
                    }
                }

                XLog.tag(LogConst.TAG_UPGRADE).i("Click download file button");
            } else if (mSign == 1) {
                // TODO ??????????????????????????? ???????????????????????????
                if (!mApp.getIpcIsLogin()) {
                    if (mIsExit) {
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setClass(this, WiFiLinkListActivity.class);
                    intent.putExtra(WiFiLinkListActivity.ACTION_FROM_MANAGER, true);
                    startActivity(intent);
                    /*
                    UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this, this.getResources()
                            .getString(R.string.update_no_connect_ipc_hint));
                    mIsDisConnect = true;*/
                } else {
                    String version = SharedPrefUtil.getIPCVersion();
                    GolukDebugUtils.i("lily", "-------version-----" + version + "------ipc_version-----" + mIpcVersion);
                    if (version.equals(mIpcVersion)) {
                        // GolukUtils.showToast(mApp.getContext(), "????????????????????????" +
                        // version + "???????????????????????????");
                        isNewVersion();
                    } else {
                        String filePath = mApp.mIpcUpdateManage.isHasIPCFile(mIpcVersion);
                        if (mApp.getIPCControlManager().isT2S()) {
                            // T1SP??????
                            File binFile = new File(FileUtil.convertFs1ToRealPath(filePath));
                            mT1SPUpgradeManager = new UpgradeManager(binFile);
                            mT1SPUpgradeManager.setListener(this);
                            mT1SPUpgradeManager.start();
                        } else {
                            // ????????????
                            boolean b = mApp.mIpcUpdateManage.ipcInstall(filePath);
                            if (b) {
                                mIsUpgrading = true;
                                mtfCardImage.setVisibility(View.GONE);
                                mtfCardText.setVisibility(View.GONE);
                                mNoBreakImage.setVisibility(View.VISIBLE);
                                mNoBreakText.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                XLog.tag(LogConst.TAG_UPGRADE).i("Click upgrade file button");
            }
        } else if (id == R.id.rl_update_voice) {
            mVoiceLayout.setVisibility(View.GONE);
            mLaterLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ??????ipc???????????? void* pvUser, int type ,int state , unsigned long param1,
     * unsigned long param2 ?????????state==2??????param1????????????????????????0~100???
     */
    public void downloadCallback(int state, Object param1, Object param2) {
        GolukDebugUtils.i("lily", "---UpdateActivity---------downloadCallback-----------state???" + state + "----param1???"
                + param1);
        mApp.mIpcUpdateManage.dismissLoadingDialog();
        mDownloadStatus = state;
        if (state == IpcUpdateManage.DOWNLOAD_STATUS) {
            int progress = (Integer) param1;
            GolukDebugUtils.i("lily", "======????????????progress=====" + progress);
            mBtnDownload.setEnabled(false);
            mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_downloading_omit) + progress + this.getResources().getString(R.string.str_ipc_update_percent_unit));

            XLog.tag(LogConst.TAG_UPGRADE).i("File downloading: " + progress + "%");
        } else if (state == IpcUpdateManage.DOWNLOAD_STATUS_SUCCESS) {
            mTextDowload.setText(this.getResources().getString(R.string.ipc_download_text));
            mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_install));
            mBtnDownload.setEnabled(true);
            mNoBreakImage.setVisibility(View.GONE);
            mNoBreakText.setVisibility(View.GONE);
            if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                    || IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
                mtfCardImage.setVisibility(View.VISIBLE);
                mtfCardText.setVisibility(View.VISIBLE);
            }
            mSign = 1;
            // ????????????????????????
            // mApp.mIpcUpdateManage.downIpcSucess();

            XLog.tag(LogConst.TAG_UPGRADE).i("File download success!");
        } else if (state == IpcUpdateManage.DOWNLOAD_STATUS_FAIL) {
            GolukUtils.showToast(mApp.getContext(),
                    this.getResources().getString(R.string.str_ipc_update_download_file_fail));
            mTextDowload.setText(this.getResources().getString(R.string.str_ipc_update_undownload));
            mBtnDownload.setText(this.getResources().getString(R.string.str_ipc_update_download_file));
            mBtnDownload.setEnabled(true);
            mSign = 0;

            XLog.tag(LogConst.TAG_UPGRADE).i("File download failed!");
        }
    }

    /**
     * ipc??????????????????
     */
    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        GolukDebugUtils.e("lily", "lily====IPC_VDCP_Msg_IPCUpgrade====msg=" + msg + "===param1=" + param1 + "==param2="
                + param2 + "--------event-----" + event);
        try {
            if (mIsExit) {
                return;
            }
            if (mApp.mIpcUpdateManage.isDownloading()) {
                return;
            }
            if (event == ENetTransEvent_IPC_UpGrade_Resp) {
                if (IPC_VDCP_Msg_IPCUpgrade == msg) {
                    if (param1 == RESULE_SUCESS) {
                        String str = (String) param2;
                        if (TextUtils.isEmpty(str)) {
                            return;
                        }
                        JSONObject json = new JSONObject(str);
                        mStage = json.getString("stage");
                        mPercent = json.getString("percent");
                        GolukDebugUtils.i("lily", "---------stage-----" + mStage + "-------percent----" + mPercent);
                        if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                                || IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
                            if (mStage.equals("1")) {
                                // ????????????????????????????????????
                                EventBus.getDefault().post(new EventIPCUpdate(EventConfig.UPDATE_TRANSFER_FILE_T1));
                                if (mPercent.equals("100")) {
                                    timerCancel();
                                    // ??????????????????
                                    EventBus.getDefault().post(
                                            new EventIPCUpdate(EventConfig.UPDATE_TRANSFER_FILE_OK_T1));
                                } else {
                                    timerTaskOne();
                                }
                            }
                        } else {
                            if (mStage.equals("1")) {
                                // ????????????????????????????????????
                                mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_FILE);
                                if (mPercent.equals("100")) {
                                    timerCancel();
                                    // ??????????????????
                                    mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_OK);
                                    timerTaskOne();
                                } else {
                                    timerTaskOne();
                                }
                            }
                            if (mStage.equals("2")) {
                                // ?????????????????????????????????????????????????????????????????????
                                mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADEING);
                                if (!mPercent.equals("95") && !mPercent.equals("100")) {
                                    timerTaskTwo();
                                } else {
                                    timerCancel();
                                    mApp.updateSuccess = true;
                                    // ????????????
                                    mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_OK);
                                    mApp.mIpcUpdateManage.mParam1 = -1;
                                }
                            }
                        }
                        if (mStage.equals("3")) {
                            if (mPercent.equals("-1")) {
                                mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_CHECK);
                            }
                        }
                    } else {
                        // TODO T?????????????????????
                        if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                                || IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
                            JSONObject jsonError = new JSONObject((String) param2);
                            int errorcode = jsonError.getInt("errcode");
                            if (1 == errorcode) {
                                UserUtils.dismissUpdateDialog(mPrepareDialog);
                                mPrepareDialog = null;
                                if (null == mCheckSDCard) {
                                    mCheckSDCard = new AlertDialog.Builder(this)
                                            .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                                            .setMessage(this.getString(R.string.str_upgrade_check_sdcard_t1))
                                            .setPositiveButton(this.getResources().getString(R.string.user_repwd_ok),
                                                    new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                            mCheckSDCard.dismiss();
                                                            mCheckSDCard = null;
                                                        }
                                                    }).show();
                                }

                                XLog.tag(LogConst.TAG_UPGRADE).i("Upgrade failed: check SD card!");
                                return;
                            }
                        }
                        if (!(null != mFirstDialog && mFirstDialog.isShowing())
                                || !(null != mSecondDialog && mSecondDialog.isShowing())) {
                            if (mStage.equals("1")) {
                                if (null == mUpdateDialogFail || !mUpdateDialogFail.isShowing()) {
                                    mUpdateHandler.sendEmptyMessage(UPDATE_IPC_FIRST_DISCONNECT);
                                }
                            } else {
                                mApp.updateSuccess = false;
                                // ????????????
                                if (null == mUpdateDialogFail || !mUpdateDialogFail.isShowing()) {
                                    mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_FAIL);
                                }
                            }
                        }
                    }
                }
            } else if (event == ENetTransEvent_IPC_VDCP_ConnectState) {
                if (mIsSendFileOk) {
                    return;
                }
                if (mIsDisConnect) {
                    return;
                }
                if (ConnectionStateMsg_DisConnected == msg) {
                    UserUtils.dismissUpdateDialog(mPrepareDialog);
                    UserUtils.dismissUpdateDialog(mUpdateDialogSuccess);
                    UserUtils.dismissUpdateDialog(mSendDialog);
                    UserUtils.dismissUpdateDialog(mSendOk);
                    UserUtils.dismissUpdateDialog(mUpdateDialog);
                    UserUtils.dismissUpdateDialog(mUpdateDialogFail);
                    UserUtils.dismissUpdateDialog(mFirstDialog);
                    UserUtils.dismissUpdateDialog(mSendDialog);
                    mPrepareDialog = null;
                    mSendDialog = null;
                    mSendOk = null;
                    mUpdateDialog = null;
                    mUpdateDialogFail = null;
                    mFirstDialog = null;
                    mSendDialog = null;
                    if (null != mUpdateDialogSuccess && mUpdateDialogSuccess.isShowing()) {
                        return;
                    }
                    UserUtils.showUpdateSuccess(mUpdateDialogSuccess, UpdateActivity.this,
                            this.getResources().getString(R.string.update_no_connect_ipc_hint));
                    mIsDisConnect = true;
                    mNoBreakImage.setVisibility(View.GONE);
                    mNoBreakText.setVisibility(View.GONE);
                    if (IPCControlManager.T1_SIGN.equals(mApp.mIPCControlManager.mProduceName)
                            || IPCControlManager.T2_SIGN.equals(mApp.mIPCControlManager.mProduceName)) {
                        mtfCardImage.setVisibility(View.VISIBLE);
                        mtfCardText.setVisibility(View.VISIBLE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????? ????????????3?????? ??????????????????????????? 1000x60x3=180000
     */
    private void timerTaskOne() {
        timerCancel();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // ipc??????
                if (mStage.equals("1")) {
                    if (null == mUpdateDialogFail || !mUpdateDialogFail.isShowing()) {
                        mUpdateHandler.sendEmptyMessage(UPDATE_IPC_FIRST_DISCONNECT);

                        XLog.tag(LogConst.TAG_UPGRADE).i("Upgrade 1 stage wait timeout 3min !");
                    }
                }
            }
        }, 180000);
    }

    /**
     * ??????????????? ????????????????????? ??????????????????????????? 1000x60x3=180000
     */
    private void timerTaskTwo() {
        timerCancel();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // ipc??????
                if (mStage.equals("2") && !mPercent.equals("100")) {
                    if (null == mUpdateDialogFail || !mUpdateDialogFail.isShowing()) {
                        mUpdateHandler.sendEmptyMessage(UPDATE_IPC_SECOND_DISCONNECT);

                        XLog.tag(LogConst.TAG_UPGRADE).i("Upgrade 2 stage wait timeout 3min !");
                    }
                }
            }
        }, 180000);
    }

    public void timerCancel() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerCancel();
        if (mApp.mIpcUpdateManage != null) {
            mApp.mIpcUpdateManage.dismissLoadingDialog();
        }

        if (null != mPrepareDialog) {
            UserUtils.dismissUpdateDialog(mPrepareDialog);
            mPrepareDialog = null;
        }

        if (null != mSendDialog) {
            UserUtils.dismissUpdateDialog(mSendDialog);
            mSendDialog = null;
        }

        if (null != mUpdateDialog) {
            UserUtils.dismissUpdateDialog(mUpdateDialog);
            mUpdateDialog = null;
        }

        if (null != mCheckSDCard) {
            mCheckSDCard.dismiss();
            mCheckSDCard = null;
        }

        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener(TAG);
        }
        EventBus.getDefault().unregister(this);

        XLog.tag(LogConst.TAG_UPGRADE).i("Leave updage page.");

        if (mT1SPUpgradeManager != null)
            mT1SPUpgradeManager.stop();
    }

    /**
     * ??????1??????
     */
    public void showUpdateFirstDisconnect(String message) {
        if (mIsExit) {
            return;
        }
        if (null == mFirstDialog) {
            mFirstDialog = new AlertDialog.Builder(UpdateActivity.this)
                    .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                    .setMessage(message)
                    .setPositiveButton(this.getResources().getString(R.string.str_button_ok),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    exit();
                                }
                            }).show();
        }
    }

    /**
     * ??????2??????
     */
    public void showUpdateSecondDisconnect(String message) {
        if (mIsExit) {
            return;
        }
        if (null == mSecondDialog) {
            mSecondDialog = new AlertDialog.Builder(UpdateActivity.this)
                    .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                    .setMessage(message)
                    .setPositiveButton(this.getResources().getString(R.string.str_button_ok),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    exit();
                                }
                            }).show();
        }
    }

    /**
     * ??????ipc???????????????UI??????
     */
    public void isNewVersion() {
        mIsDisConnect = true;
        mBtnDownload.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);
        mUpdateNewImage.setVisibility(View.VISIBLE);
        mUpdateNewText.setVisibility(View.VISIBLE);
        if (null != mIpcInfo) {
            SharedPrefUtil.saveIPCVersion(mIpcInfo.version);
        }
    }

    public void exit() {
        if (mIsUpgrading) {
            return;
        }

        mIsExit = true;
        mIsSendFileOk = false;
        mIsDisConnect = false;
        if (null != mUpdateHandler) {
            mUpdateHandler.removeCallbacksAndMessages(null);
        }
        finish();
        timerCancel();
        if (null != mUpdateDialogSuccess) {
            UserUtils.dismissUpdateDialog(mUpdateDialogSuccess);
            mUpdateDialogSuccess = null;
        }
        if (null != mPrepareDialog) {
            UserUtils.dismissUpdateDialog(mPrepareDialog);
            mPrepareDialog = null;
        }
        if (null != mSendDialog) {
            UserUtils.dismissUpdateDialog(mSendDialog);
            mSendDialog = null;
        }
        if (null != mSendOk) {
            UserUtils.dismissUpdateDialog(mSendOk);
            mSendOk = null;
        }
        if (null != mUpdateDialog) {
            UserUtils.dismissUpdateDialog(mUpdateDialog);
            mUpdateDialog = null;
        }
        if (null != mUpdateDialogFail) {
            UserUtils.dismissUpdateDialog(mUpdateDialogFail);
            mUpdateDialogFail = null;
        }
        if (null != mFirstDialog) {
            UserUtils.dismissUpdateDialog(mFirstDialog);
            mFirstDialog = null;
        }
        if (null != mSecondDialog) {
            UserUtils.dismissUpdateDialog(mSendDialog);
            mSendDialog = null;
        }
        if (null != mCheckSDCard) {
            mCheckSDCard.dismiss();
            mCheckSDCard = null;
        }

    }

    @Override
    public void onBackPressed() {
        if (mIsUpgrading) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onEnterUpgradeMode(boolean success) {
        if (!success)
            mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_FAIL);
    }

    @Override
    public void onUploadUpgradeFileStart() {
        mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_FILE);
    }

    @Override
    public void onUploadProgress(int progress) {
//        mPercent = String.valueOf(progress);
//        mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_FILE);
    }

    @Override
    public void onUploadUpgradeFileResult(boolean success) {
        if (success)
            mUpdateHandler.sendEmptyMessage(UPDATE_TRANSFER_OK);
        else
            mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_FAIL);
    }

    @Override
    public void onUpgradeStart(boolean success) {
        if (success)
            mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADEING);
        else
            mUpdateHandler.sendEmptyMessage(UPDATE_UPGRADE_FAIL);
    }

    @Override
    public void onUpgradeFinish(boolean success) {
        mUpdateHandler.sendEmptyMessage(success ? UPDATE_UPGRADE_OK : UPDATE_UPGRADE_FAIL);
    }


    public void onEventMainThread(EventWifiState event) {
        if (event != null && event.getOpCode() == EventConfig.WIFI_STATE && event.getMsg()) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiConnectManager wac = new WifiConnectManager(wifiManager, this);
            WifiRsBean wifiResult = wac.getConnResult();
            if (wifiResult != null && wifiResult.getIpc_ssid().startsWith("Goluk")) {
                String type = GolukUtils.getIpcTypeFromName(wifiResult.getIpc_ssid());
                mApp.mIPCControlManager.setIpcMode(type);
                boolean flag = mApp.mIPCControlManager.setIPCWifiState(true, "192.168.62.1");
            }
        }
    }

    public void wifiCallBack(int type, int state, int process, String message, Object arrays) {
    }

}
