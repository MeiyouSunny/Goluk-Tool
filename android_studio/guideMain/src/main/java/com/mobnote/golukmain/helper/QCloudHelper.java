package com.mobnote.golukmain.helper;

import com.mobnote.application.GolukApplication;
import com.tencent.cos.COSClient;
import com.tencent.cos.COSConfig;
import com.tencent.cos.model.PutObjectRequest;


/**
 * @描述 云服务请求接口实现
 * @作者 卜长清，buchangqing@goluk.com
 * @日期 2015-09-09
 * @版本 1.0
 */
public class QCloudHelper {
    private static class DefaultConfig {
        private static final String APPID = "1251538964";
        private static final String FILE_BUCKET = "pfile";
        private static final String PHOTO_BUCKET = "pphoto";
        private static final String VIDEO_BUCKET = "pvideo";
    }

    /******************* 业务配置 **************************/
    public static final String APPID = DefaultConfig.APPID;
    public static String VIDEO_SIGN = "";
    public static String VIDEO_BUCKET = DefaultConfig.VIDEO_BUCKET;
    public static String PHOTO_BUCKET = DefaultConfig.PHOTO_BUCKET;

    /******************* 通用 **************************/
    private static QCloudHelper instance = null;

    public QCloudHelper() {
        init();
    }


    /**
     * 单例支持
     *
     * @return
     */
    public static synchronized QCloudHelper getInstance() {
        if (instance == null) {
            synchronized (QCloudHelper.class) {
                if (instance == null) {
                    instance = new QCloudHelper();
                }
            }
        }
        return instance;
    }

    private static COSClient mCosClient;

    private void init() {
        COSConfig config = new COSConfig();
        config.setMaxRetryCount(5);
        config.setConnectionTimeout(2 * 60 * 1000);
        config.setSocketTimeout(2 * 60 * 1000);
        mCosClient = new COSClient(GolukApplication.getInstance(), APPID, config, "");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 云存储服务

    /**
     * 上传
     *
     * @param task
     * @return
     */
    public boolean upload(PutObjectRequest task, String region) {
        if (task == null) {
            return false;
        }
        mCosClient.getConfig().setEndPoint(region);
        mCosClient.putObjectAsyn(task);
        return true;
    }


    public boolean resume(PutObjectRequest task) {
        if (task == null) {
            return false;
        }
        mCosClient.resumeTask(task.getRequestId());
        return false;
    }

    public boolean pause(PutObjectRequest task) {
        if (task == null) {
            return false;
        }
        return mCosClient.pauseTask(task.getRequestId());
    }

    public boolean cancel(PutObjectRequest task) {
        if (task == null) {
            return false;
        }
        return mCosClient.cancelTask(task.getRequestId());
    }

}