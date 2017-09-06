package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.rd.lib.utils.PaintUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.DateTimeUtils;

/***
 * 截取组件 ，支持左右把手滑动
 */
public class RangeSeekBar extends View {
    /**
     * An invalid pointer id.
     */
    private static final int INVALID_POINTER_ID = 255;

    private static final int ACTION_POINTER_UP = 0x6;
    private static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00;
    private static final int ACTION_POINTER_INDEX_SHIFT = 8;
    private String TAG = "RangeSeekBar";


    private boolean mCanTouchMaxHand = false;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mMinBmp, mMaxBmp;// 开始时间
    private float thumbWidth;
    private float thumbHalfWidth;
    private float thumbHalfHeight;
    private float lineHeight;
    private float padding;
    private int duration = 10000;//时间轴对应的时间 单位：ms
    private int mMin, mMax = duration; //真正的时间单位:ms
    private int progress;// 当前进度相对于整个时间轴的进度，不是截取片段 (单位:ms)

    private Thumb pressedThumb = null;
    public boolean IS_MULTI_COLORED;
    private OnRangeSeekBarChangeListener listener;
    private int MIN_TIME = 1000;// 时间间隔至少一秒
    protected int progressNormalCcolor = 0;// 最下层线的颜色
    protected int progressBetweenColor = 0;// 最小最大线段的颜色层线的颜色
    protected int progressProgressColor = 0;// 线段中得进度的颜色
    protected int progresTextColor = 0;// 把手进度的字体颜色

    private float baseDistance = 0;
    private float textDistance = 10;

    private boolean isSide;
    private float mDownMotionX;
    private int mActivePointerId = INVALID_POINTER_ID;


    private int mScaledTouchSlop;
    private boolean mIsDragging;
    private Bitmap tempBgBmp;
    private Bitmap rangSbarHintBmp;
    private int textHeight;
    private int mTouchedWidth;
    private boolean isAutoScroll = true;

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RangeSeekBar);
        isSide = a.getBoolean(R.styleable.RangeSeekBar_isSide, false);
    }

    /**
     * 时间轴的总长
     *
     * @param duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
        mMax = duration;
        //最小时间间隔
        MIN_TIME = Math.min(Math.max(1000, duration / 100), 4000);
        invalidate();
    }


    /**
     * Callback listener interface to notify about changed range values.
     */
    public interface OnRangeSeekBarChangeListener {

        /**
         * action_move返回最大最小的截取范围
         *
         * @param bar
         * @param minValue
         * @param maxValue
         */
        public void onRangeSeekBarValuesChanged(RangeSeekBar bar,
                                                int minValue, int maxValue);

        /**
         * 松开手机，播放选择时间段的音乐
         */
        public void onPlay(int currentValue);

        /**
         * 按下暂停播放
         *
         * @param currentValue 当前进度的位置单位毫秒
         */
        public void onActionDown(int currentValue);

    }

    /**
     * Registers given listener callback to notify about changed selected
     * values.
     *
     * @param listener The listener to notify about changed selected values.
     */
    public void setOnRangeSeekBarChangeListener(
            OnRangeSeekBarChangeListener listener) {
        this.listener = listener;
    }


    protected void initColor() {
        mPaint.setColor(progresTextColor);
        invalidate();
    }

    void initdata(int mMin, int mMax, Context context) {
        progressNormalCcolor = getResources().getColor(R.color.progress_n);
        progressBetweenColor = getResources().getColor(
                R.color.progress_between);
        progressProgressColor = getResources().getColor(
                R.color.progress_progress);
        progresTextColor = getResources().getColor(R.color.kxblue);
        mPaint.setColor(progresTextColor);

        mPaint.setAntiAlias(true);
        mPaint.setTextSize(getResources()
                .getDimension(R.dimen.text_size_rangseekbar));
        mPaint.setTextAlign(Align.CENTER);

        pbigger.setAntiAlias(true);
        pbigger.setTextAlign(Align.CENTER);
        pbigger.setColor(Color.WHITE);
        pbigger.setTextSize(getResources().getDimension(
                R.dimen.text_size_rangseekbar_bigger));

        update(mMin, mMax);

        IS_MULTI_COLORED = false;
        tempBgBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.edit_music_seekbar_thumb);

        rangSbarHintBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.rangseekbar_hint);

        mMinBmp = tempBgBmp;
        mMaxBmp = tempBgBmp;

        thumbWidth = mMinBmp.getWidth();

        thumbHalfWidth = 0.5f * thumbWidth;

        thumbHalfHeight = 0.4f * mMinBmp.getHeight();

        lineHeight = 0.1f * 0.5f * mMinBmp.getHeight();

        int[] wh = PaintUtils.getHeight(mPaint);
        textHeight = wh[0];
        padding = thumbWidth / 2;
        setFocusable(true);
        setFocusableInTouchMode(true);

        mTouchedWidth = rangSbarHintBmp.getWidth();
        init();
    }

    /**
     * 单位:毫秒
     *
     * @param mMin
     * @param mMax
     */
    private void update(int mMin, int mMax) {
        this.mMin = mMin;
        this.mMax = mMax;
        progress = 0;
        invalidate();
    }

    private void init() {
        mScaledTouchSlop = ViewConfiguration.get(getContext())
                .getScaledTouchSlop();
    }


    /**
     * 获取最小值
     *
     * @return
     */
    public int getSelectedMinValue() {
        return mMin;
    }


    /**
     * 获取最大值
     *
     * @return
     */
    public int getSelectedMaxValue() {
        return mMax;
    }


    /**
     * 设置两个把手的值
     *
     * @param min
     * @param max
     */
    public void setHandleValue(int min, int max) {
        mMin = min;
        mMax = max;
        invalidate();
    }


    /**
     * Handles thumb selection and movement. Notifies listener callback on
     * certain events.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex;
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
                pressedThumb = evalPressedThumb(mDownMotionX, event.getY(pointerIndex));
                if (pressedThumb == null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    invalidate();
                    return super.onTouchEvent(event);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    isAutoScroll = false;
                    setPressed(true);
                    invalidate();
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    attemptClaimDrag();

                    if (null != listener) {
                        listener.onActionDown(progress);
                    }
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                isAutoScroll = false;
                if (pressedThumb != null) {
                    progress = mMin;
                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);
                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    mMinStr = DateTimeUtils.stringForTime(getSelectedMinValue());

                    if (listener != null) {
                        listener.onRangeSeekBarValuesChanged(this,
                                getSelectedMinValue(), getSelectedMaxValue());
                    }
                    invalidate();
                } else {
                    invalidate();
                    return super.onTouchEvent(event);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                getParent().requestDisallowInterceptTouchEvent(false);
                isAutoScroll = true;

                if (pressedThumb == null) {
                    return super.onTouchEvent(event);
                }
                progress = mMin;
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }
                pressedThumb = null;
                if (listener != null) {
                    listener.onPlay(progress);
                }
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                isAutoScroll = false;
                final int index = event.getPointerCount() - 1;
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                isAutoScroll = true;
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                isAutoScroll = true;
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        setFocusable(false);
                    }
                }, 300);
                break;
            default:
                isAutoScroll = true;
                break;
        }
        return true;
    }

    private final void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * 设置当前进度
     *
     * @param mprogress
     */
    public void setProgress(int mprogress) {
        progress = mprogress;
        invalidate();
    }

    public void setAutoScroll() {
        isAutoScroll = true;
    }

    public void resetProgress() {
        progress = mMin;
        invalidate();
    }

    private boolean mTouchLeftOrRight = true;

    private void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);
        if (Thumb.MIN.equals(pressedThumb)) {
            int min = pxToMs(x);
            if ((mMax - min) < MIN_TIME) {
                min = mMax - MIN_TIME;
            }
            mTouchLeftOrRight = true;
            mMin = min;
        } else if (Thumb.MAX.equals(pressedThumb) && mCanTouchMaxHand) {
            int max = pxToMs(x);
            if ((max - mMin) < MIN_TIME) {
                max = mMin + MIN_TIME;
            }
            mTouchLeftOrRight = false;
            mMax = max;
        }
//        Log.e(TAG, "trackTouchEvent" + mMin + ".....>" + mMax);
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    private void onStartTrackingTouch() {
        mIsDragging = true;
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    private void onStopTrackingTouch() {
        mIsDragging = false;
    }

    /**
     * Ensures correct size of the widget.
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = 0;
        if (mMinBmp != null) {

            height = mMinBmp.getHeight();
        }
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }

        setMeasuredDimension(width,
                (int) getResources().getDimension(R.dimen.rangeseekbar_height));
    }

    private final RectF m_rectDraw = new RectF();

    private String mMinStr, mMaxStr;


    private int mbottom = getResources().getDimensionPixelSize(
            R.dimen.state_width) / 2;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int bsTop;
        if (isSide) {
            bsTop = getHeight() / 2;
        } else {
            bsTop = getHeight() - mbottom - 2;
        }

        mbase.set(padding, bsTop, getWidth() - padding, bsTop + 4);
    }

    private RectF mbase = new RectF();

    /**
     * Draws the widget on the given canvas.
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        m_rectDraw.set(mbase);
        paint.setColor(progressNormalCcolor);
        canvas.drawRect(m_rectDraw, paint);
        if (mCanTouchMaxHand) {
            m_rectDraw.left = msToPx(mMin);
        } else {
            m_rectDraw.left = msToPx(0);
        }

        m_rectDraw.right = msToPx(mMax);

        // orange color
        paint.setColor(progressBetweenColor);
        baseDistance = m_rectDraw.right - m_rectDraw.left + mTouchedWidth;
        canvas.drawRect(m_rectDraw, paint);

        if (mCanTouchMaxHand) {
            m_rectDraw.left = msToPx(mMin);
        } else {
            m_rectDraw.left = msToPx(0);
        }
        m_rectDraw.right = msToPx(progress);

        paint.setColor(progressProgressColor);

        canvas.drawRect(m_rectDraw, paint);


        if (mCanTouchMaxHand) { // 左边把手不自动滚动
            mMinStr = DateTimeUtils.stringForTime(getSelectedMinValue());

        } else { // 左边把手跟随进度滚动
            mMinStr = DateTimeUtils.stringForTime(progress);
        }


        mMaxStr = DateTimeUtils
                .stringForTime(mMax);

        float textDis = baseDistance - mPaint.measureText(mMinStr) - textDistance;
        if (textDis >= 0) {
            textDis = 0;
        }
        int extraPadding = 0;
        if (isMaxDisOutOfBound) {
            extraPadding = (int) (textDis < 0 ? textDis : -textDis);
        }
        if (isAutoScroll) {
            creatMinBmp();
            if (mCanTouchMaxHand) { // 画左、右边的把手
                drawThumb(mMinBmp,
                        msToPx(mMin) + textDis / 2
                                + extraPadding, canvas);
                createMaxBmp();
                drawThumb(mMaxBmp,
                        msToPx(mMax), canvas);

            } else {
                drawThumb(mMinBmp,
                        msToPx(progress), canvas); // 左边把手跟随进度滚动
            }

        } else {
            if (mTouchLeftOrRight) {// 手指滑动左边
                creatMinBmp();
                canvas.drawBitmap(mMinBmp, m_rectDraw.right
                                - thumbHalfWidth + textDis / 2 + extraPadding,
                        (m_rectDraw.bottom + m_rectDraw.top - mMinBmp
                                .getHeight()) / 2, paint);
                if (mCanTouchMaxHand) {
                    createMaxBmp();
                    drawThumb(mMaxBmp,
                            msToPx(mMax), canvas);
                }

            } else {
                if (mCanTouchMaxHand) {
                    creatMinBmp();
                    drawThumb(mMinBmp,
                            msToPx(mMin) + textDis
                                    / 2 + extraPadding, canvas);
                    createMaxBmp();
                    canvas.drawBitmap(mMaxBmp,
                            msToPx(mMax)
                                    - thumbHalfWidth, (m_rectDraw.bottom
                                    + m_rectDraw.top - mMinBmp
                                    .getHeight()) / 2, paint);
                }
            }

        }

    }


    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putInt("MIN", mMin);
        bundle.putInt("MAX", mMax);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        mMin = bundle.getInt("MIN");
        mMax = bundle.getInt("MAX");
    }

    /***
     * 画时间进度
     * @param bmp
     * @param screenCoord
     * @param canvas
     */
    private void drawThumb(Bitmap bmp, float screenCoord, Canvas canvas) {

        if (bmp != null) {
            canvas.drawBitmap(bmp, screenCoord - thumbHalfWidth,
                    (m_rectDraw.bottom + m_rectDraw.top - bmp.getHeight()) / 2,
                    paint);
            System.gc();
        }
    }


    private void creatMinBmp() {
        mMinBmp = createBitmap(mMinStr, true);
        System.gc();
    }

    private void createMaxBmp() {
        mMaxBmp = createBitmap(mMaxStr, false);
        System.gc();
    }

    private Paint mPaint = new Paint(); // 当前播放进度的画笔
    private Paint pbigger = new Paint();// 提示信息的画笔
    private boolean isMaxDisOutOfBound;

    private Bitmap createBitmap(String text, boolean isLeft) {
        Bitmap newbmp = null;

        if (tempBgBmp != null) {
            float textDis = baseDistance - mPaint.measureText(text) - textDistance;
            if (isSide) {
                newbmp = Bitmap.createBitmap(tempBgBmp.getWidth(),
                        tempBgBmp.getHeight(), Config.ARGB_8888);
            } else {
                if (textDis < 0) {
                    newbmp = Bitmap.createBitmap(tempBgBmp.getWidth()
                                    - (int) textDis, tempBgBmp.getHeight() + 45,
                            Config.ARGB_8888);

                } else {
                    newbmp = Bitmap.createBitmap(tempBgBmp.getWidth(),
                            tempBgBmp.getHeight() + 45, Config.ARGB_8888);
                }

            }

            if (newbmp != null) {
                Canvas c = new Canvas(newbmp);
                float touchPadding = mTouchedWidth / 2;
                if (isLeft) {
                    touchPadding = -touchPadding;
                }
                if (isSide) {
                    c.drawBitmap(tempBgBmp, touchPadding, 0, paint);
                } else {
                    if (textDis < 0) {
                        float minDis = newbmp.getWidth() / 2
                                - msToPx(mMin);
                        float maxDis = newbmp.getWidth()
                                / 2
                                - (getWidth() - msToPx(mMax));
                        float dis;
                        isMaxDisOutOfBound = false;
                        if (minDis > maxDis) {
                            dis = minDis;
                            if (dis < 0) {
                                dis = 0;
                            }
                        } else {
                            dis = maxDis;
                            if (dis < 0) {
                                dis = 0;
                            } else {
                                isMaxDisOutOfBound = true;
                                dis = -dis;
                            }
                        }
                        if (isLeft) {
                            textDis = -textDis;
                            int extraPadding = 0;
                            if (isMaxDisOutOfBound) {
                                extraPadding = (int) (textDis > 0 ? textDis
                                        : -textDis);
                            }
                            c.drawBitmap(tempBgBmp, touchPadding + textDis / 2
                                    + extraPadding, 25, paint);
                            c.drawText(text, touchPadding + (newbmp.getWidth())
                                            / 2 - (textDis / 2) + dis + extraPadding,
                                    30, mPaint);
                        } else {
                            c.drawBitmap(tempBgBmp, touchPadding, 25, paint);
                            c.drawText(text, touchPadding + (tempBgBmp.getWidth())
                                    / 2 - (textDis / 2) + dis, 30, mPaint);
                        }

                    } else {
                        c.drawText(text,
                                touchPadding + (newbmp.getWidth()) / 2, 30, mPaint);
                        c.drawBitmap(tempBgBmp, touchPadding, 25, paint);
                    }

                }
                c.save(Canvas.ALL_SAVE_FLAG);// 保存
                c.restore();// 存储
            }
        }
        return newbmp;

    }

    /***
     *检测当前手指按住的位置是否在任一把手上
     * @param touchX
     * @param ty
     * @return
     */
    private Thumb evalPressedThumb(float touchX, float ty) {
        Thumb result = null;
        boolean minThumbPressed = isInHandler(ty, touchX, mMin);

        if (mCanTouchMaxHand) {
            boolean maxThumbPressed = isInHandler(ty, touchX,
                    mMax);
            if (minThumbPressed && maxThumbPressed) {
                result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
            } else if (minThumbPressed) {
                result = Thumb.MIN;
            } else if (maxThumbPressed) {
                result = Thumb.MAX;
            }
        } else {
            result = minThumbPressed ? Thumb.MIN : null;
        }

        return result;
    }

    /**
     * 检测当前手指按住的位置是否在指定的把手上
     *
     * @param ty
     * @param touchX
     * @param handlerValue
     * @return
     */
    private boolean isInHandler(float ty, float touchX, int handlerValue) {

        return Math.abs(touchX - msToPx(handlerValue)) <= thumbHalfWidth
                && (Math.abs(ty - (mbase.top + mbase.height() / 2)) < 35);
    }


    /**
     * 进度转像素
     *
     * @param msValue
     * @return
     */
    private int msToPx(int msValue) {
        return (int) (padding + ((msValue + 0.0f) / duration) * (getWidth() - 2 * padding));
    }


    /***
     * 像素转进度
     * @param px
     * @return
     */
    private int pxToMs(float px) {
        int width = getWidth();
        if (width <= 2 * padding) {
            return 0;
        } else {
            double temp = Math.min(1, Math.max(0, (px - padding + 0.0f) / (width - 2 * padding)));
            int result = (int) (duration * temp);
            return result;
        }
    }

    /**
     * Thumb constants (min and max).
     */
    private static enum Thumb {
        MIN, MAX
    }

    ;


    /**
     * 最大按钮是否可以触摸
     */
    public void canTouchRight() {
        mCanTouchMaxHand = true;
    }

}