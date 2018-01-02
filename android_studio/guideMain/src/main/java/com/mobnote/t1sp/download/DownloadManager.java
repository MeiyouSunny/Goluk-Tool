package com.mobnote.t1sp.download;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.squareup.okhttp.OkHttpClient;

import org.succlz123.okdownload.DatabaseHelp;
import org.succlz123.okdownload.OkDownloadCancelListener;
import org.succlz123.okdownload.OkDownloadEnqueueListener;
import org.succlz123.okdownload.OkDownloadError;
import org.succlz123.okdownload.OkDownloadRequest;

import java.io.File;
import java.util.List;

/**
 * T1SP下载任务管理
 */

public class DownloadManager {
    private static final String TAG = "DownloadManager";

    private static DownloadManager sInstance;
    private Context mContext;
    private OkHttpClient mOkHttpClient;
    private DatabaseHelp mDatabaseHelp;
    private OkDownloadRequest mOkDownloadRequest;
    private OkDownloadEnqueueListener mOkDownloadEnqueueListener;
    private OkDownloadTask mOkDownloadTask;

    private DownloadManager() {
    }

    private DownloadManager(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
            mDatabaseHelp = DatabaseHelp.getInstance(mContext);
        }
    }

    public static DownloadManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    return sInstance = new DownloadManager(context);
                }
            }
        }
        return sInstance;
    }

    public void enqueue(OkDownloadRequest okDownloadRequest) {
        enqueue(okDownloadRequest, null);
    }

    public void enqueue(OkDownloadRequest okDownloadRequest, OkDownloadEnqueueListener okDownloadEnqueueListener) {
        if (okDownloadRequest.getOkHttpClient() != null) {
            mOkHttpClient = okDownloadRequest.getOkHttpClient();
        }

        if (okDownloadRequest == null || okDownloadEnqueueListener == null) {
            return;
        }

        mOkDownloadRequest = okDownloadRequest;
        mOkDownloadEnqueueListener = okDownloadEnqueueListener;

        if (!isRequestValid()) {
            return;
        }

        onStart(mOkDownloadRequest, mOkDownloadEnqueueListener);
    }

    public void onStart(OkDownloadRequest okDownloadRequest, OkDownloadEnqueueListener listener) {
        if (!isUrlValid(okDownloadRequest.getUrl())) {
            mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_URL_OR_FILEPATH_IS_NOT_VALID));
            return;
        }

        if (mOkDownloadTask == null) {
            mOkDownloadTask = new OkDownloadTask(mContext, mOkHttpClient, mDatabaseHelp);
        }
        mOkDownloadTask.start(okDownloadRequest, mOkDownloadEnqueueListener);
    }

    public void onPause(OkDownloadRequest okDownloadRequest, OkDownloadEnqueueListener listener) {
        if (!isUrlValid(okDownloadRequest.getUrl())) {
            mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_URL_OR_FILEPATH_IS_NOT_VALID));
            return;
        }

        if (mOkDownloadTask == null) {
            mOkDownloadTask = new OkDownloadTask(mContext, mOkHttpClient, mDatabaseHelp);
        }

        mOkDownloadTask.pause(okDownloadRequest, listener);
    }

    public void onCancel(String url, OkDownloadCancelListener listener) {
        if (!isUrlValid(url)) {
            mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_URL_OR_FILEPATH_IS_NOT_VALID));
            return;
        }

        if (mOkDownloadTask == null) {
            mOkDownloadTask = new OkDownloadTask(mContext, mOkHttpClient, mDatabaseHelp);
        }

        mOkDownloadTask.cancel(url, listener);
    }

    public List<OkDownloadRequest> queryAll() {
        return mDatabaseHelp.execQueryAll();
    }

    public List<OkDownloadRequest> queryById(int id) {
        return mDatabaseHelp.execQuery("id", String.valueOf(id));
    }

    private boolean isRequestValid() {
        String url = mOkDownloadRequest.getUrl();
        String filePath = mOkDownloadRequest.getFilePath();

        if (!isRequestComplete(url, filePath) || !isUrlValid(url)) {
            mOkDownloadEnqueueListener.onError(new OkDownloadError(OkDownloadError.DOWNLOAD_URL_OR_FILEPATH_IS_NOT_VALID));
            return false;
        }

        return true;
    }

    private boolean isRequestComplete(String url, String filePath) {
        return !TextUtils.isEmpty(url) && !TextUtils.isEmpty(filePath);
    }

    private boolean isUrlValid(String url) {
        return URLUtil.isNetworkUrl(url);
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public static boolean hasSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableExternalMemorySize() {
        if (hasSDCard()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    public static long getTotalExternalMemorySize() {
        if (hasSDCard()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }
}
