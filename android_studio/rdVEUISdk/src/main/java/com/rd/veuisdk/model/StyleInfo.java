package com.rd.veuisdk.model;

import android.graphics.Color;
import android.graphics.RectF;
import android.util.SparseArray;

import com.rd.vecore.models.DewatermarkObject;
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
    public String code, caption, icon;
    public String mlocalpath;
    public boolean vertical;
    public long nTime = 0;
    // 记录当前下载的版本
    public boolean isdownloaded = false;
    /**
     * 仅贴纸支持apng
     */
    public boolean isApng = false;
    //字幕的拉伸区域
    public double left, top, right, buttom; // new add

    public boolean isSub() {
        return isSub;
    }

    private boolean isSub = true;


    /**
     * @return
     */
    public boolean isbUseCustomApi() {
        return bUseCustomApi;
    }

    private boolean bUseCustomApi = false;
    public int pid, type;

    /**
     * 自定义的字幕、特效
     *
     * @param isCustomApi
     * @param isSub       是否是字幕
     */
    public StyleInfo(boolean isCustomApi, boolean isSub) {
        this.isSub = isSub;
        bUseCustomApi = isCustomApi;
    }

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

    public static final double DEFAULT_RECT_W = -1;
    //部分贴纸支持直接设置横向占比  (相对于播放器的预览size  0~1.0f)
    public double rectW = DEFAULT_RECT_W;


    /**
     * 是否有设置拉伸比例
     *
     * @return
     */
    public boolean isSetSizeW() {
        return rectW != DEFAULT_RECT_W;
    }

    /**
     * 默认缩放比
     */
    public float disf = 1f;
    public double w, h;
    // 旋转角度
    public float rotateAngle = 0.0f;
    public int du;
    public int tLeft, tTop, tWidth, tHeight, tRight, tButtom;
    //字体
    public String tFont;
    //循环特效、字幕取第0帧
    public SparseArray<FrameInfo> frameArray = new SparseArray<FrameInfo>();
    public ArrayList<TimeArray> timeArrays = new ArrayList<TimeArray>();

    //默认 允许循环
    public boolean unLoop = false;

    /**
     * 修正单帧的时间
     */
    public void fixFrameDruation() {
        if (null != frameArray && frameArray.size() >= 2) {
            frameDruation = frameArray.valueAt(1).time
                    - frameArray.valueAt(0).time;
        } else {
            frameDruation = 100;
        }
    }

    private int frameDruation = 100;

    /**
     * 特效单帧画面的duration ，单位:毫秒
     *
     * @return
     */
    public int getFrameDuration() {
        return frameDruation;
    }

    //部分特效需要handler 循环绘制 (只有一张背景图，不需要循环绘制)
    public boolean needWhileDraw() {
        return st == CommonStyleUtils.STYPE.special && (null != frameArray && frameArray.size() >= 2);
    }

    public RectF mShowRectF;

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

    public void setTextRectF(RectF textRectF) {
        mTextRectF = textRectF;
    }

    //文本在组件中的显示区域
    private RectF mTextRectF = new RectF(0.01f, 0.01f, 0.99f, 0.99f);

    public void setNinePitch(RectF ninePitch) {
        mNinePitch = ninePitch;
    }

    private RectF mNinePitch = new RectF(0.1f, 0.1f, 0.99f, 0.99f);

    /**
     * 可拉伸的区域
     *
     * @return
     */
    public RectF getNinePitch() {
        return mNinePitch;
    }

    //马赛克
    public static final String FILTER_PIX = "pixelate";
    //高斯
    public static final String FILTER_BLUR = "blur";
    public String filter;
    public String filterPng;

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


    public CommonStyleUtils.STYPE st = CommonStyleUtils.STYPE.sub;


    public DewatermarkObject.Type getType() {
        return mType;
    }

    public void setType(DewatermarkObject.Type type) {
        mType = type;
    }

    private DewatermarkObject.Type mType = DewatermarkObject.Type.mosaic;

    //贴纸分类代码
    public String category;

    @Override
    public String toString() {
        return "StyleInfo{" +
                "id=" + hashCode() +
                "index=" + index +
                ", code='" + code + '\'' +
                ", caption='" + caption + '\'' +
                ", mlocalpath='" + mlocalpath + '\'' +
                ", vertical=" + vertical +
                ", nTime=" + nTime +
                ", isdownloaded=" + isdownloaded +
                ", left=" + left +
                ", top=" + top +
                ", right=" + right +
                ", buttom=" + buttom +
                ", isSub=" + isSub +
                ", pid=" + pid +
                ", type=" + type +
                ", lashen=" + lashen +
                ", onlyone=" + onlyone +
                ", shadow=" + shadow +
                ", disf=" + disf +
                ", w=" + w +
                ", h=" + h +
                ", du=" + du +
                ", tLeft=" + tLeft +
                ", tTop=" + tTop +
                ", tWidth=" + tWidth +
                ", tHeight=" + tHeight +
                ", tRight=" + tRight +
                ", tButtom=" + tButtom +
                ", tFont='" + tFont + '\'' +
//                ", frameArray=" + frameArray +
//                ", timeArrays=" + timeArrays +
                ", frameDruation=" + frameDruation +
                ", mShowRectF=" + mShowRectF +
                ", hint='" + hint + '\'' +
                ", mTextRectF=" + mTextRectF +
                ", mNinePitch=" + mNinePitch +
                ", filter='" + filter + '\'' +
                ", centerxy=" + Arrays.toString(centerxy) +
                '}';
    }
}
