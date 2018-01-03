package com.goluk.ipcsdk.command;
import android.content.Context;

import com.goluk.ipcsdk.bean.DownloadInfo;
import com.goluk.ipcsdk.bean.FileInfo;
import com.goluk.ipcsdk.bean.RecordStorageState;
import com.goluk.ipcsdk.bean.VideoInfo;
import com.goluk.ipcsdk.listener.IPCFileListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;
import com.goluk.ipcsdk.utils.IPCDataParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.com.mobnote.logic.GolukModule;

/**
 * Created by zenghao on 2016/5/26.
 */
public class IPCFileCommand extends BaseIPCCommand{

    private IPCFileListener ipcFileListener;

    /**
     * @see IPCFileListener
     * @param listener
     * @param cxt
     */
    public IPCFileCommand(IPCFileListener listener,Context cxt){
        super(cxt);
        ipcFileListener = listener;
    }

    /**
     * find file list
     * @see IPCFileListener
     * @param filetype   1:loop video 2:urgent video 4:wonderful video
     * @param limitCount
     * @param timestart  0:all
     * @param timeend
     * @param resform    0:automatic find  1：album find
     * @return true:send success false:fail
     * @author zenghao
     * @date 2015年3月21日
     */
    public boolean queryFileListInfo(int filetype, int limitCount, long timestart, long timeend, String resform) {
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        String queryParam = IPCDataParser.getQueryMoreFileJson(filetype, limitCount, timestart, timeend,resform);
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Query,
                queryParam);
    }


    /**
     * find SD status
     * @see IPCFileListener
     * @author zenghao
     * @date 2015年4月2日
     */
    public boolean queryRecordStorageStatus() {
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_RecPicUsage,"");
    }


    /**
     * find single file
     * @see IPCFileListener
     * @param filename
     * @return true:send success false:fail
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean querySingleFile(String filename) {
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SingleQuery,
                filename);
    }

    /**
     * dounload video file
     * @see IPCFileListener
     * @param filename
     * @param tag
     * @param savepath
     * @param filetime
     * @author zenghao
     * @date 2015年3月25日
     */
    public boolean downloadFile(String filename, String tag, String savepath, long filetime) {
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        String json = IPCDataParser.getDownFileJson(filename, tag, savepath, filetime);
        if (filename.contains(".mp4")) {
            //Log.e("","==downloadFile==json=" + json);
            //Log.e("xuhw", "YYYYYY====downloadFile=====json=" + json);
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDTPCmd_AddDownloadFile, json);
    }

    /**
     * stop download video file
     * @see IPCFileListener
     * @return true:send success false:fail
     * @author zenghao
     * @date 2015年5月19日
     */
    public boolean stopDownloadFile() {
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDTPCmd_StopDownloadFile, "");
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        switch (msg){
            case IPC_VDCPCmd_Query:
                ArrayList<VideoInfo> fileList = IPCDataParser.parseVideoListData((String) param2);
                ipcFileListener.callback_query_files(fileList);
                break;
            case IPC_VDCPCmd_RecPicUsage:
                RecordStorageState recordStorgeState = IPCDataParser.parseRecordStorageStatus((String) param2);
                ipcFileListener.callback_record_storage_status(recordStorgeState);
                break;
            case IPC_VDCPCmd_SingleQuery:
                FileInfo fileInfo = IPCDataParser.parseSingleFileResult((String) param2);
                ipcFileListener.callback_find_single_file(fileInfo);
                break;
            case IPC_VDTP_Msg_File:
                try {
                    DownloadInfo downloadInfo = null;
                    JSONObject json = new JSONObject((String) param2);
                    if(json!= null){
                        downloadInfo = new DownloadInfo();
                        downloadInfo.filename = json.optString("filename");
                        downloadInfo.filesize = json.optInt("filesize");
                        downloadInfo.filerecvsize = json.optInt("filerecvsize");
                        downloadInfo.status = param1;
                    }
                    ipcFileListener.callback_download_file(downloadInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
