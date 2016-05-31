package com.goluk.ipcdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.goluk.ipcsdk.utils.IPCConnectState;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private Button mConnWifiBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();

    }

    private  void initView(){
        mConnWifiBtn = (Button) findViewById(R.id.conn_wifi);
    }

    private void initListener(){
        mConnWifiBtn.setOnClickListener(this);
    }

    private void startWifiCommond(){
        Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.conn_wifi:
                startWifiCommond();
            default:
                    break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean wifiresult = IPCConnectState.getConnectState().getState();
        Toast.makeText(MainActivity.this,"wifi connect state :" + wifiresult,Toast.LENGTH_SHORT).show();
    }
}
