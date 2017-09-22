package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.rd.veuisdk.R;
import com.rd.veuisdk.mix.MixItemHolder;

/**
 * 单个视频块儿的位置
 * <p>
 * Created by JIAN on 2017/8/24.
 */

public class VideoPreviewLayout extends FrameLayout {
    private String TAG = "VideoPreviewLayout";
    private RectF videoRectF = null;
    private int color_n, color_p;

    private final int STROKE_WIDTH = 6;

    /**
     * 设置是否选择的状态
     *
     * @param isChecked
     */
    public void setCheck(boolean isChecked) {
        mPaint.setColor(isChecked ? color_p : color_n);
        invalidate();
    }

    /***
     *
     * @param context
     * @param attrs
     */
    public VideoPreviewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        color_n = getResources().getColor(R.color.white);
        color_p = getResources().getColor(R.color.main_orange);
        mPaint.setColor(color_n);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);

    }


    /**
     * 在指定位置添加一個view ,且设置后更改父容器的大小，且固定
     *
     * @param rectf
     */
    public void setCustomRect(RectF rectf) {
        videoRectF = rectf;
        needChangSize = true;
    }


    /**
     * 摄像头父容器的id
     */
    private int cameraViewId = "cameraParent".hashCode();

    /**
     * 创建摄像头父容器界面
     *
     * @return
     */
    public RelativeLayout createCameraView() {
        RelativeLayout m_rlPreviewLayout = new RelativeLayout(getContext(), null);
        m_rlPreviewLayout.setId(cameraViewId);
        int len = getChildCount();
        for (int i = 0; i < len; i++) {
            getChildAt(i).setVisibility(View.GONE);
        }
        this.addView(m_rlPreviewLayout, new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return m_rlPreviewLayout;
    }

    /**
     * 移除摄像头容器
     */
    public void removeCameraView() {
        View temp = this.findViewById(cameraViewId);
        if (null != temp) {
            this.removeView(temp);
        }
        int len = getChildCount();
        for (int i = 0; i < len; i++) {
            getChildAt(i).setVisibility(View.VISIBLE);
        }

    }


    public RectF getVideoRectF() {
        if (null == videoRectF) {
            return new RectF(0, 0, 1, 1);
        }
        return videoRectF;
    }

    private boolean needChangSize = false;

    /**
     * 重置当前容器大小
     *
     * @param rectF
     */
    public void resetChildSize(RectF rectF) {
        videoRectF = rectF;
//        Log.e(TAG, "resetChildSize: " + videoRectF.toShortString() + "..." + viewPrepared);
        if (viewPrepared) {
            setTargetSize();
        } else {
            needChangSize = true;
        }
    }

    private boolean bClearBorderLine = false;

    /**
     * 是否清除边框线
     *
     * @param clearLine
     */
    public void setClearBorderLine(boolean clearLine) {
        bClearBorderLine = clearLine;
        invalidate();
    }

    /**
     * 确认画框的位置 :当前ViewpreviewLayout.this->到指定的区域
     */
    private void setTargetSize() {
        int srcWidth = mGroupSrcRect.width(), srcHeight = mGroupSrcRect.height();
        float left = (srcWidth * videoRectF.left);
        float top = (srcHeight * videoRectF.top);
        float width = (srcWidth * videoRectF.width());
        float height = (srcHeight * videoRectF.height());
        rectVideo.set(left, top, left + width, top + height);


        //取整:防止多个画框产生边框线间隙Math.ceil(4.5) =5.0
        LayoutParams lpview = new LayoutParams((int) Math.ceil(rectVideo.width()), (int) Math.ceil(rectVideo.height()));
        lpview.leftMargin = (int) (rectVideo.left);
        lpview.topMargin = (int) (rectVideo.top);
//        Log.e(TAG, "setTargetSize: " + rectVideo.toShortString() + "....." + getWidth() + "*" + getHeight() + "...." + this.toString());
        this.setLayoutParams(lpview);


        //待处理，防止延时造成的UI闪烁
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                VideoPreviewLayout.this.requestLayout();
                setVisibility(View.VISIBLE);
            }
        }, 30);

    }

    private boolean viewPrepared = false;

    //单个视频在player中的显示位置
    private RectF rectVideo = new RectF();


    //当前容器的原始定点坐标
    private Rect mGroupSrcRect = new Rect();

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        Log.e(TAG, "onLayout: " + changed + "..." + left + "..." + top + "..." + right + "..." + bottom + "...bIsFirst:" + bIsFirst + "..videoRectF:" + (null != videoRectF ? videoRectF.toShortString() : "null") + "..id:" + this.toString());
        if (changed) {
            if (bIsFirst) {
                bIsFirst = false;
                mGroupSrcRect.set(left, top, right, bottom);
            }
            viewPrepared = true;
            if (null != videoRectF) {
                if (needChangSize) {
                    needChangSize = false;
                    setTargetSize();
                }
            }
        }

    }


    private Paint mPaint = new Paint();


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
//        Log.e(TAG, "dispatchDraw: " + rectVideo.toShortString() + "/...." + (rectVideo.width() / getWidth()) + "*" + (rectVideo.height() / getHeight()) + ".....>left:" + (rectVideo.left / getWidth()) + "_" + (rectVideo.top / getHeight()) + "xxxxxxx" + this.toString());
        //是否画边框线
        if (!bClearBorderLine) {
//                canvas.drawRect(new RectF(0, 0, rectVideo.width(), rectVideo.height()), mPaint);
            Path path = new Path();
            path.moveTo(0, 0);
            path.lineTo(rectVideo.width(), 0);
            path.lineTo(rectVideo.width(), rectVideo.height());
            path.lineTo(0, rectVideo.height());
            path.lineTo(0, 0);
            path.close();
            canvas.drawPath(path, mPaint);

        }
//        canvas.drawColor(getResources().getColor(R.color.green));
    }

    private boolean bIsFirst = true;
    private Rect rectFull = new Rect();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int previewHeight = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
        rectFull.set(getLeft(), getTop(), getRight(), getBottom());
    }


    public MixItemHolder getHolder() {
        return holder;
    }

    public void setHolder(MixItemHolder holder) {
        this.holder = holder;
    }

    private MixItemHolder holder = null;

    /**
     * 获取组件宽高比
     *
     * @return
     */
    public float getAspectRatio() {
        return getWidth() / (getHeight() + 0.0f);
    }

}
