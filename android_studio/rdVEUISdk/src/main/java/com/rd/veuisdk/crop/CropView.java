package com.rd.veuisdk.crop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.rd.veuisdk.R;

public class CropView extends View {
    private static final String LOGTAG = "CropView";

    private RectF mImageBounds = new RectF();
    private RectF mScreenBounds = new RectF();
    private RectF mScreenImageBounds = new RectF();
    private RectF mScreenCropBounds = new RectF();

    private Paint mPaint = new Paint();
    private Paint mShadowPaint = new Paint();
    private TextPaint mTextPaint = new TextPaint();

    private CropObject mCropObj = null;
    private Drawable[] mCropIndicators;
    private int mIndicatorSize;
    private int mRotation = 0;
    @SuppressWarnings("unused")
    private boolean mMovingBlock = false;
    private Matrix mDisplayMatrix = null;
    private Matrix mDisplayMatrixInverse = null;
    private boolean mDirty = false;

    private float mPrevX = 0;
    private float mPrevY = 0;
    private float mSpotX = 0;
    private float mSpotY = 0;
    private boolean mDoSpot = false;

    private int mMargin = 32;
    private int mOverlayShadowColor = 0xCF000000;
    private int mOverlayWPShadowColor = 0x5F000000;
    private int mWPMarkerColor = 0x7FFFFFFF;
    private int mMinSideSize = 90; // 限制最小边
    private int mTouchTolerance = 40;
    private float mDashOnLength = 20;
    private float mDashOffLength = 10;
    private float mAspectTextSize = 20;
    private String mAspectText = "";
    private boolean isCanMove = true;

    private enum Mode {
        NONE, MOVE
    }

    private Mode mState = Mode.NONE;

    public CropView(Context context) {
        super(context);
        setup(context);
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public CropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context);
    }

    private void setup(Context context) {
        if (isInEditMode()) {
            return;
        }
        Resources rsc = context.getResources();
        // cropIndicators:
        // TOP_LEFT,TOP_RIGHT,BOTTON_LEFT_BOTTON_RIGHT,MOVE_LEFT_RIGHTL,MOVE_TOP_BOTTOM
        mCropIndicators = new Drawable[]{
                rsc.getDrawable(R.drawable.video_crop_top_left),
                rsc.getDrawable(R.drawable.video_crop_top_right),
                rsc.getDrawable(R.drawable.video_crop_bottom_left),
                rsc.getDrawable(R.drawable.video_crop_bottom_right),
                rsc.getDrawable(R.drawable.video_crop_move_left_right),
                rsc.getDrawable(R.drawable.video_crop_move_top_bottom),};

        mIndicatorSize = (int) rsc.getDimension(R.dimen.crop_indicator_size);
        mMargin = (int) rsc.getDimension(R.dimen.preview_margin);
        mMinSideSize = (int) rsc.getDimension(R.dimen.crop_min_side);
        mTouchTolerance = (int) rsc.getDimension(R.dimen.crop_touch_tolerance);
        mOverlayShadowColor = (int) rsc.getColor(R.color.crop_shadow_color);
        mOverlayWPShadowColor = (int) rsc
                .getColor(R.color.crop_shadow_wp_color);
        mWPMarkerColor = (int) rsc.getColor(R.color.crop_wp_markers);
        mDashOnLength = rsc.getDimension(R.dimen.wp_selector_dash_length);
        mDashOffLength = rsc.getDimension(R.dimen.wp_selector_off_length);
        mAspectTextSize = rsc.getDimension(R.dimen.crop_aspect_text_size);
    }

    public void initialize(RectF newCropBounds, RectF newPhotoBounds,
                           int rotation) {
        mImageBounds.set(newPhotoBounds);
        if (mCropObj != null) {
            RectF crop = mCropObj.getInnerBounds();
            RectF containing = mCropObj.getOuterBounds();
            if (crop != newCropBounds || containing != newPhotoBounds
                    || mRotation != rotation) {
                mRotation = rotation;
                mCropObj.resetBoundsTo(newCropBounds, newPhotoBounds);
                clearDisplay();
            }
        } else {
            mRotation = rotation;
            mCropObj = new CropObject(newPhotoBounds, newCropBounds, 0);
            clearDisplay();
        }
    }

    public RectF getCrop() {
        if (mCropObj == null) {
            return null;
        }
        return mCropObj.getInnerBounds();
    }

    public RectF getPhoto() {
        return mCropObj.getOuterBounds();
    }

    private int px = 0, py = 0;
    private final int MAXOFF = 5;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mDisplayMatrix == null || mDisplayMatrixInverse == null) {
            return true;
        }
        float[] touchPoint = {x, y};
        mDisplayMatrixInverse.mapPoints(touchPoint);
        x = touchPoint[0];
        y = touchPoint[1];
        switch (event.getActionMasked()) {
            case (MotionEvent.ACTION_DOWN):

                px = (int) event.getX();
                py = (int) event.getY();

                if (null != icropListener)

                    // 播放视频的时候，是不可以移动的
                    if (!isCanMove) {
                        return false;
                    }
                if (mState == Mode.NONE) {
                    if (!mCropObj.selectEdge(x, y)) {
                        mMovingBlock = mCropObj.selectEdge(CropObject.MOVE_BLOCK);
                    }
                    mPrevX = x;
                    mPrevY = y;
                    mState = Mode.MOVE;
                }
                break;
            case (MotionEvent.ACTION_UP):

                if (null != icropListener) {

                    if ((Math.abs(event.getY() - py) > MAXOFF || Math.abs(event
                            .getX() - px) > MAXOFF)) {
                        mCropObj.selectEdge(CropObject.MOVE_NONE);
                        mMovingBlock = false;
                        mPrevX = x;
                        mPrevY = y;

                    } else {

                        if ((Math.abs(event.getY() - py) < MAXOFF && Math.abs(event
                                .getX() - px) < MAXOFF)) {

                            icropListener.onPlayState();
                        }

                    }

                } else {

                    if (mState == Mode.MOVE) {
                        mCropObj.selectEdge(CropObject.MOVE_NONE);
                        mMovingBlock = false;
                        mPrevX = x;
                        mPrevY = y;

                    }
                }
                mState = Mode.NONE;
                break;
            case (MotionEvent.ACTION_MOVE):
                if (mState == Mode.MOVE) {
                    icropListener.onMove();
                    float dx = x - mPrevX;
                    float dy = y - mPrevY;
                    mCropObj.moveCurrentSelection(dx, dy);
                    mPrevX = x;
                    mPrevY = y;
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    private void reset() {
        Log.w(LOGTAG, "crop reset called");
        mState = Mode.NONE;
        mCropObj = null;
        mRotation = 0;
        mMovingBlock = false;
        clearDisplay();
    }

    private void clearDisplay() {
        mDisplayMatrix = null;
        mDisplayMatrixInverse = null;
        invalidate();
    }

    protected void configChanged() {
        mDirty = true;
    }

    public void applyFreeAspect() {
        mCropObj.unsetAspectRatio();
        invalidate();
    }

    public void applyOriginalAspect() {
        RectF outer = mCropObj.getOuterBounds();
        float w = outer.width();
        float h = outer.height();
        if (w > 0 && h > 0) {
            applyAspect(w, h);
            mCropObj.resetBoundsTo(outer, outer);
        } else {
            Log.w(LOGTAG, "failed to set aspect ratio original");
        }
    }

    public void applyAspectText(String text) {
        mAspectText = text;
        invalidate();
    }

    public void applySquareAspect() {
        applyAspect(1, 1);
    }

    public void applyAspect(float x, float y) {
        if (x <= 0 || y <= 0) {
            throw new IllegalArgumentException("Bad arguments to applyAspect");
        }
        // If we are rotated by 90 degrees from horizontal, swap x and y
        if (((mRotation < 0) ? -mRotation : mRotation) % 180 == 90) {
            float tmp = x;
            x = y;
            y = tmp;
        }
        if (!mCropObj.setInnerAspectRatio(x, y)) {
            Log.w(LOGTAG, "failed to set aspect ratio");
        }
        invalidate();
    }

    public void setWallpaperSpotlight(float spotlightX, float spotlightY) {
        mSpotX = spotlightX;
        mSpotY = spotlightY;
        if (mSpotX > 0 && mSpotY > 0) {
            mDoSpot = true;
        }
    }

    public void unsetWallpaperSpotlight() {
        mDoSpot = false;
    }

    /**
     * Rotates first d bits in integer x to the left some number of times.
     */
    private int bitCycleLeft(int x, int times, int d) {
        int mask = (1 << d) - 1;
        int mout = x & mask;
        times %= d;
        int hi = mout >> (d - times);
        int low = (mout << times) & mask;
        int ret = x & ~mask;
        ret |= low;
        ret |= hi;
        return ret;
    }

    /**
     * Find the selected edge or corner in screen coordinates.
     */
    private int decode(int movingEdges, float rotation) {
        int rot = CropMath.constrainedRotation(rotation);
        switch (rot) {
            case 90:
                return bitCycleLeft(movingEdges, 1, 4);
            case 180:
                return bitCycleLeft(movingEdges, 2, 4);
            case 270:
                return bitCycleLeft(movingEdges, 3, 4);
            default:
                return movingEdges;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            configChanged();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mDirty) {
            mDirty = false;
            clearDisplay();
        }
        // mImageBounds.set(0, 0, canvas.getWidth(), canvas.getHeight());
        mScreenBounds.set(0, 0, canvas.getWidth(), canvas.getHeight());
        mScreenBounds.inset(mMargin, mMargin);

        // If crop object doesn't exist, create it and update it from master
        // state
        if (mCropObj == null) {
            reset();
            mCropObj = new CropObject(mImageBounds, mImageBounds, 0);
        }

        // If display matrix doesn't exist, create it and its dependencies
        if (mDisplayMatrix == null || mDisplayMatrixInverse == null) {
            mDisplayMatrix = new Matrix();
            mDisplayMatrix.reset();
            if (!CropDrawingUtils.setImageToScreenMatrix(mDisplayMatrix,
                    mImageBounds, mScreenBounds, mRotation)) {
                Log.w(LOGTAG, "failed to get screen matrix");
                mDisplayMatrix = null;
                return;
            }
            mDisplayMatrixInverse = new Matrix();
            mDisplayMatrixInverse.reset();
            if (!mDisplayMatrix.invert(mDisplayMatrixInverse)) {
                Log.w(LOGTAG, "could not invert display matrix");
                mDisplayMatrixInverse = null;
                return;
            }
            // Scale min side and tolerance by display matrix scale factor
            mCropObj.setMinInnerSideSize(mDisplayMatrixInverse
                    .mapRadius(mMinSideSize));
            mCropObj.setTouchTolerance(mDisplayMatrixInverse
                    .mapRadius(mTouchTolerance));
        }

        mScreenImageBounds.set(0, 0, canvas.getWidth(), canvas.getHeight());

        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);

        mCropObj.getInnerBounds(mScreenCropBounds);

        if (mDisplayMatrix.mapRect(mScreenCropBounds)) {
            // Draw overlay shadows
            mShadowPaint.setColor(mOverlayShadowColor);
            mShadowPaint.setStyle(Paint.Style.FILL);
            CropDrawingUtils.drawShadows(canvas, mShadowPaint,
                    mScreenCropBounds, mScreenImageBounds);

            // Draw crop rect and markers
            CropDrawingUtils.drawCropRect(canvas, mScreenCropBounds);

            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(Color.WHITE);
            mTextPaint.setTextSize(mAspectTextSize);
            mTextPaint.setShadowLayer(5, 1, 1, Color.BLACK);

            if (mUnableBorder) {

                mTextPaint.setAntiAlias(true);
                mTextPaint.setColor(Color.WHITE);
                mTextPaint.setTextSize(mAspectTextSize);
                mTextPaint.setShadowLayer(5, 1, 1, Color.BLACK);

            } else {
                // 画播放状态按钮
                if (null != tempState && !tempState.isRecycled())
                    canvas.drawBitmap(
                            tempState,
                            mScreenCropBounds.left
                                    + (mScreenCropBounds.width() - tempState
                                    .getWidth()) / 2,
                            mScreenCropBounds.top
                                    + (mScreenCropBounds.height() - tempState
                                    .getHeight()) / 2, null);
            }

            if (!mDoSpot) {
                CropDrawingUtils.drawRuleOfThird(canvas, mScreenCropBounds);
            } else {
                Paint wpPaint = new Paint();
                wpPaint.setColor(mWPMarkerColor);
                wpPaint.setStrokeWidth(3);
                wpPaint.setStyle(Paint.Style.STROKE);
                wpPaint.setPathEffect(new DashPathEffect(new float[]{
                        mDashOnLength, mDashOnLength + mDashOffLength}, 0));
                mShadowPaint.setColor(mOverlayWPShadowColor);
                CropDrawingUtils.drawWallpaperSelectionFrame(canvas,
                        mScreenCropBounds, mSpotX, mSpotY, wpPaint,
                        mShadowPaint);
            }

            CropDrawingUtils.drawIndicators(canvas, mCropIndicators,
                    mIndicatorSize, mScreenCropBounds,
                    mCropObj.isFixedAspect(),
                    decode(mCropObj.getSelectState(), mRotation));
        }
    }

    private Bitmap tempState;

    public void setStatebmp(Bitmap b) {
        if (null != tempState) {
            tempState.recycle();
        }
        tempState = b;
        invalidate();
    }

    public void setCanMove(boolean isCanMove) {
        this.isCanMove = isCanMove;
    }

    private boolean mUnableBorder = true;

    public void setUnAbleBorder() {

        mUnableBorder = false;
    }

    private ICropListener icropListener;

    public void setIcropListener(ICropListener icropListener) {
        this.icropListener = icropListener;
    }

    public interface ICropListener {

        /**
         * 响应触摸。改变play状态
         */
        void onPlayState();

        /**
         * 响应触摸move
         */
        void onMove();
    }

}
