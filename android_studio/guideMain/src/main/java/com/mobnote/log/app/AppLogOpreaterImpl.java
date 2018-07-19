package com.mobnote.log.app;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.userlogin.UploadUtil;
import com.mobnote.log.ipc.ZipUtil;
import com.mobnote.map.LngLat;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.api.Tapi;

public class AppLogOpreaterImpl implements AppLogOpreater {

    /* 上传URL */
    private static String UPLOAD_URL = HttpManager.getInstance().getWebDirectHost() + "/cdcAdmin/log.htm";
    /* 日志压缩文件名 */
    private static final String LOG_ZIP_NAME = "AppLog.zip";

    private File mAppLogDirFile;

    private CallbackLogUpload mCallback;

    public AppLogOpreaterImpl() {
        final String mAppLogDir = Environment.getExternalStorageDirectory() + File.separator + GolukFileUtils.GOLUK_LOG_PATH;
        mAppLogDirFile = new File(mAppLogDir);
    }

    @Override
    public void deleteSurplusLogFile() {
        if (!mAppLogDirFile.exists())
            return;

        File[] logFiles = mAppLogDirFile.listFiles();
        if (logFiles == null || logFiles.length < 7)
            return;

        // 按文件最后修改时间倒序排序
        Arrays.sort(logFiles, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return (int) (lhs.lastModified() - rhs.lastModified());
            }
        });
        // 删除最早的
        logFiles[0].delete();
    }

    @Override
    public File zipLogFiles() {
        File logZipFile = new File(mAppLogDirFile, LOG_ZIP_NAME);
        if (logZipFile.exists())
            logZipFile.delete();

        ZipUtil zipUtil = new ZipUtil();
        zipUtil.zipFolder(mAppLogDirFile, logZipFile);

        return logZipFile;
    }

    @Override
    public void uploadLogFile(CallbackLogUpload callback) {
        mCallback = callback;
        if (!mAppLogDirFile.exists() || mAppLogDirFile.listFiles() == null || mAppLogDirFile.listFiles().length <= 0) {
            if (mCallback != null)
                mCallback.onNoLogFileFound();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                // 压缩
                File logZipFile = zipLogFiles();
                // 上传
                Map<String, String> requestParams = getRequestParams();
                String result = null;
                try {
                    result = UploadUtil.uploadFile(UPLOAD_URL, requestParams, logZipFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                boolean success = parseUploadResult(result);
                if (success) {
                    // 上传成功,删除日志文件
                    if (mAppLogDirFile.listFiles() != null) {
                        for (File logFile : mAppLogDirFile.listFiles()) {
                            logFile.delete();
                        }
                        mUiHandler.sendEmptyMessage(MSG_TYPE_UPLOAD_LOG_SUCCESS);
                    }
                } else {
                    mUiHandler.sendEmptyMessage(MSG_TYPE_UPLOAD_LOG_FAILED);
                }

                logZipFile.delete();
            }
        }.start();
    }

    /**
     * 解析返回结果
     */
    private boolean parseUploadResult(String result) {
        if (TextUtils.isEmpty(result))
            return false;

        JSONObject resultJson = JSON.parseObject(result);
        if (resultJson == null)
            return false;

        String code = resultJson.getString("code");
        return !TextUtils.isEmpty(code) && "0".equals(code);
    }

    private static final int MSG_TYPE_UPLOAD_LOG_SUCCESS = 0;
    private static final int MSG_TYPE_UPLOAD_LOG_FAILED = 1;
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TYPE_UPLOAD_LOG_SUCCESS:
                    if (mCallback != null)
                        mCallback.onUploadSuccess();
                    break;
                case MSG_TYPE_UPLOAD_LOG_FAILED:
                    if (mCallback != null)
                        mCallback.onUploadFailed();
                    break;
            }
        }
    };

    /**
     * 获取请求参数
     */
    private Map<String, String> getRequestParams() {
        Map<String, String> requestParams = new HashMap<String, String>();
        requestParams.put("xieyi", "200");
        GolukApplication app = GolukApplication.getInstance();
        if (app.mGoluk != null) {
            String verName = GolukApplication.getInstance().mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage,
                    IPageNotifyFn.PageType_GetVersion, "fs6:/version");
            requestParams.put("commappversion", verName);
        } else {
            requestParams.put("commappversion", "");
        }
        requestParams.put("commhdtype", SharedPrefUtil.getIpcModel());
        requestParams.put("commipcversion", SharedPrefUtil.getIPCVersion());
        requestParams.put("commdevmodel", android.os.Build.MODEL);
        requestParams.put("commlat", "" + LngLat.lat);
        requestParams.put("commlon", "" + LngLat.lng);
        requestParams.put("commmid", "" + Tapi.getMobileId());
        requestParams.put("commostag", "android");
        requestParams.put("commosversion", android.os.Build.VERSION.RELEASE);
        requestParams.put("commticket", SharedPrefUtil.getUserToken());
        String uid = GolukApplication.getInstance().mCurrentUId;
        if (TextUtils.isEmpty(uid)) {
            requestParams.put("commuid", "");
        } else {
            requestParams.put("commuid", uid);
        }
        requestParams.put("commversion", GolukUtils.getCommversion());
        requestParams.put("commlocale", GolukUtils.getLanguageAndCountry());

        return requestParams;
    }

}
