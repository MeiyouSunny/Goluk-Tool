package com.mobnote.t1sp.listener;

/**
 * 监听格式化SD卡和固件升级的UDP信息返回
 */
public interface OnSettingsListener {
    /**
     * 格式化SD卡回调
     *
     * @param isFormat 返回结果
     */
    void onSdFormat(boolean isFormat);

    /**
     * 更新固件回调
     *
     * @param isUpdate 返回结果
     */
    void onUpdateFw(boolean isUpdate);
}
