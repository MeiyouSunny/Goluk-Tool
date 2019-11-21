package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.rd.lib.utils.BitmapUtils;
import com.rd.vecore.models.FlipType;
import com.rd.veuisdk.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 媒体画中画
 *
 * @author JIAN
 * @create 2019/1/6
 * @Describe
 */
public class DragView extends View {
    private String TAG = "DragView";
    /**
     * 图片的初始状态，就是开始运行的时候的原始状态
     */
    private static final int NONE = 0;

    /**
     * 拖动状态
     */
    private static final int DRAG = 1;

    /**
     * 缩放和旋转状态
     */
    private static final int ZOOM_ROTATE = 4;
    /**
     * 缩放比例区间
     */
    private final float MAX_SCALE = 3.0f;
    private final float MIN_SCALE = 0.5f;

    public int shadowColor = 0;

    /**
     * 旋转之后的图片宽度
     */
    private int mRotatedImageWidth;

    /**
     * 旋转之后图片的高度
     */
    private int mRotatedImageHeight;

    /**
     * 图片的宽度
     */
    private int mImageViewWidth;
    /**
     * 图片的高度
     */
    private int mImageViewHeight;
    /**
     * 图片的左边
     */
    private int mImageViewLeft;
    /**
     * 图片的上边
     */
    private int mImageViewTop;

    /**
     * 当前matrix
     */
    private Matrix mMatrix;


    private FlipType mFlipType;


    /**
     * 父容器大小
     */
    private Point mParentSize;
    /**
     * 默认状态为初始状态
     */
    private int mDefautMode = NONE;

    /**
     * 图片控制点的坐标
     */
    private PointF mAPoint = new PointF();
    /**
     * 图片删除点坐标
     */
    private PointF mBPoint = new PointF();

    /**
     * 原始图片，需要操作的图片
     */
    private Bitmap mOriginalBitmap;
    /**
     * 原始图片,的备份
     */
    private Bitmap mOriginalBackupBitmap;

    /**
     * 删除图片
     */
    private Bitmap mDeleteBitmap;

    /**
     * 可以控制图片旋转伸缩的图片
     */
    private Bitmap mContralBitmap;


    private Bitmap mFlipBitmap;

    /**
     * 画刷
     */
    private Paint mPaint = new Paint();

    /**
     * 图片中心坐标
     */
    private Point mImageCenterPoint = new Point(0, 0);
    /**
     * 旋转角度
     */
    private int mRotateAngle;

    /**
     * 缩放系数
     */
    private float disf = 1f;

    /**
     * 缩放框外面放置删除按钮需要的宽度<br>
     * 用于放2个图标
     */
    private int mOutLayoutImageWidth;

    /**
     * 缩放框外面放置删除按钮需要的高度
     */
    private int mOutLayoutImageHeight;

    /**
     * 镜像图片中心坐标
     */
    private Point mFlipTypeCenterPoint;


    /**
     * * 删除图片中心坐标
     */
    private Point mDeleteImageCenterPoint;

    /**
     * 控制图片中心坐标
     */
    private Point mContralImageCenterPoint;

    /**
     * 边框的左上角顶点坐标
     */
    private Point mPoint1;

    /**
     * 边框的右上角顶点坐标
     */
    private Point mPoint2;

    /**
     * 边框的右下角顶点坐标
     */
    private Point mPoint3;

    /**
     * 边框的左下角顶点坐标
     */
    private Point mPoint4;

    private int dx;
    private int dy;


    /**
     * @param context
     * @param mRotate    旋转角度  （顺时针方向）
     * @param scale      初始化时的缩放比
     * @param parentSize 父容器的大小
     * @param center
     * @param bgPath
     */
    public DragView(Context context, int mRotate, float scale, int[] parentSize, PointF center, String bgPath, FlipType flipType) {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        this.mParentSize = new Point(parentSize[0], parentSize[1]);
        this.mRotateAngle = mRotate;
        // 初始化要操作的原始图片， 先将图片引入到工程中
        this.disf = Math.min(MAX_SCALE, Math.max(MIN_SCALE, scale));
        // 消除锯齿
        this.mPaint.setAntiAlias(true);
        // 初始化删除图片
        mDeleteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_delete_new);
        // 初始化控制图片
        mContralBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_controller_new);

        setFlipType(flipType);
        // 删除图片的半宽
        this.mOutLayoutImageWidth = mDeleteBitmap.getWidth() / 2;
        this.mOutLayoutImageHeight = mDeleteBitmap.getHeight() / 2;
        this.drawFrame(bgPath);
        setCenter(new Point((int) (parentSize[0] * center.x), (int) (parentSize[1] * center.y)));
        setImageStyle(bgPath, false);
    }

    /**
     * @param flipType
     */
    public void setFlipType(FlipType flipType) {
        mFlipType = flipType;
        fixFlipBmp();
    }


    /**
     * 设置显示样式
     *
     * @param picPath
     * @param invalidate
     */
    public void setImageStyle(String picPath, boolean invalidate) {
        drawFrame(picPath);
        invaView(invalidate);
    }

    /**
     * 刷新图片信息的配置，缩放比
     *
     * @param invalidate
     */
    private void invaView(boolean invalidate) {
        setImageViewParams(mOriginalBitmap, mImageCenterPoint, mRotateAngle, disf);
        if (invalidate)
            invalidate();
    }


    private void setCenter(Point center) {
        if (!center.equals(mImageCenterPoint.x, mImageCenterPoint.y)) {
            this.mImageCenterPoint = center;
        }
    }

    /**
     * 更新中心点
     *
     * @param center
     */
    public void update(PointF center) {
        setCenter(new Point((int) (center.x * mParentSize.x), (int) (center.y * mParentSize.y)));
        invalidate();
    }

    public Point getCenter() {
        return mImageCenterPoint;
    }

    public FlipType getFlipType() {
        return mFlipType;
    }

    public float getDisf() {
        return disf;
    }

    public int getRotateAngle() {
        return mRotateAngle;
    }


    private String lastFramePic = "";

    /**
     * 画单独的一帧的画面
     *
     * @param picPath
     */
    private void drawFrame(String picPath) {
//        Log.e(TAG, "drawFrame " + picPath + "  lastFramePic:" + lastFramePic);
        if (!TextUtils.equals(lastFramePic, picPath)) {
            lastFramePic = picPath;
            if (null != mOriginalBitmap && !mOriginalBitmap.isRecycled()) {
                mOriginalBitmap.recycle();
                mOriginalBitmap = null;
                if (null != mOriginalBackupBitmap
                        && !mOriginalBackupBitmap.isRecycled()) {
                    mOriginalBackupBitmap.recycle();
                    mOriginalBackupBitmap = null;
                }
            }
            if (!TextUtils.isEmpty(picPath)) {
                mOriginalBitmap = BitmapFactory.decodeFile(picPath);
            } else {
                Log.e(TAG, "drawFrame pic is null");
            }
            if (null == mOriginalBitmap) {
                // -1的情况
                GradientDrawable gd = new GradientDrawable();// 创建drawable
                gd.setColor(Color.TRANSPARENT);
                gd.setCornerRadius(5);
                if (drawControl) {
                    gd.setStroke(5, Color.parseColor("#85B0E9"));
                } else {
                    gd.setStroke(5, Color.TRANSPARENT);
                }
                mOriginalBitmap = BitmapUtils
                        .drawableToBitmap(gd, 400, 60);
            }
            mOriginalBackupBitmap = Bitmap.createBitmap(mOriginalBitmap);
        }
    }


    private boolean drawControl = false; // 设置是否支持拖拽(编辑模式下、预览模式下的区别)

    /**
     * 是否可以随意拖动
     *
     * @param isControl
     */
    public void setControl(boolean isControl) {
        drawControl = isControl;
        invalidate();
    }

    private HashMap<Long, Bitmap> maps = new HashMap<Long, Bitmap>();

    @Override
    protected void onDraw(Canvas canvas) {
        mOnDraw(canvas);
    }

    private void clearSomeBitmap() {
        if (maps.size() > 0) {
            for (Map.Entry<Long, Bitmap> item : maps.entrySet()) {
                Bitmap b = item.getValue();
                if (null != b) {
                    if (!b.isRecycled()) {
                        b.recycle();
                    }
                    b = null;
                }
                maps.remove(item.getKey());
            }
            maps.clear();
        }

    }

    private void mOnDraw(Canvas canvas) {

        clearSomeBitmap();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
//        canvas.drawColor(Color.CYAN);// 测试用。查看区域


//        {
//            //测试专用
//            mPaint.setColor(Color.RED);
//            canvas.drawLine(mPoint1.x, mPoint1.y, mPoint2.x, mPoint2.y,
//                    mPaint);
//            canvas.drawLine(mPoint2.x, mPoint2.y, mPoint3.x, mPoint3.y,
//                    mPaint);
//            canvas.drawLine(mPoint3.x, mPoint3.y, mPoint4.x, mPoint4.y,
//                    mPaint);
//            canvas.drawLine(mPoint4.x, mPoint4.y, mPoint1.x, mPoint1.y,
//                    mPaint);
//        }
        if (null == mOriginalBitmap || mOriginalBitmap.isRecycled()) {
            Log.e(TAG, "mOnDraw no has mOriginalBitmap.... ");
            return;
        }
        int bwidth = mOriginalBitmap.getWidth();
        int bheight = mOriginalBitmap.getHeight();


//        Log.e(TAG, "mOnDraw: " + msi.mlocalpath + "   " + bwidth + "*" + bheight + "  >>>" + msi.w + "*" + msi.h + "  >" + isLaShen+"  "+msi);
        Bitmap newb = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);

        Canvas canvasTmp = new Canvas();
        canvasTmp.drawColor(Color.GRAY);
        canvasTmp.setBitmap(newb);
        canvasTmp.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
//        Rect dst = new Rect(0, 0, bwidth, bheight);

        Matrix tmp = new Matrix();

        if (mFlipType == FlipType.FLIP_TYPE_VERTICAL || mFlipType == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL) {
            tmp.postScale(1, -1);   //镜像垂直翻转
            tmp.postTranslate(0, mOriginalBitmap.getHeight());
        }
        if (mFlipType == FlipType.FLIP_TYPE_HORIZONTAL || mFlipType == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL) {
            tmp.postScale(-1, 1);   //镜像水平翻转
            tmp.postTranslate(mOriginalBitmap.getWidth(), 0);
        }


        canvasTmp.drawBitmap(mOriginalBitmap, tmp, null);
//        canvasTmp.drawBitmap(mOriginalBitmap, new Rect(0, 0, mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight()), dst, null);
        RectF rectF = new RectF(0f, 0, 1f, 1f);
        if (null != rectF) {

            int mleft = (int) (bwidth * rectF.left);
            int mtop = (int) (bheight * rectF.top);

            int mright = (int) (bwidth * rectF.right);
            int mbottom = (int) (bheight * rectF.bottom);

            // 左上角的坐标
            Point mp11 = new Point(mleft, mtop);

            // 右上角的坐标
            Point mp21 = new Point(mright, mtop);

            // 右下角坐标
            Point mp31 = new Point(mright, mbottom);

            // 左下角坐标
            Point mp41 = new Point(mleft, mbottom);


//            mPaint.setColor(Color.BLACK);
//            canvasTmp.drawLine(mp11.x, mp11.y, mp21.x, mp21.y, mPaint);
//            canvasTmp.drawLine(mp21.x, mp21.y, mp31.x, mp31.y, mPaint);
//            canvasTmp.drawLine(mp31.x, mp31.y, mp41.x, mp41.y, mPaint);
//            canvasTmp.drawLine(mp41.x, mp41.y, mp11.x, mp11.y, mPaint);

            Point centerPoint = intersects(mp41, mp21, mp11, mp31);

            // 转化坐标系
            canvasTmp.translate(centerPoint.x, centerPoint.y);

            // 设置变化（旋转缩放）之后图片的宽高

            setImageViewWH(mRotatedImageWidth, mRotatedImageHeight,
                    (mImageCenterPoint.x - mRotatedImageWidth / 2),
                    (mImageCenterPoint.y - mRotatedImageHeight / 2));


            canvas.drawBitmap(newb, mMatrix, mPaint);
            maps.put(System.currentTimeMillis(), newb);
        }

        if (drawControl) {
            // 只有在调节字幕界面。画控制器
            mPaint.setStrokeWidth(4);
            mPaint.setColor(Color.WHITE);
            // 画图片的包围框 ，顺时针画
            canvas.drawLine(mPoint1.x, mPoint1.y, mPoint2.x, mPoint2.y,
                    mPaint);
            canvas.drawLine(mPoint2.x, mPoint2.y, mPoint3.x, mPoint3.y,
                    mPaint);
            canvas.drawLine(mPoint3.x, mPoint3.y, mPoint4.x, mPoint4.y,
                    mPaint);
            canvas.drawLine(mPoint4.x, mPoint4.y, mPoint1.x, mPoint1.y,
                    mPaint);


            //绘制3个按钮图片
            drawControl(canvas, mContralBitmap, mContralImageCenterPoint);
            drawControl(canvas, mFlipBitmap, mFlipTypeCenterPoint);
            drawControl(canvas, mDeleteBitmap, mDeleteImageCenterPoint);
        }

    }

    /**
     * @param canvas
     * @param bmp
     * @param point
     */
    private void drawControl(Canvas canvas, Bitmap bmp, Point point) {
        if (null != bmp && !bmp.isRecycled())
            canvas.drawBitmap(bmp, point.x - mOutLayoutImageWidth, point.y - mOutLayoutImageHeight, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawControl) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    // 先获取点击坐标吗？
                    mAPoint.set(event.getX() + mImageViewLeft, event.getY()
                            + mImageViewTop);
                    // 先判断用户点击的是哪个按钮（图片）, 如果是2，表示要旋转和伸缩图片
                    int checkPosition = getClickPosition((int) event.getX(),
                            (int) event.getY());
                    if (checkPosition == 0) {
                        if (null != listener) {
                            listener.onClick(this);
                        }
                    }
                    if (drawControl) {
                        if (checkPosition == CLICKED_MIRROR) {
                            onNextFlip();
                            invalidate();
                            if (null != mMirrorListener) {
                                mMirrorListener.onMirror(this, mFlipType);
                            }
                        } else if (checkPosition == CLICKED_DELETE) {
                            if (null != onDelListener) {
                                onDelListener.onDelete(this);
                            }
                        } else if (checkPosition == CLICKED_CONTROL) {
                            // 设置操作模式为移动缩放模式
                            mDefautMode = ZOOM_ROTATE;
                        } else {
                            // 设置操作模式为拖动模式
                            mDefautMode = DRAG;
                        }

                    } else {
                        return false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 如果为移动缩放模式
                    if (mDefautMode == ZOOM_ROTATE) {

                        // 记录当前位置
                        mBPoint.set(event.getX() + mImageViewLeft, event.getY()
                                + mImageViewTop);
                        //自定义了默认大小
                        int tmpW = 0, tmpH;
                        tmpW = mOriginalBitmap.getWidth();
                        tmpH = mOriginalBitmap.getHeight();
                        float realL = (float) Math
                                .sqrt((float) (tmpW * tmpW + tmpH * tmpH) / 4);

                        float newL = (float) Math
                                .sqrt((mBPoint.x - (float) mImageCenterPoint.x)
                                        * (mBPoint.x - (float) mImageCenterPoint.x)
                                        + (mBPoint.y - (float) mImageCenterPoint.y)
                                        * (mBPoint.y - (float) mImageCenterPoint.y));


                        // 计算缩放系数，太复杂了。看不懂
                        disf = newL / realL;


                        // 计算旋转角度
                        double a = spacing(mAPoint.x, mAPoint.y,
                                mImageCenterPoint.x, mImageCenterPoint.y);

                        double b = spacing(mAPoint.x, mAPoint.y, mBPoint.x,
                                mBPoint.y);

                        double c = spacing(mBPoint.x, mBPoint.y,
                                mImageCenterPoint.x, mImageCenterPoint.y);

                        double cosB = (a * a + c * c - b * b) / (2 * a * c);

                        if (cosB > 1) {// 浮点运算的时候 cosB 有可能大于1.
                            cosB = 1f;
                        }

                        double angleB = Math.acos(cosB);

                        // 新的旋转角度
                        int newAngle = (int) (angleB / Math.PI * 180);

                        float p1x = mAPoint.x - (float) mImageCenterPoint.x;
                        float p2x = mBPoint.x - (float) mImageCenterPoint.x;

                        float p1y = mAPoint.y - (float) mImageCenterPoint.y;
                        float p2y = mBPoint.y - (float) mImageCenterPoint.y;

                        // 正反向。
                        if (p1x == 0) {
                            if (p2x > 0 && p1y >= 0 && p2y >= 0) {// 由 第4-》第3
                                newAngle = -newAngle;
                            } else if (p2x < 0 && p1y < 0 && p2y < 0) {// 由 第2-》第1
                                newAngle = -newAngle;
                            }
                        } else if (p2x == 0) {
                            if (p1x < 0 && p1y >= 0 && p2y >= 0) {// 由 第4-》第3
                                newAngle = -newAngle;
                            } else if (p1x > 0 && p1y < 0 && p2y < 0) {// 由 第2-》第1
                                newAngle = -newAngle;
                            }
                        } else if (p1x != 0 && p2x != 0 && p1y / p1x < p2y / p2x) {
                            if (p1x < 0 && p2x > 0 && p1y >= 0 && p2y >= 0) {// 由
                                // 第4-》第3
                                newAngle = -newAngle;
                            } else if (p2x < 0 && p1x > 0 && p1y < 0 && p2y < 0) {// 由
                                // 第2-》第1
                                newAngle = -newAngle;
                            } else {

                            }
                        } else {
                            if (p2x < 0 && p1x > 0 && p1y >= 0 && p2y >= 0) {// 由
                                // 第3-》第4

                            } else if (p2x > 0 && p1x < 0 && p1y < 0 && p2y < 0) {// 由
                                // 第1-》第2

                            } else {
                                newAngle = -newAngle;
                            }
                        }

                        mAPoint.x = mBPoint.x;
                        mAPoint.y = mBPoint.y;
                        if (disf <= MIN_SCALE) {
                            disf = MIN_SCALE;
                        } else if (disf >= MAX_SCALE) {
                            disf = MAX_SCALE;
                        }


                        // 设置图片参数
                        setImageViewParams(mOriginalBitmap, mImageCenterPoint,
                                mRotateAngle + newAngle, disf);

                        // 如果为拖动模式
                    } else if (mDefautMode == DRAG) {

                        // 记录当前位置
                        mBPoint.set(event.getX() + mImageViewLeft, event.getY()
                                + mImageViewTop);

                        // 修改中心坐标
                        mImageCenterPoint.x += mBPoint.x - mAPoint.x;
                        mImageCenterPoint.y += mBPoint.y - mAPoint.y;

                        //
                        mAPoint.x = mBPoint.x;
                        mAPoint.y = mBPoint.y;

                        // 设置中心坐标
                        setCenterPoint(mImageCenterPoint);
                    }
                    break;
                case MotionEvent.ACTION_UP:

                    // 设置操作模式为什么都不做
                    mDefautMode = NONE;
                    break;
            }
        } else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (null != listener) {
                    listener.onClick(this);
                }
            }
            return false;
        }
        return true;
    }


    /***
     * 切换镜像
     */
    private void onNextFlip() {

        if (mFlipType == null || mFlipType == FlipType.FLIP_TYPE_NONE) {
            //左右镜像
            mFlipType = FlipType.FLIP_TYPE_HORIZONTAL;
        } else if (mFlipType == FlipType.FLIP_TYPE_HORIZONTAL) {
            //上下左右都镜像
            mFlipType = FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL;
        } else {
            //取消所有镜像效果
            mFlipType = null;
        }
        fixFlipBmp();
    }

    private void fixFlipBmp() {
        if (mFlipType == FlipType.FLIP_TYPE_VERTICAL_HORIZONTAL || mFlipType == FlipType.FLIP_TYPE_VERTICAL) {
            mFlipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flip_type_vertical_new);
        } else {
            mFlipBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flip_type_horizontal_new);
        }
    }

    private Point rotateCenterPoint;


    /**
     * 设置图片的中心点，
     */
    private void setImageViewParams(Bitmap bm, Point centerPoint,
                                    int rotateAngle, float zoomFactor) {
        // 要缩放的原始图片
        mOriginalBitmap = bm;
        // 图片的中心坐标
        mImageCenterPoint = centerPoint;
        // 图片旋转的角度
        mRotateAngle = rotateAngle;
        // 图片缩放系数
        int sbmpW = 100, sbmpH = 50;
        try {
            sbmpW = (int) (mOriginalBitmap.getWidth() * zoomFactor);
            sbmpH = (int) (mOriginalBitmap.getHeight() * zoomFactor);
//            Log.e(TAG, zoomFactor + "...." + isLaShen() + "...." + zoomFactor + "setImageViewParams: " + msi.w + "*" + msi.h + "......sb:" + sbmpW + "*" + sbmpH);
        } catch (Exception e) {
            sbmpW = 100;
            sbmpH = 100;
        }
        // 计算图片的位置
        calculateImagePosition(0, 0, sbmpW, sbmpH, rotateAngle);

        // 开始构造旋转缩放参数
        mMatrix = new Matrix();

        mMatrix.setScale(zoomFactor, zoomFactor);
        // 设置旋转比例
        mMatrix.postRotate(rotateAngle % 360, sbmpW / 2, sbmpH / 2);
        // 设置移动
        mMatrix.postTranslate(dx + mOutLayoutImageWidth, dy
                + mOutLayoutImageHeight);

//        setImageViewParams: 744*346...center:568*317
//        Log.e(TAG, "setImageViewParams: " + mRotatedImageWidth + "*" + mRotatedImageHeight + "...center:" + mImageCenterPoint.x + "*" + mImageCenterPoint.y);

        // 设置小图片的宽高
        setImageViewWH(mRotatedImageWidth, mRotatedImageHeight,
                (mImageCenterPoint.x - mRotatedImageWidth / 2),
                (mImageCenterPoint.y - mRotatedImageHeight / 2));
    }


    /**
     * 设置图片的宽和高
     *
     * @param w
     * @param h
     * @param l
     * @param t
     */
    private void setImageViewWH(int w, int h, int l, int t) {
        int imageWidth = w + mOutLayoutImageWidth * 2;
        int imageHeight = h + mOutLayoutImageHeight * 2;
        int imageleft = l - mOutLayoutImageWidth;
        int imageTop = t - mOutLayoutImageHeight;

        mImageViewWidth = imageWidth;
        mImageViewHeight = imageHeight;


        mImageViewLeft = imageleft;
        mImageViewTop = imageTop;
        int mbwidth = mOutLayoutImageWidth;
        int mbheight = mOutLayoutImageHeight;
        int mright = mImageViewLeft + mImageViewWidth;
        int mbottom = mImageViewTop + mImageViewHeight;
        if (null != mParentSize) {
            if (mright < mbwidth) {
                mImageViewLeft = mbwidth - mImageViewWidth;
            }
            if (mbottom < mbheight) {
                mImageViewTop = mbottom - mImageViewHeight;
            }
            if (mImageViewLeft > mParentSize.x - mbwidth) {
                mImageViewLeft = mParentSize.x - mbwidth;
            }
            if (mImageViewTop > mParentSize.y - mbheight) {
                mImageViewTop = mParentSize.y - mbheight;
            }

        }
        mImageCenterPoint.x = mImageViewLeft + mImageViewWidth / 2;
        mImageCenterPoint.y = mImageViewTop + mImageViewHeight / 2;
        // 设置图片的布局
        this.layout(mImageViewLeft, mImageViewTop, mImageViewLeft
                + mImageViewWidth, mImageViewTop + mImageViewHeight);

    }

    private void setCenterPoint(Point c) {
        mImageCenterPoint = c;
        setImageViewWH(mRotatedImageWidth, mRotatedImageHeight,
                (mImageCenterPoint.x - mRotatedImageWidth / 2),
                (mImageCenterPoint.y - mRotatedImageHeight / 2));

    }


    /**
     * 计算图片的位置
     */
    private void calculateImagePosition(int left, int top, int right,
                                        int bottom, float angle) {
        // 左上角的坐标
        Point p1 = new Point(left, top);

        // 右上角的坐标
        Point p2 = new Point(right, top);

        // 右下角坐标
        Point p3 = new Point(right, bottom);

        // 左下角坐标
        Point p4 = new Point(left, bottom);

        // 需要围绕参考点做旋转
        rotateCenterPoint = new Point((left + right) / 2, (top + bottom) / 2);

        // 旋转之后边框顶点的坐标
        mPoint1 = rotatePoint(rotateCenterPoint, p1, angle);
        mPoint2 = rotatePoint(rotateCenterPoint, p2, angle);
        mPoint3 = rotatePoint(rotateCenterPoint, p3, angle);
        mPoint4 = rotatePoint(rotateCenterPoint, p4, angle);
        int w = 0;
        int h = 0;
        int maxX = mPoint1.x;
        int minX = mPoint1.x;

        // 这是要选出那个坐标点的X坐标最大吗？
        if (mPoint2.x > maxX) {

            maxX = mPoint2.x;
        }

        if (mPoint3.x > maxX) {

            maxX = mPoint3.x;
        }

        if (mPoint4.x > maxX) {

            maxX = mPoint4.x;
        }

        // 这是要选出那个坐标的X坐标最小吗？
        if (mPoint2.x < minX) {
            minX = mPoint2.x;
        }
        if (mPoint3.x < minX) {
            minX = mPoint3.x;
        }

        if (mPoint4.x < minX) {
            minX = mPoint4.x;
        }

        // 计算差值
        w = maxX - minX;

        int maxY = mPoint1.y;
        int minY = mPoint1.y;

        // 选最大的Y坐标
        if (mPoint2.y > maxY) {
            maxY = mPoint2.y;
        }
        if (mPoint3.y > maxY) {
            maxY = mPoint3.y;
        }
        if (mPoint4.y > maxY) {
            maxY = mPoint4.y;
        }

        // 选最小Y坐标
        if (mPoint2.y < minY) {
            minY = mPoint2.y;
        }
        if (mPoint3.y < minY) {
            minY = mPoint3.y;
        }
        if (mPoint4.y < minY) {
            minY = mPoint4.y;
        }

        // 计算差值
        h = maxY - minY;

        // 计算边框的中心坐标
        Point centerPoint = intersects(mPoint4, mPoint2, mPoint1, mPoint3);

        // 这是要计算哪个中心的坐标？
        dx = w / 2 - centerPoint.x;
        dy = h / 2 - centerPoint.y;


        // 加了这么多距离，就相当于向右移动了这么多 的距离
        mPoint1.x = mPoint1.x + dx + mOutLayoutImageWidth;
        mPoint2.x = mPoint2.x + dx + mOutLayoutImageWidth;
        mPoint3.x = mPoint3.x + dx + mOutLayoutImageWidth;
        mPoint4.x = mPoint4.x + dx + mOutLayoutImageWidth;

        // 向下移动了这么多的距离
        mPoint1.y = mPoint1.y + dy + mOutLayoutImageHeight;
        mPoint2.y = mPoint2.y + dy + mOutLayoutImageHeight;
        mPoint3.y = mPoint3.y + dy + mOutLayoutImageHeight;
        mPoint4.y = mPoint4.y + dy + mOutLayoutImageHeight;

        //
        mRotatedImageWidth = w;
        mRotatedImageHeight = h;

        //
        mFlipTypeCenterPoint = mPoint1;
        mDeleteImageCenterPoint = mPoint2;
        mContralImageCenterPoint = mPoint3;
    }

    /**
     * 对角线的交点
     *
     * @param sp3
     * @param sp4
     * @param sp1
     * @param sp2
     * @return
     */
    private Point intersects(Point sp3, Point sp4, Point sp1, Point sp2) {
        Point localPoint = new Point(0, 0);
        double num = (sp4.y - sp3.y) * (sp3.x - sp1.x) - (sp4.x - sp3.x)
                * (sp3.y - sp1.y);
        double denom = (sp4.y - sp3.y) * (sp2.x - sp1.x) - (sp4.x - sp3.x)
                * (sp2.y - sp1.y);
        localPoint.x = (int) (sp1.x + (sp2.x - sp1.x) * num / denom);
        localPoint.y = (int) (sp1.y + (sp2.y - sp1.y) * num / denom);
        return localPoint;
    }

    private final int CLICKED_MIRROR = 1;
    private final int CLICKED_CONTROL = 2;
    private final int CLICKED_DELETE = 3;

    /**
     * 是否点中3个图标， 1点中 镜像图片 ；2点中 control图片  ；3 删除图片； 0 没有点中
     * <p>
     * * 是否点中2个图标， 1点中 delete图片 ；2点中 control图片； 0 没有点中
     */
    private int getClickPosition(int x, int y) {
        int xx = x;
        int yy = y;
        int kk1 = ((xx - mFlipTypeCenterPoint.x)
                * (xx - mFlipTypeCenterPoint.x) + (yy - mFlipTypeCenterPoint.y)
                * (yy - mFlipTypeCenterPoint.y));
        int kk2 = ((xx - mContralImageCenterPoint.x)
                * (xx - mContralImageCenterPoint.x) + (yy - mContralImageCenterPoint.y)
                * (yy - mContralImageCenterPoint.y));

        int kk3 = ((xx - mDeleteImageCenterPoint.x)
                * (xx - mDeleteImageCenterPoint.x) + (yy - mDeleteImageCenterPoint.y)
                * (yy - mDeleteImageCenterPoint.y));

        if (kk1 < mOutLayoutImageWidth * mOutLayoutImageWidth) {
            return CLICKED_MIRROR;
        } else if (kk2 < mOutLayoutImageWidth * mOutLayoutImageWidth) {
            return CLICKED_CONTROL;
        } else if (kk3 <= mOutLayoutImageWidth * mOutLayoutImageWidth) {
            return CLICKED_DELETE;
        }
        return 0;
    }

    /**
     * 旋转顶点坐标
     *
     * @param rotateCenterPoint 围绕该点进行旋转
     * @param sourcePoint
     * @param angle
     * @return
     */
    private Point rotatePoint(Point rotateCenterPoint, Point sourcePoint,
                              float angle) {

        // 不明白什么意思
        sourcePoint.x = sourcePoint.x - rotateCenterPoint.x;
        sourcePoint.y = sourcePoint.y - rotateCenterPoint.y;

        // 角度a
        double alpha = 0.0;

        // 角度b
        double bate = 0.0;

        Point resultPoint = new Point();

        // 两点之间的距离
        double distance = Math.sqrt(sourcePoint.x * sourcePoint.x
                + sourcePoint.y * sourcePoint.y);

        // 如果在原点
        if (sourcePoint.x == 0 && sourcePoint.y == 0) {

            return rotateCenterPoint;

            // 在第一象限
        } else if (sourcePoint.x >= 0 && sourcePoint.y >= 0) {

            // 计算与X轴正方向的夹角, 用反三角函数，
            alpha = Math.asin(sourcePoint.y / distance);

            // 第二象限
        } else if (sourcePoint.x <= 0 && sourcePoint.y >= 0) {
            // 计算与X轴正方向的夹角, 用反三角函数，
            alpha = Math.asin(Math.abs(sourcePoint.x) / distance);
            alpha = alpha + Math.PI / 2;
            // 第三象限
        } else if (sourcePoint.x <= 0 && sourcePoint.y <= 0) {

            // 计算与x正方向的夹角
            alpha = Math.asin(Math.abs(sourcePoint.y) / distance);
            alpha = alpha + Math.PI;

            // 第四象限
        } else if (sourcePoint.x >= 0 && sourcePoint.y <= 0) {

            // 计算与x正方向的夹角
            alpha = Math.asin(sourcePoint.x / distance);
            alpha = alpha + Math.PI * 3 / 2;
        }

        // 将弧度换算成角度
        alpha = radianToDegree(alpha);

        // 旋转之后的角度
        bate = alpha + angle;

        // 将角度换算成弧度
        bate = degreeToRadian(bate);

        // 计算旋转之后的坐标点
        resultPoint.x = (int) Math.round(distance * Math.cos(bate));
        resultPoint.y = (int) Math.round(distance * Math.sin(bate));

        resultPoint.x += rotateCenterPoint.x;
        resultPoint.y += rotateCenterPoint.y;
        return resultPoint;
    }

    /**
     * 将弧度换算成角度
     */
    private double radianToDegree(double radian) {
        return radian * 180 / Math.PI;
    }

    /**
     * 将角度换算成弧度
     *
     * @param degree
     * @return
     */
    private double degreeToRadian(double degree) {
        return degree * Math.PI / 180;
    }

    /**
     * 两点的距离
     */
    private double spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }


    public void setOnClickListener(onClickListener _listener) {
        listener = _listener;
    }

    private onClickListener listener;

    public interface onClickListener {

        public void onClick(DragView view);
    }


    public void recycle() {
        if (null != mContralBitmap && !mContralBitmap.isRecycled()) {
            mContralBitmap.recycle();
        }
        if (null != mOriginalBitmap && !mOriginalBitmap.isRecycled()) {
            mOriginalBitmap.recycle();
        }
        if (null != mDeleteBitmap && !mDeleteBitmap.isRecycled()) {
            mDeleteBitmap.recycle();
        }
        clearSomeBitmap();
    }

    private onDelListener onDelListener;

    /**
     * 删除图标的监听
     *
     * @param listener
     */
    public void setDelListener(onDelListener listener) {
        onDelListener = listener;
    }

    public interface onDelListener {
        /**
         * 删除当前字幕
         */
        public void onDelete(DragView single);
    }

    /**
     * @param mirrorListener
     */
    public void setMirrorListener(onMirrorListener mirrorListener) {
        mMirrorListener = mirrorListener;
    }

    private onMirrorListener mMirrorListener;

    public interface onMirrorListener {
        /**
         * @param single
         * @param flipType
         */
        public void onMirror(DragView single, FlipType flipType);
    }

    /**
     * @return
     */
    public RectF getShowRectF() {
        RectF rectF = new RectF();
        float fw = mParentSize.x + 0.0f;
        float fh = mParentSize.y + 0.0f;
        Rect tmp = new Rect(getLeft(), getTop(), getRight(), getBottom());
        tmp.inset(mOutLayoutImageWidth, mOutLayoutImageHeight); //排除控制按钮的大小
        rectF.set(tmp.left / fw, tmp.top / fh, tmp.right / fw, tmp.bottom / fh);
        return rectF;
    }

    /**
     * 获取底图的顶点坐标 （没有旋转角度的顶点）
     *
     * @return
     */
    public RectF getSrcRectF() {
        Rect show = new Rect(getLeft(), getTop(), getRight(), getBottom());
        int angle = getRotateAngle();

//        Log.e(TAG, "getSrcRectF: angle:" + angle + " show:" + show + "  size:" + show.width() + "*" + show.height() + " point :" + mPoint1 + " " + mPoint2 + " " + mPoint3 + " _" + mPoint4);

        Matrix matrix = new Matrix();
        Point center = new Point(show.width() / 2, show.height() / 2);
        matrix.setRotate(-angle, center.x, center.y);
        float[] dst1 = new float[2];
        float[] dst3 = new float[2];


        float[] src1 = new float[]{mPoint1.x, mPoint1.y};
        float[] src3 = new float[]{mPoint3.x, mPoint3.y};


        matrix.mapPoints(dst1, src1);
        matrix.mapPoints(dst3, src3);

        //未旋转时的顶点坐标
        Rect srcRect = new Rect((int) dst1[0], (int) dst1[1], (int) dst3[0], (int) dst3[1]);

        //转化为相对于容器的坐标
        srcRect.offset(show.left, show.top);

        float fw = mParentSize.x + 0.0f;
        float fh = mParentSize.y + 0.0f;
        RectF dst = new RectF(srcRect.left / fw, srcRect.top / fh, srcRect.right / fw, srcRect.bottom / fh);

//        Log.e(TAG, "getSrcRectF: " + srcRect + "  " + dst);
        return dst;
    }

}
