package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.DateTimeUtils;

import java.lang.ref.WeakReference;

/**
 * 字幕微调进度和主题字幕
 *
 * @author abreal
 */
public class ProgressView extends View {
    /**
     * 主题字幕背景，提示可编辑区域
     */
    private Drawable mTitleBackDrawable;
    /**
     * 主题字幕显示区域
     */
    private Rect mTitleBackRect = new Rect();
    private boolean bThemeTitleBackShowing;
    private TextPaint mTitleEditPaint;
    private final String TITLE_EDIT_HITE_TEXT = getResources().getString(R.string.click_dash_line_to_edit);
    private String TAG = ProgressView.class.getName();
    private int mTitleEditHiteTextWidth;
    private int mCenterX = 0, mCenterY = 0;
    private GestureDetector gesDetector;
    private float duration;
    private int progress = 0;//单位毫秒
    private int mScale = 0;
    private Paint mPaint = new Paint();
    private boolean IsShowTime = true;
    private boolean bCanScroll = false;
    private boolean onFocuse = false;

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(getResources()
                .getDimensionPixelSize(R.dimen.text_size_19));
        mPaint.setColor(getResources().getColor(R.color.kxblue));
        gesDetector = new GestureDetector(context, new pressGestureListener());
        mTitleBackDrawable = getResources().getDrawable(
                R.drawable.theme_title_preview_back);
        mTitleEditPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        mTitleEditPaint.setColor(Color.WHITE);
        mTitleEditPaint.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.text_size_10));
        mTitleEditHiteTextWidth = Math.round(mTitleEditPaint
                .measureText(TITLE_EDIT_HITE_TEXT));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
    }


    private class pressGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (bCanScroll) {
                if (!onFocuse) {
                    if (listener != null && listener.get() != null) {
                        listener.get().onStart();
                    }
                    onFocuse = true;
                } else {
                    mCenterX = (int) (e2.getX());
                    mCenterY = (int) e2.getY();
                    onScrolling((int) (-distanceX));
                }
                invalidate();
                return true;
            }
            return true;

        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float veSlocityX, float velocityY) {
            onChanged();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (bThemeTitleBackShowing
                    && mTitleBackRect.contains(Math.round(e.getX()),
                    Math.round(e.getY())) && editTitleListener != null
                    && editTitleListener.get() != null) {
                Rect rectClick = Rect.unflattenFromString(mTitleBackRect
                        .flattenToString());
                editTitleListener.get().onEditTitleClick(ProgressView.this,
                        rectClick);
            } else if (listener != null && listener.get() != null) {
                listener.get().onClick();
            }

            return false;
        }
    }

    private void onChanged() {
        if (listener != null && listener.get() != null) {
            listener.get().onChanged();
        }
        onFocuse = false;
        invalidate();
    }

    private void onScrolling(int offx) {
        float np = progress + (offx * duration / (+0.0f + getWidth()));
        if (np != progress) {
            if (np < 0) {
                np = 0;
            } else if (np > duration) {
                np = duration;
            }
            progress = (int) np;
            if (listener != null && listener.get() != null) {
                listener.get().onProgressing(progress);
            }
        }
        int scale = mScale + offx * 10000 / getWidth();
        if (scale != 0) {
            if (scale > 10000) {
                scale = 10000;
            } else if (scale < -10000) {
                scale = -10000;
            }
            mScale = scale;
            if (listener != null && listener.get() != null) {
                listener.get().onSeekbarChanging(mScale);
            }
        }
        onFocuse = true;
        invalidate();
    }


    /**
     * 单位毫秒
     *
     * @param duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
        invalidate();
    }


    /**
     * 单位毫秒
     *
     * @param nprogress
     */
    public void setProgress(int nprogress) {
        progress = nprogress;
        invalidate();
    }


    public void setScroll(boolean canSroll) {
        this.bCanScroll = canSroll;
        if (canSroll) {
            this.setVisibility(View.VISIBLE);
        } else {
            this.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bThemeTitleBackShowing && !mTitleBackRect.isEmpty()) {
            mTitleBackDrawable.setBounds(mTitleBackRect);
            mTitleBackDrawable.draw(canvas);
            canvas.drawText(TITLE_EDIT_HITE_TEXT,
                    (getWidth() - mTitleEditHiteTextWidth) / 2, getHeight()
                            - mTitleEditPaint.descent(), mTitleEditPaint);
        }
        if (IsShowTime) {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                    Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            if (onFocuse) {
                String text = DateTimeUtils.stringForMillisecondTime(progress,
                        true, true);
                int width = (int) mPaint.measureText(text);
                int mly = mCenterY - 100;
                if (mly < 100) {
                    mly = 100;
                } else if (mCenterY > getHeight()) {
                    mly = getHeight() - 100;
                }

                if (mCenterX < 100) {
                    mCenterX = 100;
                } else if (mCenterX > getWidth()) {
                    mCenterX = getWidth() - 100;
                }
                canvas.drawText(text, mCenterX - (width) / 2, mly, mPaint);
            }
        }
    }

    public void setShowTime(boolean isShow) {
        IsShowTime = isShow;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gesDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (onFocuse) {
                onChanged();
            }
            mScale = 0;
        }
        return true;
    }

    public void showThemeTitleBack(boolean bShowing, RectF rectTitleBack) {
        bThemeTitleBackShowing = bShowing;
        if (rectTitleBack != null) {
            mTitleBackRect.set(Math.round(rectTitleBack.left * getWidth()),
                    Math.round(rectTitleBack.top * getHeight()),
                    Math.round(rectTitleBack.right * getWidth()),
                    Math.round(rectTitleBack.bottom * getHeight()));
        }
        postInvalidate();
    }

    private WeakReference<onProgressListener> listener;
    private WeakReference<onEditTitleListener> editTitleListener;

    public void setListener(onProgressListener listener) {
        this.listener = new WeakReference<onProgressListener>(listener);
    }

    public void setListener(onEditTitleListener listener) {
        this.editTitleListener = new WeakReference<onEditTitleListener>(
                listener);
    }

    public interface onProgressListener {

        void onStart();

        /**
         * 单位:毫秒
         *
         * @param progress
         */
        void onProgressing(int progress);

        void onChanged();

        void onClick();

        void onSeekbarChanging(int scale);

    }

    public interface onEditTitleListener {
        public void onEditTitleClick(View view, Rect rectTitleRegion);
    }

}
