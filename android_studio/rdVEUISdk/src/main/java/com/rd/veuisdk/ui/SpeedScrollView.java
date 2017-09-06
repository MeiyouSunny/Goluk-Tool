package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.rd.veuisdk.R;

/**
 * 调速的组件
 *
 * @author JIAN
 */
public class SpeedScrollView extends View {

    private static final String TAG = "SpeedScrollView";
    private Rect mParentRect = new Rect(0, 0, 0, 0);// 滑动范围
    private Rect[] mRectArr;
    private int halfItemWidth;
    private String[] theSpeedDescriptionArr;

    private boolean isForceIng = false;

    private float downX = -1;
    private boolean mCheckRectAngle = true;

    private Paint pBorder = new Paint(), pWhite = new Paint(),
            pText = new Paint();

    private Rect mＷhiteSelectedRect, mBlackBorderRect;
    private int mIndex;//当前速度对应的下标


    public SpeedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        pBorder.setAntiAlias(true);
        pBorder.setColor(getResources().getColor(R.color.black));
        pWhite.setColor(getResources().getColor(R.color.white));
        pText.setAntiAlias(true);
        pText.setColor(getResources().getColor(R.color.white));
        pText.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.text_size_30));
    }

    public SpeedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedScrollView(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mParentRect.isEmpty()) {
            return;
        }
        if (isForceIng) {
            canvas.drawRect(mBlackBorderRect, pBorder);
        }
        canvas.drawRect(mＷhiteSelectedRect, pWhite);
        int strWidth = (int) pText.measureText(theSpeedDescriptionArr[mIndex]);
        FontMetrics fm = pText.getFontMetrics();

        int mleft = mＷhiteSelectedRect.left
                + (mＷhiteSelectedRect.width() - strWidth) / 2;

        int mtop = (int) (mＷhiteSelectedRect.top
                + (mＷhiteSelectedRect.height()) / 2 + Math.abs(fm.ascent) / 2)
                - (int) Math.abs(fm.descent);

        canvas.drawText(theSpeedDescriptionArr[mIndex], mleft, mtop, pText);

    }

    /**
     * 设置速度描述
     *
     * @param arr
     */
    public void setSpeedDescriptions(String[] arr) {
        theSpeedDescriptionArr = arr;
    }


    /**
     * 设置父容器区域
     *
     * @param mleft
     * @param mtop
     * @param width
     * @param height
     * @param itemlineWidth
     */
    public void setParentRect(int mleft, int mtop, int width, int height,
                              int itemlineWidth) {

        mParentRect.set(mleft, mtop, mleft + width, mtop + height);
        Log.d(TAG, String.format("parent rect(%d,%d,%d,%d)", mParentRect.left,
                mParentRect.top, mParentRect.right, mParentRect.bottom));

        mRectArr = new Rect[theSpeedDescriptionArr.length];
        int itemwidth = (mParentRect.width() - itemlineWidth
                * (theSpeedDescriptionArr.length - 1))
                / theSpeedDescriptionArr.length;
        halfItemWidth = itemwidth / 2;
        for (int i = 0; i < theSpeedDescriptionArr.length; i++) {

            Rect mItemRect = new Rect();

            int ml = itemwidth * i + itemlineWidth * i + mleft - 10;
            mItemRect.set(ml, mtop - 10, ml + itemwidth + 20, mtop + height
                    + 10);

            mRectArr[i] = mItemRect;
        }

        Rect temp = mRectArr[0];
        LinearGradient lg = new LinearGradient(temp.width() / 2, 0,
                temp.width() / 2, temp.height(), new int[]{
                getResources().getColor(
                        R.color.gradient_button_back_color_begin),
                getResources().getColor(
                        R.color.gradient_button_back_color_end)},
                new float[]{0.7f, 1f}, TileMode.MIRROR);
        pWhite.setShader(lg);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mBlackBorderRect = new Rect(0, 0, right - left, bottom - top);
        mＷhiteSelectedRect = new Rect(5, 5, right - left - 5, bottom - top - 5);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                isForceIng = true;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (downX != -1) {

                    // 计算手指的偏移量
                    float pX = event.getX() - downX;// 向X轴偏移多少个单位长度
                    int mleft = (int) (getLeft() + pX), mright = mleft + getWidth();

                    mleft = Math.max(Math.max(mleft, 0), mParentRect.left - 40);
                    mright = Math.min(Math.max(getWidth(), mright),
                            mParentRect.right + 40);
                    mleft = mright - getWidth();
                    mCheckRectAngle = false;
                    layout(mleft, getTop(), mright, getBottom());
                    mCheckRectAngle = true;
                    setIndex(getLeft(), getTop(), getRight(), getBottom(), false,
                            true);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isForceIng = false;
                invalidate();
                setIndex(getLeft(), getTop(), getRight(), getBottom(), true, true);
                break;

            default:
                break;
        }

        return true;
    }

    private void setIndex(int left, int top, int right, int bottom,
                          boolean setLocation, boolean user) {
        for (int i = 0; i < mRectArr.length; i++) {

            Rect tempRect = mRectArr[i];

            if (left >= tempRect.left - halfItemWidth
                    && right < tempRect.right + halfItemWidth) {
                mIndex = i;
                break;
            }
        }
        if (setLocation) {
            setLocation(user);
        }
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        if (mCheckRectAngle && !isInEditMode()) {
            Rect rectSet = new Rect(l, t, r, b);
            Rect tagRect = mRectArr[mIndex];
            if (tagRect.equals(rectSet)) {
                super.layout(l, t, r, b);
            }
        } else {
            super.layout(l, t, r, b);
        }
    }

    /**
     * 设置速度
     *
     * @param speed
     */
    public void setSpeed(float speed) {
        if (speed == 0.25f) {
            mIndex = 0;
        } else if (speed == 0.5f) {
            mIndex = 1;
        } else if (speed == 2f) {
            mIndex = 3;
        } else if (speed == 4f) {
            mIndex = 4;
        } else {
            mIndex = (int) (Math.ceil(theSpeedDescriptionArr.length / 2.0) - 1);
        }
        setLocation(false);
        invalidate();
    }

    private void setLocation(boolean user) {
        Rect tagRect = mRectArr[mIndex];
        mCheckRectAngle = false;
        this.layout(tagRect.left, tagRect.top, tagRect.right, tagRect.bottom);
        mCheckRectAngle = true;
        if (null != mlistener) {
            mlistener.onSpeedChanged(user, getSpeed());
        }
    }

    /**
     * 设置到默认位置
     */
    public void setDefaultLocation() {
        Rect tagRect = mRectArr[(int) Math.ceil(mRectArr.length / 2)];
        this.layout(tagRect.left, tagRect.top, tagRect.right, tagRect.bottom);
        this.invalidate();
    }

    /**
     * 获取当前速度
     *
     * @return
     */
    public float getSpeed() {
        float fSpeed = 1.0f;
        switch (mIndex) {
            case 0:
                fSpeed = 0.25f;
                break;
            case 1:
                fSpeed = 0.5f;
                break;
            case 3:
                fSpeed = 2;
                break;
            case 4:
                fSpeed = 4;
                break;
            default:
                fSpeed = 1;
                break;
        }
        return fSpeed;
    }

    private onSpeedListener mlistener;

    /**
     * 设置速度改变listener
     *
     * @param onSpeedListener
     */
    public void setSpeedChangedListener(onSpeedListener onSpeedListener) {
        mlistener = onSpeedListener;
    }

    /**
     * 速度改变listener
     *
     * @author abreal
     */
    public interface onSpeedListener {

        /**
         * 响应速度发生变化
         *
         * @param user  是否为用户操作
         * @param speed 速度
         */
        public void onSpeedChanged(boolean user, float speed);

    }

}
