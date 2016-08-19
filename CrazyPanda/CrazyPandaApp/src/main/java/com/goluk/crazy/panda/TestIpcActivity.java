package com.goluk.crazy.panda;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestIpcActivity extends AppCompatActivity {
    private WifiManager mWifiManager;

    @BindView(R.id.textView2)
    TextView tvInfo;

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
//        int ipcMode = IPCUtils.adadpteIPCNameByWifiName(wifiInfo.getSSID());
//        if (ipcMode == IPCConstant.IPC_MODE_UNKNOWN) {
//            Toast.makeText(this, "Not a Goluk IPC ,please change another wifi.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        IPCManager manager = IPCManager.getInstance();
//        manager.setOnResultListener(this);
//        manager.connect();
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
