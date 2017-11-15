package com.rd.veuisdk.model;

import android.graphics.Color;
import android.graphics.RectF;
import android.util.SparseArray;

import com.rd.veuisdk.utils.CommonStyleUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 字幕特效 type==0,可以写字
 * 单个字幕特效，默认的样式（旋转、hint 、字体颜色等）
 *
 * @author JIAN
 */

public class StyleInfo {

    public int index = 0;
    public String code, caption;
    public String mlocalpath;
    public long nTime = 0;
    // 记录当前下载的版本
    public boolean isdownloaded = false;

    /**
     * 缩放系数 = disf
     */
    public float zoomFactor;

    public double left, top, right, buttom; // new add

    public int pid, type, pExtend, extendSection;
    /**
     * 是否拉伸
     */
    public boolean lashen = false;

    /**
     * 是否只以一行方式显示
     */
    public boolean onlyone = false;
    /**
     * 是否阴影
     */
    public boolean shadow = false;
    /**
     * 默认缩放比
     */
    public float disf = 1f;
    public double x, y, w, h;
    // 旋转角度
    public float rotateAngle = 0.0f;
    public int fid, du;
    public int tLeft, tTop, tWidth, tHeight, tRight, tButtom;
    //字体
    public String tFont;
    public double a, fx, fy, fw, fh, c;
    public String n;
    //循环特效、字幕取第0帧
    public SparseArray<FrameInfo> frameArray = new SparseArray<FrameInfo>();
    public ArrayList<TimeArray> timeArrays = new ArrayList<TimeArray>();
    /**
     * 描边颜色 、描边宽度
     */
    public int strokeColor = 0, strokeWidth = 0;


    public String getHint() {
        return hint;
    }

    public int getTextDefaultColor() {
        return textDefaultColor;
    }


    public RectF getTextRectF() {
        return mTextRectF;
    }

    //默认文本
    private String hint = "";
    //默认字体颜色
    private int textDefaultColor = Color.WHITE;
    //文本在组件中的显示区域
    private RectF mTextRectF = new RectF(0.01f, 0.01f, 0.99f, 0.99f);

    /**
     * 初始化默认的text 信息
     *
     * @param hint
     * @param textDefaultColor
     * @param textRectF
     */
    public void initDefault(String hint, int textDefaultColor, RectF textRectF) {
        this.hint = hint;
        this.textDefaultColor = textDefaultColor;
        if (null != textRectF) {
            this.mTextRectF = textRectF;
        }
    }

    // 图片旋转中心点坐标在x，y的比例
    public float[] centerxy = new float[]{0.5f, 0.5f};

    @Override
    public String toString() {
        return "StyleInfo{" +
                "index=" + index +
                ", code='" + code + '\'' +
                ", caption='" + caption + '\'' +
                ", mlocalpath='" + mlocalpath + '\'' +
                ", nTime=" + nTime +
                ", isdownloaded=" + isdownloaded +
                ", zoomFactor=" + zoomFactor +
                ", left=" + left +
                ", top=" + top +
                ", right=" + right +
                ", buttom=" + buttom +
                ", pid=" + pid +
                ", type=" + type +
                ", pExtend=" + pExtend +
                ", extendSection=" + extendSection +
                ", lashen=" + lashen +
                ", onlyone=" + onlyone +
                ", shadow=" + shadow +
                ", disf=" + disf +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                ", rotateAngle=" + rotateAngle +
                ", fid=" + fid +
                ", du=" + du +
                ", tLeft=" + tLeft +
                ", tTop=" + tTop +
                ", tWidth=" + tWidth +
                ", tHeight=" + tHeight +
                ", tRight=" + tRight +
                ", tButtom=" + tButtom +
                ", tFont='" + tFont + '\'' +
                ", a=" + a +
                ", fx=" + fx +
                ", fy=" + fy +
                ", fw=" + fw +
                ", fh=" + fh +
                ", c=" + c +
                ", n='" + n + '\'' +
                ", frameArray=" + frameArray +
                ", timeArrays=" + timeArrays +
                ", strokeColor=" + strokeColor +
                ", strokeWidth=" + strokeWidth +
                ", hint='" + hint + '\'' +
                ", textDefaultColor=" + textDefaultColor +
                ", mTextRectF=" + mTextRectF +
                ", centerxy=" + Arrays.toString(centerxy) +
                ", st=" + st +
                '}';
    }

    public CommonStyleUtils.STYPE st = CommonStyleUtils.STYPE.sub;

}
