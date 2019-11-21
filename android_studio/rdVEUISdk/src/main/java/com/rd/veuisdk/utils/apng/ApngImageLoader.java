package com.rd.veuisdk.utils.apng;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.apng.assist.ApngImageDownloader;
import com.rd.veuisdk.utils.apng.assist.ApngImageLoaderCallback;
import com.rd.veuisdk.utils.apng.assist.ApngImageLoadingListener;
import com.rd.veuisdk.utils.apng.assist.ApngListener;
import com.rd.veuisdk.utils.apng.assist.AssistUtil;
import com.rd.veuisdk.utils.apng.assist.PngImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main class for APNG image loading that inherited from UIL ImageLoader.
 * Same as its parent, the init() method must be called before any other methods.
 */
public class ApngImageLoader extends ImageLoader {

    private static ApngImageLoader singleton;

    private Context context;

    public static ApngImageLoader getInstance() {
        if (singleton == null) {
            synchronized (ApngImageLoader.class) {
                if (singleton == null) {
                    singleton = new ApngImageLoader();
                }
            }
        }
        return singleton;
    }

    protected ApngImageLoader() { /*Singleton*/ }

    public void init(Context context) {
        this.init(context, null, null);
    }

    public void init(Context context,
                     ImageLoaderConfiguration commonImageLoaderConfiguration,
                     ImageLoaderConfiguration apngComponentImageLoaderConfiguration) {

        this.context = context.getApplicationContext();

        if (commonImageLoaderConfiguration == null) {
            commonImageLoaderConfiguration = getDefaultCommonImageLoaderConfiguration();
        }

        if (apngComponentImageLoaderConfiguration == null) {
            apngComponentImageLoaderConfiguration = getDefaultApngComponentImageLoaderConfiguration(this.context);
        }

        // Initialize UIL for loading plain PNG files
        PngImageLoader.getInstance().init(commonImageLoaderConfiguration);

        // Initialize UIL for loading APNG component files
        super.init(apngComponentImageLoaderConfiguration);
    }

    @Override
    public void displayImage(String uri, ImageView imageView) {
        displayApng(uri, imageView, null);
    }

    @Override
    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        displayApng(uri, imageView, options, null);
    }

    /**
     * Load and display APNG in specific ImageView object with ApngConfig
     * @param uri Source URI
     * @param imageView Target view
     * @param config APNG configuration
     */
    public void displayApng(String uri, ImageView imageView, ApngConfig config) {
        super.displayImage(uri, new ImageViewAware(imageView, false), new ApngImageLoadingListener(context, Uri.parse(uri), getAutoPlayHandler(config, null)));
    }

    /**
     * Load and display APNG in specific ImageView object with DisplayImageOptions and ApngConfig
     * @param uri Source URI
     * @param imageView Target view
     * @param options UIL DisplayImageOptions
     * @param config APNG configuration
     */
    public void displayApng(String uri, ImageView imageView, DisplayImageOptions options, ApngConfig config) {
        super.displayImage(uri, imageView, options, new ApngImageLoadingListener(context, Uri.parse(uri), getAutoPlayHandler(config, null)));
    }

    public void displayApng(String uri, ImageView imageView, ApngConfig config, ApngListener apngListener) {
        super.displayImage(uri, new ImageViewAware(imageView, false), new ApngImageLoadingListener(context, Uri.parse(uri), getAutoPlayHandler(config, apngListener)));
    }

    public void displayApng(String uri, ImageView imageView, DisplayImageOptions options, ApngConfig config, ApngListener apngListener) {
        super.displayImage(uri, imageView, options, new ApngImageLoadingListener(context, Uri.parse(uri), getAutoPlayHandler(config, apngListener)));
    }

    private ImageLoaderConfiguration getDefaultApngComponentImageLoaderConfiguration(Context context) {
        DisplayImageOptions defaultDisplayImageOptions =
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                        .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                        .showImageOnLoading(R.drawable.imageloader)
                        .build();

        return new ImageLoaderConfiguration.Builder(context)
                .memoryCache(new LruMemoryCache(10 * 1024 * 1024))//缓存策略
                .memoryCacheSize(10 * 1024 * 1024)//内存缓存大小
                //设置内存缓存不允许缓存一张图片的多个尺寸，默认允许。
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(AssistUtil.getWorkingDir(context)))//缓存路径
                .diskCacheFileCount(20000)//文件数量
                .defaultDisplayImageOptions(defaultDisplayImageOptions)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())//保存的时候用MD5加密
                .imageDownloader(new ApngImageDownloader(context))
                .threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY)
                .build();
    }

    private ImageLoaderConfiguration getDefaultCommonImageLoaderConfiguration() {
        DisplayImageOptions defaultDisplayImageOptions =
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                        .cacheOnDisk(true)
                        .build();
        return new ImageLoaderConfiguration.Builder(this.context)
                .memoryCache(new LruMemoryCache(10 * 1024 * 1024))
                .memoryCacheSize(10 * 1024 * 1024)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(AssistUtil.getWorkingDir(context)))
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .defaultDisplayImageOptions(defaultDisplayImageOptions)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileCount(10000)
                .threadPoolSize(1)
                .threadPriority(Thread.NORM_PRIORITY )
                .build();
    }

    private ApngImageLoaderCallback getAutoPlayHandler(final ApngConfig config, final ApngListener apngListener) {
        if (config == null || !config.autoPlay) {
            return null;
        } else {
            return new ApngImageLoaderCallback() {
                @Override
                public void onLoadFinish(boolean success, String imageUri, View view) {
                    if (!success)
                        return;
                    ApngDrawable apngDrawable = ApngDrawable.getFromView(view);
                    if (apngDrawable == null) {
                        if (apngListener != null) {
                            apngListener.onAnimationStart(null);
                        }
                        return;
                    }
                    apngDrawable.setApngListener(apngListener);
                    if (config.numPlays > 0) {
                        apngDrawable.setNumPlays(config.numPlays);
                    }
                    apngDrawable.setShowLastFrameOnStop(config.showLastFrameOnStop);
                    apngDrawable.start();
                }
            };
        }
    }

    public static class ApngConfig {
        public int numPlays = 0;
        public boolean autoPlay = false;
        public boolean showLastFrameOnStop = false;

        /**
         * Configuration for controlling APNG behavior
         * @param numPlays Overrides the number of repetition
         * @param autoPlay Start the animation immediately after finish loading an image
         * @param showLastFrameOnStop On animation end, keep showing the last frame instead of redrawing the first
         */
        public ApngConfig(int numPlays, boolean autoPlay, boolean showLastFrameOnStop) {
            this.numPlays = numPlays;
            this.autoPlay = autoPlay;
            this.showLastFrameOnStop = showLastFrameOnStop;
        }
    }
}
