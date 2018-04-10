package com.mobnote.golukmain.ipclog;

import java.io.File;
import java.util.List;

/**
 * IPC 日志操作
 */
public interface IpcExceptionOperater {

    /**
     * 获取IPC异常列表
     */
    void getIpcExceptionList();

    /**
     * 写入IPC日志
     *
     * @param list
     * @param logFile
     */
    void saveIpcException(List<ExceptionBean> list, File logFile);

    /**
     * 保存指定IPCUI周调Exception的ID
     *
     * @param ipcId
     * @param lastExceptionId
     */
    void saveLastExceptionId(String ipcId, String lastExceptionId);

    /**
     * 获取指定IPC最后一条Exception的ID
     *
     * @param ipcId
     * @return
     */
    int getLastExceptionId(String ipcId);

    /**
     * 压缩日志文件
     *
     * @return
     */
    File zipExceptionFiles();

    /**
     * 上传日志
     */
    void uploadExceptionFile();

    /**
     * 获取当前IPC唯一标识符
     */
    String getCurrentIpcId();

    /**
     * 获取根据当前IPC对应的日志文件
     */
    File getCurrentIpcLogFile();

}
