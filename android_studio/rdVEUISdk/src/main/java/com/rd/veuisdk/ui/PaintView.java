package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.rd.lib.utils.BitmapUtils;
import com.rd.lib.utils.CoreUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 涂鸦view
 */
public class PaintView extends View {

    private int view_width = 0;//屏幕的宽度
    private int view_height = 0;//屏幕的高度
    private float preX;//起始点的x坐标
    private float preY;//起始点的y坐标
    private Path path;//路径
    private boolean mCanDraw = false;

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DisplayMetrics displayMetrics = CoreUtils.getMetrics();
        view_width = displayMetrics.widthPixels;
        view_height = displayMetrics.heightPixels;
        initView(); // 初始化
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            view_width = getWidth();
            view_height = getHeight();
        }
    }


    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    //2~10;
    private int mStrokeWidth = 5;
    private int mColor = Color.RED;
    private int mAlpha;

    /**
     * 涂鸦颜色
     */
    public void setPaintColor(int color) {
        mColor = color;
    }

    /**
     * 透明度
     *
     * @param alpha 0~1f
     */
    public void setAlpha(float alpha) {
        mAlpha = (int) (alpha * 255);
    }

    /**
     * 宽度
     *
     * @param strokeWidth 2~100
     */
    public void setStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
    }

    private Paint initPaint() {
        Paint paint = new Paint(Paint.DITHER_FLAG);//Paint.DITHER_FLAG防抖动的
        paint.setColor(mColor);
        //设置画笔风格
        paint.setStyle(Paint.Style.STROKE);//设置填充方式为描边
        paint.setStrokeJoin(Paint.Join.ROUND);//设置笔刷转弯处的连接风格
        paint.setStrokeCap(Paint.Cap.ROUND);//设置笔刷的图形样式(体现在线的端点上)
        paint.setStrokeWidth(mStrokeWidth);//设置默认笔触的宽度为1像素
        paint.setAntiAlias(true);//设置抗锯齿效果
        paint.setDither(true);//使用抖动效果
        paint.setAlpha(mAlpha);
        return paint;
    }

    private void initView() {
        mList = new ArrayList<>();
    }

    public void setCanDraw(boolean canDraw) {
        mCanDraw = canDraw;
    }

    private Paint paint;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawImp(canvas);
    }

    private void drawImp(Canvas canvas) {
        if (mList.size() > 0) {
            int len = mList.size();
            for (int i = 0; i < len; i++) {
                PathInfo pathInfo = mList.get(i);
                canvas.drawPath(pathInfo.mPath, pathInfo.mPaint);
            }
        }
        if (null != path) {
            canvas.drawPath(path, paint);//绘制路径
        }
        canvas.save();//保存canvas的状态
        canvas.restore();
    }

    private List<PathInfo> mList;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取触摸事件发生的位置
        if (!mCanDraw) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                //将绘图的起始点移到(x,y)坐标点的位置
                paint = initPaint();
                path = new Path();
                path.moveTo(x, y);
                preX = x;
                preY = y;
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                float dx = Math.abs(x - preX);
                float dy = Math.abs(y - preY);
                if (dx > 5 || dy > 5) {
                    //.quadTo贝塞尔曲线，实现平滑曲线(对比lineTo)
                    //x1，y1为控制点的坐标值，x2，y2为终点的坐标值
                    path.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2);
                    preX = x;
                    preY = y;
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mList.add(new PathInfo(path, paint));
                path = null;
            }
            break;
        }
        invalidate();
        return true;//返回true,表明处理方法已经处理该事件
    }

    /**
     * 清理全部画面
     */
    public void clear() {
        if (mList.size() > 0) {
            int len = mList.size();
            for (int i = 0; i < len; i++) {
                mList.get(i).recycle();
            }
            invalidate();
        }

    }


    public void save(String filePath) {
        //创建一个与该View相同大小的缓存区
        Bitmap cacheBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
        Canvas cacheCanvas = new Canvas();//创建一个新的画布
        //在cacheCanvas上绘制cacheBitmap
        cacheCanvas.setBitmap(cacheBitmap);
        draw(cacheCanvas);
        try {
            BitmapUtils.saveBitmapToFile(cacheBitmap, true, 100, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 撤销上一步
     */
    public void revoke() {
        if (mList.size() > 0) {
            PathInfo tmp = mList.remove(mList.size() - 1);
            tmp.recycle();
            tmp = null;
            invalidate();
        }
    }

    class PathInfo {
        public PathInfo(Path path, Paint paint) {
            mPath = path;
            mPaint = paint;
        }

        Path mPath;
        Paint mPaint;

        void recycle() {
            mPath.reset();
            mPaint.reset();
        }
    }
}

