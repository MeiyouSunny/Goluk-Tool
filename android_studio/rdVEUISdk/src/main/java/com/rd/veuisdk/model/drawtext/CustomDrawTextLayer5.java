package com.rd.veuisdk.model.drawtext;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * 低头思故乡
 */
public class CustomDrawTextLayer5 extends CustomDrawTextLayer {
    public CustomDrawTextLayer5(TextLayer layer) {
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
        mTextLayer.setFrame(null);
    }

    @Override
    void init294(float timelineFrom, float timelineTo) {
        mTextLayer.setFrame(null);
    }


    @Override
    void init305(float timelineFrom, float timelineTo) {
        mTextLayer.setFrame(null);
    }

    @Override
    void init375(float timelineFrom, float timelineTo) {
        mTextLayer.setFrame(null);
    }

    @Override
    void init385(float timelineFrom, float timelineTo) {
        //右下角顶点坐标

        int l = 394;
        int b = 467;


        float scale = 0.85f;
        RectF rect = new RectF(l - mTextLayer.getWidth() * scale, b - (mTextLayer.getHeight() * scale), l, b);
        RectF rectF = new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);


        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, rectF, new RectF(rectF))
                .setAngle(-90, 90, new PointF(rectF.right, rectF.bottom));
        iFrame.setRotateType(1);
        iFrame.setScale(0, 0, 1, 1);
        mTextLayer.setFrame(iFrame);
    }

    @Override
    void initOther(float timelineFrom, float timelineTo) {

        //右下角顶点坐标

        int l = 394;
        int b = 467;


        float scale = 0.85f;
        RectF rect = new RectF(l - mTextLayer.getWidth() * scale, b - (mTextLayer.getHeight() * scale), l, b);
        RectF rectF = new RectF(rect.left / fW, rect.top / fH, rect.right / fW, rect.bottom / fH);


        TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, rectF, new RectF(rectF));

        mTextLayer.setFrame(iFrame);

    }
}
