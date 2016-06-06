package com.goluk.ipcsdk.listener;

import com.goluk.ipcsdk.bean.DownloadInfo;
import com.goluk.ipcsdk.bean.RecordStorageState;
import com.goluk.ipcsdk.bean.FileInfo;
import com.goluk.ipcsdk.bean.VideoInfo;

import java.util.ArrayList;

/**
 * Created by hanzheng on 2016/5/27.
 */
public interface IPCFileListener {

    /**
     * @param fileList
     */
    public void callback_query_files(ArrayList<VideoInfo> fileList);

    /**
     * @param recordStorgeState
     */
    public void callback_record_storage_status(RecordStorageState recordStorgeState);

    /**
     *
     * @param fileInfo
     */
    public void callback_find_single_file(FileInfo fileInfo);

    /**
     *
     * @param downloadinfo
     */
    public void callback_download_file(DownloadInfo downloadinfo);


}
