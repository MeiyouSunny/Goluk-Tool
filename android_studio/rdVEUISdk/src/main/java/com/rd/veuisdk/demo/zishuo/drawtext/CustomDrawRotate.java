package com.rd.veuisdk.demo.zishuo.drawtext;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.SparseArray;

import com.rd.vecore.graphics.Matrix;
import com.rd.vecore.models.CanvasObject;
import com.rd.vecore.models.TextLayout;
import com.rd.veuisdk.demo.zishuo.TextNode;

public class CustomDrawRotate extends CustomHandler {

    public CustomDrawRotate(Context context) {
        super(context);
        mInt = 14;
    }

    @Override
    public void onDraw(CanvasObject canvas, float currentProgress) {
        if (mStartX == 0 || mStartY == 0 || mWidth == 0 || mHeight == 0) {
            float wScale = canvas.getWidth() / fW;
            float hScale = canvas.getHeight() / fH;
            //设置宽高为350,50   开始坐标为50,350
            mStartX = (int) (50 * wScale);
            mStartY = (int) (350 * hScale);
            mWidth = (int) (350 * wScale);
            mHeight = (int) (50 * hScale);
//            Log.d("ok", "onDraw: " + canvas.getWidth() + "/" + canvas.getHeight() + "===" + wScale + "/" + hScale);
//            Log.d("ok", "onDraw: " + mStartX + "=" + mStartY + "===" + mWidth + "=" + mHeight);
        }
        //确定现在画第几句
        for (mIndex = 0; mIndex < mTextNodes.size(); mIndex++) {
            if (currentProgress >= mTextNodes.get(mIndex).getBegin() && currentProgress < mTextNodes.get(mIndex).getEnd()) {
                break;
            }
        }
        if (mIndex >= mTextNodes.size()) {
            return;
        }
        if (mLastIndex != mIndex) {
            mLastIndex = mIndex;
            mAdd = false;
            mChange = true;
        }
        //比例
        float factor = 1;
        TextNode node = mTextNodes.get(mIndex);
        //判断当前文字是否为空
        if (TextUtils.isEmpty(node.getText())) {
            //前几步的内容
            drawOld(canvas, factor, mIndex, true);
            //当前句完成 保存 如果当前已经存在则不用添加 重复播放时会出现
            if (!mAdd && mArrays.get(mIndex) == null) {
                addDraw(true, mIndex);
            }
        } else {
            //计算比例
            if (node.getContinued() <= 0 || node.getContinued() > (node.getEnd() - node.getBegin())) {
                factor = (currentProgress - node.getBegin()) / (node.getEnd() - node.getBegin());
            } else {
                factor =(currentProgress - node.getBegin()) / node.getContinued();
                factor = factor > 1? 1 : factor;
            }
            //画当前句子
            canvas.save();
            matrix2.reset();
            if (mIndex % mInt == 6) {
                matrix2.postRotate((1 - factor) * 90, mStartX, mStartY + mHeight);
                matrix2.postScale(factor, factor, mStartX, mStartY + mHeight);
            } else if (mIndex % mInt == 13) {
                matrix2.postRotate((1 - factor) * (-90), mStartX + mWidth, mStartY + mHeight);
                matrix2.postScale(factor, factor, mStartX + mWidth, mStartY + mHeight);
            } else {
                matrix2.postScale(factor, factor, mStartX, mStartY + mHeight);
            }

            if (mChange) {
                mChange = false;
                mTextLayout = new CustomTextLayout(mWidth, mHeight, new Rect(1, 1, 1, 1), TextLayout.Alignment.left, node);
                if (node.getText().length() <3) {
                    //上下移动
                    mMoreIndex = Math.random() > 0.5? 3 : 2;
                } else if (node.getText().length() < 6){
                    //缩放
                    mMoreIndex =0;
                } else {
                    mMoreIndex =  Math.random() > 0.5? 1 : 4;
                }
            }
            //更多动画
            if (mIsMore && factor == 1) {
                //放大缩小
                //如果剩余时间大于0.5
                float remaining = node.getEnd() - node.getBegin() - node.getContinued();
                if (remaining > 0.5) {
                    float f = (currentProgress - node.getBegin() - node.getContinued() - 0.05f);
                    moreAnim(mMoreIndex, f, matrix2);
                }
            }
            canvas.setMatrix(matrix2);
            mTextLayout.draw(canvas, mStartX, mStartY);
            canvas.restore();
            //前几步的内容
            drawOld(canvas, factor, mIndex, false);
            //当前句完成 保存 如果当前已经存在则不用添加 重复播放时会出现
            if (factor >= 1 && !mAdd && mArrays.get(mIndex) == null) {
                addDraw(false, mIndex);
            }
        }
    }

    /**
     * 添加本句 empty是否为空
     */
    private void addDraw(boolean empty, int index) {
        mAddArray = new SparseArray<>();
        //如果本句为空 就不添加
        if (!empty) {
            mAddArray.put(index, mTextLayout);
        }
        if (index > 0) {
            mIndexArray = mArrays.get(index - 1);
            CustomTextLayout l;
            for (int i = index - 1, j = 1; i >= 0 && j < 10; i--, j++) {
                CustomTextLayout layout = mIndexArray.get(i);
                if (layout == null) {
                    j--;
                    continue;
                }
                l = new CustomTextLayout(layout.getWidth(), layout.getHeight(), layout.getPadding(), layout.getAlignment(), layout.getTextNode());
                Matrix m = new Matrix();
                m.set(layout.getMatrix());
                if (!empty) {
                    setMatrix(index, 1, m);
                }
                l.setMatrix(m);
                mAddArray.put(i, l);
            }
        }
        mArrays.put(index, mAddArray);
        mAdd = true;
    }

    /**
     *  绘制前面句子 empty是否为空
     */
    private void drawOld(CanvasObject canvas, float factor, int index, boolean empty) {
        if (index < 1) {
            return;
        }
        //判断是否为空 出现seek时候会出现
        if(mArrays.get(index - 1) == null) {
            mAddArray = new SparseArray<>();
            CustomTextLayout newTextLayout;
            Matrix m;
            for (int i = index - 1, k = 1; i >= 0 && k < 10; i--, k++) {
                TextNode textNode = mTextNodes.get(i);
                newTextLayout = new CustomTextLayout(mWidth, mHeight, new Rect(1, 1, 1, 1), TextLayout.Alignment.left, textNode);
                m = new Matrix();
                for (int j = i; j < index - 1; j++) {
                    //如果该句为空 就需要不执行这一步对应的动作
                    if (TextUtils.isEmpty(mTextNodes.get(j + 1).getText())) {
                        k--;
                        continue;
                    }
                    setMatrix(j + 1, 1, m);
                }
                newTextLayout.setMatrix(m);
                mAddArray.put(i, newTextLayout);
            }
            mArrays.put(index - 1, mAddArray);
        }
        mIndexArray = mArrays.get(index - 1);
//        Log.d("ok", empty + "===>要画" + mIndexArray.size() + "句-------- 当前第" + (index + 1) + "句");
        for (int i = 0; i < mIndexArray.size(); i++) {
            CustomTextLayout layout = mIndexArray.valueAt(i);
            canvas.save();
            if (layout.getMatrix() != null) {
                Matrix m = new Matrix();
                m.set(layout.getMatrix());
                if (!empty) {
                    setMatrix(index, factor, m);
                }
                canvas.setMatrix(m);
            } else if (!empty){
                matrix.reset();
                setMatrix(index, factor, matrix);
                canvas.setMatrix(matrix);
            }
            layout.draw(canvas, mStartX, mStartY);
            canvas.restore();
        }
    }

    //计算每一步的矩阵
    private void setMatrix(int i, float proportion, Matrix matrix ) {

        //顺序 放大、缩小、缩小、放大、缩小、放大、逆时针、放大、放大、缩小、缩小、放大、缩小、顺时针
        if (i % mInt == 0) {
            //上移放大
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 1.5f);
        } else if (i % mInt == 1) {
            //上移缩小
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 0.8f);
        } else if (i % mInt == 2) {
            //上移缩小
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 0.8f);
        } else if (i % mInt == 3) {
            //上移放大
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 2.2f);
        } else if (i % mInt == 4) {
            //上移缩小
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 0.8f);
        } else if (i % mInt == 5) {
            //上移放大
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 1.8f);
        } else if (i % mInt == 6) {
            //逆时针90
            postCounterClockwiseRotate(matrix, proportion);
        } else if (i % mInt == 7) {
            //上移放大
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 1.3f);
        } else if (i % mInt == 8) {
            //上移放大
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 1.8f);
        } else if (i % mInt == 9) {
            //上移缩小
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 0.8f);
        } else if (i % mInt == 10) {
            //上移缩小
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 0.5f);
        } else if (i % mInt == 11) {
            //上移放大
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 1.3f);
        } else if (i % mInt == 12) {
            //上移放大
            postUpTranslate(matrix, proportion);
            postScale(matrix, proportion, 2.0f);
        } else if (i % mInt == 13) {
            //顺时针90
            postClockwatchRotate(matrix, proportion);
        }

    }

}
