package com.mobnote.t1sp.download2;

/**
 * Created by HuangJW on 2019/3/5 10:19.
 * Mail: 499655607@qq.com
 * Powered by Goluk
 */
public interface IpcDownloadListener {

    /**
     * 更新下载个数
     *
     * @param currentDownload 当前正在下载第几个
     * @param total           总个数
     */
    void onDownloadCountUpdate(int currentDownload, int total);

    /**
     * 下载进度更新
     *
     * @param fileName 正在下载的文件名
     * @param progress 下载进度
     */
    void onProgressUpdate(String fileName, int progress);

    /**
     * 当前下载的文件结果
     *
     * @param fileName  文件名
     * @param isSuccess 是否成功
     * @param msg       错误信息
     */
    void onSingleFileDownloadResult(String fileName, boolean isSuccess, String msg);

    /**
     * 所有文件都下载完成
     *
     * @param countSuccess 下载成功个数
     * @param countfailed  下载失败个数
     * @param countTotal   总个数
     */
    void onDownloadedComplete(int countSuccess, int countfailed, int countTotal);

    /**
     * 下载过程中SD卡不足
     *
     * @param countSuccess 下载成功个数
     * @param countfailed  下载失败个数
     * @param countTotal   总个数
     */
    void onSDNoEnoughError(int countSuccess, int countfailed, int countTotal);

}
