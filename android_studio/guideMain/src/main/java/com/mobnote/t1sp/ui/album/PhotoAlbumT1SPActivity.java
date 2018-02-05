package com.mobnote.t1sp.ui.album;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mobnote.eventbus.EventExitMode;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.download.DownloaderT1spImpl;
import com.mobnote.t1sp.service.HeartbeatTask;
import com.mobnote.t1sp.util.Const;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class PhotoAlbumT1SPActivity extends BaseActivity {
    public static final String CLOSE_WHEN_EXIT = "should_close_conn";

    private boolean mShouldClose;
    private boolean mShowLocal;
    private boolean mSelectMode;
    private boolean mFromCloud;
    private FragmentAlbumT1SP mFragmentAlubm;
    private CustomLoadingDialog mDialog;

    private HeartbeatTask mHeartbeatTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_album_activity);
        mShouldClose = getIntent().getBooleanExtra(CLOSE_WHEN_EXIT, false);
        mShowLocal = getIntent().getBooleanExtra(FragmentAlbum.PARENT_VIEW, false);
        mSelectMode = getIntent().getBooleanExtra(FragmentAlbum.SELECT_MODE, false);
        mFromCloud = getIntent().getBooleanExtra("from", false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putBoolean(FragmentAlbum.PARENT_VIEW, mShowLocal);
        bundle.putBoolean(FragmentAlbum.SELECT_MODE, mSelectMode);
        bundle.putBoolean("from", mFromCloud);
        mFragmentAlubm = new FragmentAlbumT1SP();
        mFragmentAlubm.setArguments(bundle);
        fragmentTransaction.add(R.id.photo_album_fragment, mFragmentAlubm);
        fragmentTransaction.commitAllowingStateLoss();

        mDialog = new CustomLoadingDialog(this, getString(R.string.enter_album_mode_hint));
        mDialog.show();

        mHandler.sendEmptyMessageDelayed(MSG_ENTER_PLAYBACK_MODE, 1000);
    }

    private void enterPlaybackMode() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterPlaybackModeParam(true), new CommonCallback() {
            @Override
            protected void onSuccess() {
                if (mDialog.isShowing())
                    mDialog.close();

                mHandler.sendEmptyMessageDelayed(MSG_LOAD_DATA_AND_HEARTBEAT, 1500);
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                GolukDebugUtils.e(Const.LOG_TAG, errorMessage);
            }
        });
    }

    private void loadInitDataAndSendHeartbeat() {
        // 开始加载数据
        if (mFragmentAlubm != null)
            mFragmentAlubm.loadData();
        // 发送心跳
        mHeartbeatTask = new HeartbeatTask(HeartbeatTask.MODE_TYPE_PLAYBACK);
        mHeartbeatTask.start();
    }

    private static final int MSG_ENTER_PLAYBACK_MODE = 1;
    private static final int MSG_LOAD_DATA_AND_HEARTBEAT = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ENTER_PLAYBACK_MODE) {
                enterPlaybackMode();
            } else if (msg.what == MSG_LOAD_DATA_AND_HEARTBEAT) {
                loadInitDataAndSendHeartbeat();
            }
        }
    };

    @Override
    public void onBackPressed() {
        boolean hasDownloadTask = DownloaderT1spImpl.getInstance().isDownloading();
        if (hasDownloadTask) {
            preExit();
        } else {
            super.onBackPressed();
        }
    }

    private void preExit() {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(getString(R.string.str_global_dialog_title));
        dialog.setMessage(getString(R.string.msg_of_exit_when_download));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.str_button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                DownloaderT1spImpl.getInstance().cancelAllDownloadTask(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        finish();
                    }
                }, 1500);
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 发送退出设置模式指令
        EventBus.getDefault().post(new EventExitMode(2));

        if (mShouldClose && mBaseApp.isIpcLoginSuccess) {
            mBaseApp.setIpcDisconnect();
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                wifiManager.disableNetwork(wifiInfo.getNetworkId());
            }
        }

        if (mHeartbeatTask != null)
            mHeartbeatTask.stop();
    }
}
