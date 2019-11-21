package com.rd.veuisdk.demo.zishuo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.rd.veuisdk.R;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 文字列表 可移动
 */
public class ListTextView extends View {

    public ListTextView(Context context) {
        super(context);
    }

    public ListTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //画笔 中间和两边
    private Paint mPaintCenter, mPaint;
    private Rect mRect = new Rect();
    //文字存储
    private ArrayList<String> mLocalTextList = new ArrayList<>();
    private int mIndex = 0;//当前
    //自定义存储
    private String mCustomText;
    private SparseArray<String> mCustomTextArray = new SparseArray<>();
    //文字高度、每一行间隔的距离
    private int mWidth, mHeight;
    private int mIntervalHeight = 60;
    private int mBothSidesW = 100;
    //判断是Array还是只有一个
    private boolean mIsCustom = false;

    private void init() {
        //中间的文字
        mPaintCenter = new Paint();
        mPaintCenter.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.text_size_18));
        mPaintCenter.setAntiAlias(true);
        mPaintCenter.setColor(getContext().getResources().getColor(R.color.white));
        //两边的文字
        mPaint = new Paint();
        mPaint.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.text_size_18));
        mPaint.setAntiAlias(true);
        mPaint.setColor(getContext().getResources().getColor(R.color.speed_line_button_text_color));
        mPaint.getTextBounds("两边的文字", 0, 5, mRect);
        mHeight = mRect.height();
        mWidth = (int) (mPaint.measureText("两"));
    }

    //Y轴移动距离 是否到达边缘
    private int lastY, moveY = 0;
    private float downX, downY;
    private boolean hasBeenEnd = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mLocalTextList.size() > 0 || !TextUtils.isEmpty(mCustomText)) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopScroll();
                    lastY = (int) (event.getRawY());
                    downX = event.getRawX();
                    downY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int y = (int) event.getRawY();
                    //到达边缘时不能向该方向继续移动
                    if (!hasBeenEnd || (moveY > 0 && (lastY - y) < 0 || (moveY <= 0 && (lastY - y) > 0))) {
                        moveY += (lastY - y) * 0.7;
                        lastY = y;
                        mVelocityTracker.computeCurrentVelocity(1000);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //单击
                    if (mIsCustom && mListener != null && !TextUtils.isEmpty(mCustomText)
                            && Math.abs(event.getRawX() - downX) < 20
                            && Math.abs(event.getRawY() - downY) < 20) {
                        mListener.onClick(mCustomText);
                    } else {
                        mSpeed = mVelocityTracker.getYVelocity();
                        sliding();
                        mVelocityTracker.clear();
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsCustom) {
            if (mCustomTextArray.size() > 0) {
                int num = mCustomTextArray.size();
                //移动数量、偏移
                int moveNum, offset = 0;
                if (moveY < 0) {
                    //上顶
                    moveY = 0;
                    hasBeenEnd = true;
                } else if (num * (mHeight + mIntervalHeight) < getHeight()) {
                    //小于屏幕宽度 不能移动
                    moveY = 0;
                    hasBeenEnd = true;
                } else if (num * (mHeight + mIntervalHeight) - getHeight() <= moveY) {
                    //下底
                    moveY = num * (mHeight + mIntervalHeight) - getHeight();
                    offset = mHeight + mIntervalHeight;
                    hasBeenEnd = true;
                } else {
                    hasBeenEnd = false;
                    offset = moveY % (mHeight + mIntervalHeight);
                }
                moveNum = moveY / (mHeight + mIntervalHeight);
                //X Y坐标
                int startX = mBothSidesW, startY;
                for (int k = moveNum, i = 1; k < num; k++, i++) {
                    startY = i * (mHeight + mIntervalHeight) - offset;
                    if (startY > getHeight()) {
                        break;
                    }
                    canvas.drawText(mCustomTextArray.get(k), startX, startY, mPaint);
                }
            }
        } else {
            if (mLocalTextList.size() == 0) {
                return;
            }
            //限制当前index的范围
            int moveNum = moveY / (mHeight + mIntervalHeight);//移动的数量
            //偏移
            int offset;
            if (moveNum >= mLocalTextList.size()) {
                //到达下边缘
                mIndex = mLocalTextList.size() - 1;
                moveY = mLocalTextList.size() * (mHeight + mIntervalHeight);
                hasBeenEnd = true;
                offset = mHeight + mIntervalHeight;
            } else if (moveNum < 0) {
                //到达上边缘
                mIndex = 0;
                moveY = 0;
                hasBeenEnd = true;
                offset = - (mHeight + mIntervalHeight);
            } else {
                mIndex = moveNum;
                hasBeenEnd = false;
                offset = moveY % (mHeight + mIntervalHeight);
            }
            //宽度、高低、文字
            String s;
            int startX, startY;
            int height;//高度

            //画中间
            s = mLocalTextList.get(mIndex);
            mPaintCenter.getTextBounds(s, 0, s.length(), mRect);
            startX = (getWidth() - mRect.width()) / 2;
            startY = (getHeight() - mHeight) / 2 - offset;
            height = startY;
            canvas.drawText(s, startX, startY, mPaintCenter);

            //画两边
            int j = 1;
            for (int i = mIndex - 1; i >= 0; i--, j++) {
                s = mLocalTextList.get(i);
                mPaint.getTextBounds(s, 0, s.length(), mRect);
                startX = (getWidth() - mRect.width()) / 2;
                startY = height - (mRect.height() + mIntervalHeight) * j;
                if (startY < 0) {
                    break;
                }
                canvas.drawText(s, startX, startY, mPaint);
            }
            //重新计算起点
            height = mHeight + height + mIntervalHeight;
            j = 0;
            for (int i = mIndex + 1; i < mLocalTextList.size(); i++, j++) {
                s = mLocalTextList.get(i);
                mPaint.getTextBounds(s, 0, s.length(), mRect);
                startX = (getWidth() - mRect.width()) / 2;
                startY = height + (mRect.height() + mIntervalHeight) * j;
                if (startY > getHeight()) {
                    break;
                }
                canvas.drawText(s, startX, startY, mPaint);
            }
        }
    }

    /**
     * 滑动的动画
     */
    private Animation mAnimation;
    private VelocityTracker mVelocityTracker;
    private float mSpeed = 0;

    /**
     * 滑动
     */
    private void sliding(){
        //速度太小 返回
        if (Math.abs(mSpeed) < 300) {
            return;
        }
        if (mAnimation == null) {
            mAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (hasBeenEnd) {
                        stopScroll();
                    } else {
                        moveY -= (((1 - interpolatedTime) * mSpeed) / 100);
                        invalidate();
                    }
                }
            };
            mAnimation.setInterpolator(new DecelerateInterpolator());//设置一个减速插值器
        }
        stopScroll();
        mAnimation.setDuration(1000);
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
     * 设置数据
     * @param strings
     */
    public void setStringList(ArrayList<String> strings) {
        mCustomText = null;
        mLocalTextList.clear();
        mIsCustom = false;
        if (strings != null) {
            mLocalTextList.addAll(strings);
        }
        moveY = 0;
        if (mListener != null && mLocalTextList.size() > 0) {
            mListener.isEmpty(false);
        }
        invalidate();
    }

    /**
     * 自定义文本
     */
    public void setCustomText(String s) {
        mLocalTextList.clear();
        mIsCustom = true;
        this.mCustomText = s;
        mCustomTextArray.clear();
        if (!TextUtils.isEmpty(s)) {
            String[] strings = Pattern.compile("[\n]").split(s);
            int n = 0, begin = 0;
            for (int i = 0, j = 0; i < strings.length; i++) {
                String tmp = strings[i];
                n = 1;
                begin = 0;
                while (true) {
                    while (mPaint.measureText(tmp.substring(begin, n)) < getWidth() - mBothSidesW * 2
                            && n < tmp.length()) {
                        n++;
                    }
                    mCustomTextArray.put(j++, tmp.substring(begin, n));
                    if (n >= tmp.length()) {
                        break;
                    }
                    begin = n++;
                }
            }
        }
        moveY = 0;
        if (mListener != null && mCustomText != null) {
            mListener.isEmpty(false);
        }
        invalidate();
    }

    /**
     * 清空
     */
    public void clear() {
        mLocalTextList.clear();
        mCustomTextArray.clear();
        mCustomText = null;
        moveY = 0;
        if (mListener != null) {
            mListener.isEmpty(true);
        }
        stopScroll();
        mAnimation = null;
        invalidate();
    }

    //获取题词
    public ArrayList<String> getLocalTextList() {
        return mLocalTextList;
    }

    //获取自定义题词
    public String getCustomText() {
        return mCustomText;
    }

    /**
     * 判断是否为空 true不为空
     */
    public boolean isEmpty() {
        return mLocalTextList.size() > 0 || !TextUtils.isEmpty(mCustomText);
    }

    /**
     * 返回是list还是custom
     */
    public boolean isCustom() {
        return mIsCustom;
    }

    private onListClickListener mListener;

    public void setListener(onListClickListener listener) {
        this.mListener = listener;
    }

    public interface onListClickListener {

        /**
         * 单击
         */
        void onClick(String s);

        /**
         * 是否可以下一步
         */
        void isEmpty(boolean b);

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
