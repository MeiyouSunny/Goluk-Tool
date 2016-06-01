package com.goluk.ipcsdk.ipcCommond;
import android.util.Log;
import android.content.Context;

import com.goluk.ipcsdk.listener.IPCFileListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;
import com.goluk.ipcsdk.utils.IpcDataParser;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

/**
 * Created by zenghao on 2016/5/26.
 */
public class IPCFileCommand extends BaseIPCCommand{

    /**
     *
     * @param listener
     */
    public IPCFileCommand(IPCFileListener listener,Context cxt){
        super(cxt);

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
        String queryParam = IpcDataParser.getQueryMoreFileJson(filetype, limitCount, timestart, timeend,resform);
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPCManagerFn.IPC_VDCPCmd_Query,
                queryParam);
    }


    /**
     * find SD status
     * @see IPCFileListener
     * @author zenghao
     * @date 2015年4月2日
     */
    public boolean queryRecordStorageStatus() {
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPCManagerFn.IPC_VDCPCmd_RecPicUsage,"");
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
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPCManagerFn.IPC_VDCPCmd_SingleQuery,
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
        String json = IpcDataParser.getDownFileJson(filename, tag, savepath, filetime);
        if (filename.contains(".mp4")) {
            Log.e("","==downloadFile==json=" + json);
            Log.e("xuhw", "YYYYYY====downloadFile=====json=" + json);
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDTPCmd_AddDownloadFile, json);
    }


    /**
     * stop download video file
     * @see IPCFileListener
     * @return true:send success false:fail
     * @author zenghao
     * @date 2015年5月19日
     */
    public boolean stopDownloadFile() {
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDTPCmd_StopDownloadFile, "");
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {

    }
}
