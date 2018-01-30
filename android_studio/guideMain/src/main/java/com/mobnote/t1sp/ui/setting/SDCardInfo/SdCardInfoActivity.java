package com.mobnote.t1sp.ui.setting.SDCardInfo;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.mobnote.eventbus.SDCardFormatEvent;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomFormatDialog;
import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.base.control.BindTitle;
import com.mobnote.t1sp.base.ui.BackTitleActivity;
import com.mobnote.t1sp.bean.SettingInfo;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.callback.SettingInfosCallback;
import com.mobnote.t1sp.listener.OnSettingsListener;
import com.mobnote.t1sp.service.T1SPUdpService;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import likly.mvp.MvpBinder;

@MvpBinder(
)
@BindTitle(R2.string.rlcx_title)
public class SdCardInfoActivity extends BackTitleActivity implements OnSettingsListener {

    @BindView(R2.id.mTotalSize)
    TextView mTotalSize;
    @BindView(R2.id.mLeftSize)
    TextView mLeftSize;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_sdcard_info;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();

        getSDCardInfo();

        // 设置UDP监听
        T1SPUdpService.setSetListener(this);
    }

    @OnClick(R2.id.mFormatSDCard)
    public void viewClick(View view) {
        if (view.getId() == R.id.mFormatSDCard) {
            showConfirmDialog();
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

                        mFormatDialog = new CustomFormatDialog(SdCardInfoActivity.this);
                        mFormatDialog.setCancelable(false);
                        mFormatDialog.setMessage(getResources().getString(R.string.str_carrecorder_storage_format_sdcard_formating));
                        mFormatDialog.show();
                    }
                });
        confirmDialog.setRightButton(
                this.getResources().getString(R.string.str_carrecorder_storage_format_sdcard_dialog_no), null);
        confirmDialog.show();
    }

    private void onGetSDCardInfo(String sdCardInfo) {
        if (TextUtils.isEmpty(sdCardInfo))
            return;

        String[] infos = sdCardInfo.split("/");
        if (infos != null || infos.length >= 2) {
            mTotalSize.setText(infos[1]);
            mLeftSize.setText(infos[0]);
        }
    }

    @Override
    public void onSdFormat(boolean isFormat) {
        // 取消进度提示框
        if (null != mFormatDialog && mFormatDialog.isShowing())
            mFormatDialog.dismiss();

        // 刷新数据
        if (isFormat)
            getSDCardInfo();

        // 显示结果提示框
        String message = getString(isFormat ? R.string.str_carrecorder_storage_format_sdcard_success : R.string.str_carrecorder_storage_format_sdcard_fail);
        CustomDialog confirmDialog = new CustomDialog(this);
        confirmDialog.setMessage(message, Gravity.CENTER);
        confirmDialog.setLeftButton(getString(R.string.user_repwd_ok), null);
        confirmDialog.show();

        // Event
        EventBus.getDefault().post(new SDCardFormatEvent());
    }

    @Override
    public void onUpdateFw(boolean isUpdate) {
    }

    private void getSDCardInfo() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getSettingInfoParam(), new SettingInfosCallback() {
            @Override
            public void onGetSettingInfos(SettingInfo settingInfo) {
                onGetSDCardInfo(settingInfo.SDCardInfo);
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
            }
        });
    }

    private void formartSDCard() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.formatSdCardParam(), new CommonCallback() {
            @Override
            protected void onSuccess() {
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                onSdFormat(false);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销UDP监听
        T1SPUdpService.setSetListener(null);
    }

}
