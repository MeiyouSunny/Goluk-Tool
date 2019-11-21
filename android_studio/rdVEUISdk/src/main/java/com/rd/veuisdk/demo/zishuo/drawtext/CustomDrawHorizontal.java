package com.rd.veuisdk.demo.zishuo.drawtext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.rd.vecore.models.CanvasObject;
import com.rd.veuisdk.R;
import com.rd.veuisdk.demo.zishuo.TextNode;

public class CustomDrawHorizontal extends CustomHandler {

    private Paint mStrokPaint;//描边画笔
    private float startX, startY, endX, endY;
    private TextNode mNode;//当前节点
    private float mFactor;//比例
    private String mText;//文字

    //初始化
    public CustomDrawHorizontal(Context context) {
        super(context);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(context.getResources().getDimensionPixelSize(
                R.dimen.text_size_18));
        mStrokPaint = new Paint();
        mStrokPaint.setTextSize(context.getResources().getDimensionPixelSize(
                R.dimen.text_size_18));
    }

    //绘制
    @Override
    public void onDraw(CanvasObject canvas, float currentProgress) {
        //判断当前第几句
        for (mIndex = 0; mIndex < mTextNodes.size(); mIndex++) {
            if (currentProgress >= mTextNodes.get(mIndex).getBegin() && currentProgress < mTextNodes.get(mIndex).getEnd()) {
                break;
            }
        }
        if (mIndex >= mTextNodes.size()) {
            return;
        }
        //判断是否变化
        if (mLastIndex != mIndex) {
            mLastIndex = mIndex;
            mNode = mTextNodes.get(mIndex);
            //判断当前是否为空 计算比例
            mText = mNode.getText();
            if (TextUtils.isEmpty(mText) && mIndex != 0) {
                while (mIndex > 0 && TextUtils.isEmpty(mText)) {
                    mIndex--;
                    mText = mTextNodes.get(mIndex).getText();
                }
                mFactor = 1;
                mNode = mTextNodes.get(mIndex);
            } else {
                if (mNode.getContinued() <= 0 || mNode.getContinued() > (mNode.getEnd() - mNode.getBegin())) {
                    mFactor = (currentProgress - mNode.getBegin()) / (mNode.getEnd() - mNode.getBegin());
                } else {
                    mFactor = (currentProgress - mNode.getBegin()) / mNode.getContinued();
                    mFactor = mFactor > 1? 1 : mFactor;
                }
            }
            //字体、颜色、阴影
            if(!TextUtils.isEmpty(mNode.getFont())) {
                mTextPaint.setTypeface(Typeface.createFromFile(mNode.getFont()));
            } else {
                mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT,
                        Typeface.NORMAL));
            }
            mTextPaint.setColor(Color.parseColor(mNode.getColor()));
            mTextPaint.setShadowLayer(mNode.getShadowAlpha() * 5, 3, 3, Color.parseColor(mNode.getStrokeColor()));
            //描边
            if (mNode.getStrokeWidth() != 0) {
                mStrokPaint.reset();
                mStrokPaint.setAntiAlias(true);
                mStrokPaint.setTypeface(mTextPaint.getTypeface());
                mStrokPaint.setFlags(mTextPaint.getFlags());
                mStrokPaint.setAlpha(mTextPaint.getAlpha());
                mStrokPaint.setTextSize(mTextPaint.getTextSize());

                mStrokPaint.setStyle(Paint.Style.STROKE);
                mStrokPaint.setColor(Color.parseColor(mNode.getStrokeColor()));
                mStrokPaint.setStrokeWidth(mNode.getStrokeWidth() * 2);
            }
            //计算位置
            mTextPaint.getTextBounds(mText, 0, mText.length(), mRect);
            float w = mTextPaint.measureText(mText);
            startX = (canvas.getWidth() - w) / 2;
            startY = (canvas.getHeight() - mRect.height()) / 2;
            endX = (canvas.getWidth() + w) / 2;
            endY = (canvas.getHeight() + mRect.height()) / 2;
        }  else {
            //没改变 判断上一句是否为空 计算比例
            if (!TextUtils.isEmpty(mTextNodes.get(mIndex).getText())) {
                //计算比例
                if (mNode.getContinued() <= 0 || mNode.getContinued() > (mNode.getEnd() - mNode.getBegin())) {
                    mFactor = (currentProgress - mNode.getBegin()) / (mNode.getEnd() - mNode.getBegin());
                } else {
                    mFactor = (currentProgress - mNode.getBegin()) / mNode.getContinued();
                    mFactor = mFactor > 1? 1 : mFactor;
                }
            } else {
                mFactor = 1;
            }
        }
        //绘制
        canvas.lockDrawText();
        canvas.clipRect(startX, startY + mTextPaint.getFontMetricsInt().ascent, endX * mFactor, endY);
        canvas.drawText(mText, startX, startY, mTextPaint);
        if (mNode.getStrokeWidth() != 0) {
            canvas.drawText(mText, startX, startY, mStrokPaint);
        }
        canvas.unLockDrawText();
    }

}
