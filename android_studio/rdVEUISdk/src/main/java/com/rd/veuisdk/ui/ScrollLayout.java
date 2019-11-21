package com.rd.veuisdk.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * @author JIAN
 * @create 2019/3/21
 * @Describe
 */
public class ScrollLayout extends RelativeLayout {

    private String TAG = "ScrollLayout";

    /**
     * @param defaultHeight
     */
    public void setDefaultHeight(float defaultHeight) {
        this.defaultHeight = defaultHeight;
        requestLayout();
    }

    /**
     * 相对于父容器的高度的比例
     */
    private float defaultHeight = 0.5f;

    /**
     * 是否与父容器一样大小
     *
     * @param enableFullParent true  match_parent;false 高度调整为 defaultHeight
     */
    public void setEnableFullParent(boolean enableFullParent) {
        this.enableFullParent = enableFullParent;
        requestLayout();
//        AnimationSet animationSet = new AnimationSet(true);
//        Animation translate;
//        if (enableFullParent) {
//            translate = new TranslateAnimation(0, 0, defaultHeight, 0f);
//        } else {
//            translate = new TranslateAnimation(0, 0, 0f, defaultHeight);
//        }
//        final int duration = 500;
//        translate.setDuration(duration);
//        animationSet.addAnimation(translate);
//        Animation anim = new AlphaAnimation(0.8f, 1);
//        anim.setDuration(duration);
//        animationSet.addAnimation(anim);
//        animationSet.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                //动画开始
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                //动画结束
//                clearAnimation();
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//                //动画重复
//            }
//        });
//        animationSet.setInterpolator(new LinearInterpolator());
//        animationSet.setDuration(duration);
//        this.startAnimation(animationSet);

    }

    public boolean isFullParent() {
        return enableFullParent;
    }

    private boolean enableFullParent = false;


    public ScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (enableFullParent) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int wPx = MeasureSpec.getSize(widthMeasureSpec);
            int hPx = MeasureSpec.getSize(heightMeasureSpec);
            hPx = (int) (defaultHeight * hPx);
            super.onMeasure(MeasureSpec.makeMeasureSpec(wPx,
                    MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(hPx,
                            MeasureSpec.EXACTLY));
        }
    }
}
