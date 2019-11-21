package com.rd.veuisdk.ui.extrangseekbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.utils.DateTimeUtils;

/**
 * TrimActivity 截取视频播放器进度条 左右有把手的进度条
 *
 * @author JIAN
 */
public class TrimRangeSeekbarPlus extends RangSeekBarBase {
    private final String TAG = "ExtRangeSeekbarPlus";

    public final int HANDLE_LEFT = 1;
    public final int HANDLE_RIGHT = 2;
    public final int HANDLE_NONE = 0;

    private Paint mBgPaint = new Paint(), mProgressPaint = new Paint(),
            mLinePaint = new Paint(), mShadowPaint = new Paint(), mTextPaint = new Paint();
    private Rect mLeftRect = new Rect(), mRightRect = new Rect(),
            mProgressRect = new Rect(), mShadowLeftRect = new Rect(),
            mShadowRightRect = new Rect(), mLineRect = new Rect(),
            mPositionRect = new Rect(), mShadowCenterRect = new Rect();
    private Drawable mDrawableLeft, mDrawableRight, mDrawablePosition;

    private final int MARGIN = 0;// 上下5px
    private int mProgressBarWidth = 10;
    private int mHeight, mThumbHeight;
    private Resources mResources;

    public int mCurHandle = HANDLE_NONE;
    private int mHighLight = 10;
    private int mColorTransparent;
    private int mTop;
    private int mHandleWidht, mHandleHeight;

    @SuppressWarnings("deprecation")
    public TrimRangeSeekbarPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResources = getResources();
        mDrawableLeft = mResources.getDrawable(R.drawable.seekbar_hand_left);
        mDrawableRight = mResources.getDrawable(R.drawable.seekbar_hand_right);
        mDrawablePosition = mResources.getDrawable(R.drawable.edit_duration_bg_white);
        /* 设置渐变色 */
        mBgPaint.setAntiAlias(true);
        mProgressPaint.setColor(mResources.getColor(R.color.white));
        mProgressPaint.setAntiAlias(true);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(mResources.getColor(R.color.white));
        mLinePaint.setStrokeWidth(3);
        mTextPaint.setColor(mResources.getColor(R.color.black));

        mTextPaint.setAntiAlias(true);

        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStyle(Style.FILL);
        mShadowPaint.setColor(mResources.getColor(R.color.transparent_black80));
        HANDWIDTH = mResources.getDimensionPixelSize(R.dimen.handWidth);
        mProgressBarWidth = mResources.getDimensionPixelSize(R.dimen.progressbarWidth);
        mThumbHeight = mResources
                .getDimensionPixelSize(R.dimen.preview_rangseekbarplus_height);
        mHeight = mResources
                .getDimensionPixelSize(R.dimen.preview_rangseekbarplus_height);
        mTop = mResources.getDimensionPixelSize(R.dimen.preview_intercept_margintop);
        // 84*52
        double scaleSize = mTop / 52.0; // 滑动把手进度背景图宽高84px*52px

        mHandleWidht = (int) (scaleSize * 84);
        mHandleHeight = (int) (scaleSize * 52);
        int textSize = (int) (scaleSize * 18);
        mTextPaint.setTextSize(textSize);

        pLight.setAntiAlias(true);
        pLight.setStyle(Style.FILL);
        pLight.setColor(mResources.getColor(R.color.trim_point_color));
        mHighLight = mResources.getDimensionPixelSize(R.dimen.point_width);
        mColorTransparent = mResources.getColor(R.color.transparent);
    }

    /**
     * 单位：秒
     *
     * @return
     */
    public long getDuration() {
        return mDuration;
    }

    private long mDuration;

    /**
     * 单位：秒
     *
     * @param duration
     */
    public void setDuration(long duration) {
        mDuration = duration;
        max = mDuration;
        invalidate();
    }

    private long min, max;
    private int HANDWIDTH = 20;// 定义左右把手的图片宽度为20px

    public void setMin(long mMin) {
        if (bottom == 0) {
            initTopBottom();
        }
        if (mMin > max || mMin < 0) {
            mMin = 0;
        }

        this.min = mMin;
        int mleft = (int) (getSeekbarWith() * min / mDuration);

        mLeftRect.set(mleft, top, mleft + HANDWIDTH, bottom);
        mLineRect.set(mLeftRect.left, top, mLineRect.right, bottom);
        setProgress(min);
    }

    private final int DEFAULTMINDURATION = 1000;// 两个把手距离 默认最少1s
    private int minthumbDuration = DEFAULTMINDURATION;

    /**
     * 单位：秒
     *
     * @param mMin
     * @param mMax
     */
    public void setSeekBarRangeValues(long mMin, long mMax) {
        setMax(mMax);
        setMin(mMin);
    }

    public void setMax(long mMax) {
        if (mMax > mDuration) {
            mMax = mDuration;
        }
        this.max = mMax;
        int mleft = (int) (HANDWIDTH + (getSeekbarWith() * max / mDuration));
        mRightRect.set(mleft, top, mleft + HANDWIDTH, bottom);
        mLineRect.set(mLineRect.left, top, mRightRect.right, bottom);

        setProgress(max);

    }

    public long getSelectedMinValue() {
        return min;
    }

    public long getSelectedMaxValue() {
        return max;
    }

    public void setProgress(long progress) {
        if (mDuration > 0) {
            int left = 0;
            if (isByHand) {
                if (mShadowType == 1) {
                    left = (int) (HANDWIDTH + ((progress + 0.0) / mDuration * getSeekbarWith()));
                } else if (mShadowType == 2) {
                    if (progress <= min) {
                        left = (int) ((progress + 0.0) / mDuration * getSeekbarWith());
                    } else {
                        left = (int) (HANDWIDTH * 2 + ((progress + 0.0) / mDuration * getSeekbarWith()));
                    }
                }
            } else {
                left = (int) (HANDWIDTH / 2 + ((progress + 0.0) / mDuration * getSeekbarWith()));
            }
            mProgressRect.set(left, top, left + mProgressBarWidth, bottom);// 宽10px
        }
        invalidate();
    }

    /**
     * 0-mDuration 的组件宽度，去除两个把手的宽度
     *
     * @return
     */
    private double getSeekbarWith() {
//		if(isByHand){
        return getWidth() - 2 * HANDWIDTH + 0.0;
//		}else{
//			return getWidth();
//		}
    }

    private int[] lights;

    public void setHighLights(int[] ls) {
        lights = ls;
        invalidate();
    }

    private Paint pLight = new Paint();
    private Drawable trim_bg;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initTopBottom();

    }

    private int top, bottom;

    private void initTopBottom() {
        top = (mHeight - mThumbHeight) + MARGIN;
        bottom = getBottom() - MARGIN;
    }

    private boolean isFocusing = false;
    private boolean isMaxMin = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initTopBottom();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        Rect rectPbg = new Rect(mLeftRect.right - 10, top, mRightRect.left + 10,
                bottom);

        Shader mShader = new LinearGradient(0, 0, rectPbg.width(),
                rectPbg.height(), mColorTransparent, mColorTransparent, TileMode.MIRROR);
        mBgPaint.setShader(mShader);


        if (mShadowType == 2) {
            int l = (int) (getSeekbarWith() * min / mDuration);
            int r = (int) (HANDWIDTH *2 + (getSeekbarWith() / mDuration * max));
            mShadowCenterRect.set(l, top, r, bottom);
            canvas.drawRect(mShadowCenterRect, mShadowPaint);
        } else if (mShadowType == 1){
            int leftP = (int) (getSeekbarWith() * min / mDuration);
            int rightP = (int) (HANDWIDTH + (getSeekbarWith() / mDuration * max));
            int px10 = CoreUtils.dpToPixel(10);
            mShadowLeftRect.set(px10 + getLeft(), top, leftP + HANDWIDTH, bottom);
            mShadowRightRect.set(rightP, top, getRight() - px10, bottom);
            canvas.drawRect(mShadowLeftRect, mShadowPaint);
            canvas.drawRect(mShadowRightRect, mShadowPaint);
        }
        // draw highLight 关键点
        canvas.drawRect(rectPbg, mBgPaint);

        int len = 0;
        if (null != lights) {
            len = lights.length;
            int cy = top + mLeftRect.height() / 2;
            for (int i = 0; i < len; i++) {
                int left = (int) (HANDWIDTH / 2 + ((lights[i] + 0.0)
                        / mDuration * getSeekbarWith()));
                canvas.drawCircle(left, cy, mHighLight, pLight);
            }
        }

        if (isByHand) {
            mDrawableLeft.setBounds(mLeftRect);
            mDrawableLeft.draw(canvas);

            mDrawableRight.setBounds(mRightRect);
            mDrawableRight.draw(canvas);

            if (mShadowType == 1) {
                if (mProgressRect.left < mLeftRect.left + HANDWIDTH) {
                    mProgressRect.set(mLeftRect.left + HANDWIDTH, top,
                            mLeftRect.left + HANDWIDTH + mProgressBarWidth, bottom);
                }
            } else if (mShadowType == 2){
                if (mProgressRect.left > mLeftRect.left
                        && mProgressRect.left < mRightRect.left) {
                    mProgressRect.set(mLeftRect.left, top,
                            mLeftRect.left + mProgressBarWidth, bottom);
                } else if (mProgressRect.left > mRightRect.left
                        && mProgressRect.left < mRightRect.left + HANDWIDTH) {
                    mProgressRect.set(mRightRect.left + HANDWIDTH, top,
                            mRightRect.left + mProgressBarWidth + HANDWIDTH, bottom);
                }
            }

            //进度提示
            int x = 0;
            if (isMaxMin) {
                if (mLeftOrRight == 1) {
                    x = mLeftRect.left + mLeftRect.width() / 2 - mHandleWidht / 2;
                    mPositionRect.set(x, -mHandleHeight, x + mHandleWidht, 0);
                    mDrawablePosition.setBounds(mPositionRect);
                    mDrawablePosition.draw(canvas);
                    long mPosition = (long) ((mLeftRect.left) / getSeekbarWith() * mDuration);
                    setPositionText(canvas, mPosition);
                } else if (mLeftOrRight == 2) {
                    x = mRightRect.left + mRightRect.width() / 2 - mHandleWidht / 2;
                    mPositionRect.set(x, -mHandleHeight, x + mHandleWidht, 0);
                    mDrawablePosition.setBounds(mPositionRect);
                    mDrawablePosition.draw(canvas);
                    long mPosition = (long) ((mRightRect.left - HANDWIDTH)
                            / getSeekbarWith() * mDuration);
                    setPositionText(canvas, mPosition);
                }
            }

        } else {
            trim_bg.setBounds(mLineRect);
            trim_bg.draw(canvas);
            if (mProgressRect.left < mLeftRect.left + HANDWIDTH / 2) {
                mProgressRect.set(mLeftRect.left + HANDWIDTH / 2, top,
                        mLeftRect.left + HANDWIDTH / 2 + mProgressBarWidth, bottom);
            }
        }
        canvas.drawRect(mProgressRect, mProgressPaint);

        // 滑动当前进度
        if (isFocusing) {
            int x = mProgressRect.left + (mProgressRect.width() / 2);
            int mleft = x - (mHandleWidht / 2);
            mPositionRect.set(mleft, -mHandleHeight, mleft + mHandleWidht, 0);
            mDrawablePosition.setBounds(mPositionRect);
            mDrawablePosition.draw(canvas);

            long mPstion = 0;

            if (mShadowType == 1) {
                mPstion = (long) ((mProgressRect.left
                        + (mProgressRect.width() / 2) - HANDWIDTH)
                        / getSeekbarWith() * mDuration);
            } else if (mShadowType == 2) {
                if (mProgressRect.left <= mLeftRect.left) {
                    mPstion = (long) ((mProgressRect.left
                            + (mProgressRect.width() / 2))
                            / getSeekbarWith() * mDuration);
                } else {
                    mPstion = (long) ((mProgressRect.left
                            + (mProgressRect.width() / 2) - HANDWIDTH * 2)
                            / getSeekbarWith() * mDuration);
                }
            }

            setPositionText(canvas, mPstion);
        }

    }

    /**
     * 显示时间
     * @param canvas
     * @param position
     */
    private void setPositionText(Canvas canvas, long position) {

        String str = gettime((int) Math.max(0, position));

        int mwidth = (int) mTextPaint.measureText(str);

        canvas.drawText(str, (mPositionRect.left
                        + (mPositionRect.width() / 2) - (mwidth / 2)),
                (mPositionRect.top + (mPositionRect.height() / 2)), mTextPaint);
    }

    /**
     * 时间格式
     * @param progress
     * @return
     */
    private String gettime(int progress) {
        return DateTimeUtils.stringForMillisecondTime(progress, true, true);
    }

    private int pressedThumb = NONE_THUMB_PRESSED;

    private boolean isByHand = true;// 移动模式， true:依据左右把手移动 ;false 整体移动

    /**
     * @param _IsByHand 移动模式， true:依据左右把手移动setOnRangSeekBarChangeListener() ;<br/>
     *                  false 整体移动setItemVideo(onRangDurationListener listener)
     */
    public void setMoveMode(boolean _IsByHand) {
        isByHand = _IsByHand;
        if (!isByHand) {
            trim_bg = mResources.getDrawable(R.drawable.trim_line);
        }
        initTopBottom();
    }

    private int ItemDuration = 0;

    public void setItemDuration(int itemDuration) {
        ItemDuration = Math.max(0, itemDuration);
        long lastmin = getSelectedMinValue();
        int tMax = (int) (lastmin + ItemDuration);
        if (tMax <= mDuration) {
            setSeekBarRangeValues(lastmin, tMax);
        } else {
            setSeekBarRangeValues(mDuration - ItemDuration, mDuration);
        }

    }

    private long mx, lastMax;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mx = (int) event.getX();

                if (isByHand) {
                    pressedThumb = evalPressedThumb(mx);
                    // Log.e("pressedThumb", "xxx" + pressedThumb);
                    if (pressedThumb == NONE_THUMB_PRESSED) {
                        if (mShadowType == 1) {
                            if (mLeftRect.right < mx && mx < mRightRect.left) {

                            } else if (mx < mLeftRect.left) {
                                pressedThumb = MIN_THUMB_PRESSED;
                            } else if (mx > mRightRect.right) {
                                pressedThumb = MAX_THUMB_PRESSED;
                            } else {
                                isFocusing = false;
                                return false;
                            }
                        } else if (mShadowType == 2) {
                            if (mLeftRect.right < mx && mx > mRightRect.left) {
                            } else if (mx > mLeftRect.left ) {
                                pressedThumb = MIN_THUMB_PRESSED;
                            } else {
                                isFocusing = false;
                                return false;
                            }
                        }

                    }
                    if (MIN_THUMB_PRESSED == pressedThumb) {
                        if (mCurHandle == HANDLE_LEFT) {
                            setHandle(HANDLE_NONE);
                        } else {
                            setHandle(HANDLE_LEFT);
                        }
                        isMaxMin = true;
                    } else if (MAX_THUMB_PRESSED == pressedThumb) {
                        if (mCurHandle == HANDLE_RIGHT) {
                            setHandle(HANDLE_NONE);
                        } else {
                            setHandle(HANDLE_RIGHT);
                        }
                        isMaxMin = true;
                    } else if (CURRENT_THUMB_PRESSED == pressedThumb) {
                        isFocusing = true;
                        isMaxMin = false;
                        setHandle(HANDLE_NONE);
                    } else {

                    }
                    mRangeSeekBarChangeListener.beginTouch(pressedThumb);
                } else {

                    pressedThumb = evalPressedThumb(mx);
                    // Log.e("pressedThumb", "xxx" + pressedThumb);
                    if (pressedThumb == NONE_THUMB_PRESSED) {
                        if (mLeftRect.right < mx && mx < mRightRect.left) {
                        } else if (mx < mLeftRect.left) {
                            pressedThumb = MIN_THUMB_PRESSED;
                            isMaxMin = true;
                        } else if (mx > mRightRect.right) {
                            pressedThumb = MAX_THUMB_PRESSED;
                            isMaxMin = true;
                        } else {
                            isFocusing = false;
                            return false;
                        }

                    } else if (pressedThumb == CURRENT_THUMB_PRESSED) {
                        isFocusing = true;
                        isMaxMin = false;
                    }

                    lastMax = max;
                    onPress();
                    if (null != mRangDurationListener) {
                        mRangDurationListener.onItemVideoPasue((int) min);
                    }
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (event.getY() > getHeight() || 0 > event.getY()
                        || event.getX() < getLeft() || event.getX() > getRight()) {
                    if (null != mRangDurationListener) {
                        long max = (long) ((mRightRect.left - HANDWIDTH)
                                / getSeekbarWith() * mDuration);
                        if (max <= mDuration) {
                            min = max - ItemDuration;
                            mRangDurationListener.onItemVideoChanged(min, max);
                        }
                    }
                    invalidate();
                    isFocusing = false;
                    isMaxMin = false;
                    return false;
                }
                if (isByHand) {
                    if ((pressedThumb != NONE_THUMB_PRESSED)) {

                        long setValue = (long) ((event.getX() - HANDWIDTH)
                                / getSeekbarWith() * mDuration);

                        if (MIN_THUMB_PRESSED == pressedThumb) {
                            if (mShadowType == 2) {
                                setValue = (long) (event.getX() / getSeekbarWith() * mDuration);
                                if (setValue < 0) {
                                    setValue = 0;
                                }
                            }
                            long mMaxmin = max - minthumbDuration;
                            if (setValue > mMaxmin) {
                                setValue = mMaxmin;
                            }
                            setHandle(HANDLE_LEFT);
                            setMin(setValue);
                            mRangeSeekBarChangeListener.rangeSeekBarValuesChanging(setValue);
                        } else if (MAX_THUMB_PRESSED == pressedThumb) {
                            if (mShadowType == 2) {
                                setValue = (long) ((event.getX() - HANDWIDTH) / getSeekbarWith() * mDuration);
                                if (setValue > getDuration()) {
                                    setValue = getDuration();
                                }
                            }
                            long mMinMax = min + minthumbDuration;
                            if (setValue < mMinMax) {
                                setValue = mMinMax;
                            }
                            setHandle(HANDLE_RIGHT);
                            setMax(setValue);
                            mRangeSeekBarChangeListener.rangeSeekBarValuesChanging(setValue);
                        } else if (CURRENT_THUMB_PRESSED == pressedThumb) {
                            // Log.e("movetrue", min + "---" + max + "--current-"
                            // + setValue);
                            if (mShadowType == 1) {
                                if (min < setValue && setValue < max) {
                                    setProgress(setValue);
                                    mRangeSeekBarChangeListener.rangeSeekBarValuesChanging(setValue);
                                }
                            } else if (mShadowType == 2) {
                                setValue = (long) (event.getX() / getSeekbarWith() * mDuration);
                                if (setValue < min || setValue > max) {
                                    setProgress(setValue);
                                    mRangeSeekBarChangeListener.rangeSeekBarValuesChanging(setValue);
                                }
                            }

                        }

                    }

                } else {

                    onPress();
                    if (null != mRangDurationListener) {

                        if (pressedThumb == CURRENT_THUMB_PRESSED) {
                            long setValue = (long) ((event.getX() - HANDWIDTH)
                                    / getSeekbarWith() * mDuration);
                            if (min < setValue && setValue < max) {
                                setProgress(setValue);
                                mRangDurationListener.onSeekto(setValue);
                            } else if (setValue > max) {
                                setProgress(max);
                                mRangDurationListener.onSeekto(max);
                            }

                        } else {
                            // 偏移的时间量
                            long offPx = ((long) (((event.getX() - mx) - HANDWIDTH)
                                    / getSeekbarWith() * mDuration));

                            long nend = lastMax + offPx;
                            long nstart = nend - ItemDuration;
                            if (nstart < 0) {// 防止把手滑动到最边缘
                                nstart = 0;
                                nend = ItemDuration;
                            } else if (nend > mDuration) {
                                nend = mDuration;
                                nstart = nend - ItemDuration;
                            }
                            setSeekBarRangeValues(nstart, nend);
                            mRangDurationListener.onItemVideoChanging(nstart, nend);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isFocusing = false;
                isMaxMin = false;
                if (isByHand) {
                    if (pressedThumb != NONE_THUMB_PRESSED) {
                        long min = (long) ((mLeftRect.left) / getSeekbarWith() * mDuration);
                        long max = (long) ((mRightRect.left - HANDWIDTH)
                                / getSeekbarWith() * mDuration);
                        long current = (long) ((mProgressRect.left - HANDWIDTH)
                                / getSeekbarWith() * mDuration);

                        mRangeSeekBarChangeListener.rangeSeekBarValuesChanged(min, max, current);
                    }
                } else {
                    if (null != mRangDurationListener) {
                        // long min = (long) ((mLeftRect.left) / getSeekbarWith() *
                        // mDuration);
                        if (pressedThumb != CURRENT_THUMB_PRESSED) {
                            long max = (long) ((mRightRect.left - HANDWIDTH)
                                    / getSeekbarWith() * mDuration);
                            if (max <= mDuration) {
                                min = max - ItemDuration;
                                mRangDurationListener.onItemVideoChanged(min, max);
                            }
                        }
                    }
                }
                invalidate();
                pressedThumb = NONE_THUMB_PRESSED;

                break;

            default:
                break;
        }
        return true;
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX 触摸x轴 The x-coordinate of a touch event in screen space.
     */
    private int evalPressedThumb(float touchX) {
        int result = NONE_THUMB_PRESSED;
        boolean minThumbPressed = isInThumbRange(touchX, mLeftRect);
        boolean maxThumbPressed = isInThumbRange(touchX, mRightRect);
        boolean progressPressed = isInThumbRange(touchX, mProgressRect);

        if (minThumbPressed && maxThumbPressed) {

            result = (touchX / getWidth() > 0.5f) ? MIN_THUMB_PRESSED
                    : MAX_THUMB_PRESSED;
        } else if (minThumbPressed) {
            result = MIN_THUMB_PRESSED;
        } else if (maxThumbPressed) {
            result = MAX_THUMB_PRESSED;
        } else if (progressPressed) {
            result = CURRENT_THUMB_PRESSED;
        } else {
            // min< current && current <max 强制支持seekto (当前位置)
            if (mShadowType == 1) {
                if (mLeftRect.right < touchX && touchX < mRightRect.left) {
                    result = CURRENT_THUMB_PRESSED;// 强制支持seekto(当前位置)
                }
            } else if (mShadowType == 2) {
                if (mLeftRect.right > touchX || touchX > mRightRect.left) {
                    result = CURRENT_THUMB_PRESSED;// 强制支持seekto(当前位置)
                }
            }

        }
        return result;
    }

    private boolean isInThumbRange(float touchX, Rect rect) {

        return touchX > rect.left - HANDWIDTH
                && touchX < rect.right + HANDWIDTH;
    }

    private onRangDurationListener mRangDurationListener;

    /**
     * 定长截取
     *
     * @param listener
     */
    public void setItemVideo(onRangDurationListener listener) {
        mRangDurationListener = listener;
    }

    public static interface onRangDurationListener {

        public void onItemVideoPasue(int ntime);

        public void onItemVideoChanged(long start, long end);

        public void onItemVideoChanging(long start, long end);

        public void onSeekto(long seekto);

    }

    private OnRangeSeekBarChangeListener mRangeSeekBarChangeListener;

    /**
     * 自由截取
     *
     * @param listener
     */
    public void setOnRangSeekBarChangeListener(
            OnRangeSeekBarChangeListener listener) {
        mRangeSeekBarChangeListener = listener;
    }

    public void setHandle(int handleType) {
        if (mCurHandle == handleType) {
            return;
        }
        mCurHandle = handleType;
        if (handleType == HANDLE_LEFT) {
            mLeftOrRight = 1;
            mDrawableRight = mResources.getDrawable(R.drawable.seekbar_hand_right);
            mDrawableLeft = mResources.getDrawable(R.drawable.seekbar_hand_left_selected);
        } else if (handleType == HANDLE_RIGHT) {
            mLeftOrRight = 2;
            mDrawableRight = mResources.getDrawable(R.drawable.seekbar_hand_right_selected);
            mDrawableLeft = mResources.getDrawable(R.drawable.seekbar_hand_left);
        } else {
            mLeftOrRight = 0;
            mDrawableRight = mResources.getDrawable(R.drawable.seekbar_hand_right);
            mDrawableLeft = mResources.getDrawable(R.drawable.seekbar_hand_left);
        }
    }

    /**
     * 按住
     */
    private void onPress() {
        mDrawableLeft = mResources.getDrawable(R.drawable.seekbar_hand_left_selected);
        mDrawableRight = mResources.getDrawable(R.drawable.seekbar_hand_right_selected);
    }

    /**
     * 松开
     */
    private void onUnPress() {
        mDrawableLeft = mResources.getDrawable(R.drawable.seekbar_hand_left);
        mDrawableRight = mResources.getDrawable(R.drawable.seekbar_hand_right);
    }

    /**
     * 左边滑块还是右边的
     */
    private int mLeftOrRight = 0;
    /**
     * 阴影区域   两边 1 中间  2(默认两边)
     */
    private int mShadowType = 1;

    public void setShaowType(int type) {
        this.mShadowType = type;
    }

}
