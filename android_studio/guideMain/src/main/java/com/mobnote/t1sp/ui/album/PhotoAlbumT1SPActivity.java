package com.mobnote.t1sp.ui.album;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.t1sp.download.DownloaderT1spImpl;

import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackCmd;

public class PhotoAlbumT1SPActivity extends BaseActivity {
    public static final String CLOSE_WHEN_EXIT = "should_close_conn";

    private boolean mShouldClose;
    private boolean mShowLocal;
    private boolean mSelectMode;
    private boolean mFromCloud;
    private FragmentAlbumT1SP mFragmentAlubm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_album_activity);

        ApiUtil.startRecord(false, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                System.out.println("");
                ApiUtil.changeToPlaybackMode(new CallbackCmd() {
                    @Override
                    public void onSuccess(int i) {
                        System.out.println("");
                    }

                    @Override
                    public void onFail(int i, int i1) {
                    }
                });
            }

            @Override
            public void onFail(int i, int i1) {
                System.out.println("");
            }
        });

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

        // 开始加载数据
//        if (mFragmentAlubm != null)
//            mFragmentAlubm.loadData();
    }

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

        ApiUtil.changeToMovieMode(new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                ApiUtil.startRecord(true, new CallbackCmd() {
                    @Override
                    public void onSuccess(int i) {
                    }

                    @Override
                    public void onFail(int i, int i1) {
                    }
                });
            }

            @Override
            public void onFail(int i, int i1) {

            }
        });

        if (mShouldClose && mBaseApp.isIpcLoginSuccess) {
            mBaseApp.setIpcDisconnect();
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                wifiManager.disableNetwork(wifiInfo.getNetworkId());
            }
        }
    }

}
