package com.mobnote.t1sp.ui.setting.version;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.t1sp.base.control.BindTitle;
import com.mobnote.t1sp.base.ui.BackTitleActivity;
import com.mobnote.util.QRCodeEncoder;

import butterknife.BindView;
import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackVersion;
import goluk.com.t1s.api.callback.CallbackWifiInfo;
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
//    @BindView(R2.id.iv_qrcode)
//    ImageView mSNQrcode;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_version_info;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        setTitle(R.string.my_version_title_text);

        getVersionInfo();
        getWifiInfo();

        mDevicePic.setImageResource(R.drawable.connect_t1_icon_1);
    }

    private void getWifiInfo() {
        ApiUtil.queryWifiInfo(new CallbackWifiInfo() {
            @Override
            public void onSuccess(String wifiName, String wifiPwd) {
                if (TextUtils.isEmpty(wifiName))
                    return;
                String type = "";
                if (wifiName.startsWith("Goluk_T4U")) {
                    type = "T4U";
                } else if (wifiName.startsWith("Goluk_T4")) {
                    type = "T4";
                } else if (wifiName.startsWith("Goluk_T2SU")) {
                    type = "T2SU";
                } else if (wifiName.startsWith("Goluk_T2S")) {
                    type = "T2S";
                }
                mDeviceModel.setText(getString(R.string.str_goluk) + type);
            }

            @Override
            public void onFail() {
            }
        });
    }

    private void getVersionInfo() {
        ApiUtil.getVersion(new CallbackVersion() {
            @Override
            public void onSuccess(String version) {
                mDeviceVersion.setText(version);
            }

            @Override
            public void onFail() {
            }
        });
        ApiUtil.getSN(new CallbackVersion() {
            @Override
            public void onSuccess(String sn) {
                mDeviceModel.setText(getString(R.string.str_goluk) + "T2S");
                mDeviceId.setText(sn);

                //mSNQrcode.setImageBitmap(QRCodeEncoder.creatBarcode(sn, 800,300));

            }

            @Override
            public void onFail() {
            }
        });
    }

}
