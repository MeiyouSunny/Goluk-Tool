package com.rd.veuisdk.utils;

import android.graphics.Color;
import android.graphics.Rect;

import com.rd.vecore.models.CanvasObject;
import com.rd.vecore.models.TextLayout;
import com.rd.veuisdk.model.drawtext.CustomDrawTextLayer;
import com.rd.veuisdk.model.drawtext.CustomDrawTextLayer1;
import com.rd.veuisdk.model.drawtext.CustomDrawTextLayer2;
import com.rd.veuisdk.model.drawtext.CustomDrawTextLayer3;
import com.rd.veuisdk.model.drawtext.CustomDrawTextLayer4;
import com.rd.veuisdk.model.drawtext.CustomDrawTextLayer5;
import com.rd.veuisdk.model.drawtext.TextLayer;

/**
 * 自绘实现自说功能
 */
public class CustomDrawTextHandler {

    private CustomDrawTextLayer mCustomDrawTextLayer1;
    private CustomDrawTextLayer mCustomDrawTextLayer2;
    private CustomDrawTextLayer mCustomDrawTextLayer3;
    private CustomDrawTextLayer mCustomDrawTextLayer4;
    private CustomDrawTextLayer mCustomDrawTextLayer5;

    public CustomDrawTextHandler() {
        {
            TextLayer textLayer = new TextLayer(357, 53, new Rect(6, 6, 6, 6), "静夜思", Color.rgb(0, 0, 0), TextLayout.Alignment.left);
            mCustomDrawTextLayer1 = new CustomDrawTextLayer1(textLayer);
        }
        {
            TextLayer textLayer = new TextLayer(375, 71, new Rect(6, 6, 6, 6), "床前明月光", Color.rgb(230, 0, 18),TextLayout.Alignment.left);
            mCustomDrawTextLayer2 = new CustomDrawTextLayer2(textLayer);
        }
        {
            TextLayer textLayer = new TextLayer(375, 96, new Rect(6, 6, 6, 6), "疑是地上霜", Color.rgb(0, 0, 0),TextLayout.Alignment.left);
            mCustomDrawTextLayer3 = new CustomDrawTextLayer3(textLayer);
        }
        {
            TextLayer textLayer = new TextLayer(375, 80, new Rect(6, 6, 6, 6), "举头望明月", Color.rgb(230, 0, 18),TextLayout.Alignment.left);
            mCustomDrawTextLayer4 = new CustomDrawTextLayer4(textLayer);
        }
        {
            TextLayer textLayer = new TextLayer(375, 128, new Rect(6, 6, 6, 6), "低头思故乡", Color.rgb(0, 0, 0),TextLayout.Alignment.right);
            mCustomDrawTextLayer5 = new CustomDrawTextLayer5(textLayer);
        }

    }


    /**
     * 自绘
     *
     * @param canvas
     * @param currentProgress 单位：秒
     */
    public void drawText(CanvasObject canvas, float currentProgress) {

        if (null != mCustomDrawTextLayer1) {
            mCustomDrawTextLayer1.initConfigAndDraw(canvas, currentProgress);
        }
        if (null != mCustomDrawTextLayer2) {
            mCustomDrawTextLayer2.initConfigAndDraw(canvas, currentProgress);
        }
        if (null != mCustomDrawTextLayer3) {
            mCustomDrawTextLayer3.initConfigAndDraw(canvas, currentProgress);
        }
        if (null != mCustomDrawTextLayer4) {
            mCustomDrawTextLayer4.initConfigAndDraw(canvas, currentProgress);
        }
        if (null != mCustomDrawTextLayer5) {
            mCustomDrawTextLayer5.initConfigAndDraw(canvas, currentProgress);
        }


    }


}
