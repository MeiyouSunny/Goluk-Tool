package com.mobnote.golukmain.photoalbum;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class PhotoAlbumActivity extends BaseActivity {
    public static final String CLOSE_WHEN_EXIT = "should_close_conn";

    private boolean mShouldClose;
    private boolean mShowLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_album_activity);
        mShouldClose = getIntent().getBooleanExtra(CLOSE_WHEN_EXIT, false);
        mShowLocal = getIntent().getBooleanExtra(FragmentAlbum.PARENT_VIEW,false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putBoolean(FragmentAlbum.PARENT_VIEW, mShowLocal);
        FragmentAlbum fa = new FragmentAlbum();
        fa.setArguments(bundle);
        fragmentTransaction.add(R.id.photo_album_fragment, fa);
        fragmentTransaction.commitAllowingStateLoss();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mShouldClose && mBaseApp.isIpcLoginSuccess) {
            mBaseApp.setIpcDisconnect();
            WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                wifiManager.disableNetwork(wifiInfo.getNetworkId());
            }
        }
    }
}
