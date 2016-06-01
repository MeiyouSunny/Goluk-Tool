package com.goluk.ipcdemo.IPCConfig;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.goluk.ipcdemo.R;
import com.goluk.ipcsdk.listener.IPCConfigListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;

import org.json.JSONObject;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCConfigActivity extends Activity implements View.OnClickListener,IPCConfigListener{
    Button mSetAudioRecordCfgBt;
    Button mgetAudioRecordCfgBt;
    Button mInitSdkBt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipcconfig);
        initView();
        setupView();

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
        String params;
        switch (v.getId()){
            case R.id.bt_InitSdk:
                boolean isSuccess = GolukIPCSdk.getInstance().initSDK();
                Log.i("return",isSuccess + "");
                break;
            case R.id.bt_getAudioRecordCfg:
                boolean returnA = GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                        IPCManagerFn.IPC_VDCPCmd_GetRecAudioCfg, "");
                Log.i("return",returnA + "");
                break;
            case R.id.bt_setAudioRecordCfg:
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("AudioEnable", 1);

                    params = obj.toString();
                } catch (Exception e) {
                    params = "";
                }
                boolean returnB = GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                        IPCManagerFn.IPC_VDCPCmd_SetRecAudioCfg, params);
                Log.i("return",returnB + "");

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

    }
}
