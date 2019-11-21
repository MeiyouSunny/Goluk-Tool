package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 多格拖拽切换画框内容
 */
public class ExtDragLayout extends FrameLayout {

    private int nBorderWidth;

    public ExtDragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        nBorderWidth = CoreUtils.dpToPixel(1.5f);
        mDstRectBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDstRectBorderPaint.setStyle(Paint.Style.STROKE);
        mDstRectBorderPaint.setStrokeWidth(nBorderWidth);
        mDstRectBorderPaint.setAntiAlias(true);
        mDstRectBorderPaint.setColor(getResources().getColor(R.color.main_orange));
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(100);
    }

    private String TAG = "ExtDragLayout";


    private List<RectInfo> mList = new ArrayList<>();

    /**
     * 每个格子的内容
     */
    class RectInfo {

        public RectInfo(VideoPreviewLayout layout, Path path, Rect rect, RectF rectF) {
            mLayout = layout;
            mRect = rect;
            mPath = path;
            mClipRectF = rectF;
        }

        public VideoPreviewLayout getLayout() {
            return mLayout;
        }


        private VideoPreviewLayout mLayout;

        public Path getPath() {
            return mPath;
        }

        //画框的位置 ：像素
        private Path mPath;


        public RectF getClipRectF() {
            return mClipRectF;
        }

        private RectF mClipRectF;

        public Rect getRect() {
            return mRect;
        }

        private Rect mRect;

        @Override
        public String toString() {
            return "RectInfo{" +
                    "mLayout=" + mLayout +
                    ", mPath=" + mPath +
                    ", mRect=" + mRect +
                    '}';
        }
    }

    private boolean pointInPath(Path path, Point point) {
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        Region region = new Region();
        region.setPath(path, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));
        return region.contains(point.x, point.y);
    }

    //当前长按的对象
    private RectInfo mTouchedRect;

    /**
     * 获取down时按住的格子
     */
    private void initRectList() {
        mList.clear();
        //多格画框
        int len = getChildCount();
        for (int i = 0; i < len; i++) {
            VideoPreviewLayout videoPreviewLayout = (VideoPreviewLayout) getChildAt(i);
            Path path = videoPreviewLayout.getDragZoomImageView().getPath();
            path = new Path(path);
            //每个画框的顶点位置 像素
            path.offset(videoPreviewLayout.getLeft(), videoPreviewLayout.getTop());
            mList.add(new RectInfo(videoPreviewLayout, path, new Rect(videoPreviewLayout.getLeft(), videoPreviewLayout.getTop(), videoPreviewLayout.getRight(),
                    videoPreviewLayout.getBottom()), videoPreviewLayout.getDragZoomImageView().getClip()));
        }
        //当前位置选中的小画框内容
        Point point = new Point(mLastXPosition, mLastYPosition);
        for (int i = 0; i < len; i++) {
            if (pointInPath(mList.get(i).getPath(), point)) {
                mTouchedRect = mList.get(i);
                break;
            }
        }
    }

    private int mLastXPosition = -1, mLastYPosition = -1;

    private Paint mPaint = new Paint();
    private boolean enableDrawBmp = false;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (enableDrawBmp) {
            if (null != dragDstRect) {
                canvas.drawPath(dragDstRect.getPath(), mDstRectBorderPaint);
            }
            if (null != mTouchedRect) {
                //隐藏组件内的绘制bmp
                mTouchedRect.getLayout().getDragZoomImageView().setHideBmp(true);
                Bitmap bitmap = mTouchedRect.getLayout().getDragZoomImageView().getBmp();
                Rect dst = new Rect(mTouchedRect.getRect());
                dst.offset(pressX - mLastXPosition, pressY - mLastYPosition);
                Rect rectF = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                RectF clipRectF = mTouchedRect.getClipRectF();
                if (null == clipRectF || clipRectF.isEmpty()) {
                    canvas.drawBitmap(bitmap, rectF, dst, mPaint);
                } else {
                    //与之前设置的裁剪一样，看着更友好点
                    Rect clipPx = new Rect((int) (rectF.width() * clipRectF.left), (int) (clipRectF.top * rectF.height()), (int) (clipRectF.right * rectF.width()), (int) (clipRectF.bottom * rectF.height()));
                    canvas.drawBitmap(bitmap, clipPx, dst, mPaint);
                }
            }
        }
    }

    //要替换的目标画框
    private RectInfo dragDstRect = null;

    public void getFourceRect() {
        dragDstRect = null;
        int len = mList.size();
        Point point = new Point(pressX, pressY);
        RectInfo rectInfo = null;
        for (int i = 0; i < len; i++) {
            rectInfo = mList.get(i);
            Path path = rectInfo.getPath();
            if (pointInPath(path, point) && rectInfo != mTouchedRect) {
                dragDstRect = rectInfo;  //目标画框
                break;
            }
        }
    }


    private int pressX = 0, pressY;


    private Paint mDstRectBorderPaint = null;
    //是否禁用拖拽监听功能 （两个手指时，需要禁用）
    private boolean disableDragMonitor = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch ((action & MotionEvent.ACTION_MASK)) {
            case MotionEvent.ACTION_DOWN: {
                disableDragMonitor = false;
                mLastXPosition = (int) event.getX();
                mLastYPosition = (int) event.getY();
                pressX = (int) event.getX();
                pressY = (int) event.getY();
                //选中的画框
                initRectList();
                dragDstRect = null;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                pressX = (int) event.getX();
                pressY = (int) event.getY();
                if (!disableDragMonitor) {
                    if (null != mTouchedRect) {
                        enableDrawBmp = mTouchedRect.getLayout().getDragZoomImageView().isOutSide();
                        //单手指拖拽时，才交换格子数据
                        if (enableDrawBmp) {
                            //已经越界
                            mTouchedRect.getLayout().getDragZoomImageView().setHideBmp(true);
                        } else {
                            //已经越界
                            dragDstRect = null;
                            mTouchedRect.getLayout().getDragZoomImageView().setHideBmp(false);
                        }
                    }
                    getFourceRect();
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                //双手指，此处开始不再执行 交换格子内容的逻辑和提示
                disableDragMonitor = true;
                enableDrawBmp = false;
                invalidate();
            }
            break;
            case MotionEvent.ACTION_POINTER_UP: {
                disableDragMonitor = false;
                enableDrawBmp = false; //防止马上MotionEvent.ACTION_UP，执行到交换数据 （必须单手有滑动才执行交换）
                invalidate();
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (null != dragDstRect && null != mTouchedRect && mTouchedRect != dragDstRect && enableDrawBmp) {
                    //交换两个格子的数据
                    onChangeItem(mTouchedRect, dragDstRect);
                    enableDrawBmp = false;
                    invalidate();
                    //不再向DragZoomImageview中传递touch
                    return false;
                } else {
                    enableDrawBmp = false;
                    if (null != mTouchedRect) {
                        mTouchedRect.getLayout().getDragZoomImageView().setHideBmp(false);
                    }
                    invalidate();
                }
                break;
            }
            default:
                break;

        }
        return super.dispatchTouchEvent(event);
    }


    /**
     * 交换两个格子的数据
     *
     * @param from 按住的格子
     * @param to   目标格子
     */
    private void onChangeItem(RectInfo from, RectInfo to) {
        DragZoomImageView fromView = from.getLayout().getDragZoomImageView();
        Bitmap tmp = fromView.getBmp();
        DragZoomImageView toView = to.getLayout().getDragZoomImageView();
        fromView.reSetBitmap(toView.getBmp());
        toView.reSetBitmap(tmp);
        if (null != mListener) {
            mListener.onChangeItem(from.getLayout(), to.getLayout());
        }
    }

    public void setListener(IDragChangeListener listener) {
        mListener = listener;
    }

    private IDragChangeListener mListener;

    public static interface IDragChangeListener {

        /**
         * 交换集合中绑定的资源
         *
         * @param from
         * @param to
         */
        void onChangeItem(VideoPreviewLayout from, VideoPreviewLayout to);
    }

}
