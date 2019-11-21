package com.rd.veuisdk.utils;

import android.graphics.PointF;

import com.rd.vecore.models.AnimationObject;

import java.util.List;

/**
 * 作者：JIAN on 2018/6/7 16:04
 */
public class Quad2Config {
    /**
     * d : true
     * t : 0.5
     * c : [110.667,236.296,431.189,147.367,4.64481,753.345,470.676,751.495]
     */

    private boolean d;
    private double t;
    private List<Float> c;

    public boolean isD() {
        return d;
    }

    public void setD(boolean d) {
        this.d = d;
    }

    public double getT() {
        return t;
    }

    public void setT(double t) {
        this.t = t;
    }

    public void getPoints(AnimationObject animationObject) {
        animationObject.setShowPointFs(
                new PointF(c.get(0) / 540, c.get(1) / 960),
                new PointF(c.get(2) / 540, c.get(3) / 960),
                new PointF(c.get(4) / 540, c.get(5) / 960),
                new PointF(c.get(6) / 540, c.get(7) / 960));
    }

    public List<Float> getC() {
        return c;
    }

    public void setC(List<Float> c) {
        this.c = c;
    }
}
