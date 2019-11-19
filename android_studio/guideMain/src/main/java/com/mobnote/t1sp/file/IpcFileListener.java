package com.mobnote.t1sp.file;

public interface IpcFileListener {

    /**
     * 远程文件已删除完成
     */
    void onRemoteFileDeleted(boolean success);

}
