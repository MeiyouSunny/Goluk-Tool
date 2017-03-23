package com.mobnote.golukmain.wifibind;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.mobnote.eventbus.EventFinishWifiActivity;
import com.mobnote.golukmain.R;
import com.mobnote.util.ZhugeUtils;

/**
 * 获取Wifi扫描列表，单向链接
 */
public class WiFiLinkNoHotspotActivity extends WiFiLinkListActivity implements View.OnClickListener {
    public static final String AUTO_START_CONNECT = "AutoStart";
    public static final String INTENT_ACTION_RETURN_MAIN_ALBUM = "returnToAlbum";

    private boolean mAutoStart;

    private boolean mReturnToMainAlbum;

    @Override
    protected int getContentViewResourceId() {
        return R.layout.wifi_link_no_hotspot;
    }

    @Override
    protected void initView() {
        Button btnReconnect = (Button) findViewById(R.id.btn_reconnect_hotspot);
        Button btnOnlyWifi = (Button) findViewById(R.id.btn_only_connect_goluk_wifi);
        ImageButton btnClose = (ImageButton) findViewById(R.id.back_btn);
        btnReconnect.setOnClickListener(this);
        btnOnlyWifi.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        mAutoStart = getIntent().getBooleanExtra(AUTO_START_CONNECT, true);
        mReturnToMainAlbum = getIntent().getBooleanExtra(INTENT_ACTION_RETURN_MAIN_ALBUM, false);
    }


    @Override
    public void onClick(View v) {
        mAutoStart = true;
        super.onClick(v);
        if (v.getId() == R.id.btn_reconnect_hotspot) {
            mApp.setEnableSingleWifi(false);
            toNextView();
        } else if (v.getId() == R.id.btn_only_connect_goluk_wifi) {
            //IPC-连接失败页面-点击仅Wi-fi连接按钮
            ZhugeUtils.eventConnectFailWifi(this, mReturnToMainAlbum);
            mApp.setEnableSingleWifi(true);
            doConnect();
        }
    }

    @Override
    protected void setStateSwitch() {
        //override parent setStateSwitch, in case the ui reference return null
    }

    @Override
    protected void autoConnWifi() {
        //当有连接历史之后，会自动开始处理连接逻辑，但是如果是从WifiLinkComplete 过来的，就不要自动开始，静静地停留在页面就好了
        if (mAutoStart) {
            super.autoConnWifi();
        } else {
            mAutoStart = true;
        }
    }

    @Override
    protected void nextCan() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        //IPC-连接失败
        ZhugeUtils.eventConnectFail(this, mReturnToMainAlbum);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

}
