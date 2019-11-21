package com.rd.veuisdk.model.drawtext;

import android.graphics.RectF;

/**
 * 疑似地上霜
 */
public class CustomDrawTextLayer3 extends CustomDrawTextLayer {

    public CustomDrawTextLayer3(TextLayer layer) {
        super(layer);
    }

    @Override
    void init127(float timelineFrom, float timelineTo) {
        mTextLayer.setFrame(null);
    }

    @Override
    void init136(float timelineFrom, float timelineTo) {
        mTextLayer.setFrame(null);
    }



    @Override
    void init210(float timelineFrom, float timelineTo) {
        mTextLayer.setFrame(null);
    }

    @Override
    void init220(float timelineFrom, float timelineTo) {
        //左下角顶点坐标
        int l = 73;
        int b = 471;
        float scale = 0.87f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);
        RectF rectF = new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);


        RectF src1 = rect2RectF(new RectF(l, b - 2, l + 2, b), fW, fH);
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, src1, new RectF(rectF));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init294(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 471;
        float scale = 0.87f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);
        RectF rectF = new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);


        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(rectF), new RectF(rectF));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init305(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 471;
        float scale = 0.87f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);
        RectF rectF = new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);


        b = 327;
        scale = 0.61f;
        RectF dst1 = new RectF(rect.left / fW, b / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, (b + mTextLayer.getHeight() * scale) / fH);
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(rectF), new RectF(dst1));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init375(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 471;
        float scale = 0.87f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);


        b = 327;
        scale = 0.61f;
        RectF dst1 = new RectF(rect.left / fW, b / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, (b + mTextLayer.getHeight() * scale) / fH);
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst1), new RectF(dst1));
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void init385(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 471;
        float scale = 0.87f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);

        b = 327;
        scale = 0.61f;
        RectF dst1 = new RectF(rect.left / fW, b / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, (b + mTextLayer.getHeight() * scale) / fH);
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst1), new RectF(dst1));
        mTextLayer.setFrame(iFrame);

        initScreenLayer(timelineFrom, timelineTo);
    }

    @Override
    void initOther(float timelineFrom, float timelineTo) {
        int l = 73;
        int b = 471;
        float scale = 0.87f;
        RectF rect = new RectF(l, b - mTextLayer.getHeight() * scale, l + mTextLayer.getWidth() * scale, b);

        b = 327;
        scale = 0.61f;
        RectF dst1 = new RectF(rect.left / fW, b / fH, (rect.left + mTextLayer.getWidth() * scale) / fW, (b + mTextLayer.getHeight() * scale) / fH);
        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst1), new RectF(dst1));
        mTextLayer.setFrame(iFrame);

        initScreenLayer2(timelineFrom,timelineTo);

    }
}
