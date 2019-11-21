package com.rd.veuisdk.model.drawtext;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.rd.vecore.models.CanvasObject;
import com.rd.vecore.models.TextLayout;


/**
 * 旋转角度统一定义与时针一致  3点  0度； 6 点 90 ；9 点  180 ； 12 点 270
 */
public class TextLayer {
    private String TAG = "TextLayer";

    private com.rd.vecore.graphics.Matrix matrix = null;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private final int width, height;
    private Rect padding;
    private String textContent;
    private int textColor;
    private TextLayout.Alignment alignment;


    public TextLayer(int width, int height, Rect padding, String text, int textColor, TextLayout.Alignment alignment) {
        this.width = width;
        this.height = height;
        this.padding = padding;
        this.textContent = text;
        this.textColor = textColor;
        this.alignment = alignment;
        matrix = new com.rd.vecore.graphics.Matrix();

    }


    private IFrame iFrame;

    /**
     * 设置当前的状态
     *
     * @param frame
     */
    public void setFrame(IFrame frame) {
        iFrame = frame;
    }

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 绘制
     *
     * @param progress 单位：秒
     */
    public void onDraw(CanvasObject canvas, float progress) {

        if (null != iFrame && iFrame.nTimelineFrom <= progress && progress < iFrame.nTimelineTo) {
            matrix.reset();
            RectF rectF = iFrame.mRectF1;
            int pw = canvas.getWidth();
            int ph = canvas.getHeight();
            canvas.save();
            Rect rect = new Rect((int) (rectF.left * pw), (int) (rectF.top * ph), (int) (rectF.right * pw), (int) (rectF.bottom * ph));

            RectF rectF2 = iFrame.mRectF2;
            Rect rect2 = new Rect((int) (rectF2.left * pw), (int) (rectF2.top * ph), (int) (rectF2.right * pw), (int) (rectF2.bottom * ph));


            float factor = (progress - iFrame.nTimelineFrom) / (iFrame.nTimelineTo - iFrame.nTimelineFrom);


            //有上一段  ，需要计算旋转和移动
            if (0 != iFrame.angleSum || iFrame.angle1 != 0) {
                //有旋转
                int angle;
                if (iFrame.angleSum == 0) {
                    angle = iFrame.angle1;
                } else {
                    angle = (int) (iFrame.angle1 + (factor * (iFrame.angleSum)));
                }
                int rx = (int) (pw * iFrame.getRotatePointF().x);
                int ry = (int) (ph * iFrame.getRotatePointF().y);

                if (iFrame.mRotateType == 1) {
                    matrix.postRotate(angle % 360, rx, ry);
                } else {
                    matrix.postRotate(angle % 360, rx, ry);
                }
            }
            if (iFrame.getOffX() != 0 || iFrame.getOffY() != 0) {
                //有缩放
                float fScaleX = iFrame.scaleX1 + (factor * iFrame.getOffX());
                float fScaleY = iFrame.scaleY1 + (factor * iFrame.getOffY());
                matrix.postScale(fScaleX, fScaleY, (int) (pw * iFrame.getRotatePointF().x), (int) (ph * iFrame.getRotatePointF().y));
            }
            {
                if (!iFrame.mRectF1.equals(iFrame.mRectF2)) {
                    //位置有变化，平移
                    Rect dst = new Rect(rect.left + (int) ((rect2.left - rect.left) * factor),
                            rect.top + (int) ((rect2.top - rect.top) * factor),
                            rect.right + (int) ((rect2.right - rect.right) * factor),
                            rect.bottom + (int) ((rect2.bottom - rect.bottom) * factor));
                    rect = dst;
                }

            }


            //旋转
            IFrame iFrame2 = mScreenFrame;
            if (null != iFrame2 && iFrame2.nTimelineFrom <= progress && progress < iFrame2.nTimelineTo) {
                if (0 != iFrame2.angleSum || iFrame2.angle1 != 0) {

                    factor = (progress - iFrame2.nTimelineFrom) / (iFrame2.nTimelineTo - iFrame2.nTimelineFrom);
                    //有旋转
                    int angle;
                    if (iFrame2.angleSum == 0) {
                        angle = iFrame2.angle1;
                    } else {
                        angle = (int) (iFrame2.angle1 + (factor * (iFrame2.angleSum)));
                    }

                    int rx = (int) (pw * iFrame2.getRotatePointF().x), ry = (int) (ph * iFrame2.getRotatePointF().y);
                    matrix.postRotate(angle % 360, rx, ry);

                }
            }
            canvas.setMatrix(matrix);
//            TextLayout mTextLayerInfo = new TextLayout(width, height, textContent, padding, alignment);
            TextLayout mTextLayerInfo = new TextLayout(rect.width(), rect.height(), textContent, padding, alignment);

            //不能指定textsize (指定也无效，为了达到最佳显示效果，核心会通过文本长度和显示位置大小，padding，自动计算最佳的字体大小)
            //文本
            mTextPaint.reset();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(textColor);

            //绘制文本
            canvas.drawText(mTextLayerInfo, rect.left, rect.top, mTextPaint);
            canvas.restore();
        }
    }


    public void setScreenFrame(IFrame screenFrame) {
        mScreenFrame = screenFrame;
    }

    private IFrame mScreenFrame;

    public static class IFrame {


        /**
         * @param nTimelineFrom
         * @param nTimelineTo
         * @param rectF1        开始时的位置 0~1.0f
         * @param rectF2        结束时的位置
         */
        public IFrame(float nTimelineFrom, float nTimelineTo, RectF rectF1, RectF rectF2) {
            this.nTimelineFrom = nTimelineFrom;
            this.nTimelineTo = nTimelineTo;
            mRectF1 = rectF1;
            mRectF2 = rectF2;
        }

        float nTimelineFrom, nTimelineTo;
        RectF mRectF1; //0~1.0f
        RectF mRectF2; //0~1.0f

        /**
         * @param angleBegin   开始时刻的运动角度
         * @param angleSum     当前时间的总共需要运动的角度 （ 顺时针：正数； 逆时针 :负数）
         * @param rotatePointF 旋转中心点
         * @return
         */
        public IFrame setAngle(int angleBegin, int angleSum, PointF rotatePointF) {
            this.angle1 = angleBegin;
            this.angleSum = angleSum;
            mRotatePointF = rotatePointF;
            return this;
        }

        int angle1; //当前时间段开始时刻的角度
        int angleSum; //当前时间段的总共运动角度

        public PointF getRotatePointF() {
            return mRotatePointF;
        }

        //旋转中心点
        private PointF mRotatePointF = new PointF(0.5f, 0.5f);

        /**
         * 图片的旋转顶点
         *
         * @param rotateType
         */
        public void setRotateType(int rotateType) {
            mRotateType = rotateType;
        }

        int mRotateType = 0; //图片的旋转中心 ：0 左下角  、1 右下角  、2  右上角 、 3 左上角

        public IFrame setScale(float scaleX1, float scaleY1, float scaleX2, float scaleY2) {
            this.scaleX1 = scaleX1;
            this.scaleY1 = scaleY1;
            this.scaleX2 = scaleX2;
            this.scaleY2 = scaleY2;
            return this;
        }

        float scaleX1 = 1;
        float scaleY1 = 1;
        private float scaleX2 = 1;
        private float scaleY2 = 1;

        float getOffX() {
            return scaleX2 - scaleX1;
        }

        float getOffY() {
            return scaleY2 - scaleY1;
        }


    }


}
