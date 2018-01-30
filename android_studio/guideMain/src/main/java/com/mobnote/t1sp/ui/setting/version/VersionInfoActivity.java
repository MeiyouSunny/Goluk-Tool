package com.mobnote.t1sp.ui.setting.version;

import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.t1sp.base.control.BindTitle;
import com.mobnote.t1sp.base.ui.BackTitleActivity;
import com.mobnote.t1sp.bean.SettingInfo;

import butterknife.BindView;
import likly.mvp.MvpBinder;

@MvpBinder(
)
@BindTitle(R2.string.bbxx)
public class VersionInfoActivity extends BackTitleActivity {

    @BindView(R2.id.im_carrecorder_version_icon)
    ImageView mDevicePic;
    @BindView(R2.id.text_model)
    TextView mDeviceModel;
    @BindView(R2.id.mDeviceId)
    TextView mDeviceId;
    @BindView(R2.id.mVersion)
    TextView mDeviceVersion;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_version_info;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();

        SettingInfo settingInfo = getIntent().getParcelableExtra("info");
        if (settingInfo != null) {
            mDeviceModel.setText(getString(R.string.str_goluk) + settingInfo.deviceModel);
            mDeviceId.setText(settingInfo.deviceId);
            mDeviceVersion.setText(settingInfo.deviceVersion);
        }

        mDevicePic.setImageResource(R.drawable.connect_t1_icon_1);
    }

}
