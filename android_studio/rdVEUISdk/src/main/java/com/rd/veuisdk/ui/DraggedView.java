package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rd.veuisdk.R;

/**
 * 拖动到垃圾桶 {包含垃圾箱(mTrashRect) 和拖动部分(mTempBitmapRect)}
 *
 * @author JIAN
 */
public class DraggedView extends View {


    private boolean mShowTrash;

    public DraggedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        mStrRelease = res.getString(R.string.release_hand_for_del);
        mDraggedInfo = res.getString(R.string.drag_into_trash_del);
        mTashCirclePaint.setAntiAlias(true);
        mFoceCircleColor = res.getColor(R.color.transparent_black80);
        mCicColor = res.getColor(R.color.trash_border_color);
        mTashCirclePaint.setColor(mCicColor);

        mRectPaint.setAntiAlias(true);
        mRectPaint.setColor(Color.WHITE);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.WHITE);

        mTextPaint.setTextSize(res.getDimensionPixelSize(R.dimen.text_size_16));

        mStrTemp = mDraggedInfo;

        mTrashPress = res.getDrawable(R.drawable.trash_p);
        mTrashNormal = res.getDrawable(R.drawable.trash_n);
        mTrashDrawable = mTrashNormal;
    }

    private final int mFoceCircleColor, mCicColor;
    private final Drawable mTrashNormal, mTrashPress;
    private final String mStrRelease, mDraggedInfo;
    private Paint mRectPaint = new Paint(), mTextPaint = new Paint(),
            mTashCirclePaint = new Paint();

    private Drawable mTrashDrawable;
    private String mStrTemp;

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) {
            recycle();
        }
    }

    /**
     * 释放资源
     */
    private void recycle() {
        mTempBitmapRect = null;
        if (null != mTempBitmap && !mTempBitmap.isRecycled())
            mTempBitmap.recycle();
        mTempBitmap = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        if (mShowTrash) {
            if (null != mTrashRect) {
                if (mDeleteItem) {
                    mStrTemp = mStrRelease;
                    mTashCirclePaint.setColor(mCicColor);
                    mTrashDrawable = mTrashPress;
                } else {
                    mStrTemp = mDraggedInfo;
                    mTrashDrawable = mTrashNormal;
                    mTashCirclePaint.setColor(mFoceCircleColor);
                }

                int strWidth = (int) mTextPaint.measureText(mStrTemp);
                FontMetrics fm = mTextPaint.getFontMetrics();

                int mleft = mTrashRect.left + (mTrashRect.width() - strWidth)
                        / 2;

                canvas.drawCircle(mTrashRect.left + mTrashRect.width() / 2,
                        mTrashRect.top + mTrashRect.height() / 2, mRadius,
                        mTashCirclePaint);

                canvas.drawText(mStrTemp, mleft,
                        mTrashRect.top + mTrashRect.height() / 2 - mRadius
                                - Math.abs(fm.ascent), mTextPaint);

                mTrashDrawable.setBounds(mTrashRect);
                mTrashDrawable.draw(canvas);
            }
        }

        if (null != mTempBitmapRect) {
            if (mIsForceIng) {
                canvas.drawRect(new Rect(mTempBitmapRect.left - 5,
                        mTempBitmapRect.top - 5, mTempBitmapRect.right + 5,
                        mTempBitmapRect.bottom + 5), mRectPaint);

            }
            if (mTempBitmap != null && !mTempBitmap.isRecycled()) {
                canvas.drawRect(mTempBitmapRect, mTextPaint);
                canvas.drawBitmap(mTempBitmap, null, mTempBitmapRect, null);

            }
        }

    }

    private Rect mTrashRect;// 垃圾箱的区域
    private int mRadius = 0;

    /**
     * 预览区域的中心点
     */
    public void initTrashRect(int imageCenterY) {
        int width = 120;
        int mleft = (getWidth() - width) / 2;
        int nleft = mleft;
        int ntop = imageCenterY - width / 3 * 2;
        mTrashRect = new Rect(nleft, ntop, nleft + width, ntop + width);
        mRadius = (int) Math.sqrt((mTrashRect.width() * mTrashRect.width())
                + (mTrashRect.height() * mTrashRect.height())) / 2;
    }

    private Bitmap mTempBitmap;

    private Rect mTempBitmapRect;

    public void setData(Bitmap bp, int left, int top, int right, int bottom) {
        mTempBitmap = bp;
        mTempBitmapRect = new Rect(left, top, right, bottom);
        invalidate();

    }

    // 取消
    public void onCancel() {
        invalidate();
        if (null != mTrashListener) {
            mTrashListener.onCancel();
        }

    }

    private boolean mIsForceIng = false;

    private boolean mResult;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mResult = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsForceIng = true;
                mResult = mIsForceIng;

                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_SCROLL:
                if (mResult && null != mTempBitmapRect) {
                    int mx = (int) event.getX(), my = (int) event.getY();

                    // 计算手指的偏移量
                    float pX = mx
                            - (mTempBitmapRect.width() / 2 + mTempBitmapRect.left);// 向X轴偏移多少个单位长度
                    float pY = my
                            - (mTempBitmapRect.height() / 2 + mTempBitmapRect.top);// 向Y轴偏移多少个单位长度
                    int mleft = (int) (mTempBitmapRect.left + pX), mtop = (int) (mTempBitmapRect.top + pY), mright = (int) (mTempBitmapRect
                            .width() + mleft), mbottom = (int) (mTempBitmapRect
                            .height() + mtop);
                    if (null != mScollListener) {
                        mScollListener.onTouchMove(mx, my);
                    }
                    if (mtop > 0 && mright > 0 && mbottom > 0) { // 40px由于上层图片比下层图片更宽，更高
                        mTempBitmapRect.set(mleft, mtop, mright, mbottom);
                        checkCandel();
                    }

                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mResult && null != mTempBitmapRect) {
                    checkCandel();
                    if (mDeleteItem)
                        setLocation();
                    else {
                        // 还原videoTimelne的位置
                        if (null != mTrashListener) {
                            mTrashListener.onCancel();
                        }
                    }
                    mDeleteItem = false;
                } else {
                    if (null != mTrashListener) {
                        mTrashListener.onCancel();
                    }
                }
                break;

            default:
                break;
        }
        if (mResult) {
            invalidate();
            return true;
        }
        return false;
    }

    private boolean mDeleteItem = false;

    private final int MARGIN = 100;

    private void checkCandel() {

        /**
         * 判断两个rect 是否有交集
         */

        if (mTempBitmapRect.top <= mTrashRect.bottom + MARGIN
                && mTempBitmapRect.bottom > mTrashRect.top - MARGIN
                && mTempBitmapRect.left < mTrashRect.right + MARGIN
                && mTempBitmapRect.right > mTrashRect.left - MARGIN) {

            mDeleteItem = true;
        } else {
            mDeleteItem = false;
        }
    }

    /***
     * 定位垃圾箱位置
     */
    private void setLocation() {

        int centerX = mTrashRect.left + mTrashRect.width() / 2;
        int centerY = mTrashRect.top + mTrashRect.height() / 2;

        int poldcenterX = mTempBitmapRect.left + mTempBitmapRect.width() / 2;
        int poldcenterY = mTempBitmapRect.top + mTempBitmapRect.height() / 2;

        int px = centerX - poldcenterX;
        int py = centerY - poldcenterY;

        int newleft = mTempBitmapRect.left + px;
        int newtop = mTempBitmapRect.top + py;

        mTempBitmapRect.set(newleft, newtop, newleft + mTempBitmapRect.width(),
                newtop + mTempBitmapRect.height());

        if (null != mTrashListener) {
            if (mShowTrash) {
                mTrashListener.onDelete();
            } else {
                mTrashListener.onCancel();
            }

        }

    }

    private onTrashListener mTrashListener;

    public void setTrashListener(onTrashListener listener) {
        mTrashListener = listener;
    }

    private ITashScroll mScollListener;

    public void setScollListener(ITashScroll mScollListener) {
        this.mScollListener = mScollListener;
    }

    public interface ITashScroll {
        void onTouchMove(int x, int y);
    }

    public void setTrash(boolean mShowTrash) {
        this.mShowTrash = mShowTrash;
    }


    public interface onTrashListener {

        /**
         * 拖拽到垃圾箱,执行删除功能
         */
        void onDelete();

        /**
         * 取消拖拽
         */
        void onCancel();
    }

}
