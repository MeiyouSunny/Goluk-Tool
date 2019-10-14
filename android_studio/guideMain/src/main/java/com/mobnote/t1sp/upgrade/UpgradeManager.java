package com.mobnote.t1sp.upgrade;

import android.util.Log;

import java.io.File;

import goluk.com.t1s.api.ApiUtil;
import goluk.com.t1s.api.callback.CallbackCmd;
import goluk.com.t1s.api.callback.CallbackVersion;
import goluk.com.t1s.api.firmware.FirmwareUploader;
import goluk.com.t1s.api.firmware.UploadListener;

/**
 * T2S 固件升级Manager
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
        getUploadPath();
    }

    public void stop() {
        mBinFile = null;
        mListener = null;
        if (mUploadTask != null && !mUploadTask.isCancelled())
            mUploadTask.cancel(true);
        mUploadTask = null;
    }

    public void getUploadPath() {
        // 获取上传路径
        ApiUtil.getFirmwareUploadPath(new CallbackVersion() {
            @Override
            public void onSuccess(String uploadPath) {
                uploadFirmware(uploadPath);
            }

            @Override
            public void onFail() {
                if (mListener != null)
                    mListener.onUpgradeFinish(false);
            }
        });
    }

    /**
     * 上传固件
     */
    public void uploadFirmware(String uploadPath) {
        FirmwareUploader firmwareUploader = new FirmwareUploader();
        firmwareUploader.upload(mBinFile.getAbsolutePath(), uploadPath, new UploadListener() {
            @Override
            public void onFail() {
                Log.e("FirmwareUpload", "onFail");
                if (mListener != null)
                    mListener.onUploadUpgradeFileResult(false);
            }

            @Override
            public void onSuccess() {
                Log.e("FirmwareUpload", "onSuccess");
                if (mListener != null)
                    mListener.onUploadUpgradeFileResult(true);
                updateFirmware();
            }

            @Override
            public void onProgress(int progress) {
                Log.e("FirmwareUpload", "onProgress: " + progress);
                if (mListener != null)
                    mListener.onUploadProgress(progress);
            }
        }, null);
        if (mListener != null)
            mListener.onUploadUpgradeFileStart();
    }

    /**
     * 开始升级固件
     */
    public void updateFirmware() {
        Log.e("FirmwareUpload", "发送安装指令");
        ApiUtil.updateFirmware(new CallbackCmd() {
            @Override
            public void onSuccess(int cmd) {
                Log.e("FirmwareUpload", "固件更新成功");
                if (mListener != null)
                    mListener.onUpgradeFinish(true);
            }

            @Override
            public void onFail(int cmd, int status) {
                Log.e("FirmwareUpload", "固件更新失败");
                if (mListener != null)
                    mListener.onUpgradeFinish(false);
            }
        });
        if (mListener != null)
            mListener.onUpgradeStart(true);
    }

    public void setListener(UpgradeListener listener) {
        this.mListener = listener;
    }

}
