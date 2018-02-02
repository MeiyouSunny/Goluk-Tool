package com.mobnote.t1sp.ui.setting;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.mobnote.eventbus.CaptureTimeEvent;
import com.mobnote.eventbus.EventExitMode;
import com.mobnote.eventbus.SDCardFormatEvent;
import com.mobnote.eventbus.VideoResEvent;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.golukmain.carrecorder.settings.TimeSettingActivity;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.t1sp.base.control.BindTitle;
import com.mobnote.t1sp.base.ui.BackTitleActivity;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.bean.SettingValue;
import com.mobnote.t1sp.service.HeartbeatTask;
import com.mobnote.t1sp.ui.setting.SDCardInfo.SdCardInfoActivity;
import com.mobnote.t1sp.ui.setting.selection.SelectionActivity;
import com.mobnote.t1sp.ui.setting.version.VersionInfoActivity;
import com.mobnote.t1sp.util.ViewUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import likly.mvp.MvpBinder;

@MvpBinder(
        presenter = DeviceSettingsPresenterImpl.class,
        model = DeviceSettingsModelImpl.class
)
@BindTitle(R2.string.setting_title_text)
public class DeviceSettingsActivity extends BackTitleActivity<DeviceSettingsPresenter> implements DeviceSettingsView {

    @BindView(R2.id.SDCard_storage_value)
    TextView mTvSDCardStorage;
    @BindView(R2.id.video_resolve_value)
    TextView mTvVideoResolve;
    @BindView(R2.id.wonderful_video_time_value)
    TextView mTvSnapTime;
    @BindView(R2.id.shutdown_time_value)
    TextView mTvPowerOffDelay;
    @BindView(R2.id.gsensor_level_value)
    TextView mTvGSensor;
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

    // 忽略首次由程序修改设置的check状态
    private boolean mIgnoreSwtich = true;

    private CustomDialog mCustomDialog;
    private CustomLoadingDialog mDialog;

    private HeartbeatTask mHeartbeatTask;

    private SettingInfo mSettingInfo;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_settings_t1sp;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        EventBus.getDefault().register(this);

        getSettingInfo();

    }

    private void getSettingInfo() {
        mDialog = new CustomLoadingDialog(this, getString(R.string.enter_setting_mode_hint));
        mDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getPresenter().enterOrExitSettingMode(true);
                mHeartbeatTask = new HeartbeatTask(HeartbeatTask.MODE_TYPE_SETTING);
                mHeartbeatTask.start();
            }
        }, 200);
    }

    @OnClick({R2.id.SDCard_storage, R2.id.video_resolve, R2.id.wonderful_video_time, R2.id.gsensor_level,
            R2.id.shutdown_time, R2.id.time_setting, R2.id.version_info, R2.id.reset_factory})
    public void onClick(View view) {
        final int viewId = view.getId();
        if (viewId == R.id.SDCard_storage) {
            ViewUtil.goActivity(this, SdCardInfoActivity.class);
        } else if (viewId == R.id.video_resolve) {
            startSelections(R.string.spzl_title, R.array.video_res, R.array.video_res_values, mSettingInfo.videoRes, TYPE_VIDEO_RES);
        } else if (viewId == R.id.wonderful_video_time) {
            startSelections(R.string.str_wonderful_video_type_title, R.array.capture_time, R.array.capture_time_values, mSettingInfo.captureTime, TYPE_SNAP_TIME);
        } else if (viewId == R.id.gsensor_level) {
            startSelections(R.string.pzgy_title, R.array.gsendor_level, R.array.gsendor_level_values, mSettingInfo.GSensor, TYPE_GSENSOR);
        } else if (viewId == R.id.shutdown_time) {
            startSelections(R.string.str_settings_shutdown_title, R.array.power_off_delay, R.array.power_off_delay_values, mSettingInfo.powerOffDelay, TYPE_POWER_OFF_DELAY);
        } else if (viewId == R.id.reset_factory) {
            showRestFactoryConfirmDialog();
        } else if (viewId == R.id.version_info) {
            ViewUtil.goActivity(this, VersionInfoActivity.class, "info", mSettingInfo);
        } else if (viewId == R.id.time_setting) {
            ViewUtil.goActivity(this, TimeSettingActivity.class);
        }
    }

    @OnCheckedChanged({R2.id.switch_record_sound, R2.id.switch_power_sound, R2.id.switch_capture_sound, R2.id.switch_auto_rotate,
            R2.id.switch_watermark, R2.id.switch_parking_guard, R2.id.switch_mtd, R2.id.switch_emg_video_sound, R2.id.switch_dormant_mode})
    public void onChecked(CompoundButton button, boolean isChecked) {
        if (mIgnoreSwtich)
            return;
        final int viewId = button.getId();
        if (viewId == R.id.switch_record_sound) {
            getPresenter().setSoundRecord(isChecked);
        } else if (viewId == R.id.switch_power_sound) {
            getPresenter().setPowerSound(isChecked);
        } else if (viewId == R.id.switch_capture_sound) {
            getPresenter().setCaptureSound(isChecked);
        } else if (viewId == R.id.switch_emg_video_sound) {
            getPresenter().setEmgVideoSound(isChecked);
        } else if (viewId == R.id.switch_dormant_mode) {
            getPresenter().setSleepMode(isChecked);
            if (isChecked) {
                mIgnoreSwtich = true;
                switchParkingGuard.setChecked(!isChecked);
                mIgnoreSwtich = false;
            }
        } else if (viewId == R.id.switch_parking_guard) {
            getPresenter().setParkGuard(isChecked);
            if (isChecked) {
                mIgnoreSwtich = true;
                switchDormantMode.setChecked(!isChecked);
                mIgnoreSwtich = false;
            }
        } else if (viewId == R.id.switch_auto_rotate) {
            getPresenter().setAutoRotate(isChecked);
        } else if (viewId == R.id.switch_watermark) {
            getPresenter().setRecStamp(isChecked);
        } else if (viewId == R.id.switch_mtd) {
            getPresenter().setMTD(isChecked);
        }
    }

    private void startSelections(@StringRes int titleId, @ArrayRes int labelsId, @ArrayRes int valuesId, String currentValue, int requestCode) {
        ArrayList<SettingValue> values = getPresenter().generateSettingValues(this, labelsId, valuesId, currentValue);
        Intent intent = new Intent(this, SelectionActivity.class);
        intent.putExtra("title", titleId);
        intent.putParcelableArrayListExtra("values", values);
        if (requestCode == TYPE_SNAP_TIME) {
            intent.putExtra("type", SelectionActivity.TYPE_CAPTURE_TIME);
        } else if (requestCode == TYPE_GSENSOR) {
            intent.putExtra("type", SelectionActivity.TYPE_G_SENSOR);
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onGetSettingInfos(SettingInfo settingInfo) {
        if (settingInfo == null)
            return;
        mSettingInfo = settingInfo;

        mTvSDCardStorage.setText(settingInfo.SDCardInfo);
        mTvVideoResolve.setText(getPresenter().getSettingLabelByValue(this, R.array.video_res, R.array.video_res_values, settingInfo.videoRes));
        mTvPowerOffDelay.setText(getPresenter().getSettingLabelByValue(this, R.array.power_off_delay, R.array.power_off_delay_values, settingInfo.powerOffDelay));
        mTvGSensor.setText(getPresenter().getSettingLabelByValue(this, R.array.gsendor_level, R.array.gsendor_level_values, settingInfo.GSensor));
        mTvSnapTime.setText(getPresenter().getSettingLabelByValue(this, R.array.capture_time, R.array.capture_time_values, settingInfo.captureTime));
        switchRecordSound.setChecked(settingInfo.soundRecord);
        switchPowerSound.setChecked(settingInfo.powerSound);
        switchCaptureSound.setChecked(settingInfo.snapSound);
        switchAutoRotate.setChecked(settingInfo.autoRotate);
        switchWatermark.setChecked(settingInfo.recStamp);
        switchParkingGuard.setChecked(settingInfo.pkMode);
        switchDormantMode.setChecked(settingInfo.sleepMode);
        switchEmgVideoSound.setChecked(settingInfo.emgVideoSound);
//        switchMTD.setChecked(false);
//        switchMTD.setEnabled(settingInfo.parkingGuard);

        mIgnoreSwtich = false;

        if (mDialog.isShowing())
            mDialog.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        final SettingValue settingValue = data.getParcelableExtra("value");
        final String value = settingValue.value;
        getPresenter().setSelectionSettingValue(requestCode, value);
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
                        getPresenter().resetFactory();
                    }
                });
        mCustomDialog.setRightButton(this.getResources().getString(R.string.dialog_str_cancel), null);
        mCustomDialog.show();
    }

    @Override
    public void onResetFactory(final boolean isSuccess) {
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

    @Override
    public void onGetSDCardInfo(SettingInfo settingInfo) {
        if (settingInfo != null)
            mTvSDCardStorage.setText(settingInfo.SDCardInfo);
    }

    /**
     * SDcard 格式化成功
     */
    public void onEventMainThread(SDCardFormatEvent event) {
        getPresenter().getSDCardInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 发送退出设置模式指令
        EventBus.getDefault().post(new EventExitMode(1));

        EventBus.getDefault().unregister(this);
        if (mHeartbeatTask != null)
            mHeartbeatTask.stop();
    }

    @Override
    public void onSelectionSetted(int type, String value) {
        // 选项设置成功返回,更新UI
        switch (type) {
            case DeviceSettingsView.TYPE_VIDEO_RES:
                mSettingInfo.videoRes = value;
                mTvVideoResolve.setText(getPresenter().getSettingLabelByValue(this, R.array.video_res, R.array.video_res_values, value));
                // 发送Event给预览页面
                VideoResEvent eventVideoRes = new VideoResEvent();
                eventVideoRes.value = value;
                EventBus.getDefault().post(eventVideoRes);
                break;
            case DeviceSettingsView.TYPE_SNAP_TIME:
                mSettingInfo.captureTime = value;
                mTvSnapTime.setText(getPresenter().getSettingLabelByValue(this, R.array.capture_time, R.array.capture_time_values, value));
                // 发送Event给预览页面
                CaptureTimeEvent eventCaptureTime = new CaptureTimeEvent();
                eventCaptureTime.value = value;
                EventBus.getDefault().post(eventCaptureTime);
                break;
            case DeviceSettingsView.TYPE_GSENSOR:
                mSettingInfo.GSensor = value;
                mTvGSensor.setText(getPresenter().getSettingLabelByValue(this, R.array.gsendor_level, R.array.gsendor_level_values, value));
                break;
        }
    }
}
