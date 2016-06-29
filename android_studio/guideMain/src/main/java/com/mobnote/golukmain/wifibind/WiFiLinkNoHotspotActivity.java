package com.mobnote.golukmain.wifibind;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.mobnote.eventbus.EventFinishWifiActivity;
import com.mobnote.golukmain.R;

/**
 * 获取Wifi扫描列表，单向链接
 */
public class WiFiLinkNoHotspotActivity extends WiFiLinkListActivity implements View.OnClickListener {
    public static final String AUTO_START_CONNECT = "AutoStart";

    private boolean mAutoStart;

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
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btn_reconnect_hotspot) {
            mApp.setEnableSingleWifi(false);
            toNextView();
        } else if (v.getId() == R.id.btn_only_connect_goluk_wifi) {
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
        }
    }

    @Override
    protected void nextCan() {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

}
