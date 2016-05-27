package com.goluk.ipcsdk.ipcmanager;

/**
 * Created by zenghao on 2016/5/26.
 */
public class IPCFileSearchModule {

    /**
     * 多个文件查询列表
     *
     * @param filetype   1:循环影像 2:紧急视频 4:精彩视频
     * @param limitCount 最多查询条数
     * @param timestart  查询起始时间（0表示查询所有）
     * @param timeend    查询截止时间
     * @param resform    0:自动查询  1：相册查询
     * @return true:命令发送成功 false:失败
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
     * 查询录制存储状态
     *
     * @author xuhw
     * @date 2015年4月2日
     */
    public boolean queryRecordStorageStatus() {
//        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_RecPicUsage,"");
        return true;
    }


    /**
     * 单个文件查询
     *
     * @param filename 要查询的文件名
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年3月21日
     */
    public boolean querySingleFile(String filename) {
//        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_VDCPCmd_SingleQuery,
//                filename);
        return true;
    }

    /**
     * 下载文件
     *
     * @param filename 文件名称
     * @param tag      唯一标识
     * @param savepath 文件保存路径
     * @param filetime 视频文件录制起始时间（秒）
     * @author xuhw
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
     * 停止IPC下载所有任务
     *
     * @return
     * @author xuhw
     * @date 2015年5月19日
     */
    public boolean stopDownloadFile() {
//        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
//                IPC_VDTPCmd_StopDownloadFile, "");
        return true;
    }

}
