package com.rd.veuisdk.ui.extrangseekbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 定长截取 1/3 - 2/3之间的内容
 */
public class VideoTrimFixedView extends View {

    private VirtualVideo mVirtualVideo;
    //总的视频时间  截取时间     单位统一毫秒
    private long mDuration;
    private long mTrimDuration = 1000;
    //截取图片宽高、总的宽、图片的数量
    private int thumbW = 90, thumbH = 160;
    private float mTotalWidth = 0;
    private int maxCount = 40;
    //每一张的间隔时间
    private int mItemTime = 1;
    //是否可以移动截取  当视频时间小于截取时间时不能移动
    private boolean mIsCanTrim = true;
    //0    1移动进度   2移动图片
    private int mMoveStatus = 0;
    //进度条宽度
    private int mProgressBarWidth = 5;
    //把手宽度
    private int mHandleWidth = 10;
    //宽度 三分之一
    private int mOneThird = 0;
    //缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
    private SparseArray<Bitmap> mArray;

    private VelocityTracker mVelocityTracker;
    private float mSpeed = 0;

    private Resources mResources;
    private Drawable mFrontHandleBg, mRearHandleBg;
    private Paint mShadowPaint = new Paint(), mProgressPaint = new Paint();
    private Rect mShadowLeftRect = new Rect(), mShadowRightRect = new Rect(),
            mProgressRect = new Rect(), mFrontHandleRect = new Rect(),
            mRearHandleRect = new Rect();
    private PaintFlagsDrawFilter mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(
            0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Rect mBitmapRect = new Rect();

    public VideoTrimFixedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mArray = new SparseArray<>();

        mResources = getResources();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setColor(mResources.getColor(R.color.transparent_black95));

        mProgressPaint.setColor(mResources.getColor(R.color.main_orange));
        mProgressPaint.setAntiAlias(true);

        mFrontHandleBg = mResources.getDrawable(R.drawable.trim_front_handle);
        mRearHandleBg = mResources.getDrawable(R.drawable.trim_rear_handle);

        mOneThird = (int) (getWidth() / 3f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTotalWidth < mOneThird) {
            return;
        }
        //判断move移动的范围大小
        if (mMoveX < 0) {
            mMoveX = 0;
            mHasBeenEnd = true;
        } else if (mOneThird + mMoveX > mTotalWidth) {
            mMoveX = (int) (mTotalWidth - mOneThird);
            mHasBeenEnd = true;
        } else {
            mHasBeenEnd = false;
        }
        int begin = mOneThird - mMoveX - thumbW;

        //返回截取时间
        if (mListener != null && mMoveStatus == 2) {
            long startTime = (int) (mMoveX / mTotalWidth * mDuration);
            long endTime = startTime + mTrimDuration;
            //保证最大时间不超过视频时间和最小时间小于等于0
            if (endTime > mDuration) {
                startTime = mDuration - mTrimDuration;
            }
            startTime = startTime > 0 ? startTime : 0;
            mListener.OnChanged(startTime, endTime);
            mListener.OnSeek(startTime);
        }

        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        //画图片
        for (int i = 0; i < mArray.size(); i++) {
            begin += thumbW;
            if (begin + thumbW < 0) {
                continue;
            }
            if (begin > getWidth()) {
                break;
            }
            Bitmap bitmap = mArray.valueAt(i);
            if (bitmap == null) {
                continue;
            }
            mBitmapRect.set(begin, 0, begin + bitmap.getWidth(), thumbH);
            canvas.drawBitmap(bitmap, null, mBitmapRect, null);
        }

        //画阴影
        mShadowLeftRect.set(0, 0, mOneThird, getHeight());
        mShadowRightRect.set(mOneThird * 2, 0, getWidth(), getHeight());
        canvas.drawRect(mShadowLeftRect, mShadowPaint);
        canvas.drawRect(mShadowRightRect, mShadowPaint);

        //把手
        mFrontHandleRect.set(mOneThird, 0, mOneThird + mHandleWidth, getHeight());
        mRearHandleRect.set(mOneThird * 2 - mHandleWidth, 0, 2 * mOneThird, getHeight());
        mFrontHandleBg.setBounds(mFrontHandleRect);
        mRearHandleBg.setBounds(mRearHandleRect);
        mFrontHandleBg.draw(canvas);
        mRearHandleBg.draw(canvas);

        //进度条
        if (mProgressRect.left < mOneThird) {
            mProgressRect.set(mOneThird, 0, mOneThird + mProgressBarWidth, thumbH);
        } else if (mProgressRect.left > 2 * mOneThird) {
            mProgressRect.set(2 * mOneThird - mProgressBarWidth, 0, 2 * mOneThird, thumbH);
        }
        //返回进度时间
        if (mListener != null && mMoveStatus == 1) {
            mListener.OnSeek((long) ((mProgressRect.left - mOneThird + mMoveX) / mTotalWidth * mDuration));
        }
        canvas.drawRect(mProgressRect, mProgressPaint);
    }

    //移动的距离
    private boolean mHasBeenEnd = false;
    private int lastX, mMoveX = 0;//向左为正

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsCanTrim) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mListener != null) {
                        mListener.OnPause();
                    }
                    //判断是否是移动的进度条
                    if (event.getX() < mOneThird * 2 && event.getX() > mOneThird) {
                        mMoveStatus = 1;
                    } else {
                        lastX = (int) (event.getRawX());
                        mMoveStatus = 2;
                    }
                    //停止动画
                    stopScroll();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mMoveStatus == 1) {
                        int x = (int) event.getX();
                        mProgressRect.set(x, 0, x + mProgressBarWidth, thumbH);
                        invalidate();
                    } else if (mMoveStatus == 2) {
                        int x = (int) event.getRawX();
                        //到达边缘时不能向该方向继续移动
                        if (!mHasBeenEnd || (mMoveX > 0 && (lastX - x) < 0 || (mMoveX <= 0 && (lastX - x) > 0))) {
                            mMoveX += (lastX - x) * 0.7;
                            lastX = x;
                            mProgressRect.set(mOneThird, 0, mOneThird + mProgressBarWidth, thumbH);
                            mVelocityTracker.computeCurrentVelocity(1000);
                            invalidate();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mSpeed = mVelocityTracker.getXVelocity();
                    sliding();
                    mVelocityTracker.clear();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    mMoveStatus = 0;
                    break;
            }
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    /***
     * 设置视频资源和截取时间  截取开始时间   毫秒
     */
    public void setVirtualVideo(float asp, VirtualVideo player, int time) {
        setVirtualVideo(asp, player, time, 0);
    }

    public void setVirtualVideo(float asp, VirtualVideo player, int time, long start) {
        this.mTrimDuration = time;
        mOneThird = (int) (getWidth() / 3f);
        asp = Math.max(3 / 4.0f, Math.min(asp, 4 / 3.0f));
        mVirtualVideo = player;
        thumbH = getHeight();
        if (thumbH == 0) {
            thumbH = getResources().getDimensionPixelSize(
                    R.dimen.preview_rangseekbarplus_height);
        }
        thumbW = (int) (thumbH * (asp));
        mDuration = Math.max(0, Utils.s2ms(mVirtualVideo.getDuration()));
        //计算总的宽度
        mTotalWidth = mDuration * mOneThird / (mTrimDuration + 0.0f);
        //计算截取数量
        maxCount = (int) (mTotalWidth / thumbW + 1);
        //Log.d("ok", "totalWidth:" + mTotalWidth + "    duration=" + mDuration + "    width=" + getWidth() + "     trimTime=" + mTrimDuration + "   maxCount=" + maxCount);
        mItemTime = (int) (mDuration / maxCount);
        if (mDuration <= mTotalWidth) {
            mIsCanTrim = false;
        }
        //计算开始move距离
        mMoveX = (int) (start * mTotalWidth / mDuration);

        mProgressRect.set(0, 0, mProgressBarWidth, thumbH);
    }

    /**
     * 设置进度 毫秒
     *
     * @param progress
     */
    public void setProgress(long progress) {
        progress = Math.min(Math.max(0, progress), mDuration);
        float mProgress = progress * mTotalWidth / mDuration;
        int left = (int) (mProgress - mMoveX + getWidth() / 3f);
        mProgressRect.set(left, 0, left + mProgressBarWidth, thumbH);// 宽10px
        invalidate();
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
     * 开始加载图片
     */
    public void setStartThumb() {
        int splitTime = mItemTime / 2; // 每张图对应的中间时刻
        mIsCut = false;
        for (int i = 0; i < maxCount; i++) {
            downloadImage(splitTime);
            splitTime += mItemTime;
        }
    }

    private void downloadImage(final int nTime) {
        Bitmap bitmap = mArray.get(nTime);
        if (bitmap != null) {
            mHandler.sendEmptyMessage(MSG_THUMB_ITEM);
        } else {
            if (mArray.get(nTime) == null) {
                mArray.put(nTime, bitmap);
                getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = Bitmap.createBitmap(thumbW, thumbH,
                                Bitmap.Config.ARGB_8888);
                        if (null != mVirtualVideo
                                && mVirtualVideo.getSnapshot(getContext(), Utils.ms2s(nTime), bitmap)) {
                            // 将Bitmap 加入内存缓存
                            mArray.put(nTime, bitmap);
                            mHandler.sendEmptyMessage(MSG_THUMB_ITEM);
                        } else {
                            bitmap.recycle();
                        }
                    }
                });
            }
        }
    }

    /**
     * 滑动的动画
     */
    private Animation mAnimation;

    private void sliding() {
        //不是移动的图片 返回
        if (mMoveStatus != 2 || Math.abs(mSpeed) < 300) {
            return;
        }
        if (mAnimation == null) {
            mAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    //interpolatedTime是当前的时间插值， 从0-1减速变化
                    //所以(1 - interpolatedTime)就是从1-0减速变化，
                    //而(1 - interpolatedTime) * speed就是将当前速度乘以插值，速度也会跟着从speed-0减速变化，
                    //将(1 - interpolatedTime) * speed)用于重绘，就可以实现平滑的滚动
                    if (mHasBeenEnd) {
                        stopScroll();
                    } else {
                        mMoveX -= (((1 - interpolatedTime) * mSpeed) / 100);
                        invalidate();
                    }
                }
            };
            mAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mMoveStatus = 2;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mVelocityTracker == null) {
                        mMoveStatus = 0;
                        mSpeed = 0;
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mAnimation.setInterpolator(new DecelerateInterpolator());//设置一个减速插值器
        }
        stopScroll();
        mAnimation.setDuration(2000);
        startAnimation(mAnimation);
    }

    /**
     * 停止滑动
     */
    private void stopScroll() {
        if (mAnimation != null && !mAnimation.hasEnded()) {
            mAnimation.cancel();
            clearAnimation();
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
        for (int i = 0; i < mArray.size(); i++) {
            Bitmap tmp = mArray.valueAt(i);
            if (tmp != null) {
                tmp.recycle();
            }
        }
        mArray.clear();
        mArray = null;
        mMoveX = 0;
        mIsCut = false;
        stopScroll();
        mAnimation = null;
        invalidate();
    }

    private final int MSG_THUMB_ITEM = 10;
    private boolean isDrawing = false;
    private boolean mIsCut = false;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_THUMB_ITEM) {
                if (!isDrawing && mArray != null) {
                    //最后一张图片
                    Bitmap tmp = mArray.valueAt(mArray.size() - 1);
                    if (mArray.size() == maxCount && tmp != null && mIsCut == false) {
                        float w = maxCount * thumbW - mTotalWidth;
                        w = w > 0 ? w : 0;
                        int width = (int) (thumbW - w);
                        width = width > 0? width : 1;
                        tmp = Bitmap.createBitmap(tmp, 0, 0, width, thumbH);
                        mArray.setValueAt(mArray.size() - 1, tmp);
                        mIsCut = true;
                    }
                    invalidate();
                }
            }
            return false;
        }
    });

    private OnChangeListener mListener;

    public void setListener(OnChangeListener listener) {
        mListener = listener;
    }

    public interface OnChangeListener {

        /**
         * 返回开始时间和结束时间  毫秒
         *
         * @param start
         * @param end
         */
        void OnChanged(long start, long end);

        /**
         * 暂停
         */
        void OnPause();

        /**
         * 进度
         */
        void OnSeek(long time);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
}
