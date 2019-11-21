package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;

import java.util.ArrayList;

/**
 * 字幕选择Shader
 */
public class ColorPicker extends View {
    private final int PADDING = 20;
    private Paint mPointPaint = new Paint(), mStokePaint = new Paint();
    protected int[] colorArr;
    protected boolean mChangleLastStoke = false;
    private boolean mIsLandscape = false;
    private int mRadius = 0;
    private Bitmap mBmpNoColor, mBmpNoColorChecked;
    private float mDensity;
    private int columnNum = 0;

    private ArrayList<Location> mLocationList = new ArrayList<ColorPicker.Location>();


    private int mWidth, mHeight;

    /**
     * 是否用圆形图标
     */
    private boolean mDrawCircle = true;
    /**
     * 是否为空心图标
     */
    private boolean mDrawStrokeOnly = false;

    private boolean mTextEdit = false;

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPointPaint.setAntiAlias(true);
        mStokePaint.setAntiAlias(true);
        mStokePaint.setColor(Color.BLACK);
        mStokePaint.setStyle(Paint.Style.STROKE);
        mStokePaint.setStrokeWidth(mStokeWidth - 4);
        mDensity = CoreUtils.getPixelDensity();

        mBmpNoColor = BitmapFactory.decodeResource(context.getResources(), R.drawable.subtitle_effect_no_color_n);
        mBmpNoColorChecked = BitmapFactory.decodeResource(context.getResources(), R.drawable.subtitle_effect_no_color_p);
        TypedArray tA = context.obtainStyledAttributes(attrs,
                R.styleable.extColorPicker);
        mTextEdit = tA.getBoolean(R.styleable.extColorPicker_isTextEdit, false);
        if (mTextEdit) {
            colorArr = new int[]{Color.parseColor("#484848"), Color.parseColor("#FFFFFF"),
                    Color.parseColor("#e8ce6b"), Color.parseColor("#f9b73c"),
                    Color.parseColor("#e3573b"), Color.parseColor("#be213b"),
                    Color.parseColor("#00ffff"), Color.parseColor("#5da9cf"),
                    Color.parseColor("#0695b5"), Color.parseColor("#2791db"),
                    Color.parseColor("#3564b7"), Color.parseColor("#e9c930"),
                    Color.parseColor("#a6b45c"), Color.parseColor("#87a522"),
                    Color.parseColor("#32b16c"), Color.parseColor("#017e54"),
                    Color.parseColor("#fdbacc"), Color.parseColor("#ff5a85"),
                    Color.parseColor("#ca4f9b"), Color.parseColor("#71369a"),
                    Color.parseColor("#6720d4"), Color.parseColor("#164c6e"),
                    Color.parseColor("#9f9f9f"), Color.parseColor("#000000"),};
        } else {
            colorArr = new int[]{Color.parseColor("#00000000"), Color.parseColor("#FFFFFF"),
                    Color.parseColor("#e8ce6b"), Color.parseColor("#f9b73c"),
                    Color.parseColor("#e3573b"), Color.parseColor("#be213b"),
                    Color.parseColor("#00ffff"), Color.parseColor("#5da9cf"),
                    Color.parseColor("#0695b5"), Color.parseColor("#2791db"),
                    Color.parseColor("#3564b7"), Color.parseColor("#e9c930"),
                    Color.parseColor("#a6b45c"), Color.parseColor("#87a522"),
                    Color.parseColor("#32b16c"), Color.parseColor("#017e54"),
                    Color.parseColor("#fdbacc"), Color.parseColor("#ff5a85"),
                    Color.parseColor("#ca4f9b"), Color.parseColor("#71369a"),
                    Color.parseColor("#6720d4"), Color.parseColor("#164c6e"),
                    Color.parseColor("#9f9f9f"), Color.parseColor("#484848"),};
        }


        mDrawCircle = tA.getBoolean(R.styleable.extColorPicker_isDrawCircle, true);
        mDrawStrokeOnly = tA.getBoolean(R.styleable.extColorPicker_isDrawStrokeOnly, false);
        tA.recycle();

    }

    public void setColorArr(int[] colorArr) {
        this.colorArr = colorArr;
    }

    public void setColumnNum(int num) {
        columnNum = num;
    }

    public void setLandscape(boolean island) {
        mIsLandscape = island;
    }


    private class Location {
        int px;
        int py;

        Location(int x, int y) {
            this.px = x;
            this.py = y;
        }
    }


    private void initLocation() {
        mLocationList.clear();
        mWidth = getWidth();
        mHeight = getHeight();
        int mwidth = mWidth - 2 * PADDING;
        int itemwidth, itemheight;
        int colNum, rowNum;

        if (mIsLandscape) {
            colNum = 12;
            rowNum = colorArr.length / colNum;
            if (colorArr.length % colNum != 0) {
                rowNum += 1;
            }
            if (columnNum != 0) {
                colNum = columnNum;
                rowNum = colorArr.length / columnNum + 1;
            }
            itemwidth = mwidth / colNum;
            itemheight = (mHeight - rowNum * PADDING) / rowNum;
            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < colNum; j++) {
                    int centerx;
                    if (i % rowNum == 1) {
                        centerx = (int) (itemwidth * (j + 0.75)) + PADDING;

                    } else {
                        centerx = (int) (itemwidth * (j + 0.25)) + PADDING;
                    }
                    int y = (int) (itemheight * (i + 0.5)) + PADDING;
                    mLocationList.add(new Location(centerx, y));
                }
            }
        } else {
            if (columnNum != 0) {
                colNum = columnNum;
            } else {
                colNum = 8;
            }
            rowNum = colorArr.length / colNum;
            if (colorArr.length % colNum != 0) {
                rowNum += 1;
            }
            itemwidth = mwidth / colNum;
            itemheight = (mHeight - (rowNum - 1) * PADDING) / rowNum;
            for (int i = 0; i < rowNum; i++) {
                for (int j = 0; j < colNum; j++) {
                    int centerx;
//                    if (i % 2 == 1) {
//                        centerx = (int) (itemwidth * (j + 0.75)) + PADDING;
//                    } else {
                    centerx = (int) (itemwidth * (j + 0.5)) + PADDING;
//                    }
                    int y = (int) (itemheight * (i + 0.5)) + PADDING;
                    mLocationList.add(new Location(centerx, y));
                }
            }
        }
        mRadius = Math.min(itemwidth, itemheight);
        mRadius = mRadius / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initLocation();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        int mwidth = getWidth() - getPaddingLeft() - getPaddingRight();

        int itemwidth = mwidth / 6 / 2;
        int mRadius = itemwidth / 2;
        int index = 0;
        Location item;

        if (mDrawStrokeOnly) {
            mStokePaint.setStyle(Paint.Style.STROKE);
            mPointPaint.setStyle(Paint.Style.STROKE);
            mPointPaint.setStrokeWidth(CoreUtils.dpToPixel(2));
        }
        int colNum, rowNum;
        // canvas.drawRect(r, paint)
        if (mIsLandscape) {
            mRadius = itemwidth / 4;
            colNum = 12;
            rowNum = colorArr.length / colNum;
            if (colorArr.length % colNum != 0) {
                rowNum += 1;
            }
            if (columnNum != 0) {
                colNum = columnNum;
                rowNum = colorArr.length / columnNum + 1;
            }

            if (mDrawCircle) {
                for (int i = 0; i < rowNum; i++) {

                    for (int j = 0; j < colNum; j++) {
                        index = j + (i * colNum);
                        item = mLocationList.get(index);
                        mPointPaint.setColor(colorArr[index]);
                        if (mCheckedId == index) { // 画边框
                            canvas.drawCircle(item.px, item.py, mRadius
                                    + mStokeWidth, mPointPaint);
                            checkChangeStoke();
                            canvas.drawCircle(item.px, item.py, mRadius, mStokePaint);

                        } else {
                            canvas.drawCircle(item.px, item.py, mRadius, mPointPaint);
                        }

                    }
                }
            } else {
                RectF rect;
                for (int i = 0; i < rowNum; i++) {
                    for (int j = 0; j < colNum; j++) {
                        index = j + (i * colNum);
                        item = mLocationList.get(index);
                        mPointPaint.setColor(colorArr[index]);
                        rect = new RectF();
                        if (mCheckedId == index) { // 画边框
                            int r = mRadius + mStokeWidth;

                            rect.set(item.px - r, item.py - r, item.px + r,
                                    item.py + r);
                            canvas.drawRoundRect(rect, 4, 4, mPointPaint);

                            RectF rectStoke = new RectF();

                            rectStoke.set(item.px - mRadius, item.py - mRadius,
                                    item.px + mRadius, item.py + mRadius);
                            checkChangeStoke();
                            canvas.drawRoundRect(rectStoke, 4, 4, mStokePaint);

                        } else {
                            int ml = item.px - mRadius;
                            int mt = item.py - mRadius;
                            rect.set(ml, mt, ml + 2 * mRadius, mt + 2 * mRadius);
                            canvas.drawRoundRect(rect, 4, 4, mPointPaint);
                        }

                    }
                }
            }
        } else {
            if (columnNum != 0) {
                colNum = columnNum;
            } else {
                colNum = 8;
            }
            rowNum = colorArr.length / colNum;
            if (colorArr.length % colNum != 0) {
                rowNum += 1;
            }
            if (mDrawCircle) {
                for (int i = 0; i < rowNum; i++) {
                    for (int j = 0; j < colNum; j++) {
                        index = j + (i * colNum);
                        if (index >= colorArr.length) {
                            break;
                        }
                        item = mLocationList.get(index);
                        mPointPaint.setColor(colorArr[index]);
                        int py = item.py;
                        if (mDensity < 2.01) {
                            if (i == 0) {
                                py -= 8;
                            }
                            if (i == 2) {
                                py += 8;
                            }
                        }
                        if (i == 0 && j == 0 && !mTextEdit) {
                            if (mCheckedId == index) { // 画边框
                                canvas.drawBitmap(mBmpNoColorChecked, null,
                                        new Rect(item.px - mRadius - mStokeWidth, py - mRadius - mStokeWidth,
                                                item.px + mRadius + mStokeWidth, py + mRadius + mStokeWidth),
                                        new Paint(Paint.ANTI_ALIAS_FLAG));
                            } else {
                                canvas.drawBitmap(mBmpNoColor, null,
                                        new Rect(item.px - mRadius, py - mRadius, item.px + mRadius, py + mRadius),
                                        new Paint(Paint.ANTI_ALIAS_FLAG));
                            }
                        } else {
                            if (mCheckedId == index) { // 画边框
                                canvas.drawCircle(item.px, py, mRadius
                                        + mStokeWidth, mPointPaint);
                                checkChangeStoke();
                                canvas.drawCircle(item.px, py, mRadius, mStokePaint);

                            } else {
                                canvas.drawCircle(item.px, py, mRadius, mPointPaint);
                            }
                        }
                    }
                }
            } else {
                RectF rect;
                for (int i = 0; i < rowNum; i++) {
                    for (int j = 0; j < colNum; j++) {
                        index = j + (i * colNum);
                        item = mLocationList.get(index);
                        if (index >= colorArr.length) {
                            break;
                        }
                        mPointPaint.setColor(colorArr[index]);
                        rect = new RectF();
                        if (mCheckedId == index) { // 画边框
                            int r = mRadius + mStokeWidth;

                            rect.set(item.px - r, item.py - r, item.px + r,
                                    item.py + r);
                            canvas.drawRoundRect(rect, 4, 4, mPointPaint);

                            RectF rectStoke = new RectF();

                            rectStoke.set(item.px - mRadius, item.py - mRadius,
                                    item.px + mRadius, item.py + mRadius);
                            checkChangeStoke();
                            canvas.drawRoundRect(rectStoke, 4, 4, mStokePaint);

                        } else {
                            int ml = item.px - mRadius;
                            int mt = item.py - mRadius;
                            rect.set(ml, mt, ml + 2 * mRadius, mt + 2 * mRadius);
                            canvas.drawRoundRect(rect, 4, 4, mPointPaint);
                        }

                    }
                }
            }
        }

    }

    private void checkChangeStoke() {
        if (mChangleLastStoke && mCheckedId == (colorArr.length - 1)) {
            mStokePaint.setColor(Color.WHITE);
        } else {
            mStokePaint.setColor(Color.BLACK);
        }
    }

    /**
     * 还原到初始状态，一个都不选中
     */
    public void ToReset() {
        setCheckId(0);
    }

    public void clearChecked() {
        setCheckId(-1);
    }

    private int mCheckedId = 0;
    private final int mStokeWidth = 8;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onCheckId((int) event.getX(), (int) event.getY());
                break;

            default:
                break;
        }
        return false;
    }

    private void onCheckId(int x, int y) {

        int len = mLocationList.size();
        Location temp;
        for (int i = 0; i < len; i++) {
            temp = mLocationList.get(i);
            if (x > temp.px - mRadius - mStokeWidth
                    && x < temp.px + mRadius + mStokeWidth
                    && y > temp.py - mRadius - mStokeWidth
                    && y < temp.py + mRadius + mStokeWidth) {

                if (i != mCheckedId) {
                    mCheckedId = i;
                    invalidate();
                    if (null != mColorListener && mCheckedId < len) {
                        mColorListener.getColor(colorArr[mCheckedId], mCheckedId);
                    }
                }

                break;
            }

        }

    }

    public int getCheckColor(int ncheckedId) {
        setCheckId(ncheckedId);
        return colorArr[mCheckedId];

    }

    public void setCheckId(int position) {
        mCheckedId = position;
        invalidate();
    }

    public void checkColor(int checkColor) {
        for (int i = 0; i < colorArr.length; i++) {
            if (colorArr[i] == checkColor) {
                setCheckId(i);
                break;
            }
        }
    }

    /**
     * 被选中的颜色
     *
     * @return
     */
    public int getColor() {
        return colorArr[mCheckedId];
    }

    public void setDrawCircle(boolean isDrawCircle) {
        mDrawCircle = isDrawCircle;
    }

    public void setDrawStrokeOnly(boolean isDrawStrokeOnly) {
        mDrawStrokeOnly = isDrawStrokeOnly;
    }

    private IColorListener mColorListener;

    public void setColorListener(IColorListener _listener) {

        mColorListener = _listener;
    }

    public interface IColorListener {

        void getColor(int color, int position);
    }

}
