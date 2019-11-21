package com.rd.veuisdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rd.veuisdk.R;

import java.util.List;

/**
 * 作者：JIAN on 2017/11/15 12:15
 */
public class CaptionDrawRect extends View {

    private final String TAG = "CaptionDrawRect";
    private Bitmap rotationImgBtn = null;
    private Bitmap mDeleteBitmap = null;
    private Bitmap mAlignBitmap = null;
    private OnTouchListener mListener;
    private PointF prePointF = new PointF(0, 0);
    private RectF rotationRectF = new RectF();
    private RectF delRectF = new RectF();
    private RectF alignRectF = new RectF();
    private List<PointF> mListPointF;
    private boolean canScalOrRotate = false;
    private boolean canMove = false;
    private OnUIClickListener mMenuClickLisenter;
    private onClickListener mClickListener;
    private int textAlign = 1;  //0 居左 、1 居中 、 2居右
    private int[] lineColor = new int[]{Color.WHITE, Color.argb(100, 255, 0, 0)};

    public CaptionDrawRect(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    /**
     * 控制器的显示位置
     *
     * @param list
     */
    public void SetDrawRect(List<PointF> list) {
        mListPointF = list;
        invalidate();
    }

    /**
     * 控制器的坐标
     *
     * @return
     */
    public List<PointF> getList() {
        return mListPointF;
    }


    public void SetOnTouchListener(OnTouchListener listener) {
        mListener = listener;
    }

    public void SetOnAlignClickListener(OnUIClickListener alignClickListener) {
        mMenuClickLisenter = alignClickListener;
    }


    public void setClickListener(onClickListener listener) {
        mClickListener = listener;
    }

    private Paint m_paint = new Paint();


    public boolean isVisible() {
        return isVisible;
    }

    private boolean isVisible = false;


    public void setVisibleUI(boolean visible) {
        if (isVisible != visible) {
            isVisible = visible;
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isVisible && null != mListPointF) {
            // 设置颜色
            m_paint.setColor(lineColor[canMove ? 1 : 0]);
            // 设置抗锯齿
            m_paint.setAntiAlias(true);
            // 设置线宽
            m_paint.setStrokeWidth(4);
            // 设置非填充
            m_paint.setStyle(Paint.Style.STROKE);
            //设置阴影
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            m_paint.setShadowLayer(0.5f, 1, 1, 0x47000000);

            canvas.drawLine(mListPointF.get(0).x, mListPointF.get(0).y, mListPointF.get(1).x, mListPointF.get(1).y, m_paint);
            canvas.drawLine(mListPointF.get(1).x, mListPointF.get(1).y, mListPointF.get(2).x, mListPointF.get(2).y, m_paint);
            canvas.drawLine(mListPointF.get(2).x, mListPointF.get(2).y, mListPointF.get(3).x, mListPointF.get(3).y, m_paint);
            canvas.drawLine(mListPointF.get(3).x, mListPointF.get(3).y, mListPointF.get(0).x, mListPointF.get(0).y, m_paint);

            if (null != mAlignBitmap) {
                PointF align = mListPointF.get(1);
                canvas.drawBitmap(mAlignBitmap, align.x - mAlignBitmap.getHeight() / 2, align.y - mAlignBitmap.getWidth() / 2, m_paint);
                alignRectF.set(align.x - mAlignBitmap.getWidth() / 2, align.y - mAlignBitmap.getHeight() / 2, align.x - mAlignBitmap.getWidth() / 2 + mAlignBitmap.getWidth(), align.y - mAlignBitmap.getWidth() / 2 + mAlignBitmap.getHeight());

            }
            if (null != mDeleteBitmap) {
                PointF del = mListPointF.get(0);
                canvas.drawBitmap(mDeleteBitmap, del.x - mDeleteBitmap.getHeight() / 2, del.y - mDeleteBitmap.getWidth() / 2, m_paint);
                delRectF.set(del.x - mDeleteBitmap.getWidth() / 2, del.y - mDeleteBitmap.getHeight() / 2, del.x - mDeleteBitmap.getWidth() / 2 + mDeleteBitmap.getWidth(), del.y - mDeleteBitmap.getWidth() / 2 + mDeleteBitmap.getHeight());

//                canvas.drawRect(delRectF, m_paint);
            }
            if (null != rotationImgBtn) {
//控制按钮的位置
                PointF control = mListPointF.get(2);
                canvas.drawBitmap(rotationImgBtn, control.x - rotationImgBtn.getHeight() / 2, control.y - rotationImgBtn.getWidth() / 2, m_paint);


                rotationRectF.set(control.x - rotationImgBtn.getWidth() / 2, control.y - rotationImgBtn.getHeight() / 2, control.x - rotationImgBtn.getWidth() / 2 + rotationImgBtn.getWidth(), control.y - rotationImgBtn.getWidth() / 2 + rotationImgBtn.getHeight());
//                canvas.drawRect(rotationRectF, m_paint);
            }

        }


    }

    private PointF centerPointF = new PointF();
    private boolean isDeleteClick = false;
    private boolean isAlignClick = false;
    private boolean isItemClick = false;
    private final int MSG_LONG_TOUCH = 100;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LONG_TOUCH: {
                    canMove = true;
                    invalidate();
                }
                break;
                default: {
                }
                break;
            }
        }
    };

    private PointF mDownPoint = new PointF(0, 0);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float targetX = event.getX();
        float targetY = event.getY();
        if (mListPointF != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mHandler.removeMessages(MSG_LONG_TOUCH);
                    mDownPoint.set(targetX, targetY);
                    canMove = false;
                    if (isVisible) {
                        isItemClick = false;
                        isDeleteClick = delRectF.contains(targetX, targetY);
                        isAlignClick = alignRectF.contains(targetX, targetY);
                        if (!isDeleteClick && !isAlignClick) {

                            canScalOrRotate = rotationRectF.contains(targetX, targetY);

                            // 判断手指是否在字幕框内
                            RectF r = new RectF();
                            Path path = new Path();
                            path.moveTo(mListPointF.get(0).x, mListPointF.get(0).y);
                            path.lineTo(mListPointF.get(1).x, mListPointF.get(1).y);
                            path.lineTo(mListPointF.get(2).x, mListPointF.get(2).y);
                            path.lineTo(mListPointF.get(3).x, mListPointF.get(3).y);
                            path.close();
                            path.computeBounds(r, true);
                            Region region = new Region();
                            region.setPath(path, new Region((int) r.left, (int) r.top, (int) r.right, (int) r.bottom));
                            if (region.contains((int) targetX, (int) targetY)) {
                                //在红色画框内才响应单击事件
                                isItemClick = true;
                                //响应了长按，才能move
                                mHandler.sendEmptyMessageDelayed(MSG_LONG_TOUCH, 10);
                            }
                            prePointF.set(targetX, targetY);
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_MOVE: {
                    if (isVisible) {
                        if (!isDeleteClick) {
                            centerPointF.set((mListPointF.get(0).x + mListPointF.get(2).x) / 2
                                    , (mListPointF.get(0).y + mListPointF.get(2).y) / 2);
                            if (canMove) {
                                if (null != mListener) {
                                    isItemClick = false;
                                    mListener.onDrag(prePointF, new PointF(targetX, targetY));
                                }
                            } else if (canScalOrRotate) {

                                // 计算手指在屏幕上滑动的距离比例
                                double temp = Math.pow(prePointF.x - centerPointF.x, 2) + Math.pow(prePointF.y - centerPointF.y, 2);
                                //上一次的点到中心点的距离
                                double preLength = Math.sqrt(temp);

                                double temp2 = Math.pow(targetX - centerPointF.x, 2) + Math.pow(targetY - centerPointF.y, 2);
                                //新的点到中心点的距离
                                double length = Math.sqrt(temp2);

                                //每次缩放变化的比
                                float offsetScale = (float) ((length - preLength) / preLength);


                                // 计算手指滑动的角度
                                float radian = (float) (Math.atan2(targetY - centerPointF.y, targetX - centerPointF.x)
                                        - Math.atan2(prePointF.y - centerPointF.y, prePointF.x - centerPointF.x));
                                // 弧度转换为角度
                                float angle = (float) (radian * 180 / Math.PI);
                                if (null != mListener) {
                                    isItemClick = false;
                                    mListener.onScaleAndRotate(offsetScale, -angle);
                                }
                            }
                            prePointF.set(targetX, targetY);
                        }
                    }
                }
                break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    mHandler.removeMessages(MSG_LONG_TOUCH);
                    canMove = false;
                    if (isVisible) {
                        canScalOrRotate = false;
                        canMove = false;
                        if (isDeleteClick) {
                            isDeleteClick = false;
                            if (null != mMenuClickLisenter) {
                                mMenuClickLisenter.onDeleteClick();
                            }
                        } else if (isAlignClick) {
                            textAlign++;
                            if (textAlign > 2) {
                                textAlign = 0;
                            }
                            if (null != mMenuClickLisenter) {
                                if (textAlign == 0) {
                                    mAlignBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_left_new);
                                } else if (textAlign == 1) {
                                    mAlignBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_mid_new);
                                } else {
                                    mAlignBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_right_new);
                                }
                                mMenuClickLisenter.onAlignClick(textAlign);
                            }
                            isAlignClick = false;
                        } else {
                            if (Math.abs(mDownPoint.x - targetX) > 10 || Math.abs(mDownPoint.y - targetY) > 10) {
                                //超过10个像素，取消长按事件
                                //mHandler.removeMessages(MSG_LONG_TOUCH);
                                isItemClick = false;
                            } else {
                                isItemClick = true;
                            }
                            if (isItemClick) {
                                //单击事件
                                if (null != mClickListener) {
                                    mClickListener.onClick(targetX, targetY);
                                }
                            }
                        }
                    } else {
                        if (null != mClickListener) {
                            mClickListener.onClick(targetX, targetY);
                        }
                    }
                    invalidate();
                }
                break;
            }
            return true;
        } else {
            canMove = false;
            mHandler.removeMessages(MSG_LONG_TOUCH);
            return super.onTouchEvent(event);
        }
    }


    /**
     * 释放,退出字幕界面时
     */
    public void recycle() {
        this.setVisibility(View.GONE);
        if (null != mDeleteBitmap) {
            mDeleteBitmap.recycle();
            mDeleteBitmap = null;
        }
        if (null != rotationImgBtn) {
            rotationImgBtn.recycle();
            rotationImgBtn = null;
        }
        if (null != mAlignBitmap) {
            mAlignBitmap.recycle();
            mAlignBitmap = null;
        }
    }

    /**
     * 进入显示之前，初始化bmp
     */
    public void initbmp() {
        rotationImgBtn = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_controller_new);
        mDeleteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_delete_new);
        mAlignBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subtitle_effect_mid_new);
        this.setVisibility(View.VISIBLE);
    }

    public interface OnUIClickListener {
        /**
         * 响应删除按钮
         */
        void onDeleteClick();

        void onAlignClick(int align);
    }

    public interface onClickListener {
        /**
         * 点击控制器的任意一个区域
         *
         * @param x
         * @param y
         */
        void onClick(float x, float y);
    }


    public interface OnTouchListener {


        void onDrag(PointF prePointF, PointF nowPointF);

        /**
         * @param offsetScale     缩放参数：相比上一次的比例变化
         * @param offfsetRotation 旋转角度，每次的偏移量
         */
        void onScaleAndRotate(float offsetScale, float offfsetRotation);


    }

}
