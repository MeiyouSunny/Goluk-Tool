package com.goluk.ipcdemo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.ipcdemo.com.goluk.ipcdemo.widget.ToggleButton;
import com.goluk.ipcsdk.ipcCommond.IPCConfigCommand;
import com.goluk.ipcsdk.listener.IPCConfigListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;

import java.util.Calendar;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCConfigActivity extends FragmentActivity implements View.OnClickListener,IPCConfigListener,OnDateSetListener {

    ToggleButton mAudioRecordTb;
    IPCConfigCommand mIPCConfigCommand;
    TimePickerDialog mTimeDialog;
    Button mSetTimeBt;
    TextView mTimeTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipcconfig);

        initView();

        initData();

        setupView();

        initTimePicker();

        mIPCConfigCommand.getAudioRecordCfg();
        mIPCConfigCommand.getTime();

    }

    @Override
    protected void onDestroy() {
        GolukIPCSdk.getInstance().unregisterIPC(this);
        super.onDestroy();
    }

    private void initTimePicker(){
        if(mTimeDialog == null){
            mTimeDialog = new TimePickerDialog.Builder()
                    .setCallBack(this)
                    .setCancelStringId("Cancel")
                    .setSureStringId("Confirm")
                    .setTitleStringId("Pick Time")
                    .setCyclic(false)
                    .setMinMillseconds(0)
                    .setSelectorMillseconds(System.currentTimeMillis())
                    .setThemeColor(getResources().getColor(R.color.timepicker_dialog_bg))
                    .setType(Type.ALL)
                    .setWheelItemTextSize(15)
                    .build();
        }

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
        mSetTimeBt.setOnClickListener(this);
    }

    private void initView() {
        mAudioRecordTb = (ToggleButton) findViewById(R.id.tb_audioRecord);
        mSetTimeBt = (Button) findViewById(R.id.bt_setTime);
        mTimeTv = (TextView) findViewById(R.id.tv_time);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_setTime:
                mTimeDialog.show(getSupportFragmentManager(),"");
                break;
            default:
                break;
        }
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
        if(success){
            Toast.makeText(this, "Set Time Success", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Set Time Fail", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callback_getTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if(minute < 10){
            mTimeTv.setText(year + "-" + month + "-" + day + "  " + hour + ":0" + minute);
        }else{
            mTimeTv.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute);
        }
    }

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
        mIPCConfigCommand.setTime(millseconds/1000);
    }
}
