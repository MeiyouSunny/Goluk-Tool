package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.ThumbNailInfo;
import com.rd.veuisdk.utils.Utils;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 截取视频
 */
public class VideoThumbNailAlterView extends View {
    private final String TAG = "VideoThumbNailAlterView";
    private final int MSG_THUMB_ITEM = 10;
    private boolean isDrawing = false;
    private VirtualVideo mVirtualVideo;
    private int mDuration;
    private int mLastTime = -1;
    private int thumbW = 90, thumbH = 160;
    //缩略图轴对应的像素值
    private int[] params = new int[2];

    public VideoThumbNailAlterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initThread(context);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        for (Entry<Integer, ThumbNailInfo> item : mMemoryCache.entrySet()) {
            ThumbNailInfo temp = item.getValue();
            if (temp != null && temp.bmp != null && !temp.bmp.isRecycled()) {
                canvas.drawBitmap(temp.bmp, null, temp.dst, null);
            }
        }
    }

    /***
     * 设置视频资源
     * @param isTrim
     * @param player
     * @return
     */
    public int[] setVirtualVideo(boolean isTrim, VirtualVideo player) {
        mVirtualVideo = player;
        if (isTrim) {
            thumbH = getResources().getDimensionPixelSize(
                    R.dimen.preview_rangseekbarplus_trim_height);
        } else {
            thumbH = getResources().getDimensionPixelSize(
                    R.dimen.preview_rangseekbarplus_height);
        }
        int nduration = Utils.s2ms(mVirtualVideo.getDuration());
        if (nduration < 5000) {
            maxCount = 2;
        } else if (nduration < 10000) {
            maxCount = 8;
        } else if (nduration < 60000) {
            maxCount = nduration / 2000;
        } else {
            maxCount = 40;
        }
        mDuration = nduration;

        thumbW = (int) (thumbH * 9 / 16);

        maxCount = getWidth() / thumbW + 1;
        params[0] = thumbW * maxCount;
        itemTime = mDuration / maxCount;
        if ((mDuration % itemTime) != 0) {
            // 画半块
            mLastTime = (int) ((double) itemTime * (maxCount - 0.1));
            params[0] = params[0] + thumbW / 2;
        }
        params[1] = thumbH;
        return params;
    }


    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_THUMB_ITEM:
                    if (!isDrawing)
                        invalidate();
                    break;

                default:
                    break;
            }

        }

        ;
    };

    /**
     * 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
     */
    private HashMap<Integer, ThumbNailInfo> mMemoryCache;

    /**
     * 异步加载图片
     *
     * @param context
     */
    private void initThread(Context context) {
        mMemoryCache = new HashMap<Integer, ThumbNailInfo>();

    }

    /**
     * 下载Image的线程池
     */
    private ExecutorService mImageThreadPool = null;

    /**
     * 获取线程池的方法，因为涉及到并发的问题，我们加上同步锁
     *
     * @return
     */
    private ExecutorService getThreadPool() {
        if (mImageThreadPool == null) {
            synchronized (ExecutorService.class) {
                if (mImageThreadPool == null) {
                    // 为了下载图片更加的流畅，我们用了2个线程来加载图片
                    mImageThreadPool = Executors.newFixedThreadPool(4);
                }
            }
        }

        return mImageThreadPool;

    }

    /**
     * 添加Bitmap到内存缓存
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(Integer key, Rect src, Rect dst,
                                        boolean isleft, boolean isright, Bitmap bitmap) {
        ThumbNailInfo info = new ThumbNailInfo(key, src, dst, isleft, isright);
        info.bmp = bitmap;
        mMemoryCache.put(key, info);

    }

    /**
     * 从内存缓存中获取一个Bitmap
     *
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemCache(Integer key) {
        ThumbNailInfo info = mMemoryCache.get(key);
        return (null != info) ? info.bmp : null;
    }

    private int maxCount = 40;

    private int itemTime = 1;

    /**
     * 开始加载图片
     */
    public void setStartThumb() {
        Rect tdst = new Rect(0, 0, thumbW, thumbH), src = new Rect(0, 0,
                thumbW, thumbH);

        int splitTime = itemTime / 2; // 每张图对应的中间时刻

        downloadImage(splitTime, src, new Rect(tdst), true, false);

        for (int i = 1; i < (maxCount - 1); i++) {
            tdst = new Rect(tdst.right, tdst.top, tdst.right + thumbW,
                    tdst.bottom);
            splitTime += itemTime;
            downloadImage(splitTime, src, tdst, false, false);
        }

        if (mLastTime > 0) { // 有最后半张
            splitTime += itemTime;
            tdst = new Rect(tdst.right, tdst.top, tdst.right + thumbW,
                    tdst.bottom);
            downloadImage(splitTime, src, tdst, false, false);

            int half = thumbW / 2;
            src = new Rect(0, 0, half, thumbH);
            tdst = new Rect(params[0] - half, tdst.top, params[0], tdst.bottom);
            downloadImage(mLastTime, src, tdst, false, true);
        } else { // 没最后半张

            tdst = new Rect(tdst.right, tdst.top, tdst.right + thumbW,
                    tdst.bottom);
            downloadImage(mDuration - itemTime / 2, src, tdst, false, true);

        }

    }

    /**
     * @param nTime   单位:毫秒
     * @param src
     * @param dst
     * @param isleft
     * @param isright
     */
    private void downloadImage(final int nTime, final Rect src, final Rect dst,
                               final boolean isleft, final boolean isright) {

        Bitmap bitmap = getBitmapFromMemCache(nTime);
        if (bitmap != null) {
            mhandler.sendEmptyMessage(MSG_THUMB_ITEM);
        } else {
            if (mMemoryCache.get(nTime) == null) {
                addBitmapToMemoryCache(nTime, src, dst, isleft, isright, bitmap);

                getThreadPool().execute(new Runnable() {

                    @Override
                    public void run() {

                        Bitmap bitmap = Bitmap.createBitmap(thumbW, thumbH,
                                Config.ARGB_8888);

                        if (null != mVirtualVideo
                                && mVirtualVideo.getSnapshot(Utils.ms2s(nTime), bitmap)) {
                            // 将Bitmap 加入内存缓存
                            addBitmapToMemoryCache(nTime, src, dst, isleft,
                                    isright, bitmap);
                            mhandler.sendEmptyMessage(MSG_THUMB_ITEM);
                        } else {
                            bitmap.recycle();
                        }
                    }
                });
            }
        }

    }

    /**
     * 释放资源
     */
    public void recycle() {
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }
        for (Entry<Integer, ThumbNailInfo> item : mMemoryCache.entrySet()) {
            ThumbNailInfo temp = item.getValue();
            if (null != temp) {
                temp.recycle();
            }
        }
        mMemoryCache.clear();
        invalidate();
    }


}
