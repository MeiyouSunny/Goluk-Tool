/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rd.veuisdk.videoeditor.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The timeline scroll view
 */
public class TimelineHorizontalScrollView extends HorizontalScrollView {
    public final static int PLAYHEAD_NORMAL = 1;

    private final List<ScrollViewListener> mScrollListenerList;
    private final Handler mHandler;

    private int mHalfParentWidth;
    private ScaleGestureDetector mScaleDetector;
    private int mLastScrollX;
    private boolean mIsScrolling;
    private boolean mAppScroll=true;
    private boolean mEnableUserScrolling;
    private String TAG = "TimelineHorizontalScrollView";
    // The runnable which executes when the scrolling ends
    private Runnable mScrollEndedRunnable = new Runnable() {
        @Override
        public void run() {
            mIsScrolling = false;

            for (ScrollViewListener listener : mScrollListenerList) {
                listener.onScrollEnd(TimelineHorizontalScrollView.this,
                        getScrollX(), getScrollY(), mAppScroll);
            }

            mAppScroll = true;
        }
    };

    /**
     * ??????????????????
     *
     * @return
     */
    public int getHalfParentWidth() {
        return mHalfParentWidth;
    }

    public void setHalfParentWidth(int halfWidth) {
        mHalfParentWidth = halfWidth;
        // childMargin = mHalfParentWidth - 5;
        halfLine = new Rect(mHalfParentWidth - 5, 0, mHalfParentWidth + 5,
                getHeight());
        invalidate();
    }

    private Drawable mHandLine;

    @SuppressWarnings("deprecation")
    public TimelineHorizontalScrollView(Context context, AttributeSet attrs,
                                        int defStyle) {
        super(context, attrs, defStyle);
        mEnableUserScrolling = true;
        cantouch = true;
        mScrollListenerList = new ArrayList<ScrollViewListener>();
        mHandler = new Handler();
        if (!isInEditMode()) {
            try {
                setHalfParentWidth(CoreUtils.getMetrics().widthPixels / 2);
            } catch (Exception ex) {
                throw new RuntimeException("Xpk SDK not initialized!");
            }
        }
        mHalfLinePaint.setColor(getResources().getColor(R.color.white));
        mHalfLinePaint.setStyle(Style.FILL);
        mHalfLinePaint.setAntiAlias(true);
        mHandLine = getResources().getDrawable(R.drawable.line_hand);
        mLongPressRunnable = new Runnable() {

            @Override
            public void run() {
                doingLonglistener = false;
//                Log.e(TAG, "????????????");
                mCounter--;
                // ???????????????0????????????????????????Runnable??????????????????down????????????
                if (mCounter > 0 || isReleased || isMoved)
                    return;
                if (null != mLongListener) {
                    mLongListener.onLong(mXPosition, mYPosition);
                    performLongClick();// ??????????????????
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            beforce = false;
                            mAppScroll = true;
                        }
                    }, 400);

                } else {

                }


            }
        };
    }

    public TimelineHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimelineHorizontalScrollView(Context context) {
        this(context, null, 0);
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

    private int mLongdownX = 0;
    private boolean beforce = false;

    private boolean doingLonglistener = false;

    private boolean cantouch = true;

    public void setCanTouch(boolean mcantouch) {
        cantouch = mcantouch;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (cantouch) {
            int re = ev.getAction();
            if (null != mViewTouchListener) {

                if (re == MotionEvent.ACTION_DOWN) {
                    beforce = true;
                    mLongdownX = (int) ev.getX();
                    mViewTouchListener.onActionDown();
                    mAppScroll = false;
                    doingLonglistener = true;

                } else if (re == MotionEvent.ACTION_MOVE && beforce) {
                    beforce = true;
                    mLongdownX = (int) ev.getX();
                    mViewTouchListener.onActionMove();
                    mAppScroll = false;
                    doingLonglistener = true;
                } else if ((re == MotionEvent.ACTION_UP || re == MotionEvent.ACTION_CANCEL)
                        && beforce) {
                    if (doingLonglistener) {
                        mViewTouchListener.onActionUp();
                    }
                    mLongdownX = (int) ev.getX();
                }
            }
            if (re == MotionEvent.ACTION_DOWN) {
                for (ScrollViewListener listener : mScrollListenerList) {
                    listener.onScrollBegin(this, getScrollX(), getScrollY(),
                            false);
                }
            }
            if (mEnableUserScrolling) {
                if (null != mScaleDetector)
                    mScaleDetector.onTouchEvent(ev);
                return super.onTouchEvent(ev);
            } else {
                if (mScaleDetector.isInProgress()) {
                    MotionEvent cancelEvent = MotionEvent.obtain(
                            SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    if (null != mScaleDetector)
                        mScaleDetector.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }
                return true;
            }
        } else {
            return true;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (null != mScaleDetector)
            mScaleDetector.onTouchEvent(ev);
        // if (null != mViewTouchListener
        // && ev.getAction() == MotionEvent.ACTION_DOWN) {
        // beforce = true;
        // mViewTouchListener.onActionDown();
        // }
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

        } else {
            mAppScroll = true;
            if (smooth) {
                smoothScrollTo(scrollX, 0);
            } else {
                scrollTo(scrollX, 0);
            }
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

    public void resetForce() {
        beforce = false;
        mLastScrollX--;
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (cantouch) {
            int scrollX = getScrollX();
            if (mLastScrollX != scrollX) {
                mLastScrollX = scrollX;

                if (!isMoved) {
                    if (Math.abs(mLongdownX - scrollX) > TOUCH_SLOP) {
                        // ???????????????????????????????????????
                        isMoved = true;
                    }
                }

                // Cancel the previous event
                mHandler.removeCallbacks(mScrollEndedRunnable);
                if (!beforce) {
                    // Post a new event
                    mHandler.postDelayed(mScrollEndedRunnable, 400);
                    int scrollY = getScrollY();
                    if (mIsScrolling) {
                        for (ScrollViewListener listener : mScrollListenerList) {
                            listener.onScrollProgress(this, scrollX, scrollY,
                                    mAppScroll);
                        }
                    } else {
                        mIsScrolling = true;
                    }
                }

            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        halfLine = new Rect(mHalfParentWidth - 5, 0, mHalfParentWidth + 5,
                getHeight());
    }

    private Rect halfLine, tempHalfLine = new Rect();
    private Paint mHalfLinePaint = new Paint();

    private boolean mdrawline = false;

    /**
     * ??????????????????
     *
     * @param isdraw
     */
    public void drawBaseline(boolean isdraw) {
        mdrawline = isdraw;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (null != halfLine) {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                    Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            int left = getScrollX();
            int mhalf = getScrollX() + mHalfParentWidth;
            if (mdrawline) {
                left = mhalf - 5;
                tempHalfLine
                        .set(left, halfLine.top, left + 10, halfLine.bottom);
                mHandLine.setBounds(tempHalfLine);
                // mHandLine.draw(canvas);
            } else {
                left = mhalf - 2;
                tempHalfLine.set(left, halfLine.top, left + 4, halfLine.bottom);
                canvas.drawRect(tempHalfLine, mHalfLinePaint);
            }
        }

    }

    public IViewTouchListener mViewTouchListener;

    public void setViewTouchListener(IViewTouchListener mstate) {
        mViewTouchListener = mstate;
    }

    /**
     * ????????????
     *
     * @author ADMIN
     */
    public interface onLongListener {
        /**
         * ????????????
         *
         * @param mXPosition
         * @param mYPosition
         */
        public void onLong(int mXPosition, int mYPosition);

    }

    private onLongListener mLongListener;

    public void setLongListener(onLongListener listener) {
        mLongListener = listener;
    }

    // ???????????????
    private boolean isMoved;
    // ???????????????
    private boolean isReleased;
    // ??????????????????????????????????????????????????????longpress???????????????
    private int mCounter;
    // ?????????runnable
    private Runnable mLongPressRunnable;
    // ???????????????
    private static final int TOUCH_SLOP = 10;

    private int mXPosition, mYPosition;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mXPosition = (int) event.getX();
        mYPosition = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCounter++;
            isReleased = false;
            isMoved = false;
            postDelayed(mLongPressRunnable,
                    ViewConfiguration.getLongPressTimeout());// ?????? 0.5??????????????????
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            // ?????????
            isReleased = true;
        }

        return super.dispatchTouchEvent(event);
    }

    private int mduration = 0;

    /**
     * ????????????????????????
     *
     * @param duration
     */
    public void setDuration(int duration) {
        mduration = duration;
    }

    /**
     * ????????????
     *
     * @param progress
     */

    public void setProgress(int progress) {
        int mScrollX = (int) (progress * mLineWidth / mduration);
        this.appScrollTo(mScrollX, true);

    }

    private double mLineWidth = 1.0;

    public void setLineWidth(int nWidth) {
        mLineWidth = nWidth + .0;
    }

    /**
     * @return ????????????????????????
     * @SuppressWarnings("deprecation")
     */
    public int getProgress() {
        int progress = (int) (this.getScrollX() * (mduration / mLineWidth));
        return progress;
    }

}
