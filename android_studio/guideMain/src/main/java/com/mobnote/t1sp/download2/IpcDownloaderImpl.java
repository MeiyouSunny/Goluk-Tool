package com.mobnote.t1sp.download2;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.t1sp.util.GolukUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuangJW on 2019/3/5 10:58.
 * Mail: 499655607@qq.com
 * Powered by Goluk
 */
public class IpcDownloaderImpl implements IpcDownloader {

    private List<VideoInfo> mFileNames;
    // 当前下载第几个
    private int mCurrentIndex;
    // 已经下载完成的数量
    private int mCountDownloaded;
    private String mCurrentFileName;
    private String mCurrentSavaPath;
    private IpcDownloadListener mListener;

    private boolean mNeedCheckSD;
    // 是否同时下载封面
    private boolean mNeedDownloadThumb;

    private static IpcDownloaderImpl instance;

    private IpcDownloaderImpl() {
        mFileNames = new ArrayList<>();
    }

    public static IpcDownloaderImpl getInstance() {
        if (instance == null)
            instance = new IpcDownloaderImpl();
        return instance;
    }

    @Override
    public void addDownloadFile(VideoInfo fileName) {
        if (mFileNames == null)
            mFileNames = new ArrayList<>();
        if (mFileNames != null) {
            mFileNames.add(fileName);
        }
    }

    @Override
    public void addDownloadFileList(List<VideoInfo> fileNames) {
        if (mFileNames == null)
            mFileNames = new ArrayList<>();

        for (VideoInfo videoInfo : fileNames) {
            if (!mFileNames.contains(videoInfo))
                mFileNames.add(videoInfo);
        }

//        if (mFileNames != null) {
//            mFileNames.addAll(fileNames);
//        }
    }

    @Override
    public List<VideoInfo> getDownloadingFiles() {
        return mFileNames;
    }

    @Override
    public void setListener(IpcDownloadListener listener) {
        mListener = listener;
    }

    @Override
    public void start() {
        if (GolukUtils.isEmpty(mFileNames))
            return;
        startNextTask();
    }

    @Override
    public void start(boolean mNeedDownloadThumb) {
        mNeedDownloadThumb = false;
        start();
    }

    private void startNextTask() {
        if (GolukUtils.isEmpty(mFileNames))
            return;

        if (mCurrentIndex > (mFileNames.size() - 1)) {
            // 都下载完成
            if (mListener != null) {
                int totalCount = mFileNames.size();
                mListener.onDownloadedComplete(mCountDownloaded, totalCount - mCountDownloaded, totalCount);
                resetValues();
            }
            return;
        }

        mNeedCheckSD = true;

        mCurrentFileName = mFileNames.get(mCurrentIndex).filename;
        VideoInfo videoInfo = mFileNames.get(mCurrentIndex);
        mCurrentSavaPath = GolukUtils.getVideoSavePath(mCurrentFileName);

        FileDownloader.getImpl().create(videoInfo.videoUrl).setPath(mCurrentSavaPath).setCallbackProgressMinInterval(1).setListener(mTaskListener).start();
        // 同时下载视频封面
        if (mNeedDownloadThumb) {
            String thumbSavePath = GolukUtils.getThumbSavePath(mCurrentFileName);
            FileDownloader.getImpl().create(videoInfo.thumbUrl).setPath(thumbSavePath).start();
        }

        if (mListener != null)
            mListener.onDownloadCountUpdate(mCurrentIndex + 1, mFileNames.size());
    }

    private void resetValues() {
        mCurrentIndex = 0;
        mCountDownloaded = 0;
        mCurrentFileName = "";
        if (mFileNames != null) {
            mFileNames.clear();
            mFileNames = null;
        }
    }

    private SimpleDownloadListener mTaskListener = new SimpleDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            if (mNeedCheckSD) {
                // 先判断SD卡剩余空间是否足够
                judgeIsSDEnough(totalBytes);
                mNeedCheckSD = false;
            }

            int percent = Math.round((float) soFarBytes / (float) totalBytes * 100);
            if (mListener != null)
                mListener.onProgressUpdate(mCurrentFileName, percent);
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            if (mListener != null)
                mListener.onSingleFileDownloadResult(mCurrentFileName, true, "");
            mCurrentIndex++;
            mCountDownloaded++;
            startNextTask();

            mediaScan(task.getPath());
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            if (mListener != null)
                mListener.onSingleFileDownloadResult(mCurrentFileName, false, e.getMessage());
            mCurrentIndex++;
            startNextTask();
        }
    };

    /**
     * 判断SD是否有足够的空间
     *
     * @param sizeNeed 需要的大小
     */
    private void judgeIsSDEnough(long sizeNeed) {

        boolean isSDEnough = GolukUtils.isSDEnough(sizeNeed);
        if (!isSDEnough) {
            // 剩余空间不足
            if (mListener != null) {
                int totalCount = mFileNames.size();
                mListener.onSDNoEnoughError(mCountDownloaded, totalCount - mCountDownloaded, totalCount);
                resetValues();
            }
            // 取消下载
            cancel();
        }
    }

    @Override
    public void cancel() {
        FileDownloader.getImpl().pauseAll();
        resetValues();
        // 删除当前下载的临时文件
        File file = new File(mCurrentSavaPath + ".temp");
        if (file != null && file.exists())
            file.delete();
    }

    private void mediaScan(String filePath) {
        MediaScannerConnection.scanFile(GolukApplication.getInstance().getApplicationContext(),
                new String[]{filePath}, new String[]{"video/mp4"},
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e("MediaScan", "onScanCompleted " + path + " : " + uri);
                    }
                });
    }

}
