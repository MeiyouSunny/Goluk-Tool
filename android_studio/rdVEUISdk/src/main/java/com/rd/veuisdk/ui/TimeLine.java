package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

import com.rd.veuisdk.R;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 字幕、特效滚动轴 (父容器）
 */
public class TimeLine extends HorizontalScrollView {
    public final static int PLAYHEAD_NORMAL = 1;
    // 移动的阈值
    private static final int TOUCH_SLOP = 10;
    // Instance variables
    private final List<ScrollViewListener> mScrollListenerList;
    private final Handler mHandler;
    private final int mHalfParentWidth;
    private ScaleGestureDetector mScaleDetector;
    private int mLastScrollX;
    private boolean mIsScrolling;
    private boolean mAppScroll;
    private boolean mEnableUserScrolling;
    private int mLongDownX = 0;
    private boolean bFocused = false;
    private Rect mHalfLineRect, mTempHalfLineRect = new Rect();
    private Paint mPaint = new Paint();

    private int mChildMargin;
    // 是否移动了
    private boolean isMoved;
    // 是否释放了
    private boolean isReleased;
    // 计数器，防止多次点击导致最后一次形成longpress的时间变短
    private int mCounter;
    // 长按的runnable
    private Runnable mLongPressRunnable;


    private int x, y;
    private Paint mPText = new Paint();
    private String mText;
    private int textWidth = 0;

    public TimeLine(Context context) {
        this(context, null, 0);
    }

    public TimeLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("deprecation")
    public TimeLine(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.main_orange));
        mEnableUserScrolling = true;
        mScrollListenerList = new ArrayList<ScrollViewListener>();
        mHandler = new Handler();

        // Compute half the width of the screen (and therefore the parent view)
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        final Display display = wm.getDefaultDisplay();
        mHalfParentWidth = display.getWidth() / 2;
        mChildMargin = mHalfParentWidth - 2;
        mLongPressRunnable = new Runnable() {

            @Override
            public void run() {
                mCounter--;
                // 计数器大于0，说明当前执行的Runnable不是最后一次down产生的。
                if (mCounter > 0 || isReleased || isMoved)
                    return;
                performLongClick();// 回调长按事件
            }
        };
    }


    // The runnable which executes when the scrolling ends
    private Runnable mScrollEndedRunnable = new Runnable() {
        @Override
        public void run() {
            mIsScrolling = false;

            for (ScrollViewListener listener : mScrollListenerList) {
                listener.onScrollEnd(TimeLine.this, getScrollX(), getScrollY(),
                        mAppScroll);
            }

            mAppScroll = false;
        }
    };

    /**
     * 中间线的位置
     *
     * @return
     */
    public int getHalfParentWidth() {
        return mChildMargin;
    }

    /**
     * Invoked to enable/disable user scrolling (as opposed to programmatic
     * scrolling)
     *
     * @param enable true to enable user scrolling
     */
    public void enableUserScrolling(boolean enable) {
        mEnableUserScrolling = enable;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (null != mViewTouchListener) {
            int re = ev.getAction();
            if (re == MotionEvent.ACTION_DOWN) {
                bFocused = true;
                mLongDownX = (int) ev.getX();
                mViewTouchListener.onActionDown();
                mAppScroll = false;
            } else if (re == MotionEvent.ACTION_MOVE) {
                bFocused = true;
                mLongDownX = (int) ev.getX();
                mViewTouchListener.onActionMove();
                mAppScroll = false;
            } else if (re == MotionEvent.ACTION_CANCEL
                    || re == MotionEvent.ACTION_UP) {
                mViewTouchListener.onActionUp();
                bFocused = false;
                mLongDownX = (int) ev.getX();
                mAppScroll = true;
            }
        }
        if (mEnableUserScrolling) {
            if (null != mScaleDetector)
                mScaleDetector.onTouchEvent(ev);
            return super.onTouchEvent(ev);
        } else {
            if (mScaleDetector.isInProgress()) {
                final MotionEvent cancelEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_CANCEL, 0, 0, 0);
                if (null != mScaleDetector)
                    mScaleDetector.onTouchEvent(cancelEvent);
                cancelEvent.recycle();
            }
            return true;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (null != mScaleDetector)
            mScaleDetector.onTouchEvent(ev);
        if (null != mViewTouchListener
                && ev.getAction() == MotionEvent.ACTION_DOWN) {
            bFocused = true;
            mViewTouchListener.onActionDown();
        }
        return false;

    }

    /**
     * @param listener The scale listener
     */
    public void setScaleListener(
            ScaleGestureDetector.SimpleOnScaleGestureListener listener) {
        mScaleDetector = new ScaleGestureDetector(getContext(), listener);
    }

    /**
     * @param listener The listener
     */
    public void addScrollListener(ScrollViewListener listener) {
        mScrollListenerList.add(listener);
    }

    /**
     * @param listener The listener
     */
    public void removeScrollListener(ScrollViewListener listener) {
        mScrollListenerList.remove(listener);
    }

    /**
     * @return true if scrolling is in progress
     */
    public boolean isScrolling() {
        return mIsScrolling;
    }

    /**
     * The app wants to scroll (as opposed to the user)
     *
     * @param scrollX Horizontal scroll position
     * @param smooth  true to scroll smoothly
     */
    public void appScrollTo(int scrollX, boolean smooth) {
        if (getScrollX() == scrollX) {
            return;
        }

        mAppScroll = true;

        if (smooth) {
            smoothScrollTo(scrollX, 0);
        } else {
            scrollTo(scrollX, 0);
        }
    }

    /**
     * The app wants to scroll (as opposed to the user)
     *
     * @param scrollX Horizontal scroll offset
     * @param smooth  true to scroll smoothly
     */
    public void appScrollBy(int scrollX, boolean smooth) {

        mAppScroll = true;

        if (smooth) {
            smoothScrollBy(scrollX, 0);
        } else {
            scrollBy(scrollX, 0);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        final int scrollX = getScrollX();
        if (mLastScrollX != scrollX) {
            mLastScrollX = scrollX;

            if (!isMoved) {
                if (Math.abs(mLongDownX - scrollX) > TOUCH_SLOP) {
                    // 移动超过阈值，则表示移动了
                    isMoved = true;
                }
            }

            // Cancel the previous event
            mHandler.removeCallbacks(mScrollEndedRunnable);

            if (!bFocused) {
                mHandler.postDelayed(mScrollEndedRunnable, 400);

                final int scrollY = getScrollY();
                if (mIsScrolling) {
                    for (ScrollViewListener listener : mScrollListenerList) {
                        listener.onScrollProgress(this, scrollX, scrollY,
                                mAppScroll);
                    }
                } else {
                    mIsScrolling = true;

                    for (ScrollViewListener listener : mScrollListenerList) {
                        listener.onScrollBegin(this, scrollX, scrollY,
                                mAppScroll);
                    }
                }
            }

        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        if (null != mHalfLineRect) {
            mTempHalfLineRect.set(getScrollX() + mHalfLineRect.left, mHalfLineRect.top,
                    mHalfLineRect.right + getScrollX(), mHalfLineRect.bottom);
            canvas.drawRect(mTempHalfLineRect, mPaint);

            if (!TextUtils.isEmpty(mText)) {
                canvas.drawText(mText, mTempHalfLineRect.left - textWidth / 2,
                        mHalfLineRect.bottom - 150, mPText);
            }
        }

    }


    /**
     * 设置时间线要显示的文本内容
     *
     * @param str
     */
    public void setText(String str) {
        mPText.setAntiAlias(true);
        mPText.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.text_size_18));
        mPText.setColor(getResources().getColor(R.color.main_orange));
        this.mText = str;
        textWidth = (int) mPText.measureText(str);

    }

    public IViewTouchListener mViewTouchListener;

    public void setViewTouchListener(IViewTouchListener mstate) {
        mViewTouchListener = mstate;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        x = (int) event.getX();
        y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCounter++;
            isReleased = false;
            isMoved = false;
            postDelayed(mLongPressRunnable,
                    ViewConfiguration.getLongPressTimeout());// 按下 0.5秒后调用线程
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            // 释放了
            isReleased = true;
        }

        return super.dispatchTouchEvent(event);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHalfLineRect = new Rect(mChildMargin, 0, mChildMargin + 4, getHeight() - 80);
    }

}
