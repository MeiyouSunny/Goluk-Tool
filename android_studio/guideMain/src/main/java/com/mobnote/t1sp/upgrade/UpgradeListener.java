package com.mobnote.t1sp.upgrade;

/**
 * T1SP固件升级状态Listener
 */
public interface UpgradeListener {

    /**
     * 是否成功进入固件升级模式
     *
     * @param success 是否成功
     */
    void onEnterUpgradeMode(boolean success);

    /**
     * 开始上传固件到设备
     */
    void onUploadUpgradeFileStart();

    /**
     * 上传进度
     *
     * @param progress 进度
     */
    void onUploadProgress(int progress);

    /**
     * 上传固件到设备是否成功
     *
     * @param success 是否成功
     */
    void onUploadUpgradeFileResult(boolean success);

    /**
     * 开始固件升级
     *
     * @param success 发起固件升级指令是否成功
     */
    void onUpgradeStart(boolean success);

    /**
     * 开始固件升级
     *
     * @param success 是否成功
     */
    void onUpgradeFinish(boolean success);

}
