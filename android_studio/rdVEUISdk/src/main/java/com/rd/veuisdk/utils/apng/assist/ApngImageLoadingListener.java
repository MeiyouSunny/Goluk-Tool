package com.rd.veuisdk.utils.apng.assist;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.rd.veuisdk.utils.apng.ApngDrawable;
import com.rd.veuisdk.utils.apng.ApngImageLoader;

import java.io.File;

public class ApngImageLoadingListener implements ImageLoadingListener {
    private ApngImageLoaderCallback callback;
    private Context context;
    private Uri uri;

    public ApngImageLoadingListener(Context context, Uri uri, ApngImageLoaderCallback callback) {
        this.context = context;
        this.uri = uri;
        this.callback = callback;
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        if (view == null) return;
        view.setTag(uri.toString());
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        if (view == null) return;
        Object tag = view.getTag();
        if (tag != null && tag instanceof String) {
            String actualUri = tag.toString();
            File pngFile = AssistUtil.getCopiedFile(context, actualUri);
            if (pngFile == null) {

            } else if (pngFile.exists()) {
                boolean isApng = AssistUtil.isApng(pngFile);
                if (isApng) {
                    ApngDrawable drawable = new ApngDrawable(context, loadedImage, Uri.fromFile(pngFile));
                    ((ImageView) view).setImageDrawable(drawable);
                } else {
                    ((ImageView) view).setImageBitmap(loadedImage);
                }
            } else {
                MemoryCacheUtils.removeFromCache(actualUri, ApngImageLoader.getInstance().getMemoryCache());
                DiskCacheUtils.removeFromCache(actualUri, ApngImageLoader.getInstance().getDiskCache());
                ApngImageLoader.getInstance().displayImage(actualUri, (ImageView) view, this);
            }
        }
        if (shouldForward())
            callback.onLoadFinish(true, imageUri, view);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        if (view == null) return;
        view.setTag(null);
        if (shouldForward())
            callback.onLoadFinish(false, imageUri, view);
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        if (view == null) return;
        view.setTag(null);
        if (shouldForward())
            callback.onLoadFinish(false, imageUri, view);
    }

    private boolean shouldForward() {
        return callback != null;
    }
}