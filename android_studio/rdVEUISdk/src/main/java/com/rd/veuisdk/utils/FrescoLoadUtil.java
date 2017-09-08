package com.rd.veuisdk.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * 在非主线程里面支持调用
 * Fresco加载工具类
 * Glide处理非主线程和跨进程的时候有问题，一旦有办法处理就删除本方法
 */
public class FrescoLoadUtil {

    private int mWidth =0;//目标宽度
    private static FrescoLoadUtil inst;
    private ExecutorService executeBackgroundTask = Executors.newSingleThreadExecutor();

    public static FrescoLoadUtil getInstance() {
        if (inst == null) {
            inst = new FrescoLoadUtil();
        }
        return inst;
    }
    public void setWidth(int nWidth)
    {
        mWidth = nWidth;
    }

    //加载直接返回Bitmap
    public final void loadImageBitmap(String url, FrescoBitmapCallback<Bitmap> callback) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        try {
            fetch(Uri.parse(url), callback);
        } catch (Exception e) {
            //oom风险.
            e.printStackTrace();
            callback.onFailure(Uri.parse(url), e);
        }
    }

    private Bitmap getBitmapFromCache(String url) {

        Uri uri = Uri.parse(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest imageRequest = ImageRequest.fromUri(uri);
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchImageFromBitmapCache(imageRequest, CallerThreadExecutor.getInstance());
        try {
            CloseableReference<CloseableImage> imageReference = dataSource.getResult();
            if (imageReference != null) {
                try {
                    CloseableBitmap image = (CloseableBitmap) imageReference.get();
                    // do something with the image
                    Bitmap loadedImage = image.getUnderlyingBitmap();
                    if (loadedImage != null) {
                        return loadedImage;
                    } else {
                        return null;
                    }
                } finally {
                    CloseableReference.closeSafely(imageReference);
                }
            }
        } finally {
            dataSource.close();
        }
        return null;
    }


    Postprocessor postprocessor = new Postprocessor() {
        @Override
        public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
            int sw = sourceBitmap.getWidth();
            int sh = sourceBitmap.getHeight();
            float scale = (float) mWidth / (float) sw;
            int mscale = sw / mWidth;
            float heigh = scale * (float) sh;
            int nscale = sh / (int) heigh;
            CloseableReference<Bitmap> bitmapRef = bitmapFactory.createBitmap(mWidth, (int) heigh);
            try {
                Bitmap destBitmap = bitmapRef.get();
                for (int x = 0, m = 0; x < destBitmap.getWidth() && m < sw; x++, m += mscale) {
                    for (int y = 0, n = 0; y < destBitmap.getHeight() && n < sh; y++, n += nscale) {
                        destBitmap.setPixel(x, y, sourceBitmap.getPixel(m, n));
                    }
                }
                return CloseableReference.cloneOrNull(bitmapRef);
            } finally {
                CloseableReference.closeSafely(bitmapRef);
            }
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public CacheKey getPostprocessorCacheKey() {
            return null;
        }
    };

    private void fetch(final Uri uri, final FrescoBitmapCallback<Bitmap> callback) throws Exception {
        ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(uri);
        ImageRequest imageRequest = requestBuilder
                .setResizeOptions(new ResizeOptions(300, 300))
                .setProgressiveRenderingEnabled(true)
                //.setPostprocessor(postprocessor)
                .build();
        DataSource<CloseableReference<CloseableImage>> dataSource = ImagePipelineFactory.getInstance().getImagePipeline().fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
                                 @Override
                                 public void onNewResultImpl(@Nullable final Bitmap bitmap) {
                                     if (callback == null)
                                         return;
                                     if (bitmap != null && !bitmap.isRecycled()) {
                                         handlerBackgroundTask(new Callable<Bitmap>() {
                                             @Override
                                             public Bitmap call() throws Exception {
                                                 final Bitmap resultBitmap = bitmap.copy(bitmap.getConfig(), bitmap.isMutable());
                                                 if (resultBitmap != null && !resultBitmap.isRecycled())
                                                     postResult(resultBitmap, uri, callback);
                                                 return resultBitmap;
                                             }
                                         });
                                     }
                                 }

                                 @Override
                                 public void onCancellation(DataSource<CloseableReference<CloseableImage>> dataSource) {
                                     super.onCancellation(dataSource);
                                     if (callback == null)
                                         return;
                                     callback.onCancel(uri);
                                 }

                                 @Override
                                 public void onFailureImpl(DataSource dataSource) {
                                     if (callback == null)
                                         return;
                                     Throwable throwable = null;
                                     if (dataSource != null) {
                                         throwable = dataSource.getFailureCause();
                                     }
                                     callback.onFailure(uri, throwable);
                                 }
                             },
                UiThreadImmediateExecutorService.getInstance());
    }

    /**
     * @param callable Callable
     * @param <T>      T
     * @return Future
     */
    private <T> Future<T> handlerBackgroundTask(Callable<T> callable) {
        return executeBackgroundTask.submit(callable);
    }

    /**
     * 回调UI线程中去
     *
     * @param result   result
     * @param uri      uri
     * @param callback FrescoBitmapCallback
     * @param <T>      T
     */
    private <T> void postResult(final T result, final Uri uri, final FrescoBitmapCallback<T> callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(uri, result);
            }
        });
    }

    /**
     *
     */
    public interface FrescoBitmapCallback<T> {

        void onSuccess(Uri uri, T result);

        void onFailure(Uri uri, Throwable throwable);

        void onCancel(Uri uri);
    }

}