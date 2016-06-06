package com.goluk.ipcdemo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.goluk.ipcdemo.com.goluk.ipcdemo.widget.ToggleButton;
import com.goluk.ipcsdk.command.IPCConfigCommand;
import com.goluk.ipcsdk.listener.IPCConfigListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;

import org.joda.time.DateTime;

import java.util.Calendar;

/**
 * Created by leege100 on 16/5/31.
 */
public class IPCConfigActivity extends FragmentActivity implements View.OnClickListener,IPCConfigListener {

    ToggleButton mAudioRecordTb;
    IPCConfigCommand mIPCConfigCommand;
    TimePickerDialog mTimeDialog;
    DatePickerDialog mDateDialog;
    Button mSetTimeBt;
    TextView mTimeTv;
    private int mCurrSelectedYear;
    private int mCurrSelectedMonth;
    private int mCurrSelectedDay;

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
            mTimeDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    DateTime dateTime = new DateTime(mCurrSelectedYear, mCurrSelectedMonth + 1, mCurrSelectedDay, hourOfDay, minute, 0, 0);
                    mIPCConfigCommand.setTime(dateTime.getMillis()/1000);
                }
            },15,15,true);
            mDateDialog = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mCurrSelectedYear = year;
                    mCurrSelectedMonth = monthOfYear;
                    mCurrSelectedDay = dayOfMonth;
                    mTimeDialog.show();
                }
            },2016,Calendar.JUNE,10);
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
                mDateDialog.show();
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
        DateTime dateTime = new DateTime(time);
        mTimeTv.setText(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
    }

}
