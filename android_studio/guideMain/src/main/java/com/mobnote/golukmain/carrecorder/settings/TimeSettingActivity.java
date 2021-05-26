package com.mobnote.golukmain.carrecorder.settings;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.carrecorder.IpcDataParser;
import com.mobnote.golukmain.carrecorder.base.CarRecordBaseActivity;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.util.DateTimeUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import goluk.com.t1s.api.callback.CallbackCmd;
import likly.dollar.$;

/**
 * 时间设置页面
 * <p>
 * 2015年4月8日
 *
 * @author xuhw
 */
@SuppressLint("InflateParams")
public class TimeSettingActivity extends CarRecordBaseActivity implements OnClickListener, IPCManagerFn {

    public static final String TAG_LAST_SYNC_TIME = "last_sync_time";

    private static final int STATE_GPS_AUTO = 0;
    private static final int STATE_AUTO = 1;
    private static final int STATE_MOUNT = 2;
    /**
     * 当前的状态
     */
    private int mCurrentState = 0;

    /**
     * 显示年月日
     */
    private TextView mDateText = null;
    /**
     * 显示当前时间
     */
    private TextView mTimeText = null;
    /**
     * 自动同步开关按钮
     */
    private Button mAutoBtn = null;
    /**
     * 年
     */
    private int year;
    /**
     * 月
     */
    private int month;
    /**
     * 日
     */
    private int day;
    /**
     * 时
     */
    private int hour;
    /**
     * 分
     */
    private int minute;
    /**
     * 保存自动同步时间开关状态
     */
    private boolean systemtime;
    private RelativeLayout mDateLayout = null;
    private TextView line = null;
    private float density = 1;

    private RelativeLayout mGpsTimeLayout = null;
    private Button mGpsAutoBtn = null;

    private String strYear = "";
    private String strMonth = "";
    private String strDay = "";

    private boolean mHasChangTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addContentView(LayoutInflater.from(this).inflate(R.layout.carrecorder_time_setting, null));
        setTitle(getResources().getString(R.string.sjsz));
        GolukApplication.getInstance().getIPCControlManager().addIPCManagerListener("timesetting", this);
        density = SoundUtils.getInstance().getDisplayMetrics().density;
        loadRes();
        initView();
        initTimes();
        getSystemTime();
        readPreState();
        switchUIState();
    }

    private void loadRes() {
        strYear = this.getResources().getString(R.string.str_year);
        strMonth = this.getResources().getString(R.string.str_month);
        strDay = this.getResources().getString(R.string.str_day);
    }

    private void readPreState() {
        mCurrentState = SettingUtils.getInstance().getInt("ipctime");
        systemtime = SettingUtils.getInstance().getBoolean("systemtime", true);
        // 兼容上个版本
        if (-1 == mCurrentState) {
            if (systemtime) {
                mCurrentState = STATE_AUTO;
            } else {
                mCurrentState = STATE_MOUNT;
            }
        }
        if (!isT1() && mCurrentState == STATE_GPS_AUTO) {
            mCurrentState = STATE_AUTO;
        }
        // T2S
        if (GolukApplication.getInstance().getIPCControlManager().isT2S() && systemtime) {
            mCurrentState = STATE_AUTO;
        }
    }

    private void switchNextState(int state) {
        GolukDebugUtils.e("", "TimeSettingActivity--------------------switchNextState:-" + mCurrentState + "  state:"
                + state);
        switch (state) {
            case STATE_GPS_AUTO:
                if (mCurrentState == state) {
                    // 关闭
                    mCurrentState = STATE_MOUNT;
                } else {
                    mCurrentState = STATE_GPS_AUTO;
                }
                break;
            case STATE_AUTO:
                if (mCurrentState == state) {
                    // 关闭
                    mCurrentState = STATE_MOUNT;
                } else {
                    mCurrentState = STATE_AUTO;
                }
                break;
            case STATE_MOUNT:
                break;
        }
    }

    private void switchUIState() {
        GolukDebugUtils.e("", "TimeSettingActivity--------------------switchUIState:-" + mCurrentState);
        switch (mCurrentState) {
            case STATE_GPS_AUTO:
                mGpsAutoBtn.setBackgroundResource(R.drawable.set_open_btn);
                mAutoBtn.setBackgroundResource(R.drawable.set_close_btn);
                this.hideTimeLayout();
                break;
            case STATE_AUTO:
                mAutoBtn.setBackgroundResource(R.drawable.set_open_btn);
                mGpsAutoBtn.setBackgroundResource(R.drawable.set_close_btn);
                this.hideTimeLayout();
                break;
            case STATE_MOUNT:
                mAutoBtn.setBackgroundResource(R.drawable.set_close_btn);
                mGpsAutoBtn.setBackgroundResource(R.drawable.set_close_btn);
                this.showTimeLayout();
                break;
            default:
                break;
        }
    }

    private void showTimeLayout() {
        mDateLayout.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
        lineParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lineParams.setMargins((int) (15 * density), 0, 0, 0);
        line.setLayoutParams(lineParams);
    }

    private void hideTimeLayout() {
        mDateLayout.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
        lineParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        line.setLayoutParams(lineParams);
    }

    private void initTimes() {
        long lastSaveTime = $.config().getLong(TAG_LAST_SYNC_TIME, -1);
        Calendar calendar = Calendar.getInstance();
        if (lastSaveTime != -1)
            calendar.setTimeInMillis(lastSaveTime);
        else
            calendar.setTimeInMillis(System.currentTimeMillis());
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        mDateText.setText(year + strYear + month + strMonth + day + strDay);
        if (minute < 10) {
            mTimeText.setText(hour + ":0" + minute);
        } else {
            mTimeText.setText(hour + ":" + minute);
        }
    }

    /**
     * 获取当前系统时间
     *
     * @author xuhw
     * @date 2015年4月6日
     */
    private void getSystemTime() {
        // T2S 无法获取当前时间
        if (GolukApplication.getInstance().getIPCControlManager().isT2S())
            return;

        boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCSystemTime();
        GolukApplication.getInstance().getIPCControlManager().getTimeSyncCfg();
        GolukDebugUtils.e("xuhw", "YYY========getIPCSystemTime=======a=" + a);
    }

    /**
     * 初始化控件
     *
     * @author xuhw
     * @date 2015年4月6日
     */
    private void initView() {
        findViewById(R.id.mDateText).setOnClickListener(this);
        findViewById(R.id.mTimeText).setOnClickListener(this);
        mAutoBtn = (Button) findViewById(R.id.mAutoBtn);
        mDateText = (TextView) findViewById(R.id.mDateText);
        mTimeText = (TextView) findViewById(R.id.mTimeText);
        mAutoBtn.setOnClickListener(this);
        mDateLayout = (RelativeLayout) findViewById(R.id.mDateLayout);
        line = (TextView) findViewById(R.id.line);

        mGpsTimeLayout = (RelativeLayout) findViewById(R.id.time_setting_gpslayout);
        mGpsAutoBtn = (Button) findViewById(R.id.time_gps_mAutoBtn);
        if (isT1()) {
            mGpsTimeLayout.setVisibility(View.GONE);
            mGpsAutoBtn.setOnClickListener(this);
        } else {
            mGpsTimeLayout.setVisibility(View.GONE);
        }
    }

    private boolean isT1() {
        return IPCControlManager.T1_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName)
                || IPCControlManager.T2_SIGN.equals(GolukApplication.getInstance().mIPCControlManager.mProduceName);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.back_btn) {
            exit();
        } else if (id == R.id.mAutoBtn) {
            click_autoTime();
        } else if (id == R.id.mDateText) {
            mHasChangTime = true;
            click_Date();
        } else if (id == R.id.mTimeText) {
            mHasChangTime = true;
            click_Time();
        } else if (id == R.id.time_gps_mAutoBtn) {
            click_gpsAutoTime();
        } else {
        }
    }

    private void click_gpsAutoTime() {
        if (!GolukApplication.getInstance().getIpcIsLogin()) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.str_carrecoder_no_login));
            return;
        }
        switchNextState(STATE_GPS_AUTO);
        this.switchUIState();
        if (STATE_GPS_AUTO == mCurrentState) {
            GolukApplication.getInstance().getIPCControlManager().setTimeSyncCfg(1);
        }
        saveCurrentState();
    }

    private void click_autoTime() {
        if (!GolukApplication.getInstance().getIpcIsLogin()) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.str_carrecoder_no_login));
            return;
        }
        switchNextState(STATE_AUTO);
        this.switchUIState();
        if (STATE_AUTO == mCurrentState) {
            // T1SP
            if (GolukApplication.getInstance().getIPCControlManager().isT2S()) {
                long nowMill = System.currentTimeMillis();
                final String nowDate = DateTimeUtils.getTimeDateString(nowMill);
                final String nowTime = DateTimeUtils.getTimeHourString(nowMill);
                setT2SSystemTime(nowDate, nowTime, System.currentTimeMillis());
            } else {
                // Other
                long time = System.currentTimeMillis() / 1000;
                boolean a = GolukApplication.getInstance().getIPCControlManager().setIPCSystemTime(time);
                GolukDebugUtils.e("xuhw", "YYY============setIPCSystemTime===============a=" + a);
            }
        }
        saveCurrentState();
    }

    /**
     * 同步T2S系统时间
     */
    private void setT2SSystemTime(final String date, final String time, final long milles) {
        goluk.com.t1s.api.ApiUtil.setDate(date, new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                goluk.com.t1s.api.ApiUtil.setTime(time, new CallbackCmd() {
                    @Override
                    public void onSuccess(int i) {
//                        $.toast().text(R.string.str_set_ok).show();
                        $.toast().text("时间同步成功: " + date + " " + time).show();
                        // 保存时间
                        $.config().putLong(TAG_LAST_SYNC_TIME, milles);
                        // 更新显示
                        initTimes();
                    }

                    @Override
                    public void onFail(int i, int i1) {
                        $.toast().text(R.string.str_carrecoder_setting_failed).show();
                    }
                });
            }

            @Override
            public void onFail(int i, int i1) {
                $.toast().text(R.string.str_carrecoder_setting_failed).show();
            }
        });
    }

    private void saveCurrentState() {
        SettingUtils.getInstance().putInt("ipctime", mCurrentState);
        SettingUtils.getInstance().putBoolean("systemtime", mCurrentState == STATE_AUTO);
    }

    private void click_Date() {
        DatePickerDialog datePicker = new DatePickerDialog(TimeSettingActivity.this, new OnDateSetListener() {
            public void onDateSet(DatePicker view, int _year, int monthOfYear, int dayOfMonth) {
                year = _year;
                month = monthOfYear + 1;
                day = dayOfMonth;
                mDateText.setText(year + strYear + month + strMonth + day + strDay);
            }
        }, year, month - 1, day);
        datePicker.show();
    }

    private void click_Time() {
        TimePickerDialog time = new TimePickerDialog(TimeSettingActivity.this, new OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int _minute) {
                hour = hourOfDay;
                minute = _minute;
                if (minute < 10) {
                    mTimeText.setText(hourOfDay + ":0" + minute);
                } else {
                    mTimeText.setText(hourOfDay + ":" + minute);
                }
            }
        }, hour, minute, true);
        time.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (event == ENetTransEvent_IPC_VDCP_CommandResp) {
            // 获取IPC系统时间
            if (msg == IPC_VDCP_Msg_GetTime) {
                ipcCallBack_GetTime(param1, param2);
            } else if (msg == IPC_VDCP_Msg_SetTime) {
                ipcCallBack_SetTime(param1, param2);
            } else if (IPC_VDCP_Msg_GetTimeSyncCfg == msg) {
                ipcCallBack_GetTimeSynCfg(param1, param2);
            } else if (IPC_VDCP_Msg_SetTimeSyncCfg == msg) {
                ipcCallBack_SetTimeSynCfg(param1, param2);
            }
        }
    }

    private void ipcCallBack_GetTimeSynCfg(int param1, Object param2) {
        GolukDebugUtils.e("xuhw", "TimeSettingActivity    ipcCallBack_GetTimeSynCfg=======param1=" + param1
                + "==param2=" + param2);
        if (RESULE_SUCESS != param1) {
            return;
        }
        int gpsStatus = JsonUtil.parseGpsTimeState((String) param2);
        if (1 == gpsStatus) {
            if (STATE_GPS_AUTO != mCurrentState) {
                mCurrentState = STATE_GPS_AUTO;
            }
        } else {
            if (STATE_GPS_AUTO == mCurrentState) {
                mCurrentState = STATE_AUTO;
            }
        }
        this.saveCurrentState();
        this.switchUIState();
    }

    private void ipcCallBack_SetTimeSynCfg(int param1, Object param2) {
        GolukDebugUtils.e("xuhw", "TimeSettingActivity    ipcCallBack_SetTimeSynCfg=======param1=" + param1
                + "==param2=" + param2);
        if (param1 == RESULE_SUCESS) {

        }
    }

    private void ipcCallBack_SetTime(int param1, Object param2) {
        GolukDebugUtils.e("xuhw", "TimeSettingActivity    ipcCallBack_SetTime=======param1=" + param1 + "==param2="
                + param2);
        if (param1 == RESULE_SUCESS) {
            boolean a = GolukApplication.getInstance().getIPCControlManager().getIPCSystemTime();
            GolukDebugUtils.e("xuhw", "YYY========getIPCSystemTime=======a=" + a);
        }
    }

    private void ipcCallBack_GetTime(int param1, Object param2) {
        GolukDebugUtils.e("xuhw", "TimeSettingActivity    ipcCallBack_GetTime=======param1=" + param1 + "==param2="
                + param2);
        if (param1 == RESULE_SUCESS) {
            long time = IpcDataParser.parseIPCTime((String) param2) * 1000;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);

            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);

            mDateText.setText(year + strYear + month + strMonth + day + strDay);
            if (minute < 10) {
                mTimeText.setText(hour + ":0" + minute);
            } else {
                mTimeText.setText(hour + ":" + minute);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GolukApplication.getInstance().setContext(this, "timesetting");
    }

    public void exit() {
        if (!mHasChangTime) {
            finish();
            return;
        }
        if (null != GolukApplication.getInstance().getIPCControlManager()) {
            GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("timesetting");
        }

        if (GolukApplication.getInstance().getIpcIsLogin()) {
            if (STATE_MOUNT == mCurrentState) {
                long time = getMountTime();
                GolukDebugUtils.e("xuhw", "YYY=============time==" + time);
                if (0 != time) {
                    // T1SP
                    if (GolukApplication.getInstance().getIPCControlManager().isT2S()) {
                        // 秒转毫秒
                        time = time * 1000;
                        final String nowDate = DateTimeUtils.getTimeDateString(time);
                        final String nowTime = DateTimeUtils.getTimeHourString(time);
                        setT2SSystemTime(nowDate, nowTime, time);
                    } else {
                        // Other
                        boolean a = GolukApplication.getInstance().getIPCControlManager().setIPCSystemTime(time);
                        GolukDebugUtils.e("xuhw", "YYY============setIPCSystemTime===============a=" + a);
                    }
                }
            }
        }
        finish();
    }

    private long getMountTime() {
        long time = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
        Date date;
        try {
            Time t = new Time();
            t.setToNow();
            int sec = t.second;
            String timestr = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + sec;
            date = sdf.parse(timestr);
            time = date.getTime() / 1000;
        } catch (ParseException e) {
            GolukDebugUtils.e("xuhw", "YYY====str to time fail======22222222222222==");
        }

        return time;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

}
