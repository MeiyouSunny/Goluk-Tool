package com.mobnote.golukmain.photoalbum;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

public class PhotoAlbumActivity extends BaseActivity {
    public static final String CLOSE_WHEN_EXIT = "should_close_conn";

    private boolean mShouldClose;
    private boolean mShowLocal;
    private boolean mSelectMode;
    private boolean mFromCloud;
    private FragmentAlbum fa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mShouldClose = getIntent().getBooleanExtra(CLOSE_WHEN_EXIT, false);
        mShowLocal = getIntent().getBooleanExtra(FragmentAlbum.PARENT_VIEW, false);
        mSelectMode = getIntent().getBooleanExtra(FragmentAlbum.SELECT_MODE, false);
        if(mSelectMode){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        mFromCloud = getIntent().getBooleanExtra("from", false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_album_activity);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putBoolean(FragmentAlbum.PARENT_VIEW, mShowLocal);
        bundle.putBoolean(FragmentAlbum.SELECT_MODE, mSelectMode);
        bundle.putBoolean("from", mFromCloud);
        fa = new FragmentAlbum();
        fa.setArguments(bundle);
        fragmentTransaction.add(R.id.photo_album_fragment, fa);
        fragmentTransaction.commitAllowingStateLoss();
    }


    @Override
    public void onBackPressed() {
        if (fa != null && mShouldClose) {
            fa.checkDowningExit();
        }else {
            super.onBackPressed();
        }
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
    }
}
