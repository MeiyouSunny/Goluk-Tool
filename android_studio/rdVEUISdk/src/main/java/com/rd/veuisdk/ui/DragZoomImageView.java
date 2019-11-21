package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;

import java.util.List;

/**
 * 图片缩放和拖动类
 * 所有坐标均相对于此组件的左上角(0,0)参与运算
 */
public class DragZoomImageView extends View {

    private String TAG = "DragZoomImageView";
    /**
     * 记录是拖拉照片模式还是放大缩小照片模式
     */
    private int mMode = 0;// 初始状态
    /**
     * 拖拉照片模式
     */
    private static final int MODE_DRAG = 1;
    /**
     * 放大缩小照片模式
     */
    private static final int MODE_ZOOM = 2;

    /**
     * 用于记录开始时候的坐标位置
     */
    private PointF startPoint = new PointF();
    /**
     * 用于记录拖拉图片移动的坐标位置
     */
    private Matrix matrix = new Matrix();
    //放大过程中，最大放大两倍，相对于原始size
    private Matrix tmpZoomMatrix = new Matrix();
    private final float MAX_SCALE = 10.0f;
    /**
     * 记录按下时的矩阵信息
     */
    private Matrix mDownMatrix = new Matrix();

    /**
     * 两个手指的开始距离
     */
    private float startDis;
    /**
     * 两个手指的中间点
     */
    private PointF midPoint;

    private float nRotateAngle = 0;
    //真实的像素
    private float[] clipMatrixValue = null;
    private int nBorderWidth = 5;
    private int mShadowColor;
    private int nBorderColor;
    private int nHalfBorderWidth;

    public DragZoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        nBorderWidth = CoreUtils.dpToPixel(1.5f);
        nHalfBorderWidth = nBorderWidth / 2;
        mShadowColor = getResources().getColor(R.color.transparent_black80);
        nBorderColor = getResources().getColor(R.color.main_orange);
    }

    public DragZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragZoomImageView(Context context) {
        this(context, null);
    }

    public Bitmap getBmp() {
        return mBmp;
    }

    private Bitmap mBmp;


    /***
     * bitmap的原始宽高
     */
    private RectF bmpRect = new RectF();
    /***
     * 此容器的宽高
     */
    private RectF viewRect = new RectF();
    private Matrix mMatrix = new Matrix();


    /**
     * 旋转
     *
     * @param angle 旋转角度
     */
    public void setRotateAngle(float angle) {
        fixAngle(angle);
        setDefaultMatrix();
    }

    public Bitmap getMask() {
        return mask;
    }

    private Bitmap mask;

    /**
     * 设置原始
     *
     * @param bmp   原始bitmap (没有旋转角度)
     * @param angle 旋转角度
     */
    public void setBitmap(Bitmap bmp, int angle, Bitmap shadow) {
        mBmp = bmp;
        if (null != mBmp) {
            //没有设置有效的clip区域，默认保持比例裁剪，显示最多的视频内容
            bmpRect.set(0, 0, mBmp.getWidth(), mBmp.getHeight());
        }
        mask = shadow;
        fixAngle(angle);
    }

    /**
     * 交换格子数据后 ，替换图片，且之前保留的裁剪参数全部清理（仅保留旋转）
     *
     * @param bmp 新的图片内容
     */
    public void reSetBitmap(Bitmap bmp) {
        mBmp = bmp;
        hideBmp = false;
        if (null != mBmp) {
            //没有设置有效的clip区域，默认保持比例裁剪，显示最多的视频内容
            bmpRect.set(0, 0, mBmp.getWidth(), mBmp.getHeight());
        }
//        Log.e(TAG, "reSetBitmap: " + this + mBmp.getWidth() + "*" + mBmp.getHeight() + ">" + bmpRect + " >" + viewRect);
        setDefaultMatrix();
        invalidate();
    }


    /**
     * 当前旋转角度
     *
     * @return
     */
    public float getRotateAngle() {
        return nRotateAngle;
    }


    private void fixAngle(float angle) {
        nRotateAngle = angle % 360;
    }

    /**
     * 横的
     *
     * @return true  横的；false 竖的
     */
    private boolean isHor() {
        return nRotateAngle == 90 || nRotateAngle == 270;
    }

    /**
     * 设置矩阵信息
     */
    public void setDefaultMatrix() {
        if (null != mBmp) {
            float bw = bmpRect.width();
            float bh = bmpRect.height();
            if (isHor()) {
                //宽高颠倒
                float tmp = bw;
                bw = bh;
                bh = tmp;
            }
            //没有设置有效的clip区域，默认保持比例裁剪，显示最多的视频内容
            Matrix tmp = new Matrix();

            float vW = viewRect.width(), vH = viewRect.height();
            float scaleW = (vW / (bw + 0.f)), scaleH = vH / (bh + 0f);
            float scale = Math.max(scaleW, scaleH);

            {
                //保证旋转后的bmp的中间部分完全放入到view中
                tmp.setScale(scale, scale);
            }

            //平移依赖于原始后的宽高
            float dstW = mBmp.getWidth() * scale, dstH = mBmp.getHeight() * scale;
            {
                float dx = (vW - dstW) / 2.0f;
                float dy = (vH - dstH) / 2.0f;
                tmp.postTranslate(dx, dy);
            }

            //旋转之前转化size
            tmp.mapRect(mBmpScaledRectF, bmpRect);

//            Log.e(TAG, "setDefaultMatrix: " + bmpRect + " mBmpScaledRectF:" + mBmpScaledRectF + " viewRect:" + viewRect);
            {
                float px = viewRect.centerX();
                float py = viewRect.centerY();
                //旋转中心点就是view的中心点
                tmp.postRotate(nRotateAngle, px, py);
            }
            mMatrix.set(tmp);
            matrix.set(mMatrix);
            invalidate();

        }
    }

    private Matrix mBackupMatrix;


    private int nLastWidth, nLasetHeight;

    /**
     * 备份当前矩阵信息
     */
    public void onBackupMatrixValue() {
        mBackupMatrix = new Matrix(mMatrix);
        nLastWidth = getWidth();
        nLasetHeight = getHeight();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            int width = getWidth();
            int height = getHeight();
            viewRect.set(0, 0, width, height);
            if (null != mBmp) {
                if (null != clipMatrixValue) {
                    //设置了有效的clip区域
                    setClipImp(clipMatrixValue);
                    clipMatrixValue = null;
                } else {
                    if (null == mBackupMatrix) {
                        //没有设置有效的clip区域，默认保持比例裁剪，显示最多的视频内容
                        setDefaultMatrix();
                    } else {
                        {
                            {
                                //根据新的宽高偏移（保证可视范围的中心点不动）
                                float dx = (getWidth() - nLastWidth) / 2.0f;
                                float dy = (getHeight() - nLasetHeight) / 2.0f;
                                if (dx != 0 || dy != 0) {
                                    mBackupMatrix.postTranslate(dx, dy);
                                }
                            }
                            {
                                //保证bitmap完整显示
                                mBackupMatrix.mapRect(mBmpScaledRectF, bmpRect);
                                float bw = mBmpScaledRectF.width();
                                float bh = mBmpScaledRectF.height();
                                float vW = viewRect.width(), vH = viewRect.height();
                                float scaleW = (vW / (bw + 0.f)), scaleH = vH / (bh + 0f);
                                float scale = Math.max(scaleW, scaleH);
                                {
                                    if (scale > 1) {
                                        //保证bmp的中间部分完全放入到view中
                                        mBackupMatrix.postScale(scale, scale, mBmpScaledRectF.centerX(), mBmpScaledRectF.centerY());
                                    }
                                }
                            }
                        }
                        mMatrix.set(mBackupMatrix);
                        mBackupMatrix = null;
                        invalidate();
                    }
                }
            }
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                default: {
                }
                break;
            }
        }
    };

    private boolean isLongTouch = false;  //长按：移动、缩放

    private OnClickListener mOnClickListener;

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    /**
     * 按下
     */
    private void onActionDown(float downX, float downY) {
        // 记录ImageView当前的移动位置
        mDownMatrix.set(mMatrix);
        matrix.set(mMatrix);
        startPoint.set(downX, downY);
    }

    /**
     * 拖动
     */
    private void onDrag(float newX, float newY) {
        float dx = newX - startPoint.x; // 得到x轴的移动距离
        float dy = newY - startPoint.y; // 得到x轴的移动距离
        // 在没有移动之前的位置上进行移动
        matrix.set(mDownMatrix);
        //如果越界，最终action_up时会修正
        if (dx != 0 || dy != 0) {
            matrix.postTranslate(dx, dy);
            updateUI();
        }

    }

    /**
     * 缩放
     */
    private boolean onZoom(MotionEvent event) {
        // 放大缩小图片
        float endDis = distance(event);// 结束距离
        if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
            float scale = endDis / startDis;// 得到缩放倍数
            {
                tmpZoomMatrix.set(mDownMatrix);
                tmpZoomMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
                //临时的放大区域，用来判断缩放是否越界
                RectF tmpScaleRectF = new RectF();
                tmpZoomMatrix.mapRect(tmpScaleRectF, bmpRect);

                float tScaleW = tmpScaleRectF.width() / (bmpRect.width() + 0.0f);
                float tScaleH = tmpScaleRectF.height() / (bmpRect.height() + 0.0f);

//                Log.e(TAG, "onZoom: " + " bmpRect:" + bmpRect + " viewRect:"
//                        + viewRect + " tmpScaleRectF:" + tmpScaleRectF + "  tScaleW:" + tScaleW + " tScaleH:" + tScaleH + "  scale:" + scale);
                if (tmpScaleRectF.width() >= viewRect.width() && tmpScaleRectF.height() >= viewRect.height() && Math.min(tScaleW, tScaleH) <= MAX_SCALE) {
                    //最小与viewRect的宽高一致
                    matrix.set(tmpZoomMatrix);
                    updateUI();
                }
            }
        }

        return true;
    }

    private boolean isOutSided = false;

    public boolean isOutSide() {
        return isOutSided;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
//        Log.e(TAG, "onTouchEvent: " + event + ">" + this);
        if (null == mBmp) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN: {
                isLongTouch = false;
                mMode = 0;
                isOutSided = false;
                mHandler.removeCallbacks(mRoundable);
                if (pointInPath(path, (int) event.getX(), (int) event.getY())) {
                    onActionDown(event.getX(), event.getY());
                    mOnClickListener.onClick(this);
                } else {
                    return false;
                }
            }
            break;
            // 手指在屏幕上移动，改事件会被不断触发
            case MotionEvent.ACTION_MOVE: {
                isLongTouch = true;
                if (mMode == 0) {
                    mMode = MODE_DRAG;
                }
                mHandler.removeCallbacks(mRoundable);

                int pointX = (int) event.getX();
                int pointY = (int) event.getY();


                //是否已经越界 （越界了，把bitmap交由父容器替换画框）
                isOutSided = (0 > pointX || getWidth() < pointX || 0 > pointY || getHeight() < pointY);

                if (pointInPath(path, (int) event.getX(), (int) event.getY())) {
                    // 拖拉图片
                    if (mMode == MODE_DRAG) {
                        onDrag(event.getX(), event.getY());
                    } else if (mMode == MODE_ZOOM) {
                        return onZoom(event);
                    }
                } else {
                    if (!updateUI()) {
                        //没完全在画框内，需要修正
                        mHandler.removeCallbacks(mRoundable);
                        mHandler.postDelayed(mRoundable, 200);
                    }
                    return false;
                }
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                // 手指离开屏幕
                mMode = 0;
                isOutSided = false;
                if (null != mOnClickListener && !isLongTouch) {
                    mOnClickListener.onClick(this);
                } else {
                    if (!updateUI()) {
                        //没完全在画框内，需要修正
                        mHandler.removeCallbacks(mRoundable);
                        mHandler.postDelayed(mRoundable, 200);
                    }
                }
            }

            break;
            case MotionEvent.ACTION_POINTER_UP: {
                // 当触点离开屏幕，但是屏幕上还有触点(手指)
                mMode = MODE_DRAG;
                if (!updateUI()) {
                    //没完全在画框内，需要修正
                    mHandler.removeCallbacks(mRoundable);
                    mHandler.postDelayed(mRoundable, 200);
                }
                if (true) {
                    //退出缩放模式就必须
                    return false;
                }
            }
            break;
            // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
            case MotionEvent.ACTION_POINTER_DOWN: {
                mMode = MODE_ZOOM;
                /** 计算两个手指间的距离 */
                startDis = distance(event);
                /** 计算两个手指间的中间点 */
                midPoint = mid(event);
                mDownMatrix.set(mMatrix);
            }
            break;
            default:
                break;
        }
        return true;
    }

    /**
     * 是否在画框范围
     *
     * @return true 缩略图再画框范围内；false  缩略图没在画框范围
     */
    private boolean updateUI() {
        matrix.mapRect(mBmpScaledRectF, bmpRect);
        boolean re = mBmpScaledRectF.contains(viewRect);
//        Log.e(TAG, "updateUI:  bmpRect : " + bmpRect + " viewRect:" + viewRect + "   mBmpScaledRectF:" + mBmpScaledRectF + ">>" + re);
        mMatrix.set(matrix);
        invalidate();
        return re;

    }


    private Runnable mRoundable = new Runnable() {
        @Override
        public void run() {
            fixClipRectF();
        }
    };


    private boolean fixMatrix(Matrix tmp) {
        float offPX = viewRect.right - mBmpScaledRectF.right;
        if (offPX > 0) {
        } else {
            offPX = viewRect.left - mBmpScaledRectF.left;
            if (offPX > 0) {
                offPX = 0;
            }
        }
        float offPY = viewRect.bottom - mBmpScaledRectF.bottom;
        if (offPY > 0) {
        } else {
            offPY = viewRect.top - mBmpScaledRectF.top;
            if (offPY > 0) {
                offPY = 0;
            }
        }
//        Log.e(TAG, "fixMatrix:  bmpRect : " + bmpRect + " viewRect:" + viewRect + "   mBmpScaledRectF:" + mBmpScaledRectF + "  "
//                + mBmpScaledRectF.width() + "*" + mBmpScaledRectF.height());
        if (offPX != 0 || offPY != 0) {
            tmp.postTranslate(offPX, offPY);
            tmp.mapRect(mBmpScaledRectF, bmpRect);
            return true;
        }
        return false;
    }

    /**
     * 缩略图归位到画框中（保证画框内被缩略图完全填充）
     */
    private void fixClipRectF() {
        boolean re = fixMatrix(matrix);
        mMatrix.set(matrix);
        if (re) {
            invalidate();
        }
    }


    /***
     *
     * 旋转之后要保留的区域
     * 要保留的区域在原始图片的相对位置 (0~1.0f)
     *
     * @return
     */
    public RectF getClip() {

        RectF imageScaled = new RectF();
        mMatrix.mapRect(imageScaled, bmpRect);
//        Log.e(TAG, "getClip: " + " bmpRect:" + bmpRect + " viewRect:" + viewRect + " imageScaled:" + imageScaled);
        if (!imageScaled.isEmpty() && !viewRect.isEmpty()) {
            float tw = imageScaled.width();
            float th = imageScaled.height();
            float left = (viewRect.left - imageScaled.left);
            float top = (viewRect.top - imageScaled.top);
            float right = (viewRect.right - imageScaled.left);
            float bottom = (viewRect.bottom - imageScaled.top);

            RectF tmp = new RectF(Math.max(0, left / tw), Math.max(0, top / th), Math.min(1, right / tw), Math.min(1, bottom / th));
//            Log.e(TAG, "getClip: "+tmp );
            return tmp;
        }
        return null;

    }

    /**
     * 当前matrix
     *
     * @return
     */
    public float[] getMatrixValue() {
        float[] values = new float[9];
        mMatrix.getValues(values);
        return values;
    }


    /**
     * 矩阵信息
     */
    private void setClipImp(float[] clipMatrixValue) {
        Matrix matrix = new Matrix();
        if (null != clipMatrixValue) {
            matrix.setValues(clipMatrixValue);
        }
        mMatrix.set(matrix);

    }


    //bitmap 缩放平移之后的位置 (所有坐标)
    private RectF mBmpScaledRectF = new RectF();


    /**
     * 计算两个手指间的距离
     */
    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 计算两个手指间的中间点
     */
    private PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void setPointFList(List<PointF> pointFList) {
        mPointFList = pointFList;
    }

    private List<PointF> mPointFList;

    public Path getPath() {
        return path;
    }

    private Path path = new Path();

    private boolean pointInPath(Path path, int pointX, int pointY) {
        if (null != mask) { //异形
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);
            Region region = new Region();
            region.setPath(path, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));
            if (bounds.isEmpty()) {
                return true;
            }
            return region.contains(pointX, pointY);
        } else {
            //矩形
            return true;
        }
    }

    /**
     * 是否需要隐藏bmp
     *
     * @param hideBmp
     */
    public void setHideBmp(boolean hideBmp) {
        this.hideBmp = hideBmp;
        invalidate();
    }

    private boolean hideBmp = false;


    @Override
    protected void onDraw(Canvas canvas) {
        if (null != mBmp && matrix != null) {
            int vw = getWidth();
            int vh = getHeight();
            if (null == mask) {
                if (!hideBmp) {
                    canvas.drawBitmap(mBmp, mMatrix, null);
                    path.reset();
                    path.moveTo(nHalfBorderWidth, nHalfBorderWidth);
                    path.lineTo(nHalfBorderWidth, vh - nHalfBorderWidth);
                    path.lineTo(vw - nHalfBorderWidth, vh - nHalfBorderWidth);
                    path.lineTo(vw - nHalfBorderWidth, nHalfBorderWidth);
                    path.lineTo(nHalfBorderWidth, nHalfBorderWidth);
                    path.close();
                    if (bIsShadowMode) {
                        canvas.drawColor(mShadowColor);
                    } else {
                        paint.reset();
                        {
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(nBorderWidth);

                            paint.setColor(nBorderColor);
                            canvas.drawPath(path, paint);
                        }
                    }
                }
            } else {
                paint.reset();

                if (!hideBmp) {
                    int layerID = canvas.saveLayer(0, 0, getWidth(), getHeight(), paint, Canvas.ALL_SAVE_FLAG);
                    //一次绘制图层
                    canvas.drawBitmap(mBmp, mMatrix, paint);
                    //设置图层混合模式
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                    canvas.drawBitmap(mask, new Rect(0, 0, mask.getWidth(), mask.getHeight()), new Rect(0, 0, getWidth(), getHeight()), paint);
                    canvas.restoreToCount(layerID);

                    if (null != mPointFList) {
                        paint.reset();

                        int len = mPointFList.size();

                        PointF pointF = mPointFList.get(0);


                        path.reset();
                        path.moveTo((pointF.x * vw) + nHalfBorderWidth, (pointF.y * vh) + nHalfBorderWidth);
                        for (int i = 1; i < len; i++) {
                            pointF = mPointFList.get(i);
                            path.lineTo((pointF.x * vw), (pointF.y * vh));
                        }

                        pointF = mPointFList.get(0);
                        path.lineTo((pointF.x * vw) + nHalfBorderWidth, (pointF.y * vh) + nHalfBorderWidth);
                        path.close();


                        //阴影
                        if (bIsShadowMode) {
                            paint.setColor(mShadowColor);
                        } else {
                            paint.setColor(nBorderColor);
                            paint.setStrokeWidth(nBorderWidth);
                            paint.setStyle(Paint.Style.STROKE);
                        }
                        canvas.drawPath(path, paint);
                    }
                }
            }
        }
    }


    //是否显示遮罩
    private boolean bIsShadowMode = false;

    public void setShadowMode(boolean isShadow) {

        bIsShadowMode = isShadow;
        invalidate();
    }
}
