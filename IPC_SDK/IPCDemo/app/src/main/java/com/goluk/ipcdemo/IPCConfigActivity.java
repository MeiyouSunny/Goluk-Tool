package com.goluk.ipcdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.goluk.ipcdemo.com.goluk.ipcdemo.widget.ToggleButton;
import com.goluk.ipcsdk.ipcCommond.IPCConfigCommand;
import com.goluk.ipcsdk.listener.IPCConfigListener;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCConfigActivity extends Activity implements View.OnClickListener,IPCConfigListener{

    ToggleButton mAudioRecordTb;
    IPCConfigCommand mIPCConfigCommand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipcconfig);

        initView();

        initData();

        setupView();

        mIPCConfigCommand.getAudioRecordCfg();

    }

    private void initData() {
        mIPCConfigCommand = new IPCConfigCommand(this,this);
    }

    private void setupView() {

        mAudioRecordTb.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                boolean isSendSeccuss = mIPCConfigCommand.setAudioRecordCfg(on);
                if(isSendSeccuss){
                    return;
                }else{
                    if(on){
                        mAudioRecordTb.setToggleOff(true);
                    }else{
                        mAudioRecordTb.setToggleOn(true);
                    }
                }
            }
        });
    }

    private void initView() {
        mAudioRecordTb = (ToggleButton) findViewById(R.id.tb_audioRecord);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void callback_setAudeoRecord(boolean success) {
        if(success){
            Toast.makeText(this, "VoiceRecordSettingSuccess", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "VoiceRecordSettingFail", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callback_getAudeoRecord(boolean enable) {
        if(enable){
            mAudioRecordTb.setToggleOn(true);
        }else{
            mAudioRecordTb.setToggleOff(true);
        }
    }

    @Override
    public void callback_setTime(boolean success) {

    }

    @Override
    public void callback_getTime(long timestamp) {

    }
}
