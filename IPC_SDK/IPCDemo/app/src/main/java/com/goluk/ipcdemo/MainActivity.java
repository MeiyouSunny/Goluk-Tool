package com.goluk.ipcdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.goluk.ipcsdk.command.IPCConnCommand;
import com.goluk.ipcsdk.listener.IPCConnListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;

public class MainActivity extends Activity implements View.OnClickListener, IPCConnListener {

    private Button mConnWifiBt;
    private Button mConnIPCBt;
    private Button mIPCConfigBt;
    private Button mIPCFileManageBt;
    private Button mRealtimePlayBt;
    IPCConnCommand mIPCConnCommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();

    }

    @Override
    protected void onDestroy() {
        GolukIPCSdk.getInstance().unregisterIPC(this);
        super.onDestroy();
    }

    private void initData() {
        mIPCConnCommand = new IPCConnCommand(this, this);
    }

    private void initView() {
        mConnWifiBt = (Button) findViewById(R.id.btConnWifi);
        mConnIPCBt = (Button) findViewById(R.id.btConnIPC);
        mIPCConfigBt = (Button) findViewById(R.id.btIPCConfig);
        mIPCFileManageBt = (Button) findViewById(R.id.btIPCFileManage);
        mRealtimePlayBt = (Button) findViewById(R.id.btRealtimePlay);

    }

    private void initListener() {
        mConnWifiBt.setOnClickListener(this);
        mConnIPCBt.setOnClickListener(this);
        mIPCConfigBt.setOnClickListener(this);
        mIPCFileManageBt.setOnClickListener(this);

        mRealtimePlayBt.setOnClickListener(this);
    }

    private void startWifiCommond() {
        Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        startActivity(intent);
    }

    private void startIPCConfigActivity() {
        Intent intent = new Intent(this, IPCConfigActivity.class);
        this.startActivity(intent);
    }

    private void startRealtimePlay() {
        Intent intent = new Intent(this, RealTimePlayActivity.class);
        this.startActivity(intent);
    }

    private void startConnectIPC() {

        mIPCConnCommand.connectIPC();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btConnWifi:
                startWifiCommond();
                break;
            case R.id.btConnIPC:
                startConnectIPC();
                break;
            case R.id.btRealtimePlay:
                startRealtimePlay();
                break;
            case R.id.btIPCConfig:
                startIPCConfigActivity();
                break;
            case R.id.btIPCFileManage:
                Intent intent = new Intent(this, VideoListActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void callback_ConnIPC(boolean isSuccess) {
        Toast.makeText(this, "IPC init success", Toast.LENGTH_SHORT).show();
    }

}
