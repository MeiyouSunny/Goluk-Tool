package com.rd.veuisdk.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import androidx.core.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.rd.lib.utils.CoreUtils;
import com.rd.recorder.ICameraZoomHandler;
import com.rd.recorder.api.RecorderCore;
import com.rd.veuisdk.R;

/**
 * 录制界面左右切换滤镜，点击摄像头聚焦
 */
public class GlTouchView extends View {
    private String TAG = "GlTouchView";
    private Rect mDstRect = new Rect();
    private boolean mDoEnd = false;// 判断手势离开时，是继续切换滤镜还是取消切换
    // 滤镜比例(从左到右 左边滤镜所占百分比)
    private double mFilterProportion = 0.01;
    private ValueAnimator mValueAnimator;
    private final int MSG_END = 564, MSG_CANCEL = 565;
    private boolean mIsMoving = false;


    private float mDownFocusX = 0;
    private int mTargetX = 0, mCurrentX = 0;
    private boolean mIsLeftToRight = false;
    private int mOffX;
    private boolean mEnableMoveFilter = false;


    private Paint mPaint;
    private int mXPosition, mYPosition;
    private final int mRadius;

    private boolean needDraw = false;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (needDraw) {
            canvas.drawCircle(mXPosition, mYPosition, mRadius, mPaint);
            canvas.drawCircle(mXPosition, mYPosition, 15, mPaint);
        }
    }

    private void setLocation(int x, int y) {
        needDraw = true;
        this.mXPosition = x;
        this.mYPosition = y;
        removeCallbacks(mRunnable);
        postDelayed(mRunnable, 800);
        invalidate();
    }

    private void removeAll() {
        removeCallbacks(mRunnable);
        alphaGone();
    }

    private void alphaGone() {
        needDraw = false;
        invalidate();
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            alphaGone();
        }
    };


    public GlTouchView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        mFlignerDetector = new GestureDetector(context, new pressGestureListener());
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.white));
        mRadius = CoreUtils.dpToPixel(35);
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

    }


    public void onPrepared() {
        needDraw = false;
    }


    public void mEnableMoveFilter(boolean enable) {
        mEnableMoveFilter = enable;
    }

    private boolean isZoomTouch = false;
    private int mTouchEventCounter = 0;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mFlignerDetector) {
            mFlignerDetector.onTouchEvent(event);
        }
        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN) {
            mTouchEventCounter = 0;
            if (mEnableMoveFilter) {
                mDoEnd = false;
                mDownFocusX = event.getX();
                mIsMoving = false;
                if (null != mValueAnimator) {
                    mValueAnimator.end();
                    mValueAnimator = null;
                }
                mHandler.removeMessages(MSG_END);
            }
        }
        mTouchEventCounter++;
        if (mTouchEventCounter == 9) {
            //新增i ==9防止双指时，刚开始的几次取MotionEventCompat.getPointerCount(event)值！=2
            int pCount = MotionEventCompat.getPointerCount(event);
            isZoomTouch = pCount > 1;
        }
        if (mTouchEventCounter < 9) {
            return true;
        }
        if (isZoomTouch) {
            //缩放相机
            if (null != mZoomHandler) {
                mZoomHandler.onTouch(event);
            }
        } else {
//            if (pCount <= 1 || (re == MotionEvent.ACTION_CANCEL || re == MotionEvent.ACTION_UP)) {
            //左右滑动切换相机滤镜
            if (mEnableMoveFilter) {
                if (eventAction == MotionEvent.ACTION_MOVE) {
                    float focusX = event.getX();
                    if (focusX - mDownFocusX > 10) {// 从左到右
                        if (!mIsLeftToRight) {// 当前右到左---->左到右
                            mIsMoving = false;
                            mIsLeftToRight = true;
                        }
                        int nleft = getLeft();
                        mOffX = (int) (focusX - mDownFocusX);
                        double temp = (mOffX + 0.0f) / getWidth();
                        if (mFilterProportion != temp) {
                            mFilterProportion = temp;
                            mCurrentX = mOffX;
                            mDstRect.set(nleft, getTop(), nleft + mOffX, getBottom());
                            mTargetX = getRight();
                            if (!mIsMoving) {
                                mIsMoving = true;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChangeStart(mIsLeftToRight,
                                            mFilterProportion);
                                }
                            } else {
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(mIsLeftToRight,
                                            mFilterProportion);
                                }
                            }
                            invalidate();
                        }
                    } else if (mDownFocusX - focusX > 10) {// 右到左
                        if (mIsLeftToRight) {// 防止滑动--左到右-->右到左
                            mIsMoving = false;
                            mIsLeftToRight = false;
                        }
                        mTargetX = getLeft();
                        mOffX = (int) (mDownFocusX - focusX);
                        double temp = 1 - ((mOffX + 0.0) / getWidth());
                        if (mFilterProportion != temp) {
                            mFilterProportion = temp;
                            mCurrentX = getWidth() - mOffX;
                            mDstRect.set(mCurrentX, getTop(), getRight(), getBottom());
                            if (!mIsMoving) {
                                mIsMoving = true;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChangeStart(mIsLeftToRight,
                                            mFilterProportion);
                                }
                            } else {
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(mIsLeftToRight,
                                            mFilterProportion);
                                }
                            }
                            invalidate();
                        }
                    }
                } else if (eventAction == MotionEvent.ACTION_CANCEL
                        || eventAction == MotionEvent.ACTION_UP) {

                    float poffx = Math.abs(event.getX() - mDownFocusX);
                    if ((mDoEnd && poffx >= getWidth() / 5) || (!mDoEnd && poffx > getWidth() / 2)) {
                        if (mIsMoving) {// 松开手势时，执行切换
                            getNewAnimationSet(mCurrentX, mTargetX, true);
                        }
                    } else {
                        if (mIsMoving) {// 松开手势时，取消切换
                            if (mIsLeftToRight) {
                                mTargetX = 0;
                            } else {
                                mTargetX = getRight();
                            }
                            getNewAnimationSet(mCurrentX, mTargetX, false);
                        }
                    }
                    removeAll();
                }
            }
        }

        if (eventAction == MotionEvent.ACTION_UP || eventAction == MotionEvent.ACTION_CANCEL) {
            isZoomTouch = false;
        }
        return true;
    }


    private void getNewAnimationSet(final int current, final int target, final boolean doEnd) {
        // 创建一个加速器
        mValueAnimator = ValueAnimator.ofInt(current, target);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int t = (Integer) animation.getAnimatedValue();
                if (doEnd) {
                    mHandler.removeMessages(MSG_END);
                    mHandler.obtainMessage(MSG_END, current, t).sendToTarget();
                } else {
                    mHandler.removeMessages(MSG_CANCEL);
                    if (current < target) {
                        mHandler.obtainMessage(MSG_CANCEL, current, t + 1)
                                .sendToTarget();
                    } else {
                        mHandler.obtainMessage(MSG_CANCEL, current, t - 1)
                                .sendToTarget();
                    }
                }
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());// 匀速移动
        mValueAnimator.setDuration(200);
        mValueAnimator.start();
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_END: {// 松开手势，自动完成剩余部分的滑动
                    int tempTarget = msg.arg2;
                    int cur = msg.arg1;
                    if (cur < tempTarget) {// 从左往右
                        if (tempTarget >= getRight()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChanging(true, 1.0);
                                mCcvlListener.onFilterChangeEnd();
                            }
                        } else {
                            int nleft = getLeft();
                            double temp = (tempTarget - nleft + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                mDstRect.set(nleft, getTop(), tempTarget, getBottom());
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(true, mFilterProportion);
                                }
                            }
                        }
                        invalidate();
                    } else if (cur != tempTarget) {// 从右往左
                        if (tempTarget <= getLeft()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChanging(false, 0);
                                mCcvlListener.onFilterChangeEnd();
                            }
                        } else {
                            double temp = (tempTarget + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                mDstRect.set(tempTarget, getTop(), getRight(), getBottom());
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(false, mFilterProportion);
                                }
                            }
                        }
                        invalidate();
                    }
                }
                break;
                case MSG_CANCEL: {// 响应松开时，取消切换
                    int tempTarget = msg.arg2; // 定速取消 ,,变量 ->1080
                    int cur = msg.arg1;// 离开时手势的位置
                    if (cur < tempTarget) {// 滑动时从右到左--->取消时从左到右
                        if (tempTarget >= getRight()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterCanceling(true, 1);
                                mCcvlListener.onFilterChangeCanceled();
                            }
                        } else {
                            mDstRect.set(tempTarget, getTop(), getRight(), getBottom());
                            int nleft = getLeft();
                            double temp = (tempTarget - nleft + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterCanceling(true, mFilterProportion);
                                }
                            }
                        }
                        invalidate();
                    } else {// 从右往左
                        if (tempTarget <= getLeft()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterCanceling(false, 0);
                                mCcvlListener.onFilterChangeCanceled();
                            }
                        } else {
                            mDstRect.set(getLeft(), getTop(), tempTarget, getBottom());
                            double temp = (tempTarget + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterCanceling(false, mFilterProportion);
                                }
                            }
                        }
                        invalidate();
                    }
                }
                break;
                default:
                    break;
            }
        }
    };

    /**
     * 手势listener
     *
     * @author abreal
     */
    private class pressGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            mDoEnd = true;
            if (mTouchEventCounter < 9) {
                if (mEnableMoveFilter) {
                    if (e1.getX() < e2.getX()) { // 向右fling
                        getNewAnimationSet(0, getRight(), true);
                    } else { // 向左fling
                        getNewAnimationSet(getRight(), 0, true);
                    }
                } else {
                    if (e1.getX() < e2.getX()) { // 向右fling
                        mCcvlListener.onSwitchFilterToRight();
                    } else { // 向左fling
                        mCcvlListener.onSwitchFilterToLeft();
                    }
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mCcvlListener != null) {
                mCcvlListener.onSingleTapUp(e);
            }
            if (!RecorderCore.isFaceFront()) {
                int dx = (int) e.getX(), dy = (int) e.getY();
                int height = getHeight();
                int width = getWidth();
                if (mRadius < dx && dx < (width - mRadius) && dy > mRadius && dy < (height - mRadius)) {
                    setLocation(dx, dy);
                }
            }
            return super.onSingleTapUp(e);
        }

        @Override
        // 双击
        public boolean onDoubleTap(MotionEvent e) {
            removeAll();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            removeAll();
            if (mCcvlListener != null) {
                mCcvlListener.onDoubleTap(e);
            }
            return super.onDoubleTapEvent(e);
        }
    }

    private GestureDetector mFlignerDetector;

    public void setViewHandler(CameraCoderViewListener ccvl) {
        mCcvlListener = ccvl;
    }

    protected CameraCoderViewListener mCcvlListener;
    private ICameraZoomHandler mZoomHandler;

    /**
     * 切换摄像头特效Listener
     *
     * @author abreal
     */
    public interface CameraCoderViewListener {
        /**
         * 向左切换
         */
        void onSwitchFilterToLeft();

        /**
         * 向右切换
         */
        void onSwitchFilterToRight();

        /**
         * 单击
         */
        void onSingleTapUp(MotionEvent e);

        /**
         * 双击
         */
        void onDoubleTap(MotionEvent e);

        /**
         * 即将开始切换滤镜(准备同时绘制两个滤镜)
         *
         * @param leftToRight      true 从左往右，否则从右往左;
         * @param filterProportion 左边滤镜所占百分比
         */
        void onFilterChangeStart(boolean leftToRight, double filterProportion);

        /**
         * 左右实时滑动滤镜
         *
         * @param leftToRight       true 从左往右，否则从右往左;
         * @param mFilterProportion 左边滤镜所占百分比
         */
        void onFilterChanging(boolean leftToRight, double mFilterProportion);

        /**
         * 滑动滤镜结束(绘制完整的单个滤镜)
         */
        void onFilterChangeEnd();

        /**
         * 手势离开取消滤镜
         *
         * @param leftToRight       true 从左往右，否则从右往左;
         * @param mFilterProportion 左边滤镜所占百分比
         */

        void onFilterCanceling(boolean leftToRight, double mFilterProportion);

        /**
         * 取消切换滤镜
         */
        void onFilterChangeCanceled();

    }

    public void setZoomHandler(ICameraZoomHandler hlrZoom) {
        mZoomHandler = hlrZoom;
    }

    public void recycle() {
        if (null != mValueAnimator) {
            mValueAnimator.end();
            mValueAnimator = null;
        }
        needDraw = false;
        mFlignerDetector = null;
        mCcvlListener = null;
    }
}
