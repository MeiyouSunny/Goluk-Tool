package com.mobnote.t1sp.ui.download;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.StringRes;
import android.util.Log;

import com.mobnote.application.GlobalWindow;
import com.mobnote.golukmain.R;
import com.mobnote.t1sp.util.CollectionUtils;

import org.succlz123.okdownload.OkDownloadEnqueueListener;
import org.succlz123.okdownload.OkDownloadError;
import org.succlz123.okdownload.OkDownloadRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * T1SP 下载管理
 */
public class DownloaderT1spImpl implements DownloaderT1sp, OkDownloadEnqueueListener {

    private static DownloaderT1spImpl mInstance;

    private static Context mContext;

    private DownloadManager mDownloadManager;
    /* 总共需要下载的列表 */
    public List<Task> mListTotal;
    /* 已经下载完成的列表 */
    public List<Task> mListDownloaded;
    /* 是否正在下载 */
    private boolean isRunning;

    private SoundPool mSoundPool;
    private int mSoundId;

    private DownloaderT1spImpl(Context context) {
        mContext = context;
        if (mListTotal == null)
            mListTotal = new ArrayList<>();
        if (mListDownloaded == null)
            mListDownloaded = new ArrayList<>();
        if (mDownloadManager == null)
            mDownloadManager = DownloadManager.getInstance(context);

        mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
//        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                mSoundId = sampleId;
//            }
//        });
        mSoundId = mSoundPool.load(mContext, R.raw.ec_alert5, 1);
    }

    public static synchronized void init(Context context) {
        if (mInstance == null) {
            mInstance = new DownloaderT1spImpl(context);
        }
    }

    public static DownloaderT1sp getInstance() {
        if (mInstance == null)
            mInstance = new DownloaderT1spImpl(mContext);

        return mInstance;
    }

    @Override
    public void addDownloadTasks(List<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks))
            return;

        mListTotal.addAll(tasks);

        if (!isRunning) {
            isRunning = true;
            updateUiDownloadCount();
            startNextTask();
        }
    }

    /**
     * 取出下一个任务并开始下载
     */
    private void startNextTask() {
        Task task = mListTotal.get(mListDownloaded.size());
        // 如果文件已经存在
        File videoFile = new File(task.savePath);
        if (videoFile.exists()) {
            checkDownloadListProgress();
        } else {
            videoFile.getParentFile().mkdirs();
            // 新建下载任务
            OkDownloadRequest request = new OkDownloadRequest.Builder()
                    .url(task.downloadPath)
                    .filePath(task.savePath)
                    .build();
            mDownloadManager.enqueue(request, this);
        }

        //updateUiDownloadCount();
    }

    @Override
    public void cancelAllDownloadTask(boolean showCancelMsg) {
        if (showCancelMsg) {
            GlobalWindow.getInstance().reset();
            GlobalWindow.getInstance().toFailed(getString(R.string.str_video_transfer_cancle));
        }
        destory();
    }

    @Override
    public void destory() {
        isRunning = false;
        if (mListTotal != null)
            mListTotal.clear();
        if (mListDownloaded != null)
            mListDownloaded.clear();
        if (mSoundPool != null) {
            //mSoundPool.release();
            //mSoundPool = null;
        }

        mInstance = null;
    }

    /**
     * 检查任务列表是否下载完成并更新UI
     */
    private void checkDownloadListProgress() {
        mListDownloaded.add(mListTotal.get(mListDownloaded.size()));
        // 判断所有下载任务是否完成
        if (mListDownloaded.size() >= mListTotal.size()) {
            // 所有任务下载完成
            GlobalWindow.getInstance().topWindowSucess(getString(R.string.str_video_transfer_success));
            playSuccessSound();
            // 结束
            destory();
        } else {
            // 开始下一个任务
            startNextTask();
            updateUiDownloadCount();
        }
    }

    /**
     * 更新下载进度
     *
     * @param progress 下载进度
     */
    private void updateProgress(int progress) {
        // 更新UI进度
        // TODO
        GlobalWindow.getInstance().refreshPercent(progress);
    }

    /**
     * 新的Task开始下载
     */
    private void newTaskStart() {
        // 更新UI进度
        // TODO
    }

    /**
     * 更新UI: 正在下载数量/总数量
     */
    private void updateUiDownloadCount() {
        final String showTxt = getString(R.string.str_video_transfer_ongoing)
                + getDownloadingCount() + getString(R.string.str_slash) + mListTotal.size();
        if (!GlobalWindow.getInstance().isShow()) {
            GlobalWindow.getInstance().createVideoUploadWindow(showTxt);
        } else {
            GlobalWindow.getInstance().updateText(showTxt);
        }
    }

    /**
     * 获取正在下载的数量
     */
    private int getDownloadingCount() {
        return mListDownloaded.size() + 1;
    }

    private String getString(@StringRes int stringId) {
        if (mContext == null)
            return "";
        return mContext.getResources().getString(stringId);
    }

    /**
     * 播放下载完成音效
     */
    private void playSuccessSound() {
        if (null != mSoundPool) {
            mSoundPool.play(mSoundId, 1, 1, 1, 0, 1);
        }
    }

    private static final int MSG_TYPE_START = 1;
    private static final int MSG_TYPE_UPDATE_PROGRESS = 2;
    private static final int MSG_TYPE_SINGLE_COMPLETE = 3;
    private Handler mUihandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (!isRunning)
                return;
            switch (msg.what) {
                case MSG_TYPE_START:
                    newTaskStart();
                    break;
                case MSG_TYPE_UPDATE_PROGRESS:
                    updateProgress((int) msg.obj);
                    break;
                case MSG_TYPE_SINGLE_COMPLETE:
                    checkDownloadListProgress();
                    break;
            }
        }
    };

    // ======下载监听======
    @Override
    public void onStart(int id) {
        mUihandler.sendEmptyMessage(MSG_TYPE_START);
    }

    @Override
    public void onProgress(int progress, long cacheSize, long totalSize) {
        Message.obtain(mUihandler, MSG_TYPE_UPDATE_PROGRESS, progress).sendToTarget();
    }

    @Override
    public void onRestart() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onFinish() {
        mUihandler.sendEmptyMessage(MSG_TYPE_SINGLE_COMPLETE);
        Log.e("download", "Success one task");
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(OkDownloadError error) {
        Log.e("download", "error");
    }

}
