package com.rd.veuisdk.model.drawtext;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * 窗前明月光
 */
public class CustomDrawTextLayer2 extends CustomDrawTextLayer {
    public CustomDrawTextLayer2(TextLayer layer) {
        super(layer);
    }


    @Override
    void init127(float timelineFrom, float timelineTo) {
        mTextLayer.setFrame(null);
    }

    @Override
    void init136(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 454;
        float scale = 0.9f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);
        RectF rectF = new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);


        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, rectF, new RectF(rectF))
                .setAngle(90, -90, new PointF(rectF.left, rectF.bottom)).setScale(0, 0, 1, 1);
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init210(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 454;

        float scale = 0.9f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);
        RectF rectF = new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);


        //不变
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, rectF, new RectF(rectF))
                .setAngle(0, 0, new PointF(rectF.left, rectF.bottom));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init220(float timelineFrom, float timelineTo) {

        int l = 73;
        int b = 454;

        float scale = 0.9f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);
        RectF rectF = new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);


        //上移动-变小
        b = 374;
        scale = 0.58f;
        RectF dst = new RectF(rect.left / fW, (b - mTextLayer.getHeight() * scale) / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, b / fH);
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, rectF, new RectF(dst));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init294(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 454;

        float scale = 0.9f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);

        b = 374;
        scale = 0.58f;
        RectF dst = new RectF(rect.left / fW, (b - mTextLayer.getHeight() * scale) / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, b / fH);
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst), new RectF(dst));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init305(float timelineFrom, float timelineTo) {

        int l = 73;
        int b = 454;

        float scale = 0.9f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);


        b = 374;
        scale = 0.58f;
        RectF dst = new RectF(rect.left / fW, (b - mTextLayer.getHeight() * scale) / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, b / fH);


        b = 317;
        scale = 0.32f;
        RectF dst1 = new RectF(rect.left / fW, (b - mTextLayer.getHeight() * scale) / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, b / fH);
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst), new RectF(dst1));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init375(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 454;

        float scale = 0.9f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);


        b = 317;
        scale = 0.32f;
        RectF dst1 = new RectF(rect.left / fW, (b - mTextLayer.getHeight() * scale) / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, b / fH);

        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst1), new RectF(dst1));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init385(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 454;

        float scale = 0.9f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);


        b = 317;
        scale = 0.32f;
        RectF dst1 = new RectF(rect.left / fW, (b - mTextLayer.getHeight() * scale) / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, b / fH);


        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst1), new RectF(dst1));
        mTextLayer.setFrame(iFrame);


        initScreenLayer(timelineFrom, timelineTo);
    }

    @Override
    void initOther(float timelineFrom, float timelineTo) {

        //屏幕外的效果，就不用绘制

    }
}
