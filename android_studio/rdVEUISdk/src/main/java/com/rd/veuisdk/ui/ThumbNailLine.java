package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import com.rd.vecore.VirtualVideo;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.ui.extrangseekbar.RangSeekBarBase;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.ThumbNailUtils;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 字幕、特效、配音缩略图组件
 * 时间轴 (包含缩略图和字幕时间段信息)
 *
 * @author JIAN
 */
public class ThumbNailLine extends RangSeekBarBase {
    public final String TAG = "ThumbNailLine";
    private int mHeight;// 组件高度
    private boolean isReleased = false;
    private boolean isAudio = false;
    private Rect lastSrc = new Rect();
    private Rect leftRect = new Rect(), rightRect = new Rect();
    private boolean showCurrent = false;
    private Rect tempbg = new Rect();
    private int pressedThumb = RangSeekBarBase.NONE_THUMB_PRESSED;

    private boolean cantouch = false;
    private boolean result = false, canMoveItem = false, misAdding = false;
    /**
     * An invalid pointer id.
     */
    public static final int INVALID_POINTER_ID = 255;

    private float mDownMotionX;
    private int mActivePointerId = INVALID_POINTER_ID;
    //手指
    private int mPointerIndex;

    private boolean isLongClick = false;
    /**
     * 边框线宽度
     */
    private final int BORDER_LINE_WIDTH;
    private Paint mPaint = new Paint(), pCurrent = new Paint(), pText = new Paint(),
            mPbg = new Paint();
    private Bitmap mLeftBmp, mRightBmp;// 当前item的控制把手
    private Bitmap mLeftSelectedBmp, mRightSelectedBmp;
    private int mHeader = 0;
    private int mLast = 0;

    private int lastPx = -1, headerPx = -1;

    /**
     * @param context
     * @param attrs
     */
    public ThumbNailLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ThumbNailLine(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initThread(context);
        mPaint.setColor(getResources().getColor(R.color.main_orange_transparent_66));
        mPaint.setAntiAlias(true);
        pCurrent.setColor(getResources().getColor(R.color.white));
        pCurrent.setStyle(Style.STROKE);
        BORDER_LINE_WIDTH = getResources().getDimensionPixelSize(
                R.dimen.borderline_width2);
        pCurrent.setStrokeWidth(BORDER_LINE_WIDTH);
        pCurrent.setAntiAlias(true);
        pText.setColor(Color.WHITE);
        pText.setAntiAlias(true);
        pText.setTextSize(getResources().getDimensionPixelSize(
                R.dimen.text_size_14));

        mLeftBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.special_hand_left);
        mRightBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.special_hand_right);

        mLeftSelectedBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.special_hand_left_p);
        mRightSelectedBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.special_hand_right_p);
        mPbg.setColor(Color.BLACK);
        mPbg.setAntiAlias(true);
        setCantouch(true);

    }


    public void setIsAudio(boolean isAudio) {
        this.isAudio = isAudio;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mHeight = ThumbNailUtils.THUMB_HEIGHT;
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        // canvas.drawColor(Color.BLACK);

        canvas.drawRect(tempbg, mPbg);

        Map<Integer, ThumbInfo> maps = mMemoryCache.snapshot();
        Set<Entry<Integer, ThumbInfo>> entrySet = maps.entrySet();

        for (Entry<Integer, ThumbInfo> item : entrySet) {
            ThumbInfo temp = item.getValue();

            if (mlefttime <= temp.nTime && temp.nTime <= mrighttime
                    && item.getKey() != lastTime && temp != null
                    && temp.bmp != null && !temp.bmp.isRecycled()) {
                canvas.drawBitmap(temp.bmp, null, temp.rect, null);

            }

        }

        entrySet.clear();
        maps.clear();

        if (lastTime > 0) {
            ThumbInfo temp = mMemoryCache.get(lastTime);
            if (temp != null && temp.bmp != null && !temp.bmp.isRecycled()) {
                canvas.drawBitmap(temp.bmp, lastSrc, temp.rect, null);
            }
        }

        int len = listSubInfos.size();
//        Log.e(TAG, "ondraw-----------------------------------》" + len);
        for (int i = 0; i < len; i++) {
            SubInfo info = listSubInfos.get(i);
//            Log.e(TAG, "ondraw" + i + "...>" + info.getRect().toShortString() + "...." + info.getStart() + "<>" + info.getEnd());
            if (!showCurrent || null == currentSub
                    || currentSub.getId() != info.getId()) {
                canvas.drawRect(info.getRect(), mPaint);
                drawText(info, canvas);
            }

        }
        if (showCurrent && null != currentSub) {

            Rect thecurrent = currentSub.getRect();

            if (null != thecurrent) {

                int half = (int) (BORDER_LINE_WIDTH * 0.5);

                thecurrent = new Rect(thecurrent.left - half, thecurrent.top
                        + half, thecurrent.right + half, thecurrent.bottom
                        - half);
                canvas.drawRect(thecurrent, mPaint);
                if (thecurrent.width() < 30 && !misAdding) {
                    thecurrent.left = thecurrent.left - 15;
                    thecurrent.right = thecurrent.right + 15;
                }
                canvas.drawRect(thecurrent, pCurrent);
                if (canMoveItem) {
                    // 画把手
                    int bw = mLeftBmp.getWidth();
                    int bh = mLeftBmp.getHeight();
                    int bleft = thecurrent.left - bw;
                    int btop = thecurrent.top + (thecurrent.height() - bh) / 2;
                    leftRect.set(bleft - 5, btop - 5, bleft + bw + 5, btop + bh
                            + 5);
                    if (pressedThumb == RangSeekBarBase.MIN_THUMB_PRESSED
                            && result) {
                        canvas.drawBitmap(mLeftSelectedBmp, null, leftRect, null);
                    } else {
                        canvas.drawBitmap(mLeftBmp, null, leftRect, null);
                    }

                    int mw = thecurrent.width();

                    rightRect.set(leftRect.left + mw + bw, leftRect.top,
                            leftRect.right + mw + bw, leftRect.bottom);

                    if (pressedThumb == RangSeekBarBase.MAX_THUMB_PRESSED
                            && result) {
                        canvas.drawBitmap(mRightSelectedBmp, null, rightRect, null);
                    } else {
                        canvas.drawBitmap(mRightBmp, null, rightRect, null);
                    }

                }
            }

            drawText(currentSub, canvas);

        }

    }

    private void drawText(SubInfo info, Canvas canvas) {
        if (!isAudio) {
            pText.setTextSize(getResources().getDimensionPixelSize(
                    R.dimen.text_size_14));
            int strWidth = (int) pText.measureText(info.getStr());
            FontMetrics fm = pText.getFontMetrics();
            int widthPx = info.getEnd() - info.getStart();
            if (strWidth > widthPx) {

                pText.setTextSize(getResources().getDimensionPixelSize(
                        R.dimen.text_size_12));
                strWidth = (int) pText.measureText(info.getStr());

                // //更加item的宽判断该区域范围内两行总共能显示多少个汉字 else 不能完全显示的就...
                String data = info.getStr();
                if (widthPx * 2 < strWidth) {

                    // 获取该区域能显示的字符串

                    int itemWidth = (int) pText.measureText("串");

                    int canShowStrCount = widthPx * 2 / itemWidth;

                    if (canShowStrCount > data.length()) {
                        canShowStrCount = data.length() - 1;
                    }
                    try {
                        data = data.substring(0, canShowStrCount);
                    } catch (Exception e) {
                        e.printStackTrace();
                        data = info.getStr();
                    }
                }

                // 分成两行

                int itemlen = data.length() / 2;

                String str;
                for (int i = 0; i < 2; i++) {

                    if (i == 1) {
                        str = data.substring(i * itemlen, data.length());
                    } else {
                        str = data.substring(i * itemlen, itemlen * (i + 1));
                    }

                    int mleft = info.getStart()
                            + ((info.getEnd() - info.getStart()) - (int) pText
                            .measureText(str)) / 2;
                    int mtop = (int) (0 + (i + 1) * (mHeight) / 3 + Math
                            .abs(fm.ascent) / 2) - (int) Math.abs(fm.descent);

                    canvas.drawText(str, mleft, mtop, pText);
                }

            } else {

                int mleft = info.getStart() + (widthPx - strWidth) / 2;
                int mtop = (int) (0 + (mHeight) / 2 + Math.abs(fm.ascent) / 2)
                        - (int) Math.abs(fm.descent);
                canvas.drawText(info.getStr(), mleft, mtop, pText);
            }
        }

    }

    /**
     * 字幕起始位置记录
     */
    private ArrayList<SubInfo> listSubInfos = new ArrayList<SubInfo>();

    /**
     * 新增单个SubInfo
     *
     * @param start
     * @param end
     * @param str
     * @param id
     */
    public void addRect(int start, int end, String str, int id) {
//        Log.e(TAG, "addRect: " + start + "<>" + end + "--->" + id);
        misAdding = true;
        int startpx = getScrollXByPadding(start);
        int endpx = getScrollXByPadding(end);

        int maxRight = getMaxRightbyself(startpx);
        if (endpx >= maxRight) {
            endpx = maxRight;
        }

        SubInfo info = new SubInfo(startpx, endpx, ThumbNailUtils.THUMB_HEIGHT,
                str, id);
        info.setTimeLine(start, end);
        listSubInfos.add(info);
        currentSub = new SubInfo(info, ThumbNailUtils.THUMB_HEIGHT);
        showCurrent = true;
        invalidate();

    }

    /**
     * 显示当前焦点框
     *
     * @param id
     */
    public void showCurrent(int id) {

        SubInfo info = getItem(getIndex(id));
        currentSub = null;
        if (null != info) {
            currentSub = new SubInfo(info, ThumbNailUtils.THUMB_HEIGHT);
            showCurrent = true;
        }
        invalidate();
    }

    public void setShowCurrentFalse() {

        showCurrent = false;
        currentSub = null;

        invalidate();
    }

    /**
     * 获取左边把手的最小值,最大值
     *
     * @param bgotoleftOrRight 向左，向右
     * @param mright           右边把手的px
     * @param mpx              当前触摸点的px
     * @return
     */

    public int getMinLeftByself(boolean bgotoleftOrRight, int mright, float mpx) {

        SubInfo temp = null, target = null;
        int len = listSubInfos.size();
        for (int i = 0; i < len; i++) {
            temp = listSubInfos.get(i);
            if (null == currentSub || currentSub.getId() != temp.getId()) {

                if (mright >= temp.getStart()) // 获取最靠近右边
                {
                    if (null == target) {
                        target = temp;
                    } else if (target.getEnd() < temp.getEnd()) {
                        target = temp;
                    }

                }

            }
        }
        if (null != target) {
            if (bgotoleftOrRight) {
                return target.getEnd();
            } else {
                return target.getEnd();
            }
        }
        return (getpadding() + getHeaderPx());

    }

    private void initThemePx() {
        headerPx = (int) (TempVideoParams.getInstance().getThemeHeader()
                / (mDuration + 0.0) * params[0]);
        lastPx = (int) (TempVideoParams.getInstance().getThemeLast()
                / (mDuration + 0.0) * params[0]);
    }

    /**
     * 获取片头片尾的像素
     *
     * @return
     */
    private int getHeaderPx() {

        return headerPx;
    }


    private int getLastPx() {

        return lastPx;
    }

    public boolean canAddSub(int nPos, int nDuration, int nWidth, int header,
                             int last) {
        // 计算一个像素需要的时间
        // double onePxTime = nDuration / nWidth;
        double nTimeByOneSec = nDuration / nWidth;
        nTimeByOneSec = nTimeByOneSec * 10;
        if (header >= 0) {
            mHeader = header;
            mLast = last;
            nTimeByOneSec = nTimeByOneSec * 2;
        }

        int nCheak = nPos;

        SubInfo temp = null;
        int len = listSubInfos.size();
        // Log.e("robeein"," len: "+len+" nCheak: "+nCheak+" nDuration: "+nDuration+"    "+listSubInfos.toString());
        boolean canAdd = true;
        if (nCheak <= nDuration
                && nCheak >= (nDuration - ((header < 0) ? mLast
                : nTimeByOneSec))) {
            canAdd = false;
        } else {
            for (int i = 0; i < len; i++) {
                temp = listSubInfos.get(i);
                int nStart = temp.getTimelinefrom();
                // int nEnd = temp.getTimelineTo();getScrollXByPadding(nCheak);
                if (nCheak <= nStart && nCheak >= (nStart - nTimeByOneSec)) {
                    canAdd = false;
                    break;
                }
            }
        }

        return canAdd;

    }

    /**
     * 获取右边把手的最大值
     *
     * @param mleft
     * @return
     */
    private int getMaxRightbyself(int mleft) {

        SubInfo temp = null, target = null;
        int len = listSubInfos.size();
        boolean noCurrent = (null == currentSub);
        for (int i = 0; i < len; i++) {
            temp = listSubInfos.get(i);

            if (noCurrent || currentSub.getId() != temp.getId()) {
                if (mleft < temp.getStart()) {
                    if (null != target) {
                        if (target.getStart() > temp.getStart()) {
                            target = temp;
                        }
                    } else {
                        target = temp;
                    }
                }

            } else {

            }
        }
        if (null != target) {
            return target.getStart() - 10;// getpadding() * 2;
        } else {
            return params[0] - getLastPx() + 10;// getpadding();
        }

    }

    /**
     * 获取当前item，右边把手的最大值(单位：ms)
     *
     * @param mleft
     * @return
     */
    public int getMaxRightbyMs(int mleft) {

        int maxpx = getMaxRightbyself(mleft);

        int maxms = getProgress(maxpx);
        return maxms;
    }

    public int getCurrent() {
        if (null != currentSub && null != currentSub.getRect()) {
            return currentSub.getRect().left;
        }
        return -1;
    }


    /**
     * 是否支持触摸移动
     *
     * @param mCantouch
     */
    public void setCantouch(boolean mCantouch) {

        cantouch = mCantouch;
    }

    /**
     * 控制左右区间是否调整
     *
     * @param canmove
     */
    public void setMoveItem(boolean canmove) {
        canMoveItem = canmove;
    }

    public void setIsAdding(boolean isAdding) {
        misAdding = isAdding;
    }


    /**
     * 处理按下的逻辑
     *
     * @param touchX
     */
    private void onActionDown(float touchX) {
        pressedThumb = RangSeekBarBase.NONE_THUMB_PRESSED;


        if (canMoveItem) {
            pressedThumb = evalPressedThumb(touchX);
            if (pressedThumb != RangSeekBarBase.NONE_THUMB_PRESSED) {// 处理后面的move,up
                result = true;

            } else {

                result = false;
            }
        } else {

            result = false;
        }

        invalidate();


    }


    private Runnable onLongListener = new Runnable() {

        @Override
        public void run() {
            if (!isReleased) {
                isLongClick = true;
            } else {
                isLongClick = false;
            }
            // Log.d(TAG, "onlong....." + isLongClick + "...." + pressedThumb);
        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!cantouch) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isLongClick = false;
                isReleased = false;
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                mPointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(mPointerIndex);
                onActionDown(mDownMotionX);
                postDelayed(onLongListener, 300);

                break;
            case MotionEvent.ACTION_MOVE:
                mPointerIndex = event.findPointerIndex(mActivePointerId);
                int x = (int) event.getX(mPointerIndex);
                int mp = -(int) (x - mDownMotionX);
                if (result) {
                    if (!isLongClick) {
                        if (!isReleased) {
                            if (Math.abs(mp) < 50) {
                                isReleased = false;
                            } else {
                                isReleased = true;
                                removeCallbacks(onLongListener);
                                isLongClick = false;
                                return false;
                            }
                        }
                    }

                    if (isLongClick) {
                        result = onTouching(x);
                        if (result) {
                            udpate();
                            invalidate();
                        }
                    } else {
                        if (isReleased) {
                            HorizontalScrollView hor = (HorizontalScrollView) getParent()
                                    .getParent();
                            hor.smoothScrollBy(mp, 0);
                            return false;
                        }

                    }
                } else {
                    HorizontalScrollView hor = (HorizontalScrollView) getParent()
                            .getParent();
                    hor.smoothScrollBy(mp, 0);
                    return false;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (result) {
                    if (isLongClick) {
                        if (pressedThumb != RangSeekBarBase.NONE_THUMB_PRESSED) {
                            udpate();
                            invalidate();
                        }
                    }

                }
                listener.onTouchUp();
                pressedThumb = RangSeekBarBase.NONE_THUMB_PRESSED;
                result = true;
                break;
            default:
                result = false;
                break;
        }
        return result;

    }

    /**
     * 通过距离，计算出当前宽度的时间
     *
     * @param scrollX 单位px
     * @return 单位ms
     */
    public int getProgress(int scrollX) {
        return (int) (mDuration * ((scrollX - getpadding()) / (params[0] + .0)));
    }

    private boolean onTouching(int eventX) {
        if (currentSub == null) {
            return true;
        }
        if (RangSeekBarBase.MIN_THUMB_PRESSED == pressedThumb) {


            if (eventX < currentSub.getStart())// 左边把手向左
            {
                // 判断区间是否满足最小值
                int offpx = getMinLeftByself(false, currentSub.getEnd(), eventX); // 左边把手的最小值

                if (eventX < offpx) {
                    if (currentSub.getEnd() - currentSub.getStart() < MIN_THUMB) {// 时间满足最小要求
                        eventX = currentSub.getStart();
                    } else {
                        eventX = offpx;
                    }
                }
                int index = getIndex(currentSub.getId());
                SubInfo info = getItem(index);
                if (null != info) {
                    info.setStart(eventX);
                    info.setTimeLine(getProgress(eventX), info.getTimelineTo());
                    listSubInfos.set(index, info);
                    currentSub.setStart(eventX);
                    currentSub.setTimeLine(getProgress(eventX),
                            currentSub.getTimelineTo());
                }

            } else if (eventX > currentSub.getStart()) {// 左边把手向右移动
                // 左边把手的最大值
                int mleftMax = currentSub.getEnd() - MIN_THUMB;
                if (currentSub.getStart() <= mleftMax) {
                    // 缩小到最小值(缩小到最小值后把手不再向右滑动)
                    if (eventX > mleftMax) {
                        eventX = mleftMax;
                    }
                    int index = getIndex(currentSub.getId());
                    SubInfo info = getItem(index);
                    if (null != info) {
                        info.setStart(eventX);
                        info.setTimeLine(getProgress(eventX),
                                info.getTimelineTo());
                        listSubInfos.set(index, info);
                        currentSub.setStart(eventX);
                        currentSub.setTimeLine(getProgress(eventX),
                                currentSub.getTimelineTo());
                    }
                }
            }

            return true;
        } else if (RangSeekBarBase.MAX_THUMB_PRESSED == pressedThumb) {

            if (eventX > currentSub.getEnd()) {// 右边把手往右滑
                // 往右移动的最大值
                int mRightMin = currentSub.getStart();
                int offpx = getMaxRightbyself(mRightMin + 0);
                if (offpx > currentSub.getStart()) {
                    eventX = Math.min(offpx, eventX);
                }

                int index = getIndex(currentSub.getId());
                SubInfo info = getItem(index);
                if (null != info) {
                    info.setEnd(eventX);
                    info.setTimeLine(info.getTimelinefrom(),
                            getProgress(eventX));
                    listSubInfos.set(index, info);
                    currentSub.setEnd(eventX);
                    currentSub.setTimeLine(currentSub.getTimelinefrom(),
                            getProgress(eventX));
                }

            } else if (eventX < currentSub.getEnd()) {// 右边把手往左滑

                int dpx = currentSub.getEnd() - currentSub.getStart();

                if (dpx >= MIN_THUMB) {// 区间大于min 可缩小区间到min

                    int index = getIndex(currentSub.getId());
                    SubInfo info = getItem(index);
                    if (null != info) {
                        int mRightMin = currentSub.getStart() + MIN_THUMB;

                        if (eventX < mRightMin) {
                            eventX = mRightMin;
                        }
                        info.setEnd(eventX);
                        info.setTimeLine(info.getTimelinefrom(),
                                getProgress(eventX));
                        listSubInfos.set(index, info);
                        currentSub.setEnd(eventX);
                        currentSub.setTimeLine(currentSub.getTimelinefrom(),
                                getProgress(eventX));
                    }

                }

            }
            return true;
        }

        return false;
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX 触摸x轴 The x-coordinate of a touch event in screen space.
     */
    private int evalPressedThumb(float touchX) {
        int result = RangSeekBarBase.NONE_THUMB_PRESSED;

        if (null != currentSub) {

            int half = (currentSub.getEnd() + currentSub.getStart()) / 2;
            boolean leftPressed = isInHand(touchX, currentSub.getStart());
            boolean rightPressed = isInHand(touchX, currentSub.getEnd());
            if (leftPressed && rightPressed) {

                if (touchX < half) {

                    result = RangSeekBarBase.MIN_THUMB_PRESSED;

                } else {
                    result = RangSeekBarBase.MAX_THUMB_PRESSED;
                }

            } else if (leftPressed) {
                result = RangSeekBarBase.MIN_THUMB_PRESSED;
            } else if (rightPressed) {
                result = RangSeekBarBase.MAX_THUMB_PRESSED;
            }
        }

        return result;
    }

    private SubInfo currentSub;

    /**
     * 退出正在编辑的状态
     */
    public void clearCurrent() {
//        Log.e(TAG, "clearCurrent: " + ((null != currentSub) ? currentSub.getId() : "null"));
        currentSub = null;
        showCurrent = false;
        invalidate();
    }

    /**
     * 清除所有区域
     */
    public void clearAll() {
//        Log.e(TAG, "clearAll: ");
        listSubInfos.clear();
        clearCurrent();
    }

    public ArrayList<SubInfo> getData() {
        ArrayList<SubInfo> mlist = new ArrayList<SubInfo>();

        SubInfo temp, item;
        for (int i = 0; i < listSubInfos.size(); i++) {
            temp = listSubInfos.get(i);
            item = new SubInfo(getProgress(temp.getStart()),
                    getProgress(temp.getEnd()), temp.getId());
            item.setStart(temp.getStart());
            item.setEnd(temp.getEnd());
            item.setStr(temp.getStr());
            mlist.add(item);
        }
        return mlist;
    }

    /**
     * 还原旧的集合数据
     *
     * @param mlist
     */
    public void prepareData(ArrayList<SubInfo> mlist) {
//        Log.e(TAG, "prepareData: " + mlist.size() + "...." + listSubInfos.size());
        listSubInfos.clear();
        for (SubInfo subInfo : mlist) {
            if (subInfo.getTimelineTo() != 0) {
                int startpx = getScrollXByPadding(subInfo.getTimelinefrom());
                int endpx = getScrollXByPadding(subInfo.getTimelineTo());
                subInfo.getRect().set(startpx, 0, endpx,
                        ThumbNailUtils.THUMB_HEIGHT);
            }
            listSubInfos.add(subInfo);
        }
        invalidate();
    }

    /**
     * 当前正在编辑的区域
     *
     * @return 单位ms[]
     */
    public int[] getCurrent(int wid) {
        try {
            if (null != currentSub) {
                return new int[]{getProgress(currentSub.getStart()),
                        getProgress(currentSub.getEnd())};
            } else {
                Rect rect = getItem(getIndex(wid)).getRect();
                return new int[]{getProgress(rect.left),
                        getProgress(rect.right)};

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前编辑位置的px
     *
     * @param wid
     * @return
     */
    public int[] getCurrentPx(int wid) {
        try {
            if (null != currentSub) {
                return new int[]{currentSub.getStart(), currentSub.getEnd()};
            } else {
                Rect rect = getItem(getIndex(wid)).getRect();
                return new int[]{rect.left, rect.right};

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 替换Id时间段的文本(仅针对字幕)
     *
     * @param id
     * @param str
     */
    public void replace(int id, String str) {
//        Log.e(TAG, "replace: " + id + ".....>" + str);
        int index = getIndex(id);
        if (index > -1 && index <= listSubInfos.size() - 1) {
            SubInfo info = listSubInfos.get(index);
            info.setStr(str);
            listSubInfos.set(index, info);
        }
        if (null != currentSub) {
            currentSub.setStr(str);
        }
        invalidate();
    }

    public void replace(int id, int startms, int endms) {
//        Log.e(TAG, "replace: " + id + ".....>" + startms + "<>" + endms);
        int index = getIndex(id);
        int pstart = getScrollXByPadding(startms), pend = getScrollXByPadding(endms);
        if (index > -1 && index <= listSubInfos.size() - 1) {
            SubInfo info = listSubInfos.get(index);
            info.setStart(pstart);
            info.setEnd(pend);
            info.setTimeLine(startms, endms);
            listSubInfos.set(index, info);
        }
        if (null != currentSub) {
            currentSub.setStart(pstart);
            currentSub.setEnd(pend);
            currentSub.setTimeLine(startms, endms);
        }
        invalidate();
    }

    /**
     * 实时更新区域
     *
     * @param id
     * @param start 单位ms
     * @param end
     */
    public void update(int id, int start, int end) {
//        Log.e(TAG, "update: " + id + "  start" + start + "<>" + end + "------------>" + listSubInfos.size());
        int index = getIndex(id);
        int startp = getScrollXByPadding(start);
        int endp = getScrollXByPadding(end);
        if (index > -1 && index <= listSubInfos.size() - 1) {
            SubInfo info = listSubInfos.get(index);
            info.setStart(startp);
            info.setEnd(endp);
            info.setTimeLine(start, end);
            listSubInfos.set(index, info);
        }
        if (null != currentSub) {
            currentSub.setStart(startp);
            currentSub.setEnd(endp);
            currentSub.setTimeLine(start, end);
        }
        invalidate();

    }


    public boolean checkCanAdd(int cpx) {

        cpx = cpx + getpadding();
        int len = listSubInfos.size();
        SubInfo sinfo, target = null;

        if (len == 0) {
            return true;
        }
        ArrayList<SubInfo> mleft = new ArrayList<SubInfo>(), mright = new ArrayList<SubInfo>();
        for (int i = 0; i < len; i++) {
            sinfo = listSubInfos.get(i);

            if (sinfo.getEnd() <= cpx) {
                mleft.add(sinfo);
            } else {
                mright.add(sinfo);
            }

        }

        // 左边最大值

        int mleftMax = 0;
        len = mleft.size();
        for (int i = 0; i < len; i++) {
            sinfo = mleft.get(i);
            if (null != target) {
                mleftMax = ((mleftMax < sinfo.getEnd()) ? sinfo.getEnd()
                        : mleftMax);
            } else {
                mleftMax = sinfo.getEnd();
            }

        }
        int mrightMin = mleftMax;
        len = mright.size();
        target = null;
        for (int i = 0; i < len; i++) {
            sinfo = mright.get(i);
            if (null != target) {

                mrightMin = (mrightMin < sinfo.getStart()) ? sinfo.getStart()
                        : mrightMin;

            } else {
                mrightMin = sinfo.getStart();
            }

        }
        int moff = mrightMin - cpx;
        if (moff > 0 && moff < MIN_THUMB) {
            return false;
        }

        return true;
    }

    /**
     * 获取索引
     *
     * @param id
     * @return
     */
    private synchronized int getIndex(int id) {
        int index = -1, len = listSubInfos.size();
        SubInfo temp;
        for (int i = 0; i < len; i++) {
            temp = listSubInfos.get(i);
            if (id == temp.getId()) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 切换被选中的项(重新编辑时间段)
     *
     * @param id
     */
    public void editSub(int id) {
        SubInfo src = getItem(getIndex(id));
        if (null != src) {
            currentSub = new SubInfo(src, ThumbNailUtils.THUMB_HEIGHT);
            if (null != currentSub) {
                showCurrent = true;
                invalidate();
            }
        } else {
            Log.e(TAG, "editSub: 未找到原subInfo");
        }
    }

    /**
     * 获取时间段的信息
     *
     * @param index
     * @return
     */
    private SubInfo getItem(int index) {

//        Log.e(TAG, "getItem: " + listSubInfos.size() + "...." + index);

        return (index >= 0 && index <= listSubInfos.size() - 1) ? listSubInfos
                .get(index) : null;
    }

    /**
     * 移除指定的项
     *
     * @param id
     */
    public void removeById(int id) {

//        Log.e(TAG, "removeById: " + id + "......>size:" + listSubInfos.size());
        int index = getIndex(id);
        if (-1 < index && index <= (listSubInfos.size() - 1)) {
            SubInfo sb = listSubInfos.remove(index);
            if (null != sb && null != currentSub
                    && sb.getId() == currentSub.getId()) {
                clearCurrent();
            }
        }
        invalidate();

    }

    private int mDuration;//单位：毫秒

    /**
     * 回调区域范围
     */
    private void udpate() {

        if (null != currentSub) {
            listener.updateThumb(currentSub.getId(),
                    getProgress(currentSub.getStart()),
                    getProgress(currentSub.getEnd()));

        }

    }

    private IThumbLineListener listener;

    public void setSubtitleThumbNailListener(IThumbLineListener _listener) {
        listener = _listener;
    }

    private VirtualVideo mVirtualVideo;

    private int halfParentWidth;

    private int[] params = new int[2];
    private int lastTime = -1;

    /**
     * 设置虚拟视频
     *
     * @param virtualVideo
     */
    public void setVirtualVideo(VirtualVideo virtualVideo) {
        mVirtualVideo = virtualVideo;
    }

    /**
     * 准备UI
     *
     * @param duration
     * @param halfpWidth
     * @return
     */
    public int[] setDuration(int duration, int halfpWidth) {
        halfParentWidth = halfpWidth;
        mDuration = duration;
        maxCount = Math.max(1, Math.min(30, (int) (mDuration / 2000.0)));
        itemTime = mDuration / maxCount;
        params[0] = ThumbNailUtils.THUMB_WIDTH * maxCount;
        params[1] = ThumbNailUtils.THUMB_HEIGHT;
        int re = mDuration % itemTime;
        if (re > 0) {
            // 画半块
            lastTime = (mDuration - (int) (itemTime * 0.3));
            params[0] = params[0] + ThumbNailUtils.THUMB_WIDTH / 2;

        }

        tempbg.set(getpadding(), 0, params[0] + getpadding(),
                ThumbNailUtils.THUMB_HEIGHT);
        initThemePx();
        isEditorPrepared = true;
        return params;
    }

    private final int THUMB_ITEM = 6;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case THUMB_ITEM:
                    mLastRefleshTime = System.currentTimeMillis();
                    invalidate();
                    break;

                default:
                    break;
            }

        }

        ;
    };

    /**
     * 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
     */
    private LruCache<Integer, ThumbInfo> mMemoryCache;

    /**
     * 异步加载图片
     *
     * @param context
     */
    private void initThread(Context context) {
        // 获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 64;
        // Log.d(TAG, "mCacheSize...." + mCacheSize);
        // 给LruCache分配1/8 4M
        mMemoryCache = new LruCache<Integer, ThumbInfo>(mCacheSize) {

            // 必须重写此方法，来测量Bitmap的大小
            @Override
            protected int sizeOf(Integer key, ThumbInfo value) {

                if (null != value && null != value.bmp
                        && !value.bmp.isRecycled()) {
                    return value.bmp.getByteCount();
                }

                return 0;
            }

            @Override
            protected void entryRemoved(boolean evicted, Integer key,
                                        ThumbInfo oldValue, ThumbInfo newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                if (null != oldValue) {
                    oldValue.recycle();
                }

            }

        };

    }

    /**
     * 下载Image的线程池
     */
    private ExecutorService mImageThreadPool = null;

    /**
     * 获取线程池的方法，因为涉及到并发的问题，我们加上同步锁
     *
     * @return
     */
    private ExecutorService getThreadPool() {
        if (mImageThreadPool == null) {
            synchronized (ExecutorService.class) {
                if (mImageThreadPool == null) {
                    // 为了下载图片更加的流畅，我们用了2个线程来加载图片
                    mImageThreadPool = Executors.newFixedThreadPool(4);
                }
            }
        }

        return mImageThreadPool;

    }

    /**
     * 添加Bitmap到内存缓存
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(Integer key, Rect rect, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            ThumbInfo info = new ThumbInfo(key, rect, bitmap);
            info.isloading = false;
            mMemoryCache.put(key, info);
        }
    }

    /**
     * 从内存缓存中获取一个Bitmap
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemCache(Integer key) {
        ThumbInfo info = mMemoryCache.get(key);
        return (null != info) ? info.bmp : null;
    }

    /**
     * 取单个thumb
     *
     * @author JIAN
     */
    private class ThumbInfo {
        int nTime;// 图片时刻
        Rect rect;
        Bitmap bmp;
        boolean isloading = false;

        public ThumbInfo(int Time, Rect rect, Bitmap bmp) {
            this.nTime = Time;
            this.rect = rect;
            this.bmp = bmp;
        }

        @Override
        public String toString() {
            return "ThumbInfo [nTime=" + nTime + ", rect=" + rect + ", bmp="
                    + ((null != bmp) ? bmp.getByteCount() : "null")
                    + ", isloading=" + isloading + "]";
        }

        public void recycle() {

            if (null != bmp) {
                if (!bmp.isRecycled()) {
                    bmp.recycle();
                }
                bmp = null;
            }

        }

    }

    private int maxCount = 40;

    private int itemTime = 100;

    private int visibleCount = 10; // 屏幕区域内左边第一个可见的缩略图的时刻

    public void prepare(int visibleWidth) {
        visibleCount = (int) Math.ceil(((visibleWidth) + .0)
                / ThumbNailUtils.THUMB_WIDTH) + 2;// 可见的所率图个数;
    }

    private int leftCount = 10;

    /**
     * 开始加载图片
     *
     * @param scrollX 已向左偏移的像素
     */
    public void setStartThumb(int scrollX) {
        long tempflesh = System.currentTimeMillis();
        leftCount = (int) Math.ceil((scrollX - halfParentWidth + .0)
                / ThumbNailUtils.THUMB_WIDTH) - 2;// 已滑动到左边的个数

        mlefttime = (leftCount) * itemTime;
        mrighttime = (leftCount + visibleCount) * itemTime;
        if (mLastRefleshTime == 0 || tempflesh - mLastRefleshTime > 150) { // 减少刷新频率
            Rect temp;
            if (lastTime > 0) {
                ThumbInfo info = mMemoryCache.get(lastTime);
                if (null == info || info.isloading == false) {
                    int half = ThumbNailUtils.THUMB_WIDTH / 2;
                    int left = params[0] - half + getpadding();
                    temp = new Rect(left, 0, left + half,
                            ThumbNailUtils.THUMB_HEIGHT);
                    lastSrc.set(0, 0, half, ThumbNailUtils.THUMB_HEIGHT);
                    downloadImage(lastTime, temp);
                }

            }
            int nLeft = getpadding() + leftCount * ThumbNailUtils.THUMB_WIDTH;// 时间线左边的偏移
            for (int i = 0; i < visibleCount; i++) {

                int key = (int) ((leftCount + i + 0.5) * itemTime);
                if (0 < key && key <= mDuration) {
                    ThumbInfo info = mMemoryCache.get(key);
                    if (null == info || null == info.bmp
                            || info.isloading == false) {
                        int mleft = nLeft + i * ThumbNailUtils.THUMB_WIDTH;
                        if (mleft <= params[0]) { // 防止超过视频边界
                            temp = new Rect(mleft, 0, mleft
                                    + ThumbNailUtils.THUMB_WIDTH,
                                    ThumbNailUtils.THUMB_HEIGHT);
                            downloadImage(key, temp);
                        }
                    }
                }

            }

            mLastRefleshTime = tempflesh;
            invalidate();
        }

    }

    private long mLastRefleshTime = 0;
    private int mlefttime = 0, mrighttime = mDuration;
    private boolean isEditorPrepared = false;

    private void downloadImage(final int nTime, final Rect rect) {

        Bitmap bitmap = getBitmapFromMemCache(nTime);

        boolean hasBmp = (bitmap != null && !bitmap.isRecycled());
        // Log.e("downloadImage", nTime + "--" + isEditorPrepared + "------"
        // + hasBmp);
        if (hasBmp) {
            mHandler.sendEmptyMessage(THUMB_ITEM);
        } else {

            if (isEditorPrepared && (!hasBmp)) {
                ThumbInfo info = new ThumbInfo(nTime, rect, null);
                info.isloading = true;
                mMemoryCache.put(nTime, info);
                getThreadPool().execute(new Runnable() {

                    @Override
                    public void run() {
                        if (mlefttime <= nTime && nTime <= mrighttime
                                || nTime == lastTime) {
                            Bitmap bitmap = Bitmap.createBitmap(
                                    ThumbNailUtils.THUMB_WIDTH,
                                    ThumbNailUtils.THUMB_HEIGHT,
                                    Config.ARGB_8888);

                            if (mVirtualVideo != null && mVirtualVideo.getSnapshot(getContext(), Utils.ms2s(nTime), bitmap)) {
                                // 将Bitmap 加入内存缓存
                                addBitmapToMemoryCache(nTime, rect, bitmap);
                                mHandler.sendEmptyMessage(THUMB_ITEM);

                            } else {

                                ThumbInfo info = new ThumbInfo(nTime, rect,
                                        null);
                                info.isloading = false;
                                mMemoryCache.put(nTime, info);
                                bitmap.recycle();
                            }
                        } else {
                            ThumbInfo info = new ThumbInfo(nTime, rect, null);
                            info.isloading = false;
                            mMemoryCache.put(nTime, info);
                        }

                    }
                });
            }
        }

    }

    /**
     * 释放资源
     */
    public void recycle() {
        recycle(false);
    }

    /**
     * 释放资源
     *
     * @param includeSnapshotEditor 是否包括获取缩略图的editor
     */
    public void recycle(boolean includeSnapshotEditor) {
        isEditorPrepared = false;
        mLastRefleshTime = 0;
        if (includeSnapshotEditor) {
            mVirtualVideo = null;
            if (null != mLeftBmp && !mLeftBmp.isRecycled()) {
                mLeftBmp.recycle();
            }
            if (null != mRightBmp && !mRightBmp.isRecycled()) {
                mRightBmp.recycle();
            }
            if (null != mLeftSelectedBmp && !mLeftSelectedBmp.isRecycled()) {
                mLeftSelectedBmp.recycle();
            }
            if (null != mRightSelectedBmp && !mRightSelectedBmp.isRecycled()) {
                mRightSelectedBmp.recycle();
            }

        }
        mMemoryCache.evictAll();
        mlefttime = 0;
        mrighttime = 0;

        File f = new File(PathUtils.getDownLoadDirName());
        File[] fs = f.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {

                if (filename.endsWith(".png")) {
                    return true;
                }
                return false;
            }
        });
        if (null != fs && fs.length > 0) {
            for (int i = 0; i < fs.length; i++) {
                fs[i].delete();
            }
        }

    }

    private int getScrollX(long progress) {
        return (int) (getThumbWidth() * progress / mDuration);
    }

    private int getScrollXByPadding(long progress) {
        return getScrollX(progress) + getpadding();
    }

    public double getThumbWidth() {
        return (params[0] + 0.0);
    }

}
