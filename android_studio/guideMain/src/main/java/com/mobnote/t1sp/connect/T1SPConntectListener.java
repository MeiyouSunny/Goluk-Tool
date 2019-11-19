package com.mobnote.t1sp.connect;

/**
 * T1SP 连接状态回调
 */
public interface T1SPConntectListener {

    /**
     * 断开连接
     */
    void onT1SPDisconnected();

    /**
     * 开始连接
     */
    void onT1SPConnectStart();

    /**
     * 连接结果
     *
     * @param success 成功/失败
     */
    void onT1SPConnectResult(boolean success);

}
