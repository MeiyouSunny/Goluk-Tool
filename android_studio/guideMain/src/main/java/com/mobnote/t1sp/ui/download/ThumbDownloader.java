package com.mobnote.t1sp.ui.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mobnote.t1sp.util.CollectionUtils;
import com.mobnote.t1sp.util.FileUtil;

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

    public ThumbDownloader(Context context) {
        mContext = context;
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
        for (String url : mUrls) {
            if (!isRunning)
                return;
            try {
                File thumbCacheFile = getThumbCacheFileByUrl(url);
                if (thumbCacheFile.exists())
                    continue;

                File result = Glide.with(mContext)
                        .load(url)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get(10, TimeUnit.SECONDS);

                if (result != null) {
                    FileUtil.copyFile(result, getThumbCacheFileByUrl(url));
                    if (mListener != null) {
                        Message msg = Message.obtain(mUihandler, 0, url);
                        msg.sendToTarget();
                    }
                    Log.e("Thumb", "Success");
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Thumb", "Fail");
                continue;
            }
        }

        isRunning = false;
        mUrls.clear();
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

    private RequestListener mGlideListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            //imageView.setImageDrawable(resource);
            Message msg = Message.obtain(mUihandler, 0, model);
            msg.sendToTarget();
            return false;
        }
    };

    public void setListener(ThumbDownloadListener listener) {
        this.mListener = listener;
    }

    public interface ThumbDownloadListener {
        void onThumbDownload(String thumbUrl);
    }

}
