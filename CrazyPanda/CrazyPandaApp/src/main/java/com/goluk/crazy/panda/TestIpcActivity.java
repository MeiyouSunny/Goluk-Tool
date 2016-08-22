package com.goluk.crazy.panda;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.crazy.panda.ipc.base.IPCManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestIpcActivity extends AppCompatActivity {
    private WifiManager mWifiManager;

    @BindView(R.id.textView2)
    TextView tvInfo;

    @BindView(R.id.slContent)
    ScrollView scrollView;

    private boolean clickButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_ipc);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        ButterKnife.bind(this);
        clickButton = false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!clickButton) {
            return;
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Toast.makeText(this, "WIfi Closed", Toast.LENGTH_SHORT).show();
            return;
        }
        IPCManager manager = IPCManager.getInstance();
        if (manager.bind(wifiInfo.getBSSID())) {
            Toast.makeText(this, "bind ipc success ", Toast.LENGTH_SHORT).show();
            tvInfo.setText("连接成功了");
            scrollView.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, "bind ipc field ", Toast.LENGTH_SHORT).show();
        tvInfo.setText("失败");
        scrollView.setVisibility(View.GONE);
        clickButton = false;
    }


    @OnClick(R.id.button)
    public void chooseSystemWifi() {
        clickButton = true;
        try {
            Intent chooseWifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(chooseWifi);
        } catch (Exception e) {
            Toast.makeText(this, "No Application to choose Wifi", Toast.LENGTH_LONG).show();
        }
    }


}
