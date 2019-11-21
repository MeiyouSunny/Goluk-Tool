package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.rd.veuisdk.model.SpliceGridMediaInfo;


/**
 * 单个格子的位置
 */
public class VideoPreviewLayout extends FrameLayout {
    private String TAG = "VideoPreviewLayout";
    private RectF videoRectF = null;

    /***
     *
     * @param context
     * @param attrs
     */
    public VideoPreviewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 在指定位置添加一個view ,且设置后更改父容器的大小，且固定
     *
     * @param rectF
     */
    public void setCustomRect(RectF rectF) {
        videoRectF = new RectF(rectF);
        needChangSize = true;
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
        videoRectF = new RectF(rectF);
//        Log.e(TAG, "resetChildSize: " + videoRectF + "..." + viewPrepared);
        if (viewPrepared) {
            setTargetSize();
        } else {
            needChangSize = true;
        }
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

//        Log.e(TAG, "setTargetSize:  videoRectF: " + videoRectF + "  mGroupSrcRect:" + mGroupSrcRect + "rectVideo: " + rectVideo + "...." +
//                "." + getWidth() + "*" + getHeight());

        //取整:防止多个画框产生边框线间隙Math.ceil(4.5) =5.0
        LayoutParams lpview = new LayoutParams((int) Math.ceil(rectVideo.width()), (int) Math.ceil(rectVideo.height()));
        lpview.leftMargin = (int) (rectVideo.left);
        lpview.topMargin = (int) (rectVideo.top);
        this.setLayoutParams(lpview);


    }

    private boolean viewPrepared = false;

    //单个视频在player中的显示位置
    private RectF rectVideo = new RectF();


    //当前容器的原始定点坐标
    private Rect mGroupSrcRect = new Rect();


    public void setIsFirst(boolean bIsFirst) {
        this.bIsFirst = bIsFirst;
    }

    private boolean bIsFirst = true;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int previewHeight = MeasureSpec.getSize(heightMeasureSpec);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));

//        android.util.Log.e(TAG, "onMeasure: " + bIsFirst + ">" + previewWidth + "*" + previewHeight);
        if (bIsFirst) {
            bIsFirst = false;
            mGroupSrcRect.set(0, 0, previewWidth, previewHeight);
            viewPrepared = true;
            if (null != videoRectF) {
                if (needChangSize) {
                    needChangSize = false;
                    setTargetSize();
                }
            }
        }


    }


    private SpliceGridMediaInfo mSpliceGridMediaInfo;


    public SpliceGridMediaInfo getBindGrid() {
        return mSpliceGridMediaInfo;
    }

    public void setBindGrid(SpliceGridMediaInfo mixMediaInfo) {
        mSpliceGridMediaInfo = mixMediaInfo;
    }

    public DragZoomImageView getDragZoomImageView() {
        return mDragZoomImageView;
    }

    public void setDragZoomImageView(DragZoomImageView dragZoomImageView) {
        mDragZoomImageView = dragZoomImageView;
    }

    private DragZoomImageView mDragZoomImageView;

}
