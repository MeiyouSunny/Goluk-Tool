package com.rd.veuisdk.demo.zishuo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 录音轨道
 */
public class WaveformView extends View {

    public WaveformView(Context context) {
        super(context);
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public static final int WVTYPE_CENTER_LINE = 0;//竖线从中间开始 向上向下长度相同
    public static final int WVTYPE_SINGLE = 1;//竖线从底部开始向上计算

    private Integer mWaveType = WVTYPE_CENTER_LINE;//波形展示类型
    //中间线颜色、宽度
    private int mCenterLineColor = Color.RED;
    private int mCenterLineWidth = 4;
    //竖线
    private int mLineWidth = 2;//竖线的宽度
    private int mLineSpace = 8;//竖线之间的间隔宽度
    //时间
    private int mTimeLineWidth = 2;
    private int mTimeLineHeightMin = 8;
    private int mTimeLineHeightMax = 2 * mTimeLineHeightMin;

    private List<Integer> values = new ArrayList<>();//存放数值
    private int mMinHeight = 45;//最小40
    private int mMaxHeight = 90;//最大值90
    private boolean mIsRecordingPause = false;//是否暂停录制

    //中线、竖线、时间线、时间文字
    private Paint mPaintCenterLine, mPaintLine,
    mPaintTimeLine, mPaintTimeText;
    private Rect mRect = new Rect();

    //判断内容是否过少
    private int mLimitedNum = 0;

    private void init(){
        //中线红色、宽度为1
        mPaintCenterLine = new Paint();
        mPaintCenterLine.setStrokeWidth(mCenterLineWidth);
        mPaintCenterLine.setColor(mCenterLineColor);
        //竖线 宽为1
        mPaintLine = new Paint();
        mPaintLine.setStrokeWidth(mLineWidth);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(getContext().getResources().getColor(R.color.one_key_make_solid));
        //时间
        mPaintTimeLine = new Paint();
        mPaintTimeLine.setStrokeWidth(mTimeLineWidth);
        mPaintTimeLine.setColor(getContext().getResources().getColor(R.color.edit_text_gray));
        //时间文字
        mPaintTimeText = new Paint();
        mPaintTimeText.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.text_size_10));
        mPaintTimeText.setAntiAlias(true);
        mPaintTimeText.setColor(getContext().getResources().getColor(R.color.edit_text_gray));
    }

    /**
     * 设置声音
     * @param value
     */
    public void putValue(int value){
        if (value > mMaxHeight){
           value = mMaxHeight;
        }
        if (value < mMinHeight + 25) {
            value = mMinHeight + 1;
        }
        values.add(value);
        //判断有效值
        if (value > mMinHeight + 25) {
            mLimitedNum++;
        }
        invalidate();
    }

    private int lastX, moveX = 0;
    private boolean hasBeenEnd = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsRecordingPause) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) (event.getRawX());
                    stopScroll();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getRawX();
                    //到达边缘时不能向该方向继续移动
                    if (!hasBeenEnd || (moveX >= 0 && (lastX - x) < 0 || (moveX < 0 && (lastX - x) > 0))) {
                        moveX += (lastX - x) * 0.7;
                        lastX = x;
                        mListener.onDrag();
                        mVelocityTracker.computeCurrentVelocity(1000);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mSpeed = mVelocityTracker.getXVelocity();
                    sliding();
                    mVelocityTracker.clear();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int center = getWidth() / 2;
        //画中线
        canvas.drawLine(center - mCenterLineWidth / 2, 0, center - mCenterLineWidth / 2, getHeight(), mPaintCenterLine);
        //判断是否到达两边
        int w = mLineSpace + mLineWidth;
        if (moveX + values.size() * w < 0) {
            moveX = -values.size() * w;
            hasBeenEnd = true;
        } else if (moveX > 0) {
            moveX = 0;
            hasBeenEnd = true;
        } else {
            hasBeenEnd = false;
        }
        //判断是否录制中
        if (mIsRecordingPause) {
            if (moveX == 0) {
                //可以继续录制
                mListener.onCanRecording(true);
            } else {
                //不能录制
                mListener.onCanRecording(false);
            }
        }

        /* ------------画竖线---------------   */
        //计算移动线条数、偏移
        int moveLineSize = moveX / w;
        int startOffset = moveX % w;
        int startX =0;
        //中线左边
        int j = 1;
        for (int i = values.size() + moveLineSize - 1; i >= 0; i--, j++) {
            startX = center - j * w - startOffset;
            if (startX < 0) {
                break;
            }
            //画竖线
            drawLine(startX, values.get(i), canvas);
        }
        //中线右边
        j = 0;
        for (int i = values.size() + moveLineSize; i < values.size() && i >= 0; i ++, j++) {
            startX = center + j * w - startOffset;
            if (startX > getWidth()) {
                break;
            }
            //画竖线
            drawLine(startX, values.get(i), canvas);
        }

        /* ---------------画时间--------------*/
        //中线左边第一个
        int tmp = values.size() + moveLineSize;
        int tmp2 = 0;
        mListener.onTime(tmp * 50 - startOffset / w * 50);
        while (tmp % 5 != 0) {
            tmp--;
            tmp2++;
        }
        int centerX = center - startOffset - tmp2 * w;
        //右边
        int t = 1;
        while (true) {
            startX = centerX + t * 5 * w;
            if (startX > getWidth()) {
                break;
            }
            if ((tmp + t * 5) % 20 == 0) {
                drawTimeText(canvas, startX, (tmp + t * 5) * 50);
                canvas.drawLine(startX, 0, startX, mTimeLineHeightMax, mPaintTimeLine);
            } else {
                canvas.drawLine(startX, 0, startX, mTimeLineHeightMin, mPaintTimeLine);
            }
            t++;
        }
        //左边
        t = 0;
        while (true) {
            startX = centerX - t * 5 * w;
            if (startX < 0 || tmp - t * 5 < 0) {
                break;
            }
            if ((tmp - t * 5) % 20 == 0) {
                drawTimeText(canvas, startX, (tmp - t * 5) * 50);
                canvas.drawLine(startX, 0, startX, mTimeLineHeightMax, mPaintTimeLine);
            } else {
                canvas.drawLine(startX, 0, startX, mTimeLineHeightMin, mPaintTimeLine);
            }
            t++;
        }
    }

    //绘制竖线
    private void drawLine(int startX, int value, Canvas canvas) {
        int endX = 0;
        int startY = 0;
        int endY = 0;
        //线高度
        int lineHeight = (int) ((value - mMinHeight + 0.0f) / (mMaxHeight - mMinHeight) * getHeight() * 0.8);
        switch (mWaveType) {
            case WVTYPE_CENTER_LINE:
                endX = startX;
                startY = (getHeight() - lineHeight) / 2;
                endY = (getHeight() - lineHeight) / 2 + lineHeight;
                break;
            case WVTYPE_SINGLE:
                endX = startX;
                startY = getHeight() - lineHeight;
                endY = getHeight();
                break;
        }
        canvas.drawLine(startX, startY, endX, endY, mPaintLine);
    }

    //绘制时间
    private void drawTimeText(Canvas canvas, int startX, long time) {
        int strWidth;
        int strHeight;
        String timeStr = DateTimeUtils.stringForTime(time);
        mPaintTimeText.getTextBounds(timeStr, 0, timeStr.length(), mRect);
        strWidth = mRect.width();
        strHeight = mRect.height();
        canvas.drawText(timeStr, startX - strWidth / 2, 0, mPaintTimeText);
    }

    public void setRecordingPause(boolean over){
        mIsRecordingPause = over;
    }

    public boolean isRecordingPause(){
        return mIsRecordingPause;
    }

    /**
     * 是否有效录音 true有效 false无效
     * @return
     */
    public boolean isLimited() {
        return mLimitedNum > 20;
    }

    /**
     * 删除所有
     */
    public void deleteAll() {
        values.clear();
        mLimitedNum = 0;
        invalidate();
    }

    /**
     * 释放资源
     */
    public void recycle() {
        values.clear();
        mLimitedNum = 0;
        stopScroll();
        mAnimation = null;
        values = null;
    }

    /**
     * 播放 设置时间 秒
     */
    public void setTime(float position) {
        int index = values.size() - Utils.s2ms(position) / 50;
        index = index >= 0? index : 0;
        moveX = -index * (mLineSpace + mLineWidth);
        invalidate();
    }

    /**
     * 播放完成
     */
    public void playComplete() {
        moveX = 0;
        invalidate();
    }

    /**
     * 回到原点
     */
    public void seekStart(){
        moveX = -values.size() * (mLineSpace + mLineWidth);
        invalidate();
    }

    /**
     * 返回总的时间 毫秒
     */
    public long getDuration() {
        return values.size() * 50;
    }

    /**
     * 回调
     */
    private WaveListener mListener;

    public void setListener(WaveListener listener) {
        this.mListener = listener;
    }

    public interface WaveListener {
        /**
         * 回调时间
         * @param time 毫秒
         */
        void onTime(long time);

        /**
         * 是否可以继续录制
         */
        void onCanRecording(boolean can);

        /**
         * 拖动 暂停
         */
        void onDrag();

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
                        moveX -= (((1 - interpolatedTime) * mSpeed) / 100);
                        invalidate();
                    }
                }
            };
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