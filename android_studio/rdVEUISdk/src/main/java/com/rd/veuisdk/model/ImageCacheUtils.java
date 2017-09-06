package com.rd.veuisdk.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.utils.ThumbNailUtils;

import java.lang.ref.WeakReference;

/**
 * 图片缓存工具
 *
 * @author abreal
 */
public class ImageCacheUtils {

    /**
     * 缓存参数
     *
     * @author abreal
     */
    public static class CacheParam {
        /**
         * 是否为视频
         */
        private boolean isVideo;
        /**
         * 视频时获取缩略图时间
         */
        private long lVideoSnapshotTime;

        private int nWidth, nHeight;

        private String strResourcePath;

        /**
         * 缓存参数构造函数
         *
         * @param isVideo 是否为视频
         * @param time    截取缩略图时间
         */
        public CacheParam(boolean isVideo, long time) {
            this.isVideo = isVideo;
            lVideoSnapshotTime = time;
        }

        /**
         * 缓存参数构造函数
         *
         * @param isVideo 是否为视频
         * @param time    截取缩略图时间
         * @param nWidth  缓存图片宽度
         * @param nHeight 缓存图片高度
         */
        public CacheParam(boolean isVideo, long time, int nWidth, int nHeight) {
            this.isVideo = isVideo;
            lVideoSnapshotTime = time;
            this.nWidth = nWidth;
            this.nHeight = nHeight;
        }

        /**
         * 缓存参数构造函数<br>
         * 主要用于图片
         *
         * @param nWidth  缓存图片宽度
         * @param nHeight 缓存图片高度
         */
        public CacheParam(int nWidth, int nHeight) {
            this.nWidth = nWidth;
            this.nHeight = nHeight;
        }

        /**
         * 缓存参数默认构造函数<br>
         * 主要用于图片
         */
        public CacheParam() {

        }
    }

    private static final String TAG = "ImageCacheUtils";
    /**
     * 单例对象
     */
    private static ImageCacheUtils instance;

    /**
     * 图片缓存技术的核心类,在程序内存达到设定值时,会将最近最少使用的图片从缓存中移除
     */
    private LruCache<String, Bitmap> mMemoryCache;

    private Context mContext;

    private static int width = ThumbNailUtils.THUMB_WIDTH;
    private static int height = ThumbNailUtils.THUMB_HEIGHT;

    /**
     * 单例模式的构造函数
     */
    private ImageCacheUtils(Context context) {

        // 获取应用程序的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        Log.d(TAG, "" + maxMemory);

        // 设置图片缓存为最大可用内存的14
        int cacheSize = maxMemory / 4;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        mContext = context;
        setTargetSize(ThumbNailUtils.THUMB_WIDTH, ThumbNailUtils.THUMB_HEIGHT);
    }

    public static ImageCacheUtils getInstance(Context context) {
        if (instance == null) {
            instance = new ImageCacheUtils(context.getApplicationContext());
        }
        return instance;
    }

    public void recycle() {
        if (null != mMemoryCache) {
            mMemoryCache.evictAll();
            mMemoryCache = null;
        }
        instance = null;
    }

    public void setTargetSize(int mwidth, int mheight) {
        width = mwidth;
        height = mheight;
    }

    /**
     * 将图片存入缓存中
     *
     * @param key
     * @param bitmap
     */
    @SuppressLint("NewApi")
    private void addBitmapToMemroyCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 从缓存中取出一张图片, 如果不存在就返回null
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromCache(String key) {
        return mMemoryCache.get(key);
    }

    private int calculateInSmpleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {

        // 图片的原始高度和宽度
        final int height = options.outHeight;

        final int width = options.outWidth;

        int inSmaleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfWidth = width / 2;

            final int halfHeight = height / 2;

            while (((halfWidth / inSmaleSize) > reqWidth)
                    && ((halfHeight / inSmaleSize) > reqHeight)) {
                inSmaleSize *= 2;
            }
        }

        return inSmaleSize;
    }

    /**
     * 根据图片的资源id对图片解析
     *
     * @author jeck
     */
    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;

        private int data = 0;

        public BitmapWorkerTask(ImageView imageView) {

            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];

            // 返回的是100X100的图片,这个地方需要我们根据自己的图片要求,自定义加载
            return decodeSampledBitmapFromResource(mContext.getResources(),
                    data, width, height);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (imageViewReference != null && bitmap != null) {

                // 将解析好的图片先保存在缓存中
                addBitmapToMemroyCache(String.valueOf(data), bitmap);

                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        /**
         * 从资源文件中按要求解析图片
         *
         * @param res
         * @param resId
         * @param reqWidth
         * @param reqHeight
         * @return
         */
        private Bitmap decodeSampledBitmapFromResource(Resources res,
                                                       int resId, int reqWidth, int reqHeight) {

            final BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;

            BitmapFactory.decodeResource(res, resId, options);

            // 计算 inSampleSize
            options.inSampleSize = calculateInSmpleSize(options, reqWidth,
                    reqHeight);

            // 解析资源中的图片
            options.inJustDecodeBounds = false;

            Bitmap bmpImage = BitmapFactory.decodeResource(res, resId, options);
            return ThumbnailUtils.extractThumbnail(bmpImage, reqWidth,
                    reqHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
    }

    /**
     * 为控件加载图片
     *
     * @param resId     资源id
     * @param imageView 加载资源后的显示组件
     */
    public void loadBitmap(int resId, ImageView imageView) {
        Log.d(TAG, "" + resId);
        // 先从缓存中去取图片
        Bitmap bitmap = getBitmapFromCache(String.valueOf(resId));

        // 如果缓存中没有该图片,那就解析
        if (bitmap == null) {

            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute(resId);
        } else {

            // 如果有,就直接为ImageView加载图片
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 根据参数加载位图
     *
     * @param strResPath 资源路径
     * @param param      加载参数
     * @param imageView  加载后的位图显示组件
     */
    public void loadBitmap(String strResPath, CacheParam param,
                           ImageView imageView) {
        if (null == param) {
            param = new CacheParam();
        }
        param.strResourcePath = strResPath;
        // 先从缓存中去取图片
        Bitmap bitmap = getBitmapFromCache(strResPath
                + String.valueOf(param.lVideoSnapshotTime));

        // 如果缓存中没有该图片,那就解析
        if (bitmap == null) {
            // 从视频路径抓一张缩略图出来
            new CacheParamTask(imageView).execute(param);
        } else {
            // 如果有,就直接为ImageView加载图片
            imageView.setImageBitmap(bitmap);
        }
    }

    class CacheParamTask extends AsyncTask<CacheParam, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;

        private String data = null;

        private boolean isVideo;

        private long time;

        public CacheParamTask(ImageView imageView) {

            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(CacheParam... params) {
            CacheParam taskParam = params[0];
            data = taskParam.strResourcePath;
            isVideo = taskParam.isVideo;
            time = taskParam.lVideoSnapshotTime;
            int nTaskWidth = width, nTashHeight = height;
            if (taskParam.nWidth > 0) {
                nTaskWidth = taskParam.nWidth;
            }
            if (taskParam.nHeight > 0) {
                nTashHeight = taskParam.nHeight;
            }

            if (isVideo) {
                // 返回的是视频的缩略图,
                return createVideoThumbnailByTime(data, time, nTaskWidth,
                        nTashHeight);
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(data);
                return ThumbnailUtils.extractThumbnail(bitmap, nTaskWidth,
                        nTashHeight);
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                // 将解析好的图片先保存在缓存中
                addBitmapToMemroyCache(data + String.valueOf(time), bitmap);
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        private Bitmap createVideoThumbnailByTime(String videoPath, long time,
                                                  int nThumbnailWidth, int nThumbnailHeight) {
            Bitmap bitmap = null;
            if (!TextUtils.isEmpty(videoPath)) {
                bitmap = Bitmap.createBitmap(nThumbnailWidth, nThumbnailHeight,
                        Config.ARGB_8888);
                if (!VirtualVideo.getSnapShot(videoPath, (float) Math.max(time, 1000), bitmap, false)) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            return bitmap;
        }
    }

}
