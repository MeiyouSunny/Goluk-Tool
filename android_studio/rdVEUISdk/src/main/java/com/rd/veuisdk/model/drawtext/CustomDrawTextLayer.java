package com.rd.veuisdk.model.drawtext;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.models.CanvasObject;

/**
 * 自定义绘制文本-处理时间线
 */
public abstract class CustomDrawTextLayer {

    public CustomDrawTextLayer(TextLayer layer) {
        mTextLayer = layer;
    }

    protected TextLayer mTextLayer;
    protected static final float fW = 480.0f, fH = 852.0f;

    /**
     * 配置当前时刻，当前layer的效果并绘制
     *
     * @param canvas
     * @param currentProgress 当前进度：秒
     */
    public void initConfigAndDraw(CanvasObject canvas, float currentProgress) {

        float timelineFrom = 0f;
        float timelineTo = 0f;
        if (0 <= currentProgress && currentProgress < (timelineTo = onTimeMake(127))) {
            init127(timelineFrom, timelineTo);
        } else if ((timelineFrom = onTimeMake(127)) <= currentProgress && currentProgress < (timelineTo = onTimeMake(136))) {
            init136(timelineFrom, timelineTo);
        } else if ((timelineFrom = onTimeMake(136)) <= currentProgress && currentProgress < (timelineTo = onTimeMake(210))) {
            init210(timelineFrom, timelineTo);
        } else if ((timelineFrom = onTimeMake(210)) <= currentProgress && currentProgress < (timelineTo = onTimeMake(220))) {
            init220(timelineFrom, timelineTo);
        } else if ((timelineFrom = onTimeMake(220)) <= currentProgress && currentProgress < (timelineTo = onTimeMake(294))) {
            init294(timelineFrom, timelineTo);
        } else if ((timelineFrom = onTimeMake(294)) <= currentProgress && currentProgress < (timelineTo = onTimeMake(305))) {
            init305(timelineFrom, timelineTo);
        } else if ((timelineFrom = onTimeMake(305)) <= currentProgress && currentProgress < (timelineTo = onTimeMake(375))) {
            init375(timelineFrom, timelineTo);
        } else if ((timelineFrom = onTimeMake(375)) <= currentProgress && currentProgress < (timelineTo = onTimeMake(385))) {
            init385(timelineFrom, timelineTo);
        } else {
            initOther( onTimeMake(385),  onTimeMake(1000));
        }
        if (null != canvas) {
            drawText(canvas, currentProgress);
        }

    }


    /**
     * 0~127帧数
     */
    abstract void init127(float timelineFrom, float timelineTo);

    abstract void init136(float timelineFrom, float timelineTo);

    abstract void init210(float timelineFrom, float timelineTo);

    abstract void init220(float timelineFrom, float timelineTo);

    abstract void init294(float timelineFrom, float timelineTo);

    abstract void init305(float timelineFrom, float timelineTo);

    abstract void init375(float timelineFrom, float timelineTo);

    abstract void init385(float timelineFrom, float timelineTo);

    abstract void initOther(float timelineFrom, float timelineTo);

    /**
     * 实时绘制
     *
     * @param canvas
     * @param progress
     */
    private void drawText(CanvasObject canvas, float progress) {
        mTextLayer.onDraw(canvas, progress);
    }


    /**
     * @param rect
     * @param fW
     * @param fH
     * @return
     */
    RectF rect2RectF(Rect rect, float fW, float fH) {
        return new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);
    }

    RectF rect2RectF(RectF rect, float fW, float fH) {
        return new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);
    }


    //单位：秒
    private float onTimeMake(int frame) {
        return frame / (30.0f);
    }

    /**
     * 旋转
     */
    protected void initScreenLayer(float lineFrom, float lineTo) {
        TextLayer.IFrame iFrame = new TextLayer.IFrame(lineFrom, lineTo, null, null);
        iFrame.setAngle(0, 90, new PointF(402 / fW, 460 / fH));
        mTextLayer.setScreenFrame(iFrame);
    }

    /**
     * 旋转2
     */
    protected void initScreenLayer2(float lineFrom, float lineTo) {
        TextLayer.IFrame iFrame = new TextLayer.IFrame(lineFrom, lineTo, null, null);
        iFrame.setAngle(90,0, new PointF(402 / fW, 460 / fH));
        mTextLayer.setScreenFrame(iFrame);
    }
}
