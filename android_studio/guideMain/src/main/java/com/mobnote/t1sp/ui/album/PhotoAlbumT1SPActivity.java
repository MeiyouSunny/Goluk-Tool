package com.mobnote.t1sp.ui.album;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.service.HeartbeatTask;

import likly.dollar.$;

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

        enterPlaybackMode();
    }

    private void enterPlaybackMode() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterPlaybackModeParam(true), new CommonCallback() {
                    @Override
                    protected void onSuccess() {
                        if (mDialog.isShowing())
                            mDialog.close();
                        // 开始加载数据
                        mFragmentAlubm.loadData();
                        // 发送心跳
                        mHeartbeatTask = new HeartbeatTask(HeartbeatTask.MODE_TYPE_PLAYBACK);
                        mHeartbeatTask.start();
                    }

                    @Override
                    protected void onServerError(int errorCode, String errorMessage) {
                        Log.e("mode", errorMessage);
                    }
                });
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        if (mFragmentAlubm != null && mShouldClose) {
            mFragmentAlubm.checkDowningExit();
        } else {
            super.onBackPressed();
            //enterVideoMode();
        }
    }

    public void enterVideoMode() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterVideoModeParam(), new CommonCallback() {
            @Override
            public void onStart() {
                mDialog = new CustomLoadingDialog(PhotoAlbumT1SPActivity.this, null);
                mDialog.show();
            }

            @Override
            protected void onSuccess() {
                $.toast().text(R.string.recovery_to_record).show();
                finish();
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                $.toast().text(errorMessage).show();
            }

            @Override
            public void onFinish() {
                if (mDialog.isShowing())
                    mDialog.close();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
