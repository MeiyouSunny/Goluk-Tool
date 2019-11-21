package com.rd.veuisdk.demo.zishuo.drawtext;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.SparseArray;

import com.rd.vecore.graphics.Matrix;
import com.rd.vecore.models.CanvasObject;
import com.rd.veuisdk.demo.zishuo.TempZishuoParams;
import com.rd.veuisdk.demo.zishuo.TextNode;

import java.util.ArrayList;

public abstract class CustomHandler {

    public static float fW = 480.0f, fH = 640.0f;//屏幕的宽高 要算比例
    public Matrix matrix = null;//前面
    public Matrix matrix2 = null;//当前
    public Paint mTextPaint;
    public ArrayList<TextNode> mTextNodes;//文字
    public int mIndex = 0;//文字下标
    public int mLastIndex = -1;//上一步的下标
    public boolean mAdd = false;//是否下一句
    public boolean mChange = true;//是否改变 即是下一句
    public CustomTextLayout mTextLayout;//当前句
    public int mInt;//多少个一个循环
    public Context mContext;
    //更多动画
    public boolean mIsMore = false;
    public int mMoreIndex = 0;
    private android.graphics.Matrix mMatrix;
    private Camera mCamera;
    private float[] mValue;
    //开始X Y坐标 逆时针旋转 (mStartX, mStartY + mHeight)  顺时针(mStart + mWidth, mStartY + mHeight)
    public int mStartX = 0, mStartY = 0;
    //宽度和高度
    public int mWidth = 0, mHeight = 0;
    public Rect mRect = new Rect(0, 0, 0, 0);
    //新增、全部、当前
    public SparseArray<CustomTextLayout> mAddArray;
    public SparseArray<SparseArray<CustomTextLayout>> mArrays = new SparseArray<>();
    public SparseArray<CustomTextLayout> mIndexArray;

    public CustomHandler(Context context) {
        this.mContext = context;
        mTextNodes = TempZishuoParams.getInstance().getTextNodes();
        mIsMore = TempZishuoParams.getInstance().isMore();
        mTextPaint = new Paint();

        matrix = new Matrix();
        matrix2 = new Matrix();
    }

    /**
     * 更多动画
     */
    public void moreAnim(int index, float time, Matrix matrix){
        //持续时间0.3秒
        if (index == 0) {
            zoom(time, matrix);
        } else if (index == 1){
            rotate(time, matrix);
        } else if (index == 2) {
            translate(time, matrix);
        } else if (index == 3) {
            rotateX(time, matrix);
        } else if (index == 4) {
            rotateY(time, matrix);
        }
    }

    /**
     * 放大缩小 放大0.95-1.05 100毫秒一次
     */
    private void zoom(float f, Matrix matrix){
        float scale = 1;
        float x = 0.05f;
        if (f < 0) {
            scale = 1;
        } else if (f < 0.05) {
            scale = (float) (1 + f / 0.05 * x);
        } else if (f < 0.15) {
            scale = (float) (1 + x - (f - 0.05) / 0.1 * 2 * x);
        } else if (f < 0.25) {
            scale = (float) (1 - x + (f - 0.15) / 0.1 * 2 * x);
        } else if (f < 0.3) {
            scale = (float) (1 + x - (f - 0.25) / 0.05 * x);
        } else {
            scale = 1;
        }
        matrix.postScale(scale, scale, mStartX + mWidth / 2, mStartY + mHeight / 2);
    }

    /**
     * 旋转 -3 -3
     */
    private void rotate(float f, Matrix matrix){
        float angle = 0;
        int x = 2;
        if (f < 0) {
            angle = 0;
        } else if (f < 0.05) {
            angle =  - f * 10 * x;
        } else if (f < 0.15) {
            angle = (float) ((f - 0.05) / 0.1 * 2 * x - x);
        } else if (f < 0.25) {
            angle = (float) (x - (f - 0.15) / 0.1 * 2 * x);
        } else if (f < 0.3) {
            angle = (float) ((f - 0.25) / 0.05 * x - x);
        } else {
            angle = 0;
        }
        matrix.postRotate(angle, mStartX + mWidth / 2, mStartY + mHeight / 2);
    }

    /**
     * 移动 上下抖动 -3 - 3
     */
    private void translate(float time, Matrix matrix) {
        float Y = 0;
        int x = 2;
        if (time < 0) {
            Y = 0;
        } else if (time < 0.05) {
            Y =  - time * 10 * x;
        } else if (time < 0.15) {
            Y = (float) ((time - 0.05) / 0.1 * 2 * x - x);
        } else if (time < 0.25) {
            Y = (float) (x - (time - 0.15) / 0.1 * 2 * x);
        } else if (time < 0.3) {
            Y = (float) ((time - 0.25) / 0.05 * x - x);
        } else {
            Y = 0;
        }
        matrix.postTranslate(0, Y);
    }

    /**
     * 绕X轴旋转 -30 - 0
     */
    private void rotateX(float f, Matrix matrix) {
        if (mMatrix == null) {
            mMatrix = new android.graphics.Matrix();
            mCamera = new Camera();
            mValue = new float[9];
        }
        mCamera.save();
        mMatrix.reset();
        float angle = 0;
        int x = 30;
        if (f < 0) {
            angle = 0;
        } else if (f < 0.1) {
            angle = (float) (- f / 0.1 * x);
        } else if (f < 0.2) {
            angle = (float) ((f - 0.1) / 0.1 * x - x);
        } else if (f < 0.3) {
            angle = (float) ( - (f - 0.2) / 0.1 * x);
        } else {
            angle = 0;
        }
        mCamera.rotateX(angle);
        mCamera.getMatrix(mMatrix);
        mMatrix.getValues(mValue);
        matrix.setValues(mValue);
        mCamera.restore();
        //旋转中心点
        matrix.preTranslate(-(mStartX + mWidth / 2), -(mStartY + mHeight));
        matrix.postTranslate(mStartX + mWidth / 2, mStartY + mHeight);
    }

    /**
     * 绕Y轴旋转 -10 - 10
     */
    private void rotateY(float f, Matrix matrix) {
        if (mMatrix == null) {
            mMatrix = new android.graphics.Matrix();
            mCamera = new Camera();
            mValue = new float[9];
        }
        mCamera.save();
        mMatrix.reset();
        float angle = 0;
        int x = 10;
        if (f < 0) {
            angle = 0;
        } else if (f < 0.05) {
            angle =  - f * 10 * x;
        } else if (f < 0.15) {
            angle = (float) ((f - 0.05) / 0.1 * 2 * x - x);
        } else if (f < 0.25) {
            angle = (float) (x - (f - 0.15) / 0.1 * 2 * x);
        } else if (f < 0.3) {
            angle = (float) ((f - 0.25) / 0.05 * x - x);
        } else {
            angle = 0;
        }
        mCamera.rotateY(angle);
        mCamera.getMatrix(mMatrix);
        mMatrix.getValues(mValue);
        matrix.setValues(mValue);
        mCamera.restore();
        matrix.preTranslate(-(mStartX + mWidth / 2), -(mStartY + mHeight / 2));
        matrix.postTranslate(mStartX + mWidth / 2, mStartY + mHeight / 2);
    }

    /**
     * 上移文字高度
     */
    public void postUpTranslate(Matrix matrix, float proportion) {
        matrix.postTranslate(0, -mHeight * proportion);
    }

    /**
     * 逆时针旋转 90
     */
    public void postCounterClockwiseRotate(Matrix matrix, float proportion) {
        matrix.postRotate(proportion * -90 % 360, mStartX, mStartY + mHeight);
    }

    /**
     * 顺时针旋转 90
     */
    public void postClockwatchRotate(Matrix matrix, float proportion) {
        matrix.postRotate(proportion * 90 % 360, mStartX + mWidth, mStartY + mHeight);
    }

    /**
     * 放大、缩小
     */
    public void postScale(Matrix matrix, float proportion, float scal) {
        matrix.postScale(1 - (1 - scal) * proportion, 1 - (1 - scal) * proportion, mStartX, mStartY);
    }

    /**
     * 重置
     */
    public void reset() {
        mArrays.clear();
        if (mAddArray != null) {
            mAddArray.clear();
            mAddArray = null;
        }
        if (mIndexArray != null) {
            mIndexArray.clear();
            mIndexArray = null;
        }
        mWidth = 0;
        mHeight = 0;
        mStartX = 0;
        mStartY = 0;

        mTextNodes = TempZishuoParams.getInstance().getTextNodes();
        mIsMore = TempZishuoParams.getInstance().isMore();
        mLastIndex = -1;
        mChange = true;
        mAdd = false;
    }

    /**
     * 绘制
     * @param canvas
     * @param currentProgress
     */
    public abstract void onDraw(CanvasObject canvas, float currentProgress);

}
