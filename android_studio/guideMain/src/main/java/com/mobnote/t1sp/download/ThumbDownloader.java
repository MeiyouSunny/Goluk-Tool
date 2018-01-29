package com.mobnote.t1sp.download;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.FileUtil;
import com.mobnote.t1sp.util.ThumbUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * T1SP 缩略图 Downloader
 */
public class ThumbDownloader implements Runnable {

    private Context mContext;
    private List<String> mUrls;
    private boolean isRunning;
    private ThumbDownloadListener mListener;
    private boolean mIsWonderfulVideo;

    public ThumbDownloader(Context context, boolean isWonderfulVideo) {
        mContext = context;
        mIsWonderfulVideo = isWonderfulVideo;
        mUrls = new ArrayList<>();
    }

    public void addUrls(List<String> urls) {
        if (CollectionUtils.isEmpty(urls))
            return;

        mUrls.addAll(urls);

        if (!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }

    }

    @Override
    public void run() {
        if (CollectionUtils.isEmpty(mUrls))
            return;
        int size = mUrls.size();
        for (int i = 0; i < size; i++) {
            String url = mUrls.get(i);
            if (!isRunning)
                return;
            try {
                File thumbCacheFile = getThumbCacheFileByUrl(url);
                if (thumbCacheFile.exists())
                    continue;

                if (mIsWonderfulVideo)
                    getThumbFromVideo(url, thumbCacheFile);
                else
                    getThumbFromThumbFile(url, thumbCacheFile);

            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        isRunning = false;
        mUrls.clear();
    }

    /**
     * 从视频文件获取第一帧下载到本地作为缩略图
     *
     * @param url            视频地址
     * @param thumbCacheFile 保存的缩略图文件
     */
    private void getThumbFromVideo(String url, File thumbCacheFile) {
        Bitmap bitmapThumb = ThumbUtil.getNetVideoThumb(url);
        if (bitmapThumb != null) {
            // 保存到本地缓存目录
            FileUtil.saveBitmap(bitmapThumb, thumbCacheFile, 10);
            if (mListener != null) {
                Message msg = Message.obtain(mUihandler, 0, url);
                msg.sendToTarget();
            }
        }
    }

    /**
     * 直接下载缩略图文件到本地
     *
     * @param url            视频地址
     * @param thumbCacheFile 保存的缩略图文件
     */
    private void getThumbFromThumbFile(String url, File thumbCacheFile) throws Exception {
        File result = Glide.with(mContext)
                .load(url)
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get(10, TimeUnit.SECONDS);

        if (result != null) {
            FileUtil.copyFile(result, thumbCacheFile);
            if (mListener != null) {
                Message msg = Message.obtain(mUihandler, 0, url);
                msg.sendToTarget();
            }
        }
    }

    private File getThumbCacheFileByUrl(String thumbUrl) {
        if (TextUtils.isEmpty(thumbUrl))
            return null;
        final String fileName = FileUtil.getFileNameFromPath(thumbUrl);
        File thumbCacheFile = new File(FileUtil.getThumbCacheByVideoName(fileName));
        thumbCacheFile.getParentFile().mkdirs();

        return thumbCacheFile;
    }

    public void stop() {
        isRunning = false;
        if (mUrls != null) {
            mUrls.clear();
            mUrls = null;
        }
        mListener = null;
    }

    private Handler mUihandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (mListener != null)
                mListener.onThumbDownload((String) msg.obj);
        }
    };

    public void setListener(ThumbDownloadListener listener) {
        this.mListener = listener;
    }

    public interface ThumbDownloadListener {
        void onThumbDownload(String thumbUrl);
    }

}
