package com.rd.veuisdk.demo.zishuo.drawtext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.rd.vecore.models.CanvasObject;
import com.rd.veuisdk.R;
import com.rd.veuisdk.demo.zishuo.TextNode;

import java.util.ArrayList;

public class CustomDrawVertical extends CustomHandler {

    private Paint mStrokPaint;//描边画笔
    private float startX, startY, endX, endY;
    private TextNode mNode;
    private float mFactor;
    private String mText;
    private float h = 0, w = 0;

    //初始化
    public CustomDrawVertical(Context context) {
        super(context);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(context.getResources().getDimensionPixelSize(
                R.dimen.text_size_18));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mStrokPaint = new Paint();
        mStrokPaint.setTextSize(context.getResources().getDimensionPixelSize(
                R.dimen.text_size_18));
        mStrokPaint.setTextAlign(Paint.Align.CENTER);
        h = mTextPaint.getFontMetricsInt().descent - mTextPaint.getFontMetricsInt().ascent;
        w = mContext.getResources().getDimension(R.dimen.text_size_18);
    }

    //绘制
    @Override
    public void onDraw(CanvasObject canvas, float currentProgress) {
        for (mIndex = 0; mIndex < mTextNodes.size(); mIndex++) {
            if (currentProgress >= mTextNodes.get(mIndex).getBegin() && currentProgress < mTextNodes.get(mIndex).getEnd()) {
                break;
            }
        }
        if (mIndex >= mTextNodes.size()) {
            return;
        }
        //判断是否下一句
        if (mLastIndex != mIndex) {
            mLastIndex = mIndex;
            mNode = mTextNodes.get(mIndex);
            //计算比例
            if (mNode.getContinued() <= 0 || mNode.getContinued() > (mNode.getEnd() - mNode.getBegin())) {
                mFactor = (currentProgress - mNode.getBegin()) / (mNode.getEnd() - mNode.getBegin());
            } else {
                mFactor = (currentProgress - mNode.getBegin()) / mNode.getContinued();
                mFactor = mFactor > 1? 1 : mFactor;
            }
            mText = mNode.getText();
            //判断为空就画上一句
            if (TextUtils.isEmpty(mText)) {
                while (mIndex >= 0 && TextUtils.isEmpty(mText)) {
                    mIndex--;
                    mText = mTextNodes.get(mIndex).getText();
                }
                mFactor = 1;
                mNode = mTextNodes.get(mIndex);
            }
            startX = canvas.getWidth() / 2;
            startY = (canvas.getHeight() - h * mText.length()) / 2;
            endX = (canvas.getWidth() + w) / 2;
            endY = (canvas.getHeight() + h * mText.length()) / 2;
            getVerticalString(mText);
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
                mStrokPaint.setTextAlign(Paint.Align.CENTER);

                mStrokPaint.setStyle(Paint.Style.STROKE);
                mStrokPaint.setColor(Color.parseColor(mNode.getStrokeColor()));
                mStrokPaint.setStrokeWidth(mNode.getStrokeWidth() * 2);
            }
        } else {
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
        canvas.clipRect(startX - w / 2, startY + mTextPaint.getFontMetricsInt().ascent, endX, endY * mFactor);
        for (int i = 0; i < mTextVertical.size(); i++) {
            canvas.drawText(mTextVertical.get(i), startX, startY + h * i, mTextPaint);
            if (mNode.getStrokeWidth() != 0) {
                canvas.drawText(mTextVertical.get(i), startX, startY + h * i, mStrokPaint);
            }
        }
        canvas.unLockDrawText();
    }

    private ArrayList<String> mTextVertical = new ArrayList<>();

    private void getVerticalString(String s) {
        mTextVertical.clear();
        if (TextUtils.isEmpty(s)) {
            return;
        }
        for (int i = 0; i < s.length(); i++) {
            mTextVertical.add(s.substring(i, i + 1));
        }
    }

}
