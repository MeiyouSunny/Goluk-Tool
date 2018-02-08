package com.mobnote.t1sp.download;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.mobnote.application.GlobalWindow;
import com.mobnote.eventbus.EventDownloadVideoFinish;
import com.mobnote.golukmain.R;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * T1SP 下载管理
 */
public class DownloaderT1spImpl implements DownloaderT1sp {

    private static DownloaderT1spImpl mInstance;

    private static Context mContext;

    /* 总共需要下载的列表 */
    public List<Task> mListTotal;
    /* 已经下载完成的列表 */
    public List<Task> mListDownloaded;
    /* 是否正在下载 */
    private boolean isRunning;
    // 当前正在下载的本地文件
    private File mCurrentFile;

    private SoundPool mSoundPool;
    private int mSoundId;

    private List<IDownloadSuccess> mListeners;

    private DownloaderT1spImpl(Context context) {
        mContext = context;
        if (mListTotal == null)
            mListTotal = new ArrayList<>();
        if (mListDownloaded == null)
            mListDownloaded = new ArrayList<>();
        if (mListeners == null)
            mListeners = new ArrayList<>();

        FileDownloader.setup(context);

        mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
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
    public void addDownloadTasks(List<Task> tasks, IDownloadSuccess listener) {
        if (CollectionUtils.isEmpty(tasks))
            return;

        addTasks(tasks);
        updateUiDownloadCount();

        if (mListeners != null)
            mListeners.add(listener);

        if (!isRunning) {
            isRunning = true;
            updateUiDownloadCount();
            startNextTask();
        }
    }

    /**
     * 需要过滤掉已经在下载列表的Task
     */
    private void addTasks(List<Task> tasks) {
        if (mListTotal == null || CollectionUtils.isEmpty(tasks))
            return;
        for (Task task : tasks) {
            if (!isInTaskList(task)) {
                mListTotal.add(task);
            }
        }
    }

    private boolean isInTaskList(Task task) {
        if (task == null)
            return true;
        for (Task temp : mListTotal) {
            if (TextUtils.equals(temp.downloadPath, task.downloadPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 取出下一个任务并开始下载
     */
    private void startNextTask() {
        Task task = mListTotal.get(mListDownloaded.size());
        mCurrentFile = new File(task.savePath);

        // 下载对应的GPS文件
        downloadGpsFile(task);
        // 如果文件已经存在
        if (mCurrentFile.exists()) {
            checkDownloadListProgress();
        } else {
            mCurrentFile.getParentFile().mkdirs();
            // 新建下载任务
            download(task.downloadPath, task.savePath, mDownloadListener);
        }
    }

    private void downloadGpsFile(Task task) {
        File gpsFile = new File(task.getGpsSavePath());
        if (!gpsFile.exists()) {
            gpsFile.getParentFile().mkdirs();
            download(task.getGpsDownloadPath(), task.getGpsSavePath(), null);
        }
    }

    private void download(String url, String savePath, FileDownloadListener listener) {
        FileDownloader.getImpl().create(url).setPath(savePath).setListener(listener).start();
    }

    // ======下载监听======
    private SimpleDownloadListener mDownloadListener = new SimpleDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            mUihandler.sendEmptyMessage(MSG_TYPE_START);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            int percent = Math.round((float) soFarBytes / (float) totalBytes * 100);
            Message.obtain(mUihandler, MSG_TYPE_UPDATE_PROGRESS, percent).sendToTarget();
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            //final String videoName = getCurrentDownloadingVideoName(task.getFilename());

            onCallbackListener(task.getFilename(), true);

            mUihandler.sendEmptyMessage(MSG_TYPE_SINGLE_COMPLETE);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            //final String videoName = getCurrentDownloadingVideoName();
            onCallbackListener(task.getFilename(), false);

            mUihandler.sendEmptyMessage(MSG_TYPE_SINGLE_COMPLETE);
        }
    };

    /**
     * 是否正在下载
     */
    public boolean isDownloading() {
        return isRunning;
    }

    @Override
    public void cancelAllDownloadTask(boolean showCancelMsg) {
        // 取消下载任务
        FileDownloader.getImpl().pauseAll();
        // 删除当前正在下载的文件
        if (mCurrentFile != null && mCurrentFile.exists())
            mCurrentFile.delete();
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
        if (mListeners != null)
            mListeners.clear();
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
            isRunning = false;
            GlobalWindow.getInstance().topWindowSucess(getString(R.string.str_video_transfer_success));
            playSuccessSound();
            // 发送本地更新视频Event
            EventBus.getDefault().post(new EventDownloadVideoFinish());
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
        GlobalWindow.getInstance().refreshPercent(progress);
    }

    /**
     * 新的Task开始下载
     */
    private void newTaskStart() {
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

    private String getCurrentDownloadingVideoName(File file) {
        if (file == null)
            return "";
        String filePath = file.getAbsolutePath();
        return FileUtil.getFileNameFromPath(filePath);
    }

    private void onCallbackListener(String videoName, boolean success) {
        if (CollectionUtils.isEmpty(mListeners))
            return;
        for (IDownloadSuccess listener : mListeners) {
            if (listener != null)
                listener.onVideoDownloadSuccess(videoName, success);
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

}
