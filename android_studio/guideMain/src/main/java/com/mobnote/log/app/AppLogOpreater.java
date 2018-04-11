package com.mobnote.log.app;

import java.io.File;

/**
 * App log oprater
 */
public interface AppLogOpreater {

    /**
     * 删除多余的日志文件(只保留最近7天)
     */
    void deleteSurplusLogFile();

    /**
     * 压缩日志文件
     *
     * @return
     */
    File zipLogFiles();

    /**
     * 上传日志
     */
    void uploadLogFile(CallbackLogUpload callback);

    /**
     * 上传Listener
     */
    interface CallbackLogUpload {

        void onNoLogFileFound();

        void onUploadSuccess();

        void onUploadFailed();
    }

}
