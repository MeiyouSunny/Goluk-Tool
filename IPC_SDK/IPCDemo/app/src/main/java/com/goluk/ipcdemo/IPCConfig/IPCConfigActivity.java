package com.goluk.ipcdemo.IPCConfig;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.goluk.ipcdemo.R;
import com.goluk.ipcsdk.ipcCommond.IPCConfigCommand;
import com.goluk.ipcsdk.listener.IPCConfigListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCConfigActivity extends Activity implements View.OnClickListener,IPCConfigListener{

    Button mSetAudioRecordCfgBt;
    Button mgetAudioRecordCfgBt;
    Button mInitSdkBt;
    IPCConfigCommand mIPCConfigCommand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipcconfig);

        initView();

        initData();

        setupView();

    }

    private void initData() {
        mIPCConfigCommand = new IPCConfigCommand(this,this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupView() {
        mSetAudioRecordCfgBt.setOnClickListener(this);
        mgetAudioRecordCfgBt.setOnClickListener(this);
        mInitSdkBt.setOnClickListener(this);
    }

    private void initView() {
        mSetAudioRecordCfgBt = (Button) findViewById(R.id.bt_setAudioRecordCfg);
        mgetAudioRecordCfgBt = (Button) findViewById(R.id.bt_getAudioRecordCfg);
        mInitSdkBt = (Button) findViewById(R.id.bt_InitSdk);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.bt_InitSdk:
                 GolukIPCSdk.getInstance().connectIPC();
                break;
            case R.id.bt_getAudioRecordCfg:
                GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                        IPCManagerFn.IPC_VDCPCmd_GetRecAudioCfg, "");
                break;
            case R.id.bt_setAudioRecordCfg:
                mIPCConfigCommand.enableAudioRecord(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void callback_enableAudeoRecord(boolean success) {

    }

    @Override
    public void callback_setTime(boolean success) {
        Log.i("setTime",success+"");
    }
}
