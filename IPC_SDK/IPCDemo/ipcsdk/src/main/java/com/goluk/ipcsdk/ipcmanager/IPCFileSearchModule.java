package com.goluk.ipcsdk.ipcmanager;

/**
 * Created by zenghao on 2016/5/26.
 */
public class IPCFileSearchModule {

    /**
     * find file list
     * @see com.goluk.ipcsdk.listener.IPCFileManagerListener
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
//        String queryParam = IpcDataParser.getQueryMoreFileJson(filetype, limitCount, timestart, timeend,resform);
//        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_Query,
//                queryParam);
        return true;
    }


    /**
     * find SD status
     * @see com.goluk.ipcsdk.listener.IPCFileManagerListener
     * @author zenghao
     * @date 2015年4月2日
     */
    public boolean queryRecordStorageStatus() {
//        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_RecPicUsage,"");
        return true;
    }


    /**
     * find single file
     * @see com.goluk.ipcsdk.listener.IPCFileManagerListener
     * @param filename
     * @return true:send success false:fail
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean querySingleFile(String filename) {
//        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SingleQuery,
//                filename);
        return true;
    }

    /**
     * dounload video file
     * @see com.goluk.ipcsdk.listener.IPCFileManagerListener
     * @param filename
     * @param tag
     * @param savepath
     * @param filetime
     * @author zenghao
     * @date 2015年3月25日
     */
    public boolean downloadFile(String filename, String tag, String savepath, long filetime) {
//        String json = JsonUtil.getDownFileJson(filename, tag, savepath, filetime);
//        if (filename.contains(".mp4")) {
//            GFileUtils.writeIPCLog("==downloadFile==json=" + json);
//            GolukDebugUtils.e("xuhw", "YYYYYY====downloadFile=====json=" + json);
//        }
//        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
//                IPC_VDTPCmd_AddDownloadFile, json);
        return true;
    }


    /**
     * stop download video file
     * @see com.goluk.ipcsdk.listener.IPCFileManagerListener
     * @return true:send success false:fail
     * @author zenghao
     * @date 2015年5月19日
     */
    public boolean stopDownloadFile() {
//        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
//                IPC_VDTPCmd_StopDownloadFile, "");
        return true;
    }

}
