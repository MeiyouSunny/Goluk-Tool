package com.mobnote.golukmain.wifibind;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;

/**
 * 获取Wifi扫描列表，单向链接
 */
public class WiFiLinkNoHotspotActivity extends WiFiLinkListActivity implements View.OnClickListener {
    private Button btnReconnect;
    private Button btnOnlyWifi;

    @Override


    protected int getContentViewResourceId() {
        return R.layout.wifi_link_no_hotspot;
    }

    @Override
    protected void initView() {
        btnReconnect = (Button) findViewById(R.id.btn_reconnect_hotspot);
        btnOnlyWifi = (Button) findViewById(R.id.btn_only_connect_goluk_wifi);
        btnReconnect.setOnClickListener(this);
        btnOnlyWifi.setOnClickListener(this);
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
    protected void toNextView() {
        if (mApp.getEnableSingleWifi()) {
            setDefaultInfo();
            if (mReturnToMainAlbum) {
                Intent mainIntent = new Intent(WiFiLinkNoHotspotActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            } else {
                Intent mainIntent = new Intent(WiFiLinkNoHotspotActivity.this, CarRecorderActivity.class);
                startActivity(mainIntent);
                finish();
            }
        } else {
            super.toNextView();
        }
    }

    @Override
    protected void setStateSwitch() {
        //override parent setStateSwitch, in case the ui reference return null
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
