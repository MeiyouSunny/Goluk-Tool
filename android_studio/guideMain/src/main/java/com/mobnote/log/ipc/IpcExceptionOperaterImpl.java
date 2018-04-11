package com.mobnote.log.ipc;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.userlogin.UploadUtil;
import com.mobnote.map.LngLat;
import com.mobnote.util.SharedPrefUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.Tapi;

public class IpcExceptionOperaterImpl implements IpcExceptionOperater, IPCManagerFn {

    /* 上传URL */
    private static String UPLOAD_URL = HttpManager.getInstance().getWebDirectHost() + "/cdcAdmin/deviceLog.htm";
    /* 日志保存目录 */
    private static final String LOG_DIR = Environment.getExternalStorageDirectory()
            + File.separator + "goluk" + File.separator + "IpcLog";
    /* 日志压缩文件名 */
    private static final String LOG_ZIP_NAME = "IpcException.zip";
    /* 获取Exception条数 */
    private static final int COUNT = 1000;

    private Context mContext;
    private GolukApplication mApp;
    private IPCControlManager mIpcManager;

    public IpcExceptionOperaterImpl(Context context) {
        mContext = context;
        mApp = GolukApplication.getInstance();
        mIpcManager = mApp.getIPCControlManager();
        init();
    }

    private void init() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists())
            logDir.mkdirs();
    }

    @Override
    public void getIpcExceptionList() {
        mIpcManager.addIPCManagerListener("IpcException", this);

        int lastExceptionId = getLastExceptionId(getCurrentIpcId());
        mIpcManager.getExceptionList(lastExceptionId, COUNT);
    }

    @Override
    public void saveIpcException(final List<ExceptionBean> list, final File logFile) {
        new Thread() {
            @Override
            public void run() {
                final String data = JSON.toJSONString(list) + "\n\n";
                FileWriter fileWriter = new FileWriter();
                fileWriter.writeDataToFile(data, logFile);
            }
        }.start();

    }

    @Override
    public void saveLastExceptionId(String ipcId, String lastExceptionId) {
        IpcExceptionId ipcException = new IpcExceptionId(ipcId, Integer.parseInt(lastExceptionId));

        List<IpcExceptionId> ipcExceptionIds;
        String json = SharedPrefUtil.getIpcExceptionInfo();
        if (TextUtils.isEmpty(json)) {
            ipcExceptionIds = new ArrayList<>();
            ipcExceptionIds.add(ipcException);
        } else {
            ipcExceptionIds = JSON.parseArray(json, IpcExceptionId.class);
            boolean existAlready = false;
            for (IpcExceptionId ipcExceptionId : ipcExceptionIds) {
                if (TextUtils.equals(ipcId, ipcExceptionId.ipcId)) {
                    ipcExceptionId.lastExceptionId = Integer.parseInt(lastExceptionId);
                    existAlready = true;
                    break;
                }
            }
            if (!existAlready) {
                ipcExceptionIds.add(ipcException);
            }
        }

        json = JSON.toJSONString(ipcExceptionIds);
        SharedPrefUtil.saveIpcExceptionInfo(json);
    }

    @Override
    public int getLastExceptionId(String ipcId) {
        String json = SharedPrefUtil.getIpcExceptionInfo();
        if (TextUtils.isEmpty(json)) {
            return 0;
        } else {
            List<IpcExceptionId> ipcExceptionIds = JSON.parseArray(json, IpcExceptionId.class);
            if (ipcExceptionIds != null && !ipcExceptionIds.isEmpty()) {
                for (IpcExceptionId exceptionId : ipcExceptionIds) {
                    if (TextUtils.equals(ipcId, exceptionId.ipcId))
                        return exceptionId.lastExceptionId;
                }
            }
        }

        return 0;
    }

    @Override
    public File zipExceptionFiles() {
        File logDir = new File(LOG_DIR);
        File logZipFile = new File(logDir, LOG_ZIP_NAME);
        if (logZipFile.exists())
            logZipFile.delete();

        ZipUtil zipUtil = new ZipUtil();
        zipUtil.zipFolder(logDir, logZipFile);

        return logZipFile;
    }

    @Override
    public String getCurrentIpcId() {
        return mIpcManager.mProduceName + "-" + mIpcManager.mDeviceSn + "-" + SharedPrefUtil.getIPCVersion();
    }

    @Override
    public File getCurrentIpcLogFile() {
        String ipcId = getCurrentIpcId() + ".txt";
        File logDir = new File(LOG_DIR);
        File ipcLogFile = new File(logDir, ipcId);
        return ipcLogFile;
    }

    @Override
    public void uploadExceptionFile() {
        // 没有日志文件
        final File logDir = new File(LOG_DIR);
        if (!logDir.exists() || logDir.listFiles() == null)
            return;

        new Thread() {
            @Override
            public void run() {
                // 压缩
                File logZipFile = zipExceptionFiles();
                // 上传
                Map<String, String> requestParams = getRequestParams();
                String result = null;
                try {
                    result = UploadUtil.uploadFile(UPLOAD_URL, requestParams, logZipFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!TextUtils.isEmpty(result)) {
                    JSONObject resultJson = JSON.parseObject(result);
                    if (resultJson != null) {
                        String code = resultJson.getString("code");
                        if (!TextUtils.isEmpty(code) && "0".equals(code)) {
                            // 上传成功,删除日志文件
                            for (File logFile : logDir.listFiles()) {
                                logFile.delete();
                            }
                        }
                    }
                }

                logZipFile.delete();
            }
        }.start();
    }

    /**
     * 获取请求参数
     */
    private Map<String, String> getRequestParams() {
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("xieyi", "100");
        GolukApplication app = GolukApplication.getInstance();
        if (app.mGoluk != null) {
            String verName = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
                    IPageNotifyFn.PageType_GetVersion, "fs6:/version");
            requestParams.put("commappversion", verName);
        } else {
            requestParams.put("commappversion", "");
        }
        requestParams.put("commhdtype", SharedPrefUtil.getIpcModel());
        requestParams.put("ipcversion", SharedPrefUtil.getIPCVersion());
        requestParams.put("commlat", "" + LngLat.lat);
        requestParams.put("commlon", "" + LngLat.lng);
        requestParams.put("commmid", "" + Tapi.getMobileId());
        requestParams.put("commostag", "android");
        String uid = GolukApplication.getInstance().mCurrentUId;
        if (TextUtils.isEmpty(uid)) {
            requestParams.put("commuid", "");
        } else {
            requestParams.put("commuid", uid);
        }

        return requestParams;
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (msg == IPC_VDCP_Msg_GetExceptionList) {
            final String data = (String) param2;

            List<ExceptionBean> list = JSON.parseArray(data, ExceptionBean.class);
            if (list != null && !list.isEmpty()) {
                // 保存Exception ID
                ExceptionBean exception = list.get(list.size() - 1);
                saveLastExceptionId(getCurrentIpcId(), exception.id);
                // 保存到本地文件
                saveIpcException(list, getCurrentIpcLogFile());
            }

            GolukApplication.getInstance().getIPCControlManager().removeIPCManagerListener("IpcException");
        }
    }

}
