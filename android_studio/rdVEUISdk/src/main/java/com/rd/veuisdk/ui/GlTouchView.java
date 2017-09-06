package com.rd.veuisdk.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.rd.lib.utils.CoreUtils;
import com.rd.recorder.ICameraZoomHandler;
import com.rd.recorder.api.RecorderCore;
import com.rd.veuisdk.R;

/**
 * 录制界面左右切换滤镜，点击摄像头聚焦
 */
public class GlTouchView extends FrameLayout {
    private FocuView mFocuView;
    private Paint mPaint = new Paint();
    private Rect mDstRect = new Rect();
    private boolean mDoEnd = false;// 判断手势离开时，是继续切换滤镜还是取消切换
    // 滤镜比例(从左到右 左边滤镜所占百分比)
    private double mFilterProportion = 0.01;
    private ValueAnimator mValueAnimator;
    private boolean mIsTouching = false;
    private final int MSG_END = 564, MSG_CANCEL = 565;
    private boolean mIsMoving = false;


    private float mXPosition = 0;
    private int mTargetX = 0, mCurrentX = 0;
    private boolean mIsLeftToRight = false;
    private int mOffX;
    private static boolean mEnableMoveFilter = true;

    public GlTouchView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.transparent_black));
        mFocuView = new FocuView(context, null);
        mFlignerDetector = new GestureDetector(context,
                new pressGestureListener());

    }

    private boolean mIsAdded = false;

    public void onPrepared() {
        if (null != mFocuView) {
            if (!mIsAdded) {
                mIsAdded = true;
                mFocuView.setVisibility(View.INVISIBLE);
                GlTouchView.this.addView(mFocuView);
            }
        }
    }


    public static void mEnableMoveFilter(boolean enable) {
        mEnableMoveFilter = enable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int re = event.getAction();
        if (null != mFlignerDetector) {
            mFlignerDetector.onTouchEvent(event);
        }
        // if (null != mZoomHandler) {
        // mZoomHandler.onTouch(event);
        // }
//		Log.e("onTouchEvent", "onTouchEvent: " + re + "........"
//				+ mEnableMoveFilter);
        if (mEnableMoveFilter) {
            if (re == MotionEvent.ACTION_DOWN) {
                mDoEnd = false;
                mXPosition = event.getX();
                mIsMoving = false;
                if (null != mValueAnimator) {
                    mValueAnimator.end();
                    mValueAnimator = null;
                }
                mHandler.removeMessages(MSG_END);
            } else if (re == MotionEvent.ACTION_MOVE) {
                float nx = event.getX();
                if (nx - mXPosition > 10) {// 从左到右
                    if (!mIsLeftToRight) {// 防止滑动----->右到左---->左到右
                        mIsMoving = false;
                        mIsLeftToRight = true;
                    }
                    int nleft = getLeft();
                    mOffX = (int) (nx - mXPosition);
                    double temp = (mOffX + 0.0f) / getWidth();
                    if (mFilterProportion != temp) {
                        mFilterProportion = temp;
                        mCurrentX = mOffX;
                        mDstRect.set(nleft, getTop(), nleft + mOffX, getBottom());
                        mTargetX = getRight();
                        if (!mIsMoving) {
                            mIsMoving = true;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChangeStart(false,
                                        mFilterProportion);
                            }
                        } else {
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChanging(false,
                                        mFilterProportion);
                            }
                        }
                        invalidate();
                    }

                } else if (mXPosition - nx > 10) {// 从右到左
                    if (mIsLeftToRight) {// 防止滑动---左到右-->右到左
                        mIsMoving = false;
                        mIsLeftToRight = false;
                    }
                    mTargetX = getLeft();
                    mOffX = (int) (mXPosition - nx);
                    double temp = 1 - ((mOffX + 0.0) / getWidth());
                    if (mFilterProportion != temp) {
                        mFilterProportion = temp;
                        mCurrentX = getWidth() - mOffX;
                        mDstRect.set(mCurrentX, getTop(), getRight(), getBottom());
                        if (!mIsMoving) {
                            mIsMoving = true;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChangeStart(true,
                                        mFilterProportion);
                            }
                        } else {
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChanging(true,
                                        mFilterProportion);
                            }
                        }

                        invalidate();
                    }
                }
            } else if (re == MotionEvent.ACTION_CANCEL
                    || re == MotionEvent.ACTION_UP) {

                float poffx = Math.abs(event.getX() - mXPosition);
                if ((mDoEnd && poffx >= getWidth() / 5)
                        || (!mDoEnd && poffx > getWidth() / 2)) {
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
                mFocuView.removeAll();
            }
        }
        return true;
    }


    private void getNewAnimationSet(final int current, final int target,
                                    final boolean mDoEnd) {


        // 创建一个加速器
        mValueAnimator = ValueAnimator.ofInt(current, target);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int t = (Integer) animation.getAnimatedValue();
                if (mDoEnd) {
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
                                mCcvlListener.onFilterChangeEnd();
                            }
                        } else {
                            int nleft = getLeft();
                            double temp = (tempTarget - nleft + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                mDstRect.set(nleft, getTop(), tempTarget, getBottom());
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(false,
                                            mFilterProportion);
                                }
                            }
                        }
                        invalidate();

                    } else {// 从右往左
                        if (tempTarget <= getLeft()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChangeEnd();
                            }
                        } else {
                            double temp = (tempTarget + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                mDstRect.set(tempTarget, getTop(), getRight(),
                                        getBottom());
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterChanging(true,
                                            mFilterProportion);
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
                                mCcvlListener.onFilterChangeCanceled();
                            }
                        } else {
                            mDstRect.set(tempTarget, getTop(), getRight(), getBottom());
                            int nleft = getLeft();
                            double temp = (tempTarget - nleft + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterCanceling(false,
                                            mFilterProportion);
                                }
                            }
                        }
                        invalidate();
                    } else {// 从右往左
                        if (tempTarget <= getLeft()) {
                            mIsMoving = false;
                            if (null != mCcvlListener) {
                                mCcvlListener.onFilterChangeCanceled();
                            }
                        } else {
                            mDstRect.set(getLeft(), getTop(), tempTarget, getBottom());
                            double temp = (tempTarget + 0.0f) / getWidth();
                            if (mFilterProportion != temp) {
                                mFilterProportion = temp;
                                if (null != mCcvlListener) {
                                    mCcvlListener.onFilterCanceling(true,
                                            mFilterProportion);
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

        /*
         * (non-Javadoc)
         *
         * @see
         * android.view.GestureDetector.SimpleOnGestureListener#onLongPress(
         * android.view.MotionEvent)
         */
        @Override
        public void onLongPress(MotionEvent e) {
            // Log.d(TAG, "onLongPress");
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {

            mDoEnd = true;
            // Log.e("onFling", "onFling: " + e1.getX() + ".............." +
            // e2.getX());

            // if (mCcvlListener != null) {
            // if (e1.getX() < e2.getX()) { // 向右fling
            // mCcvlListener.onSwitchFilterToRight();
            // } else { // 向左fling
            // mCcvlListener.onSwitchFilterToLeft();
            // }
            // }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.view.GestureDetector.SimpleOnGestureListener#onSingleTapUp
         * (android.view.MotionEvent)
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Log.e(TAG, "onSingleTapUp: " + "");
            if (mCcvlListener != null) {
                mCcvlListener.onSingleTapUp(e);
            }
            if (!RecorderCore.isFaceFront()) {
                int dx = (int) e.getX(), dy = (int) e.getY();
                int height = getHeight();
                int bh = getResources().getDimensionPixelSize(
                        R.dimen.text_size_20);
                int width = getWidth();
                int tw = CoreUtils.dpToPixel(70);
                if (dx < (width - tw) && dy > CoreUtils.dpToPixel(55)
                        && dy < (height - bh)) {
                    mFocuView.setLocation(dx, dy);
                }
            }
            return super.onSingleTapUp(e);
        }

        @Override
        // 双击
        public boolean onDoubleTap(MotionEvent e) {
            // Log.e(TAG, "onDoubleTap: " + "");
            mFocuView.removeAll();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // Log.e(TAG, "onDoubleTapEvent: " + "");
            // GlTouchView.this.removeCallbacks(cameraFocus);
            mFocuView.removeAll();
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
         *
         * @param e
         */
        void onSingleTapUp(MotionEvent e);

        /**
         * 双击
         *
         * @param e
         */
        void onDoubleTap(MotionEvent e);

        /**
         * 即将开始切换滤镜(准备同时绘制两个滤镜)
         *
         * @param leftORight        true,从右往左; false 从左往右
         * @param mFilterProportion 左边滤镜所占百分比
         */
        void onFilterChangeStart(boolean leftORight, double mFilterProportion);

        /**
         * 左右实时滑动滤镜
         *
         * @param leftORight        true,从右往左; false 从左往右
         * @param mFilterProportion 左边滤镜所占百分比
         */
        void onFilterChanging(boolean leftORight, double mFilterProportion);

        /**
         * 滑动滤镜结束(绘制完整的单个滤镜)
         */
        void onFilterChangeEnd();

        /**
         * 手势离开取消滤镜
         *
         * @param leftORight        true,从右往左; false 从左往右
         * @param mFilterProportion 左边滤镜所占百分比
         */

        void onFilterCanceling(boolean leftORight, double mFilterProportion);

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
        removeView(mFocuView);
        mFlignerDetector = null;
        mFocuView = null;
        mCcvlListener = null;
    }
}
