package com.mobnote.t1sp.upgrade;

import com.mobnote.t1sp.api.ApiUtil;
import com.mobnote.t1sp.api.ParamsBuilder;
import com.mobnote.t1sp.callback.CommonCallback;
import com.mobnote.t1sp.listener.OnSettingsListener;
import com.mobnote.t1sp.service.T1SPUdpService;

import java.io.File;

/**
 * T1SP 固件升级Manager
 */
public class UpgradeManager {

    private File mBinFile;
    private UploadTask mUploadTask;
    private UpgradeListener mListener;

    public UpgradeManager(File binFile) {
        mBinFile = binFile;
    }

    /**
     * 开始升级流程
     */
    public void start() {
//        enterUpgradeMode();
        getFWInfo();
    }

    public void stop() {
        mBinFile = null;
        mListener = null;
        if (mUploadTask != null && !mUploadTask.isCancelled())
            mUploadTask.cancel(true);
        mUploadTask = null;
    }

    private void getFWInfo() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.getDeviceInfoParam(), new CommonCallback() {
            @Override
            protected void onSuccess() {
                enterUpgradeMode();
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                if (mListener != null)
                    mListener.onEnterUpgradeMode(false);
            }
        });
    }

    /**
     * 进入固件升级模式
     */
    private void enterUpgradeMode() {
        ApiUtil.apiServiceAit().sendRequest(ParamsBuilder.enterUpdaetModeParam(), new CommonCallback() {
            @Override
            protected void onSuccess() {
                if (mListener != null)
                    mListener.onEnterUpgradeMode(true);
                // 进入固件升级模式,开始上传固件
                uploadUpgradeFile(mBinFile);
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                if (mListener != null)
                    mListener.onEnterUpgradeMode(false);
            }
        });
    }

    /**
     * 上传固件到设备
     */
    private void uploadUpgradeFile(File binFile) {
        mUploadTask = new UploadTask();
        mUploadTask.setListener(new UploadTask.UploadListener() {
            @Override
            public void onUploaded(boolean success) {
                if (mListener != null)
                    mListener.onUploadUpgradeFileResult(success);
                // 固件上传完成,开始升级固件
                if (success)
                    startUpgrade();
            }

            @Override
            public void onUploadProgress(int progress) {
                if (mListener != null)
                    mListener.onUploadProgress(progress);
            }
        });
        mUploadTask.execute(binFile);
        if (mListener != null)
            mListener.onUploadUpgradeFileStart();
    }

    /**
     * 开始升级固件
     */
    private void startUpgrade() {
        ApiUtil.apiServiceAit().updateFirmware(new CommonCallback() {
            @Override
            protected void onSuccess() {
                if (mListener != null) {
                    mListener.onUpgradeStart(true);
                    // UDP监听固件升级回调
                    T1SPUdpService.setSetListener(mUdpListener);
                }
            }

            @Override
            protected void onServerError(int errorCode, String errorMessage) {
                if (mListener != null)
                    mListener.onUpgradeStart(false);
            }
        });
    }

    public void setListener(UpgradeListener listener) {
        this.mListener = listener;
    }

    // UDP监听
    private OnSettingsListener mUdpListener = new OnSettingsListener() {
        @Override
        public void onSdFormat(boolean isFormat) {
        }

        @Override
        public void onUpdateFw(boolean isUpdate) {
            if (mListener != null)
                mListener.onUpgradeFinish(isUpdate);
        }
    };

}
