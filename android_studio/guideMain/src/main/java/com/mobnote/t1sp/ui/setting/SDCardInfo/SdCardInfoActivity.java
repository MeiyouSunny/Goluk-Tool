package com.mobnote.t1sp.ui.setting.SDCardInfo;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.mobnote.eventbus.SDCardFormatEvent;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.R2;
import com.mobnote.golukmain.carrecorder.view.CustomDialog;
import com.mobnote.golukmain.carrecorder.view.CustomFormatDialog;
import com.mobnote.t1sp.api.setting.IPCConfigListener;
import com.mobnote.t1sp.api.setting.IpcConfigOption;
import com.mobnote.t1sp.api.setting.IpcConfigOptionF4;
import com.mobnote.t1sp.api.setting.SimpleIpcConfigListener;
import com.mobnote.t1sp.base.control.BindTitle;
import com.mobnote.t1sp.base.ui.BackTitleActivity;
import com.mobnote.t1sp.util.StringUtil;

import butterknife.BindView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import likly.mvp.MvpBinder;

@MvpBinder(
)
@BindTitle(R2.string.rlcx_title)
public class SdCardInfoActivity extends BackTitleActivity {

    @BindView(R2.id.mTotalSize)
    TextView mTotalSize;
    @BindView(R2.id.mLeftSize)
    TextView mUsedSize;

    private IpcConfigOption mConfigOption;
    private IPCConfigListener mConfigListener;

    @Override
    public int initLayoutResId() {
        return R.layout.activity_sdcard_info;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        setTitle(R.string.rlcx_title);

        getSDCardInfo();
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

    private void onSdFormated(boolean isFormat) {
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

    private void getSDCardInfo() {
        mConfigListener = new SimpleIpcConfigListener() {
            @Override
            public void onSDCapacityGet(double total, double free) {
                mTotalSize.setText(StringUtil.getSize(total));
                mUsedSize.setText(StringUtil.getSize(total - free));
            }

            @Override
            public void onFormatSDCardResult(boolean success) {
                onSdFormated(success);
            }
        };

        mConfigOption = new IpcConfigOptionF4(mConfigListener);
        mConfigOption.getSDCapacity();
    }

    private void formartSDCard() {
        if (mConfigOption != null) {
            mConfigOption.formatSD();
        }
    }

}
