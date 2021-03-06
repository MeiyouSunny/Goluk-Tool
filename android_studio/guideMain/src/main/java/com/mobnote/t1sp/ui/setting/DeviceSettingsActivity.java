package com.mobnote.t1sp.ui.setting;

import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.RestoreFactoryEvent;
import com.mobnote.eventbus.SDCardFormatEvent;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.golukmain.carrecorder.settings.TimeSettingActivity;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomFormatDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.t1sp.api.setting.IPCConfigListener;
import com.mobnote.t1sp.api.setting.IpcConfigOption;
import com.mobnote.t1sp.api.setting.IpcConfigOptionF4;
import com.mobnote.t1sp.base.ui.BackTitleActivity;
import com.mobnote.t1sp.bean.SettingValue;
import com.mobnote.t1sp.connect.T1SPConnecter;
import com.mobnote.t1sp.connect.T1SPConntectListener;
import com.mobnote.t1sp.ui.setting.SDCardInfo.SdCardInfoActivity;
import com.mobnote.t1sp.ui.setting.selection.SelectionActivity;
import com.mobnote.t1sp.ui.setting.version.VersionInfoActivity;
import com.mobnote.t1sp.util.StringUtil;
import com.mobnote.t1sp.util.ViewUtil;

import androidx.annotation.StringRes;
import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackCmd;
import likly.dollar.$;
import likly.mvp.MvpBinder;

@MvpBinder(
        presenter = DeviceSettingsPresenterImpl.class,
        model = DeviceSettingsModelImpl.class
)
public class DeviceSettingsActivity extends BackTitleActivity<DeviceSettingsPresenter> implements DeviceSettingsView, IPCConfigListener, CompoundButton.OnCheckedChangeListener, T1SPConntectListener {

    //    @BindView(R2.id.title)
//    TextView mTitle;
    @BindView(R2.id.SDCard_storage_value)
    TextView mTvSDCardStorage;
    @BindView(R2.id.video_resolve_value)
    TextView mTvVideoResolve;
    @BindView(R2.id.video_time_value)
    TextView mTvRecordTime;
    @BindView(R2.id.wonderful_video_quality)
    RelativeLayout mLayoutCaptureQulity;
    @BindView(R2.id.lineWonderfulQuality)
    View mLineCaptureQulity;
    @BindView(R2.id.wonderful_video_quality_value)
    TextView mTvCaptureQulity;
    @BindView(R2.id.tv_volume_level)
    TextView mTvVolumeLevel;
    @BindView(R2.id.wonderful_video_time_value)
    TextView mTvSnapTime;
    @BindView(R2.id.shutdown_time_value)
    TextView mTvPowerOffDelay;
    @BindView(R2.id.gsensor_level_value)
    TextView mTvGSensor;
    @BindView(R2.id.acc_option_value)
    TextView mTvAccOption;
    @BindView(R2.id.language_value)
    TextView mTvLanguage;
    @BindView(R2.id.switch_record_sound)
    SwitchButton switchRecordSound;
    @BindView(R2.id.switch_power_sound)
    SwitchButton switchPowerSound;
    @BindView(R2.id.switch_capture_sound)
    SwitchButton switchCaptureSound;
    @BindView(R2.id.switch_auto_rotate)
    SwitchButton switchAutoRotate;
    @BindView(R2.id.switch_watermark)
    SwitchButton switchWatermark;
    @BindView(R2.id.switch_parking_guard)
    SwitchButton switchParkingGuard;
    @BindView(R2.id.switch_mtd)
    SwitchButton switchMTD;
    @BindView(R2.id.switch_emg_video_sound)
    SwitchButton switchEmgVideoSound;
    @BindView(R2.id.switch_dormant_mode)
    SwitchButton switchDormantMode;
    @BindView(R2.id.switch_fatigue)
    SwitchButton switchFatigue;

    @BindView(R2.id.sensor_xy_et)
    EditText mEtSensorXY;
    @BindView(R2.id.sensor_z_et)
    EditText mEtSensorZ;

    @BindView(R2.id.language_set)
    RelativeLayout mLayoutLanguage;

    // ????????????????????????????????????check??????
    private boolean mIgnoreSwtich = true;

    private CustomDialog mCustomDialog;
    private CustomLoadingDialog mDialog;

    private IpcConfigOption mConfigOption;

    private String[] mArrayVideoQulity, mArrayGSensorLevel, mArrayCaptureQulity, mArrayVolumeLevel, mArrayLanguages, mArrayRecordTime, mArrayAccOptions;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_settings_t1sp;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        EventBus.getDefault().register(this);
        setTitle(R.string.setting_title_text);

        switchRecordSound.setOnCheckedChangeListener(this);
        switchPowerSound.setOnCheckedChangeListener(this);
        switchCaptureSound.setOnCheckedChangeListener(this);
        switchAutoRotate.setOnCheckedChangeListener(this);
        switchWatermark.setOnCheckedChangeListener(this);
        switchParkingGuard.setOnCheckedChangeListener(this);
        switchMTD.setOnCheckedChangeListener(this);
        switchEmgVideoSound.setOnCheckedChangeListener(this);
        switchDormantMode.setOnCheckedChangeListener(this);
        switchFatigue.setOnCheckedChangeListener(this);

        if (GolukApplication.getInstance().getIPCControlManager().isT1S()) {
            mArrayVideoQulity = getResources().getStringArray(R.array.video_qulity_lables_t1s);
            mLayoutCaptureQulity.setVisibility(View.GONE);
            mLineCaptureQulity.setVisibility(View.GONE);
            mLayoutLanguage.setVisibility(View.GONE);
        } else {
            mArrayVideoQulity = getResources().getStringArray(R.array.video_qulity_lables);
        }

        mArrayCaptureQulity = getResources().getStringArray(R.array.capture_qulity_lables);
        mArrayGSensorLevel = getResources().getStringArray(R.array.parking_guard_and_mtd);
        mArrayAccOptions = getResources().getStringArray(R.array.acc_option_values);
        mArrayVolumeLevel = getResources().getStringArray(R.array.list_tone_volume);
        mArrayLanguages = getResources().getStringArray(R.array.list_language_t);
        mArrayRecordTime = getResources().getStringArray(R.array.record_time);

        mConfigOption = new IpcConfigOptionF4(this);
        mConfigOption.getAllSettingConfig();

//        ApiUtil.startRecord(false, new CallbackCmd() {
//            @Override
//            public void onSuccess(int i) {
//            }
//
//            @Override
//            public void onFail(int i, int i1) {
//            }
//        });

        T1SPConnecter.instance().addListener(this);
    }

    private void getSettingInfo() {
        mDialog = new CustomLoadingDialog(this, "");
//        mDialog.show();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                getPresenter().enterOrExitSettingMode(true);
////                mHeartbeatTask = new HeartbeatTask(HeartbeatTask.MODE_TYPE_SETTING);
////                mHeartbeatTask.start();
//                getPresenter().getAllInfo();
//            }
//        }, 200);

    }

    @OnClick({R2.id.SDCard_storage, R2.id.mFormatSDCard, R2.id.video_resolve, R2.id.wonderful_video_quality, R2.id.wonderful_video_time, R2.id.gsensor_level,
            R2.id.volume_level, R2.id.shutdown_time, R2.id.time_setting, R2.id.version_info, R2.id.reset_factory, R2.id.language_set, R2.id.video_time, R2.id.sensor_xy_bt,
            R2.id.sensor_z_bt, R2.id.acc_option})
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.SDCard_storage) {
            ViewUtil.goActivity(this, SdCardInfoActivity.class);
        } else if (viewId == R.id.mFormatSDCard) {
            showConfirmDialog();
        } else if (viewId == R.id.video_resolve) {
            startSelections(R.string.spzl_title, mArrayVideoQulity, ViewUtil.getTextViewValue(mTvVideoResolve), TYPE_VIDEO_RES);
        } else if (viewId == R.id.gsensor_level) {
            startSelections(R.string.pzgy_title, mArrayGSensorLevel, ViewUtil.getTextViewValue(mTvGSensor), TYPE_GSENSOR);
        } else if (viewId == R.id.wonderful_video_quality) {
            startSelections(R.string.str_wonderful_video_quality_title, mArrayCaptureQulity, ViewUtil.getTextViewValue(mTvCaptureQulity), TYPE_CAPTURE_QULITY);
        } else if (viewId == R.id.acc_option) {
            startSelections(R.string.acc_option, mArrayAccOptions, ViewUtil.getTextViewValue(mTvAccOption), TYPE_ACC_OPTION);
        } else if (viewId == R.id.volume_level) {
            startSelections(R.string.str_settings_tone_title, mArrayVolumeLevel, ViewUtil.getTextViewValue(mTvVolumeLevel), TYPE_VOLUME_LEVEL);
        } else if (viewId == R.id.reset_factory) {
            showRestFactoryConfirmDialog();
        } else if (viewId == R.id.version_info) {
            ViewUtil.goActivity(this, VersionInfoActivity.class);
        } else if (viewId == R.id.time_setting) {
            ViewUtil.goActivity(this, TimeSettingActivity.class);
        } else if (viewId == R.id.language_set) {
            startSelections(R.string.str_settings_language_title, mArrayLanguages, ViewUtil.getTextViewValue(mTvLanguage), TYPE_LANGUAGE);
        } else if (viewId == R.id.video_time) {
            startSelections(R.string.record_time, mArrayRecordTime, ViewUtil.getTextViewValue(mTvRecordTime), TYPE_VIDEO_TIME);
        } else if (viewId == R.id.sensor_xy_bt) {
            int xy = Integer.valueOf(mEtSensorXY.getText().toString());
            ApiUtil.setGSensorXY(xy, new CallbackCmd() {
                @Override
                public void onSuccess(int i) {
                    $.toast().text("????????????").show();
                }

                @Override
                public void onFail(int i, int i1) {
                }
            });
        } else if (viewId == R.id.sensor_z_bt) {
            int z = Integer.valueOf(mEtSensorZ.getText().toString());
            ApiUtil.setGSensorZ(z, new CallbackCmd() {
                @Override
                public void onSuccess(int i) {
                    $.toast().text("????????????").show();
                }

                @Override
                public void onFail(int i, int i1) {
                }
            });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final int viewId = buttonView.getId();
        if (viewId == R.id.switch_record_sound) {
            mConfigOption.setSoundRecordStatus(isChecked);

        } else if (viewId == R.id.switch_power_sound) {
            mConfigOption.setSoundPowerStatus(isChecked);

        } else if (viewId == R.id.switch_capture_sound) {
            mConfigOption.setSoundCaptureStatus(isChecked);

        } else if (viewId == R.id.switch_emg_video_sound) {
            mConfigOption.setSoundUrgentStatus(isChecked);

        } else if (viewId == R.id.switch_dormant_mode) {
            mConfigOption.setParkSleepMode(isChecked);
            if (isChecked) {
                switchParkingGuard.setChecked(false);
                mConfigOption.setParkSecurityMode(false);
            }
        } else if (viewId == R.id.switch_parking_guard) {
            mConfigOption.setParkSecurityMode(isChecked);
            if (isChecked) {
                switchDormantMode.setChecked(false);
                mConfigOption.setParkSleepMode(false);
            }
        } else if (viewId == R.id.switch_auto_rotate) {
            mConfigOption.setAutoRotate(isChecked);
        } else if (viewId == R.id.switch_watermark) {
            mConfigOption.setWatermarkStatus(isChecked);
        } else if (viewId == R.id.switch_mtd) {
            //getPresenter().setMTD(isChecked);
        } else if (viewId == R.id.switch_fatigue) {
            mConfigOption.setDriveFatigue(isChecked);
        }
    }

    private void formartSDCard() {
        if (mConfigOption != null) {
            mConfigOption.formatSD();
        }
    }

    private CustomFormatDialog mFormatDialog;

    private void showConfirmDialog() {
        CustomDialog confirmDialog = new CustomDialog(this);
        confirmDialog.setMessage(getString(R.string.str_carrecorder_storage_format_sdcard_dialog_message), Gravity.CENTER);
        confirmDialog.setLeftButton(
                this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_yes),
                new CustomDialog.OnLeftClickListener() {
                    @Override
                    public void onClickListener() {
                        formartSDCard();

                        mFormatDialog = new CustomFormatDialog(DeviceSettingsActivity.this);
                        mFormatDialog.setCancelable(false);
                        mFormatDialog.setMessage(getResources().getString(R.string.str_carrecorder_storage_format_sdcard_formating));
                        mFormatDialog.show();
                    }
                });
        confirmDialog.setRightButton(
                this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_no), null);
        confirmDialog.show();
    }

    private void onSdFormated(boolean isFormat) {
        // ?????????????????????
        if (null != mFormatDialog && mFormatDialog.isShowing())
            mFormatDialog.dismiss();

        // ????????????
        if (isFormat)
            mConfigOption.getSDCapacity();

        // ?????????????????????
        String message = getString(isFormat ? R.string.str_carrecorder_storage_format_sdcard_success : R.string.str_carrecorder_storage_format_sdcard_fail);
        CustomDialog confirmDialog = new CustomDialog(this);
        confirmDialog.setMessage(message, Gravity.CENTER);
        confirmDialog.setLeftButton(getString(R.string.user_repwd_ok), null);
        confirmDialog.show();

        // Event
        EventBus.getDefault().post(new SDCardFormatEvent());
    }

    private void startSelections(@StringRes int titleId, String[] lables, String selectedLable, int requestCode) {
        Intent intent = new Intent(this, SelectionActivity.class);
        intent.putExtra("title", titleId);
        intent.putExtra("values", lables);
        intent.putExtra("selectedLable", selectedLable);
        if (requestCode == TYPE_SNAP_TIME) {
            intent.putExtra("type", SelectionActivity.TYPE_CAPTURE_TIME);
        } else if (requestCode == TYPE_GSENSOR) {
            intent.putExtra("type", SelectionActivity.TYPE_G_SENSOR);
        }
        startActivityForResult(intent, requestCode);
    }

    private void setSwitchState(SwitchButton switchView, boolean check) {
        switchView.setOnCheckedChangeListener(null);
        switchView.setChecked(check);
        switchView.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        final SettingValue settingValue = data.getParcelableExtra("value");
        if (requestCode == TYPE_VIDEO_RES) {
            mTvVideoResolve.setText(settingValue.description);
            mConfigOption.setVideoEncodeConfig(settingValue.value);
        } else if (requestCode == TYPE_CAPTURE_QULITY) {
            mTvCaptureQulity.setText(settingValue.description);
            mConfigOption.setCaptureVideoQulity(settingValue.value);
        } else if (requestCode == TYPE_GSENSOR) {
            mTvGSensor.setText(settingValue.description);
            mConfigOption.setCollisionSensity(settingValue.value);
        } else if (requestCode == TYPE_VOLUME_LEVEL) {
            mTvVolumeLevel.setText(settingValue.description);
            mConfigOption.setVolumeValue(settingValue.value);
        } else if (requestCode == TYPE_LANGUAGE) {
            mTvLanguage.setText(settingValue.description);
            mConfigOption.setLanguage(settingValue.value);
        } else if (requestCode == TYPE_VIDEO_TIME) {
            mTvRecordTime.setText(settingValue.description);
            mConfigOption.setCycleRecTime(settingValue.value + 1);
        } else if (requestCode == TYPE_ACC_OPTION) {
            mTvAccOption.setText(settingValue.description);
            mConfigOption.setAccOption(settingValue.value);
        }
    }

    private void showRestFactoryConfirmDialog() {
        if (mCustomDialog == null) {
            mCustomDialog = new CustomDialog(this);
        }
        mCustomDialog.setMessage(this.getResources().getString(R.string.str_reset_message), Gravity.CENTER);
        mCustomDialog.setLeftButton(this.getResources().getString(R.string.user_personal_sign_title),
                new CustomDialog.OnLeftClickListener() {
                    @Override
                    public void onClickListener() {
                        boolean result = mConfigOption.resetFactory();
                        if (result) {
                            ApiUtil.reconnectWIFI(new CallbackCmd() {
                                @Override
                                public void onSuccess(int i) {
                                    onResetFactory(true);
                                }

                                @Override
                                public void onFail(int i, int i1) {
                                    onResetFactory(false);
                                }
                            });
                        }
                    }
                });
        mCustomDialog.setRightButton(this.getResources().getString(R.string.dialog_str_cancel), null);
        mCustomDialog.show();
    }

    private void onResetFactory(final boolean isSuccess) {
        RestoreFactoryEvent eventFactory = new RestoreFactoryEvent();
        EventBus.getDefault().post(eventFactory);

        // ???????????????????????????
        if (isSuccess) {
            SettingUtils.getInstance().putBoolean("systemtime", true);
        }

        String message = getString(isSuccess ? R.string.str_restore_success : R.string.str_restore_fail);
        CustomDialog confirmDialog = new CustomDialog(this);
        confirmDialog.setCancelable(false);
        confirmDialog.setMessage(message, Gravity.CENTER);
        confirmDialog.setLeftButton(this.getResources().getString(R.string.user_personal_sign_title),
                new CustomDialog.OnLeftClickListener() {
                    @Override
                    public void onClickListener() {
                        if (isSuccess)
                            finish();
                    }
                });
        confirmDialog.show();
    }

    /**
     * SDcard ???????????????
     */
    public void onEventMainThread(SDCardFormatEvent event) {
        mConfigOption.getSDCapacity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        ApiUtil.saveMenuInfo(new CallbackCmd() {
            @Override
            public void onSuccess(int i) {
                startRecord();
            }

            @Override
            public void onFail(int i, int i1) {
                startRecord();
            }
        });
        T1SPConnecter.instance().removeListener(this);
    }

    private void startRecord() {
//        ApiUtil.startRecord(true, new CallbackCmd() {
//            @Override
//            public void onSuccess(int i) {
//            }
//
//            @Override
//            public void onFail(int i, int i1) {
//            }
//        });
    }

    @Override
    public void showLoadingDialog() {

    }

    @Override
    public void hideLoadingDialog() {

    }

    @Override
    public void onDeviceTimeSet(boolean success) {

    }

    @Override
    public void onDeviceTimeGet(long timestamp) {
    }

    @Override
    public void onParkSleepModeSet(boolean success) {

    }

    @Override
    public void onDriveFatigueSet(boolean success) {

    }

    @Override
    public void onParkSleepModeGet(boolean enable) {
        setSwitchState(switchDormantMode, enable);
    }

    @Override
    public void onDriveFatigueGet(boolean enable) {
        setSwitchState(switchFatigue, enable);
    }

    @Override
    public void onParkSecurityModeSet(boolean success) {

    }

    @Override
    public void onParkSecurityModeGet(boolean enable) {
        setSwitchState(switchParkingGuard, enable);
    }

    @Override
    public void onRecordStatusGet(boolean enable) {

    }

    @Override
    public void onRecordStatusSet(boolean success) {

    }

    @Override
    public void onSoundRecordStatusGet(boolean enable) {
        setSwitchState(switchRecordSound, enable);
    }

    @Override
    public void onSoundRecordStatusSet(boolean success) {

    }

    @Override
    public void onWatermarkStatusGet(boolean enable) {
        setSwitchState(switchWatermark, enable);
    }

    @Override
    public void onWatermarkStatusSet(boolean success) {

    }

    @Override
    public void onSoundPowerStatusGet(boolean enable) {
        setSwitchState(switchPowerSound, enable);
    }

    @Override
    public void onSoundPowerAndCaptureStatusSet(boolean success) {

    }

    @Override
    public void onSoundCaptureStatusGet(boolean enable) {
        setSwitchState(switchCaptureSound, enable);
    }

    @Override
    public void onSoundUrgentStatusGet(boolean enable) {
        setSwitchState(switchEmgVideoSound, enable);
    }

    @Override
    public void onSoundUrgentStatusSet(boolean success) {

    }

    @Override
    public void onVolumeValueGet(int value) {
        mTvVolumeLevel.setText(mArrayVolumeLevel[value]);
    }

    @Override
    public void onVolumeValueSet(boolean success) {

    }

    @Override
    public void onCaptureVideoQulityGet(int index) {
        mTvCaptureQulity.setText(mArrayCaptureQulity[index]);
    }

    @Override
    public void onCaptureVideoQulitySet(boolean success) {

    }

    @Override
    public void onCaptureVideoTypeGet(int value) {
    }

    @Override
    public void onCaptureVideoTypeSet(boolean success) {

    }

    @Override
    public void onCollisionSensityGet(int value) {
        mTvGSensor.setText(mArrayGSensorLevel[value]);
    }

    @Override
    public void onCollisionSensitySet(boolean success) {

    }

    @Override
    public void onVideoEncodeConfigGet(int index) {
        mTvVideoResolve.setText(mArrayVideoQulity[index]);
    }

    @Override
    public void onVideoEncodeConfigSet(boolean success) {

    }

    @Override
    public void onSDCapacityGet(double total, double free) {
        String SDInfo = StringUtil.getSize(total) + "/" + StringUtil.getSize(free);
        mTvSDCardStorage.setText(SDInfo);
    }

    @Override
    public void onFormatSDCardResult(boolean success) {
        onSdFormated(success);
    }

    @Override
    public void onResetFactoryResult(boolean success) {

    }

    @Override
    public void onTimeslapseConfigGet(boolean enable) {

    }

    @Override
    public void onTimeslapseConfigSet(boolean success) {

    }

    @Override
    public void onLanguageGet(int type) {
        mTvLanguage.setText(mArrayLanguages[type]);
    }

    @Override
    public void onLanguageSet(boolean success) {
    }

    @Override
    public void onAutoRotateGet(boolean enable) {
        setSwitchState(switchAutoRotate, enable);
    }

    @Override
    public void onAutoRotateSet(boolean success) {

    }

    @Override
    public void onCycleRecTimeGet(int timeType) {
        mTvRecordTime.setText(mArrayRecordTime[timeType - 1]);
    }

    @Override
    public void onCycleRecTimeSet(boolean success) {

    }

    @Override
    public void onACCOptionGet(int option) {
        mTvAccOption.setText(mArrayAccOptions[option]);
    }

    @Override
    public void onACCOptionSet(boolean success) {

    }

    @Override
    public void onT1SPDisconnected() {
        finish();
    }

    @Override
    public void onT1SPConnectStart() {
    }

    @Override
    public void onT1SPConnectResult(boolean success) {
    }

}
