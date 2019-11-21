package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.SysAlertDialog;

/**
 * 横向拖动单个item
 *
 * @author JIAN
 */
public class DragItemScrollView extends View {
    private float mMinDuration = 0;

    private final int MPADDING = 30;
    private final int BORDER_WIDTH = 4;
    private Paint mBorderPaint = new Paint(), mWhitePaint = new Paint(),
            mTextPaint = new Paint();

    private Rect mWhiteSelectedRect = new Rect(),
            mBlackBorderRect = new Rect();
    private int mIndex = 3;
    private Paint mPaint = new Paint(), mItemTextPaint = new Paint();
    private CharSequence[] mCharArrays = null;
    private Rect[] mRects = null;
    private int mHalfItemWidth;
    private int mItemWidth;
    private int mTextHeightOff1 = 0, mTextHeightOff2;
    private int MPADDING_LEFT = 60;
    private int mItemSizeN = 0, mItemSizeP = 0, mItemP = 0;
    private float mDuration;
    private boolean mIsLimit;

    public DragItemScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray tA = context.obtainStyledAttributes(attrs,
                R.styleable.extdragarray);
        mCharArrays = tA.getTextArray(R.styleable.extdragarray_sArrays);
        tA.recycle();
        mIndex = (int) Math.ceil(mCharArrays.length / 2.0 - 1);
        mRects = new Rect[mCharArrays.length];
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(getResources().getColor(R.color.black));
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setStrokeWidth(BORDER_WIDTH);

        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources()
                .getColor(R.color.speed_line_button_back_color));
        mItemTextPaint.setAntiAlias(true);
        mItemTextPaint.setColor(getResources().getColor(
                R.color.speed_line_button_text_color));
        mItemTextPaint
                .setTextSize(getResources().getDimension(R.dimen.text_size_16));
        FontMetrics fm = mItemTextPaint.getFontMetrics();
        mTextHeightOff1 = (int) ((Math.abs(fm.ascent) + Math.abs(fm.leading) + Math
                .abs(fm.descent)) / 2 - Math.abs(fm.descent));

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(getResources().getColor(R.color.public_menu_back_color));
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.text_size_30));
        fm = mTextPaint.getFontMetrics();
        mTextHeightOff2 = (int) ((Math.abs(fm.ascent) + Math.abs(fm.leading) + Math
                .abs(fm.descent)) / 2 - Math.abs(fm.descent));

        MPADDING_LEFT = CoreUtils.dpToPixel(30);

        mGesDetector = new GestureDetector(context, new pressGestureListener());
        mItemSizeN = getResources().getDimensionPixelSize(R.dimen.speed_item_size_n);
        mItemSizeP = getResources().getDimensionPixelSize(R.dimen.speed_item_size_p);

        mItemP = (mItemSizeP - mItemSizeN) / 2;


    }

    public void setMinDuration(float minDuration) {
        mMinDuration = minDuration;
    }

    private GestureDetector mGesDetector;

    public DragItemScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragItemScrollView(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawColor(Color.GREEN);
        Rect temp = null;
        String text = null;
        int itemlength = 0;
        int len = mRects.length;
        for (int i = 0; i < len; i++) {
            temp = mRects[i];
            canvas.drawRect(temp, mPaint);
            text = (String) mCharArrays[i];
            itemlength = (int) mItemTextPaint.measureText(text);
            int x = (temp.right + temp.right - mItemWidth) / 2 - itemlength / 2;
            canvas.drawText(text, x, mTextY1, mItemTextPaint);

        }

        mWhiteSelectedRect.set(mCurrentCheckedRect.left - mItemP,
                mCurrentCheckedRect.top - mItemP, mCurrentCheckedRect.right
                        + mItemP, mCurrentCheckedRect.bottom + mItemP);

        if (mIsForced) {
            mBlackBorderRect.set(mWhiteSelectedRect.left - BORDER_WIDTH,
                    mWhiteSelectedRect.top - BORDER_WIDTH,
                    mWhiteSelectedRect.right + BORDER_WIDTH,
                    mWhiteSelectedRect.bottom + BORDER_WIDTH);

            canvas.drawRect(mBlackBorderRect, mBorderPaint);
        }

        canvas.drawRect(mWhiteSelectedRect, mWhitePaint);

        text = (String) mCharArrays[mIndex];
        int strWidth = (int) mTextPaint.measureText(text);
        int mleft = mWhiteSelectedRect.left
                + (mWhiteSelectedRect.width() - strWidth) / 2;
        canvas.drawText(text, mleft, mTextY2, mTextPaint);
    }

    private int mTextY1, mTextY2;

    public void setDuration(float duration) {
        mDuration = duration;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int len = mCharArrays.length;
        Rect rect = null;
        mItemWidth = mItemSizeN;

        MPADDING_LEFT = (getWidth() - BORDER_WIDTH * (len - 1) - mItemSizeN * len) / 2;
        mHalfItemWidth = mItemWidth / 2;
        int mleft = MPADDING_LEFT;
        for (int i = 0; i < len; i++) {

            int mtop = (getHeight() - mItemWidth) / 2;
            rect = new Rect(mleft, mtop, mleft + mItemWidth, mtop
                    + mItemWidth);
            mRects[i] = rect;
            mleft += (mItemWidth + BORDER_WIDTH);

        }

        mTextY1 = (int) (0 + mRects[mIndex].height() / 2 + mTextHeightOff1)
                + mRects[mIndex].top;

        mCurrentCheckedRect = new Rect(mRects[mIndex]);

        mTextY2 = (int) (0 + mCurrentCheckedRect.height() / 2 + mTextHeightOff2)
                + mCurrentCheckedRect.top;

        mWhitePaint.setColor(getResources().getColor(R.color.white));
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//
////        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
//
////        super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth,
////                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(2 * MPADDING
////                + mItemWidth, MeasureSpec.EXACTLY));
//
//    }

    private boolean mIsForced = false;

    private float mXPosition = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGesDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mIsForced) {
                mXPosition = event.getX();
                onActionUp();
            }
        }
        invalidate();
        return true;
    }

    private class pressGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {

            if (e2.getX() > mItemWidth / 2
                    && e2.getX() < getWidth() - mItemWidth / 2) {
                mCurrentCheckedRect.offset(-(int) distanceX, 0);
                setIndex(false, true);
            }
            mXPosition = e2.getX();
            return true;

        }

        @Override
        public boolean onDown(MotionEvent e) {
            mIsForced = true;
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float veSlocityX, float velocityY) {
            onActionUp();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mXPosition = e.getX();
            onActionUp();
            return false;
        }
    }

    private void onActionUp() {
        mCurrentCheckedRect.offsetTo((int) mXPosition - mItemWidth / 2,
                mCurrentCheckedRect.top);
        mIsForced = false;
        setIndex(true, true);
    }

    private void setIndex(boolean setLocation, boolean user) {
        for (int i = 0; i < mRects.length; i++) {

            Rect tempRect = mRects[i];

            if (mCurrentCheckedRect.left >= tempRect.left - mHalfItemWidth
                    && mCurrentCheckedRect.right < tempRect.right
                    + mHalfItemWidth) {
                if (mMinDuration != 0) {
                    int scale = i - mIndex;
                    float speed = 1;
                    if (scale > 0) {
                        for (int n = 0; n < scale; n++) {
                            speed *= 2;
                        }
                    } else {
                        for (int n = 0; n < Math.abs(scale); n++) {
                            speed /= 2;
                        }
                    }
                    if (mDuration / speed < mMinDuration) {
                        mIsLimit = true;
                        break;
                    }
                    mIsLimit = false;
                    mDuration /= speed;
                }
                mIndex = i;
                break;
            }
        }
        if (setLocation) {
            if (mIsLimit) {
                SysAlertDialog.showAutoHideDialog(getContext(), 0, R.string.video_speed_duration_too_short_to_change, 2000);
            }
            setLocation(user);
        }
    }

    private Rect mCurrentCheckedRect;

    /**
     * 设置速度
     *
     * @param index
     */
    public void setCheckIndex(int index) {
        mIndex = index;
        if (mIndex > mCharArrays.length || mIndex < 0) {
            mIndex = (int) (Math.ceil(mCharArrays.length / 2.0) - 1);
        }
        mCurrentCheckedRect = new Rect(mRects[mIndex]);
        invalidate();
    }

    private void setLocation(boolean user) {
        Rect tagRect = mRects[mIndex];
        mCurrentCheckedRect = new Rect(tagRect);
        if (null != mCheckListener) {
            mCheckListener.onCheckedChanged(user, mIndex);
        }
    }

    private onCheckedListener mCheckListener;

    /**
     * 设置速度改变listener
     *
     * @param onSpeedListener
     */
    public void setCheckedChangedListener(onCheckedListener onSpeedListener) {
        mCheckListener = onSpeedListener;
    }

    /**
     * 速度改变listener
     *
     * @author abreal
     */
    public interface onCheckedListener {

        /**
         * 响应速度发生变化
         *
         * @param user  是否为用户操作
         * @param index 速度
         */
        public void onCheckedChanged(boolean user, int index);

    }

}
