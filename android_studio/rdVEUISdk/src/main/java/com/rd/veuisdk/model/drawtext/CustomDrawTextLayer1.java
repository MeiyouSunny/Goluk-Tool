package com.rd.veuisdk.model.drawtext;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * 静夜思
 */
public class CustomDrawTextLayer1 extends CustomDrawTextLayer {
    private int x = 0;
    private int y = 0;

    public CustomDrawTextLayer1(TextLayer layer) {
        super(layer);
    }

    @Override
    void init127(float timelineFrom, float timelineTo) {
        {
            //左下角顶点 （水平状态下，矩形的左下角的顶点坐标）
            x = 53;
            y = 452;
            float scale = 1f;
            RectF rectF = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(rectF), new RectF(rectF));
            mTextLayer.setFrame(iFrame);
        }
    }

    @Override
    void init136(float timelineFrom, float timelineTo) {
        {  //旋转90
            x = 69;
            y = 455;
            float scale = 1f;
            RectF rectF = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            RectF dst = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(rectF), new RectF(dst));
            PointF rotatePointF = new PointF(rectF.left, rectF.bottom);
            iFrame.setAngle(0, -90, rotatePointF);
            mTextLayer.setFrame(iFrame);
        }
    }

    @Override
    void init210(float timelineFrom, float timelineTo) {
        {
            //竖着静止
            x = 69;
            y = 455;
            float scale = 1f;
            RectF rectF = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            RectF dst = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            PointF rotatePointF = new PointF(rectF.left, rectF.bottom);
            TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst),
                    new RectF(dst)).setAngle(270, 0, new PointF(rotatePointF.x, rotatePointF.y));
            mTextLayer.setFrame(iFrame);
        }
    }

    @Override
    void init220(float timelineFrom, float timelineTo) {
        {
            //竖着缩小 1
            x = 69;
            y = 455;
            RectF src = new RectF(x / fW, (y - mTextLayer.getHeight() * 1) / fH, (x + mTextLayer.getWidth() * 1) / fW, y / fH);


            float scale = 0.68f;
            //宽高缩小为0.68f
            x = 69 + (455 - 374);
            y = 455;

            RectF dst2 = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(src),
                    new RectF(dst2)).setAngle(270, 0, new PointF(src.left, src.bottom));
            mTextLayer.setFrame(iFrame);
        }
    }

    @Override
    void init294(float timelineFrom, float timelineTo) {
        {
            //竖着静止1
            x = 69;
            y = 455;
            float scale = 1f;
            RectF rectF = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);


            x = 69 + (455 - 374);
            y = 455;
            scale = 0.68f;                      //宽高缩小为0.68f
            RectF src = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(src),
                    new RectF(src)).setAngle(270, 0, new PointF(rectF.left, rectF.bottom));
            mTextLayer.setFrame(iFrame);
        }
    }

    @Override
    void init305(float timelineFrom, float timelineTo) {
        {
            //竖着缩小2
            x = 69;
            y = 455;
            RectF src0 = new RectF(x / fW, (y - mTextLayer.getHeight() * 1) / fH, (x + mTextLayer.getWidth() * 1) / fW, y / fH);


            float scale = 0.68f;
            x = 69 + (455 - 374);
            y = 455;
            RectF dst2 = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);


            //缩小为0.42f
            x = 69 + (455 - 374) + (374 - 318);
            y = 455;
            scale = 0.42f;
            RectF dst3 = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);

            TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(dst2),
                    new RectF(dst3)).setAngle(270, 0, new PointF(src0.left, src0.bottom));
            mTextLayer.setFrame(iFrame);
        }

    }

    @Override
    void init375(float timelineFrom, float timelineTo) {
        {
            //竖着静止2
            x = 69;
            y = 455;
            float scale = 1f;
            RectF src0 = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);


            //缩小为0.42f
            x = 69 + (455 - 374) + (374 - 318);
            y = 455;
            scale = 0.42f;

            RectF rectF = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(rectF),
                    new RectF(rectF)).setAngle(270, 0, new PointF(src0.left, src0.bottom));
            mTextLayer.setFrame(iFrame);
        }

    }

    @Override
    void init385(float timelineFrom, float timelineTo) {

        {
            x = 69;
            y = 455;
            RectF src0 = new RectF(x / fW, (y - mTextLayer.getHeight() * 1) / fH, (x + mTextLayer.getWidth() * 1) / fW, y / fH);


            //缩小为0.42f
            x = 69 + (455 - 374) + (374 - 318);
            y = 455;
            float scale = 0.42f;
            RectF rectF = new RectF(x / fW, (y - mTextLayer.getHeight() * scale) / fH, (x + mTextLayer.getWidth() * scale) / fW, y / fH);
            // 一直停留在此处，整体旋转
            TextLayer.IFrame iFrame = new TextLayer.IFrame(timelineFrom, timelineTo, new RectF(rectF),
                    new RectF(rectF)).setAngle(270, 0, new PointF(src0.left, src0.bottom));
            mTextLayer.setFrame(iFrame);


            //旋转
            initScreenLayer(timelineFrom, timelineTo);

        }
    }

    @Override
    void initOther(float timelineFrom, float timelineTo) {

        //屏幕外的效果，就不用绘制

    }
}
