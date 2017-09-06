package com.rd.veuisdk.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.rd.veuisdk.R;

/**
 * 支持高亮显示HighLight
 *
 * @author JIAN
 * @date 2016-11-28 下午5:04:59
 */
public class RdSeekBar extends SeekBar {

    private Paint pHighLight = new Paint();
    private int radiusHighLight = 8;
    private int[] lights;

    public RdSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        pHighLight.setAntiAlias(true);
        pHighLight.setStyle(Style.FILL);
        Resources res = getResources();
        pHighLight.setColor(res.getColor(R.color.trim_point_color));
        radiusHighLight = res
                .getDimensionPixelSize(R.dimen.highlight_width_preview);
    }


    public void setHighLights(int[] ls) {
        lights = ls;
        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (null != lights) {
            int len = lights.length;
            int ty = (getHeight() / 2);
            int pLeft = getPaddingLeft(), pRight = getPaddingRight(), pWidth = getWidth();
            for (int i = 0; i < len; i++) {
                int left = (int) (pLeft + ((lights[i] + 0.0) / getMax())
                        * (pWidth - pLeft - pRight));
                canvas.drawCircle(left, ty, radiusHighLight, pHighLight);
                canvas.drawCircle(left, ty, radiusHighLight, pHighLight);
            }

        }

    }
}
