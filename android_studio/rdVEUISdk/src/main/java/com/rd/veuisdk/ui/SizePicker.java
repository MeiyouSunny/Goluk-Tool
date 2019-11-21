package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * 贴纸大小选择器
 *
 * @author JIAN
 */
public class SizePicker extends View {
    private final int ITEM_WIDTH = 5;
    public static final int START_SIZE = 16;
    private final int END_SIZE = 35;
    private final int HORIZONTAL_SPCING = 1;
    private final int ITEM_COUNT = 100;
    private float MIN = AppConfig.MIN_SCALE;
    private float MAX = AppConfig.MAX_SCALE;
    private Paint mPaint = new Paint();
    private int index = 0;
    private ArrayList<DisfInfo> list = new ArrayList<DisfInfo>();

    public SizePicker(Context context) {
        this(context, null, 0);
    }

    public SizePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SizePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    private void init(Context context) {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#6B6A70"));
    }

    private class DisfInfo {
        public DisfInfo(Rect rect, double size) {
            this.rect = rect;
            this.size = size;
        }

        Rect rect;
        double size;

    }


    private void initLocation() {
        list.clear();
        int mtop = getHeight() - 150, top2 = getHeight() - 100;
        int nAddLeft = 0;
        for (int i = START_SIZE; i <= END_SIZE; i++) {

            for (int j = 0; j < ITEM_COUNT; j++) {

                int mleft = 0;

                if (j == 0) {
                    mleft = nAddLeft;
                    if (i == END_SIZE) {
                        j = ITEM_COUNT; // 最后一个单位的大小，只能以整数结尾，不能是（35.33、35.67）
                        // 强制退出循环
                    }
                    nAddLeft = mleft + ITEM_WIDTH;
                    list.add(new DisfInfo(new Rect(mleft, mtop, nAddLeft,
                            getHeight()), i));

                } else {
                    mleft = nAddLeft;
                    if (j == 33 || j == 67) {
                        nAddLeft = mleft + 5;
                    } else {
                        nAddLeft = mleft + 3;
                    }
                    list.add(new DisfInfo(new Rect(mleft, top2, nAddLeft,
                            getHeight()), i + j * 1.0 / ITEM_COUNT));

                }

            }

        }

    }

    /**
     * 组件需要的宽度
     *
     * @return
     */
    public int getNeedMinWidth() {
        int nAddLeft = 0;
        for (int i = START_SIZE; i <= END_SIZE; i++) {
            for (int j = 0; j < ITEM_COUNT; j++) {
                int mleft = 0;
                if (j == 0) {
                    mleft = nAddLeft;
                    if (i == END_SIZE) {
                        j = ITEM_COUNT; // 最后一个单位的大小，只能以整数结尾，不能是（35.33、35.67）
                    }
                    nAddLeft = mleft + ITEM_WIDTH;
                } else {
                    mleft = nAddLeft;
                    if (j == 33 || j == 67) {
                        nAddLeft = mleft + 5;
                    } else {
                        nAddLeft = mleft + 3;
                    }
                }
            }
        }
        return nAddLeft;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initLocation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
        int len = list.size();
        for (int i = 0; i < len; i++) {
            int re = i % 100;
            if ((re == 0) || (re == 33) || (re == 67))
                canvas.drawRect(list.get(i).rect, mPaint);
        }

    }


    public double[] getInfo(int scrollX) {
        if (scrollX > HORIZONTAL_SPCING / 3) {
            int poff = HORIZONTAL_SPCING / 3;
            int len = list.size();
            int tindex = 0;
            for (int i = 0; i < len; i++) {
                Rect temp = list.get(i).rect;
                if (temp.left - poff <= scrollX && scrollX <= temp.right + poff) {
                    tindex = i;
                    break;
                }

            }
            if (index != tindex && tindex != 0) {
                index = tindex;
            }
        } else {
            index = 0;
        }
        double[] ds = new double[2];
        ds[0] = list.size() > 0 ? (MIN + ((MAX - MIN) * ((index + 0.0) / list
                .size()))) : 0;
        ds[1] = index < list.size() ? list.get(index).size : 0;
        return ds;

    }

    /**
     * 重新编辑字幕，还原到之前保存的缩放比
     *
     * @param disf
     * @return
     */
    public int setDisf(float disf) {
        float off = disf - MIN;
        int len = list.size();
        float item = (MAX - MIN) / len;
        int index = (int) (off / item);
        int scrollX = 0;
        if (index >= len - 1) {
            return getWidth();
        }
        for (int i = 0; i < len; i++) {
            if (i == index) {
                scrollX = list.get(i).rect.centerX();
                break;

            }

        }
        return scrollX;

    }

    /**
     * 字幕特效目标大小转成缩放比
     *
     * @param targetSize SizePicker#START_SIZE--SizePicker#END_SIZE
     * @return
     */
    public float textToDisf(int targetSize) {
        targetSize = Math.min(END_SIZE, Math.max(START_SIZE, targetSize));
        return MIN + ((MAX - MIN) * (targetSize - START_SIZE) / (END_SIZE - START_SIZE + 0.0f));
    }


}
