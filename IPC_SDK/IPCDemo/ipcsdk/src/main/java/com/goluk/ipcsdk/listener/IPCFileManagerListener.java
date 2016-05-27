package com.goluk.ipcsdk.listener;

import com.goluk.ipcsdk.bean.DownloadInfo;
import com.goluk.ipcsdk.bean.RecordStorgeState;
import com.goluk.ipcsdk.bean.VideoFileInfo;
import com.goluk.ipcsdk.bean.VideoInfo;

import java.util.ArrayList;

/**
 * Created by hanzheng on 2016/5/27.
 */
public interface IPCFileManagerListener {

    /**
     * @param fileList
     */
    public void callback_query_files(ArrayList<VideoInfo> fileList);

    /**
     * @param recordStorgeState
     */
    public void callback_record_storage_status(RecordStorgeState recordStorgeState);

    /**
     *
     * @param fileInfo
     */
    public void callback_find_single_file(VideoFileInfo fileInfo);

    /**
     *
     * @param downloadinfo
     */
    public void callback_download_file(DownloadInfo downloadinfo);


}
