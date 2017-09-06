/**
 * s * Copyright (c) 2011, Animoto Inc.All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.rd.veuisdk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.models.Scene;

import java.util.ArrayList;
import java.util.Collections;

/**
 * base on com.animoto.android.views.DraggableGridView
 *
 * @author Tom quinn
 * @author abreal
 *         支持拖拽位置
 */
@SuppressLint("WrongCall")
public class DraggableGridView extends ViewGroup {

    public static final String TAG = "DraggableGridView";

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int TAG_KEY_NEED_HIDE_CLOSE_BUTTON = 1;

    // layout vars
    public static final float mChildRatio = .9f;

    private int mColumnCount, mChildSize, mPadding, mDpi, mScrollPosition = 0;
    private float mLastDelta = 0;
    private Handler mHandler = new Handler();
    // dragging vars
    private int mDraggedIndex = -1, mLastXPosition = -1, mLastYPosition = -1, mLastTarget = -1;
    private boolean mTempEnabled = true, mIsTouching = false, mTouchAndMoved = false;
    // anim vars
    public static int mAnimDuration = 150;
    protected ArrayList<Integer> mNewPositions = new ArrayList<Integer>();
    // listeners
    protected RearrangeListAdapater mRearrageAdapter;
    protected OnClickListener mSecondaryOnClickListener;
    private OnItemClickListener mOnItemClickListener;
    private int mOrientation = HORIZONTAL;// 方向，水平还是垂直
    private AdapterDataSetObserver mDataSetObserver;
    private int mItemCount, mOldItemCount;
    private int mFixedScroll = 0; // 为了完整显示第一个item,偏移后的scroll量
    private int mItemWidth;
    private int mItemHeight;
    private int mItemCloseButtonHorizontalPadding;
    private int mItemCloseButtonVerticalPadding;
    private boolean mAddItem;
    private ArrayList<Scene> mArrSceneInfo;

    /**
     * Rearrange item listener
     *
     * @author abreal
     */
    public interface RearrangeListAdapater extends ListAdapter {
        void onRearrange(int oldIndex, int newIndex);

        void onTouched(int index);

        int getItemCount();
    }

    public interface onLonglistener {

        void onLong(int index, View item);

        void onCancel();

    }

    private onLonglistener mOnLongListener;

    public void setLongLisenter(onLonglistener longListener) {
        mOnLongListener = longListener;
    }


    /**
     * constructor
     *
     * @param context
     * @param attrs
     */
    public DraggableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setListeners();
        mHandler.removeCallbacks(mUpdateTask);
        mHandler.postAtTime(mUpdateTask, SystemClock.uptimeMillis() + 500);
        setChildrenDrawingOrderEnabled(true);

        if (!isInEditMode()) {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay()
                    .getMetrics(metrics);
            mDpi = metrics.densityDpi;
        } else {
            mDpi = 160;
        }

        mChildSize = CoreUtils.dpToPixel(90);
        mPadding = CoreUtils.dpToPixel(5);
        mItemCloseButtonHorizontalPadding = CoreUtils.dpToPixel(5);
        mItemCloseButtonVerticalPadding = CoreUtils.dpToPixel(5);
        // GestureDetector myGesDetector = new GestureDetector(listener)
    }

    /**
     * 设置Item大小（dp为单位)
     *
     * @param fItemSize
     * @param fItemSpace
     */
    public void setItemSize(float fItemSize, float fItemSpace) {
        mChildSize = CoreUtils.dpToPixel(fItemSize);
        mPadding = CoreUtils.dpToPixel(fItemSpace);
    }

    /**
     * 设置Item大小(通过dim)
     *
     * @param nWidthDimId
     * @param nHeightDimId
     */
    public void setItemSize(int nWidthDimId, int nHeightDimId) {
        mItemWidth = Math.round(getResources().getDimension(nWidthDimId));
        mItemHeight = Math.round(getResources().getDimension(nHeightDimId));
        mChildSize = Math.max(mItemWidth, mItemHeight);
    }

    /**
     * 设置item关闭边距(通过dim)
     *
     * @param nPaddingHorizontalDimId
     * @param nPaddingVerticalDimId
     */
    public void setItemCloseButtonPadding(int nPaddingHorizontalDimId,
                                          int nPaddingVerticalDimId) {
        mItemCloseButtonHorizontalPadding = Math.round(getResources()
                .getDimension(nPaddingHorizontalDimId));
        mItemCloseButtonVerticalPadding = Math.round(getResources()
                .getDimension(nPaddingVerticalDimId));
    }

    protected void setListeners() {
        // this.setOnClickListener(this);
        // setOnLongClickListener(this);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mSecondaryOnClickListener = l;
    }

    protected Runnable mUpdateTask = new Runnable() {
        public void run() {
            if (mDraggedIndex != -1) {
                int nMinCheckCoor, nMaxCheckCoor;
                int nCheckCoor;
                if (mOrientation == VERTICAL) {
                    nCheckCoor = mLastYPosition;
                    nMinCheckCoor = mChildSize / 3;
                    nMaxCheckCoor = getBottom() - getTop() - mChildSize / 3;
                } else {
                    nCheckCoor = mLastXPosition;
                    nMinCheckCoor = mChildSize / 3;
                    nMaxCheckCoor = getRight() - getLeft() - mChildSize / 3;
                }

                if (nCheckCoor < nMinCheckCoor && mScrollPosition > 0) {
                    mScrollPosition -= 20;
                } else if (nCheckCoor > nMaxCheckCoor
                        && mScrollPosition < getMaxScroll()) {
                    mScrollPosition += 20;
                }
                // Log.d(TAG, String.format("check:%d,min:%d,max:%d",
                // nCheckCoor,
                // nMinCheckCoor, nMaxCheckCoor));
            } else if (mLastDelta != 0 && !mIsTouching) {
                mScrollPosition += mLastDelta;
                mLastDelta *= .5;
                if (Math.abs(mLastDelta) < .25)
                    mLastDelta = 0;
            }
            if (clampScroll() || mDraggedIndex != -1) {
                mHandler.postDelayed(this, 25);
            } else if (mFixedScroll != 0) {
                mScrollPosition = mFixedScroll;
                mFixedScroll = 0;
                mHandler.postDelayed(this, 25);
                return;
            }
            onLayout(true, getLeft(), getTop(), getRight(), getBottom());
        }
    };

    // OVERRIDES
    @Override
    public void addView(View child) {
        super.addView(child);
        mNewPositions.add(-1);
    }

    ;

    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
        mNewPositions.remove(index);
    }

    ;

    // LAYOUT
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Log.e("onLayout-" + this.toString(), l + "..." + t + ".." + r +
        // "...."
        // + b);

        if (null != mRearrageAdapter) {
            if (mOldItemCount != mItemCount) {
                DraggableGridViewItem itemView;
                View contentView;
                while (mItemCount < getChildCount()) {
                    itemView = (DraggableGridViewItem) this
                            .getChildAt(getChildCount() - 1);
                    if (null != itemView) {
                        itemView.removeAllViews();
                        itemView = null;
                    }
                    this.removeViewAt(getChildCount() - 1);
                }
                if (mOrientation == HORIZONTAL) {
                    mColumnCount = mItemCount;
                }

                for (int nTmp = 0; nTmp < mItemCount; nTmp++) {
                    if (nTmp < getChildCount()) {
                        itemView = (DraggableGridViewItem) this
                                .getChildAt(nTmp);
                    } else {
                        itemView = null;
                    }

                    contentView = mRearrageAdapter.getView(nTmp, itemView,
                            this);
                    if (itemView == null) {
                        itemView = new DraggableGridViewItem(this.getContext(),
                                contentView);
                        this.addView(itemView);
                    } else {
                        contentView = itemView.getContentView();
                    }
                    ViewGroup.LayoutParams p = contentView.getLayoutParams();

                    p.width = mItemWidth;
                    p.height = mItemHeight;
                    contentView.setLayoutParams(p);

                    int childWidthSpec = getChildMeasureSpec(
                            MeasureSpec.makeMeasureSpec(mItemWidth,
                                    MeasureSpec.EXACTLY), 0, p.width);

                    int childHeightSpec = getChildMeasureSpec(
                            MeasureSpec.makeMeasureSpec(mItemHeight,
                                    MeasureSpec.EXACTLY), 0, p.height);
                    contentView.measure(childWidthSpec, childHeightSpec);

                    Point xy = getCoorFromIndex(nTmp);
                    // TODO
                    if (mOrientation == HORIZONTAL) {
                        itemView.layout(xy.x, xy.y - mPadding, xy.x
                                + mItemWidth + mPadding, xy.y + mItemHeight);

                    } else {
                        itemView.layout(xy.x, xy.y, xy.x + mItemWidth
                                + mPadding, xy.y + mItemHeight + mPadding);
                    }

                }
                mOldItemCount = mItemCount;
                // this.setChildrenDrawingCacheEnabled(true);
                // this.setChildrenDrawnWithCacheEnabled(true);
                mHandler.postDelayed(mUpdateTask, 25);
            } else {
                try {
                    for (int i = 0; i < mItemCount; i++) {
                        if (i != mDraggedIndex) {
                            Point xy = getCoorFromIndex(i);
                            View child = getChildAt(i);
                            if (mOrientation == HORIZONTAL) {
                                child.layout(xy.x, xy.y - mPadding, xy.x
                                        + mItemWidth + mPadding, xy.y
                                        + mItemHeight);
                            } else {
                                child.layout(xy.x, xy.y, xy.x + mItemWidth
                                        + mPadding, xy.y + mItemHeight
                                        + mPadding);
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * Ensures correct size of the widget.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = mItemWidth;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = mItemHeight;

        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }

        // 如果是竖向
        if (mOrientation != HORIZONTAL) {
            // compute width of view, in dp
            float w = width / (mDpi / 160f);

            // determine number of columns, at least 2
            mColumnCount = 2;
            int sub = 120;
            w -= 140;
            while (w > 0) {
                mColumnCount++;
                w -= sub;
                sub += 20;
            }
            // determine mChildSize and mPadding, in px
            mChildSize = width / mColumnCount;
            mChildSize = Math.round(mChildSize * mChildRatio);
            mPadding = (width - (mChildSize * mColumnCount)) / (mColumnCount + 1);
        } else {

            // 横向
            height = mPadding * 2 + mItemHeight;
        }
        setMeasuredDimension(width, height);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mDraggedIndex == -1)
            return i;
        else if (i == childCount - 1)
            return mDraggedIndex;
        else if (i >= mDraggedIndex)
            return i + 1;
        return i;
    }

    /**
     * 获取指定View范围内的child索引位置
     *
     * @param x
     * @param y
     * @return
     */
    public int getIndexFromCoor(int x, int y) {
        int col, row;
        if (mOrientation == VERTICAL) {
            col = getColOrRowFromCoor(x);
            row = getColOrRowFromCoor(y + mScrollPosition);
            if (col > 4) {
                col = 4;
            }
        } else { // 水平
            col = getColOrRowFromCoor(x + mScrollPosition);
            row = getColOrRowFromCoor(y);
        }
        if (col == -1 || row == -1) // touch is between columns or rows
            return -1;
        int index = row * mColumnCount + col;
        if (index >= getChildCount())
            return -1;
        return index;
    }

    protected int getColOrRowFromCoor(int coor) {
        coor -= mPadding;
        for (int i = 0; coor >= 0; i++) {
            if (coor < mChildSize)
                return i;
            coor -= (mChildSize);
        }
        return -1;
    }

    protected int getTargetFromCoor(int x, int y) {
        // touch is between rows{
        if (getColOrRowFromCoor(y + mScrollPosition) == -1)
            return -1;
        // if (getIndexFromCoor(x, y) != -1) //touch on top of another visual
        // return -1;

        int leftPos = getIndexFromCoor(x - (mChildSize / 4), y);
        int rightPos = getIndexFromCoor(x + (mChildSize / 4), y);
        if (leftPos == -1 && rightPos == -1) // touch is in the middle of
            return -1; // nowhere
        if (leftPos == rightPos) // touch is in the middle of a visual
            return -1;

        int target = -1;
        if (rightPos > -1)
            target = rightPos;
        else if (leftPos > -1)
            target = leftPos + 1;
        if (mDraggedIndex < target)
            return target - 1;

        // Toast.makeText(getContext(), "Target: " + target + ".",
        // Toast.LENGTH_SHORT).show();
        return target;
    }

    protected Point getCoorFromIndex(int index) {
        int col = index % mColumnCount;
        int row = index / mColumnCount;
        if (mOrientation == VERTICAL) {
            return new Point(mPadding + (mChildSize + mPadding) * col, (mChildSize)
                    * row - mScrollPosition);
        } else {
            return new Point((mChildSize) * index - mScrollPosition, mPadding);
        }
    }

    public int getIndexOf(View child) {
        for (int i = 0; i < getChildCount(); i++)
            if (getChildAt(i) == child)
                return i;
        return -1;
    }

    /**
     * 响应
     */
    public void onClick(View view) {
        if (view instanceof DraggableGridViewItem) {
            int nRemoveIndex = getIndexOf(view);

            if (nRemoveIndex >= 0) {
                if (mOnItemClickListener != null) {
                    DraggableGridViewItem itemView = (DraggableGridViewItem) view;
                    mOnItemClickListener.onItemClick(null,
                            itemView.getContentView(), nRemoveIndex, -1);
                }
            }
        }
        // else if (mTempEnabled) {
        // if (mSecondaryOnClickListener != null)
        // mSecondaryOnClickListener.onClick(view);
        // int lastindex = getLastIndex();
        // if (mOnItemClickListener != null && lastindex != -1) {
        // mOnItemClickListener.onItemClick(null, getChildAt(lastindex),
        // lastindex, lastindex / mColumnCount);
        // }
        // } else {
        // int lastindex = getLastIndex();
        // if (mOnItemClickListener != null && lastindex != -1) {
        // mOnItemClickListener.onItemClick(null, getChildAt(lastindex),
        // lastindex, lastindex / mColumnCount);
        // }
        // }
    }

    /**
     * 开始拖放指定项目
     *
     * @param view
     * @return
     */
    protected boolean performDrag(View view) {
        if (!mTempEnabled)
            return false;
        int index = getLastIndex();
        if (index != -1) {
            mDraggedIndex = index;
            animateDragged();
            if (mRearrageAdapter != null) {
                mRearrageAdapter.onTouched(mDraggedIndex);
            }

            return true;
        }

        return false;
    }

    protected boolean performOnLong() {

        if (!mTempEnabled)
            return false;

        // Log.e("onlong....", "preformonlong...");
        int index = getLastIndex();
        if (index != -1) {
            mDraggedIndex = index;
            View child = animateDragged(true);
            if (mOnLongListener != null) {
                mOnLongListener.onLong(mDraggedIndex, child);
            }
            return true;
        }

        return false;
    }

    private boolean mIsDoLong = false;
    //
    // @Override
    // public boolean onInterceptTouchEvent(MotionEvent ev) {
    // int action = ev.getAction();
    // switch (action) {
    // case MotionEvent.ACTION_DOWN:
    // case MotionEvent.ACTION_MOVE: // 移动事件
    // case MotionEvent.ACTION_UP:
    // case MotionEvent.ACTION_CANCEL:
    // break;
    // }
    // return false;
    // }

    private Runnable mLongClickRunable = new Runnable() {

        @Override
        public void run() {
            mIsDoLong = true;
            performOnLong();
            removeCallbacks(this);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int action = ev.getAction();
        // Log.e("onTouchEvent", action + "---" + mIsDoLong);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mIsDoLong = false;
                // final View viewDown = view;
                mTempEnabled = true;
                mLastXPosition = (int) ev.getX();
                mLastYPosition = (int) ev.getY();
                mIsTouching = true;
                mTouchAndMoved = false;
                mDraggedIndex = -1;
                setLastViewPressed(true);
                // 长按
                if (getChildCount() > 1) {
                    if (mAddItem && getLastIndex() == mItemCount - 1) {
                        break;
                    }
                    postDelayed(mLongClickRunable,
                            ViewConfiguration.getLongPressTimeout());
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: // 移动事件
                if (Math.abs(ev.getX() - mLastXPosition) > 20
                        || Math.abs(ev.getY() - mLastYPosition) > 15) {
                    mIsDoLong = false;
                    mTouchAndMoved = true;
                    removeCallbacks(mLongClickRunable);
                }

                if (!mIsDoLong) {
                    doActionMove((int) ev.getX(), (int) ev.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (-1 != mDraggedIndex)
                    animateDragged(false);
                if (mIsDoLong) {
                    if (null != mOnLongListener) {
                        mOnLongListener.onCancel();
                    }
                } else {
                    if (!mTouchAndMoved) {
                        onClick(getlastIndexView());
                    }
                    doAcitonUp(false);
                    removeCallbacks(mLongClickRunable);
                }

                mIsDoLong = false;
                mDraggedIndex = -1;
                invalidate();
                break;
        }
        return true;
    }

    public void doActionMove(int mx, int my) {
        int delta;
        if (mOrientation == VERTICAL) {

            // 如果是在垂直方向上移动
            delta = mLastYPosition - my;
        } else {
            delta = mLastXPosition - mx;
        }
        if (mDraggedIndex != -1) {
            // 移动过程中X的坐标
            int x = mx, y = my;
            // 移动过程总Y的坐标
            // 需要绘制的当前左上角的水平坐标(l=左边)和垂直坐标(t=上边)
            int l = x - mDraggedOffsetX, t = 0;
            // 绘制图片需要左上角和右下角坐标
            getChildAt(mDraggedIndex).layout(l, t, l + mChildSize, t + mChildSize);

            // check for new target hover
            int target = getTargetFromCoor(x, y);
            if (mLastTarget != target) {
                if (target != -1) {
                    if (mAddItem && target == mItemCount - 1) {
                        animateGap(target - 1);
                        mLastTarget = target - 1;
                    } else {
                        animateGap(target);
                        mLastTarget = target;
                    }

                }
            }
            mHandler.removeCallbacks(mUpdateTask);
            mHandler.post(mUpdateTask);
        } else {
            mScrollPosition += delta;
            clampScroll();
            if (Math.abs(delta) > 2)
                mTempEnabled = false;
            onLayout(true, getLeft(), getTop(), getRight(), getBottom());
        }

        mLastXPosition = mx;
        mLastYPosition = my;
        mLastDelta = delta;
        if (mLastDelta != 0) {
            setLastViewPressed(false);
        }
    }

    @SuppressWarnings("deprecation")
    private void doAcitonUp(boolean reback) {

        if (mDraggedIndex != -1) {
            View v = getChildAt(mDraggedIndex);
            if (mLastTarget != -1)
                reorderChildren();
            else {
                Point xy = getCoorFromIndex(mDraggedIndex);
                v.layout(xy.x, xy.y, xy.x + mChildSize, xy.y + mChildSize);
            }
            v.clearAnimation();
            if (v instanceof ImageView) {
                ((ImageView) v).setAlpha(255);
            }

            if (mRearrageAdapter != null) {
                mRearrageAdapter.onTouched(mDraggedIndex);
            }
            if (reback) {
                onClick(v);
                reback = false;
            }
        }

        mIsTouching = false;
        mTempEnabled = false;
        mLastTarget = -1;
        mDraggedIndex = -1;
        mHandler.removeCallbacks(mUpdateTask);
        mHandler.post(mUpdateTask);
        setLastViewPressed(false);
    }

    private void setLastViewPressed(boolean pressed) {
        View lastView = getlastIndexView();
        if (null != lastView) {
            lastView.setPressed(pressed);
        }
    }

    private int mDraggedOffsetX, mDraggedOffsetY;

    /**
     * 开始动画拖掇
     */
    protected View animateDragged() {

        return animateDragged(false);
    }

    /**
     * 开始动画拖掇
     */
    protected View animateDragged(boolean gone) {
        View v = getChildAt(mDraggedIndex);
        Point ptDragged = getCoorFromIndex(mDraggedIndex);
        mDraggedOffsetX = mLastXPosition - ptDragged.x - 5;
        mDraggedOffsetY = mLastYPosition - ptDragged.y - 5;
        int l = mLastXPosition - mDraggedOffsetX, t = mLastYPosition - mDraggedOffsetY;

        v.layout(l, t, l + mChildSize, t + mChildSize);
        AlphaAnimation alpha = null;
        if (gone) {
            alpha = new AlphaAnimation(1, 0f);
        } else {

            alpha = new AlphaAnimation(1, 1f);
        }
        alpha.setDuration(mAnimDuration);
        alpha.setFillEnabled(true);
        alpha.setFillAfter(true);

        v.clearAnimation();
        v.startAnimation(alpha);
        v.setPressed(false);
        return v;
    }

    protected void animateGap(int target) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (i == mDraggedIndex)
                continue;
            int newPos = i;
            if (mDraggedIndex < target && i >= mDraggedIndex + 1 && i <= target)
                newPos--;
            else if (target < mDraggedIndex && i >= target && i < mDraggedIndex)
                newPos++;

            // animate
            int oldPos = i;
            if (mNewPositions.get(i) != -1)
                oldPos = mNewPositions.get(i);
            if (oldPos == newPos) {
                continue;
            }

            Point oldXY = getCoorFromIndex(oldPos);
            Point newXY = getCoorFromIndex(newPos);
            Point oldOffset = new Point(oldXY.x - v.getLeft(), oldXY.y
                    - v.getTop());
            Point newOffset = new Point(newXY.x - v.getLeft(), newXY.y
                    - v.getTop());

            AlphaAnimation alpha = new AlphaAnimation(1, 1);
            alpha.setDuration(mAnimDuration);

            AnimationSet animSet = new AnimationSet(true);
            animSet.setFillEnabled(true);
            animSet.setFillAfter(true);

            TranslateAnimation translate = new TranslateAnimation(
                    Animation.ABSOLUTE, oldOffset.x, Animation.ABSOLUTE,
                    newOffset.x, Animation.ABSOLUTE, oldOffset.y,
                    Animation.ABSOLUTE, newOffset.y);
            translate.setDuration(mAnimDuration);
            animSet.addAnimation(alpha);
            animSet.addAnimation(translate);
            v.clearAnimation();
            v.startAnimation(animSet);
            mNewPositions.set(i, newPos);
        }
    }

    protected void reorderChildren() {
        // FIGURE OUT HOW TO REORDER CHILDREN WITHOUT REMOVING THEM ALL AND
        // RECONSTRUCTING THE LIST!!!
        if (mRearrageAdapter != null) {
            mRearrageAdapter.onRearrange(mDraggedIndex, mLastTarget);
        }
        ArrayList<View> children = new ArrayList<View>();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).clearAnimation();
            children.add(getChildAt(i));
        }
        removeAllViews();
        while (mDraggedIndex != mLastTarget)
            if (mLastTarget == children.size()) // mDraggedIndex and dropped to the
            // right of the last element
            {
                children.add(children.remove(mDraggedIndex));
                mDraggedIndex = mLastTarget;
            } else if (mDraggedIndex < mLastTarget) // shift to the right
            {
                Collections.swap(children, mDraggedIndex, mDraggedIndex + 1);
                Collections.swap(mArrSceneInfo, mDraggedIndex, mDraggedIndex + 1);
                mDraggedIndex++;
            } else if (mDraggedIndex > mLastTarget) // shift to the left
            {
                Collections.swap(children, mDraggedIndex, mDraggedIndex - 1);
                Collections.swap(mArrSceneInfo, mDraggedIndex, mDraggedIndex - 1);
                mDraggedIndex--;
            }
        for (int i = 0; i < children.size(); i++) {
            mNewPositions.set(i, -1);
            addView(children.get(i));
            mRearrageAdapter.getView(i, children.get(i), this);
        }
        onLayout(true, getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * 滚动到最小坐标
     */
    public void scrollToMinCoor() {
        mScrollPosition = 0;
    }

    /**
     * 滚动到最大坐标
     */
    public void scrollToMaxCoor() {
        mScrollPosition = Integer.MAX_VALUE;
        clampScroll();
    }

    protected boolean clampScroll() {
        int stretch = 3, overreach = mOrientation == VERTICAL ? getHeight() / 2
                : getWidth() / 2;
        int max = getMaxScroll();
        max = Math.max(max, 0);
        int nOldScroll = mScrollPosition;

        if (mScrollPosition < -overreach) {
            mScrollPosition = -overreach;
            mLastDelta = 0;
        } else if (mScrollPosition > max + overreach) {

            mScrollPosition = max + overreach;
            mLastDelta = 0;
        } else if (mScrollPosition < 0) {
            if (mScrollPosition >= -stretch) {
                mScrollPosition = 0;
            } else if (!mIsTouching) {
                mScrollPosition -= mScrollPosition / stretch;
            }

        } else if (mScrollPosition > max) {
            if (mScrollPosition <= max + stretch)
                mScrollPosition = max;
            else if (!mIsTouching)
                mScrollPosition += (max - mScrollPosition) / stretch;
        }
        return nOldScroll != mScrollPosition;
    }

    protected int getMaxScroll() {
        int max;
        if (mOrientation == VERTICAL) {
            int rowCount = (int) Math.ceil((double) getChildCount() / mColumnCount);
            max = rowCount * mChildSize - getHeight() + 2 * mPadding;
        } else {
            int mColumnCount = getChildCount();
            max = mColumnCount * mChildSize - getWidth() + 2 * mPadding;
        }
        return max;
    }

    public int getLastIndex() {
        return getIndexFromCoor(mLastXPosition, mLastYPosition);
    }

    public View getlastIndexView() {
        int index = getLastIndex();
        return getChildAt(index);
    }

    /**
     * Sets the data behind this GridView.
     *
     * @param adapter the adapter providing the grid's data
     */
    public void setAdapter(RearrangeListAdapater adapter) {
        if (mRearrageAdapter != null && mDataSetObserver != null) {
            mRearrageAdapter.unregisterDataSetObserver(mDataSetObserver);
            mDataSetObserver = null;
        }
        this.removeAllViewsInLayout();
        invalidate();
        mOldItemCount = 0;
        mRearrageAdapter = adapter;
        if (mRearrageAdapter != null) {
            mOldItemCount = mItemCount;
            mItemCount = mRearrageAdapter.getCount();

            mDataSetObserver = new AdapterDataSetObserver();
            mRearrageAdapter.registerDataSetObserver(mDataSetObserver);

        }
        requestLayout();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener = l;
    }

    class AdapterDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            mOldItemCount = 0;
            // mOldItemCount = mItemCount;
            mItemCount = mRearrageAdapter.getCount();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            mOldItemCount = 0;
            requestLayout();
        }

        public void clearSavedState() {
        }
    }

    /**
     * grid view item
     *
     * @author abreal
     */
    private class DraggableGridViewItem extends ViewGroup {
        private View m_contentView;

        /**
         * constructor
         *
         * @param context
         */
        public DraggableGridViewItem(Context context, View contentView) {
            super(context);
            m_contentView = contentView;
            this.addView(m_contentView);
        }

        public View getContentView() {
            return m_contentView;
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            m_contentView.layout(mItemCloseButtonHorizontalPadding,
                    mItemCloseButtonVerticalPadding, mItemWidth
                            + mItemCloseButtonHorizontalPadding,
                    mItemHeight + mItemCloseButtonVerticalPadding);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            // Log.e("child  item...", "onTouchEvent..." + ev.getAction());
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE: // 移动事件
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            return false;
        }
    }

    public void resetData() {
        mLastTarget = -1;
        mDraggedIndex = -1;
    }

    public void reset() {
        doAcitonUp(true);
    }

    public void setAddItemInfo(ArrayList<Scene> arr) {
        mArrSceneInfo = arr;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setAddItem(boolean isAdd) {
        mAddItem = isAdd;
    }
}
